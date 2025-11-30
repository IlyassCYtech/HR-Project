<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="layout/header.jsp">
    <jsp:param name="title" value="Mon Profil - Système RH" />
    <jsp:param name="page" value="profil" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">Mon Profil</h1>
            <p class="subtitle">Consultez vos informations personnelles et professionnelles</p>
        </div>
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

<div style="display: grid; grid-template-columns: 1fr 2fr; gap: 24px;">
    <!-- Carte Utilisateur -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-user-circle" style="margin-right: 12px;"></i>Compte Utilisateur
        </div>
        <div style="padding: 32px; text-align: center;">
            <div style="width: 120px; height: 120px; border-radius: 50%; background: linear-gradient(135deg, #C5A572 0%, #A08555 100%); display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; font-size: 48px; color: white; font-weight: 700;">
                ${utilisateur.username.substring(0,1).toUpperCase()}
            </div>
            
            <h3 style="font-family: 'Playfair Display', serif; font-size: 24px; margin-bottom: 8px; color: #1A1A1A;">
                ${utilisateur.username}
            </h3>
            
            <div style="display: inline-block; padding: 8px 16px; background: #C5A572; color: white; border-radius: 4px; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 24px;">
                ${utilisateur.role.libelle}
            </div>
            
            <div style="padding: 16px; background: #F5F5F5; border-radius: 8px; margin-top: 24px;">
                <div style="margin-bottom: 12px;">
                    <label class="form-label">NOM D'UTILISATEUR</label>
                    <p style="font-size: 14px; color: #1A1A1A; font-weight: 600;">
                        ${utilisateur.username}
                    </p>
                </div>
                <div>
                    <label class="form-label">RÔLE</label>
                    <p style="font-size: 14px; color: #1A1A1A; font-weight: 600;">
                        ${utilisateur.role.libelle}
                    </p>
                </div>
            </div>
        </div>
    </div>

    <!-- Informations Employé (si disponible) -->
    <c:choose>
        <c:when test="${not empty employe}">
            <div>
                <div class="card" style="margin-bottom: 24px;">
                    <div class="card-header">
                        <i class="fas fa-id-card" style="margin-right: 12px;"></i>Informations Personnelles
                    </div>
                    <div style="padding: 32px;">
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
                            <div>
                                <label class="form-label">MATRICULE</label>
                                <p style="font-family: 'Courier New', monospace; font-size: 18px; color: #C5A572; font-weight: 600;">
                                    ${employe.matricule}
                                </p>
                            </div>
                            
                            <div>
                                <label class="form-label">NOM COMPLET</label>
                                <p style="font-size: 16px; color: #1A1A1A; font-weight: 600;">
                                    ${employe.prenom} ${employe.nom}
                                </p>
                            </div>
                            
                            <div>
                                <label class="form-label">EMAIL</label>
                                <p style="font-size: 14px; color: #1A1A1A;">
                                    <i class="fas fa-envelope" style="color: #C5A572; margin-right: 8px;"></i>
                                    <a href="mailto:${employe.email}" style="color: #1A1A1A; text-decoration: none;">${employe.email}</a>
                                </p>
                            </div>
                            
                            <div>
                                <label class="form-label">TÉLÉPHONE</label>
                                <p style="font-size: 14px; color: #1A1A1A;">
                                    <i class="fas fa-phone" style="color: #C5A572; margin-right: 8px;"></i>
                                    ${employe.telephone}
                                </p>
                            </div>
                            
                            <div style="grid-column: 1 / -1;">
                                <label class="form-label">ADRESSE</label>
                                <p style="font-size: 14px; color: #1A1A1A; line-height: 1.6;">
                                    <i class="fas fa-map-marker-alt" style="color: #C5A572; margin-right: 8px;"></i>
                                    ${employe.adresse}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <i class="fas fa-briefcase" style="margin-right: 12px;"></i>Informations Professionnelles
                    </div>
                    <div style="padding: 32px;">
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
                            <div>
                                <label class="form-label">POSTE</label>
                                <p style="font-size: 16px; color: #1A1A1A; font-weight: 600;">
                                    ${employe.poste}
                                </p>
                            </div>
                            
                            <div>
                                <label class="form-label">GRADE</label>
                                <p style="font-size: 16px; color: #1A1A1A; font-weight: 600;">
                                    ${employe.grade}
                                </p>
                            </div>
                            
                            <div>
                                <label class="form-label">DÉPARTEMENT</label>
                                <p style="font-size: 14px; color: #1A1A1A;">
                                    <c:choose>
                                        <c:when test="${not empty employe.departement}">
                                            <a href="${pageContext.request.contextPath}/app/departements?action=show&id=${employe.departement.id}"
                                               style="color: #C5A572; text-decoration: none;">
                                                ${employe.departement.nom}
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #999999;">Aucun département</span>
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                            
                            <div>
                                <label class="form-label">STATUT</label>
                                <p>
                                    <c:choose>
                                        <c:when test="${employe.statut == 'ACTIF'}">
                                            <span class="badge-elegant badge-success">Actif</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-elegant badge-secondary">${employe.statut}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                            
                            <div>
							    <label class="form-label">DATE D'EMBAUCHE</label>
							    <p style="font-size: 14px; color: #1A1A1A;">
							        ${employe.dateEmbauche.dayOfMonth}/${employe.dateEmbauche.monthValue}/${employe.dateEmbauche.year}
							    </p>
							</div>
                            
                            <div>
                                <label class="form-label">ANCIENNETÉ</label>
                                <p style="font-size: 14px; color: #1A1A1A; font-weight: 600;">
                                    ${employe.anciennete} ans
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Boutons d'action -->
                <div style="margin-top: 24px; display: flex; gap: 12px;">
                    <a href="${pageContext.request.contextPath}/app/employes?action=show&id=${employe.id}" class="btn btn-primary">
                        <i class="fas fa-eye" style="margin-right: 8px;"></i>Voir mon profil complet
                    </a>
                    <a href="${pageContext.request.contextPath}/app/conges-absences" class="btn btn-secondary">
                        <i class="fas fa-calendar-alt" style="margin-right: 8px;"></i>Mes congés
                    </a>
                    <a href="${pageContext.request.contextPath}/app/fiches-paie" class="btn btn-secondary">
                        <i class="fas fa-file-invoice-dollar" style="margin-right: 8px;"></i>Mes fiches de paie
                    </a>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="card">
                <div style="padding: 60px 40px; text-align: center;">
                    <i class="fas fa-user-slash" style="font-size: 48px; color: #E0E0E0; margin-bottom: 16px;"></i>
                    <h3 style="font-family: 'Playfair Display', serif; font-size: 24px; margin-bottom: 12px;">
                        Aucun employé associé
                    </h3>
                    <p style="color: #666666; margin-bottom: 24px;">
                        Votre compte utilisateur n'est pas encore lié à une fiche employé.
                    </p>
                    <p style="font-size: 12px; color: #999999;">
                        Contactez le service RH pour plus d'informations.
                    </p>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- Changer le mot de passe -->
