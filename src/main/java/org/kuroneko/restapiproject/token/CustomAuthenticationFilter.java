package org.kuroneko.restapiproject.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.kuroneko.restapiproject.account.AccountRepository;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.LoginForm;
import org.kuroneko.restapiproject.exception.InputNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountVORepository accountVORepository;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authRequest;

        String username = obtainUsername(request);
        if (username == null) {
            username = "";
        }
        String password = obtainPassword(request);
        if (password == null) {
            password = "";
        }

        try{
            LoginForm loginForm = new ObjectMapper().readValue(request.getInputStream(), LoginForm.class);
            AccountVO accountVO = this.accountRepository.findByEmail(loginForm.getEmail()).map(account ->
                    new AccountVO(account.getEmail(), account.getPassword(), account.getAuthority()))
                    .orElseThrow(() -> new UsernameNotFoundException(loginForm.getEmail()));
            AccountVO save = this.accountVORepository.save(accountVO);
            authRequest = new UsernamePasswordAuthenticationToken(accountVO.getEmail(), accountVO.getPassword());
        } catch (IOException e) {
            throw new InputNotFoundException(e);
        }

        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
