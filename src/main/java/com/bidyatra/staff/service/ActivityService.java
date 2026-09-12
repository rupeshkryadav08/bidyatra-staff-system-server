package com.bidyatra.staff.service;

import com.bidyatra.staff.dto.ActivityRequest;
import com.bidyatra.staff.model.Activity;
import com.bidyatra.staff.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository repository;

    public ActivityService(ActivityRepository repository) {
        this.repository = repository;
    }

    public Activity create(ActivityRequest request, String username) {
        Activity a = new Activity();
        apply(a, request);
        a.setCreatedBy(username);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        return repository.save(a);
    }

    public Activity update(String id, ActivityRequest request) {
        Activity a = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        apply(a, request);
        a.setUpdatedAt(LocalDateTime.now());
        return repository.save(a);
    }

    public Activity get(String id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Activity not found")); }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public List<Activity> all() {
        return repository.findAllByOrderByDateDescCreatedAtDesc();
    }

    public List<Activity> byDate(LocalDate date) {
        return repository.findByDateOrderByCreatedAtDesc(date);
    }

    public List<Activity> mine(String username) {
        return repository.findByCreatedByOrderByDateDescCreatedAtDesc(username);
    }

    public List<Activity> mineByDate(String username, LocalDate date) {
        return repository.findByCreatedByAndDateOrderByCreatedAtDesc(username, date);
    }

    private void apply(Activity a, ActivityRequest r) {
        a.setDate(r.date());
        a.setStaffName(r.staffName().trim());
        a.setVisitingLocation(r.visitingLocation() == null ? "" : r.visitingLocation().trim());
        a.setMeetDriver(r.meetDriver());
        a.setLogin(r.login());
        a.setDocumentsIssues(r.documentsIssues());
        a.setOther(r.other() == null ? "" : r.other().trim());
    }
}
