package com.gestionrh.projetfinalspringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gestionrh.projetfinalspringboot.model.enums.StatutEmploye;
import com.gestionrh.projetfinalspringboot.model.enums.StatutProjet;
import com.gestionrh.projetfinalspringboot.model.enums.StatutConge;
import com.gestionrh.projetfinalspringboot.repository.EmployeRepository;
import com.gestionrh.projetfinalspringboot.repository.ProjetRepository;
import com.gestionrh.projetfinalspringboot.repository.CongeAbsenceRepository;
import com.gestionrh.projetfinalspringboot.repository.FichePaieRepository;
import com.gestionrh.projetfinalspringboot.repository.DepartementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour les statistiques et rapports
 */
@Controller
@RequestMapping("/statistiques")
@PreAuthorize("hasAnyRole('ADMIN', 'RH', 'CHEF_DEPT')")
public class StatistiquesController {

    @Autowired
    private EmployeRepository employeRepository;
    
    @Autowired
    private ProjetRepository projetRepository;
    
    @Autowired
    private CongeAbsenceRepository congeAbsenceRepository;
    
    @Autowired
    private FichePaieRepository fichePaieRepository;
    
    @Autowired
    private DepartementRepository departementRepository;

    /**
     * Affiche les statistiques générales
     */
    @GetMapping
    public String showStatistiques(Model model) {
        // Statistiques des employés
        Long totalEmployes = employeRepository.count();
        Long employesActifs = employeRepository.countByStatut(StatutEmploye.ACTIF);
        Long employesSuspendus = employeRepository.countByStatut(StatutEmploye.SUSPENDU);
        Long employesDemissionnaires = employeRepository.countByStatut(StatutEmploye.DEMISSION);
        
        model.addAttribute("totalEmployes", totalEmployes);
        model.addAttribute("employesActifs", employesActifs);
        model.addAttribute("employesSuspendus", employesSuspendus);
        model.addAttribute("employesDemissionnaires", employesDemissionnaires);
        
        
        // Statistiques des départements
        Long totalDepartements = departementRepository.count();
        model.addAttribute("totalDepartements", totalDepartements);
        
        // Statistiques des projets
        Long totalProjets = projetRepository.count();
        Long projetsEnCours = projetRepository.countByStatut(StatutProjet.EN_COURS);
        Long projetsTermines = projetRepository.countByStatut(StatutProjet.TERMINE);
        Long projetsPlanifies = projetRepository.countByStatut(StatutProjet.PLANIFIE);
        Long projetsAnnules = projetRepository.countByStatut(StatutProjet.ANNULE);
        
        model.addAttribute("totalProjets", totalProjets);
        model.addAttribute("projetsEnCours", projetsEnCours);
        model.addAttribute("projetsTermines", projetsTermines);
        model.addAttribute("projetsPlanifies", projetsPlanifies);
        model.addAttribute("projetsAnnules", projetsAnnules);
        
        // Projets en retard
        LocalDate today = LocalDate.now();
        Long projetsEnRetard = projetRepository.findProjetsEnRetard(today).stream().count();
        model.addAttribute("projetsEnRetard", projetsEnRetard);
        
        // Statistiques des congés
        Long totalConges = congeAbsenceRepository.count();
        Long congesEnAttente = congeAbsenceRepository.countByStatut(StatutConge.EN_ATTENTE);
        Long congesApprouves = congeAbsenceRepository.countByStatut(StatutConge.APPROUVE);
        Long congesRefuses = congeAbsenceRepository.countByStatut(StatutConge.REFUSE);
        
        model.addAttribute("totalConges", totalConges);
        model.addAttribute("congesEnAttente", congesEnAttente);
        model.addAttribute("congesApprouves", congesApprouves);
        model.addAttribute("congesRefuses", congesRefuses);
        
        // Statistiques des fiches de paie
        Long totalFichesPaie = fichePaieRepository.count();
        model.addAttribute("totalFichesPaie", totalFichesPaie);
        
        // Masse salariale (mois en cours)
        int moisActuel = LocalDate.now().getMonthValue();
        int anneeActuelle = LocalDate.now().getYear();
        
        BigDecimal masseSalariale = fichePaieRepository.findByMoisAndAnnee(moisActuel, anneeActuelle)
                .stream()
                .map(fp -> fp.getSalaireNet())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("masseSalariale", masseSalariale);
        model.addAttribute("moisStatistiques", getMoisNom(moisActuel));
        model.addAttribute("anneeStatistiques", anneeActuelle);
        
        // Statistiques par département
        Map<String, Long> employesParDepartement = new HashMap<>();
        departementRepository.findAll().forEach(dept -> {
            Long count = departementRepository.countEmployesByDepartementId(dept.getId());
            employesParDepartement.put(dept.getNom(), count);
        });
        model.addAttribute("employesParDepartement", employesParDepartement);
        
        // Nouveaux employés ce mois
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        Long nouveauxEmployes = employeRepository.findByDateEmbaucheBetween(debutMois, finMois).stream().count();
        model.addAttribute("nouveauxEmployes", nouveauxEmployes);
        
        return "statistiques";
    }
    
    /**
     * Retourne le nom du mois en français
     */
    private String getMoisNom(int mois) {
        String[] moisNoms = {"", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", 
                             "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return moisNoms[mois];
    }
}
