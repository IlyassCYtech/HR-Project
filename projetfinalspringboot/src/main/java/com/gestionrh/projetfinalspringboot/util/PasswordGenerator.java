package com.gestionrh.projetfinalspringboot.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utilitaire pour générer des mots de passe BCrypt
 * Exécuter comme application Java standalone
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // Mot de passe admin
        String adminPassword = "admin123";
        String adminHash = passwordEncoder.encode(adminPassword);
        System.out.println("========================================");
        System.out.println("GÉNÉRATION DE MOTS DE PASSE BCRYPT");
        System.out.println("========================================\n");
        
        System.out.println("1. Admin (admin / admin123):");
        System.out.println("   Hash: " + adminHash);
        System.out.println();
        
        // Mot de passe standard pour les autres
        String standardPassword = "password123";
        String standardHash = passwordEncoder.encode(standardPassword);
        System.out.println("2. Utilisateurs standards (password123):");
        System.out.println("   Hash: " + standardHash);
        System.out.println();
        
        // Vérification
        System.out.println("========================================");
        System.out.println("VÉRIFICATION DES HASHES");
        System.out.println("========================================\n");
        
        boolean adminMatch = passwordEncoder.matches("admin123", adminHash);
        System.out.println("Admin password matches: " + adminMatch);
        
        boolean standardMatch = passwordEncoder.matches("password123", standardHash);
        System.out.println("Standard password matches: " + standardMatch);
        
        System.out.println("\n========================================");
        System.out.println("SCRIPT SQL À EXÉCUTER");
        System.out.println("========================================\n");
        
        System.out.println("USE gestion_rh_springboot;");
        System.out.println();
        System.out.println("-- Mettre à jour le mot de passe admin");
        System.out.println("UPDATE utilisateurs SET password_hash = '" + adminHash + "' WHERE username = 'admin';");
        System.out.println();
        System.out.println("-- Mettre à jour les autres utilisateurs");
        System.out.println("UPDATE utilisateurs SET password_hash = '" + standardHash + "' WHERE username != 'admin';");
        System.out.println();
        System.out.println("-- Vérifier");
        System.out.println("SELECT username, role, statut FROM utilisateurs;");
        
        System.out.println("\n========================================");
        System.out.println("IDENTIFIANTS DE CONNEXION");
        System.out.println("========================================");
        System.out.println("Admin: admin / admin123");
        System.out.println("RH: marie.dubois / password123");
        System.out.println("Chef projet: pierre.leroy / password123");
        System.out.println("========================================\n");
    }
}
