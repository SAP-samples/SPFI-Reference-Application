package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// need to define exceptions with response status, not predefined
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class AppException extends Exception {
    private static final long serialVersionUID = 1L;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
