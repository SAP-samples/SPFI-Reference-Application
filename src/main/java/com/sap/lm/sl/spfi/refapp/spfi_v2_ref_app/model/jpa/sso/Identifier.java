package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Identifier {
    @NotNull
    private String id;

    public Identifier() {

    }
    public Identifier(@NotNull String id) {
        this.id = id;
    }
}
