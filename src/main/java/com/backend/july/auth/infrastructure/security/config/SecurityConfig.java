package com.backend.july.auth.infrastructure.security.config;


import static com.backend.july.auth.infrastructure.security.config.SecurityEndpoints.API_DOCS_PUBLIC;
import static com.backend.july.auth.infrastructure.security.config.SecurityEndpoints.AUTH_PUBLIC;
import static com.backend.july.auth.infrastructure.security.config.SecurityEndpoints.SYSTEM_PUBLIC;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(AUTH_PUBLIC).permitAll()
                        .requestMatchers(API_DOCS_PUBLIC).permitAll()
                        .requestMatchers(SYSTEM_PUBLIC).permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}