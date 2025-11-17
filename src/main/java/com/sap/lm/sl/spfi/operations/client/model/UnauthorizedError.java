package com.sap.lm.sl.spfi.operations.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class UnauthorizedError {
    private String error;
    private String error_description;

    public UnauthorizedError() {
        
    }
    public UnauthorizedError(String error, String error_description) {
        this.error = error;
        this.error_description = error_description;
        
    }
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError_description() {
        return error_description;
    }

    public void setError_description(String error_description) {
        this.error_description = error_description;
    }
}
