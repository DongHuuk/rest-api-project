package org.kuroneko.restapiproject.errors;

import lombok.NoArgsConstructor;
import org.kuroneko.restapiproject.main.MainController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.validation.Errors;

@NoArgsConstructor
public class ErrorsResource extends EntityModel<Errors> {
    private Errors errors;

    public ErrorsResource(Errors errors) {
        this.errors = errors;
        add(WebMvcLinkBuilder.linkTo(MainController.class).withRel("index"));
    }

    public Errors getErrors() {
        return errors;
    }
}
