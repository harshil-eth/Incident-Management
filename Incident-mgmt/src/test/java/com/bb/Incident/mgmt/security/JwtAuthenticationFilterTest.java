package com.bb.Incident.mgmt.security;

import com.bb.Incident.mgmt.exception.AuthenticationFailedException;
import com.bb.Incident.mgmt.exception.InvalidJwtTokenException;
import com.bb.Incident.mgmt.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldReturnUnauthorized_WhenTokenIsInvalid() throws Exception {
        // Arrange
        String invalidToken = "Bearer invalid.token";
        when(request.getHeader("Authorization")).thenReturn(invalidToken);
        when(request.getRequestURI()).thenReturn("/v1/incidents"); // Mocking the request URI
        when(jwtUtil.extractUsername("invalid.token")).thenReturn("testUser");
        when(userDetailsService.loadUserByUsername("testUser")).thenThrow(new UsernameNotFoundException("User not found")); // Simulate user not found
        when(jwtUtil.validateToken("invalid.token", "testUser")).thenReturn(false); // Set to return false for validation

        // Create a mock PrintWriter
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Unauthorized: Authentication error"); // Verify the error message
    }

    @Test
    void doFilterInternal_ShouldReturnUnauthorized_WhenNoTokenIsProvided() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/v1/incidents"); // Mocking the request URI

        // Create a mock PrintWriter
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer); // Mocking getWriter to return the PrintWriter

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Unauthorized: No JWT token found"); // Verify the correct message for no token
    }

    @Test
    void doFilterInternal_ShouldReturnUnauthorized_WhenTokenIsExpired() throws Exception {
        // Arrange
        String jwt = "expired.jwt.token";
        String username = "testUser";

        // Mocking the request header and URI
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(request.getRequestURI()).thenReturn("/v1/incidents"); // Ensure the request URI is mocked

        when(jwtUtil.extractUsername(jwt)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(mock(UserDetails.class));
        when(jwtUtil.validateToken(jwt, username)).thenReturn(false);

        // Create a mock PrintWriter
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Invalid Jwt Token"); // Update to expect the actual message
    }


    @Test
    void doFilterInternal_ShouldAllowPublicEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/v1/tenants");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}

