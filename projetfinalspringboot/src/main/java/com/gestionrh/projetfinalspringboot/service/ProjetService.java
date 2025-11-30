package com.gestionrh.projetfinalspringboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.projetfinalspringboot.model.entity.Projet;
import com.gestionrh.projetfinalspringboot.model.enums.StatutProjet;
import com.gestionrh.projetfinalspringboot.repository.ProjetRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service pour la gestion des projets
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProjetService {
    
    private final ProjetRepository projetRepository;
    
    public List<Projet> getAllProjets() {
        return projetRepository.findAllWithDetails();
    }
    
    public List<Projet> findAll() {
        return getAllProjets();
    }
    
    public List<Projet> findAllWithDetails() {
        return projetRepository.findAllWithDetails();
    }
    
    public Optional<Projet> getProjetById(Long id) {
        return projetRepository.findById(id);
    }
    
    public Optional<Projet> findById(Long id) {
        return getProjetById(id);
    }
    
    public Optional<Projet> findByIdComplete(Long id) {
        return projetRepository.findByIdComplete(id);
    }
    
    public List<Projet> getProjetsByStatut(StatutProjet statut) {
        return projetRepository.findByStatut(statut);
    }
    
    public List<Projet> getProjetsByDepartement(Long departementId) {
        return projetRepository.findByDepartementId(departementId);
    }
    
    public List<Projet> getProjetsByChefProjet(Long chefProjetId) {
        return projetRepository.findByChefProjetId(chefProjetId);
    }
    
    public List<Projet> searchProjets(String searchTerm) {
        return projetRepository.searchProjets(searchTerm);
    }
    
    public Projet createProjet(Projet projet) {
        return projetRepository.save(projet);
    }
    
    public Projet save(Projet projet) {
        if (projet.getId() != null) {
            return updateProjet(projet.getId(), projet);
        }
        return createProjet(projet);
    }
    
    public Projet updateProjet(Long id, Projet projet) {
        Projet existing = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'ID: " + id));
        
        existing.setNom(projet.getNom());
        existing.setDescription(projet.getDescription());
        existing.setDateDebut(projet.getDateDebut());
        existing.setDateFinPrevue(projet.getDateFinPrevue());
        existing.setDateFinReelle(projet.getDateFinReelle());
        existing.setBudget(projet.getBudget());
        existing.setStatut(projet.getStatut());
        existing.setPriorite(projet.getPriorite());
        existing.setChefProjet(projet.getChefProjet());
        existing.setDepartement(projet.getDepartement());
        
        return projetRepository.save(existing);
    }
    
    public void deleteProjet(Long id) {
        projetRepository.deleteById(id);
    }
    
    public void deleteById(Long id) {
        deleteProjet(id);
    }
    
    public Long countByStatut(StatutProjet statut) {
        return projetRepository.countByStatut(statut);
    }
}
