package com.sap.lm.sl.spfi.refapp.mocks;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.lm.sl.spfi.refapp.controllers.AppException;
import com.sap.lm.sl.spfi.refapp.services.AppTenant;
import com.sap.lm.sl.spfi.refapp.services.TenantService;
import com.sap.lm.sl.spfi.refapp.services.TenantServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping(path = MockControllerConstants.TENANTS_PATH)
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;


    @org.springframework.beans.factory.annotation.Autowired
    public TenantController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        this.request = request;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getTenants() throws AppException {
        logger.debug("Getting tenants");
        TenantService tenantService = new TenantServiceImpl();
        List<String> tenants = tenantService.listTenants();
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<List<String>> responseEntity = new ResponseEntity<>(tenants, headers, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping(path = "{tenantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppTenant> getTenant(@PathVariable("tenantId") String tenantId) throws AppException {
        logger.debug("Getting tenant");
        TenantService tenantService = new TenantServiceImpl();
        AppTenant tenant = tenantService.getTenant(tenantId);
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<AppTenant> responseEntity = new ResponseEntity<>(tenant, headers, HttpStatus.OK);
        return responseEntity;
    }

}
