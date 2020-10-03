package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.article.domain.Article;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ArticleRepositoryExtension {
    List<Article> findByNumber(List<Long> list);
}
