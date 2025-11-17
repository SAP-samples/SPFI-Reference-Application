package com.sap.lm.sl.spfi.operations.client;

public class SpfiClientException extends Exception {

    private static final long serialVersionUID = 6786789654L;

    public SpfiClientException(String message) {
        super(message);
    }
    
    public SpfiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
