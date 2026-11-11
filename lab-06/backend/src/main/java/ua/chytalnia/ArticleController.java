package ua.chytalnia;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleRepository articles;

    ArticleController(ArticleRepository articles) {
        this.articles = articles;
    }

    @GetMapping
    List<Article> list() {
        return articles.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @GetMapping("/{id}")
    Article get(@PathVariable long id) {
        return find(id);
    }

    @PostMapping
    ResponseEntity<Article> create(@Valid @RequestBody ArticleInput input) {
        Article article = articles.save(new Article(input));
        return ResponseEntity.created(URI.create("/api/articles/" + article.getId())).body(article);
    }

    @PutMapping("/{id}")
    @Transactional
    Article update(@PathVariable long id, @Valid @RequestBody ArticleInput input) {
        Article article = find(id);
        article.update(input);
        return article;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void delete(@PathVariable long id) {
        articles.delete(find(id));
    }

    private Article find(long id) {
        return articles.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Статтю не знайдено."));
    }
}
