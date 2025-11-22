package com.gestionrh.projetfinalspringboot.model.enums;

/**
 * Énumération représentant le statut d'un utilisateur
 */
public enum StatutUtilisateur {
    ACTIF("Actif"),
    INACTIVE("Inactif"),
    BLOQUE("Bloqué");
    
    private final String libelle;
    
    StatutUtilisateur(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
}
