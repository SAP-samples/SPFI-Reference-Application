package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Resource {
    private String group;
    private String version;
    private String type;
    private String spec;
}
