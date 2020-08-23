package org.kuroneko.restapiproject.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
