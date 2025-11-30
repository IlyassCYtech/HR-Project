package com.gestionrh.dao;

import java.util.List;

import com.gestionrh.model.Utilisateur;
import com.gestionrh.model.Utilisateur.Role;
import com.gestionrh.model.Utilisateur.StatutUtilisateur;

/**
 * Interface DAO pour l'entité Utilisateur
 */
public interface UtilisateurDAO extends GenericDAO<Utilisateur, Long> {
    
    /**
     * Trouve un utilisateur par son nom d'utilisateur
     * @param username Le nom d'utilisateur
     * @return L'utilisateur trouvé ou null
     */
    Utilisateur findByUsername(String username);
    
    /**
     * Trouve un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur trouvé ou null
     */
    Utilisateur findByEmail(String email);
    
    /**
     * Authentifie un utilisateur
     * @param username Le nom d'utilisateur
     * @param password Le mot de passe
     * @return L'utilisateur authentifié ou null
     */
    Utilisateur authenticate(String username, String password);
    
    /**
     * Trouve les utilisateurs par rôle
     * @param role Le rôle recherché
     * @return Liste des utilisateurs ayant ce rôle
     */
    List<Utilisateur> findByRole(Role role);
    
    /**
     * Trouve les utilisateurs par statut
     * @param statut Le statut recherché
     * @return Liste des utilisateurs ayant ce statut
     */
    List<Utilisateur> findByStatut(StatutUtilisateur statut);
    
    /**
     * Trouve les utilisateurs actifs
     * @return Liste des utilisateurs actifs
     */
    List<Utilisateur> findActiveUsers();
    
    /**
     * Trouve les utilisateurs bloqués
     * @return Liste des utilisateurs bloqués
     */
    List<Utilisateur> findBlockedUsers();
    
    /**
     * Vérifie si un nom d'utilisateur existe
     * @param username Le nom d'utilisateur à vérifier
     * @return true si le nom d'utilisateur existe, false sinon
     */
    boolean existsByUsername(String username);
    
    /**
     * Vérifie si un email existe
     * @param email L'email à vérifier
     * @return true si l'email existe, false sinon
     */
    boolean existsByEmail(String email);
    
    /**
     * Met à jour la dernière connexion d'un utilisateur
     * @param integer L'ID de l'utilisateur
     */
    void updateLastLogin(Integer integer);
    
    /**
     * Incrémente le nombre de tentatives de connexion
     * @param userId L'ID de l'utilisateur
     */
    void incrementLoginAttempts(Integer userId);
    
    /**
     * Remet à zéro les tentatives de connexion
     * @param userId L'ID de l'utilisateur
     */
    void resetLoginAttempts(Integer userId);
    
    /**
     * Trouve un utilisateur par l'ID de l'employé associé
     * @param employeId L'ID de l'employé
     * @return L'utilisateur trouvé ou null
     */
    Utilisateur findByEmployeId(Long employeId);
}
