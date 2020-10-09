package org.kuroneko.restapiproject.community.validation;

import org.kuroneko.restapiproject.article.domain.ArticleForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ArticleValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return ArticleForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ArticleForm articleForm = (ArticleForm) target;

        if (articleForm.getDivision() >= 3) {
            errors.rejectValue("division", "wrong.division", "division is not range");
        }
    }
}
