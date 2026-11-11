package ua.chytalnia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query(value = """
        SELECT 'category' AS dimension, category AS label, count(*) AS amount
        FROM articles GROUP BY category
        UNION ALL
        SELECT 'language' AS dimension, language AS label, count(*) AS amount
        FROM articles GROUP BY language
        ORDER BY dimension, amount DESC, label
        """, nativeQuery = true)
    List<AnalyticsCount> analyticsCounts();

    interface AnalyticsCount {
        String getDimension();
        String getLabel();
        long getAmount();
    }
}
