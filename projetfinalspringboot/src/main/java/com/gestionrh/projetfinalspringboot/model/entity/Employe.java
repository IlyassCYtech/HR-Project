package com.gestionrh.projetfinalspringboot.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gestionrh.projetfinalspringboot.model.enums.Grade;
import com.gestionrh.projetfinalspringboot.model.enums.StatutEmploye;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entité représentant un employé de l'entreprise
 */
@Entity
@Table(name = "employes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le matricule est obligatoire")
    @Size(max = 20, message = "Le matricule ne peut pas dépasser 20 caractères")
    @Column(name = "matricule", nullable = false, unique = true, length = 20)
    private String matricule;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-]+$", message = "Le nom ne doit contenir que des lettres")
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-]+$", message = "Le prénom ne doit contenir que des lettres")
    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Email(message = "L'email doit être valide")
    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;

    @Past(message = "La date de naissance doit être dans le passé")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @NotNull(message = "La date d'embauche est obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_embauche", nullable = false)
    private LocalDate dateEmbauche;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_fin")
    private LocalDate dateFin;

    @NotNull(message = "Le salaire de base est obligatoire")
    @DecimalMin(value = "0.0", message = "Le salaire doit être positif")
    @Column(name = "salaire_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal salaireBase;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "grade", nullable = false)
    private Grade grade = Grade.JUNIOR;

    @NotBlank(message = "Le poste est obligatoire")
    @Size(max = 100, message = "Le poste ne peut pas dépasser 100 caractères")
    @Column(name = "poste", nullable = false, length = 100)
    private String poste;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "statut", nullable = false)
    private StatutEmploye statut = StatutEmploye.ACTIF;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Employe manager;

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Builder.Default
    private Set<Employe> subordinnes = new HashSet<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Builder.Default
    private Set<EmployeProjet> projets = new HashSet<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Builder.Default
    private Set<FichePaie> fichesPaie = new HashSet<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Builder.Default
    private Set<CongeAbsence> congesAbsences = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (dateModification == null) {
            dateModification = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutEmploye.ACTIF;
        }
        if (grade == null) {
            grade = Grade.JUNIOR;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    // Méthodes utilitaires
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean isActif() {
        return statut == StatutEmploye.ACTIF;
    }

    public int getAnciennete() {
        if (dateEmbauche == null)
            return 0;
        LocalDate dateFin = this.dateFin != null ? this.dateFin : LocalDate.now();
        return dateFin.getYear() - dateEmbauche.getYear();
    }

    /**
     * Validation personnalisée: dateFin doit être après dateEmbauche
     */
    @AssertTrue(message = "La date de fin de contrat doit être après la date d'embauche")
    public boolean isDateFinValid() {
        if (dateEmbauche == null || dateFin == null) {
            return true;
        }
        return dateFin.isAfter(dateEmbauche);
    }
}
