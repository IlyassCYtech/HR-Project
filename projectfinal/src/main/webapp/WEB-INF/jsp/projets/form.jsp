<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="${empty projet ? 'Nouveau' : 'Modifier'} Projet - Système RH" />
    <jsp:param name="page" value="projets" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">
                ${empty projet ? 'Nouveau projet' : 'Modifier le projet'}
            </h1>
            <p class="subtitle">
                ${empty projet ? 'Créer un nouveau projet' : 'Mettre à jour les informations du projet'}
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/app/projets" class="btn btn-secondary">
            <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
        </a>
    </div>
</div>

<c:if test="${not empty error}">
    <div class="alert alert-danger" role="alert">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>${error}
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/app/projets" method="post">
    <input type="hidden" name="action" value="${empty projet ? 'create' : 'update'}">
    <c:if test="${not empty projet}">
        <input type="hidden" name="id" value="${projet.id}">
    </c:if>

    <!-- Informations générales -->
    <div class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <i class="fas fa-info-circle" style="margin-right: 12px;"></i>Informations générales
        </div>
        <div style="padding: 32px;">
            <div style="margin-bottom: 24px;">
                <label class="form-label" for="nom">
                    NOM DU PROJET <span style="color: #C5A572;">*</span>
                </label>
                <input type="text" 
                       id="nom" 
                       name="nom" 
                       class="form-control" 
                       value="${projet.nom}" 
                       required 
                       minlength="3"
                       maxlength="150"
                       placeholder="Ex: Refonte du système RH"
                       title="Le nom doit contenir entre 3 et 150 caractères">
            </div>

            <div style="margin-bottom: 24px;">
                <label class="form-label" for="description">
                    DESCRIPTION <span style="color: #C5A572;">*</span>
                </label>
                <textarea id="description" 
                          name="description" 
                          class="form-control" 
                          rows="4" 
                          required
                          minlength="10"
                          maxlength="2000"
                          placeholder="Description détaillée du projet et de ses objectifs"
                          title="La description doit contenir entre 10 et 2000 caractères">${projet.description}</textarea>
                <small style="color: #666666; display: block; margin-top: 4px;">
                    <span id="charCount">0</span> / 2000 caractères
                </small>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 24px;">
                <div>
                    <label class="form-label" for="statut">
                        STATUT <span style="color: #C5A572;">*</span>
                    </label>
                    <select id="statut" name="statut" class="form-control" required>
                        <option value="">-- Sélectionner --</option>
                        <option value="PLANIFIE" ${projet.statut.name() == 'PLANIFIE' ? 'selected' : ''}>Planifié</option>
                        <option value="EN_COURS" ${projet.statut.name() == 'EN_COURS' ? 'selected' : ''}>En cours</option>
                        <option value="TERMINE" ${projet.statut.name() == 'TERMINE' ? 'selected' : ''}>Terminé</option>
                        <option value="ANNULE" ${projet.statut.name() == 'ANNULE' ? 'selected' : ''}>Annulé</option>
                    </select>
                </div>

                <div>
                    <label class="form-label" for="dateDebut">
                        DATE DE DÉBUT <span style="color: #C5A572;">*</span>
                    </label>
                    <input type="date" 
                           id="dateDebut" 
                           name="dateDebut" 
                           class="form-control" 
                           value="${projet.dateDebut}" 
                           required>
                </div>

                <div>
                    <label class="form-label" for="dateFinPrevue">
                        DATE DE FIN PRÉVUE <span style="color: #C5A572;">*</span>
                    </label>
                    <input type="date" 
                           id="dateFinPrevue" 
                           name="dateFinPrevue" 
                           class="form-control" 
                           value="${projet.dateFinPrevue}" 
                           required>
                </div>
            </div>
        </div>
    </div>

    <!-- Équipe -->
    <div class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <i class="fas fa-users" style="margin-right: 12px;"></i>Équipe du projet
        </div>
        <div style="padding: 32px;">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
                <div>
                    <label class="form-label" for="chefProjetId">
                        CHEF DE PROJET <span style="color: #C5A572;">*</span>
                    </label>
                    <select id="chefProjetId" name="chefProjetId" class="form-control" required>
                        <option value="">-- Sélectionner un chef de projet --</option>
                        <c:forEach var="chef" items="${chefsProjet}">
                            <option value="${chef.id}" ${not empty projet.chefProjet && projet.chefProjet.id == chef.id ? 'selected' : ''}>
                                ${chef.prenom} ${chef.nom} - ${chef.poste}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label class="form-label" for="departementId">
                        DÉPARTEMENT
                        <c:if test="${departementFixe}">
                            <span style="color: #C5A572; font-size: 12px;">(fixé à votre département)</span>
                        </c:if>
                    </label>
                    <select id="departementId" name="departementId" class="form-control" ${departementFixe ? 'disabled' : ''}>
                        <option value="">-- Sélectionner un département --</option>
                        <c:forEach var="dept" items="${departements}">
                            <option value="${dept.id}" 
                                    ${(not empty projet && not empty projet.departement && projet.departement.id == dept.id) || 
                                      (departementFixe && departementFixeId == dept.id) ? 'selected' : ''}
                                    ${departementFixe && departementFixeId != dept.id ? 'disabled' : ''}>
                                ${dept.nom}
                            </option>
                        </c:forEach>
                    </select>
                    <%-- Champ caché pour soumettre la valeur si le select est disabled --%>
                    <c:if test="${departementFixe}">
                        <input type="hidden" name="departementId" value="${departementFixeId}">
                    </c:if>
                </div>
            </div>

            <div style="margin-top: 24px;">
                <label class="form-label" for="employeIds">
                    MEMBRES DE L'ÉQUIPE
                </label>
                <select id="employeIds" name="employeIds" class="form-control" multiple size="8">
                    <c:forEach var="emp" items="${employes}">
                        <c:set var="isSelected" value="false" />
                        <c:if test="${not empty projet.employes}">
                            <c:forEach var="projEmp" items="${projet.employes}">
                                <c:if test="${projEmp.employe.id == emp.id}">
                                    <c:set var="isSelected" value="true" />
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <option value="${emp.id}" ${isSelected ? 'selected' : ''}>
                            ${emp.prenom} ${emp.nom} - ${emp.poste}
                            <c:if test="${not empty emp.departement}">
                                (${emp.departement.nom})
                            </c:if>
                        </option>
                    </c:forEach>
                </select>
                <small style="display: block; margin-top: 8px; color: #666666; font-size: 12px;">
                    <i class="fas fa-info-circle"></i> Maintenez Ctrl (Windows) ou Cmd (Mac) pour sélectionner plusieurs employés
                </small>
            </div>
        </div>
    </div>

    <div style="display: flex; gap: 12px;">
        <button type="submit" class="btn btn-primary" id="submitBtn">
            <i class="fas fa-save" style="margin-right: 8px;"></i>Enregistrer
        </button>
        <a href="${pageContext.request.contextPath}/app/projets" class="btn btn-secondary">
            Annuler
        </a>
    </div>
