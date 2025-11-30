<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Générer Identifiants Employés - Système RH" />
    <jsp:param name="page" value="employes" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">Générer des Identifiants</h1>
            <p class="subtitle">Création de comptes utilisateur pour les employés</p>
        </div>
        <a href="${pageContext.request.contextPath}/app/employes" class="btn btn-secondary">
            <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
        </a>
    </div>
</div>

<c:if test="${not empty error}">
    <div class="alert alert-danger" role="alert">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>${error}
    </div>
</c:if>

<c:if test="${not empty success}">
    <div class="alert alert-success" role="alert">
        <i class="fas fa-check-circle" style="margin-right: 8px;"></i>${success}
    </div>
</c:if>

<!-- Statistiques -->
<div class="stats-grid" style="grid-template-columns: repeat(3, 1fr); margin-bottom: 32px;">
    <div class="stat-card">
        <i class="fas fa-users"></i>
        <h4>${totalEmployes}</h4>
        <p>Employés total</p>
    </div>
    <div class="stat-card" style="background: linear-gradient(135deg, #28A745 0%, #20883A 100%); color: white;">
        <i class="fas fa-user-check"></i>
        <h4>${employesAvecCompte}</h4>
        <p>Avec compte</p>
    </div>
    <div class="stat-card" style="background: linear-gradient(135deg, #FFA500 0%, #FF8C00 100%); color: white;">
        <i class="fas fa-user-plus"></i>
        <h4>${employesSansCompte}</h4>
        <p>Sans compte</p>
    </div>
</div>

<!-- Onglets -->
<div style="display: flex; gap: 0; margin-bottom: 24px; border-bottom: 2px solid #E0E0E0;">
    <button class="tab-button active" onclick="showTab('sans-compte')" id="tab-sans-compte">
        <i class="fas fa-user-plus" style="margin-right: 8px;"></i>Sans compte (${employesSansCompte})
    </button>
    <button class="tab-button" onclick="showTab('avec-compte')" id="tab-avec-compte">
        <i class="fas fa-key" style="margin-right: 8px;"></i>Réinitialiser mot de passe (${employesAvecCompte})
    </button>
</div>

<!-- Liste des employés sans compte -->
<div class="card tab-content" id="content-sans-compte">
    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <i class="fas fa-users-cog" style="margin-right: 12px;"></i>Employés sans compte utilisateur
        </div>
        <c:if test="${not empty employesSansCompte and employesSansCompte > 0}">
            <div style="display: flex; gap: 12px;">
                <form action="${pageContext.request.contextPath}/app/employes" method="post" style="display: inline;">
                    <input type="hidden" name="action" value="generateAllCredentials">
                    <button type="submit" class="btn btn-primary btn-sm" 
                            onclick="return confirm('Générer des identifiants pour TOUS les employés sans compte ?\n\n${employesSansCompte} compte(s) seront créés.');">
                        <i class="fas fa-magic" style="margin-right: 8px;"></i>Tout générer
                    </button>
                </form>
                <form action="${pageContext.request.contextPath}/app/employes" method="get" style="display: inline;">
                    <input type="hidden" name="action" value="exportCredentialsZIP">
                    <button type="submit" class="btn btn-secondary btn-sm" ${empty credentialsGenerated ? 'disabled' : ''}>
                        <i class="fas fa-file-archive" style="margin-right: 8px;"></i>Télécharger ZIP
                    </button>
                </form>
            </div>
        </c:if>
    </div>
    
    <c:choose>
        <c:when test="${not empty employesList and employesList.size() > 0}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>MATRICULE</th>
                        <th>NOM ET PRÉNOM</th>
                        <th>POSTE</th>
                        <th>DÉPARTEMENT</th>
                        <th>EMAIL</th>
                        <th style="text-align: center;">ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="emp" items="${employesList}">
                        <tr>
                            <td style="font-family: 'Courier New', monospace; font-weight: 600; color: #C5A572;">
                                ${emp.matricule}
                            </td>
                            <td>
                                <div style="font-weight: 600; color: #1A1A1A;">${emp.prenom} ${emp.nom}</div>
                            </td>
                            <td>${emp.poste}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty emp.departement}">
                                        ${emp.departement.nom}
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: #999;">Aucun</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty emp.email}">
                                        ${emp.email}
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: #999;">Non renseigné</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <form action="${pageContext.request.contextPath}/app/employes" method="post" style="display: inline;">
                                    <input type="hidden" name="action" value="generateCredential">
                                    <input type="hidden" name="employeId" value="${emp.id}">
                                    <button type="submit" class="btn btn-primary btn-sm" 
                                            onclick="return confirm('Générer un identifiant pour ${emp.prenom} ${emp.nom} ?');">
                                        <i class="fas fa-key"></i> Générer
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div style="padding: 80px 40px; text-align: center;">
                <i class="fas fa-check-circle" style="font-size: 64px; color: #28A745; margin-bottom: 24px;"></i>
                <h3 style="font-family: 'Playfair Display', serif; font-size: 24px; font-weight: 600; color: #1A1A1A; margin-bottom: 12px;">
                    Tous les employés ont un compte !
                </h3>
                <p style="color: #666666; margin-bottom: 24px;">
                    Aucun employé ne nécessite la création d'un compte utilisateur
                </p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- Liste des employés avec compte (réinitialisation) -->
