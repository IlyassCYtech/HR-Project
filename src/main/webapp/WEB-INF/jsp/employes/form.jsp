<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="${isEdit ? 'Modifier' : 'Ajouter'} un Employé - Système RH" />
    <jsp:param name="page" value="employes" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1>
                <i class="fas ${isEdit ? 'fa-edit' : 'fa-user-plus'}" style="margin-right: 12px; color: #C5A572;"></i>
                ${isEdit ? 'Modifier' : 'Ajouter'} un employé
            </h1>
            <p class="subtitle">
                ${isEdit ? 'Modifier les informations de l\'employé' : 'Créer un nouveau profil employé'}
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/app/employes" class="btn btn-secondary">
            <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour à la liste
        </a>
    </div>
</div>

<!-- Messages -->
<c:if test="${not empty error}">
    <div class="alert alert-danger">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>${error}
    </div>
</c:if>

<!-- Formulaire -->
<form method="POST" action="${pageContext.request.contextPath}/app/employes">
    <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
    <c:if test="${isEdit}">
        <input type="hidden" name="id" value="${employe.id}">
    </c:if>

    <!-- Informations personnelles -->
    <div class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <i class="fas fa-user" style="margin-right: 12px;"></i>Informations personnelles
        </div>
        <div style="padding: 32px;">
            <div class="grid-3" style="margin-bottom: 24px;">
                <div class="form-group">
                    <label class="form-label">Matricule <span style="color: #C5A572;">*</span></label>
                    <input type="text" class="form-control" name="matricule" 
                           value="${employe.matricule}" required ${isEdit ? 'readonly' : ''}
                           placeholder="Ex: EMP001">
                    <c:if test="${!isEdit}">
                        <small style="color: #666; font-size: 12px;">Le matricule doit être unique</small>
                    </c:if>
                </div>

                <div class="form-group">
                    <label class="form-label">Nom <span style="color: #C5A572;">*</span></label>
                    <input type="text" class="form-control" name="nom" 
                           value="${employe.nom}" required placeholder="Nom de famille">
                </div>

                <div class="form-group">
                    <label class="form-label">Prénom <span style="color: #C5A572;">*</span></label>
                    <input type="text" class="form-control" name="prenom" 
                           value="${employe.prenom}" required placeholder="Prénom">
                </div>
            </div>

            <div class="grid-2" style="margin-bottom: 24px;">
                <div class="form-group">
                    <label class="form-label">Email <span style="color: #C5A572;">*</span></label>
                    <input type="email" class="form-control" name="email" 
                           value="${employe.email}" required placeholder="prenom.nom@entreprise.com">
                </div>

                <div class="form-group">
                    <label class="form-label">Téléphone</label>
                    <input type="tel" class="form-control" name="telephone" 
                           value="${employe.telephone}" placeholder="06 12 34 56 78">
                </div>
            </div>

            <div class="grid-2" style="margin-bottom: 24px;">
                <div class="form-group">
                    <label class="form-label">Date de naissance</label>
                    <input type="date" class="form-control" name="dateNaissance" 
                           value="${employe.dateNaissance}">
                </div>

                <div class="form-group">
                    <label class="form-label">Date d'embauche <span style="color: #C5A572;">*</span></label>
                    <input type="date" class="form-control" name="dateEmbauche" 
                           value="${employe.dateEmbauche}" required>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">Adresse</label>
                <textarea class="form-control" name="adresse" rows="3" 
                          placeholder="Adresse complète">${employe.adresse}</textarea>
            </div>
        </div>
    </div>

    <!-- Informations professionnelles -->
    <div class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <i class="fas fa-briefcase" style="margin-right: 12px;"></i>Informations professionnelles
        </div>
        <div style="padding: 32px;">
            <div class="grid-2" style="margin-bottom: 24px;">
                <div class="form-group">
                    <label class="form-label">Poste <span style="color: #C5A572;">*</span></label>
                    <input type="text" class="form-control" name="poste" 
                           value="${employe.poste}" required placeholder="Ex: Développeur, Manager...">
                </div>

                <div class="form-group">
                    <label class="form-label">Grade</label>
                    <select class="form-control" name="grade">
                        <option value="">-- Sélectionner un grade --</option>
                        <option value="STAGIAIRE" ${employe.grade == 'STAGIAIRE' ? 'selected' : ''}>Stagiaire</option>
                        <option value="JUNIOR" ${employe.grade == 'JUNIOR' ? 'selected' : ''}>Junior</option>
                        <option value="SENIOR" ${employe.grade == 'SENIOR' ? 'selected' : ''}>Senior</option>
                        <option value="EXPERT" ${employe.grade == 'EXPERT' ? 'selected' : ''}>Expert</option>
                        <option value="MANAGER" ${employe.grade == 'MANAGER' ? 'selected' : ''}>Manager</option>
                        <option value="DIRECTEUR" ${employe.grade == 'DIRECTEUR' ? 'selected' : ''}>Directeur</option>
                    </select>
                </div>
            </div>

            <div class="grid-3" style="margin-bottom: 24px;">
                <div class="form-group">
                    <label class="form-label">Salaire de base <span style="color: #C5A572;">*</span></label>
                    <div style="display: flex; align-items: stretch;">
                        <input type="number" class="form-control" name="salaire" 
                               value="${employe.salaireBase}" step="0.01" required 
                               style="border-right: none;" placeholder="0.00">
                        <span style="border: 1px solid #E0E0E0; padding: 14px 16px; background: #FAFAFA; border-left: none; font-size: 14px; color: #666666;">€</span>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label">Département</label>
                    <select class="form-control" name="departementId">
                        <option value="">-- Aucun département --</option>
                        <c:forEach var="dept" items="${departements}">
                            <option value="${dept.id}" 
                                    ${employe.departement != null && employe.departement.id == dept.id ? 'selected' : ''}>
                                ${dept.nom}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label">Manager</label>
                    <select class="form-control" name="managerId">
                        <option value="">-- Aucun --</option>
                        <c:forEach var="mgr" items="${managers}">
                            <option value="${mgr.id}" 
                                    ${employe.manager != null && employe.manager.id == mgr.id ? 'selected' : ''}>
                                ${mgr.prenom} ${mgr.nom}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">Statut <span style="color: #C5A572;">*</span></label>
                <select class="form-control" name="statut" required>
                    <option value="ACTIF" ${employe.statut == 'ACTIF' || empty employe.statut ? 'selected' : ''}>Actif</option>
                    <option value="SUSPENDU" ${employe.statut == 'SUSPENDU' ? 'selected' : ''}>Suspendu</option>
                    <option value="DEMISSION" ${employe.statut == 'DEMISSION' ? 'selected' : ''}>Démission</option>
                    <option value="LICENCIE" ${employe.statut == 'LICENCIE' ? 'selected' : ''}>Licencié</option>
                    <option value="RETRAITE" ${employe.statut == 'RETRAITE' ? 'selected' : ''}>Retraité</option>
                </select>
            </div>
        </div>
    </div>

    <!-- Boutons d'action -->
    <div class="card">
        <div style="padding: 32px; display: flex; justify-content: space-between; gap: 12px;">
            <a href="${pageContext.request.contextPath}/app/employes" class="btn btn-secondary">
                <i class="fas fa-times" style="margin-right: 8px;"></i>Annuler
            </a>
            <button type="submit" class="btn btn-primary">
                <i class="fas fa-save" style="margin-right: 8px;"></i>${isEdit ? 'Modifier' : 'Créer'} l'employé
            </button>
        </div>
    </div>
</form>

<jsp:include page="../layout/footer.jsp" />
