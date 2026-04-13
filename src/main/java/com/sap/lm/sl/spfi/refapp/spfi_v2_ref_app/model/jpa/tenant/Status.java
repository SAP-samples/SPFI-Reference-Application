package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Status {
    private String state;
    private StatusDetails details;
    private String lastModified;

}
