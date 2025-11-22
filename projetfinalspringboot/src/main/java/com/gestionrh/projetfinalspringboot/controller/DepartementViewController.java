package com.gestionrh.projetfinalspringboot.controller;

import com.gestionrh.projetfinalspringboot.model.entity.Departement;
import com.gestionrh.projetfinalspringboot.service.DepartementService;
import com.gestionrh.projetfinalspringboot.service.EmployeService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping({"/list", ""})
    public String listDepartements(Model model) {
        model.addAttribute("departements", departementService.findAll());
        
        // Charger les statistiques pour chaque département
        model.addAttribute("employeCountsMap", departementService.getEmployeeCountsMap());
        model.addAttribute("page", "departements");
        return "departements/list";
    }

    @GetMapping("/form")
    public String showCreateForm(Model model) {
        model.addAttribute("departement", new Departement());
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("page", "departements");
        return "departements/form";
    }

    @GetMapping("/form/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Departement> departement = departementService.findById(id);
        if (departement.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Département introuvable");
            return "redirect:/departements/list";
        }
        model.addAttribute("departement", departement.get());
        model.addAttribute("employes", employeService.findAll());
        model.addAttribute("page", "departements");
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
        model.addAttribute("departement", dept);
        model.addAttribute("employes", dept.getEmployes()); // Employés du département
        model.addAttribute("employesSansService", departementService.findEmployesSansDepartement()); // Pour le modal
        model.addAttribute("nbEmployes", dept.getEmployes() != null ? dept.getEmployes().size() : 0);
        model.addAttribute("nbProjets", departementService.countProjetsByDepartement(id));
        model.addAttribute("page", "departements");
        return "departements/show";
    }

    @PostMapping("/delete/{id}")
    public String deleteDepartement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Departement> departement = departementService.findById(id);
            if (departement.isPresent()) {
                departementService.deleteById(id);
                redirectAttributes.addFlashAttribute("message", "Département supprimé avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
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
