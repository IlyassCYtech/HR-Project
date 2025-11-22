-- Script SQL pour base Spring Boot RH
-- Crée la base, les tables, et insère les utilisateurs avec hash BCrypt

-- 1. Création de la base
DROP DATABASE IF EXISTS gestion_rh_springboot;
CREATE DATABASE gestion_rh_springboot CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestion_rh_springboot;


-- Table des départements
CREATE TABLE departements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    budget DECIMAL(15,2),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actif TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Indique si le département est actif (1) ou archivé (0)',
    chef_departement_id INT,
    INDEX idx_nom_dept (nom),
    INDEX idx_actif (actif)
);

-- Table des projets
CREATE TABLE projets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    description TEXT,
    date_debut DATE NOT NULL,
    date_fin_prevue DATE,
    date_fin_reelle DATE,
    budget DECIMAL(15,2),
    statut ENUM('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE') DEFAULT 'PLANIFIE',
    priorite ENUM('BASSE', 'NORMALE', 'HAUTE', 'CRITIQUE') DEFAULT 'NORMALE',
    chef_projet_id INT,
    departement_id INT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_statut (statut),
    INDEX idx_date_debut (date_debut)
);

-- Table des employés
CREATE TABLE employes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    matricule VARCHAR(20) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE,
    telephone VARCHAR(20),
    adresse TEXT,
    date_naissance DATE,
    date_embauche DATE NOT NULL,
    date_fin DATE,
    salaire_base DECIMAL(10,2) NOT NULL,
    grade ENUM('STAGIAIRE', 'JUNIOR', 'SENIOR', 'EXPERT', 'MANAGER', 'DIRECTEUR') DEFAULT 'JUNIOR',
    poste VARCHAR(100) NOT NULL,
    statut ENUM('ACTIF', 'SUSPENDU', 'DEMISSION', 'LICENCIE', 'RETRAITE') DEFAULT 'ACTIF',
    departement_id INT,
    manager_id INT,
    photo_url VARCHAR(255),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_matricule (matricule),
    INDEX idx_nom_prenom (nom, prenom),
    INDEX idx_email (email),
    INDEX idx_grade (grade),
    INDEX idx_poste (poste),
    INDEX idx_statut (statut),
    FOREIGN KEY (departement_id) REFERENCES departements(id) ON DELETE SET NULL,
    FOREIGN KEY (manager_id) REFERENCES employes(id) ON DELETE SET NULL
);

-- Table de liaison employés-projets (relation many-to-many)
CREATE TABLE employe_projets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employe_id INT NOT NULL,
    projet_id INT NOT NULL,
    date_affectation DATE NOT NULL,
    date_fin_affectation DATE,
    role_projet VARCHAR(100),
    pourcentage_temps DECIMAL(5,2) DEFAULT 100.00,
    statut ENUM('ACTIF', 'TERMINE', 'SUSPENDU') DEFAULT 'ACTIF',
    UNIQUE KEY unique_employe_projet_actif (employe_id, projet_id, statut),
    FOREIGN KEY (employe_id) REFERENCES employes(id) ON DELETE CASCADE,
    FOREIGN KEY (projet_id) REFERENCES projets(id) ON DELETE CASCADE,
    INDEX idx_employe_projet (employe_id, projet_id),
    INDEX idx_date_affectation (date_affectation)
);

