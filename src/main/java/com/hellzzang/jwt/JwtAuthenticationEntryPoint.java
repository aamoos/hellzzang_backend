package com.hellzzang.jwt;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
* @package : com.example.jwt.jwt
* @name : JwtAuthenticationEntryPoint.java
* @date : 2023-04-19 오후 5:21
* @author : hj
* @Description: 유효한 자격증명을 제공하지 않고 접근하려 할 때 401 UnAuthorized 에러를 리턴
**/
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}