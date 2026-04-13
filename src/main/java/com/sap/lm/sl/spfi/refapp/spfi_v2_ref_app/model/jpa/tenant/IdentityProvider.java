package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Setter
@Getter
public class IdentityProvider {
    @NotBlank(message = "authProtocol cannot be null or blank")
    @Pattern(regexp = "^(kerberos|saml2|oidc)$", message = "authProtocol must be one of kerberos, saml2, or oidc")
    private String authProtocol;
    private String metadataURL;

}
