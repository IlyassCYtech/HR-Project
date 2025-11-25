package com.gestionrh.projetfinalspringboot.dto;

import com.gestionrh.projetfinalspringboot.model.enums.Grade;
import com.gestionrh.projetfinalspringboot.model.enums.Role;
import com.gestionrh.projetfinalspringboot.model.enums.StatutEmploye;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour la création et modification d'un employé
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeDto {
    
    private Long id;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;
    
    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Le téléphone doit être valide")
    private String telephone;
    
    @NotNull(message = "La date d'embauche est obligatoire")
    private LocalDate dateEmbauche;
    
    @NotNull(message = "Le salaire de base est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le salaire doit être positif")
    private BigDecimal salaireBase;
    
    @NotNull(message = "Le grade est obligatoire")
    private Grade grade;
    
    @NotNull(message = "Le statut est obligatoire")
    private StatutEmploye statut;
    
    @NotNull(message = "Le département est obligatoire")
    private Long departementId;
    
    private String departementNom;
    
    // Pour la création d'utilisateur
    private Boolean creerUtilisateur;
    
    @NotNull(message = "Le rôle est obligatoire pour créer un utilisateur")
    private Role role;
    
    private String username;
}
