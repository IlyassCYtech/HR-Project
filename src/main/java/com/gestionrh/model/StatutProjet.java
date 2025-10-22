package com.gestionrh.model;

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
    
    @Override
    public String toString() {
        return libelle;
    }
}
