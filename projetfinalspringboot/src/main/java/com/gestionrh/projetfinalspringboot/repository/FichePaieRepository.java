package com.gestionrh.projetfinalspringboot.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionrh.projetfinalspringboot.model.entity.FichePaie;

/**
 * Repository pour l'entité Fiche de Paie
 */
@Repository
public interface FichePaieRepository extends JpaRepository<FichePaie, Long> {
    
    @Query("SELECT DISTINCT f FROM FichePaie f " +
           "LEFT JOIN FETCH f.employe " +
           "LEFT JOIN FETCH f.validePar " +
           "ORDER BY f.annee DESC, f.mois DESC")
    List<FichePaie> findAllWithDetails();
    
    List<FichePaie> findByEmployeId(Long employeId);
    
    Optional<FichePaie> findByEmployeIdAndMoisAndAnnee(Long employeId, Integer mois, Integer annee);
    
    List<FichePaie> findByMoisAndAnnee(Integer mois, Integer annee);
    
    List<FichePaie> findByAnnee(Integer annee);
    
    @Query("SELECT f FROM FichePaie f WHERE f.employe.id = :employeId ORDER BY f.annee DESC, f.mois DESC")
    List<FichePaie> findByEmployeIdOrderByDateDesc(@Param("employeId") Long employeId);
    
    @Query("SELECT f FROM FichePaie f WHERE f.employe.departement.id = :departementId AND f.mois = :mois AND f.annee = :annee")
    List<FichePaie> findByDepartementIdAndMoisAndAnnee(
        @Param("departementId") Long departementId,
        @Param("mois") Integer mois,
        @Param("annee") Integer annee
    );
    
    @Query("SELECT f FROM FichePaie f WHERE CAST(f.dateCreation AS date) BETWEEN :dateDebut AND :dateFin ORDER BY f.dateCreation")
    List<FichePaie> findByPeriode(@Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);
    
    @Query("SELECT f FROM FichePaie f WHERE f.salaireBase > :montant ORDER BY f.salaireBase DESC")
    List<FichePaie> findBySalaireBaseGreaterThan(@Param("montant") Double montant);
    
    @Query("SELECT e.id FROM Employe e WHERE e.statut = 'ACTIF' AND e.id NOT IN " +
           "(SELECT f.employe.id FROM FichePaie f WHERE f.mois = :mois AND f.annee = :annee)")
    List<Long> findEmployeIdsWithoutFichePaie(@Param("mois") Integer mois, @Param("annee") Integer annee);
    
    @Query("SELECT SUM(f.salaireBase) FROM FichePaie f WHERE f.mois = :mois AND f.annee = :annee")
    Double calculerMasseSalariale(@Param("mois") Integer mois, @Param("annee") Integer annee);
    
    // Note: calculerMasseSalarialeNette supprimée car salaireNet est une méthode calculée, pas un champ
    // Utiliser la méthode dans le service pour calculer la masse salariale nette
    
    @Query("SELECT COUNT(f) FROM FichePaie f WHERE f.mois = :mois AND f.annee = :annee")
    Long countByMoisAndAnnee(@Param("mois") Integer mois, @Param("annee") Integer annee);
    
    @Query("SELECT COUNT(f) FROM FichePaie f WHERE f.employe.id = :employeId")
    Long countByEmployeId(@Param("employeId") Long employeId);
}

