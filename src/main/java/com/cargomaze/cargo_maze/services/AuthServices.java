package com.cargomaze.cargo_maze.services;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

@Service
public class AuthServices {

    public String getEmailFromToken(String token) {
        try {
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
            return signedJWT.getJWTClaimsSet().getStringClaim("email");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
