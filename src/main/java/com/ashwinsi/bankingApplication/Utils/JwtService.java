package com.ashwinsi.bankingApplication.Utils;

import com.ashwinsi.bankingApplication.DTO.JwtDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtService {
    private String JWT_SECRET = "asdsadasdsadadsd";
    private Integer JWT_EXP = 1000 * 60 * 60 * 30;


    public String generateJWTToken(Long userId, String role){
        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXP))
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
                .compact();
    }

    public boolean isValidToken(String token){
        try{
            Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();

            return true;
        }catch(Exception e) {
            return false;
        }
    }

    public JwtDTO parseJWTToken(String token){
        Claims claims =  Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJwt(token)
                .getBody();

        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);

        return new JwtDTO(userId, role);
    }
}
