<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Générer des Fiches de Paie - Système RH" />
    <jsp:param name="page" value="fiches-paie" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">Générer des Fiches de Paie</h1>
            <p class="subtitle">Génération individuelle ou en masse des bulletins de salaire</p>
        </div>
        <a href="${pageContext.request.contextPath}/app/fiches-paie" class="btn btn-secondary">
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

<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
    <!-- Génération individuelle -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-user" style="margin-right: 12px;"></i>Génération individuelle
        </div>
        <div style="padding: 32px;">
            <p style="color: #666666; margin-bottom: 24px; line-height: 1.6;">
                Générer une fiche de paie pour un employé spécifique
            </p>
            
            <!-- Filtres de recherche -->
            <div style="background: #F5F5F5; padding: 16px; border-radius: 8px; margin-bottom: 24px;">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                    <div>
                        <label class="form-label" style="font-size: 12px; margin-bottom: 4px;">
                            RECHERCHER PAR NOM
                        </label>
                        <input type="text" 
                               id="searchEmploye" 
                               class="form-control" 
                               placeholder="Nom ou prénom..."
                               style="padding: 8px 12px; font-size: 13px;">
                    </div>
                    <div>
                        <label class="form-label" style="font-size: 12px; margin-bottom: 4px;">
                            FILTRER PAR DÉPARTEMENT
                        </label>
                        <select id="filterDepartement" class="form-control" style="padding: 8px 12px; font-size: 13px;">
                            <option value="">Tous les départements</option>
                            <option value="none">Aucun département</option>
                            <c:forEach var="dept" items="${departements}">
                                <option value="${dept.id}">${dept.nom}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>
            
            <form action="${pageContext.request.contextPath}/app/fiches-paie" method="post">
                <input type="hidden" name="action" value="generateOne">
                
                <div style="margin-bottom: 24px;">
                    <label class="form-label" for="employeId">
                        EMPLOYÉ <span style="color: #C5A572;">*</span>
                    </label>
                    <select id="employeId" name="employeId" class="form-control" required>
                        <option value="">-- Sélectionner un employé --</option>
                        <c:forEach var="emp" items="${employes}">
                            <option value="${emp.id}" 
                                    data-nom="${emp.prenom} ${emp.nom}" 
                                    data-dept="${not empty emp.departement ? emp.departement.id : ''}">
                                ${emp.prenom} ${emp.nom} - ${emp.poste}<c:if test="${not empty emp.departement}"> (${emp.departement.nom})</c:if>
                            </option>
                        </c:forEach>
                    </select>
                    <div id="noResultMessage" style="display: none; margin-top: 8px; color: #DC3545; font-size: 13px;">
                        <i class="fas fa-info-circle"></i> Aucun employé trouvé avec ces critères
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px;">
                    <div>
                        <label class="form-label" for="mois">
                            MOIS <span style="color: #C5A572;">*</span>
                        </label>
                        <select id="mois" name="mois" class="form-control" required>
                            <option value="1">Janvier</option>
                            <option value="2">Février</option>
                            <option value="3">Mars</option>
                            <option value="4">Avril</option>
                            <option value="5">Mai</option>
                            <option value="6">Juin</option>
                            <option value="7">Juillet</option>
                            <option value="8">Août</option>
                            <option value="9">Septembre</option>
                            <option value="10">Octobre</option>
                            <option value="11">Novembre</option>
                            <option value="12">Décembre</option>
                        </select>
                    </div>
                    <div>
                        <label class="form-label" for="annee">
                            ANNÉE <span style="color: #C5A572;">*</span>
                        </label>
                        <select id="annee" name="annee" class="form-control" required>
                            <c:forEach var="y" begin="2020" end="2025">
                                <option value="${y}" ${y == 2025 ? 'selected' : ''}>${y}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%;">
                    <i class="fas fa-file-invoice-dollar" style="margin-right: 8px;"></i>Générer la fiche
                </button>
            </form>
        </div>
    </div>

    <!-- Génération en masse -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-users" style="margin-right: 12px;"></i>Génération en masse
        </div>
        <div style="padding: 32px;">
            <p style="color: #666666; margin-bottom: 24px; line-height: 1.6;">
                Générer les fiches de paie pour tous les employés actifs
            </p>
            
            <form action="${pageContext.request.contextPath}/app/fiches-paie" method="post">
                <input type="hidden" name="action" value="generateAll">
                
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px;">
                    <div>
                        <label class="form-label" for="moisMasse">
                            MOIS <span style="color: #C5A572;">*</span>
                        </label>
                        <select id="moisMasse" name="mois" class="form-control" required>
                            <option value="1">Janvier</option>
                            <option value="2">Février</option>
                            <option value="3">Mars</option>
                            <option value="4">Avril</option>
                            <option value="5">Mai</option>
                            <option value="6">Juin</option>
                            <option value="7">Juillet</option>
                            <option value="8">Août</option>
                            <option value="9">Septembre</option>
                            <option value="10">Octobre</option>
                            <option value="11">Novembre</option>
                            <option value="12">Décembre</option>
                        </select>
                    </div>
                    <div>
                        <label class="form-label" for="anneeMasse">
                            ANNÉE <span style="color: #C5A572;">*</span>
                        </label>
                        <select id="anneeMasse" name="annee" class="form-control" required>
                            <c:forEach var="y" begin="2020" end="2025">
                                <option value="${y}" ${y == 2025 ? 'selected' : ''}>${y}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div style="padding: 16px; background: #F5F5F5; border-radius: 8px; margin-bottom: 24px;">
                    <div style="display: flex; align-items: center; gap: 12px;">
                        <i class="fas fa-info-circle" style="color: #C5A572; font-size: 24px;"></i>
                        <div style="flex: 1;">
                            <div style="font-weight: 600; color: #1A1A1A; margin-bottom: 4px;">
                                Employés actifs : ${employesActifs}
                            </div>
                            <div style="font-size: 12px; color: #666666;">
                                Les fiches seront générées uniquement pour les employés avec le statut ACTIF
                            </div>
                        </div>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%;">
                    <i class="fas fa-file-invoice-dollar" style="margin-right: 8px;"></i>Générer toutes les fiches
                </button>
            </form>
        </div>
    </div>
