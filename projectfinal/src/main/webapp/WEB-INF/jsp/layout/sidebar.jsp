<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- Sidebar de navigation -->
<nav class="col-md-3 col-lg-2 d-md-block bg-light sidebar collapse" style="position: fixed; top: 56px; bottom: 0; left: 0; z-index: 100; padding: 0; box-shadow: 0 .125rem .25rem rgba(0,0,0,.075);">
    <div class="position-sticky pt-3">
        <!-- Informations utilisateur -->
        <div class="text-center pb-3 border-bottom">
            <i class="fas fa-user-circle fa-3x text-primary mb-2"></i>
            <h6 class="mb-0">${utilisateur.username}</h6>
            <small class="text-muted">${utilisateur.role.libelle}</small>
        </div>
        
        <!-- Menu principal -->
        <ul class="nav flex-column px-3 pt-3">
            <li class="nav-item">
                <a class="nav-link ${currentPage == 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/dashboard">
                    <i class="fas fa-tachometer-alt me-2"></i>Tableau de bord
                </a>
            </li>
            
            <li class="nav-item">
                <a class="nav-link ${currentPage == 'employes' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/employes">
                    <i class="fas fa-users me-2"></i>Employés
                </a>
            </li>
            
            <li class="nav-item">
                <a class="nav-link ${currentPage == 'departements' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/departements">
                    <i class="fas fa-building me-2"></i>Départements
                </a>
            </li>
            
            <li class="nav-item">
                <a class="nav-link ${currentPage == 'projets' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/projets">
                    <i class="fas fa-project-diagram me-2"></i>Projets
                </a>
            </li>
            
            <li class="nav-item">
                <a class="nav-link ${currentPage == 'fiches-paie' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/fiches-paie">
                    <i class="fas fa-file-invoice-dollar me-2"></i>Fiches de paie
                </a>
            </li>
            
            <li class="nav-item">
                <a class="nav-link ${currentPage == 'conges' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/conges-absences">
                    <i class="fas fa-calendar-check me-2"></i>Congés
                </a>
            </li>
            
            <hr class="my-3">
            
            <li class="nav-item">
                <a class="nav-link" href="${pageContext.request.contextPath}/logout">
                    <i class="fas fa-sign-out-alt me-2"></i>Déconnexion
                </a>
            </li>
        </ul>
    </div>
</nav>