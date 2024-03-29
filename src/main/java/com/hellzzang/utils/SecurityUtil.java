package com.hellzzang.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
* @package : com.example.jwt.utils
* @name : SecurityUtil.java
* @date : 2023-04-20 오후 1:04
* @author : hj
* @Description: SecurityContext 에서 전역으로 유저 정보를 제공하는 유틸 클래스
**/
public class SecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    private SecurityUtil() {
    }

    /**
    * @methodName : getCurrentuserId
    * @date : 2023-04-20 오후 1:04
    * @author : hj
    * @Description: SecurityContext 에 유저 정보가 저장되는 시점
    **/
    //Request 가 들어올 때 JwtFilter 의 doFilter 에서 저장
    public static Optional<String> getCurrentuserId() { //jwtfilter 클래스의 dofilter 메소드에서 저장한 security context의 인증정보에서 User정보를 리턴
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.debug("Security Context에 인증 정보가 없습니다.");
            return Optional.empty();
        }

        String userId = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            userId = springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            userId = (String) authentication.getPrincipal();
        }

        return Optional.ofNullable(userId);
    }
}