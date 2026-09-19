package com.rothbard212.painel_estoque.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey chave;
    private final long VALIDADE_MS = 1000 * 60 * 60 * 10; // 10 horas

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
        this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String gerarToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + VALIDADE_MS))
                .signWith(chave)
                .compact();
    }

    public String extrairEmail(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean tokenValido(String token) {
        try {
            Jwts.parser().verifyWith(chave).build().parseSignedClaims(token);
            return true;
        } catch (Exception erro) {
            System.out.println("ERRO AO VALIDAR TOKEN: " + erro.getClass().getSimpleName() + " - " + erro.getMessage());
            return false;
        }
    }
}