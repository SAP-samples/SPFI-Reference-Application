package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository;


import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller.AppException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller.NotFoundException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.StateRequest;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.Status;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.Tenant;

import java.util.List;

public interface ITenantRepository {

    Tenant readTenantByCrmTenantId(String sapCrmTenantId) throws NotFoundException, AppException;

    Tenant readTenantByTenantId(String tenantId) throws AppException;

    List<Tenant> readTenants() throws AppException;

    Tenant storeTenant(Tenant tenantData) throws AppException;

    void updateTenant(String tenantId, Tenant tenant) throws AppException;

    void deleteTenantByTenantId(String tenantId) throws AppException;

    Status getTenantStatus(String tenantId) throws AppException;

    Status changeTenantStatus(String tenantId, StateRequest stateRequest) throws AppException;
}
