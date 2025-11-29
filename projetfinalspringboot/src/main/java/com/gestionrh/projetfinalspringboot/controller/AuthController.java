package com.gestionrh.projetfinalspringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour les pages d'authentification
 */
@Controller
public class AuthController {
    
    /**
     * Affiche la page de login
     */
    @GetMapping({"/login", "/gestion-rh/login"})
    public String login() {
        return "login";
    }
    
    /**
     * Affiche la page d'erreur personnalisée
     */
    @GetMapping("/error")
    public String error() {
        return "error";
    }
}
