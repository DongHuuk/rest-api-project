package org.kuroneko.restapiproject.article;

import lombok.NoArgsConstructor;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.community.CommunityController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

@NoArgsConstructor
public class ArticleResource extends EntityModel<Article> {
    public ArticleResource(Article article, Link... links) {
        super(article, links);
    }
}
