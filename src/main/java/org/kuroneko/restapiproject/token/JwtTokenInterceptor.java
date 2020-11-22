package org.kuroneko.restapiproject.token;

import io.jsonwebtoken.Claims;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

@Log4j2
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private AccountVORepository accountVORepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader(AuthConstants.AUTH_HEADER);
        String method = request.getMethod();
        String path = request.getPathInfo();
        String[] split;
        if (path != null) {
            split = path.split("/");
        }else{
            path = request.getServletPath();
            split = path.split("/");
        }

        if (header != null) {
//            String token = TokenUtils.getTokenFromHeader(header);
            if (TokenUtils.isValidToken(header)) {
                Claims claims = TokenUtils.getClaimsFormToken(header);
                String email = claims.get("email").toString();
                AccountVO accountVO = this.accountVORepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(this.getClass() + " is not Found Username - " + email));

                UsernamePasswordAuthenticationToken newToken = new UsernamePasswordAuthenticationToken(accountVO, null,
                        Collections.singleton(new SimpleGrantedAuthority(accountVO.getAuthority().toString())));

                SecurityContextHolder.getContext().setAuthentication(newToken);

                return true;
            }
        }

        if (method.equalsIgnoreCase("get") && path.contains("/community/")
                && split.length == 3) {
            try {
                int i = Integer.parseInt(split[2]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if(method.equalsIgnoreCase("get") && path.contains("/best")){
            return true;
        } else if (method.equalsIgnoreCase("get") && path.equals("/community")) {
            return true;
        } else if(method.equalsIgnoreCase("get") && path.contains("/community") && path.contains("/article")
            && split.length == 5){
            return true;
        } else if (path.equals("/accounts") && (method.equalsIgnoreCase("post") || method.equalsIgnoreCase("get"))) {
            return true;
        } else if (method.equalsIgnoreCase("options")) {
            log.info("request method options is true");
            return true;
        }

        response.sendRedirect("/error/unauthorized");
        return false;
    }
}
