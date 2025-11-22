package com.gestionrh.projetfinalspringboot.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gestionrh.projetfinalspringboot.model.enums.StatutConge;
import com.gestionrh.projetfinalspringboot.model.enums.TypeConge;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entité représentant les congés et absences des employés
 */
@Entity
@Table(name = "conges_absences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CongeAbsence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Employe employe;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_conge", nullable = false)
    private TypeConge typeConge;
    
    @NotNull(message = "La date de début est obligatoire")
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    
    @NotNull(message = "La date de fin est obligatoire")
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;
    
    @Min(value = 1, message = "Le nombre de jours doit être positif")
    @Column(name = "nombre_jours", nullable = false)
    private Integer nombreJours;
    
    @Column(name = "motif", columnDefinition = "TEXT")
    private String motif;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "statut", nullable = false)
    private StatutConge statut = StatutConge.EN_ATTENTE;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approuve_par")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Employe approuvePar;
    
    @Column(name = "date_demande", nullable = false, updatable = false)
    private LocalDateTime dateDemande;
    
    @Column(name = "date_approbation")
    private LocalDateTime dateApprobation;
    
    @Column(name = "commentaires_approbation", columnDefinition = "TEXT")
    private String commentairesApprobation;
    
    @PrePersist
    @PreUpdate
    protected void validerDonnees() {
        if (dateDemande == null) {
            dateDemande = LocalDateTime.now();
        }
        if (dateDebut != null && dateFin != null) {
            this.nombreJours = calculerNombreJours();
        }
        if (statut == null) {
            statut = StatutConge.EN_ATTENTE;
        }
    }
    
    // Méthodes utilitaires
    public Integer calculerNombreJours() {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
    }
    
    public boolean isApprouve() {
        return statut == StatutConge.APPROUVE;
    }
    
    public boolean isEnCours() {
        return isApprouve() && 
               LocalDate.now().isAfter(dateDebut.minusDays(1)) && 
               LocalDate.now().isBefore(dateFin.plusDays(1));
    }
    
    public boolean isPasse() {
        return LocalDate.now().isAfter(dateFin);
    }
    
    public boolean isFutur() {
        return LocalDate.now().isBefore(dateDebut);
    }
    
    public void approuver(Employe approbateur, String commentaires) {
        this.statut = StatutConge.APPROUVE;
        this.approuvePar = approbateur;
        this.dateApprobation = LocalDateTime.now();
        this.commentairesApprobation = commentaires;
    }
    
    public void refuser(Employe approbateur, String commentaires) {
        this.statut = StatutConge.REFUSE;
        this.approuvePar = approbateur;
        this.dateApprobation = LocalDateTime.now();
        this.commentairesApprobation = commentaires;
    }
    
    public void annuler() {
        this.statut = StatutConge.ANNULE;
    }
    
    public String getPeriode() {
        if (dateDebut == null || dateFin == null) {
            return "";
        }
        if (dateDebut.equals(dateFin)) {
            return dateDebut.toString();
        }
        return dateDebut + " au " + dateFin;
    }
}
