package com.gestionrh.projetfinalspringboot.model.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entité représentant une fiche de paie mensuelle d'un employé
 */
@Entity
@Table(name = "fiches_paie", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"employe_id", "mois", "annee"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichePaie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
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
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "La prime de performance doit être positive")
    @Column(name = "prime_performance", precision = 10, scale = 2)
    private BigDecimal primePerformance = BigDecimal.ZERO;
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "La prime d'ancienneté doit être positive")
    @Column(name = "prime_anciennete", precision = 10, scale = 2)
    private BigDecimal primeAnciennete = BigDecimal.ZERO;
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "La prime de responsabilité doit être positive")
    @Column(name = "prime_responsabilite", precision = 10, scale = 2)
    private BigDecimal primeResponsabilite = BigDecimal.ZERO;
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Les autres primes doivent être positives")
    @Column(name = "autres_primes", precision = 10, scale = 2)
    private BigDecimal autresPrimes = BigDecimal.ZERO;
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Les heures supplémentaires doivent être positives")
    @Column(name = "heures_supplementaires", precision = 5, scale = 2)
    private BigDecimal heuresSupplementaires = BigDecimal.ZERO;
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Le taux horaire supplémentaire doit être positif")
    @Column(name = "taux_horaire_sup", precision = 8, scale = 2)
    private BigDecimal tauxHoraireSup = BigDecimal.ZERO;
    
    @Builder.Default
    @Min(value = 0, message = "Les jours de congés payés doivent être positifs")
    @Column(name = "jours_conges_payes")
    private Integer joursCongesPayes = 0;
    
    @Builder.Default
    @Min(value = 0, message = "Les jours d'absence non payés doivent être positifs")
    @Column(name = "jours_absence_non_payes")
    private Integer joursAbsenceNonPayes = 0;
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Les cotisations sociales doivent être positives")
    @Column(name = "cotisations_sociales", precision = 10, scale = 2)
    private BigDecimal cotisationsSociales = BigDecimal.ZERO;
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Les impôts doivent être positifs")
    @Column(name = "impots", precision = 10, scale = 2)
    private BigDecimal impots = BigDecimal.ZERO;
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Les autres déductions doivent être positives")
    @Column(name = "autres_deductions", precision = 10, scale = 2)
    private BigDecimal autresDeductions = BigDecimal.ZERO;
    
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_validation")
    private LocalDateTime dateValidation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Employe validePar;
    
    @Column(name = "commentaires", columnDefinition = "TEXT")
    private String commentaires;
    
    @PrePersist
    @PreUpdate
    protected void calculerMontants() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
    
    // Méthodes de calcul
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
    
    // Alias pour compatibilité
    public BigDecimal calculerNetAPayer() {
        return calculerSalaireNet();
    }
    
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
    
    public BigDecimal getNetAPayer() {
        return getSalaireNet();
    }
}
