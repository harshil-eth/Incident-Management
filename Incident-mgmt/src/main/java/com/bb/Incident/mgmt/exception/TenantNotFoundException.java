package com.bb.Incident.mgmt.exception;

public class TenantNotFoundException extends RuntimeException{

    public TenantNotFoundException(String message) {
        super(message);
    }
}
