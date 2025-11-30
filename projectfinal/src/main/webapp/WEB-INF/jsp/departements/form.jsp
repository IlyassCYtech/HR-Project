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

<c:choose>
    <c:when test="${chefDeptMode}">
        <%-- Mode CHEF_DEPT : Affichage en lecture seule avec gestion des membres uniquement --%>
        <div class="card">
            <div class="card-header">
                <i class="fas fa-building" style="margin-right: 12px;"></i>Informations du département
                <span style="margin-left: 12px; color: #C5A572; font-size: 14px;">(Consultation uniquement)</span>
            </div>
            <div style="padding: 32px;">
                <div style="background: #FFF8E7; border-left: 4px solid #C5A572; padding: 16px; margin-bottom: 24px; border-radius: 4px;">
                    <i class="fas fa-info-circle" style="color: #C5A572; margin-right: 8px;"></i>
                    <strong>Mode Chef de Département :</strong> Vous pouvez consulter les informations et gérer les membres de votre département, mais vous ne pouvez pas modifier les informations générales.
                </div>
                
                <div style="margin-bottom: 24px;">
                    <label class="form-label">NOM DU DÉPARTEMENT</label>
                    <input type="text" class="form-control" value="${departement.nom}" disabled>
                </div>
                
                <div style="margin-bottom: 24px;">
                    <label class="form-label">DESCRIPTION</label>
                    <textarea class="form-control" rows="4" disabled>${departement.description}</textarea>
                </div>
                
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 24px;">
                    <div>
                        <label class="form-label">RESPONSABLE</label>
                        <input type="text" class="form-control" value="${not empty departement.chefDepartement ? departement.chefDepartement.prenom.concat(' ').concat(departement.chefDepartement.nom) : 'Non défini'}" disabled>
                    </div>
                    <div>
                        <label class="form-label">BUDGET ANNUEL (€)</label>
                        <input type="text" class="form-control" value="${not empty departement.budget ? departement.budget : 'Non défini'}" disabled>
                    </div>
                </div>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <%-- Mode ADMIN/RH : Formulaire complet modifiable --%>
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
                               minlength="2"
                               maxlength="100"
                               placeholder="Ex: Ressources Humaines"
                               title="Le nom doit contenir entre 2 et 100 caractères (lettres, chiffres, espaces, tirets, apostrophes)">
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
                          maxlength="1000"
                          placeholder="Description des missions et responsabilités du département"
                          title="Maximum 1000 caractères">${departement.description}</textarea>
                <small style="color: #666666; display: block; margin-top: 4px;">
                    <span id="charCount">0</span> / 1000 caractères
                </small>
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
                           max="999999999"
                           step="0.01"
                           placeholder="Ex: 500000"
                           title="Budget entre 0 et 999,999,999 €">
                    <small style="color: #666666; display: block; margin-top: 4px;">
                        Montant annuel en euros (optionnel)
                    </small>
                </div>
                    </div>
                </div>
            </div>

            <div style="display: flex; gap: 12px; margin-top: 24px;">
                <button type="submit" class="btn btn-primary" id="submitBtn">
                    <i class="fas fa-save" style="margin-right: 8px;"></i>Enregistrer
                </button>
                <a href="${pageContext.request.contextPath}/app/departements" class="btn btn-secondary">
                    Annuler
                </a>
            </div>
        </form>
    </c:otherwise>
