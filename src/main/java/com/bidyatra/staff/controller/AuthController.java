package com.bidyatra.staff.controller;

import com.bidyatra.staff.dto.LoginRequest;
import com.bidyatra.staff.dto.OtpVerifyRequest;
import com.bidyatra.staff.model.User;
import com.bidyatra.staff.repository.UserRepository;
import com.bidyatra.staff.security.JwtService;
import com.bidyatra.staff.service.OtpService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OtpService otpService;


    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            OtpService otpService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.otpService = otpService;
    }


    /* =====================================================
       LOGIN
    ===================================================== */

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {

        String username =
                request.username()
                        .trim();


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        request.password()
                )
        );


        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );


        /* =================================================
           ADMIN LOGIN → OTP
        ================================================= */

        if (user.getRole().name().equals("ADMIN")) {

            try {

                otpService.send(
                        user.getUsername()
                );

            } catch (RuntimeException ex) {

                ex.printStackTrace();

                return ResponseEntity
                        .status(503)
                        .body(
                                Map.of(
                                        "message",
                                        "Admin OTP email could not be sent. Check SMTP configuration."
                                )
                        );
            }


            return ResponseEntity.ok(
                    Map.of(
                            "requiresOtp",
                            true,

                            "username",
                            user.getUsername(),

                            "message",
                            "Verification code sent to admin email."
                    )
            );
        }


        /* =================================================
           STAFF LOGIN → DIRECT JWT
        ================================================= */

        return ResponseEntity.ok(
                generateTokenResponse(user)
        );
    }


    /* =====================================================
       VERIFY OTP
    ===================================================== */

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody OtpVerifyRequest request
    ) {

        if (request == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "OTP request is missing."
                            )
                    );
        }


        String username =
                request.username() == null
                        ? ""
                        : request.username()
                        .trim();


        String code =
                request.code() == null
                        ? ""
                        : request.code()
                        .trim();


        System.out.println();
        System.out.println(
                "================================="
        );

        System.out.println(
                "VERIFY OTP REQUEST"
        );

        System.out.println(
                "Username: [" +
                        username +
                        "]"
        );

        System.out.println(
                "Code length: " +
                        code.length()
        );

        System.out.println(
                "================================="
        );


        if (username.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Admin username is missing."
                            )
                    );
        }


        if (!code.matches("\\d{6}")) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "OTP must be exactly 6 digits."
                            )
                    );
        }


        User user =
                userRepository
                        .findByUsername(username)
                        .orElse(null);


        if (user == null) {

            System.out.println(
                    "USER NOT FOUND: " +
                            username
            );

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "message",
                                    "Admin user not found."
                            )
                    );
        }


        if (!user.getRole()
                .name()
                .equals("ADMIN")) {

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "message",
                                    "OTP verification is only available for admin."
                            )
                    );
        }


        boolean valid =
                otpService.verify(
                        username,
                        code
                );


        if (!valid) {

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid or expired verification code."
                            )
                    );
        }


        System.out.println(
                "ADMIN OTP LOGIN SUCCESS"
        );


        return ResponseEntity.ok(
                generateTokenResponse(user)
        );
    }


    /* =====================================================
       JWT
    ===================================================== */

    private Map<String, Object> generateTokenResponse(
            User user
    ) {

        UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" +
                                                user.getRole().name()
                                )
                        )
                );


        String token =
                jwtService.generateToken(
                        userDetails
                );


        return Map.of(
                "token",
                token,

                "name",
                user.getName(),

                "username",
                user.getUsername(),

                "role",
                user.getRole().name()
        );
    }
}