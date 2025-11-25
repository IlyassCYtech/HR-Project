package com.gestionrh.projetfinalspringboot.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour la génération et modification d'une fiche de paie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FichePaieDto {
    
    private Long id;
    
    @NotNull(message = "L'employé est obligatoire")
    private Long employeId;
    
    private String employeNom;
    
    @NotNull(message = "Le mois est obligatoire")
    @Min(value = 1, message = "Le mois doit être entre 1 et 12")
    @Max(value = 12, message = "Le mois doit être entre 1 et 12")
    private Integer mois;
    
    @NotNull(message = "L'année est obligatoire")
    @Min(value = 2000, message = "L'année doit être supérieure à 2000")
    @Max(value = 2100, message = "L'année doit être inférieure à 2100")
    private Integer annee;
    
    @NotNull(message = "Le salaire de base est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le salaire de base doit être positif")
    private BigDecimal salaireBase;
    
    @NotNull(message = "Les heures travaillées sont obligatoires")
    @DecimalMin(value = "0.0", message = "Les heures travaillées doivent être positives ou nulles")
    @DecimalMax(value = "744.0", message = "Les heures travaillées ne peuvent pas dépasser 744h (31 jours * 24h)")
    private BigDecimal heuresTravaillees;
    
    @NotNull(message = "Les heures supplémentaires sont obligatoires")
    @DecimalMin(value = "0.0", message = "Les heures supplémentaires doivent être positives ou nulles")
    private BigDecimal heuresSupplementaires;
    
    @NotNull(message = "Les primes sont obligatoires")
    @DecimalMin(value = "0.0", message = "Les primes doivent être positives ou nulles")
    private BigDecimal primes;
    
    @NotNull(message = "Les déductions sont obligatoires")
    @DecimalMin(value = "0.0", message = "Les déductions doivent être positives ou nulles")
    private BigDecimal deductions;
    
    private BigDecimal cotisationsSociales;
    
    private BigDecimal impots;
    
    private BigDecimal salaireNet;
    
    private BigDecimal salaireBrut;
    
    private LocalDate dateGeneration;
    
    /**
     * Retourne la période au format "Mois Année"
     */
    public String getPeriode() {
        if (mois == null || annee == null) {
            return "";
        }
        String[] moisNoms = {"", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", 
                             "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return moisNoms[mois] + " " + annee;
    }
}
