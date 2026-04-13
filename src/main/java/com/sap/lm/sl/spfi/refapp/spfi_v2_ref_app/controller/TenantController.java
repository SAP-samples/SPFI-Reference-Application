package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.State;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.*;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.FileTenantRepository;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.TenantUtils;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.TenantUtils.ETAG_PATTERN;
import static com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.TenantUtils.TENANT_ID_PATTERN;


@RestController
@RequestMapping(path = MockControllerConstants.TENANTS_V2_PATH)
public class TenantController {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);
    private final HttpServletRequest request;
    private static final Set<String> IN_PROGRESS_STATES = getInProgressStates();
    private static final Set<String> UPDATE_ALLOWED_STATES = getUpdateAllowedStates();

    @Autowired
    TenantService tenantService;

    @Autowired
    FileTenantRepository fileTenantRepository;


    @Autowired
    public TenantController(ObjectMapper objectMapper, HttpServletRequest request) {
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        this.request = request;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Tenant>> getTenants() throws AppException {
        logger.debug("Getting tenants");
        List<Tenant> tenants = tenantService.readTenants();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.RETRY_AFTER, "120");
        ResponseEntity<List<Tenant>> responseEntity = new ResponseEntity<>(tenants, headers, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping(path = "{tenantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tenant> getTenantV2(@PathVariable("tenantId") String tenantId) throws AppException {
        if (!TenantUtils.isValidTenantId(tenantId)) {
            throw new InvalidParamException("tenantId", tenantId, "A tenantId must follow the pattern: " + TENANT_ID_PATTERN);
        }
        logger.debug("Getting tenant");
        Tenant tenant = tenantService.getTenant(tenantId);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.RETRY_AFTER, "120");
        TenantUtils tenantUtils = new TenantUtils();
        headers.add("Etag", tenantUtils.generateETag(tenant));

        ResponseEntity<Tenant> responseEntity = new ResponseEntity<>(tenant, headers, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> activateTenant(@RequestBody @Valid Tenant tenantData) throws AppException {
        logger.debug("Activating tenant");

        String sapCrmTenantId = tenantData.getSapId();
        Tenant tenant = fileTenantRepository.readTenantByCrmTenantId(sapCrmTenantId);      // checking if tenant already exists based on sap id
        if (tenant != null) {
            logger.debug("Tenant with CRM-Tenant-ID {} already exists", sapCrmTenantId);
            return prepareResponseEntityForConflict(tenant);
        }

        //boolean isSlowProvisioning = counter.getAndIncrement() % 2 == 0; // Even => slow, odd => fast provisioning
        boolean isSlowProvisioning = true;

        // generating tenant id & sso ids
        String tenantId = UUID.randomUUID().toString();
        tenantData.setId(tenantId);

        TenantUtils tenantUtils = new TenantUtils();
        List<Sso> ssoList = tenantData.getSso();
        if (ssoList != null && !ssoList.isEmpty()) {
            for (Sso sso : ssoList) {
                String ssoId = UUID.randomUUID().toString();
                sso.setId(ssoId);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        Map<String, Object> response;
        String location;
        if (isSlowProvisioning) {
            logger.debug("Slow Provisioning of tenant,  CRM-Tenant-ID: {}", tenantData.getSapId());
            tenantService.slowTenantActivation(tenantData);
            location = tenantUtils.getBaseUrl(request) + "/v2/tenants/" + tenantId + "/status"; // For slow provisioning, return the status URL
            headers.add(HttpHeaders.LOCATION, location);
            response = tenantUtils.prepareResponseForProvisioning(tenantData);
            return new ResponseEntity<>(response, headers, HttpStatus.ACCEPTED);
        } else {
            logger.debug("Fast Provisioning of tenant,  CRM-Tenant-ID: {}", tenantData.getSapId());
            response = tenantService.fastTenantActivation(tenantData, "Active");
            location = tenantUtils.getBaseUrl(request) + "/v2/tenants/" + tenantId;
            headers.add(HttpHeaders.LOCATION, location);
            return new ResponseEntity<>(response, headers, HttpStatus.CREATED);
        }
    }

    @PutMapping(path = "{tenantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateTenant(@PathVariable("tenantId") String tenantId, @RequestHeader("If-Match") String eTag, @RequestBody Tenant tenantData) throws AppException {
        if (!TenantUtils.isValidTenantId(tenantId)) {
            throw new InvalidParamException("tenantId", tenantId, "A tenantId must follow the pattern: " + TENANT_ID_PATTERN);
        }
        if (!TenantUtils.isValidEtag(eTag)) {
            throw new InvalidParamException("eTag", eTag, "An eTag must follow the pattern: " + ETAG_PATTERN);
        }
        if (!fileTenantRepository.isTenantExist(tenantId)) {
            logger.info("Tenant with Tenant-ID {} does not exist", tenantId);
            throw new NotFoundException(MessageFormat.format("Tenant with Tenant-ID {0} does not exist", tenantId));
        }

        Tenant tenant = fileTenantRepository.readTenantByTenantId(tenantId);
        TenantUtils tenantUtils = new TenantUtils();
        HttpHeaders headers = new HttpHeaders();
        String latestEtag = tenantUtils.generateETag(tenant);
        if (!eTag.equals(latestEtag)) {
            return prepareResponseEntityForEtagConflict();
        }

        String currentState = tenant.getStatus().getState();
        if (!UPDATE_ALLOWED_STATES.contains(currentState)) {
            throw new AppException(MessageFormat.format("Tenant is currently {0} process. Update request is rejected.",currentState));
        }

        logger.debug("Updating tenant");
        //boolean isSlowTenantUpdate = counter.getAndIncrement() % 2 == 0; // Even => slow, odd => fast tenant update
        boolean isSlowTenantUpdate = true;
        if (isSlowTenantUpdate) {
            logger.debug("Slow Update of tenant,  CRM-Tenant-ID: {}", tenant.getSapId());
            tenantService.slowTenantUpdate(tenant, tenantData);
            String location = tenantUtils.getBaseUrl(request) + "/v2/tenants/" + tenantId + "/status"; // For slow tenant update, return the status URL
            headers.add(HttpHeaders.LOCATION, location);
            return new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
        } else {
            logger.debug("Fast Update of tenant,  CRM-Tenant-ID: {}", tenantData.getSapId());
            tenantService.fastTenantUpdate(tenantId, tenantData);
            return new ResponseEntity<>(headers, HttpStatus.OK);
        }
    }

    @DeleteMapping(path = "{tenantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteTenant(@PathVariable("tenantId") String tenantId) throws AppException {
        if (!TenantUtils.isValidTenantId(tenantId)) {
            throw new InvalidParamException("tenantId", tenantId, "A tenantId must follow the pattern: " + TENANT_ID_PATTERN);
        }
        Tenant tenant = fileTenantRepository.readTenantByTenantId(tenantId);
        if (tenant == null) {
            logger.info("Tenant with Tenant-ID {} does not exist", tenantId);
            throw new NotFoundException(MessageFormat.format("Tenant with Tenant-ID {0} does not exist", tenantId));
        }

        String currentState = tenant.getStatus().getState();
        if (IN_PROGRESS_STATES.contains(currentState)) {
            throw new AppException(MessageFormat.format("Tenant is currently {0} process. Deletion request is rejected.",currentState));
        }

        logger.debug("Deleting tenant");
        //boolean isSlowTenantDeletion = counter.getAndIncrement() % 2 == 0; // Even => slow, odd => fast deletion
        boolean isSlowTenantDeletion = true;
        TenantUtils tenantUtils = new TenantUtils();
        HttpHeaders headers = new HttpHeaders();
        if (isSlowTenantDeletion) {
            logger.debug("Slow Deletion of tenant,  Tenant-ID: {}", tenantId);
            tenantService.slowTenantDeletion(tenant);
            String location = tenantUtils.getBaseUrl(request) + "/v2/tenants/" + tenantId + "/status"; // For slow provisioning, return the status URL
            headers.add(HttpHeaders.LOCATION, location);
            return new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
        } else {
            logger.debug("Fast Deletion of tenant,  Tenant-ID: {}", tenantId);
            tenantService.fastTenantDeletion(tenantId);
            return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping(path = "{tenantId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Status> getTenantStatus(@PathVariable("tenantId") String tenantId) throws AppException {
        if (!TenantUtils.isValidTenantId(tenantId)) {
            throw new InvalidParamException("tenantId", tenantId, "A tenantId must follow the pattern: " + TENANT_ID_PATTERN);
        }
        logger.debug("Getting tenant status");
        Status status = tenantService.getTenantStatus(tenantId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.RETRY_AFTER, "120");
        ResponseEntity<Status> responseEntity = new ResponseEntity<>(status, headers, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping(path = "{tenantId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> changeTenantStatus(@PathVariable("tenantId") String tenantId, @RequestBody @Valid StateRequest stateRequest) throws AppException {
        if (!TenantUtils.isValidTenantId(tenantId)) {
            throw new InvalidParamException("tenantId", tenantId, "A tenantId must follow the pattern: " + TENANT_ID_PATTERN);
        }
        Tenant tenant = fileTenantRepository.readTenantByTenantId(tenantId);
        if (tenant == null) {
            logger.info("Tenant with Tenant-ID {} does not exist", tenantId);
            throw new NotFoundException(MessageFormat.format("Tenant with Tenant-ID {0} does not exist", tenantId));
        }

        // Validate status transition
        String currentStatus = tenant.getStatus().getState();
        String newStatus = stateRequest.getState();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new AppException("The applied state transition is not allowed.");
        }

        logger.debug("Changing tenant status");

        //boolean isSlowTenantStatusUpdate = counter.getAndIncrement() % 2 == 0; // Even => slow, odd => fast deletion
        boolean isSlowTenantStatusUpdate = true;
        HttpHeaders headers = new HttpHeaders();
        if (isSlowTenantStatusUpdate) {
            logger.debug("Slow Status Update of tenant,  Tenant-ID: {}", tenantId);
            tenantService.slowTenantStatusUpdate(tenantId, stateRequest, tenant);
            headers.add(HttpHeaders.RETRY_AFTER, "120");
            return new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
        } else {
            logger.debug("Fast Status Update of tenant,  Tenant-ID: {}", tenantId);
            Status status = tenantService.fastTenantStatusUpdate(tenantId, stateRequest);
            return new ResponseEntity<>(status, headers, HttpStatus.OK);
        }


    }



    private @NotNull ResponseEntity<Object> prepareResponseEntityForConflict(Tenant tenant) {
        HttpHeaders headers = new HttpHeaders();
        ErrorResponse.ErrorDetail errorDetail = new ErrorResponse.ErrorDetail("900", "Tenant with CRM-Tenant-Id " + tenant.getSapId() + " already exists");
        ErrorResponse errorResponse = new ErrorResponse(
                "409-01",
                "The given resource already exists.",
                "Tenant",
                Collections.singletonList(errorDetail)
        );
        TenantUtils tenantUtils = new TenantUtils();
        String tenantId = tenant.getId();
        String location = tenantUtils.getBaseUrl(request) + "/v2/tenants/" + tenantId + "/status"; // For slow provisioning, return the status URL
        headers.add(HttpHeaders.LOCATION, location);
        return new ResponseEntity<>(errorResponse, headers, HttpStatus.CONFLICT);
    }

    private @NotNull ResponseEntity<Object> prepareResponseEntityForEtagConflict() {
        ErrorResponse.ErrorDetail errorDetail = new ErrorResponse.ErrorDetail("900", "Entity Tag Mismatch");
        ErrorResponse errorResponse = new ErrorResponse(
                "409-03",
                "Precondition missing.",
                "ETAG",
                Collections.singletonList(errorDetail)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    public static Set<String> getInProgressStates() {
        Set<String> inProgressStates = new HashSet<>();
        inProgressStates.add(State.IN_ACTIVATION.getReadableState());
        inProgressStates.add(State.IN_UPDATE.getReadableState());
        inProgressStates.add(State.IN_DELETION.getReadableState());
        inProgressStates.add(State.IN_BLOCKING.getReadableState());
        return inProgressStates;
    }

    public static Set<String> getUpdateAllowedStates() {
        Set<String> updateAllowedStates = new HashSet<>();
        updateAllowedStates.add(State.ACTIVE.getReadableState());
        return updateAllowedStates;
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // Transition rules for status change
        if (newStatus.equals(State.ACTIVE.getState())) {
            return currentStatus.equals(State.BLOCKED.getReadableState()) || currentStatus.equals(State.IN_RECOVERABLE_ERROR.getReadableState());
        } else if (newStatus.equals(State.BLOCKED.getState())) {
            return currentStatus.equals(State.ACTIVE.getReadableState()) || currentStatus.equals(State.IN_RECOVERABLE_ERROR.getReadableState());
        }
        return false;
    }

}
