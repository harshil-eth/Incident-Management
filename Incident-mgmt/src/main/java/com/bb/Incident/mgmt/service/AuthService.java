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

//        System.out.println("AuthHeader: " + authHeader);  encoded value 
        // have to decode this authHeader to get the credentials
        String base64Credentials = authHeader.substring(6);
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));

//        System.out.println("Credentials: " + credentials);

        String[] values = credentials.split(":", 2);

//        System.out.println("Values: " + Arrays.toString(values));

        String username = values[0];
        String password = values[1];

        // in case of wrong username or password, its breaking before this
        System.out.println("username:: " + username);
        System.out.println("password:: " + password);

        // Authenticate tenant
        try {
            System.out.println("bye");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            System.out.println("hii");
            throw new AuthenticationFailedException("Incorrect username or password", e);
        }

        // Load tenant details
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String roles = tenantService.getTenantRolesByUsername(username);

        // Generating JWT token with tenant roles
        return jwtUtil.generateToken(userDetails.getUsername(), roles);
    }
}
