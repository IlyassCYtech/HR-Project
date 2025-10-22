package com.gestionrh.servlet;

import com.gestionrh.dao.impl.CongeAbsenceDAOImpl;
import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.dao.CongeAbsenceDAO;
import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.model.CongeAbsence;
import com.gestionrh.model.Employe;
import com.gestionrh.model.Utilisateur;
import com.gestionrh.model.CongeAbsence.StatutDemande;
import com.gestionrh.model.CongeAbsence.TypeConge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@WebServlet("/app/conges-absences")
public class CongeAbsenceServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(CongeAbsenceServlet.class);
    
    private CongeAbsenceDAO congeAbsenceDAO;
    private EmployeDAO employeDAO;
    
    @Override
    public void init() throws ServletException {
        congeAbsenceDAO = new CongeAbsenceDAOImpl();
        employeDAO = new EmployeDAOImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        try {
            switch (action != null ? action : "") {
                case "list":
                    listerConges(request, response);
                    break;
                case "new":
                    afficherFormulaireCreation(request, response);
                    break;
                case "edit":
                    afficherFormulaireModification(request, response);
                    break;
                case "details":
                    afficherDetails(request, response);
                    break;
                case "approve":
                    approuverConge(request, response);
                    break;
                case "reject":
                    rejeterConge(request, response);
                    break;
                case "mesConges":
                    listerMesConges(request, response);
                    break;
                case "enAttente":
                    listerCongesEnAttente(request, response);
                    break;
                default:
                    listerConges(request, response);
                    break;
            }
        } catch (Exception e) {
            logger.error("Erreur dans CongeAbsenceServlet: ", e);
            request.setAttribute("error", "Une erreur est survenue: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        try {
            switch (action != null ? action : "") {
                case "create":
                    creerConge(request, response);
                    break;
                case "update":
                    modifierConge(request, response);
                    break;
                case "delete":
                    supprimerConge(request, response);
                    break;
                case "approve":
                    approuverConge(request, response);
                    break;
                case "reject":
                    rejeterConge(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/app/conges-absences");
                    break;
            }
        } catch (Exception e) {
            logger.error("Erreur dans CongeAbsenceServlet: ", e);
            request.setAttribute("error", "Une erreur est survenue: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }
    
    private void listerConges(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String typeFilter = request.getParameter("type");
        String statutFilter = request.getParameter("statut");
        String employeIdStr = request.getParameter("employeId");
        
        List<CongeAbsence> conges;
        
        if (employeIdStr != null && !employeIdStr.isEmpty()) {
            Long employeId = Long.valueOf(employeIdStr);
            conges = congeAbsenceDAO.findByEmployeId(employeId);
        } else {
            conges = congeAbsenceDAO.findAll();
        }
        
        // Filtrage par type
        if (typeFilter != null && !typeFilter.isEmpty() && !typeFilter.equals("TOUS")) {
            try {
                TypeConge type = TypeConge.valueOf(typeFilter);
                conges = conges.stream()
                    .filter(c -> c.getTypeConge() == type)
                    .collect(java.util.stream.Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Valeur d'enum invalide, ignorer le filtre
                logger.warn("Valeur de TypeConge invalide: " + typeFilter);
            }
        }
        
        // Filtrage par statut
        if (statutFilter != null && !statutFilter.isEmpty() && !statutFilter.equals("TOUS")) {
            try {
                StatutDemande statut = StatutDemande.valueOf(statutFilter);
                conges = conges.stream()
                    .filter(c -> c.getStatut() == statut)
                    .collect(java.util.stream.Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Valeur d'enum invalide, ignorer le filtre
                logger.warn("Valeur de StatutDemande invalide: " + statutFilter);
            }
        }
        
        List<Employe> employes = employeDAO.findAll();
        
        request.setAttribute("conges", conges);
        request.setAttribute("employes", employes);
        request.setAttribute("typesConge", TypeConge.values());
        request.setAttribute("statutsConge", StatutDemande.values());
        request.setAttribute("typeFilter", typeFilter);
        request.setAttribute("statutFilter", statutFilter);
        request.setAttribute("employeIdFilter", employeIdStr);
        
        request.getRequestDispatcher("/WEB-INF/jsp/conges/list.jsp").forward(request, response);
    }
    
    private void listerMesConges(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Employe employeConnecte = (Employe) session.getAttribute("employe");
        
        if (employeConnecte == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        List<CongeAbsence> mesConges = congeAbsenceDAO.findByEmployeId(employeConnecte.getId());
        
        request.setAttribute("mesConges", mesConges);
        request.setAttribute("typesConge", TypeConge.values());
        request.setAttribute("statutsConge", StatutDemande.values());
        
        request.getRequestDispatcher("/WEB-INF/jsp/conges/mes-conges.jsp").forward(request, response);
    }
    
    private void listerCongesEnAttente(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<CongeAbsence> congesEnAttente = congeAbsenceDAO.findEnAttente();
        
        request.setAttribute("congesEnAttente", congesEnAttente);
        
        request.getRequestDispatcher("/WEB-INF/jsp/conges/en-attente.jsp").forward(request, response);
    }
    
    private void afficherFormulaireCreation(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Employe> employes = employeDAO.findAll();
        
        request.setAttribute("employes", employes);
        request.setAttribute("typesConge", TypeConge.values());
        
        request.getRequestDispatcher("/WEB-INF/jsp/conges/form.jsp").forward(request, response);
    }
    
    private void afficherFormulaireModification(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.valueOf(request.getParameter("id"));
        CongeAbsence conge = congeAbsenceDAO.findById(id);
        
        if (conge == null) {
            request.setAttribute("error", "Congé non trouvé");
            listerConges(request, response);
            return;
        }
        
        List<Employe> employes = employeDAO.findAll();
        
        request.setAttribute("conge", conge);
        request.setAttribute("employes", employes);
        request.setAttribute("typesConge", TypeConge.values());
        request.setAttribute("statutsConge", StatutDemande.values());
        
        request.getRequestDispatcher("/WEB-INF/jsp/conges/form.jsp").forward(request, response);
    }
    
    private void afficherDetails(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.valueOf(request.getParameter("id"));
        CongeAbsence conge = congeAbsenceDAO.findById(id);
        
        if (conge == null) {
            request.setAttribute("error", "Congé non trouvé");
            listerConges(request, response);
            return;
        }
        
        // DEBUG: Vérifier si le validateur est chargé
        logger.info("DEBUG - Congé ID: {}, Statut: {}", conge.getId(), conge.getStatut());
        if (conge.getApprouvePar() != null) {
            logger.info("DEBUG - Validateur chargé: {} {} (ID: {})", 
                conge.getApprouvePar().getPrenom(), 
                conge.getApprouvePar().getNom(), 
                conge.getApprouvePar().getId());
        } else {
            logger.warn("DEBUG - Aucun validateur chargé pour le congé ID: {}", conge.getId());
        }
        
        // Formater la date d'approbation pour le JSP (LocalDateTime -> String)
        if (conge.getDateApprobation() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dateApprobationFormatee = conge.getDateApprobation().format(formatter);
            request.setAttribute("dateApprobationFormatee", dateApprobationFormatee);
        }
        
        request.setAttribute("conge", conge);
        
        request.getRequestDispatcher("/WEB-INF/jsp/conges/show.jsp").forward(request, response);
    }
    
    private void creerConge(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            CongeAbsence conge = construireCongeDepuisParametres(request);
            
            // Validation des dates
            if (conge.getDateDebut().isAfter(conge.getDateFin())) {
                request.setAttribute("error", "La date de début doit être antérieure à la date de fin");
                afficherFormulaireCreation(request, response);
                return;
            }
            
            // Vérification des conflits de dates
            if (verifierConflitDates(conge)) {
                request.setAttribute("error", "Ces dates entrent en conflit avec un autre congé");
                afficherFormulaireCreation(request, response);
                return;
            }
            
            conge.setStatut(StatutDemande.EN_ATTENTE);
            conge.setDateDemande(LocalDateTime.now());
            
            congeAbsenceDAO.save(conge);
            
            request.setAttribute("success", "Demande de congé créée avec succès");
            response.sendRedirect(request.getContextPath() + "/app/conges-absences?action=list");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création du congé: ", e);
            request.setAttribute("error", "Erreur lors de la création: " + e.getMessage());
            afficherFormulaireCreation(request, response);
        }
    }
    
    private void modifierConge(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            CongeAbsence congeExistant = congeAbsenceDAO.findById(id);
            
            if (congeExistant == null) {
                request.setAttribute("error", "Congé non trouvé");
                listerConges(request, response);
                return;
            }
            
            // Mise à jour des informations
            congeExistant.setTypeConge(TypeConge.valueOf(request.getParameter("type")));
            congeExistant.setDateDebut(LocalDate.parse(request.getParameter("dateDebut"), 
                DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            congeExistant.setDateFin(LocalDate.parse(request.getParameter("dateFin"), 
                DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            congeExistant.setMotif(request.getParameter("motif"));
            
            String statutParam = request.getParameter("statut");
            if (statutParam != null && !statutParam.isEmpty()) {
                congeExistant.setStatut(StatutDemande.valueOf(statutParam));
            }
            
            // Validation des dates
            if (congeExistant.getDateDebut().isAfter(congeExistant.getDateFin())) {
                request.setAttribute("error", "La date de début doit être antérieure à la date de fin");
                afficherFormulaireModification(request, response);
                return;
            }
            
            congeAbsenceDAO.update(congeExistant);
            
            request.setAttribute("success", "Congé modifié avec succès");
            response.sendRedirect(request.getContextPath() + "/app/conges-absences?action=list");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la modification du congé: ", e);
            request.setAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            afficherFormulaireModification(request, response);
        }
    }
    
    private void supprimerConge(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            CongeAbsence conge = congeAbsenceDAO.findById(id);
            
            if (conge == null) {
                request.setAttribute("error", "Congé non trouvé");
                listerConges(request, response);
                return;
            }
            
            // Vérifier si le congé peut être supprimé (pas encore approuvé et en cours)
            if (conge.getStatut() == StatutDemande.APPROUVE && 
                conge.getDateDebut().isBefore(LocalDate.now()) && 
                conge.getDateFin().isAfter(LocalDate.now())) {
                request.setAttribute("error", "Impossible de supprimer un congé en cours");
                listerConges(request, response);
                return;
            }
            
            congeAbsenceDAO.deleteById(id);
            
            request.setAttribute("success", "Congé supprimé avec succès");
            response.sendRedirect(request.getContextPath() + "/app/conges-absences?action=list");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du congé: ", e);
            request.setAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
            listerConges(request, response);
        }
    }
    
    private void approuverConge(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            String commentaire = request.getParameter("commentaire");
            
            HttpSession session = request.getSession();
            
            // Essayer de récupérer l'ID de l'employé depuis la session
            Long employeId = (Long) session.getAttribute("employeId");
            
            if (employeId == null) {
                // Fallback: essayer via utilisateur.getEmploye()
                Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
                if (utilisateur != null && utilisateur.getEmploye() != null) {
                    employeId = utilisateur.getEmploye().getId();
                }
            }
            
            if (employeId == null) {
                logger.error("Aucun employé connecté pour approuver le congé");
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }
            
            logger.info("Approbation du congé {} par l'employé ID: {}", id, employeId);
            
            // Appeler la méthode DAO qui fait tout en une seule transaction
            congeAbsenceDAO.approuverConge(id, employeId, commentaire);
            
            logger.info("Congé {} approuvé avec succès par l'employé ID: {}", id, employeId);
            
            response.sendRedirect(request.getContextPath() + "/app/conges-absences?success=approved");
            
        } catch (IllegalArgumentException e) {
            logger.error("Erreur lors de l'approbation: " + e.getMessage(), e);
            response.sendRedirect(request.getContextPath() + "/app/conges-absences?error=notfound");
        } catch (Exception e) {
            logger.error("Erreur lors de l'approbation du congé: ", e);
            response.sendRedirect(request.getContextPath() + "/app/conges-absences?error=approve_failed");
        }
    }
    
    private void rejeterConge(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            String commentaire = request.getParameter("commentaire");
            
            HttpSession session = request.getSession();
            
            // Essayer de récupérer l'ID de l'employé depuis la session
            Long employeId = (Long) session.getAttribute("employeId");
            
            if (employeId == null) {
                // Fallback: essayer via utilisateur.getEmploye()
                Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
                if (utilisateur != null && utilisateur.getEmploye() != null) {
                    employeId = utilisateur.getEmploye().getId();
                }
            }
            
            if (employeId == null) {
                logger.error("Aucun employé connecté pour rejeter le congé");
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }
            
            logger.info("Rejet du congé {} par l'employé ID: {}", id, employeId);
            
            // Appeler la méthode DAO qui fait tout en une seule transaction
            congeAbsenceDAO.rejeterConge(id, employeId, commentaire);
            
            logger.info("Congé {} rejeté avec succès par l'employé ID: {}", id, employeId);
            
            response.sendRedirect(request.getContextPath() + "/app/conges-absences?success=rejected");
            
        } catch (IllegalArgumentException e) {
            logger.error("Erreur lors du rejet: " + e.getMessage(), e);
            response.sendRedirect(request.getContextPath() + "/app/conges-absences?error=notfound");
        } catch (Exception e) {
            logger.error("Erreur lors du rejet du congé: ", e);
            response.sendRedirect(request.getContextPath() + "/app/conges-absences?error=reject_failed");
        }
    }
    
    /**
     * Construit un objet CongeAbsence à partir des paramètres de requête
     */
    private CongeAbsence construireCongeDepuisParametres(HttpServletRequest request) {
        CongeAbsence conge = new CongeAbsence();
        
        // Employé - soit depuis le formulaire (admin) soit depuis la session (employé)
        String employeIdParam = request.getParameter("employeId");
        Long employeId;
        
        if (employeIdParam != null && !employeIdParam.isEmpty()) {
            // Cas admin: employeId fourni dans le formulaire
            employeId = Long.valueOf(employeIdParam);
        } else {
            // Cas employé: récupérer l'utilisateur connecté depuis la session
            HttpSession session = request.getSession();
            Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
            // Récupérer l'employé associé à l'utilisateur
            employeId = utilisateur.getEmploye().getId();
        }
        
        Employe employe = employeDAO.findById(employeId);
        conge.setEmploye(employe);
        
        // Type de congé
        conge.setTypeConge(TypeConge.valueOf(request.getParameter("type")));
        
        // Dates
        conge.setDateDebut(LocalDate.parse(request.getParameter("dateDebut"), 
            DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        conge.setDateFin(LocalDate.parse(request.getParameter("dateFin"), 
            DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        // Motif
        conge.setMotif(request.getParameter("motif"));
        
        // Calcul automatique du nombre de jours
        long nombreJours = ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin()) + 1;
        conge.setNombreJours((int) nombreJours);
        
        return conge;
    }
    
    /**
     * Vérifie s'il y a un conflit de dates avec d'autres congés de l'employé
     */
    private boolean verifierConflitDates(CongeAbsence nouveauConge) {
        List<CongeAbsence> congesEmploye = congeAbsenceDAO.findByEmployeId(
            nouveauConge.getEmploye().getId());
        
        for (CongeAbsence congeExistant : congesEmploye) {
            // Ignorer les congés rejetés
            if (congeExistant.getStatut() == StatutDemande.REFUSE) {
                continue;
            }
            
            // Vérifier le chevauchement de dates
            if (!(nouveauConge.getDateFin().isBefore(congeExistant.getDateDebut()) || 
                  nouveauConge.getDateDebut().isAfter(congeExistant.getDateFin()))) {
                return true; // Conflit détecté
            }
        }
        
        return false; // Pas de conflit
    }
}
