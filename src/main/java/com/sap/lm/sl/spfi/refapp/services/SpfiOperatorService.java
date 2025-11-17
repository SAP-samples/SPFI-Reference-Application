package com.sap.lm.sl.spfi.refapp.services;

import com.sap.lm.sl.spfi.refapp.controllers.AppException;

public interface SpfiOperatorService {
    public void processFulfillment(String xSapSaasfulfillmentNotifcationId, String xSapSaasfulfillmentCallbackUrl) throws AppException;
}
