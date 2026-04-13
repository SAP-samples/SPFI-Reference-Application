package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {
    private String id;
    private IdentityProvider identityProvider;
    private Status status;

    public Config() {
    }

    public Config(String id, IdentityProvider identityProvider, Status status) {
        this.id = id;
        this.identityProvider = identityProvider;
        this.status = status;
    }
}
