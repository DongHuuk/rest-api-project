package org.kuroneko.restapiproject.account.validation;

import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.AccountPasswordForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class AccountPasswordValidation implements Validator {
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(AccountPasswordForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AccountPasswordForm accountPasswordForm = (AccountPasswordForm) o;

        if (!accountPasswordForm.checkedPassword()) {
            errors.rejectValue("password", "wrong.password", "not matching password and checkingPassword");
        }

    }
}
