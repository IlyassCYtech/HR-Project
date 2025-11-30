package com.gestionrh.servlet;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.model.Employe;
import com.gestionrh.model.Utilisateur;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/app/profil")
public class ProfilServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfilServlet.class);
    private EmployeDAO employeDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.employeDAO = new EmployeDAOImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        logger.info("=== Affichage du profil utilisateur ===");
        
        try {
            HttpSession session = request.getSession(false);
            
            if (session == null) {
                logger.warn("Aucune session trouvée");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            
            // Récupérer l'utilisateur de la session
            Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
            
            if (utilisateur == null) {
                logger.warn("Aucun utilisateur dans la session");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            
            logger.info("Utilisateur connecté: {} (ID: {})", utilisateur.getUsername(), utilisateur.getId());
            
            // Chercher l'employé associé à cet utilisateur
            Employe employe = null;
            
            // D'abord vérifier s'il y a un employeId dans la session
            Long employeId = (Long) session.getAttribute("employeId");
            
            if (employeId != null) {
                // Optimisation: récupérer directement par ID (plus rapide)
                employe = employeDAO.findById(employeId);
                logger.info("Employé récupéré via employeId en session (RAPIDE): {} {} (ID: {})", 
                           employe != null ? employe.getPrenom() : "null",
                           employe != null ? employe.getNom() : "null",
                           employeId);
            } else {
                // Sinon, essayer de trouver l'employé par l'email de l'utilisateur (plus lent)
                logger.info("Pas d'employeId en session, recherche par email (LENT)...");
                String email = utilisateur.getUsername();
                if (email != null && email.contains("@")) {
                    employe = employeDAO.findByEmail(email);
                    if (employe != null) {
                        logger.info("Employé trouvé via email: {} {} (ID: {})", 
                                   employe.getPrenom(), employe.getNom(), employe.getId());
                        // IMPORTANT: Stocker l'employeId dans la session pour les prochaines fois
                        session.setAttribute("employeId", employe.getId());
                        logger.info("EmployeId {} stocké en session pour optimisation future", employe.getId());
                    } else {
                        logger.warn("Aucun employé trouvé pour l'email: {}", email);
                    }
                }
            }
            
            // Ajouter l'employé à la requête (peut être null)
            if (employe != null) {
                request.setAttribute("employe", employe);
                logger.info("Employé ajouté à la requête - Profil prêt à afficher");
            } else {
                logger.info("Aucun employé associé - Affichage du profil utilisateur uniquement");
            }
            
            // Forward vers la page de profil
            request.getRequestDispatcher("/WEB-INF/jsp/profil.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'affichage du profil", e);
            request.setAttribute("error", "Une erreur est survenue: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }
}
