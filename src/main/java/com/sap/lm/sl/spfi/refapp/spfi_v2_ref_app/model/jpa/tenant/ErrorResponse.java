package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ErrorResponse {
    private String code;
    private String message;
    private String target;
    private List<ErrorDetail> details;

    // Constructor
    public ErrorResponse(String code, String message, String target, List<ErrorDetail> details) {
        this.code = code;
        this.message = message;
        this.target = target;
        this.details = details;
    }

    // Inner class for error details
    public static class ErrorDetail {
        private String code;
        private String message;

        // Constructor
        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }

        // Getters and setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}


