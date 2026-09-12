package com.bidyatra.staff.service;

import com.bidyatra.staff.model.OtpCode;
import com.bidyatra.staff.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    private final SecureRandom random = new SecureRandom();

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.admin.otp-recipient:}")
    private String otpRecipient;

    public OtpService(
            OtpRepository otpRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender
    ) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    /**
     * Generate and send OTP
     */
    public void send(String adminUsername) {

        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalArgumentException(
                    "Admin username is empty."
            );
        }

        adminUsername = adminUsername.trim();

        if (mailUsername == null || mailUsername.isBlank()) {
            throw new IllegalStateException(
                    "SMTP username is not configured."
            );
        }

        if (otpRecipient == null || otpRecipient.isBlank()) {
            throw new IllegalStateException(
                    "OTP recipient email is not configured."
            );
        }

        // Generate 6 digit OTP
        String code = String.format(
                "%06d",
                random.nextInt(1_000_000)
        );

        // Remove previous OTP
        otpRepository.deleteByUsername(adminUsername);

        // Create OTP
        OtpCode otp = new OtpCode();

        otp.setUsername(adminUsername);

        // Never store plain OTP
        otp.setCodeHash(
                passwordEncoder.encode(code)
        );

        otp.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        otp.setAttempts(0);

        // Save OTP BEFORE sending email
        otpRepository.save(otp);

        System.out.println();
        System.out.println("=================================");
        System.out.println("OTP GENERATED SUCCESSFULLY");
        System.out.println("Admin Username: " + adminUsername);
        System.out.println("OTP: " + code);
        System.out.println("Recipient: " + otpRecipient);
        System.out.println("Expires: " + otp.getExpiresAt());
        System.out.println("=================================");
        System.out.println();

        // Prepare email
        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(mailUsername);

        message.setTo(otpRecipient);

        message.setSubject(
                "BidYatra Admin Verification Code"
        );

        message.setText(
                "Hello,\n\n"
                        + "Your BidYatra Admin verification code is:\n\n"
                        + code
                        + "\n\n"
                        + "This OTP is valid for 5 minutes.\n"
                        + "Do not share this code with anyone.\n\n"
                        + "Regards,\n"
                        + "BidYatra Mobility Technologies Private Limited"
        );

        try {

            mailSender.send(message);

            System.out.println(
                    "OTP EMAIL SENT SUCCESSFULLY"
            );

        } catch (RuntimeException ex) {

            // Remove OTP if email failed
            otpRepository.deleteByUsername(
                    adminUsername
            );

            System.err.println(
                    "OTP EMAIL SEND FAILED"
            );

            ex.printStackTrace();

            throw ex;
        }
    }


    /**
     * Verify OTP
     */
    public boolean verify(
            String username,
            String code
    ) {

        System.out.println();
        System.out.println("=================================");
        System.out.println("OTP VERIFICATION STARTED");
        System.out.println("Username received: [" + username + "]");
        System.out.println(
                "Code length: " +
                        (code == null ? 0 : code.length())
        );
        System.out.println("=================================");


        // Validate username
        if (username == null ||
                username.isBlank()) {

            System.out.println(
                    "OTP ERROR: USERNAME EMPTY"
            );

            return false;
        }


        // Validate code
        if (code == null) {

            System.out.println(
                    "OTP ERROR: CODE NULL"
            );

            return false;
        }


        username = username.trim();

        code = code.trim();


        // OTP must contain exactly 6 digits
        if (!code.matches("\\d{6}")) {

            System.out.println(
                    "OTP ERROR: INVALID FORMAT"
            );

            return false;
        }


        // Find OTP
        OtpCode otp =
                otpRepository
                        .findTopByUsernameOrderByExpiresAtDesc(
                                username
                        )
                        .orElse(null);


        // OTP not found
        if (otp == null) {

            System.out.println(
                    "OTP ERROR: OTP NOT FOUND FOR USER"
            );

            System.out.println(
                    "Username searched: [" +
                            username +
                            "]"
            );

            return false;
        }


        System.out.println(
                "OTP RECORD FOUND"
        );

        System.out.println(
                "OTP Username: [" +
                        otp.getUsername() +
                        "]"
        );

        System.out.println(
                "OTP Expiry: " +
                        otp.getExpiresAt()
        );

        System.out.println(
                "Current Time: " +
                        LocalDateTime.now()
        );

        System.out.println(
                "Attempts: " +
                        otp.getAttempts()
        );


        // Check expiry
        if (otp.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            System.out.println(
                    "OTP ERROR: OTP EXPIRED"
            );

            otpRepository.delete(otp);

            return false;
        }


        // Check attempts
        if (otp.getAttempts() >= 5) {

            System.out.println(
                    "OTP ERROR: MAXIMUM ATTEMPTS REACHED"
            );

            otpRepository.delete(otp);

            return false;
        }


        // Increase attempt
        otp.setAttempts(
                otp.getAttempts() + 1
        );


        // BCrypt comparison
        boolean matched =
                passwordEncoder.matches(
                        code,
                        otp.getCodeHash()
                );


        System.out.println(
                "OTP MATCH RESULT: " +
                        matched
        );


        // Correct OTP
        if (matched) {

            System.out.println(
                    "OTP VERIFIED SUCCESSFULLY"
            );

            otpRepository.delete(otp);

            return true;
        }


        // Wrong OTP
        System.out.println(
                "OTP ERROR: WRONG OTP"
        );


        if (otp.getAttempts() >= 5) {

            otpRepository.delete(otp);

        } else {

            otpRepository.save(otp);
        }


        return false;
    }
}