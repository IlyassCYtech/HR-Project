package com.gestionrh.projetfinalspringboot.controller;

import com.gestionrh.projetfinalspringboot.model.entity.Employe;
import com.gestionrh.projetfinalspringboot.model.entity.EmployeProjet;
import com.gestionrh.projetfinalspringboot.model.entity.Projet;
import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.model.enums.PrioriteProjet;
import com.gestionrh.projetfinalspringboot.model.enums.StatutProjet;
import com.gestionrh.projetfinalspringboot.repository.EmployeProjetRepository;
import com.gestionrh.projetfinalspringboot.service.DepartementService;
import com.gestionrh.projetfinalspringboot.service.EmployeService;
import com.gestionrh.projetfinalspringboot.service.ProjetService;
import com.gestionrh.projetfinalspringboot.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projets")
@RequiredArgsConstructor
public class ProjetViewController {

    private final ProjetService projetService;
    private final DepartementService departementService;
    private final EmployeService employeService;
    private final EmployeProjetRepository employeProjetRepository;
    private final UtilisateurService utilisateurService;

    @GetMapping({"/list", ""})
    @Transactional(readOnly = true)
    public String listProjets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Long departementId,
            Authentication authentication,
            Model model) {
        
        // Récupérer l'utilisateur connecté
        String username = authentication.getName();
        Utilisateur utilisateur = utilisateurService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer les projets avec les relations chargées (JOIN FETCH)
        var projets = projetService.findAllWithDetails();
        
        // Restriction pour les utilisateurs non-admin/RH : voir uniquement leurs projets
        boolean isAdminOrRH = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                                 auth.getAuthority().equals("ROLE_RH"));
        
        if (!isAdminOrRH && utilisateur.getEmploye() != null) {
            // Filtrer pour ne montrer que les projets où l'employé est assigné
            Long employeId = utilisateur.getEmploye().getId();
            projets = projets.stream()
                    .filter(p -> p.getEmployes() != null && 
                           p.getEmployes().stream()
                                   .anyMatch(ep -> ep.getEmploye() != null && 
                                                  ep.getEmploye().getId().equals(employeId)))
                    .toList();
        }
        
        // Filtrer par recherche (nom du projet)
        if (search != null && !search.isEmpty()) {
            projets = projets.stream()
                    .filter(p -> p.getNom() != null && 
                           p.getNom().toLowerCase().contains(search.toLowerCase()))
                    .toList();
        }
        
        // Filtrer par statut
        if (statut != null && !statut.isEmpty()) {
            projets = projets.stream()
                    .filter(p -> p.getStatut() != null && 
                           p.getStatut().name().equals(statut))
                    .toList();
        }
        
        // Filtrer par département
        if (departementId != null) {
            projets = projets.stream()
                    .filter(p -> p.getDepartement() != null && 
                           p.getDepartement().getId().equals(departementId))
                    .toList();
        }
        
        model.addAttribute("projets", projets);
        model.addAttribute("departements", departementService.findAll());
        model.addAttribute("isAdminOrRH", isAdminOrRH);
        model.addAttribute("page", "projets");
        return "projets/list";
    }

    @GetMapping("/form")
    public String showCreateForm(Model model) {
        model.addAttribute("projet", new Projet());
        model.addAttribute("departements", departementService.findAll());
        model.addAttribute("employes", employeService.findAll());
        return "projets/form";
    }

    @GetMapping("/form/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        // Charger le projet avec ses relations pour l'édition
        Optional<Projet> projet = projetService.findByIdComplete(id);
        if (projet.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Projet introuvable");
            return "redirect:/projets/list";
        }
        model.addAttribute("projet", projet.get());
        model.addAttribute("departements", departementService.findAll());
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("page", "projets");
        return "projets/form";
    }

    @PostMapping("/create")
    public String createProjet(@RequestParam("nom") String nom,
                               @RequestParam("description") String description,
                               @RequestParam("statut") StatutProjet statut,
                               @RequestParam("priorite") PrioriteProjet priorite,
                               @RequestParam("dateDebut") String dateDebut,
                               @RequestParam("dateFinPrevue") String dateFinPrevue,
                               @RequestParam(value = "chefProjetId", required = false) Long chefProjetId,
                               @RequestParam(value = "departementId", required = false) Long departementId,
                               @RequestParam(value = "employeIds", required = false) List<Long> employeIds,
                               RedirectAttributes redirectAttributes) {
        try {
            Projet projet = new Projet();
            projet.setNom(nom);
            projet.setDescription(description);
            projet.setStatut(statut);
            projet.setPriorite(priorite);
            projet.setDateDebut(java.time.LocalDate.parse(dateDebut));
            projet.setDateFinPrevue(java.time.LocalDate.parse(dateFinPrevue));
            
            // Charger le chef de projet comme entité gérée si un ID est fourni
            if (chefProjetId != null && chefProjetId > 0) {
                employeService.findById(chefProjetId)
                    .ifPresent(projet::setChefProjet);
            }
            
            // Charger le département comme entité gérée si un ID est fourni
            if (departementId != null && departementId > 0) {
                departementService.findById(departementId)
                    .ifPresent(projet::setDepartement);
            }
            
            Projet saved = projetService.save(projet);
            
            // Ajouter les membres de l'équipe si des IDs sont fournis
            if (employeIds != null && !employeIds.isEmpty()) {
                for (Long employeId : employeIds) {
                    employeService.findById(employeId).ifPresent(employe -> {
                        EmployeProjet newEp = EmployeProjet.builder()
                            .employe(employe)
                            .projet(saved)
                            .dateAffectation(java.time.LocalDate.now())
                            .build();
                        employeProjetRepository.save(newEp);
                    });
                }
            }
            
            redirectAttributes.addFlashAttribute("message", "Projet créé avec succès");
            return "redirect:/projets/show/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/projets/form";
        }
    }

    @PostMapping("/update/{id}")
    public String updateProjet(@PathVariable Long id, 
                              @RequestParam("nom") String nom,
                              @RequestParam("description") String description,
                              @RequestParam("statut") StatutProjet statut,
                              @RequestParam("priorite") PrioriteProjet priorite,
                              @RequestParam("dateDebut") String dateDebut,
                              @RequestParam("dateFinPrevue") String dateFinPrevue,
                              @RequestParam(value = "chefProjetId", required = false) Long chefProjetId,
                              @RequestParam(value = "departementId", required = false) Long departementId,
                              @RequestParam(value = "employeIds", required = false) List<Long> employeIds,
                              RedirectAttributes redirectAttributes) {
        try {
            // Charger le projet existant
            Optional<Projet> existingOpt = projetService.findById(id);
            if (!existingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Projet non trouvé");
                return "redirect:/projets/list";
            }
            
            Projet existing = existingOpt.get();
            
            // Mettre à jour les champs simples
            existing.setNom(nom);
            existing.setDescription(description);
            existing.setStatut(statut);
            existing.setPriorite(priorite);
            existing.setDateDebut(java.time.LocalDate.parse(dateDebut));
            existing.setDateFinPrevue(java.time.LocalDate.parse(dateFinPrevue));
            
            // Charger le chef de projet comme entité gérée si un ID est fourni
            if (chefProjetId != null && chefProjetId > 0) {
                employeService.findById(chefProjetId)
                    .ifPresent(existing::setChefProjet);
            } else {
                existing.setChefProjet(null);
            }
            
            // Charger le département comme entité gérée si un ID est fourni
            if (departementId != null && departementId > 0) {
                departementService.findById(departementId)
                    .ifPresent(existing::setDepartement);
            } else {
                existing.setDepartement(null);
            }
            
            projetService.save(existing);
            
            // Gérer les membres de l'équipe
            // 1. Récupérer les affectations actuelles
            List<EmployeProjet> affectationsActuelles = employeProjetRepository.findByProjetId(id);
            List<Long> employeIdsActuels = affectationsActuelles.stream()
                .map(ep -> ep.getEmploye().getId())
                .collect(java.util.stream.Collectors.toList());
            
            // 2. Si pas d'employés sélectionnés, créer une liste vide
            if (employeIds == null) {
                employeIds = new java.util.ArrayList<>();
            }
            
            // 3. Supprimer les employés qui ne sont plus sélectionnés
            for (EmployeProjet ep : affectationsActuelles) {
                if (!employeIds.contains(ep.getEmploye().getId())) {
                    employeProjetRepository.delete(ep);
                }
            }
            
            // 4. Ajouter les nouveaux employés sélectionnés
            for (Long employeId : employeIds) {
                if (!employeIdsActuels.contains(employeId)) {
                    employeService.findById(employeId).ifPresent(employe -> {
                        EmployeProjet newEp = EmployeProjet.builder()
                            .employe(employe)
                            .projet(existing)
                            .dateAffectation(java.time.LocalDate.now())
                            .build();
                        employeProjetRepository.save(newEp);
                    });
                }
            }
            
            redirectAttributes.addFlashAttribute("message", "Projet mis à jour avec succès");
            return "redirect:/projets/show/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            return "redirect:/projets/form/" + id;
        }
    }

    @GetMapping("/show/{id}")
    public String showProjet(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        // Utiliser findByIdComplete pour charger toutes les relations (employes, departement, chefProjet)
        Optional<Projet> projet = projetService.findByIdComplete(id);
        if (projet.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Projet introuvable");
            return "redirect:/projets/list";
        }
        model.addAttribute("projet", projet.get());
        model.addAttribute("page", "projets");
        return "projets/show";
    }

    @PostMapping("/delete/{id}")
    public String deleteProjet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            projetService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Projet supprimé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/projets/list";
    }
}
