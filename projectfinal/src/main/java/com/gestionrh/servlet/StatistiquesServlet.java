package com.gestionrh.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.DepartementDAO;
import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.dao.ProjetDAO;
import com.gestionrh.dao.impl.DepartementDAOImpl;
import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.dao.impl.ProjetDAOImpl;
import com.gestionrh.model.Departement;
import com.gestionrh.model.Employe;
import com.gestionrh.model.Projet;
import com.gestionrh.model.StatutEmploye;
import com.gestionrh.model.StatutProjet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet de statistiques et rapports pour l'application de gestion RH
 */
@WebServlet("/app/statistiques")
public class StatistiquesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(StatistiquesServlet.class);
    
    private EmployeDAO employeDAO;
    private DepartementDAO departementDAO;
    private ProjetDAO projetDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.employeDAO = new EmployeDAOImpl();
        this.departementDAO = new DepartementDAOImpl();
        this.projetDAO = new ProjetDAOImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Statistiques globales
            long totalEmployes = employeDAO.count();
            long totalDepartements = departementDAO.count();
            long totalProjets = projetDAO.count();
            List<Projet> projetsEnCours = projetDAO.findByStatut(StatutProjet.EN_COURS);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEmployes", totalEmployes);
            stats.put("totalDepartements", totalDepartements);
            stats.put("totalProjets", totalProjets);
            stats.put("projetsEnCours", projetsEnCours.size());
            
            // Calcul des moyennes
            if (totalDepartements > 0) {
                stats.put("moyenneEmployesParDept", (double) totalEmployes / totalDepartements);
                stats.put("moyenneProjetsParDept", (double) totalProjets / totalDepartements);
            } else {
                stats.put("moyenneEmployesParDept", 0.0);
                stats.put("moyenneProjetsParDept", 0.0);
            }
            
            if (totalProjets > 0) {
                List<Projet> tousLesProjets = projetDAO.findAll();
                int totalMembres = 0;
                for (Projet p : tousLesProjets) {
                    totalMembres += p.getEmployes().size();
                }
                stats.put("moyenneMembresParProjet", (double) totalMembres / totalProjets);
            } else {
                stats.put("moyenneMembresParProjet", 0.0);
            }
            
            request.setAttribute("stats", stats);
            
            // Employés par département
            List<Departement> departements = departementDAO.findAll();
            List<Map<String, Object>> deptStats = new ArrayList<>();
            
            for (Departement dept : departements) {
                List<Employe> employesDept = employeDAO.findByDepartementId(dept.getId());
                Map<String, Object> deptData = new HashMap<>();
                deptData.put("nom", dept.getNom());
                deptData.put("nbEmployes", employesDept.size());
                deptData.put("pourcentage", totalEmployes > 0 ? (employesDept.size() * 100.0 / totalEmployes) : 0);
                deptStats.add(deptData);
            }
            request.setAttribute("departements", deptStats);
            
            // Répartition par grade
            List<Employe> tousEmployes = employeDAO.findAll();
            Map<String, Integer> compteurGrades = new HashMap<>();
            for (Employe emp : tousEmployes) {
                String grade = emp.getGrade().toString();
                compteurGrades.put(grade, compteurGrades.getOrDefault(grade, 0) + 1);
            }
            
            List<Map<String, Object>> gradeStats = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : compteurGrades.entrySet()) {
                Map<String, Object> gradeData = new HashMap<>();
                gradeData.put("nom", entry.getKey());
                gradeData.put("count", entry.getValue());
                gradeData.put("pourcentage", totalEmployes > 0 ? (entry.getValue() * 100.0 / totalEmployes) : 0);
                gradeStats.add(gradeData);
            }
            request.setAttribute("grades", gradeStats);
            
            // Liste de tous les projets
            request.setAttribute("projets", projetDAO.findAll());
            
            // Statuts des employés
            List<Map<String, Object>> statutsEmployes = new ArrayList<>();
            for (StatutEmploye statut : StatutEmploye.values()) {
                List<Employe> employesParStatut = employeDAO.findByStatut(statut);
                Map<String, Object> statutData = new HashMap<>();
                statutData.put("nom", statut.toString());
                statutData.put("count", employesParStatut.size());
                statutsEmployes.add(statutData);
            }
            request.setAttribute("statutsEmployes", statutsEmployes);
            
            // Statuts des projets
            List<Map<String, Object>> statutsProjets = new ArrayList<>();
            for (StatutProjet statut : StatutProjet.values()) {
                List<Projet> projetsParStatut = projetDAO.findByStatut(statut);
                Map<String, Object> statutData = new HashMap<>();
                statutData.put("nom", statut.toString());
                statutData.put("count", projetsParStatut.size());
                statutsProjets.add(statutData);
            }
            request.setAttribute("statutsProjets", statutsProjets);
            
            request.getRequestDispatcher("/WEB-INF/jsp/statistiques.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Erreur dans StatistiquesServlet", e);
            request.setAttribute("error", "Erreur lors du chargement des statistiques: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }
}
