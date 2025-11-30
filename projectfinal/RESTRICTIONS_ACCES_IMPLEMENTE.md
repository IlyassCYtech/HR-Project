# Restrictions d'accès implémentées

## Date : 28 octobre 2025

## Modifications apportées

### 1. Congés - Masquage des motifs pour les employés

**Fichiers modifiés :**
- `src/main/webapp/WEB-INF/jsp/conges/list.jsp`
- `src/main/webapp/WEB-INF/jsp/conges/show.jsp`

**Comportement :**
- ✅ **ADMIN et RH** : Voient tous les motifs des congés
- ✅ **EMPLOYE** : Voient "Confidentiel" à la place des motifs dans la liste
- ✅ **EMPLOYE** : Le champ motif n'est pas affiché dans la page de détail

**Code ajouté dans list.jsp (ligne ~152-168) :**
```jsp
<c:choose>
    <c:when test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
        <c:choose>
            <c:when test="${not empty conge.motif}">
                ${conge.motif}
            </c:when>
            <c:otherwise>
                <span style="color: #999999;">-</span>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <span style="color: #999999; font-style: italic;">Confidentiel</span>
    </c:otherwise>
</c:choose>
```

**Code ajouté dans show.jsp (ligne ~97-104) :**
```jsp
<c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
    <c:if test="${not empty conge.motif}">
        <div>
            <label class="form-label">MOTIF</label>
            <p style="font-size: 14px; color: #1A1A1A; line-height: 1.6; margin-top: 8px; white-space: pre-line;">
                ${conge.motif}
            </p>
        </div>
    </c:if>
</c:if>
```

---

### 2. Fiches de paie - Accès restreint aux propres fiches

**Fichiers modifiés :**
- `src/main/java/com/gestionrh/servlet/FichePaieServlet.java`

**Imports ajoutés :**
```java
import com.gestionrh.model.Utilisateur;
import jakarta.servlet.http.HttpSession;
```

**Comportement :**
- ✅ **ADMIN et RH** : Voient toutes les fiches de paie de tous les employés
- ✅ **EMPLOYE** : Voient UNIQUEMENT leurs propres fiches de paie
- ✅ **Sécurité** : Tentative d'accès direct à une fiche d'un autre employé = Erreur

**Code ajouté dans listFichesPaie() (ligne ~137-170) :**
```java
HttpSession session = request.getSession();
Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");

// Support des deux formats: "mois" (YYYY-MM) ou "mois" + "annee" séparés
String moisAnneeStr = request.getParameter("mois");
String employeIdStr = request.getParameter("employeId");

// Compatibilité avec ancien format
if (employeIdStr == null || employeIdStr.isEmpty()) {
    employeIdStr = request.getParameter("employe");
}

// *** FILTRAGE PAR RÔLE : Les employés ne voient que leurs propres fiches ***
if (utilisateur.getRole() == Utilisateur.Role.EMPLOYE) {
    Long employeIdFromSession = (Long) session.getAttribute("employeId");
    if (employeIdFromSession != null) {
        employeIdStr = employeIdFromSession.toString();
        logger.info("EMPLOYE role détecté - filtrage automatique sur employeId: {}", employeIdFromSession);
    } else {
        // Si pas d'employeId en session, chercher par email
        String email = utilisateur.getUsername();
        if (email != null && email.contains("@")) {
            Employe employe = employeDAO.findByEmail(email);
            if (employe != null) {
                employeIdStr = employe.getId().toString();
                session.setAttribute("employeId", employe.getId());
                logger.info("EMPLOYE role - employeId {} trouvé via email et stocké", employe.getId());
            }
        }
    }
}
```

**Code ajouté dans showFichePaie() (ligne ~247-275) :**
```java
// *** SÉCURITÉ : Un employé ne peut voir que ses propres fiches ***
HttpSession session = request.getSession();
Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");

if (utilisateur.getRole() == Utilisateur.Role.EMPLOYE) {
    Long employeIdFromSession = (Long) session.getAttribute("employeId");
    if (employeIdFromSession == null) {
        String email = utilisateur.getUsername();
        if (email != null && email.contains("@")) {
            Employe employe = employeDAO.findByEmail(email);
            if (employe != null) {
                employeIdFromSession = employe.getId();
                session.setAttribute("employeId", employe.getId());
            }
        }
    }
    
    // Vérifier que la fiche appartient bien à l'employé connecté
    if (employeIdFromSession == null || !fichePaie.getEmploye().getId().equals(employeIdFromSession)) {
        logger.warn("Tentative d'accès non autorisé à la fiche {} par employé {}", id, employeIdFromSession);
        request.setAttribute("error", "Vous n'êtes pas autorisé à consulter cette fiche de paie");
        listFichesPaie(request, response);
        return;
    }
}
```

---

### 3. Lien de profil corrigé dans navbar.jsp

**Fichier modifié :**
- `src/main/webapp/WEB-INF/jsp/layout/navbar.jsp`

**Changement :**
- ❌ Ancien : `href="${pageContext.request.contextPath}/profil"`
- ✅ Nouveau : `href="${pageContext.request.contextPath}/app/profil"`

**Note :** navbar.jsp semble être un fichier obsolète (non inclus dans header.jsp)

---

## Tests à effectuer

### Congés
1. ✅ Se connecter en tant qu'**ADMIN** → Voir tous les motifs
2. ✅ Se connecter en tant qu'**RH** → Voir tous les motifs
3. ✅ Se connecter en tant qu'**EMPLOYE** → Voir "Confidentiel" dans la liste
4. ✅ Ouvrir le détail d'un congé en tant qu'EMPLOYE → Le motif ne s'affiche pas

### Fiches de paie
1. ✅ Se connecter en tant qu'**ADMIN** → Voir toutes les fiches
2. ✅ Se connecter en tant qu'**RH** → Voir toutes les fiches
3. ✅ Se connecter en tant qu'**EMPLOYE** → Voir uniquement ses fiches
4. ✅ Essayer d'accéder à `/app/fiches-paie?action=show&id=X` (X = fiche d'un autre) en tant qu'EMPLOYE → Message d'erreur

### Profil
1. ✅ Cliquer sur le nom d'utilisateur dans la sidebar → Accès au profil
2. ✅ Vérifier que tous les liens dans profil.jsp fonctionnent

---

## Sécurité

### Niveau de protection
- ✅ **Backend** : Filtrage côté serveur dans les servlets (FichePaieServlet)
- ✅ **Frontend** : Masquage visuel dans les JSP (conges/list.jsp, conges/show.jsp)
- ✅ **Session** : Utilisation de `employeId` en session pour éviter les requêtes répétées
- ✅ **Logs** : Journalisation des tentatives d'accès non autorisé

### Points d'attention
- La sécurité backend est ESSENTIELLE car un utilisateur pourrait manipuler l'URL
- Le masquage frontend améliore l'UX mais ne suffit pas
- Les logs permettent de détecter les tentatives de contournement

---

## Conformité au cahier des charges

✅ **Employés peuvent voir leurs congés mais PAS les motifs** (sauf RH/ADMIN)
✅ **Employés peuvent voir UNIQUEMENT leurs propres fiches de paie** (RH/ADMIN voient tout)
✅ **Liens de profil fonctionnent correctement**

## Prochaines étapes suggérées

1. Tester en conditions réelles avec différents rôles
2. Vérifier les logs pour s'assurer du bon fonctionnement
3. Ajouter des tests unitaires pour les méthodes de sécurité
4. Documenter les règles d'accès dans un manuel utilisateur
