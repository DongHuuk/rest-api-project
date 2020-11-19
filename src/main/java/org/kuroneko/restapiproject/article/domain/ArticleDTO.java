package org.kuroneko.restapiproject.article.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.kuroneko.restapiproject.comments.CommentsDTO;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO extends RepresentationModel<ArticleDTO> {

    private Long id;

    private Long number;

    private String title;

    private String description;

    private String source;

    private ArticleThema division;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<CommentsDTO> comments = new ArrayList<>();

    private int report;

    private String communityTitle;

    private Long accountId;

    private String userName;

    private String userEmail;

    private String authority;

}
