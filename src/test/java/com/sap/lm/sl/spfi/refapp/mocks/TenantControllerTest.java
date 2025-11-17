package com.sap.lm.sl.spfi.refapp.mocks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import com.sap.lm.sl.spfi.refapp.services.TenantService;
import com.sap.lm.sl.spfi.refapp.services.TenantServiceImpl;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@AutoConfigureMockMvc
public class TenantControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getTenants() throws Exception {
        this.mockMvc.perform(get(MockControllerConstants.TENANTS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void getTenant() throws Exception {
        TenantService tenantService = new TenantServiceImpl();
        List<String> tenants = tenantService.listTenants();
        if (tenants.isEmpty()) {
            fail("No tenants yet");
        }
        String tenantId = tenants.get(0);
        this.mockMvc.perform(get(MockControllerConstants.TENANT_PATH.replace("{tenantId}", tenantId)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.sapCrmId", is(tenantId)));
    }

    @Test
    public void getTenantNotFound() throws Exception {
        this.mockMvc.perform(get(MockControllerConstants.TENANT_PATH.replace("{tenantId}", "123")))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.errorType", is("com.sap.lm.sl.spfi.refapp.controllers.NotFoundException")));
    }

}
