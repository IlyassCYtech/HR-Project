package com.gestionrh.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.dao.DepartementDAO;
import com.gestionrh.dao.ProjetDAO;
import com.gestionrh.dao.CongeAbsenceDAO;
import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.dao.impl.DepartementDAOImpl;
import com.gestionrh.dao.impl.ProjetDAOImpl;
import com.gestionrh.dao.impl.CongeAbsenceDAOImpl;
import com.gestionrh.model.Utilisateur;
import com.gestionrh.model.StatutProjet;
import com.gestionrh.model.CongeAbsence.StatutDemande;
import com.gestionrh.util.HibernateUtil;

/**
 * Servlet pour le tableau de bord principal
 */
@WebServlet("/app/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DashboardServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Utilisateur utilisateur = (Utilisateur) httpSession.getAttribute("utilisateur");
        request.setAttribute("utilisateur", utilisateur);
        
        logger.debug("Affichage du tableau de bord pour l'utilisateur: {}", utilisateur.getUsername());
        
        // Charger les statistiques
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            EmployeDAO employeDAO = new EmployeDAOImpl();
            DepartementDAO departementDAO = new DepartementDAOImpl();
            ProjetDAO projetDAO = new ProjetDAOImpl();
            CongeAbsenceDAO congeDAO = new CongeAbsenceDAOImpl();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("nbEmployes", employeDAO.count());
            stats.put("nbDepartements", departementDAO.count());
            stats.put("nbProjetsActifs", projetDAO.countByStatut(StatutProjet.EN_COURS));
            stats.put("nbCongesEnAttente", congeDAO.countByStatut(StatutDemande.EN_ATTENTE));
            
            request.setAttribute("stats", stats);
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des statistiques", e);
            // Mettre des valeurs par défaut
            Map<String, Object> stats = new HashMap<>();
            stats.put("nbEmployes", 0);
            stats.put("nbDepartements", 0);
            stats.put("nbProjetsActifs", 0);
            stats.put("nbCongesEnAttente", 0);
            request.setAttribute("stats", stats);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        
        request.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(request, response);
    }
}
