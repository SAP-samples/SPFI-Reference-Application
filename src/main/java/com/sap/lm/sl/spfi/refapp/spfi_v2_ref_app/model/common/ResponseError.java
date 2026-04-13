package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.common;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseError {

    private String code;
    private String message;
    private String target;
    private List<ErrorDetail> details;
}
