package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class AdditionalProperties {
    private Map<String, Object> fromManager;
    private Map<String, Object> fromProvider;
}