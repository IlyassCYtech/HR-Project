package com.gestionrh.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un utilisateur du système avec ses informations d'authentification
 */
@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
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
    private Employe employe;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutUtilisateur statut = StatutUtilisateur.ACTIF;
    
    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;
    
    @Min(value = 0, message = "Le nombre de tentatives doit être positif")
    @Column(name = "tentatives_connexion")
    private Integer tentativesConnexion = 0;
    
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    // Énumérations
    public enum Role {
        ADMIN("Administrateur"),
        RH("Ressources Humaines"),
        CHEF_DEPT("Chef de Département"),
        CHEF_PROJET("Chef de Projet"),
        EMPLOYE("Employé");
        
        private final String libelle;
        
        Role(String libelle) {
            this.libelle = libelle;
        }
        
        public String getLibelle() {
            return libelle;
        }
    }
    
    public enum StatutUtilisateur {
        ACTIF("Actif"),
        INACTIVE("Inactif"),
        BLOQUE("Bloqué");
        
        private final String libelle;
        
        StatutUtilisateur(String libelle) {
            this.libelle = libelle;
        }
        
        public String getLibelle() {
            return libelle;
        }
    }
    
    // Constructeurs
    public Utilisateur() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }
    
    public Utilisateur(String username, String passwordHash, String email, Role role) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
    }
    
    // Méthodes utilitaires
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (dateModification == null) {
            dateModification = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
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
    
    public boolean isChef() {
        return role == Role.CHEF_DEPT || role == Role.CHEF_PROJET || role == Role.ADMIN;
    }
    
    public void enregistrerConnexion() {
        this.derniereConnexion = LocalDateTime.now();
        this.tentativesConnexion = 0;
    }
    
    public void incrementerTentativesConnexion() {
        this.tentativesConnexion++;
        if (this.tentativesConnexion >= 5) {
            this.statut = StatutUtilisateur.BLOQUE;
        }
    }
    
    public void debloquer() {
        this.statut = StatutUtilisateur.ACTIF;
        this.tentativesConnexion = 0;
    }
    
    public void desactiver() {
        this.statut = StatutUtilisateur.INACTIVE;
    }
    
    public void activer() {
        this.statut = StatutUtilisateur.ACTIF;
        this.tentativesConnexion = 0;
    }
    
    public String getNomComplet() {
        if (employe != null) {
            return employe.getNomComplet();
        }
        return username;
    }
    
    /**
     * Retourne le nom à afficher dans l'interface
     * Pour compatibilité avec les JSP qui utilisent ${utilisateur.nom}
     */
    public String getNom() {
        if (employe != null) {
            return employe.getNom();
        }
        return username;
    }
    
    /**
     * Retourne le prénom à afficher dans l'interface
     * Pour compatibilité avec les JSP
     */
    public String getPrenom() {
        if (employe != null) {
            return employe.getPrenom();
        }
        return "";
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public Employe getEmploye() {
        return employe;
    }
    
    public void setEmploye(Employe employe) {
        this.employe = employe;
    }
    
    public StatutUtilisateur getStatut() {
        return statut;
    }
    
    public void setStatut(StatutUtilisateur statut) {
        this.statut = statut;
    }
    
    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }
    
    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }
    
    public Integer getTentativesConnexion() {
        return tentativesConnexion;
    }
    
    public void setTentativesConnexion(Integer tentativesConnexion) {
        this.tentativesConnexion = tentativesConnexion;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDateTime getDateModification() {
        return dateModification;
    }
    
    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }
    
    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilisateur)) return false;
        Utilisateur that = (Utilisateur) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", statut=" + statut +
                ", derniereConnexion=" + derniereConnexion +
                '}';
    }
}
