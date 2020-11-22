package org.kuroneko.restapiproject.article.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.comments.domain.Comments;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.token.AccountVO;

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

    @Lob
    private String description;

    private String source;

    @Enumerated(EnumType.STRING)
    private ArticleThema division;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "article", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Comments> comments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    private Account account;

    @ManyToOne
    @JsonBackReference
    private Community community;

    @ManyToMany
    @JsonBackReference
    private List<AccountVO> agreeList = new ArrayList<>();

    @ManyToMany
    @JsonBackReference
    private List<AccountVO> disagreeList = new ArrayList<>();

    //추천
    private int agree;
    //비추천
    private int disagree;

    private int report;

    public void setComments(Comments comments) {
        this.comments.add(comments);
        if (comments.getArticle() == null) {
            comments.setArticle(this);
        }
    }

}
