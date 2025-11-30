package com.gestionrh.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entité représentant l'affectation d'un employé à un projet
 * Table de liaison avec attributs supplémentaires
 */
@Entity
@Table(name = "employe_projets")
public class EmployeProjet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;
    
    @NotNull(message = "La date d'affectation est obligatoire")
    @Column(name = "date_affectation", nullable = false)
    private LocalDate dateAffectation;
    
    @Column(name = "date_fin_affectation")
    private LocalDate dateFinAffectation;
    
    @Size(max = 100, message = "Le rôle ne peut pas dépasser 100 caractères")
    @Column(name = "role_projet", length = 100)
    private String roleProjet;
    
    @DecimalMin(value = "0.0", message = "Le pourcentage doit être positif")
    @DecimalMax(value = "100.0", message = "Le pourcentage ne peut pas dépasser 100")
    @Column(name = "pourcentage_temps", precision = 5, scale = 2)
    private BigDecimal pourcentageTemps = new BigDecimal("100.00");
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutAffectation statut = StatutAffectation.ACTIF;
    
    // Énumération pour le statut d'affectation
    public enum StatutAffectation {
        ACTIF, TERMINE, SUSPENDU
    }
    
    // Constructeurs
    public EmployeProjet() {
        this.dateAffectation = LocalDate.now();
    }
    
    public EmployeProjet(Employe employe, Projet projet, LocalDate dateAffectation, String roleProjet) {
        this();
        this.employe = employe;
        this.projet = projet;
        this.dateAffectation = dateAffectation;
        this.roleProjet = roleProjet;
    }
    
    // Méthodes utilitaires
    public boolean isActif() {
        return statut == StatutAffectation.ACTIF;
    }
    
    public boolean isTermine() {
        return statut == StatutAffectation.TERMINE || 
               (dateFinAffectation != null && LocalDate.now().isAfter(dateFinAffectation));
    }
    
    public long getDureeAffectationEnJours() {
        LocalDate dateFin = dateFinAffectation != null ? dateFinAffectation : LocalDate.now();
        if (dateAffectation == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dateAffectation, dateFin);
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Employe getEmploye() {
        return employe;
    }
    
    public void setEmploye(Employe employe) {
        this.employe = employe;
    }
    
    public Projet getProjet() {
        return projet;
    }
    
    public void setProjet(Projet projet) {
        this.projet = projet;
    }
    
    public LocalDate getDateAffectation() {
        return dateAffectation;
    }
    
    public void setDateAffectation(LocalDate dateAffectation) {
        this.dateAffectation = dateAffectation;
    }
    
    public LocalDate getDateFinAffectation() {
        return dateFinAffectation;
    }
    
    public void setDateFinAffectation(LocalDate dateFinAffectation) {
        this.dateFinAffectation = dateFinAffectation;
    }
    
    public String getRoleProjet() {
        return roleProjet;
    }
    
    public void setRoleProjet(String roleProjet) {
        this.roleProjet = roleProjet;
    }
    
    public BigDecimal getPourcentageTemps() {
        return pourcentageTemps;
    }
    
    public void setPourcentageTemps(BigDecimal pourcentageTemps) {
        this.pourcentageTemps = pourcentageTemps;
    }
    
    public StatutAffectation getStatut() {
        return statut;
    }
    
    public void setStatut(StatutAffectation statut) {
        this.statut = statut;
    }
    
    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmployeProjet)) return false;
        EmployeProjet that = (EmployeProjet) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "EmployeProjet{" +
                "id=" + id +
                ", employe=" + (employe != null ? employe.getNomComplet() : "null") +
                ", projet=" + (projet != null ? projet.getNom() : "null") +
                ", roleProjet='" + roleProjet + '\'' +
                ", pourcentageTemps=" + pourcentageTemps +
                ", statut=" + statut +
                '}';
    }
}
