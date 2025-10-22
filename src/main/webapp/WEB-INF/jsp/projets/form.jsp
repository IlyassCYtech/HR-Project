<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="${empty projet ? 'Nouveau' : 'Modifier'} Projet - Système RH" />
    <jsp:param name="page" value="projets" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">
                ${empty projet ? 'Nouveau projet' : 'Modifier le projet'}
            </h1>
            <p class="subtitle">
                ${empty projet ? 'Créer un nouveau projet' : 'Mettre à jour les informations du projet'}
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/app/projets" class="btn btn-secondary">
            <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
        </a>
    </div>
</div>

<c:if test="${not empty error}">
    <div class="alert alert-danger" role="alert">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>${error}
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/app/projets" method="post">
    <input type="hidden" name="action" value="${empty projet ? 'create' : 'update'}">
    <c:if test="${not empty projet}">
        <input type="hidden" name="id" value="${projet.id}">
    </c:if>

    <!-- Informations générales -->
    <div class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <i class="fas fa-info-circle" style="margin-right: 12px;"></i>Informations générales
        </div>
        <div style="padding: 32px;">
            <div style="margin-bottom: 24px;">
                <label class="form-label" for="nom">
                    NOM DU PROJET <span style="color: #C5A572;">*</span>
                </label>
                <input type="text" 
                       id="nom" 
                       name="nom" 
                       class="form-control" 
                       value="${projet.nom}" 
                       required 
                       placeholder="Ex: Refonte du système RH">
            </div>

            <div style="margin-bottom: 24px;">
                <label class="form-label" for="description">
                    DESCRIPTION <span style="color: #C5A572;">*</span>
                </label>
                <textarea id="description" 
                          name="description" 
                          class="form-control" 
                          rows="4" 
                          required
                          placeholder="Description détaillée du projet et de ses objectifs">${projet.description}</textarea>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 24px;">
                <div>
                    <label class="form-label" for="statut">
                        STATUT <span style="color: #C5A572;">*</span>
                    </label>
                    <select id="statut" name="statut" class="form-control" required>
                        <option value="">-- Sélectionner --</option>
                        <option value="PLANIFIE" ${projet.statut.name() == 'PLANIFIE' ? 'selected' : ''}>Planifié</option>
                        <option value="EN_COURS" ${projet.statut.name() == 'EN_COURS' ? 'selected' : ''}>En cours</option>
                        <option value="TERMINE" ${projet.statut.name() == 'TERMINE' ? 'selected' : ''}>Terminé</option>
                        <option value="ANNULE" ${projet.statut.name() == 'ANNULE' ? 'selected' : ''}>Annulé</option>
                    </select>
                </div>

                <div>
                    <label class="form-label" for="dateDebut">
                        DATE DE DÉBUT <span style="color: #C5A572;">*</span>
                    </label>
                    <input type="date" 
                           id="dateDebut" 
                           name="dateDebut" 
                           class="form-control" 
                           value="${projet.dateDebut}" 
                           required>
                </div>

                <div>
                    <label class="form-label" for="dateFinPrevue">
                        DATE DE FIN PRÉVUE <span style="color: #C5A572;">*</span>
                    </label>
                    <input type="date" 
                           id="dateFinPrevue" 
                           name="dateFinPrevue" 
                           class="form-control" 
                           value="${projet.dateFinPrevue}" 
                           required>
                </div>
            </div>
        </div>
    </div>

    <!-- Équipe -->
    <div class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <i class="fas fa-users" style="margin-right: 12px;"></i>Équipe du projet
        </div>
        <div style="padding: 32px;">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
                <div>
                    <label class="form-label" for="chefProjetId">
                        CHEF DE PROJET <span style="color: #C5A572;">*</span>
                    </label>
                    <select id="chefProjetId" name="chefProjetId" class="form-control" required>
                        <option value="">-- Sélectionner un chef de projet --</option>
                        <c:forEach var="chef" items="${chefsProjet}">
                            <option value="${chef.id}" ${not empty projet.chefProjet && projet.chefProjet.id == chef.id ? 'selected' : ''}>
                                ${chef.prenom} ${chef.nom} - ${chef.poste}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label class="form-label" for="departementId">
                        DÉPARTEMENT
                    </label>
                    <select id="departementId" name="departementId" class="form-control">
                        <option value="">-- Sélectionner un département --</option>
                        <c:forEach var="dept" items="${departements}">
                            <option value="${dept.id}" ${projet.departement.id == dept.id ? 'selected' : ''}>
                                ${dept.nom}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div style="margin-top: 24px;">
                <label class="form-label" for="employeIds">
                    MEMBRES DE L'ÉQUIPE
                </label>
                <select id="employeIds" name="employeIds" class="form-control" multiple size="8">
                    <c:forEach var="emp" items="${employes}">
                        <c:set var="isSelected" value="false" />
                        <c:if test="${not empty projet.employes}">
                            <c:forEach var="projEmp" items="${projet.employes}">
                                <c:if test="${projEmp.employe.id == emp.id}">
                                    <c:set var="isSelected" value="true" />
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <option value="${emp.id}" ${isSelected ? 'selected' : ''}>
                            ${emp.prenom} ${emp.nom} - ${emp.poste}
                            <c:if test="${not empty emp.departement}">
                                (${emp.departement.nom})
                            </c:if>
                        </option>
                    </c:forEach>
                </select>
                <small style="display: block; margin-top: 8px; color: #666666; font-size: 12px;">
                    <i class="fas fa-info-circle"></i> Maintenez Ctrl (Windows) ou Cmd (Mac) pour sélectionner plusieurs employés
                </small>
            </div>
        </div>
    </div>

    <div style="display: flex; gap: 12px;">
        <button type="submit" class="btn btn-primary">
            <i class="fas fa-save" style="margin-right: 8px;"></i>Enregistrer
        </button>
        <a href="${pageContext.request.contextPath}/app/projets" class="btn btn-secondary">
            Annuler
        </a>
    </div>
</form>

<jsp:include page="../layout/footer.jsp" />
