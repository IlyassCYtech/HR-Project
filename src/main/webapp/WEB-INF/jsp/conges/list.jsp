<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Gestion des Congés - Système RH" />
    <jsp:param name="page" value="conges" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">Gestion des Congés</h1>
            <p class="subtitle">Vue d'ensemble des congés et absences</p>
        </div>
        <a href="${pageContext.request.contextPath}/app/conges-absences?action=new" class="btn btn-primary">
            <i class="fas fa-plus" style="margin-right: 8px;"></i>Nouvelle demande
        </a>
    </div>
</div>

<c:if test="${not empty success}">
    <div class="alert alert-success" role="alert">
        <i class="fas fa-check-circle" style="margin-right: 8px;"></i>${success}
    </div>
</c:if>

<!-- Filtres -->
<div class="card" style="margin-bottom: 24px;">
    <div class="card-header">
        <i class="fas fa-filter" style="margin-right: 12px;"></i>Filtres
    </div>
    <form method="GET" action="${pageContext.request.contextPath}/app/conges-absences">
        <div style="padding: 24px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px;">
            <div>
                <label class="form-label" for="employeId">EMPLOYÉ</label>
                <select id="employeId" name="employeId" class="form-control">
                    <option value="">Tous les employés</option>
                    <c:forEach var="emp" items="${employes}">
                        <option value="${emp.id}" ${param.employeId == emp.id ? 'selected' : ''}>
                            ${emp.prenom} ${emp.nom}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div>
                <label class="form-label" for="type">TYPE</label>
                <select id="type" name="type" class="form-control">
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
                <button type="submit" class="btn btn-primary" style="flex: 1; margin-right: 8px;">
                    <i class="fas fa-search" style="margin-right: 8px;"></i>Filtrer
                </button>
                <a href="${pageContext.request.contextPath}/app/conges-absences" class="btn btn-secondary">
                    Réinitialiser
                </a>
            </div>
        </div>
    </form>
</div>

<!-- Liste des congés -->
<div class="card">
    <div class="card-header">
        <i class="fas fa-calendar-check" style="margin-right: 12px;"></i>Liste des demandes de congés
    </div>
    
    <c:choose>
        <c:when test="${not empty conges}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>EMPLOYÉ</th>
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
                                            ${conge.employe.poste}
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
                                <c:choose>
                                    <c:when test="${conge.statut.name() == 'EN_ATTENTE'}">
                                        <span class="badge-elegant badge-warning">En attente</span>
                                    </c:when>
                                    <c:when test="${conge.statut.name() == 'APPROUVE'}">
                                        <span class="badge-elegant badge-success">Approuvé</span>
                                    </c:when>
                                    <c:when test="${conge.statut.name() == 'REFUSE'}">
                                        <span class="badge-elegant badge-danger">Refusé</span>
                                    </c:when>
                                    <c:when test="${conge.statut.name() == 'ANNULE'}">
                                        <span class="badge-elegant badge-secondary">Annulé</span>
                                    </c:when>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <div style="display: inline-flex; gap: 6px; align-items: center;">
                                    <a href="${pageContext.request.contextPath}/app/conges-absences?action=details&id=${conge.id}" 
                                       class="btn btn-outline btn-sm" 
                                       style="padding: 6px 10px; font-size: 13px;"
                                       title="Voir">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    
                                    <!-- Boutons pour congés EN_ATTENTE -->
                                    <c:if test="${conge.statut.name() == 'EN_ATTENTE'}">
                                        <form action="${pageContext.request.contextPath}/app/conges-absences" method="post" style="display: inline; margin: 0;">
                                            <input type="hidden" name="action" value="approve">
                                            <input type="hidden" name="id" value="${conge.id}">
                                            <button type="submit" 
                                                    class="btn btn-sm" 
                                                    style="background: #10B981; color: white; border: 1px solid #10B981; padding: 6px 10px; font-size: 13px;"
                                                    title="Approuver"
                                                    onclick="return confirm('Approuver cette demande de congé ?')">
                                                <i class="fas fa-check"></i>
                                            </button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/app/conges-absences" method="post" style="display: inline; margin: 0;">
                                            <input type="hidden" name="action" value="reject">
                                            <input type="hidden" name="id" value="${conge.id}">
                                            <button type="submit" 
                                                    class="btn btn-sm" 
                                                    style="background: #EF4444; color: white; border: 1px solid #EF4444; padding: 6px 10px; font-size: 13px;"
                                                    title="Refuser"
                                                    onclick="return confirm('Refuser cette demande de congé ?')">
                                                <i class="fas fa-times"></i>
                                            </button>
                                        </form>
                                    </c:if>
                                    
                                    <!-- Bouton pour ré-approuver un congé REFUSE -->
                                    <c:if test="${conge.statut.name() == 'REFUSE'}">
                                        <form action="${pageContext.request.contextPath}/app/conges-absences" method="post" style="display: inline; margin: 0;">
                                            <input type="hidden" name="action" value="approve">
                                            <input type="hidden" name="id" value="${conge.id}">
                                            <button type="submit" 
                                                    class="btn btn-sm" 
                                                    style="background: #10B981; color: white; border: 1px solid #10B981; padding: 6px 10px; font-size: 13px;"
                                                    title="Approuver quand même"
                                                    onclick="return confirm('Approuver cette demande précédemment refusée ?')">
                                                <i class="fas fa-check"></i>
                                            </button>
                                        </form>
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
                    Aucune demande de congé n'a été trouvée
                </p>
                <a href="${pageContext.request.contextPath}/app/conges-absences?action=new" class="btn btn-primary">
                    <i class="fas fa-plus" style="margin-right: 8px;"></i>Nouvelle demande
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../layout/footer.jsp" />
