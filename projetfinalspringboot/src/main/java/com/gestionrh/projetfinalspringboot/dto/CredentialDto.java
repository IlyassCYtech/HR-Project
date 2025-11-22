package com.gestionrh.projetfinalspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO pour stocker les identifiants générés en session
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long employeId;
    private String employeNom;
    private String username;
    private String password;
    private LocalDateTime dateCreationObject;
    
    public String getDateCreation() {
        if (dateCreationObject != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return dateCreationObject.format(formatter);
        }
        return "";
    }
}
