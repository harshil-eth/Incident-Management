package com.bb.Incident.mgmt.exception;

public class UserAlreadyAssignedException extends RuntimeException{

    public UserAlreadyAssignedException(String message) {
        super(message);
    }
}
