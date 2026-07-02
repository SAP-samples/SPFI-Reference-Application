package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class Endpoint {

    @NotBlank(message = "type cannot be null or blank")
    private String type;

    @NotBlank(message = "url cannot be null or blank")
    private String url;

    private String displayName;

    private Map<String, String> properties;
}
