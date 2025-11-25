package com.gestionrh.projetfinalspringboot.model.enums;

/**
 * Énumération représentant la priorité d'un projet
 */
public enum PrioriteProjet {
    BASSE("Basse"),
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
}
