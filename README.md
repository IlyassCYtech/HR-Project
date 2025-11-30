# 🌟 RH Élégance - Système de Gestion RH Moderne

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.11-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![License](https://img.shields.io/badge/license-Educational-lightgrey.svg)

**La solution complète pour gérer vos ressources humaines avec élégance et efficacité**

[🚀 Démarrage Rapide](#-installation-et-démarrage) • [📖 Fonctionnalités](#-fonctionnalités-principales) • [🎨 Captures d'écran](#-aperçu-de-lapplication) • [👥 Comptes de Test](#-comptes-de-test)

</div>

---

## 🎯 Pourquoi RH Élégance ?

RH Élégance est une **application web moderne** de gestion des ressources humaines qui simplifie et automatise tous vos processus RH :

✨ **Interface élégante** - Design professionnel avec thème doré  
🔒 **Sécurité renforcée** - Authentification par rôles et protection des données  
⚡ **Performances optimales** - Recherches et filtres instantanés  
📱 **Responsive** - Fonctionne sur ordinateur, tablette et mobile  
🤖 **Automatisation** - Calculs automatiques, notifications et synchronisation  

---

## 🚀 Installation et Démarrage

### Prérequis

Avant de commencer, assurez-vous d'avoir :

- ☕ **Java 17** ou supérieur ([Télécharger](https://adoptium.net/))
- 🗄️ **MySQL 8.0** ou supérieur ([Télécharger](https://dev.mysql.com/downloads/mysql/))
- 📦 **Maven 3.8+** (inclus avec l'IDE)
- 💻 **IDE** : Eclipse, IntelliJ IDEA ou VS Code

### Installation en 3 étapes

#### Étape 1 : Cloner le projet

```bash
git clone https://github.com/IlyassCYtech/HR-Project.git
cd projetfinalspringboot
```

#### Étape 2 : Configurer la base de données

1. **Démarrez MySQL** et connectez-vous :
```bash
mysql -u root -p
```

2. **Exécutez le script SQL** :
```sql
source database_setup_springboot.sql
```

Ou via MySQL Workbench :
- Ouvrez MySQL Workbench
- Connectez-vous à votre serveur
- Ouvrez le fichier `database_setup_springboot.sql`
- Exécutez le script (⚡ Execute)

#### Étape 3 : Configurer l'application

Modifiez `src/main/resources/application.properties` :

```properties
# Base de données
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_rh_springboot
spring.datasource.username=root
spring.datasource.password=VOTRE_MOT_DE_PASSE

# Email (optionnel pour les tests)
spring.mail.username=votre.email@gmail.com
spring.mail.password=votre_app_password
```

### 🎬 Lancer l'application

**Option 1 : Avec Maven**
```bash
mvn clean install
mvn spring-boot:run
```

**Option 2 : Depuis votre IDE**
- Ouvrez le projet dans Eclipse/IntelliJ
- Lancez `ProjetfinalspringbootApplication.java`
- Attendez le message : `Started ProjetfinalspringbootApplication in X seconds`

**🌐 Accédez à l'application** : [http://localhost:8080/gestion-rh](http://localhost:8080/gestion-rh)

---

## 👥 Comptes de Test

### 🔑 Administrateur
```
Username: admin
Password: admin123
Accès complet à toutes les fonctionnalités
```

### 👔 Ressources Humaines
```
Username: rh
Password: rh123
Gestion des employés, congés et fiches de paie
```

### 📊 Chef de Département
```
Username: manager
Password: manager123
Gestion de son département et approbation des congés
```

### 💼 Chef de Projet
```
Username: pierre.leroy
Password: password123
Gestion des membres de ses projets
```

### 👤 Employé Standard
```
Username: claire.durand
Password: password123
Consultation de son profil et gestion de ses congés
```

---

## 📖 Fonctionnalités Principales

### 🔐 Authentification & Sécurité

![Capture d'écran - Page de connexion]
<!-- Ajouter capture d'écran login.png -->

**Connexion sécurisée avec design élégant**
- ✅ Authentification par username/mot de passe (BCrypt)
- ✅ Réinitialisation de mot de passe par email
- ✅ Gestion des rôles : ADMIN, RH, CHEF_DEPT, CHEF_PROJET, EMPLOYE
- ✅ Protection CSRF et sessions sécurisées
- ✅ Tokens d'authentification avec expiration

**Ce que vous pouvez faire :**
- Connexion rapide avec vos identifiants
- Récupération de mot de passe en un clic
- Déconnexion sécurisée

---

### 👨‍💼 Gestion des Employés

![Capture d'écran - Liste des employés]
<!-- Ajouter capture d'écran employes-list.png -->

**Gérez tous vos employés en un seul endroit**

#### 📋 Liste des Employés
- ✅ **Recherche intelligente** : Par nom, prénom ou matricule
- ✅ **Filtres avancés** : Département, Poste, Grade, Statut
- ✅ **Vue complète** : Photo, Email, Téléphone, Département
- ✅ **Actions rapides** : Voir, Modifier, Supprimer
- ✅ **Export** : Génération d'identifiants en PDF

**Ce que vous pouvez faire :**
- Trouver un employé en quelques secondes
- Filtrer par grade : STAGIAIRE → DIRECTEUR
- Voir uniquement votre département (CHEF_DEPT)
- Générer les identifiants de connexion en masse

---

![Capture d'écran - Profil employé]
<!-- Ajouter capture d'écran employe-profile.png -->

#### 👤 Profil Détaillé
- ✅ **Onglet Informations** : Coordonnées, Poste, Salaire, Ancienneté
- ✅ **Onglet Projets** : Tous les projets de l'employé
- ✅ **Onglet Congés** : Historique et solde
- ✅ **Onglet Fiches de Paie** : Bulletins mensuels
- ✅ **Onglet Documents** : CV, Contrats, Diplômes

**Ce que vous pouvez faire :**
- Consulter toutes les informations d'un employé
- Voir ses projets et ses congés en un clic
- Télécharger ses fiches de paie
- Modifier son profil (selon les permissions)

---

![Capture d'écran - Formulaire employé]
<!-- Ajouter capture d'écran employe-form.png -->

#### ➕ Créer/Modifier un Employé
- ✅ **Formulaire complet** : Informations personnelles et professionnelles
- ✅ **Création de compte** : Génération automatique d'identifiants
- ✅ **Upload photo** : Ajout d'une photo de profil
- ✅ **Validations** : Email unique, Matricule unique
- ✅ **Synchronisation** : Email automatiquement synchronisé

**Ce que vous pouvez faire :**
- Créer un nouvel employé en 2 minutes
- Générer automatiquement son compte utilisateur
- Assigner un département et un manager
- Définir son grade et son salaire

---

### 🏢 Gestion des Départements

![Capture d'écran - Liste des départements]
<!-- Ajouter capture d'écran departements-list.png -->

**Organisez votre entreprise par départements**

#### 📊 Liste des Départements
- ✅ **Vue d'ensemble** : Nom, Budget, Chef, Nombre d'employés
- ✅ **Statut visuel** : Actif (Vert) / Inactif (Gris)
- ✅ **Filtres** : Par nom ou statut
- ✅ **Actions** : Voir détails, Modifier, Archiver
- ✅ **Alertes** : Départements sans chef

**Ce que vous pouvez faire :**
- Voir tous les départements en un coup d'œil
- Identifier les départements sans chef
- Archiver les départements inactifs
- Créer de nouveaux départements

---

![Capture d'écran - Détails département]
<!-- Ajouter capture d'écran departement-details.png -->

#### 🔍 Détails du Département
- ✅ **Statistiques** : Effectif, Masse salariale
- ✅ **Répartition par grade** : Graphique pie chart
- ✅ **Liste des employés** : Tableau filtrable
- ✅ **Projets rattachés** : Tous les projets du département
- ✅ **Chef de département** : Coordonnées et profil

**Ce que vous pouvez faire :**
- Visualiser la composition du département
- Analyser la répartition des grades
- Accéder rapidement aux employés
- Voir les projets en cours

---

### 📁 Gestion des Projets

![Capture d'écran - Liste des projets]
<!-- Ajouter capture d'écran projets-list.png -->

**Pilotez vos projets avec efficacité**

#### 📊 Liste des Projets
- ✅ **Vue cartes** : Design moderne avec badges colorés
- ✅ **Statut visuel** : PLANIFIE, EN_COURS, TERMINE, ANNULE
- ✅ **Priorité** : BASSE, NORMALE, HAUTE, CRITIQUE
- ✅ **Barre de progression** : Avancement calculé automatiquement
- ✅ **Filtres** : Statut, Priorité, Département, Employé

**Ce que vous pouvez faire :**
- Voir tous vos projets actifs
- Filtrer par priorité critique
- Suivre la progression en temps réel
- Voir uniquement vos projets (EMPLOYE)

---

![Capture d'écran - Détails projet]
<!-- Ajouter capture d'écran projet-details.png -->

#### 🔍 Détails du Projet
- ✅ **Informations complètes** : Dates, Budget, Statut
- ✅ **Chef de projet** : Lien vers son profil
- ✅ **Équipe** : Liste des membres avec photos
- ✅ **Progression** : Pourcentage d'avancement
- ✅ **Statistiques** : Temps écoulé/restant

**Ce que vous pouvez faire :**
- Voir toute l'équipe du projet
- Contacter le chef de projet
- Suivre l'avancement
- Modifier les membres (CHEF_PROJET)

---

![Capture d'écran - Formulaire projet]
<!-- Ajouter capture d'écran projet-form.png -->

#### ➕ Créer/Modifier un Projet
- ✅ **Formulaire intuitif** : Nom, Dates, Budget, Priorité
- ✅ **Assignation** : Chef de projet et département
- ✅ **Équipe** : Sélection multiple des membres
- ✅ **Automatique** : Chef ajouté automatiquement aux membres
- ✅ **Validations** : Dates cohérentes, Budget positif

**Ce que vous pouvez faire :**
- Créer un nouveau projet en quelques clics
- Définir les dates et le budget
- Assigner un chef de projet
- Constituer l'équipe

---

### 📅 Gestion des Congés

![Capture d'écran - Liste des congés]
<!-- Ajouter capture d'écran conges-list.png -->

**Simplifiez la gestion des absences**

#### 📋 Liste des Congés
- ✅ **Vue complète** : Employé, Type, Dates, Motif, Statut
- ✅ **Badges colorés** : EN_ATTENTE (Orange), APPROUVE (Vert), REFUSE (Rouge)
- ✅ **Confidentialité** : Motifs confidentiels masqués
- ✅ **Filtres** : Par employé, Type, Statut
- ✅ **Actions rapides** : Approuver/Refuser en un clic

**Ce que vous pouvez faire :**
- Voir toutes les demandes en attente
- Filtrer par type (Maladie, Congés payés, etc.)
- Approuver ou refuser avec commentaire
- Consulter l'historique complet

---

![Capture d'écran - Mes congés]
<!-- Ajouter capture d'écran mes-conges.png -->

#### 📝 Mes Congés (Vue Employé)
- ✅ **Solde de congés** : Jours restants en temps réel
- ✅ **Historique** : Toutes vos demandes
- ✅ **Statistiques** : Congés pris/restants
- ✅ **Création** : Nouvelle demande en 1 minute
- ✅ **Modification** : Tant que EN_ATTENTE

**Ce que vous pouvez faire :**
- Consulter votre solde de congés
- Faire une nouvelle demande
- Voir le statut de vos demandes
- Modifier avant approbation

---

![Capture d'écran - Formulaire congé]
<!-- Ajouter capture d'écran conge-form.png -->

#### ➕ Demander un Congé
- ✅ **Types disponibles** : Congés payés, Maladie, Maternité/Paternité, Formation
- ✅ **Calcul automatique** : Nombre de jours calculé
- ✅ **Motif confidentiel** : Option pour masquer le motif
- ✅ **Validations** : Chevauchements, Solde disponible
- ✅ **Notification** : RH/Chef alertés automatiquement

**Ce que vous pouvez faire :**
- Sélectionner les dates de début et fin
- Choisir le type de congé
- Ajouter un motif (facultatif)
- Marquer comme confidentiel si nécessaire

---

### 💰 Gestion des Fiches de Paie

![Capture d'écran - Liste des fiches de paie]
<!-- Ajouter capture d'écran fiches-paie-list.png -->

**Gérez les salaires avec précision**

#### 📋 Liste des Fiches de Paie
- ✅ **Vue mensuelle** : Par employé et période
- ✅ **Calculs automatiques** : Brut, Net, Primes, Déductions
- ✅ **Tri intelligent** : Par date décroissante
- ✅ **Filtres** : Employé, Mois, Année, Département
- ✅ **Export PDF** : Bulletin officiel

**Ce que vous pouvez faire :**
- Voir toutes les fiches du mois
- Filtrer par employé ou département
- Télécharger en PDF
- Créer de nouvelles fiches

---

![Capture d'écran - Détails fiche de paie]
<!-- Ajouter capture d'écran fiche-paie-details.png -->

#### 💵 Détails de la Fiche de Paie
- ✅ **Salaire de base** : Montant mensuel
- ✅ **Primes** : Performance, Ancienneté, Responsabilité
- ✅ **Heures supplémentaires** : Calcul automatique
- ✅ **Déductions** : Cotisations, Impôts, Absences
- ✅ **Salaire net** : Calculé automatiquement

**Ce que vous pouvez faire :**
- Voir le détail ligne par ligne
- Comprendre les calculs
- Télécharger le bulletin
- Modifier si nécessaire (ADMIN/RH)

---

### 🎨 Dashboard & Statistiques

![Capture d'écran - Dashboard]
<!-- Ajouter capture d'écran dashboard.png -->

**Vue d'ensemble de votre activité RH**

#### 📊 Tableau de Bord
- ✅ **KPI en temps réel** : Effectif, Masse salariale, Projets actifs
- ✅ **Graphiques** : Répartition par département, grade, statut
- ✅ **Alertes** : Congés en attente, Contrats à renouveler
- ✅ **Actions rapides** : Créer employé, Approuver congés
- ✅ **Adapté au rôle** : Contenu personnalisé

**Ce que vous pouvez faire :**
- Visualiser les indicateurs clés
- Analyser les tendances
- Voir les alertes importantes
- Accéder rapidement aux actions

---

## 🔒 Sécurité & Rôles

### Hiérarchie des Permissions

| Rôle | Employés | Départements | Projets | Congés | Fiches Paie |
|------|----------|--------------|---------|--------|-------------|
| **ADMIN** | ✅ Complet | ✅ Complet | ✅ Complet | ✅ Complet | ✅ Complet |
| **RH** | ✅ Complet | ✅ Lecture | ✅ Lecture | ✅ Approbation | ✅ Complet |
| **CHEF_DEPT** | 👁️ Son département | 👁️ Son département | ✅ Son département | ✅ Approbation | 👁️ Son département |
| **CHEF_PROJET** | 👁️ Membres | - | ✅ Ses projets | - | - |
| **EMPLOYE** | 👁️ Son profil | - | 👁️ Ses projets | ✅ Ses demandes | 👁️ Ses fiches |

### Protections Automatiques

- 🔒 **Anti-auto-suppression** : Impossible de supprimer son propre compte
- 🔒 **Suppression en cascade** : Utilisateur supprimé avec l'employé (trigger SQL)
- 🔒 **Motifs confidentiels** : Masqués aux employés normaux
- 🔒 **Synchronisation email** : Automatique entre employé et utilisateur
- 🔒 **Mots de passe** : Hashés avec BCrypt
- 🔒 **Sessions** : Expiration et invalidation automatique

---

## 🛠️ Technologies Utilisées

### Backend
- ☕ **Java 17** - Langage de programmation
- 🍃 **Spring Boot 3.4.11** - Framework applicatif
- 🔐 **Spring Security 6.4.12** - Authentification et autorisation
- 📧 **Spring Mail** - Envoi d'emails
- 🗄️ **Hibernate/JPA** - ORM pour la base de données

### Frontend
- 🎨 **Thymeleaf** - Moteur de templates
- 🎭 **HTML5/CSS3** - Structure et style
- ⚡ **JavaScript** - Interactions dynamiques
- 🎯 **Font Awesome** - Icônes
- 🖋️ **Google Fonts** - Typographie (Playfair Display, Inter)

### Base de Données
- 🗄️ **MySQL 8.0** - Stockage relationnel
- 🔄 **Triggers SQL** - Synchronisation automatique
- 📊 **Vues SQL** - Requêtes optimisées
- 🔗 **Contraintes CASCADE** - Intégrité référentielle

### Outils & Build
- 📦 **Maven** - Gestion des dépendances
- 📄 **iText 7** - Génération de PDF
- 🔢 **QR Code** - Génération de QR codes
- 🔧 **Lombok** - Réduction du code boilerplate

---

## 📁 Structure du Projet

```
projetfinalspringboot/
├── src/
│   ├── main/
│   │   ├── java/com/gestionrh/projetfinalspringboot/
│   │   │   ├── controller/          # Contrôleurs MVC
│   │   │   │   ├── EmployeViewController.java
│   │   │   │   ├── DepartementViewController.java
│   │   │   │   ├── ProjetViewController.java
│   │   │   │   ├── CongeViewController.java
│   │   │   │   └── FichePaieViewController.java
│   │   │   ├── model/               # Entités JPA
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Employe.java
│   │   │   │   │   ├── Departement.java
│   │   │   │   │   ├── Projet.java
│   │   │   │   │   ├── CongeAbsence.java
│   │   │   │   │   ├── FichePaie.java
│   │   │   │   │   └── Utilisateur.java
│   │   │   │   └── enums/           # Énumérations
│   │   │   ├── repository/          # Repositories JPA
│   │   │   ├── service/             # Services métier
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── config/              # Configuration Spring Security
│   │   │   └── util/                # Utilitaires (PDF, Email)
│   │   └── resources/
│   │       ├── application.properties   # Configuration
│   │       ├── templates/              # Templates Thymeleaf
│   │       │   ├── layout/
│   │       │   ├── employes/
│   │       │   ├── departements/
│   │       │   ├── projets/
│   │       │   ├── conges/
│   │       │   └── fiches-paie/
│   │       └── static/
│   │           ├── css/
│   │           ├── js/
│   │           └── images/
│   └── test/                        # Tests unitaires
├── database_setup_springboot.sql    # Script SQL
├── pom.xml                          # Dépendances Maven
├── README.md                        # Ce fichier
└── FONCTIONNALITES_APPLICATION.md   # Documentation complète
```

---

## 🐛 Dépannage

### Problème : Erreur de connexion à MySQL

**Solution :**
```properties
# Vérifiez vos identifiants dans application.properties
spring.datasource.username=root
spring.datasource.password=VOTRE_MOT_DE_PASSE
```

### Problème : Port 8080 déjà utilisé

**Solution :**
```properties
# Changez le port dans application.properties
server.port=8081
```

### Problème : Erreur "Table doesn't exist"

**Solution :**
```bash
# Réexécutez le script SQL
mysql -u root -p < database_setup_springboot.sql
```

### Problème : Emails non envoyés

**Solution :**
```properties
# Activez l'accès aux applications moins sécurisées dans Gmail
# Ou générez un mot de passe d'application
spring.mail.password=votre_app_password
```

---

## 📚 Documentation Complète

Pour plus de détails sur toutes les fonctionnalités, consultez :
- 📖 [FONCTIONNALITES_APPLICATION.md](FONCTIONNALITES_APPLICATION.md) - Documentation exhaustive (107k+ caractères)
- 🗄️ [database_setup_springboot.sql](database_setup_springboot.sql) - Structure de la base de données avec commentaires

---

## 🎯 Roadmap

### Version 1.1 (Q1 2026)
- [ ] Module de recrutement complet
- [ ] Gestion des documents RH (upload/storage)
- [ ] Notifications email automatiques
- [ ] Export Excel/CSV pour tous les modules

### Version 1.2 (Q2 2026)
- [ ] Module de pointage et présence
- [ ] Planning des équipes
- [ ] Gestion des formations
- [ ] API REST publique

### Version 2.0 (Q3-Q4 2026)
- [ ] Évaluations de performance
- [ ] Entretiens annuels
- [ ] Application mobile (iOS/Android)
- [ ] Tableau de bord temps réel avec IA

---

## 🤝 Contribution

Ce projet est développé dans un cadre éducatif. Les suggestions et améliorations sont les bienvenues !

### Comment contribuer ?

1. **Fork** le projet
2. **Créez** une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. **Committez** vos changements (`git commit -m 'Add some AmazingFeature'`)
4. **Push** vers la branche (`git push origin feature/AmazingFeature`)
5. **Ouvrez** une Pull Request

---

## 📄 Licence

Ce projet est développé dans un cadre éducatif par **IlyassCYtech**.

---

## 🙏 Remerciements

Merci d'avoir pris le temps de découvrir **RH Élégance** ! 

Si vous avez des questions ou suggestions, n'hésitez pas à ouvrir une issue sur GitHub.

---

<div align="center">

**Développé avec ❤️ par [IlyassCYtech](https://github.com/IlyassCYtech)**

⭐ Si vous aimez ce projet, n'oubliez pas de lui donner une étoile !

[🔝 Retour en haut](#-rh-élégance---système-de-gestion-rh-moderne)

</div>