<div class="card tab-content" id="content-avec-compte" style="display: none;">
    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <i class="fas fa-key" style="margin-right: 12px;"></i>Réinitialiser les mots de passe
        </div>
        <c:if test="${not empty employesAvecCompteList and employesAvecCompteList.size() > 0}">
            <div style="display: flex; gap: 12px;">
                <form action="${pageContext.request.contextPath}/app/employes" method="post" style="display: inline;">
                    <input type="hidden" name="action" value="resetAllPasswords">
                    <button type="submit" class="btn btn-warning btn-sm" 
                            onclick="return confirm('Réinitialiser les mots de passe de TOUS les employés avec compte ?\n\n${employesAvecCompte} mot(s) de passe seront réinitialisés.');">
                        <i class="fas fa-redo-alt" style="margin-right: 8px;"></i>Tout réinitialiser
                    </button>
                </form>
                <form action="${pageContext.request.contextPath}/app/employes" method="get" style="display: inline;">
                    <input type="hidden" name="action" value="exportCredentialsZIP">
                    <button type="submit" class="btn btn-secondary btn-sm" ${empty credentialsGenerated ? 'disabled' : ''}>
                        <i class="fas fa-file-archive" style="margin-right: 8px;"></i>Télécharger ZIP
                    </button>
                </form>
            </div>
        </c:if>
    </div>
    
    <c:choose>
        <c:when test="${not empty employesAvecCompteList and employesAvecCompteList.size() > 0}">
            <table class="table-elegant">
                <thead>
                    <tr>
                        <th>MATRICULE</th>
                        <th>NOM ET PRÉNOM</th>
                        <th>POSTE</th>
                        <th>IDENTIFIANT ACTUEL</th>
                        <th>EMAIL</th>
                        <th style="text-align: center;">ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="emp" items="${employesAvecCompteList}">
                        <tr>
                            <td style="font-family: 'Courier New', monospace; font-weight: 600; color: #C5A572;">
                                ${emp.matricule}
                            </td>
                            <td>
                                <div style="font-weight: 600; color: #1A1A1A;">${emp.prenom} ${emp.nom}</div>
                            </td>
                            <td>${emp.poste}</td>
                            <td style="font-family: 'Courier New', monospace; background: #F5F5F5; padding: 8px; border-radius: 4px;">
                                ${employeUsernames[emp.id]}
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty emp.email}">
                                        ${emp.email}
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: #999;">Non renseigné</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <form action="${pageContext.request.contextPath}/app/employes" method="post" style="display: inline;">
                                    <input type="hidden" name="action" value="resetPassword">
                                    <input type="hidden" name="employeId" value="${emp.id}">
                                    <button type="submit" class="btn btn-warning btn-sm" 
                                            onclick="return confirm('Réinitialiser le mot de passe de ${emp.prenom} ${emp.nom} ?\n\nUn nouveau mot de passe sera généré.');">
                                        <i class="fas fa-redo"></i> Réinitialiser
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div style="padding: 80px 40px; text-align: center;">
                <i class="fas fa-users" style="font-size: 64px; color: #999; margin-bottom: 24px;"></i>
                <h3 style="font-family: 'Playfair Display', serif; font-size: 24px; font-weight: 600; color: #1A1A1A; margin-bottom: 12px;">
                    Aucun employé avec compte
                </h3>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- Derniers identifiants générés (si disponibles) -->
