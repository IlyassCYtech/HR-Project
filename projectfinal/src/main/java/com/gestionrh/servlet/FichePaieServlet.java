package com.gestionrh.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestionrh.dao.DepartementDAO;
import com.gestionrh.dao.EmployeDAO;
import com.gestionrh.dao.FichePaieDAO;
import com.gestionrh.dao.impl.DepartementDAOImpl;
import com.gestionrh.dao.impl.EmployeDAOImpl;
import com.gestionrh.dao.impl.FichePaieDAOImpl;
import com.gestionrh.model.Departement;
import com.gestionrh.model.Employe;
import com.gestionrh.model.FichePaie;
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

@WebServlet("/app/fiches-paie")
public class FichePaieServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(FichePaieServlet.class);
    private FichePaieDAO fichePaieDAO;
    private EmployeDAO employeDAO;
    private DepartementDAO departementDAO;
    
    // Constantes pour le calcul de la paie
    private static final BigDecimal TAUX_SECURITE_SOCIALE = new BigDecimal("0.2295"); // 22.95%
    private static final BigDecimal TAUX_CSG_CRDS = new BigDecimal("0.098"); // 9.8%
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.fichePaieDAO = new FichePaieDAOImpl();
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
                    listFichesPaie(request, response);
                    break;
                case "show":
                    showFichePaie(request, response);
                    break;
                case "generate":
                    showGenerateForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "pdf":
                    generatePDF(request, response);
                    break;
                case "downloadZip":
                    downloadZip(request, response);
                    break;
                case "employee":
                    showEmployeeFiches(request, response);
                    break;
                case "delete":
                    deleteFichePaie(request, response);
                    break;
                default:
                    listFichesPaie(request, response);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans FichePaieServlet: {}", e.getMessage(), e);
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
                case "generateOne":
                    createFichePaie(request, response);
                    break;
                case "generate-all":
                case "generateAll":
                    generateAllFichesPaie(request, response);
                    break;
                case "update":
                    updateFichePaie(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/app/fiches-paie");
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans FichePaieServlet POST: {}", e.getMessage(), e);
            request.setAttribute("error", "Une erreur est survenue: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }
    
    private void listFichesPaie(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        
        // Support des deux formats: "mois" (YYYY-MM) ou "mois" + "annee" séparés
        String moisAnneeStr = request.getParameter("mois"); // Format YYYY-MM du input type="month"
        String employeIdStr = request.getParameter("employeId"); // Nouveau nom cohérent avec JSP
        
        // Compatibilité avec ancien format
        if (employeIdStr == null || employeIdStr.isEmpty()) {
            employeIdStr = request.getParameter("employe");
        }
        
        // *** FILTRAGE PAR RÔLE : Seuls ADMIN et RH voient toutes les fiches ***
        // Les autres (EMPLOYE, CHEF_DEPT, CHEF_PROJET) ne voient que leurs propres fiches
        boolean isAdminOrRH = (utilisateur.getRole() == Utilisateur.Role.ADMIN || 
                               utilisateur.getRole() == Utilisateur.Role.RH);
        
        if (!isAdminOrRH) {
            Long employeIdFromSession = (Long) session.getAttribute("employeId");
            if (employeIdFromSession != null) {
                employeIdStr = employeIdFromSession.toString();
                logger.info("{} role détecté - filtrage automatique sur employeId: {}", 
                    utilisateur.getRole(), employeIdFromSession);
            } else {
                // Si pas d'employeId en session, chercher par utilisateur
                Employe employe = utilisateur.getEmploye();
                if (employe == null) {
                    // Fallback: chercher par email
                    String email = utilisateur.getUsername();
                    if (email != null && email.contains("@")) {
                        employe = employeDAO.findByEmail(email);
                    }
                }
                
                if (employe != null) {
                    employeIdStr = employe.getId().toString();
                    session.setAttribute("employeId", employe.getId());
                    logger.info("{} role - employeId {} trouvé et stocké", 
                        utilisateur.getRole(), employe.getId());
                }
            }
        }
        
        List<FichePaie> fichesPaie;
        double masseSalariale = 0.0;
        Integer mois = null;
        Integer annee = null;
        
        // Parser le format YYYY-MM si présent
        if (moisAnneeStr != null && !moisAnneeStr.isEmpty() && moisAnneeStr.contains("-")) {
            String[] parts = moisAnneeStr.split("-");
            if (parts.length == 2) {
                try {
                    annee = Integer.parseInt(parts[0]);
                    mois = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    logger.warn("Format de date invalide: {}", moisAnneeStr);
                }
            }
        }
        
        // Appliquer les filtres
        if (mois != null && annee != null && employeIdStr != null && !employeIdStr.isEmpty()) {
            // Filtre combiné: mois/année ET employé
            Long employeId = Long.parseLong(employeIdStr);
            List<FichePaie> fichesByEmploye = fichePaieDAO.findByEmployeId(employeId);
            final int finalMois = mois;
            final int finalAnnee = annee;
            fichesPaie = fichesByEmploye.stream()
                .filter(f -> f.getMois().equals(finalMois) && f.getAnnee().equals(finalAnnee))
                .toList();
            // Masse salariale = salaire de base de l'employé filtré
            Employe employe = employeDAO.findById(employeId);
            masseSalariale = employe != null && employe.getSalaireBase() != null 
                ? employe.getSalaireBase().doubleValue() : 0.0;
            logger.debug("Masse salariale (salaire base) pour employé {} période {}/{}: {}", 
                employeId, mois, annee, masseSalariale);
        } else if (mois != null && annee != null) {
            // Filtre par mois/année uniquement
            fichesPaie = fichePaieDAO.findByMoisAnnee(mois, annee);
            // Masse salariale = somme des salaires de base de tous les employés actifs
            List<Employe> employesActifs = employeDAO.findActifs();
            masseSalariale = employesActifs.stream()
                .filter(e -> e.getSalaireBase() != null)
                .mapToDouble(e -> e.getSalaireBase().doubleValue())
                .sum();
            logger.debug("Masse salariale (salaires de base) pour {}/{}: {} (nombre employés actifs: {})", 
                mois, annee, masseSalariale, employesActifs.size());
        } else if (employeIdStr != null && !employeIdStr.isEmpty()) {
            // Filtre par employé uniquement
            Long employeId = Long.parseLong(employeIdStr);
            fichesPaie = fichePaieDAO.findByEmployeId(employeId);
            // Masse salariale = salaire de base de l'employé filtré
            Employe employe = employeDAO.findById(employeId);
            masseSalariale = employe != null && employe.getSalaireBase() != null 
                ? employe.getSalaireBase().doubleValue() : 0.0;
            logger.debug("Masse salariale (salaire base) pour l'employé {}: {}", employeId, masseSalariale);
        } else {
            // Aucun filtre: toutes les fiches
            fichesPaie = fichePaieDAO.findAll();
            // Masse salariale = somme des salaires de base de tous les employés actifs
            List<Employe> employesActifs = employeDAO.findActifs();
            masseSalariale = employesActifs.stream()
                .filter(e -> e.getSalaireBase() != null)
                .mapToDouble(e -> e.getSalaireBase().doubleValue())
                .sum();
            logger.debug("Masse salariale totale (salaires de base): {} (nombre employés actifs: {})", 
                masseSalariale, employesActifs.size());
        }
        
        List<Employe> employes = employeDAO.findActifs();
        
        // Récupérer le message de succès de la session (si présent)
        String successMessage = (String) request.getSession().getAttribute("success");
        if (successMessage != null) {
            request.setAttribute("success", successMessage);
            request.getSession().removeAttribute("success");
        }
        
        List<Departement> departements = departementDAO.findActifs();
        
        request.setAttribute("fichesPaie", fichesPaie);
        request.setAttribute("employes", employes);
        request.setAttribute("departements", departements);
        request.setAttribute("masseSalariale", masseSalariale);
        request.setAttribute("currentMois", moisAnneeStr);
        request.setAttribute("currentEmployeId", employeIdStr);
        
        request.getRequestDispatcher("/WEB-INF/jsp/fiches-paie/list.jsp").forward(request, response);
    }
    
    private void showFichePaie(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Long id = Long.parseLong(request.getParameter("id"));
        FichePaie fichePaie = fichePaieDAO.findById(id);
        
        if (fichePaie == null) {
            request.setAttribute("error", "Fiche de paie non trouvée");
            listFichesPaie(request, response);
            return;
        }
        
        // *** SÉCURITÉ : Un employé ne peut voir que ses propres fiches ***
        HttpSession session = request.getSession();
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        
        if (utilisateur.getRole() == Utilisateur.Role.EMPLOYE) {
            Long employeIdFromSession = (Long) session.getAttribute("employeId");
            if (employeIdFromSession == null) {
                String email = utilisateur.getUsername();
                if (email != null && email.contains("@")) {
                    Employe employe = employeDAO.findByEmail(email);
                    if (employe != null) {
                        employeIdFromSession = employe.getId();
                        session.setAttribute("employeId", employe.getId());
                    }
                }
            }
            
            // Vérifier que la fiche appartient bien à l'employé connecté
            if (employeIdFromSession == null || !fichePaie.getEmploye().getId().equals(employeIdFromSession)) {
                logger.warn("Tentative d'accès non autorisé à la fiche {} par employé {}", id, employeIdFromSession);
                request.setAttribute("error", "Vous n'êtes pas autorisé à consulter cette fiche de paie");
                listFichesPaie(request, response);
                return;
            }
        }
        
        request.setAttribute("fiche", fichePaie);
        request.getRequestDispatcher("/WEB-INF/jsp/fiches-paie/show.jsp").forward(request, response);
    }
    
    private void showGenerateForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Employe> employes = employeDAO.findActifs();
        List<Departement> departements = departementDAO.findActifs();
        
        request.setAttribute("employes", employes);
        request.setAttribute("departements", departements);
        request.setAttribute("employesActifs", employes.size());
        
        request.getRequestDispatcher("/WEB-INF/jsp/fiches-paie/generate.jsp").forward(request, response);
    }
    
    /**
     * Affiche le formulaire d'édition d'une fiche de paie (RH/Admin uniquement)
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Vérifier les droits
        HttpSession session = request.getSession(false);
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        boolean isRHorAdmin = utilisateur != null && 
            (utilisateur.getRole() == Utilisateur.Role.ADMIN || utilisateur.getRole() == Utilisateur.Role.RH);
        
        if (!isRHorAdmin) {
            logger.warn("Tentative d'accès non autorisé au formulaire d'édition de fiche de paie");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès non autorisé");
            return;
        }
        
        Long id = Long.parseLong(request.getParameter("id"));
        FichePaie fiche = fichePaieDAO.findById(id);
        
        if (fiche == null) {
            request.setAttribute("error", "Fiche de paie non trouvée");
            listFichesPaie(request, response);
            return;
        }
        
        request.setAttribute("fiche", fiche);
        request.getRequestDispatcher("/WEB-INF/jsp/fiches-paie/edit.jsp").forward(request, response);
    }
    
    /**
     * Met à jour une fiche de paie (RH/Admin uniquement)
     */
    private void updateFichePaie(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Vérifier les droits
        HttpSession session = request.getSession(false);
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        boolean isRHorAdmin = utilisateur != null && 
            (utilisateur.getRole() == Utilisateur.Role.ADMIN || utilisateur.getRole() == Utilisateur.Role.RH);
        
        if (!isRHorAdmin) {
            logger.warn("Tentative de modification non autorisée de fiche de paie");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès non autorisé");
            return;
        }
        
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            FichePaie fiche = fichePaieDAO.findById(id);
            
            if (fiche == null) {
                request.setAttribute("error", "Fiche de paie non trouvée");
                listFichesPaie(request, response);
                return;
            }
            
            // Récupération des valeurs
            BigDecimal salaireBase = new BigDecimal(request.getParameter("salaireBase"));
            BigDecimal primesTotal = new BigDecimal(request.getParameter("primes"));
            BigDecimal deductionsTotal = new BigDecimal(request.getParameter("deductions"));
            
            // Validation
            if (salaireBase.compareTo(BigDecimal.ZERO) <= 0) {
                request.setAttribute("error", "Le salaire de base doit être supérieur à 0");
                request.setAttribute("fiche", fiche);
                request.getRequestDispatcher("/WEB-INF/jsp/fiches-paie/edit.jsp").forward(request, response);
                return;
            }
            
            // Calcul du salaire brut et net à payer
            BigDecimal salaireBrut = salaireBase.add(primesTotal);
            
            if (deductionsTotal.compareTo(salaireBrut) > 0) {
                request.setAttribute("error", "Les déductions ne peuvent pas être supérieures au salaire brut");
                request.setAttribute("fiche", fiche);
                request.getRequestDispatcher("/WEB-INF/jsp/fiches-paie/edit.jsp").forward(request, response);
                return;
            }
            
            BigDecimal netAPayer = salaireBrut.subtract(deductionsTotal);
            
            // Mise à jour de la fiche
            // Mettre le salaire de base
            fiche.setSalaireBase(salaireBase);
            
            // Mettre toutes les primes à zéro sauf autresPrimes qui contiendra le total
            fiche.setPrimePerformance(BigDecimal.ZERO);
            fiche.setPrimeAnciennete(BigDecimal.ZERO);
            fiche.setPrimeResponsabilite(BigDecimal.ZERO);
            fiche.setAutresPrimes(primesTotal);
            
            // Calculer les cotisations à partir du total des déductions
            // On met 70% en cotisations sociales et 30% en autres déductions (approximatif)
            BigDecimal cotisations = deductionsTotal.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal autresDeductions = deductionsTotal.subtract(cotisations);
            
            fiche.setCotisationsSociales(cotisations);
            fiche.setAutresDeductions(autresDeductions);
            fiche.setImpots(BigDecimal.ZERO);
            
            // Sauvegarde
            fichePaieDAO.update(fiche);
            
            logger.info("Fiche de paie {} modifiée par {} - Nouveau net à payer: {}", 
                id, utilisateur.getUsername(), netAPayer);
            
            // Redirection vers la page de détails
            response.sendRedirect(request.getContextPath() + "/app/fiches-paie?action=show&id=" + id);
            
        } catch (NumberFormatException e) {
            logger.error("Erreur de format dans les données de la fiche: {}", e.getMessage());
            request.setAttribute("error", "Format de données invalide");
            listFichesPaie(request, response);
        }
    }
    
    private void showEmployeeFiches(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String employeIdParam = request.getParameter("employeId");
            if (employeIdParam == null || employeIdParam.isEmpty()) {
                employeIdParam = request.getParameter("id");
            }
            
            if (employeIdParam == null || employeIdParam.isEmpty()) {
                request.setAttribute("error", "Paramètre employeId manquant");
                listFichesPaie(request, response);
                return;
            }
            
            Long employeId = Long.parseLong(employeIdParam);
            Employe employe = employeDAO.findById(employeId);
            
            if (employe == null) {
                request.setAttribute("error", "Employé non trouvé");
                listFichesPaie(request, response);
                return;
            }
            
            List<FichePaie> fichesPaie = fichePaieDAO.findByEmployeId(employeId);
            
            logger.info("Affichage des fiches de paie pour l'employé {} {} - {} fiches trouvées", 
                employe.getPrenom(), employe.getNom(), fichesPaie.size());
            
            // Calculer les statistiques
            BigDecimal dernierSalaire = null;
            BigDecimal totalAnnuel = BigDecimal.ZERO;
            
            if (!fichesPaie.isEmpty()) {
                // Le dernier salaire (fiche la plus récente)
                FichePaie derniereFiche = fichesPaie.stream()
                    .max((f1, f2) -> {
                        int cmp = Integer.compare(f1.getAnnee(), f2.getAnnee());
                        return cmp != 0 ? cmp : Integer.compare(f1.getMois(), f2.getMois());
                    })
                    .orElse(null);
                
                if (derniereFiche != null) {
                    dernierSalaire = derniereFiche.getNetAPayer();
                }
                
                // Total de l'année en cours
                int anneeActuelle = java.time.LocalDate.now().getYear();
                totalAnnuel = fichesPaie.stream()
                    .filter(f -> f.getAnnee() == anneeActuelle)
                    .map(FichePaie::getNetAPayer)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            
            request.setAttribute("employe", employe);
            request.setAttribute("fichesPaie", fichesPaie);
            request.setAttribute("dernierSalaire", dernierSalaire);
            request.setAttribute("totalAnnuel", totalAnnuel);
            request.getRequestDispatcher("/WEB-INF/jsp/fiches-paie/employee.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            logger.error("Format d'ID employé invalide", e);
            request.setAttribute("error", "Format d'ID employé invalide");
            listFichesPaie(request, response);
        }
    }
    
    private void createFichePaie(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long employeId = Long.parseLong(request.getParameter("employeId"));
            int mois = Integer.parseInt(request.getParameter("mois"));
            int annee = Integer.parseInt(request.getParameter("annee"));
            
            Employe employe = employeDAO.findById(employeId);
            if (employe == null) {
                request.setAttribute("error", "Employé non trouvé");
                showGenerateForm(request, response);
                return;
            }
            
            // Vérifier si une fiche existe déjà
            FichePaie existante = fichePaieDAO.findByEmployeAndMoisAnnee(employeId, mois, annee);
            if (existante != null) {
                request.setAttribute("error", "Une fiche de paie existe déjà pour cet employé et cette période");
                showGenerateForm(request, response);
                return;
            }
            
            // Créer la fiche de paie avec calcul automatique
            FichePaie fichePaie = calculerFichePaie(employe, mois, annee, request);
            fichePaieDAO.save(fichePaie);
            
            logger.info("Fiche de paie générée avec succès pour {} - {}/{}", 
                employe.getPrenom() + " " + employe.getNom(), mois, annee);
            
            // Rediriger vers la liste (sans message)
            response.sendRedirect(request.getContextPath() + "/app/fiches-paie");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la fiche de paie", e);
            request.setAttribute("error", "Erreur lors de la création: " + e.getMessage());
            showGenerateForm(request, response);
        }
    }
    
    private void generateAllFichesPaie(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int mois = Integer.parseInt(request.getParameter("mois"));
            int annee = Integer.parseInt(request.getParameter("annee"));
            
            List<Employe> employes = employeDAO.findActifs();
            int generated = 0;
            int skipped = 0;
            
            for (Employe employe : employes) {
                // Vérifier si une fiche existe déjà
                FichePaie existante = fichePaieDAO.findByEmployeAndMoisAnnee(employe.getId(), mois, annee);
                if (existante != null) {
                    skipped++;
                    continue;
                }
                
                // Créer la fiche de paie
                FichePaie fichePaie = calculerFichePaie(employe, mois, annee, request);
                fichePaieDAO.save(fichePaie);
                generated++;
            }
            
            logger.info("Génération en masse terminée pour {}/{}: {} créées, {} ignorées", 
                mois, annee, generated, skipped);
            
            // Rediriger vers la liste (sans message)
            response.sendRedirect(request.getContextPath() + "/app/fiches-paie");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la génération massive", e);
            request.setAttribute("error", "Erreur lors de la génération: " + e.getMessage());
            showGenerateForm(request, response);
        }
    }
    
    private void deleteFichePaie(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            FichePaie fichePaie = fichePaieDAO.findById(id);
            
            if (fichePaie == null) {
                request.setAttribute("error", "Fiche de paie non trouvée");
                listFichesPaie(request, response);
                return;
            }
            
            fichePaieDAO.delete(fichePaie);
            
            request.setAttribute("success", "Fiche de paie supprimée avec succès");
            response.sendRedirect(request.getContextPath() + "/app/fiches-paie");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la fiche de paie", e);
            request.setAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
            listFichesPaie(request, response);
        }
    }
    
    /**
     * Calcule automatiquement une fiche de paie
     * Formule: Net = Salaire de base + Primes - Déductions (Sécurité sociale + CSG/CRDS)
     */
    private FichePaie calculerFichePaie(Employe employe, int mois, int annee, HttpServletRequest request) {
        FichePaie fichePaie = new FichePaie();
        
        fichePaie.setEmploye(employe);
        fichePaie.setMois(mois);
        fichePaie.setAnnee(annee);
        
        // Salaire de base
        BigDecimal salaireBase = employe.getSalaireBase();
        fichePaie.setSalaireBase(salaireBase);
        
        // Calcul des primes
        BigDecimal primeAnciennete = calculerPrimeAnciennete(employe);
        fichePaie.setPrimeAnciennete(primeAnciennete);
        
        // Prime exceptionnelle (saisie manuelle)
        String primeExceptionnelleStr = request.getParameter("primeExceptionnelle");
        BigDecimal primePerformance = BigDecimal.ZERO;
        if (primeExceptionnelleStr != null && !primeExceptionnelleStr.isEmpty()) {
            try {
                primePerformance = new BigDecimal(primeExceptionnelleStr);
            } catch (NumberFormatException e) {
                logger.warn("Prime exceptionnelle invalide: {}", primeExceptionnelleStr);
            }
        }
        fichePaie.setPrimePerformance(primePerformance);
        
        // Calcul du salaire brut total
        BigDecimal salaireBrutTotal = salaireBase.add(primeAnciennete).add(primePerformance);
        
        // Calcul des déductions (charges sociales)
        BigDecimal cotisationsSecuriteSociale = salaireBrutTotal.multiply(TAUX_SECURITE_SOCIALE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal cotisationsCsgCrds = salaireBrutTotal.multiply(TAUX_CSG_CRDS)
            .setScale(2, RoundingMode.HALF_UP);
        
        fichePaie.setCotisationsSociales(cotisationsSecuriteSociale.add(cotisationsCsgCrds));
        
        // Calcul du net à payer automatiquement (méthode dans la classe FichePaie)
        
        return fichePaie;
    }
    
    /**
     * Calcule la prime d'ancienneté (50€ par année d'ancienneté)
     */
    private BigDecimal calculerPrimeAnciennete(Employe employe) {
        if (employe.getDateEmbauche() == null) {
            return BigDecimal.ZERO;
        }
        
        LocalDate dateEmbauche = employe.getDateEmbauche();
        LocalDate maintenant = LocalDate.now();
        
        long anneesAnciennete = ChronoUnit.YEARS.between(dateEmbauche, maintenant);
        
        return new BigDecimal(anneesAnciennete * 50);
    }
    
    /**
     * Génère un PDF professionnel pour une fiche de paie (format standard français)
     */
    private void generatePDF(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de fiche de paie manquant");
            return;
        }
        
        try {
            Long id = Long.parseLong(idStr);
            FichePaie fiche = fichePaieDAO.findById(id);
            
            if (fiche == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Fiche de paie non trouvée");
                return;
            }
            
            // Configuration du format de nombre français
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
            symbols.setGroupingSeparator(' ');
            symbols.setDecimalSeparator(',');
            DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
            DecimalFormat dfPercent = new DecimalFormat("0.0", symbols);
            
            // Création du document PDF (marges réduites pour tenir sur 1 page)
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Couleurs professionnelles
            BaseColor darkBlue = new BaseColor(31, 56, 100);  // Bleu foncé professionnel
            BaseColor lightGray = new BaseColor(240, 240, 240);
            BaseColor mediumGray = new BaseColor(200, 200, 200);
            BaseColor darkGray = new BaseColor(80, 80, 80);
            
            // Polices
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, darkBlue);
            Font smallBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);
            Font smallNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, darkGray);
            Font mediumBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
            Font mediumNormal = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
            Font headerWhite = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
            Font netFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            
            // ============= EN-TÊTE =============
            Paragraph header = new Paragraph("BULLETIN DE PAIE", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(5);
            document.add(header);
            
            Paragraph period = new Paragraph("Période : " + String.format("%02d/%d", fiche.getMois(), fiche.getAnnee()), 
                FontFactory.getFont(FontFactory.HELVETICA, 10, darkGray));
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(10);
            document.add(period);
            
            // ============= EMPLOYEUR / SALARIÉ =============
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 1});
            infoTable.setSpacingAfter(8);
            
            // EMPLOYEUR
            PdfPCell employerCell = new PdfPCell();
            employerCell.setBorder(Rectangle.BOX);
            employerCell.setBorderColor(mediumGray);
            employerCell.setPadding(8);
            employerCell.addElement(new Paragraph("EMPLOYEUR", smallBold));
            employerCell.addElement(new Paragraph("RH ÉLÉGANCE", mediumBold));
            employerCell.addElement(new Paragraph("123 Avenue des Champs-Élysées", smallNormal));
            employerCell.addElement(new Paragraph("75008 Paris, France", smallNormal));
            employerCell.addElement(new Paragraph("SIRET: 123 456 789 00012", smallNormal));
            infoTable.addCell(employerCell);
            
            // SALARIÉ
            PdfPCell employeeCell = new PdfPCell();
            employeeCell.setBorder(Rectangle.BOX);
            employeeCell.setBorderColor(mediumGray);
            employeeCell.setPadding(8);
            employeeCell.addElement(new Paragraph("SALARIÉ", smallBold));
            employeeCell.addElement(new Paragraph(
                fiche.getEmploye().getPrenom() + " " + fiche.getEmploye().getNom().toUpperCase(), mediumBold));
            employeeCell.addElement(new Paragraph("Matricule: " + fiche.getEmploye().getMatricule(), smallNormal));
            employeeCell.addElement(new Paragraph("Poste: " + fiche.getEmploye().getPoste(), smallNormal));
            employeeCell.addElement(new Paragraph("Département: " + fiche.getEmploye().getDepartement().getNom(), smallNormal));
            employeeCell.addElement(new Paragraph("Grade: " + fiche.getEmploye().getGrade(), smallNormal));
            infoTable.addCell(employeeCell);
            
            document.add(infoTable);
            
            // ============= TABLEAU DES RUBRIQUES =============
            PdfPTable salaryTable = new PdfPTable(4);
            salaryTable.setWidthPercentage(100);
            salaryTable.setWidths(new float[]{3.5f, 1.2f, 1.2f, 1.5f});
            salaryTable.setSpacingAfter(5);
            
            // EN-TÊTE TABLEAU
            addTableHeader(salaryTable, "LIBELLÉ", headerWhite, darkBlue);
            addTableHeader(salaryTable, "BASE", headerWhite, darkBlue);
            addTableHeader(salaryTable, "TAUX", headerWhite, darkBlue);
            addTableHeader(salaryTable, "MONTANT", headerWhite, darkBlue);
            
            // === RÉMUNÉRATION BRUTE ===
            addSectionHeader(salaryTable, "RÉMUNÉRATION BRUTE", 4);
            
            addTableRow(salaryTable, "Salaire de base", "151,67 h", "", df.format(fiche.getSalaireBase()) + " €", false);
            
            if (fiche.getPrimes().compareTo(BigDecimal.ZERO) > 0) {
                addTableRow(salaryTable, "Primes et indemnités", "", "", df.format(fiche.getPrimes()) + " €", false);
            }
            
            addTableRow(salaryTable, "TOTAL BRUT", "", "", df.format(fiche.getSalaireBrut()) + " €", true);
            
            // === COTISATIONS ET CONTRIBUTIONS ===
            addSectionHeader(salaryTable, "COTISATIONS ET CONTRIBUTIONS", 4);
            
            if (fiche.getDeductions().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal tauxSecu = new BigDecimal("0.2295"); // 22.95%
                BigDecimal tauxCsg = new BigDecimal("0.098"); // 9.8%
                
                BigDecimal cotisSecu = fiche.getSalaireBrut().multiply(tauxSecu).setScale(2, RoundingMode.HALF_UP);
                BigDecimal cotisCSG = fiche.getSalaireBrut().multiply(tauxCsg).setScale(2, RoundingMode.HALF_UP);
                
                addTableRow(salaryTable, "Sécurité sociale", df.format(fiche.getSalaireBrut()) + " €", "22,95 %", 
                    df.format(cotisSecu) + " €", false);
                addTableRow(salaryTable, "CSG / CRDS", df.format(fiche.getSalaireBrut()) + " €", "9,80 %", 
                    df.format(cotisCSG) + " €", false);
                
                addTableRow(salaryTable, "TOTAL COTISATIONS", "", "", df.format(fiche.getDeductions()) + " €", true);
            }
            
            document.add(salaryTable);
            
            // ============= NET À PAYER =============
            PdfPTable netTable = new PdfPTable(2);
            netTable.setWidthPercentage(100);
            netTable.setSpacingBefore(10);
            netTable.setSpacingAfter(10);
            
            PdfPCell netLabelCell = new PdfPCell(new Phrase("NET À PAYER", netFont));
            netLabelCell.setBorder(Rectangle.NO_BORDER);
            netLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            netLabelCell.setPadding(10);
            netLabelCell.setBackgroundColor(lightGray);
            netTable.addCell(netLabelCell);
            
            PdfPCell netAmountCell = new PdfPCell(new Phrase(df.format(fiche.getNetAPayer()) + " €", netFont));
            netAmountCell.setBorder(Rectangle.NO_BORDER);
            netAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            netAmountCell.setPadding(10);
            netAmountCell.setBackgroundColor(lightGray);
            netTable.addCell(netAmountCell);
            
            document.add(netTable);
            
            // ============= BAS DE PAGE =============
            Paragraph footer = new Paragraph("Bulletin émis le " + fiche.getDateCreationFormatted() + 
                " - N° " + fiche.getId() + " - Document à conserver sans limitation de durée", 
                FontFactory.getFont(FontFactory.HELVETICA, 7, darkGray));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(15);
            document.add(footer);
            
            document.close();
            
            // Envoi du PDF au client
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", 
                String.format("attachment; filename=\"Bulletin_Paie_%s_%s_%02d_%d.pdf\"",
                    fiche.getEmploye().getNom().replaceAll("\\s+", "_"),
                    fiche.getEmploye().getPrenom().replaceAll("\\s+", "_"),
                    fiche.getMois(),
                    fiche.getAnnee()));
            response.setContentLength(baos.size());
            
            baos.writeTo(response.getOutputStream());
            response.getOutputStream().flush();
            
        } catch (DocumentException e) {
            logger.error("Erreur lors de la génération du PDF: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Erreur lors de la génération du PDF");
        } catch (NumberFormatException e) {
            logger.error("ID invalide: {}", idStr);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID invalide");
        }
    }
    
    /**
     * Ajoute un en-tête de tableau
     */
    private void addTableHeader(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }
    
    /**
     * Ajoute un en-tête de section dans le tableau
     */
    private void addSectionHeader(PdfPTable table, String text, int colspan) {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, sectionFont));
        cell.setColspan(colspan);
        cell.setBackgroundColor(new BaseColor(100, 100, 100));
        cell.setPadding(5);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }
    
    /**
     * Ajoute une ligne au tableau des salaires
     */
    private void addTableRow(PdfPTable table, String libelle, String base, String taux, String montant, boolean isBold) {
        Font cellFont = isBold ? 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK) :
            FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
        
        BaseColor bgColor = isBold ? new BaseColor(240, 240, 240) : BaseColor.WHITE;
        
        // Libellé
        PdfPCell c1 = new PdfPCell(new Phrase(libelle, cellFont));
        c1.setPadding(5);
        c1.setBackgroundColor(bgColor);
        c1.setBorder(Rectangle.BOTTOM);
        c1.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(c1);
        
        // Base
        PdfPCell c2 = new PdfPCell(new Phrase(base, cellFont));
        c2.setPadding(5);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBackgroundColor(bgColor);
        c2.setBorder(Rectangle.BOTTOM);
        c2.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(c2);
        
        // Taux
        PdfPCell c3 = new PdfPCell(new Phrase(taux, cellFont));
        c3.setPadding(5);
        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c3.setBackgroundColor(bgColor);
        c3.setBorder(Rectangle.BOTTOM);
        c3.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(c3);
        
        // Montant
        PdfPCell c4 = new PdfPCell(new Phrase(montant, cellFont));
        c4.setPadding(5);
        c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c4.setBackgroundColor(bgColor);
        c4.setBorder(Rectangle.BOTTOM);
        c4.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(c4);
    }
    
    /**
     * Crée une cellule pour la section période (fond noir)
     */
    private PdfPCell createPeriodCell(String label, String value, Font labelFont, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(15);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph labelPara = new Paragraph(label, labelFont);
        labelPara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(labelPara);
        
        cell.addElement(new Paragraph(" ", labelFont));
        
        Paragraph valuePara = new Paragraph(value, 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE));
        valuePara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(valuePara);
        
        return cell;
    }
    
    /**
     * Crée une cellule pour le tableau des salaires
     */
    private PdfPCell createSalaryCell(String text, Font font, boolean centered) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(12);
        cell.setBorderColor(new BaseColor(232, 232, 232));
        cell.setBorderWidth(0);
        cell.setBorderWidthBottom(1);
        if (centered) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        } else {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        return cell;
    }
    
    /**
     * Calcule les primes basées sur l'ancienneté et les paramètres de la requête
     */
    
    /**
     * Télécharge les fiches de paie en ZIP
     */
    private void downloadZip(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String type = request.getParameter("type");
        HttpSession session = request.getSession(false);
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        Long employeIdSession = (Long) session.getAttribute("employeId");
        
        List<FichePaie> fiches;
        String filename;
        
        // Déterminer les fiches à inclure dans le ZIP
        if ("my".equals(type)) {
            // Télécharger uniquement les fiches de l'employé connecté
            if (employeIdSession == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session invalide");
                return;
            }
            fiches = fichePaieDAO.findByEmployeId(employeIdSession);
            Employe employe = employeDAO.findById(employeIdSession);
            filename = "fiches_paie_" + employe.getNom().replaceAll("[^a-zA-Z0-9]", "_") + ".zip";
            logger.info("Génération ZIP pour employé {} : {} fiches", employeIdSession, fiches.size());
            
        } else if ("all".equals(type)) {
            // Télécharger toutes les fiches (RH/Admin uniquement)
            boolean isRHorAdmin = utilisateur != null && 
                (utilisateur.getRole() == Utilisateur.Role.ADMIN || utilisateur.getRole() == Utilisateur.Role.RH);
            
            if (!isRHorAdmin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès non autorisé");
                return;
            }
            
            fiches = fichePaieDAO.findAll();
            filename = "toutes_fiches_paie_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".zip";
            logger.info("Génération ZIP pour TOUTES les fiches (RH/Admin) : {} fiches", fiches.size());
            
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Type de téléchargement invalide");
            return;
        }
        
        // Vérifier qu'il y a des fiches
        if (fiches.isEmpty()) {
            request.setAttribute("error", "Aucune fiche de paie disponible pour le téléchargement");
            listFichesPaie(request, response);
            return;
        }
        
        // Configurer la réponse HTTP pour le téléchargement ZIP
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        try (OutputStream os = response.getOutputStream();
             ZipOutputStream zos = new ZipOutputStream(os)) {
            
            // Ajouter chaque fiche de paie comme PDF dans le ZIP
            for (FichePaie fiche : fiches) {
                try {
                    // Générer le nom du fichier pour cette fiche
                    Employe employe = fiche.getEmploye();
                    String nomEmploye = employe.getNom().replaceAll("[^a-zA-Z0-9]", "_");
                    String prenomEmploye = employe.getPrenom().replaceAll("[^a-zA-Z0-9]", "_");
                    String period = fiche.getMois().toString().replace("-", "_");
                    String pdfFilename = String.format("fiche_paie_%s_%s_%s.pdf", 
                        nomEmploye, prenomEmploye, period);
                    
                    // Créer une entrée dans le ZIP
                    ZipEntry zipEntry = new ZipEntry(pdfFilename);
                    zos.putNextEntry(zipEntry);
                    
                    // Générer le PDF dans le flux du ZIP
                    byte[] pdfBytes = generatePDFBytes(fiche);
                    zos.write(pdfBytes);
                    zos.closeEntry();
                    
                    logger.debug("Ajouté au ZIP: {}", pdfFilename);
                    
                } catch (Exception e) {
                    logger.error("Erreur lors de la génération du PDF pour la fiche {}: {}", 
                        fiche.getId(), e.getMessage());
                    // Continuer avec les autres fiches
                }
            }
            
            zos.finish();
            logger.info("ZIP généré avec succès: {} ({} fiches)", filename, fiches.size());
            
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du ZIP: {}", e.getMessage(), e);
            throw new ServletException("Erreur lors de la génération du ZIP", e);
        }
    }
    
    /**
     * Génère un PDF pour une fiche de paie et retourne les bytes
     */
    private byte[] generatePDFBytes(FichePaie fiche) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        generatePDFContent(document, fiche);
        document.close();
        
        return baos.toByteArray();
    }
    
    /**
     * Génère le contenu PDF d'une fiche de paie (logique partagée)
     */
    private void generatePDFContent(Document document, FichePaie fiche) throws DocumentException {
        // Configuration du format de nombre français
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        
        // Couleurs professionnelles
        BaseColor darkBlue = new BaseColor(31, 56, 100);
        BaseColor lightGray = new BaseColor(240, 240, 240);
        BaseColor mediumGray = new BaseColor(200, 200, 200);
        BaseColor darkGray = new BaseColor(80, 80, 80);
        
        // Polices
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, darkBlue);
        Font smallBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);
        Font smallNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, darkGray);
        Font mediumBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
        Font headerWhite = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
        Font netFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
        
        // ============= EN-TÊTE =============
        Paragraph header = new Paragraph("BULLETIN DE PAIE", titleFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(5);
        document.add(header);
        
        Paragraph period = new Paragraph("Période : " + String.format("%02d/%d", fiche.getMois(), fiche.getAnnee()), 
            FontFactory.getFont(FontFactory.HELVETICA, 10, darkGray));
        period.setAlignment(Element.ALIGN_CENTER);
        period.setSpacingAfter(10);
        document.add(period);
        
        // ============= EMPLOYEUR / SALARIÉ =============
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});
        infoTable.setSpacingAfter(8);
        
        // EMPLOYEUR
        PdfPCell employerCell = new PdfPCell();
        employerCell.setBorder(Rectangle.BOX);
        employerCell.setBorderColor(mediumGray);
        employerCell.setPadding(8);
        employerCell.addElement(new Paragraph("EMPLOYEUR", smallBold));
        employerCell.addElement(new Paragraph("RH ÉLÉGANCE", mediumBold));
        employerCell.addElement(new Paragraph("123 Avenue des Champs-Élysées", smallNormal));
        employerCell.addElement(new Paragraph("75008 Paris, France", smallNormal));
        employerCell.addElement(new Paragraph("SIRET: 123 456 789 00012", smallNormal));
        infoTable.addCell(employerCell);
        
        // SALARIÉ
        PdfPCell employeeCell = new PdfPCell();
        employeeCell.setBorder(Rectangle.BOX);
        employeeCell.setBorderColor(mediumGray);
        employeeCell.setPadding(8);
        employeeCell.addElement(new Paragraph("SALARIÉ", smallBold));
        employeeCell.addElement(new Paragraph(
            fiche.getEmploye().getPrenom() + " " + fiche.getEmploye().getNom().toUpperCase(), mediumBold));
        employeeCell.addElement(new Paragraph("Matricule: " + fiche.getEmploye().getMatricule(), smallNormal));
        employeeCell.addElement(new Paragraph("Poste: " + fiche.getEmploye().getPoste(), smallNormal));
        employeeCell.addElement(new Paragraph("Département: " + fiche.getEmploye().getDepartement().getNom(), smallNormal));
        employeeCell.addElement(new Paragraph("Grade: " + fiche.getEmploye().getGrade(), smallNormal));
        infoTable.addCell(employeeCell);
        
        document.add(infoTable);
        
        // ============= TABLEAU DES RUBRIQUES =============
        PdfPTable salaryTable = new PdfPTable(4);
        salaryTable.setWidthPercentage(100);
        salaryTable.setWidths(new float[]{3.5f, 1.2f, 1.2f, 1.5f});
        salaryTable.setSpacingAfter(5);
        
        // EN-TÊTE TABLEAU
        addTableHeader(salaryTable, "LIBELLÉ", headerWhite, darkBlue);
        addTableHeader(salaryTable, "BASE", headerWhite, darkBlue);
        addTableHeader(salaryTable, "TAUX", headerWhite, darkBlue);
        addTableHeader(salaryTable, "MONTANT", headerWhite, darkBlue);
        
        // === RÉMUNÉRATION BRUTE ===
        addSectionHeader(salaryTable, "RÉMUNÉRATION BRUTE", 4);
        
        addTableRow(salaryTable, "Salaire de base", "151,67 h", "", df.format(fiche.getSalaireBase()) + " €", false);
        
        if (fiche.getPrimes().compareTo(BigDecimal.ZERO) > 0) {
            addTableRow(salaryTable, "Primes et indemnités", "", "", df.format(fiche.getPrimes()) + " €", false);
        }
        
        addTableRow(salaryTable, "TOTAL BRUT", "", "", df.format(fiche.getSalaireBrut()) + " €", true);
        
        // === COTISATIONS ET CONTRIBUTIONS ===
        addSectionHeader(salaryTable, "COTISATIONS ET CONTRIBUTIONS", 4);
        
        if (fiche.getDeductions().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tauxSecu = new BigDecimal("0.2295"); // 22.95%
            BigDecimal tauxCsg = new BigDecimal("0.098"); // 9.8%
            
            BigDecimal cotisSecu = fiche.getSalaireBrut().multiply(tauxSecu).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cotisCSG = fiche.getSalaireBrut().multiply(tauxCsg).setScale(2, RoundingMode.HALF_UP);
            
            addTableRow(salaryTable, "Sécurité sociale", df.format(fiche.getSalaireBrut()) + " €", "22,95 %", 
                df.format(cotisSecu) + " €", false);
            addTableRow(salaryTable, "CSG / CRDS", df.format(fiche.getSalaireBrut()) + " €", "9,80 %", 
                df.format(cotisCSG) + " €", false);
            
            addTableRow(salaryTable, "TOTAL COTISATIONS", "", "", df.format(fiche.getDeductions()) + " €", true);
        }
        
        document.add(salaryTable);
        
        // ============= NET À PAYER =============
        PdfPTable netTable = new PdfPTable(2);
        netTable.setWidthPercentage(100);
        netTable.setSpacingBefore(10);
        netTable.setSpacingAfter(10);
        
        PdfPCell netLabelCell = new PdfPCell(new Phrase("NET À PAYER", netFont));
        netLabelCell.setBorder(Rectangle.NO_BORDER);
        netLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        netLabelCell.setPadding(10);
        netLabelCell.setBackgroundColor(lightGray);
        netTable.addCell(netLabelCell);
        
        PdfPCell netAmountCell = new PdfPCell(new Phrase(df.format(fiche.getNetAPayer()) + " €", netFont));
        netAmountCell.setBorder(Rectangle.NO_BORDER);
        netAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        netAmountCell.setPadding(10);
        netAmountCell.setBackgroundColor(lightGray);
        netTable.addCell(netAmountCell);
        
        document.add(netTable);
        
        // ============= BAS DE PAGE =============
        Paragraph footer = new Paragraph("Bulletin émis le " + fiche.getDateCreationFormatted() + 
            " - N° " + fiche.getId() + " - Document à conserver sans limitation de durée", 
            FontFactory.getFont(FontFactory.HELVETICA, 7, darkGray));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(15);
        document.add(footer);
    }
}