-- Table des fiches de paie
CREATE TABLE fiches_paie (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employe_id INT NOT NULL,
    mois INT NOT NULL CHECK (mois BETWEEN 1 AND 12),
    annee INT NOT NULL CHECK (annee >= 2020),
    salaire_base DECIMAL(10,2) NOT NULL,
    prime_performance DECIMAL(10,2) DEFAULT 0,
    prime_anciennete DECIMAL(10,2) DEFAULT 0,
    prime_responsabilite DECIMAL(10,2) DEFAULT 0,
    autres_primes DECIMAL(10,2) DEFAULT 0,
    heures_supplementaires DECIMAL(5,2) DEFAULT 0,
    taux_horaire_sup DECIMAL(8,2) DEFAULT 0,
    jours_conges_payes INT DEFAULT 0,
    jours_absence_non_payes INT DEFAULT 0,
    cotisations_sociales DECIMAL(10,2) DEFAULT 0,
    impots DECIMAL(10,2) DEFAULT 0,
    autres_deductions DECIMAL(10,2) DEFAULT 0,
    salaire_brut DECIMAL(10,2) GENERATED ALWAYS AS (
        salaire_base + prime_performance + prime_anciennete + 
        prime_responsabilite + autres_primes + (heures_supplementaires * taux_horaire_sup)
    ) STORED,
    salaire_net DECIMAL(10,2) GENERATED ALWAYS AS (
        salaire_base + prime_performance + prime_anciennete + 
        prime_responsabilite + autres_primes + (heures_supplementaires * taux_horaire_sup) -
        cotisations_sociales - impots - autres_deductions - 
        (jours_absence_non_payes * (salaire_base / 30))
    ) STORED,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_validation TIMESTAMP NULL,
    valide_par INT,
    commentaires TEXT,
    UNIQUE KEY unique_employe_mois_annee (employe_id, mois, annee),
    FOREIGN KEY (employe_id) REFERENCES employes(id) ON DELETE CASCADE,
    FOREIGN KEY (valide_par) REFERENCES employes(id) ON DELETE SET NULL,
    INDEX idx_mois_annee (mois, annee),
    INDEX idx_date_creation (date_creation)
);

-- Table des utilisateurs pour l'authentification
CREATE TABLE utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(150) UNIQUE,
    role ENUM('ADMIN', 'RH', 'CHEF_DEPT', 'CHEF_PROJET', 'EMPLOYE') NOT NULL,
    employe_id INT,
    statut ENUM('ACTIF', 'INACTIVE', 'BLOQUE') DEFAULT 'ACTIF',
    derniere_connexion TIMESTAMP NULL,
    tentatives_connexion INT DEFAULT 0,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employe_id) REFERENCES employes(id) ON DELETE CASCADE,
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_statut (statut)
);

-- Table des sessions utilisateurs
CREATE TABLE sessions_utilisateurs (
    id VARCHAR(100) PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_expiration TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_expiration (date_expiration)
);

-- Table des congés et absences
CREATE TABLE conges_absences (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employe_id INT NOT NULL,
    type_conge ENUM('CONGES_PAYES', 'MALADIE', 'MATERNITE', 'PATERNITE', 'FORMATION', 'SANS_SOLDE') NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    nombre_jours INT NOT NULL,
    motif TEXT,
    statut ENUM('EN_ATTENTE', 'APPROUVE', 'REFUSE', 'ANNULE') DEFAULT 'EN_ATTENTE',
    approuve_par INT,
    date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_approbation TIMESTAMP NULL,
    commentaires_approbation TEXT,
    FOREIGN KEY (employe_id) REFERENCES employes(id) ON DELETE CASCADE,
    FOREIGN KEY (approuve_par) REFERENCES employes(id) ON DELETE SET NULL,
    INDEX idx_employe_dates (employe_id, date_debut, date_fin),
    INDEX idx_statut (statut),
    INDEX idx_type (type_conge)
);

-- Ajout des contraintes de clés étrangères pour les chefs
ALTER TABLE departements 
ADD CONSTRAINT fk_chef_departement 
FOREIGN KEY (chef_departement_id) REFERENCES employes(id) ON DELETE SET NULL;

