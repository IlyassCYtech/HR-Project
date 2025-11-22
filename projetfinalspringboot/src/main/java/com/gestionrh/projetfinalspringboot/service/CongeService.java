package com.gestionrh.projetfinalspringboot.service;

import com.gestionrh.projetfinalspringboot.model.entity.CongeAbsence;
import com.gestionrh.projetfinalspringboot.repository.CongeAbsenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des congés et absences
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CongeService {

    private final CongeAbsenceRepository congeRepository;

    public List<CongeAbsence> findAll() {
        return congeRepository.findAllWithDetails();
    }

    public Optional<CongeAbsence> findById(Long id) {
        return congeRepository.findById(id);
    }

    public CongeAbsence save(CongeAbsence conge) {
        return congeRepository.save(conge);
    }

    public void deleteById(Long id) {
        congeRepository.deleteById(id);
    }

    public List<CongeAbsence> findByEmployeId(Long employeId) {
        return congeRepository.findByEmployeId(employeId);
    }

    public List<CongeAbsence> findByStatut(String statut) {
        return congeRepository.findAll().stream()
                .filter(c -> c.getStatut().name().equals(statut))
                .toList();
    }
    
    // Nouvelles méthodes requises par le controller
    public List<CongeAbsence> findCongesEnAttente() {
        return congeRepository.findByStatutEnAttente();
    }
    
    public java.util.Map<String, Object> getStatsConges() {
        var stats = new java.util.HashMap<String, Object>();
        stats.put("total", congeRepository.count());
        stats.put("enAttente", congeRepository.countByStatutEnAttente());
        stats.put("approuves", congeRepository.countByStatutApprouve());
        stats.put("refuses", congeRepository.countByStatutRefuse());
        return stats;
    }
    
    public List<CongeAbsence> findMesConges() {
        // TODO: Récupérer l'employé connecté depuis le contexte de sécurité
        // Pour l'instant retourner tous les congés 
        return findAll();
    }
    
    public java.util.Map<String, Object> getSoldeConges() {
        var solde = new java.util.HashMap<String, Object>();
        solde.put("joursRestants", 25); // TODO: Calculer le vrai solde
        solde.put("joursUtilises", 5);
        solde.put("joursEnAttente", 3);
        return solde;
    }
    
    public Long countMesDemandes() {
        // TODO: Compter les demandes de l'employé connecté
        return congeRepository.count();
    }
    
    public void approveConge(Long congeId, com.gestionrh.projetfinalspringboot.model.entity.Employe approbateur, String commentaire) {
        CongeAbsence conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé"));
        
        // Mettre à jour le congé avec les informations de validation
        conge.setStatut(com.gestionrh.projetfinalspringboot.model.enums.StatutConge.APPROUVE);
        conge.setApprouvePar(approbateur);
        conge.setDateApprobation(java.time.LocalDateTime.now());
        
        if (commentaire != null && !commentaire.trim().isEmpty()) {
            conge.setCommentairesApprobation(commentaire);
        }
        
        congeRepository.save(conge);
    }
    
    public void rejectConge(Long congeId, com.gestionrh.projetfinalspringboot.model.entity.Employe approbateur, String commentaire) {
        CongeAbsence conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé"));
        
        // Mettre à jour le congé avec les informations de validation
        conge.setStatut(com.gestionrh.projetfinalspringboot.model.enums.StatutConge.REFUSE);
        conge.setApprouvePar(approbateur);
        conge.setDateApprobation(java.time.LocalDateTime.now());
        
        if (commentaire != null && !commentaire.trim().isEmpty()) {
            conge.setCommentairesApprobation(commentaire);
        }
        
        congeRepository.save(conge);
    }
}
