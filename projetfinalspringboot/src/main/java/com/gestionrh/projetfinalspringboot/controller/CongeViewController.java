package com.gestionrh.projetfinalspringboot.controller;

import com.gestionrh.projetfinalspringboot.model.entity.CongeAbsence;
import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.service.CongeService;
import com.gestionrh.projetfinalspringboot.service.EmployeService;
import com.gestionrh.projetfinalspringboot.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
@RequestMapping("/conges")
@RequiredArgsConstructor
public class CongeViewController {

    private final CongeService congeService;
    private final EmployeService employeService;
    private final UtilisateurService utilisateurService;

    @GetMapping({ "/list", "" })
    public String listConges(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Long employeId,
            Model model) {

        var conges = congeService.findAll();

        // Filtrer par employé
        if (employeId != null) {
            conges = conges.stream()
                    .filter(c -> c.getEmploye() != null &&
                            c.getEmploye().getId().equals(employeId))
                    .toList();
        }

        // Filtrer par type
        if (type != null && !type.isEmpty() && !type.equals("TOUS")) {
            conges = conges.stream()
                    .filter(c -> c.getTypeConge() != null &&
                            c.getTypeConge().name().equals(type))
                    .toList();
        }

        // Filtrer par statut
        if (statut != null && !statut.isEmpty() && !statut.equals("TOUS")) {
            conges = conges.stream()
                    .filter(c -> c.getStatut() != null &&
                            c.getStatut().name().equals(statut))
                    .toList();
        }

        model.addAttribute("conges", conges);
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("activePage", "conges");
        return "conges/list";
    }

    @GetMapping("/form")
    public String showCreateForm(Authentication authentication, Model model) {
        CongeAbsence conge = new CongeAbsence();

        // Récupérer l'utilisateur connecté et son employé
        String username = authentication.getName();
        Utilisateur utilisateur = utilisateurService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Pré-remplir avec l'employé connecté si disponible
        if (utilisateur.getEmploye() != null) {
            conge.setEmploye(utilisateur.getEmploye());
        }

        model.addAttribute("conge", conge);
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("currentEmploye", utilisateur.getEmploye());
        return "conges/form";
    }

    @GetMapping("/form/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<CongeAbsence> conge = congeService.findById(id);
        if (conge.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Congé introuvable");
            return "redirect:/conges/list";
        }
        // Vérifier si le congé est déjà traité (APPROUVE ou REFUSE)
        if (conge.get().getStatut() == com.gestionrh.projetfinalspringboot.model.enums.StatutConge.APPROUVE ||
                conge.get().getStatut() == com.gestionrh.projetfinalspringboot.model.enums.StatutConge.REFUSE) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de modifier une demande déjà traitée (Approuvée ou Refusée)");
            return "redirect:/conges/list";
        }

