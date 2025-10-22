<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Système de Gestion RH - Accueil</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header bg-primary text-white text-center">
                        <h2><i class="bi bi-people-fill"></i> Système de Gestion RH</h2>
                    </div>
                    <div class="card-body text-center">
                        <h4 class="mb-4">Bienvenue dans l'application de Gestion RH</h4>
                        <p class="text-muted mb-4">
                            Plateforme complète pour la gestion des ressources humaines, 
                            départements, projets et fiches de paie.
                        </p>
                        
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <div class="card border-success">
                                    <div class="card-body">
                                        <i class="bi bi-box-arrow-in-right fs-1 text-success"></i>
                                        <h5 class="mt-2">Connexion</h5>
                                        <p class="small text-muted">Accéder à votre espace de travail</p>
                                        <a href="<%= request.getContextPath() %>/login" class="btn btn-success">
                                            <i class="bi bi-arrow-right"></i> Se Connecter
                                        </a>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <div class="card border-info">
                                    <div class="card-body">
                                        <i class="bi bi-gear fs-1 text-info"></i>
                                        <h5 class="mt-2">Test</h5>
                                        <p class="small text-muted">Vérifier la configuration</p>
                                        <a href="<%= request.getContextPath() %>/simple-test.jsp" class="btn btn-info">
                                            <i class="bi bi-tools"></i> Test App
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="alert alert-info mt-4">
                            <h6><i class="bi bi-info-circle"></i> Fonctionnalités disponibles</h6>
                            <div class="row text-start">
                                <div class="col-md-6">
                                    <ul class="list-unstyled">
                                        <li><i class="bi bi-check"></i> Gestion des Employés</li>
                                        <li><i class="bi bi-check"></i> Gestion des Départements</li>
                                        <li><i class="bi bi-check"></i> Gestion des Projets</li>
                                    </ul>
                                </div>
                                <div class="col-md-6">
                                    <ul class="list-unstyled">
                                        <li><i class="bi bi-check"></i> Fiches de Paie</li>
                                        <li><i class="bi bi-check"></i> Congés et Absences</li>
                                        <li><i class="bi bi-check"></i> Rapports et Statistiques</li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="card-footer text-center text-muted">
                        <small>
                            Context Path: <%= request.getContextPath() %> | 
                            Server: <%= application.getServerInfo() %> |
                            Date: <%= new java.util.Date() %>
                        </small>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>