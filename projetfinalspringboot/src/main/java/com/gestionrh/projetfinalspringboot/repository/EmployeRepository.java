package com.gestionrh.projetfinalspringboot.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionrh.projetfinalspringboot.model.entity.Employe;
import com.gestionrh.projetfinalspringboot.model.enums.Grade;
import com.gestionrh.projetfinalspringboot.model.enums.StatutEmploye;

/**
 * Repository pour l'entité Employé
 */
@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    
    // Méthode avec chargement EAGER des relations pour les vues
    @Query("SELECT DISTINCT e FROM Employe e " +
           "LEFT JOIN FETCH e.departement " +
           "LEFT JOIN FETCH e.manager " +
           "ORDER BY e.nom, e.prenom")
    List<Employe> findAllWithDetails();
    
    Optional<Employe> findByMatricule(String matricule);
    
    Optional<Employe> findByEmail(String email);
    
    List<Employe> findByStatut(StatutEmploye statut);
    
    List<Employe> findByDepartementId(Long departementId);
    
    List<Employe> findByManagerId(Long managerId);
    
    List<Employe> findByGrade(Grade grade);
    
    @Query("SELECT e FROM Employe e WHERE e.grade IN :grades AND e.statut = 'ACTIF' ORDER BY e.nom, e.prenom")
    List<Employe> findByGrades(@Param("grades") List<Grade> grades);
    
    @Query("SELECT e FROM Employe e WHERE e.nom LIKE %:nom% ORDER BY e.nom, e.prenom")
    List<Employe> findByNomContaining(@Param("nom") String nom);
    
    @Query("SELECT e FROM Employe e WHERE e.prenom LIKE %:prenom% ORDER BY e.nom, e.prenom")
    List<Employe> findByPrenomContaining(@Param("prenom") String prenom);
    
    @Query("SELECT e FROM Employe e WHERE e.nom LIKE %:searchTerm% OR e.prenom LIKE %:searchTerm% OR e.matricule LIKE %:searchTerm%")
    List<Employe> searchEmployes(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT e FROM Employe e WHERE e.poste LIKE %:poste% ORDER BY e.nom, e.prenom")
    List<Employe> findByPosteContaining(@Param("poste") String poste);
    
    @Query("SELECT e FROM Employe e WHERE e.departement IS NULL AND e.statut = 'ACTIF' ORDER BY e.nom, e.prenom")
    List<Employe> findWithoutDepartement();
    
    @Query("SELECT e FROM Employe e WHERE e.dateEmbauche BETWEEN :startDate AND :endDate")
    List<Employe> findByDateEmbaucheBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e FROM Employe e WHERE e.statut = 'ACTIF' AND e.grade IN ('SENIOR', 'EXPERT', 'MANAGER', 'DIRECTEUR') ORDER BY e.nom, e.prenom")
    List<Employe> findPotentialManagers();
    
    @Query("SELECT COUNT(e) FROM Employe e WHERE e.statut = :statut")
    Long countByStatut(@Param("statut") StatutEmploye statut);
    
    @Query("SELECT COUNT(e) FROM Employe e WHERE e.departement.id = :departementId")
    Long countByDepartementId(@Param("departementId") Long departementId);
    
    @Query("SELECT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.id = :id")
    Optional<Employe> findByIdWithDepartement(@Param("id") Long id);
    
    @Query("SELECT e FROM Employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH e.manager WHERE e.id = :id")
    Optional<Employe> findByIdWithDetails(@Param("id") Long id);
    
    boolean existsByMatricule(String matricule);
    
    boolean existsByEmail(String email);
}

