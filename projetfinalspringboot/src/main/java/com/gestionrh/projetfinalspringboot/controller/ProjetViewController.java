package com.gestionrh.projetfinalspringboot.controller;

import com.gestionrh.projetfinalspringboot.model.entity.Employe;
import com.gestionrh.projetfinalspringboot.model.entity.EmployeProjet;
import com.gestionrh.projetfinalspringboot.model.entity.Projet;
import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.model.enums.PrioriteProjet;
import com.gestionrh.projetfinalspringboot.model.enums.Role;
import com.gestionrh.projetfinalspringboot.model.enums.StatutProjet;
import com.gestionrh.projetfinalspringboot.repository.EmployeProjetRepository;
import com.gestionrh.projetfinalspringboot.service.DepartementService;
import com.gestionrh.projetfinalspringboot.service.EmployeService;
import com.gestionrh.projetfinalspringboot.service.ProjetService;
import com.gestionrh.projetfinalspringboot.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
            @RequestParam(required = false) String priorite,
            @RequestParam(required = false) Long departementId,
            Authentication authentication,
            Model model) {
        
        // *** SÉCURITÉ : Récupérer l'utilisateur connecté ***
        String username = authentication.getName();
        Utilisateur utilisateur = utilisateurService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer les projets avec les relations chargées (JOIN FETCH)
        var projets = projetService.findAllWithDetails();
        
        // *** RESTRICTION : Si ni RH ni ADMIN, voir uniquement SES projets ***
        boolean isAdminOrRH = (utilisateur.getRole() == com.gestionrh.projetfinalspringboot.model.enums.Role.ADMIN || 
                               utilisateur.getRole() == com.gestionrh.projetfinalspringboot.model.enums.Role.RH);
        
        if (!isAdminOrRH) {
            if (utilisateur.getEmploye() == null) {
                // Utilisateur sans employé lié ne peut voir aucun projet
                projets = new ArrayList<>();
            } else {
                // Filtrer pour ne montrer que les projets où l'employé est assigné
                Long employeId = utilisateur.getEmploye().getId();
                projets = projets.stream()
                        .filter(p -> p.getEmployes() != null && 
                               p.getEmployes().stream()
                                       .anyMatch(ep -> ep.getEmploye() != null && 
                                                      ep.getEmploye().getId().equals(employeId)))
                        .toList();
            }
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
        
        // Filtrer par priorité
        if (priorite != null && !priorite.isEmpty()) {
            projets = projets.stream()
                    .filter(p -> p.getPriorite() != null && 
                           p.getPriorite().name().equals(priorite))
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
    public String showCreateForm(Model model, RedirectAttributes redirectAttributes) {
        // Vérifier les permissions pour les chefs de département
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);
        
        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            Role userRole = utilisateur.getRole();
            
            // Si c'est un chef de département, il ne peut créer que dans son département
            if (userRole == Role.CHEF_DEPT) {
                if (utilisateur.getEmploye() == null || utilisateur.getEmploye().getDepartement() == null) {
                    redirectAttributes.addFlashAttribute("error", "Vous devez être rattaché à un département pour créer des projets");
                    return "redirect:/projets/list";
                }
                // Pré-remplir le département (sera forcé lors de la création)
                Projet projet = new Projet();
                projet.setDepartement(utilisateur.getEmploye().getDepartement());
                model.addAttribute("projet", projet);
                model.addAttribute("isChefDept", true);
                model.addAttribute("departements", List.of(utilisateur.getEmploye().getDepartement()));
                model.addAttribute("employes", employeService.findAll());
                return "projets/form";
            }
        }
        
        model.addAttribute("projet", new Projet());
        model.addAttribute("departements", departementService.findAll());
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("isChefDept", false);
        return "projets/form";
    }

    @GetMapping("/form/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        // Charger le projet avec ses relations pour l'édition
        Optional<Projet> projetOpt = projetService.findByIdComplete(id);
        if (projetOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Projet introuvable");
            return "redirect:/projets/list";
        }
        
        Projet projet = projetOpt.get();
        
        // Vérifier les permissions pour les chefs de département
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);
        
        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            Role userRole = utilisateur.getRole();
            
            // Seuls ADMIN, RH, CHEF_DEPT et CHEF_PROJET peuvent modifier des projets
            if (userRole != Role.ADMIN && userRole != Role.RH) {
                
                // Si c'est un chef de département, il ne peut modifier que les projets de son département
                if (userRole == Role.CHEF_DEPT) {
                    if (utilisateur.getEmploye() == null || 
                        utilisateur.getEmploye().getDepartement() == null ||
                        projet.getDepartement() == null ||
                        !utilisateur.getEmploye().getDepartement().getId().equals(projet.getDepartement().getId())) {
                        redirectAttributes.addFlashAttribute("error", "Vous ne pouvez modifier que les projets de votre département");
                        return "redirect:/projets/list";
                    }
                }
                // Si c'est un chef de projet, il ne peut modifier que son propre projet (uniquement les membres)
                else if (userRole == Role.CHEF_PROJET) {
                    if (utilisateur.getEmploye() == null || 
                        projet.getChefProjet() == null ||
                        !utilisateur.getEmploye().getId().equals(projet.getChefProjet().getId())) {
                        redirectAttributes.addFlashAttribute("error", "Vous ne pouvez modifier que votre propre projet");
                        return "redirect:/projets/list";
                    }
                }
                // Les employés normaux ne peuvent pas modifier de projets
                else {
                    redirectAttributes.addFlashAttribute("error", "Vous n'avez pas les permissions pour modifier des projets");
                    return "redirect:/projets/list";
                }
            }
        }
        
        model.addAttribute("projet", projet);
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
            // Vérifier les permissions pour les chefs de département
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);
            
            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                Role userRole = utilisateur.getRole();
                
                // Si c'est un chef de département, forcer son département et vérifier
                if (userRole == Role.CHEF_DEPT) {
                    if (utilisateur.getEmploye() == null || utilisateur.getEmploye().getDepartement() == null) {
                        redirectAttributes.addFlashAttribute("error", "Vous devez être rattaché à un département pour créer des projets");
                        return "redirect:/projets/list";
                    }
                    
                    Long chefDeptId = utilisateur.getEmploye().getDepartement().getId();
                    
                    // Vérifier que le département fourni correspond au département du chef
                    if (departementId != null && !departementId.equals(chefDeptId)) {
                        redirectAttributes.addFlashAttribute("error", "Vous ne pouvez créer des projets que dans votre département");
                        return "redirect:/projets/form";
                    }
                    
                    // Forcer le département du chef
                    departementId = chefDeptId;
                }
            }
            
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
            
            // Vérifier les permissions
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);
            
            boolean isChefProjet = false;
            
            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                Role userRole = utilisateur.getRole();
                
                // Seuls ADMIN, RH, CHEF_DEPT et CHEF_PROJET peuvent modifier des projets
                if (userRole != Role.ADMIN && userRole != Role.RH) {
                    
                    // Si c'est un chef de département, vérifier que le projet est dans son département
                    if (userRole == Role.CHEF_DEPT) {
                        if (utilisateur.getEmploye() == null || 
                            utilisateur.getEmploye().getDepartement() == null ||
                            existing.getDepartement() == null ||
                            !utilisateur.getEmploye().getDepartement().getId().equals(existing.getDepartement().getId())) {
                            redirectAttributes.addFlashAttribute("error", "Vous ne pouvez modifier que les projets de votre département");
                            return "redirect:/projets/list";
                        }
                    }
                    // Si c'est un chef de projet, vérifier que c'est son projet
                    else if (userRole == Role.CHEF_PROJET) {
                        if (utilisateur.getEmploye() == null || 
                            existing.getChefProjet() == null ||
                            !utilisateur.getEmploye().getId().equals(existing.getChefProjet().getId())) {
                            redirectAttributes.addFlashAttribute("error", "Vous ne pouvez modifier que votre propre projet");
                            return "redirect:/projets/list";
                        }
                        isChefProjet = true;
                    }
                    // Les employés normaux ne peuvent pas modifier de projets
                    else {
                        redirectAttributes.addFlashAttribute("error", "Vous n'avez pas les permissions pour modifier des projets");
                        return "redirect:/projets/list";
                    }
                }
            }
            
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
            // Vérifier si l'utilisateur a le droit de modifier les membres
            boolean canModifyMembers = false;
            
            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                Role userRole = utilisateur.getRole();
                
                // ADMIN, RH et CHEF_DEPT peuvent modifier les membres
                if (userRole == Role.ADMIN || userRole == Role.RH || userRole == Role.CHEF_DEPT) {
                    canModifyMembers = true;
                }
                // CHEF_PROJET peut modifier les membres si c'est son projet
                else if (isChefProjet) {
                    canModifyMembers = true;
                }
            }
            
            if (canModifyMembers) {
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
            }
            
            redirectAttributes.addFlashAttribute("message", "Projet mis à jour avec succès");
            return "redirect:/projets/show/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            return "redirect:/projets/form/" + id;
        }
    }

    @GetMapping("/show/{id}")
    public String showProjet(@PathVariable Long id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        // *** SÉCURITÉ : Vérifier les droits d'accès ***
        String username = authentication.getName();
        Utilisateur utilisateur = utilisateurService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Utiliser findByIdComplete pour charger toutes les relations (employes, departement, chefProjet)
        Optional<Projet> projetOpt = projetService.findByIdComplete(id);
        if (projetOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Projet introuvable");
            return "redirect:/projets/list";
        }
        
        Projet projet = projetOpt.get();
        
        // *** RESTRICTION : Si ni RH ni ADMIN, vérifier que l'utilisateur est assigné au projet ***
        boolean isAdminOrRH = (utilisateur.getRole() == com.gestionrh.projetfinalspringboot.model.enums.Role.ADMIN || 
                               utilisateur.getRole() == com.gestionrh.projetfinalspringboot.model.enums.Role.RH);
        
        if (!isAdminOrRH) {
            if (utilisateur.getEmploye() == null) {
                redirectAttributes.addFlashAttribute("error", "Accès refusé : vous n'êtes pas assigné à ce projet");
                return "redirect:/projets/list";
            }
            
            Long employeId = utilisateur.getEmploye().getId();
            boolean estAssigne = projet.getEmployes() != null && 
                                projet.getEmployes().stream()
                                    .anyMatch(ep -> ep.getEmploye() != null && 
                                                   ep.getEmploye().getId().equals(employeId));
            
            if (!estAssigne) {
                redirectAttributes.addFlashAttribute("error", "Accès refusé : vous n'êtes pas assigné à ce projet");
                return "redirect:/projets/list";
            }
        }
        
        model.addAttribute("projet", projet);
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
