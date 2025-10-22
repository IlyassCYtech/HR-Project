package com.gestionrh.dao;

import com.gestionrh.model.Departement;
import java.util.List;

public interface DepartementDAO extends GenericDAO<Departement, Long> {
    List<Departement> findByNom(String nom);
    List<Departement> findActifs();
    long countEmployes(Long departementId);
    Departement findByChefId(Long chefId);
}
