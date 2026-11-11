package ua.chytalnia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 80)
    private String author;

    @Column(nullable = false, length = 1000)
    private String summary;

    protected Article() {}

    Article(ArticleInput input) {
        update(input);
    }

    void update(ArticleInput input) {
        title = input.title().strip();
        author = input.author().strip();
        summary = input.summary().strip();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getSummary() { return summary; }
}
