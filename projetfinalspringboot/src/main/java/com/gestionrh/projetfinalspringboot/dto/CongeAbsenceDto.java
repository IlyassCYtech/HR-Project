package com.gestionrh.projetfinalspringboot.dto;

import com.gestionrh.projetfinalspringboot.model.enums.StatutConge;
import com.gestionrh.projetfinalspringboot.model.enums.TypeConge;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour la création et modification d'un congé/absence
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongeAbsenceDto {
    
    private Long id;
    
    @NotNull(message = "L'employé est obligatoire")
    private Long employeId;
    
    private String employeNom;
    
    @NotNull(message = "Le type de congé est obligatoire")
    private TypeConge typeConge;
    
    @NotNull(message = "La date de début est obligatoire")
    @FutureOrPresent(message = "La date de début doit être aujourd'hui ou dans le futur")
    private LocalDate dateDebut;
    
    @NotNull(message = "La date de fin est obligatoire")
    @Future(message = "La date de fin doit être dans le futur")
    private LocalDate dateFin;
    
    @Size(max = 1000, message = "La raison ne peut pas dépasser 1000 caractères")
    private String raison;
    
    private StatutConge statut;
    
    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
    private String commentaireValidation;
    
    private Long validateurId;
    
    private String validateurNom;
    
    private LocalDate dateValidation;
    
    /**
     * Validation personnalisée: dateFin doit être après dateDebut
     */
    @AssertTrue(message = "La date de fin doit être après la date de début")
    public boolean isDateFinValid() {
        if (dateDebut == null || dateFin == null) {
            return true; // Laissez @NotNull gérer les valeurs nulles
        }
        return dateFin.isAfter(dateDebut);
    }
}
