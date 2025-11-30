package com.gestionrh.dao.impl;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gestionrh.util.TransactionUtil;

import com.gestionrh.dao.NotificationDAO;
import com.gestionrh.model.Notification;
import com.gestionrh.model.NotificationUser;
import com.gestionrh.model.Utilisateur;
import java.util.List;

public class NotificationDAOImpl implements NotificationDAO{
	
	private static final Logger logger = LoggerFactory.getLogger(NotificationDAOImpl.class);
	
	@Override
	public void creerNotificationPourDepartement(int departementId, String message, String type) {

	    TransactionUtil.executeInTransaction((Session session) -> {
	        logger.info("Création d'une notification pour le département {} (avec admins)", departementId);

	        // 1) Créer la notification
	        Notification notification = new Notification();
	        notification.setMessage(message);
	        notification.setType(type != null ? type : "INFO");
	        session.persist(notification);

	        // 2) Utilisateurs du département
	        String hqlDep = "SELECT u FROM Utilisateur u " +
	                        "JOIN u.employe e " +
	                        "JOIN e.departement d " +
	                        "WHERE d.id = :depId " +
	                        "AND e.statut = 'ACTIF' " +
	                        "AND u.statut = 'ACTIF'";

	        Query<Utilisateur> queryDep = session.createQuery(hqlDep, Utilisateur.class);
	        queryDep.setParameter("depId", departementId);

	        List<Utilisateur> utilisateursDept = queryDep.getResultList();
	        logger.info("Utilisateurs du département {} : {}", departementId, utilisateursDept.size());

	        // 3) Utilisateurs ADMIN (actifs)
	        String hqlAdmin = "SELECT u FROM Utilisateur u " +
	                          "WHERE u.role = 'ADMIN' " +
	                          "AND u.statut = 'ACTIF'";

	        Query<Utilisateur> queryAdmin = session.createQuery(hqlAdmin, Utilisateur.class);
	        List<Utilisateur> admins = queryAdmin.getResultList();
	        logger.info("Admins actifs : {}", admins.size());

	        // 4) Fusion des listes sans doublons (par id)
	        List<Utilisateur> tousDestinataires = new java.util.ArrayList<>();
	        java.util.Set<Integer> idsDejaAjoutes = new java.util.HashSet<>();

	        for (Utilisateur u : utilisateursDept) {
	            if (u.getId() != null && idsDejaAjoutes.add(u.getId())) {
	                tousDestinataires.add(u);
	            }
	        }

	        for (Utilisateur u : admins) {
	            if (u.getId() != null && idsDejaAjoutes.add(u.getId())) {
	                tousDestinataires.add(u);
	            }
	        }

	        logger.info("Nombre total de destinataires (dept + admins) : {}", tousDestinataires.size());

	        // 5) Créer les NotificationUser
	        for (Utilisateur u : tousDestinataires) {
	            NotificationUser nu = new NotificationUser();
	            nu.setNotification(notification);
	            nu.setUtilisateur(u);
	            session.persist(nu);
	        }

	        return null;
	    });
	}

	@Override
	public void creerNotificationPourProjet(int projetId, String message, String type) {

	    TransactionUtil.executeInTransaction((Session session) -> {
	        logger.info("Création d'une notification pour le projet {} (avec admins)", projetId);

	        // 1) Créer la notification
	        Notification notification = new Notification();
	        notification.setMessage(message);
	        notification.setType(type != null ? type : "INFO");
	        session.persist(notification);

	        // 2) Utilisateurs du projet
	        String hqlProj = "SELECT DISTINCT u FROM Utilisateur u " +
	                         "JOIN u.employe e " +
	                         "JOIN e.projets ep " +  
	                         "JOIN ep.projet p " +
	                         "WHERE p.id = :projId " +
	                         "AND ep.statut = 'ACTIF' " +
	                         "AND e.statut = 'ACTIF' " +
	                         "AND u.statut = 'ACTIF'";

	        Query<Utilisateur> queryProj = session.createQuery(hqlProj, Utilisateur.class);
	        queryProj.setParameter("projId", projetId);

	        List<Utilisateur> utilisateursProjet = queryProj.getResultList();
	        logger.info("Utilisateurs du projet {} : {}", projetId, utilisateursProjet.size());

	        // 3) Utilisateurs ADMIN (actifs)
	        String hqlAdmin = "SELECT u FROM Utilisateur u " +
	                          "WHERE u.role = 'ADMIN' " +
	                          "AND u.statut = 'ACTIF'";

	        Query<Utilisateur> queryAdmin = session.createQuery(hqlAdmin, Utilisateur.class);
	        List<Utilisateur> admins = queryAdmin.getResultList();
	        logger.info("Admins actifs : {}", admins.size());

	        // 4) Fusion des listes sans doublons (par id)
	        List<Utilisateur> tousDestinataires = new java.util.ArrayList<>();
	        java.util.Set<Integer> idsDejaAjoutes = new java.util.HashSet<>();

	        for (Utilisateur u : utilisateursProjet) {
	            if (u.getId() != null && idsDejaAjoutes.add(u.getId())) {
	                tousDestinataires.add(u);
	            }
	        }

	        for (Utilisateur u : admins) {
	            if (u.getId() != null && idsDejaAjoutes.add(u.getId())) {
	                tousDestinataires.add(u);
	            }
	        }

	        logger.info("Nombre total de destinataires (projet + admins) : {}", tousDestinataires.size());

	        // 5) Créer les NotificationUser
	        for (Utilisateur u : tousDestinataires) {
	            NotificationUser nu = new NotificationUser();
	            nu.setNotification(notification);
	            nu.setUtilisateur(u);
	            session.persist(nu);
	        }

	        return null;
	    });
	}

	@Override
	public List<NotificationUser> getNotificationsUtilisateur(int utilisateurId, boolean seulementNonLues) {
		return TransactionUtil.executeInTransaction((Session session)->{
            StringBuilder hql = new StringBuilder(
                    "FROM NotificationUser nu " +
                    "JOIN FETCH nu.notification n " +
                    "WHERE nu.utilisateur.id = :userId "
                );

                if (seulementNonLues) {
                    hql.append("AND nu.estLu = false ");
                }

                hql.append("ORDER BY nu.dateCreation DESC");

                Query<NotificationUser> query = session.createQuery(hql.toString(), NotificationUser.class);
                query.setParameter("userId", utilisateurId);

                return query.getResultList();
		});
	}

	@Override
	public void marquerCommeLue(int notificationUserId) {
        TransactionUtil.executeInTransaction((Session session) -> {
            NotificationUser nu = session.get(NotificationUser.class, notificationUserId);
            if (nu != null && !nu.isEstLu()) {
                nu.marquerLu();
                session.merge(nu);
                logger.info("NotificationUser {} marquée comme lue", notificationUserId);
            } else {
                logger.info("NotificationUser {} déjà lue ou inexistante", notificationUserId);
            }
            return null;
        });
	}

			
}
