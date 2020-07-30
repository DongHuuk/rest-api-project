package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.domain.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class AccountUser extends User {
    private Account account;

    public AccountUser(Account account) {
        super(account.getEmail(),account.getPassword() , List.of(new SimpleGrantedAuthority("ROLE_" + account.getAuthority().toString())));
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
}
