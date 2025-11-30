<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Gestion des Employés - Système RH" />
    <jsp:param name="page" value="employes" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1>Gestion des Employés</h1>
            <p class="subtitle">Liste complète et recherche avancée</p>
        </div>
        <c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH' }">
            <div style="display: flex; gap: 12px;">
                <a href="${pageContext.request.contextPath}/app/employes?action=generateCredentials" class="btn btn-secondary">
                    <i class="fas fa-key" style="margin-right: 8px;"></i>Générer identifiants
                </a>
                <a href="${pageContext.request.contextPath}/app/employes?action=add" class="btn btn-primary">
                    <i class="fas fa-user-plus" style="margin-right: 8px;"></i>Nouvel employé
                </a>
            </div>
        </c:if>
    </div>
</div>

<!-- Messages -->
<c:if test="${not empty error}">
    <div class="alert alert-danger">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>${error}
    </div>
</c:if>

<c:if test="${not empty success}">
    <div class="alert alert-success">
        <i class="fas fa-check-circle" style="margin-right: 8px;"></i>${success}
    </div>
</c:if>

<!-- Filtres de recherche -->
<div class="card" style="margin-bottom: 24px;">
    <div class="card-header">
        <i class="fas fa-filter" style="margin-right: 12px;"></i>Filtres de recherche
    </div>
    
    <form method="GET" action="${pageContext.request.contextPath}/app/employes">
        <div class="grid-3" style="padding: 0 32px; margin-top: 24px;">
            <div class="form-group">
                <label class="form-label">Nom / Prénom</label>
                <input type="text" class="form-control" name="search" value="${currentSearch}" placeholder="Rechercher...">
            </div>
            
            <div class="form-group">
                <label class="form-label">Matricule</label>
                <input type="text" class="form-control" name="matricule" value="${currentMatricule}" placeholder="Ex: EMP001">
            </div>
            
            <div class="form-group">
                <label class="form-label">Département</label>
                <select class="form-control" name="departement">
                    <option value="">Tous les départements</option>
                    <c:forEach var="dept" items="${departements}">
                        <option value="${dept.id}" ${currentDepartement == dept.id ? 'selected' : ''}>
                            ${dept.nom}
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>
        
        <div class="grid-3" style="padding: 0 32px; margin-bottom: 24px;">
            <div class="form-group">
                <label class="form-label">Poste</label>
                <input type="text" class="form-control" name="poste" value="${currentPoste}" placeholder="Ex: Développeur">
            </div>
            
            <div class="form-group">
                <label class="form-label">Grade</label>
                <select class="form-control" name="grade">
                    <option value="">Tous les grades</option>
                    <option value="STAGIAIRE" ${currentGrade == 'STAGIAIRE' ? 'selected' : ''}>Stagiaire</option>
                    <option value="JUNIOR" ${currentGrade == 'JUNIOR' ? 'selected' : ''}>Junior</option>
                    <option value="SENIOR" ${currentGrade == 'SENIOR' ? 'selected' : ''}>Senior</option>
                    <option value="EXPERT" ${currentGrade == 'EXPERT' ? 'selected' : ''}>Expert</option>
                    <option value="MANAGER" ${currentGrade == 'MANAGER' ? 'selected' : ''}>Manager</option>
                    <option value="DIRECTEUR" ${currentGrade == 'DIRECTEUR' ? 'selected' : ''}>Directeur</option>
                </select>
            </div>
            
            <div class="form-group">
                <label class="form-label">Statut</label>
                <select class="form-control" name="statut">
                    <option value="">Tous les statuts</option>
                    <option value="ACTIF" ${currentStatut == 'ACTIF' ? 'selected' : ''}>Actif</option>
                    <option value="SUSPENDU" ${currentStatut == 'SUSPENDU' ? 'selected' : ''}>Suspendu</option>
                    <option value="DEMISSION" ${currentStatut == 'DEMISSION' ? 'selected' : ''}>Démission</option>
                    <option value="LICENCIE" ${currentStatut == 'LICENCIE' ? 'selected' : ''}>Licencié</option>
                    <option value="RETRAITE" ${currentStatut == 'RETRAITE' ? 'selected' : ''}>Retraité</option>
                </select>
            </div>
        </div>
        
        <div style="padding: 0 32px 32px; display: flex; gap: 12px;">
            <button type="submit" class="btn btn-primary">
                <i class="fas fa-search" style="margin-right: 8px;"></i>Rechercher
            </button>
            <a href="${pageContext.request.contextPath}/app/employes" class="btn btn-secondary">
                <i class="fas fa-times" style="margin-right: 8px;"></i>Réinitialiser
            </a>
        </div>
    </form>
