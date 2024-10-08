package com.bb.Incident.mgmt.service;

import com.bb.Incident.mgmt.exception.AuthenticationFailedException;
import com.bb.Incident.mgmt.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private TenantService tenantService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAuthenticationToken_ValidCredentials_ReturnsToken() throws Exception {
        String authHeader = "Basic dXNlcm5hbWU6cGFzc3dvcmQ="; // Base64 for username:password
        String username = "username";
        String password = "password";

        // Mocking behavior
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationToken); // Return a valid authentication token

        // Mock userDetails and tenant service
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(tenantService.getTenantRolesByUsername(username)).thenReturn("ROLE_USER");

        // Mock JWT utility
        when(jwtUtil.generateToken(username, "ROLE_USER")).thenReturn("generated_jwt_token");

        String token = authService.createAuthenticationToken(authHeader);

        assertEquals("generated_jwt_token", token);
    }


    @Test
    void createAuthenticationToken_InvalidCredentials_ThrowsException() {
        String authHeader = "Basic dXNlcm5hbWU6cGFzc3dvcmQ=";

        // Mocking behavior
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        Exception exception = assertThrows(AuthenticationFailedException.class, () -> {
            authService.createAuthenticationToken(authHeader);
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void createAuthenticationToken_MissingAuthorizationHeader_ThrowsException() {
        String authHeader = null;

        Exception exception = assertThrows(Exception.class, () -> {
            authService.createAuthenticationToken(authHeader);
        });

        assertEquals("Missing or invalid Authorization header", exception.getMessage());
    }
}
