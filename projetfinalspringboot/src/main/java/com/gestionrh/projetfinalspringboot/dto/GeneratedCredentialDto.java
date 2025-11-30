package com.gestionrh.projetfinalspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO pour stocker temporairement les identifiants générés
 * (pour affichage et export PDF)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedCredentialDto {
    
    private Long employeId;
    private String employeNom;
    private String username;
    private String password;
    private LocalDateTime dateCreation;
    
    public GeneratedCredentialDto(Long employeId, String employeNom, String username, String password) {
        this.employeId = employeId;
        this.employeNom = employeNom;
        this.username = username;
        this.password = password;
        this.dateCreation = LocalDateTime.now();
    }
    
    /**
     * Retourne la date de création formatée
     */
    public String getDateCreationFormatted() {
        if (dateCreation == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateCreation.format(formatter);
    }
}
