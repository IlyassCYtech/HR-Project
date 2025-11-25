package com.gestionrh.projetfinalspringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/403")
    public String error403(Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", "Accès refusé");
        model.addAttribute("errorDescription", "Vous n'avez pas la permission d'accéder à cette ressource.");
        return "error/403";
    }

    @GetMapping("/404")
    public String error404(Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "Page non trouvée");
        model.addAttribute("errorDescription", "La page que vous recherchez n'existe pas.");
        return "error/404";
    }

    @GetMapping("/500")
    public String error500(Model model) {
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "Erreur serveur");
        model.addAttribute("errorDescription", "Une erreur interne s'est produite. Veuillez réessayer plus tard.");
        return "error/500";
    }
}
