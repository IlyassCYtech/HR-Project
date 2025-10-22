<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="${projet.nom} - Système RH" />
    <jsp:param name="page" value="projets" />
</jsp:include>

<div class="header">
    <div style="display: flex; align-items: center; gap: 24px;">
        <div style="width: 80px; height: 80px; background: linear-gradient(135deg, #C5A572 0%, #A08555 100%); border-radius: 12px; display: flex; align-items: center; justify-content: center;">
            <i class="fas fa-project-diagram" style="font-size: 36px; color: white;"></i>
        </div>
        <div style="flex: 1;">
            <h1 style="margin-bottom: 8px;">${projet.nom}</h1>
            <p class="subtitle">
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
            </p>
        </div>
        <div style="display: flex; gap: 12px;">
            <a href="${pageContext.request.contextPath}/app/projets?action=edit&id=${projet.id}" class="btn btn-primary">
                <i class="fas fa-edit" style="margin-right: 8px;"></i>Modifier
            </a>
            <a href="${pageContext.request.contextPath}/app/projets" class="btn btn-secondary">
                <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
            </a>
        </div>
    </div>
</div>

<!-- Statistiques -->
<div class="stats-grid" style="grid-template-columns: repeat(3, 1fr); margin-bottom: 32px;">
    <div class="stat-card">
        <i class="fas fa-users"></i>
        <h4>${not empty projet.employes ? projet.employes.size() : 0}</h4>
        <p>Membres de l'équipe</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-calendar"></i>
        <h4>
            ${projet.dateDebut}
        </h4>
        <p>Date de début</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-flag-checkered"></i>
        <h4>
            ${projet.dateFinPrevue}
        </h4>
        <p>Date de fin prévue</p>
    </div>
</div>

<div style="display: grid; grid-template-columns: 2fr 1fr; gap: 24px; margin-bottom: 24px;">
    <!-- Description -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-info-circle" style="margin-right: 12px;"></i>Description du projet
        </div>
        <div style="padding: 32px;">
            <p style="font-size: 14px; color: #1A1A1A; line-height: 1.8; white-space: pre-line;">
                ${projet.description}
            </p>
        </div>
    </div>

    <!-- Informations -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-clipboard-list" style="margin-right: 12px;"></i>Informations
        </div>
        <div style="padding: 32px;">
            <div style="margin-bottom: 24px;">
                <label class="form-label">CHEF DE PROJET</label>
                <c:choose>
                    <c:when test="${not empty projet.chefProjet}">
                        <div style="display: flex; align-items: center; gap: 12px; margin-top: 12px;">
                            <div class="avatar">
                                ${projet.chefProjet.prenom.substring(0,1)}${projet.chefProjet.nom.substring(0,1)}
                            </div>
                            <div>
                                <div style="font-weight: 600; color: #1A1A1A;">
                                    <a href="${pageContext.request.contextPath}/app/employes?action=show&id=${projet.chefProjet.id}"
                                       style="color: #C5A572; text-decoration: none;">
                                        ${projet.chefProjet.prenom} ${projet.chefProjet.nom}
                                    </a>
                                </div>
                                <div style="font-size: 12px; color: #666666;">
                                    ${projet.chefProjet.poste}
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p style="color: #999999; margin-top: 8px;">Non assigné</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <div>
                <label class="form-label">DÉPARTEMENT</label>
                <p style="font-size: 14px; color: #1A1A1A; font-weight: 500; margin-top: 8px;">
                    <c:choose>
                        <c:when test="${not empty projet.departement}">
                            ${projet.departement.nom}
                        </c:when>
                        <c:otherwise>
                            <span style="color: #999999;">Non assigné</span>
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </div>
</div>

<!-- Équipe -->
<div class="card">
    <div class="card-header">
        <i class="fas fa-users" style="margin-right: 12px;"></i>Équipe du projet
    </div>
    
    <c:choose>
        <c:when test="${not empty projet.employes}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>EMPLOYÉ</th>
                        <th>POSTE</th>
                        <th>DÉPARTEMENT</th>
                        <th style="text-align: center;">STATUT</th>
                        <th style="text-align: center;">ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="empProjet" items="${projet.employes}">
                        <tr>
                            <td>
                                <div style="display: flex; align-items: center; gap: 12px;">
                                    <div class="avatar">
                                        ${empProjet.employe.prenom.substring(0,1)}${empProjet.employe.nom.substring(0,1)}
                                    </div>
                                    <div>
                                        <div style="font-weight: 600; color: #1A1A1A;">
                                            ${empProjet.employe.prenom} ${empProjet.employe.nom}
                                        </div>
                                        <div style="font-size: 12px; color: #666666; font-family: 'Courier New', monospace;">
                                            ${empProjet.employe.matricule}
                                        </div>
                                    </div>
                                </div>
                            </td>
                            <td>${empProjet.employe.poste}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty empProjet.employe.departement}">
                                        ${empProjet.employe.departement.nom}
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: #999;">Aucun</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <c:choose>
                                    <c:when test="${empProjet.employe.statut == 'ACTIF'}">
                                        <span class="badge-elegant badge-success">Actif</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-elegant badge-secondary">${empProjet.employe.statut}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <a href="${pageContext.request.contextPath}/app/employes?action=show&id=${empProjet.employe.id}" 
                                   class="btn btn-outline btn-sm">
                                    <i class="fas fa-eye"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div style="padding: 60px 40px; text-align: center;">
                <i class="fas fa-users" style="font-size: 48px; color: #E0E0E0; margin-bottom: 16px;"></i>
                <p style="color: #666666;">Aucun membre dans ce projet</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../layout/footer.jsp" />
