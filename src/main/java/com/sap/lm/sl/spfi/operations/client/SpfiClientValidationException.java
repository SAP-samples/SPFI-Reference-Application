package com.sap.lm.sl.spfi.operations.client;


public class SpfiClientValidationException extends SpfiClientException {

    private static final long serialVersionUID = 8786789654L;

    public SpfiClientValidationException(String message) {
        super(message);
    }

    public SpfiClientValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
