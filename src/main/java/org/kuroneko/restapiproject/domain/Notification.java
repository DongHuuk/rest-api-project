package org.kuroneko.restapiproject.domain;

import lombok.*;

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

    private LocalDateTime createTime;

    private boolean checked;

    @ManyToOne
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comments comments;
}
