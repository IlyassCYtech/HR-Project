<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../layout/header.jsp">
    <jsp:param name="title" value="${empty conge ? 'Nouvelle' : 'Modifier'} Demande de Congé - Système RH" />
    <jsp:param name="page" value="conges" />
</jsp:include>

<div class="header">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="margin-bottom: 8px;">
                ${empty conge ? 'Nouvelle demande de congé' : 'Modifier la demande'}
            </h1>
            <p class="subtitle">
                ${empty conge ? 'Créer une nouvelle demande de congé ou d\'absence' : 'Mettre à jour votre demande'}
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/app/conges-absences" class="btn btn-secondary">
            <i class="fas fa-arrow-left" style="margin-right: 8px;"></i>Retour
        </a>
    </div>
</div>

<c:if test="${not empty error}">
    <div class="alert alert-danger" role="alert">
        <i class="fas fa-exclamation-circle" style="margin-right: 8px;"></i>${error}
    </div>
</c:if>

<div style="display: grid; grid-template-columns: 2fr 1fr; gap: 24px;">
    <!-- Formulaire -->
    <div>
        <form action="${pageContext.request.contextPath}/app/conges-absences" method="post" id="congeForm">
            <input type="hidden" name="action" value="${empty conge ? 'create' : 'update'}">
            <c:if test="${not empty conge}">
                <input type="hidden" name="id" value="${conge.id}">
            </c:if>

            <div class="card" style="margin-bottom: 24px;">
                <div class="card-header">
                    <i class="fas fa-calendar-alt" style="margin-right: 12px;"></i>Informations de la demande
                </div>
                <div style="padding: 32px;">
                    <div style="margin-bottom: 24px;">
                        <label class="form-label" for="type">
                            TYPE DE CONGÉ <span style="color: #C5A572;">*</span>
                        </label>
                        <select id="type" name="type" class="form-control" required>
                            <option value="">-- Sélectionner un type --</option>
                            <option value="CONGES_PAYES" ${conge.typeConge.name() == 'CONGES_PAYES' ? 'selected' : ''}>Congés payés</option>
                            <option value="MALADIE" ${conge.typeConge.name() == 'MALADIE' ? 'selected' : ''}>Maladie</option>
                            <option value="MATERNITE" ${conge.typeConge.name() == 'MATERNITE' ? 'selected' : ''}>Maternité</option>
                            <option value="PATERNITE" ${conge.typeConge.name() == 'PATERNITE' ? 'selected' : ''}>Paternité</option>
                            <option value="FORMATION" ${conge.typeConge.name() == 'FORMATION' ? 'selected' : ''}>Formation</option>
                            <option value="SANS_SOLDE" ${conge.typeConge.name() == 'SANS_SOLDE' ? 'selected' : ''}>Sans solde</option>
                        </select>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 24px;">
                        <div>
                            <label class="form-label" for="dateDebut">
                                DATE DE DÉBUT <span style="color: #C5A572;">*</span>
                            </label>
                            <input type="date" 
                                   id="dateDebut" 
                                   name="dateDebut" 
                                   class="form-control" 
                                   value="${conge.dateDebut}" 
                                   required
                                   onchange="calculateDays()">
                        </div>

                        <div>
                            <label class="form-label" for="dateFin">
                                DATE DE FIN <span style="color: #C5A572;">*</span>
                            </label>
                            <input type="date" 
                                   id="dateFin" 
                                   name="dateFin" 
                                   class="form-control" 
                                   value="${conge.dateFin}" 
                                   required
                                   onchange="calculateDays()">
                        </div>
                    </div>

                    <div style="padding: 16px; background: #F5F5F5; border-radius: 8px; margin-bottom: 24px;">
                        <label class="form-label">NOMBRE DE JOURS</label>
                        <div style="font-size: 28px; font-weight: 700; color: #C5A572;" id="nombreJours">
                            ${not empty conge ? conge.nombreJours : '0'} jours
                        </div>
                    </div>

                    <div style="margin-bottom: 24px;">
                        <label class="form-label" for="motif">
                            MOTIF
                        </label>
                        <textarea id="motif" 
                                  name="motif" 
                                  class="form-control" 
                                  rows="4" 
                                  placeholder="Décrivez brièvement la raison de votre demande">${conge.motif}</textarea>
                    </div>
                </div>
            </div>

            <div style="display: flex; gap: 12px;">
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-paper-plane" style="margin-right: 8px;"></i>Envoyer la demande
                </button>
                <a href="${pageContext.request.contextPath}/app/conges-absences" class="btn btn-secondary">
                    Annuler
                </a>
            </div>
        </form>
    </div>

    <!-- Solde de congés -->
    <div class="card">
        <div class="card-header">
            <i class="fas fa-wallet" style="margin-right: 12px;"></i>Votre solde
        </div>
        <div style="padding: 32px;">
            <c:if test="${not empty soldeConges}">
                <div style="margin-bottom: 24px;">
                    <label class="form-label">CONGÉS PAYÉS</label>
                    <div style="font-size: 32px; font-weight: 700; color: #C5A572; margin-top: 8px;">
                        ${soldeConges.congesPayes} jours
                    </div>
                </div>

                <div style="padding: 16px; background: #F5F5F5; border-radius: 8px;">
                    <i class="fas fa-info-circle" style="color: #C5A572; margin-right: 8px;"></i>
                    <span style="font-size: 12px; color: #666666;">
                        Les soldes sont mis à jour après validation de vos demandes
                    </span>
                </div>
            </c:if>
        </div>
    </div>
</div>

<script>
function calculateDays() {
    const dateDebut = document.getElementById('dateDebut').value;
    const dateFin = document.getElementById('dateFin').value;
    
    if (dateDebut && dateFin) {
        const debut = new Date(dateDebut);
        const fin = new Date(dateFin);
        const diff = Math.abs(fin - debut);
        const days = Math.ceil(diff / (1000 * 60 * 60 * 24)) + 1;
        
        document.getElementById('nombreJours').textContent = days + ' jours';
    }
}
</script>

<jsp:include page="../layout/footer.jsp" />
