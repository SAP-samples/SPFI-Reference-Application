package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Setter
@Getter
public class Sso {
    private String id;

    @Valid
    @NotNull(message = "identityProvider cannot be null.")
    private IdentityProvider identityProvider;
    private Status status;

}
