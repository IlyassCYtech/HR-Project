package com.gestionrh.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.model.CongeAbsence;
import com.gestionrh.model.Departement;
import com.gestionrh.model.Employe;
import com.gestionrh.model.EmployeProjet;
import com.gestionrh.model.FichePaie;
import com.gestionrh.model.Notification;
import com.gestionrh.model.NotificationUser;
import com.gestionrh.model.Projet;
import com.gestionrh.model.Utilisateur;

/**
 * Utilitaire pour gérer la SessionFactory Hibernate
 * Singleton pattern pour une seule instance de SessionFactory
 */
public class HibernateUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;
    
    static {
        try {
            createSessionFactory();
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation d'Hibernate", e);
            throw new ExceptionInInitializerError("Erreur lors de l'initialisation d'Hibernate: " + e.getMessage());
        }
    }
    
    private static void createSessionFactory() {
        try {
            logger.info("Initialisation de la SessionFactory Hibernate...");
            
            // Création de la configuration
            Configuration configuration = new Configuration();
            
            // Chargement du fichier de configuration
            configuration.configure("hibernate.cfg.xml");
            
            // Ajout des classes d'entités si pas déjà dans hibernate.cfg.xml
            configuration.addAnnotatedClass(Departement.class);
            configuration.addAnnotatedClass(Employe.class);
            configuration.addAnnotatedClass(Projet.class);
            configuration.addAnnotatedClass(EmployeProjet.class);
            configuration.addAnnotatedClass(FichePaie.class);
            configuration.addAnnotatedClass(CongeAbsence.class);
            configuration.addAnnotatedClass(Utilisateur.class);
            configuration.addAnnotatedClass(Notification.class);
            configuration.addAnnotatedClass(NotificationUser.class);
            
            // Création du service registry
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();
            
            // Création de la session factory
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            
            logger.info("SessionFactory Hibernate initialisée avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la SessionFactory", e);
            if (serviceRegistry != null) {
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
            }
            throw e;
        }
    }
    
    /**
     * Retourne la SessionFactory Hibernate
     * @return SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            logger.warn("SessionFactory fermée, recréation...");
            createSessionFactory();
        }
        return sessionFactory;
    }
    
    /**
     * Ferme la SessionFactory et libère les ressources
     */
    public static void shutdown() {
        logger.info("Fermeture de la SessionFactory Hibernate...");
        try {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
            if (serviceRegistry != null) {
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
            }
            logger.info("SessionFactory fermée avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de la fermeture de la SessionFactory", e);
        }
    }
    
    /**
     * Vérifie si la SessionFactory est ouverte
     * @return true si ouverte, false sinon
     */
    public static boolean isSessionFactoryOpen() {
        return sessionFactory != null && !sessionFactory.isClosed();
    }
    
    /**
     * Recreate la SessionFactory si nécessaire
     */
    public static void recreateSessionFactory() {
        logger.info("Recréation de la SessionFactory...");
        shutdown();
        createSessionFactory();
    }
}
