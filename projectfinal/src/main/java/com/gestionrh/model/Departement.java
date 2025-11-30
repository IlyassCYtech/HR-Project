package com.gestionrh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entité représentant un département de l'entreprise
 */
@Entity
@Table(name = "departements")
public class Departement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom du département est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Column(name = "nom", nullable = false, unique = true, length = 100)
    private String nom;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @DecimalMin(value = "0.0", message = "Le budget doit être positif")
    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;
    
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "actif", nullable = false)
    private Boolean actif = true;
    
    // Relation avec le chef de département
    @OneToOne
    @JoinColumn(name = "chef_departement_id")
    private Employe chefDepartement;
    
    // Relation avec les employés du département
    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    private Set<Employe> employes = new HashSet<>();
    
    // Relation avec les projets du département
    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    private Set<Projet> projets = new HashSet<>();
    
    // Constructeurs
    public Departement() {
        this.dateCreation = LocalDateTime.now();
        this.actif = true;
    }
    
    public Departement(String nom, String description, BigDecimal budget) {
        this();
        this.nom = nom;
        this.description = description;
        this.budget = budget;
    }
    
    // Méthodes utilitaires
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (actif == null) {
            actif = true;
        }
    }
    
    public int getNombreEmployes() {
        return employes != null ? employes.size() : 0;
    }
    
    public int getNombreProjets() {
        return projets != null ? projets.size() : 0;
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
    
    public BigDecimal getBudget() {
        return budget;
    }
    
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public Boolean getActif() {
        return actif;
    }
    
    public void setActif(Boolean actif) {
        this.actif = actif;
    }
    
    public Employe getChefDepartement() {
        return chefDepartement;
    }
    
    public void setChefDepartement(Employe chefDepartement) {
        this.chefDepartement = chefDepartement;
    }
    
    // Getter pour chef (alias pour compatibilité avec DAO)
    public Employe getChef() {
        return chefDepartement;
    }
    
    public void setChef(Employe chef) {
        this.chefDepartement = chef;
    }
    
    public Set<Employe> getEmployes() {
        return employes;
    }
    
    public void setEmployes(Set<Employe> employes) {
        this.employes = employes;
    }
    
    public Set<Projet> getProjets() {
        return projets;
    }
    
    public void setProjets(Set<Projet> projets) {
        this.projets = projets;
    }
    
    // Méthodes utilitaires pour la gestion des relations
    public void addEmploye(Employe employe) {
        employes.add(employe);
        employe.setDepartement(this);
    }
    
    public void removeEmploye(Employe employe) {
        employes.remove(employe);
        employe.setDepartement(null);
    }
    
    public void addProjet(Projet projet) {
        projets.add(projet);
        projet.setDepartement(this);
    }
    
    public void removeProjet(Projet projet) {
        projets.remove(projet);
        projet.setDepartement(null);
    }
    
    // Méthodes equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Departement)) return false;
        
        Departement that = (Departement) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Departement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", budget=" + budget +
                ", actif=" + actif +
                ", nombreEmployes=" + getNombreEmployes() +
                '}';
    }
}
