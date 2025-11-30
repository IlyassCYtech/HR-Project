package com.gestionrh.projetfinalspringboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuration de Spring Security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/h2-console/**"))
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/login", "/error", "/css/**", "/js/**",
                                                                "/images/**", "/webjars/**")
                                                .permitAll()
                                                .requestMatchers("/h2-console/**").permitAll()
                                                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                                                .requestMatchers("/api/health", "/api/info").permitAll()
                                                // Password reset
                                                .requestMatchers("/forgot-password", "/reset-password").permitAll()

                                                // *** ACCÈS FORMULAIRES ET MODIFICATIONS ***
                                                // Projets : ADMIN, RH, CHEF_DEPT uniquement
                                                .requestMatchers("/projets/form/**", "/projets/create",
                                                                "/projets/update/**",
                                                                "/projets/delete/**")
                                                .hasAnyRole("ADMIN", "RH", "CHEF_DEPT")

                                                // Départements :
                                                // - Création : ADMIN, RH uniquement
                                                .requestMatchers("/departements/form", "/departements/create")
                                                .hasAnyRole("ADMIN", "RH")
                                                // - Modification : ADMIN, RH, CHEF_DEPT (le contrôleur vérifie que
                                                // c'est son département)
                                                .requestMatchers("/departements/form/**", "/departements/update/**")
                                                .hasAnyRole("ADMIN", "RH", "CHEF_DEPT")
                                                // - Suppression : ADMIN, RH uniquement
                                                .requestMatchers("/departements/delete/**")
                                                .hasAnyRole("ADMIN", "RH")

                                                // Fiches de paie : ADMIN, RH uniquement
                                                .requestMatchers("/fiches-paie/form/**", "/fiches-paie/create",
                                                                "/fiches-paie/update/**",
                                                                "/fiches-paie/generate/**", "/fiches-paie/delete/**")
                                                .hasAnyRole("ADMIN", "RH")

                                                // Congés :
                                                // - Création : Accessible à tous les utilisateurs authentifiés (pour
                                                // demander
                                                // un congé)
                                                .requestMatchers("/conges/form", "/conges/create").authenticated()
                                                // - Modification/Suppression/Approbation : ADMIN, RH uniquement
                                                .requestMatchers("/conges/update/**", "/conges/delete/**",
                                                                "/conges/approve/**",
                                                                "/conges/reject/**", "/conges/form/**")
                                                .hasAnyRole("ADMIN", "RH")

                                                // Employés :
                                                // - Création : ADMIN, RH uniquement
                                                .requestMatchers("/employes/form", "/employes/create")
                                                .hasAnyRole("ADMIN", "RH")
                                                // - Modification : Accessible aux authentifiés (le contrôleur vérifie
                                                // que c'est ADMIN, RH ou l'employé lui-même)
                                                .requestMatchers("/employes/form/**", "/employes/update/**")
                                                .authenticated()
                                                // - Suppression : ADMIN, RH uniquement
                                                .requestMatchers("/employes/delete/**")
                                                .hasAnyRole("ADMIN", "RH")
                                                // - Génération d'identifiants : ADMIN, RH uniquement
                                                .requestMatchers("/employes/generate-credentials/**",
                                                                "/employes/reset-password/**",
                                                                "/employes/export-credentials-pdf",
                                                                "/employes/export-credentials-zip")
                                                .hasAnyRole("ADMIN", "RH")

                                                .requestMatchers("/profil/**").authenticated()
                                                .requestMatchers("/statistiques/**")
                                                .hasAnyRole("ADMIN", "RH", "CHEF_DEPT")
                                                .requestMatchers("/dashboard").authenticated()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .maximumSessions(1));

                // Pour H2 Console (développement uniquement)
                http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
