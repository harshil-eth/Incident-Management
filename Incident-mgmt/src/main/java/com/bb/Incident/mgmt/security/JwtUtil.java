package com.bb.Incident.mgmt.security;

import com.bb.Incident.mgmt.exception.InvalidJwtTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    String secret;

    @Value("${jwt.expiration}")
    Long expiration;

    public String extractUsername(String token) {
        return extractClaim(token, "sub");
    }

    public String extractClaim(String token, String claim) {
        String[] parts = token.split("\\.");

        if (parts.length != 3) {
            throw new InvalidJwtTokenException("Invalid JWT token");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        Map<String, Object> claims = parseJson(payload);
        return claims.get(claim).toString();
    }

    public Boolean isTokenExpired(String token) {
        Date expirationDate = new Date(Long.parseLong(extractClaim(token, "exp")));
        return expirationDate.before(new Date());
    }

    public List<String> extractRoles(String token) {
        String roles = extractClaim(token, "roles");
        return Arrays.asList(roles.split(" "));
    }

    public String generateToken(String username, String roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        System.out.println("Generating token for username " + username + " with roles: " + roles);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        claims.put("sub", subject);
        claims.put("iat", now);
        claims.put("exp", now + expiration);

        JSONObject json = new JSONObject(claims);
//        System.out.println(json);

        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(json.toString().getBytes());
//        System.out.println(payload);
        String signature = hmacSha256(header + "." + payload, secret);

        return header + "." + payload + "." + signature;
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA-256", e);
        }
    }

    private Map<String, Object> parseJson(String json) {
        // Simple JSON parser (you can use a more robust one if allowed)
        Map<String, Object> map = new HashMap<>();
        json = json.replace("{", "").replace("}", "");
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            map.put(keyValue[0].trim().replace("\"", ""), keyValue[1].trim().replace("\"", ""));
        }
        return map;
    }
}
