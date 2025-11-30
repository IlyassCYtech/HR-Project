package com.gestionrh.projetfinalspringboot.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionrh.projetfinalspringboot.model.entity.Projet;
import com.gestionrh.projetfinalspringboot.model.enums.StatutProjet;

/**
 * Repository pour l'entité Projet
 */
@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {
    
    // Méthode avec chargement EAGER des relations pour les vues
    @Query("SELECT p FROM Projet p " +
           "LEFT JOIN FETCH p.chefProjet " +
           "LEFT JOIN FETCH p.departement " +
           "ORDER BY p.dateDebut DESC")
    List<Projet> findAllWithDetails();
    
    List<Projet> findByStatut(StatutProjet statut);
    
    List<Projet> findByDepartementId(Long departementId);
    
    List<Projet> findByChefProjetId(Long chefProjetId);
    
    @Query("SELECT p FROM Projet p WHERE p.nom LIKE %:nom% ORDER BY p.dateDebut DESC")
    List<Projet> findByNomContaining(@Param("nom") String nom);
    
    @Query("SELECT p FROM Projet p WHERE p.nom LIKE %:searchTerm% OR p.description LIKE %:searchTerm%")
    List<Projet> searchProjets(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT p FROM Projet p WHERE p.dateDebut BETWEEN :startDate AND :endDate")
    List<Projet> findByDateDebutBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT p FROM Projet p WHERE p.dateFinPrevue BETWEEN :startDate AND :endDate")
    List<Projet> findByDateFinPrevueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT p FROM Projet p WHERE p.statut = 'EN_COURS' ORDER BY p.dateDebut DESC")
    List<Projet> findProjetsActifs();
    
    @Query("SELECT p FROM Projet p WHERE p.dateFinPrevue < :date AND p.statut = 'EN_COURS'")
    List<Projet> findProjetsEnRetard(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Projet p LEFT JOIN FETCH p.employes WHERE p.id = :id")
    Optional<Projet> findByIdWithEmployes(@Param("id") Long id);
    
    @Query("SELECT p FROM Projet p LEFT JOIN FETCH p.departement LEFT JOIN FETCH p.chefProjet WHERE p.id = :id")
    Optional<Projet> findByIdWithDetails(@Param("id") Long id);
    
    // Méthode complète pour la page de détail (tous les champs + employes + departement + chefProjet)
    @Query("SELECT DISTINCT p FROM Projet p " +
           "LEFT JOIN FETCH p.chefProjet " +
           "LEFT JOIN FETCH p.departement " +
           "LEFT JOIN FETCH p.employes ep " +
           "LEFT JOIN FETCH ep.employe e " +
           "LEFT JOIN FETCH e.departement " +
           "WHERE p.id = :id")
    Optional<Projet> findByIdComplete(@Param("id") Long id);
    
    @Query("SELECT COUNT(p) FROM Projet p WHERE p.statut = :statut")
    Long countByStatut(@Param("statut") StatutProjet statut);
    
    @Query("SELECT COUNT(p) FROM Projet p WHERE p.departement.id = :departementId")
    Long countByDepartementId(@Param("departementId") Long departementId);
    
    @Query("SELECT COUNT(p) FROM Projet p WHERE p.chefProjet.id = :chefProjetId")
    Long countByChefProjetId(@Param("chefProjetId") Long chefProjetId);
}

