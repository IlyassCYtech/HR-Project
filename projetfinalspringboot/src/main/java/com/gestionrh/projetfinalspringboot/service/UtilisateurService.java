package com.gestionrh.projetfinalspringboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.model.enums.Role;
import com.gestionrh.projetfinalspringboot.model.enums.StatutUtilisateur;
import com.gestionrh.projetfinalspringboot.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service pour la gestion des utilisateurs
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UtilisateurService {
    
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAllWithDetails();
    }
    
    public Optional<Utilisateur> getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id);
    }
    
    public Optional<Utilisateur> getUtilisateurByUsername(String username) {
        return utilisateurRepository.findByUsername(username);
    }
    
    public Optional<Utilisateur> getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }
    
    public List<Utilisateur> getUtilisateursByRole(Role role) {
        return utilisateurRepository.findByRole(role);
    }
    
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        String pwd = utilisateur.getPasswordHash();
        if (pwd != null && !(pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$"))) {
            utilisateur.setPasswordHash(passwordEncoder.encode(pwd));
        }
        // sinon, déjà encodé
        return utilisateurRepository.save(utilisateur);
    }
    
    public Utilisateur updateUtilisateur(Long id, Utilisateur utilisateur) {
        Utilisateur existingUtilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        
        existingUtilisateur.setUsername(utilisateur.getUsername());
        existingUtilisateur.setEmail(utilisateur.getEmail());
        existingUtilisateur.setRole(utilisateur.getRole());
        existingUtilisateur.setStatut(utilisateur.getStatut());
        
        // Mettre à jour le mot de passe seulement s'il est fourni
        if (utilisateur.getPasswordHash() != null && !utilisateur.getPasswordHash().isEmpty()) {
            existingUtilisateur.setPasswordHash(passwordEncoder.encode(utilisateur.getPasswordHash()));
        }
        
        return utilisateurRepository.save(existingUtilisateur);
    }
    
    public void deleteUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }
    
    public boolean existsByUsername(String username) {
        return utilisateurRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }
    
    public void activerUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        utilisateur.setStatut(StatutUtilisateur.ACTIF);
        utilisateur.setTentativesConnexion(0);
        utilisateurRepository.save(utilisateur);
    }
    
    public void bloquerUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        utilisateur.setStatut(StatutUtilisateur.BLOQUE);
        utilisateurRepository.save(utilisateur);
    }
    
    /**
     * Met à jour le mot de passe d'un utilisateur
     */
    public void updatePassword(Long id, String newPassword) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        utilisateur.setPasswordHash(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(utilisateur);
    }
    
    // Alias pour compatibilité
    public Optional<Utilisateur> findByUsername(String username) {
        return getUtilisateurByUsername(username);
    }
    
    public Optional<Utilisateur> findByEmployeId(Long employeId) {
        return utilisateurRepository.findByEmployeId(employeId);
    }
    
    public Utilisateur save(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }
}
