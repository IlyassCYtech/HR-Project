# Système de Réinitialisation de Mot de Passe

## 📧 Vue d'ensemble

Ce système permet aux utilisateurs de réinitialiser leur mot de passe en recevant un email avec un lien sécurisé.

## ✨ Fonctionnalités

- **Email sécurisé** : Envoi via Gmail SMTP avec authentification
- **Token unique** : Chaque demande génère un UUID unique
- **Expiration automatique** : Les tokens expirent après 1 heure
- **Usage unique** : Un token ne peut être utilisé qu'une seule fois
- **Validation** : Vérification complète (existence, expiration, utilisation)
- **Sécurité** : Mot de passe hashé avec BCrypt

## 🔧 Configuration

### Application Properties

```properties
# Configuration Email (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=rh.elegence@gmail.com
spring.mail.password=wzsz unfs injz zzpi
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Configuration Password Reset
password.reset.token-validity-hours=1
password.reset.base-url=http://localhost:8080
password.reset.context-path=/gestion-rh
```

### Prérequis Gmail

1. **Activer l'authentification à 2 facteurs** sur votre compte Gmail
2. **Créer un mot de passe d'application** :
   - Allez sur https://myaccount.google.com/apppasswords
   - Sélectionnez "Courrier" et "Autre appareil"
   - Copiez le mot de passe de 16 caractères généré
   - Utilisez-le dans `spring.mail.password`

## 🗂️ Structure de la Base de Données

```sql
CREATE TABLE password_reset_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    utilisateur_id INT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used TINYINT(1) DEFAULT 0,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_expiry (expiry_date)
);
```

## 🔄 Flux de Réinitialisation

### 1. Demande de réinitialisation

**URL** : `/forgot-password`

**Étapes** :
1. L'utilisateur entre son email
2. Le système vérifie si un compte existe avec cet email
3. Un token UUID est généré
4. Le token est sauvegardé avec une date d'expiration (1h)
5. Un email est envoyé avec le lien de réinitialisation

**Email envoyé** :
```
Sujet : Réinitialisation de votre mot de passe - RH Élégance
Lien : http://localhost:8080/gestion-rh/reset-password?token={UUID}
```

### 2. Validation du token

**URL** : `/reset-password?token={UUID}`

**Vérifications** :
- ✅ Le token existe
- ✅ Le token n'a pas expiré (< 1h)
- ✅ Le token n'a pas été utilisé
- ❌ Sinon : redirection vers `/forgot-password` avec erreur

### 3. Réinitialisation du mot de passe

**URL** : `/reset-password` (POST)

**Validations** :
- Mot de passe minimum 6 caractères
- Mot de passe et confirmation identiques
- Token valide

**Actions** :
- Le mot de passe est hashé avec BCrypt
- Le mot de passe de l'utilisateur est mis à jour
- Le token est marqué comme utilisé
- Redirection vers `/login` avec message de succès

## 📁 Architecture du Code

### Entité

**PasswordResetToken.java**
```java
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    private Long id;
    private String token;          // UUID unique
    private Utilisateur utilisateur;
    private LocalDateTime expiryDate;
    private boolean used;
    private LocalDateTime createdDate;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}
```

### Repository

**PasswordResetTokenRepository.java**
- `findByToken(String token)` : Recherche par token
- `findByUtilisateur(Utilisateur user)` : Recherche par utilisateur
- `deleteByUtilisateur(Utilisateur user)` : Suppression par utilisateur

### Services

**EmailService.java**
- Utilise `JavaMailSender` pour envoyer des emails HTML
- Template Thymeleaf : `templates/email/password-reset.html`
- Sender : `rh.elegance@gmail.com`

**PasswordResetService.java**
- `createPasswordResetTokenForUser(String email)` : Crée token et envoie email
- `validatePasswordResetToken(String token)` : Valide le token
- `resetPassword(String token, String newPassword)` : Réinitialise le mot de passe
- `getUserByToken(String token)` : Récupère l'utilisateur depuis le token

### Controller

**PasswordResetController.java**
- `GET /forgot-password` : Affiche le formulaire d'email
- `POST /forgot-password` : Traite la demande de réinitialisation
- `GET /reset-password?token=xxx` : Affiche le formulaire de nouveau mot de passe
- `POST /reset-password` : Traite la réinitialisation

### Templates

**forgot-password.html**
- Formulaire d'entrée d'email
- Style élégant avec fond Palais de l'Élysée
- Messages d'erreur/succès

**reset-password.html**
- Formulaire de nouveau mot de passe
- Indicateur de force du mot de passe
- Toggle pour afficher/masquer le mot de passe
- Validation JavaScript

**email/password-reset.html**
- Email HTML responsive
- Bouton CTA pour réinitialiser
- Lien de secours si le bouton ne fonctionne pas
- Branding RH Élégance

## 🔒 Sécurité

### Protection CSRF
Tous les formulaires incluent le token CSRF :
```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
```

### Accès Public
Configuration dans `SecurityConfig.java` :
```java
.requestMatchers("/forgot-password", "/reset-password").permitAll()
```

