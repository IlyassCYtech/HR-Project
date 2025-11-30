package com.gestionrh.dao.impl;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.model.Employe;
import com.gestionrh.model.Grade;
import com.gestionrh.model.StatutEmploye;
import com.gestionrh.util.TransactionUtil;

public class EmployeDAOImpl extends GenericDAOImpl<Employe, Long> implements EmployeDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeDAOImpl.class);

    public EmployeDAOImpl() {
        super();
    }

    @Override
    public Employe findById(Long id) {
        logger.debug("Recherche de l'employé par ID: {}", id);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT e FROM Employe e " +
                           "LEFT JOIN FETCH e.departement " +
                           "LEFT JOIN FETCH e.manager " +
                           "WHERE e.id = :id", Employe.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        });
    }

    @Override
    public List<Employe> findByNom(String nom) {
        logger.debug("Recherche des employés par nom: {}", nom);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE LOWER(e.nom) LIKE LOWER(:nom) ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("nom", "%" + nom + "%");
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findByPrenom(String prenom) {
        logger.debug("Recherche des employés par prénom: {}", prenom);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE LOWER(e.prenom) LIKE LOWER(:prenom) ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("prenom", "%" + prenom + "%");
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findByNomOrPrenom(String nomPrenom) {
        logger.debug("Recherche des employés par nom ou prénom: {}", nomPrenom);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE LOWER(e.nom) LIKE LOWER(:search) OR LOWER(e.prenom) LIKE LOWER(:search) ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("search", "%" + nomPrenom + "%");
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findByDepartementId(Long departementId) {
        logger.debug("Recherche des employés par département: {}", departementId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.departement.id = :deptId ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("deptId", departementId);
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findWithoutDepartement() {
        logger.debug("Recherche des employés sans département");
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT e FROM Employe e WHERE e.departement IS NULL AND e.statut = 'ACTIF' ORDER BY e.nom, e.prenom", Employe.class);
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findByManagerId(Long managerId) {
        logger.debug("Recherche des employés par manager: {}", managerId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.manager.id = :managerId ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("managerId", managerId);
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findByStatut(StatutEmploye statut) {
        logger.debug("Recherche des employés par statut: {}", statut);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.statut = :statut ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findByGrade(Grade grade) {
        logger.debug("Recherche des employés par grade: {}", grade);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.grade = :grade ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("grade", grade);
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findByGrades(Grade... grades) {
        logger.debug("Recherche des employés par grades multiples: {}", (Object[]) grades);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.grade IN (:grades) AND e.statut = 'ACTIF' ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameterList("grades", grades);
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findByPoste(String poste) {
        logger.debug("Recherche des employés par poste: {}", poste);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE LOWER(e.poste) LIKE LOWER(:poste) ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("poste", "%" + poste + "%");
            return query.getResultList();
        });
    }

    @Override
    public Employe findByMatricule(String matricule) {
        logger.debug("Recherche de l'employé par matricule: {}", matricule);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.matricule = :matricule", Employe.class);
            query.setParameter("matricule", matricule);
            return query.uniqueResult();
        });
    }

    @Override
    public Employe findByEmail(String email) {
        logger.debug("Recherche de l'employé par email: {}", email);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.email = :email", Employe.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        });
    }

    @Override
    public List<Employe> findActifs() {
        logger.debug("Recherche des employés actifs");
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.statut = :statut ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("statut", StatutEmploye.ACTIF);
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findPotentialManagers() {
        logger.debug("Recherche des managers potentiels");
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.statut = :statut AND e.grade IN (:grades) ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("statut", StatutEmploye.ACTIF);
            query.setParameterList("grades", List.of(Grade.SENIOR, Grade.EXPERT, Grade.MANAGER, Grade.DIRECTEUR));
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findByDateEmbauche(LocalDate dateEmbauche) {
        logger.debug("Recherche des employés par date d'embauche: {}", dateEmbauche);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.dateEmbauche = :dateEmbauche ORDER BY e.nom, e.prenom", Employe.class);
            query.setParameter("dateEmbauche", dateEmbauche);
            return query.getResultList();
        });
    }

    @Override
    public List<Employe> findByPeriodeEmbauche(LocalDate dateDebut, LocalDate dateFin) {
        logger.debug("Recherche des employés embauchés entre {} et {}", dateDebut, dateFin);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement WHERE e.dateEmbauche BETWEEN :debut AND :fin ORDER BY e.dateEmbauche DESC", Employe.class);
            query.setParameter("debut", dateDebut);
            query.setParameter("fin", dateFin);
            return query.getResultList();
        });
    }

    @Override
    public long countByDepartement(Long departementId) {
        logger.debug("Comptage des employés par département: {}", departementId);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Long> query = session
                .createQuery("SELECT COUNT(e) FROM Employe e WHERE e.departement.id = :deptId AND e.statut = :statut", Long.class);
            query.setParameter("deptId", departementId);
            query.setParameter("statut", StatutEmploye.ACTIF);
            Long count = query.uniqueResult();
            return count != null ? count : 0L;
        });
    }

    @Override
    public long countByStatut(StatutEmploye statut) {
        logger.debug("Comptage des employés par statut: {}", statut);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Long> query = session
                .createQuery("SELECT COUNT(e) FROM Employe e WHERE e.statut = :statut", Long.class);
            query.setParameter("statut", statut);
            Long count = query.uniqueResult();
            return count != null ? count : 0L;
        });
    }

    @Override
    public boolean existsByMatricule(String matricule) {
        logger.debug("Vérification de l'existence du matricule: {}", matricule);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Long> query = session
                .createQuery("SELECT COUNT(e) FROM Employe e WHERE e.matricule = :matricule", Long.class);
            query.setParameter("matricule", matricule);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        });
    }

    @Override
    public boolean existsByEmail(String email) {
        logger.debug("Vérification de l'existence de l'email: {}", email);
        return TransactionUtil.executeInTransaction(session -> {
            Query<Long> query = session
                .createQuery("SELECT COUNT(e) FROM Employe e WHERE e.email = :email", Long.class);
            query.setParameter("email", email);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        });
    }

    @Override
    public List<Employe> findAll() {
        logger.debug("Récupération de tous les employés");
        return TransactionUtil.executeInTransaction(session -> {
            Query<Employe> query = session
                .createQuery("SELECT DISTINCT e FROM Employe e LEFT JOIN FETCH e.departement ORDER BY e.nom, e.prenom", Employe.class);
            return query.getResultList();
        });
    }
}
