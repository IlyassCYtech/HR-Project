<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="layout/header.jsp">
    <jsp:param name="title" value="Statistiques et Rapports - Système RH" />
    <jsp:param name="page" value="statistiques" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">Statistiques et Rapports</h1>
            <p class="subtitle">Vue d'ensemble des indicateurs clés de votre système RH</p>
        </div>
        <a href="${pageContext.request.contextPath}/app/dashboard" class="btn btn-secondary">
            <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour au tableau de bord
        </a>
    </div>
</div>

<!-- Statistiques globales -->
<div class="stats-grid">
    <div class="stat-card">
        <i class="fas fa-users"></i>
        <h4>${stats.totalEmployes}</h4>
        <p>Employés actifs</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-building"></i>
        <h4>${stats.totalDepartements}</h4>
        <p>Départements</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-project-diagram"></i>
        <h4>${stats.totalProjets}</h4>
        <p>Total projets</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-clock"></i>
        <h4>${stats.projetsEnCours}</h4>
        <p>Projets en cours</p>
    </div>
</div>

<!-- Graphiques et détails -->
<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-top: 24px;">
    <!-- Employés par département -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-building" style="margin-right: 12px;"></i>Employés par département
        </div>
        <div style="padding: 24px;">
            <c:forEach var="dept" items="${departements}">
                <div style="margin-bottom: 20px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                        <span style="font-weight: 600; color: #1A1A1A; font-size: 14px;">
                            ${dept.nom}
                        </span>
                        <span class="badge-elegant badge-primary">
                            ${dept.nbEmployes} employé${dept.nbEmployes > 1 ? 's' : ''}
                        </span>
                    </div>
                    <div style="width: 100%; height: 8px; background-color: #F5F5F5; border-radius: 4px; overflow: hidden;">
                        <div style="width: ${dept.pourcentage}%; height: 100%; background: linear-gradient(135deg, #C5A572 0%, #D4B58D 100%); transition: width 0.3s ease;"></div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- Répartition par grade -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-graduation-cap" style="margin-right: 12px;"></i>Répartition par grade
        </div>
        <div style="padding: 24px;">
            <c:forEach var="grade" items="${grades}">
                <div style="margin-bottom: 20px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                        <span style="font-weight: 600; color: #1A1A1A; font-size: 14px;">
                            ${grade.nom}
                        </span>
                        <span class="badge-elegant badge-success">
                            ${grade.count} employé${grade.count > 1 ? 's' : ''}
                        </span>
                    </div>
                    <div style="width: 100%; height: 8px; background-color: #F5F5F5; border-radius: 4px; overflow: hidden;">
                        <div style="width: ${grade.pourcentage}%; height: 100%; background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%); transition: width 0.3s ease;"></div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</div>

<!-- État des projets -->
<div class="card" style="margin-top: 24px;">
    <div class="card-header">
        <i class="fas fa-tasks" style="margin-right: 12px;"></i>État des projets
    </div>
    <div class="table-container">
        <table class="table-elegant">
            <thead>
                <tr>
                    <th>Projet</th>
                    <th>Statut</th>
                    <th>Département</th>
                    <th style="text-align: center;">Membres</th>
                    <th>Dates</th>
                    <th style="text-align: center;">Chef de projet</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="projet" items="${projets}">
                    <tr>
                        <td>
                            <div style="font-weight: 600; color: #1A1A1A;">
                                ${projet.nom}
                            </div>
                            <c:if test="${not empty projet.description}">
                                <div style="font-size: 12px; color: #999999; margin-top: 4px; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                    ${projet.description}
                                </div>
                            </c:if>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${projet.statut.name() == 'EN_COURS'}">
                                    <span class="badge-elegant badge-warning">En cours</span>
                                </c:when>
                                <c:when test="${projet.statut.name() == 'TERMINE'}">
                                    <span class="badge-elegant badge-success">Terminé</span>
                                </c:when>
                                <c:when test="${projet.statut.name() == 'ANNULE'}">
                                    <span class="badge-elegant badge-danger">Annulé</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge-elegant badge-secondary">${projet.statut}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty projet.departement}">
                                    ${projet.departement.nom}
                                </c:when>
                                <c:otherwise>
                                    <span style="color: #999999; font-style: italic;">Non assigné</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td style="text-align: center;">
                            <span class="badge-elegant badge-primary">
                                ${projet.employes.size()}
                            </span>
                        </td>
                        <td>
                            <div style="font-size: 13px;">
                                <div style="color: #666666;">
                                    <i class="fas fa-calendar-alt" style="margin-right: 4px; font-size: 11px;"></i>
                                    ${projet.dateDebut}
                                </div>
                                <c:if test="${not empty projet.dateFinPrevue}">
                                    <div style="color: #999999; margin-top: 2px;">
                                        <i class="fas fa-arrow-right" style="margin-right: 4px; font-size: 11px;"></i>
                                        ${projet.dateFinPrevue}
                                    </div>
                                </c:if>
                            </div>
                        </td>
                        <td style="text-align: center;">
                            <c:choose>
                                <c:when test="${not empty projet.chefProjet}">
                                    <div class="avatar avatar-sm" title="${projet.chefProjet.prenom} ${projet.chefProjet.nom}">
                                        ${projet.chefProjet.prenom.substring(0,1)}${projet.chefProjet.nom.substring(0,1)}
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <span style="color: #999999;">—</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<!-- Statistiques supplémentaires -->
<div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-top: 24px;">
    <!-- Statut des employés -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-user-check" style="margin-right: 12px;"></i>Statut employés
        </div>
        <div style="padding: 24px;">
            <c:forEach var="statut" items="${statutsEmployes}">
                <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #F5F5F5;">
                    <span style="color: #666666; font-size: 14px;">${statut.nom}</span>
                    <span style="font-weight: 600; color: #1A1A1A; font-size: 14px;">${statut.count}</span>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- Statut des projets -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-chart-pie" style="margin-right: 12px;"></i>Statut projets
        </div>
        <div style="padding: 24px;">
            <c:forEach var="statut" items="${statutsProjets}">
                <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #F5F5F5;">
                    <span style="color: #666666; font-size: 14px;">${statut.nom}</span>
                    <span style="font-weight: 600; color: #1A1A1A; font-size: 14px;">${statut.count}</span>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- Moyennes -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-calculator" style="margin-right: 12px;"></i>Moyennes
        </div>
        <div style="padding: 24px;">
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #F5F5F5;">
                <span style="color: #666666; font-size: 14px;">Employés/Département</span>
                <span style="font-weight: 600; color: #1A1A1A; font-size: 14px;">
                    <fmt:formatNumber value="${stats.moyenneEmployesParDept}" maxFractionDigits="1"/>
                </span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #F5F5F5;">
                <span style="color: #666666; font-size: 14px;">Membres/Projet</span>
                <span style="font-weight: 600; color: #1A1A1A; font-size: 14px;">
                    <fmt:formatNumber value="${stats.moyenneMembresParProjet}" maxFractionDigits="1"/>
                </span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px 0;">
                <span style="color: #666666; font-size: 14px;">Projets/Département</span>
                <span style="font-weight: 600; color: #1A1A1A; font-size: 14px;">
                    <fmt:formatNumber value="${stats.moyenneProjetsParDept}" maxFractionDigits="1"/>
                </span>
            </div>
        </div>
    </div>
</div>

<style>
.avatar-sm {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, #C5A572 0%, #D4B58D 100%);
    color: white;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 11px;
    letter-spacing: 0.5px;
    margin: 0 auto;
}
</style>

<jsp:include page="layout/footer.jsp" />