</form>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    const nomInput = document.getElementById('nom');
    const descriptionInput = document.getElementById('description');
    const dateDebutInput = document.getElementById('dateDebut');
    const dateFinPrevueInput = document.getElementById('dateFinPrevue');
    const chefProjetSelect = document.getElementById('chefProjetId');
    const submitBtn = document.getElementById('submitBtn');

    // Patterns de validation
    const nameRegex = /^[a-zA-ZÀ-ÿ0-9][a-zA-ZÀ-ÿ0-9\s\-'&().:]*[a-zA-ZÀ-ÿ0-9]$/;
    const dangerousCharsRegex = /<script|<iframe|javascript:|onerror=|onload=|<\?php/i;

    // Fonction de sanitization
    function sanitizeInput(value) {
        if (!value) return '';
        return value.replace(/<script|<iframe|<\?php/gi, '').trim();
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

    // Validation du nom du projet
    function validateNom() {
        const value = nomInput.value.trim();
        
        if (value === '') {
            return createErrorElement(nomInput, '⚠️ Le nom du projet est obligatoire.');
        }
        
        if (value.length < 3) {
            return createErrorElement(nomInput, '⚠️ Le nom doit contenir au moins 3 caractères.');
        }
        
        if (value.length > 150) {
            return createErrorElement(nomInput, '⚠️ Le nom ne peut pas dépasser 150 caractères.');
        }
        
        // Vérifier les caractères dangereux
        if (dangerousCharsRegex.test(value)) {
            return createErrorElement(nomInput, '⚠️ Le nom contient des éléments non autorisés.');
        }
        
        // Vérifier les séquences répétées
        if (/--/.test(value) || /''/.test(value) || /  /.test(value)) {
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
            return createErrorElement(descriptionInput, '⚠️ La description est obligatoire.');
        }
        
        if (value.length < 10) {
            return createErrorElement(descriptionInput, '⚠️ La description doit contenir au moins 10 caractères.');
        }
        
        if (value.length > 2000) {
            return createErrorElement(descriptionInput, '⚠️ La description ne peut pas dépasser 2000 caractères.');
        }
        
        // Vérifier les caractères dangereux (XSS)
        if (dangerousCharsRegex.test(value)) {
            return createErrorElement(descriptionInput, '⚠️ La description contient des éléments non autorisés.');
        }
        
        return clearError(descriptionInput);
    }

    // Validation des dates
    function validateDates() {
        const dateDebut = dateDebutInput.value;
        const dateFinPrevue = dateFinPrevueInput.value;
        
        if (!dateDebut) {
            return createErrorElement(dateDebutInput, '⚠️ La date de début est obligatoire.');
        }
        
        if (!dateFinPrevue) {
            return createErrorElement(dateFinPrevueInput, '⚠️ La date de fin prévue est obligatoire.');
        }
        
        const debut = new Date(dateDebut);
        const fin = new Date(dateFinPrevue);
        
        // La date de début ne peut pas être dans le futur lointain
        const maxFutureDate = new Date();
        maxFutureDate.setFullYear(maxFutureDate.getFullYear() + 10);
        
        if (debut > maxFutureDate) {
            return createErrorElement(dateDebutInput, '⚠️ La date de début est trop éloignée dans le futur.');
        }
        
        // La date de fin doit être après la date de début
        if (fin <= debut) {
            return createErrorElement(dateFinPrevueInput, '⚠️ La date de fin doit être postérieure à la date de début.');
        }
        
        // Vérifier que la durée n'est pas trop courte (au moins 1 jour)
        const diffDays = Math.floor((fin - debut) / (1000 * 60 * 60 * 24));
        if (diffDays < 1) {
            return createErrorElement(dateFinPrevueInput, '⚠️ Le projet doit durer au moins 1 jour.');
        }
        
        // Avertissement si le projet dure plus de 5 ans
        if (diffDays > 1825) { // 5 ans
            if (!confirm('⚠️ Ce projet a une durée de plus de 5 ans. Voulez-vous continuer ?')) {
                return createErrorElement(dateFinPrevueInput, '⚠️ Durée du projet jugée trop longue.');
            }
        }
        
        clearError(dateDebutInput);
        clearError(dateFinPrevueInput);
        return true;
    }

    // Validation du chef de projet
    function validateChefProjet() {
        const value = chefProjetSelect.value;
        
        if (!value || value === '') {
            return createErrorElement(chefProjetSelect, '⚠️ Vous devez sélectionner un chef de projet.');
        }
        
        return clearError(chefProjetSelect);
    }

    // Validation en temps réel
    if (nomInput) {
        nomInput.addEventListener('input', function() {
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
            
            if (count > 1900) {
                charCount.style.color = '#D32F2F';
                charCount.style.fontWeight = 'bold';
            } else if (count > 1800) {
                charCount.style.color = '#F57C00';
            } else {
                charCount.style.color = '#666666';
                charCount.style.fontWeight = 'normal';
            }
        }
        
        // Initialiser le compteur
        updateCharCount();
        
        descriptionInput.addEventListener('input', function() {
            const sanitized = sanitizeInput(this.value);
            if (this.value !== sanitized) {
                this.value = sanitized;
            }
            updateCharCount();
        });
        descriptionInput.addEventListener('blur', validateDescription);
    }
    
    if (dateDebutInput) {
        dateDebutInput.addEventListener('change', validateDates);
    }
    
    if (dateFinPrevueInput) {
        dateFinPrevueInput.addEventListener('change', validateDates);
    }
    
    if (chefProjetSelect) {
        chefProjetSelect.addEventListener('change', validateChefProjet);
    }

    // Validation à la soumission
    form.addEventListener('submit', function(e) {
        // Désactiver le bouton pour éviter les doubles soumissions
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin" style="margin-right: 8px;"></i>Enregistrement...';
        
        // Valider tous les champs
        const validNom = validateNom();
        const validDescription = validateDescription();
        const validDates = validateDates();
        const validChefProjet = validateChefProjet();
        
        // Si une validation échoue
        if (!validNom || !validDescription || !validDates || !validChefProjet) {
            e.preventDefault();
            
            // Réactiver le bouton
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-save" style="margin-right: 8px;"></i>Enregistrer';
            
            // Construire le message d'erreur
            let errors = [];
            if (!validNom) errors.push('- Nom du projet invalide');
            if (!validDescription) errors.push('- Description invalide');
            if (!validDates) errors.push('- Dates invalides');
            if (!validChefProjet) errors.push('- Chef de projet non sélectionné');
            
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
        descriptionInput.value = sanitizeInput(descriptionInput.value);
        
        // Validation finale de sécurité côté client
        const nomValue = nomInput.value.trim();
        if (nomValue.length < 3 || nomValue.length > 150) {
            e.preventDefault();
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-save" style="margin-right: 8px;"></i>Enregistrer';
            alert('⚠️ Le nom du projet doit contenir entre 3 et 150 caractères.');
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
