package com.sap.lm.sl.spfi.refapp.mocks;

import com.sap.lm.sl.spfi.operations.client.model.Activate;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject;

public class ActivateStatus {
    private StatusSubject subject = null;
    private Activate details;
    public StatusSubject getSubject() {
        return subject;
    }
    public void setSubject(StatusSubject subject) {
        this.subject = subject;
    }
    public Activate getDetails() {
        return details;
    }
    public void setDetails(Activate details) {
        this.details = details;
    }
}