</div>

<script>
    // Filtrage dynamique de la liste des employés
    document.addEventListener('DOMContentLoaded', function() {
        const searchInput = document.getElementById('searchEmploye');
        const deptFilter = document.getElementById('filterDepartement');
        const employeSelect = document.getElementById('employeId');
        const noResultMsg = document.getElementById('noResultMessage');
        const allOptions = Array.from(employeSelect.options).slice(1); // Exclure l'option vide
        
        function filterEmployes() {
            const searchTerm = searchInput.value.toLowerCase().trim();
            const selectedDept = deptFilter.value;
            let visibleCount = 0;
            
            // Réinitialiser le select (garder l'option vide)
            employeSelect.innerHTML = '<option value="">-- Sélectionner un employé --</option>';
            
            allOptions.forEach(option => {
                const nom = option.getAttribute('data-nom').toLowerCase();
                const dept = option.getAttribute('data-dept');
                
                // Vérifier les critères de recherche
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
                    visibleCount++;
                }
            });
            
            // Afficher/masquer le message "aucun résultat"
            if (visibleCount === 0) {
                noResultMsg.style.display = 'block';
                employeSelect.disabled = true;
            } else {
                noResultMsg.style.display = 'none';
                employeSelect.disabled = false;
            }
        }
        
        // Écouteurs d'événements
        searchInput.addEventListener('input', filterEmployes);
        deptFilter.addEventListener('change', filterEmployes);
        
        // Réinitialiser les filtres si on sélectionne manuellement un employé
        employeSelect.addEventListener('change', function() {
            if (this.value) {
                // Optionnel : on peut afficher l'employé sélectionné même s'il ne correspond pas aux filtres
            }
        });
    });
</script>

<jsp:include page="../layout/footer.jsp" />
