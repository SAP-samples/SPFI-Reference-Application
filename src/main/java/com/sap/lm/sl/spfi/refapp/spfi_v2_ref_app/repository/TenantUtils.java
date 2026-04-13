package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller.AppException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.Sso;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.Status;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.StatusDetails;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.Tenant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.bouncycastle.jcajce.provider.digest.SHA3;


import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.FileTenantRepository.TIMESTAMP_WEB_FORMAT;


@Service
public class TenantUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String TENANT_ID_PATTERN = "^[a-z0-9]+([-]?[a-z0-9])*$";
    public static final String ETAG_PATTERN = "^[a-f0-9]{64}$";

    public static boolean isValidValue(String value, String pattern) {
        value = value == null ? "" : value.trim();
        if (value.isEmpty()) {
            return false; // The value cannot be empty or contain only spaces
        }

        // Check if value matches the given pattern
        return Pattern.matches(pattern, value);
    }

    public static boolean isValidTenantId(String tenantId) {
        return isValidValue(tenantId, TENANT_ID_PATTERN);
    }

    public static boolean isValidEtag(String eTag) {
        return isValidValue(eTag, ETAG_PATTERN);
    }

    public String getBaseUrl(HttpServletRequest request) {
        // Construct the base URL dynamically using the request information
        String protocol = request.getScheme(); // "http" or "https"
        String host = request.getServerName(); // Hostname or IP address
        int port = request.getServerPort(); // Port number

        // If the port is 80 (HTTP) or 443 (HTTPS), you don't need to include it in the URL
        String portPart = (port == 80 || port == 443) ? "" : ":" + port;

        // Build and return the full base URL
        return protocol + "://" + host + portPart;
    }

    public String generateETag(Object tenant) throws AppException {
        try {
            // Serialize the object to JSON
            String jsonString = objectMapper.writeValueAsString(tenant);
            // Create a MessageDigest instance for SHA3-256 from BouncyCastle
            MessageDigest digest = new SHA3.Digest256();

            // Compute the hash
            byte[] hash = digest.digest(jsonString.getBytes());

            // Print the hash in hexadecimal format
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (JsonProcessingException e) {
            throw new AppException("Exception while generating ETag", e);
        }
    }

    public Status getStatus(String state) {
        Status status = new Status();
        status.setState(state);

        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_WEB_FORMAT);
        String currentDataTime = dateFormat.format(date);
        status.setLastModified(currentDataTime);

        StatusDetails statusDetails = new StatusDetails();
        statusDetails.setCode(state);
        switch (state) {
            case "Active":
                statusDetails.setCode("active");
                statusDetails.setMessage("The resource is fully active and functional.");
                break;
            case "Blocked":
                statusDetails.setCode("blocked");
                statusDetails.setMessage("The resource is fully blocked.");
                break;
            case "In Activation":
                statusDetails.setCode("in-activation");
                statusDetails.setMessage("The resource is currently in its (re)activation process.");
                break;
            case "In Update":
                statusDetails.setCode("in-update");
                statusDetails.setMessage("The resource is currently in the process of being updated.");
                break;
            case "In Blocking":
                statusDetails.setCode("in-blocking");
                statusDetails.setMessage("The resource is currently in the process to be blocked.");
                break;
            case "In Deletion":
                statusDetails.setCode("in-deletion");
                statusDetails.setMessage("The resource is currently in the process of being deleted.");
                break;
            case "In Self-Recoverable Error":
                statusDetails.setCode("in-self-recoverable-error");
                statusDetails.setMessage("The resource is in a self-recoverable error.");
                break;
            case "In Recoverable Error":
                statusDetails.setCode("in-recoverable-error");
                statusDetails.setMessage("The resource is in a recoverable error.");
                break;
            case "Final Error":
                statusDetails.setCode("final-error");
                statusDetails.setMessage("The resource is in a final non-recoverable error.");
                break;
            default:
                statusDetails.setMessage("Unknown");
        }

        status.setDetails(statusDetails);
        return status;
    }

    public Map<String, Object> prepareResponseForProvisioning(@RequestBody @Valid Tenant tenantData) {
        Map<String, Object> response = new HashMap<>();
        if (tenantData.getSso() != null && !tenantData.getSso().isEmpty()) {
            List<String> ssoIds = new ArrayList<>();
            for (Sso sso : tenantData.getSso()) {
                ssoIds.add(sso.getId());
            }
            response.put("tenantId", tenantData.getId());
            response.put("ssoId", ssoIds);
        } else {
            response.put("id", tenantData.getId());     //if sso is not there, just return tenant id
        }
        return response;
    }

    public Tenant setTenantState(Tenant tenant, String state) {
        TenantUtils tenantUtils = new TenantUtils();
        // generating sso details & updating it in tenant
        List<Sso> ssoList = tenant.getSso();
        if (ssoList != null && !ssoList.isEmpty()) {
            for (Sso sso : ssoList) {
                Status status = tenantUtils.getStatus(state); // Use dynamic state
                sso.setStatus(status);
            }
        }

        // generating tenant status details based on the passed state
        Status status = tenantUtils.getStatus(state);
        tenant.setStatus(status);
        return tenant;
    }
}