<div class="card" style="margin-top: 24px; max-width: 600px;">
    <div class="card-header">
        <i class="fas fa-key" style="margin-right: 12px;"></i>Modifier le mot de passe
    </div>
    <div style="padding: 32px;">
        <form id="passwordForm" action="${pageContext.request.contextPath}/app/change-password" method="post">
            <div style="margin-bottom: 24px;">
                <label for="currentPassword" class="form-label">MOT DE PASSE ACTUEL</label>
                <input type="password" id="currentPassword" name="currentPassword" class="form-control" 
                       placeholder="Entrez votre mot de passe actuel" required>
            </div>

            <div style="margin-bottom: 24px;">
                <label for="newPassword" class="form-label">NOUVEAU MOT DE PASSE</label>
                <input type="password" id="newPassword" name="newPassword" class="form-control" 
                       placeholder="Entrez le nouveau mot de passe (min. 6 caractères)" minlength="6" required>
                <small style="color: #666; font-size: 12px;">Le mot de passe doit contenir au moins 6 caractères</small>
            </div>

            <div style="margin-bottom: 24px;">
                <label for="confirmPassword" class="form-label">CONFIRMER LE MOT DE PASSE</label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                       placeholder="Confirmez le nouveau mot de passe" minlength="6" required>
                <small id="passwordMatchError" class="text-danger" style="display:none;">
                    <i class="fas fa-exclamation-triangle"></i> Les mots de passe ne correspondent pas
                </small>
            </div>

            <button type="submit" class="btn btn-primary">
                <i class="fas fa-save" style="margin-right: 8px;"></i>Modifier le mot de passe
            </button>
        </form>

        <script>
            document.addEventListener('DOMContentLoaded', function () {
                const form = document.getElementById('passwordForm');
                const newPass = document.getElementById('newPassword');
                const confirmPass = document.getElementById('confirmPassword');
                const errorMsg = document.getElementById('passwordMatchError');

                if (form) {
                    form.addEventListener('submit', function (event) {
                        if (newPass.value !== confirmPass.value) {
                            event.preventDefault();
                            errorMsg.style.display = 'block';
                        } else {
                            errorMsg.style.display = 'none';
                        }
                    });

                    // Validation instantanée
                    confirmPass.addEventListener('input', function () {
                        if (confirmPass.value && newPass.value !== confirmPass.value) {
                            errorMsg.style.display = 'block';
                        } else {
                            errorMsg.style.display = 'none';
                        }
                    });
                }
            });
        </script>
    </div>
</div>

<jsp:include page="layout/footer.jsp" />
