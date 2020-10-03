package org.kuroneko.restapiproject.comments.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.article.domain.Article;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class Comments {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private Long number;

    private String description;

    private LocalDateTime createTime;

    //추천
    private int agree;
    //비추천
    private int disagree;
    //신고하기
    private int report;

    private int originNo; //checking sub comments

    private int groupOrd; // 댓글 - 0 , 댓답글 - 1

    @ManyToOne
    @JsonBackReference
    private Article article;

    @ManyToOne
    @JsonBackReference
    private Account account;

}
