package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.domain.Article;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ArticleRepositoryExtension {
    List<Article> findByNumber(List<Long> list);
}
