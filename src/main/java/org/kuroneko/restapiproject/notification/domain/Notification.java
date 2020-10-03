package org.kuroneko.restapiproject.notification.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.comments.domain.Comments;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    private Long id;

    private Long number;

    private LocalDateTime createTime;

    private boolean checked;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    private Account account;

    @ManyToOne
    private Article article;

    @ManyToOne
    private Comments comments;
}
