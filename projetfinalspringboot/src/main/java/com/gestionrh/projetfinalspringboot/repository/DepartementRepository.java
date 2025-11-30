package com.gestionrh.projetfinalspringboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionrh.projetfinalspringboot.model.entity.Departement;

/**
 * Repository pour l'entité Département
 */
@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {
    
    // Méthode avec chargement EAGER des relations pour les vues
    @Query("SELECT DISTINCT d FROM Departement d " +
           "LEFT JOIN FETCH d.chefDepartement " +
           "LEFT JOIN FETCH d.employes " +
           "ORDER BY d.nom")
    List<Departement> findAllWithDetails();
    
    Optional<Departement> findByNom(String nom);
    
    List<Departement> findByActif(Boolean actif);
    
    @Query("SELECT d FROM Departement d WHERE d.nom LIKE %:nom%")
    List<Departement> findByNomContaining(@Param("nom") String nom);
    
    @Query("SELECT d FROM Departement d WHERE d.actif = true ORDER BY d.nom")
    List<Departement> findActifs();
    
    @Query("SELECT d FROM Departement d WHERE d.chefDepartement.id = :chefId")
    Optional<Departement> findByChefId(@Param("chefId") Long chefId);
    
    @Query("SELECT d FROM Departement d LEFT JOIN FETCH d.employes WHERE d.id = :id")
    Optional<Departement> findByIdWithEmployes(@Param("id") Long id);
    
    @Query("SELECT d FROM Departement d LEFT JOIN FETCH d.projets WHERE d.id = :id")
    Optional<Departement> findByIdWithProjets(@Param("id") Long id);
    
    @Query("SELECT d FROM Departement d LEFT JOIN FETCH d.chefDepartement LEFT JOIN FETCH d.employes WHERE d.id = :id")
    Optional<Departement> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT COUNT(e) FROM Employe e WHERE e.departement.id = :departementId")
    Long countEmployesByDepartementId(@Param("departementId") Long departementId);
    
    @Query("SELECT COUNT(e) FROM Employe e WHERE e.departement.id = :departementId AND e.statut = 'ACTIF'")
    Long countEmployesActifsByDepartementId(@Param("departementId") Long departementId);
    
    @Query("SELECT COUNT(p) FROM Projet p WHERE p.departement.id = :departementId")
    Long countProjetsByDepartementId(@Param("departementId") Long departementId);
    
    @Query("SELECT COUNT(p) FROM Projet p WHERE p.departement.id = :departementId AND p.statut = 'EN_COURS'")
    Long countProjetsActifsByDepartementId(@Param("departementId") Long departementId);
    
    // Nouvelles méthodes pour les fonctionnalités manquantes
    @Query("SELECT d FROM Departement d WHERE LOWER(d.nom) LIKE LOWER(CONCAT('%', :nom, '%'))")
    List<Departement> findByNomContainingIgnoreCase(@Param("nom") String nom);
    
    @Query("SELECT e FROM Employe e WHERE e.departement IS NULL")
    List<com.gestionrh.projetfinalspringboot.model.entity.Employe> findEmployesSansDepartement();
    
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Employe e SET e.departement.id = :departementId WHERE e.id = :employeId")
    void affecterEmployeAuDepartement(@Param("departementId") Long departementId, @Param("employeId") Long employeId);
    
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Employe e SET e.departement = NULL WHERE e.id = :employeId AND e.departement.id = :departementId")
    void retirerEmployeDuDepartement(@Param("departementId") Long departementId, @Param("employeId") Long employeId);
}

