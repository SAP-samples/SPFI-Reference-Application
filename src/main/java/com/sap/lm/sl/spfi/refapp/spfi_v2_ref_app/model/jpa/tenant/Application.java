package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class Application {

    @NotBlank(message = "globalTenantId cannot be null or blank")
    private String globalTenantId;

    private List<Endpoint> endpoints;

    private Map<String, Object> additionalProperties;
}
