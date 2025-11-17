package com.sap.lm.sl.spfi.refapp.services;

import java.util.List;
import java.util.Map;

import com.sap.lm.sl.spfi.operations.client.model.ActivateContext;
import com.sap.lm.sl.spfi.operations.client.model.BlockContext;
import com.sap.lm.sl.spfi.operations.client.model.SsosetupContext;
import com.sap.lm.sl.spfi.operations.client.model.TerminateContext;
import com.sap.lm.sl.spfi.refapp.controllers.AppException;
import com.sap.lm.sl.spfi.refapp.controllers.NotFoundException;

public interface TenantService {
    public AppTenant activate(String xSapSpfiNotifcationId, String xSapSpfiCallbackUrl, String sapCrmTenantId, ActivateContext activateContext) throws AppException;

    public AppTenant terminate(String xSapSpfiNotifcationId, String xSapSpfiCallbackUrl, String sapCrmTenantId, TerminateContext terminateContext) throws NotFoundException, AppException;

    public AppTenant setupSso(String xSapSpfiNotifcationId, String sapCrmTenantId, SsosetupContext ssosetupContext) throws AppException;

    public AppTenant getTenant(String sapCrmTenantId) throws AppException;

    public Map<String, AppTenant> getTenants() throws AppException;

    public List<String> listTenants() throws AppException;

    public AppTenant block(String xSapSaasfulfillmentNotifcationId, String sapCrmTenantId, BlockContext details) throws AppException;
}
