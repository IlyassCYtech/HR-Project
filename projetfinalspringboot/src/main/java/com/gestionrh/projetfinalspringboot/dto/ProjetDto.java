package com.gestionrh.projetfinalspringboot.dto;

import com.gestionrh.projetfinalspringboot.model.enums.PrioriteProjet;
import com.gestionrh.projetfinalspringboot.model.enums.StatutProjet;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour la création et modification d'un projet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetDto {
    
    private Long id;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 200, message = "Le nom doit contenir entre 2 et 200 caractères")
    private String nom;
    
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;
    
    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;
    
    private LocalDate dateFin;
    
    @NotNull(message = "Le budget est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le budget doit être positif")
    private BigDecimal budget;
    
    @NotNull(message = "Le statut est obligatoire")
    private StatutProjet statut;
    
    @NotNull(message = "La priorité est obligatoire")
    private PrioriteProjet priorite;
    
    @NotNull(message = "Le chef de projet est obligatoire")
    private Long chefProjetId;
    
    private String chefProjetNom;
    
    @NotNull(message = "Le département est obligatoire")
    private Long departementId;
    
    private String departementNom;
}
