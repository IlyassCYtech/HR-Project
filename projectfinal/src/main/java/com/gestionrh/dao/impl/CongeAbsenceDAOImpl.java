package com.gestionrh.dao.impl;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.CongeAbsenceDAO;
import com.gestionrh.model.CongeAbsence;
import com.gestionrh.model.CongeAbsence.StatutDemande;
import com.gestionrh.model.TypeConge;
import com.gestionrh.util.TransactionUtil;

public class CongeAbsenceDAOImpl extends GenericDAOImpl<CongeAbsence, Long> implements CongeAbsenceDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(CongeAbsenceDAOImpl.class);

    public CongeAbsenceDAOImpl() {
        super();
    }

    @Override
    public CongeAbsence findById(Long id) {
        logger.debug("Recherche du congé par ID: {}", id);
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement WHERE c.id = :id", CongeAbsence.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        });
    }

    @Override
    public List<CongeAbsence> findByEmployeId(Long employeId) {
        logger.debug("Recherche des congés par employé: {}", employeId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT DISTINCT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement WHERE c.employe.id = :employeId ORDER BY c.dateDebut DESC", CongeAbsence.class);
            query.setParameter("employeId", employeId);
            return query.getResultList();
        });
    }

    @Override
    public List<CongeAbsence> findByStatut(StatutDemande statut) {
        logger.debug("Recherche des congés par statut: {}", statut);
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT DISTINCT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement WHERE c.statut = :statut ORDER BY c.dateDebut DESC", CongeAbsence.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        });
    }

    @Override
    public List<CongeAbsence> findByTypeConge(TypeConge typeConge) {
        logger.debug("Recherche des congés par type: {}", typeConge);
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT DISTINCT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement WHERE c.typeConge = :type ORDER BY c.dateDebut DESC", CongeAbsence.class);
            query.setParameter("type", typeConge);
            return query.getResultList();
        });
    }

    @Override
    public List<CongeAbsence> findByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        logger.debug("Recherche des congés entre {} et {}", dateDebut, dateFin);
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT DISTINCT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement WHERE c.dateDebut <= :dateFin AND c.dateFin >= :dateDebut ORDER BY c.dateDebut", CongeAbsence.class);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);
            return query.getResultList();
        });
    }

    @Override
    public List<CongeAbsence> findEnAttente() {
        logger.debug("Recherche des congés en attente");
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT DISTINCT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement WHERE c.statut = :statut ORDER BY c.dateDebut", CongeAbsence.class);
            query.setParameter("statut", StatutDemande.EN_ATTENTE);
            return query.getResultList();
        });
    }

    @Override
    public List<CongeAbsence> findByApprobateur(Long approbateurId) {
        logger.debug("Recherche des congés par approbateur: {}", approbateurId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT DISTINCT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement WHERE c.approuvePar.id = :approbId ORDER BY c.dateDebut DESC", CongeAbsence.class);
            query.setParameter("approbId", approbateurId);
            return query.getResultList();
        });
    }

    @Override
    public List<CongeAbsence> findCongesEmployeAnnee(Long employeId, int annee) {
        logger.debug("Recherche des congés de l'employé {} pour l'année {}", employeId, annee);
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT DISTINCT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement WHERE c.employe.id = :employeId " +
                        "AND YEAR(c.dateDebut) = :annee ORDER BY c.dateDebut", CongeAbsence.class);
            query.setParameter("employeId", employeId);
            query.setParameter("annee", annee);
            return query.getResultList();
        });
    }

    @Override
    public int calculerJoursCongesUtilises(Long employeId, int annee) {
        logger.debug("Calcul des jours de congés utilisés pour employé {} année {}", employeId, annee);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Integer> query = session
                .createQuery("SELECT COALESCE(SUM(c.nombreJours), 0) FROM CongeAbsence c " +
                        "WHERE c.employe.id = :employeId AND YEAR(c.dateDebut) = :annee " +
                        "AND c.statut = :statut AND c.typeConge = :type", Integer.class);
            query.setParameter("employeId", employeId);
            query.setParameter("annee", annee);
            query.setParameter("statut", StatutDemande.APPROUVE);
            query.setParameter("type", TypeConge.CONGES_PAYES);
            Integer jours = query.uniqueResult();
            return jours != null ? jours : 0;
        });
    }

    @Override
    public List<CongeAbsence> findConflits(LocalDate dateDebut, LocalDate dateFin, Long employeId) {
        logger.debug("Recherche des conflits de congés pour employé {} entre {} et {}", employeId, dateDebut, dateFin);
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT DISTINCT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement WHERE c.employe.id = :employeId " +
                        "AND c.statut IN (:statuts) " +
                        "AND ((c.dateDebut <= :dateFin AND c.dateFin >= :dateDebut))", CongeAbsence.class);
            query.setParameter("employeId", employeId);
            query.setParameterList("statuts", List.of(StatutDemande.APPROUVE, StatutDemande.EN_ATTENTE));
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);
            return query.getResultList();
        });
    }

    @Override
    public List<CongeAbsence> findCongesEquipe(Long chefId) {
        logger.debug("Recherche des congés de l'équipe du chef: {}", chefId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT DISTINCT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement d LEFT JOIN FETCH d.chefDepartement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement WHERE e.departement.chefDepartement.id = :chefId " +
                        "ORDER BY c.dateDebut DESC", CongeAbsence.class);
            query.setParameter("chefId", chefId);
            return query.getResultList();
        });
    }

    @Override
    public List<CongeAbsence> findAll() {
        logger.debug("Récupération de tous les congés");
        return TransactionUtil.executeInTransaction(session -> {
            Query<CongeAbsence> query = session
                .createQuery("SELECT DISTINCT c FROM CongeAbsence c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.departement LEFT JOIN FETCH c.approuvePar a LEFT JOIN FETCH a.departement ORDER BY c.dateDebut DESC", CongeAbsence.class);
            return query.getResultList();
        });
    }

    @Override
    public long countByStatut(StatutDemande statut) {
        logger.debug("Comptage des congés par statut: {}", statut);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Long> query = session
                .createQuery("SELECT COUNT(c) FROM CongeAbsence c WHERE c.statut = :statut", Long.class);
            query.setParameter("statut", statut);
            return query.uniqueResult();
        });
    }
    
    @Override
    public void approuverConge(Long congeId, Long approbateurId, String commentaire) {
        logger.info("Approbation du congé {} par l'employé {}", congeId, approbateurId);
        TransactionUtil.executeInTransaction(session -> {
            // Charger le congé
            CongeAbsence conge = session.get(CongeAbsence.class, congeId);
            if (conge == null) {
                throw new IllegalArgumentException("Congé non trouvé: " + congeId);
            }
            
            // Utiliser session.getReference pour créer un proxy Hibernate
            com.gestionrh.model.Employe approbateur = session.getReference(
                com.gestionrh.model.Employe.class, approbateurId);
            
            // Mettre à jour le congé
            conge.setStatut(StatutDemande.APPROUVE);
            conge.setApprouvePar(approbateur);
            conge.setDateApprobation(java.time.LocalDateTime.now());
            
            if (commentaire != null && !commentaire.trim().isEmpty()) {
                conge.setCommentairesApprobation(commentaire);
            }
            
            session.merge(conge);
            logger.info("Congé {} approuvé avec succès par l'employé {}", congeId, approbateurId);
            return null;
        });
    }
    
    @Override
    public void rejeterConge(Long congeId, Long approbateurId, String commentaire) {
        logger.info("Rejet du congé {} par l'employé {}", congeId, approbateurId);
        TransactionUtil.executeInTransaction(session -> {
            // Charger le congé
            CongeAbsence conge = session.get(CongeAbsence.class, congeId);
            if (conge == null) {
                throw new IllegalArgumentException("Congé non trouvé: " + congeId);
            }
            
            // Utiliser session.getReference pour créer un proxy Hibernate
            com.gestionrh.model.Employe approbateur = session.getReference(
                com.gestionrh.model.Employe.class, approbateurId);
            
            // Mettre à jour le congé
            conge.setStatut(StatutDemande.REFUSE);
            conge.setApprouvePar(approbateur);
            conge.setDateApprobation(java.time.LocalDateTime.now());
            
            if (commentaire != null && !commentaire.trim().isEmpty()) {
                conge.setCommentairesApprobation(commentaire);
            }
            
            session.merge(conge);
            logger.info("Congé {} rejeté avec succès par l'employé {}", congeId, approbateurId);
            return null;
        });
    }
}
