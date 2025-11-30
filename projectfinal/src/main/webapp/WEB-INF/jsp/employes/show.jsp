<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Vérifier si l'utilisateur peut voir les données confidentielles --%>
<c:set var="isOwnProfile" value="${sessionScope.employeId == employe.id}" />
<c:set var="isRHorAdmin" value="${sessionScope.utilisateur.role eq 'ADMIN' or sessionScope.utilisateur.role eq 'RH'}" />
<c:set var="isChefOfDept" value="${not empty isChefOfEmployeeDept and isChefOfEmployeeDept}" />
<c:set var="canViewConfidential" value="${isOwnProfile or isRHorAdmin or isChefOfDept}" />

<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Profil de ${employe.prenom} ${employe.nom} - Système RH" />
    <jsp:param name="page" value="employes" />
</jsp:include>

<div class="header">
    <div style="display: flex; align-items: center; gap: 24px;">
        <div class="avatar-lg">
            ${employe.prenom.substring(0,1)}${employe.nom.substring(0,1)}
        </div>
        <div style="flex: 1;">
            <h1 style="margin-bottom: 8px;">${employe.prenom} ${employe.nom}</h1>
            <p class="subtitle">
                ${employe.poste}
                <c:if test="${not empty employe.departement}">
                    • ${employe.departement.nom}
                </c:if>
                • ${employe.matricule}
            </p>
        </div>
        <div style="display: flex; gap: 12px;">
	        <c:if test="${ sessionScope.utilisateur.role eq 'ADMIN' or sessionScope.utilisateur.role eq 'RH' }">
	            <a href="${pageContext.request.contextPath}/app/employes?action=edit&id=${employe.id}" class="btn btn-primary">
	                <i class="fas fa-edit" style="margin-right: 8px;"></i>Modifier
	            </a>
	        </c:if>
            <a href="${pageContext.request.contextPath}/app/employes" class="btn btn-secondary">
                <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
            </a>
        </div>
    </div>
</div>

<!-- Statistiques rapides -->
<div class="stats-grid" style="display:grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap:24px; margin-bottom: 32px;">
    <div class="stat-card">
        <i class="fas fa-building"></i>
        <h4>${not empty employe.departement ? employe.departement.nom : 'N/A'}</h4>
        <p>Département</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-star"></i>
        <h4>${employe.grade}</h4>
        <p>Grade</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-euro-sign"></i>
        <c:choose>
            <c:when test="${canViewConfidential}">
                <h4><fmt:formatNumber value="${employe.salaireBase}" pattern="#,##0"/> €</h4>
                <p>Salaire de base</p>
            </c:when>
            <c:otherwise>
                <h4><i class="fas fa-lock" style="color: #999;"></i></h4>
                <p style="color: #999;">Confidentiel</p>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="stat-card">
        <i class="fas fa-calendar"></i>
        <h4>${employe.dateEmbauche.dayOfMonth}/${employe.dateEmbauche.monthValue}/${employe.dateEmbauche.year}</h4>
        <p>Date d'embauche</p>
    </div>
</div>

