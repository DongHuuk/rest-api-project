package org.kuroneko.restapiproject.comments;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kuroneko.restapiproject.article.domain.ArticleDTO;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper=false)
public class CommentsDTO extends RepresentationModel<ArticleDTO> {

    private Long number;

    private String description;

    private LocalDateTime createTime;

    private int agree;

    private int disagree;

    private int report;

    private int originNo; //checking sub comments

    private int groupOrd; // 댓글 - 0 , 댓답글 - 1
    
    //TODO 답글을 요청할 컨트롤러 처리 작성 필요 추후
    private Long commentNumber; // 댓답글의 경우 어떤 댓글에서의 답글인지

    private Long accountId;

    private String accountUsername;

    private Long articleId;

    private Long articleNumber;
}