        model.addAttribute("conge", conge.get());
        model.addAttribute("employes", employeService.findAll());
        return "conges/form";
    }

    @PostMapping("/create")
    public String createConge(@ModelAttribute CongeAbsence conge, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Récupérer l'utilisateur connecté
            String username = authentication.getName();
            Utilisateur utilisateur = utilisateurService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Vérifier si c'est un ADMIN/RH
            boolean isAdminOrRH = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") ||
                            auth.getAuthority().equals("ROLE_RH"));

            // Charger l'employé comme entité gérée
            if (conge.getEmploye() != null && conge.getEmploye().getId() != null) {
                // Si ADMIN/RH, utiliser l'employé sélectionné
                // Sinon, forcer l'employé connecté
                if (isAdminOrRH) {
                    employeService.findById(conge.getEmploye().getId())
                            .ifPresent(conge::setEmploye);
                } else {
                    // Pour non-ADMIN/RH, toujours utiliser l'employé connecté
                    if (utilisateur.getEmploye() != null) {
                        conge.setEmploye(utilisateur.getEmploye());
                    } else {
                        redirectAttributes.addFlashAttribute("error",
                                "Vous devez être lié à un employé pour créer une demande");
                        return "redirect:/conges/form";
                    }
                }
            } else {
                // Si pas d'employé sélectionné, utiliser l'employé connecté
                if (utilisateur.getEmploye() != null) {
                    conge.setEmploye(utilisateur.getEmploye());
                } else {
                    redirectAttributes.addFlashAttribute("error", "L'employé est obligatoire");
                    return "redirect:/conges/form";
                }
            }

            // Calculer automatiquement le nombre de jours entre dateDebut et dateFin
            if (conge.getDateDebut() != null && conge.getDateFin() != null) {
                long nombreJours = ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin()) + 1;
                conge.setNombreJours((int) nombreJours);
            }

            // Charger l'approbateur comme entité gérée si un ID est fourni
            if (conge.getApprouvePar() != null && conge.getApprouvePar().getId() != null) {
                employeService.findById(conge.getApprouvePar().getId())
                        .ifPresent(conge::setApprouvePar);
            } else {
                conge.setApprouvePar(null);
            }

            // Définir le statut par défaut à EN_ATTENTE lors de la création
            if (conge.getStatut() == null) {
                conge.setStatut(com.gestionrh.projetfinalspringboot.model.enums.StatutConge.EN_ATTENTE);
            }

            congeService.save(conge);
            redirectAttributes.addFlashAttribute("message", "Demande de congé créée");
            return "redirect:/conges/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/conges/form";
        }
    }

    @PostMapping("/update/{id}")
    public String updateConge(@PathVariable Long id, @ModelAttribute CongeAbsence conge,
            RedirectAttributes redirectAttributes) {
        try {
            // Charger le congé existant
            Optional<CongeAbsence> existingOpt = congeService.findById(id);
            if (!existingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Congé non trouvé");
                return "redirect:/conges/list";
            }

            CongeAbsence existing = existingOpt.get();

            // Vérifier si le congé est déjà traité (APPROUVE ou REFUSE)
            if (existing.getStatut() == com.gestionrh.projetfinalspringboot.model.enums.StatutConge.APPROUVE ||
                    existing.getStatut() == com.gestionrh.projetfinalspringboot.model.enums.StatutConge.REFUSE) {
                redirectAttributes.addFlashAttribute("error",
                        "Impossible de modifier une demande déjà traitée (Approuvée ou Refusée)");
                return "redirect:/conges/list";
            }

            // Mettre à jour les champs simples
            existing.setTypeConge(conge.getTypeConge());
            existing.setDateDebut(conge.getDateDebut());
            existing.setDateFin(conge.getDateFin());
            existing.setNombreJours(conge.getNombreJours());
            existing.setMotif(conge.getMotif());
            existing.setStatut(conge.getStatut());
            existing.setCommentairesApprobation(conge.getCommentairesApprobation());

            // Charger l'employé comme entité gérée si un ID est fourni
            if (conge.getEmploye() != null && conge.getEmploye().getId() != null) {
                employeService.findById(conge.getEmploye().getId())
                        .ifPresent(existing::setEmploye);
            }

            // Charger l'approbateur comme entité gérée si un ID est fourni
            if (conge.getApprouvePar() != null && conge.getApprouvePar().getId() != null) {
                employeService.findById(conge.getApprouvePar().getId())
                        .ifPresent(existing::setApprouvePar);
            } else {
                existing.setApprouvePar(null);
            }

            congeService.save(existing);
            redirectAttributes.addFlashAttribute("message", "Congé mis à jour");
            return "redirect:/conges/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            return "redirect:/conges/form/" + id;
        }
    }

    @GetMapping("/show/{id}")
    public String showConge(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<CongeAbsence> conge = congeService.findById(id);
        if (conge.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Congé introuvable");
            return "redirect:/conges/list";
        }
        model.addAttribute("conge", conge.get());
        return "conges/show";
    }

    @PostMapping("/delete/{id}")
    public String deleteConge(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            congeService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Congé supprimé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/conges/list";
    }

    @GetMapping("/en-attente")
    public String congesEnAttente(Model model) {
        model.addAttribute("congesEnAttente", congeService.findCongesEnAttente());
        model.addAttribute("stats", congeService.getStatsConges());
        return "conges/en-attente";
    }

    @GetMapping("/mes-conges")
    public String mesConges(Model model) {
        model.addAttribute("conges", congeService.findMesConges());
        model.addAttribute("soldeConges", congeService.getSoldeConges());
        model.addAttribute("nombreDemandes", congeService.countMesDemandes());
        return "conges/mes-conges";
    }

    @PostMapping("/approve/{id}")
    public String approveConge(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Récupérer l'utilisateur connecté
            String username = authentication.getName();
            Utilisateur utilisateur = utilisateurService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (utilisateur.getEmploye() == null) {
                throw new RuntimeException("Aucun employé associé à cet utilisateur");
            }

            // Approuver le congé avec l'employé connecté comme approbateur
            congeService.approveConge(id, utilisateur.getEmploye(), commentaire);
            redirectAttributes.addFlashAttribute("message", "Congé approuvé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/conges/show/" + id;
    }

    @PostMapping("/reject/{id}")
    public String rejectConge(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Récupérer l'utilisateur connecté
            String username = authentication.getName();
            Utilisateur utilisateur = utilisateurService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (utilisateur.getEmploye() == null) {
                throw new RuntimeException("Aucun employé associé à cet utilisateur");
            }

            // Rejeter le congé avec l'employé connecté comme approbateur
            congeService.rejectConge(id, utilisateur.getEmploye(), commentaire);
            redirectAttributes.addFlashAttribute("message", "Congé rejeté avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/conges/show/" + id;
    }
}
