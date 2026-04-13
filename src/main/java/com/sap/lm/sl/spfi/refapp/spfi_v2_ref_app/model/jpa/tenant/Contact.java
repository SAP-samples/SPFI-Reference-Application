package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Setter
@Getter
public class Contact {
    @NotBlank(message = "contact name cannot be null or blank")
    private String name;
    private String id;
    private String email;
    private String phone;

}
