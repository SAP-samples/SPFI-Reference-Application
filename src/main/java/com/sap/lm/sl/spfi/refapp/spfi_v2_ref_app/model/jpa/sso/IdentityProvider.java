package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentityProvider {
    @NotNull
    AuthProtocol authProtocol;
    @NotNull
    String metadataUrl;
}

