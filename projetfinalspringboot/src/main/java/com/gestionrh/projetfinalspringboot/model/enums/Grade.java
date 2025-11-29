package com.gestionrh.projetfinalspringboot.model.enums;

/**
 * Énumération représentant les grades des employés
 * Doit correspondre exactement à l'ENUM dans la base de données
 */
public enum Grade {
    STAGIAIRE("Stagiaire"),
    JUNIOR("Junior"),
    SENIOR("Senior"),
    EXPERT("Expert"),
    MANAGER("Manager"),
    DIRECTEUR("Directeur");
    
    private final String libelle;
    
    Grade(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
}
