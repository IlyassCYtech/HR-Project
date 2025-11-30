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

<!-- Sécurité : accès réservé RH, ADMIN ou l'employé lui-même -->
<c:if test="${canViewEmploye}">
<!-- Formulaire -->
<form id="employeForm" method="POST" action="${pageContext.request.contextPath}/app/employes">
    <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
    <!-- Token CSRF pour la sécurité -->
    <input type="hidden" name="csrfToken" value="${csrfToken}">
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
                    <input type="text" class="form-control" name="matricule" id="matricule"
                           value="${employe.matricule}" required ${isEdit ? 'readonly' : ''}
                           placeholder="Ex: EMP001" pattern="[A-Za-z0-9_-]{3,20}"
                           title="Le matricule doit contenir entre 3 et 20 caractères alphanumériques">
                    <c:if test="${!isEdit}">
                        <small style="color: #666; font-size: 12px;">Le matricule doit être unique (3-20 caractères)</small>
                    </c:if>
                </div>

                <div class="form-group">
                    <label class="form-label">Nom <span style="color: #C5A572;">*</span></label>
                    <input type="text" class="form-control" name="nom" id="nom"
                           value="${employe.nom}" required placeholder="Nom de famille"
                           minlength="2" maxlength="50" ${isSelfEdit ? 'readonly' : ''}>
                    <small id="nomError" class="text-danger" style="display:none;">Le nom ne doit contenir que des lettres.</small>
                </div>

                <div class="form-group">
                    <label class="form-label">Prénom <span style="color: #C5A572;">*</span></label>
                    <input type="text" class="form-control" name="prenom" id="prenom"
                           value="${employe.prenom}" required placeholder="Prénom"
                           minlength="2" maxlength="50" ${isSelfEdit ? 'readonly' : ''}>
                    <small id="prenomError" class="text-danger" style="display:none;">Le prénom ne doit contenir que des lettres.</small>
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
                    <input type="tel" class="form-control" name="telephone" id="telephone"
                           value="${employe.telephone}" placeholder="06 12 34 56 78">
                    <small id="telephoneError" class="text-danger" style="display:none;">Numéro de téléphone invalide (10 chiffres minimum).</small>
                </div>
            </div>

            <div class="grid-2" style="margin-bottom: 24px;">
                <div class="form-group">
                    <label class="form-label">Date de naissance</label>
                    <input type="date" class="form-control" name="dateNaissance" id="dateNaissance"
                           value="${employe.dateNaissance}" max="2007-11-31" min="1950-01-01"
                           ${isSelfEdit ? 'readonly' : ''}>
                    <small style="color: #666; font-size: 12px;">L'employé doit avoir au moins 18 ans</small>
                    <small id="ageError" class="text-danger" style="display:none;">L'employé doit être majeur (18 ans) à la date d'embauche.</small>
                </div>

                <div class="form-group">
                    <label class="form-label">Date d'embauche <span style="color: #C5A572;">*</span></label>
                    <input type="date" class="form-control" name="dateEmbauche" id="dateEmbauche"
                           value="${employe.dateEmbauche}" required ${isSelfEdit ? 'readonly' : ''}>
                    <small id="hireDateError" class="text-danger" style="display:none;"></small>
                </div>
            </div>
            
            <div class="form-group" style="margin-bottom: 24px;">
                <label class="form-label">Date de fin de contrat</label>
                <input type="date" class="form-control" name="dateFin" id="dateFin"
                       value="${employe.dateFin}" ${isSelfEdit ? 'readonly' : ''}>
                <small style="color: #666; font-size: 12px;">Optionnel - Doit être postérieure à la date d'embauche</small>
                <small id="dateError" class="text-danger" style="display:none;">La date de fin doit être postérieure à la date d'embauche.</small>
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
                           value="${employe.poste}" required placeholder="Ex: Développeur, Manager..."
                           minlength="2" maxlength="100" ${isSelfEdit ? 'readonly' : ''}>
                </div>

                <div class="form-group">
                    <label class="form-label">Grade</label>
                    <select class="form-control" name="grade" ${isSelfEdit ? 'disabled' : ''}>
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
                               value="${employe.salaireBase}" step="0.01" min="0.01" max="999999.99" required 
                               style="border-right: none;" placeholder="0.00" ${isSelfEdit ? 'readonly' : ''}>
                        <span style="border: 1px solid #E0E0E0; padding: 14px 16px; background: #FAFAFA; border-left: none; font-size: 14px; color: #666666;">€</span>
                    </div>
                    <small style="color: #666; font-size: 12px;">Le salaire doit être positif</small>
                </div>

                <div class="form-group">
                    <label class="form-label">Département</label>
                    <select class="form-control" name="departementId" ${isSelfEdit ? 'disabled' : ''}>
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
                    <select class="form-control" name="managerId" ${isSelfEdit ? 'disabled' : ''}>
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
                <select class="form-control" name="statut" required ${isSelfEdit ? 'disabled' : ''}>
                    <option value="ACTIF" ${employe.statut == 'ACTIF' || empty employe.statut ? 'selected' : ''}>Actif</option>
                    <option value="SUSPENDU" ${employe.statut == 'SUSPENDU' ? 'selected' : ''}>Suspendu</option>
                    <option value="DEMISSION" ${employe.statut == 'DEMISSION' ? 'selected' : ''}>Démission</option>
                    <option value="LICENCIE" ${employe.statut == 'LICENCIE' ? 'selected' : ''}>Licencié</option>
                    <option value="RETRAITE" ${employe.statut == 'RETRAITE' ? 'selected' : ''}>Retraité</option>
                </select>
            </div>
        </div>
    </div>

    <!-- Section Rôle Utilisateur - Visible uniquement si l'employé a un compte utilisateur ET que l'utilisateur connecté est ADMIN ou RH -->
    <c:if test="${hasUtilisateur && isAdminOrRH}">
    <div class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <i class="fas fa-user-shield" style="margin-right: 12px;"></i>Compte Utilisateur
        </div>
        <div style="padding: 32px;">
            <div class="alert alert-info" style="margin-bottom: 20px;">
                <i class="fas fa-info-circle" style="margin-right: 8px;"></i>
                Cet employé possède un compte utilisateur. Vous pouvez modifier son rôle ci-dessous.
            </div>
            <div class="form-group">
                <label class="form-label">Rôle <span style="color: #C5A572;">*</span></label>
                <select name="utilisateurRole" class="form-control">
                    <option value="EMPLOYE" ${not empty employeUtilisateur && employeUtilisateur.role.name() == 'EMPLOYE' ? 'selected' : ''}>EMPLOYE</option>
                    <option value="RH" ${not empty employeUtilisateur && employeUtilisateur.role.name() == 'RH' ? 'selected' : ''}>RH</option>
                    <option value="ADMIN" ${not empty employeUtilisateur && employeUtilisateur.role.name() == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                    <option value="CHEF_DEPT" ${not empty employeUtilisateur && employeUtilisateur.role.name() == 'CHEF_DEPT' ? 'selected' : ''}>CHEF DE DÉPARTEMENT</option>
                    <option value="CHEF_PROJET" ${not empty employeUtilisateur && employeUtilisateur.role.name() == 'CHEF_PROJET' ? 'selected' : ''}>CHEF DE PROJET</option>
                </select>
                <small style="color: #666; font-size: 12px;">
                    Le rôle détermine les permissions de l'utilisateur dans l'application
                </small>
            </div>
        </div>
        </div>
    
    </c:if>

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
</c:if>

<!-- Message accès refusé -->
<c:if test="${!canViewEmploye}">
    <div class="alert alert-danger" style="margin-top:40px;">
        <i class="fas fa-lock" style="margin-right: 8px;"></i>
        Accès refusé : vous n'avez pas les droits pour modifier ce profil employé.
    </div>
</c:if>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('employeForm');
    if (!form) return;

    const nomInput = document.getElementById('nom');
    const prenomInput = document.getElementById('prenom');
    const telephoneInput = document.getElementById('telephone');
    const dateNaissanceInput = document.getElementById('dateNaissance');
    const dateEmbaucheInput = document.getElementById('dateEmbauche');
    const dateFinInput = document.getElementById('dateFin');

    const nomError = document.getElementById('nomError');
    const prenomError = document.getElementById('prenomError');
    const telephoneError = document.getElementById('telephoneError');
    const ageError = document.getElementById('ageError');
    const hireDateError = document.getElementById('hireDateError');
    const dateError = document.getElementById('dateError');

    // Patterns de validation stricts
    const nameRegex = /^[a-zA-ZÀ-ÿ][a-zA-ZÀ-ÿ\s\-']*[a-zA-ZÀ-ÿ]$/;
    const phoneCleanRegex = /[^0-9+]/g;
    const emailRegex = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/;

    function validateName(input, errorElement, fieldName) {
        if (!input || !errorElement) return true;
        if (input.hasAttribute('readonly') || input.disabled) {
            errorElement.style.display = 'none';
            return true;
        }

        const value = input.value.trim();
        if (value === '' && input.hasAttribute('required')) {
            errorElement.style.display = 'block';
            errorElement.textContent = 'Le ' + fieldName + ' est obligatoire.';
            return false;
        }
        if (value === '') {
            errorElement.style.display = 'none';
            return true;
        }
        
        // Vérifications strictes
        if (value.length < 2 || value.length > 50) {
            errorElement.style.display = 'block';
            errorElement.textContent = 'Le ' + fieldName + ' doit contenir entre 2 et 50 caractères.';
            return false;
        }
        
        // Pas de chiffres
        if (/\d/.test(value)) {
            errorElement.style.display = 'block';
            errorElement.textContent = 'Le ' + fieldName + ' ne doit pas contenir de chiffres.';
            return false;
        }
        
        // Pas de caractères spéciaux dangereux
        if (/<|>|&|"|\\/.test(value)) {
            errorElement.style.display = 'block';
            errorElement.textContent = 'Le ' + fieldName + ' contient des caractères non autorisés.';
            return false;
        }
        
        // Pattern strict
        if (!nameRegex.test(value)) {
            errorElement.style.display = 'block';
            errorElement.textContent = 'Le ' + fieldName + ' ne doit contenir que des lettres, espaces, tirets et apostrophes.';
            return false;
        }
        
        // Pas de séquences répétées
        if (/--/.test(value) || /''/.test(value) || /  /.test(value)) {
            errorElement.style.display = 'block';
            errorElement.textContent = 'Le ' + fieldName + ' contient des caractères répétés invalides.';
            return false;
        }
        
        errorElement.style.display = 'none';
        return true;
    }
    
    function validateEmail() {
        const emailInput = document.querySelector('input[name="email"]');
        if (!emailInput) return true;
        
        const value = emailInput.value.trim();
        if (value === '') {
            return false;
        }
        
        if (value.length > 150) {
            return false;
        }
        
        return emailRegex.test(value);
    }

    function validatePhone() {
        if (!telephoneInput || !telephoneError) return true;
        
        const value = telephoneInput.value.trim();
        if (value === '') {
            telephoneError.style.display = 'none';
            return true;
        }

        // Nettoyer : garder uniquement chiffres et +
        const cleaned = value.replace(phoneCleanRegex, '');
        
        // Formats français valides
        const frenchFormats = [
            /^0[1-9]\d{8}$/,           // 0612345678
            /^\+33[1-9]\d{8}$/,        // +33612345678
            /^0033[1-9]\d{8}$/         // 0033612345678
        ];
        
        let isValid = false;
        for (let pattern of frenchFormats) {
            if (pattern.test(cleaned)) {
                isValid = true;
                break;
            }
        }
        
        if (!isValid) {
            telephoneError.style.display = 'block';
            telephoneError.textContent = 'Format de téléphone invalide (format français attendu).';
            return false;
        }

        telephoneError.style.display = 'none';
        return true;
    }

    function validateAge() {
        if (!dateNaissanceInput || !dateEmbaucheInput || !ageError) return true;
        if (dateNaissanceInput.hasAttribute('readonly') || dateEmbaucheInput.hasAttribute('readonly')) {
            ageError.style.display = 'none';
            return true;
        }

        const birthDate = dateNaissanceInput.value;
        const hireDate = dateEmbaucheInput.value;

        if (!birthDate || !hireDate) {
            ageError.style.display = 'none';
            return true;
        }

        const birth = new Date(birthDate);
        const hire = new Date(hireDate);
        const age = hire.getFullYear() - birth.getFullYear();
        const monthDiff = hire.getMonth() - birth.getMonth();

        if (monthDiff < 0 || (monthDiff === 0 && hire.getDate() < birth.getDate())) {
            age--;
        }

        if (age < 18) {
            ageError.style.display = 'block';
            return false;
        }

        ageError.style.display = 'none';
        return true;
    }

    function validateDates() {
        if (!dateEmbaucheInput || !dateFinInput || !dateError) return true;
        if (dateFinInput.hasAttribute('readonly')) {
            dateError.style.display = 'none';
            return true;
        }

        const hireDate = dateEmbaucheInput.value;
        const endDate = dateFinInput.value;

        if (!hireDate || !endDate) {
            dateError.style.display = 'none';
            return true;
        }

        if (new Date(endDate) <= new Date(hireDate)) {
            dateError.style.display = 'block';
            return false;
        }

        dateError.style.display = 'none';
        return true;
    }

    // Validation en temps réel
    if (nomInput) nomInput.addEventListener('blur', function() { validateName(nomInput, nomError, 'nom'); });
    if (prenomInput) prenomInput.addEventListener('blur', function() { validateName(prenomInput, prenomError, 'prénom'); });
    if (telephoneInput) telephoneInput.addEventListener('blur', validatePhone);
    if (dateNaissanceInput) dateNaissanceInput.addEventListener('change', validateAge);
    if (dateEmbaucheInput) dateEmbaucheInput.addEventListener('change', function() {
        validateAge();
        validateDates();
    });
    if (dateFinInput) dateFinInput.addEventListener('change', validateDates);

    // Validation à la soumission
    form.addEventListener('submit', function(e) {
        // Validation de tous les champs
        const validNom = validateName(nomInput, nomError, 'nom');
        const validPrenom = validateName(prenomInput, prenomError, 'prénom');
        const validEmail = validateEmail();
        const validPhone = validatePhone();
        const validAge = validateAge();
        const validDates = validateDates();

        // Vérifier tous les résultats
        if (!validNom || !validPrenom || !validEmail || !validPhone || !validAge || !validDates) {
            e.preventDefault();
            
            // Message d'erreur détaillé
            let errors = [];
            if (!validNom) errors.push('- Nom invalide');
            if (!validPrenom) errors.push('- Prénom invalide');
            if (!validEmail) errors.push('- Email invalide');
            if (!validPhone) errors.push('- Téléphone invalide');
            if (!validAge) errors.push('- Âge invalide');
            if (!validDates) errors.push('- Dates incohérentes');
            
            alert('⚠️ Veuillez corriger les erreurs suivantes :\n\n' + errors.join('\n'));
            return false;
        }
        
        // Dernière vérification de sécurité
        const matricule = document.querySelector('input[name="matricule"]');
        if (matricule && !matricule.hasAttribute('readonly')) {
            const matriculeValue = matricule.value.trim();
            if (!/^[A-Za-z0-9_-]{3,20}$/.test(matriculeValue)) {
                e.preventDefault();
                alert('⚠️ Le matricule est invalide (3-20 caractères alphanumériques, tirets ou underscores).');
                return false;
            }
        }
    });
});
</script>

<jsp:include page="../layout/footer.jsp" />
