package com.gestionrh.projetfinalspringboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.projetfinalspringboot.model.entity.Departement;
import com.gestionrh.projetfinalspringboot.repository.DepartementRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service pour la gestion des départements
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DepartementService {
    
    private final DepartementRepository departementRepository;
    private final com.gestionrh.projetfinalspringboot.repository.EmployeRepository employeRepository;
    
    public List<Departement> getAllDepartements() {
        return departementRepository.findAllWithDetails();
    }
    
    // Alias pour compatibilité
    public List<Departement> findAll() {
        return getAllDepartements();
    }
    
    public Optional<Departement> getDepartementById(Long id) {
        return departementRepository.findById(id);
    }
    
    // Alias pour compatibilité
    public Optional<Departement> findById(Long id) {
        return getDepartementById(id);
    }
    
    public Optional<Departement> getDepartementByNom(String nom) {
        return departementRepository.findByNom(nom);
    }
    
    // Alias pour compatibilité
    public Optional<Departement> findByNom(String nom) {
        return getDepartementByNom(nom);
    }
    
    public List<Departement> getDepartementsActifs() {
        return departementRepository.findByActif(true);
    }
    
    public Departement createDepartement(Departement departement) {
        return departementRepository.save(departement);
    }
    
    // Alias pour compatibilité
    public Departement save(Departement departement) {
        if (departement.getId() != null) {
            return updateDepartement(departement.getId(), departement);
        }
        return createDepartement(departement);
    }
    
    public Departement updateDepartement(Long id, Departement departement) {
        Departement existing = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec l'ID: " + id));
        
        existing.setNom(departement.getNom());
        existing.setDescription(departement.getDescription());
        existing.setBudget(departement.getBudget());
        existing.setActif(departement.getActif());
        existing.setChefDepartement(departement.getChefDepartement());
        
        return departementRepository.save(existing);
    }
    
    public void deleteDepartement(Long id) {
        departementRepository.deleteById(id);
    }
    
    // Alias pour compatibilité
    public void deleteById(Long id) {
        deleteDepartement(id);
    }
    
    public Long countEmployes(Long departementId) {
        return departementRepository.countEmployesByDepartementId(departementId);
    }
    
    public Long countProjets(Long departementId) {
        return departementRepository.countProjetsByDepartementId(departementId);
    }
    
    public Optional<Departement> findByIdWithEmployes(Long id) {
        return departementRepository.findByIdWithEmployes(id);
    }
    
    // Nouvelles méthodes requises par le controller
    public List<Departement> findByNomContaining(String nom) {
        return departementRepository.findByNomContainingIgnoreCase(nom);
    }
    
    public java.util.Map<Long, Long> getEmployeeCountsMap() {
        List<Departement> departements = findAll();
        return departements.stream()
                .collect(java.util.stream.Collectors.toMap(
                    Departement::getId,
                    dept -> countEmployes(dept.getId())
                ));
    }
    
    public List<com.gestionrh.projetfinalspringboot.model.entity.Employe> findEmployesSansDepartement() {
        return departementRepository.findEmployesSansDepartement();
    }
    
    public Long countProjetsByDepartement(Long departementId) {
        return countProjets(departementId);
    }
    
    public void affecterEmploye(Long departementId, Long employeId) {
        Departement departement = departementRepository.findById(departementId)
            .orElseThrow(() -> new RuntimeException("Département introuvable"));
        com.gestionrh.projetfinalspringboot.model.entity.Employe employe = 
            employeRepository.findById(employeId)
            .orElseThrow(() -> new RuntimeException("Employé introuvable"));
        
        employe.setDepartement(departement);
        employeRepository.save(employe);
    }
    
    public void retirerEmploye(Long departementId, Long employeId) {
        com.gestionrh.projetfinalspringboot.model.entity.Employe employe = 
            employeRepository.findById(employeId)
            .orElseThrow(() -> new RuntimeException("Employé introuvable"));
        
        if (employe.getDepartement() != null && employe.getDepartement().getId().equals(departementId)) {
            employe.setDepartement(null);
            employeRepository.save(employe);
        }
    }
}
