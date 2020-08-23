package org.kuroneko.restapiproject.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties("password")
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    private String username;

    private String email;

    private String password;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Enumerated(EnumType.STRING)
    private UserAuthority authority;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Article> article = new HashSet<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Comments> comments = new HashSet<>();

    @OneToMany(mappedBy = "account")
    @JsonManagedReference
    private Set<Notification> notification = new HashSet<>();

    public void setArticle(Article article){
        this.article.add(article);
        if (article.getAccount() == null) {
            article.setAccount(this);
        }
    }

    public void setComments(Comments comments) {
        this.comments.add(comments);
        if (comments.getAccount() == null) {
            comments.setAccount(this);
        }
    }

}
