package com.sap.lm.sl.spfi.operations.client;


public class SpfiClientUrlNotFoundException extends SpfiClientException {

    private static final long serialVersionUID = 8786789654L;

    public SpfiClientUrlNotFoundException(String message) {
        super(message);
    }

    public SpfiClientUrlNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
