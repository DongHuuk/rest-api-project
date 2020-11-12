package org.kuroneko.restapiproject.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.kuroneko.restapiproject.account.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;

@Log4j2
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Resource(name = "accountDetailsService")
    private UserDetailsService userDetailsService;
    @Autowired
    private AccountVORepository accountVORepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //authentication -> Form으로 입력받은 값, Filter에서 넘겨준 값
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        String userName = token.getName();
        String password = (String) token.getCredentials();

        //DB 참조,
        User accountUser = (User) this.userDetailsService.loadUserByUsername(userName);

        //Form값과 DB값을 비교
        if (!userName.equalsIgnoreCase(accountUser.getUsername())) {
            throw new BadCredentialsException(accountUser.getUsername() + "Invalid email");
        }

        if (!this.passwordEncoder.matches(password, accountUser.getPassword())) {
            throw new BadCredentialsException(accountUser.getUsername() + "Invalid password");
        }

        //값의 체크가 정상적으로 이루어 졌다면, 객체로 Principal로써 등록하기 위해 조회 및 token 새로 추가 후 등록
        AccountVO accountVO = this.accountVORepository.findByEmail(accountUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(userName));

        UsernamePasswordAuthenticationToken newToken = new UsernamePasswordAuthenticationToken(accountVO, password,
                Collections.singleton(new SimpleGrantedAuthority(accountVO.getAuthority().toString())));

//        SecurityContextHolder.getContext().setAuthentication(newToken);
        newToken.setDetails(authentication.getDetails());

        return newToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
