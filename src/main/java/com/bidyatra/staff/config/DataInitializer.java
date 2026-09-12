package com.bidyatra.staff.config;

import com.bidyatra.staff.model.Role;
import com.bidyatra.staff.model.User;
import com.bidyatra.staff.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createInitialAdmin(
            UserRepository repository,
            PasswordEncoder encoder,
            @Value("${app.admin.username}") String username,
            @Value("${app.admin.password}") String password) {

        return args -> {
            if (!repository.existsByUsername(username)) {
                User admin = new User();
                admin.setName("BidYatra Admin");
                admin.setUsername(username);
                admin.setPassword(encoder.encode(password));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                admin.setCreatedAt(LocalDateTime.now());
                repository.save(admin);
            }
        };
    }
}
