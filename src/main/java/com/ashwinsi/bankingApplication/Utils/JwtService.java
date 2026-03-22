package com.ashwinsi.bankingApplication.Utils;

import com.ashwinsi.bankingApplication.Config.EnvConfig;
import com.ashwinsi.bankingApplication.DTO.JwtDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtService {

    private EnvConfig envConfig;
    private String JWT_SECRET;
    private Long JWT_EXP;

    JwtService(EnvConfig envConfig){
        this.envConfig = envConfig;
        this.JWT_SECRET = envConfig.getJwtSecret();
        this.JWT_EXP = envConfig.getJwtExpirationTime();
    }


    public String generateJWTToken(UUID userId, String role){
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
