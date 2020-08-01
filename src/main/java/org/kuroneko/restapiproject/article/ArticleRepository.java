package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