<c:if test="${not empty recentCredentials}">
    <div class="card" style="margin-top: 24px;">
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <i class="fas fa-history" style="margin-right: 12px;"></i>Identifiants générés (Session en cours)
            </div>
            <form action="${pageContext.request.contextPath}/app/employes" method="get" style="display: inline;">
                <input type="hidden" name="action" value="exportCredentialsPDF">
                <button type="submit" class="btn btn-secondary btn-sm">
                    <i class="fas fa-file-pdf" style="margin-right: 8px;"></i>Télécharger PDF
                </button>
            </form>
        </div>
        
        <table class="table-elegant">
            <thead>
                <tr>
                    <th>EMPLOYÉ</th>
                    <th>IDENTIFIANT</th>
                    <th>MOT DE PASSE</th>
                    <th>DATE DE CRÉATION</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="cred" items="${recentCredentials}">
                    <tr>
                        <td style="font-weight: 600;">${cred.employeNom}</td>
                        <td style="font-family: 'Courier New', monospace; background: #F5F5F5; padding: 8px; border-radius: 4px;">
                            ${cred.username}
                        </td>
                        <td style="font-family: 'Courier New', monospace; background: #FFF3CD; padding: 8px; border-radius: 4px; font-weight: 600;">
                            ${cred.password}
                        </td>
                        <td>${cred.dateCreation}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        
        <div style="padding: 16px; background: #FFF3CD; border-left: 4px solid #FFA500; margin: 16px;">
            <p style="margin: 0; color: #856404; font-size: 14px;">
                <i class="fas fa-exclamation-triangle" style="margin-right: 8px;"></i>
                <strong>Important :</strong> Ces identifiants ne seront plus accessibles après cette session. 
                Pensez à les télécharger en PDF pour les transmettre aux employés.
            </p>
        </div>
    </div>
</c:if>

<style>
.tab-button {
    padding: 12px 24px;
    background: transparent;
    border: none;
    border-bottom: 3px solid transparent;
    cursor: pointer;
    font-size: 16px;
    font-weight: 600;
    color: #666;
    transition: all 0.3s ease;
}

.tab-button:hover {
    color: #1F3864;
    background: rgba(31, 56, 100, 0.05);
}

.tab-button.active {
    color: #1F3864;
    border-bottom-color: #C5A572;
}

.tab-content {
    display: none;
}

.tab-content.active {
    display: block;
}
</style>

<script>
function showTab(tabName) {
    // Masquer tous les contenus
    document.querySelectorAll('.tab-content').forEach(content => {
        content.style.display = 'none';
    });
    
    // Désactiver tous les boutons
    document.querySelectorAll('.tab-button').forEach(button => {
        button.classList.remove('active');
    });
    
    // Afficher le contenu sélectionné
    document.getElementById('content-' + tabName).style.display = 'block';
    
    // Activer le bouton sélectionné
    document.getElementById('tab-' + tabName).classList.add('active');
}

// Initialiser l'affichage (par défaut sur "sans compte")
document.addEventListener('DOMContentLoaded', function() {
    showTab('sans-compte');
});
</script>

<jsp:include page="../layout/footer.jsp" />
