package com.gestionrh.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Entité représentant les congés et absences des employés
 */
@Entity
@Table(name = "conges_absences")
public class CongeAbsence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
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
    @Column(name = "statut", nullable = false)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approuve_par")
    private Employe approuvePar;
    
    @Column(name = "date_demande", nullable = false, updatable = false)
    private LocalDateTime dateDemande;
    
    @Column(name = "date_approbation")
    private LocalDateTime dateApprobation;
    
    @Column(name = "commentaires_approbation", columnDefinition = "TEXT")
    private String commentairesApprobation;
    
    // Énumérations
    public enum TypeConge {
        CONGES_PAYES("Congés payés"),
        MALADIE("Maladie"),
        MATERNITE("Maternité"),
        PATERNITE("Paternité"),
        FORMATION("Formation"),
        SANS_SOLDE("Sans solde");
        
        private final String libelle;
        
        TypeConge(String libelle) {
            this.libelle = libelle;
        }
        
        public String getLibelle() {
            return libelle;
        }
    }
    
    public enum StatutDemande {
        EN_ATTENTE("En attente"),
        APPROUVE("Approuvé"),
        REFUSE("Refusé"),
        ANNULE("Annulé");
        
        private final String libelle;
        
        StatutDemande(String libelle) {
            this.libelle = libelle;
        }
        
        public String getLibelle() {
            return libelle;
        }
    }
    
    // Constructeurs
    public CongeAbsence() {
        this.dateDemande = LocalDateTime.now();
    }
    
    public CongeAbsence(Employe employe, TypeConge typeConge, LocalDate dateDebut, 
                        LocalDate dateFin, String motif) {
        this();
        this.employe = employe;
        this.typeConge = typeConge;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.motif = motif;
        this.nombreJours = calculerNombreJours();
    }
    
    // Méthodes utilitaires
    @PrePersist
    @PreUpdate
    protected void validerDonnees() {
        if (dateDemande == null) {
            dateDemande = LocalDateTime.now();
        }
        if (dateDebut != null && dateFin != null) {
            this.nombreJours = calculerNombreJours();
        }
    }
    
    public Integer calculerNombreJours() {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
    }
    
    public boolean isApprouve() {
        return statut == StatutDemande.APPROUVE;
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
        this.statut = StatutDemande.APPROUVE;
        this.approuvePar = approbateur;
        this.dateApprobation = LocalDateTime.now();
        this.commentairesApprobation = commentaires;
    }
    
    public void refuser(Employe approbateur, String commentaires) {
        this.statut = StatutDemande.REFUSE;
        this.approuvePar = approbateur;
        this.dateApprobation = LocalDateTime.now();
        this.commentairesApprobation = commentaires;
    }
    
    public void annuler() {
        this.statut = StatutDemande.ANNULE;
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
    
    public TypeConge getTypeConge() {
        return typeConge;
    }
    
    public void setTypeConge(TypeConge typeConge) {
        this.typeConge = typeConge;
    }
    
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
    
    public LocalDate getDateFin() {
        return dateFin;
    }
    
    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }
    
    public Integer getNombreJours() {
        return nombreJours;
    }
    
    public void setNombreJours(Integer nombreJours) {
        this.nombreJours = nombreJours;
    }
    
    public String getMotif() {
        return motif;
    }
    
    public void setMotif(String motif) {
        this.motif = motif;
    }
    
    public StatutDemande getStatut() {
        return statut;
    }
    
    public void setStatut(StatutDemande statut) {
        this.statut = statut;
    }
    
    public Employe getApprouvePar() {
        return approuvePar;
    }
    
    public void setApprouvePar(Employe approuvePar) {
        this.approuvePar = approuvePar;
    }
    
    public LocalDateTime getDateDemande() {
        return dateDemande;
    }
    
    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
    }
    
    public LocalDateTime getDateApprobation() {
        return dateApprobation;
    }
    
    public void setDateApprobation(LocalDateTime dateApprobation) {
        this.dateApprobation = dateApprobation;
    }
    
    public String getCommentairesApprobation() {
        return commentairesApprobation;
    }
    
    public void setCommentairesApprobation(String commentairesApprobation) {
        this.commentairesApprobation = commentairesApprobation;
    }
    
    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CongeAbsence)) return false;
        CongeAbsence that = (CongeAbsence) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "CongeAbsence{" +
                "id=" + id +
                ", employe=" + (employe != null ? employe.getNomComplet() : "null") +
                ", typeConge=" + typeConge +
                ", periode=" + getPeriode() +
                ", nombreJours=" + nombreJours +
                ", statut=" + statut +
                '}';
    }
}
