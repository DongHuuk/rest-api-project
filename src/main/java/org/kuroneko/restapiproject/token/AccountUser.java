package org.kuroneko.restapiproject.token;

import org.kuroneko.restapiproject.account.domain.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class AccountUser extends User {
    private AccountVO account;

    public AccountUser(AccountVO account) {
        super(account.getEmail(),account.getPassword() , List.of(new SimpleGrantedAuthority("ROLE_" + account.getAuthority().toString())));
        this.account = account;
    }

    public AccountVO getAccount() {
        return account;
    }
}
