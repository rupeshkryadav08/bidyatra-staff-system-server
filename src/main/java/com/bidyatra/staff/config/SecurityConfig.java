package com.bidyatra.staff.config;

import com.bidyatra.staff.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /* =========================================================
       PASSWORD ENCODER
    ========================================================= */

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /* =========================================================
       AUTHENTICATION PROVIDER
    ========================================================= */

    @Bean
    DaoAuthenticationProvider authenticationProvider(
            UserDetailsService service,
            PasswordEncoder encoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(service);
        provider.setPasswordEncoder(encoder);

        return provider;
    }


    /* =========================================================
       AUTHENTICATION MANAGER
    ========================================================= */

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }


    /* =========================================================
       SECURITY FILTER CHAIN
    ========================================================= */

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter)
            throws Exception {

        http

                /* -------------------------------------------------
                   CSRF
                ------------------------------------------------- */

                .csrf(csrf -> csrf.disable())


                /* -------------------------------------------------
                   CORS
                ------------------------------------------------- */

                .cors(cors -> {})


                /* -------------------------------------------------
                   SESSION
                ------------------------------------------------- */

                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                /* -------------------------------------------------
                   AUTHORIZATION
                ------------------------------------------------- */

                .authorizeHttpRequests(auth -> auth

                        /* =========================================
                           PUBLIC STATIC FILES
                        ========================================= */

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/style.css",
                                "/script.js",
                                "/logo.jpeg",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()


                        /* =========================================
                           PUBLIC AUTH ENDPOINTS

                           IMPORTANT:
                           Both login and OTP verification happen
                           BEFORE JWT is created.
                        ========================================= */

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/verify-otp"
                        ).permitAll()


                        /* =========================================
                           ADMIN ONLY
                        ========================================= */

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")


                        /* =========================================
                           REPORTS
                        ========================================= */

                        .requestMatchers(
                                "/api/reports/**"
                        ).authenticated()


                        /* =========================================
                           ACTIVITIES
                        ========================================= */

                        .requestMatchers(
                                "/api/activities/**"
                        ).authenticated()


                        /* =========================================
                           OTHER API
                        ========================================= */

                        .requestMatchers(
                                "/api/**"
                        ).authenticated()


                        /* =========================================
                           EVERYTHING ELSE
                        ========================================= */

                        .anyRequest().permitAll()
                )


                /* -------------------------------------------------
                   JWT FILTER
                ------------------------------------------------- */

                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}