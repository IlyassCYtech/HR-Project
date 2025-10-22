package com.gestionrh.dao.impl;

import com.gestionrh.dao.ProjetDAO;
import com.gestionrh.model.Projet;
import com.gestionrh.model.StatutProjet;
import com.gestionrh.util.TransactionUtil;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class ProjetDAOImpl extends GenericDAOImpl<Projet, Long> implements ProjetDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjetDAOImpl.class);

    public ProjetDAOImpl() {
        super();
    }

    @Override
    public Projet findById(Long id) {
        logger.debug("Recherche du projet par ID: {}", id);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Projet> query = session
                .createQuery("SELECT DISTINCT p FROM Projet p " +
                           "LEFT JOIN FETCH p.departement " +
                           "LEFT JOIN FETCH p.chefProjet " +
                           "LEFT JOIN FETCH p.employes ep " +
                           "LEFT JOIN FETCH ep.employe e " +
                           "LEFT JOIN FETCH e.departement " +
                           "WHERE p.id = :id", Projet.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        });
    }

    @Override
    public List<Projet> findByStatut(StatutProjet statut) {
        logger.debug("Recherche des projets par statut: {}", statut);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Projet> query = session
                .createQuery("SELECT DISTINCT p FROM Projet p " +
                           "LEFT JOIN FETCH p.departement " +
                           "LEFT JOIN FETCH p.chefProjet " +
                           "WHERE p.statut = :statut " +
                           "ORDER BY p.dateDebut DESC", Projet.class);
            query.setParameter("statut", statut);
            List<Projet> projets = query.getResultList();
            
            if (!projets.isEmpty()) {
                Query<Projet> employesQuery = session
                    .createQuery("SELECT DISTINCT p FROM Projet p " +
                               "LEFT JOIN FETCH p.employes ep " +
                               "LEFT JOIN FETCH ep.employe e " +
                               "LEFT JOIN FETCH e.departement " +
                               "WHERE p IN :projets", Projet.class);
                employesQuery.setParameter("projets", projets);
                employesQuery.getResultList();
            }
            
            return projets;
        });
    }

    @Override
    public List<Projet> findByDepartementId(Long departementId) {
        logger.debug("Recherche des projets par département: {}", departementId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Projet> query = session
                .createQuery("SELECT DISTINCT p FROM Projet p " +
                           "LEFT JOIN FETCH p.departement " +
                           "LEFT JOIN FETCH p.chefProjet " +
                           "WHERE p.departement.id = :departementId " +
                           "ORDER BY p.dateDebut DESC", Projet.class);
            query.setParameter("departementId", departementId);
            List<Projet> projets = query.getResultList();
            
            if (!projets.isEmpty()) {
                Query<Projet> employesQuery = session
                    .createQuery("SELECT DISTINCT p FROM Projet p " +
                               "LEFT JOIN FETCH p.employes ep " +
                               "LEFT JOIN FETCH ep.employe e " +
                               "LEFT JOIN FETCH e.departement " +
                               "WHERE p IN :projets", Projet.class);
                employesQuery.setParameter("projets", projets);
                employesQuery.getResultList();
            }
            
            return projets;
        });
    }

    @Override
    public List<Projet> findByChefProjetId(Long chefProjetId) {
        logger.debug("Recherche des projets par chef de projet: {}", chefProjetId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Projet> query = session
                .createQuery("SELECT DISTINCT p FROM Projet p " +
                           "LEFT JOIN FETCH p.departement " +
                           "LEFT JOIN FETCH p.chefProjet " +
                           "WHERE p.chefProjet.id = :chefProjetId " +
                           "ORDER BY p.dateDebut DESC", Projet.class);
            query.setParameter("chefProjetId", chefProjetId);
            List<Projet> projets = query.getResultList();
            
            if (!projets.isEmpty()) {
                Query<Projet> employesQuery = session
                    .createQuery("SELECT DISTINCT p FROM Projet p " +
                               "LEFT JOIN FETCH p.employes ep " +
                               "LEFT JOIN FETCH ep.employe e " +
                               "LEFT JOIN FETCH e.departement " +
                               "WHERE p IN :projets", Projet.class);
                employesQuery.setParameter("projets", projets);
                employesQuery.getResultList();
            }
            
            return projets;
        });
    }

    @Override
    public List<Projet> findByDateDebut(LocalDate dateDebut) {
        logger.debug("Recherche des projets par date de début: {}", dateDebut);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Projet> query = session
                .createQuery("SELECT DISTINCT p FROM Projet p " +
                           "LEFT JOIN FETCH p.departement " +
                           "LEFT JOIN FETCH p.chefProjet " +
                           "WHERE p.dateDebut = :dateDebut " +
                           "ORDER BY p.dateDebut DESC", Projet.class);
            query.setParameter("dateDebut", dateDebut);
            List<Projet> projets = query.getResultList();
            
            if (!projets.isEmpty()) {
                Query<Projet> employesQuery = session
                    .createQuery("SELECT DISTINCT p FROM Projet p " +
                               "LEFT JOIN FETCH p.employes ep " +
                               "LEFT JOIN FETCH ep.employe e " +
                               "LEFT JOIN FETCH e.departement " +
                               "WHERE p IN :projets", Projet.class);
                employesQuery.setParameter("projets", projets);
                employesQuery.getResultList();
            }
            
            return projets;
        });
    }

    @Override
    public List<Projet> findByDateFin(LocalDate dateFin) {
        logger.debug("Recherche des projets par date de fin: {}", dateFin);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Projet> query = session
                .createQuery("SELECT DISTINCT p FROM Projet p " +
                           "LEFT JOIN FETCH p.departement " +
                           "LEFT JOIN FETCH p.chefProjet " +
                           "WHERE p.dateFin = :dateFin " +
                           "ORDER BY p.dateDebut DESC", Projet.class);
            query.setParameter("dateFin", dateFin);
            List<Projet> projets = query.getResultList();
            
            if (!projets.isEmpty()) {
                Query<Projet> employesQuery = session
                    .createQuery("SELECT DISTINCT p FROM Projet p " +
                               "LEFT JOIN FETCH p.employes ep " +
                               "LEFT JOIN FETCH ep.employe e " +
                               "LEFT JOIN FETCH e.departement " +
                               "WHERE p IN :projets", Projet.class);
                employesQuery.setParameter("projets", projets);
                employesQuery.getResultList();
            }
            
            return projets;
        });
    }

    @Override
    public List<Projet> findProjetsActifs() {
        logger.debug("Recherche des projets actifs");
        return TransactionUtil.executeInTransaction(session -> {
            Query<Projet> query = session
                .createQuery("SELECT DISTINCT p FROM Projet p " +
                           "LEFT JOIN FETCH p.departement " +
                           "LEFT JOIN FETCH p.chefProjet " +
                           "WHERE p.statut = :statut " +
                           "ORDER BY p.dateDebut DESC", Projet.class);
            query.setParameter("statut", StatutProjet.EN_COURS);
            List<Projet> projets = query.getResultList();
            
            if (!projets.isEmpty()) {
                Query<Projet> employesQuery = session
                    .createQuery("SELECT DISTINCT p FROM Projet p " +
                               "LEFT JOIN FETCH p.employes ep " +
                               "LEFT JOIN FETCH ep.employe e " +
                               "LEFT JOIN FETCH e.departement " +
                               "WHERE p IN :projets", Projet.class);
                employesQuery.setParameter("projets", projets);
                employesQuery.getResultList();
            }
            
            return projets;
        });
    }

    @Override
    public List<Projet> findProjetsEnRetard() {
        logger.debug("Recherche des projets en retard");
        return TransactionUtil.executeInTransaction(session -> {
            LocalDate aujourdHui = LocalDate.now();
            Query<Projet> query = session
                .createQuery("SELECT DISTINCT p FROM Projet p " +
                           "LEFT JOIN FETCH p.departement " +
                           "LEFT JOIN FETCH p.chefProjet " +
                           "WHERE p.dateFin < :aujourdHui " +
                           "AND p.statut = :statut " +
                           "ORDER BY p.dateFin ASC", Projet.class);
            query.setParameter("aujourdHui", aujourdHui);
            query.setParameter("statut", StatutProjet.EN_COURS);
            List<Projet> projets = query.getResultList();
            
            if (!projets.isEmpty()) {
                Query<Projet> employesQuery = session
                    .createQuery("SELECT DISTINCT p FROM Projet p " +
                               "LEFT JOIN FETCH p.employes ep " +
                               "LEFT JOIN FETCH ep.employe e " +
                               "LEFT JOIN FETCH e.departement " +
                               "WHERE p IN :projets", Projet.class);
                employesQuery.setParameter("projets", projets);
                employesQuery.getResultList();
            }
            
            return projets;
        });
    }

    @Override
    public List<Projet> findByNom(String nom) {
        logger.debug("Recherche des projets par nom: {}", nom);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Projet> query = session
                .createQuery("SELECT DISTINCT p FROM Projet p " +
                           "LEFT JOIN FETCH p.departement " +
                           "LEFT JOIN FETCH p.chefProjet " +
                           "WHERE LOWER(p.nom) LIKE LOWER(:nom) " +
                           "ORDER BY p.dateDebut DESC", Projet.class);
            query.setParameter("nom", "%" + nom + "%");
            List<Projet> projets = query.getResultList();
            
            if (!projets.isEmpty()) {
                Query<Projet> employesQuery = session
                    .createQuery("SELECT DISTINCT p FROM Projet p " +
                               "LEFT JOIN FETCH p.employes ep " +
                               "LEFT JOIN FETCH ep.employe e " +
                               "LEFT JOIN FETCH e.departement " +
                               "WHERE p IN :projets", Projet.class);
                employesQuery.setParameter("projets", projets);
                employesQuery.getResultList();
            }
            
            return projets;
        });
    }

    @Override
    public long countByStatut(StatutProjet statut) {
        logger.debug("Comptage des projets par statut: {}", statut);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Long> query = session
                .createQuery("SELECT COUNT(p) FROM Projet p WHERE p.statut = :statut", Long.class);
            query.setParameter("statut", statut);
            return query.uniqueResult();
        });
    }

    @Override
    public List<Projet> findAll() {
        logger.debug("Récupération de tous les projets");
        return TransactionUtil.executeInTransaction(session -> {
            Query<Projet> query = session
                .createQuery("SELECT DISTINCT p FROM Projet p " +
                           "LEFT JOIN FETCH p.departement " +
                           "LEFT JOIN FETCH p.chefProjet " +
                           "ORDER BY p.dateDebut DESC", Projet.class);
            List<Projet> projets = query.getResultList();
            
            if (!projets.isEmpty()) {
                Query<Projet> employesQuery = session
                    .createQuery("SELECT DISTINCT p FROM Projet p " +
                               "LEFT JOIN FETCH p.employes ep " +
                               "LEFT JOIN FETCH ep.employe e " +
                               "LEFT JOIN FETCH e.departement " +
                               "WHERE p IN :projets", Projet.class);
                employesQuery.setParameter("projets", projets);
                employesQuery.getResultList();
            }
            
            return projets;
        });
    }
}
