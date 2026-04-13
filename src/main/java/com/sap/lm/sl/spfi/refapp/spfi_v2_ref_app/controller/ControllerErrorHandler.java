package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller;


import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.exception.LockTimeoutException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.exception.OperationNotAllowed;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.common.ResponseError;
import jakarta.ws.rs.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.FileNotFoundException;

@ControllerAdvice
public class ControllerErrorHandler {


    @ExceptionHandler({NotFoundException.class, FileNotFoundException.class})
    public final ResponseEntity<Object> handleNotFoundException(Exception ex, WebRequest request) {
        ResponseError error = new ResponseError();
        error.setCode(HttpStatus.NOT_FOUND.toString());
        error.setMessage(ex.getMessage());
        error.setTarget(request.getContextPath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler({OperationNotAllowed.class})
    public final ResponseEntity<Object> handleOperationNotSupportedException(Exception ex, WebRequest request) {
        ResponseError error = new ResponseError();
        error.setCode(HttpStatus.BAD_REQUEST.toString());
        error.setMessage(ex.getMessage());
        error.setTarget(request.getContextPath());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({LockTimeoutException.class})
    public final ResponseEntity<Object> handleBusyException(Exception ex, WebRequest request) {
        ResponseError error = new ResponseError();
        error.setCode(HttpStatus.SERVICE_UNAVAILABLE.toString());
        error.setMessage(ex.getMessage());
        error.setTarget(request.getContextPath());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        ResponseError error = new ResponseError();
        error.setCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        error.setMessage(ex.getMessage());
        error.setTarget(request.getContextPath());
        return ResponseEntity.internalServerError().body(error);
    }


}
