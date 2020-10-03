package org.kuroneko.restapiproject.community.validation;

import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CreateValidation implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(CommunityForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

    }
}
