package com.sap.lm.sl.spfi.operations.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerError {
    
    private ServerErrorDetails error;

    public ServerError() {

    }

    public ServerError(String errorMessage, String code) {
        this.setError(new ServerErrorDetails(errorMessage, code));
    }

    public ServerErrorDetails getError() {
        return error;
    }

    public void setError(ServerErrorDetails error) {
        this.error = error;
    }

}
