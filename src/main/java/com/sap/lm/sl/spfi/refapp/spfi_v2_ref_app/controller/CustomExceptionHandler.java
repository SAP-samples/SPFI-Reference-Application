package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller;


import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.ErrorResponse;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.ServerError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice(assignableTypes = TenantController.class)
@Order(1)
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException exception,
                                                                     WebRequest request) {
        ServerError apiError = new ServerError(exception.getLocalizedMessage(), exception.getClass().getName());
        return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

   // @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {

        List<ErrorResponse.ErrorDetail> errorDetails = ex.getBindingResult().getFieldErrors().stream()
                .map(x -> {
                    String msg = x.getDefaultMessage();
                    return new ErrorResponse.ErrorDetail("900", msg);
                })
                .collect(Collectors.toList()); // Collecting all error details into a list

        ErrorResponse errorResponse = new ErrorResponse(
                "400-01",
                "Invalid message body.", // General error message
                ex.getClass().getSimpleName(),
                errorDetails // The list of error details
        );

        // Return the ResponseEntity with status and body
        return new ResponseEntity<>(errorResponse, status);

    }

    @ExceptionHandler(InvalidParamException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTenantIdException(InvalidParamException ex) {
        // Create error details
        List<ErrorResponse.ErrorDetail> errorDetails = new ArrayList<>();
        errorDetails.add(new ErrorResponse.ErrorDetail(
                "900", // Error code
                "The value" +  " [" + ex.getValue() + "]" +  " provided for " + ex.getParam() + " is invalid. " + ex.getMessage() // Message
        ));

        // Create and return ErrorResponse
        ErrorResponse errorResponse = new ErrorResponse(
                "400-01",  // General error code
                "Invalid message body.",  // General message
                ex.getParam(),
                errorDetails // List of errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle HTTP method not supported errors (404 errors or invalid path)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPath(MethodArgumentTypeMismatchException ex) {
        List<ErrorResponse.ErrorDetail> details = new ArrayList<>();
        details.add(new ErrorResponse.ErrorDetail(
                "900",
                "string"
        ));

        ErrorResponse errorResponse = new ErrorResponse(
                "400-02", // Custom error code for invalid path
                "Invalid path.",
                ex.getClass().getSimpleName(),
                details
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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
        //return convertToResponseEntity(exception, HttpStatus.NOT_FOUND, request, exception.getClass().getName());

        ErrorResponse errorResponse;
        ErrorResponse.ErrorDetail errorDetail = new ErrorResponse.ErrorDetail("900", exception.getMessage());

        errorResponse = new ErrorResponse(
                "404-01",
                "Unable to retrieve desired resource.",
                exception.getClass().getName(),
                Collections.singletonList(errorDetail)
        );

        // Return the ResponseEntity with status and body
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleNotFoundException(AppException exception, WebRequest request) {

        ErrorResponse errorResponse;
        HttpHeaders headers = new HttpHeaders();
        ErrorResponse.ErrorDetail errorDetail = new ErrorResponse.ErrorDetail("900", exception.getMessage());
        if (exception.getMessage().equals("The applied state transition is not allowed.")) {
            errorResponse = new ErrorResponse(
                    "409-02", // The status code as a string
                    exception.getMessage(), // General error message
                    exception.getClass().getName(),
                    Collections.singletonList(errorDetail)// The list of error details
            );
        } else if (exception.getMessage().equals("Too Many Requests")) {
            errorResponse = new ErrorResponse(
                    "429-01",
                    "Unwilling to fulfill the request.",
                    exception.getClass().getName(),
                    Collections.singletonList(errorDetail)
            );
        } else {
            errorResponse = new ErrorResponse(
                    "409-01",
                    "The given resource already exists.",
                    exception.getClass().getName(),
                    Collections.singletonList(errorDetail)
            );
        }

        // Return the ResponseEntity with status and body
        return new ResponseEntity<>(errorResponse, headers, HttpStatus.CONFLICT);
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
