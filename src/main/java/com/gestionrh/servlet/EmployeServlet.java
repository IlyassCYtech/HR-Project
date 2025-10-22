package com.gestionrh.servlet;

import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.dao.impl.DepartementDAOImpl;
import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.dao.DepartementDAO;
import com.gestionrh.model.Employe;
import com.gestionrh.model.Departement;
import com.gestionrh.model.StatutEmploye;
import com.gestionrh.model.Grade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/app/employes")
public class EmployeServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeServlet.class);
    private EmployeDAO employeDAO;
    private DepartementDAO departementDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.employeDAO = new EmployeDAOImpl();
        this.departementDAO = new DepartementDAOImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) action = "list";
        
        try {
            switch (action) {
                case "list":
                    listEmployes(request, response);
                    break;
                case "show":
                    showEmploye(request, response);
                    break;
                case "add":
                    showAddForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteEmploye(request, response);
                    break;
                default:
                    listEmployes(request, response);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans EmployeServlet: {}", e.getMessage(), e);
            request.setAttribute("error", "Une erreur est survenue: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) action = "create";
        
        try {
            switch (action) {
                case "create":
                    createEmploye(request, response);
                    break;
                case "update":
                    updateEmploye(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/app/employes");
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans EmployeServlet POST: {}", e.getMessage(), e);
            request.setAttribute("error", "Une erreur est survenue: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }
    
    private void listEmployes(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String search = request.getParameter("search");
        String matricule = request.getParameter("matricule");
        String departementIdStr = request.getParameter("departement");
        String statutStr = request.getParameter("statut");
        String gradeStr = request.getParameter("grade");
        String poste = request.getParameter("poste");
        
        List<Employe> employes;
        
        // Recherche par matricule (prioritaire)
        if (matricule != null && !matricule.trim().isEmpty()) {
            Employe employe = employeDAO.findByMatricule(matricule.trim());
            employes = employe != null ? List.of(employe) : List.of();
        }
        // Recherche par nom ou prénom
        else if (search != null && !search.trim().isEmpty()) {
            employes = employeDAO.findByNomOrPrenom(search.trim());
        } 
        // Filtre par département
        else if (departementIdStr != null && !departementIdStr.isEmpty()) {
            Long departementId = Long.parseLong(departementIdStr);
            employes = employeDAO.findByDepartementId(departementId);
        } 
        // Filtre par statut
        else if (statutStr != null && !statutStr.isEmpty()) {
            StatutEmploye statut = StatutEmploye.valueOf(statutStr);
            employes = employeDAO.findByStatut(statut);
        }
        // Filtre par grade
        else if (gradeStr != null && !gradeStr.isEmpty()) {
            Grade grade = Grade.valueOf(gradeStr);
            employes = employeDAO.findByGrade(grade);
        }
        // Filtre par poste
        else if (poste != null && !poste.trim().isEmpty()) {
            employes = employeDAO.findByPoste(poste.trim());
        }
        // Tous les employés
        else {
            employes = employeDAO.findAll();
        }
        
        List<Departement> departements = departementDAO.findActifs();
        
        request.setAttribute("employes", employes);
        request.setAttribute("departements", departements);
        request.setAttribute("statutsEmploye", StatutEmploye.values());
        request.setAttribute("currentSearch", search);
        request.setAttribute("currentMatricule", matricule);
        request.setAttribute("currentDepartement", departementIdStr);
        request.setAttribute("currentStatut", statutStr);
        request.setAttribute("currentGrade", gradeStr);
        request.setAttribute("currentPoste", poste);
        
        request.getRequestDispatcher("/WEB-INF/jsp/employes/list.jsp").forward(request, response);
    }
    
    private void showEmploye(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.parseLong(request.getParameter("id"));
        Employe employe = employeDAO.findById(id);
        
        if (employe == null) {
            request.setAttribute("error", "Employé non trouvé");
            listEmployes(request, response);
            return;
        }
        
        request.setAttribute("employe", employe);
        request.getRequestDispatcher("/WEB-INF/jsp/employes/show.jsp").forward(request, response);
    }
    
    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Departement> departements = departementDAO.findActifs();
        List<Employe> managers = employeDAO.findPotentialManagers();
        
        request.setAttribute("departements", departements);
        request.setAttribute("managers", managers);
        request.setAttribute("statutsEmploye", StatutEmploye.values());
        
        request.getRequestDispatcher("/WEB-INF/jsp/employes/form.jsp").forward(request, response);
    }
    
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.parseLong(request.getParameter("id"));
        Employe employe = employeDAO.findById(id);
        
        if (employe == null) {
            request.setAttribute("error", "Employé non trouvé");
            listEmployes(request, response);
            return;
        }
        
        List<Departement> departements = departementDAO.findActifs();
        List<Employe> managers = employeDAO.findPotentialManagers();
        
        request.setAttribute("employe", employe);
        request.setAttribute("departements", departements);
        request.setAttribute("managers", managers);
        request.setAttribute("statutsEmploye", StatutEmploye.values());
        request.setAttribute("isEdit", true);
        
        request.getRequestDispatcher("/WEB-INF/jsp/employes/form.jsp").forward(request, response);
    }
    
    private void createEmploye(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Employe employe = extractEmployeFromRequest(request);
            
            // Vérifier si le matricule existe déjà
            Employe existingByMatricule = employeDAO.findByMatricule(employe.getMatricule());
            if (existingByMatricule != null) {
                request.setAttribute("error", "Un employé avec le matricule '" + employe.getMatricule() + "' existe déjà");
                request.setAttribute("employe", employe);
                showAddForm(request, response);
                return;
            }
            
            // Vérifier si l'email existe déjà
            Employe existingByEmail = employeDAO.findByEmail(employe.getEmail());
            if (existingByEmail != null) {
                request.setAttribute("error", "Un employé avec l'email '" + employe.getEmail() + "' existe déjà");
                request.setAttribute("employe", employe);
                showAddForm(request, response);
                return;
            }
            
            employeDAO.save(employe);
            
            request.setAttribute("success", "Employé créé avec succès");
            response.sendRedirect(request.getContextPath() + "/app/employes");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'employé", e);
            request.setAttribute("error", "Erreur lors de la création: " + e.getMessage());
            showAddForm(request, response);
        }
    }
    
    private void updateEmploye(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        logger.info("=== DEBUT updateEmploye ===");
        
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            logger.info("Modification de l'employé ID: {}", id);
            
            Employe employe = employeDAO.findById(id);
            
            if (employe == null) {
                logger.warn("Employé non trouvé: {}", id);
                request.setAttribute("error", "Employé non trouvé");
                listEmployes(request, response);
                return;
            }
            
            // Récupérer le nouveau matricule du formulaire
            String newMatricule = request.getParameter("matricule");
            
            // Vérifier si le matricule a changé et si le nouveau existe déjà
            if (!employe.getMatricule().equals(newMatricule)) {
                Employe existingByMatricule = employeDAO.findByMatricule(newMatricule);
                if (existingByMatricule != null && !existingByMatricule.getId().equals(id)) {
                    request.setAttribute("error", "Un employé avec le matricule '" + newMatricule + "' existe déjà");
                    request.setAttribute("employe", employe);
                    showEditForm(request, response);
                    return;
                }
            }
            
            // Récupérer le nouvel email du formulaire
            String newEmail = request.getParameter("email");
            
            // Vérifier si l'email a changé et si le nouveau existe déjà
            if (!employe.getEmail().equals(newEmail)) {
                Employe existingByEmail = employeDAO.findByEmail(newEmail);
                if (existingByEmail != null && !existingByEmail.getId().equals(id)) {
                    request.setAttribute("error", "Un employé avec l'email '" + newEmail + "' existe déjà");
                    request.setAttribute("employe", employe);
                    showEditForm(request, response);
                    return;
                }
            }
            
            updateEmployeFromRequest(employe, request);
            employeDAO.update(employe);
            
            logger.info("Employé {} {} modifié avec succès", employe.getPrenom(), employe.getNom());
            logger.info("=== FIN updateEmploye (succès) ===");
            
            request.setAttribute("success", "Employé modifié avec succès");
            response.sendRedirect(request.getContextPath() + "/app/employes");
            
        } catch (Exception e) {
            logger.error("=== ERREUR updateEmploye ===", e);
            request.setAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            showEditForm(request, response);
        }
    }
    
    private void deleteEmploye(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            Employe employe = employeDAO.findById(id);
            
            if (employe == null) {
                request.setAttribute("error", "Employé non trouvé");
                listEmployes(request, response);
                return;
            }
            
            // Suppression logique - changement de statut seulement
            employe.setStatut(StatutEmploye.DEMISSION);
            employeDAO.update(employe);
            
            request.setAttribute("success", "Employé désactivé avec succès");
            response.sendRedirect(request.getContextPath() + "/app/employes");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de l'employé", e);
            request.setAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
            listEmployes(request, response);
        }
    }
    
    private Employe extractEmployeFromRequest(HttpServletRequest request) {
        Employe employe = new Employe();
        updateEmployeFromRequest(employe, request);
        return employe;
    }
    
    private void updateEmployeFromRequest(Employe employe, HttpServletRequest request) {
        // Matricule (obligatoire pour création, readonly en modification)
        String matricule = request.getParameter("matricule");
        if (matricule != null && !matricule.isEmpty()) {
            employe.setMatricule(matricule);
        }
        
        employe.setNom(request.getParameter("nom"));
        employe.setPrenom(request.getParameter("prenom"));
        employe.setEmail(request.getParameter("email"));
        employe.setTelephone(request.getParameter("telephone"));
        employe.setAdresse(request.getParameter("adresse"));
        
        // Date de naissance
        String dateNaissanceStr = request.getParameter("dateNaissance");
        if (dateNaissanceStr != null && !dateNaissanceStr.isEmpty()) {
            try {
                employe.setDateNaissance(LocalDate.parse(dateNaissanceStr));
            } catch (DateTimeParseException e) {
                logger.warn("Format de date de naissance invalide: {}", dateNaissanceStr);
            }
        }
        
        // Date d'embauche
        String dateEmbaucheStr = request.getParameter("dateEmbauche");
        if (dateEmbaucheStr != null && !dateEmbaucheStr.isEmpty()) {
            try {
                employe.setDateEmbauche(LocalDate.parse(dateEmbaucheStr));
            } catch (DateTimeParseException e) {
                logger.warn("Format de date d'embauche invalide: {}", dateEmbaucheStr);
            }
        }
        
        // Poste
        employe.setPoste(request.getParameter("poste"));
        
        // Grade
        String gradeStr = request.getParameter("grade");
        if (gradeStr != null && !gradeStr.isEmpty()) {
            try {
                employe.setGrade(Grade.valueOf(gradeStr));
            } catch (IllegalArgumentException e) {
                logger.warn("Grade invalide: {}", gradeStr);
            }
        }
        
        // Salaire
        String salaireStr = request.getParameter("salaire");
        if (salaireStr != null && !salaireStr.isEmpty()) {
            try {
                employe.setSalaireBase(new BigDecimal(salaireStr));
            } catch (NumberFormatException e) {
                logger.warn("Format de salaire invalide: {}", salaireStr);
            }
        }
        
        // Département
        String departementIdStr = request.getParameter("departementId");
        if (departementIdStr != null && !departementIdStr.isEmpty()) {
            Long departementId = Long.parseLong(departementIdStr);
            Departement departement = departementDAO.findById(departementId);
            employe.setDepartement(departement);
        } else {
            // Si vide, on met le département à null (aucun département)
            // Vérifier si l'employé est chef d'un département
            List<Departement> allDepartements = departementDAO.findAll();
            for (Departement dept : allDepartements) {
                if (dept.getChefDepartement() != null && 
                    dept.getChefDepartement().getId().equals(employe.getId())) {
                    // L'employé est chef, on le retire
                    logger.info("Retrait du chef {} du département {} car l'employé n'a plus de département", 
                        employe.getNom(), dept.getNom());
                    dept.setChefDepartement(null);
                    departementDAO.update(dept);
                }
            }
            employe.setDepartement(null);
        }
        
        // Manager
        String managerIdStr = request.getParameter("managerId");
        if (managerIdStr != null && !managerIdStr.isEmpty()) {
            Long managerId = Long.parseLong(managerIdStr);
            Employe manager = employeDAO.findById(managerId);
            employe.setManager(manager);
        }
        
        // Statut
        String statutStr = request.getParameter("statut");
        if (statutStr != null && !statutStr.isEmpty()) {
            employe.setStatut(StatutEmploye.valueOf(statutStr));
        }
    }
}
