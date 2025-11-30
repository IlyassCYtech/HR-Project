package com.gestionrh.dao.impl;

import java.time.LocalDateTime;
import org.mindrot.jbcrypt.BCrypt;
	
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.UtilisateurDAO;
import com.gestionrh.model.Utilisateur;
import com.gestionrh.model.Utilisateur.Role;
import com.gestionrh.model.Utilisateur.StatutUtilisateur;
import com.gestionrh.util.TransactionUtil;

/**
 * Implémentation du DAO pour l'entité Utilisateur
 */
@SuppressWarnings("unused")
public class UtilisateurDAOImpl extends GenericDAOImpl<Utilisateur, Long> implements UtilisateurDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(UtilisateurDAOImpl.class);

    public UtilisateurDAOImpl() {
        super();
    }

    @Override
    public Utilisateur findByUsername(String username) {
        return TransactionUtil.executeInTransaction(session -> {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u LEFT JOIN FETCH u.employe WHERE u.username = :username", Utilisateur.class);
            query.setParameter("username", username);
            return query.uniqueResult();
        });
    }

    @Override
    public Utilisateur findByEmail(String email) {
        return TransactionUtil.executeInTransaction(session -> {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u LEFT JOIN FETCH u.employe WHERE u.email = :email", Utilisateur.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        });
    }

    @Override
    public Utilisateur authenticate(String username, String password) {
        return TransactionUtil.executeInTransaction(session -> {
            logger.info("=== DEBUT AUTHENTIFICATION ===");
            logger.info("Username fourni: {}", username);
            logger.info("Password fourni: {} caractères", password != null ? password.length() : 0);

            // 1) Récupérer l'utilisateur par username (sans filtre de statut d'abord)
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u LEFT JOIN FETCH u.employe " +
                "WHERE u.username = :username",
                Utilisateur.class
            );
            query.setParameter("username", username);

            Utilisateur user = query.uniqueResult();

            if (user == null) {
                logger.error("❌ Utilisateur non trouvé dans la base: {}", username);
                return null;
            }

            logger.info("✅ Utilisateur trouvé - ID: {}, Role: {}, Statut: {}", 
                user.getId(), user.getRole(), user.getStatut());

            // 2) Vérifier le statut
            if (user.getStatut() != StatutUtilisateur.ACTIF) {
                logger.error("❌ Utilisateur inactif - Statut: {}", user.getStatut());
                return null;
            }

            // 3) Récupérer le hash stocké
            String stored = user.getPasswordHash();
            if (stored == null || stored.isEmpty()) {
                logger.error("❌ Aucun mot de passe stocké pour l'utilisateur: {}", username);
                return null;
            }

            logger.info("Hash stocké - Type: {}, Longueur: {}, Préfixe: {}", 
                stored.startsWith("$2") ? "BCrypt" : "Plain text",
                stored.length(),
                stored.length() > 10 ? stored.substring(0, 10) : stored);

            boolean passwordOk = false;

            // 4) Vérification du mot de passe
            if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
                // Mode BCrypt
                logger.info("🔐 Vérification BCrypt...");
                try {
                    passwordOk = BCrypt.checkpw(password, stored);
                    logger.info("Résultat BCrypt.checkpw: {}", passwordOk);
                } catch (Exception e) {
                    logger.error("❌ Erreur BCrypt pour {}: {}", username, e.getMessage(), e);
                    passwordOk = false;
                }
            } else {
                // Mode plain text (legacy)
                logger.info("📝 Vérification plain text...");
                passwordOk = stored.equals(password);
                logger.info("Comparaison '{}' == '{}': {}", stored, password, passwordOk);
            }

            if (passwordOk) {
                logger.info("✅ ✅ ✅ Authentification REUSSIE pour: {}", username);
                return user;
            } else {
                logger.error("❌ ❌ ❌ Mot de passe INCORRECT pour: {}", username);
                return null;
            }
        });
    }

    @Override
    public List<Utilisateur> findByRole(Role role) {
        return TransactionUtil.executeInTransaction(session -> {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u LEFT JOIN FETCH u.employe WHERE u.role = :role ORDER BY u.username", 
                Utilisateur.class);
            query.setParameter("role", role);
            return query.getResultList();
        });
    }

    @Override
    public List<Utilisateur> findByStatut(StatutUtilisateur statut) {
        return TransactionUtil.executeInTransaction(session -> {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u LEFT JOIN FETCH u.employe WHERE u.statut = :statut ORDER BY u.username", 
                Utilisateur.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        });
    }

    @Override
    public List<Utilisateur> findActiveUsers() {
        return findByStatut(StatutUtilisateur.ACTIF);
    }

    @Override
    public List<Utilisateur> findBlockedUsers() {
        return findByStatut(StatutUtilisateur.BLOQUE);
    }

    @Override
    public boolean existsByUsername(String username) {
        return TransactionUtil.executeInTransaction(session -> {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM Utilisateur u WHERE u.username = :username", Long.class);
            query.setParameter("username", username);
            return query.uniqueResult() > 0;
        });
    }

    @Override
    public boolean existsByEmail(String email) {
        return TransactionUtil.executeInTransaction(session -> {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM Utilisateur u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.uniqueResult() > 0;
        });
    }

    @Override
    public void updateLastLogin(Integer userId) {
        TransactionUtil.executeInTransaction(session -> {
            Utilisateur utilisateur = session.get(Utilisateur.class, userId);
            if (utilisateur != null) {
                utilisateur.setDerniereConnexion(LocalDateTime.now());
                session.merge(utilisateur);
                logger.info("Dernière connexion mise à jour pour l'utilisateur ID: {}", userId);
            }
            return null;
        });
    }

    @Override
    public void incrementLoginAttempts(Integer userId) {
        TransactionUtil.executeInTransaction(session -> {
            Utilisateur utilisateur = session.get(Utilisateur.class, userId);
            if (utilisateur != null) {
                int tentatives = utilisateur.getTentativesConnexion() != null ? 
                    utilisateur.getTentativesConnexion() : 0;
                utilisateur.setTentativesConnexion(tentatives + 1);
                
                // Bloquer après 5 tentatives
                if (utilisateur.getTentativesConnexion() >= 5) {
                    utilisateur.setStatut(StatutUtilisateur.BLOQUE);
                    logger.warn("Utilisateur {} bloqué après {} tentatives", 
                        utilisateur.getUsername(), utilisateur.getTentativesConnexion());
                }
                
                session.merge(utilisateur);
            }
            return null;
        });
    }

    @Override
    public void resetLoginAttempts(Integer userId) {
        TransactionUtil.executeInTransaction(session -> {
            Utilisateur utilisateur = session.get(Utilisateur.class, userId);
            if (utilisateur != null) {
                utilisateur.setTentativesConnexion(0);
                session.merge(utilisateur);
                logger.info("Tentatives de connexion réinitialisées pour l'utilisateur ID: {}", userId);
            }
            return null;
        });
    }

    @Override
    public List<Utilisateur> findAll() {
        return TransactionUtil.executeInTransaction(session -> {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u LEFT JOIN FETCH u.employe ORDER BY u.username", 
                Utilisateur.class);
            return query.getResultList();
        });
    }

    @Override
    public Utilisateur findByEmployeId(Long employeId) {
        return TransactionUtil.executeInTransaction(session -> {
            Query<Utilisateur> query = session.createQuery(
                "SELECT u FROM Utilisateur u LEFT JOIN FETCH u.employe e WHERE e.id = :employeId", 
                Utilisateur.class);
            query.setParameter("employeId", employeId);
            List<Utilisateur> results = query.getResultList();
            if (results.isEmpty()) {
                logger.debug("Aucun utilisateur trouvé pour l'employé ID: {}", employeId);
                return null;
            }
            logger.debug("Utilisateur trouvé pour l'employé ID: {}", employeId);
            return results.get(0);
        });
    }
}
