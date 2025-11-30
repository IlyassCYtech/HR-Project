package com.gestionrh.dao;

import java.time.LocalDate;
import java.util.List;

import com.gestionrh.model.FichePaie;

public interface FichePaieDAO extends GenericDAO<FichePaie, Long> {
    List<FichePaie> findByEmployeId(Long employeId);
    List<FichePaie> findByMoisAnnee(int mois, int annee);
    List<FichePaie> findByAnnee(int annee);
    FichePaie findByEmployeAndMoisAnnee(Long employeId, int mois, int annee);
    List<FichePaie> findByPeriode(LocalDate dateDebut, LocalDate dateFin);
    List<FichePaie> findByBrutSuperieurA(Double montant);
    List<FichePaie> findNonGenerees(int mois, int annee);
    double calculerMasseSalariale(int mois, int annee);
}
