package com.gestionrh.projetfinalspringboot.model.enums;

/**
 * Énumération représentant les grades des employés
 */
public enum Grade {
    STAGIAIRE("Stagiaire"),
    JUNIOR("Junior"),
    CONFIRME("Confirmé"),
    SENIOR("Senior"),
    EXPERT("Expert"),
    MANAGER("Manager"),
    DIRECTEUR("Directeur"),
    PRINCIPAL("Principal");
    
    private final String libelle;
    
    Grade(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
}
