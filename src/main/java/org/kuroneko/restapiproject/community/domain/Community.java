package org.kuroneko.restapiproject.community.domain;

import lombok.*;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.article.domain.Article;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class Community {

    @Id
    private String id;

    private String title;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "community", fetch = FetchType.LAZY)
    private Set<Article> article;

    @ManyToOne
    private Account manager;

}