</c:choose>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    const nomInput = document.getElementById('nom');
    const descriptionInput = document.getElementById('description');
    const budgetInput = document.getElementById('budget');
    const submitBtn = document.getElementById('submitBtn');

    // Patterns de validation
    const nameRegex = /^[a-zA-ZÀ-ÿ0-9][a-zA-ZÀ-ÿ0-9\s\-'&()]*[a-zA-ZÀ-ÿ0-9]$/;
    const dangerousCharsRegex = /<|>|"|\\|;|\|/;

    // Fonction de sanitization (nettoyage)
    function sanitizeInput(value) {
        if (!value) return '';
        // Supprimer les caractères dangereux
        return value.replace(/<|>|"|\\|;|\|/g, '').trim();
    }

    // Créer un élément d'erreur
    function createErrorElement(input, message) {
        let errorElement = input.parentElement.querySelector('.text-danger');
        if (!errorElement) {
            errorElement = document.createElement('small');
            errorElement.className = 'text-danger';
            errorElement.style.display = 'block';
            errorElement.style.marginTop = '4px';
            errorElement.style.fontSize = '12px';
            input.parentElement.appendChild(errorElement);
        }
        errorElement.textContent = message;
        errorElement.style.display = 'block';
        input.classList.add('is-invalid');
        return false;
    }

    // Supprimer l'erreur
    function clearError(input) {
        const errorElement = input.parentElement.querySelector('.text-danger');
        if (errorElement) {
            errorElement.style.display = 'none';
        }
        input.classList.remove('is-invalid');
        return true;
    }

    // Validation du nom du département
    function validateNom() {
        const value = nomInput.value.trim();
        
        if (value === '') {
            return createErrorElement(nomInput, '⚠️ Le nom du département est obligatoire.');
        }
        
        if (value.length < 2) {
            return createErrorElement(nomInput, '⚠️ Le nom doit contenir au moins 2 caractères.');
        }
        
        if (value.length > 100) {
            return createErrorElement(nomInput, '⚠️ Le nom ne peut pas dépasser 100 caractères.');
        }
        
        // Vérifier les caractères dangereux
        if (dangerousCharsRegex.test(value)) {
            return createErrorElement(nomInput, '⚠️ Le nom contient des caractères non autorisés (< > " \\ ; |).');
        }
        
        // Vérifier le pattern
        if (!nameRegex.test(value)) {
            return createErrorElement(nomInput, '⚠️ Le nom ne doit contenir que des lettres, chiffres, espaces et - \' & ( ).');
        }
        
        // Vérifier les séquences répétées
        if (/--/.test(value) || /''/.test(value) || /  /.test(value) || /&&/.test(value)) {
            return createErrorElement(nomInput, '⚠️ Le nom contient des caractères répétés invalides.');
        }
        
        // Vérifier que ce n'est pas uniquement des chiffres
        if (/^\d+$/.test(value)) {
            return createErrorElement(nomInput, '⚠️ Le nom ne peut pas être uniquement composé de chiffres.');
        }
        
        return clearError(nomInput);
    }

    // Validation de la description
    function validateDescription() {
        const value = descriptionInput.value.trim();
        
        if (value === '') {
            return clearError(descriptionInput);
        }
        
        if (value.length > 1000) {
            return createErrorElement(descriptionInput, '⚠️ La description ne peut pas dépasser 1000 caractères.');
        }
        
        // Vérifier les caractères dangereux (XSS)
        if (/<script|<iframe|javascript:|onerror=|onload=/i.test(value)) {
            return createErrorElement(descriptionInput, '⚠️ La description contient des éléments non autorisés.');
        }
        
        // Permettre quelques caractères HTML basiques mais bloquer les dangereux
        const dangerousHtml = /<(?!\/?(b|i|u|p|br|strong|em)>)[^>]*>/gi;
        if (dangerousHtml.test(value)) {
            return createErrorElement(descriptionInput, '⚠️ La description contient des balises HTML non autorisées.');
        }
        
        return clearError(descriptionInput);
    }

    // Validation du budget
    function validateBudget() {
        const value = budgetInput.value.trim();
        
        if (value === '') {
            return clearError(budgetInput);
        }
        
        const budgetNum = parseFloat(value);
        
        if (isNaN(budgetNum)) {
            return createErrorElement(budgetInput, '⚠️ Le budget doit être un nombre valide.');
        }
        
        if (budgetNum < 0) {
            return createErrorElement(budgetInput, '⚠️ Le budget ne peut pas être négatif.');
        }
        
        if (budgetNum > 999999999) {
            return createErrorElement(budgetInput, '⚠️ Le budget est trop élevé (maximum: 999,999,999 €).');
        }
        
        // Vérifier les décimales (pas plus de 2)
        if (value.includes('.')) {
            const decimals = value.split('.')[1];
            if (decimals && decimals.length > 2) {
                return createErrorElement(budgetInput, '⚠️ Le budget ne peut avoir que 2 décimales maximum.');
            }
        }
        
        return clearError(budgetInput);
    }

    // Validation en temps réel
    if (nomInput) {
        nomInput.addEventListener('input', function() {
            // Sanitize pendant la saisie
            const sanitized = sanitizeInput(this.value);
            if (this.value !== sanitized) {
                this.value = sanitized;
            }
        });
        
        nomInput.addEventListener('blur', validateNom);
    }
    
    if (descriptionInput) {
        const charCount = document.getElementById('charCount');
        
        // Compteur de caractères
        function updateCharCount() {
            const count = descriptionInput.value.length;
            charCount.textContent = count;
            
            if (count > 900) {
                charCount.style.color = '#D32F2F';
                charCount.style.fontWeight = 'bold';
            } else if (count > 800) {
                charCount.style.color = '#F57C00';
            } else {
                charCount.style.color = '#666666';
                charCount.style.fontWeight = 'normal';
            }
        }
        
        // Initialiser le compteur
        updateCharCount();
        
        descriptionInput.addEventListener('input', updateCharCount);
        descriptionInput.addEventListener('blur', validateDescription);
    }
    
    if (budgetInput) {
        budgetInput.addEventListener('blur', validateBudget);
        
        // Formater le budget avec des espaces
        budgetInput.addEventListener('blur', function() {
            const value = parseFloat(this.value);
            if (!isNaN(value) && value > 0) {
                // Formater avec séparateur de milliers
                this.value = value.toFixed(2);
            }
        });
    }

    // Validation à la soumission
    form.addEventListener('submit', function(e) {
        // Désactiver le bouton pour éviter les doubles soumissions
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin" style="margin-right: 8px;"></i>Enregistrement...';
        
        // Valider tous les champs
        const validNom = validateNom();
        const validDescription = validateDescription();
        const validBudget = validateBudget();
        
        // Si une validation échoue
        if (!validNom || !validDescription || !validBudget) {
            e.preventDefault();
            
            // Réactiver le bouton
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-save" style="margin-right: 8px;"></i>Enregistrer';
            
            // Construire le message d'erreur
            let errors = [];
            if (!validNom) errors.push('- Nom du département invalide');
            if (!validDescription) errors.push('- Description invalide');
            if (!validBudget) errors.push('- Budget invalide');
            
            alert('⚠️ Veuillez corriger les erreurs suivantes :\n\n' + errors.join('\n'));
            
            // Scroller vers le premier champ en erreur
            const firstError = form.querySelector('.is-invalid');
            if (firstError) {
                firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                firstError.focus();
            }
            
            return false;
        }
        
        // Dernière sanitization avant soumission
        nomInput.value = sanitizeInput(nomInput.value);
        if (descriptionInput.value.trim()) {
            descriptionInput.value = sanitizeInput(descriptionInput.value);
        }
        
        // Validation finale de sécurité côté client
        const nomValue = nomInput.value.trim();
        if (nomValue.length < 2 || nomValue.length > 100) {
            e.preventDefault();
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-save" style="margin-right: 8px;"></i>Enregistrer';
            alert('⚠️ Le nom du département doit contenir entre 2 et 100 caractères.');
            nomInput.focus();
            return false;
        }
        
        // Si tout est OK, laisser le formulaire se soumettre
        return true;
    });
    
    // Prévenir la réactivation du bouton si l'utilisateur revient en arrière
    window.addEventListener('pageshow', function(event) {
        if (event.persisted) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-save" style="margin-right: 8px;"></i>Enregistrer';
        }
    });
});
</script>

<jsp:include page="../layout/footer.jsp" />
