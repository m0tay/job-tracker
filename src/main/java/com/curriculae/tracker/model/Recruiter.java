package com.curriculae.tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recrutadores")
public class Recruiter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String name;

    @Column(name = "empresa")
    private String company;

    @Column(name = "cargo")
    private String role;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "stack")
    private String stack;

    @Column(name = "mensagem_enviada", columnDefinition = "TEXT")
    private String sentMessage;

    @Column(name = "data_contacto")
    private LocalDate contactDate;

    @Column(name = "resposta")
    private String response;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "candidatura_id")
    private Long applicationId;
}
