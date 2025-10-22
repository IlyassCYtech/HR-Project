package com.gestionrh.model;

/**
 * Énumération représentant les différents statuts d'une demande de congé
 */
public enum StatutConge {
    EN_ATTENTE("En attente"),
    APPROUVE("Approuvé"),
    REFUSE("Refusé"),
    ANNULE("Annulé");
    
    private final String libelle;
    
    StatutConge(String libelle) {
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
