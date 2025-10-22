package com.gestionrh.dao.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.GenericDAO;
import com.gestionrh.util.HibernateUtil;
import com.gestionrh.util.TransactionUtil;

/**
 * Implémentation générique des opérations CRUD avec Hibernate
 * @param <T> Type de l'entité
 * @param <ID> Type de l'identifiant
 */
public abstract class GenericDAOImpl<T, ID extends Serializable> implements GenericDAO<T, ID> {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericDAOImpl.class);
    
    private final Class<T> entityClass;
    private final SessionFactory sessionFactory;
    
    @SuppressWarnings("unchecked")
    public GenericDAOImpl() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }
    
    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
    
    protected Session openSession() {
        return sessionFactory.openSession();
    }
    
    @Override
    public T save(T entity) {
        return TransactionUtil.executeInTransaction(session -> {
            session.persist(entity); // Nouvelle API Hibernate 6
            logger.debug("Entité {} sauvegardée avec succès", entityClass.getSimpleName());
            return entity;
        });
    }
    
    @Override
    public T update(T entity) {
        return TransactionUtil.executeInTransaction(session -> {
            T updatedEntity = session.merge(entity);
            logger.debug("Entité {} mise à jour avec succès", entityClass.getSimpleName());
            return updatedEntity;
        });
    }
    
    @Override
    public T saveOrUpdate(T entity) {
        return TransactionUtil.executeInTransaction(session -> {
            T result = session.merge(entity); // merge remplace saveOrUpdate
            logger.debug("Entité {} sauvegardée/mise à jour avec succès", entityClass.getSimpleName());
            return result;
        });
    }
    
    @Override
    public void delete(T entity) {
        TransactionUtil.executeInTransaction(session -> {
            session.remove(entity); // remove remplace delete
            logger.debug("Entité {} supprimée avec succès", entityClass.getSimpleName());
        });
    }
    
    @Override
    public void deleteById(ID id) {
        TransactionUtil.executeInTransaction(session -> {
            T entity = session.get(entityClass, id);
            if (entity != null) {
                session.remove(entity); // remove remplace delete
            }
            logger.debug("Entité {} avec ID {} supprimée avec succès", entityClass.getSimpleName(), id);
        });
    }
    
    @Override
    public T findById(ID id) {
        return TransactionUtil.executeInTransaction(session -> {
            T entity = session.get(entityClass, id);
            logger.debug("Entité {} avec ID {} trouvée: {}", entityClass.getSimpleName(), id, entity != null);
            return entity;
        });
    }
    
    @Override
    public List<T> findAll() {
        return TransactionUtil.executeInTransaction(session -> {
            Query<T> query = session.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
            List<T> results = query.getResultList();
            logger.debug("Trouvé {} entités de type {}", results.size(), entityClass.getSimpleName());
            return results;
        });
    }
    
    @Override
    public long count() {
        return TransactionUtil.executeInTransaction(session -> {
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM " + entityClass.getSimpleName(), Long.class);
            Long count = query.getSingleResult();
            logger.debug("Nombre d'entités {} : {}", entityClass.getSimpleName(), count);
            return count != null ? count : 0L;
        });
    }
    
    @Override
    public boolean exists(ID id) {
        return findById(id) != null;
    }
    
    @Override
    public List<T> findWithPagination(int firstResult, int maxResults) {
        return TransactionUtil.executeInTransaction(session -> {
            Query<T> query = session.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);
            List<T> results = query.getResultList();
            logger.debug("Trouvé {} entités de type {} avec pagination", results.size(), entityClass.getSimpleName());
            return results;
        });
    }
    
    /**
     * Exécute une requête HQL personnalisée
     * @param hql La requête HQL
     * @param parameters Les paramètres de la requête
     * @return Liste des résultats
     */
    protected List<T> executeQuery(String hql, Object... parameters) {
        return TransactionUtil.executeInTransaction(session -> {
            Query<T> query = session.createQuery(hql, entityClass);
            
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i, parameters[i]);
            }
            
            return query.getResultList();
        });
    }
    
    /**
     * Exécute une requête HQL qui retourne un résultat unique
     * @param hql La requête HQL
     * @param parameters Les paramètres de la requête
     * @return Le résultat unique ou null
     */
    protected T executeUniqueQuery(String hql, Object... parameters) {
        return TransactionUtil.executeInTransaction(session -> {
            Query<T> query = session.createQuery(hql, entityClass);
            
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i, parameters[i]);
            }
            
            return query.getSingleResultOrNull();
        });
    }
}
