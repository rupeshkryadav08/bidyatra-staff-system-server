package com.bidyatra.staff.controller;

import com.bidyatra.staff.dto.UserRequest;
import com.bidyatra.staff.model.User;
import com.bidyatra.staff.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {
    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public AdminController(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @GetMapping
    public List<User> users() {
        return repository.findAll().stream().peek(u -> u.setPassword(null)).toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserRequest request) {
        String username = request.username().trim();
        if (repository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        User u = new User();
        u.setName(request.name().trim());
        u.setUsername(username);
        u.setPassword(encoder.encode(request.password()));
        u.setRole(request.role());
        u.setEnabled(true);
        u.setCreatedAt(LocalDateTime.now());
        User saved = repository.save(u);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
