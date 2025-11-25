package com.gestionrh.projetfinalspringboot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gestionrh.projetfinalspringboot.model.entity.Departement;
import com.gestionrh.projetfinalspringboot.service.DepartementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller REST pour la gestion des départements
 */
@RestController
@RequestMapping("/api/departements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DepartementController {
    
    private final DepartementService departementService;
    
    @GetMapping
    public ResponseEntity<List<Departement>> getAllDepartements() {
        List<Departement> departements = departementService.getAllDepartements();
        return ResponseEntity.ok(departements);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Departement> getDepartementById(@PathVariable Long id) {
        return departementService.getDepartementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/actifs")
    public ResponseEntity<List<Departement>> getDepartementsActifs() {
        List<Departement> departements = departementService.getDepartementsActifs();
        return ResponseEntity.ok(departements);
    }
    
    @GetMapping("/{id}/stats")
    public ResponseEntity<DepartementStats> getDepartementStats(@PathVariable Long id) {
        if (!departementService.getDepartementById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Long nbEmployes = departementService.countEmployes(id);
        Long nbProjets = departementService.countProjets(id);
        
        DepartementStats stats = new DepartementStats(nbEmployes, nbProjets);
        return ResponseEntity.ok(stats);
    }
    
    @PostMapping
    public ResponseEntity<Departement> createDepartement(@Valid @RequestBody Departement departement) {
        Departement created = departementService.createDepartement(departement);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Departement> updateDepartement(@PathVariable Long id, @Valid @RequestBody Departement departement) {
        try {
            Departement updated = departementService.updateDepartement(id, departement);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartement(@PathVariable Long id) {
        try {
            departementService.deleteDepartement(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Classe interne pour les statistiques
    public static class DepartementStats {
        public Long nombreEmployes;
        public Long nombreProjets;
        
        public DepartementStats(Long nombreEmployes, Long nombreProjets) {
            this.nombreEmployes = nombreEmployes;
            this.nombreProjets = nombreProjets;
        }
    }
}