### Hachage des Mots de Passe
BCrypt avec salt automatique :
```java
passwordEncoder.encode(newPassword)
```

### Tokens
- UUID version 4 (128 bits, cryptographiquement sécurisé)
- Stockage en base de données
- Expiration après 1 heure
- Usage unique

## 🎨 Interface Utilisateur

### Page de Connexion
- Lien "Mot de passe oublié ?" ajouté sous le bouton de connexion
- Icône clé (FontAwesome)
- Couleur dorée (#C5A572) cohérente avec le design

### Page Mot de Passe Oublié
- Fond : Image du Palais de l'Élysée
- Card semi-transparente avec backdrop-filter
- Info box avec instructions
- Messages de succès/erreur

### Page Nouveau Mot de Passe
- Gradient violet élégant
- Indicateur de force du mot de passe (faible/moyen/fort)
- Toggle pour afficher/masquer les mots de passe
- Validation en temps réel

### Email HTML
- Design responsive
- Header avec gradient violet
- Bouton CTA doré (#C5A572)
- Footer professionnel
- Lien de secours

## 📝 Messages

### Succès
- **Forgot Password** : "Un email de réinitialisation a été envoyé si un compte existe avec cet email"
- **Reset Password** : "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter."

### Erreurs
- **Token invalide** : "Le lien de réinitialisation est invalide ou a expiré"
- **Token expiré** : "Le lien de réinitialisation a expiré. Veuillez faire une nouvelle demande"
- **Token utilisé** : "Ce lien de réinitialisation a déjà été utilisé"
- **Mots de passe différents** : "Les mots de passe ne correspondent pas"
- **Mot de passe court** : "Le mot de passe doit contenir au moins 6 caractères"

## 🧪 Test du Système

### 1. Test de la demande

1. Allez sur http://localhost:8080/gestion-rh/login
2. Cliquez sur "Mot de passe oublié ?"
3. Entrez un email valide (ex: jean.martin@entreprise.com)
4. Vérifiez la réception de l'email

### 2. Test de la réinitialisation

1. Ouvrez l'email reçu
2. Cliquez sur le bouton "Réinitialiser mon mot de passe"
3. Entrez un nouveau mot de passe (min 6 caractères)
4. Confirmez le mot de passe
5. Vérifiez la redirection vers login avec succès

### 3. Test de l'expiration

1. Générez un token
2. Attendez 1 heure
3. Essayez d'utiliser le lien
4. Vérifiez le message d'erreur "expiré"

### 4. Test de l'usage unique

1. Réinitialisez un mot de passe avec succès
2. Réutilisez le même lien
3. Vérifiez le message d'erreur "déjà utilisé"

## 🐛 Dépannage

### Email non reçu

**Vérifications** :
1. Le mot de passe d'application Gmail est correct
2. L'authentification à 2 facteurs est activée
3. L'adresse email de l'utilisateur existe dans la base
4. Vérifiez les logs Spring pour les erreurs SMTP

**Logs utiles** :
```
logging.level.org.springframework.mail=DEBUG
```

### Erreur SMTP Authentication Failed

**Solution** :
1. Régénérez un mot de passe d'application Gmail
2. Vérifiez que vous utilisez le mot de passe d'app (16 caractères) et non le mot de passe Gmail
3. Vérifiez `spring.mail.properties.mail.smtp.auth=true`

### Token non trouvé

**Causes possibles** :
- La table `password_reset_tokens` n'existe pas → Exécutez le script SQL
- Le token a été supprimé manuellement
- Problème de cascade delete

## 🔄 Maintenance

### Nettoyage des tokens expirés

Ajoutez une tâche planifiée pour supprimer les tokens expirés :

```java
@Scheduled(cron = "0 0 * * * *") // Toutes les heures
public void cleanupExpiredTokens() {
    LocalDateTime now = LocalDateTime.now();
    List<PasswordResetToken> expiredTokens = tokenRepository.findByExpiryDateBefore(now);
    tokenRepository.deleteAll(expiredTokens);
}
```

### Surveillance

Loggez les événements importants :
- Demandes de réinitialisation
- Tokens expirés utilisés
- Tentatives avec tokens invalides
- Réinitialisations réussies

## 📊 Statistiques

Vous pouvez tracker :
- Nombre de demandes par jour
- Taux de réussite des réinitialisations
- Temps moyen entre demande et réinitialisation
- Nombre de tokens expirés

## 🚀 Améliorations Futures

- [ ] Limiter le nombre de demandes par IP/email
- [ ] Notification par SMS en plus de l'email
- [ ] Historique des réinitialisations
- [ ] Questions de sécurité
- [ ] Authentification à deux facteurs
- [ ] Template d'email personnalisable
- [ ] Support multi-langues
- [ ] Dashboard admin pour gérer les tokens

## 📞 Support

En cas de problème :
1. Vérifiez les logs Spring Boot
2. Testez la connexion SMTP avec un client mail
3. Vérifiez que la table existe en base
4. Consultez la documentation Gmail SMTP

---

**Version** : 1.0.0  
**Date** : 2025  
**Auteur** : RH Élégance Team
