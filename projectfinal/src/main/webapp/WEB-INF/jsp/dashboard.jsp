<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="layout/header.jsp">
    <jsp:param name="title" value="Tableau de bord - Système de Gestion RH" />
    <jsp:param name="page" value="dashboard" />
</jsp:include>


<div class="header">
    <h1>Tableau de bord</h1>
    <p class="subtitle">Vue d'ensemble de votre système de gestion des ressources humaines</p>
</div>

<!-- Statistiques -->
<div class="stats-grid">
    <div class="stat-card">
        <i class="fas fa-users"></i>
        <h4>${stats.nbEmployes != null ? stats.nbEmployes : '150'}</h4>
        <p>Employés</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-building"></i>
        <h4>${stats.nbDepartements != null ? stats.nbDepartements : '8'}</h4>
        <p>Départements</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-project-diagram"></i>
        <h4>${stats.nbProjetsActifs != null ? stats.nbProjetsActifs : '25'}</h4>
        <p>Projets actifs</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-calendar-check"></i>
        <h4>${stats.nbCongesEnAttente != null ? stats.nbCongesEnAttente : '12'}</h4>
        <p>Congés en attente</p>
    </div>
</div>

<!-- Contenu principal -->
<div style="display: grid; grid-template-columns: 2fr 1fr; gap: 24px;">
    <!-- Notifications -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-bell" style="margin-right: 12px;"></i>Notifications récentes
        </div>
        
        <c:choose>
            <c:when test="${not empty notifications}">
                <c:forEach var="notif" items="${notifications}" varStatus="status">
                    <div style="padding: 20px 0; ${status.last ? '' : 'border-bottom: 1px solid #F5F5F5;'}">
                        <h6 style="font-size: 15px; font-weight: 600; color: #1A1A1A; margin-bottom: 6px;">
                            ${notif.titre}
                        </h6>
                        <p style="font-size: 14px; color: #666666; margin-bottom: 6px; font-weight: 300;">
                            ${notif.message}
                        </p>
                        <small style="font-size: 12px; color: #999999; text-transform: uppercase; letter-spacing: 0.5px;">
                            ${notif.dateRelative}
                        </small>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div style="padding: 20px 0; border-bottom: 1px solid #F5F5F5;">
                    <h6 style="font-size: 15px; font-weight: 600; color: #1A1A1A; margin-bottom: 6px;">
                        Nouvelle demande de congé
                    </h6>
                    <p style="font-size: 14px; color: #666666; margin-bottom: 6px; font-weight: 300;">
                        Claire Durand a demandé des congés du 15 au 22 novembre
                    </p>
                    <small style="font-size: 12px; color: #999999; text-transform: uppercase; letter-spacing: 0.5px;">
                        Il y a 2h
                    </small>
                </div>
                
                <div style="padding: 20px 0; border-bottom: 1px solid #F5F5F5;">
                    <h6 style="font-size: 15px; font-weight: 600; color: #1A1A1A; margin-bottom: 6px;">
                        Projet terminé
                    </h6>
                    <p style="font-size: 14px; color: #666666; margin-bottom: 6px; font-weight: 300;">
                        Le projet "Nouveau Site Web" a été marqué comme terminé
                    </p>
                    <small style="font-size: 12px; color: #999999; text-transform: uppercase; letter-spacing: 0.5px;">
                        Hier
                    </small>
                </div>
                
                <div style="padding: 20px 0;">
                    <h6 style="font-size: 15px; font-weight: 600; color: #1A1A1A; margin-bottom: 6px;">
                        Nouvel employé
                    </h6>
                    <p style="font-size: 14px; color: #666666; margin-bottom: 6px; font-weight: 300;">
                        Thomas Martin a rejoint le département IT
                    </p>
                    <small style="font-size: 12px; color: #999999; text-transform: uppercase; letter-spacing: 0.5px;">
                        Il y a 3 jours
                    </small>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    
    <!-- Sidebar Actions -->
    <div>
        <div class="card">
            <div class="card-header">
                <i class="fas fa-lightning-bolt" style="margin-right: 12px;"></i>Actions rapides
            </div>
            
            <div style="display: flex; flex-direction: column; gap: 12px;">
                <c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
                    <a href="${pageContext.request.contextPath}/app/employes?action=add" class="btn btn-primary">
                        <i class="fas fa-user-plus" style="margin-right: 8px;"></i>Ajouter un employé
                    </a>
                </c:if>
                <c:if test="${utilisateur.role ne 'EMPLOYE' and utilisateur.role ne 'CHEF_PROJET'}">
	                <a href="${pageContext.request.contextPath}/app/projets?action=add" class="btn btn-secondary">
	                    <i class="fas fa-plus" style="margin-right: 8px;"></i>Créer un projet
	                </a>
                </c:if>
                <a href="${pageContext.request.contextPath}/app/conges-absences?action=new" class="btn btn-secondary">
                    <i class="fas fa-calendar-plus" style="margin-right: 8px;"></i>Demander des congés
                </a>
                
                <c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
                    <a href="${pageContext.request.contextPath}/app/statistiques" class="btn btn-outline">
                        <i class="fas fa-chart-line" style="margin-right: 8px;"></i>Voir les statistiques
                    </a>
                </c:if>
            </div>
        </div>
        
        <!-- Informations -->
        <div class="card" style="margin-top: 24px;">
            <div class="card-header">
                <i class="fas fa-info-circle" style="margin-right: 12px;"></i>Informations système
            </div>
            
            <div style="font-size: 13px; line-height: 2;">
                <div style="display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #F5F5F5;">
                    <span style="color: #666666;">Version</span>
                    <span style="font-weight: 600;">1.0.0</span>
                </div>
                <div style="display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #F5F5F5;">
                    <span style="color: #666666;">Mise à jour</span>
                    <span style="font-weight: 600;">19/10/2025</span>
                </div>
                <div style="display: flex; justify-content: space-between; padding: 8px 0;">
                    <span style="color: #666666;">Serveur</span>
                    <span style="font-weight: 600;">Tomcat 10.1</span>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="layout/footer.jsp" />