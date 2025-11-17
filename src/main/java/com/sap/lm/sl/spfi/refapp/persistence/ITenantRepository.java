package com.sap.lm.sl.spfi.refapp.persistence;

import java.util.List;
import java.util.Map;

import com.sap.lm.sl.spfi.refapp.controllers.AppException;
import com.sap.lm.sl.spfi.refapp.services.AppTenant;

public interface ITenantRepository {
    public void storeTenant(AppTenant tenantData) throws AppException;

    public AppTenant readTenantByCrmTenantId(String tenantId) throws AppException;

    public void deleteTenantByCrmTenantId(String tenantId) throws AppException;

    public Map<String, AppTenant> readTenants() throws AppException;

    public List<String> listTenants() throws AppException;
}
