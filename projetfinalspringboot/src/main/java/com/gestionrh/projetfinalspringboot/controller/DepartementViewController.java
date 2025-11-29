package com.gestionrh.projetfinalspringboot.controller;

import com.gestionrh.projetfinalspringboot.model.entity.Departement;
import com.gestionrh.projetfinalspringboot.model.entity.Employe;
import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.model.enums.Role;
import com.gestionrh.projetfinalspringboot.service.DepartementService;
import com.gestionrh.projetfinalspringboot.service.EmployeService;
import com.gestionrh.projetfinalspringboot.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    @GetMapping({ "/list", "" })
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
    public String showCreateForm(Model model, RedirectAttributes redirectAttributes) {
        // Vérifier les permissions : seuls ADMIN, RH et CHEF_DEPT peuvent créer des
        // départements
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);

        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            Role userRole = utilisateur.getRole();

            // Seuls ADMIN et RH peuvent créer des départements
            if (userRole != Role.ADMIN && userRole != Role.RH) {
                redirectAttributes.addFlashAttribute("error",
                        "Vous n'avez pas les permissions pour créer des départements");
                return "redirect:/departements/list";
            }
        }

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

        // Vérifier les permissions : seuls ADMIN, RH et CHEF_DEPT peuvent modifier des
        // départements
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);

        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            Role userRole = utilisateur.getRole();

            // Seuls ADMIN, RH et CHEF_DEPT peuvent modifier des départements
            if (userRole != Role.ADMIN && userRole != Role.RH && userRole != Role.CHEF_DEPT) {
                redirectAttributes.addFlashAttribute("error",
                        "Vous n'avez pas les permissions pour modifier des départements");
                return "redirect:/departements/list";
            }

            // Si c'est un chef de département, il ne peut modifier que son propre
            // département
            if (userRole == Role.CHEF_DEPT) {
                if (utilisateur.getEmploye() == null ||
                        utilisateur.getEmploye().getDepartement() == null ||
                        !utilisateur.getEmploye().getDepartement().getId().equals(id)) {
                    redirectAttributes.addFlashAttribute("error",
                            "Vous ne pouvez modifier que votre propre département");
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
    @Transactional
    public String createDepartement(@ModelAttribute Departement departement,
            @RequestParam(value = "chefDepartementId", required = false) Long chefDepartementId,
            RedirectAttributes redirectAttributes) {
        try {
            // Vérifier les permissions : seuls ADMIN, RH et CHEF_DEPT peuvent créer des
            // départements
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);

            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                Role userRole = utilisateur.getRole();

                // Seuls ADMIN et RH peuvent créer des départements
                if (userRole != Role.ADMIN && userRole != Role.RH) {
                    redirectAttributes.addFlashAttribute("error",
                            "Vous n'avez pas les permissions pour créer des départements");
                    return "redirect:/departements/list";
                }
            }

            // Vérifier si le nom du département existe déjà
            if (departementService.findByNom(departement.getNom()).isPresent()) {
                redirectAttributes.addFlashAttribute("error",
                        "Le département '" + departement.getNom() + "' existe déjà. Veuillez choisir un autre nom.");
                redirectAttributes.addFlashAttribute("departement", departement);
                return "redirect:/departements/form";
            }

            // Sauvegarder d'abord le département pour obtenir son ID
            Departement saved = departementService.save(departement);
            
            // Charger le chef de département comme entité gérée si un ID est fourni
            if (chefDepartementId != null && chefDepartementId > 0) {
                employeService.findById(chefDepartementId)
                        .ifPresent(chef -> {
                            // Assigner le chef au département
                            saved.setChefDepartement(chef);
                            
                            // Assigner le département à l'employé chef
                            chef.setDepartement(saved);
                            employeService.save(chef);
                            
                            // Mettre à jour le rôle de l'utilisateur associé à CHEF_DEPT
                            utilisateurService.findByEmployeId(chef.getId()).ifPresent(userChef -> {
                                // Ne changer que si ce n'est pas déjà ADMIN ou RH
                                if (userChef.getRole() != Role.ADMIN && userChef.getRole() != Role.RH) {
                                    // Recharger l'utilisateur complet pour éviter les problèmes de validation
                                    utilisateurService.getUtilisateurById(userChef.getId()).ifPresent(fullUser -> {
                                        fullUser.setRole(Role.CHEF_DEPT);
                                        utilisateurService.save(fullUser);
                                    });
                                }
                            });
                            
                            // Sauvegarder le département avec le chef assigné
                            departementService.save(saved);
                        });
            } else {
                saved.setChefDepartement(null);
            }
            redirectAttributes.addFlashAttribute("message", "Département " + saved.getNom() + " créé avec succès");
            return "redirect:/departements/show/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("departement", departement);
            return "redirect:/departements/form";
        }
    }

    @PostMapping("/update/{id}")
    @Transactional
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

            // Vérifier les permissions : seuls ADMIN, RH et CHEF_DEPT peuvent modifier des
            // départements
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);

            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                Role userRole = utilisateur.getRole();

                // Seuls ADMIN, RH et CHEF_DEPT peuvent modifier des départements
                if (userRole != Role.ADMIN && userRole != Role.RH && userRole != Role.CHEF_DEPT) {
                    redirectAttributes.addFlashAttribute("error",
                            "Vous n'avez pas les permissions pour modifier des départements");
                    return "redirect:/departements/list";
                }

                // Si c'est un chef de département, il ne peut modifier que son propre
                // département
                if (userRole == Role.CHEF_DEPT) {
                    if (utilisateur.getEmploye() == null ||
                            utilisateur.getEmploye().getDepartement() == null ||
                            !utilisateur.getEmploye().getDepartement().getId().equals(id)) {
                        redirectAttributes.addFlashAttribute("error",
                                "Vous ne pouvez modifier que votre propre département");
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

            // Gérer le changement de chef de département
            Employe ancienChef = existing.getChefDepartement();
            
            // Rétrograder l'ancien chef AVANT d'assigner le nouveau (si nécessaire)
            if (ancienChef != null) {
                // Vérifier si on change de chef ou si on le retire
                boolean chefChange = (chefDepartementId == null || chefDepartementId == 0 || 
                                     !ancienChef.getId().equals(chefDepartementId));
                
                if (chefChange) {
                    // Rechercher l'utilisateur de l'ancien chef
                    Optional<Utilisateur> userAncienChefOpt = utilisateurService.findByEmployeId(ancienChef.getId());
                    if (userAncienChefOpt.isPresent()) {
                        // Recharger complètement l'utilisateur pour avoir tous les champs
                        Long ancienUserId = userAncienChefOpt.get().getId();
                        Utilisateur fullAncienUser = utilisateurService.getUtilisateurById(ancienUserId).orElse(null);
                        
                        if (fullAncienUser != null) {
                            Role ancienRole = fullAncienUser.getRole();
                            // Rétrograder uniquement si c'était un CHEF_DEPT
                            if (ancienRole == Role.CHEF_DEPT) {
                                fullAncienUser.setRole(Role.EMPLOYE);
                                utilisateurService.save(fullAncienUser);
                                System.out.println("✓ Rétrogradation: " + fullAncienUser.getUsername() + " : CHEF_DEPT → EMPLOYE");
                            } else {
                                System.out.println("✗ Pas de rétrogradation pour " + fullAncienUser.getUsername() + " (rôle actuel: " + ancienRole + ")");
                            }
                        }
                    }
                }
            }
            
            // Charger le nouveau chef de département si un ID est fourni
            if (chefDepartementId != null && chefDepartementId > 0) {
                Optional<Employe> nouveauChefOpt = employeService.findById(chefDepartementId);
                if (nouveauChefOpt.isPresent()) {
                    Employe nouveauChef = nouveauChefOpt.get();
                    existing.setChefDepartement(nouveauChef);
                    
                    // Assigner le département au nouveau chef
                    nouveauChef.setDepartement(existing);
                    employeService.save(nouveauChef);
                    
                    // Mettre à jour le rôle du nouveau chef à CHEF_DEPT
                    Optional<Utilisateur> userChefOpt = utilisateurService.findByEmployeId(nouveauChef.getId());
                    if (userChefOpt.isPresent()) {
                        // Recharger complètement l'utilisateur
                        Long nouveauUserId = userChefOpt.get().getId();
                        Utilisateur fullUser = utilisateurService.getUtilisateurById(nouveauUserId).orElse(null);
                        
                        if (fullUser != null) {
                            Role roleActuel = fullUser.getRole();
                            // Ne changer que si ce n'est pas déjà ADMIN ou RH
                            if (roleActuel != Role.ADMIN && roleActuel != Role.RH) {
                                fullUser.setRole(Role.CHEF_DEPT);
                                utilisateurService.save(fullUser);
                                System.out.println("✓ Promotion: " + fullUser.getUsername() + " : " + roleActuel + " → CHEF_DEPT");
                            } else {
                                System.out.println("✗ Pas de promotion pour " + fullUser.getUsername() + " (déjà " + roleActuel + ")");
                            }
                        }
                    }
                }
            } else {
                // Si on retire le chef de département
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
            // Vérifier les permissions : seuls ADMIN et RH peuvent supprimer des
            // départements
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Utilisateur> utilisateurOpt = utilisateurService.findByUsername(username);

            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                Role userRole = utilisateur.getRole();

                // Seuls ADMIN et RH peuvent supprimer des départements (pas CHEF_DEPT)
                if (userRole != Role.ADMIN && userRole != Role.RH) {
                    redirectAttributes.addFlashAttribute("error",
                            "Vous n'avez pas les permissions pour supprimer des départements");
                    return "redirect:/departements/list";
                }
            }

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
