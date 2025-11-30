<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="Modifier Fiche de Paie - Système RH" />
    <jsp:param name="page" value="fiches-paie" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">Modifier la Fiche de Paie</h1>
            <p class="subtitle">
                ${fiche.employe.prenom} ${fiche.employe.nom} - ${fiche.mois}/${fiche.annee}
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/app/fiches-paie?action=show&id=${fiche.id}" class="btn btn-secondary">
            <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
        </a>
    </div>
</div>

<c:if test="${not empty error}">
    <div class="alert alert-danger" role="alert">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>${error}
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/app/fiches-paie" method="post" id="editForm">
    <input type="hidden" name="action" value="update">
    <input type="hidden" name="id" value="${fiche.id}">
    
    <div class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <i class="fas fa-info-circle" style="margin-right: 12px;"></i>Informations de base
        </div>
        <div style="padding: 32px;">
            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 24px; margin-bottom: 24px;">
                <div>
                    <label class="form-label">EMPLOYÉ</label>
                    <p style="font-size: 16px; font-weight: 600; color: #1A1A1A; margin-top: 8px;">
                        ${fiche.employe.prenom} ${fiche.employe.nom}
                    </p>
                    <p style="font-size: 13px; color: #666; margin-top: 4px;">
                        ${fiche.employe.matricule} - ${fiche.employe.poste}
                    </p>
                </div>
                <div>
                    <label class="form-label">PÉRIODE</label>
                    <p style="font-size: 16px; font-weight: 600; color: #1A1A1A; margin-top: 8px;">
                        ${fiche.mois}/${fiche.annee}
                    </p>
                </div>
                <div>
                    <label class="form-label">DATE DE CRÉATION</label>
                    <p style="font-size: 16px; font-weight: 600; color: #1A1A1A; margin-top: 8px;">
                        ${fiche.dateCreationFormatted}
                    </p>
                </div>
            </div>
            
            <div style="padding: 16px; background: #FFF3CD; border-left: 4px solid #FFA500; border-radius: 4px;">
                <p style="margin: 0; color: #856404; font-size: 14px;">
                    <i class="fas fa-exclamation-triangle" style="margin-right: 8px;"></i>
                    <strong>Attention :</strong> La modification des valeurs recalculera automatiquement les totaux.
                </p>
            </div>
        </div>
    </div>
    
    <div class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <i class="fas fa-euro-sign" style="margin-right: 12px;"></i>Éléments de rémunération
        </div>
        <div style="padding: 32px;">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 32px;">
                <div>
                    <label class="form-label" for="salaireBase">
                        SALAIRE DE BASE <span style="color: #C5A572;">*</span>
                    </label>
                    <div style="position: relative;">
                        <input type="number" 
                               id="salaireBase" 
                               name="salaireBase" 
                               class="form-control" 
                               value="${fiche.salaireBase}"
                               step="0.01"
                               min="0"
                               required
                               style="padding-right: 40px;">
                        <span style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); color: #666; font-weight: 600;">€</span>
                    </div>
                    <small style="color: #666; font-size: 12px; margin-top: 4px; display: block;">
                        Salaire brut de base pour 151,67 heures
                    </small>
                </div>
                
                <div>
                    <label class="form-label" for="primes">
                        PRIMES ET INDEMNITÉS
                    </label>
                    <div style="position: relative;">
                        <input type="number" 
                               id="primes" 
                               name="primes" 
                               class="form-control" 
                               value="${fiche.primes}"
                               step="0.01"
                               min="0"
                               style="padding-right: 40px;">
                        <span style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); color: #666; font-weight: 600;">€</span>
                    </div>
                    <small style="color: #666; font-size: 12px; margin-top: 4px; display: block;">
                        Prime d'ancienneté, 13ème mois, etc.
                    </small>
                </div>
            </div>
            
            <div style="margin-top: 24px; padding: 16px; background: #F5F5F5; border-radius: 8px;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-weight: 600; color: #666;">SALAIRE BRUT CALCULÉ</span>
                    <span id="salaireBrutDisplay" style="font-size: 24px; font-weight: 700; color: #C5A572;">
                        <fmt:formatNumber value="${fiche.salaireBrut}" pattern="#,##0.00"/> €
                    </span>
                </div>
            </div>
        </div>
    </div>
    
    <div class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <i class="fas fa-calculator" style="margin-right: 12px;"></i>Cotisations et déductions
        </div>
        <div style="padding: 32px;">
            <div>
                <label class="form-label" for="deductions">
                    TOTAL DES DÉDUCTIONS (Sécurité sociale + CSG/CRDS)
                </label>
                <div style="position: relative;">
                    <input type="number" 
                           id="deductions" 
                           name="deductions" 
                           class="form-control" 
                           value="${fiche.deductions}"
                           step="0.01"
                           min="0"
                           style="padding-right: 40px;">
                    <span style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); color: #666; font-weight: 600;">€</span>
                </div>
                <small style="color: #666; font-size: 12px; margin-top: 4px; display: block;">
                    Par défaut : environ 32,75% du salaire brut (22,95% + 9,8%)
                </small>
                <button type="button" class="btn btn-outline btn-sm" onclick="calculateDefaultDeductions()" style="margin-top: 8px;">
                    <i class="fas fa-sync-alt" style="margin-right: 6px;"></i>Calculer automatiquement
                </button>
            </div>
            
            <div style="margin-top: 24px; padding: 16px; background: #F5F5F5; border-radius: 8px;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-weight: 600; color: #666;">NET À PAYER CALCULÉ</span>
                    <span id="netAPayerDisplay" style="font-size: 28px; font-weight: 700; color: #1A1A1A;">
                        <fmt:formatNumber value="${fiche.netAPayer}" pattern="#,##0.00"/> €
                    </span>
                </div>
            </div>
        </div>
    </div>
    
    <div style="display: flex; gap: 12px; justify-content: flex-end;">
        <a href="${pageContext.request.contextPath}/app/fiches-paie?action=show&id=${fiche.id}" class="btn btn-secondary">
            <i class="fas fa-times" style="margin-right: 8px;"></i>Annuler
        </a>
        <button type="submit" class="btn btn-primary">
            <i class="fas fa-save" style="margin-right: 8px;"></i>Enregistrer les modifications
        </button>
    </div>
