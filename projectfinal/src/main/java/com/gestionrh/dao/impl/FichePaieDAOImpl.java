package com.gestionrh.dao.impl;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.FichePaieDAO;
import com.gestionrh.model.FichePaie;
import com.gestionrh.util.TransactionUtil;

public class FichePaieDAOImpl extends GenericDAOImpl<FichePaie, Long> implements FichePaieDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(FichePaieDAOImpl.class);

    public FichePaieDAOImpl() {
        super();
    }

    @Override
    public FichePaie findById(Long id) {
        logger.debug("Recherche de la fiche de paie par ID: {}", id);
        return TransactionUtil.executeInTransaction(session -> {
            Query<FichePaie> query = session
                .createQuery("SELECT f FROM FichePaie f LEFT JOIN FETCH f.employe e LEFT JOIN FETCH e.departement WHERE f.id = :id", FichePaie.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        });
    }

    @Override
    public List<FichePaie> findByEmployeId(Long employeId) {
        logger.debug("Recherche des fiches de paie par employé: {}", employeId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<FichePaie> query = session
                .createQuery("SELECT DISTINCT f FROM FichePaie f LEFT JOIN FETCH f.employe e LEFT JOIN FETCH e.departement WHERE f.employe.id = :employeId ORDER BY f.annee DESC, f.mois DESC", FichePaie.class);
            query.setParameter("employeId", employeId);
            return query.getResultList();
        });
    }

    @Override
    public List<FichePaie> findByMoisAnnee(int mois, int annee) {
        logger.debug("Recherche des fiches de paie pour {}/{}", mois, annee);
        return TransactionUtil.executeInTransaction(session -> {
            Query<FichePaie> query = session
                .createQuery("SELECT DISTINCT f FROM FichePaie f LEFT JOIN FETCH f.employe e LEFT JOIN FETCH e.departement WHERE f.mois = :mois AND f.annee = :annee ORDER BY e.nom, e.prenom", FichePaie.class);
            query.setParameter("mois", mois);
            query.setParameter("annee", annee);
            return query.getResultList();
        });
    }

    @Override
    public List<FichePaie> findByAnnee(int annee) {
        logger.debug("Recherche des fiches de paie pour l'année: {}", annee);
        return TransactionUtil.executeInTransaction(session -> {
            Query<FichePaie> query = session
                .createQuery("SELECT DISTINCT f FROM FichePaie f LEFT JOIN FETCH f.employe e LEFT JOIN FETCH e.departement WHERE f.annee = :annee ORDER BY f.mois DESC, e.nom", FichePaie.class);
            query.setParameter("annee", annee);
            return query.getResultList();
        });
    }

    @Override
    public FichePaie findByEmployeAndMoisAnnee(Long employeId, int mois, int annee) {
        logger.debug("Recherche de la fiche de paie pour employé {} - {}/{}", employeId, mois, annee);
        return TransactionUtil.executeInTransaction(session -> {
            Query<FichePaie> query = session
                .createQuery("SELECT f FROM FichePaie f LEFT JOIN FETCH f.employe e LEFT JOIN FETCH e.departement WHERE f.employe.id = :employeId AND f.mois = :mois AND f.annee = :annee", FichePaie.class);
            query.setParameter("employeId", employeId);
            query.setParameter("mois", mois);
            query.setParameter("annee", annee);
            return query.uniqueResult();
        });
    }

    @Override
    public List<FichePaie> findByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        logger.debug("Recherche des fiches de paie entre {} et {}", dateDebut, dateFin);
        return TransactionUtil.executeInTransaction(session -> {
            Query<FichePaie> query = session
                .createQuery("SELECT DISTINCT f FROM FichePaie f LEFT JOIN FETCH f.employe e LEFT JOIN FETCH e.departement WHERE f.dateCreation BETWEEN :debut AND :fin ORDER BY f.dateCreation DESC", FichePaie.class);
            query.setParameter("debut", dateDebut);
            query.setParameter("fin", dateFin);
            return query.getResultList();
        });
    }

    @Override
    public List<FichePaie> findByBrutSuperieurA(Double montant) {
        logger.debug("Recherche des fiches de paie avec salaire brut > {}", montant);
        return TransactionUtil.executeInTransaction(session -> {
            Query<FichePaie> query = session
                .createQuery("SELECT DISTINCT f FROM FichePaie f LEFT JOIN FETCH f.employe e LEFT JOIN FETCH e.departement WHERE f.salaireBrut > :montant ORDER BY f.salaireBrut DESC", FichePaie.class);
            query.setParameter("montant", montant);
            return query.getResultList();
        });
    }

    @Override
    public List<FichePaie> findNonGenerees(int mois, int annee) {
        logger.debug("Recherche des employés sans fiche de paie pour {}/{}", mois, annee);
        return TransactionUtil.executeInTransaction(session -> {
            Query<FichePaie> query = session
                .createQuery("SELECT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.statut = 'ACTIF' " +
                        "AND e.id NOT IN (" +
                        "SELECT f.employe.id FROM FichePaie f " +
                        "WHERE f.mois = :mois AND f.annee = :annee" +
                        ") ORDER BY e.nom, e.prenom", FichePaie.class);
            query.setParameter("mois", mois);
            query.setParameter("annee", annee);
            return query.getResultList();
        });
    }

    @Override
    public double calculerMasseSalariale(int mois, int annee) {
        logger.debug("Calcul de la masse salariale pour {}/{}", mois, annee);
        return TransactionUtil.executeInTransaction(session -> {
            // Compter d'abord le nombre de fiches
            Query<Long> countQuery = session
                .createQuery("SELECT COUNT(f) FROM FichePaie f WHERE f.mois = :mois AND f.annee = :annee", Long.class);
            countQuery.setParameter("mois", mois);
            countQuery.setParameter("annee", annee);
            Long count = countQuery.uniqueResult();
            logger.debug("Nombre de fiches de paie trouvées pour {}/{}: {}", mois, annee, count);
            
            Query<java.math.BigDecimal> query = session
                .createQuery("SELECT COALESCE(SUM(" +
                           "COALESCE(f.salaireBase, 0) + " +
                           "COALESCE(f.primePerformance, 0) + " +
                           "COALESCE(f.primeAnciennete, 0) + " +
                           "COALESCE(f.primeResponsabilite, 0) + " +
                           "COALESCE(f.autresPrimes, 0) + " +
                           "(COALESCE(f.heuresSupplementaires, 0) * COALESCE(f.tauxHoraireSup, 0))" +
                           "), 0.0) FROM FichePaie f WHERE f.mois = :mois AND f.annee = :annee", 
                           java.math.BigDecimal.class);
            query.setParameter("mois", mois);
            query.setParameter("annee", annee);
            java.math.BigDecimal masse = query.uniqueResult();
            double result = masse != null ? masse.doubleValue() : 0.0;
            logger.debug("Masse salariale calculée: {} pour {}/{}", result, mois, annee);
            return result;
        });
    }

    @Override
    public List<FichePaie> findAll() {
        logger.debug("Récupération de toutes les fiches de paie");
        return TransactionUtil.executeInTransaction(session -> {
            Query<FichePaie> query = session
                .createQuery("SELECT DISTINCT f FROM FichePaie f LEFT JOIN FETCH f.employe e LEFT JOIN FETCH e.departement ORDER BY f.annee DESC, f.mois DESC, e.nom", FichePaie.class);
            return query.getResultList();
        });
    }
    
}
