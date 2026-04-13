package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.service;



import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller.AppException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller.NotFoundException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.StateRequest;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.Status;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.Tenant;

import java.util.List;
import java.util.Map;

public interface TenantService {

    Tenant getTenant(String sapCrmTenantId) throws NotFoundException, AppException;

    List<Tenant> readTenants() throws AppException;

    public Map<String, Object> fastTenantActivation(Tenant tenant, String state) throws AppException;

    public void slowTenantActivation(Tenant tenant) throws AppException;

    public void fastTenantDeletion(String tenantId) throws AppException;

    public void slowTenantDeletion(Tenant tenant) throws AppException;

    Status getTenantStatus(String tenantId) throws AppException;

    Status fastTenantStatusUpdate(String tenantId, StateRequest stateRequest) throws AppException;

    void slowTenantStatusUpdate(String tenantId, StateRequest stateRequest, Tenant tenant) throws AppException;

    public void fastTenantUpdate(String tenantId, Tenant tenantData) throws AppException;

    public void slowTenantUpdate(Tenant tenant, Tenant tenantData) throws AppException;
}
