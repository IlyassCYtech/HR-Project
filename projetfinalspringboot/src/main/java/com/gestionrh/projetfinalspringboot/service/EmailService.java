package com.gestionrh.projetfinalspringboot.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    public void sendPasswordResetEmail(String to, String token, String resetUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("rh.elegance@gmail.com");
            helper.setTo(to);
            helper.setSubject("Réinitialisation de votre mot de passe - RH Élégance");
            
            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("token", token);
            
            // Générer le HTML depuis le template
            String htmlContent = templateEngine.process("email/password-reset", context);
            helper.setText(htmlContent, true);
            
            // Envoyer l'email
            mailSender.send(message);
            
            log.info("Email de réinitialisation envoyé à : {}", to);
            
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email à : {}", to, e);
            throw new RuntimeException("Impossible d'envoyer l'email de réinitialisation", e);
        }
    }
}
