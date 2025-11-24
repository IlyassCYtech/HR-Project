package com.gestionrh.projetfinalspringboot.model.enums;

/**
 * Énumération représentant les différents statuts d'un projet
 */
public enum StatutProjet {
    PLANIFIE("Planifié"),
    EN_COURS("En cours"),
    TERMINE("Terminé"),
    ANNULE("Annulé");
    
    private final String libelle;
    
    StatutProjet(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
}
