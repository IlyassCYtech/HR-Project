package com.gestionrh.filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Filtre d'authentification pour sécuriser l'accès aux pages de l'application
 */
// @WebFilter("/app/*")
public class AuthenticationFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("Initialisation du filtre d'authentification");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        
        System.out.println("Vérification de l'authentification pour: " + requestURI);
        
        // Vérifier si l'utilisateur est connecté
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("utilisateur") != null);
        
        if (isLoggedIn) {
            // Utilisateur connecté, continuer
            System.out.println("Utilisateur authentifié, accès autorisé");
            chain.doFilter(request, response);
        } else {
            // Utilisateur non connecté, rediriger vers la page de connexion
            System.out.println("Utilisateur non authentifié, redirection vers login");
            String loginURL = contextPath + "/login";
            
            // Sauvegarder l'URL demandée pour redirection après connexion
            String originalURL = requestURI;
            String queryString = httpRequest.getQueryString();
            if (queryString != null) {
                originalURL += "?" + queryString;
            }
            session = httpRequest.getSession(true);
            session.setAttribute("originalURL", originalURL);
            
            httpResponse.sendRedirect(loginURL);
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("Destruction du filtre d'authentification");
    }
}
