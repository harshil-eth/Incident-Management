package com.bb.Incident.mgmt.security;

import com.bb.Incident.mgmt.exception.InvalidJwtTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @BeforeEach
    void setUp() {
        // Set your secret and expiration values for testing
        jwtUtil = new JwtUtil();
        jwtUtil.secret = "mySecretKey"; // Replace with your test secret
        jwtUtil.expiration = 3600000L; // 1 hour in milliseconds
    }

    @Test
    void extractUsername_ShouldReturnUsername_WhenTokenIsValid() {
        String token = jwtUtil.generateToken("testUser", "ROLE_USER");
        String username = jwtUtil.extractUsername(token);
        assertEquals("testUser", username);
    }

    @Test
    void extractClaim_ShouldReturnClaimValue_WhenTokenIsValid() {
        String token = jwtUtil.generateToken("testUser", "ROLE_USER");
        String claimValue = jwtUtil.extractClaim(token, "roles");
        assertEquals("ROLE_USER", claimValue);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtUtil.generateToken("testUser", "ROLE_USER");
        assertTrue(jwtUtil.validateToken(token, "testUser"));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        String token = jwtUtil.generateToken("testUser", "ROLE_USER");
        assertFalse(jwtUtil.validateToken(token, "anotherUser"));
    }

    @Test
    void extractClaim_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token";
        assertThrows(InvalidJwtTokenException.class, () -> jwtUtil.extractClaim(invalidToken, "roles"));
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtUtil.generateToken("testUser", "ROLE_USER");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
}
