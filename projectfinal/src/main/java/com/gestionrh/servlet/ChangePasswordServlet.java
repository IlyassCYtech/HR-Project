package com.gestionrh.servlet;

import java.io.IOException;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.UtilisateurDAO;
import com.gestionrh.dao.impl.UtilisateurDAOImpl;
import com.gestionrh.model.Utilisateur;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet pour changer le mot de passe de l'utilisateur connecté
 */
@WebServlet("/app/change-password")
public class ChangePasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ChangePasswordServlet.class);
    
    private UtilisateurDAO utilisateurDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        utilisateurDAO = new UtilisateurDAOImpl();
        logger.info("ChangePasswordServlet initialisé");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        logger.info("=== CHANGEMENT DE MOT DE PASSE ===");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        logger.info("Tentative de changement de mot de passe pour: {}", utilisateur.getUsername());
        
        // Récupérer les paramètres
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // Validation des paramètres
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            session.setAttribute("error", "Le mot de passe actuel est obligatoire");
            response.sendRedirect(request.getContextPath() + "/app/profil");
            return;
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            session.setAttribute("error", "Le nouveau mot de passe est obligatoire");
            response.sendRedirect(request.getContextPath() + "/app/profil");
            return;
        }
        
        if (newPassword.length() < 6) {
            session.setAttribute("error", "Le mot de passe doit contenir au moins 6 caractères");
            response.sendRedirect(request.getContextPath() + "/app/profil");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("error", "Les mots de passe ne correspondent pas");
            response.sendRedirect(request.getContextPath() + "/app/profil");
            return;
        }
        
        try {
            // Récupérer l'utilisateur depuis la base
            Utilisateur dbUser = utilisateurDAO.findById(Long.valueOf(utilisateur.getId()));
            
            if (dbUser == null) {
                logger.error("Utilisateur non trouvé: ID {}", utilisateur.getId());
                session.setAttribute("error", "Utilisateur non trouvé");
                response.sendRedirect(request.getContextPath() + "/app/profil");
                return;
            }
            
            String storedHash = dbUser.getPasswordHash();
            boolean passwordOk = false;
            
            // Vérifier l'ancien mot de passe
            if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
                // Hash BCrypt
                try {
                    passwordOk = BCrypt.checkpw(currentPassword, storedHash);
                } catch (Exception e) {
                    logger.error("Erreur BCrypt: {}", e.getMessage());
                    passwordOk = false;
                }
            } else {
                // Plain text (legacy)
                passwordOk = storedHash.equals(currentPassword);
            }
            
            if (!passwordOk) {
                logger.warn("Mot de passe actuel incorrect pour: {}", utilisateur.getUsername());
                session.setAttribute("error", "Le mot de passe actuel est incorrect");
                response.sendRedirect(request.getContextPath() + "/app/profil");
                return;
            }
            
            // Hasher le nouveau mot de passe
            String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
            
            // Mettre à jour le mot de passe
            dbUser.setPasswordHash(newHash);
            utilisateurDAO.update(dbUser);
            
            // Mettre à jour l'utilisateur en session
            session.setAttribute("utilisateur", dbUser);
            
            logger.info("✅ Mot de passe modifié avec succès pour: {}", utilisateur.getUsername());
            session.setAttribute("success", "Mot de passe modifié avec succès");
            response.sendRedirect(request.getContextPath() + "/app/profil");
            
        } catch (Exception e) {
            logger.error("Erreur lors du changement de mot de passe", e);
            session.setAttribute("error", "Erreur lors du changement de mot de passe: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/app/profil");
        }
    }
}
