package org.kuroneko.restapiproject.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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

    @ManyToOne
    private Account account;

    @ManyToOne
    private Article article;

    @ManyToOne
    private Comments comments;

}
