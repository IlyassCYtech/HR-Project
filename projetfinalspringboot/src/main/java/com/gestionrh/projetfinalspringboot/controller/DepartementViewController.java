package com.gestionrh.projetfinalspringboot.controller;

import com.gestionrh.projetfinalspringboot.model.entity.Departement;
import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.model.enums.Role;
import com.gestionrh.projetfinalspringboot.service.DepartementService;
import com.gestionrh.projetfinalspringboot.service.EmployeService;
import com.gestionrh.projetfinalspringboot.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Contrôleur MVC pour les vues des départements
 */
@Controller
@RequestMapping("/departements")
@RequiredArgsConstructor
public class DepartementViewController {

    private final DepartementService departementService;
    private final EmployeService employeService;
    private final UtilisateurService utilisateurService;

    @GetMapping({"/list", ""})
    public String listDepartements(Model model) {
        model.addAttribute("departements", departementService.findAll());
        
        // Charger les statistiques pour chaque département
        model.addAttribute("employeCountsMap", departementService.getEmployeeCountsMap());
        
        // Ajouter l'ID du département de l'utilisateur connecté (pour CHEF_DEPT)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            Optional<Utilisateur> utilisateur = utilisateurService.findByUsername(authentication.getName());
            if (utilisateur.isPresent() && utilisateur.get().getEmploye() != null 
                && utilisateur.get().getEmploye().getDepartement() != null) {
                model.addAttribute("departementConnecteId", utilisateur.get().getEmploye().getDepartement().getId());
            }
        }
        
