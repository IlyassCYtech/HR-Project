package com.gestionrh.dao;

import com.gestionrh.model.EmployeProjet;
import com.gestionrh.model.StatutEmployeProjet;
import java.time.LocalDate;
import java.util.List;

public interface EmployeProjetDAO extends GenericDAO<EmployeProjet, Long> {
    List<EmployeProjet> findByEmployeId(Long employeId);
    List<EmployeProjet> findByProjetId(Long projetId);
    List<EmployeProjet> findByStatut(StatutEmployeProjet statut);
    List<EmployeProjet> findByEmployeAndStatut(Long employeId, StatutEmployeProjet statut);
    List<EmployeProjet> findByProjetAndStatut(Long projetId, StatutEmployeProjet statut);
    List<EmployeProjet> findActifs();
    List<EmployeProjet> findByPeriode(LocalDate dateDebut, LocalDate dateFin);
    EmployeProjet findByEmployeAndProjet(Long employeId, Long projetId);
    List<EmployeProjet> findProjetsEmployeActifs(Long employeId);
    List<EmployeProjet> findEmployesProjetsActifs(Long projetId);
    int countEmployesParProjet(Long projetId);
}
