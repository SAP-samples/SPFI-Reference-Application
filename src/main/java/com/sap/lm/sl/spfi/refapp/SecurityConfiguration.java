/**
 * SPDX-FileCopyrightText: 2018-2021 SAP SE or an SAP affiliate company and Cloud Security Client Java contributors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sap.lm.sl.spfi.refapp;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sap.lm.sl.spfi.refapp.controllers.ControllersConstants;
import com.sap.lm.sl.spfi.refapp.mocks.MockControllerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity(debug = true) // TODO "debug" may include sensitive information. Do not use in a production system!
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@Profile(value = { "cloud" })
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    public static final String NOTIFY_ROLE = "NOTIFY";
    public static final String SAAS_OPERATOR_ROLE = "SAAS_OPERATOR";
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Inject
    AppConfig appConfig;

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(appConfig.getTenantOpIssuer());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf().disable()
            .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeRequests()
                    .antMatchers(
                        MockControllerConstants.TENANTS_PATH,
                        MockControllerConstants.TENANTS_PATH+"/**")
                    .permitAll()
                    .antMatchers(ControllersConstants.NOTIFY_PATH, 
                                 MockControllerConstants.IASTEST_PATH).hasAuthority(NOTIFY_ROLE)
                    .antMatchers(MockControllerConstants.SAAS_OPERATOR_UPDATE_STATUS_ABS_PATH,
                                 MockControllerConstants.SAAS_OPERATOR_RESOLVE_ABS_PATH).hasAuthority(SAAS_OPERATOR_ROLE)
                    .anyRequest().denyAll()
                .and()
                    .oauth2ResourceServer()
                    .jwt()
                    .jwtAuthenticationConverter(jwtAuthenticationConverter());
                
        // @formatter:on
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter defaults = new JwtGrantedAuthoritiesConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = defaults.convert(jwt);
            if (!validateSignature(jwt)) {
                return authorities;
            }
            if (validateNotifyToken(jwt)) {
                authorities.add(new SimpleGrantedAuthority(NOTIFY_ROLE));
            }
            if (validateMockSaasOpToken(jwt)) {
                authorities.add(new SimpleGrantedAuthority(SAAS_OPERATOR_ROLE));
            }
            return authorities;
        });
        return authenticationConverter;
    }

    private boolean validateNotifyToken(Jwt jwt) {
        String tenantOpClientId = appConfig.getTenantOpClientId();
        String tenantOpIssuer = appConfig.getTenantOpIssuer();
        String issuer = jwt.getClaim("iss");
        String subject = jwt.getClaim("sub");

        if (!issuer.equals(tenantOpIssuer)) {
            logger.error("Invalid issuer (/notify token validation)");
            logger.debug("Invalid issuer: expected: {}, received: {}", tenantOpIssuer, issuer ); // TODO "sensitive information. Do not use in a production system!
            return false;
        }

        if (!subject.equals(tenantOpClientId)) {
            logger.error("Invalid subject (/notify token validation)");
            logger.debug("Invalid subject: expected: {}, received: {}", tenantOpClientId, subject ); // TODO "sensitive information. Do not use in a production system!
            return false;
        }
        return true;
    }

    private boolean validateMockSaasOpToken(Jwt jwt) {
        String clientId = appConfig.getClientId();
        String subject = jwt.getClaim("sub");

        if (!subject.equals(clientId)) {
            logger.error("Invalid subject (mock SAAS operator token validation)");
            logger.debug("Invalid subject: expected: {}, received {}", appConfig.getClientId(), subject ); // TODO "sensitive information. Do not use in a production system!
            return false;
        }
        return true;
    }

    public static boolean validateSignature(Jwt jwt) {
        Map<String, Object> headers = jwt.getHeaders();
        String jku = (String) headers.get("jku");
        String alg = (String) headers.get("alg");
        String token = jwt.getTokenValue();

        URL jkuUrl = null;
        try {
            jkuUrl = new URL(jku);
        } catch (MalformedURLException e) {
            logger.error("Malformed JKU URL: {}", e.getMessage(),e);
            return false;
        }

        URL issuerUrl = null;
        try {
            issuerUrl = new URL(jwt.getClaim("iss"));
        } catch (MalformedURLException e) {
            logger.error("Malformed issuer URL: {}", e.getMessage(),e);
            return false;
        }

        // validate JKU URL
        if (!issuerUrl.getHost().equals(jkuUrl.getHost())) {
            logger.error("Issuer and JKU hosts are not equal");
            return false;
        }

        DecodedJWT decodedJwt = JWT.decode(token);
        JwkProvider provider = new UrlJwkProvider(jkuUrl);
        Jwk jwk;
        try {
            jwk = provider.get(decodedJwt.getKeyId());
            Algorithm algorithm = null;
            if ("RS256".equals(alg)) {
                algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            } else {
                logger.error("Not supported token signing algorithm {}", alg);
            }
            algorithm.verify(decodedJwt);
        } catch (JwkException e) {
            logger.error("Authorization token signature validation failed: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }

}