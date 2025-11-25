package com.gestionrh.projetfinalspringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.service.UtilisateurService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Contrôleur pour la gestion du profil utilisateur
 */
@Controller
@RequestMapping("/profil")
public class ProfilController {

    @Autowired
    private UtilisateurService utilisateurService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Affiche le profil de l'utilisateur connecté
     */
    @GetMapping
    public String showProfil(Authentication authentication, Model model) {
        String username = authentication.getName();
        Utilisateur utilisateur = utilisateurService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        model.addAttribute("utilisateur", utilisateur);
        
        // Charger l'employé associé si disponible
        if (utilisateur.getEmploye() != null) {
            model.addAttribute("employe", utilisateur.getEmploye());
        }
        
        model.addAttribute("passwordForm", new PasswordChangeForm());
        
        return "profil";
    }

    /**
     * Change le mot de passe de l'utilisateur
     */
    @PostMapping("/change-password")
    @Transactional
    public String changePassword(
            @Valid @ModelAttribute("passwordForm") PasswordChangeForm form,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            String username = authentication.getName();
            Utilisateur utilisateur = utilisateurService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            model.addAttribute("utilisateur", utilisateur);
            if (utilisateur.getEmploye() != null) {
                model.addAttribute("employe", utilisateur.getEmploye());
            }
            return "profil";
        }
        
        // Vérifier que les nouveaux mots de passe correspondent
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.passwordForm", 
                    "Les mots de passe ne correspondent pas");
            
            String username = authentication.getName();
            Utilisateur utilisateur = utilisateurService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            model.addAttribute("utilisateur", utilisateur);
            if (utilisateur.getEmploye() != null) {
                model.addAttribute("employe", utilisateur.getEmploye());
            }
            return "profil";
        }
        
        try {
            String username = authentication.getName();
            Utilisateur utilisateur = utilisateurService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Vérifier l'ancien mot de passe
            if (!passwordEncoder.matches(form.getCurrentPassword(), utilisateur.getPasswordHash())) {
                result.rejectValue("currentPassword", "error.passwordForm", 
                        "Le mot de passe actuel est incorrect");
                model.addAttribute("utilisateur", utilisateur);
                if (utilisateur.getEmploye() != null) {
                    model.addAttribute("employe", utilisateur.getEmploye());
                }
                return "profil";
            }
            
            // Changer le mot de passe
            utilisateur.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
            utilisateurService.save(utilisateur);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Mot de passe modifié avec succès");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Erreur lors du changement de mot de passe: " + e.getMessage());
        }
        
        return "redirect:/profil";
    }

    /**
     * Classe interne pour le formulaire de changement de mot de passe
     */
    @Data
    public static class PasswordChangeForm {
        
        @NotBlank(message = "Le mot de passe actuel est obligatoire")
        private String currentPassword;
        
        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        private String newPassword;
        
        @NotBlank(message = "La confirmation est obligatoire")
        private String confirmPassword;
    }
}
