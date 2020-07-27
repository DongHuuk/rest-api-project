package org.kuroneko.restapiproject.account.validation;

import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.AccountResource;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
public class AccountValidation implements Validator {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(AccountForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AccountForm accountForm = (AccountForm) o;
        Optional<Account> byEmail = accountRepository.findByEmail(accountForm.getEmail());

        if (!accountForm.checkedPassword()) {
            errors.rejectValue("password", "wrong.password", "not matching password and checkingPassword");
        }
        if (byEmail.isPresent()) {
            errors.rejectValue("email", "duplicate.email", "duplicated Email");
        }

    }
}