</div>

<!-- Liste des employés -->
<div class="card">
    <div class="card-header">
        <i class="fas fa-list" style="margin-right: 12px;"></i>
        Liste des employés 
        <span class="badge-elegant badge-info" style="margin-left: 12px;">${employes.size()} résultats</span>
    </div>
    
    <c:choose>
        <c:when test="${empty employes}">
            <div style="text-align: center; padding: 80px 32px; color: #999999;">
                <i class="fas fa-users" style="font-size: 64px; margin-bottom: 24px; color: #E0E0E0;"></i>
                <p style="font-size: 16px; font-weight: 300;">Aucun employé trouvé</p>
                <p style="font-size: 13px;">Essayez de modifier vos critères de recherche</p>
            </div>
        </c:when>
        <c:otherwise>
            <div style="overflow-x: auto;">
                <table class="table-elegant">
                    <thead>
                        <tr>
                            <th>Matricule</th>
                            <th>Employé</th>
                            <th>Email</th>
                            <th>Département</th>
                            <th>Poste</th>
                            <th>Grade</th>
                            <th>Statut</th>
                            <th style="text-align: right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="employe" items="${employes}">
                            <tr>
                                <td>
                                    <strong style="font-family: 'Courier New', monospace; color: #C5A572;">
                                        ${employe.matricule}
                                    </strong>
                                </td>
                                <td>
                                    <div style="display: flex; align-items: center; gap: 12px;">
                                        <div class="avatar">
                                            ${employe.prenom.substring(0,1)}${employe.nom.substring(0,1)}
                                        </div>
                                        <div>
                                            <div style="font-weight: 600; color: #1A1A1A;">
                                                ${employe.prenom} ${employe.nom}
                                            </div>
                                            <div style="font-size: 12px; color: #999999;">
                                                ${employe.telephone}
                                            </div>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <c:if test="${not empty employe.email}">
                                        <a href="mailto:${employe.email}" style="color: #C5A572; text-decoration: none;">
                                            ${employe.email}
                                        </a>
                                    </c:if>
                                </td>
                                <td>
                                    <c:if test="${not empty employe.departement}">
                                        <span class="badge-elegant badge-info">
                                            ${employe.departement.nom}
                                        </span>
                                    </c:if>
                                </td>
                                <td>${employe.poste}</td>
                                <td>
                                    <span style="font-size: 12px; color: #666666;">
                                        ${employe.grade}
                                    </span>
                                </td>
                                <td>
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
                                        <c:otherwise>
                                            <span class="badge-elegant badge-warning">${employe.statut}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align: right; white-space: nowrap;">
                                    <a href="${pageContext.request.contextPath}/app/employes?action=show&id=${employe.id}" 
                                       class="btn btn-outline" 
                                       style="padding: 8px 12px; margin-right: 4px;"
                                       title="Voir les détails">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <%-- Bouton Modifier: visible pour ADMIN/RH ou pour l'employé lui-même --%>
                                    <c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH' or (utilisateur.employe != null and utilisateur.employe.id == employe.id)}">
	                                    <a href="${pageContext.request.contextPath}/app/employes?action=edit&id=${employe.id}" 
	                                       class="btn btn-secondary" 
	                                       style="padding: 8px 12px; margin-right: 4px;"
	                                       title="Modifier">
	                                        <i class="fas fa-edit"></i>
	                                    </a>
                                    </c:if>
                                    <%-- Boutons de gestion: seulement pour ADMIN/RH --%>
                                    <c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
	                                    <c:if test="${employe.statut == 'ACTIF'}">
	                                        <a href="${pageContext.request.contextPath}/app/employes?action=delete&id=${employe.id}" 
	                                           class="btn btn-secondary" 
	                                           style="padding: 8px 12px;"
	                                           title="Désactiver"
	                                           onclick="return confirm('Êtes-vous sûr de vouloir désactiver ${employe.prenom} ${employe.nom} ?')">
	                                            <i class="fas fa-user-times"></i>
	                                        </a>
	                                    </c:if>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../layout/footer.jsp" />