package com.gestionrh.projetfinalspringboot.model.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gestionrh.projetfinalspringboot.model.enums.Role;
import com.gestionrh.projetfinalspringboot.model.enums.StatutUtilisateur;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entité représentant un utilisateur du système
 */
@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {
    // Getters explicites pour compatibilité Spring Security/Lombok
    public String getUsername() {
        return username;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public Role getRole() {
        return role;
    }
    public StatutUtilisateur getStatut() {
        return statut;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Email(message = "L'email doit être valide")
    @Column(name = "email", unique = true, length = 150)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Employe employe;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "statut", nullable = false)
    private StatutUtilisateur statut = StatutUtilisateur.ACTIF;
    
    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;
    
    @Builder.Default
    @Min(value = 0, message = "Le nombre de tentatives doit être positif")
    @Column(name = "tentatives_connexion")
    private Integer tentativesConnexion = 0;
    
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (dateModification == null) {
            dateModification = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutUtilisateur.ACTIF;
        }
        if (tentativesConnexion == null) {
            tentativesConnexion = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    // Méthodes utilitaires
    public boolean isActif() {
        return statut == StatutUtilisateur.ACTIF;
    }
    
    public boolean isBloque() {
        return statut == StatutUtilisateur.BLOQUE;
    }
    
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    public boolean isRH() {
        return role == Role.RH || role == Role.ADMIN;
    }
    
    public boolean isChefDepartement() {
        return role == Role.CHEF_DEPT || role == Role.ADMIN;
    }
    
    public boolean isChefProjet() {
        return role == Role.CHEF_PROJET || role == Role.ADMIN;
    }
}
