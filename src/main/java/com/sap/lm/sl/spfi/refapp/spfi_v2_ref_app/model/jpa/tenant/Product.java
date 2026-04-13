package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Product {
    private String productId;
    private String unit;
    private int quota;
}
