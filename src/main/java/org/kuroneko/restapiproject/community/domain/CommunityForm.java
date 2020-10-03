package org.kuroneko.restapiproject.community.domain;

import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.article.domain.Article;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.Set;

public class CommunityForm {

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
