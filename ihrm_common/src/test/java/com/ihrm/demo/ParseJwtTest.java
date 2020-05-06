package com.ihrm.demo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 *
 * 解析token串
 */
public class ParseJwtTest {

    public static void main(String[] args) {

        String token =
                "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMjMiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE1ODYzNTMxNDIsImNvbXBhbnlJZCI6IjEyMzQ1NiIsImNvbXBhbnlOYW1lIjoi6buR6amsIn0.E3CQnJRyBCUkvd3t31cZr_lIJ78TT-JYgPJwB1Sz_SI";

        Claims claims = Jwts.parser().setSigningKey("ihrm").parseClaimsJws(token).getBody();

        System.out.println("id: " + claims.getId());
        System.out.println("subject: " + claims.getSubject());
        System.out.println("issuer: " + claims.getIssuedAt());

        //解析自定义claim中的内容
        String companyId = (String) claims.get("companyId");
        String companyName = (String) claims.get("companyName");
        System.out.println(companyId + "------" + companyName);
    }
}
