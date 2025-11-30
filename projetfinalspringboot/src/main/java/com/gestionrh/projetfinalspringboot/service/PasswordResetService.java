package com.gestionrh.projetfinalspringboot.service;

import com.gestionrh.projetfinalspringboot.model.entity.PasswordResetToken;
import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final PasswordResetTokenRepository tokenRepository;
    private final UtilisateurService utilisateurService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.password-reset.token-validity-hours:1}")
    private int tokenValidityHours;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${server.servlet.context-path:/gestion-rh}")
    private String contextPath;
    
    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurByEmail(email);
        
        if (utilisateurOpt.isEmpty()) {
            log.warn("Tentative de réinitialisation pour email inexistant : {}", email);
            // Ne pas révéler que l'email n'existe pas (sécurité)
            return;
        }
        
        Utilisateur utilisateur = utilisateurOpt.get();
        
        // Supprimer les anciens tokens de cet utilisateur
        tokenRepository.findByUtilisateur(utilisateur).ifPresent(tokenRepository::delete);
        
        // Créer un nouveau token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUtilisateur(utilisateur);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(tokenValidityHours));
        resetToken.setUsed(false);
        
        tokenRepository.save(resetToken);
        
        // Construire l'URL de réinitialisation
        String resetUrl = baseUrl + contextPath + "/reset-password?token=" + token;
        
        // Envoyer l'email
        emailService.sendPasswordResetEmail(email, token, resetUrl);
        
        log.info("Token de réinitialisation créé pour l'utilisateur : {}", email);
    }
    
    @Transactional
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);
        
        if (resetTokenOpt.isEmpty()) {
            log.warn("Token de réinitialisation invalide : {}", token);
            return false;
        }
        
        PasswordResetToken resetToken = resetTokenOpt.get();
        
        if (resetToken.isUsed()) {
            log.warn("Token déjà utilisé : {}", token);
            return false;
        }
        
        if (resetToken.isExpired()) {
            log.warn("Token expiré : {}", token);
            return false;
        }
        
        return true;
    }
    
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);
        
        if (resetTokenOpt.isEmpty()) {
            log.warn("Token de réinitialisation invalide : {}", token);
            return false;
        }
        
        PasswordResetToken resetToken = resetTokenOpt.get();
        
        if (resetToken.isUsed() || resetToken.isExpired()) {
            log.warn("Token invalide ou expiré : {}", token);
            return false;
        }
        
        // Mettre à jour le mot de passe
        Utilisateur utilisateur = resetToken.getUtilisateur();
        utilisateur.setPasswordHash(passwordEncoder.encode(newPassword));
        utilisateurService.save(utilisateur);
        
        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        log.info("Mot de passe réinitialisé avec succès pour l'utilisateur : {}", utilisateur.getUsername());
        
        return true;
    }
    
    public Optional<Utilisateur> getUserByToken(String token) {
        return tokenRepository.findByToken(token)
                .map(PasswordResetToken::getUtilisateur);
    }
}
