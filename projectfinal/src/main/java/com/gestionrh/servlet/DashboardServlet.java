package com.gestionrh.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.CongeAbsenceDAO;
import com.gestionrh.dao.DepartementDAO;
import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.dao.ProjetDAO;
import com.gestionrh.dao.impl.CongeAbsenceDAOImpl;
import com.gestionrh.dao.impl.DepartementDAOImpl;
import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.dao.impl.ProjetDAOImpl;
import com.gestionrh.model.CongeAbsence.StatutDemande;
import com.gestionrh.model.StatutProjet;
import com.gestionrh.model.Utilisateur;
import com.gestionrh.util.HibernateUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
            
            // *** FILTRAGE DES STATISTIQUES PAR RÔLE ***
            if (utilisateur.getRole() == Utilisateur.Role.EMPLOYE) {
                // Pour un EMPLOYE : statistiques limitées à son périmètre
                Long employeId = (Long) httpSession.getAttribute("employeId");
                if (employeId == null) {
                    String email = utilisateur.getUsername();
                    if (email != null && email.contains("@")) {
                        var employe = employeDAO.findByEmail(email);
                        if (employe != null) {
                            employeId = employe.getId();
                            httpSession.setAttribute("employeId", employeId);
                        }
                    }
                }
                
                if (employeId != null) {
                    var employe = employeDAO.findById(employeId);
                    // Employés de son département
                    if (employe != null && employe.getDepartement() != null) {
                        stats.put("nbEmployes", employeDAO.findByDepartementId(employe.getDepartement().getId()).size());
                        stats.put("nbDepartements", 1); // Son département uniquement
                    } else {
                        stats.put("nbEmployes", 1);
                        stats.put("nbDepartements", 0);
                    }
                    // Ses projets : filtrer manuellement
                    final Long finalEmployeId = employeId;
                    long nbProjetsActifs = projetDAO.findAll().stream()
                        .filter(p -> p.getStatut() == StatutProjet.EN_COURS)
                        .filter(p -> p.getEmployes() != null && 
                                    p.getEmployes().stream()
                                    .anyMatch(ep -> ep.getEmploye() != null && 
                                                   ep.getEmploye().getId().equals(finalEmployeId)))
                        .count();
                    stats.put("nbProjetsActifs", nbProjetsActifs);
                    
                    // Ses congés en attente
                    stats.put("nbCongesEnAttente", congeDAO.findByEmployeId(employeId).stream()
                        .filter(c -> c.getStatut() == StatutDemande.EN_ATTENTE).count());
                } else {
                    // Valeurs par défaut si aucun employé trouvé
                    stats.put("nbEmployes", 0);
                    stats.put("nbDepartements", 0);
                    stats.put("nbProjetsActifs", 0);
                    stats.put("nbCongesEnAttente", 0);
                }
                
                logger.info("Statistiques EMPLOYE - Département limité");
                
            } else {
                // Pour ADMIN/RH/CHEF : statistiques globales
                stats.put("nbEmployes", employeDAO.count());
                stats.put("nbDepartements", departementDAO.count());
                stats.put("nbProjetsActifs", projetDAO.countByStatut(StatutProjet.EN_COURS));
                stats.put("nbCongesEnAttente", congeDAO.countByStatut(StatutDemande.EN_ATTENTE));
                
                logger.info("Statistiques globales pour rôle: {}", utilisateur.getRole());
            }
            
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
