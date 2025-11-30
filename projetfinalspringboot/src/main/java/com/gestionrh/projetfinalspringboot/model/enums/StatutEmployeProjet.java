package com.gestionrh.projetfinalspringboot.model.enums;

/**
 * Énumération représentant le statut d'un employé sur un projet
 */
public enum StatutEmployeProjet {
    ACTIF("Actif"),
    TERMINE("Terminé"),
    SUSPENDU("Suspendu");
    
    private final String libelle;
    
    StatutEmployeProjet(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
}
