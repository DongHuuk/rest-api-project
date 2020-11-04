package org.kuroneko.restapiproject.article;

import lombok.NoArgsConstructor;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

@NoArgsConstructor
public class ArticleDTOResource extends EntityModel<ArticleDTO> {
    public ArticleDTOResource(ArticleDTO articleDTO, Link... links) {
        super(articleDTO, links);
    }
}
