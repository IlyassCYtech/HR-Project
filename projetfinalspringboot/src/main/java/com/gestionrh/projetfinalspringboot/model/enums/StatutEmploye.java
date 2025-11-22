package com.gestionrh.projetfinalspringboot.model.enums;

/**
 * Énumération représentant le statut d'un employé
 */
public enum StatutEmploye {
    ACTIF("Actif"),
    EN_CONGE("En congé"),
    SUSPENDU("Suspendu"),
    DEMISSIONNAIRE("Démissionnaire"),
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
