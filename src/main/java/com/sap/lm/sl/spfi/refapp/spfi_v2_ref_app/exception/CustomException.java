package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.exception;


import java.io.Serial;

public class CustomException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String message;

    public CustomException(String message) {
        super(message);
        this.message = message;
    }

    public String toString() {
        return message;
    }
}
