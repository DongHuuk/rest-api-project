package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.account.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountDetailsService implements UserDetailsService {
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Account> byEmail = this.accountRepository.findByEmail(email);

        if (byEmail.isEmpty()) {
            throw new UsernameNotFoundException(email);
        }

        return new AccountUser(byEmail.get());
    }
}
