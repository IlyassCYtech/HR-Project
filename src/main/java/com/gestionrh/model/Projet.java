package com.gestionrh.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un projet de l'entreprise
 */
@Entity
@Table(name = "projets")
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
    @Column(name = "statut", nullable = false)
    private StatutProjet statut = StatutProjet.PLANIFIE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priorite", nullable = false)
    private PrioriteProjet priorite = PrioriteProjet.NORMALE;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_projet_id")
    private Employe chefProjet;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;
    
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    // Relation avec les employés du projet
    @OneToMany(mappedBy = "projet", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeProjet> employes = new HashSet<>();
    
    // Constructeurs
    public Projet() {
        this.dateCreation = LocalDateTime.now();
    }
    
    public Projet(String nom, String description, LocalDate dateDebut, 
                  LocalDate dateFinPrevue, BigDecimal budget) {
        this();
        this.nom = nom;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFinPrevue = dateFinPrevue;
        this.budget = budget;
    }
    
    // Méthodes utilitaires
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
    
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
        LocalDate dateFin = dateFinReelle != null ? dateFinReelle : 
                           (dateFinPrevue != null ? dateFinPrevue : LocalDate.now());
        if (dateDebut == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin);
    }
    
    public double getPourcentageAvancement() {
        if (dateDebut == null || dateFinPrevue == null) return 0.0;
        
        if (isTermine()) return 100.0;
        
        long totalJours = java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFinPrevue);
        long joursEcoules = java.time.temporal.ChronoUnit.DAYS.between(dateDebut, LocalDate.now());
        
        if (totalJours <= 0) return 0.0;
        
        double pourcentage = (double) joursEcoules / totalJours * 100.0;
        return Math.min(100.0, Math.max(0.0, pourcentage));
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
    
    public LocalDate getDateFinPrevue() {
        return dateFinPrevue;
    }
    
    public void setDateFinPrevue(LocalDate dateFinPrevue) {
        this.dateFinPrevue = dateFinPrevue;
    }
    
    public LocalDate getDateFinReelle() {
        return dateFinReelle;
    }
    
    public void setDateFinReelle(LocalDate dateFinReelle) {
        this.dateFinReelle = dateFinReelle;
    }
    
    public BigDecimal getBudget() {
        return budget;
    }
    
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
    
    public StatutProjet getStatut() {
        return statut;
    }
    
    public void setStatut(StatutProjet statut) {
        this.statut = statut;
    }
    
    public PrioriteProjet getPriorite() {
        return priorite;
    }
    
    public void setPriorite(PrioriteProjet priorite) {
        this.priorite = priorite;
    }
    
    public Employe getChefProjet() {
        return chefProjet;
    }
    
    public void setChefProjet(Employe chefProjet) {
        this.chefProjet = chefProjet;
    }
    
    public Departement getDepartement() {
        return departement;
    }
    
    public void setDepartement(Departement departement) {
        this.departement = departement;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public Set<EmployeProjet> getEmployes() {
        return employes;
    }
    
    public void setEmployes(Set<EmployeProjet> employes) {
        this.employes = employes;
    }
    
    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Projet)) return false;
        Projet projet = (Projet) o;
        return id != null && id.equals(projet.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Projet{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", statut=" + statut +
                ", priorite=" + priorite +
                ", dateDebut=" + dateDebut +
                ", dateFinPrevue=" + dateFinPrevue +
                ", nombreEmployes=" + getNombreEmployes() +
                '}';
    }
}
