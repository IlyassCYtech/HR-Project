package com.gestionrh.projetfinalspringboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import com.gestionrh.projetfinalspringboot.model.entity.Utilisateur;
import com.gestionrh.projetfinalspringboot.model.enums.Role;
import com.gestionrh.projetfinalspringboot.model.enums.StatutUtilisateur;
import com.gestionrh.projetfinalspringboot.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Classe principale de l'application Spring Boot
 * Gestion RH - Migration du projet JEE vers Spring Boot
 */
@SpringBootApplication
@EnableJpaRepositories
public class ProjetfinalspringbootApplication {
	@Bean
	public CommandLineRunner initAdminUser(@Autowired UtilisateurRepository utilisateurRepository, 
			@Autowired PasswordEncoder passwordEncoder) {
		return args -> {
			String username = "admin";
			String email = "admin@entreprise.com";
			String wantedPassword = "admin123";
			Utilisateur admin = utilisateurRepository.findByUsername(username).orElse(null);
			String wantedHash = passwordEncoder.encode(wantedPassword);
			if (admin == null) {
				admin = Utilisateur.builder()
						.username(username)
						.email(email)
						.role(Role.ADMIN)
						.statut(StatutUtilisateur.ACTIF)
						.passwordHash(wantedHash)
						.build();
				utilisateurRepository.save(admin);
				System.out.println("[INIT] Utilisateur admin créé avec le mot de passe 'admin123'.");
			} else {
				admin.setPasswordHash(wantedHash);
				admin.setStatut(StatutUtilisateur.ACTIF);
				utilisateurRepository.save(admin);
				System.out.println("[INIT] Utilisateur admin mis à jour avec le mot de passe 'admin123'.");
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(ProjetfinalspringbootApplication.class, args);
		System.out.println("\n==========================================================");
		System.out.println("🚀 Application Gestion RH démarrée avec succès!");
		System.out.println("📍 URL: http://localhost:8080/gestion-rh");
		System.out.println("🏥 Health Check: http://localhost:8080/gestion-rh/api/health");
		System.out.println("ℹ️  Info: http://localhost:8080/gestion-rh/api/info");
		System.out.println("==========================================================\n");
	}
	
	
	
	

	
}