        model.addAttribute("activePage", "departements");
        return "departements/list";
    }

    @GetMapping("/form")
    public String showCreateForm(Model model) {
        model.addAttribute("departement", new Departement());
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("activePage", "departements");
        return "departements/form";
    }

    @GetMapping("/form/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Departement> departement = departementService.findById(id);
        if (departement.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Département introuvable");
            return "redirect:/departements/list";
        }
        
        // Vérifier les permissions pour les chefs de département
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);
        
        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            Role userRole = utilisateur.getRole();
            
            // Si c'est un chef de département, il ne peut modifier que son propre département
            if (userRole == Role.CHEF_DEPT) {
                if (utilisateur.getEmploye() == null || 
                    utilisateur.getEmploye().getDepartement() == null ||
                    !utilisateur.getEmploye().getDepartement().getId().equals(id)) {
                    redirectAttributes.addFlashAttribute("error", "Vous ne pouvez modifier que votre propre département");
                    return "redirect:/departements/list";
                }
            }
        }
        
        model.addAttribute("departement", departement.get());
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("activePage", "departements");
        return "departements/form";
    }

    @PostMapping("/create")
    public String createDepartement(@ModelAttribute Departement departement, 
                                    @RequestParam(value = "chefDepartementId", required = false) Long chefDepartementId,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Vérifier si le nom du département existe déjà
            if (departementService.findByNom(departement.getNom()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Le département '" + departement.getNom() + "' existe déjà. Veuillez choisir un autre nom.");
                redirectAttributes.addFlashAttribute("departement", departement);
                return "redirect:/departements/form";
            }
            
            // Charger le chef de département comme entité gérée si un ID est fourni
            if (chefDepartementId != null && chefDepartementId > 0) {
                employeService.findById(chefDepartementId)
                    .ifPresent(departement::setChefDepartement);
            } else {
                departement.setChefDepartement(null);
            }
            
            Departement saved = departementService.save(departement);
            redirectAttributes.addFlashAttribute("message", "Département " + saved.getNom() + " créé avec succès");
            return "redirect:/departements/show/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("departement", departement);
            return "redirect:/departements/form";
        }
    }

    @PostMapping("/update/{id}")
    public String updateDepartement(@PathVariable Long id, 
                                    @ModelAttribute Departement departement,
                                    @RequestParam(value = "chefDepartementId", required = false) Long chefDepartementId,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Charger le département existant
            Optional<Departement> existingOpt = departementService.findById(id);
            if (!existingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Département non trouvé");
                return "redirect:/departements";
            }
            
            Departement existing = existingOpt.get();
            
            // Vérifier les permissions pour les chefs de département
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);
            
            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                Role userRole = utilisateur.getRole();
                
                // Si c'est un chef de département, il ne peut modifier que son propre département
                if (userRole == Role.CHEF_DEPT) {
                    if (utilisateur.getEmploye() == null || 
                        utilisateur.getEmploye().getDepartement() == null ||
                        !utilisateur.getEmploye().getDepartement().getId().equals(id)) {
                        redirectAttributes.addFlashAttribute("error", "Vous ne pouvez modifier que votre propre département");
                        return "redirect:/departements/list";
                    }
                }
            }
            
            // Vérifier si le nouveau nom est déjà utilisé par un autre département
            if (!existing.getNom().equals(departement.getNom())) {
                Optional<Departement> deptAvecMemeNom = departementService.findByNom(departement.getNom());
                if (deptAvecMemeNom.isPresent()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Le nom '" + departement.getNom() + "' est déjà utilisé par un autre département.");
                    return "redirect:/departements/form/" + id;
                }
            }
            
            // Mettre à jour les champs simples
            existing.setNom(departement.getNom());
            existing.setDescription(departement.getDescription());
            existing.setBudget(departement.getBudget());
            existing.setDateCreation(departement.getDateCreation());
            existing.setActif(departement.getActif());
            
            // Charger le chef de département comme entité gérée si un ID est fourni
            if (chefDepartementId != null && chefDepartementId > 0) {
                employeService.findById(chefDepartementId)
                    .ifPresent(existing::setChefDepartement);
            } else {
                existing.setChefDepartement(null);
            }
            
            Departement updated = departementService.save(existing);
            redirectAttributes.addFlashAttribute("message", "Département " + updated.getNom() + " mis à jour");
            return "redirect:/departements/show/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            return "redirect:/departements/form/" + id;
        }
    }

    @GetMapping("/show/{id}")
    public String showDepartement(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Departement> departement = departementService.findByIdWithEmployes(id);
        if (departement.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Département introuvable");
            return "redirect:/departements/list";
        }
        
        Departement dept = departement.get();
        
        // Vérifier si l'utilisateur peut modifier ce département
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean canEdit = false;
        
        if (authentication != null && authentication.getName() != null) {
            Optional<Utilisateur> utilisateur = utilisateurService.findByUsername(authentication.getName());
            if (utilisateur.isPresent()) {
                Role userRole = utilisateur.get().getRole();
                
                // ADMIN et RH peuvent tout modifier
                if (userRole == Role.ADMIN || userRole == Role.RH) {
                    canEdit = true;
                }
                // CHEF_DEPT peut modifier uniquement son propre département
                else if (userRole == Role.CHEF_DEPT) {
                    if (utilisateur.get().getEmploye() != null 
                        && utilisateur.get().getEmploye().getDepartement() != null
                        && utilisateur.get().getEmploye().getDepartement().getId().equals(id)) {
                        canEdit = true;
                    }
                }
            }
        }
        
        model.addAttribute("departement", dept);
        model.addAttribute("employes", dept.getEmployes());
        model.addAttribute("employesSansService", departementService.findEmployesSansDepartement());
        model.addAttribute("nbEmployes", dept.getEmployes() != null ? dept.getEmployes().size() : 0);
        model.addAttribute("nbProjets", departementService.countProjetsByDepartement(id));
        model.addAttribute("canEditDepartement", canEdit);
        model.addAttribute("activePage", "departements");
        return "departements/show";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Departement> departement = departementService.findByIdWithEmployes(id);
            if (departement.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Département introuvable");
                return "redirect:/departements/list";
            }
            
            // SUPPRESSION EN CASCADE : Retirer automatiquement les employés et projets
            Departement dept = departement.get();
            
            // Retirer tous les employés du département (mise à null de leur département)
            if (dept.getEmployes() != null && !dept.getEmployes().isEmpty()) {
                for (var employe : dept.getEmployes()) {
                    departementService.retirerEmploye(id, employe.getId());
                }
            }
            
            // Les projets seront gérés par la cascade JPA si configurée
            // Sinon, on pourrait les retirer manuellement ici
            
            departementService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Département '" + dept.getNom() + "' supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/departements/list";
    }

    @PostMapping("/affecter-employe")
    public String affecterEmploye(@RequestParam Long departementId, 
                                 @RequestParam Long employeId, 
                                 RedirectAttributes redirectAttributes) {
        try {
            departementService.affecterEmploye(departementId, employeId);
            redirectAttributes.addFlashAttribute("message", "Employé affecté au département avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'affectation: " + e.getMessage());
        }
        return "redirect:/departements/show/" + departementId;
    }

    @GetMapping("/retirer-employe/{departementId}/{employeId}")
    public String retirerEmploye(@PathVariable Long departementId,
                                @PathVariable Long employeId,
                                RedirectAttributes redirectAttributes) {
        try {
            departementService.retirerEmploye(departementId, employeId);
            redirectAttributes.addFlashAttribute("message", "Employé retiré du département avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du retrait: " + e.getMessage());
        }
        return "redirect:/departements/show/" + departementId;
    }
}
