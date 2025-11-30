package com.gestionrh.dao.impl;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.DepartementDAO;
import com.gestionrh.model.Departement;
import com.gestionrh.util.TransactionUtil;

public class DepartementDAOImpl extends GenericDAOImpl<Departement, Long> implements DepartementDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(DepartementDAOImpl.class);

    public DepartementDAOImpl() {
        super();
    }

    @Override
    public Departement findById(Long id) {
        logger.debug("Recherche du département par ID: {}", id);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Departement> query = session
                .createQuery("SELECT d FROM Departement d LEFT JOIN FETCH d.chefDepartement WHERE d.id = :id", Departement.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        });
    }

    @Override
    public List<Departement> findByNom(String nom) {
        logger.debug("Recherche des départements par nom: {}", nom);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Departement> query = session
                .createQuery("SELECT DISTINCT d FROM Departement d LEFT JOIN FETCH d.chefDepartement WHERE LOWER(d.nom) LIKE LOWER(:nom)", Departement.class);
            query.setParameter("nom", "%" + nom + "%");
            return query.getResultList();
        });
    }

    @Override
    public List<Departement> findActifs() {
        logger.debug("Récupération des départements actifs");
        return TransactionUtil.executeInTransaction(session -> {
            Query<Departement> query = session
                .createQuery("SELECT DISTINCT d FROM Departement d LEFT JOIN FETCH d.chefDepartement WHERE d.actif = true ORDER BY d.nom", Departement.class);
            return query.getResultList();
        });
    }

    @Override
    public long countEmployes(Long departementId) {
        logger.debug("Comptage des employés pour le département: {}", departementId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Long> query = session
                .createQuery("SELECT COUNT(e) FROM Employe e WHERE e.departement.id = :deptId AND e.statut = 'ACTIF'", Long.class);
            query.setParameter("deptId", departementId);
            Long count = query.uniqueResult();
            return count != null ? count : 0L;
        });
    }

    @Override
    public Departement findByChefId(Long chefId) {
        logger.debug("Recherche du département par chef ID: {}", chefId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Departement> query = session
                .createQuery("SELECT d FROM Departement d LEFT JOIN FETCH d.chefDepartement WHERE d.chefDepartement.id = :chefId", Departement.class);
            query.setParameter("chefId", chefId);
            return query.uniqueResult();
        });
    }

    @Override
    public List<Departement> findAll() {
        logger.debug("Récupération de tous les départements");
        return TransactionUtil.executeInTransaction(session -> {
            Query<Departement> query = session
                .createQuery("SELECT DISTINCT d FROM Departement d LEFT JOIN FETCH d.chefDepartement ORDER BY d.nom", Departement.class);
            return query.getResultList();
        });
    }

    @Override
    public void delete(Departement departement) {
        logger.info("Suppression PHYSIQUE du département: {}", departement.getNom());
        try {
            // Appel de la méthode parente pour suppression physique
            super.delete(departement);
            logger.info("Département {} supprimé définitivement de la base de données", departement.getNom());
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du département: {}", departement.getNom(), e);
            throw e;
        }
    }

    @Override
    public void deleteById(Long id) {
        logger.info("Suppression PHYSIQUE du département par ID: {}", id);
        try {
            Departement departement = findById(id);
            if (departement != null) {
                delete(departement);
            } else {
                logger.warn("Département non trouvé pour suppression, ID: {}", id);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du département par ID: {}", id, e);
            throw e;
        }
    }
}
