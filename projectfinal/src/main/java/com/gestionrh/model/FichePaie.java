package com.gestionrh.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Entité représentant une fiche de paie mensuelle d'un employé
 */
@Entity
@Table(name = "fiches_paie", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"employe_id", "mois", "annee"}))
public class FichePaie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @Min(value = 1, message = "Le mois doit être entre 1 et 12")
    @Max(value = 12, message = "Le mois doit être entre 1 et 12")
    @Column(name = "mois", nullable = false)
    private Integer mois;
    
    @Min(value = 2020, message = "L'année doit être supérieure à 2020")
    @Column(name = "annee", nullable = false)
    private Integer annee;
    
    @NotNull(message = "Le salaire de base est obligatoire")
    @DecimalMin(value = "0.0", message = "Le salaire de base doit être positif")
    @Column(name = "salaire_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal salaireBase;
    
    @DecimalMin(value = "0.0", message = "La prime de performance doit être positive")
    @Column(name = "prime_performance", precision = 10, scale = 2)
    private BigDecimal primePerformance = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "La prime d'ancienneté doit être positive")
    @Column(name = "prime_anciennete", precision = 10, scale = 2)
    private BigDecimal primeAnciennete = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "La prime de responsabilité doit être positive")
    @Column(name = "prime_responsabilite", precision = 10, scale = 2)
    private BigDecimal primeResponsabilite = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Les autres primes doivent être positives")
    @Column(name = "autres_primes", precision = 10, scale = 2)
    private BigDecimal autresPrimes = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Les heures supplémentaires doivent être positives")
    @Column(name = "heures_supplementaires", precision = 5, scale = 2)
    private BigDecimal heuresSupplementaires = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Le taux horaire supplémentaire doit être positif")
    @Column(name = "taux_horaire_sup", precision = 8, scale = 2)
    private BigDecimal tauxHoraireSup = BigDecimal.ZERO;
    
    @Min(value = 0, message = "Les jours de congés payés doivent être positifs")
    @Column(name = "jours_conges_payes")
    private Integer joursCongesPayes = 0;
    
    @Min(value = 0, message = "Les jours d'absence non payés doivent être positifs")
    @Column(name = "jours_absence_non_payes")
    private Integer joursAbsenceNonPayes = 0;
    
    @DecimalMin(value = "0.0", message = "Les cotisations sociales doivent être positives")
    @Column(name = "cotisations_sociales", precision = 10, scale = 2)
    private BigDecimal cotisationsSociales = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Les impôts doivent être positifs")
    @Column(name = "impots", precision = 10, scale = 2)
    private BigDecimal impots = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Les autres déductions doivent être positives")
    @Column(name = "autres_deductions", precision = 10, scale = 2)
    private BigDecimal autresDeductions = BigDecimal.ZERO;
    
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_validation")
    private LocalDateTime dateValidation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par")
    private Employe validePar;
    
    @Column(name = "commentaires", columnDefinition = "TEXT")
    private String commentaires;
    
    // Constructeurs
    public FichePaie() {
        this.dateCreation = LocalDateTime.now();
    }
    
    public FichePaie(Employe employe, Integer mois, Integer annee, BigDecimal salaireBase) {
        this();
        this.employe = employe;
        this.mois = mois;
        this.annee = annee;
        this.salaireBase = salaireBase;
    }
    
    // Méthodes de calcul
    @PrePersist
    @PreUpdate
    protected void calculerMontants() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
    
    public BigDecimal calculerTotalPrimes() {
        BigDecimal total = BigDecimal.ZERO;
        if (primePerformance != null) total = total.add(primePerformance);
        if (primeAnciennete != null) total = total.add(primeAnciennete);
        if (primeResponsabilite != null) total = total.add(primeResponsabilite);
        if (autresPrimes != null) total = total.add(autresPrimes);
        return total;
    }
    
    public BigDecimal calculerMontantHeuresSupplementaires() {
        if (heuresSupplementaires == null || tauxHoraireSup == null) {
            return BigDecimal.ZERO;
        }
        return heuresSupplementaires.multiply(tauxHoraireSup);
    }
    
    public BigDecimal calculerDeductionAbsences() {
        if (joursAbsenceNonPayes == null || joursAbsenceNonPayes == 0 || salaireBase == null) {
            return BigDecimal.ZERO;
        }
        // Calcul basé sur 30 jours par mois
        BigDecimal salaireJournalier = salaireBase.divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
        return salaireJournalier.multiply(new BigDecimal(joursAbsenceNonPayes));
    }
    
    public BigDecimal calculerSalaireBrut() {
        BigDecimal brut = salaireBase != null ? salaireBase : BigDecimal.ZERO;
        brut = brut.add(calculerTotalPrimes());
        brut = brut.add(calculerMontantHeuresSupplementaires());
        return brut;
    }
    
    public BigDecimal calculerTotalDeductions() {
        BigDecimal total = BigDecimal.ZERO;
        if (cotisationsSociales != null) total = total.add(cotisationsSociales);
        if (impots != null) total = total.add(impots);
        if (autresDeductions != null) total = total.add(autresDeductions);
        total = total.add(calculerDeductionAbsences());
        return total;
    }
    
    public BigDecimal calculerSalaireNet() {
        return calculerSalaireBrut().subtract(calculerTotalDeductions());
    }
    
    // Getters pour compatibilité JSP
    public BigDecimal getPrimes() {
        return calculerTotalPrimes();
    }
    
    public BigDecimal getDeductions() {
        return calculerTotalDeductions();
    }
    
    public BigDecimal getSalaireBrut() {
        return calculerSalaireBrut();
    }
    
    public BigDecimal getSalaireNet() {
        return calculerSalaireNet();
    }
    
    // Alias pour compatibilité JSP
    public BigDecimal getNetAPayer() {
        return getSalaireNet();
    }
    
    // Méthodes utilitaires
    public boolean isValidee() {
        return dateValidation != null && validePar != null;
    }
    
    public String getPeriode() {
        return String.format("%02d/%d", mois, annee);
    }
    
    public String getDateCreationFormatted() {
        if (dateCreation == null) return "";
        return dateCreation.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    public void valider(Employe validateur) {
        this.validePar = validateur;
        this.dateValidation = LocalDateTime.now();
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
    
    public Integer getMois() {
        return mois;
    }
    
    public void setMois(Integer mois) {
        this.mois = mois;
    }
    
    public Integer getAnnee() {
        return annee;
    }
    
    public void setAnnee(Integer annee) {
        this.annee = annee;
    }
    
    public BigDecimal getSalaireBase() {
        return salaireBase;
    }
    
    public void setSalaireBase(BigDecimal salaireBase) {
        this.salaireBase = salaireBase;
    }
    
    public BigDecimal getPrimePerformance() {
        return primePerformance;
    }
    
    public void setPrimePerformance(BigDecimal primePerformance) {
        this.primePerformance = primePerformance;
    }
    
    public BigDecimal getPrimeAnciennete() {
        return primeAnciennete;
    }
    
    public void setPrimeAnciennete(BigDecimal primeAnciennete) {
        this.primeAnciennete = primeAnciennete;
    }
    
    public BigDecimal getPrimeResponsabilite() {
        return primeResponsabilite;
    }
    
    public void setPrimeResponsabilite(BigDecimal primeResponsabilite) {
        this.primeResponsabilite = primeResponsabilite;
    }
    
    public BigDecimal getAutresPrimes() {
        return autresPrimes;
    }
    
    public void setAutresPrimes(BigDecimal autresPrimes) {
        this.autresPrimes = autresPrimes;
    }
    
    public BigDecimal getHeuresSupplementaires() {
        return heuresSupplementaires;
    }
    
    public void setHeuresSupplementaires(BigDecimal heuresSupplementaires) {
        this.heuresSupplementaires = heuresSupplementaires;
    }
    
    public BigDecimal getTauxHoraireSup() {
        return tauxHoraireSup;
    }
    
    public void setTauxHoraireSup(BigDecimal tauxHoraireSup) {
        this.tauxHoraireSup = tauxHoraireSup;
    }
    
    public Integer getJoursCongesPayes() {
        return joursCongesPayes;
    }
    
    public void setJoursCongesPayes(Integer joursCongesPayes) {
        this.joursCongesPayes = joursCongesPayes;
    }
    
    public Integer getJoursAbsenceNonPayes() {
        return joursAbsenceNonPayes;
    }
    
    public void setJoursAbsenceNonPayes(Integer joursAbsenceNonPayes) {
        this.joursAbsenceNonPayes = joursAbsenceNonPayes;
    }
    
    public BigDecimal getCotisationsSociales() {
        return cotisationsSociales;
    }
    
    public void setCotisationsSociales(BigDecimal cotisationsSociales) {
        this.cotisationsSociales = cotisationsSociales;
    }
    
    public BigDecimal getImpots() {
        return impots;
    }
    
    public void setImpots(BigDecimal impots) {
        this.impots = impots;
    }
    
    public BigDecimal getAutresDeductions() {
        return autresDeductions;
    }
    
    public void setAutresDeductions(BigDecimal autresDeductions) {
        this.autresDeductions = autresDeductions;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDateTime getDateValidation() {
        return dateValidation;
    }
    
    public void setDateValidation(LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }
    
    public Employe getValidePar() {
        return validePar;
    }
    
    public void setValidePar(Employe validePar) {
        this.validePar = validePar;
    }
    
    public String getCommentaires() {
        return commentaires;
    }
    
    public void setCommentaires(String commentaires) {
        this.commentaires = commentaires;
    }
    
    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FichePaie)) return false;
        FichePaie fichePaie = (FichePaie) o;
        return id != null && id.equals(fichePaie.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "FichePaie{" +
                "id=" + id +
                ", employe=" + (employe != null ? employe.getNomComplet() : "null") +
                ", periode=" + getPeriode() +
                ", salaireBrut=" + calculerSalaireBrut() +
                ", salaireNet=" + calculerSalaireNet() +
                ", validee=" + isValidee() +
                '}';
    }
}
