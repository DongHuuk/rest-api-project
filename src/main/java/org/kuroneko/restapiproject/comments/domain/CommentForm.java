package org.kuroneko.restapiproject.comments.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CommentForm {

    @NotEmpty
    private String description;

    private boolean originNo; //checking sub comments

}
