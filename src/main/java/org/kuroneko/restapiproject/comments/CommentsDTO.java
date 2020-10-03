package org.kuroneko.restapiproject.comments;

import lombok.Data;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
public class CommentsDTO extends RepresentationModel<ArticleDTO> {

    private Long number;

    private String description;

    private LocalDateTime createTime;

    private int agree;

    private int disagree;

    private int originNo; //checking sub comments

    private int groupOrd; // 댓글 - 0 , 댓답글 - 1

    private Long articleId;

    private Long articleNumber;
}
