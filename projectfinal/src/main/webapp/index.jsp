<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Système de Gestion RH</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .hero-section {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 100px 0;
        }
        .feature-card {
            transition: transform 0.3s;
            border: none;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }
        .feature-card:hover {
            transform: translateY(-5px);
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="#">
                <i class="fas fa-users me-2"></i>
                Gestion RH
            </a>
            <div class="navbar-nav ms-auto">
                <a class="nav-link" href="${pageContext.request.contextPath}/login">
                    <i class="fas fa-sign-in-alt me-1"></i>
                    Connexion
                </a>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero-section text-center">
        <div class="container">
            <div class="alert alert-success mb-4" role="alert">
                ✅ <strong>Application Active!</strong> Context: <%= request.getContextPath() %> | Date: <%= new java.util.Date() %>
            </div>
            <h1 class="display-4 fw-bold mb-4">Système de Gestion RH</h1>
            <p class="lead mb-5">Plateforme complète pour la gestion des ressources humaines, départements, projets et fiches de paie</p>
            <a href="${pageContext.request.contextPath}/login" class="btn btn-light btn-lg">
                <i class="fas fa-rocket me-2"></i>
                Commencer
            </a>
        </div>
    </section>

    <!-- Features Section -->
    <section class="py-5">
        <div class="container">
            <div class="row text-center mb-5">
                <div class="col-12">
                    <h2 class="fw-bold">Fonctionnalités Principales</h2>
                    <p class="text-muted">Découvrez toutes les fonctionnalités de notre système</p>
                </div>
            </div>
            
            <div class="row g-4">
                <div class="col-md-6 col-lg-3">
                    <div class="card feature-card h-100 text-center p-4">
                        <div class="card-body">
                            <i class="fas fa-users fa-3x text-primary mb-3"></i>
                            <h5 class="card-title">Gestion des Employés</h5>
                            <p class="card-text">Gérez facilement les profils, les postes et les affectations de vos employés.</p>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-6 col-lg-3">
                    <div class="card feature-card h-100 text-center p-4">
                        <div class="card-body">
                            <i class="fas fa-building fa-3x text-success mb-3"></i>
                            <h5 class="card-title">Départements</h5>
                            <p class="card-text">Organisez votre entreprise par départements et suivez leur performance.</p>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-6 col-lg-3">
                    <div class="card feature-card h-100 text-center p-4">
                        <div class="card-body">
                            <i class="fas fa-project-diagram fa-3x text-warning mb-3"></i>
                            <h5 class="card-title">Projets</h5>
                            <p class="card-text">Planifiez, assignez et suivez l'avancement de tous vos projets.</p>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-6 col-lg-3">
                    <div class="card feature-card h-100 text-center p-4">
                        <div class="card-body">
                            <i class="fas fa-file-invoice-dollar fa-3x text-info mb-3"></i>
                            <h5 class="card-title">Fiches de Paie</h5>
                            <p class="card-text">Automatisez le calcul et la génération des fiches de paie mensuelles.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Stats Section -->
    <section class="py-5 bg-light">
        <div class="container">
            <div class="row text-center">
                <div class="col-md-3">
                    <div class="mb-3">
                        <i class="fas fa-user-tie fa-2x text-primary"></i>
                    </div>
                    <h3 class="fw-bold">Employés</h3>
                    <p class="text-muted">Gestion complète des profils</p>
                </div>
                
                <div class="col-md-3">
                    <div class="mb-3">
                        <i class="fas fa-chart-line fa-2x text-success"></i>
                    </div>
                    <h3 class="fw-bold">Rapports</h3>
                    <p class="text-muted">Statistiques détaillées</p>
                </div>
                
                <div class="col-md-3">
                    <div class="mb-3">
                        <i class="fas fa-calendar-check fa-2x text-warning"></i>
                    </div>
                    <h3 class="fw-bold">Congés</h3>
                    <p class="text-muted">Suivi des absences</p>
                </div>
                
                <div class="col-md-3">
                    <div class="mb-3">
                        <i class="fas fa-shield-alt fa-2x text-info"></i>
                    </div>
                    <h3 class="fw-bold">Sécurité</h3>
                    <p class="text-muted">Accès sécurisé par rôles</p>
                </div>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer class="bg-dark text-white py-4">
        <div class="container">
            <div class="row">
                <div class="col-md-6">
                    <h5>Système de Gestion RH</h5>
                    <p class="mb-0">Solution complète pour la gestion des ressources humaines</p>
                </div>
                <div class="col-md-6 text-md-end">
                    <p class="mb-0">&copy; 2024 Gestion RH. Tous droits réservés.</p>
                </div>
            </div>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>