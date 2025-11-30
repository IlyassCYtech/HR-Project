package com.gestionrh.projetfinalspringboot.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de test pour vérifier que l'application fonctionne
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "Gestion RH Spring Boot");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Système de Gestion RH");
        response.put("description", "Application Spring Boot pour la gestion des ressources humaines");
        response.put("features", new String[]{
            "Gestion des employés",
            "Gestion des départements",
            "Gestion des projets",
            "Gestion des congés",
            "Gestion des fiches de paie",
            "Authentification et autorisation"
        });
        return ResponseEntity.ok(response);
    }
}
