package com.gestionrh.projetfinalspringboot.controller;

import com.gestionrh.projetfinalspringboot.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    
    /**
     * Afficher le formulaire "Mot de passe oublié"
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }
    
    /**
     * Traiter la demande de réinitialisation
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                       RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.createPasswordResetTokenForUser(email);
            
            redirectAttributes.addFlashAttribute("success", 
                "Si cet email existe dans notre système, vous recevrez un lien de réinitialisation.");
            
            log.info("Demande de réinitialisation de mot de passe pour : {}", email);
            
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation du mot de passe", e);
            redirectAttributes.addFlashAttribute("error", 
                "Une erreur s'est produite. Veuillez réessayer plus tard.");
        }
        
        return "redirect:/forgot-password";
    }
    
    /**
     * Afficher le formulaire de réinitialisation avec token
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model,
                                       RedirectAttributes redirectAttributes) {
        
        if (!passwordResetService.validatePasswordResetToken(token)) {
            redirectAttributes.addFlashAttribute("error", 
                "Le lien de réinitialisation est invalide ou a expiré.");
            return "redirect:/login";
        }
        
        model.addAttribute("token", token);
        return "reset-password";
    }
    
    /**
     * Traiter la réinitialisation du mot de passe
     */
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {
        
        // Vérifier que les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas.");
            redirectAttributes.addAttribute("token", token);
            return "redirect:/reset-password";
        }
        
        // Vérifier la longueur du mot de passe
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 6 caractères.");
            redirectAttributes.addAttribute("token", token);
            return "redirect:/reset-password";
        }
        
        // Réinitialiser le mot de passe
        boolean success = passwordResetService.resetPassword(token, password);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", 
                "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", 
                "Le lien de réinitialisation est invalide ou a expiré.");
            return "redirect:/login";
        }
    }
}
