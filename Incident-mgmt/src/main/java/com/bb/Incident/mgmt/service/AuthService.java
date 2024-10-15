package com.bb.Incident.mgmt.service;

import com.bb.Incident.mgmt.exception.AuthenticationFailedException;
import com.bb.Incident.mgmt.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("Invalid username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        System.out.println("inside authservice, userDetails: " + userDetails);
        final String roles = tenantService.getTenantRolesByUsername(username);
        System.out.println("inside authservice, roles: " + roles);

        return jwtUtil.generateToken(userDetails.getUsername(), roles);
    }
}
