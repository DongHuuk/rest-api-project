package org.kuroneko.restapiproject.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    @GeneratedValue
    private int number;

    private String title;

    private String description;

    private String source;

    @Enumerated(EnumType.STRING)
    private ArticleThema division;

    private LocalDateTime createTime;

    @OneToMany
    private Set<Comments> comments;

    private int report;

}
