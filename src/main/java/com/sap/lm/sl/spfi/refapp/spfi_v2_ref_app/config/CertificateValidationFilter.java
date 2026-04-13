package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

public class CertificateValidationFilter extends OncePerRequestFilter {

    private final Logger log = LoggerFactory.getLogger(CertificateValidationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        String clientCert = request.getHeader("X-Forwarded-Client-Cert");
        String clientSubjectCn = request.getHeader("X-Ssl-Client-Subject-Cn");
        String clientIssuerDn = request.getHeader("X-Ssl-Client-Issuer-Dn");
        String clientRootCaDn = request.getHeader("X-Ssl-Client-Root-Ca-Dn");
        String clientNotBefore = request.getHeader("X-Ssl-Client-Notbefore");
        String clientNotAfter = request.getHeader("X-Ssl-Client-Notafter");
        // log the certificate information
        log.debug("clientCert : {}", clientCert);
        log.debug("clientSubjectCn : {}", clientSubjectCn);
        log.debug("clientIssuerDn : {}", clientIssuerDn);
        log.debug("clientRootCaDn : {}", clientRootCaDn);
        log.debug("clientNotBefore : {}", clientNotBefore);
        log.debug("clientNotAfter : {}", clientNotAfter);

        // Additional validation logic based on headers
        if (clientCert == null || !isValidCertificate(clientCert)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Client Certificate");
            return;
        }

        if (clientNotBefore != null && clientNotAfter != null) {
            // Example: Validate the expiration date of the certificate
            if (!isCertificateValid(clientNotBefore, clientNotAfter)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Client Certificate Expired");
                return;
            }
        }
        // If the certificate is valid, set the authentication in the SecurityContext
        authenticateRequest(clientSubjectCn);

        filterChain.doFilter(request, response); // Continue the request processing
    }

    // Example validation methods
    private boolean isValidCertificate(String cert) {
        // Implement certificate validation logic here (e.g., check if it's in a valid format)
        return true;  // Placeholder for actual logic
    }

    private boolean isCertificateValid(String notBefore, String notAfter) {
        // Implement expiration date validation logic here
        return true;  // Placeholder for actual logic
    }

    private void authenticateRequest(String clientSubjectCn) {
        // Create a custom authentication based on the certificate information (e.g., clientSubjectCn)
        if (clientSubjectCn != null) {
            // You could use the CN as a principal or any other field to create authentication
            Authentication authentication = new UsernamePasswordAuthenticationToken(clientSubjectCn, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication); // Set the authentication in the SecurityContext
        }
    }
}
