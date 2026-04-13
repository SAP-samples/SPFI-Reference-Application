package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerErrorDetails {
    private String message;
    private String code;

    public ServerErrorDetails() {

    }

    public ServerErrorDetails(String errorMessage, String code) {
        this.message = errorMessage;
        this.setCode(code);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String error) {
        this.message = error;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
