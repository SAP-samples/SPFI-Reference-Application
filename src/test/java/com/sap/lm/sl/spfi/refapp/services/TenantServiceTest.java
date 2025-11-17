package com.sap.lm.sl.spfi.refapp.services;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.sap.lm.sl.spfi.operations.client.model.ActivateContext;
import com.sap.lm.sl.spfi.operations.client.model.Tenant;
import com.sap.lm.sl.spfi.refapp.controllers.AppException;
import com.sap.lm.sl.spfi.refapp.persistence.FileTenantRepository;

public class TenantServiceTest {
    @Test
    public void createTenantTest() throws AppException {
        TenantService tenantService = new TenantServiceImpl();
        String notificationId = UUID.randomUUID().toString();
        ActivateContext activateContext = new ActivateContext();
        Tenant tenant = new Tenant();
        tenant.setSolutionId("Solution-Id");
        activateContext.setTenant(tenant );
        tenantService.activate(notificationId, "test","Crm-Tenant-ID", activateContext);
    }

    @Test
    public void getTenantsTest() throws AppException {
        TenantService tenantService = new TenantServiceImpl();
        Map<String, AppTenant> tenants = tenantService.getTenants();
        System.out.println(tenants.size());
    }

    @Test
    public void listTenantsTest() throws AppException {
        TenantService tenantService = new TenantServiceImpl();
        List<String> tenants = tenantService.listTenants();
        System.out.println("Number of tenants: " + tenants.size());
        for (String tenant : tenants) {
            System.out.println(tenant);
        }
    }

    @Test
    public void parseFileNameTest() throws AppException {
        String crmTenantId = FileTenantRepository.getCrmTenantId("abc_def");
        assertEquals("def", crmTenantId);
    }
}
