<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Demande de Congé - Système RH" />
    <jsp:param name="page" value="conges" />
</jsp:include>

<div class="header">
    <div style="display: flex; align-items: center; gap: 24px;">
        <div style="width: 80px; height: 80px; background: linear-gradient(135deg, #C5A572 0%, #A08555 100%); border-radius: 12px; display: flex; align-items: center; justify-content: center;">
            <i class="fas fa-calendar-check" style="font-size: 36px; color: white;"></i>
        </div>
        <div style="flex: 1;">
            <h1 style="margin-bottom: 8px;">Demande de Congé</h1>
            <p class="subtitle">
                ${conge.typeConge} • ${conge.nombreJours} jours
            </p>
        </div>
        <div style="display: flex; gap: 12px;">
            <%-- Vérifier si l'utilisateur peut modifier ce congé --%>
            <c:set var="isRHorAdmin" value="${sessionScope.utilisateur.role eq 'ADMIN' or sessionScope.utilisateur.role eq 'RH'}" />
            <c:set var="isOwnConge" value="${sessionScope.employeId == conge.employe.id}" />
            <c:set var="canEdit" value="${isRHorAdmin or isOwnConge}" />
            
            <c:if test="${conge.statut.name() == 'EN_ATTENTE' and canEdit}">
                <a href="${pageContext.request.contextPath}/app/conges-absences?action=edit&id=${conge.id}" class="btn btn-primary">
                    <i class="fas fa-edit" style="margin-right: 8px;"></i>Modifier
                </a>
            </c:if>
            <a href="${pageContext.request.contextPath}/app/conges-absences" class="btn btn-secondary">
                <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
            </a>
        </div>
    </div>
</div>

<!-- Statut -->
<div style="text-align: center; margin-bottom: 32px;">
    <c:choose>
        <c:when test="${conge.statut.name() == 'EN_ATTENTE'}">
            <span class="badge-elegant badge-warning" style="font-size: 16px; padding: 12px 24px;">
                <i class="fas fa-clock"></i> En attente de validation
            </span>
        </c:when>
        <c:when test="${conge.statut.name() == 'APPROUVE'}">
            <span class="badge-elegant badge-success" style="font-size: 16px; padding: 12px 24px;">
                <i class="fas fa-check-circle"></i> Approuvée
            </span>
        </c:when>
        <c:when test="${conge.statut.name() == 'REFUSE'}">
            <span class="badge-elegant badge-danger" style="font-size: 16px; padding: 12px 24px;">
                <i class="fas fa-times-circle"></i> Refusée
            </span>
        </c:when>
        <c:when test="${conge.statut.name() == 'ANNULE'}">
            <span class="badge-elegant badge-secondary" style="font-size: 16px; padding: 12px 24px;">
                <i class="fas fa-ban"></i> Annulée
            </span>
        </c:when>
    </c:choose>
</div>

