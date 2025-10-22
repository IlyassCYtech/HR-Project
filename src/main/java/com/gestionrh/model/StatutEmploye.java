package com.gestionrh.model;

/**
 * Énumération représentant les différents statuts d'un employé
 */
public enum StatutEmploye {
    ACTIF("Actif"),
    SUSPENDU("Suspendu"),
    DEMISSION("Démission"),
    LICENCIE("Licencié"),
    RETRAITE("Retraité");
    
    private final String libelle;
    
    StatutEmploye(String libelle) {
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
