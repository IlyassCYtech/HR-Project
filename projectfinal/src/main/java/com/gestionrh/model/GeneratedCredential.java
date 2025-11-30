package com.gestionrh.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe pour stocker temporairement les identifiants générés
 * (pour affichage et export PDF)
 */
public class GeneratedCredential {
    
    private Long employeId;
    private String employeNom;
    private String username;
    private String password;
    private LocalDateTime dateCreation;
    
    public GeneratedCredential(Long employeId, String employeNom, String username, String password) {
        this.employeId = employeId;
        this.employeNom = employeNom;
        this.username = username;
        this.password = password;
        this.dateCreation = LocalDateTime.now();
    }
    
    // Getters
    public Long getEmployeId() {
        return employeId;
    }
    
    public String getEmployeNom() {
        return employeNom;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public LocalDateTime getDateCreationObject() {
        return dateCreation;
    }
    
    public String getDateCreation() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateCreation.format(formatter);
    }
    
    // Setters
    public void setEmployeId(Long employeId) {
        this.employeId = employeId;
    }
    
    public void setEmployeNom(String employeNom) {
        this.employeNom = employeNom;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
