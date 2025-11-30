package com.gestionrh.dao;

import java.time.LocalDate;
import java.util.List;

import com.gestionrh.model.Projet;
import com.gestionrh.model.StatutProjet;

public interface ProjetDAO extends GenericDAO<Projet, Long> {
    List<Projet> findByStatut(StatutProjet statut);
    List<Projet> findByDepartementId(Long departementId);
    List<Projet> findByChefProjetId(Long chefProjetId);
    List<Projet> findByDateDebut(LocalDate dateDebut);
    List<Projet> findByDateFin(LocalDate dateFin);
    List<Projet> findProjetsActifs();
    List<Projet> findProjetsEnRetard();
    List<Projet> findByNom(String nom);
    long countByStatut(StatutProjet statut);
}
