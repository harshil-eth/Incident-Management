package com.bb.Incident.mgmt.exception;

public class IncidentNotFoundException extends RuntimeException{

    public IncidentNotFoundException(String message) {
        super(message);
    }
}
