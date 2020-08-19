package org.kuroneko.restapiproject.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @OneToMany
    private Set<Comments> comments = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    private Account account;

    private int report;

    public void setComments(Comments comments) {
        this.comments.add(comments);
        if (comments.getArticle() == null) {
            comments.setArticle(this);
        }
    }

}
