<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Demandes en Attente - Système RH" />
    <jsp:param name="page" value="conges" />
</jsp:include>

<div class="header">
    <div>
        <h1 style="margin-bottom: 8px;">Demandes de Congés en Attente</h1>
        <p class="subtitle">Validation RH des demandes de congés et absences</p>
    </div>
</div>

<!-- Statistiques -->
<div class="stats-grid" style="grid-template-columns: repeat(4, 1fr); margin-bottom: 32px;">
    <div class="stat-card">
        <i class="fas fa-clock"></i>
        <h4>${stats.enAttente}</h4>
        <p>En attente</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-check-circle"></i>
        <h4>${stats.approuvees}</h4>
        <p>Approuvées (mois)</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-times-circle"></i>
        <h4>${stats.rejetees}</h4>
        <p>Rejetées (mois)</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-calendar-day"></i>
        <h4>${stats.joursTotal}</h4>
        <p>Jours total (mois)</p>
    </div>
</div>

<!-- Liste des demandes -->
<div class="card">
    <div class="card-header">
        <i class="fas fa-tasks" style="margin-right: 12px;"></i>Demandes en attente de validation
    </div>
    
    <c:choose>
        <c:when test="${not empty congesEnAttente}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>EMPLOYÉ</th>
                        <th>TYPE</th>
                        <th>PÉRIODE</th>
                        <th style="text-align: center;">JOURS</th>
                        <th>MOTIF</th>
                        <th style="text-align: center;">ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="conge" items="${congesEnAttente}">
                        <tr>
                            <td>
                                <div style="display: flex; align-items: center; gap: 12px;">
                                    <div class="avatar">
                                        ${conge.employe.prenom.substring(0,1)}${conge.employe.nom.substring(0,1)}
                                    </div>
                                    <div>
                                        <div style="font-weight: 600; color: #1A1A1A;">
                                            ${conge.employe.prenom} ${conge.employe.nom}
                                        </div>
                                        <div style="font-size: 12px; color: #666666;">
                                            ${conge.employe.poste}<c:if test="${not empty conge.employe.departement}"> • ${conge.employe.departement.nom}</c:if>
                                        </div>
                                    </div>
                                </div>
                            </td>
                            <td style="font-weight: 600; color: #1A1A1A;">
                                ${conge.typeConge}
                            </td>
                            <td>
                                <div style="font-size: 13px; color: #1A1A1A;">
                                    <div style="margin-bottom: 4px;">
                                        <i class="fas fa-play-circle" style="color: #C5A572; margin-right: 6px; font-size: 11px;"></i>
                                        ${conge.dateDebut}
                                    </div>
                                    <div>
                                        <i class="fas fa-flag-checkered" style="color: #C5A572; margin-right: 6px; font-size: 11px;"></i>
                                        ${conge.dateFin}
                                    </div>
                                </div>
                            </td>
                            <td style="text-align: center;">
                                <span style="display: inline-flex; align-items: center; justify-content: center; min-width: 36px; height: 36px; background: #F5F5F5; border-radius: 8px; font-weight: 600; color: #1A1A1A;">
                                    ${conge.nombreJours}
                                </span>
                            </td>
                            <td>
                                <div style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; color: #666666;">
                                    <c:choose>
                                        <c:when test="${not empty conge.motif}">
                                            ${conge.motif}
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #999999;">-</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </td>
                            <td style="text-align: center;">
                                <div style="display: inline-flex; gap: 8px;">
                                    <a href="${pageContext.request.contextPath}/app/conges-absences?action=show&id=${conge.id}" 
                                       class="btn btn-outline btn-sm" title="Voir">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <form action="${pageContext.request.contextPath}/app/conges-absences" method="post" style="display: inline;">
                                        <input type="hidden" name="action" value="approve">
                                        <input type="hidden" name="id" value="${conge.id}">
                                        <button type="submit" class="btn btn-primary btn-sm" title="Approuver">
                                            <i class="fas fa-check"></i>
                                        </button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/app/conges-absences" method="post" style="display: inline;">
                                        <input type="hidden" name="action" value="reject">
                                        <input type="hidden" name="id" value="${conge.id}">
                                        <button type="submit" class="btn btn-secondary btn-sm" title="Rejeter">
                                            <i class="fas fa-times"></i>
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div style="padding: 60px 40px; text-align: center;">
                <i class="fas fa-check-circle" style="font-size: 48px; color: #E0E0E0; margin-bottom: 16px;"></i>
                <p style="color: #666666;">Aucune demande en attente de validation</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../layout/footer.jsp" />
