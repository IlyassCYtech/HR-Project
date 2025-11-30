package com.gestionrh.dao;

import java.util.List;

import com.gestionrh.model.Departement;

public interface DepartementDAO extends GenericDAO<Departement, Long> {
    List<Departement> findByNom(String nom);
    List<Departement> findActifs();
    long countEmployes(Long departementId);
    Departement findByChefId(Long chefId);
}
