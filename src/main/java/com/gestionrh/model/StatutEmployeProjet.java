package com.gestionrh.model;

/**
 * Énumération représentant les différents statuts d'affectation d'un employé à un projet
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
    
    @Override
    public String toString() {
        return libelle;
    }
}
