<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Gestion des Projets - Système RH" />
    <jsp:param name="page" value="projets" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">Projets</h1>
            <p class="subtitle">Gestion des projets et des équipes</p>
        </div>
        <a href="${pageContext.request.contextPath}/app/projets?action=add" class="btn btn-primary">
            <i class="fas fa-plus" style="margin-right: 8px;"></i>Nouveau projet
        </a>
    </div>
</div>

<c:if test="${not empty success}">
    <div class="alert alert-success" role="alert">
        <i class="fas fa-check-circle" style="margin-right: 8px;"></i>${success}
    </div>
</c:if>

<c:if test="${not empty error}">
    <div class="alert alert-danger" role="alert">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>${error}
    </div>
</c:if>

<!-- Filtres et Recherche -->
<div class="card" style="margin-bottom: 24px;">
    <div class="card-header">
        <i class="fas fa-filter" style="margin-right: 12px;"></i>Recherche et Filtres
    </div>
    <form method="GET" action="${pageContext.request.contextPath}/app/projets">
        <div style="padding: 24px;">
            <!-- Barre de recherche -->
            <div style="margin-bottom: 20px;">
                <label class="form-label" for="search">RECHERCHER UN PROJET</label>
                <input type="text" 
                       id="search" 
                       name="search" 
                       class="form-control" 
                       placeholder="Rechercher par nom de projet..."
                       value="${param.search}"
                       style="font-size: 14px;">
            </div>
            
            <!-- Filtres -->
            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;">
                <div>
                    <label class="form-label" for="statut">STATUT</label>
                    <select id="statut" name="statut" class="form-control">
                        <option value="">Tous les statuts</option>
                        <option value="PLANIFIE" ${param.statut == 'PLANIFIE' ? 'selected' : ''}>Planifié</option>
                        <option value="EN_COURS" ${param.statut == 'EN_COURS' ? 'selected' : ''}>En cours</option>
                        <option value="TERMINE" ${param.statut == 'TERMINE' ? 'selected' : ''}>Terminé</option>
                        <option value="ANNULE" ${param.statut == 'ANNULE' ? 'selected' : ''}>Annulé</option>
                    </select>
                </div>

                <div>
                    <label class="form-label" for="departementId">DÉPARTEMENT</label>
                    <select id="departementId" name="departementId" class="form-control">
                        <option value="">Tous les départements</option>
                        <c:forEach var="dept" items="${departements}">
                            <option value="${dept.id}" ${param.departementId == dept.id ? 'selected' : ''}>
                                ${dept.nom}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div style="display: flex; align-items: flex-end;">
                    <button type="submit" class="btn btn-primary" style="flex: 1; margin-right: 8px;">
                        <i class="fas fa-search" style="margin-right: 8px;"></i>Rechercher
                    </button>
                    <a href="${pageContext.request.contextPath}/app/projets" class="btn btn-secondary">
                        Réinitialiser
                    </a>
                </div>
            </div>
        </div>
    </form>
</div>

<!-- Liste des projets -->
<div class="card">
    <div class="card-header">
        <i class="fas fa-project-diagram" style="margin-right: 12px;"></i>Liste des projets
    </div>
    
    <c:choose>
        <c:when test="${not empty projets}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>NOM DU PROJET</th>
                        <th>RESPONSABLE</th>
                        <th>DATES</th>
                        <th style="text-align: center;">ÉQUIPE</th>
                        <th style="text-align: center;">STATUT</th>
                        <th style="text-align: center;">ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="projet" items="${projets}">
                        <tr>
                            <td>
                                <div style="font-weight: 600; color: #1A1A1A; margin-bottom: 4px;">
                                    ${projet.nom}
                                </div>
                                <c:if test="${not empty projet.description}">
                                    <div style="font-size: 12px; color: #666666; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                        ${projet.description}
                                    </div>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty projet.chefProjet}">
                                        <div style="display: flex; align-items: center; gap: 12px;">
                                            <div class="avatar">
                                                ${projet.chefProjet.prenom.substring(0,1)}${projet.chefProjet.nom.substring(0,1)}
                                            </div>
                                            <div>
                                                <div style="font-weight: 500; color: #1A1A1A;">
                                                    ${projet.chefProjet.prenom} ${projet.chefProjet.nom}
                                                </div>
                                                <div style="font-size: 12px; color: #666666;">
                                                    ${projet.chefProjet.poste}
                                                </div>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: #999999;">Non assigné</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div style="font-size: 13px; color: #1A1A1A;">
                                    <c:if test="${not empty projet.dateDebut}">
                                        <div style="margin-bottom: 4px;">
                                            <i class="fas fa-play-circle" style="color: #C5A572; margin-right: 6px; font-size: 11px;"></i>
                                            ${projet.dateDebut}
                                        </div>
                                    </c:if>
                                    <c:if test="${not empty projet.dateFinPrevue}">
                                        <div>
                                            <i class="fas fa-flag-checkered" style="color: #C5A572; margin-right: 6px; font-size: 11px;"></i>
                                            ${projet.dateFinPrevue}
                                        </div>
                                    </c:if>
                                </div>
                            </td>
                            <td style="text-align: center;">
                                <span style="display: inline-flex; align-items: center; justify-content: center; min-width: 36px; height: 36px; background: #F5F5F5; border-radius: 8px; font-weight: 600; color: #1A1A1A;">
                                    -
                                </span>
                            </td>
                            <td style="text-align: center;">
                                <c:choose>
                                    <c:when test="${projet.statut.name() == 'PLANIFIE'}">
                                        <span class="badge-elegant badge-warning">Planifié</span>
                                    </c:when>
                                    <c:when test="${projet.statut.name() == 'EN_COURS'}">
                                        <span class="badge-elegant badge-info">En cours</span>
                                    </c:when>
                                    <c:when test="${projet.statut.name() == 'TERMINE'}">
                                        <span class="badge-elegant badge-success">Terminé</span>
                                    </c:when>
                                    <c:when test="${projet.statut.name() == 'ANNULE'}">
                                        <span class="badge-elegant badge-danger">Annulé</span>
                                    </c:when>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <div style="display: inline-flex; gap: 8px;">
                                    <a href="${pageContext.request.contextPath}/app/projets?action=show&id=${projet.id}" 
                                       class="btn btn-outline btn-sm" title="Voir les détails">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/projets?action=edit&id=${projet.id}" 
                                       class="btn btn-secondary btn-sm" title="Modifier">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    <button onclick="if(confirm('Êtes-vous sûr de vouloir supprimer ce projet ?')) { window.location.href='${pageContext.request.contextPath}/app/projets?action=delete&id=${projet.id}'; }"
                                            class="btn btn-secondary btn-sm" title="Supprimer">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div style="padding: 80px 40px; text-align: center;">
                <i class="fas fa-project-diagram" style="font-size: 64px; color: #E0E0E0; margin-bottom: 24px;"></i>
                <h3 style="font-family: 'Playfair Display', serif; font-size: 24px; font-weight: 600; color: #1A1A1A; margin-bottom: 12px;">
                    Aucun projet
                </h3>
                <p style="color: #666666; margin-bottom: 24px;">
                    Créez votre premier projet pour commencer
                </p>
                <a href="${pageContext.request.contextPath}/app/projets?action=add" class="btn btn-primary">
                    <i class="fas fa-plus" style="margin-right: 8px;"></i>Créer un projet
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../layout/footer.jsp" />
