package com.bb.Incident.mgmt.exception;

public class CustomAccessDeniedException extends RuntimeException{

    public CustomAccessDeniedException(String message) {
        super(message);
    }
}
