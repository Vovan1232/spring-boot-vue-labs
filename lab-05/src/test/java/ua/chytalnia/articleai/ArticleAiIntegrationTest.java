package ua.chytalnia.articleai;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleAiIntegrationTest {
    private static final String CITATION = "Лозовик Ю.М. Розвиток штучного інтелекту в економіці. Київ, стор. 10-20.";
    private static final String FENCED_JSON = """
        ```json
        {"title_of_the_article": null, "year_of_publication": null, "extra_field": "ignored"}
        ```
        """;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final AtomicReference<JsonNode> REQUEST = new AtomicReference<>();
    private static final AtomicReference<Fixture> RESPONSE = new AtomicReference<>();
    private static final HttpServer PROVIDER = startProvider();

    @Autowired
    private MockMvc mvc;

    @DynamicPropertySource
    static void providerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.ai.openai.api-key", () -> "local-test-key");
        registry.add("spring.ai.openai.base-url", () -> "http://127.0.0.1:" + PROVIDER.getAddress().getPort());
    }

    @BeforeEach
    void resetFixture() {
        REQUEST.set(null);
        RESPONSE.set(new Fixture(200, """
            {
              "title_of_the_article": "Розвиток штучного інтелекту в економіці",
              "first_page": "10",
              "last_page": "20",
              "total_volume_of_printed_sheets": "11",
              "language": "ukr",
              "published_in_ukraine": "1"
            }
            """));
    }

    @AfterAll
    static void stopProvider() {
        PROVIDER.stop(0);
    }

    @Test
    void displaysFormAndExample() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Підставити приклад")))
            .andExpect(content().string(containsString("10.1234/example")));
    }

    @Test
    void sendsPromptToOpenAiModelAndRendersTheParsedArticle() throws Exception {
        mvc.perform(post("/generate-all").param("message", CITATION))
            .andExpect(status().isOk())
            .andExpect(model().attribute("fields", hasEntry("total_volume_of_printed_sheets", "11")))
            .andExpect(model().attribute("fields", hasEntry("published_in_ukraine", "1")))
            .andExpect(content().string(containsString("Структурований запис")))
            .andExpect(content().string(containsString("Час генерації")))
            .andExpect(content().string(containsString("Усі поля (37)")));

        JsonNode request = REQUEST.get();
        assertThat(request.path("model").asText()).isEqualTo("gpt-4o-mini");
        assertThat(request.path("response_format").path("type").asText()).isEqualTo("json_object");
        assertThat(request.path("messages").get(0).path("role").asText()).isEqualTo("system");
        assertThat(request.path("messages").get(0).path("content").asText())
            .contains("last_page − first_page + 1", "Category A", "Не вигадуй");
        assertThat(request.path("messages").get(1).path("content").asText()).isEqualTo(CITATION);
    }

    @Test
    void parsesFencedJsonAndNormalizesMissingAndNullValues() throws Exception {
        RESPONSE.set(new Fixture(200, FENCED_JSON));

        mvc.perform(post("/generate-all").param("message", "Неповне цитування {2025}"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("fields", hasEntry("title_of_the_article", "")))
            .andExpect(model().attribute("fields", hasEntry("year_of_publication", "0")))
            .andExpect(model().attribute("fields", hasEntry("published_abroad", "0")))
            .andExpect(model().attribute("fields", hasEntry("specialized", "Category A")))
            .andExpect(content().string(not(containsString("extra_field"))));
        assertThat(REQUEST.get().path("messages").get(1).path("content").asText())
            .isEqualTo("Неповне цитування {2025}");
    }

    @Test
    void reportsInvalidJson() throws Exception {
        RESPONSE.set(new Fixture(200, "{\"author_set\": \"Автор\"} unexpected text"));
        mvc.perform(post("/generate-all").param("message", CITATION))
            .andExpect(status().isBadGateway())
            .andExpect(content().string(containsString("некоректний JSON")));
    }

    @Test
    void reportsProviderErrorWithoutLeakingItsResponse() throws Exception {
        RESPONSE.set(new Fixture(401, "provider-only-message"));
        mvc.perform(post("/generate-all").param("message", CITATION))
            .andExpect(status().isBadGateway())
            .andExpect(content().string(containsString("Не вдалося отримати відповідь OpenAI")))
            .andExpect(content().string(not(containsString("provider-only-message"))));
    }

    @Test
    void rejectsBlankInputWithoutCallingProvider() throws Exception {
        mvc.perform(post("/generate-all").param("message", " \t "))
            .andExpect(status().isBadRequest());
        assertThat(REQUEST.get()).isNull();
    }

    private static HttpServer startProvider() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/v1/chat/completions", exchange -> {
                try (exchange) {
                    REQUEST.set(JSON.readTree(exchange.getRequestBody()));
                    Fixture fixture = RESPONSE.get();
                    Object payload = fixture.status() == 200
                        ? Map.of(
                            "id", "chatcmpl-test", "object", "chat.completion",
                            "model", "gpt-4o-mini", "created", 1700000000,
                            "choices", List.of(Map.of("index", 0, "finish_reason", "stop",
                                "message", Map.of("role", "assistant", "content", fixture.content()))))
                        : Map.of("error", Map.of("message", fixture.content()));
                    byte[] body = JSON.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(fixture.status(), body.length);
                    exchange.getResponseBody().write(body);
                }
            });
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start test provider", exception);
        }
    }

    private record Fixture(int status, String content) {}
}
