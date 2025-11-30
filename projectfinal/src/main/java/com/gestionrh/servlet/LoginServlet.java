package com.gestionrh.servlet;

import java.io.IOException;

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
 * Servlet de gestion de l'authentification
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    
    private UtilisateurDAO utilisateurDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        utilisateurDAO = new UtilisateurDAOImpl();
        logger.info("LoginServlet initialisé");
    }
    
    /**
     * Affiche la page de connexion
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Vérifier si l'utilisateur est déjà connecté
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("utilisateur") != null) {
            response.sendRedirect(request.getContextPath() + "/app/dashboard");
            return;
        }
        
        // Afficher la page de connexion
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }
    
    /**
     * Traite la tentative de connexion
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            
            request.setAttribute("errorMessage", "Nom d'utilisateur et mot de passe requis");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }
        
        try {
            // Tentative d'authentification
            Utilisateur utilisateur = utilisateurDAO.authenticate(username.trim(), password);
            
            if (utilisateur != null) {
                // Connexion réussie
                HttpSession session = request.getSession(true);
                session.setAttribute("utilisateur", utilisateur);
                session.setAttribute("userId", utilisateur.getId());
                session.setAttribute("username", utilisateur.getUsername());
                session.setAttribute("role", utilisateur.getRole());
                
                // Stocker l'ID de l'employé si l'utilisateur a un employé associé
                if (utilisateur.getEmploye() != null) {
                    Long employeId = utilisateur.getEmploye().getId();
                    session.setAttribute("employeId", employeId);
                    logger.info("Employé ID: {} mis en session pour l'utilisateur: {}", employeId, username);
                } else {
                    logger.info("Aucun employé associé pour l'utilisateur: {}", username);
                }
                
                // Mettre à jour la dernière connexion
                utilisateurDAO.updateLastLogin(utilisateur.getId());
                
                logger.info("Connexion réussie pour l'utilisateur: {}", username);
                
                // Rediriger vers la page demandée ou le tableau de bord
                String originalURL = (String) session.getAttribute("originalURL");
                if (originalURL != null) {
                    session.removeAttribute("originalURL");
                    response.sendRedirect(originalURL);
                } else {
                    response.sendRedirect(request.getContextPath() + "/app/dashboard");
                }
                
            } else {
                // Connexion échouée
                logger.warn("Tentative de connexion échouée pour l'utilisateur: {}", username);
                
                request.setAttribute("errorMessage", "Nom d'utilisateur ou mot de passe incorrect");
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'authentification: ", e);
            request.setAttribute("errorMessage", "Erreur lors de la connexion. Veuillez réessayer.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        }
    }
}
