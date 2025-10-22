package com.gestionrh.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Servlet de déconnexion
 * Gère la fermeture de session et la redirection vers la page de connexion
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Récupérer le nom d'utilisateur avant de détruire la session (pour le log)
        HttpSession session = request.getSession(false);
        String username = null;
        
        if (session != null) {
            Object utilisateur = session.getAttribute("utilisateur");
            if (utilisateur != null) {
                username = utilisateur.toString();
            }
            
            // Invalider la session
            session.invalidate();
            logger.info("Utilisateur déconnecté : {}", username != null ? username : "inconnu");
        }
        
        // Rediriger vers la page de connexion avec un message
        response.sendRedirect(request.getContextPath() + "/?logout=success");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Permettre aussi la déconnexion via POST (pour les formulaires)
        doGet(request, response);
    }
}
