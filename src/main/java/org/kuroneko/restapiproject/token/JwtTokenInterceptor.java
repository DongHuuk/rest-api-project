package org.kuroneko.restapiproject.token;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Log4j2
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader(AuthConstants.AUTH_HEADER);
        String method = request.getMethod();
        String path = request.getPathInfo();
        String[] split;

        if (header != null) {
//            String token = TokenUtils.getTokenFromHeader(header);
            if (TokenUtils.isValidToken(header)) {
                return true;
            }
        }

        if (path != null) {
            split = path.split("/");
        }else{
            path = request.getServletPath();
            split = path.split("/");
        }


        if (method.equalsIgnoreCase("get") && path.contains("/community/")
                && split.length == 3) {
            try {
                int i = Integer.parseInt(split[2]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (method.equalsIgnoreCase("get") && path.equals("/community")) {
            return true;
        } else if(method.equalsIgnoreCase("get") && path.contains("/community") && path.contains("/article")
            && split.length == 5){
            return true;
        }
        else if (path.equals("/accounts") && (method.equalsIgnoreCase("post") || method.equalsIgnoreCase("get"))) {
            return true;
        }

        response.sendRedirect("/error/unauthorized");
        return false;
    }
}
