package com.gestionrh.projetfinalspringboot.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionrh.projetfinalspringboot.model.entity.EmployeProjet;
import com.gestionrh.projetfinalspringboot.model.enums.StatutEmployeProjet;

/**
 * Repository pour l'entité EmployeProjet (Association Employé-Projet)
 */
@Repository
public interface EmployeProjetRepository extends JpaRepository<EmployeProjet, Long> {
    
    List<EmployeProjet> findByEmployeId(Long employeId);
    
    List<EmployeProjet> findByProjetId(Long projetId);
    
    List<EmployeProjet> findByStatut(StatutEmployeProjet statut);
    
    @Query("SELECT ep FROM EmployeProjet ep WHERE ep.employe.id = :employeId AND ep.projet.id = :projetId")
    Optional<EmployeProjet> findByEmployeIdAndProjetId(
        @Param("employeId") Long employeId,
        @Param("projetId") Long projetId
    );
    
    @Query("SELECT ep FROM EmployeProjet ep WHERE ep.employe.id = :employeId AND ep.statut = :statut")
    List<EmployeProjet> findByEmployeIdAndStatut(
        @Param("employeId") Long employeId,
        @Param("statut") StatutEmployeProjet statut
    );
    
    @Query("SELECT ep FROM EmployeProjet ep WHERE ep.projet.id = :projetId AND ep.statut = :statut")
    List<EmployeProjet> findByProjetIdAndStatut(
        @Param("projetId") Long projetId,
        @Param("statut") StatutEmployeProjet statut
    );
    
    @Query("SELECT ep FROM EmployeProjet ep WHERE ep.statut = 'ACTIF' ORDER BY ep.dateAffectation DESC")
    List<EmployeProjet> findActifs();
    
    @Query("SELECT ep FROM EmployeProjet ep WHERE ep.employe.id = :employeId AND ep.statut = 'ACTIF'")
    List<EmployeProjet> findProjetsEmployeActifs(@Param("employeId") Long employeId);
    
    @Query("SELECT ep FROM EmployeProjet ep WHERE ep.projet.id = :projetId AND ep.statut = 'ACTIF'")
    List<EmployeProjet> findEmployesProjetsActifs(@Param("projetId") Long projetId);
    
    @Query("SELECT ep FROM EmployeProjet ep WHERE ep.dateAffectation BETWEEN :dateDebut AND :dateFin OR ep.dateFinAffectation BETWEEN :dateDebut AND :dateFin")
    List<EmployeProjet> findByPeriode(@Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);
    
    @Query("SELECT COUNT(ep) FROM EmployeProjet ep WHERE ep.projet.id = :projetId")
    Long countEmployesByProjetId(@Param("projetId") Long projetId);
    
    @Query("SELECT COUNT(ep) FROM EmployeProjet ep WHERE ep.projet.id = :projetId AND ep.statut = 'ACTIF'")
    Long countEmployesActifsByProjetId(@Param("projetId") Long projetId);
    
    @Query("SELECT COUNT(ep) FROM EmployeProjet ep WHERE ep.employe.id = :employeId")
    Long countProjetsByEmployeId(@Param("employeId") Long employeId);
    
    @Query("SELECT COUNT(ep) FROM EmployeProjet ep WHERE ep.employe.id = :employeId AND ep.statut = 'ACTIF'")
    Long countProjetsActifsByEmployeId(@Param("employeId") Long employeId);
}

