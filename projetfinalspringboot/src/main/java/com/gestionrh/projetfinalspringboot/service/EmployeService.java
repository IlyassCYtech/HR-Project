package com.gestionrh.projetfinalspringboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.projetfinalspringboot.model.entity.Employe;
import com.gestionrh.projetfinalspringboot.model.enums.StatutEmploye;
import com.gestionrh.projetfinalspringboot.repository.EmployeRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service pour la gestion des employés
 */
@Service
@Transactional
@RequiredArgsConstructor
public class EmployeService {
    
    private final EmployeRepository employeRepository;
    
    public List<Employe> getAllEmployes() {
        return employeRepository.findAllWithDetails();
    }
    
    // Alias pour compatibilité avec les contrôleurs
    public List<Employe> findAll() {
        return getAllEmployes();
    }
    
    public Optional<Employe> getEmployeById(Long id) {
        return employeRepository.findById(id);
    }
    
    // Alias pour compatibilité
    public Optional<Employe> findById(Long id) {
        return getEmployeById(id);
    }
    
    // Trouver un employé avec ses relations chargées (pour les vues)
    public Optional<Employe> findByIdWithDetails(Long id) {
        return employeRepository.findByIdWithDetails(id);
    }
    
    public Optional<Employe> getEmployeByMatricule(String matricule) {
        return employeRepository.findByMatricule(matricule);
    }
    
    // Alias pour compatibilité
    public Optional<Employe> findByMatricule(String matricule) {
        return getEmployeByMatricule(matricule);
    }
    
    public Optional<Employe> getEmployeByEmail(String email) {
        return employeRepository.findByEmail(email);
    }
    
    // Alias pour compatibilité
    public Optional<Employe> findByEmail(String email) {
        return getEmployeByEmail(email);
    }
    
    public List<Employe> getEmployesByDepartement(Long departementId) {
        return employeRepository.findByDepartementId(departementId);
    }
    
    public List<Employe> getEmployesByStatut(StatutEmploye statut) {
        return employeRepository.findByStatut(statut);
    }
    
    public List<Employe> searchEmployes(String searchTerm) {
        return employeRepository.searchEmployes(searchTerm);
    }
    
    public Employe createEmploye(Employe employe) {
        return employeRepository.save(employe);
    }
    
    // Alias pour compatibilité
    public Employe save(Employe employe) {
        if (employe.getId() != null) {
            return updateEmploye(employe.getId(), employe);
        }
        return createEmploye(employe);
    }
    
    public Employe updateEmploye(Long id, Employe employe) {
        Employe existingEmploye = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'ID: " + id));
        
        existingEmploye.setMatricule(employe.getMatricule());
        existingEmploye.setNom(employe.getNom());
        existingEmploye.setPrenom(employe.getPrenom());
        existingEmploye.setEmail(employe.getEmail());
        existingEmploye.setTelephone(employe.getTelephone());
        existingEmploye.setAdresse(employe.getAdresse());
        existingEmploye.setDateNaissance(employe.getDateNaissance());
        existingEmploye.setDateEmbauche(employe.getDateEmbauche());
        existingEmploye.setSalaireBase(employe.getSalaireBase());
        existingEmploye.setGrade(employe.getGrade());
        existingEmploye.setPoste(employe.getPoste());
        existingEmploye.setStatut(employe.getStatut());
        existingEmploye.setDepartement(employe.getDepartement());
        existingEmploye.setManager(employe.getManager());
        
        return employeRepository.save(existingEmploye);
    }
    
    public void deleteEmploye(Long id) {
        employeRepository.deleteById(id);
    }
    
    // Alias pour compatibilité
    public void deleteById(Long id) {
        deleteEmploye(id);
    }
    
    public Long countEmployesByStatut(StatutEmploye statut) {
        return employeRepository.countByStatut(statut);
    }
}
