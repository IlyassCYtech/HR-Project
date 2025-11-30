<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Mes Congés - Système RH" />
    <jsp:param name="page" value="conges" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">Mes Congés</h1>
            <p class="subtitle">Gérez vos demandes de congés et absences</p>
        </div>
        <a href="${pageContext.request.contextPath}/app/conges-absences?action=new" class="btn btn-primary">
            <i class="fas fa-plus" style="margin-right: 8px;"></i>Nouvelle demande
        </a>
    </div>
</div>

<!-- Solde -->
<c:if test="${not empty soldeConges}">
    <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-bottom: 32px;">
        <div class="card" style="background: linear-gradient(135deg, #C5A572 0%, #A08555 100%); color: white;">
            <div style="padding: 32px;">
                <label style="font-size: 11px; font-weight: 600; letter-spacing: 1.5px; text-transform: uppercase; color: rgba(255,255,255,0.8); margin-bottom: 8px; display: block;">
                    Congés payés
                </label>
                <h2 style="font-family: 'Playfair Display', serif; font-size: 42px; font-weight: 700; margin: 0; color: white;">
                    ${soldeConges.congesPayes}
                </h2>
                <p style="font-size: 14px; margin-top: 8px; color: rgba(255,255,255,0.9);">jours disponibles</p>
            </div>
        </div>

        <div class="stat-card">
            <i class="fas fa-clock"></i>
            <h4>${nombreDemandes}</h4>
            <p>Demandes en cours</p>
        </div>
    </div>
</c:if>

<!-- Filtres -->
<div class="card" style="margin-bottom: 24px;">
    <div class="card-header">
        <i class="fas fa-filter" style="margin-right: 12px;"></i>Filtres
    </div>
    <form method="GET" action="${pageContext.request.contextPath}/app/conges-absences">
        <input type="hidden" name="action" value="mesConges">
        <div style="padding: 24px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;">
            <div>
                <label class="form-label" for="type">TYPE</label>
                <select id="type" name="type" class="form-control" >
                    <option value="">Tous les types</option>
                    <option value="CONGES_PAYES" ${param.type == 'CONGES_PAYES' ? 'selected' : ''}>Congés payés</option>
                    <option value="MALADIE" ${param.type == 'MALADIE' ? 'selected' : ''}>Maladie</option>
                    <option value="MATERNITE" ${param.type == 'MATERNITE' ? 'selected' : ''}>Maternité</option>
                    <option value="PATERNITE" ${param.type == 'PATERNITE' ? 'selected' : ''}>Paternité</option>
                    <option value="FORMATION" ${param.type == 'FORMATION' ? 'selected' : ''}>Formation</option>
                    <option value="SANS_SOLDE" ${param.type == 'SANS_SOLDE' ? 'selected' : ''}>Sans solde</option>
                </select>
            </div>

            <div>
                <label class="form-label" for="statut">STATUT</label>
                <select id="statut" name="statut" class="form-control">
                    <option value="">Tous les statuts</option>
                    <option value="EN_ATTENTE" ${param.statut == 'EN_ATTENTE' ? 'selected' : ''}>En attente</option>
                    <option value="APPROUVE" ${param.statut == 'APPROUVE' ? 'selected' : ''}>Approuvé</option>
                    <option value="REJETE" ${param.statut == 'REJETE' ? 'selected' : ''}>Rejeté</option>
                </select>
            </div>

            <div style="display: flex; align-items: flex-end;">
                <button type="submit" class="btn btn-primary" style="flex: 1; margin-right: 8px;" 
                <c:if test="${utilisateur.role ne 'ADMIN' and utilisateur.role ne 'RH'}">disabled</c:if>>
                    <i class="fas fa-search" style="margin-right: 8px;"></i>Filtrer
                </button>
                <a href="${pageContext.request.contextPath}/app/conges-absences?action=mesConges" class="btn btn-secondary">
                    Réinitialiser
                </a>
            </div>
        </div>
    </form>
</div>

<!-- Liste des demandes -->
<div class="card">
    <div class="card-header">
        <i class="fas fa-list" style="margin-right: 12px;"></i>Historique de mes demandes
    </div>
    
    <c:choose>
        <c:when test="${not empty conges}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>TYPE</th>
                        <th>PÉRIODE</th>
                        <th style="text-align: center;">JOURS</th>
                        <th>MOTIF</th>
                        <th style="text-align: center;">STATUT</th>
                        <th style="text-align: center;">ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="conge" items="${conges}">
                        <tr>
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
                                <div style="max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; color: #666666;">
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
                                <c:choose>
                                    <c:when test="${conge.statut.name() == 'EN_ATTENTE'}">
                                        <span class="badge-elegant badge-warning">En attente</span>
                                    </c:when>
                                    <c:when test="${conge.statut.name() == 'APPROUVE'}">
                                        <span class="badge-elegant badge-success">Approuvé</span>
                                    </c:when>
                                    <c:when test="${conge.statut.name() == 'REJETE'}">
                                        <span class="badge-elegant badge-danger">Rejeté</span>
                                    </c:when>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <div style="display: inline-flex; gap: 8px;">
                                    <a href="${pageContext.request.contextPath}/app/conges-absences?action=show&id=${conge.id}" 
                                       class="btn btn-outline btn-sm" title="Voir les détails">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <c:if test="${conge.statut.name() == 'EN_ATTENTE'}">
                                        <a href="${pageContext.request.contextPath}/app/conges-absences?action=edit&id=${conge.id}" 
                                           class="btn btn-secondary btn-sm" title="Modifier">
                                            <i class="fas fa-edit"></i>
                                        </a>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div style="padding: 80px 40px; text-align: center;">
                <i class="fas fa-calendar-check" style="font-size: 64px; color: #E0E0E0; margin-bottom: 24px;"></i>
                <h3 style="font-family: 'Playfair Display', serif; font-size: 24px; font-weight: 600; color: #1A1A1A; margin-bottom: 12px;">
                    Aucune demande
                </h3>
                <p style="color: #666666; margin-bottom: 24px;">
                    Créez votre première demande de congé
                </p>
                <a href="${pageContext.request.contextPath}/app/conges-absences?action=new" class="btn btn-primary">
                    <i class="fas fa-plus" style="margin-right: 8px;"></i>Nouvelle demande
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../layout/footer.jsp" />
