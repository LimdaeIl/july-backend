package com.backend.july.auth.infrastructure.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class JWTHashUtil {

    private static final String SHA_256 = "SHA-256";

    public String sha256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("SHA-256 알고리즘을 사용할 수 있어야 합니다.", e);
        }
    }
}

