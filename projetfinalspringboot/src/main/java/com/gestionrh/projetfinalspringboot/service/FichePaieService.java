package com.gestionrh.projetfinalspringboot.service;

import com.gestionrh.projetfinalspringboot.model.entity.FichePaie;
import com.gestionrh.projetfinalspringboot.repository.FichePaieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des fiches de paie
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FichePaieService {

    private final FichePaieRepository fichePaieRepository;

    public List<FichePaie> findAll() {
        return fichePaieRepository.findAllWithDetails();
    }

    public Optional<FichePaie> findById(Long id) {
        return fichePaieRepository.findById(id);
    }

    public FichePaie save(FichePaie fichePaie) {
        return fichePaieRepository.save(fichePaie);
    }

    public void deleteById(Long id) {
        fichePaieRepository.deleteById(id);
    }

    public List<FichePaie> findByEmployeId(Long employeId) {
        return fichePaieRepository.findByEmployeId(employeId);
    }

    /**
     * Calculer la masse salariale totale (somme de tous les salaires nets)
     */
    public java.math.BigDecimal calculateMasseSalariale() {
        List<FichePaie> fiches = findAll();
        return fiches.stream()
            .map(FichePaie::getNetAPayer)
            .filter(java.util.Objects::nonNull)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /**
     * Calculer la masse salariale pour un mois/année spécifiques
     */
    public java.math.BigDecimal calculateMasseSalarialeForPeriod(Integer mois, Integer annee) {
        List<FichePaie> fiches = findAll();
        return fiches.stream()
            .filter(f -> (mois == null || f.getMois() == mois) && (annee == null || f.getAnnee() == annee))
            .map(FichePaie::getNetAPayer)
            .filter(java.util.Objects::nonNull)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    // Génération automatique en masse des fiches de paie pour tous les employés actifs
    public int generateAllFichesPaie(List<com.gestionrh.projetfinalspringboot.model.entity.Employe> employes, int mois, int annee) {
        int generated = 0;
        for (com.gestionrh.projetfinalspringboot.model.entity.Employe employe : employes) {
            // Vérifier si une fiche existe déjà
            boolean exists = fichePaieRepository.findByEmployeId(employe.getId())
                .stream().anyMatch(f -> f.getMois() == mois && f.getAnnee() == annee);
            
            if (exists) continue;
            
            // Créer une nouvelle fiche
            java.math.BigDecimal salaireBase = employe.getSalaireBase();
            java.math.BigDecimal cotisations = salaireBase.multiply(new java.math.BigDecimal("0.22")); // 22%
            java.math.BigDecimal impots = salaireBase.multiply(new java.math.BigDecimal("0.15")); // 15%
            java.math.BigDecimal netAPayer = salaireBase.subtract(cotisations).subtract(impots);
            
            // Validation : le salaire NET ne peut pas être négatif
            if (netAPayer.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le salaire NET de " + employe.getNom() + " " + employe.getPrenom() + 
                    " serait négatif ou nul (" + netAPayer + " €). Salaire de base insuffisant par rapport aux charges.");
            }
            
            com.gestionrh.projetfinalspringboot.model.entity.FichePaie fiche = com.gestionrh.projetfinalspringboot.model.entity.FichePaie.builder()
                .employe(employe)
                .mois(mois)
                .annee(annee)
                .salaireBase(salaireBase)
                .cotisationsSociales(cotisations)
                .impots(impots)
                .build();
            
            fichePaieRepository.save(fiche);
            generated++;
        }
        return generated;
    }
}
