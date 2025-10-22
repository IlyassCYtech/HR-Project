<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="${empty departement ? 'Nouveau' : 'Modifier'} Département - Système RH" />
    <jsp:param name="page" value="departements" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">
                ${empty departement ? 'Nouveau département' : 'Modifier le département'}
            </h1>
            <p class="subtitle">
                ${empty departement ? 'Créer un nouveau département' : 'Mettre à jour les informations du département'}
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/app/departements" class="btn btn-secondary">
            <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
        </a>
    </div>
</div>

<c:if test="${not empty error}">
    <div class="alert alert-danger" role="alert">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>${error}
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/app/departements" method="post">
    <input type="hidden" name="action" value="${empty departement ? 'create' : 'update'}">
    <c:if test="${not empty departement}">
        <input type="hidden" name="id" value="${departement.id}">
    </c:if>

    <div class="card">
        <div class="card-header">
            <i class="fas fa-building" style="margin-right: 12px;"></i>Informations du département
        </div>
        <div style="padding: 32px;">
            <div style="margin-bottom: 24px;">
                <label class="form-label" for="nom">
                    NOM DU DÉPARTEMENT <span style="color: #C5A572;">*</span>
                </label>
                <input type="text" 
                       id="nom" 
                       name="nom" 
                       class="form-control" 
                       value="${departement.nom}" 
                       required 
                       placeholder="Ex: Ressources Humaines">
                <c:if test="${isEdit}">
                    <small style="color: #666666; display: block; margin-top: 4px;">
                        Code du département : <span style="font-family: 'Courier New', monospace; color: #C5A572; font-weight: 600;">DEPT-${departement.id}</span>
                    </small>
                </c:if>
            </div>

            <div style="margin-bottom: 24px;">
                <label class="form-label" for="description">
                    DESCRIPTION
                </label>
                <textarea id="description" 
                          name="description" 
                          class="form-control" 
                          rows="4" 
                          placeholder="Description des missions et responsabilités du département">${departement.description}</textarea>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
                <div>
                    <label class="form-label" for="chefDepartementId">
                        RESPONSABLE
                    </label>
                    <select id="chefDepartementId" name="chefDepartementId" class="form-control">
                        <option value="">-- Sélectionner un responsable --</option>
                        <c:forEach var="emp" items="${employes}">
                            <option value="${emp.id}" ${not empty departement.chefDepartement && departement.chefDepartement.id == emp.id ? 'selected' : ''}>
                                ${emp.prenom} ${emp.nom} - ${emp.poste}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label class="form-label" for="budget">
                        BUDGET ANNUEL (€)
                    </label>
                    <input type="number" 
                           id="budget" 
                           name="budget" 
                           class="form-control" 
                           value="${departement.budget}" 
                           min="0"
                           step="1000"
                           placeholder="Ex: 500000">
                </div>
            </div>
        </div>
    </div>

    <div style="display: flex; gap: 12px; margin-top: 24px;">
        <button type="submit" class="btn btn-primary">
            <i class="fas fa-save" style="margin-right: 8px;"></i>Enregistrer
        </button>
        <a href="${pageContext.request.contextPath}/app/departements" class="btn btn-secondary">
            Annuler
        </a>
    </div>
</form>

<jsp:include page="../layout/footer.jsp" />
