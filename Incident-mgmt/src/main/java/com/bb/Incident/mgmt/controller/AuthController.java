package com.bb.Incident.mgmt.controller;

import com.bb.Incident.mgmt.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/login")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/tenant")
    public String createAuthenticationToken(@RequestHeader(value = "Authorization", required = false) String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            throw new Exception("Missing or invalid Authorization header");
        }
        return authService.createAuthenticationToken(authHeader);
    }
}
