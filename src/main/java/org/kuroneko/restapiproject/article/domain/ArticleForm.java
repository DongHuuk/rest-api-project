package org.kuroneko.restapiproject.article.domain;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class ArticleForm {

    @NotEmpty
    @Length(max = 50)
    private String title;
    @NotEmpty
    private String description;

    private String source;

    private int division;

}
