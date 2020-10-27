package org.kuroneko.restapiproject.config;

import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.kuroneko.restapiproject.token.AccountDetailsService;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.AccountService;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.token.AccountVO;
import org.kuroneko.restapiproject.token.AccountVORepository;
import org.kuroneko.restapiproject.token.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.transaction.annotation.Transactional;

import java.security.Security;
import java.util.Collections;

@TestConfiguration
public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountVORepository accountVORepository;

    @Override
    @Transactional
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String withAccountEmail = withAccount.value();

        Account account = new Account();
        account.setEmail(withAccountEmail);
        account.setPassword("1234567890");
        account.setUsername("테스트1");
        accountService.createNewAccount(account);
        account.setAuthority(UserAuthority.ROOT);
        AccountVO accountVO = this.accountVORepository.findByEmail(withAccountEmail).orElseThrow();
        accountVO.setAuthority(UserAuthority.ROOT);
//        User principal = (User) accountDetailsService.loadUserByUsername(accountVO.getEmail());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(accountVO, accountVO.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(accountVO.getAuthority().toString())));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);

        return context;
    }
}
