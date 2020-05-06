package com.ihrm.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtils {

    //签发token的私钥
    private String key;
    //token的过期失效的时间长度
    private Long ttl;

    public String createJwt(String id, String name, Map<String, Object> map){

        long now = System.currentTimeMillis();

        long exp = now + ttl;

        /**
         * 设置认证token
         *      id:登录用户id
         *      subject：登录用户名
         *
         */
        JwtBuilder jwtBuilder = Jwts.builder().setId(id).setSubject(name)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, key);

        //设置自定义claim
        for (Map.Entry<String, Object> entry : map.entrySet()) {

            jwtBuilder.claim(entry.getKey(), entry.getValue());
        }
        //设置过期时间
        jwtBuilder.setExpiration(new Date(exp));

        //创建token
        String token = jwtBuilder.compact();

        return token;
    }

    /**
     * 解析token字符串获取clamis
     */
    public Claims parseJwt(String token) {

        Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        return claims;
    }
}
