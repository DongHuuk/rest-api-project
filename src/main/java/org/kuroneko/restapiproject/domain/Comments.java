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
public class Comments {

    @Id
    @GeneratedValue
    private Long id;

    private String description;

    private LocalDateTime createTime;

    private int agree;

    private int disagree;

    private int report;

    private int originNo; //checking sub comments

    private int groupOrd;

    @ManyToOne
    private Article article;

}
