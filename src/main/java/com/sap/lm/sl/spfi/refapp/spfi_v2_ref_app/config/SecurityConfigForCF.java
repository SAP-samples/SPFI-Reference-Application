package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Profile("cf")
@EnableWebSecurity(debug = true) // TODO "debug" may include sensitive information. Do not use in a production system!
public class SecurityConfigForCF {


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((AbstractHttpConfigurer::disable)) // Disable CSRF protection
                .requiresChannel((channel) -> channel.anyRequest().requiresSecure()) // Require HTTPS for all requests
                .addFilterBefore(new CertificateValidationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(
                        (httpSecurity) -> {
                            httpSecurity
                                    .requestMatchers("/v2/tenants/**").authenticated()
                                    .requestMatchers("/cf/cert/info").permitAll()
                                    .requestMatchers("/health").permitAll()
                                    .anyRequest().denyAll();
                        }); // Configure authorization rules
        return http.build();
    }
}
