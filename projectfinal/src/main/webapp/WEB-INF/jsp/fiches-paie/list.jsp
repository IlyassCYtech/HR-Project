<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Gestion des Fiches de Paie - Système RH" />
    <jsp:param name="page" value="fiches-paie" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">Fiches de Paie</h1>
            <p class="subtitle">Gestion et génération des bulletins de salaire</p>
        </div>
        <c:if test="${sessionScope.utilisateur.role eq 'ADMIN' or sessionScope.utilisateur.role eq 'RH'}">
            <div style="display: flex; gap: 12px;">
                <a href="${pageContext.request.contextPath}/app/fiches-paie?action=generate" class="btn btn-primary">
                    <i class="fas fa-file-invoice-dollar" style="margin-right: 8px;"></i>Générer des fiches
                </a>
                <button type="button" class="btn btn-secondary" onclick="downloadAllZip()">
                    <i class="fas fa-file-archive" style="margin-right: 8px;"></i>Télécharger tout (ZIP)
                </button>
            </div>
        </c:if>
        <c:if test="${sessionScope.utilisateur.role ne 'ADMIN' and sessionScope.utilisateur.role ne 'RH'}">
            <button type="button" class="btn btn-primary" onclick="downloadMyFichesZip()">
                <i class="fas fa-file-archive" style="margin-right: 8px;"></i>Télécharger toutes mes fiches (ZIP)
            </button>
        </c:if>
    </div>
</div>

<c:if test="${not empty error}">
    <div class="alert alert-danger" role="alert" style="margin-bottom: 24px;">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>
        <strong>${error}</strong>
    </div>
</c:if>

<!-- Masse salariale (visible uniquement pour RH et Admin) -->
<c:if test="${not empty masseSalariale and (sessionScope.utilisateur.role eq 'ADMIN' or sessionScope.utilisateur.role eq 'RH')}">
    <div class="card" style="background: linear-gradient(135deg, #1A1A1A 0%, #2A2A2A 100%); color: white; margin-bottom: 24px;">
        <div style="padding: 32px;">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <label style="font-size: 11px; font-weight: 600; letter-spacing: 1.5px; text-transform: uppercase; color: #C5A572; margin-bottom: 8px; display: block;">
                        Masse salariale totale
                    </label>
                </div>
                <div style="display: flex; align-items: center; gap: 24px;">
                    <h2 style="font-family: 'Playfair Display', serif; font-size: 42px; font-weight: 700; margin: 0; color: white;">
                        <fmt:formatNumber value="${masseSalariale}" pattern="#,##0"/>
                    </h2>
                    <div style="width: 80px; height: 80px; background: rgba(197, 165, 114, 0.2); border-radius: 12px; display: flex; align-items: center; justify-content: center;">
                        <i class="fas fa-euro-sign" style="font-size: 36px; color: #C5A572;"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
</c:if>

<!-- Filtres -->
<div class="card" style="margin-bottom: 24px;">
    <div class="card-header">
        <i class="fas fa-filter" style="margin-right: 12px;"></i>Filtres
    </div>
    <form method="GET" action="${pageContext.request.contextPath}/app/fiches-paie">
        <div style="padding: 24px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;">
            <%-- Champ Employé : visible pour ADMIN/RH uniquement --%>
            <c:choose>
                <c:when test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
                    <div>
                        <label class="form-label" for="employeId">EMPLOYÉ</label>
                        <select id="employeId" name="employeId" class="form-control">
                            <option value="">Tous les employés</option>
                            <c:forEach var="emp" items="${employes}">
                                <option value="${emp.id}" 
                                        ${currentEmployeId == emp.id.toString() ? 'selected' : ''}
                                        data-nom="${emp.prenom} ${emp.nom}"
                                        data-dept="${not empty emp.departement ? emp.departement.id : ''}">
                                    ${emp.prenom} ${emp.nom}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </c:when>
                <c:otherwise>
                    <%-- Pour les autres rôles, champ caché avec leur propre ID --%>
                    <input type="hidden" name="employeId" value="${sessionScope.employeId}">
                    <div>
                        <label class="form-label">MES FICHES DE PAIE</label>
                        <div class="form-control" style="background: #F5F5F5; color: #666;">
                            <i class="fas fa-user" style="margin-right: 8px;"></i>
                            Mes fiches uniquement
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

            <div>
                <label class="form-label" for="mois">PÉRIODE (MOIS/ANNÉE)</label>
                <input type="month" 
                       id="mois" 
                       name="mois" 
                       class="form-control" 
                       value="${currentMois}"
                       placeholder="Sélectionner une période">
            </div>

            <div style="display: flex; align-items: flex-end;">
                <button type="submit" class="btn btn-primary" style="flex: 1; margin-right: 8px;">
                    <i class="fas fa-search" style="margin-right: 8px;"></i>Filtrer
                </button>
                <a href="${pageContext.request.contextPath}/app/fiches-paie" class="btn btn-secondary">
                    <i class="fas fa-times" style="margin-right: 8px;"></i>Réinitialiser
                </a>
            </div>
        </div>
    </form>
</div>

