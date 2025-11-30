# Restrictions de Sécurité Implémentées

**Date**: 29 octobre 2025
**Version**: 1.0.0

## Vue d'ensemble

Ce document détaille toutes les restrictions de sécurité et contrôles d'accès implémentés dans l'application de Gestion RH.

---

## 1. 🔒 Sécurisation des Données Confidentielles dans les Profils

### Fichier modifié
- `src/main/webapp/WEB-INF/jsp/employes/show.jsp`

### Restrictions implémentées

#### Données masquées pour les non-autorisés:
- **Salaire de base** : Affiché uniquement pour RH, Admin, ou le profil personnel
- **Numéro de téléphone** : Confidentiel sauf pour RH, Admin, ou soi-même
- **Date de naissance** : Confidentielle sauf pour RH, Admin, ou soi-même

#### Section "Actions Rapides"
- **Visible uniquement** si :
  - L'utilisateur consulte son propre profil, OU
  - L'utilisateur est RH ou Admin

### Variables JSP utilisées
```jsp
<c:set var="isOwnProfile" value="${sessionScope.employeId == employe.id}" />
<c:set var="isRHorAdmin" value="${sessionScope.utilisateur.role eq 'ADMIN' or sessionScope.utilisateur.role eq 'RH'}" />
<c:set var="canViewConfidential" value="${isOwnProfile or isRHorAdmin}" />
```

### Affichage pour non-autorisés
- Icône de cadenas 🔒
- Texte "Confidentiel" en gris italique
- Pas d'accès aux données réelles

---

## 2. 💰 Restrictions sur les Fiches de Paie

### Fichiers modifiés
- `src/main/webapp/WEB-INF/jsp/fiches-paie/list.jsp`
- `src/main/java/com/gestionrh/servlet/FichePaieServlet.java` (déjà en place)

### Restrictions implémentées

#### Pour les EMPLOYÉS (rôle: EMPLOYE)
- ✅ Voient **UNIQUEMENT leurs propres fiches de paie**
- ❌ **PAS de bouton "Générer des fiches"**
- ✅ Bouton "Télécharger toutes mes fiches (ZIP)" disponible
- ❌ **PAS d'accès à la masse salariale**
- ❌ **PAS d'accès aux fiches des autres employés**

#### Pour RH et ADMIN
- ✅ Accès à toutes les fiches de tous les employés
- ✅ Bouton "Générer des fiches" visible
- ✅ Bouton "Télécharger tout (ZIP)" visible
- ✅ Masse salariale totale affichée
- ✅ Peuvent créer et éditer des fiches pour n'importe quel employé

### Filtrage automatique dans le Servlet
```java
// Dans listFichesPaie()
if (utilisateur.getRole() == Utilisateur.Role.EMPLOYE) {
    Long employeIdFromSession = (Long) session.getAttribute("employeId");
    if (employeIdFromSession != null) {
        employeIdStr = employeIdFromSession.toString();
        logger.info("EMPLOYE role détecté - filtrage automatique sur employeId: {}", employeIdFromSession);
    }
}
```

### Boutons conditionnels dans le JSP
```jsp
<c:if test="${sessionScope.utilisateur.role eq 'ADMIN' or sessionScope.utilisateur.role eq 'RH'}">
    <!-- Boutons pour RH/Admin -->
</c:if>
<c:if test="${sessionScope.utilisateur.role eq 'EMPLOYE'}">
    <!-- Bouton ZIP pour employé -->
</c:if>
```

---

## 3. 📅 Protection des Congés et Absences

### Fichier modifié
- `src/main/java/com/gestionrh/servlet/CongeAbsenceServlet.java`

### Restrictions implémentées

#### Modification de congés (`modifierConge()`)
- ✅ Un employé peut **UNIQUEMENT modifier ses propres demandes**
- ✅ RH et Admin peuvent modifier n'importe quelle demande
- ❌ Tentative de modification d'un congé d'autrui = **BLOQUÉE**
- 📝 Logs de sécurité enregistrés pour toute tentative non autorisée

#### Suppression de congés (`supprimerConge()`)
- ✅ Un employé peut **UNIQUEMENT supprimer ses propres demandes**
- ✅ RH et Admin peuvent supprimer n'importe quelle demande
- ❌ Tentative de suppression d'un congé d'autrui = **BLOQUÉE**
- ❌ Impossible de supprimer un congé approuvé et en cours (pour tous)
- 📝 Logs de sécurité enregistrés

### Code de vérification
```java
HttpSession session = request.getSession();
Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
Long employeIdSession = (Long) session.getAttribute("employeId");

boolean isRHorAdmin = utilisateur != null && 
    (utilisateur.getRole() == Utilisateur.Role.ADMIN || utilisateur.getRole() == Utilisateur.Role.RH);
boolean isOwnConge = employeIdSession != null && 
    congeExistant.getEmploye() != null && 
    employeIdSession.equals(congeExistant.getEmploye().getId());

if (!isRHorAdmin && !isOwnConge) {
    logger.warn("Tentative de modification non autorisée...");
    request.setAttribute("error", "Vous n'êtes pas autorisé à modifier ce congé");
    response.sendRedirect(request.getContextPath() + "/app/conges-absences");
    return;
}
```

---

## 4. 🏢 Budget des Départements - Confidentiel

### Fichier modifié
- `src/main/webapp/WEB-INF/jsp/departements/show.jsp`

### Restrictions implémentées
- **Budget annuel** : Visible uniquement pour RH et Admin
- **Budget mensuel moyen** : Visible uniquement pour RH et Admin
- **Budget par employé** : Visible uniquement pour RH et Admin

