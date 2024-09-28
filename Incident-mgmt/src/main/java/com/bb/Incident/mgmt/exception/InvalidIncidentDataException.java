package com.bb.Incident.mgmt.exception;

public class InvalidIncidentDataException extends RuntimeException{

    public InvalidIncidentDataException (String message) {
        super(message);
    }
}
