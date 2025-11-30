package com.gestionrh.projetfinalspringboot.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionrh.projetfinalspringboot.model.entity.CongeAbsence;
import com.gestionrh.projetfinalspringboot.model.enums.StatutConge;
import com.gestionrh.projetfinalspringboot.model.enums.TypeConge;

/**
 * Repository pour l'entité Congé/Absence
 */
@Repository
public interface CongeAbsenceRepository extends JpaRepository<CongeAbsence, Long> {
    
    // Méthode avec chargement EAGER des relations pour les vues
    @Query("SELECT DISTINCT c FROM CongeAbsence c " +
           "LEFT JOIN FETCH c.employe " +
           "LEFT JOIN FETCH c.approuvePar " +
           "ORDER BY c.dateDebut DESC")
    List<CongeAbsence> findAllWithDetails();
    
    List<CongeAbsence> findByEmployeId(Long employeId);
    
    List<CongeAbsence> findByStatut(StatutConge statut);
    
    List<CongeAbsence> findByTypeConge(TypeConge typeConge);
    
    @Query("SELECT c FROM CongeAbsence c WHERE c.employe.id = :employeId AND c.statut = :statut")
    List<CongeAbsence> findByEmployeIdAndStatut(@Param("employeId") Long employeId, @Param("statut") StatutConge statut);
    
    @Query("SELECT c FROM CongeAbsence c WHERE c.employe.id = :employeId AND YEAR(c.dateDebut) = :annee")
    List<CongeAbsence> findByEmployeIdAndAnnee(@Param("employeId") Long employeId, @Param("annee") int annee);
    
    @Query("SELECT c FROM CongeAbsence c WHERE c.employe.id = :employeId AND c.dateDebut BETWEEN :startDate AND :endDate")
    List<CongeAbsence> findByEmployeIdAndDateDebutBetween(
        @Param("employeId") Long employeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT c FROM CongeAbsence c WHERE c.dateDebut BETWEEN :startDate AND :endDate")
    List<CongeAbsence> findByPeriode(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT c FROM CongeAbsence c WHERE c.dateDebut <= :date AND c.dateFin >= :date AND c.statut = 'APPROUVE'")
    List<CongeAbsence> findCongesEnCoursADate(@Param("date") LocalDate date);
    
    @Query("SELECT c FROM CongeAbsence c WHERE c.employe.manager.id = :chefId AND c.statut = 'EN_ATTENTE'")
    List<CongeAbsence> findCongesEquipeEnAttente(@Param("chefId") Long chefId);
    
    @Query("SELECT c FROM CongeAbsence c WHERE c.employe.manager.id = :chefId")
    List<CongeAbsence> findCongesEquipe(@Param("chefId") Long chefId);
    
    @Query("SELECT SUM(c.nombreJours) FROM CongeAbsence c WHERE c.employe.id = :employeId AND c.typeConge = :typeConge AND c.statut = 'APPROUVE' AND YEAR(c.dateDebut) = :annee")
    Integer sumJoursCongesByEmployeAndTypeAndAnnee(
        @Param("employeId") Long employeId,
        @Param("typeConge") TypeConge typeConge,
        @Param("annee") int annee
    );
    
    @Query("SELECT SUM(c.nombreJours) FROM CongeAbsence c WHERE c.employe.id = :employeId AND c.statut = 'APPROUVE' AND YEAR(c.dateDebut) = :annee")
    Integer calculerJoursCongesUtilises(@Param("employeId") Long employeId, @Param("annee") int annee);
    
    @Query("SELECT c FROM CongeAbsence c WHERE c.employe.id = :employeId AND " +
           "((c.dateDebut BETWEEN :dateDebut AND :dateFin) OR " +
           "(c.dateFin BETWEEN :dateDebut AND :dateFin) OR " +
           "(c.dateDebut <= :dateDebut AND c.dateFin >= :dateFin)) AND " +
           "c.statut IN ('EN_ATTENTE', 'APPROUVE')")
    List<CongeAbsence> findConflits(
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin,
        @Param("employeId") Long employeId
    );
    
    @Query("SELECT c FROM CongeAbsence c WHERE c.employe.departement.id = :departementId AND c.statut = :statut")
    List<CongeAbsence> findByDepartementIdAndStatut(
        @Param("departementId") Long departementId,
        @Param("statut") StatutConge statut
    );
    
    @Query("SELECT c FROM CongeAbsence c WHERE c.approuvePar.id = :approbateurId ORDER BY c.dateDebut DESC")
    List<CongeAbsence> findByApprobateurId(@Param("approbateurId") Long approbateurId);
    
    @Query("SELECT COUNT(c) FROM CongeAbsence c WHERE c.statut = :statut")
    Long countByStatut(@Param("statut") StatutConge statut);
    
    // Nouvelles méthodes pour le service
    @Query("SELECT c FROM CongeAbsence c WHERE c.statut = 'EN_ATTENTE' ORDER BY c.dateDebut ASC")
    List<CongeAbsence> findByStatutEnAttente();
    
    @Query("SELECT COUNT(c) FROM CongeAbsence c WHERE c.statut = 'EN_ATTENTE'")
    Long countByStatutEnAttente();
    
    @Query("SELECT COUNT(c) FROM CongeAbsence c WHERE c.statut = 'APPROUVE'")
    Long countByStatutApprouve();
    
    @Query("SELECT COUNT(c) FROM CongeAbsence c WHERE c.statut = 'REFUSE'")
    Long countByStatutRefuse();
}