</form>

<script>
    // Calcul automatique du salaire brut
    function updateBrut() {
        const base = parseFloat(document.getElementById('salaireBase').value) || 0;
        const primes = parseFloat(document.getElementById('primes').value) || 0;
        const brut = base + primes;
        
        document.getElementById('salaireBrutDisplay').textContent = 
            brut.toLocaleString('fr-FR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' €';
        
        updateNet();
    }
    
    // Calcul automatique du net à payer
    function updateNet() {
        const base = parseFloat(document.getElementById('salaireBase').value) || 0;
        const primes = parseFloat(document.getElementById('primes').value) || 0;
        const deductions = parseFloat(document.getElementById('deductions').value) || 0;
        const brut = base + primes;
        const net = brut - deductions;
        
        document.getElementById('netAPayerDisplay').textContent = 
            net.toLocaleString('fr-FR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' €';
    }
    
    // Calcul automatique des déductions (32,75% du brut)
    function calculateDefaultDeductions() {
        const base = parseFloat(document.getElementById('salaireBase').value) || 0;
        const primes = parseFloat(document.getElementById('primes').value) || 0;
        const brut = base + primes;
        const deductions = Math.round(brut * 0.3275 * 100) / 100; // 32.75%
        
        document.getElementById('deductions').value = deductions.toFixed(2);
        updateNet();
    }
    
    // Écouteurs d'événements
    document.getElementById('salaireBase').addEventListener('input', updateBrut);
    document.getElementById('primes').addEventListener('input', updateBrut);
    document.getElementById('deductions').addEventListener('input', updateNet);
    
    // Validation avant soumission
    document.getElementById('editForm').addEventListener('submit', function(e) {
        const base = parseFloat(document.getElementById('salaireBase').value) || 0;
        const deductions = parseFloat(document.getElementById('deductions').value) || 0;
        const primes = parseFloat(document.getElementById('primes').value) || 0;
        const brut = base + primes;
        
        if (deductions > brut) {
            e.preventDefault();
            alert('⚠️ Les déductions ne peuvent pas être supérieures au salaire brut !');
            return false;
        }
        
        if (base <= 0) {
            e.preventDefault();
            alert('⚠️ Le salaire de base doit être supérieur à 0 !');
            return false;
        }
        
        return confirm('Confirmer la modification de cette fiche de paie ?\n\n' +
                      'Salaire brut : ' + brut.toFixed(2) + ' €\n' +
                      'Déductions : ' + deductions.toFixed(2) + ' €\n' +
                      'Net à payer : ' + (brut - deductions).toFixed(2) + ' €');
    });
</script>

<jsp:include page="../layout/footer.jsp" />
