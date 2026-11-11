package ua.chytalnia.articleai;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
public class ChatControllerApi {
    private static final int MAX_INPUT_LENGTH = 6000;
    private static final String EXAMPLE = """
        Лозовик Ю.М. Розвиток штучного інтелекту в економіці. Журнал "Цифрова Економіка", Том 5, Випуск 2, 2025 рік, Київ, стор. 10-20. DOI: 10.1234/example.""";
    private static final Pattern JSON_FENCE = Pattern.compile(
        "\\A```(?:json)?[ \\t]*\\R?(.*?)\\R?[ \\t]*```\\z",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final TypeReference<Map<String, String>> RESULT_FIELDS = new TypeReference<>() {};

    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final SystemMessage instructions;
    private final PromptTemplate inputTemplate = new PromptTemplate("{message}");
    private final boolean apiKeyConfigured;

    public ChatControllerApi(
        OpenAiChatModel chatModel,
        ObjectMapper objectMapper,
        @Value("classpath:article-prompt.txt") Resource promptResource,
        @Value("${spring.ai.openai.api-key}") String apiKey
    ) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.instructions = new SystemMessage(promptResource);
        this.apiKeyConfigured = !apiKey.isBlank() && !apiKey.equals("local-no-key");
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("example", EXAMPLE);
        model.addAttribute("message", "");
        model.addAttribute("apiKeyConfigured", apiKeyConfigured);
        return "index";
    }

    @PostMapping("/generate-all")
    public ModelAndView generateAll(@RequestParam(defaultValue = "") String message) {
        long started = System.nanoTime();
        if (message.isBlank() || message.length() > MAX_INPUT_LENGTH) {
            return failure(message, started, HttpStatus.BAD_REQUEST,
                "Введіть текст цитування довжиною від 1 до 6000 символів.");
        }
        if (!apiKeyConfigured) {
            return failure(message, started, HttpStatus.SERVICE_UNAVAILABLE,
                "Задайте OPENAI_API_KEY перед запуском застосунку.");
        }

        Prompt prompt = new Prompt(List.of(
            instructions,
            inputTemplate.createMessage(Map.of("message", message.strip()))
        ));
        try {
            ChatResponse response = chatModel.call(prompt);
            if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
                return failure(message, started, HttpStatus.BAD_GATEWAY, "Модель повернула порожню відповідь.");
            }
            SaveArticleToEntity article = parse(response.getResult().getOutput().getText());
            Map<String, String> fields = objectMapper.convertValue(article, RESULT_FIELDS);
            return view(message, started, HttpStatus.OK)
                .addObject("article", article)
                .addObject("fields", fields);
        } catch (IOException exception) {
            return failure(message, started, HttpStatus.BAD_GATEWAY,
                "Модель повернула некоректний JSON. Спробуйте згенерувати результат ще раз.");
        } catch (RuntimeException exception) {
            log.warn("OpenAI request failed ({})", exception.getClass().getSimpleName());
            return failure(message, started, HttpStatus.BAD_GATEWAY,
                "Не вдалося отримати відповідь OpenAI. Перевірте доступ до API та повторіть спробу.");
        }
    }

    private SaveArticleToEntity parse(String output) throws IOException {
        if (output == null || output.isBlank()) {
            throw new IOException("Empty model response");
        }
        String json = output.strip();
        Matcher fence = JSON_FENCE.matcher(json);
        if (fence.matches()) {
            json = fence.group(1).strip();
        }
        if (!json.startsWith("{")) {
            throw new IOException("Expected a JSON object");
        }
        return objectMapper.readerFor(SaveArticleToEntity.class)
            .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .readValue(json);
    }

    private ModelAndView failure(String message, long started, HttpStatus status, String error) {
        return view(message, started, status).addObject("error", error);
    }

    private ModelAndView view(String message, long started, HttpStatus status) {
        return new ModelAndView("index", status)
            .addObject("example", EXAMPLE)
            .addObject("apiKeyConfigured", apiKeyConfigured)
            .addObject("message", message)
            .addObject("elapsedMs", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
    }
}
