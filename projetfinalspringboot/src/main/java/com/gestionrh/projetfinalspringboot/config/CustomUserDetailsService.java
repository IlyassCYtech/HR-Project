package com.gestionrh.projetfinalspringboot.config;

import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Service pour charger les utilisateurs pour Spring Security
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Tentative de chargement de l'utilisateur: {}", username);
        Utilisateur utilisateur = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé: {}", username);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + username);
                });

        log.info("Utilisateur trouvé: {} | Statut: {} | Role: {}", utilisateur.getUsername(), utilisateur.getStatut(), utilisateur.getRole());
        log.debug("Hash du mot de passe récupéré: {}", utilisateur.getPasswordHash());

        boolean locked = utilisateur.getStatut().name().equals("BLOQUE");
        boolean disabled = !utilisateur.getStatut().name().equals("ACTIF");
        log.info("Compte locké: {} | Compte désactivé: {}", locked, disabled);

        return User.builder()
                .username(utilisateur.getUsername())
                .password(utilisateur.getPasswordHash())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())
                ))
                .accountExpired(false)
                .accountLocked(locked)
                .credentialsExpired(false)
                .disabled(disabled)
                .build();
    }
}
