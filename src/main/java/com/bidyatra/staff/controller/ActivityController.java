package com.bidyatra.staff.controller;

import com.bidyatra.staff.dto.ActivityRequest;
import com.bidyatra.staff.model.Activity;
import com.bidyatra.staff.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    @GetMapping
    public List<Activity> all(@RequestParam(required = false) String date, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (admin) {
            if (date != null && !date.isBlank()) return service.byDate(LocalDate.parse(date));
            return service.all();
        }
        if (date != null && !date.isBlank()) return service.mineByDate(authentication.getName(), LocalDate.parse(date));
        return service.mine(authentication.getName());
    }

    @PostMapping
    public Activity create(@Valid @RequestBody ActivityRequest request,
                           Authentication authentication) {
        return service.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public Activity update(@PathVariable String id,
                           @Valid @RequestBody ActivityRequest request, Authentication authentication) {
        Activity existing = service.get(id);
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !existing.getCreatedBy().equals(authentication.getName())) throw new org.springframework.security.access.AccessDeniedException("Not your record");
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication authentication) {
        Activity existing = service.get(id);
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !existing.getCreatedBy().equals(authentication.getName())) throw new org.springframework.security.access.AccessDeniedException("Not your record");
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
