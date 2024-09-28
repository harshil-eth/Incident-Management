package com.bb.Incident.mgmt.exception;

public class TenantAlreadyExistsException extends RuntimeException{

    public TenantAlreadyExistsException(String message) {
        super(message);
    }
}
