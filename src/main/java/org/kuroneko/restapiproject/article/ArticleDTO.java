package org.kuroneko.restapiproject.article;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.ArticleThema;
import org.kuroneko.restapiproject.domain.Comments;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ArticleDTO {

    private Long number;

    private String title;

    private String description;

    private String source;

    private ArticleThema division;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Set<Comments> comments;

    private Long accountId;

    private String userName;

    private String userEmail;

    private String authority;

}
