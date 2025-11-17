package com.sap.lm.sl.spfi.refapp.controllers;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.sap.lm.sl.spfi.operations.client.model.ServerError;

/**
 * A simple exception mapper for exceptions that also provides the error messages as part of the response. Gathers
 * all @ExceptionHandler methods in a single class so that exceptions from all controllers are handled consistently in
 * one place.
 */
@RestControllerAdvice
public class CustomExceptionMapper extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException exception,
            WebRequest request) {
        ServerError apiError = new ServerError(exception.getLocalizedMessage(), exception.getClass().getName());
        return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Here we have to override implementation of ResponseEntityExceptionHandler.
     */
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        return convertToResponseEntity(exception, HttpStatus.BAD_REQUEST, request, exception.getClass().getName());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleBadRequestException(BadRequestException exception, WebRequest request) {
        return convertToResponseEntity(exception, HttpStatus.BAD_REQUEST, request, exception.getErrorCode());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleNotAuthorizedExceptionException(NotAuthorizedException exception, WebRequest request) {
        return convertToResponseEntity(exception, HttpStatus.FORBIDDEN, request, exception.getClass().getName());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleNotFoundException(NotFoundException exception, WebRequest request) {
        return convertToResponseEntity(exception, HttpStatus.NOT_FOUND, request, exception.getClass().getName());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleNotFoundException(AppException exception, WebRequest request) {
        return convertToResponseEntity(exception, HttpStatus.UNPROCESSABLE_ENTITY, request, exception.getClass().getName());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleAll(Exception exception, WebRequest request) {
        return convertToResponseEntity(exception, HttpStatus.INTERNAL_SERVER_ERROR, request, exception.getClass().getName());
    }

    private ResponseEntity<Object> convertToResponseEntity(Exception exception, HttpStatus status, WebRequest request, String code) {
        logger.error("Web request failed: " + exception.getMessage());
        ServerError apiError = new ServerError(exception.getLocalizedMessage(), code);
        return new ResponseEntity<Object>(apiError, new HttpHeaders(), status);
    }

}
