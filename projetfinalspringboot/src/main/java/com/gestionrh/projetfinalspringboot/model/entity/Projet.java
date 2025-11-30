package com.gestionrh.projetfinalspringboot.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gestionrh.projetfinalspringboot.model.enums.PrioriteProjet;
import com.gestionrh.projetfinalspringboot.model.enums.StatutProjet;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entité représentant un projet de l'entreprise
 */
@Entity
@Table(name = "projets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du projet est obligatoire")
    @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères")
    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin_prevue")
    private LocalDate dateFinPrevue;

    @Column(name = "date_fin_reelle")
    private LocalDate dateFinReelle;

    @DecimalMin(value = "0.0", message = "Le budget doit être positif")
    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "statut", nullable = false)
    private StatutProjet statut = StatutProjet.PLANIFIE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "priorite", nullable = false)
    private PrioriteProjet priorite = PrioriteProjet.NORMALE;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_projet_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Employe chefProjet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Departement departement;

    @OneToMany(mappedBy = "projet", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Builder.Default
    private Set<EmployeProjet> employes = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutProjet.PLANIFIE;
        }
        if (priorite == null) {
            priorite = PrioriteProjet.NORMALE;
        }
    }

    // Méthodes utilitaires
    public boolean isTermine() {
        return statut == StatutProjet.TERMINE;
    }

    public boolean isEnCours() {
        return statut == StatutProjet.EN_COURS;
    }

    public boolean isEnRetard() {
        if (dateFinPrevue == null || isTermine()) {
            return false;
        }
        return LocalDate.now().isAfter(dateFinPrevue);
    }

    public int getNombreEmployes() {
        return employes != null ? employes.size() : 0;
    }

    public long getDureeEnJours() {
        LocalDate dateFin = dateFinReelle != null ? dateFinReelle
                : (dateFinPrevue != null ? dateFinPrevue : LocalDate.now());
        if (dateDebut == null)
            return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin);
    }

    public double getPourcentageAvancement() {
        if (dateDebut == null || dateFinPrevue == null)
            return 0.0;
        if (isTermine())
            return 100.0;

        long totalJours = java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFinPrevue);
        long joursEcoules = java.time.temporal.ChronoUnit.DAYS.between(dateDebut, LocalDate.now());

        if (totalJours <= 0)
            return 0.0;

        double pourcentage = (double) joursEcoules / totalJours * 100.0;
        return Math.min(100.0, Math.max(0.0, pourcentage));
    }

    /**
     * Validation personnalisée: dateFinPrevue doit être après dateDebut
     */
    @AssertTrue(message = "La date de fin prévue doit être après la date de début")
    public boolean isDateFinValid() {
        if (dateDebut == null || dateFinPrevue == null) {
            return true;
        }
        return dateFinPrevue.isAfter(dateDebut);
    }
}
