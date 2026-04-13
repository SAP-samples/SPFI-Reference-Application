package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

@Profile("cf")
@RestController
public class CfCertificatePassthroughController {

    @GetMapping("/cf/cert/info")
    public ResponseEntity<Object> getCfCertInfo(HttpServletRequest request) throws IOException, URISyntaxException {

        String clientCert = request.getHeader("X-Forwarded-Client-Cert");
        String clientSubjectCn = request.getHeader("X-Ssl-Client-Subject-Cn");
        String clientIssuerDn = request.getHeader("X-Ssl-Client-Issuer-Dn");
        String clientRootCaDn = request.getHeader("X-Ssl-Client-Root-Ca-Dn");
        String clientNotBefore = request.getHeader("X-Ssl-Client-Notbefore");
        String clientNotAfter = request.getHeader("X-Ssl-Client-Notafter");

        return ResponseEntity.ok().body("clientCert : " + clientCert + "\n" +
                "clientSubjectCn : " + clientSubjectCn + "\n" +
                "clientIssuerDn : " + clientIssuerDn + "\n" +
                "clientRootCaDn : " + clientRootCaDn + "\n" +
                "clientNotBefore : " + clientNotBefore + "\n" +
                "clientNotAfter : " + clientNotAfter);
    }
}
