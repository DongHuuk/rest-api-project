package org.kuroneko.restapiproject.article.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.community.domain.Community;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class Article {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private Long number;

    private String title;

    private String description;

    private String source;

    @Enumerated(EnumType.STRING)
    private ArticleThema division;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @OneToMany(fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Comments> comments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    private Account account;

    @ManyToOne
    @JsonBackReference
    private Community community;

    private int report;

    public void setComments(Comments comments) {
        this.comments.add(comments);
        if (comments.getArticle() == null) {
            comments.setArticle(this);
        }
    }

}