<!-- Liste des fiches -->
<div class="card">
    <div class="card-header">
        <i class="fas fa-file-invoice" style="margin-right: 12px;"></i>Liste des fiches de paie
    </div>
    
    <c:choose>
        <c:when test="${not empty fichesPaie}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>PÉRIODE</th>
                        <th>EMPLOYÉ</th>
                        <th>SALAIRE BASE</th>
                        <th>PRIMES</th>
                        <th>DÉDUCTIONS</th>
                        <th>NET À PAYER</th>
                        <th style="text-align: center;">ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="fiche" items="${fichesPaie}">
                        <tr>
                            <td>
                                <div style="font-family: 'Courier New', monospace; font-weight: 600; color: #C5A572;">
                                    ${fiche.mois}/${fiche.annee}
                                </div>
                            </td>
                            <td>
                                <div style="display: flex; align-items: center; gap: 12px;">
                                    <div class="avatar">
                                        ${fiche.employe.prenom.substring(0,1)}${fiche.employe.nom.substring(0,1)}
                                    </div>
                                    <div>
                                        <div style="font-weight: 600; color: #1A1A1A;">
                                            ${fiche.employe.prenom} ${fiche.employe.nom}
                                        </div>
                                        <div style="font-size: 12px; color: #666666;">
                                            ${fiche.employe.poste}
                                        </div>
                                    </div>
                                </div>
                            </td>
                            <td style="font-weight: 600;">
                                <fmt:formatNumber value="${fiche.salaireBase}" pattern="#,##0"/> €
                            </td>
                            <td style="color: #22C55E; font-weight: 600;">
                                + <fmt:formatNumber value="${fiche.primes}" pattern="#,##0"/> €
                            </td>
                            <td style="color: #EF4444; font-weight: 600;">
                                - <fmt:formatNumber value="${fiche.deductions}" pattern="#,##0"/> €
                            </td>
                            <td>
                                <div style="font-size: 16px; font-weight: 700; color: #1A1A1A;">
                                    <fmt:formatNumber value="${fiche.netAPayer}" pattern="#,##0"/> €
                                </div>
                            </td>
                            <td style="text-align: center;">
                                <div style="display: inline-flex; gap: 8px;">
                                    <a href="${pageContext.request.contextPath}/app/fiches-paie?action=show&id=${fiche.id}" 
                                       class="btn btn-outline btn-sm" title="Voir le bulletin">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/fiches-paie?action=pdf&id=${fiche.id}" 
                                       class="btn btn-secondary btn-sm" title="Télécharger PDF" target="_blank">
                                        <i class="fas fa-file-pdf"></i>
                                    </a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div style="padding: 80px 40px; text-align: center;">
                <i class="fas fa-file-invoice-dollar" style="font-size: 64px; color: #E0E0E0; margin-bottom: 24px;"></i>
                <h3 style="font-family: 'Playfair Display', serif; font-size: 24px; font-weight: 600; color: #1A1A1A; margin-bottom: 12px;">
                    Aucune fiche de paie
                </h3>
                <p style="color: #666666; margin-bottom: 24px;">
                    Générez des fiches de paie pour vos employés
                </p>
                <a href="${pageContext.request.contextPath}/app/fiches-paie?action=generate" class="btn btn-primary">
                    <i class="fas fa-plus" style="margin-right: 8px;"></i>Générer des fiches
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script>
    // Filtrage dynamique de la liste des employés
    document.addEventListener('DOMContentLoaded', function() {
        const searchInput = document.getElementById('searchEmployeList');
        const deptFilter = document.getElementById('filterDepartementList');
        const employeSelect = document.getElementById('employeId');
        
        if (searchInput && deptFilter && employeSelect) {
            const allOptions = Array.from(employeSelect.options).slice(1); // Exclure "Tous les employés"
            
            function filterEmployes() {
                const searchTerm = searchInput.value.toLowerCase().trim();
                const selectedDept = deptFilter.value;
                
                // Réinitialiser le select (garder l'option "Tous")
                employeSelect.innerHTML = '<option value="">Tous les employés</option>';
                
                allOptions.forEach(option => {
                    const nom = option.getAttribute('data-nom').toLowerCase();
                    const dept = option.getAttribute('data-dept');
                    
                    const matchesSearch = !searchTerm || nom.includes(searchTerm);
                    let matchesDept = false;
                    
                    if (!selectedDept) {
                        // Tous les départements
                        matchesDept = true;
                    } else if (selectedDept === 'none') {
                        // Aucun département (dept vide)
                        matchesDept = !dept || dept === '';
                    } else {
                        // Département spécifique
                        matchesDept = dept === selectedDept;
                    }
                    
                    if (matchesSearch && matchesDept) {
                        employeSelect.appendChild(option.cloneNode(true));
                    }
                });
            }
            
            searchInput.addEventListener('input', filterEmployes);
            deptFilter.addEventListener('change', filterEmployes);
        }
    });
    
    // Fonction pour télécharger toutes les fiches de l'employé connecté en ZIP
    function downloadMyFichesZip() {
        window.location.href = '${pageContext.request.contextPath}/app/fiches-paie?action=downloadZip&type=my';
    }
    
    // Fonction pour télécharger toutes les fiches en ZIP (RH/Admin)
    function downloadAllZip() {
        if (confirm('Voulez-vous télécharger toutes les fiches de paie en ZIP ?')) {
            window.location.href = '${pageContext.request.contextPath}/app/fiches-paie?action=downloadZip&type=all';
        }
    }
</script>

<jsp:include page="../layout/footer.jsp" />
