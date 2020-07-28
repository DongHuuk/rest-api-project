package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.kuroneko.restapiproject.domain.UserAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account createNewAccount(Account account) {
        account.setCreateTime(LocalDateTime.now());
        account.setAuthority(UserAuthority.USER);

        return accountRepository.save(account);
    }

    public Errors checkAccountEmail(AccountForm accountForm, Errors errors) {
        Optional<Account> byEmail = accountRepository.findByEmail(accountForm.getEmail());
        if (byEmail.isPresent()) {
            errors.rejectValue("email", "wrong.email", "duplicate Email. check please");
        }
        return errors;
    }

    public Account updateAccount(Account account) {
        account.setUpdateTime(LocalDateTime.now());
        return accountRepository.save(account);
    }
}
