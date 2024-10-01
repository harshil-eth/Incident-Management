package com.bb.Incident.mgmt.service;

import com.bb.Incident.mgmt.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private TenantService tenantService;

    public String createAuthenticationToken(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            throw new Exception("Missing or invalid Authorization header");
        }

        String base64Credentials = authHeader.substring(6);
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] values = credentials.split(":", 2);
        String username = values[0];
        String password = values[1];

        // Authenticate tenant
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (Exception e) {
            throw new Exception("Incorrect username or password", e);
        }

        // Load tenant details
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String roles = tenantService.getTenantRolesByUsername(username);

        // Generating JWT token with tenant roles
        return jwtUtil.generateToken(userDetails.getUsername(), roles);
    }
}