package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NotAuthorizedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotAuthorizedException(String message) {
        super(message);
    }
    
    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
