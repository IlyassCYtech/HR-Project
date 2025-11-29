package com.gestionrh.projetfinalspringboot.model.enums;

/**
 * Énumération représentant les rôles des utilisateurs
 */
public enum Role {
    ADMIN("Administrateur"),
    RH("Ressources Humaines"),
    CHEF_DEPT("Chef de Département"),
    CHEF_PROJET("Chef de Projet"),
    EMPLOYE("Employé");
    
    private final String libelle;
    
    Role(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
}
