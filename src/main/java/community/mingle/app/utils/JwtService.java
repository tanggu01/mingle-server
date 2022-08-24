package community.mingle.app.utils;


import community.mingle.app.config.secret.Secret;
import community.mingle.app.config.BaseException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static community.mingle.app.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class JwtService {


    /*
    JWT 생성
    @param userIdx
    @return String
     */
    public String createJwt(Long userIdx) {
        Date now = new Date();
        return Jwts.builder()
                .setIssuer("mingle.community")
                .setHeaderParam("type","jwt")
                .claim("userIdx",userIdx)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+1*(1000*60*60*24*365)))
                .signWith(SignatureAlgorithm.HS256, Secret.JWT_SECRET_KEY)
                .compact();
    }

    /*
    Header에서 X-ACCESS-TOKEN 으로 JWT 추출
    @return String
     */
    public String getJwt(){ //resolveToken
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("X-ACCESS-TOKEN");
    }

//    public boolean checkClaim(String jwt) {
//        try {
//            Claims claims = Jwts.parser()
//                    .setSigningKey(secretKey.getBytes())
//                    .parseClaimsJws(jwt).getBody();
//            return true;
//
//        }catch(ExpiredJwtException e) {
////            logger.error("Token Expired");
//            return false;
//
//        }catch(JwtException e) {
////            logger.error("Token Error");
//            return false;
//        }
//    }


    /*
    header에서 받아온 JWT에서 userIdx 추출
    @return int
    @throws BaseException
     */
    public Long getUserIdx() throws BaseException{
        //1. JWT 추출
        String accessToken = getJwt();



        if(accessToken == null || accessToken.length() == 0) {
            throw new BaseException(EMPTY_JWT);
        }

        // 2. JWT parsing  -> getUserPk : jwt 에서 회원 구분 pk 추출
        Jws<Claims> claims;
        try{
            claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_SECRET_KEY)
                    .parseClaimsJws(accessToken);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }

        // 3. userIdx 추출
        return claims.getBody().get("userIdx",Long.class);
    }

    //jwt 로 인증정보를 조회
//    public Authentication getAuthentication(String accessToken) {
//    }


    //jwt의 유효성 및 만료일자 확인
    public boolean validateToken(String accessToken) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(Secret.JWT_SECRET_KEY)
                    .parseClaimsJws(accessToken);
            return !claimsJws.getBody().getExpiration().before(new Date()); //만료날짜가 현재보다 이전이면 false
        } catch (Exception e) {
            return false;
        }
    }


}
