package com.bidyatra.staff.repository;

import com.bidyatra.staff.model.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface ActivityRepository extends MongoRepository<Activity, String> {
    List<Activity> findByDateOrderByCreatedAtDesc(LocalDate date);
    List<Activity> findAllByOrderByDateDescCreatedAtDesc();
    List<Activity> findByCreatedByOrderByDateDescCreatedAtDesc(String createdBy);
    List<Activity> findByCreatedByAndDateOrderByCreatedAtDesc(String createdBy, LocalDate date);
}
