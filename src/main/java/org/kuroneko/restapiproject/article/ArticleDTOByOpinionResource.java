package org.kuroneko.restapiproject.article;

import lombok.NoArgsConstructor;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.kuroneko.restapiproject.article.domain.ArticleDTOByOpinion;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

@NoArgsConstructor
public class ArticleDTOByOpinionResource extends EntityModel<ArticleDTOByOpinion> {
    public ArticleDTOByOpinionResource(ArticleDTOByOpinion resource, Link... links) {
        super(resource, links);
    }
}
