package com.curriculae.tracker.service;

import com.curriculae.tracker.model.JobApplication;
import com.curriculae.tracker.repository.InteractionRepository;
import com.curriculae.tracker.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobApplicationService {
    private final JobApplicationRepository repository;
    private final InteractionRepository interactionRepository;

    public List<JobApplication> findAll() {
        return repository.findAll();
    }

    public JobApplication findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public JobApplication save(JobApplication application) {
        if (application.getApplicationDate() == null) {
            application.setApplicationDate(LocalDate.now());
        }
        if (application.getLastContactDate() == null) {
            application.setLastContactDate(application.getApplicationDate());
        }
        if (application.getContactCount() == null) {
            application.setContactCount(1);
        }
        return repository.save(application);
    }

    @Transactional
    public void deleteById(Long id) {
        interactionRepository.deleteByApplicationId(id);
        repository.deleteById(id);
    }


    public long daysSinceLastContact(JobApplication app) {
        if (app.getLastContactDate() == null) return 0;
        return ChronoUnit.DAYS.between(app.getLastContactDate(), LocalDate.now());
    }

    public long daysInPipeline(JobApplication app) {
        if (app.getApplicationDate() == null) return 0;
        return ChronoUnit.DAYS.between(app.getApplicationDate(), LocalDate.now());
    }

    public String recommendedAction(JobApplication app) {
        String status = app.getStatus();
        if (status == null) return "Unknown";
        if (status.equals("Rejeitado") || status.equals("Ghosting")) {
            return "Closed";
        }
        long days = daysSinceLastContact(app);
        if (status.equals("Em Avaliação")) {
            return days > 7 ? "Recontact" : "Hot";
        }
        if (status.equals("Aguardam Resposta")) {
            return days > 7 ? "Recontact" : "Wait";
        }
        return "Unknown";
    }

    public String actionIcon(String action) {
        return switch (action) {
            case "Closed" -> "[x]";
            case "Recontact" -> "[!]";
            case "Hot" -> "[*]";
            case "Wait" -> "[-]";
            default -> "[?]";
        };
    }

    public List<JobApplication> findNeedingFollowUp() {
        return findAll().stream()
                .filter(a -> "Recontact".equals(recommendedAction(a)))
                .toList();
    }


    public Map<String, Long> getMetrics() {
        List<JobApplication> all = findAll();
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("total", (long) all.size());
        m.put("interviews", all.stream().filter(a -> "Em Avaliação".equals(a.getStatus())).count());
        m.put("waiting", all.stream().filter(a -> "Aguardam Resposta".equals(a.getStatus())).count());
        m.put("recontact", all.stream().filter(a -> "Recontact".equals(recommendedAction(a))).count());
        m.put("closed", all.stream().filter(a -> {
            String s = a.getStatus();
            return "Rejeitado".equals(s) || "Ghosting".equals(s);
        }).count());
        return m;
    }
}
