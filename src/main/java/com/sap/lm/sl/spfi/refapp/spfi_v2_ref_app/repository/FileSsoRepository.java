package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.Config;
import org.json.JSONObject;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
public class FileSsoRepository implements SsoRepository {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String parentDirectory;

    public FileSsoRepository() {
        String tempDirectory = System.getenv("TEMP");
        parentDirectory = Objects.requireNonNullElse(tempDirectory, "/tmp");
    }

    public void save(Config config, String tenantId, String fileId) throws IOException {
        JSONFileStore JSONFileStore = new JSONFileStore(this.getUploadPath(tenantId));
        String fileName = JSONFileStore.extendWithFileFormat(fileId);
        JSONFileStore.store(convertToJson(config), fileName);
    }

    @Override
    public List<Config> findAllByTenantId(String tenantId) throws IOException {
        JSONFileStore JSONFileStore = new JSONFileStore(this.getUploadPath(tenantId));
        List<JSONObject> jsons = JSONFileStore.fetchAll();
        return jsons.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json.toString(), Config.class);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Config> findByTenantIdAndSsoId(String tenantId, String ssoId)  {
        JSONFileStore JSONFileStore = new JSONFileStore(this.getUploadPath(tenantId));
        String fileName = JSONFileStore.extendWithFileFormat(ssoId);
        try {
            JSONObject json = JSONFileStore.fetch(fileName);
            return Optional.of(objectMapper.readValue(json.toString(), Config.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteByTenantIdAndSsoId(String tenantId, String ssoId) throws IOException {
        JSONFileStore JSONFileStore = new JSONFileStore(this.getUploadPath(tenantId));
        String fileName = JSONFileStore.extendWithFileFormat(ssoId);
        JSONFileStore.delete(fileName);
    }

    public String getUploadPath(String tenantId) {
        return String.format("%s%stenants%s%s/sso%s", parentDirectory, File.separator, File.separator, tenantId, File.separator);
    }

    // convert ssoConfig to json
    public JSONObject convertToJson(Config ssoConfig) {
        return new JSONObject(ssoConfig);
    }

}
