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
@Table(name = "candidaturas")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Empresa")
    private String company;

    @Column(name = "Vaga")
    private String position;

    @Column(name = "Estado")
    private String status;

    @Column(name = "Data_Candidatura")
    private LocalDate applicationDate;

    @Column(name = "Detalhes", columnDefinition = "TEXT")
    private String details;

    @Column(name = "Num_Contactos")
    private Integer contactCount;

    @Column(name = "Ultimo_Contacto")
    private LocalDate lastContactDate;

    @Column(name = "Pasta_Empresa")
    private String companyFolder;
}
