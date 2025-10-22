package com.gestionrh.dao;

import com.gestionrh.model.CongeAbsence;
import com.gestionrh.model.CongeAbsence.StatutDemande;
import com.gestionrh.model.TypeConge;
import java.time.LocalDate;
import java.util.List;

public interface CongeAbsenceDAO extends GenericDAO<CongeAbsence, Long> {
    List<CongeAbsence> findByEmployeId(Long employeId);
    List<CongeAbsence> findByStatut(StatutDemande statut);
    List<CongeAbsence> findByTypeConge(TypeConge typeConge);
    List<CongeAbsence> findByPeriode(LocalDate dateDebut, LocalDate dateFin);
    List<CongeAbsence> findEnAttente();
    List<CongeAbsence> findByApprobateur(Long approbateurId);
    List<CongeAbsence> findCongesEmployeAnnee(Long employeId, int annee);
    int calculerJoursCongesUtilises(Long employeId, int annee);
    List<CongeAbsence> findConflits(LocalDate dateDebut, LocalDate dateFin, Long employeId);
    List<CongeAbsence> findCongesEquipe(Long chefId);
    long countByStatut(StatutDemande statut);
    
    // Méthodes spécifiques pour l'approbation/rejet avec validation
    void approuverConge(Long congeId, Long approbateurId, String commentaire);
    void rejeterConge(Long congeId, Long approbateurId, String commentaire);
}
