package com.gestionrh.projetfinalspringboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.model.enums.Role;
import com.gestionrh.projetfinalspringboot.model.enums.StatutUtilisateur;

/**
 * Repository pour l'entité Utilisateur
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    @Query("SELECT DISTINCT u FROM Utilisateur u " +
           "LEFT JOIN FETCH u.employe " +
           "ORDER BY u.username")
    List<Utilisateur> findAllWithDetails();
    
    Optional<Utilisateur> findByUsername(String username);
    
    Optional<Utilisateur> findByEmail(String email);
    
    List<Utilisateur> findByRole(Role role);
    
    List<Utilisateur> findByStatut(StatutUtilisateur statut);
    
    Optional<Utilisateur> findByEmployeId(Long employeId);
    
    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.employe WHERE u.username = :username")
    Optional<Utilisateur> findByUsernameWithEmploye(@Param("username") String username);
    
    @Query("SELECT u FROM Utilisateur u WHERE u.statut = 'ACTIF' ORDER BY u.username")
    List<Utilisateur> findActifs();
    
    @Query("SELECT u FROM Utilisateur u WHERE u.username LIKE %:searchTerm% OR u.email LIKE %:searchTerm%")
    List<Utilisateur> searchUtilisateurs(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.role = :role")
    Long countByRole(@Param("role") Role role);
    
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.statut = :statut")
    Long countByStatut(@Param("statut") StatutUtilisateur statut);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Utilisateur u WHERE u.username = :username")
    boolean existsByUsername(@Param("username") String username);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Utilisateur u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
}

