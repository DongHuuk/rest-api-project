package org.kuroneko.restapiproject.account.validation;

import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.stream.Collectors;

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
        String username = accountForm.getUsername();
        List<String> filter = accountRepository.findAll().stream()
                .map(Account::getUsername)
                .filter(a -> a.equals(username))
                .collect(Collectors.toList());

        if (!filter.isEmpty()) {
            errors.rejectValue("username", "wrong.username", "duplicate username. check please");
        }

        if (!accountForm.checkedPassword()) {
            errors.rejectValue("password", "wrong.password", "not matching password and checkingPassword");
        }

    }
}
