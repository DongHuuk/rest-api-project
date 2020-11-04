package org.kuroneko.restapiproject.token;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/*
    CustomUsernamePasswordAuthenticationFilter 수행 후 적용 될 Handler
 */
@Log4j2
@Component
public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Autowired
    private AccountVORepository accountVORepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        AccountVO accountVO = (AccountVO) authentication.getPrincipal();
        String token = TokenUtils.generateJwtToken(this.accountVORepository.findByEmail(accountVO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(accountVO.getEmail())));

//        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader(AuthConstants.AUTH_HEADER, AuthConstants.TOKEN_TYPE + " " + token);
    }
}
