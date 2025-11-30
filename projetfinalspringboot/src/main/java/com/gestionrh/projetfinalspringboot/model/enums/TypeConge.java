package com.gestionrh.projetfinalspringboot.model.enums;

/**
 * Énumération représentant les différents types de congés
 */
public enum TypeConge {
    CONGES_PAYES("Congés payés"),
    MALADIE("Maladie"),
    MATERNITE("Maternité"),
    PATERNITE("Paternité"),
    FORMATION("Formation"),
    SANS_SOLDE("Sans solde");
    
    private final String libelle;
    
    TypeConge(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
}
