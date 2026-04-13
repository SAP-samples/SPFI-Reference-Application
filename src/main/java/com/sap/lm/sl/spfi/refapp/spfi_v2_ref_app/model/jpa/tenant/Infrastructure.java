package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Infrastructure {
    private String platform;
    private String region;
    private String zone;
    private String dataCenter;
    private String landscape;
    private String environment;
    private String host;
    private String internalIdentifier;

}
