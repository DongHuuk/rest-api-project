package org.kuroneko.restapiproject.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class AccountDetailsVO implements UserDetails {

    private AccountVO account;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return account.getIsEnable();
    }

    @Override
    public boolean isAccountNonLocked() {
        return account.getIsEnable();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return account.getIsEnable();
    }

    @Override
    public boolean isEnabled() {
        return account.getIsEnable();
    }
}
