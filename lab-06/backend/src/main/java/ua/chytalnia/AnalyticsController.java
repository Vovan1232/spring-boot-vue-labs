package ua.chytalnia;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final ArticleRepository articles;

    AnalyticsController(ArticleRepository articles) {
        this.articles = articles;
    }

    @GetMapping
    Statistics statistics() {
        List<Count> categories = new ArrayList<>();
        List<Count> languages = new ArrayList<>();
        for (ArticleRepository.AnalyticsCount row : articles.analyticsCounts()) {
            List<Count> group = row.getDimension().equals("category") ? categories : languages;
            group.add(new Count(row.getLabel(), row.getAmount()));
        }
        long total = categories.stream().mapToLong(Count::count).sum();
        return new Statistics(total, categories, languages);
    }

    record Statistics(long total, List<Count> categories, List<Count> languages) {}
    record Count(String label, long count) {}
}
