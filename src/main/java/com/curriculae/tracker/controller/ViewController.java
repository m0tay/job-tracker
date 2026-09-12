package com.curriculae.tracker.controller;

import com.curriculae.tracker.model.Interaction;
import com.curriculae.tracker.model.JobApplication;
import com.curriculae.tracker.model.Recruiter;
import com.curriculae.tracker.repository.InteractionRepository;
import com.curriculae.tracker.service.InteractionService;
import com.curriculae.tracker.service.JobApplicationService;
import com.curriculae.tracker.service.RecruiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final JobApplicationService applicationService;
    private final RecruiterService recruiterService;
    private final InteractionService interactionService;
    private final InteractionRepository interactionRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM");


    @GetMapping("/")
    public String dashboard(Model model,
                            @RequestParam(required = false) String statusFilter) {
        List<JobApplication> all = applicationService.findAll();
        Map<String, Long> metrics = applicationService.getMetrics();
        model.addAttribute("metrics", metrics);

        // Build enriched rows for the pipeline table
        List<Map<String, Object>> rows = new ArrayList<>();
        for (JobApplication app : all) {
            String action = applicationService.recommendedAction(app);
            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("All")) {
                if (!app.getStatus().equals(statusFilter)) continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", app.getId());
            row.put("company", app.getCompany());
            row.put("position", app.getPosition());
            row.put("status", app.getStatus());
            row.put("action", applicationService.actionIcon(action) + " " + action);
            row.put("actionRaw", action);
            row.put("daysSinceContact", applicationService.daysSinceLastContact(app));
            row.put("lastContact", app.getLastContactDate() != null ? app.getLastContactDate().format(FMT) : "—");
            row.put("contactCount", app.getContactCount() != null ? app.getContactCount() : 0);
            row.put("details", app.getDetails() != null ? app.getDetails() : "");
            rows.add(row);
        }
        model.addAttribute("rows", rows);

        // Status options for filter dropdown
        Set<String> statuses = all.stream().map(JobApplication::getStatus).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        model.addAttribute("statuses", statuses);
        model.addAttribute("statusFilter", statusFilter);

        return "dashboard";
    }


    @GetMapping("/applications/new")
    public String newApplicationForm(Model model) {
        model.addAttribute("app", new JobApplication());
        return "application-form";
    }

    @PostMapping("/applications/new")
    public String createApplication(@ModelAttribute JobApplication app, RedirectAttributes ra) {
        applicationService.save(app);
        ra.addFlashAttribute("success", "Application for " + app.getCompany() + " saved!");
        return "redirect:/";
    }


    @GetMapping("/applications/{id}/edit")
    public String editApplicationForm(@PathVariable Long id, Model model) {
        JobApplication app = applicationService.findById(id);
        if (app == null) return "redirect:/";
        model.addAttribute("app", app);
        model.addAttribute("editing", true);
        return "application-form";
    }

    @PostMapping("/applications/{id}/edit")
    public String updateApplication(@PathVariable Long id, @ModelAttribute JobApplication app, RedirectAttributes ra) {
        app.setId(id);
        // Preserve fields not in the form
        JobApplication existing = applicationService.findById(id);
        if (existing != null) {
            if (app.getApplicationDate() == null) app.setApplicationDate(existing.getApplicationDate());
            if (app.getLastContactDate() == null) app.setLastContactDate(existing.getLastContactDate());
            if (app.getContactCount() == null) app.setContactCount(existing.getContactCount());
            if (app.getCompanyFolder() == null) app.setCompanyFolder(existing.getCompanyFolder());
        }
        applicationService.save(app);
        ra.addFlashAttribute("success", "Application updated!");
        return "redirect:/";
    }

    @PostMapping("/applications/{id}/delete")
    public String deleteApplication(@PathVariable Long id, RedirectAttributes ra) {
        applicationService.deleteById(id);
        ra.addFlashAttribute("success", "Application deleted.");
        return "redirect:/";
    }


    @GetMapping("/interactions/new")
    public String newInteractionForm(Model model) {
        List<JobApplication> apps = applicationService.findAll().stream()
                .filter(a -> !"Rejeitado".equals(a.getStatus()) && !"Ghosting".equals(a.getStatus()))
                .toList();
        List<Recruiter> recruiters = recruiterService.findAll();
        model.addAttribute("applications", apps);
        model.addAttribute("recruiters", recruiters);
        model.addAttribute("interaction", new Interaction());
        return "interaction-form";
    }

    @PostMapping("/interactions/new")
    public String createInteraction(@ModelAttribute Interaction interaction,
                                    @RequestParam(required = false) String autoUpdate,
                                    RedirectAttributes ra) {
        interactionService.logInteraction(interaction);
        if ("true".equals(autoUpdate)) {
            interactionService.autoUpdateStatus(interaction);
        }
        ra.addFlashAttribute("success", "Interaction logged!");
        return "redirect:/history";
    }


    @GetMapping("/follow-up")
    public String followUp(Model model) {
        List<JobApplication> needFollowUp = applicationService.findNeedingFollowUp();
        List<Map<String, Object>> entries = new ArrayList<>();
        for (JobApplication app : needFollowUp) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("app", app);
            entry.put("daysSinceContact", applicationService.daysSinceLastContact(app));
            entry.put("dateFmt", app.getApplicationDate() != null ? app.getApplicationDate().format(FMT) : "—");
            entries.add(entry);
        }
        model.addAttribute("entries", entries);
        return "follow-up";
    }


    @GetMapping("/history")
    public String history(Model model, @RequestParam(required = false) Long companyId) {
        List<Interaction> interactions;
        if (companyId != null) {
            interactions = interactionService.findByApplicationId(companyId);
        } else {
            interactions = interactionRepository.findAllByOrderByDateDesc();
        }

        // Enrich each interaction with company/recruiter name
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (Interaction i : interactions) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("interaction", i);
            entry.put("date", i.getDate() != null ? i.getDate().format(FMT) : "—");

            String target = "";
            if (i.getApplicationId() != null) {
                JobApplication app = applicationService.findById(i.getApplicationId());
                if (app != null) target = app.getCompany();
            }
            if (i.getRecruiterId() != null) {
                Recruiter rec = recruiterService.findById(i.getRecruiterId());
                if (rec != null) {
                    target += (target.isEmpty() ? "" : " · ") + rec.getName();
                }
            }
            entry.put("target", target);
            timeline.add(entry);
        }

        model.addAttribute("timeline", timeline);
        model.addAttribute("totalInteractions", interactions.size());
        model.addAttribute("companiesContacted", interactions.stream()
                .map(Interaction::getApplicationId).filter(Objects::nonNull).distinct().count());

        // Company list for filter
        model.addAttribute("applications", applicationService.findAll());
        model.addAttribute("companyId", companyId);

        return "history";
    }


    @GetMapping("/recruiters")
    public String recruitersList(Model model,
                                 @RequestParam(required = false) String responseFilter) {
        List<Recruiter> all = recruiterService.findAll();

        // Metrics
        model.addAttribute("totalRecruiters", all.size());
        model.addAttribute("positiveCount", recruiterService.countByResponse("Positivo"));
        model.addAttribute("negativeCount", recruiterService.countByResponse("Negativo"));
        model.addAttribute("ghostCount", recruiterService.countByResponse("Ghost"));
        model.addAttribute("pendingCount", recruiterService.countByResponse("Pendente"));

        // Filtered list
        List<Recruiter> filtered = all;
        if (responseFilter != null && !responseFilter.isEmpty() && !responseFilter.equals("All")) {
            filtered = all.stream().filter(r -> responseFilter.equals(r.getResponse())).toList();
        }

        // Enrich rows
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Recruiter rec : filtered) {
            Map<String, Object> row = new HashMap<>();
            row.put("recruiter", rec);
            row.put("daysSinceContact", recruiterService.daysSinceContact(rec));
            row.put("dateFmt", rec.getContactDate() != null ? rec.getContactDate().format(FMT) : "—");
            rows.add(row);
        }
        model.addAttribute("rows", rows);
        model.addAttribute("responseFilter", responseFilter);

        return "recruiters";
    }

    @GetMapping("/recruiters/new")
    public String newRecruiterForm(Model model) {
        model.addAttribute("recruiter", new Recruiter());
        model.addAttribute("applications", applicationService.findAll());
        return "recruiter-form";
    }

    @PostMapping("/recruiters/new")
    public String createRecruiter(@ModelAttribute Recruiter recruiter, RedirectAttributes ra) {
        recruiterService.save(recruiter);
        ra.addFlashAttribute("success", "Recruiter " + recruiter.getName() + " saved!");
        return "redirect:/recruiters";
    }

    @GetMapping("/recruiters/{id}/edit")
    public String editRecruiterForm(@PathVariable Long id, Model model) {
        Recruiter rec = recruiterService.findById(id);
        if (rec == null) return "redirect:/recruiters";
        model.addAttribute("recruiter", rec);
        return "recruiter-response-form";
    }

    @PostMapping("/recruiters/{id}/update-response")
    public String updateRecruiterResponse(@PathVariable Long id,
                                          @RequestParam String response,
                                          @RequestParam(required = false) String notes,
                                          RedirectAttributes ra) {
        recruiterService.updateResponse(id, response, notes);
        ra.addFlashAttribute("success", "Recruiter response updated!");
        return "redirect:/recruiters";
    }

    @PostMapping("/recruiters/{id}/delete")
    public String deleteRecruiter(@PathVariable Long id, RedirectAttributes ra) {
        recruiterService.deleteById(id);
        ra.addFlashAttribute("success", "Recruiter deleted.");
        return "redirect:/recruiters";
    }
}
