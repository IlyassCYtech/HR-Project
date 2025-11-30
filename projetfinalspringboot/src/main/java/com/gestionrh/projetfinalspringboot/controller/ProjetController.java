package com.gestionrh.projetfinalspringboot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gestionrh.projetfinalspringboot.model.entity.Projet;
import com.gestionrh.projetfinalspringboot.model.enums.StatutProjet;
import com.gestionrh.projetfinalspringboot.service.ProjetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller REST pour la gestion des projets
 */
@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjetController {
    
    private final ProjetService projetService;
    
    @GetMapping
    public ResponseEntity<List<Projet>> getAllProjets() {
        List<Projet> projets = projetService.getAllProjets();
        return ResponseEntity.ok(projets);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Projet> getProjetById(@PathVariable Long id) {
        return projetService.getProjetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Projet>> getProjetsByStatut(@PathVariable StatutProjet statut) {
        List<Projet> projets = projetService.getProjetsByStatut(statut);
        return ResponseEntity.ok(projets);
    }
    
    @GetMapping("/departement/{departementId}")
    public ResponseEntity<List<Projet>> getProjetsByDepartement(@PathVariable Long departementId) {
        List<Projet> projets = projetService.getProjetsByDepartement(departementId);
        return ResponseEntity.ok(projets);
    }
    
    @GetMapping("/chef/{chefProjetId}")
    public ResponseEntity<List<Projet>> getProjetsByChefProjet(@PathVariable Long chefProjetId) {
        List<Projet> projets = projetService.getProjetsByChefProjet(chefProjetId);
        return ResponseEntity.ok(projets);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Projet>> searchProjets(@RequestParam String query) {
        List<Projet> projets = projetService.searchProjets(query);
        return ResponseEntity.ok(projets);
    }
    
    @PostMapping
    public ResponseEntity<Projet> createProjet(@Valid @RequestBody Projet projet) {
        Projet created = projetService.createProjet(projet);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Projet> updateProjet(@PathVariable Long id, @Valid @RequestBody Projet projet) {
        try {
            Projet updated = projetService.updateProjet(id, projet);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjet(@PathVariable Long id) {
        try {
            projetService.deleteProjet(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/stats/par-statut")
    public ResponseEntity<StatsParStatut> getStatistiquesParStatut() {
        Long planifies = projetService.countByStatut(StatutProjet.PLANIFIE);
        Long enCours = projetService.countByStatut(StatutProjet.EN_COURS);
        Long termines = projetService.countByStatut(StatutProjet.TERMINE);
        Long annules = projetService.countByStatut(StatutProjet.ANNULE);
        
        StatsParStatut stats = new StatsParStatut(planifies, enCours, termines, annules);
        return ResponseEntity.ok(stats);
    }
    
    // Classe interne pour les statistiques
    public static class StatsParStatut {
        public Long planifies;
        public Long enCours;
        public Long termines;
        public Long annules;
        
        public StatsParStatut(Long planifies, Long enCours, Long termines, Long annules) {
            this.planifies = planifies;
            this.enCours = enCours;
            this.termines = termines;
            this.annules = annules;
        }
    }
}
