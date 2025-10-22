<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Fiches de Paie de ${employe.prenom} ${employe.nom} - Système RH" />
    <jsp:param name="page" value="fiches-paie" />
</jsp:include>

<div class="header">
    <div style="display: flex; align-items: center; gap: 24px;">
        <div class="avatar-lg">
            ${employe.prenom.substring(0,1)}${employe.nom.substring(0,1)}
        </div>
        <div style="flex: 1;">
            <h1 style="margin-bottom: 8px;">Fiches de Paie</h1>
            <p class="subtitle">
                ${employe.prenom} ${employe.nom} • ${employe.matricule} • ${employe.poste}
            </p>
        </div>
        <div style="display: flex; gap: 12px;">
            <a href="${pageContext.request.contextPath}/app/employes?action=show&id=${employe.id}" class="btn btn-secondary">
                <i class="fas fa-user" style="margin-right: 8px;"></i>Voir le profil
            </a>
            <a href="${pageContext.request.contextPath}/app/fiches-paie" class="btn btn-secondary">
                <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
            </a>
        </div>
    </div>
</div>

<!-- Statistiques -->
<div class="stats-grid" style="grid-template-columns: repeat(4, 1fr); margin-bottom: 32px;">
    <div class="stat-card">
        <i class="fas fa-file-invoice-dollar"></i>
        <h4>${fichesPaie.size()}</h4>
        <p>Fiches générées</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-euro-sign"></i>
        <h4><fmt:formatNumber value="${employe.salaireBase}" pattern="#,##0"/> €</h4>
        <p>Salaire de base</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-chart-line"></i>
        <h4>
            <c:choose>
                <c:when test="${not empty dernierSalaire}">
                    <fmt:formatNumber value="${dernierSalaire}" pattern="#,##0"/> €
                </c:when>
                <c:otherwise>N/A</c:otherwise>
            </c:choose>
        </h4>
        <p>Dernier net payé</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-calculator"></i>
        <h4>
            <c:choose>
                <c:when test="${not empty totalAnnuel}">
                    <fmt:formatNumber value="${totalAnnuel}" pattern="#,##0"/> €
                </c:when>
                <c:otherwise>N/A</c:otherwise>
            </c:choose>
        </h4>
        <p>Total annuel</p>
    </div>
</div>

<!-- Historique des fiches -->
<div class="card">
    <div class="card-header">
        <i class="fas fa-history" style="margin-right: 12px;"></i>Historique des fiches de paie
    </div>
    
    <c:choose>
        <c:when test="${not empty fichesPaie}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>PÉRIODE</th>
                        <th>SALAIRE BASE</th>
                        <th>PRIMES</th>
                        <th>DÉDUCTIONS</th>
                        <th>NET À PAYER</th>
                        <th>ÉVOLUTION</th>
                        <th style="text-align: center;">ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="fiche" items="${fichesPaie}" varStatus="status">
                        <tr>
                            <td>
                                <div style="font-family: 'Courier New', monospace; font-weight: 600; color: #C5A572;">
                                    ${fiche.mois}/${fiche.annee}
                                </div>
                                <div style="font-size: 12px; color: #666666;">
                                    <fmt:formatDate value="${fiche.dateGeneration}" pattern="dd/MM/yyyy" />
                                </div>
                            </td>
                            <td style="font-weight: 600;">
                                <fmt:formatNumber value="${fiche.salaireBase}" pattern="#,##0"/> €
                            </td>
                            <td style="color: #22C55E; font-weight: 600;">
                                + <fmt:formatNumber value="${fiche.primes}" pattern="#,##0"/> €
                            </td>
                            <td style="color: #EF4444; font-weight: 600;">
                                - <fmt:formatNumber value="${fiche.deductions}" pattern="#,##0"/> €
                            </td>
                            <td>
                                <div style="font-size: 16px; font-weight: 700; color: #1A1A1A;">
                                    <fmt:formatNumber value="${fiche.netAPayer}" pattern="#,##0"/> €
                                </div>
                            </td>
                            <td>
                                <c:if test="${status.index > 0}">
                                    <c:set var="previousFiche" value="${fichesPaie[status.index - 1]}" />
                                    <c:set var="diff" value="${fiche.netAPayer - previousFiche.netAPayer}" />
                                    <c:choose>
                                        <c:when test="${diff > 0}">
                                            <span style="color: #22C55E; font-weight: 600; display: flex; align-items: center; gap: 4px;">
                                                <i class="fas fa-arrow-up"></i>
                                                <fmt:formatNumber value="${diff}" pattern="#,##0"/> €
                                            </span>
                                        </c:when>
                                        <c:when test="${diff < 0}">
                                            <span style="color: #EF4444; font-weight: 600; display: flex; align-items: center; gap: 4px;">
                                                <i class="fas fa-arrow-down"></i>
                                                <fmt:formatNumber value="${-diff}" pattern="#,##0"/> €
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #999999;">
                                                <i class="fas fa-minus"></i> Stable
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </td>
                            <td style="text-align: center;">
                                <div style="display: inline-flex; gap: 8px;">
                                    <a href="${pageContext.request.contextPath}/app/fiches-paie?action=show&id=${fiche.id}" 
                                       class="btn btn-outline btn-sm" title="Voir le bulletin">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/fiches-paie?action=pdf&id=${fiche.id}" 
                                       class="btn btn-secondary btn-sm" title="Télécharger PDF" target="_blank">
                                        <i class="fas fa-file-pdf"></i>
                                    </a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div style="padding: 60px 40px; text-align: center;">
                <i class="fas fa-file-invoice-dollar" style="font-size: 48px; color: #E0E0E0; margin-bottom: 16px;"></i>
                <p style="color: #666666;">Aucune fiche de paie générée pour cet employé</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../layout/footer.jsp" />
