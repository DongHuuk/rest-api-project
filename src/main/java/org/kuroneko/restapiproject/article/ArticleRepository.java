package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByNumber(Long valueOf);
}
