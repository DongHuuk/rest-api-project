package org.kuroneko.restapiproject.token;

import io.jsonwebtoken.*;
import lombok.extern.log4j.Log4j2;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.UserAuthority;
import org.springframework.context.annotation.Profile;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Log4j2
public class TokenUtils {

    private static final String securityKey = "Is_A_SecurityKeyForJwtTokenInCommunitySiteProject";

    public static String generateJwtToken(AccountVO accountVO) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(accountVO.getEmail()) // 제목
                .setHeader(createHeader())
                .setClaims(createClaims(accountVO))
                .setExpiration(createExpireDateForOneYear()) // 유효기간
                .signWith(SignatureAlgorithm.HS256, createSigningKey()); // 암호화 방식

        return builder.compact();
    }

    private static Key createSigningKey() {
        byte[] apiKeySecurityBytes = DatatypeConverter.parseBase64Binary(securityKey);
        return new SecretKeySpec(apiKeySecurityBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    private static Map<String, Object> createClaims(AccountVO accountVO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", accountVO.getEmail());
        claims.put("role", accountVO.getAuthority());

        return claims;
    }

    private static Date createExpireDateForOneYear() {
        //token 만료일 7일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 7);

        return calendar.getTime();
    }

    public static boolean isValidToken(String token) {
        try {
            Claims claims = getClaimsFormToken(token);

            log.info("expireTime : " + claims.getExpiration());
            log.info("email : " + claims.get("email"));
            log.info("role : " + claims.get("role"));
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token Expired");
            return false;
        } catch (JwtException e) {
            log.error("Token Tampered");
            return false;
        } catch (NullPointerException e) {
            log.error("Token is Null");
            return false;
        }
    }

    public static Claims getClaimsFormToken(String token) {
        return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(securityKey))
                .parseClaimsJws(token).getBody();
    }

    public static String getTokenFromHeader(String header) {
        return header.split(" ")[1];
    }

    private static Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        header.put("regDate", System.currentTimeMillis());

        return header;
    }

    private static String getUserEmailFromToken(String token) {
        Claims claims = getClaimsFormToken(token);
        return (String) claims.get("email");
    }

    private static UserAuthority getRoleFormToken(String token) {
        Claims claims = getClaimsFormToken(token);
        return (UserAuthority) claims.get("role");
    }
}
