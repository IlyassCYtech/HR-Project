package com.gestionrh.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un employé de l'entreprise
 */
@Entity
@Table(name = "employes")
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
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
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
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;
    
    @NotNull(message = "La date d'embauche est obligatoire")
    @Column(name = "date_embauche", nullable = false)
    private LocalDate dateEmbauche;
    
    @Column(name = "date_fin")
    private LocalDate dateFin;
    
    @NotNull(message = "Le salaire de base est obligatoire")
    @DecimalMin(value = "0.0", message = "Le salaire doit être positif")
    @Column(name = "salaire_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal salaireBase;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false)
    private Grade grade = Grade.JUNIOR;
    
    @NotBlank(message = "Le poste est obligatoire")
    @Size(max = 100, message = "Le poste ne peut pas dépasser 100 caractères")
    @Column(name = "poste", nullable = false, length = 100)
    private String poste;
    
    @Enumerated(EnumType.STRING)
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
    private Departement departement;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employe manager;
    
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private Set<Employe> subordinnes = new HashSet<>();
    
    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<EmployeProjet> projets = new HashSet<>();
    
    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<FichePaie> fichesPaie = new HashSet<>();
    
    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<CongeAbsence> congesAbsences = new HashSet<>();
    
    // Constructeurs
    public Employe() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        this.statut = StatutEmploye.ACTIF;
        this.grade = Grade.JUNIOR;
    }
    
    public Employe(String matricule, String nom, String prenom, String email, 
                   LocalDate dateEmbauche, BigDecimal salaireBase, String poste) {
        this();
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.dateEmbauche = dateEmbauche;
        this.salaireBase = salaireBase;
        this.poste = poste;
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
    
    public String getNomComplet() {
        return prenom + " " + nom;
    }
    
    public int getAnciennete() {
        if (dateEmbauche == null) return 0;
        LocalDate dateFin = this.dateFin != null ? this.dateFin : LocalDate.now();
        return dateFin.getYear() - dateEmbauche.getYear();
    }
    
    public boolean isActif() {
        return StatutEmploye.ACTIF.equals(statut);
    }
    
    public boolean hasManagerRole() {
        return Grade.MANAGER.equals(grade) || Grade.DIRECTEUR.equals(grade);
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMatricule() {
        return matricule;
    }
    
    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public LocalDate getDateNaissance() {
        return dateNaissance;
    }
    
    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
    
    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }
    
    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }
    
    public LocalDate getDateFin() {
        return dateFin;
    }
    
    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }
    
    public BigDecimal getSalaireBase() {
        return salaireBase;
    }
    
    public void setSalaireBase(BigDecimal salaireBase) {
        this.salaireBase = salaireBase;
    }
    
    public Grade getGrade() {
        return grade;
    }
    
    public void setGrade(Grade grade) {
        this.grade = grade;
    }
    
    public String getPoste() {
        return poste;
    }
    
    public void setPoste(String poste) {
        this.poste = poste;
    }
    
    public StatutEmploye getStatut() {
        return statut;
    }
    
    public void setStatut(StatutEmploye statut) {
        this.statut = statut;
    }
    
    public String getPhotoUrl() {
        return photoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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
    
    public Departement getDepartement() {
        return departement;
    }
    
    public void setDepartement(Departement departement) {
        this.departement = departement;
    }
    
    public Employe getManager() {
        return manager;
    }
    
    public void setManager(Employe manager) {
        this.manager = manager;
    }
    
    public Set<Employe> getSubordonnes() {
        return subordinnes;
    }
    
    public void setSubordonnes(Set<Employe> subordinnes) {
        this.subordinnes = subordinnes;
    }
    
    public Set<EmployeProjet> getProjets() {
        return projets;
    }
    
    public void setProjets(Set<EmployeProjet> projets) {
        this.projets = projets;
    }
    
    public Set<FichePaie> getFichesPaie() {
        return fichesPaie;
    }
    
    public void setFichesPaie(Set<FichePaie> fichesPaie) {
        this.fichesPaie = fichesPaie;
    }
    
    public Set<CongeAbsence> getCongesAbsences() {
        return congesAbsences;
    }
    
    public void setCongesAbsences(Set<CongeAbsence> congesAbsences) {
        this.congesAbsences = congesAbsences;
    }
    
    // Méthodes utilitaires pour les relations
    public void addSubordonne(Employe employe) {
        subordinnes.add(employe);
        employe.setManager(this);
    }
    
    public void removeSubordonne(Employe employe) {
        subordinnes.remove(employe);
        employe.setManager(null);
    }
    
    public void addProjet(EmployeProjet employeProjet) {
        projets.add(employeProjet);
        employeProjet.setEmploye(this);
    }
    
    public void removeProjet(EmployeProjet employeProjet) {
        projets.remove(employeProjet);
        employeProjet.setEmploye(null);
    }
    
    // Méthodes equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employe)) return false;
        
        Employe employe = (Employe) o;
        return id != null && id.equals(employe.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Employe{" +
                "id=" + id +
                ", matricule='" + matricule + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", poste='" + poste + '\'' +
                ", statut=" + statut +
                '}';
    }
}
