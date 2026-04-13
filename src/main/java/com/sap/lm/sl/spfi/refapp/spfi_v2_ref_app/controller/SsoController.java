package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller;


import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.exception.ConflictException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.exception.CustomException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.exception.LockTimeoutException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.exception.OperationNotAllowed;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.*;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.service.SsoServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
public class SsoController {

    private final SsoServiceImpl ssoService;

    public SsoController(SsoServiceImpl ssoServiceV2) {
        this.ssoService = ssoServiceV2;
    }

    @GetMapping(path = "/v2/tenants/{tenantId}/sso")
    public ResponseEntity<Object> getSsoConfigs(@PathVariable String tenantId) throws FileNotFoundException {
        try {
            List<Config> configs = ssoService.getSsoConfigs(tenantId);
            long count = configs.stream().filter(config -> !config.getStatus().getState().equals(State.FINAL_ERROR) && !config.getStatus().getState().equals(State.ACTIVE)).count();
            if (count > 0) {
                return ResponseEntity.ok().header("Retry-After", "120").body(configs);
            }
            return ResponseEntity.ok(configs);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(path = "/v2/tenants/{tenantId}/sso")
    public ResponseEntity<Object> provisionSso(@PathVariable String tenantId, @RequestBody @Valid IdentityProvider identityProvider) {
        Config config = null;
        try {
            config = ssoService.provisionSso(identityProvider, tenantId);
            Identifier id = new Identifier();
            id.setId(config.getId());
            if (!config.getStatus().getState().equals(State.FINAL_ERROR) && !config.getStatus().getState().equals(State.ACTIVE)) {
                return ResponseEntity.accepted()
                        .header("Location", String.format("%s/v2/tenant/%s/sso/%s", "HOST", tenantId, config.getId()))
                        .body(id);
            }
            return ResponseEntity.created(new URI(String.format("%s/v2/tenant/%s/sso/%s", "HOST", tenantId, config.getId())))
                    .body(id);
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("Location", String.format("%s/v2/tenant/%s/sso/%s", "HOST", tenantId, e))
                    .body(new Identifier(e.toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(path = "/v2/tenants/{tenantId}/sso/{ssoId}")
    public ResponseEntity<Object> getSsoConfig(@PathVariable String tenantId, @PathVariable String ssoId) throws FileNotFoundException {
        try {
            Optional<Config> optionalConfig = ssoService.getSsoConfig(tenantId, ssoId);
            if (optionalConfig.isEmpty()) {
                throw new FileNotFoundException("SSO config not found");
            } else {
                Config config = optionalConfig.get();
                if (!config.getStatus().getState().equals(State.FINAL_ERROR) && !config.getStatus().getState().equals(State.ACTIVE)) {
                    return ResponseEntity.ok().header("Retry-After", "120").body(config);
                }
                return ResponseEntity.ok(config);
            }

        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping(path = "/v2/tenants/{tenantId}/sso/{ssoId}")
    public ResponseEntity<Object> deleteSsoConfig(@PathVariable String tenantId, @PathVariable String ssoId) throws CustomException {
        try {
            Optional<Config> optionalConfig = ssoService.deleteSsoConfig(tenantId, ssoId);
            if (optionalConfig.isEmpty()) {
                throw new FileNotFoundException("SSO config not found");
            }
            Config config = optionalConfig.get();
            if (config.getStatus().getState().equals(State.IN_DELETION)) {
                return ResponseEntity.accepted()
                        .header("location", String.format("%s/v2/tenant/%s/sso/%s/status", "HOST", tenantId, ssoId))
                        .build();
            }
            return ResponseEntity.noContent().build();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping(path = "/v2/tenants/{tenantId}/sso/{ssoId}/status")
    public ResponseEntity<Status> getSsoConfigStatus(@PathVariable String tenantId, @PathVariable String ssoId) throws FileNotFoundException {
        try {
            Optional<Config> optionalConfig = ssoService.getSsoConfig(tenantId, ssoId);
            if (optionalConfig.isEmpty()) {
                throw new FileNotFoundException(
                        String.format("SSO config not found for tenant: %s and ssoId: %s", tenantId, ssoId));
            }
            Config config = optionalConfig.get();
            HttpHeaders headers = new HttpHeaders();
            if (!config.getStatus().getState().equals(State.FINAL_ERROR) && !config.getStatus().getState().equals(State.ACTIVE)) {
                headers.set("Retry-After", "120");
            }

            return new ResponseEntity<>(config.getStatus(), headers, HttpStatus.OK);

        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
