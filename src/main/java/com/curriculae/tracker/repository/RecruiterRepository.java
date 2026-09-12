package com.curriculae.tracker.repository;

import com.curriculae.tracker.model.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    List<Recruiter> findByApplicationId(Long applicationId);
}
