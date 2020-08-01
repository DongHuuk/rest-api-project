package org.kuroneko.restapiproject.article;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ArticleForm {

    @NotEmpty
    private String title;
    @NotEmpty
    private String description;

    private String source;

    private int division;

}
