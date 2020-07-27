package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.domain.Account;
import org.kuroneko.restapiproject.domain.UserAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account createNewAccount(Account account) {
        account.setCreateTime(LocalDateTime.now());
        account.setAuthority(UserAuthority.USER);

        return accountRepository.save(account);
    }
}
