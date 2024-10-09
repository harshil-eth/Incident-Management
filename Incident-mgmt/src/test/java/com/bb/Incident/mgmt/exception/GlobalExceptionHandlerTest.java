package com.bb.Incident.mgmt.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleTenantNotFoundException_ShouldReturnNotFound() {
        TenantNotFoundException ex = new TenantNotFoundException("Tenant not found");
        ResponseEntity<String> response = globalExceptionHandler.handleTenantNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Tenant not found", response.getBody());
    }

    @Test
    void handleTenantAlreadyExistsException_ShouldReturnConflict() {
        TenantAlreadyExistsException ex = new TenantAlreadyExistsException("Tenant already exists");
        ResponseEntity<String> response = globalExceptionHandler.handleTenantAlreadyExistsException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Tenant already exists", response.getBody());
    }

    @Test
    void handleSocTenantDeletionException_ShouldReturnForbidden() {
        SocTenantDeletionException ex = new SocTenantDeletionException("Deletion not allowed");
        ResponseEntity<String> response = globalExceptionHandler.handleSocTenantDeletionException(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Deletion not allowed", response.getBody());
    }

    @Test
    void handleOpenIncidentsException_ShouldReturnConflict() {
        OpenIncidentsException ex = new OpenIncidentsException("There are open incidents");
        ResponseEntity<String> response = globalExceptionHandler.handleOpenIncidentsException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("There are open incidents", response.getBody());
    }

    @Test
    void handleUserNotFoundException_ShouldReturnNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        ResponseEntity<String> response = globalExceptionHandler.handleUserNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void handleUserAlreadyExistsException_ShouldReturnConflict() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists");
        ResponseEntity<String> response = globalExceptionHandler.handleUserAlreadyExistsException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already exists", response.getBody());
    }

    @Test
    void handleIncidentNotFoundException_ShouldReturnNotFound() {
        IncidentNotFoundException ex = new IncidentNotFoundException("Incident not found");
        ResponseEntity<String> response = globalExceptionHandler.handleIncidentNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Incident not found", response.getBody());
    }

    @Test
    void handleIncidentAlreadyExistsException_ShouldReturnConflict() {
        IncidentAlreadyExistsException ex = new IncidentAlreadyExistsException("Incident already exists");
        ResponseEntity<String> response = globalExceptionHandler.handleIncidentAlreadyExistsException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Incident already exists", response.getBody());
    }

    @Test
    void handleInvalidIncidentDataException_ShouldReturnBadRequest() {
        InvalidIncidentDataException ex = new InvalidIncidentDataException("Invalid incident data");
        ResponseEntity<String> response = globalExceptionHandler.handleInvalidIncidentDataException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid incident data", response.getBody());
    }

    @Test
    void handleInvalidJwtTokenException_ShouldReturnUnauthorized() {
        InvalidJwtTokenException ex = new InvalidJwtTokenException("Invalid JWT token");
        ResponseEntity<String> response = globalExceptionHandler.handleInvalidJwtTokenException(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid JWT token", response.getBody());
    }

    @Test
    void handleUserAlreadyAssignedException_ShouldReturnConflict() {
        UserAlreadyAssignedException ex = new UserAlreadyAssignedException("User already assigned");
        ResponseEntity<String> response = globalExceptionHandler.handleUserAlreadyAssignedException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already assigned", response.getBody());
    }

    @Test
    void handleInvalidFilterException_ShouldReturnBadRequest() {
        InvalidFilterException ex = new InvalidFilterException("Invalid filter");
        ResponseEntity<String> response = globalExceptionHandler.handleInvalidFilterException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid filter", response.getBody());
    }

    @Test
    void handleDatabaseConnectionException_ShouldReturnServiceUnavailable() {
        DatabaseConnectionException ex = new DatabaseConnectionException("Database connection error");
        ResponseEntity<String> response = globalExceptionHandler.handleDatabaseConnectionException(ex);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Database connection error", response.getBody());
    }

    @Test
    void handleAccessDeniedException_ShouldReturnForbidden() {
        CustomAccessDeniedException ex = new CustomAccessDeniedException("Access denied");
        ResponseEntity<String> response = globalExceptionHandler.handleCustomAccessDeniedException(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void handleAuthenticationFailedException_ShouldReturnUnauthorized() {
        AuthenticationFailedException ex = new AuthenticationFailedException("Authentication failed");
        ResponseEntity<String> response = globalExceptionHandler.handleAuthenticationFailedException(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication failed", response.getBody());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<String> response = globalExceptionHandler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }
}
