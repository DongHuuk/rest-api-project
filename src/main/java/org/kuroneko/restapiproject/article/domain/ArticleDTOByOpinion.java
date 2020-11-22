package org.kuroneko.restapiproject.article.domain;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDTOByOpinion {
    private Long id;
    private Long number;
    private int agree;
    private int disagree;
}
