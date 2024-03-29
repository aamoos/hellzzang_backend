package com.hellzzang.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellzzang.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
* @package : com.example.jwt.jwt
* @name : TokenProvider.java
* @date : 2023-04-19 오후 5:23
* @author : hj
* @Description: 토큰의 생성, 토큰의 유효성 검증, 암호화 설정 등의 역할을 담당하는 클래스
**/
@Component
public class TokenProvider implements InitializingBean {
    //InitializingBean을 implements 받아 afterPropertiesSet을 Override 하는 이유는
    //TokenProvider Bean이 생성되고, 주입을 받은 후에 secret 값을 Base64 Decode해서 key변수에 할당하기 위함

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";

    //access token 시크릿키
    private final String secret;

    //access token 만료시간
    private long tokenValidityInMilliseconds = Duration.ofMinutes(30).toMillis(); // 만료시간 30분

    //refresh token 만료시간
    private long refreshTokenValidityInMilliseconds = Duration.ofDays(14).toMillis(); // 만료시간 2주

    private Key key;

    public TokenProvider(    //application.yml에서 정의한 header와 validity 값 주입
                             @Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    //빈이 생성이 되고 의존성 주입 이후에 secret값을 Base64 Decode해서 key 변수에 할당하기 위함
    @Override
    public void afterPropertiesSet() {
        //access token
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
    * @methodName : createToken
    * @date : 2023-04-20 오후 1:00
    * @author : hj
    * @Description: 유저 정보를 가지고 AccessToken을 생성하는 메서드
    **/
    //Authentication 객체에 포함되어 있는 권한 정보들을 담은 토큰 생성
    public String createToken(User user) {

        //권한 가져오기
//        String authorities = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.joining(","));

        //권한 가져오기
        String authorities = user.getAuthority();

        //현재 시간을 밀리초로 환산
        long now = (new Date()).getTime();

        //만료시간 30분으로 설정
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        // 사용자 인덱스 정보 추가
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());

        //토큰 생성하여 리턴
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUserId())
                .claim(AUTHORITIES_KEY, authorities) //JWT의 body이고 key-value 데이터를 추가함. 여기서는 권한정보
                .setExpiration(validity)  //만료일 설정
                .signWith(key, SignatureAlgorithm.HS512) //HS512 알고리즘 적용
                .compact(); //토큰 생성
    }

    public String generateToken2(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));


        //Access Token 생성
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setExpiration(new Date(System.currentTimeMillis()+ 1000 * 60 * 30))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(User user) {

        //현재 시간을 밀리초로 환산
        long now = (new Date()).getTime();

        //만료시간 2주로 설정
        Date validity = new Date(now + this.refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(user.getUserId())
                .setExpiration(validity)  //만료일 설정
                .signWith(key, SignatureAlgorithm.HS512) //HS512 알고리즘 적용
                .compact(); //토큰 생성
    }

    /**
    * @methodName : getAuthentication
    * @date : 2023-04-20 오후 1:01
    * @author : hj
    * @Description: JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    **/
    //토큰에 담겨있는 권한 정보들을 이용해 Authentication 객체를 리턴
    //JwtFilter에서 사용됨
    public Authentication getAuthentication(String token) {
        //토큰 복호화
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        //클레임에서 권한 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        //UserDetails 객체를 만들어서 Authentication 리턴
        org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
    * @methodName : getJwtTokenPayload
    * @date : 2023-04-26 오후 5:06
    * @author : 김재성
    * @Description: token으로 정보 추출
    **/
    public Map<String, Object> getJwtTokenPayload(String token) throws Exception {
        String[] check = token.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(check[1]));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> returnMap = mapper.readValue(payload, Map.class);
        return returnMap;
    }

    /**
    * @methodName : getJwtTokenuserId
    * @date : 2023-04-28 오후 5:51
    * @author : 김재성
    * @Description: jwt token으로 현재 로그인한 사용자 인덱스 가져오기
    **/
    public Long getJwtTokenId(String token){
        String[] check = token.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(check[1]));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> returnMap = null;
        try {
            returnMap = mapper.readValue(payload, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        int id = (int) returnMap.get("id");

        return Long.valueOf(id);
    }

    /**
    * @methodName : validateToken
    * @date : 2023-04-20 오후 1:01
    * @author : hj
    * @Description: 토큰 정보 검증하는 메서드
    **/
    public boolean validateToken(String token) throws Exception {
        System.out.println(getJwtTokenPayload(token));
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}