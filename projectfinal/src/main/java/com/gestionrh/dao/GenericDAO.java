package com.gestionrh.dao;

import java.io.Serializable;
import java.util.List;

/**
 * Interface générique pour les opérations CRUD de base
 * @param <T> Type de l'entité
 * @param <ID> Type de l'identifiant
 */
public interface GenericDAO<T, ID extends Serializable> {
    
    /**
     * Sauvegarde une entité
     * @param entity L'entité à sauvegarder
     * @return L'entité sauvegardée
     */
    T save(T entity);
    
    /**
     * Met à jour une entité
     * @param entity L'entité à mettre à jour
     * @return L'entité mise à jour
     */
    T update(T entity);
    
    /**
     * Sauvegarde ou met à jour une entité
     * @param entity L'entité à sauvegarder ou mettre à jour
     * @return L'entité sauvegardée ou mise à jour
     */
    T saveOrUpdate(T entity);
    
    /**
     * Supprime une entité
     * @param entity L'entité à supprimer
     */
    void delete(T entity);
    
    /**
     * Supprime une entité par son ID
     * @param id L'ID de l'entité à supprimer
     */
    void deleteById(ID id);
    
    /**
     * Trouve une entité par son ID
     * @param id L'ID de l'entité
     * @return L'entité trouvée ou null
     */
    T findById(ID id);
    
    /**
     * Trouve toutes les entités
     * @return Liste de toutes les entités
     */
    List<T> findAll();
    
    /**
     * Compte le nombre total d'entités
     * @return Le nombre d'entités
     */
    long count();
    
    /**
     * Vérifie si une entité existe
     * @param id L'ID de l'entité
     * @return true si l'entité existe, false sinon
     */
    boolean exists(ID id);
    
    /**
     * Trouve les entités avec pagination
     * @param firstResult Index du premier résultat
     * @param maxResults Nombre maximum de résultats
     * @return Liste paginée des entités
     */
    List<T> findWithPagination(int firstResult, int maxResults);
}
