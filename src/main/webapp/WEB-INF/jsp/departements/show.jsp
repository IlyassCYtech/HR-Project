<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="${departement.nom} - Système RH" />
    <jsp:param name="page" value="departements" />
</jsp:include>

<div class="header">
    <div style="display: flex; align-items: center; gap: 24px;">
        <div style="width: 80px; height: 80px; background: linear-gradient(135deg, #C5A572 0%, #A08555 100%); border-radius: 12px; display: flex; align-items: center; justify-content: center;">
            <i class="fas fa-building" style="font-size: 36px; color: white;"></i>
        </div>
        <div style="flex: 1;">
            <h1 style="margin-bottom: 8px;">${departement.nom}</h1>
            <p class="subtitle">
                Code: <span style="font-family: 'Courier New', monospace; color: #C5A572; font-weight: 600;">DEPT-${departement.id}</span>
            </p>
        </div>
        <div style="display: flex; gap: 12px;">
            <a href="${pageContext.request.contextPath}/app/departements?action=edit&id=${departement.id}" class="btn btn-primary">
                <i class="fas fa-edit" style="margin-right: 8px;"></i>Modifier
            </a>
            <a href="${pageContext.request.contextPath}/app/departements?action=delete&id=${departement.id}" 
               class="btn btn-danger"
               onclick="return confirm('⚠️ ATTENTION ⚠️\n\nVoulez-vous vraiment SUPPRIMER définitivement ce département ?\n\n• ${nbEmployes} employé(s) seront désaffectés\n• Le département sera supprimé de la base de données\n• Cette action est IRRÉVERSIBLE\n\nConfirmer la suppression ?');">
                <i class="fas fa-trash" style="margin-right: 8px;"></i>Supprimer
            </a>
            <a href="${pageContext.request.contextPath}/app/departements" class="btn btn-secondary">
                <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
            </a>
        </div>
    </div>
</div>

<!-- Statistiques -->
<div class="stats-grid" style="grid-template-columns: repeat(3, 1fr); margin-bottom: 32px;">
    <div class="stat-card">
        <i class="fas fa-users"></i>
        <h4>${nbEmployes}</h4>
        <p>Employés</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-euro-sign"></i>
        <h4>
            <c:choose>
                <c:when test="${not empty departement.budget && departement.budget > 0}">
                    <fmt:formatNumber value="${departement.budget}" pattern="#,##0"/> €
                </c:when>
                <c:otherwise>Non défini</c:otherwise>
            </c:choose>
        </h4>
        <p>Budget annuel</p>
    </div>
    <div class="stat-card">
        <i class="fas fa-project-diagram"></i>
        <h4>${nbProjets}</h4>
        <p>Projets</p>
    </div>
</div>

<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 24px;">
    <!-- Informations générales -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-info-circle" style="margin-right: 12px;"></i>Informations générales
        </div>
        <div style="padding: 32px;">
            <div style="margin-bottom: 24px;">
                <label class="form-label">CODE</label>
                <p style="font-family: 'Courier New', monospace; font-size: 18px; color: #C5A572; font-weight: 600;">
                    DEPT-${departement.id}
                </p>
            </div>

            <div style="margin-bottom: 24px;">
                <label class="form-label">DESCRIPTION</label>
                <p style="font-size: 14px; color: #1A1A1A; line-height: 1.6;">
                    <c:choose>
                        <c:when test="${not empty departement.description}">
                            ${departement.description}
                        </c:when>
                        <c:otherwise>
                            <span style="color: #999999;">Aucune description</span>
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>

            <div>
                <label class="form-label">RESPONSABLE</label>
                <c:choose>
                    <c:when test="${not empty departement.chefDepartement}">
                        <div style="display: flex; align-items: center; gap: 12px; margin-top: 12px;">
                            <div class="avatar">
                                ${departement.chefDepartement.prenom.substring(0,1)}${departement.chefDepartement.nom.substring(0,1)}
                            </div>
                            <div>
                                <div style="font-weight: 600; color: #1A1A1A;">
                                    <a href="${pageContext.request.contextPath}/app/employes?action=show&id=${departement.chefDepartement.id}"
                                       style="color: #C5A572; text-decoration: none;">
                                        ${departement.chefDepartement.prenom} ${departement.chefDepartement.nom}
                                    </a>
                                </div>
                                <div style="font-size: 12px; color: #666666;">
                                    ${departement.chefDepartement.poste}
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p style="color: #999999; margin-top: 8px;">Aucun responsable assigné</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Budget -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-chart-line" style="margin-right: 12px;"></i>Budget
        </div>
        <div style="padding: 32px;">
            <div style="margin-bottom: 24px;">
                <label class="form-label">BUDGET ANNUEL</label>
                <p style="font-size: 28px; color: #1A1A1A; font-weight: 600; margin-top: 8px;">
                    <c:choose>
                        <c:when test="${not empty departement.budget && departement.budget > 0}">
                            <fmt:formatNumber value="${departement.budget}" pattern="#,##0"/> €
                        </c:when>
                        <c:otherwise>
                            <span style="color: #999999; font-size: 16px;">Non défini</span>
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>

            <c:if test="${not empty departement.budget && departement.budget > 0}">
                <div style="padding: 16px; background: #F5F5F5; border-radius: 8px;">
                    <div style="margin-bottom: 8px;">
                        <label class="form-label">BUDGET MENSUEL MOYEN</label>
                        <p style="font-size: 18px; color: #1A1A1A; font-weight: 600;">
                            <fmt:formatNumber value="${departement.budget / 12}" pattern="#,##0"/> €
                        </p>
                    </div>
                    <div>
                        <label class="form-label">BUDGET PAR EMPLOYÉ</label>
                        <p style="font-size: 18px; color: #1A1A1A; font-weight: 600;">
                            <c:choose>
                                <c:when test="${nbEmployes > 0}">
                                    <fmt:formatNumber value="${departement.budget / nbEmployes}" pattern="#,##0"/> €
                                </c:when>
                                <c:otherwise>N/A</c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</div>

