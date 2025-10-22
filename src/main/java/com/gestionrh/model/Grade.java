package com.gestionrh.model;

/**
 * Énumération représentant les différents grades d'un employé
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
    
    @Override
    public String toString() {
        return libelle;
    }
}
