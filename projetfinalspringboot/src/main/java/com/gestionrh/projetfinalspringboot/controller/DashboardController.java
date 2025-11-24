package com.gestionrh.projetfinalspringboot.controller;

import com.gestionrh.projetfinalspringboot.model.enums.StatutProjet;
import com.gestionrh.projetfinalspringboot.service.CongeService;
import com.gestionrh.projetfinalspringboot.service.DepartementService;
import com.gestionrh.projetfinalspringboot.service.EmployeService;
import com.gestionrh.projetfinalspringboot.service.ProjetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour la page d'accueil et le dashboard
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeService employeService;
    private final DepartementService departementService;
    private final ProjetService projetService;
    private final CongeService congeService;

    /**
     * Page d'accueil - redirige vers le dashboard
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    /**
     * Dashboard principal
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Statistiques avec vraies données
        DashboardStats stats = new DashboardStats();
        stats.totalEmployes = employeService.findAll().size();
        stats.totalDepartements = departementService.findAll().size();
        stats.projetsActifs = projetService.getProjetsByStatut(StatutProjet.EN_COURS).size();
        stats.congesEnAttente = congeService.findCongesEnAttente().size();
        stats.projetsRetard = 0; // À implémenter plus tard avec logique de date
        
        model.addAttribute("stats", stats);
        model.addAttribute("employes", employeService.findAll().stream().limit(5).toList());
        model.addAttribute("departements", departementService.findAll());
        model.addAttribute("notifications", java.util.Collections.emptyList());
        model.addAttribute("page", "dashboard");
        
        return "dashboard";
    }

    /**
     * Classe pour les statistiques du dashboard
     */
    public static class DashboardStats {
        public int totalEmployes = 0;
        public int totalDepartements = 0;
        public int projetsActifs = 0;
        public int congesEnAttente = 0;
        public int projetsRetard = 0;
        
        public int getTotalEmployes() { return totalEmployes; }
        public int getTotalDepartements() { return totalDepartements; }
        public int getProjetsActifs() { return projetsActifs; }
        public int getCongesEnAttente() { return congesEnAttente; }
        public int getProjetsRetard() { return projetsRetard; }
    }
}