<!-- Liste des employés -->
<div class="card">
    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <i class="fas fa-users" style="margin-right: 12px;"></i>Employés du département
        </div>
        <button onclick="document.getElementById('modalAffecterEmploye').style.display='flex'" class="btn btn-primary btn-sm">
            <i class="fas fa-user-plus" style="margin-right: 8px;"></i>Affecter un employé
        </button>
    </div>
    
    <c:choose>
        <c:when test="${not empty employes}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>EMPLOYÉ</th>
                        <th>POSTE</th>
                        <th>GRADE</th>
                        <th>SALAIRE</th>
                        <th style="text-align: center;">STATUT</th>
                        <th style="text-align: center;">ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="emp" items="${employes}">
                        <tr>
                            <td>
                                <div style="display: flex; align-items: center; gap: 12px;">
                                    <div class="avatar">
                                        ${emp.prenom.substring(0,1)}${emp.nom.substring(0,1)}
                                    </div>
                                    <div>
                                        <div style="font-weight: 600; color: #1A1A1A;">
                                            ${emp.prenom} ${emp.nom}
                                        </div>
                                        <div style="font-size: 12px; color: #666666; font-family: 'Courier New', monospace;">
                                            ${emp.matricule}
                                        </div>
                                    </div>
                                </div>
                            </td>
                            <td>${emp.poste}</td>
                            <td>${emp.grade}</td>
                            <td style="font-weight: 600;">
                                <fmt:formatNumber value="${emp.salaireBase}" pattern="#,##0"/> €
                            </td>
                            <td style="text-align: center;">
                                <c:choose>
                                    <c:when test="${emp.statut == 'ACTIF'}">
                                        <span class="badge-elegant badge-success">Actif</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-elegant badge-secondary">${emp.statut}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <div style="display: flex; gap: 8px; justify-content: center;">
                                    <a href="${pageContext.request.contextPath}/app/employes?action=edit&id=${emp.id}" 
                                       class="btn btn-primary btn-sm" title="Modifier">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/employes?action=show&id=${emp.id}" 
                                       class="btn btn-outline btn-sm" title="Voir détails">
                                        <i class="fas fa-eye"></i>
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
                <i class="fas fa-users" style="font-size: 48px; color: #E0E0E0; margin-bottom: 16px;"></i>
                <p style="color: #666666;">Aucun employé dans ce département</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- Modal pour affecter un employé existant -->
<div id="modalAffecterEmploye" style="display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); align-items: center; justify-content: center; z-index: 9999;">
    <div class="card" style="width: 500px; max-height: 80vh; overflow-y: auto;">
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <i class="fas fa-user-plus" style="margin-right: 12px;"></i>Affecter un employé au département
            </div>
            <button onclick="document.getElementById('modalAffecterEmploye').style.display='none'" style="background: none; border: none; font-size: 24px; color: #666; cursor: pointer;">×</button>
        </div>
        <form action="${pageContext.request.contextPath}/app/departements" method="POST" style="padding: 24px;">
            <input type="hidden" name="action" value="affecterEmploye">
            <input type="hidden" name="departementId" value="${departement.id}">
            
            <div style="margin-bottom: 24px;">
                <label class="form-label">EMPLOYÉ <span style="color: #DC3545;">*</span></label>
                <select name="employeId" class="form-control" required style="padding: 12px; font-size: 14px;">
                    <option value="">-- Sélectionner un employé --</option>
                    <c:forEach var="emp" items="${employesSansService}">
                        <option value="${emp.id}">
                            ${emp.matricule} - ${emp.prenom} ${emp.nom}
                            <c:if test="${not empty emp.poste}"> (${emp.poste})</c:if>
                        </option>
                    </c:forEach>
                </select>
                <small style="color: #666; font-size: 12px; display: block; margin-top: 8px;">
                    <i class="fas fa-info-circle"></i> Seuls les employés sans département sont affichés
                </small>
            </div>

            <div style="display: flex; gap: 12px; justify-content: flex-end;">
                <button type="button" onclick="document.getElementById('modalAffecterEmploye').style.display='none'" class="btn btn-secondary">
                    Annuler
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-check" style="margin-right: 8px;"></i>Affecter
                </button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../layout/footer.jsp" />
