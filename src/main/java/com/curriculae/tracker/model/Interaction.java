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
@Table(name = "interacoes")
public class Interaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidatura_id")
    private Long applicationId;

    @Column(name = "recrutador_id")
    private Long recruiterId;

    @Column(name = "data")
    private LocalDate date;

    @Column(name = "tipo")
    private String type;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notes;
}
