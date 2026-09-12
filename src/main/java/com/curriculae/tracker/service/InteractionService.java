package com.curriculae.tracker.service;

import com.curriculae.tracker.model.Interaction;
import com.curriculae.tracker.repository.InteractionRepository;
import com.curriculae.tracker.repository.JobApplicationRepository;
import com.curriculae.tracker.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InteractionService {
    private final InteractionRepository repository;
    private final JobApplicationRepository applicationRepository;
    private final RecruiterRepository recruiterRepository;

    public List<Interaction> findAll() {
        return repository.findAll();
    }

    public List<Interaction> findByApplicationId(Long applicationId) {
        return repository.findByApplicationIdOrderByDateDesc(applicationId);
    }

    public List<Interaction> findByRecruiterId(Long recruiterId) {
        return repository.findByRecruiterIdOrderByDateDesc(recruiterId);
    }

    /**
     * Logs an interaction and auto-updates the parent entity's last contact date
     * and contact count, mirroring the Python app's behavior.
     */
    @Transactional
    public Interaction logInteraction(Interaction interaction) {
        Interaction saved = repository.save(interaction);

        // Auto-update the parent job application
        if (interaction.getApplicationId() != null) {
            applicationRepository.findById(interaction.getApplicationId()).ifPresent(app -> {
                LocalDate interactionDate = interaction.getDate() != null ? interaction.getDate() : LocalDate.now();
                if (app.getLastContactDate() == null || interactionDate.isAfter(app.getLastContactDate())) {
                    app.setLastContactDate(interactionDate);
                }
                app.setContactCount((app.getContactCount() != null ? app.getContactCount() : 0) + 1);
                applicationRepository.save(app);
            });
        }

        // Auto-update the parent recruiter
        if (interaction.getRecruiterId() != null) {
            recruiterRepository.findById(interaction.getRecruiterId()).ifPresent(rec -> {
                LocalDate interactionDate = interaction.getDate() != null ? interaction.getDate() : LocalDate.now();
                if (rec.getContactDate() == null || interactionDate.isAfter(rec.getContactDate())) {
                    rec.setContactDate(interactionDate);
                }
                recruiterRepository.save(rec);
            });
        }

        return saved;
    }

    /**
     * Auto-update status on the parent entity based on interaction type.
     * E.g. "Rejeição" → set application status to "Rejeitado".
     */
    @Transactional
    public void autoUpdateStatus(Interaction interaction) {
        String type = interaction.getType();
        if (type == null) return;

        if (interaction.getApplicationId() != null) {
            applicationRepository.findById(interaction.getApplicationId()).ifPresent(app -> {
                switch (type) {
                    case "Rejeição" -> app.setStatus("Rejeitado");
                    case "Entrevista" -> app.setStatus("Em Avaliação");
                }
                applicationRepository.save(app);
            });
        }

        if (interaction.getRecruiterId() != null) {
            recruiterRepository.findById(interaction.getRecruiterId()).ifPresent(rec -> {
                switch (type) {
                    case "Resposta Recebida", "Entrevista" -> rec.setResponse("Positivo");
                    case "Rejeição" -> rec.setResponse("Negativo");
                }
                recruiterRepository.save(rec);
            });
        }
    }
}
