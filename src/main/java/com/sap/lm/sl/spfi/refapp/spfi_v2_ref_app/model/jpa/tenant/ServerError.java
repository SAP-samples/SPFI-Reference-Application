package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

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
