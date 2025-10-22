package com.gestionrh.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import com.gestionrh.util.HibernateUtil;

/**
 * Listener pour l'initialisation et la fermeture d'Hibernate
 * au démarrage et à l'arrêt de l'application web
 */
// @WebListener
public class HibernateContextListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initialisation du contexte de l'application - Démarrage d'Hibernate");
        try {
            // Initialisation d'Hibernate au démarrage de l'application
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate initialisé avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation d'Hibernate: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible d'initialiser Hibernate", e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Arrêt du contexte de l'application - Fermeture d'Hibernate");
        try {
            // Fermeture propre d'Hibernate à l'arrêt de l'application
            HibernateUtil.shutdown();
            System.out.println("Hibernate fermé avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture d'Hibernate: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
