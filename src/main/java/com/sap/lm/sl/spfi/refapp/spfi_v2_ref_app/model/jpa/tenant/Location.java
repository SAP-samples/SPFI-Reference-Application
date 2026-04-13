package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Setter
@Getter
public class Location {
    @Valid
    @NotNull(message = "geography cannot be null")
    private Geography geography;

    @Valid
    @NotNull(message = "infrastructure cannot be null")
    private Infrastructure infrastructure;

}
