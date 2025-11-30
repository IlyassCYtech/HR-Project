package com.gestionrh.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.DepartementDAO;
import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.dao.UtilisateurDAO;
import com.gestionrh.dao.impl.DepartementDAOImpl;
import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.dao.impl.UtilisateurDAOImpl;
import com.gestionrh.model.Departement;
import com.gestionrh.model.Employe;
import com.gestionrh.model.GeneratedCredential;
import com.gestionrh.model.Grade;
import com.gestionrh.model.StatutEmploye;
import com.gestionrh.model.Utilisateur;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/app/employes")
public class EmployeServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeServlet.class);
    private EmployeDAO employeDAO;
    private DepartementDAO departementDAO;
    private UtilisateurDAO utilisateurDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.employeDAO = new EmployeDAOImpl();
        this.departementDAO = new DepartementDAOImpl();
        this.utilisateurDAO = new UtilisateurDAOImpl();
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
                case "generateCredentials":
                    showGenerateCredentialsPage(request, response);
                    break;
                case "exportCredentialsPDF":
                    exportCredentialsPDF(request, response);
                    break;
                case "exportCredentialsZIP":
                    exportCredentialsZIP(request, response);
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
                case "generateCredential":
                    generateSingleCredential(request, response);
                    break;
                case "generateAllCredentials":
                    generateAllCredentials(request, response);
                    break;
                case "resetPassword":
                    resetPasswordForEmployee(request, response);
                    break;
                case "resetAllPasswords":
                    resetAllPasswords(request, response);
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
        
        // Récupérer l'utilisateur associé à cet employé, s'il existe
        Utilisateur utilisateurAssocie = utilisateurDAO.findByEmployeId(id);
        
        // Vérifier si l'utilisateur connecté est chef du département de cet employé
        Long employeIdSession = (Long) request.getSession().getAttribute("employeId");
        boolean isChefOfEmployeeDept = false;
        
        if (employeIdSession != null && employe.getDepartement() != null) {
            Departement dept = employe.getDepartement();
            if (dept.getChefDepartement() != null && 
                dept.getChefDepartement().getId().equals(employeIdSession)) {
                isChefOfEmployeeDept = true;
                logger.info("Utilisateur {} est chef du département de l'employé consulté", employeIdSession);
            }
        }
        
        request.setAttribute("employe", employe);
        request.setAttribute("utilisateurAssocie", utilisateurAssocie);
        request.setAttribute("isChefOfEmployeeDept", isChefOfEmployeeDept);
        request.getRequestDispatcher("/WEB-INF/jsp/employes/show.jsp").forward(request, response);
    }
    
    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Vérifier que l'utilisateur est ADMIN ou RH
        HttpSession session = request.getSession(false);
        Utilisateur utilisateur = (session != null) ? (Utilisateur) session.getAttribute("utilisateur") : null;
        
        if (utilisateur == null || 
            (utilisateur.getRole() != Utilisateur.Role.ADMIN && 
             utilisateur.getRole() != Utilisateur.Role.RH)) {
            request.setAttribute("error", "Accès refusé : seuls les administrateurs et RH peuvent créer des employés");
            response.sendRedirect(request.getContextPath() + "/app/employes");
            return;
        }
        
        List<Departement> departements = departementDAO.findActifs();
        List<Employe> managers = employeDAO.findPotentialManagers();
        
        request.setAttribute("departements", departements);
        request.setAttribute("managers", managers);
        request.setAttribute("statutsEmploye", StatutEmploye.values());
        request.setAttribute("canViewEmploye", true);
        request.setAttribute("isAdminOrRH", true);
        request.setAttribute("isSelfEdit", false);
        request.setAttribute("hasUtilisateur", false);
        
        // Générer et passer le token CSRF
        String csrfToken = generateCSRFToken(request);
        request.setAttribute("csrfToken", csrfToken);
        
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
        
        // Vérifier les permissions
        HttpSession session = request.getSession(false);
        Utilisateur utilisateur = (session != null) ? (Utilisateur) session.getAttribute("utilisateur") : null;
        
        boolean isAdminOrRH = utilisateur != null && 
            (utilisateur.getRole() == Utilisateur.Role.ADMIN || 
             utilisateur.getRole() == Utilisateur.Role.RH);
        
        boolean isSelf = utilisateur != null && utilisateur.getEmploye() != null && 
                         utilisateur.getEmploye().getId().equals(id);
        
        // Si ce n'est ni admin/RH ni l'employé lui-même, refuser l'accès
        if (!isAdminOrRH && !isSelf) {
            request.setAttribute("error", "Vous ne pouvez modifier que votre propre profil");
            response.sendRedirect(request.getContextPath() + "/app/employes");
            return;
        }
        
        // Vérifier si l'employé est chef de département (seulement pour admin/RH)
        if (isAdminOrRH) {
            List<Departement> allDepartements = departementDAO.findAll();
            for (Departement dept : allDepartements) {
                if (dept.getChefDepartement() != null && 
                    dept.getChefDepartement().getId().equals(id)) {
                    request.setAttribute("error", "❌ Impossible de modifier cet employé car il est chef du département '" + dept.getNom() + "'. Veuillez d'abord retirer son rôle de chef de département.");
                    request.setAttribute("employe", employe);
                    request.getRequestDispatcher("/WEB-INF/jsp/employes/show.jsp").forward(request, response);
                    return;
                }
            }
        }
        
        List<Departement> departements = departementDAO.findActifs();
        List<Employe> managers = employeDAO.findPotentialManagers();
        
        // Vérifier si cet employé a un compte utilisateur
        Utilisateur employeUtilisateur = utilisateurDAO.findByEmployeId(id);
        
        request.setAttribute("employe", employe);
        request.setAttribute("departements", departements);
        request.setAttribute("managers", managers);
        request.setAttribute("statutsEmploye", StatutEmploye.values());
        request.setAttribute("isEdit", true);
        
        // Attributs de sécurité
        request.setAttribute("canViewEmploye", isAdminOrRH || isSelf);
        request.setAttribute("isAdminOrRH", isAdminOrRH);
        request.setAttribute("isSelfEdit", isSelf && !isAdminOrRH);
        request.setAttribute("hasUtilisateur", employeUtilisateur != null);
        request.setAttribute("employeUtilisateur", employeUtilisateur);
        
        // Générer et passer le token CSRF
        String csrfToken = generateCSRFToken(request);
        request.setAttribute("csrfToken", csrfToken);
        
        request.getRequestDispatcher("/WEB-INF/jsp/employes/form.jsp").forward(request, response);
    }
    
    private void createEmploye(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Validation CSRF
        if (!validateCSRFToken(request)) {
            logger.error("❌ Tentative de création d'employé avec token CSRF invalide");
            request.setAttribute("error", "Token de sécurité invalide. Veuillez réessayer.");
            showAddForm(request, response);
            return;
        }
        
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
        
        // Validation CSRF
        if (!validateCSRFToken(request)) {
            logger.error("❌ Tentative de modification d'employé avec token CSRF invalide");
            request.setAttribute("error", "Token de sécurité invalide. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/app/employes");
            return;
        }
        
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
            
            // Vérifier si l'employé est chef de département
            List<Departement> allDepartements = departementDAO.findAll();
            for (Departement dept : allDepartements) {
                if (dept.getChefDepartement() != null && 
                    dept.getChefDepartement().getId().equals(id)) {
                    logger.warn("Tentative de modification d'un chef de département: {}", id);
                    request.setAttribute("error", "❌ Impossible de modifier cet employé car il est chef du département '" + dept.getNom() + "'. Veuillez d'abord retirer son rôle de chef de département.");
                    request.setAttribute("employe", employe);
                    showEditForm(request, response);
                    return;
                }
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
            
            // Vérifier les permissions pour cette modification
            HttpSession session = request.getSession(false);
            Utilisateur utilisateurConnecte = (session != null) ? (Utilisateur) session.getAttribute("utilisateur") : null;
            
            boolean isAdminOrRH = utilisateurConnecte != null && 
                (utilisateurConnecte.getRole() == Utilisateur.Role.ADMIN || 
                 utilisateurConnecte.getRole() == Utilisateur.Role.RH);
            
            boolean isSelf = utilisateurConnecte != null && utilisateurConnecte.getEmploye() != null && 
                             utilisateurConnecte.getEmploye().getId().equals(id);
            
            // Si ce n'est ni admin/RH ni l'employé lui-même, refuser
            if (!isAdminOrRH && !isSelf) {
                logger.warn("Tentative de modification non autorisée de l'employé {}", id);
                request.setAttribute("error", "Vous ne pouvez modifier que votre propre profil");
                response.sendRedirect(request.getContextPath() + "/app/employes");
                return;
            }
            
            updateEmployeFromRequest(employe, request);
            employeDAO.update(employe);
            
            // Si l'utilisateur connecté est ADMIN ou RH ET que l'employé a un compte utilisateur
            // Alors on peut mettre à jour son rôle
            if (isAdminOrRH) {
                String utilisateurRole = request.getParameter("utilisateurRole");
                if (utilisateurRole != null && !utilisateurRole.isEmpty()) {
                    // Chercher l'utilisateur associé à cet employé
                    Utilisateur employeUtilisateur = utilisateurDAO.findByEmployeId(id);
                    if (employeUtilisateur != null) {
                        try {
                            Utilisateur.Role newRole = Utilisateur.Role.valueOf(utilisateurRole);
                            employeUtilisateur.setRole(newRole);
                            utilisateurDAO.update(employeUtilisateur);
                            logger.info("Rôle de l'utilisateur mis à jour: {} -> {}", employeUtilisateur.getUsername(), newRole);
                        } catch (IllegalArgumentException e) {
                            logger.warn("Rôle invalide fourni: {}", utilisateurRole);
                        }
                    }
                }
            }
            
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
            
            // Vérifier si l'utilisateur tente de se supprimer lui-même
            Long employeIdSession = (Long) request.getSession().getAttribute("employeId");
            if (employeIdSession != null && employeIdSession.equals(id)) {
                request.setAttribute("error", "Vous ne pouvez pas vous supprimer vous-même");
                response.sendRedirect(request.getContextPath() + "/app/employes");
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
    
    private void updateEmployeFromRequest(Employe employe, HttpServletRequest request) throws IllegalArgumentException {
        // Matricule (obligatoire pour création, readonly en modification)
        String matricule = request.getParameter("matricule");
        if (matricule != null && !matricule.isEmpty()) {
            // Validation et sanitization du matricule
            matricule = sanitizeInput(matricule);
            if (!isValidMatricule(matricule)) {
                throw new IllegalArgumentException("Le matricule doit contenir entre 3 et 20 caractères alphanumériques, tirets ou underscores");
            }
            employe.setMatricule(matricule);
        }
        
        // Nom - Validation stricte
        String nom = sanitizeInput(request.getParameter("nom"));
        if (!isValidName(nom)) {
            throw new IllegalArgumentException("Le nom ne doit contenir que des lettres, espaces, tirets et apostrophes (2-50 caractères)");
        }
        employe.setNom(nom);
        
        // Prénom - Validation stricte
        String prenom = sanitizeInput(request.getParameter("prenom"));
        if (!isValidName(prenom)) {
            throw new IllegalArgumentException("Le prénom ne doit contenir que des lettres, espaces, tirets et apostrophes (2-50 caractères)");
        }
        employe.setPrenom(prenom);
        
        // Email - Validation stricte
        String email = sanitizeInput(request.getParameter("email"));
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("L'email n'est pas valide");
        }
        employe.setEmail(email.toLowerCase());
        
        // Téléphone - Validation et nettoyage
        String telephone = request.getParameter("telephone");
        if (telephone != null && !telephone.isEmpty()) {
            telephone = sanitizeInput(telephone);
            if (!isValidPhoneNumber(telephone)) {
                throw new IllegalArgumentException("Le numéro de téléphone n'est pas valide (format français attendu)");
            }
            employe.setTelephone(telephone);
        } else {
            employe.setTelephone(null);
        }
        
        // Adresse - Sanitization
        String adresse = request.getParameter("adresse");
        if (adresse != null && !adresse.isEmpty()) {
            adresse = sanitizeInput(adresse);
            if (adresse.length() > 200) {
                throw new IllegalArgumentException("L'adresse ne doit pas dépasser 200 caractères");
            }
            employe.setAdresse(adresse);
        } else {
            employe.setAdresse(null);
        }
        
        // Date de naissance
        LocalDate dateNaissance = null;
        String dateNaissanceStr = request.getParameter("dateNaissance");
        if (dateNaissanceStr != null && !dateNaissanceStr.isEmpty()) {
            try {
                dateNaissance = LocalDate.parse(dateNaissanceStr);
                employe.setDateNaissance(dateNaissance);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Format de date de naissance invalide");
            }
        }
        
        // Date d'embauche
        LocalDate dateEmbauche = null;
        String dateEmbaucheStr = request.getParameter("dateEmbauche");
        if (dateEmbaucheStr != null && !dateEmbaucheStr.isEmpty()) {
            try {
                dateEmbauche = LocalDate.parse(dateEmbaucheStr);
                employe.setDateEmbauche(dateEmbauche);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Format de date d'embauche invalide");
            }
        }
        
        // Date de fin de contrat
        LocalDate dateFin = null;
        String dateFinStr = request.getParameter("dateFin");
        if (dateFinStr != null && !dateFinStr.isEmpty()) {
            try {
                dateFin = LocalDate.parse(dateFinStr);
                employe.setDateFin(dateFin);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Format de date de fin invalide");
            }
        }
        
        // Validation de la cohérence des dates
        validateDates(dateNaissance, dateEmbauche, dateFin);
        
        // Poste - Validation et sanitization
        String poste = sanitizeInput(request.getParameter("poste"));
        if (poste == null || poste.isEmpty()) {
            throw new IllegalArgumentException("Le poste est obligatoire");
        }
        if (poste.length() < 2 || poste.length() > 100) {
            throw new IllegalArgumentException("Le poste doit contenir entre 2 et 100 caractères");
        }
        employe.setPoste(poste);
        
        // Grade
        String gradeStr = request.getParameter("grade");
        if (gradeStr != null && !gradeStr.isEmpty()) {
            try {
                employe.setGrade(Grade.valueOf(gradeStr));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Grade invalide");
            }
        }
        
        // Salaire - Validation stricte
        String salaireStr = request.getParameter("salaire");
        if (salaireStr == null || salaireStr.isEmpty()) {
            throw new IllegalArgumentException("Le salaire est obligatoire");
        }
        try {
            BigDecimal salaire = new BigDecimal(salaireStr);
            validateSalaire(salaire);
            employe.setSalaireBase(salaire);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format de salaire invalide");
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
    
    /**
     * Génère un mot de passe aléatoire sécurisé
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
    
    /**
     * Affiche la page de génération des identifiants
     */
    private void showGenerateCredentialsPage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Récupérer tous les employés
        List<Employe> allEmployes = employeDAO.findAll();
        
        // Récupérer tous les utilisateurs
        List<Utilisateur> allUtilisateurs = utilisateurDAO.findAll();
        List<Long> employeIdsWithAccount = allUtilisateurs.stream()
            .filter(u -> u.getEmploye() != null)
            .map(u -> u.getEmploye().getId())
            .collect(Collectors.toList());
        
        // Filtrer ceux qui n'ont pas de compte utilisateur
        List<Employe> employesSansCompte = allEmployes.stream()
            .filter(e -> !employeIdsWithAccount.contains(e.getId()))
            .collect(Collectors.toList());
        
        // Filtrer ceux qui ont un compte utilisateur
        List<Employe> employesAvecCompte = allEmployes.stream()
            .filter(e -> employeIdsWithAccount.contains(e.getId()))
            .collect(Collectors.toList());
        
        // Créer un Map des usernames pour les employés avec compte
        java.util.Map<Long, String> employeUsernames = new java.util.HashMap<>();
        for (Utilisateur user : allUtilisateurs) {
            if (user.getEmploye() != null) {
                employeUsernames.put(user.getEmploye().getId(), user.getUsername());
            }
        }
        
        // Récupérer les identifiants générés de la session en cours
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<GeneratedCredential> recentCredentials = 
            (List<GeneratedCredential>) session.getAttribute("generatedCredentials");
        
        // Statistiques
        request.setAttribute("totalEmployes", allEmployes.size());
        request.setAttribute("employesAvecCompte", employesAvecCompte.size());
        request.setAttribute("employesSansCompte", employesSansCompte.size());
        request.setAttribute("employesList", employesSansCompte);
        request.setAttribute("employesAvecCompteList", employesAvecCompte);
        request.setAttribute("employeUsernames", employeUsernames);
        request.setAttribute("recentCredentials", recentCredentials);
        request.setAttribute("credentialsGenerated", recentCredentials != null && !recentCredentials.isEmpty());
        
        request.getRequestDispatcher("/WEB-INF/jsp/employes/generate-credentials.jsp").forward(request, response);
    }
    
    /**
     * Génère un identifiant pour un seul employé
     */
    private void generateSingleCredential(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long employeId = Long.parseLong(request.getParameter("employeId"));
        Employe employe = employeDAO.findById(employeId);
        
        if (employe == null) {
            request.setAttribute("error", "Employé non trouvé");
            showGenerateCredentialsPage(request, response);
            return;
        }
        
        // Vérifier si un compte existe déjà
        List<Utilisateur> allUtilisateurs = utilisateurDAO.findAll();
        boolean hasAccount = allUtilisateurs.stream()
            .anyMatch(u -> u.getEmploye() != null && u.getEmploye().getId().equals(employeId));
        
        if (hasAccount) {
            request.setAttribute("error", "Cet employé a déjà un compte utilisateur");
            showGenerateCredentialsPage(request, response);
            return;
        }
        
        // Générer l'identifiant et le mot de passe
        String username = generateUsername(employe);
        String password = generateRandomPassword();
        
        // Créer l'utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername(username);
        utilisateur.setPasswordHash(password); // Le DAO doit hasher le mot de passe
        utilisateur.setRole(Utilisateur.Role.EMPLOYE);
        utilisateur.setEmploye(employe);
        
        utilisateurDAO.save(utilisateur);
        
        // Stocker les identifiants dans la session
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<GeneratedCredential> credentials = 
            (List<GeneratedCredential>) session.getAttribute("generatedCredentials");
        
        if (credentials == null) {
            credentials = new ArrayList<>();
        }
        
        String employeNom = employe.getPrenom() + " " + employe.getNom();
        credentials.add(new GeneratedCredential(employeId, employeNom, username, password));
        session.setAttribute("generatedCredentials", credentials);
        
        logger.info("Identifiant généré pour l'employé {} : {}", employeId, username);
        
        request.setAttribute("success", "Identifiant créé avec succès pour " + employeNom);
        showGenerateCredentialsPage(request, response);
    }
    
    /**
     * Génère des identifiants pour tous les employés sans compte
     */
    private void generateAllCredentials(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Employe> allEmployes = employeDAO.findAll();
        
        // Récupérer les utilisateurs existants
        List<Utilisateur> allUtilisateurs = utilisateurDAO.findAll();
        List<Long> employeIdsWithAccount = allUtilisateurs.stream()
            .filter(u -> u.getEmploye() != null)
            .map(u -> u.getEmploye().getId())
            .collect(Collectors.toList());
        
        // Filtrer les employés sans compte
        List<Employe> employesSansCompte = allEmployes.stream()
            .filter(e -> !employeIdsWithAccount.contains(e.getId()))
            .collect(Collectors.toList());
        
        if (employesSansCompte.isEmpty()) {
            request.setAttribute("error", "Aucun employé sans compte");
            showGenerateCredentialsPage(request, response);
            return;
        }
        
        HttpSession session = request.getSession();
        List<GeneratedCredential> credentials = new ArrayList<>();
        int count = 0;
        
        for (Employe employe : employesSansCompte) {
            try {
                String username = generateUsername(employe);
                String password = generateRandomPassword();
                
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setUsername(username);
                utilisateur.setPasswordHash(password);
                utilisateur.setRole(Utilisateur.Role.EMPLOYE);
                utilisateur.setEmploye(employe);
                
                utilisateurDAO.save(utilisateur);
                
                String employeNom = employe.getPrenom() + " " + employe.getNom();
                credentials.add(new GeneratedCredential(employe.getId(), employeNom, username, password));
                count++;
                
            } catch (Exception e) {
                logger.error("Erreur lors de la génération de l'identifiant pour l'employé {}: {}", 
                    employe.getId(), e.getMessage());
            }
        }
        
        session.setAttribute("generatedCredentials", credentials);
        logger.info("{} identifiants générés en masse", count);
        
        request.setAttribute("success", count + " identifiant(s) créé(s) avec succès");
        showGenerateCredentialsPage(request, response);
    }
    
    /**
     * Génère un nom d'utilisateur basé sur le nom de l'employé
     */
    private String generateUsername(Employe employe) {
        String base = (employe.getPrenom().charAt(0) + employe.getNom()).toLowerCase()
            .replaceAll("[^a-z0-9]", "");
        
        // Vérifier l'unicité
        String username = base;
        int suffix = 1;
        while (utilisateurDAO.findByUsername(username) != null) {
            username = base + suffix;
            suffix++;
        }
        
        return username;
    }
    
    /**
     * Exporte les identifiants générés en PDF
     */
    private void exportCredentialsPDF(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<GeneratedCredential> credentials = 
            (List<GeneratedCredential>) session.getAttribute("generatedCredentials");
        
        if (credentials == null || credentials.isEmpty()) {
            request.setAttribute("error", "Aucun identifiant à exporter");
            showGenerateCredentialsPage(request, response);
            return;
        }
        
        try {
            // Configuration de la réponse
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", 
                "attachment; filename=\"identifiants_employes.pdf\"");
            
            OutputStream os = response.getOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, os);
            
            document.open();
            
            // Couleurs
            BaseColor darkBlue = new BaseColor(31, 56, 100);
            BaseColor gold = new BaseColor(197, 165, 114);
            
            // Polices
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, darkBlue);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            Font codeFont = FontFactory.getFont(FontFactory.COURIER, 10, BaseColor.BLACK);
            
            // Titre
            Paragraph title = new Paragraph("IDENTIFIANTS EMPLOYÉS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);
            
            Paragraph subtitle = new Paragraph("Document confidentiel - À distribuer aux employés", normalFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);
            
            // Tableau
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2f, 2.5f, 1.5f});
            table.setSpacingBefore(10);
            
            // En-tête
            addTableHeader(table, "EMPLOYÉ", headerFont, darkBlue);
            addTableHeader(table, "MATRICULE", headerFont, darkBlue);
            addTableHeader(table, "IDENTIFIANT", headerFont, gold);
            addTableHeader(table, "MOT DE PASSE", headerFont, gold);
            
            // Données
            for (GeneratedCredential cred : credentials) {
                // Récupérer l'employé pour avoir le matricule
                Employe emp = employeDAO.findById(cred.getEmployeId());
                
                addTableCell(table, cred.getEmployeNom(), boldFont);
                addTableCell(table, emp != null ? emp.getMatricule() : "-", normalFont);
                addTableCell(table, cred.getUsername(), codeFont);
                addTableCell(table, cred.getPassword(), codeFont);
            }
            
            document.add(table);
            
            // Avertissement
            Paragraph warning = new Paragraph(
                "\n⚠️ IMPORTANT : Ces identifiants doivent être changés lors de la première connexion. " +
                "Conservez ce document en lieu sûr.", 
                FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(255, 0, 0)));
            warning.setSpacingBefore(20);
            document.add(warning);
            
            document.close();
            os.flush();
            
            logger.info("PDF d'identifiants généré avec {} entrées", credentials.size());
            
        } catch (DocumentException e) {
            logger.error("Erreur lors de la génération du PDF: {}", e.getMessage(), e);
            throw new ServletException("Erreur lors de la génération du PDF", e);
        }
    }
    
    private void addTableHeader(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }
    
    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(cell);
    }
    
    /**
     * Réinitialise le mot de passe d'un employé existant
     */
    private void resetPasswordForEmployee(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long employeId = Long.parseLong(request.getParameter("employeId"));
        Employe employe = employeDAO.findById(employeId);
        
        if (employe == null) {
            request.setAttribute("error", "Employé non trouvé");
            showGenerateCredentialsPage(request, response);
            return;
        }
        
        // Vérifier si un compte existe
        List<Utilisateur> allUtilisateurs = utilisateurDAO.findAll();
        Utilisateur utilisateur = allUtilisateurs.stream()
            .filter(u -> u.getEmploye() != null && u.getEmploye().getId().equals(employeId))
            .findFirst()
            .orElse(null);
        
        if (utilisateur == null) {
            request.setAttribute("error", "Cet employé n'a pas de compte utilisateur");
            showGenerateCredentialsPage(request, response);
            return;
        }
        
        // Générer un nouveau mot de passe
        String newPassword = generateRandomPassword();
        
        // Mettre à jour l'utilisateur
        utilisateur.setPasswordHash(newPassword); // Le DAO doit hasher le mot de passe
        utilisateurDAO.update(utilisateur);
        
        // Stocker les identifiants dans la session
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<GeneratedCredential> credentials = 
            (List<GeneratedCredential>) session.getAttribute("generatedCredentials");
        
        if (credentials == null) {
            credentials = new ArrayList<>();
        }
        
        String employeNom = employe.getPrenom() + " " + employe.getNom();
        credentials.add(new GeneratedCredential(employeId, employeNom, utilisateur.getUsername(), newPassword));
        session.setAttribute("generatedCredentials", credentials);
        
        logger.info("Mot de passe réinitialisé pour l'employé {} : {}", employeId, utilisateur.getUsername());
        
        request.setAttribute("success", "Mot de passe réinitialisé avec succès pour " + employeNom);
        showGenerateCredentialsPage(request, response);
    }
    
    /**
     * Réinitialise les mots de passe de tous les employés avec compte
     */
    private void resetAllPasswords(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Récupérer les utilisateurs existants
        List<Utilisateur> allUtilisateurs = utilisateurDAO.findAll();
        
        if (allUtilisateurs.isEmpty()) {
            request.setAttribute("error", "Aucun compte utilisateur à réinitialiser");
            showGenerateCredentialsPage(request, response);
            return;
        }
        
        HttpSession session = request.getSession();
        List<GeneratedCredential> credentials = new ArrayList<>();
        int count = 0;
        
        for (Utilisateur utilisateur : allUtilisateurs) {
            if (utilisateur.getEmploye() != null) {
                try {
                    Employe employe = utilisateur.getEmploye();
                    
                    // Générer un nouveau mot de passe
                    String newPassword = generateRandomPassword();
                    
                    // Mettre à jour l'utilisateur
                    utilisateur.setPasswordHash(newPassword);
                    utilisateurDAO.update(utilisateur);
                    
                    String employeNom = employe.getPrenom() + " " + employe.getNom();
                    credentials.add(new GeneratedCredential(employe.getId(), employeNom, 
                        utilisateur.getUsername(), newPassword));
                    count++;
                    
                } catch (Exception e) {
                    logger.error("Erreur lors de la réinitialisation du mot de passe pour l'utilisateur {}: {}", 
                        utilisateur.getUsername(), e.getMessage());
                }
            }
        }
        
        session.setAttribute("generatedCredentials", credentials);
        logger.info("{} mots de passe réinitialisés en masse", count);
        
        request.setAttribute("success", count + " mot(s) de passe réinitialisé(s) avec succès");
        showGenerateCredentialsPage(request, response);
    }
    
    /**
     * Exporte les identifiants en PDFs individuels dans un ZIP
     */
    private void exportCredentialsZIP(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<GeneratedCredential> credentials = 
            (List<GeneratedCredential>) session.getAttribute("generatedCredentials");
        
        if (credentials == null || credentials.isEmpty()) {
            request.setAttribute("error", "Aucun identifiant à exporter");
            showGenerateCredentialsPage(request, response);
            return;
        }
        
        try {
            // Configuration de la réponse pour un fichier ZIP
            String zipFilename = "identifiants_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".zip";
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFilename + "\"");
            
            OutputStream os = response.getOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(os);
            
            // Générer un PDF pour chaque employé
            for (GeneratedCredential cred : credentials) {
                Employe emp = employeDAO.findById(cred.getEmployeId());
                if (emp == null) continue;
                
                // Créer le nom du fichier PDF (format: identifiant_NomPrenom.pdf)
                String pdfFilename = "identifiant_" + emp.getNom() + emp.getPrenom() + ".pdf";
                pdfFilename = pdfFilename.replaceAll("[^a-zA-Z0-9._-]", "");
                
                // Créer l'entrée ZIP
                ZipEntry zipEntry = new ZipEntry(pdfFilename);
                zipOut.putNextEntry(zipEntry);
                
                // Générer le PDF individuel
                byte[] pdfBytes = generateIndividualCredentialPDF(cred, emp);
                zipOut.write(pdfBytes);
                zipOut.closeEntry();
            }
            
            zipOut.close();
            os.flush();
            
            logger.info("ZIP d'identifiants généré avec {} fichiers PDF", credentials.size());
            
        } catch (DocumentException e) {
            logger.error("Erreur lors de la génération du ZIP: {}", e.getMessage(), e);
            throw new ServletException("Erreur lors de la génération du ZIP", e);
        }
    }
    
    /**
     * Génère un PDF pour un employé individuel
     */
    private byte[] generateIndividualCredentialPDF(GeneratedCredential cred, Employe emp) 
            throws DocumentException, IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Couleurs
        BaseColor darkBlue = new BaseColor(31, 56, 100);
        BaseColor gold = new BaseColor(197, 165, 114);
        BaseColor lightGray = new BaseColor(245, 245, 245);
        
        // Polices
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, darkBlue);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, new BaseColor(100, 100, 100));
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new BaseColor(80, 80, 80));
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 14, darkBlue);
        Font codeFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 16, darkBlue);
        Font warningFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new BaseColor(255, 0, 0));
        
        // En-tête avec logo/titre
        Paragraph title = new Paragraph("IDENTIFIANT EMPLOYÉ", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        document.add(title);
        
        Paragraph subtitle = new Paragraph("Système de Gestion des Ressources Humaines", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(30);
        document.add(subtitle);
        
        // Ligne de séparation
        com.itextpdf.text.pdf.draw.LineSeparator line = 
            new com.itextpdf.text.pdf.draw.LineSeparator();
        line.setLineColor(gold);
        line.setLineWidth(2);
        document.add(new com.itextpdf.text.Chunk(line));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        
        // Informations de l'employé
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1.2f, 2f});
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(20);
        
        addInfoRow(infoTable, "MATRICULE :", emp.getMatricule(), labelFont, valueFont, lightGray);
        addInfoRow(infoTable, "NOM :", emp.getNom(), labelFont, valueFont, BaseColor.WHITE);
        addInfoRow(infoTable, "PRÉNOM :", emp.getPrenom(), labelFont, valueFont, lightGray);
        addInfoRow(infoTable, "POSTE :", emp.getPoste(), labelFont, valueFont, BaseColor.WHITE);
        if (emp.getDepartement() != null) {
            addInfoRow(infoTable, "DÉPARTEMENT :", emp.getDepartement().getNom(), labelFont, valueFont, lightGray);
        }
        
        document.add(infoTable);
        
        // Section des identifiants (mise en évidence)
        PdfPTable credTable = new PdfPTable(1);
        credTable.setWidthPercentage(100);
        credTable.setSpacingBefore(20);
        credTable.setSpacingAfter(20);
        
        PdfPCell headerCell = new PdfPCell(new Phrase("VOS IDENTIFIANTS DE CONNEXION", 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE)));
        headerCell.setBackgroundColor(gold);
        headerCell.setPadding(15);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setBorder(Rectangle.NO_BORDER);
        credTable.addCell(headerCell);
        
        // Identifiant
        PdfPCell usernameCell = new PdfPCell();
        usernameCell.setBorder(Rectangle.BOX);
        usernameCell.setBorderColor(gold);
        usernameCell.setPadding(15);
        usernameCell.setBackgroundColor(new BaseColor(250, 250, 250));
        
        Paragraph userLabel = new Paragraph("Identifiant :", labelFont);
        userLabel.setSpacingAfter(5);
        usernameCell.addElement(userLabel);
        
        Paragraph userValue = new Paragraph(cred.getUsername(), codeFont);
        usernameCell.addElement(userValue);
        credTable.addCell(usernameCell);
        
        // Mot de passe
        PdfPCell passwordCell = new PdfPCell();
        passwordCell.setBorder(Rectangle.BOX);
        passwordCell.setBorderColor(gold);
        passwordCell.setPadding(15);
        passwordCell.setBackgroundColor(new BaseColor(255, 248, 220));
        
        Paragraph passLabel = new Paragraph("Mot de passe :", labelFont);
        passLabel.setSpacingAfter(5);
        passwordCell.addElement(passLabel);
        
        Paragraph passValue = new Paragraph(cred.getPassword(), codeFont);
        passwordCell.addElement(passValue);
        credTable.addCell(passwordCell);
        
        document.add(credTable);
        
        // Instructions
        Paragraph instructions = new Paragraph();
        instructions.setSpacingBefore(30);
        instructions.add(new com.itextpdf.text.Chunk("INSTRUCTIONS :", 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, darkBlue)));
        instructions.add(new com.itextpdf.text.Chunk("\n\n", FontFactory.getFont(FontFactory.HELVETICA, 11)));
        instructions.add(new com.itextpdf.text.Chunk("1. ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK)));
        instructions.add(new com.itextpdf.text.Chunk("Connectez-vous au système avec vos identifiants ci-dessus\n", 
            FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK)));
        instructions.add(new com.itextpdf.text.Chunk("2. ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK)));
        instructions.add(new com.itextpdf.text.Chunk("Il est recommandé de changer votre mot de passe lors de la première connexion\n", 
            FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK)));
        instructions.add(new com.itextpdf.text.Chunk("3. ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK)));
        instructions.add(new com.itextpdf.text.Chunk("Conservez ce document dans un endroit sûr\n", 
            FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK)));
        document.add(instructions);
        
        // Avertissement de sécurité
        PdfPTable warningTable = new PdfPTable(1);
        warningTable.setWidthPercentage(100);
        warningTable.setSpacingBefore(30);
        
        PdfPCell warningCell = new PdfPCell();
        warningCell.setBackgroundColor(new BaseColor(255, 243, 205));
        warningCell.setBorder(Rectangle.LEFT);
        warningCell.setBorderColor(new BaseColor(255, 165, 0));
        warningCell.setBorderWidth(4);
        warningCell.setPadding(15);
        
        Paragraph warning = new Paragraph();
        warning.add(new com.itextpdf.text.Chunk("⚠ CONFIDENTIEL\n", warningFont));
        warning.add(new com.itextpdf.text.Chunk(
            "Ce document contient des informations confidentielles. " +
            "Ne partagez jamais vos identifiants avec qui que ce soit. " +
            "En cas de perte ou de vol, contactez immédiatement le service RH.",
            FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(100, 100, 100))));
        
        warningCell.addElement(warning);
        warningTable.addCell(warningCell);
        document.add(warningTable);
        
        // Pied de page
        Paragraph footer = new Paragraph(
            "\nDocument généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(150, 150, 150)));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(40);
        document.add(footer);
        
        document.close();
        
        return baos.toByteArray();
    }
    
    /**
     * Ajoute une ligne au tableau d'informations
     */
    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont, BaseColor bgColor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(bgColor);
        labelCell.setPadding(10);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBackgroundColor(bgColor);
        valueCell.setPadding(10);
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }
    
    // ===================== MÉTHODES DE VALIDATION ET SÉCURITÉ =====================
    
    /**
     * Génère un token CSRF et le stocke en session
     * @param request La requête HTTP
     * @return Le token CSRF généré
     */
    private String generateCSRFToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String token = java.util.UUID.randomUUID().toString();
        session.setAttribute("csrfToken", token);
        logger.debug("Token CSRF généré: {}", token);
        return token;
    }
    
    /**
     * Valide le token CSRF
     * @param request La requête HTTP
     * @return true si le token est valide, false sinon
     */
    private boolean validateCSRFToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            logger.warn("Session expirée lors de la validation CSRF");
            return false;
        }
        
        String sessionToken = (String) session.getAttribute("csrfToken");
        String requestToken = request.getParameter("csrfToken");
        
        if (sessionToken == null || requestToken == null) {
            logger.warn("Token CSRF manquant - Session: {}, Request: {}", 
                sessionToken != null, requestToken != null);
            return false;
        }
        
        boolean isValid = sessionToken.equals(requestToken);
        if (!isValid) {
            logger.warn("⚠️ CSRF Token invalide! Tentative d'attaque CSRF détectée");
        }
        
        return isValid;
    }
    
    /**
     * Nettoie et sanitize une chaîne de caractères pour prévenir les attaques XSS
     * @param input La chaîne à nettoyer
     * @return La chaîne nettoyée ou null
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Trim et normalisation
        String cleaned = input.trim();
        
        // Remplacer les caractères dangereux pour XSS
        cleaned = cleaned.replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\"", "&quot;")
                        .replace("'", "&#x27;")
                        .replace("/", "&#x2F;")
                        .replace("\\", "&#x5C;");
        
        // Supprimer les caractères de contrôle
        cleaned = cleaned.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        
        return cleaned.isEmpty() ? null : cleaned;
    }
    
    /**
     * Valide le format d'un nom ou prénom
     * - Uniquement lettres, espaces, tirets et apostrophes
     * - Pas de chiffres
     * - Entre 2 et 50 caractères
     * - Pas de caractères spéciaux dangereux
     * @param name Le nom à valider
     * @return true si valide, false sinon
     */
    private boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Vérifier la longueur
        if (name.length() < 2 || name.length() > 50) {
            return false;
        }
        
        // Pattern strict : lettres (avec accents), espaces, tirets, apostrophes uniquement
        // Pas de chiffres, pas de caractères spéciaux
        String namePattern = "^[a-zA-ZÀ-ÿ][a-zA-ZÀ-ÿ\\s\\-']*[a-zA-ZÀ-ÿ]$";
        if (!name.matches(namePattern)) {
            return false;
        }
        
        // Vérifier qu'il n'y a pas de séquences suspectes
        if (name.contains("--") || name.contains("''") || name.contains("  ")) {
            return false;
        }
        
        // Pas de chiffres
        if (name.matches(".*\\d.*")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Valide le format d'un matricule
     * - Alphanumérique avec tirets et underscores
     * - Entre 3 et 20 caractères
     * @param matricule Le matricule à valider
     * @return true si valide, false sinon
     */
    private boolean isValidMatricule(String matricule) {
        if (matricule == null || matricule.isEmpty()) {
            return false;
        }
        
        // Pattern : alphanumérique, tirets, underscores uniquement
        String matriculePattern = "^[A-Za-z0-9_-]{3,20}$";
        return matricule.matches(matriculePattern);
    }
    
    /**
     * Valide le format d'un email
     * - Format standard RFC 5322
     * - Longueur maximale 150 caractères
     * @param email L'email à valider
     * @return true si valide, false sinon
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        // Vérifier la longueur
        if (email.length() > 150) {
            return false;
        }
        
        // Pattern RFC 5322 simplifié mais robuste
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                             "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        
        if (!email.matches(emailPattern)) {
            return false;
        }
        
        // Vérifications supplémentaires
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        // Vérifier que le domaine n'est pas suspect
        String domain = parts[1];
        if (domain.startsWith("-") || domain.endsWith("-") || 
            domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Valide le format d'un numéro de téléphone français
     * - Formats acceptés : 06 12 34 56 78, 0612345678, +33612345678, +33 6 12 34 56 78
     * - Minimum 10 chiffres
     * @param phoneNumber Le numéro à valider
     * @return true si valide, false sinon
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        
        // Nettoyer le numéro (garder uniquement chiffres et +)
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Format français
        // - 10 chiffres commençant par 0
        // - ou 11 chiffres commençant par +33
        // - ou 12 chiffres commençant par 0033
        if (cleaned.matches("^0[1-9]\\d{8}$")) {
            return true; // 0612345678
        }
        if (cleaned.matches("^\\+33[1-9]\\d{8}$")) {
            return true; // +33612345678
        }
        if (cleaned.matches("^0033[1-9]\\d{8}$")) {
            return true; // 0033612345678
        }
        
        return false;
    }
    
    /**
     * Valide la cohérence des dates d'un employé
     * @param dateNaissance Date de naissance
     * @param dateEmbauche Date d'embauche
     * @param dateFin Date de fin de contrat
     * @throws IllegalArgumentException Si les dates sont incohérentes
     */
    private void validateDates(LocalDate dateNaissance, LocalDate dateEmbauche, LocalDate dateFin) {
        LocalDate now = LocalDate.now();
        
        // Date de naissance
        if (dateNaissance != null) {
            // Pas dans le futur
            if (dateNaissance.isAfter(now)) {
                throw new IllegalArgumentException("La date de naissance ne peut pas être dans le futur");
            }
            
            // Pas trop vieille (limite raisonnable : 100 ans)
            if (dateNaissance.isBefore(now.minusYears(100))) {
                throw new IllegalArgumentException("La date de naissance n'est pas réaliste");
            }
            
            // Au moins 18 ans à la date d'embauche
            if (dateEmbauche != null) {
                long years = java.time.temporal.ChronoUnit.YEARS.between(dateNaissance, dateEmbauche);
                if (years < 18) {
                    throw new IllegalArgumentException("L'employé doit avoir au moins 18 ans à la date d'embauche");
                }
            }
        }
        
        // Date d'embauche
        if (dateEmbauche != null) {
            // Pas trop loin dans le futur (max 1 an)
            if (dateEmbauche.isAfter(now.plusYears(1))) {
                throw new IllegalArgumentException("La date d'embauche ne peut pas être plus d'un an dans le futur");
            }
            
            // Pas trop ancienne (limite raisonnable : 70 ans)
            if (dateEmbauche.isBefore(now.minusYears(70))) {
                throw new IllegalArgumentException("La date d'embauche n'est pas réaliste");
            }
        }
        
        // Date de fin
        if (dateFin != null && dateEmbauche != null) {
            // Doit être après la date d'embauche
            if (dateFin.isBefore(dateEmbauche) || dateFin.isEqual(dateEmbauche)) {
                throw new IllegalArgumentException("La date de fin de contrat doit être postérieure à la date d'embauche");
            }
        }
    }
    
    /**
     * Valide le salaire d'un employé
     * @param salaire Le salaire à valider
     * @throws IllegalArgumentException Si le salaire est invalide
     */
    private void validateSalaire(BigDecimal salaire) {
        if (salaire == null) {
            throw new IllegalArgumentException("Le salaire est obligatoire");
        }
        
        // SMIC France (environ 1700€ brut mensuel)
        BigDecimal smicMensuel = new BigDecimal("1700.00");
        
        // Salaire maximum raisonnable (1 million d'euros par mois)
        BigDecimal salaireMax = new BigDecimal("1000000.00");
        
        if (salaire.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le salaire doit être supérieur à zéro");
        }
        
        if (salaire.compareTo(smicMensuel) < 0) {
            logger.warn("⚠️ Salaire inférieur au SMIC: {}", salaire);
            // On peut choisir de ne pas bloquer, juste logger un warning
        }
        
        if (salaire.compareTo(salaireMax) > 0) {
            throw new IllegalArgumentException("Le salaire dépasse la limite maximale acceptable");
        }
    }
}
