package com.gestionrh.projetfinalspringboot.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.gestionrh.projetfinalspringboot.model.enums.StatutProjet;
import com.gestionrh.projetfinalspringboot.model.enums.PrioriteProjet;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entité représentant un département de l'entreprise
 */
@Entity
@Table(name = "departements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    
    @Builder.Default
    @Column(name = "actif", nullable = false)
    private Boolean actif = true;
    
    // Relation avec le chef de département
    @OneToOne
    @JoinColumn(name = "chef_departement_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employe chefDepartement;
    
    // Relation avec les employés du département
    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Employe> employes = new HashSet<>();
    
    // Relation avec les projets du département
    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Projet> projets = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (actif == null) {
            actif = true;
        }
    }
    
    // Méthodes utilitaires
    public int getNombreEmployes() {
        return employes != null ? employes.size() : 0;
    }
    
    public int getNombreProjets() {
        return projets != null ? projets.size() : 0;
    }
}