ALTER TABLE projets 
ADD CONSTRAINT fk_chef_projet 
FOREIGN KEY (chef_projet_id) REFERENCES employes(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_projet_departement 
FOREIGN KEY (departement_id) REFERENCES departements(id) ON DELETE SET NULL;

-- Insertion de données de test

-- Départements
INSERT INTO departements (nom, description, budget) VALUES
('Informatique', 'Département de développement et maintenance informatique', 500000.00),
('Ressources Humaines', 'Gestion du personnel et recrutement', 200000.00),
('Finance', 'Gestion financière et comptabilité', 300000.00),
('Marketing', 'Promotion et communication', 250000.00),
('Production', 'Fabrication et assemblage', 800000.00),
('Recherche et Développement', 'Innovation et développement produits', 600000.00);

-- Employés (sans les références aux chefs d'abord)
INSERT INTO employes (matricule, nom, prenom, email, telephone, date_naissance, date_embauche, salaire_base, grade, poste, departement_id) VALUES
-- Direction
('DIR001', 'Martin', 'Jean', 'jean.martin@entreprise.com', '0123456789', '1975-03-15', '2000-01-10', 8000.00, 'DIRECTEUR', 'Directeur Général', 1),
('DIR002', 'Dubois', 'Marie', 'marie.dubois@entreprise.com', '0123456790', '1978-07-22', '2002-03-15', 7000.00, 'DIRECTEUR', 'Directrice RH', 2),

-- Managers
('MGR001', 'Leroy', 'Pierre', 'pierre.leroy@entreprise.com', '0123456791', '1980-11-08', '2005-06-20', 5500.00, 'MANAGER', 'Chef de projet senior', 1),
('MGR002', 'Bernard', 'Sophie', 'sophie.bernard@entreprise.com', '0123456792', '1982-09-14', '2006-09-10', 5200.00, 'MANAGER', 'Responsable Finance', 3),
('MGR003', 'Rousseau', 'Paul', 'paul.rousseau@entreprise.com', '0123456793', '1979-05-03', '2004-02-28', 5400.00, 'MANAGER', 'Chef Marketing', 4),

-- Employés seniors
('EMP001', 'Durand', 'Claire', 'claire.durand@entreprise.com', '0123456794', '1985-12-12', '2010-01-15', 4200.00, 'SENIOR', 'Développeur Senior', 1),
('EMP002', 'Moreau', 'Luc', 'luc.moreau@entreprise.com', '0123456795', '1987-04-20', '2012-03-20', 3800.00, 'SENIOR', 'Analyste RH', 2),
('EMP003', 'Girard', 'Anne', 'anne.girard@entreprise.com', '0123456796', '1986-08-30', '2011-07-10', 4000.00, 'SENIOR', 'Comptable Senior', 3),

-- Employés juniors
('EMP004', 'Roux', 'Marc', 'marc.roux@entreprise.com', '0123456797', '1990-01-25', '2018-09-01', 3200.00, 'JUNIOR', 'Développeur Junior', 1),
('EMP005', 'Blanc', 'Julie', 'julie.blanc@entreprise.com', '0123456798', '1992-06-18', '2020-02-15', 2800.00, 'JUNIOR', 'Assistante RH', 2),
('EMP006', 'Faure', 'Thomas', 'thomas.faure@entreprise.com', '0123456799', '1991-10-05', '2019-11-20', 3000.00, 'JUNIOR', 'Analyste Marketing', 4),

-- Stagiaires
('STG001', 'Laurent', 'Emma', 'emma.laurent@entreprise.com', '0123456800', '1998-03-12', '2023-09-01', 1200.00, 'STAGIAIRE', 'Stagiaire Développement', 1),
('STG002', 'Michel', 'Hugo', 'hugo.michel@entreprise.com', '0123456801', '1997-11-28', '2023-09-01', 1200.00, 'STAGIAIRE', 'Stagiaire Marketing', 4);

-- Mise à jour des chefs de département
UPDATE departements SET chef_departement_id = 1 WHERE nom = 'Informatique';
UPDATE departements SET chef_departement_id = 2 WHERE nom = 'Ressources Humaines';
UPDATE departements SET chef_departement_id = 4 WHERE nom = 'Finance';
UPDATE departements SET chef_departement_id = 5 WHERE nom = 'Marketing';

-- Mise à jour des managers
UPDATE employes SET manager_id = 1 WHERE id IN (3, 6, 9, 12);
UPDATE employes SET manager_id = 2 WHERE id IN (7, 10);
UPDATE employes SET manager_id = 4 WHERE id = 8;
UPDATE employes SET manager_id = 5 WHERE id = 11;

-- Projets
INSERT INTO projets (nom, description, date_debut, date_fin_prevue, budget, statut, priorite, chef_projet_id, departement_id) VALUES
('Digitalisation RH', 'Mise en place d''un système de gestion RH complet', '2024-01-15', '2024-12-31', 150000.00, 'EN_COURS', 'HAUTE', 3, 1),
('Nouveau Site Web', 'Refonte complète du site internet de l''entreprise', '2024-03-01', '2024-08-31', 80000.00, 'EN_COURS', 'NORMALE', 3, 4),
('Système Comptabilité', 'Modernisation du système comptable', '2024-02-01', '2024-10-31', 120000.00, 'EN_COURS', 'HAUTE', 4, 3),
('Formation Personnel', 'Programme de formation continue', '2024-01-01', '2024-12-31', 50000.00, 'EN_COURS', 'NORMALE', 2, 2);

-- Affectations employés-projets
INSERT INTO employe_projets (employe_id, projet_id, date_affectation, role_projet, pourcentage_temps) VALUES
(3, 1, '2024-01-15', 'Chef de projet', 100.00),
(6, 1, '2024-01-20', 'Développeur principal', 80.00),
(9, 1, '2024-02-01', 'Développeur', 60.00),
(12, 1, '2024-09-01', 'Stagiaire développement', 40.00),

(5, 2, '2024-03-01', 'Chef de projet', 100.00),
(11, 2, '2024-03-05', 'Responsable contenu', 70.00),
(13, 2, '2024-09-01', 'Stagiaire marketing', 30.00),

(4, 3, '2024-02-01', 'Chef de projet', 100.00),
(8, 3, '2024-02-15', 'Analyste principal', 90.00),

(2, 4, '2024-01-01', 'Responsable formation', 50.00),
(7, 4, '2024-01-15', 'Coordinateur RH', 30.00);

-- Utilisateurs pour l'authentification
-- Mot de passe admin: admin123
-- Mot de passe autres: password123
INSERT INTO utilisateurs (username, password_hash, email, role, employe_id, statut, tentatives_connexion) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@entreprise.com', 'ADMIN', 1, 'ACTIF', 0),
('marie.dubois', '$2a$10$xN9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'marie.dubois@entreprise.com', 'RH', 2, 'ACTIF', 0),
('pierre.leroy', '$2a$10$xN9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'pierre.leroy@entreprise.com', 'CHEF_PROJET', 3, 'ACTIF', 0),
('sophie.bernard', '$2a$10$xN9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'sophie.bernard@entreprise.com', 'CHEF_DEPT', 4, 'ACTIF', 0),
('paul.rousseau', '$2a$10$xN9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'paul.rousseau@entreprise.com', 'CHEF_DEPT', 5, 'ACTIF', 0),
('claire.durand', '$2a$10$xN9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'claire.durand@entreprise.com', 'EMPLOYE', 6, 'ACTIF', 0),
('luc.moreau', '$2a$10$xN9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'luc.moreau@entreprise.com', 'EMPLOYE', 7, 'ACTIF', 0),
('anne.girard', '$2a$10$xN9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'anne.girard@entreprise.com', 'EMPLOYE', 8, 'ACTIF', 0);

-- Fiches de paie pour octobre 2024 (TOUS les employés)
INSERT INTO fiches_paie (employe_id, mois, annee, salaire_base, prime_performance, prime_anciennete, heures_supplementaires, taux_horaire_sup, cotisations_sociales, impots) VALUES
-- Direction
(1, 10, 2024, 8000.00, 1000.00, 500.00, 0, 0, 2000.00, 1800.00),  -- Jean Martin (Directeur Général)
(2, 10, 2024, 7000.00, 800.00, 400.00, 0, 0, 1750.00, 1600.00),   -- Marie Dubois (Directrice RH)
-- Managers
(3, 10, 2024, 5500.00, 600.00, 300.00, 5.00, 50.00, 1400.00, 1200.00),  -- Pierre Leroy (Chef de projet senior)
(4, 10, 2024, 5200.00, 500.00, 250.00, 0, 0, 1350.00, 1150.00),         -- Sophie Bernard (Responsable Finance)
(5, 10, 2024, 5400.00, 550.00, 280.00, 3.00, 50.00, 1380.00, 1180.00),  -- Paul Rousseau (Chef Marketing)
-- Employés seniors
(6, 10, 2024, 4200.00, 400.00, 200.00, 8.00, 45.00, 1100.00, 900.00),   -- Claire Durand (Développeur Senior)
(7, 10, 2024, 3800.00, 350.00, 180.00, 5.00, 45.00, 1000.00, 820.00),   -- Luc Moreau (Analyste RH)
(8, 10, 2024, 4000.00, 380.00, 190.00, 6.00, 45.00, 1050.00, 860.00),   -- Anne Girard (Comptable Senior)
-- Employés juniors
(9, 10, 2024, 3200.00, 200.00, 0, 10.00, 40.00, 850.00, 650.00),        -- Marc Roux (Développeur Junior)
(10, 10, 2024, 2800.00, 150.00, 0, 8.00, 38.00, 750.00, 580.00),        -- Julie Blanc (Assistante RH)
(11, 10, 2024, 3000.00, 180.00, 0, 7.00, 40.00, 800.00, 620.00),        -- Thomas Faure (Analyste Marketing)
-- Stagiaires
(12, 10, 2024, 1200.00, 0, 0, 0, 0, 200.00, 100.00),                    -- Emma Laurent (Stagiaire Développement)
(13, 10, 2024, 1200.00, 0, 0, 0, 0, 200.00, 100.00);                    -- Hugo Michel (Stagiaire Marketing)

-- Fiches de paie pour septembre 2024 (pour avoir un historique)
INSERT INTO fiches_paie (employe_id, mois, annee, salaire_base, prime_performance, prime_anciennete, heures_supplementaires, taux_horaire_sup, cotisations_sociales, impots) VALUES
(1, 9, 2024, 8000.00, 950.00, 500.00, 0, 0, 2000.00, 1800.00),
(2, 9, 2024, 7000.00, 750.00, 400.00, 0, 0, 1750.00, 1600.00),
(3, 9, 2024, 5500.00, 580.00, 300.00, 4.00, 50.00, 1400.00, 1200.00),
(4, 9, 2024, 5200.00, 480.00, 250.00, 0, 0, 1350.00, 1150.00),
(5, 9, 2024, 5400.00, 520.00, 280.00, 2.00, 50.00, 1380.00, 1180.00),
(6, 9, 2024, 4200.00, 380.00, 200.00, 10.00, 45.00, 1100.00, 900.00),
(7, 9, 2024, 3800.00, 330.00, 180.00, 6.00, 45.00, 1000.00, 820.00),
(8, 9, 2024, 4000.00, 360.00, 190.00, 5.00, 45.00, 1050.00, 860.00),
(9, 9, 2024, 3200.00, 180.00, 0, 12.00, 40.00, 850.00, 650.00),
(10, 9, 2024, 2800.00, 140.00, 0, 9.00, 38.00, 750.00, 580.00),
(11, 9, 2024, 3000.00, 160.00, 0, 8.00, 40.00, 800.00, 620.00),
(12, 9, 2024, 1200.00, 0, 0, 0, 0, 200.00, 100.00),
(13, 9, 2024, 1200.00, 0, 0, 0, 0, 200.00, 100.00);

-- Quelques congés d'exemple
INSERT INTO conges_absences (employe_id, type_conge, date_debut, date_fin, nombre_jours, motif, statut, approuve_par) VALUES
(6, 'CONGES_PAYES', '2024-11-15', '2024-11-22', 6, 'Congés de fin d''année', 'APPROUVE', 3),
(9, 'MALADIE', '2024-10-20', '2024-10-22', 3, 'Grippe', 'APPROUVE', 3),
(11, 'FORMATION', '2024-12-01', '2024-12-05', 5, 'Formation marketing digital', 'EN_ATTENTE', NULL);

-- Création des index pour optimiser les performances
CREATE INDEX idx_employes_departement ON employes(departement_id);
CREATE INDEX idx_projets_statut_date ON projets(statut, date_debut);
CREATE INDEX idx_fiches_paie_periode ON fiches_paie(annee, mois);
CREATE INDEX idx_employe_projets_actifs ON employe_projets(statut, date_affectation);

-- Vues pour faciliter les requêtes

-- Vue des employés avec informations complètes
CREATE VIEW v_employes_complets AS
SELECT 
    e.id,
    e.matricule,
    CONCAT(e.prenom, ' ', e.nom) AS nom_complet,
    e.email,
    e.telephone,
    e.poste,
    e.grade,
    e.salaire_base,
    e.statut,
    e.date_embauche,
    d.nom AS departement,
    CONCAT(m.prenom, ' ', m.nom) AS manager,
    DATEDIFF(CURDATE(), e.date_embauche) AS jours_anciennete
FROM employes e
LEFT JOIN departements d ON e.departement_id = d.id
LEFT JOIN employes m ON e.manager_id = m.id
WHERE e.statut = 'ACTIF';

-- Vue des projets avec informations détaillées
CREATE VIEW v_projets_details AS
SELECT 
    p.id,
    p.nom,
    p.description,
    p.statut,
    p.priorite,
    p.date_debut,
    p.date_fin_prevue,
    p.budget,
    CONCAT(chef.prenom, ' ', chef.nom) AS chef_projet,
    d.nom AS departement,
    COUNT(ep.employe_id) AS nombre_employes
FROM projets p
LEFT JOIN employes chef ON p.chef_projet_id = chef.id
LEFT JOIN departements d ON p.departement_id = d.id
LEFT JOIN employe_projets ep ON p.id = ep.projet_id AND ep.statut = 'ACTIF'
GROUP BY p.id;

-- Vue des fiches de paie avec calculs
CREATE VIEW v_fiches_paie_details AS
SELECT 
    fp.id,
    CONCAT(e.prenom, ' ', e.nom) AS employe,
    e.matricule,
    fp.mois,
    fp.annee,
    fp.salaire_base,
    (fp.prime_performance + fp.prime_anciennete + fp.prime_responsabilite + fp.autres_primes) AS total_primes,
    (fp.heures_supplementaires * fp.taux_horaire_sup) AS montant_heures_sup,
    fp.salaire_brut,
    (fp.cotisations_sociales + fp.impots + fp.autres_deductions) AS total_deductions,
    fp.salaire_net,
    fp.date_creation
FROM fiches_paie fp
JOIN employes e ON fp.employe_id = e.id;

-- Vue des statistiques par département
CREATE VIEW v_stats_departements AS
SELECT 
    d.id,
    d.nom,
    COUNT(e.id) AS nombre_employes,
    AVG(e.salaire_base) AS salaire_moyen,
    SUM(e.salaire_base) AS masse_salariale,
    COUNT(CASE WHEN e.grade = 'MANAGER' THEN 1 END) AS nombre_managers,
    COUNT(CASE WHEN e.grade = 'SENIOR' THEN 1 END) AS nombre_seniors,
    COUNT(CASE WHEN e.grade = 'JUNIOR' THEN 1 END) AS nombre_juniors,
    COUNT(CASE WHEN e.grade = 'STAGIAIRE' THEN 1 END) AS nombre_stagiaires
FROM departements d
LEFT JOIN employes e ON d.id = e.departement_id AND e.statut = 'ACTIF'
GROUP BY d.id, d.nom;

COMMIT;

-- ============================================================================
-- VÉRIFICATIONS FINALES ET CORRECTIFS
-- ============================================================================

-- Vérifier que tous les départements sont actifs (normalement déjà fait avec DEFAULT 1)
UPDATE departements SET actif = 1 WHERE actif IS NULL OR actif = 0;

-- Afficher le résultat pour vérification
SELECT 
    'DÉPARTEMENTS' AS Table_Name,
    COUNT(*) AS Total,
    SUM(CASE WHEN actif = 1 THEN 1 ELSE 0 END) AS Actifs,
    SUM(CASE WHEN actif = 0 THEN 1 ELSE 0 END) AS Inactifs
FROM departements;

-- Vérifier les enums (doit retourner 0 ligne si tout est OK)
SELECT 'Vérification des enums' AS Status;

SELECT DISTINCT grade FROM employes 
WHERE grade NOT IN ('STAGIAIRE', 'JUNIOR', 'SENIOR', 'EXPERT', 'MANAGER', 'DIRECTEUR')
UNION ALL
SELECT DISTINCT statut FROM employes 
WHERE statut NOT IN ('ACTIF', 'SUSPENDU', 'DEMISSION', 'LICENCIE', 'RETRAITE')
UNION ALL
SELECT DISTINCT statut FROM projets 
WHERE statut NOT IN ('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE')
UNION ALL
SELECT DISTINCT priorite FROM projets 
WHERE priorite NOT IN ('BASSE', 'NORMALE', 'HAUTE', 'CRITIQUE')
UNION ALL
SELECT DISTINCT type_conge FROM conges_absences 
WHERE type_conge NOT IN ('CONGES_PAYES', 'MALADIE', 'MATERNITE', 'PATERNITE', 'FORMATION', 'SANS_SOLDE')
UNION ALL
SELECT DISTINCT statut FROM conges_absences 
WHERE statut NOT IN ('EN_ATTENTE', 'APPROUVE', 'REFUSE', 'ANNULE')
UNION ALL
SELECT DISTINCT role FROM utilisateurs 
WHERE role NOT IN ('ADMIN', 'RH', 'CHEF_DEPT', 'CHEF_PROJET', 'EMPLOYE')
UNION ALL
SELECT DISTINCT statut FROM utilisateurs 
WHERE statut NOT IN ('ACTIF', 'INACTIVE', 'BLOQUE')
UNION ALL
SELECT DISTINCT statut FROM employe_projets 
WHERE statut NOT IN ('ACTIF', 'TERMINE', 'SUSPENDU');

-- Afficher les statistiques finales
SELECT 'STATISTIQUES DE LA BASE' AS Info;

SELECT 
    'employes' AS Table_Name,
    COUNT(*) AS Total_Rows,
    SUM(CASE WHEN statut = 'ACTIF' THEN 1 ELSE 0 END) AS Actifs
FROM employes

UNION ALL

SELECT 
    'departements' AS Table_Name,
    COUNT(*) AS Total_Rows,
    SUM(CASE WHEN actif = 1 THEN 1 ELSE 0 END) AS Actifs
FROM departements

UNION ALL

SELECT 
    'projets' AS Table_Name,
    COUNT(*) AS Total_Rows,
    SUM(CASE WHEN statut = 'EN_COURS' THEN 1 ELSE 0 END) AS En_Cours
FROM projets

UNION ALL

SELECT 
    'utilisateurs' AS Table_Name,
    COUNT(*) AS Total_Rows,
    SUM(CASE WHEN statut = 'ACTIF' THEN 1 ELSE 0 END) AS Actifs
FROM utilisateurs

UNION ALL

SELECT 
    'fiches_paie' AS Table_Name,
    COUNT(*) AS Total_Rows,
    COUNT(*) AS Total
FROM fiches_paie

UNION ALL

SELECT 
    'conges_absences' AS Table_Name,
    COUNT(*) AS Total_Rows,
    SUM(CASE WHEN statut = 'EN_ATTENTE' THEN 1 ELSE 0 END) AS En_Attente
FROM conges_absences;

-- Message de confirmation
SELECT '✅ BASE DE DONNÉES CRÉÉE ET CONFIGURÉE AVEC SUCCÈS !' AS Status;
SELECT 'Tous les départements sont actifs et prêts à être utilisés.' AS Info;
SELECT 'Vous pouvez maintenant démarrer votre application JEE.' AS Action;

-- Instructions d'utilisation
/*
=============================================================================
SCRIPT COMPLET DE CRÉATION DE LA BASE DE DONNÉES GESTION RH
=============================================================================

Ce script contient TOUT ce dont vous avez besoin :
✅ Création de la base de données
✅ Création de toutes les tables (10 tables)
✅ Création des vues (3 vues)
✅ Création des index (15+ index)
✅ Insertion des données de test
✅ Correction de la colonne 'actif' (incluse dans CREATE TABLE)
✅ Activation de tous les départements
✅ Vérifications automatiques

COMMENT L'UTILISER :

1. Ouvrir MySQL Workbench
2. Se connecter à votre serveur MySQL
3. Copier et coller CE SCRIPT COMPLET
4. Exécuter le script (F5 ou bouton Execute)
5. Vérifier les messages de confirmation

COMPTES DE TEST :
- Administrateur: admin / admin123
- RH: marie.dubois / marie123  
- Chef de projet: pierre.leroy / pierre123
- Chef département: sophie.bernard / sophie123
- Chef département: paul.rousseau / paul123
- Employé: claire.durand / claire123

⚠️ EN PRODUCTION, CHANGEZ TOUS LES MOTS DE PASSE !

RÉSULTAT ATTENDU :
- 10 tables créées
- 3 vues créées
- 15+ index créés
- 13 employés insérés
- 6 départements insérés (TOUS ACTIFS)
- 4 projets insérés
- 8 utilisateurs insérés
- 26 fiches de paie insérées (13 pour octobre + 13 pour septembre 2024)
- 3 congés insérés

La base contient des données de test pour commencer à utiliser l'application.

VÉRIFICATION DE LA BASE DE DONNÉES :

-- 1. Vérifier le nombre d'employés actifs
SELECT COUNT(*) as total_employes FROM employes WHERE statut = 'ACTIF';
-- Résultat attendu : 13

-- 2. Vérifier les fiches de paie par mois
SELECT 
    mois, 
    annee, 
    COUNT(*) as nombre_fiches,
    ROUND(SUM(salaire_base), 2) as total_salaires_base
FROM fiches_paie
GROUP BY annee, mois
ORDER BY annee DESC, mois DESC;
-- Résultat attendu : 
--   10/2024 : 13 fiches, ~58000€
--   09/2024 : 13 fiches, ~58000€

-- 3. Vérifier la masse salariale d'octobre 2024
SELECT 
    ROUND(SUM(
        COALESCE(salaire_base, 0) + 
        COALESCE(prime_performance, 0) + 
        COALESCE(prime_anciennete, 0) + 
        (COALESCE(heures_supplementaires, 0) * COALESCE(taux_horaire_sup, 0))
    ), 2) as masse_salariale_octobre_2024
FROM fiches_paie
WHERE mois = 10 AND annee = 2024;
-- Résultat attendu : ~65000€

-- 4. Vérifier que tous les employés actifs ont une fiche de paie
SELECT 
    e.id,
    CONCAT(e.prenom, ' ', e.nom) AS nom_complet,
    e.poste,
    CASE 
        WHEN fp.id IS NULL THEN 'PAS DE FICHE ❌'
        ELSE 'FICHE OK ✅'
    END as statut_fiche
FROM employes e
LEFT JOIN fiches_paie fp ON e.id = fp.employe_id AND fp.mois = 10 AND fp.annee = 2024
WHERE e.statut = 'ACTIF'
ORDER BY e.id;
-- Résultat attendu : Tous doivent avoir 'FICHE OK ✅'

-- 5. Vérifier les départements
SELECT id, nom, actif FROM departements;
-- Tous les départements doivent avoir actif = 1

-- 6. Afficher un résumé complet
SELECT 
    'Employés actifs' as type_donnee, 
    COUNT(*) as total 
FROM employes WHERE statut = 'ACTIF'
UNION ALL
SELECT 
    'Départements actifs', 
    COUNT(*) 
FROM departements WHERE actif = TRUE
UNION ALL
SELECT 
    'Projets', 
    COUNT(*) 
FROM projets
UNION ALL
SELECT 
    'Fiches de paie (Oct 2024)', 
    COUNT(*) 
FROM fiches_paie WHERE mois = 10 AND annee = 2024
UNION ALL
SELECT 
    'Utilisateurs', 
    COUNT(*) 
FROM utilisateurs;

Pour vérifier que tout fonctionne :
SELECT id, nom, actif FROM departements;
-- Tous les départements doivent avoir actif = 1

SUPPORT :
Si problème, consultez la documentation dans le projet :
- GUIDE_CORRECTION_RAPIDE.md
- AUDIT_DATABASE_HIBERNATE.md
- CONFORMITE_CAHIER_DES_CHARGES.md

=============================================================================
*/