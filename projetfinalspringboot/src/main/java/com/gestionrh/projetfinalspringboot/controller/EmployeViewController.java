package com.gestionrh.projetfinalspringboot.controller;

import com.gestionrh.projetfinalspringboot.dto.CredentialDto;
import com.gestionrh.projetfinalspringboot.model.entity.Employe;
import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.model.enums.Role;
import com.gestionrh.projetfinalspringboot.model.enums.StatutUtilisateur;
import com.gestionrh.projetfinalspringboot.service.DepartementService;
import com.gestionrh.projetfinalspringboot.service.EmployeService;
import com.gestionrh.projetfinalspringboot.service.UtilisateurService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Contrôleur MVC pour les vues des employés (non-REST)
 */
@Controller
@RequestMapping("/employes")
@RequiredArgsConstructor
public class EmployeViewController {

    private final EmployeService employeService;
    private final DepartementService departementService;
    private final UtilisateurService utilisateurService;

    /**
     * Afficher la liste des employés
     */
    @GetMapping("/list")
    public String listEmployes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departement,
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        // TODO: Implémenter la recherche et la pagination
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("departements", departementService.findAll());
        model.addAttribute("totalEmployes", employeService.findAll().size());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 1);
        
        return "employes/list";
    }

    /**
     * Afficher le formulaire de création
     */
    @GetMapping("/form")
    public String showCreateForm(Model model) {
        model.addAttribute("employe", new Employe());
        model.addAttribute("departements", departementService.findAll());
        model.addAttribute("managers", employeService.findAll());
        return "employes/form";
    }

    /**
     * Afficher le formulaire de modification
     */
    @GetMapping("/form/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Employe> employe = employeService.findByIdWithDetails(id);
        
        if (employe.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Employé introuvable");
            return "redirect:/employes/list";
        }
        
        model.addAttribute("employe", employe.get());
        model.addAttribute("departements", departementService.findAll());
        model.addAttribute("managers", employeService.findAll()); // Liste de tous les employés potentiels managers
        return "employes/form";
    }

    /**
     * Créer un nouvel employé
     */
    @PostMapping("/create")
    public String createEmploye(@ModelAttribute Employe employe, RedirectAttributes redirectAttributes) {
        try {
            // Vérifier si le matricule existe déjà
            if (employeService.findByMatricule(employe.getMatricule()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Le matricule '" + employe.getMatricule() + "' existe déjà. Veuillez en choisir un autre.");
                redirectAttributes.addFlashAttribute("employe", employe);
                return "redirect:/employes/form";
            }
            
            // Vérifier si l'email existe déjà (si fourni)
            if (employe.getEmail() != null && !employe.getEmail().trim().isEmpty() 
                && employeService.findByEmail(employe.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", 
                    "L'email '" + employe.getEmail() + "' est déjà utilisé. Veuillez en choisir un autre.");
                redirectAttributes.addFlashAttribute("employe", employe);
                return "redirect:/employes/form";
            }
            
            // Gérer le département (charger depuis la base si ID fourni)
            if (employe.getDepartement() != null && employe.getDepartement().getId() != null) {
                departementService.findById(employe.getDepartement().getId())
                    .ifPresent(employe::setDepartement);
            } else {
                employe.setDepartement(null);
            }
            
            // Gérer le manager (charger depuis la base si ID fourni)
            if (employe.getManager() != null && employe.getManager().getId() != null) {
                employeService.findById(employe.getManager().getId())
                    .ifPresent(employe::setManager);
            } else {
                employe.setManager(null);
            }
            
            Employe saved = employeService.save(employe);
            redirectAttributes.addFlashAttribute("message", 
                "Employé " + saved.getPrenom() + " " + saved.getNom() + " créé avec succès");
            return "redirect:/employes/show/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
            redirectAttributes.addFlashAttribute("employe", employe);
            return "redirect:/employes/form";
        }
    }

    /**
     * Mettre à jour un employé
     */
    @PostMapping("/update/{id}")
    public String updateEmploye(@PathVariable Long id, @ModelAttribute Employe employe, 
                                RedirectAttributes redirectAttributes) {
        try {
            // Charger l'employé existant
            Optional<Employe> existingOpt = employeService.findById(id);
            if (existingOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Employé introuvable");
                return "redirect:/employes/list";
            }
            
            Employe existing = existingOpt.get();
            
            // Vérifier si le nouveau matricule est déjà utilisé par un autre employé
            if (!existing.getMatricule().equals(employe.getMatricule())) {
                Optional<Employe> employeAvecMemeMatricule = employeService.findByMatricule(employe.getMatricule());
                if (employeAvecMemeMatricule.isPresent()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Le matricule '" + employe.getMatricule() + "' est déjà utilisé par un autre employé.");
                    return "redirect:/employes/form/" + id;
                }
            }
            
            // Vérifier si le nouvel email est déjà utilisé par un autre employé
            if (employe.getEmail() != null && !employe.getEmail().trim().isEmpty() 
                && !employe.getEmail().equals(existing.getEmail())) {
                Optional<Employe> employeAvecMemeEmail = employeService.findByEmail(employe.getEmail());
                if (employeAvecMemeEmail.isPresent()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "L'email '" + employe.getEmail() + "' est déjà utilisé par un autre employé.");
                    return "redirect:/employes/form/" + id;
                }
            }
            
            // Mettre à jour les champs simples
            existing.setMatricule(employe.getMatricule());
            existing.setNom(employe.getNom());
            existing.setPrenom(employe.getPrenom());
            existing.setEmail(employe.getEmail());
            existing.setTelephone(employe.getTelephone());
            existing.setAdresse(employe.getAdresse());
            existing.setDateNaissance(employe.getDateNaissance());
            existing.setDateEmbauche(employe.getDateEmbauche());
            existing.setPoste(employe.getPoste());
            existing.setGrade(employe.getGrade());
            existing.setStatut(employe.getStatut());
            existing.setSalaireBase(employe.getSalaireBase());
            
            // Gérer le département (charger depuis la base si ID fourni)
            if (employe.getDepartement() != null && employe.getDepartement().getId() != null) {
                departementService.findById(employe.getDepartement().getId())
                    .ifPresent(existing::setDepartement);
            } else {
                existing.setDepartement(null);
            }
            
            // Gérer le manager (charger depuis la base si ID fourni)
            if (employe.getManager() != null && employe.getManager().getId() != null) {
                employeService.findById(employe.getManager().getId())
                    .ifPresent(existing::setManager);
            } else {
                existing.setManager(null);
            }
            
            Employe updated = employeService.save(existing);
            redirectAttributes.addFlashAttribute("message", 
                "Employé " + updated.getPrenom() + " " + updated.getNom() + " mis à jour avec succès");
            return "redirect:/employes/show/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            return "redirect:/employes/form/" + id;
        }
    }

    /**
     * Afficher les détails d'un employé
     */
    @GetMapping("/show/{id}")
    public String showEmploye(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Employe> employe = employeService.findByIdWithDetails(id);
        
        if (employe.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Employé introuvable");
            return "redirect:/employes/list";
        }
        
        model.addAttribute("employe", employe.get());
        return "employes/show";
    }

    /**
     * Afficher le formulaire de génération d'identifiants
     */
    @GetMapping("/generate-credentials")
    public String showGenerateCredentialsForm(HttpSession session, Model model) {
        List<Employe> tousLesEmployes = employeService.findAll();
        
        // Séparer les employés avec et sans compte
        List<Employe> employesSansCompte = new ArrayList<>();
        List<Employe> employesAvecCompte = new ArrayList<>();
        Map<Long, String> employeUsernames = new HashMap<>();
        
        for (Employe emp : tousLesEmployes) {
            if (emp.getEmail() != null && !emp.getEmail().trim().isEmpty()) {
                Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurByEmail(emp.getEmail());
                if (utilisateur.isPresent()) {
                    employesAvecCompte.add(emp);
                    employeUsernames.put(emp.getId(), utilisateur.get().getUsername());
                } else {
                    employesSansCompte.add(emp);
                }
            } else {
                employesSansCompte.add(emp);
            }
        }
        
        // Calculer les statistiques
        model.addAttribute("totalEmployes", tousLesEmployes.size());
        model.addAttribute("employesAvecCompte", employesAvecCompte.size());
        model.addAttribute("employesSansCompte", employesSansCompte.size());
        
        // Ajouter les listes
        model.addAttribute("employesSansCompteList", employesSansCompte);
        model.addAttribute("employesAvecCompteList", employesAvecCompte);
        model.addAttribute("employeUsernames", employeUsernames);
        
        // Ajouter les identifiants récemment générés depuis la session
        @SuppressWarnings("unchecked")
        List<CredentialDto> recentCredentials = (List<CredentialDto>) session.getAttribute("recentCredentials");
        if (recentCredentials != null && !recentCredentials.isEmpty()) {
            model.addAttribute("recentCredentials", recentCredentials);
        }
        
        return "employes/generate-credentials";
    }

    /**
     * Générer les identifiants pour un employé
     */
    @PostMapping("/generate-credentials")
    public String generateCredentials(@RequestParam Long employeId, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Optional<Employe> employeOpt = employeService.findById(employeId);
            if (!employeOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Employé introuvable");
                return "redirect:/employes/generate-credentials";
            }
            
            Employe employe = employeOpt.get();
            
            // Vérifier si l'employé a un email
            if (employe.getEmail() == null || employe.getEmail().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "L'employé doit avoir une adresse email pour générer des identifiants");
                return "redirect:/employes/generate-credentials";
            }
            
            // Vérifier si l'employé a déjà un compte utilisateur
            if (utilisateurService.getUtilisateurByEmail(employe.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Un compte utilisateur existe déjà pour cet employé");
                return "redirect:/employes/generate-credentials";
            }
            
            // Générer le nom d'utilisateur : prenom.nom
            String username = (employe.getPrenom() + "." + employe.getNom())
                .toLowerCase()
                .replaceAll("[^a-z0-9.]", "")
                .replaceAll("\\.+", ".");
            
            // Vérifier si le username existe déjà
            int suffix = 1;
            String finalUsername = username;
            while (utilisateurService.existsByUsername(finalUsername)) {
                finalUsername = username + suffix;
                suffix++;
            }
            
            // Générer un mot de passe temporaire aléatoire
            String tempPassword = generateRandomPassword();
            
            // IMPORTANT: Stocker en session AVANT d'encoder le mot de passe
            @SuppressWarnings("unchecked")
            List<CredentialDto> recentCredentials = (List<CredentialDto>) session.getAttribute("recentCredentials");
            if (recentCredentials == null) {
                recentCredentials = new ArrayList<>();
            }
            recentCredentials.add(CredentialDto.builder()
                .employeId(employe.getId())
                .employeNom(employe.getPrenom() + " " + employe.getNom())
                .username(finalUsername)
                .password(tempPassword)  // Mot de passe en CLAIR
                .dateCreationObject(LocalDateTime.now())
                .build());
            session.setAttribute("recentCredentials", recentCredentials);
            
            // Créer l'utilisateur (le mot de passe sera encodé par le service)
            Utilisateur utilisateur = Utilisateur.builder()
                .username(finalUsername)
                .passwordHash(tempPassword)  // sera encodé par le service
                .email(employe.getEmail())
                .role(Role.EMPLOYE)
                .statut(StatutUtilisateur.ACTIF)
                .dateCreation(LocalDateTime.now())
                .build();
            
            utilisateurService.createUtilisateur(utilisateur);
            
            redirectAttributes.addFlashAttribute("success", 
                "Identifiants créés avec succès pour " + employe.getPrenom() + " " + employe.getNom() + 
                " | Username: " + finalUsername + " | Mot de passe temporaire: " + tempPassword);
            
            return "redirect:/employes/generate-credentials";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la génération: " + e.getMessage());
            return "redirect:/employes/generate-credentials";
        }
    }
    
    /**
     * Générer tous les identifiants pour les employés sans compte
     */
    @PostMapping("/generate-all-credentials")
    public String generateAllCredentials(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            List<Employe> tousLesEmployes = employeService.findAll();
            int compteur = 0;
            
            // EFFACER la session précédente pour recommencer à zéro
            session.removeAttribute("recentCredentials");
            
            // Préparer une nouvelle liste pour la session
            List<CredentialDto> recentCredentials = new ArrayList<>();
            
            for (Employe employe : tousLesEmployes) {
                // Vérifier si l'employé a un email et n'a pas encore de compte
                if (employe.getEmail() != null && !employe.getEmail().trim().isEmpty()) {
                    if (!utilisateurService.getUtilisateurByEmail(employe.getEmail()).isPresent()) {
                        // Générer le nom d'utilisateur
                        String username = (employe.getPrenom() + "." + employe.getNom())
                            .toLowerCase()
                            .replaceAll("[^a-z0-9.]", "")
                            .replaceAll("\\.+", ".");
                        
                        int suffix = 1;
                        String finalUsername = username;
                        while (utilisateurService.existsByUsername(finalUsername)) {
                            finalUsername = username + suffix;
                            suffix++;
                        }
                        
                        String tempPassword = generateRandomPassword();
                        
                        // Stocker en session AVANT d'encoder
                        recentCredentials.add(CredentialDto.builder()
                            .employeId(employe.getId())
                            .employeNom(employe.getPrenom() + " " + employe.getNom())
                            .username(finalUsername)
                            .password(tempPassword)  // Mot de passe en CLAIR
                            .dateCreationObject(LocalDateTime.now())
                            .build());
                        
                        Utilisateur utilisateur = Utilisateur.builder()
                            .username(finalUsername)
                            .passwordHash(tempPassword)
                            .email(employe.getEmail())
                            .role(Role.EMPLOYE)
                            .statut(StatutUtilisateur.ACTIF)
                            .dateCreation(LocalDateTime.now())
                            .build();
                        
                        utilisateurService.createUtilisateur(utilisateur);
                        
                        compteur++;
                    }
                }
            }
            
            session.setAttribute("recentCredentials", recentCredentials);
            
            if (compteur > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    compteur + " compte(s) créé(s) avec succès ! Les identifiants sont disponibles ci-dessous.");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Aucun compte à créer. Tous les employés ont déjà un compte.");
            }
            
            return "redirect:/employes/generate-credentials";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la génération en masse: " + e.getMessage());
            return "redirect:/employes/generate-credentials";
        }
    }
    
    /**
     * Réinitialiser le mot de passe d'un employé
     */
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam Long employeId, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Optional<Employe> employeOpt = employeService.findById(employeId);
            if (!employeOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Employé introuvable");
                return "redirect:/employes/generate-credentials";
            }
            
            Employe employe = employeOpt.get();
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurByEmail(employe.getEmail());
            
            if (!utilisateurOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Aucun compte utilisateur trouvé pour cet employé");
                return "redirect:/employes/generate-credentials";
            }
            
            Utilisateur utilisateur = utilisateurOpt.get();
            String newPassword = generateRandomPassword();
            
            // Mettre à jour le mot de passe
            utilisateurService.updatePassword(utilisateur.getId(), newPassword);
            
            // Ajouter à la session pour le PDF/ZIP
            @SuppressWarnings("unchecked")
            List<CredentialDto> recentCredentials = (List<CredentialDto>) session.getAttribute("recentCredentials");
            if (recentCredentials == null) {
                recentCredentials = new ArrayList<>();
            }
            recentCredentials.add(CredentialDto.builder()
                .employeId(employe.getId())
                .employeNom(employe.getPrenom() + " " + employe.getNom())
                .username(utilisateur.getUsername())
                .password(newPassword)  // Mot de passe en CLAIR
                .dateCreationObject(LocalDateTime.now())
                .build());
            session.setAttribute("recentCredentials", recentCredentials);
            
            redirectAttributes.addFlashAttribute("success", 
                "Mot de passe réinitialisé pour " + employe.getPrenom() + " " + employe.getNom());
            
            return "redirect:/employes/generate-credentials";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la réinitialisation: " + e.getMessage());
            return "redirect:/employes/generate-credentials";
        }
    }
    
    /**
     * Réinitialiser tous les mots de passe
     */
    @PostMapping("/reset-all-passwords")
    public String resetAllPasswords(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            List<Employe> tousLesEmployes = employeService.findAll();
            int compteur = 0;
            
            // EFFACER la session précédente pour recommencer à zéro
            session.removeAttribute("recentCredentials");
            
            // Préparer une nouvelle liste pour la session
            List<CredentialDto> recentCredentials = new ArrayList<>();
            
            for (Employe employe : tousLesEmployes) {
                if (employe.getEmail() != null && !employe.getEmail().trim().isEmpty()) {
                    Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurByEmail(employe.getEmail());
                    if (utilisateurOpt.isPresent()) {
                        Utilisateur utilisateur = utilisateurOpt.get();
                        String newPassword = generateRandomPassword();
                        utilisateurService.updatePassword(utilisateur.getId(), newPassword);
                        
                        // Ajouter à la session pour le PDF/ZIP
                        recentCredentials.add(CredentialDto.builder()
                            .employeId(employe.getId())
                            .employeNom(employe.getPrenom() + " " + employe.getNom())
                            .username(utilisateur.getUsername())
                            .password(newPassword)  // Mot de passe en CLAIR
                            .dateCreationObject(LocalDateTime.now())
                            .build());
                        
                        compteur++;
                    }
                }
            }
            
            session.setAttribute("recentCredentials", recentCredentials);
            
            if (compteur > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    compteur + " mot(s) de passe réinitialisé(s) avec succès ! Les nouveaux identifiants sont disponibles ci-dessous.");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Aucun mot de passe à réinitialiser.");
            }
            
            return "redirect:/employes/generate-credentials";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la réinitialisation en masse: " + e.getMessage());
            return "redirect:/employes/generate-credentials";
        }
    }
    
    /**
     * Effacer la session des identifiants générés
     */
    @PostMapping("/clear-credentials-session")
    public String clearCredentialsSession(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("recentCredentials");
        redirectAttributes.addFlashAttribute("success", "La liste des identifiants a été effacée de la session.");
        return "redirect:/employes/generate-credentials";
    }
    
    /**
     * Générer un mot de passe aléatoire
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    /**
     * Exporter les identifiants en PDF (tableau de tous les identifiants générés)
     */
    @GetMapping("/export-credentials-pdf")
    public ResponseEntity<ByteArrayResource> exportCredentialsPDF(HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            List<CredentialDto> credentials = (List<CredentialDto>) session.getAttribute("recentCredentials");
            
            if (credentials == null || credentials.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Récupérer les employés pour avoir leurs informations
            List<Employe> employes = employeService.findAll();
            
            // Générer le PDF avec iText
            byte[] pdfBytes = com.gestionrh.projetfinalspringboot.util.PdfCredentialsUtil
                .generateCredentialsTablePDF(credentials, employes);
            
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=identifiants_employes.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
                
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Exporter les identifiants en ZIP (un PDF par employé)
     */
    @GetMapping("/export-credentials-zip")
    public ResponseEntity<ByteArrayResource> exportCredentialsZIP(HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            List<CredentialDto> credentials = (List<CredentialDto>) session.getAttribute("recentCredentials");
            
            if (credentials == null || credentials.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Récupérer tous les employés
            List<Employe> employes = employeService.findAll();
            
            String zipFilename = "identifiants_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".zip";
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                
                for (CredentialDto cred : credentials) {
                    // Trouver l'employé correspondant
                    Employe emp = employes.stream()
                        .filter(e -> e.getId().equals(cred.getEmployeId()))
                        .findFirst()
                        .orElse(null);
                    
                    if (emp == null) continue;
                    
                    // Créer le nom du fichier PDF
                    String pdfFilename = "identifiant_" + emp.getNom() + emp.getPrenom() + ".pdf";
                    pdfFilename = pdfFilename.replaceAll("[^a-zA-Z0-9._-]", "");
                    
                    // Créer l'entrée ZIP
                    ZipEntry zipEntry = new ZipEntry(pdfFilename);
                    zos.putNextEntry(zipEntry);
                    
                    // Générer le PDF individuel
                    byte[] pdfBytes = com.gestionrh.projetfinalspringboot.util.PdfCredentialsUtil
                        .generateIndividualCredentialPDF(cred, emp);
                    zos.write(pdfBytes);
                    zos.closeEntry();
                }
            }
            
            byte[] data = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(data);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
                
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprimer un employé
     */
    @PostMapping("/delete/{id}")
    public String deleteEmploye(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Employe> employe = employeService.findById(id);
            if (employe.isPresent()) {
                employeService.deleteById(id);
                redirectAttributes.addFlashAttribute("message", 
                    "Employé " + employe.get().getPrenom() + " " + employe.get().getNom() + " supprimé avec succès");
            } else {
                redirectAttributes.addFlashAttribute("error", "Employé introuvable");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/employes/list";
    }
}
