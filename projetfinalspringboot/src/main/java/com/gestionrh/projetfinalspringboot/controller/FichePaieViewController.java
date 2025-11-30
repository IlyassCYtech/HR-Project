package com.gestionrh.projetfinalspringboot.controller;

import com.gestionrh.projetfinalspringboot.model.entity.FichePaie;
import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.model.enums.Role;
import com.gestionrh.projetfinalspringboot.service.EmployeService;
import com.gestionrh.projetfinalspringboot.service.FichePaieService;
import com.gestionrh.projetfinalspringboot.service.PdfService;
import com.gestionrh.projetfinalspringboot.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/fiches-paie")
@RequiredArgsConstructor
public class FichePaieViewController {

    private final FichePaieService fichePaieService;
    private final EmployeService employeService;
    private final PdfService pdfService;
    private final UtilisateurService utilisateurService;

    @GetMapping({ "/list", "" })
    public String listFichesPaie(Model model,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) Long departementId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String mois) {

        // *** SÉCURITÉ : Récupérer l'utilisateur connecté ***
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);

        if (utilisateurOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé");
            return "redirect:/login";
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        Role userRole = utilisateur.getRole();

        List<FichePaie> fichesPaie;

        // *** RESTRICTION : Si l'utilisateur n'est ni RH ni ADMIN, ne montrer que ses
        // propres fiches ***
        if (userRole != Role.RH && userRole != Role.ADMIN) {
            // Vérifier que l'utilisateur a un employé lié
            if (utilisateur.getEmploye() == null) {
                redirectAttributes.addFlashAttribute("error", "Vous n'avez pas accès à cette fonctionnalité");
                return "redirect:/dashboard";
            }

            // IMPORTANT : Forcer l'employeId et ignorer les autres filtres de recherche
            Long employeIdForce = utilisateur.getEmploye().getId();

            // Récupérer les fiches de l'employé
            fichesPaie = fichePaieService.findByEmployeId(employeIdForce);

            // Appliquer UNIQUEMENT le filtre mois si présent
            if (mois != null && !mois.isEmpty()) {
                try {
                    String[] parts = mois.split("-");
                    if (parts.length == 2) {
                        final int annee = Integer.parseInt(parts[0]);
                        final int moisNum = Integer.parseInt(parts[1]);
                        fichesPaie = fichesPaie.stream()
                                .filter(f -> f.getMois() != null && f.getAnnee() != null &&
                                        f.getMois() == moisNum && f.getAnnee() == annee)
                                .toList();
                    }
                } catch (NumberFormatException e) {
                    // Si erreur de parsing, ignorer le filtre
                    System.err.println("Erreur parsing mois pour employé: " + e.getMessage());
                }
            }
        } else {
            // *** RH et ADMIN peuvent voir toutes les fiches avec tous les filtres ***

            // Commencer avec toutes les fiches
            fichesPaie = fichePaieService.findAll();

            // Appliquer le filtre mois EN PREMIER
            if (mois != null && !mois.isEmpty()) {
                try {
                    String[] parts = mois.split("-");
                    if (parts.length == 2) {
                        final int annee = Integer.parseInt(parts[0]);
                        final int moisNum = Integer.parseInt(parts[1]);
                        fichesPaie = fichesPaie.stream()
                                .filter(f -> f.getMois() != null && f.getAnnee() != null &&
                                        f.getMois() == moisNum && f.getAnnee() == annee)
                                .toList();
                    }
                } catch (NumberFormatException e) {
                    // Si erreur de parsing, ignorer le filtre
                    System.err.println("Erreur parsing mois: " + e.getMessage());
                }
            }

            // Filtre par employé (UNIQUEMENT pour RH/ADMIN)
            if (employeId != null) {
                final Long employeIdFinal = employeId;
                fichesPaie = fichesPaie.stream()
                        .filter(f -> f.getEmploye() != null && f.getEmploye().getId().equals(employeIdFinal))
                        .toList();
            }

            // Filtre par département (UNIQUEMENT pour RH/ADMIN)
            if (departementId != null) {
                final Long departementIdFinal = departementId;
                fichesPaie = fichesPaie.stream()
                        .filter(f -> f.getEmploye() != null && f.getEmploye().getDepartement() != null
                                && f.getEmploye().getDepartement().getId().equals(departementIdFinal))
                        .toList();
            }

            // Recherche par nom (UNIQUEMENT pour RH/ADMIN)
            if (search != null && !search.trim().isEmpty()) {
                final String searchLower = search.toLowerCase().trim();
                fichesPaie = fichesPaie.stream()
                        .filter(f -> f.getEmploye() != null &&
                                (f.getEmploye().getNom().toLowerCase().contains(searchLower) ||
                                        f.getEmploye().getPrenom().toLowerCase().contains(searchLower) ||
                                        f.getEmploye().getMatricule().toLowerCase().contains(searchLower)))
                        .toList();
            }
        }

        // Trier les fiches par date décroissante (plus récent en premier)
        fichesPaie = fichesPaie.stream()
                .sorted((f1, f2) -> {
                    int compareAnnee = Integer.compare(
                            f2.getAnnee() != null ? f2.getAnnee() : 0,
                            f1.getAnnee() != null ? f1.getAnnee() : 0);
                    if (compareAnnee != 0)
                        return compareAnnee;
                    return Integer.compare(
                            f2.getMois() != null ? f2.getMois() : 0,
                            f1.getMois() != null ? f1.getMois() : 0);
                })
                .toList();

        model.addAttribute("fichesPaie", fichesPaie);

        // Calculer la masse salariale (UNIQUEMENT pour RH/ADMIN)
        if (userRole == Role.RH || userRole == Role.ADMIN) {
            java.math.BigDecimal masseSalariale;
            if (mois != null && !mois.isEmpty()) {
                try {
                    String[] parts = mois.split("-");
                    if (parts.length == 2) {
                        int annee = Integer.parseInt(parts[0]);
                        int moisNum = Integer.parseInt(parts[1]);
                        masseSalariale = fichePaieService.calculateMasseSalarialeForPeriod(moisNum, annee);
                    } else {
                        masseSalariale = fichePaieService.calculateMasseSalariale();
                    }
                } catch (NumberFormatException e) {
                    masseSalariale = fichePaieService.calculateMasseSalariale();
                }
            } else {
                masseSalariale = fichePaieService.calculateMasseSalariale();
            }
            model.addAttribute("masseSalariale", masseSalariale);
        }

        // Ajouter les listes pour les filtres (UNIQUEMENT pour RH/ADMIN)
        if (userRole == Role.RH || userRole == Role.ADMIN) {
            model.addAttribute("employes", employeService.findAll());
            model.addAttribute("departements", employeService.findAll().stream()
                    .map(emp -> emp.getDepartement())
                    .filter(dept -> dept != null)
                    .distinct()
                    .toList());
        }

        model.addAttribute("activePage", "fiches-paie");
        return "fiches-paie/list";
    }

    @GetMapping("/generate")
    public String showGenerateForm(Model model) {
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("departements", employeService.findAll().stream()
                .map(e -> e.getDepartement())
                .filter(d -> d != null)
                .distinct()
                .toList());
        long employesActifs = employeService.findAll().stream()
                .filter(e -> e.getStatut() != null && e.getStatut().name().equals("ACTIF"))
                .count();
        model.addAttribute("employesActifs", employesActifs);
        model.addAttribute("moisActuel", java.time.LocalDate.now().getMonthValue());
        model.addAttribute("anneeActuelle", java.time.LocalDate.now().getYear());
        return "fiches-paie/generate";
    }

    @PostMapping("/generate")
    public String generateAllFichesPaie(@RequestParam int mois, @RequestParam int annee,
            RedirectAttributes redirectAttributes) {
        try {
            int generated = fichePaieService.generateAllFichesPaie(employeService.findAll(), mois, annee);
            redirectAttributes.addFlashAttribute("success",
                    generated + " fiches de paie générées pour " + getMoisNom(mois) + " " + annee);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la génération: " + e.getMessage());
        }
        return "redirect:/fiches-paie/list";
    }

    @PostMapping("/generate-one")
    public String generateOneFichePaie(@RequestParam Long employeId,
            @RequestParam int mois,
            @RequestParam int annee,
            RedirectAttributes redirectAttributes) {
        try {
            var employe = employeService.findById(employeId)
                    .orElseThrow(() -> new RuntimeException("Employé introuvable"));

            // Générer la fiche pour cet employé
            fichePaieService.generateAllFichesPaie(List.of(employe), mois, annee);

            redirectAttributes.addFlashAttribute("success",
                    "Fiche de paie générée pour " + employe.getPrenom() + " " + employe.getNom() +
                            " - " + getMoisNom(mois) + " " + annee);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la génération: " + e.getMessage());
        }
        return "redirect:/fiches-paie/list";
    }

    @GetMapping("/employee/{employeId}")
    public String listFichesPaieByEmploye(@PathVariable Long employeId, Model model,
            RedirectAttributes redirectAttributes) {
        var employe = employeService.findById(employeId);
        if (employe.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Employé introuvable");
            return "redirect:/employes/list";
        }
        model.addAttribute("employe", employe.get());
        model.addAttribute("fichesPaie", fichePaieService.findByEmployeId(employeId));
        model.addAttribute("activePage", "fiches-paie");
        return "fiches-paie/employee";
    }

    @GetMapping("/pdf/{id}")
    public void downloadFichePaiePdf(@PathVariable Long id, HttpServletResponse response) {
        // *** SÉCURITÉ : Vérifier les droits d'accès ***
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);

        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        fichePaieService.findById(id).ifPresent(fiche -> {
            // *** RESTRICTION : Si ni RH ni ADMIN, vérifier que c'est sa propre fiche ***
            if (utilisateur.getRole() != Role.RH && utilisateur.getRole() != Role.ADMIN) {
                if (utilisateur.getEmploye() == null ||
                        !fiche.getEmploye().getId().equals(utilisateur.getEmploye().getId())) {
                    throw new RuntimeException(
                            "Accès refusé : vous ne pouvez télécharger que vos propres fiches de paie");
                }
            }

            try {
                byte[] pdf = pdfService.generateFichePaiePdf(fiche);
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=fiche_paie_" + id + ".pdf");
                response.getOutputStream().write(pdf);
                response.getOutputStream().flush();
            } catch (Exception e) {
                throw new RuntimeException("Erreur export PDF", e);
            }
        });
    }

    @GetMapping("/form")
    public String showCreateForm(Model model) {
        model.addAttribute("fichePaie", new FichePaie());
        model.addAttribute("employes", employeService.findAll());
        return "fiches-paie/edit";
    }

    @GetMapping("/form/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<FichePaie> fichePaie = fichePaieService.findById(id);
        if (fichePaie.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Fiche de paie introuvable");
            return "redirect:/fiches-paie/list";
        }
        model.addAttribute("fichePaie", fichePaie.get());
        model.addAttribute("employes", employeService.findAll());
        return "fiches-paie/edit";
    }

    @PostMapping("/create")
    public String createFichePaie(@ModelAttribute FichePaie fichePaie, RedirectAttributes redirectAttributes) {
        try {
            fichePaieService.save(fichePaie);
            redirectAttributes.addFlashAttribute("message", "Fiche de paie créée");
            return "redirect:/fiches-paie/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/fiches-paie/form";
        }
    }

    @PostMapping("/update/{id}")
    public String updateFichePaie(@PathVariable Long id, @ModelAttribute FichePaie fichePaie,
            RedirectAttributes redirectAttributes) {
        try {
            // VALIDATION : Le salaire NET ne peut pas être négatif
            if (fichePaie.getNetAPayer() != null
                    && fichePaie.getNetAPayer().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error",
                        "Le salaire NET à payer ne peut pas être négatif ou nul (" + fichePaie.getNetAPayer() + " €)");
                return "redirect:/fiches-paie/form/" + id;
            }

            // Vérification cohérence : salaire base > cotisations + impôts (si tous les
            // champs sont présents)
            if (fichePaie.getSalaireBase() != null && fichePaie.getCotisationsSociales() != null) {
                java.math.BigDecimal total = fichePaie.getCotisationsSociales();
                // Ajouter impôts si présent (le champ peut être nommé différemment)
                if (fichePaie.getImpots() != null) {
                    total = total.add(fichePaie.getImpots());
                }
                if (fichePaie.getSalaireBase().compareTo(total) < 0) {
                    redirectAttributes.addFlashAttribute("error",
                            "Le salaire de base (" + fichePaie.getSalaireBase()
                                    + " €) est insuffisant pour couvrir les charges (" + total + " €)");
                    return "redirect:/fiches-paie/form/" + id;
                }
            }

            fichePaie.setId(id);
            fichePaieService.save(fichePaie);
            redirectAttributes.addFlashAttribute("message", "Fiche de paie mise à jour");
            return "redirect:/fiches-paie/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/fiches-paie/form/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteFichePaie(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            fichePaieService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Fiche de paie supprimée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/fiches-paie/list";
    }

    @GetMapping("/show/{id}")
    public String showFichePaie(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        // *** SÉCURITÉ : Vérifier les droits d'accès ***
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);

        if (utilisateurOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé");
            return "redirect:/login";
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        Optional<FichePaie> fichePaie = fichePaieService.findById(id);
        if (fichePaie.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Fiche de paie introuvable");
            return "redirect:/fiches-paie/list";
        }

        // *** RESTRICTION : Si ni RH ni ADMIN, vérifier que c'est sa propre fiche ***
        if (utilisateur.getRole() != Role.RH && utilisateur.getRole() != Role.ADMIN) {
            if (utilisateur.getEmploye() == null ||
                    !fichePaie.get().getEmploye().getId().equals(utilisateur.getEmploye().getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "Accès refusé : vous ne pouvez consulter que vos propres fiches de paie");
                return "redirect:/fiches-paie/list";
            }
        }

        model.addAttribute("fichePaie", fichePaie.get());
        model.addAttribute("activePage", "fiches-paie");
        return "fiches-paie/show";
    }

    @GetMapping("/edit/{id}")
    public String showEditFormPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<FichePaie> fichePaie = fichePaieService.findById(id);
        if (fichePaie.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Fiche de paie introuvable");
            return "redirect:/fiches-paie/list";
        }
        model.addAttribute("fichePaie", fichePaie.get());
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("activePage", "fiches-paie");
        return "fiches-paie/edit";
    }

    /**
     * Télécharger toutes les fiches de paie filtrées dans un fichier ZIP
     * Applique les mêmes filtres que l'affichage de la liste
     */
    @GetMapping("/download-all-zip")
    public void downloadAllFichesZip(HttpServletResponse response,
            @RequestParam(required = false) String mois,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String departement) {
        try {
            List<FichePaie> fiches;
            String filename;

            // Commencer avec toutes les fiches ou celles d'un employé
            if (employeId != null) {
                fiches = fichePaieService.findByEmployeId(employeId);
                filename = "fiches_employe_" + employeId + ".zip";
            } else {
                fiches = fichePaieService.findAll();
                filename = "toutes_fiches_paie.zip";
            }

            // Appliquer le filtre de mois si présent
            if (mois != null && !mois.isEmpty()) {
                String[] parts = mois.split("-");
                int annee = Integer.parseInt(parts[0]);
                int moisNum = Integer.parseInt(parts[1]);
                fiches = fiches.stream()
                        .filter(f -> f.getMois() != null && f.getAnnee() != null &&
                                f.getMois() == moisNum && f.getAnnee() == annee)
                        .toList();
                filename = "fiches_paie_" + String.format("%02d", moisNum) + "_" + annee + ".zip";
            }

            // Appliquer le filtre de recherche par nom
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                fiches = fiches.stream()
                        .filter(f -> {
                            String nomComplet = (f.getEmploye().getPrenom() + " " + f.getEmploye().getNom())
                                    .toLowerCase();
                            return nomComplet.contains(searchLower);
                        })
                        .toList();
            }

            // Appliquer le filtre de département
            if (departement != null && !departement.isEmpty()) {
                if ("none".equals(departement)) {
                    // Filtrer les employés sans département
                    fiches = fiches.stream()
                            .filter(f -> f.getEmploye().getDepartement() == null)
                            .toList();
                } else {
                    // Filtrer par département spécifique
                    Long deptId = Long.parseLong(departement);
                    fiches = fiches.stream()
                            .filter(f -> f.getEmploye().getDepartement() != null &&
                                    f.getEmploye().getDepartement().getId().equals(deptId))
                            .toList();
                }
            }

            // Vérifier qu'il y a des fiches à télécharger
            if (fiches.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Aucune fiche de paie trouvée avec ces filtres");
                return;
            }

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try (java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
                for (FichePaie fiche : fiches) {
                    byte[] pdf = pdfService.generateFichePaiePdf(fiche);
                    // Nettoyer le nom de fichier pour éviter les caractères problématiques
                    String nomClean = fiche.getEmploye().getNom().replaceAll("[^a-zA-Z0-9]", "_");
                    // Ajouter l'ID de la fiche pour garantir l'unicité
                    String entryName = "fiche_" + nomClean + "_" +
                            String.format("%02d", fiche.getMois()) + "_" +
                            fiche.getAnnee() + "_" + fiche.getId() + ".pdf";
                    java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(entryName);
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(pdf);
                    zipOut.closeEntry();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création du ZIP: " + e.getMessage(), e);
        }
    }

    /**
     * Télécharger toutes les fiches de paie filtrées dans un fichier ZIP (méthode
     * POST)
     * Applique les mêmes filtres que l'affichage de la liste
     */
    @PostMapping("/download-all-zip")
    public void downloadAllFichesZipPost(HttpServletResponse response,
            @RequestParam(required = false) String mois,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String departement) {
        // Appeler la même logique que la méthode GET
        downloadAllFichesZip(response, mois, employeId, search, departement);
    }

    /**
     * Télécharger toutes les fiches de paie d'un employé par nom d'utilisateur ou
     * ID
     * *** SÉCURITÉ : Accepte soit un ID numérique soit un username ***
     */
    @GetMapping("/download-employee-zip/{identifier}")
    public void downloadEmployeeFichesZipByIdentifier(@PathVariable String identifier, HttpServletResponse response) {
        try {
            // *** SÉCURITÉ : Vérifier les droits d'accès ***
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);

            if (utilisateurOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Utilisateur non trouvé");
                return;
            }

            Utilisateur utilisateur = utilisateurOpt.get();
            com.gestionrh.projetfinalspringboot.model.entity.Employe employe = null;

            // Essayer de parser comme ID numérique d'abord
            try {
                Long employeId = Long.parseLong(identifier);
                employe = employeService.findById(employeId).orElse(null);
            } catch (NumberFormatException e) {
                // Si ce n'est pas un nombre, chercher par nom d'utilisateur
                Optional<Utilisateur> targetUserOpt = utilisateurService.findByUsername(identifier);
                if (targetUserOpt.isPresent() && targetUserOpt.get().getEmploye() != null) {
                    employe = targetUserOpt.get().getEmploye();
                }
            }

            if (employe == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employé introuvable");
                return;
            }

            // *** RESTRICTION : Si ni RH ni ADMIN ni CHEF_DEPT, vérifier que c'est ses
            // propres fiches ***
            if (utilisateur.getRole() != Role.RH &&
                    utilisateur.getRole() != Role.ADMIN &&
                    utilisateur.getRole() != Role.CHEF_DEPT) {
                if (utilisateur.getEmploye() == null ||
                        !utilisateur.getEmploye().getId().equals(employe.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Accès refusé : vous ne pouvez télécharger que vos propres fiches");
                    return;
                }
            }

            List<FichePaie> fiches = fichePaieService.findByEmployeId(employe.getId());

            // Vérifier qu'il y a des fiches à télécharger
            if (fiches.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Aucune fiche de paie trouvée pour cet employé");
                return;
            }

            // Nettoyer les noms pour éviter les caractères problématiques
            String nomClean = employe.getNom().replaceAll("[^a-zA-Z0-9]", "_");
            String prenomClean = employe.getPrenom().replaceAll("[^a-zA-Z0-9]", "_");
            String filename = "fiches_" + nomClean + "_" + prenomClean + ".zip";

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try (java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
                for (FichePaie fiche : fiches) {
                    byte[] pdf = pdfService.generateFichePaiePdf(fiche);
                    String entryName = "fiche_" +
                            String.format("%02d", fiche.getMois()) + "_" +
                            fiche.getAnnee() + ".pdf";
                    java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(entryName);
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(pdf);
                    zipOut.closeEntry();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création du ZIP: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne le nom du mois en français
     */
    private String getMoisNom(int mois) {
        String[] moisNoms = { "", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre" };
        return moisNoms[mois];
    }
}
