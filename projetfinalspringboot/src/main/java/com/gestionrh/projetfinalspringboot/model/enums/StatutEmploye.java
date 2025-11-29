package com.gestionrh.projetfinalspringboot.model.enums;

/**
 * Énumération représentant le statut d'un employé
 * Doit correspondre aux valeurs ENUM de la table 'employes' en base de données:
 * ENUM('ACTIF', 'SUSPENDU', 'DEMISSION', 'LICENCIE', 'RETRAITE')
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
}
