package com.curriculae.tracker.service;

import com.curriculae.tracker.model.Recruiter;
import com.curriculae.tracker.repository.InteractionRepository;
import com.curriculae.tracker.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterService {
    private final RecruiterRepository repository;
    private final InteractionRepository interactionRepository;

    public List<Recruiter> findAll() {
        return repository.findAll();
    }

    public Recruiter findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Recruiter> findByApplicationId(Long applicationId) {
        return repository.findByApplicationId(applicationId);
    }

    public Recruiter save(Recruiter recruiter) {
        if (recruiter.getResponse() == null) {
            recruiter.setResponse("Pendente");
        }
        if (recruiter.getContactDate() == null) {
            recruiter.setContactDate(LocalDate.now());
        }
        return repository.save(recruiter);
    }

    @Transactional
    public void updateResponse(Long id, String response, String notes) {
        repository.findById(id).ifPresent(rec -> {
            rec.setResponse(response);
            rec.setNotes(notes);
            repository.save(rec);
        });
    }

    @Transactional
    public void deleteById(Long id) {
        interactionRepository.deleteByRecruiterId(id);
        repository.deleteById(id);
    }

    public long daysSinceContact(Recruiter rec) {
        if (rec.getContactDate() == null) return 0;
        return ChronoUnit.DAYS.between(rec.getContactDate(), LocalDate.now());
    }


    public long countByResponse(String response) {
        return findAll().stream().filter(r -> response.equals(r.getResponse())).count();
    }
}
