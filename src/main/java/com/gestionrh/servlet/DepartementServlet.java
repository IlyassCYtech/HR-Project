package com.gestionrh.servlet;

import com.gestionrh.dao.impl.DepartementDAOImpl;
import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.dao.impl.ProjetDAOImpl;
import com.gestionrh.dao.DepartementDAO;
import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.dao.ProjetDAO;
import com.gestionrh.model.Departement;
import com.gestionrh.model.Employe;
import com.gestionrh.model.Projet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/app/departements")
public class DepartementServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(DepartementServlet.class);
    private DepartementDAO departementDAO;
    private EmployeDAO employeDAO;
    private ProjetDAO projetDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.departementDAO = new DepartementDAOImpl();
        this.employeDAO = new EmployeDAOImpl();
        this.projetDAO = new ProjetDAOImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) action = "list";
        
        logger.info("=== doGet appelé - action: {} ===", action);
        
        try {
            switch (action) {
                case "list":
                    listDepartements(request, response);
                    break;
                case "show":
                    showDepartement(request, response);
                    break;
                case "add":
                    showAddForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    logger.info("Appel de deleteDepartement depuis doGet");
                    deleteDepartement(request, response);
                    break;
                default:
                    listDepartements(request, response);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans DepartementServlet: {}", e.getMessage(), e);
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
                    createDepartement(request, response);
                    break;
                case "update":
                    updateDepartement(request, response);
                    break;
                case "affecterEmploye":
                    affecterEmploye(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/app/departements");
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans DepartementServlet POST: {}", e.getMessage(), e);
            request.setAttribute("error", "Une erreur est survenue: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }
    
    private void listDepartements(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String search = request.getParameter("search");
        List<Departement> departements;
        
        if (search != null && !search.trim().isEmpty()) {
            departements = departementDAO.findByNom(search.trim());
        } else {
            departements = departementDAO.findAll();
        }
        
        // Charger le nombre d'employés pour chaque département
        java.util.Map<Long, Long> employeCountsMap = new java.util.HashMap<>();
        for (Departement dept : departements) {
            long count = employeDAO.countByDepartement(dept.getId());
            employeCountsMap.put(dept.getId(), count);
        }
        
        request.setAttribute("departements", departements);
        request.setAttribute("employeCountsMap", employeCountsMap);
        request.setAttribute("currentSearch", search);
        
        request.getRequestDispatcher("/WEB-INF/jsp/departements/list.jsp").forward(request, response);
    }
    
    private void showDepartement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.parseLong(request.getParameter("id"));
        Departement departement = departementDAO.findById(id);
        
        if (departement == null) {
            request.setAttribute("error", "Département non trouvé");
            listDepartements(request, response);
            return;
        }
        
        // Charger les employés du département
        List<Employe> employes = employeDAO.findByDepartementId(id);
        
        // Charger les employés sans département (pour le modal d'affectation)
        List<Employe> employesSansService = employeDAO.findWithoutDepartement();
        
        // Charger les projets du département
        List<Projet> projets = projetDAO.findByDepartementId(id);
        
        // Compter les employés et projets
        int nbEmployes = employes.size();
        int nbProjets = projets.size();
        
        request.setAttribute("departement", departement);
        request.setAttribute("employes", employes);
        request.setAttribute("employesSansService", employesSansService);
        request.setAttribute("projets", projets);
        request.setAttribute("nbEmployes", nbEmployes);
        request.setAttribute("nbProjets", nbProjets);
        
        request.getRequestDispatcher("/WEB-INF/jsp/departements/show.jsp").forward(request, response);
    }
    
    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Charger tous les employés actifs pour le select
        List<Employe> employes = employeDAO.findActifs();
        
        // Filtrer les employés qui sont déjà chefs d'un autre département
        List<Departement> allDepartements = departementDAO.findAll();
        employes.removeIf(emp -> {
            for (Departement dept : allDepartements) {
                if (dept.getChefDepartement() != null && 
                    dept.getChefDepartement().getId().equals(emp.getId())) {
                    return true; // Cet employé est déjà chef d'un département
                }
            }
            return false;
        });
        
        request.setAttribute("employes", employes);
        
        request.getRequestDispatcher("/WEB-INF/jsp/departements/form.jsp").forward(request, response);
    }
    
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.parseLong(request.getParameter("id"));
        Departement departement = departementDAO.findById(id);
        
        if (departement == null) {
            request.setAttribute("error", "Département non trouvé");
            listDepartements(request, response);
            return;
        }
        
        // Charger tous les employés actifs pour le select
        List<Employe> employes = employeDAO.findActifs();
        
        // Filtrer les employés qui sont déjà chefs d'un autre département
        // (mais garder le chef actuel de ce département)
        Long currentChefId = (departement.getChefDepartement() != null) 
            ? departement.getChefDepartement().getId() 
            : null;
        
        List<Departement> allDepartements = departementDAO.findAll();
        employes.removeIf(emp -> {
            // Ne pas exclure le chef actuel de ce département
            if (currentChefId != null && emp.getId().equals(currentChefId)) {
                return false;
            }
            // Exclure ceux qui sont chefs d'autres départements
            for (Departement dept : allDepartements) {
                if (!dept.getId().equals(id) && // Ignorer le département actuel
                    dept.getChefDepartement() != null && 
                    dept.getChefDepartement().getId().equals(emp.getId())) {
                    return true; // Cet employé est déjà chef d'un autre département
                }
            }
            return false;
        });
        
        request.setAttribute("departement", departement);
        request.setAttribute("employes", employes);
        request.setAttribute("isEdit", true);
        
        request.getRequestDispatcher("/WEB-INF/jsp/departements/form.jsp").forward(request, response);
    }
    
    private void createDepartement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Departement departement = extractDepartementFromRequest(request);
            departementDAO.save(departement);
            
            request.setAttribute("success", "Département créé avec succès");
            response.sendRedirect(request.getContextPath() + "/app/departements");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création du département", e);
            request.setAttribute("error", "Erreur lors de la création: " + e.getMessage());
            showAddForm(request, response);
        }
    }
    
    private void updateDepartement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            Departement departement = departementDAO.findById(id);
            
            if (departement == null) {
                request.setAttribute("error", "Département non trouvé");
                listDepartements(request, response);
                return;
            }
            
            updateDepartementFromRequest(departement, request);
            departementDAO.update(departement);
            
            request.setAttribute("success", "Département modifié avec succès");
            response.sendRedirect(request.getContextPath() + "/app/departements");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la modification du département", e);
            request.setAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            showEditForm(request, response);
        }
    }
    
    private void deleteDepartement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        logger.info("=== DEBUT deleteDepartement ===");
        
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            logger.info("Tentative de suppression du département ID: {}", id);
            
            Departement departement = departementDAO.findById(id);
            
            if (departement == null) {
                logger.warn("Département non trouvé: {}", id);
                request.setAttribute("error", "Département non trouvé");
                listDepartements(request, response);
                return;
            }
            
            logger.info("Département trouvé: {} ({})", departement.getNom(), departement.getId());
            
            // Vérifier s'il y a des employés dans ce département
            long nombreEmployes = departementDAO.countEmployes(id);
            logger.info("Nombre d'employés dans le département: {}", nombreEmployes);
            
            // Si le département a des employés, les désaffecter
            if (nombreEmployes > 0) {
                logger.info("Début de la désaffectation de {} employé(s)...", nombreEmployes);
                List<Employe> employes = employeDAO.findByDepartementId(id);
                logger.info("Liste des employés récupérée: {} employé(s)", employes.size());
                for (Employe employe : employes) {
                    logger.debug("Désaffectation de l'employé: {} {} (ID: {})", 
                                 employe.getPrenom(), employe.getNom(), employe.getId());
                    employe.setDepartement(null);
                    employeDAO.update(employe);
                }
                logger.info("Désaffectation de {} employé(s) du département {} terminée", nombreEmployes, departement.getNom());
            }
            
            // Suppression PHYSIQUE du département
            logger.info("Suppression PHYSIQUE du département...");
            departementDAO.delete(departement);
            logger.info("Département {} supprimé définitivement de la base de données", departement.getNom());
            
            String message = nombreEmployes > 0 
                ? "Département supprimé définitivement. " + nombreEmployes + " employé(s) ont été désaffecté(s)."
                : "Département supprimé définitivement.";
            
            logger.info("Message de succès préparé: {}", message);
            request.setAttribute("success", message);
            logger.info("Redirection vers /app/departements");
            response.sendRedirect(request.getContextPath() + "/app/departements");
            logger.info("=== FIN deleteDepartement (succès) ===");
            
        } catch (Exception e) {
            logger.error("=== ERREUR dans deleteDepartement ===", e);
            request.setAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
            listDepartements(request, response);
            logger.info("=== FIN deleteDepartement (échec) ===");
        }
    }
    
    private Departement extractDepartementFromRequest(HttpServletRequest request) {
        Departement departement = new Departement();
        updateDepartementFromRequest(departement, request);
        return departement;
    }
    
    private void updateDepartementFromRequest(Departement departement, HttpServletRequest request) {
        departement.setNom(request.getParameter("nom"));
        departement.setDescription(request.getParameter("description"));
        
        // Budget
        String budgetStr = request.getParameter("budget");
        if (budgetStr != null && !budgetStr.isEmpty()) {
            try {
                departement.setBudget(new BigDecimal(budgetStr));
            } catch (NumberFormatException e) {
                logger.warn("Format de budget invalide: {}", budgetStr);
            }
        }
        
        // Chef de département
        String chefIdStr = request.getParameter("chefDepartementId");
        if (chefIdStr != null && !chefIdStr.isEmpty()) {
            try {
                Long chefId = Long.parseLong(chefIdStr);
                Employe chef = employeDAO.findById(chefId);
                departement.setChefDepartement(chef);
                
                // Affecter automatiquement le chef au département
                if (chef != null && chef.getDepartement() != departement) {
                    logger.info("Affectation automatique du chef {} au département {}", 
                        chef.getNom(), departement.getNom());
                    chef.setDepartement(departement);
                    employeDAO.update(chef);
                }
            } catch (NumberFormatException e) {
                logger.warn("Format d'ID chef invalide: {}", chefIdStr);
            }
        }
        
        // Actif
        String actifStr = request.getParameter("actif");
        if (actifStr != null) {
            departement.setActif(actifStr.equals("true") || actifStr.equals("on"));
        } else {
            departement.setActif(true); // Par défaut actif
        }
    }
    
    private void affecterEmploye(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long departementId = Long.parseLong(request.getParameter("departementId"));
            Long employeId = Long.parseLong(request.getParameter("employeId"));
            
            logger.info("Affectation de l'employé {} au département {}", employeId, departementId);
            
            // Récupérer l'employé et le département
            Employe employe = employeDAO.findById(employeId);
            Departement departement = departementDAO.findById(departementId);
            
            if (employe == null) {
                request.setAttribute("error", "Employé non trouvé");
                showDepartement(request, response);
                return;
            }
            
            if (departement == null) {
                request.setAttribute("error", "Département non trouvé");
                listDepartements(request, response);
                return;
            }
            
            // Affecter l'employé au département
            employe.setDepartement(departement);
            employeDAO.update(employe);
            
            logger.info("Employé {} {} affecté au département {}", 
                       employe.getPrenom(), employe.getNom(), departement.getNom());
            
            // Rediriger vers la page du département avec un message de succès
            response.sendRedirect(request.getContextPath() + "/app/departements?action=show&id=" + departementId);
            
        } catch (NumberFormatException e) {
            logger.error("Erreur de format des paramètres", e);
            request.setAttribute("error", "Paramètres invalides");
            listDepartements(request, response);
        } catch (Exception e) {
            logger.error("Erreur lors de l'affectation de l'employé", e);
            request.setAttribute("error", "Erreur lors de l'affectation: " + e.getMessage());
            showDepartement(request, response);
        }
    }
}
