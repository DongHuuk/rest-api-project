package org.kuroneko.restapiproject.token;

import org.kuroneko.restapiproject.account.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Component
public class AccountDetailsService implements UserDetailsService {
    @Autowired
    private AccountVORepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<AccountVO> byEmail = accountRepository.findByEmail(email);
        if (byEmail.isEmpty()) {
            throw new UsernameNotFoundException(email);
        }
        AccountVO accountVO = byEmail.get();
        return AccountUser.builder()
                .username(accountVO.getEmail())
                .password(accountVO.getPassword())
                .roles(accountVO.getAuthority().toString())
                .build();
    }
}
