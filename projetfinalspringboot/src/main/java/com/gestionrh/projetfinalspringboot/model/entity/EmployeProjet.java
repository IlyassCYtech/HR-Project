package com.gestionrh.projetfinalspringboot.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gestionrh.projetfinalspringboot.model.enums.StatutEmployeProjet;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entité représentant l'affectation d'un employé à un projet
 * Table de liaison avec attributs supplémentaires
 */
@Entity
@Table(name = "employe_projets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeProjet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Employe employe;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Projet projet;
    
    @NotNull(message = "La date d'affectation est obligatoire")
    @Column(name = "date_affectation", nullable = false)
    private LocalDate dateAffectation;
    
    @Column(name = "date_fin_affectation")
    private LocalDate dateFinAffectation;
    
    @Size(max = 100, message = "Le rôle ne peut pas dépasser 100 caractères")
    @Column(name = "role_projet", length = 100)
    private String roleProjet;
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Le pourcentage doit être positif")
    @DecimalMax(value = "100.0", message = "Le pourcentage ne peut pas dépasser 100")
    @Column(name = "pourcentage_temps", precision = 5, scale = 2)
    private BigDecimal pourcentageTemps = new BigDecimal("100.00");
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "statut", nullable = false)
    private StatutEmployeProjet statut = StatutEmployeProjet.ACTIF;
    
    @PrePersist
    protected void onCreate() {
        if (dateAffectation == null) {
            dateAffectation = LocalDate.now();
        }
        if (statut == null) {
            statut = StatutEmployeProjet.ACTIF;
        }
        if (pourcentageTemps == null) {
            pourcentageTemps = new BigDecimal("100.00");
        }
    }
    
    // Méthodes utilitaires
    public boolean isActif() {
        return statut == StatutEmployeProjet.ACTIF;
    }
    
    public boolean isTermine() {
        return statut == StatutEmployeProjet.TERMINE || 
               (dateFinAffectation != null && LocalDate.now().isAfter(dateFinAffectation));
    }
    
    public long getDureeAffectationEnJours() {
        LocalDate dateFin = dateFinAffectation != null ? dateFinAffectation : LocalDate.now();
        if (dateAffectation == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dateAffectation, dateFin);
    }
}
