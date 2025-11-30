package com.gestionrh.util;

import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilitaire pour gérer les transactions Hibernate de manière consistante
 */
public class TransactionUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionUtil.class);
    
    /**
     * Exécute une opération dans une transaction avec retour de valeur
     */
    public static <T> T executeInTransaction(Function<Session, T> operation) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            
            T result = operation.apply(session);
            
            transaction.commit();
            return result;
            
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Erreur lors du rollback", rollbackEx);
                }
            }
            logger.error("Erreur lors de l'exécution de la transaction", e);
            throw new RuntimeException("Erreur lors de l'opération en base", e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception closeEx) {
                    logger.error("Erreur lors de la fermeture de la session", closeEx);
                }
            }
        }
    }
    
    /**
     * Exécute une opération dans une transaction sans retour de valeur
     */
    public static void executeInTransaction(Consumer<Session> operation) {
        executeInTransaction(session -> {
            operation.accept(session);
            return null;
        });
    }
}