package com.gestionrh.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.DepartementDAO;
import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.dao.ProjetDAO;
import com.gestionrh.dao.UtilisateurDAO;
import com.gestionrh.dao.impl.DepartementDAOImpl;
import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.dao.impl.ProjetDAOImpl;
import com.gestionrh.dao.impl.UtilisateurDAOImpl;
import com.gestionrh.model.Departement;
import com.gestionrh.model.Employe;
import com.gestionrh.model.Projet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.gestionrh.dao.NotificationDAO;
import com.gestionrh.dao.impl.NotificationDAOImpl;
import com.gestionrh.model.Utilisateur;


@WebServlet("/app/departements")
public class DepartementServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(DepartementServlet.class);
    private DepartementDAO departementDAO;
    private EmployeDAO employeDAO;
    private ProjetDAO projetDAO;
    private NotificationDAO notificationDAO;
    private UtilisateurDAO utilisateurDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.departementDAO = new DepartementDAOImpl();
        this.employeDAO = new EmployeDAOImpl();
        this.projetDAO = new ProjetDAOImpl();
        this.notificationDAO = new NotificationDAOImpl();
        this.utilisateurDAO = new UtilisateurDAOImpl();
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
                case "retirerEmploye":
                    retirerEmploye(request, response);
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
        
        // Charger l'employé connecté pour vérifier son département
        Long employeIdSession = (Long) request.getSession().getAttribute("employeId");
        if (employeIdSession != null) {
            Employe employeConnecte = employeDAO.findById(employeIdSession);
            request.setAttribute("employeConnecte", employeConnecte);
        }
        
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
        
        // Vérifier si c'est un CHEF_DEPT qui accède au formulaire
        Utilisateur utilisateur = (Utilisateur) request.getSession().getAttribute("utilisateur");
        boolean isOwnDepartment = false;
        
        if (utilisateur != null && utilisateur.getRole() == Utilisateur.Role.CHEF_DEPT) {
            Employe employe = utilisateur.getEmploye();
            if (employe != null && employe.getDepartement() != null) {
                isOwnDepartment = employe.getDepartement().getId().equals(id);
            }
            
            // CHEF_DEPT ne peut gérer que les membres de SON département
            if (!isOwnDepartment) {
                request.setAttribute("error", "Vous ne pouvez gérer que les membres de votre propre département");
                listDepartements(request, response);
                return;
            }
            
            // Marquer que c'est un chef de département qui édite (mode limité)
            request.setAttribute("chefDeptMode", true);
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
        
        logger.info("=== DEBUT createDepartement ===");
        try {
            logger.info("Extraction des données du département depuis la requête...");
            Departement departement = extractDepartementFromRequest(request);
            
            logger.info("Département à créer: nom={}, description={}, budget={}, chef={}, actif={}", 
                departement.getNom(), 
                departement.getDescription(),
                departement.getBudget(),
                departement.getChefDepartement() != null ? departement.getChefDepartement().getId() : "null",
                departement.getActif());
            
            logger.info("Appel de departementDAO.save()...");
            departementDAO.save(departement);
            logger.info("✅ Département sauvegardé avec ID: {}", departement.getId());
            
            // Affecter le chef au département APRÈS la sauvegarde
            if (departement.getChefDepartement() != null) {
                Employe chef = departement.getChefDepartement();
                
                // 1. Affecter le chef au département s'il n'y est pas déjà
                if (chef.getDepartement() == null || !chef.getDepartement().getId().equals(departement.getId())) {
                    logger.info("Affectation du chef {} au département {} après création", 
                        chef.getNom(), departement.getNom());
                    chef.setDepartement(departement);
                    employeDAO.update(chef);
                    logger.info("✅ Chef affecté au département");
                }
                
                // 2. Attribuer le rôle CHEF_DEPT au chef s'il a un compte utilisateur
                // SAUF s'il est déjà ADMIN ou RH
                Utilisateur utilisateurChef = utilisateurDAO.findByEmployeId(chef.getId());
                if (utilisateurChef != null) {
                    Utilisateur.Role roleActuel = utilisateurChef.getRole();
                    if (roleActuel != Utilisateur.Role.ADMIN && roleActuel != Utilisateur.Role.RH) {
                        logger.info("Attribution du rôle CHEF_DEPT à {} (ancien rôle: {})", 
                            chef.getNom(), roleActuel);
                        utilisateurChef.setRole(Utilisateur.Role.CHEF_DEPT);
                        utilisateurDAO.update(utilisateurChef);
                        logger.info("✅ Rôle CHEF_DEPT attribué");
                    } else {
                        logger.info("Le chef {} conserve son rôle {} (prioritaire)", chef.getNom(), roleActuel);
                    }
                }
            }
            
            // Créer notification uniquement si l'ID est disponible
            if (departement.getId() != null) {
                try {
                    Utilisateur utilisateurConnecte = (Utilisateur) request.getSession().getAttribute("utilisateur");
                    String auteur = (utilisateurConnecte != null) ? utilisateurConnecte.getUsername() : "Système";

                    int depId = departement.getId().intValue();
                    String message = "Le département \"" + departement.getNom()
                            + "\" a été créé par " + auteur + ".";
                    notificationDAO.creerNotificationPourDepartement(
                            depId, 
                            message,
                            "INFO"
                    );
                    logger.info("✅ Notification créée");
                } catch (Exception notifEx) {
                    logger.warn("⚠️ Impossible de créer la notification: {}", notifEx.getMessage(), notifEx);
                    // Ne pas bloquer la création du département si la notification échoue
                }
            }
            
            logger.info("=== FIN createDepartement (succès) ===");
            request.setAttribute("success", "Département créé avec succès");
            response.sendRedirect(request.getContextPath() + "/app/departements");
            
        } catch (Exception e) {
            logger.error("❌ ❌ ❌ Erreur lors de la création du département", e);
            logger.error("Type d'exception: {}", e.getClass().getName());
            logger.error("Message: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Cause: {}", e.getCause().getMessage());
            }
            request.setAttribute("error", "Erreur lors de la création: " + e.getMessage());
            showAddForm(request, response);
            logger.info("=== FIN createDepartement (échec) ===");
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
            
            // Vérifier si c'est un CHEF_DEPT qui modifie
            Utilisateur utilisateur = (Utilisateur) request.getSession().getAttribute("utilisateur");
            boolean isChefDept = (utilisateur != null && utilisateur.getRole() == Utilisateur.Role.CHEF_DEPT);
            
            if (isChefDept) {
                Employe employe = utilisateur.getEmploye();
                
                // Vérifier que c'est bien SON département
                if (employe == null || employe.getDepartement() == null || 
                    !employe.getDepartement().getId().equals(id)) {
                    request.setAttribute("error", "Vous ne pouvez gérer que les membres de votre propre département");
                    listDepartements(request, response);
                    return;
                }
                
                // CHEF_DEPT ne peut PAS modifier le département via ce formulaire
                // Il peut seulement voir et gérer les membres via d'autres actions
                request.setAttribute("error", "Vous ne pouvez pas modifier les informations du département. Utilisez la gestion des membres.");
                listDepartements(request, response);
                return;
            }
            
            // ADMIN/RH peuvent tout modifier
            // Récupérer l'ancien chef avant la mise à jour
            Employe ancienChef = departement.getChefDepartement();
            
            // Mettre à jour le département avec les nouvelles valeurs
            updateDepartementFromRequest(departement, request, false); // false = update complet
            
            // Récupérer le nouveau chef après la mise à jour
            Employe nouveauChef = departement.getChefDepartement();
            
            // Gérer le changement de chef
            if (nouveauChef != null) {
                // 1. Affecter le nouveau chef au département s'il n'y est pas déjà
                if (nouveauChef.getDepartement() == null || !nouveauChef.getDepartement().getId().equals(departement.getId())) {
                    logger.info("Affectation du nouveau chef {} au département {}", 
                        nouveauChef.getNom(), departement.getNom());
                    nouveauChef.setDepartement(departement);
                    employeDAO.update(nouveauChef);
                    logger.info("✅ Nouveau chef affecté au département");
                }
                
                // 2. Attribuer le rôle CHEF_DEPT au nouveau chef (sauf ADMIN/RH)
                Utilisateur utilisateurNouveauChef = utilisateurDAO.findByEmployeId(nouveauChef.getId());
                if (utilisateurNouveauChef != null) {
                    Utilisateur.Role roleActuel = utilisateurNouveauChef.getRole();
                    if (roleActuel != Utilisateur.Role.ADMIN && roleActuel != Utilisateur.Role.RH) {
                        logger.info("Attribution du rôle CHEF_DEPT au nouveau chef {} (ancien rôle: {})", 
                            nouveauChef.getNom(), roleActuel);
                        utilisateurNouveauChef.setRole(Utilisateur.Role.CHEF_DEPT);
                        utilisateurDAO.update(utilisateurNouveauChef);
                        logger.info("✅ Rôle CHEF_DEPT attribué au nouveau chef");
                    } else {
                        logger.info("Le nouveau chef {} conserve son rôle {} (prioritaire)", 
                            nouveauChef.getNom(), roleActuel);
                    }
                }
                
                // 3. L'ancien chef garde son rôle CHEF_DEPT et reste dans le département
                if (ancienChef != null && !ancienChef.getId().equals(nouveauChef.getId())) {
                    logger.info("L'ancien chef {} reste membre du département {} et conserve son rôle CHEF_DEPT", 
                        ancienChef.getNom(), departement.getNom());
                    // L'ancien chef garde son département et son rôle - aucune action nécessaire
                }
            }
            
            departementDAO.update(departement);
            
            // Créer notification (non bloquant)
            try {
                Utilisateur utilisateurConnecte = (Utilisateur) request.getSession().getAttribute("utilisateur");
                String auteur = (utilisateurConnecte != null) ? utilisateurConnecte.getUsername() : "Système";
                int depId = id.intValue();
                String message = "Le département \"" + departement.getNom() + "\" a été modifié par " + auteur + ".";
                notificationDAO.creerNotificationPourDepartement(depId, message, "INFO");
            } catch (Exception notifEx) {
                logger.warn("Impossible de créer la notification: {}", notifEx.getMessage());
            }
            
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
            
            int depId = id.intValue(); // conversion Long -> int
            String notifMessage;
            if (nombreEmployes > 0) {
                notifMessage = "Le département \"" + departement.getNom() + "\" a été supprimé. "
                             + nombreEmployes + " employé(s) ont été désaffecté(s).";
            } else {
                notifMessage = "Le département \"" + departement.getNom() + "\" a été supprimé.";
            }
            notificationDAO.creerNotificationPourDepartement(depId, notifMessage, "ALERTE");
            
        } catch (Exception e) {
            logger.error("=== ERREUR dans deleteDepartement ===", e);
            request.setAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
            listDepartements(request, response);
            logger.info("=== FIN deleteDepartement (échec) ===");
        }
    }
    
    private Departement extractDepartementFromRequest(HttpServletRequest request) {
        Departement departement = new Departement();
        updateDepartementFromRequest(departement, request, false); // false = création (ne pas mettre à jour l'employé)
        return departement;
    }
    
    private void updateDepartementFromRequest(Departement departement, HttpServletRequest request, boolean membersOnly) {
        // Si membersOnly = true, c'est un CHEF_DEPT qui ne peut modifier que les membres
        // Si membersOnly = false, c'est ADMIN/RH qui peut tout modifier
        
        if (!membersOnly) {
            // ADMIN/RH peuvent modifier toutes les informations du département
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
                    
                    // Affecter automatiquement le chef au département en mode modification
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
        
        // CHEF_DEPT peut gérer les membres (c'est géré via une autre action: affecterEmploye/retirerEmploye)
        // On ne fait rien ici pour membersOnly=true car la gestion des membres passe par des actions séparées
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
            
            Utilisateur utilisateurConnecte = (Utilisateur) request.getSession().getAttribute("utilisateur");
            String auteur = (utilisateurConnecte != null) ? utilisateurConnecte.getUsername() : "Système";

            String message = "L'employé " + employe.getPrenom() + " " + employe.getNom()
                    + " a été affecté au département \"" + departement.getNom()
                    + "\" par " + auteur + ".";

            notificationDAO.creerNotificationPourDepartement(
                    departementId.intValue(),
                    message,
                    "INFO"
            );
            
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
    
    private void retirerEmploye(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long departementId = Long.parseLong(request.getParameter("departementId"));
            Long employeId = Long.parseLong(request.getParameter("employeId"));
            
            logger.info("Retrait de l'employé {} du département {}", employeId, departementId);
            
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
            
            // Contrôle de sécurité pour CHEF_DEPT
            Utilisateur utilisateur = (Utilisateur) request.getSession().getAttribute("utilisateur");
            if (utilisateur != null && utilisateur.getRole() == Utilisateur.Role.CHEF_DEPT) {
                Employe employeConnecte = utilisateur.getEmploye();
                
                // Vérifier que c'est bien SON département
                if (employeConnecte == null || employeConnecte.getDepartement() == null || 
                    !employeConnecte.getDepartement().getId().equals(departementId)) {
                    request.setAttribute("error", "Vous ne pouvez gérer que les membres de votre propre département");
                    response.sendRedirect(request.getContextPath() + "/app/departements?action=show&id=" + departementId);
                    return;
                }
                
                // CHEF_DEPT ne peut PAS se retirer lui-même
                if (employeConnecte.getId().equals(employeId)) {
                    request.setAttribute("error", "Vous ne pouvez pas vous retirer vous-même du département");
                    response.sendRedirect(request.getContextPath() + "/app/departements?action=show&id=" + departementId);
                    return;
                }
            }
            
            // Vérifier que l'employé est bien dans ce département
            if (employe.getDepartement() == null || !employe.getDepartement().getId().equals(departementId)) {
                request.setAttribute("error", "Cet employé n'appartient pas à ce département");
                showDepartement(request, response);
                return;
            }
            
            // Vérifier si l'employé est chef du département
            if (departement.getChefDepartement() != null && 
                departement.getChefDepartement().getId().equals(employeId)) {
                request.setAttribute("error", "❌ Impossible de retirer cet employé car il est chef de ce département. Veuillez d'abord retirer son rôle de chef.");
                response.sendRedirect(request.getContextPath() + "/app/departements?action=show&id=" + departementId + "&error=chef");
                return;
            }
            
            // Retirer l'employé du département
            employe.setDepartement(null);
            employeDAO.update(employe);
            
            logger.info("Employé {} {} retiré du département {}", 
                       employe.getPrenom(), employe.getNom(), departement.getNom());
            
            Utilisateur utilisateurConnecte = (Utilisateur) request.getSession().getAttribute("utilisateur");
            String auteur = (utilisateurConnecte != null) ? utilisateurConnecte.getUsername() : "Système";

            String message = "L'employé " + employe.getPrenom() + " " + employe.getNom()
                    + " a été retiré du département \"" + departement.getNom()
                    + "\" par " + auteur + ".";

            notificationDAO.creerNotificationPourDepartement(
                    departementId.intValue(),
                    message,
                    "INFO" // ou "ALERTE" si tu veux que ça ressorte plus
            );
            
            // Rediriger vers la page du département avec un message de succès
            response.sendRedirect(request.getContextPath() + "/app/departements?action=show&id=" + departementId);
            
        } catch (NumberFormatException e) {
            logger.error("Erreur de format des paramètres", e);
            request.setAttribute("error", "Paramètres invalides");
            listDepartements(request, response);
        } catch (Exception e) {
            logger.error("Erreur lors du retrait de l'employé", e);
            request.setAttribute("error", "Erreur lors du retrait: " + e.getMessage());
            showDepartement(request, response);
        }
    }
}