<!-- Deux grandes cartes : perso / pro -->
<div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); gap:24px; margin-bottom: 24px;">
    <!-- Informations personnelles -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-user" style="margin-right: 12px;"></i>Informations personnelles
        </div>
        <div style="padding: 32px;">
            <div style="margin-bottom: 24px;">
                <label class="form-label">Email</label>
                <p style="font-size: 16px; color: #1A1A1A; font-weight: 500;">
                    <c:choose>
                        <c:when test="${not empty employe.email}">
                            <a href="mailto:${employe.email}" style="color: #C5A572; text-decoration: none;">
                                ${employe.email}
                            </a>
                        </c:when>
                        <c:otherwise>
                            <span style="color: #999999;">Non renseigné</span>
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>

            <div style="margin-bottom: 24px;">
                <label class="form-label">Téléphone</label>
                <c:choose>
                    <c:when test="${canViewConfidential}">
                        <p style="font-size: 16px; color: #1A1A1A; font-weight: 500;">
                            <c:choose>
                                <c:when test="${not empty employe.telephone}">
                                    ${employe.telephone}
                                </c:when>
                                <c:otherwise>
                                    <span style="color: #999999;">Non renseigné</span>
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <p style="font-size: 16px; color: #999999; font-style: italic;">
                            <i class="fas fa-lock" style="margin-right: 8px;"></i>Confidentiel
                        </p>
                    </c:otherwise>
                </c:choose>
            </div>

            <div style="margin-bottom: 24px;">
                <label class="form-label">Adresse</label>
                <p style="font-size: 16px; color: #1A1A1A; font-weight: 500;">
                    <c:choose>
                        <c:when test="${not empty employe.adresse}">
                            ${employe.adresse}
                        </c:when>
                        <c:otherwise>
                            <span style="color: #999999;">Non renseignée</span>
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>

            <div>
                <label class="form-label">Date de naissance</label>
                <c:choose>
                    <c:when test="${canViewConfidential}">
                        <p style="font-size: 16px; color: #1A1A1A; font-weight: 500;">
                            <c:choose>
                                <c:when test="${not empty employe.dateNaissance}">
                                    ${employe.dateNaissance.dayOfMonth}/${employe.dateNaissance.monthValue}/${employe.dateNaissance.year}
                                </c:when>
                                <c:otherwise>
                                    <span style="color: #999999;">Non renseignée</span>
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <p style="font-size: 16px; color: #999999; font-style: italic;">
                            <i class="fas fa-lock" style="margin-right: 8px;"></i>Confidentiel
                        </p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Informations professionnelles -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-briefcase" style="margin-right: 12px;"></i>Informations professionnelles
        </div>
        <div style="padding: 32px;">
            <div style="margin-bottom: 24px;">
                <label class="form-label">Date d'embauche</label>
                <p style="font-size: 16px; color: #1A1A1A; font-weight: 500;">
                    ${employe.dateEmbauche.dayOfMonth}/${employe.dateEmbauche.monthValue}/${employe.dateEmbauche.year}
                </p>
            </div>

            <div style="margin-bottom: 24px;">
                <label class="form-label">Manager</label>
                <c:set var="managerObj" value="${employe.getManager()}" />
                <p style="font-size: 16px; color: #1A1A1A; font-weight: 500;">
                    <c:choose>
                        <c:when test="${not empty managerObj}">
                            <a href="${pageContext.request.contextPath}/app/employes?action=show&id=${managerObj.id}"
                               style="color: #C5A572; text-decoration: none;">
                                ${managerObj.prenom} ${managerObj.nom}
                            </a>
                        </c:when>
                        <c:otherwise>
                            <span style="color: #999999;">Aucun manager</span>
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>

            <div style="margin-bottom: 24px;">
                <label class="form-label">Statut</label>
                <p>
                    <c:choose>
                        <c:when test="${employe.statut == 'ACTIF'}">
                            <span class="badge-elegant badge-success">Actif</span>
                        </c:when>
                        <c:when test="${employe.statut == 'SUSPENDU'}">
                            <span class="badge-elegant badge-warning">Suspendu</span>
                        </c:when>
                        <c:when test="${employe.statut == 'DEMISSION'}">
                            <span class="badge-elegant badge-info">Démission</span>
                        </c:when>
                        <c:when test="${employe.statut == 'LICENCIE'}">
                            <span class="badge-elegant badge-danger">Licencié</span>
                        </c:when>
                        <c:when test="${employe.statut == 'RETRAITE'}">
                            <span class="badge-elegant badge-info">Retraité</span>
                        </c:when>
                    </c:choose>
                </p>
            </div>

            <div>
                <label class="form-label">Rôle utilisateur</label>
                <p style="font-size: 16px; color: #1A1A1A; font-weight: 500;">
                    <c:choose>
                        <c:when test="${not empty utilisateurAssocie}">
                            <c:choose>
                                <c:when test="${utilisateurAssocie.role == 'ADMIN'}">
                                    <span class="badge-elegant badge-danger">Administrateur</span>
                                </c:when>
                                <c:when test="${utilisateurAssocie.role == 'RH'}">
                                    <span class="badge-elegant badge-warning">Ressources Humaines</span>
                                </c:when>
                                <c:when test="${utilisateurAssocie.role == 'CHEF_DEPT'}">
                                    <span class="badge-elegant badge-info">Chef de Département</span>
                                </c:when>
                                <c:when test="${utilisateurAssocie.role == 'CHEF_PROJET'}">
                                    <span class="badge-elegant badge-primary">Chef de Projet</span>
                                </c:when>
                                <c:when test="${utilisateurAssocie.role == 'EMPLOYE'}">
                                    <span class="badge-elegant badge-secondary">Employé</span>
                                </c:when>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <span style="color: #999999;">Aucun compte utilisateur</span>
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </div>
</div>

<!-- Actions rapides (visible uniquement si propre profil ou RH/Admin) -->
<c:if test="${canViewConfidential}">
    <div class="card">
        <div class="card-header">
            <i class="fas fa-lightning-bolt" style="margin-right: 12px;"></i>Actions rapides
        </div>
        <div style="padding: 32px; display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px;">
            <a href="${pageContext.request.contextPath}/app/fiches-paie?employeId=${employe.id}" 
               class="btn btn-outline">
                <i class="fas fa-file-invoice-dollar" style="margin-right: 8px;"></i>Voir les fiches de paie
            </a>
            <a href="${pageContext.request.contextPath}/app/conges-absences?employeId=${employe.id}" 
               class="btn btn-outline">
                <i class="fas fa-calendar-check" style="margin-right: 8px;"></i>Voir les congés
            </a>
            <a href="${pageContext.request.contextPath}/app/projets?employe=${employe.id}" 
               class="btn btn-outline">
                <i class="fas fa-project-diagram" style="margin-right: 8px;"></i>Voir les projets
            </a>
        </div>
    </div>
</c:if>

<style>
    /* Avatar dans le style du thème */
    .avatar-lg {
        width: 72px;
        height: 72px;
        border-radius: 50%;
        background: linear-gradient(135deg, #C5A572 0%, #D4B58D 100%);
        color: #fff;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 24px;
        letter-spacing: 1px;
    }

    /* Empêche les cartes de forcer un débordement horizontal */
    .card {
        min-width: 0;
    }
</style>

<jsp:include page="../layout/footer.jsp" />
