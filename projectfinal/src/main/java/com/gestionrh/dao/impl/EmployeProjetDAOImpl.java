package com.gestionrh.dao.impl;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.EmployeProjetDAO;
import com.gestionrh.model.EmployeProjet;
import com.gestionrh.model.StatutEmployeProjet;

public class EmployeProjetDAOImpl extends GenericDAOImpl<EmployeProjet, Long> implements EmployeProjetDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeProjetDAOImpl.class);

    public EmployeProjetDAOImpl() {
        super();
    }

    @Override
    public List<EmployeProjet> findByEmployeId(Long employeId) {
        logger.debug("Recherche des affectations par employé: {}", employeId);
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep WHERE ep.employe.id = :employeId ORDER BY ep.dateAffectation DESC", EmployeProjet.class);
            query.setParameter("employeId", employeId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par employé: {}", employeId, e);
            throw e;
        }
    }

    @Override
    public List<EmployeProjet> findByProjetId(Long projetId) {
        logger.debug("Recherche des affectations par projet: {}", projetId);
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep WHERE ep.projet.id = :projetId ORDER BY ep.dateAffectation DESC", EmployeProjet.class);
            query.setParameter("projetId", projetId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par projet: {}", projetId, e);
            throw e;
        }
    }

    @Override
    public List<EmployeProjet> findByStatut(StatutEmployeProjet statut) {
        logger.debug("Recherche des affectations par statut: {}", statut);
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep WHERE ep.statut = :statut ORDER BY ep.dateAffectation DESC", EmployeProjet.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par statut: {}", statut, e);
            throw e;
        }
    }

    @Override
    public List<EmployeProjet> findByEmployeAndStatut(Long employeId, StatutEmployeProjet statut) {
        logger.debug("Recherche des affectations pour employé {} avec statut {}", employeId, statut);
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep WHERE ep.employe.id = :employeId AND ep.statut = :statut ORDER BY ep.dateAffectation DESC", EmployeProjet.class);
            query.setParameter("employeId", employeId);
            query.setParameter("statut", statut);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche pour employé {} statut {}", employeId, statut, e);
            throw e;
        }
    }

    @Override
    public List<EmployeProjet> findByProjetAndStatut(Long projetId, StatutEmployeProjet statut) {
        logger.debug("Recherche des affectations pour projet {} avec statut {}", projetId, statut);
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep WHERE ep.projet.id = :projetId AND ep.statut = :statut ORDER BY ep.dateAffectation DESC", EmployeProjet.class);
            query.setParameter("projetId", projetId);
            query.setParameter("statut", statut);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche pour projet {} statut {}", projetId, statut, e);
            throw e;
        }
    }

    @Override
    public List<EmployeProjet> findActifs() {
        logger.debug("Recherche des affectations actives");
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep WHERE ep.statut = :statut ORDER BY ep.dateAffectation DESC", EmployeProjet.class);
            query.setParameter("statut", StatutEmployeProjet.ACTIF);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche des affectations actives", e);
            throw e;
        }
    }

    @Override
    public List<EmployeProjet> findByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        logger.debug("Recherche des affectations entre {} et {}", dateDebut, dateFin);
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep WHERE ep.dateAffectation BETWEEN :debut AND :fin ORDER BY ep.dateAffectation DESC", EmployeProjet.class);
            query.setParameter("debut", dateDebut);
            query.setParameter("fin", dateFin);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par période: {} - {}", dateDebut, dateFin, e);
            throw e;
        }
    }

    @Override
    public EmployeProjet findByEmployeAndProjet(Long employeId, Long projetId) {
        logger.debug("Recherche de l'affectation employé {} projet {}", employeId, projetId);
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep WHERE ep.employe.id = :employeId AND ep.projet.id = :projetId", EmployeProjet.class);
            query.setParameter("employeId", employeId);
            query.setParameter("projetId", projetId);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche employé {} projet {}", employeId, projetId, e);
            throw e;
        }
    }

    @Override
    public List<EmployeProjet> findProjetsEmployeActifs(Long employeId) {
        logger.debug("Recherche des projets actifs pour l'employé: {}", employeId);
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep WHERE ep.employe.id = :employeId AND ep.statut = :statut ORDER BY ep.dateAffectation DESC", EmployeProjet.class);
            query.setParameter("employeId", employeId);
            query.setParameter("statut", StatutEmployeProjet.ACTIF);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche des projets actifs pour l'employé: {}", employeId, e);
            throw e;
        }
    }

    @Override
    public List<EmployeProjet> findEmployesProjetsActifs(Long projetId) {
        logger.debug("Recherche des employés actifs pour le projet: {}", projetId);
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep WHERE ep.projet.id = :projetId AND ep.statut = :statut ORDER BY ep.employe.nom, ep.employe.prenom", EmployeProjet.class);
            query.setParameter("projetId", projetId);
            query.setParameter("statut", StatutEmployeProjet.ACTIF);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche des employés actifs pour le projet: {}", projetId, e);
            throw e;
        }
    }

    @Override
    public int countEmployesParProjet(Long projetId) {
        logger.debug("Comptage des employés pour le projet: {}", projetId);
        try {
            Query<Long> query = getCurrentSession()
                .createQuery("SELECT COUNT(ep) FROM EmployeProjet ep WHERE ep.projet.id = :projetId AND ep.statut = :statut", Long.class);
            query.setParameter("projetId", projetId);
            query.setParameter("statut", StatutEmployeProjet.ACTIF);
            Long count = query.uniqueResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des employés pour le projet: {}", projetId, e);
            throw e;
        }
    }

    @Override
    public List<EmployeProjet> findAll() {
        logger.debug("Récupération de toutes les affectations");
        try {
            Query<EmployeProjet> query = getCurrentSession()
                .createQuery("FROM EmployeProjet ep ORDER BY ep.dateAffectation DESC", EmployeProjet.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de toutes les affectations", e);
            throw e;
        }
    }
}