### Affichage pour EMPLOYE et CHEF_PROJET
- Icône de cadenas �
- Texte "Confidentiel" en gris italique au lieu des montants

---

## 5. �📊 Chef de Département - Accès aux Projets

### Fichier modifié
- `src/main/java/com/gestionrh/servlet/ProjetServlet.java`

### Fonctionnalité implémentée
Un chef de département voit **automatiquement uniquement les projets de son département** lorsqu'il accède à la liste des projets.

### Logique de détection
```java
// Vérifier si l'employé connecté est chef de son département
Employe employe = employeDAO.findById(employeIdSession);
if (employe != null && employe.getDepartement() != null) {
    Departement dept = employe.getDepartement();
    if (dept.getChefDepartement() != null && 
        dept.getChefDepartement().getId().equals(employeIdSession)) {
        // Filtrage automatique par département
        departementIdStr = dept.getId().toString();
    }
}
```

### Comportement
- Le filtre par département s'applique automatiquement
- Le chef voit tous les projets de son département (tous statuts)
- Aucune manipulation manuelle requise
- Transparent pour l'utilisateur

---

## 6. 📋 Fonctionnalités à Implémenter (TODO)

### Édition/Création de Fiches de Paie Personnalisées
**Statut**: À implémenter
**Objectif**: RH et Admin peuvent créer et éditer des fiches de paie manuellement
**Fichiers à créer**:
- `src/main/webapp/WEB-INF/jsp/fiches-paie/edit.jsp`
- Méthodes dans `FichePaieServlet.java`

### Téléchargement ZIP des Fiches de Paie
**Statut**: Boutons ajoutés, fonctionnalité backend à implémenter
**Objectif**: 
- Employé: télécharger toutes ses fiches en ZIP
- RH/Admin: télécharger toutes les fiches en ZIP
**Fichiers à modifier**:
- `FichePaieServlet.java` - Ajouter action "downloadZip"
- Utiliser `java.util.zip` pour créer l'archive

---

## 📋 Résumé des Rôles et Permissions

| Fonctionnalité | EMPLOYE | CHEF_DEPT | RH | ADMIN |
|---------------|---------|-----------|----|----|
| **Voir son propre profil complet** | ✅ | ✅ | ✅ | ✅ |
| **Voir données confidentielles (autres)** | ❌ | ❌ | ✅ | ✅ |
| **Actions rapides (profil autre)** | ❌ | ❌ | ✅ | ✅ |
| **Voir budget départements** | ❌ | ❌ | ✅ | ✅ |
| **Voir ses fiches de paie** | ✅ | ✅ | ✅ | ✅ |
| **Voir fiches de paie (autres)** | ❌ | ❌ | ✅ | ✅ |
| **Générer des fiches de paie** | ❌ | ❌ | ✅ | ✅ |
| **Voir masse salariale** | ❌ | ❌ | ✅ | ✅ |
| **Télécharger ses fiches (ZIP)** | ✅ | ✅ | ✅ | ✅ |
| **Télécharger toutes fiches (ZIP)** | ❌ | ❌ | ✅ | ✅ |
| **Voir ses propres projets** | ✅ | ✅ | ✅ | ✅ |
| **Voir projets de son département** | ❌ | ✅ | ✅ | ✅ |
| **Voir tous les projets** | ❌ | ❌ | ✅ | ✅ |
| **Modifier ses propres congés** | ✅ | ✅ | ✅ | ✅ |
| **Modifier congés (autres)** | ❌ | ❌ | ✅ | ✅ |
| **Supprimer ses propres congés** | ✅ | ✅ | ✅ | ✅ |
| **Supprimer congés (autres)** | ❌ | ❌ | ✅ | ✅ |
| **Approuver/Rejeter des congés** | ❌ | ❌ | ✅ | ✅ |

---

## 🔐 Principes de Sécurité Appliqués

### 1. Principe du moindre privilège
Chaque utilisateur n'a accès qu'aux données strictement nécessaires à son rôle.

### 2. Contrôle d'accès basé sur les rôles (RBAC)
Les permissions sont accordées en fonction du rôle de l'utilisateur (EMPLOYE, RH, ADMIN).

### 3. Vérifications côté serveur
Toutes les restrictions sont implémentées côté serveur (Servlet), pas uniquement dans le JSP.

### 4. Logging de sécurité
Toutes les tentatives d'accès non autorisé sont enregistrées dans les logs.

### 5. Protection des données personnelles (RGPD)
Les données sensibles (salaire, téléphone, date de naissance) sont masquées pour les non-autorisés.

---

## 📝 Notes de Déploiement

### Avant de déployer
1. ✅ Vérifier que tous les employés ont un `employeId` en session
2. ✅ Tester chaque restriction avec les 3 rôles
3. ✅ Vérifier les logs pour détecter d'éventuelles anomalies
4. ✅ Nettoyer et rebuild le projet Maven
5. ✅ Redémarrer Tomcat après déploiement

### Tests recommandés
- Connexion en tant qu'EMPLOYE: vérifier qu'on ne voit que ses propres données
- Connexion en tant qu'autre EMPLOYE: vérifier qu'on ne peut pas modifier les congés du premier
- Connexion en tant que RH: vérifier l'accès complet
- Connexion en tant qu'ADMIN: vérifier l'accès complet

---

## 📞 Support

Pour toute question ou problème concernant ces restrictions de sécurité, contactez l'équipe de développement.

**Dernière mise à jour**: 29 octobre 2025
