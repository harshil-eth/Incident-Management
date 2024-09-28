package com.bb.Incident.mgmt.exception;

public class DatabaseConnectionException extends RuntimeException{

    public DatabaseConnectionException(String message) {
        super(message);
    }
}
