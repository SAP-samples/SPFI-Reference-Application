package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller;

import lombok.Getter;

@Getter
public class InvalidParamException extends RuntimeException {

    private String param;
    private String value;
    private String message;

    public InvalidParamException(String param, String value, String message) {
        this.param = param;
        this.value = value;
        this.message = message;
    }

}
