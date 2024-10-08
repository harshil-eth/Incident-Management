package com.bb.Incident.mgmt.controller;

import com.bb.Incident.mgmt.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAuthenticationToken_ValidCredentials_ReturnsToken() throws Exception {
        String authHeader = "Basic dXNlcm5hbWU6cGFzc3dvcmQ="; // Base64 for username:password
        String expectedToken = "generated_jwt_token";

        when(authService.createAuthenticationToken(authHeader)).thenReturn(expectedToken);

        String actualToken = authController.createAuthenticationToken(authHeader);

        assertEquals(expectedToken, actualToken);
    }

    @Test
    void createAuthenticationToken_InvalidCredentials_ThrowsException() throws Exception {
        String authHeader = "Basic dXNlcm5hbWU6cGFzc3dvcmQ=";

        when(authService.createAuthenticationToken(authHeader)).thenThrow(new Exception("Invalid username or password"));

        Exception exception = assertThrows(Exception.class, () -> {
            authController.createAuthenticationToken(authHeader);
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void createAuthenticationToken_MissingAuthorizationHeader_ThrowsException() {
        String authHeader = null;

        Exception exception = assertThrows(Exception.class, () -> {
            authController.createAuthenticationToken(authHeader);
        });

        assertEquals("Missing or invalid Authorization header", exception.getMessage());
    }
}
