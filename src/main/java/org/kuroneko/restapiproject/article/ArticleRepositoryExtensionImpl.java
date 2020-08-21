package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.domain.Article;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class ArticleRepositoryExtensionImpl extends QuerydslRepositorySupport implements ArticleRepositoryExtension {

    public ArticleRepositoryExtensionImpl() {
        super(Article.class);
    }

}
