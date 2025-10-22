package com.gestionrh.servlet;

import com.gestionrh.dao.impl.ProjetDAOImpl;
import com.gestionrh.dao.impl.DepartementDAOImpl;
import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.dao.ProjetDAO;
import com.gestionrh.dao.DepartementDAO;
import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.model.Projet;
import com.gestionrh.model.Departement;
import com.gestionrh.model.Employe;
import com.gestionrh.model.EmployeProjet;
import com.gestionrh.model.StatutProjet;
import com.gestionrh.model.PrioriteProjet;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/app/projets")
public class ProjetServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjetServlet.class);
    private ProjetDAO projetDAO;
    private DepartementDAO departementDAO;
    private EmployeDAO employeDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.projetDAO = new ProjetDAOImpl();
        this.departementDAO = new DepartementDAOImpl();
        this.employeDAO = new EmployeDAOImpl();
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
        
        if (departementIdStr != null && !departementIdStr.isEmpty()) {
            Long departementId = Long.parseLong(departementIdStr);
            projets = projets.stream()
                .filter(p -> p.getDepartement() != null && p.getDepartement().getId().equals(departementId))
                .collect(java.util.stream.Collectors.toList());
            logger.debug("Filtrage par département ID '{}': {} projets restants", departementId, projets.size());
        }
        
        // Filtre par employé
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
        
        try {
            Projet projet = extractProjetFromRequest(request);
            projetDAO.save(projet);
            
            request.setAttribute("success", "Projet créé avec succès");
            response.sendRedirect(request.getContextPath() + "/app/projets");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création du projet", e);
            request.setAttribute("error", "Erreur lors de la création: " + e.getMessage());
            showAddForm(request, response);
        }
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
            
            updateProjetFromRequest(projet, request);
            projetDAO.update(projet);
            
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
