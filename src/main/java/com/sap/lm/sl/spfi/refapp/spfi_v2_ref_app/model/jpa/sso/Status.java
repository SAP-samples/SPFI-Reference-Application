package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso;

import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.common.StatusDetails;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

@Getter
@Setter
public class Status {
    @NonNull
    private State state;
    private StatusDetails details;
    private String lastModified;

    public Status() {
    }

    public Status(@NotNull State state ) {
        this.state = state;
        this.lastModified = new Date().toString();
    }
}
