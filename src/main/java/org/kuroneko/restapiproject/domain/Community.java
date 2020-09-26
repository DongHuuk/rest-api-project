package org.kuroneko.restapiproject.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
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