<div style="display: grid; grid-template-columns: 2fr 1fr; gap: 24px;">
    <!-- Détails de la demande -->
    <div>
        <div class="card" style="margin-bottom: 24px;">
            <div class="card-header">
                <i class="fas fa-info-circle" style="margin-right: 12px;"></i>Informations de la demande
            </div>
            <div style="padding: 32px;">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 32px; margin-bottom: 24px;">
                    <div>
                        <label class="form-label">TYPE DE CONGÉ</label>
                        <p style="font-size: 16px; font-weight: 600; color: #1A1A1A; margin-top: 8px;">
                            ${conge.typeConge}
                        </p>
                    </div>
                    <div>
                        <label class="form-label">NOMBRE DE JOURS</label>
                        <p style="font-size: 28px; font-weight: 700; color: #C5A572; margin-top: 8px;">
                            ${conge.nombreJours} jours
                        </p>
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 32px; margin-bottom: 24px;">
                    <div>
                        <label class="form-label">DATE DE DÉBUT</label>
                        <p style="font-size: 16px; font-weight: 600; color: #1A1A1A; margin-top: 8px;">
                            ${conge.dateDebut}
                        </p>
                    </div>
                    <div>
                        <label class="form-label">DATE DE FIN</label>
                        <p style="font-size: 16px; font-weight: 600; color: #1A1A1A; margin-top: 8px;">
                            ${conge.dateFin}
                        </p>
                    </div>
                </div>

                <c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
                    <c:if test="${not empty conge.motif}">
                        <div>
                            <label class="form-label">MOTIF</label>
                            <p style="font-size: 14px; color: #1A1A1A; line-height: 1.6; margin-top: 8px; white-space: pre-line;">
                                ${conge.motif}
                            </p>
                        </div>
                    </c:if>
                </c:if>
            </div>
        </div>

        <!-- Validation RH -->
        <c:if test="${conge.statut != 'EN_ATTENTE'}">
            <div class="card">
                <div class="card-header">
                    <i class="fas fa-user-shield" style="margin-right: 12px;"></i>Validation RH
                </div>
                <div style="padding: 32px;">
                    <div style="margin-bottom: 24px;">
                        <label class="form-label">VALIDÉ PAR</label>
                        <c:choose>
                            <c:when test="${not empty conge.approuvePar}">
                                <div style="display: flex; align-items: center; gap: 12px; margin-top: 12px;">
                                    <div class="avatar">
                                        <c:out value="${conge.approuvePar.prenom.substring(0,1)}${conge.approuvePar.nom.substring(0,1)}"/>
                                    </div>
                                    <div style="flex: 1;">
                                        <div style="font-weight: 600; color: #1A1A1A;">
                                            <c:out value="${conge.approuvePar.prenom}"/> <c:out value="${conge.approuvePar.nom}"/>
                                        </div>
                                        <div style="font-size: 12px; color: #666666;">
                                            Matricule : <c:out value="${conge.approuvePar.matricule}"/>
                                        </div>
                                        <div style="font-size: 12px; color: #666666;">
                                            Date validation : <c:out value="${dateApprobationFormatee}"/>
                                        </div>
                                        <a href="${pageContext.request.contextPath}/app/employes?action=show&id=${conge.approuvePar.id}" 
                                           class="btn btn-outline btn-sm" 
                                           style="margin-top: 10px; display: inline-flex; align-items: center; gap: 6px;">
                                            <i class="fas fa-user"></i> Voir profil RH
                                        </a>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p style="color: #999999; margin-top: 8px;">Non renseigné</p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <c:if test="${not empty conge.commentairesApprobation}">
                        <div>
                            <label class="form-label">COMMENTAIRE</label>
                            <p style="font-size: 14px; color: #1A1A1A; line-height: 1.6; margin-top: 8px; white-space: pre-line;">
                                <c:out value="${conge.commentairesApprobation}"/>
                            </p>
                        </div>
                    </c:if>
                </div>
            </div>
        </c:if>
    </div>

    <!-- Informations employé -->
    <div>
        <div class="card">
            <div class="card-header">
                <i class="fas fa-user" style="margin-right: 12px;"></i>Employé
            </div>
            <div style="padding: 32px; text-align: center;">
                <div class="avatar-lg" style="margin: 0 auto 16px;">
                    ${conge.employe.prenom.substring(0,1)}${conge.employe.nom.substring(0,1)}
                </div>
                <div style="font-size: 18px; font-weight: 600; color: #1A1A1A; margin-bottom: 4px;">
                    ${conge.employe.prenom} ${conge.employe.nom}
                </div>
                <div style="font-size: 14px; color: #666666; margin-bottom: 8px;">
                    ${conge.employe.poste}
                </div>
                <div style="font-size: 12px; color: #999999; font-family: 'Courier New', monospace;">
                    ${conge.employe.matricule}
                </div>
            </div>
        </div>

        <c:if test="${sessionScope.utilisateur.role == 'ADMIN' || sessionScope.utilisateur.role == 'RH'}">
            <c:if test="${conge.statut.name() == 'EN_ATTENTE'}">
                <div class="card" style="margin-top: 24px;">
                    <div class="card-header">
                        <i class="fas fa-tasks" style="margin-right: 12px;"></i>Actions RH
                    </div>
                    <div style="padding: 24px;">
                        <form action="${pageContext.request.contextPath}/app/conges-absences" method="post" style="margin-bottom: 12px;">
                            <input type="hidden" name="action" value="approve">
                            <input type="hidden" name="id" value="${conge.id}">
                            <button type="submit" class="btn btn-primary" style="width: 100%;">
                                <i class="fas fa-check" style="margin-right: 8px;"></i>Approuver
                            </button>
                        </form>

                        <form action="${pageContext.request.contextPath}/app/conges-absences" method="post">
                            <input type="hidden" name="action" value="reject">
                            <input type="hidden" name="id" value="${conge.id}">
                            <button type="submit" class="btn btn-secondary" style="width: 100%;">
                                <i class="fas fa-times" style="margin-right: 8px;"></i>Rejeter
                            </button>
                        </form>
                    </div>
                </div>
            </c:if>
        </c:if>
    </div>
</div>

<jsp:include page="../layout/footer.jsp" />
