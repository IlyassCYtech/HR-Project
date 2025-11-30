<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Gestion des Départements - Système RH" />
    <jsp:param name="page" value="departements" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">Départements</h1>
            <p class="subtitle">Gestion de l'organisation et des départements</p>
        </div>
       	<c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
	        <a href="${pageContext.request.contextPath}/app/departements?action=add" class="btn btn-primary">
	            <i class="fas fa-plus" style="margin-right: 8px;"></i>Nouveau département
	        </a>
        </c:if>
    </div>
</div>

<c:if test="${not empty message}">
    <div class="alert alert-success" role="alert">
        <i class="fas fa-check-circle" style="margin-right: 8px;"></i>${message}
    </div>
</c:if>

<c:if test="${not empty error}">
    <div class="alert alert-danger" role="alert">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>${error}
    </div>
</c:if>

<div class="card">
    <div class="card-header">
        <i class="fas fa-building" style="margin-right: 12px;"></i>Liste des départements
    </div>
    
    <c:choose>
        <c:when test="${not empty departements}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>CODE</th>
                        <th>NOM</th>
                        <th>RESPONSABLE</th>
                        <th style="text-align: center;">EMPLOYÉS</th>
                        <th>BUDGET ANNUEL</th>
                        <th style="text-align: center;">ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="dept" items="${departements}">
                        <tr>
                            <td>
                                <span style="font-family: 'Courier New', monospace; color: #C5A572; font-weight: 600; font-size: 14px;">
                                    DEPT-${dept.id}
                                </span>
                            </td>
                            <td>
                                <div style="font-weight: 600; color: #1A1A1A; margin-bottom: 2px;">
                                    ${dept.nom}
                                </div>
                                <c:if test="${not empty dept.description}">
                                    <div style="font-size: 12px; color: #666666; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                        ${dept.description}
                                    </div>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty dept.chefDepartement}">
                                        <div style="display: flex; align-items: center; gap: 12px;">
                                            <div class="avatar">
                                                ${dept.chefDepartement.prenom.substring(0,1)}${dept.chefDepartement.nom.substring(0,1)}
                                            </div>
                                            <div>
                                                <div style="font-weight: 500; color: #1A1A1A;">
                                                    ${dept.chefDepartement.prenom} ${dept.chefDepartement.nom}
                                                </div>
                                                <div style="font-size: 12px; color: #666666;">
                                                    ${dept.chefDepartement.poste}
                                                </div>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: #999999;">Non assigné</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <span style="display: inline-flex; align-items: center; justify-content: center; min-width: 36px; height: 36px; background: #F5F5F5; border-radius: 8px; font-weight: 600; color: #1A1A1A;">
                                    <c:out value="${employeCountsMap[dept.id] != null ? employeCountsMap[dept.id] : 0}" default="0"/>
                                </span>
                            </td>
                            <td>
                            
                                <c:choose>
                                	<c:when test="${utilisateur.role eq 'EMPLOYE' or utilisateur.role eq 'CHEF_PROJET'}">
								        <span style="color: #999999; font-style: italic;">Indisponible</span>
								    </c:when>
                                    <c:when test="${not empty dept.budget && dept.budget > 0}">
                                        <span style="font-weight: 600; color: #1A1A1A;">
                                            <fmt:formatNumber value="${dept.budget}" pattern="#,##0"/> €
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: #999999;">Non défini</span>
                                    </c:otherwise>
                                </c:choose>
                               
                            </td>
                            <td style="text-align: center;">
                                <div style="display: inline-flex; gap: 8px;">
                                    <a href="${pageContext.request.contextPath}/app/departements?action=show&id=${dept.id}" 
                                       class="btn btn-outline btn-sm" title="Voir les détails">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <c:if test="${utilisateur.role eq 'CHEF_DEPT' }">
	                                    <a href="${pageContext.request.contextPath}/app/departements?action=edit&id=${dept.id}" 
	                                       class="btn btn-secondary btn-sm" title="Modifier">
	                                        <i class="fas fa-edit"></i>
	                                    </a>
                                    </c:if>
                                    <c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH' }">
	                                    <a href="${pageContext.request.contextPath}/app/departements?action=delete&id=${dept.id}"
	                                       class="btn btn-danger btn-sm" title="Supprimer"
	                                       onclick="return confirm('⚠️ Supprimer définitivement le département ${dept.nom} ?');">
	                                        <i class="fas fa-trash"></i>
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
                <i class="fas fa-building" style="font-size: 64px; color: #E0E0E0; margin-bottom: 24px;"></i>
                <h3 style="font-family: 'Playfair Display', serif; font-size: 24px; font-weight: 600; color: #1A1A1A; margin-bottom: 12px;">
                    Aucun département
                </h3>
                <p style="color: #666666; margin-bottom: 24px;">
                    Créez votre premier département pour organiser vos équipes
                </p>
                <a href="${pageContext.request.contextPath}/app/departements?action=add" class="btn btn-primary">
                    <i class="fas fa-plus" style="margin-right: 8px;"></i>Créer un département
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../layout/footer.jsp" />
