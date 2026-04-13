package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("default")
@EnableWebSecurity(debug = true) // TODO "debug" may include sensitive information. Do not use in a production system!
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.
                csrf((AbstractHttpConfigurer::disable)) // Disable CSRF protection
                .requiresChannel((channel) -> channel.anyRequest().requiresSecure()) // Require HTTPS for all requests
                .x509(
                        (x509) -> x509
                                .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                                .userDetailsService(x509UserDetailsService()))  // Configure X.509 authentication
                .authorizeHttpRequests(
                        (httpSecurity) -> {
                            httpSecurity
                                    .requestMatchers("/v2/tenants/**").authenticated()
                                    .requestMatchers("/health").permitAll()
                                    .anyRequest().denyAll();
                        }); // Configure authorization rules
        return http.build();
    }

    @Bean
    public UserDetailsService x509UserDetailsService() {
        return username -> {
            System.out.println("Extracted CN: " + username);

            return User.withUsername(username)
                    .password("")
                    .authorities("ROLE_USER")
                    .build();
        };
    }


}