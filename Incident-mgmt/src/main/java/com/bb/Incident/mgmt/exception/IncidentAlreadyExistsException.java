package com.bb.Incident.mgmt.exception;

public class IncidentAlreadyExistsException extends RuntimeException{

    public IncidentAlreadyExistsException(String message) {
        super(message);
    }
}
