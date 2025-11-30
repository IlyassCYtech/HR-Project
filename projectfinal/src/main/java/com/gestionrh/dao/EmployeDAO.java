package com.gestionrh.dao;

import java.time.LocalDate;
import java.util.List;

import com.gestionrh.model.Employe;
import com.gestionrh.model.Grade;
import com.gestionrh.model.StatutEmploye;

public interface EmployeDAO extends GenericDAO<Employe, Long> {
    List<Employe> findByNom(String nom);
    List<Employe> findByPrenom(String prenom);
    List<Employe> findByNomOrPrenom(String nomPrenom);
    List<Employe> findByDepartementId(Long departementId);
    List<Employe> findWithoutDepartement();
    List<Employe> findByManagerId(Long managerId);
    List<Employe> findByStatut(StatutEmploye statut);
    List<Employe> findByGrade(Grade grade);
    List<Employe> findByGrades(Grade... grades);
    List<Employe> findByPoste(String poste);
    Employe findByMatricule(String matricule);
    Employe findByEmail(String email);
    List<Employe> findActifs();
    List<Employe> findPotentialManagers();
    List<Employe> findByDateEmbauche(LocalDate dateEmbauche);
    List<Employe> findByPeriodeEmbauche(LocalDate dateDebut, LocalDate dateFin);
    long countByDepartement(Long departementId);
    long countByStatut(StatutEmploye statut);
    boolean existsByMatricule(String matricule);
    boolean existsByEmail(String email);
}
