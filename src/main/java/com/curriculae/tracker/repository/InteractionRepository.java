package com.curriculae.tracker.repository;

import com.curriculae.tracker.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByApplicationIdOrderByDateDesc(Long applicationId);
    List<Interaction> findByRecruiterIdOrderByDateDesc(Long recruiterId);
    List<Interaction> findAllByOrderByDateDesc();
    void deleteByApplicationId(Long applicationId);
    void deleteByRecruiterId(Long recruiterId);
}
