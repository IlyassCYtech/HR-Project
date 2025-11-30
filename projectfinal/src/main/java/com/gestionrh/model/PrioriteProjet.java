package com.gestionrh.model;

/**
 * Énumération représentant les différents niveaux de priorité d'un projet
 */
public enum PrioriteProjet {
    FAIBLE("Faible"),
    NORMALE("Normale"), 
    HAUTE("Haute"),
    CRITIQUE("Critique");
    
    private final String libelle;
    
    PrioriteProjet(String libelle) {
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
