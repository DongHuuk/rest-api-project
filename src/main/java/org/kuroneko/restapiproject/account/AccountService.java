package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.AccountForm;
import org.kuroneko.restapiproject.domain.UserAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account createNewAccount(Account account) {
        account.setCreateTime(LocalDateTime.now());
        account.setAuthority(UserAuthority.USER);

        return accountRepository.save(account);
    }

    public Errors checkAccountEmailAndUsername(AccountForm accountForm, Errors errors) {
        Optional<Account> byEmail = accountRepository.findByEmail(accountForm.getEmail());
        String username = accountForm.getUsername();

        List<String> filter = accountRepository.findAll().stream()
                .map(Account::getUsername)
                .filter(a -> a.equals(username))
                .collect(Collectors.toList());

        if (!filter.isEmpty()) {
            errors.rejectValue("username", "wrong.username", "duplicate username. check please");
        }

        if (byEmail.isPresent()) {
            errors.rejectValue("email", "wrong.email", "duplicate Email. check please");
        }
        return errors;
    }

    public Errors checkUpdateAccount(AccountForm accountForm, Errors errors, Account account) {
        String username = accountForm.getUsername();

        List<String> filter = accountRepository.findAll().stream()
                .map(Account::getUsername)
                .filter(a -> !a.equals(account.getUsername()) && a.equals(username))
                .collect(Collectors.toList());

        if (!filter.isEmpty()) {
            errors.rejectValue("username", "wrong.username", "duplicate username. check please");
        }

        return errors;
    }

    public Account updateAccount(Account account) {
        account.setUpdateTime(LocalDateTime.now());
        return accountRepository.save(account);
    }
}
