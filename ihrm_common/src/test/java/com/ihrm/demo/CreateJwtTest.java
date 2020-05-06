package com.ihrm.demo;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 *
 * 生成token
 */

public class CreateJwtTest {

    //通过jwt创建token
    public static void main(String[] args) {

        JwtBuilder jwtBuilder = Jwts.builder().setId("123").setSubject("小白")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "ihrm")
                .claim("companyId", "123456")
                .claim("companyName", "黑马");

        //打印token
        String token = jwtBuilder.compact();

        System.out.println(token);
    }
}
