package com.gestionrh.servlet;

import java.io.IOException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.gestionrh.model.EmployeProjet;
import com.gestionrh.model.PrioriteProjet;
import com.gestionrh.model.Projet;
import com.gestionrh.model.StatutProjet;
import com.gestionrh.model.Utilisateur;
import com.gestionrh.dao.NotificationDAO;
import com.gestionrh.dao.impl.NotificationDAOImpl;
import com.gestionrh.model.NotificationUser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/app/projets")
public class ProjetServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjetServlet.class);
    private ProjetDAO projetDAO;
    private DepartementDAO departementDAO;
    private EmployeDAO employeDAO;
    private NotificationDAO notificationDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.projetDAO = new ProjetDAOImpl();
        this.departementDAO = new DepartementDAOImpl();
        this.employeDAO = new EmployeDAOImpl();
        this.notificationDAO = new NotificationDAOImpl(); 
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) action = "list";
        
        try {
            switch (action) {
                case "list":
                    listProjets(request, response);
                    break;
                case "show":
                    showProjet(request, response);
                    break;
                case "add":
                    showAddForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteProjet(request, response);
                    break;
                default:
                    listProjets(request, response);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans ProjetServlet: {}", e.getMessage(), e);
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
                    createProjet(request, response);
                    break;
                case "update":
                    updateProjet(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/app/projets");
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans ProjetServlet POST: {}", e.getMessage(), e);
            request.setAttribute("error", "Une erreur est survenue: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }
    
    private void listProjets(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String search = request.getParameter("search");
        String statutStr = request.getParameter("statut");
        String departementIdStr = request.getParameter("departementId");
        String employeIdStr = request.getParameter("employe");
        
        // Vérifier le rôle de l'utilisateur
        Utilisateur utilisateur = (Utilisateur) request.getSession().getAttribute("utilisateur");
        Long employeIdSession = (Long) request.getSession().getAttribute("employeId");
        
        boolean isChefDepartement = false;
        Long chefDepartementId = null;
        
        // Vérifier si l'employé est chef de département
        if (employeIdSession != null) {
            Employe employe = employeDAO.findById(employeIdSession);
            if (employe != null && employe.getDepartement() != null) {
                Departement dept = employe.getDepartement();
                if (dept.getChefDepartement() != null && dept.getChefDepartement().getId().equals(employeIdSession)) {
                    isChefDepartement = true;
                    chefDepartementId = dept.getId();
                    logger.info("Chef de département détecté: {} est chef de {} (ID: {})", 
                        employe.getNom(), dept.getNom(), dept.getId());
                }
            }
        }
        
        // Récupérer tous les projets
        List<Projet> projets = projetDAO.findAll();
        
        // Appliquer les filtres de manière cumulative
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            projets = projets.stream()
                .filter(p -> p.getNom() != null && p.getNom().toLowerCase().contains(searchLower))
                .collect(java.util.stream.Collectors.toList());
            logger.debug("Filtrage par recherche '{}': {} projets trouvés", search, projets.size());
        }
        
        if (statutStr != null && !statutStr.isEmpty()) {
            StatutProjet statut = StatutProjet.valueOf(statutStr);
            projets = projets.stream()
                .filter(p -> p.getStatut() == statut)
                .collect(java.util.stream.Collectors.toList());
            logger.debug("Filtrage par statut '{}': {} projets restants", statut, projets.size());
        }
        
        // Filtre par département (si spécifié dans les paramètres)
        if (departementIdStr != null && !departementIdStr.isEmpty()) {
            Long departementId = Long.parseLong(departementIdStr);
            projets = projets.stream()
                .filter(p -> p.getDepartement() != null && p.getDepartement().getId().equals(departementId))
                .collect(java.util.stream.Collectors.toList());
            logger.debug("Filtrage par département ID '{}': {} projets restants", departementId, projets.size());
        }
        
        // Filtre par employé (si spécifié dans les paramètres)
        if (employeIdStr != null && !employeIdStr.isEmpty()) {
            Long employeId = Long.parseLong(employeIdStr);
            projets = projets.stream()
                .filter(p -> {
                    // Vérifier si l'employé est chef de projet
                    if (p.getChefProjet() != null && p.getChefProjet().getId().equals(employeId)) {
                        return true;
                    }
                    // Vérifier si l'employé est membre du projet
                    if (p.getEmployes() != null) {
                        return p.getEmployes().stream()
                            .anyMatch(ep -> ep.getEmploye() != null && ep.getEmploye().getId().equals(employeId));
                    }
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
            logger.debug("Filtrage par employé ID '{}': {} projets restants", employeId, projets.size());
        }
        
        // FILTRAGE BASÉ SUR LES DROITS D'ACCÈS (si aucun filtre explicite)
        // Si ni departementIdStr ni employeIdStr ne sont spécifiés, appliquer le filtrage automatique
        if (departementIdStr == null && employeIdStr == null) {
            if (utilisateur != null && utilisateur.getRole() == Utilisateur.Role.EMPLOYE) {
                if (isChefDepartement && employeIdSession != null) {
                    // Chef de département : voir projets du département OU projets où il est membre/chef
                    final Long deptId = chefDepartementId;
                    final Long empId = employeIdSession;
                    projets = projets.stream()
                        .filter(p -> {
                            // Projets du département
                            if (p.getDepartement() != null && p.getDepartement().getId().equals(deptId)) {
                                return true;
                            }
                            // Projets où il est chef de projet
                            if (p.getChefProjet() != null && p.getChefProjet().getId().equals(empId)) {
                                return true;
                            }
                            // Projets où il est membre
                            if (p.getEmployes() != null) {
                                return p.getEmployes().stream()
                                    .anyMatch(ep -> ep.getEmploye() != null && ep.getEmploye().getId().equals(empId));
                            }
                            return false;
                        })
                        .collect(java.util.stream.Collectors.toList());
                    logger.info("Chef de département {} - Filtrage: {} projets (département {} + projets personnels)", 
                        employeIdSession, projets.size(), deptId);
                } else if (employeIdSession != null) {
                    // Employé simple : voir uniquement SES projets (membre ou chef)
                    final Long empId = employeIdSession;
                    projets = projets.stream()
                        .filter(p -> {
                            // Projets où il est chef de projet
                            if (p.getChefProjet() != null && p.getChefProjet().getId().equals(empId)) {
                                return true;
                            }
                            // Projets où il est membre
                            if (p.getEmployes() != null) {
                                return p.getEmployes().stream()
                                    .anyMatch(ep -> ep.getEmploye() != null && ep.getEmploye().getId().equals(empId));
                            }
                            return false;
                        })
                        .collect(java.util.stream.Collectors.toList());
                    logger.info("Employé {} - Filtrage: {} projets personnels uniquement", employeIdSession, projets.size());
                }
            }
            // RH et ADMIN voient tous les projets (pas de filtre)
        }
        
        logger.info("Résultat final: {} projets affichés", projets.size());
        
        List<Departement> departements = departementDAO.findActifs();
        logger.info("Nombre de départements actifs chargés: {}", departements != null ? departements.size() : 0);
        if (departements != null && !departements.isEmpty()) {
            logger.info("Premier département: {} (ID: {})", departements.get(0).getNom(), departements.get(0).getId());
        }
        
        request.setAttribute("projets", projets);
        request.setAttribute("departements", departements);
        request.setAttribute("statutsProjets", StatutProjet.values());
        request.setAttribute("currentSearch", search);
        request.setAttribute("currentStatut", statutStr);
        request.setAttribute("currentDepartement", departementIdStr);
        request.setAttribute("currentEmploye", employeIdStr);
        
        // Si filtré par employé, charger les infos de l'employé pour affichage
        if (employeIdStr != null && !employeIdStr.isEmpty()) {
            Employe employe = employeDAO.findById(Long.parseLong(employeIdStr));
            request.setAttribute("employeFiltre", employe);
        }
        
        request.getRequestDispatcher("/WEB-INF/jsp/projets/list.jsp").forward(request, response);
    }
    
    private void showProjet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.parseLong(request.getParameter("id"));
        Projet projet = projetDAO.findById(id);
        
        if (projet == null) {
            request.setAttribute("error", "Projet non trouvé");
            listProjets(request, response);
            return;
        }
        
        request.setAttribute("projet", projet);
        request.getRequestDispatcher("/WEB-INF/jsp/projets/show.jsp").forward(request, response);
    }
    
    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Vérifier si c'est un CHEF_DEPT : il ne peut créer que dans son département
        Utilisateur utilisateur = (Utilisateur) request.getSession().getAttribute("utilisateur");
        if (utilisateur != null && utilisateur.getRole() == Utilisateur.Role.CHEF_DEPT) {
            Employe employe = utilisateur.getEmploye();
            if (employe != null && employe.getDepartement() != null) {
                // Initialiser le département pour éviter LazyInitializationException
                Departement deptChef = employe.getDepartement();
                deptChef.getId(); // Force l'initialisation
                
                // Marquer que le département est fixé pour ce chef
                request.setAttribute("departementFixe", true);
                request.setAttribute("departementFixeId", deptChef.getId());
            }
        }
        
        List<Departement> departements = departementDAO.findActifs();
        List<Employe> chefsProjet = employeDAO.findPotentialManagers();
        List<Employe> employes = employeDAO.findActifs();
        
        request.setAttribute("departements", departements);
        request.setAttribute("chefsProjet", chefsProjet);
        request.setAttribute("employes", employes);
        request.setAttribute("statutsProjets", StatutProjet.values());
        request.setAttribute("prioritesProjets", PrioriteProjet.values());
        
        request.getRequestDispatcher("/WEB-INF/jsp/projets/form.jsp").forward(request, response);
    }
    
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.parseLong(request.getParameter("id"));
        Projet projet = projetDAO.findById(id);
        
        if (projet == null) {
            request.setAttribute("error", "Projet non trouvé");
            listProjets(request, response);
            return;
        }
        
        // Contrôle de sécurité : vérifier les permissions de modification
        Utilisateur utilisateur = (Utilisateur) request.getSession().getAttribute("utilisateur");
        if (utilisateur == null) {
            request.setAttribute("error", "Vous devez être connecté pour modifier un projet");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Utilisateur.Role role = utilisateur.getRole();
        
        // ADMIN et RH peuvent modifier tous les projets
        boolean isAdminOrRH = (role == Utilisateur.Role.ADMIN || role == Utilisateur.Role.RH);
        
        if (!isAdminOrRH) {
            // CHEF_PROJET ne peut JAMAIS modifier de projet
            if (role == Utilisateur.Role.CHEF_PROJET) {
                request.setAttribute("error", "Les chefs de projet ne peuvent pas modifier les projets. Contactez un administrateur ou RH.");
                listProjets(request, response);
                return;
            }
            
            // CHEF_DEPT peut modifier UNIQUEMENT les projets de son département
            if (role == Utilisateur.Role.CHEF_DEPT) {
                Employe employe = utilisateur.getEmploye();
                if (employe == null || employe.getDepartement() == null) {
                    request.setAttribute("error", "Vous n'êtes affecté à aucun département");
                    listProjets(request, response);
                    return;
                }
                
                // Initialiser le département pour éviter LazyInitializationException
                Departement deptChef = employe.getDepartement();
                if (deptChef != null) {
                    deptChef.getId(); // Force l'initialisation
                }
                
                // Vérifier que le projet appartient au département du chef
                if (projet.getDepartement() == null || 
                    !projet.getDepartement().getId().equals(deptChef.getId())) {
                    request.setAttribute("error", "Vous ne pouvez modifier que les projets de votre département");
                    listProjets(request, response);
                    return;
                }
                
                // Marquer que le département est fixé pour ce chef
                request.setAttribute("departementFixe", true);
                request.setAttribute("departementFixeId", deptChef.getId());
                
                logger.info("Chef de département {} autorisé à modifier le projet {} de son département", 
                    employe.getNom(), projet.getNom());
            } else {
                // EMPLOYE et autres rôles : accès refusé
                request.setAttribute("error", "Vous n'avez pas les permissions pour modifier ce projet");
                listProjets(request, response);
                return;
            }
        }
        
        List<Departement> departements = departementDAO.findActifs();
        List<Employe> chefsProjet = employeDAO.findPotentialManagers();
        List<Employe> employes = employeDAO.findActifs();
        
        request.setAttribute("projet", projet);
        request.setAttribute("departements", departements);
        request.setAttribute("chefsProjet", chefsProjet);
        request.setAttribute("employes", employes);
        request.setAttribute("statutsProjets", StatutProjet.values());
        request.setAttribute("prioritesProjets", PrioriteProjet.values());
        request.setAttribute("isEdit", true);
        
        request.getRequestDispatcher("/WEB-INF/jsp/projets/form.jsp").forward(request, response);
    }
    
    private void createProjet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Contrôle de sécurité pour CHEF_DEPT : ne peut créer que dans son département
        Utilisateur utilisateur = (Utilisateur) request.getSession().getAttribute("utilisateur");
        if (utilisateur != null && utilisateur.getRole() == Utilisateur.Role.CHEF_DEPT) {
            Employe employe = utilisateur.getEmploye();
            if (employe != null && employe.getDepartement() != null) {
                Long departementIdForm = Long.parseLong(request.getParameter("departementId"));
                Long departementIdChef = employe.getDepartement().getId();
                
                if (!departementIdChef.equals(departementIdForm)) {
                    request.setAttribute("error", "Vous ne pouvez créer des projets que dans votre département");
                    showAddForm(request, response);
                    return;
                }
            }
        }

        Projet projet;
        try {
            // 1️⃣ Création et sauvegarde du projet
            projet = extractProjetFromRequest(request);
            
            // Vérifier si un projet avec le même nom existe déjà
            List<Projet> existingProjets = projetDAO.findByNom(projet.getNom());
            if (existingProjets != null && !existingProjets.isEmpty()) {
                request.setAttribute("error", "Un projet avec le nom '" + projet.getNom() + "' existe déjà");
                showAddForm(request, response);
                return;
            }
            
            projetDAO.save(projet);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du projet", e);
            request.setAttribute("error", "Erreur lors de la création: " + e.getMessage());
            showAddForm(request, response);
            return; // très important : on sort de la méthode
        }

        // 2️⃣ Notification : on essaie, mais si ça plante, on ne bloque pas l'utilisateur
        try {
            Utilisateur utilisateurConnecte = (Utilisateur) request.getSession().getAttribute("utilisateur");
            String auteur = (utilisateurConnecte != null) ? utilisateurConnecte.getUsername() : "Système";

            int projId = projet.getId().intValue();
            String message = "Le projet \"" + projet.getNom()
                    + "\" a été créé par " + auteur + ".";

            notificationDAO.creerNotificationPourProjet(
                    projId,
                    message,
                    "INFO"
            );
        } catch (Exception eNotif) {
            logger.warn("Projet créé mais erreur lors de la notification: {}", eNotif.getMessage(), eNotif);
            // on ne met PAS d'error dans la requête, c'est juste une alerte pour les logs
        }

        // 3️⃣ Redirection OK
        response.sendRedirect(request.getContextPath() + "/app/projets");
    }

    
    private void updateProjet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            Projet projet = projetDAO.findById(id);
            
            if (projet == null) {
                request.setAttribute("error", "Projet non trouvé");
                listProjets(request, response);
                return;
            }
            
            // Contrôle de sécurité : vérifier les permissions de modification
            Utilisateur utilisateur = (Utilisateur) request.getSession().getAttribute("utilisateur");
            if (utilisateur == null) {
                request.setAttribute("error", "Vous devez être connecté pour modifier un projet");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            
            Utilisateur.Role role = utilisateur.getRole();
            
            // ADMIN et RH peuvent modifier tous les projets
            boolean isAdminOrRH = (role == Utilisateur.Role.ADMIN || role == Utilisateur.Role.RH);
            
            if (!isAdminOrRH) {
                // CHEF_PROJET ne peut JAMAIS modifier de projet
                if (role == Utilisateur.Role.CHEF_PROJET) {
                    request.setAttribute("error", "Les chefs de projet ne peuvent pas modifier les projets");
                    listProjets(request, response);
                    return;
                }
                
                // CHEF_DEPT peut modifier UNIQUEMENT les projets de son département
                if (role == Utilisateur.Role.CHEF_DEPT) {
                    Employe employe = utilisateur.getEmploye();
                    if (employe == null || employe.getDepartement() == null) {
                        request.setAttribute("error", "Vous n'êtes affecté à aucun département");
                        listProjets(request, response);
                        return;
                    }
                    
                    // Vérifier que le projet appartient au département du chef
                    if (projet.getDepartement() == null || 
                        !projet.getDepartement().getId().equals(employe.getDepartement().getId())) {
                        request.setAttribute("error", "Vous ne pouvez modifier que les projets de votre département");
                        listProjets(request, response);
                        return;
                    }
                } else {
                    // EMPLOYE et autres rôles : accès refusé
                    request.setAttribute("error", "Vous n'avez pas les permissions pour modifier ce projet");
                    listProjets(request, response);
                    return;
                }
            }
            
            // Contrôle supplémentaire pour CHEF_DEPT : ne peut pas changer le département
            if (utilisateur.getRole() == Utilisateur.Role.CHEF_DEPT) {
                Long nouveauDepartementId = Long.parseLong(request.getParameter("departementId"));
                Long departementActuelId = projet.getDepartement().getId();
                
                if (!nouveauDepartementId.equals(departementActuelId)) {
                    request.setAttribute("error", "Vous ne pouvez pas changer le département d'un projet");
                    showEditForm(request, response);
                    return;
                }
            }
            
            // Récupérer le nouveau nom avant la mise à jour
            String nouveauNom = request.getParameter("nom");
            
            // Vérifier si le nom a changé et si le nouveau nom existe déjà
            if (!projet.getNom().equals(nouveauNom)) {
                List<Projet> existingProjets = projetDAO.findByNom(nouveauNom);
                if (existingProjets != null && !existingProjets.isEmpty()) {
                    // Vérifier que ce n'est pas le même projet
                    boolean isDuplicate = false;
                    for (Projet p : existingProjets) {
                        if (!p.getId().equals(id)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (isDuplicate) {
                        request.setAttribute("error", "Un projet avec le nom '" + nouveauNom + "' existe déjà");
                        showEditForm(request, response);
                        return;
                    }
                }
            }
            
            updateProjetFromRequest(projet, request);
            projetDAO.update(projet);
            
            Utilisateur utilisateurConnecte = (Utilisateur) request.getSession().getAttribute("utilisateur");
            String auteur = (utilisateurConnecte != null) ? utilisateurConnecte.getUsername() : "Système";

            int projId = projet.getId().intValue();
            String message = "Le projet \"" + projet.getNom()
                    + "\" a été modifié par " + auteur + ".";

            notificationDAO.creerNotificationPourProjet(
                    projId,
                    message,
                    "INFO"
            );
            
            request.setAttribute("success", "Projet modifié avec succès");
            response.sendRedirect(request.getContextPath() + "/app/projets");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la modification du projet", e);
            request.setAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            showEditForm(request, response);
        }
    }
    
    private void deleteProjet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            Projet projet = projetDAO.findById(id);
            
            if (projet == null) {
                request.setAttribute("error", "Projet non trouvé");
                listProjets(request, response);
                return;
            }
            
            // Marquer comme annulé au lieu de supprimer
            projet.setStatut(StatutProjet.ANNULE);
            projetDAO.update(projet);
            
            Utilisateur utilisateurConnecte = (Utilisateur) request.getSession().getAttribute("utilisateur");
            String auteur = (utilisateurConnecte != null) ? utilisateurConnecte.getUsername() : "Système";

            int projId = projet.getId().intValue();
            String message = "Le projet \"" + projet.getNom()
                    + "\" a été annulé par " + auteur + ".";

            notificationDAO.creerNotificationPourProjet(
                    projId,
                    message,
                    "WARN"
            );

            
            request.setAttribute("success", "Projet annulé avec succès");
            response.sendRedirect(request.getContextPath() + "/app/projets");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'annulation du projet", e);
            request.setAttribute("error", "Erreur lors de l'annulation: " + e.getMessage());
            listProjets(request, response);
        }
    }
    
    private Projet extractProjetFromRequest(HttpServletRequest request) {
        Projet projet = new Projet();
        updateProjetFromRequest(projet, request);
        return projet;
    }
    
    private void updateProjetFromRequest(Projet projet, HttpServletRequest request) {
        projet.setNom(request.getParameter("nom"));
        projet.setDescription(request.getParameter("description"));
        
        // Dates
        String dateDebutStr = request.getParameter("dateDebut");
        if (dateDebutStr != null && !dateDebutStr.isEmpty()) {
            try {
                projet.setDateDebut(LocalDate.parse(dateDebutStr));
            } catch (DateTimeParseException e) {
                logger.warn("Format de date de début invalide: {}", dateDebutStr);
            }
        }
        
        String dateFinPrevueStr = request.getParameter("dateFinPrevue");
        if (dateFinPrevueStr != null && !dateFinPrevueStr.isEmpty()) {
            try {
                projet.setDateFinPrevue(LocalDate.parse(dateFinPrevueStr));
            } catch (DateTimeParseException e) {
                logger.warn("Format de date de fin prévue invalide: {}", dateFinPrevueStr);
            }
        }
        
        String dateFinReelleStr = request.getParameter("dateFinReelle");
        if (dateFinReelleStr != null && !dateFinReelleStr.isEmpty()) {
            try {
                projet.setDateFinReelle(LocalDate.parse(dateFinReelleStr));
            } catch (DateTimeParseException e) {
                logger.warn("Format de date de fin réelle invalide: {}", dateFinReelleStr);
            }
        }
        
        // Budget
        String budgetStr = request.getParameter("budget");
        if (budgetStr != null && !budgetStr.isEmpty()) {
            try {
                projet.setBudget(new BigDecimal(budgetStr));
            } catch (NumberFormatException e) {
                logger.warn("Format de budget invalide: {}", budgetStr);
            }
        }
        
        // Statut
        String statutStr = request.getParameter("statut");
        if (statutStr != null && !statutStr.isEmpty()) {
            projet.setStatut(StatutProjet.valueOf(statutStr));
        }
        
        // Priorité
        String prioriteStr = request.getParameter("priorite");
        if (prioriteStr != null && !prioriteStr.isEmpty()) {
            projet.setPriorite(PrioriteProjet.valueOf(prioriteStr));
        }
        
        // Chef de projet
        String chefProjetIdStr = request.getParameter("chefProjetId");
        if (chefProjetIdStr != null && !chefProjetIdStr.isEmpty()) {
            Long chefProjetId = Long.parseLong(chefProjetIdStr);
            Employe chefProjet = employeDAO.findById(chefProjetId);
            projet.setChefProjet(chefProjet);
        }
        
        // Département
        String departementIdStr = request.getParameter("departementId");
        if (departementIdStr != null && !departementIdStr.isEmpty()) {
            Long departementId = Long.parseLong(departementIdStr);
            Departement departement = departementDAO.findById(departementId);
            projet.setDepartement(departement);
        }
        
        // IMPORTANT : Gestion des membres du projet
        String[] employeIds = request.getParameterValues("employeIds");
        
        // Convertir les IDs sélectionnés en Set pour faciliter la comparaison
        Set<Long> selectedEmployeIds = new HashSet<>();
        if (employeIds != null) {
            for (String empIdStr : employeIds) {
                try {
                    selectedEmployeIds.add(Long.parseLong(empIdStr));
                } catch (NumberFormatException e) {
                    logger.warn("ID employé invalide: {}", empIdStr);
                }
            }
        }
        
        // Obtenir les IDs des membres actuels
        Set<Long> currentEmployeIds = new HashSet<>();
        if (projet.getEmployes() != null) {
            for (EmployeProjet ep : projet.getEmployes()) {
                if (ep.getEmploye() != null) {
                    currentEmployeIds.add(ep.getEmploye().getId());
                }
            }
        }
        
        // Supprimer les membres qui ne sont plus sélectionnés
        if (projet.getEmployes() != null) {
            projet.getEmployes().removeIf(ep -> 
                ep.getEmploye() != null && !selectedEmployeIds.contains(ep.getEmploye().getId())
            );
        }
        
        // Ajouter les nouveaux membres (ceux qui ne sont pas déjà présents)
        for (Long empId : selectedEmployeIds) {
            if (!currentEmployeIds.contains(empId)) {
                Employe employe = employeDAO.findById(empId);
                
                if (employe != null) {
                    // Créer une nouvelle association EmployeProjet
                    EmployeProjet employeProjet = new EmployeProjet();
                    employeProjet.setEmploye(employe);
                    employeProjet.setProjet(projet);
                    employeProjet.setDateAffectation(LocalDate.now());
                    employeProjet.setRoleProjet("Membre"); // Rôle par défaut
                    
                    // Ajouter à la collection
                    if (projet.getEmployes() == null) {
                        projet.setEmployes(new HashSet<>());
                    }
                    projet.getEmployes().add(employeProjet);
                    
                    logger.debug("Ajout de l'employé {} au projet {}", employe.getNom(), projet.getNom());
                }
            }
        }
        
        logger.info("Projet {} mis à jour avec {} membres", projet.getNom(), 
                    projet.getEmployes() != null ? projet.getEmployes().size() : 0);
    }
}
