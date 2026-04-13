package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository;


import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface SsoRepository {

    void save(Config config, String tenantId, String fileId) throws IOException;

    List<Config> findAllByTenantId(String tenantId) throws IOException;

    Optional<Config> findByTenantIdAndSsoId(String tenantId, String ssoId) throws FileNotFoundException;

    void deleteByTenantIdAndSsoId(String tenantId, String ssoId) throws IOException;

}
