package org.kuroneko.restapiproject.article.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/*
    index화면에서 보여줄 값
 */

@Data
public class ArticleMiniDTO {

    private Long articleId;

    private String articleTitle;

    private int commentsCount;
}
