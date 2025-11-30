# Nouvelles restrictions d'accès - 29 octobre 2025

## ✅ Modifications COMPLÉTÉES

### 1. Profil employé - Données confidentielles masquées

**Fichier :** `employes/show.jsp`

#### Modifications :
- Ajout d'une variable `canViewConfidential` qui vérifie :
  ```jsp
  ${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH' or sessionScope.employeId == employe.id}
  ```

#### Données masquées pour les non-autorisés :
- ✅ **Salaire de base** : Affiche "Confidentiel" avec icône 🔒
- ✅ **Téléphone** : Affiche "Confidentiel" avec icône 🔒
- ✅ **Date de naissance** : Affiche "Confidentiel" avec icône 🔒

#### Qui peut voir quoi :
| Utilisateur | Salaire | Téléphone | Date naissance |
|-------------|---------|-----------|----------------|
| **ADMIN/RH** | ✅ Tous | ✅ Tous | ✅ Tous |
| **Employé (son profil)** | ✅ Le sien | ✅ Le sien | ✅ La sienne |
| **Employé (autre profil)** | ❌ Confidentiel | ❌ Confidentiel | ❌ Confidentiel |

---

### 2. Profil employé - Actions rapides masquées

**Fichier :** `employes/show.jsp`

#### Modification :
Section "Actions rapides" enveloppée dans :
```jsp
<c:if test="${canViewConfidential}">
    <!-- Actions rapides : Voir fiches, congés, projets -->
</c:if>
```

#### Comportement :
- ✅ **ADMIN/RH** : Voient toutes les actions rapides pour tous les employés
- ✅ **Employé** : Voit les actions rapides uniquement sur son propre profil
- ❌ **Employé** : Ne voit PAS les actions rapides sur les profils des autres

---

### 3. Fiches de paie - Génération bloquée pour employés

**Fichiers modifiés :**
1. `fiches-paie/list.jsp` (frontend)
2. `FichePaieServlet.java` (backend)

#### Bouton masqué (JSP) :
```jsp
<c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
    <a href="${pageContext.request.contextPath}/app/fiches-paie?action=generate" class="btn btn-primary">
        <i class="fas fa-file-invoice-dollar"></i>Générer des fiches
    </a>
</c:if>
```

#### Sécurité backend (Servlet) :

**Dans `showGenerateForm()` :**
```java
// *** SÉCURITÉ : Seuls ADMIN/RH peuvent générer des fiches ***
HttpSession session = request.getSession();
Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");

if (utilisateur.getRole() != Utilisateur.Role.ADMIN && utilisateur.getRole() != Utilisateur.Role.RH) {
    logger.warn("Tentative d'accès non autorisé à la génération de fiches par: {}", utilisateur.getUsername());
    request.setAttribute("error", "Vous n'êtes pas autorisé à générer des fiches de paie");
    listFichesPaie(request, response);
    return;
}
```

**Dans `doPost()` :**
```java
// *** SÉCURITÉ : Seuls ADMIN/RH peuvent créer/générer des fiches ***
HttpSession session = request.getSession();
Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");

if (utilisateur.getRole() != Utilisateur.Role.ADMIN && utilisateur.getRole() != Utilisateur.Role.RH) {
    logger.warn("Tentative de création/génération de fiche non autorisée par: {}", utilisateur.getUsername());
    request.setAttribute("error", "Vous n'êtes pas autorisé à créer ou générer des fiches de paie");
    listFichesPaie(request, response);
    return;
}
```

#### Comportement :
- ✅ **ADMIN/RH** : Peuvent générer des fiches
- ❌ **Employés** : Bouton caché + accès backend bloqué avec message d'erreur
- 📝 **Logs** : Tentatives d'accès non autorisé enregistrées

---

## 🚧 Modifications À FAIRE

### 4. Téléchargement ZIP de toutes les fiches (employé)

**Objectif :** Permettre à un employé de télécharger toutes ses fiches de paie en un seul fichier ZIP.

#### Implémentation suggérée :

**Fichier :** `FichePaieServlet.java`

**Nouvelle action dans `doGet()` :**
```java
case "downloadAllZip":
    downloadAllFichesAsZip(request, response);
    break;
```

**Nouvelle méthode :**
```java
private void downloadAllFichesAsZip(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    
    HttpSession session = request.getSession();
    Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
    Long employeId = (Long) session.getAttribute("employeId");
    
    // Pour EMPLOYE : seulement ses fiches
    if (utilisateur.getRole() == Utilisateur.Role.EMPLOYE) {
        if (employeId == null) {
            String email = utilisateur.getUsername();
            Employe employe = employeDAO.findByEmail(email);
            if (employe != null) {
                employeId = employe.getId();
            }
        }
    } else {
        // Pour ADMIN/RH : demander l'employeId en paramètre
        String empIdStr = request.getParameter("employeId");
        if (empIdStr != null) {
            employeId = Long.parseLong(empIdStr);
        }
    }
    
    if (employeId == null) {
        request.setAttribute("error", "Aucun employé spécifié");
        listFichesPaie(request, response);
        return;
    }
    
    List<FichePaie> fiches = fichePaieDAO.findByEmployeId(employeId);
    
    if (fiches.isEmpty()) {
        request.setAttribute("error", "Aucune fiche de paie disponible");
        listFichesPaie(request, response);
        return;
    }
    
    // Créer le ZIP
    response.setContentType("application/zip");
    Employe employe = employeDAO.findById(employeId);
    String filename = "fiches_paie_" + employe.getNom() + "_" + employe.getPrenom() + ".zip";
    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    
    try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
        for (FichePaie fiche : fiches) {
            // Générer le PDF pour chaque fiche
            byte[] pdfBytes = generatePDFBytes(fiche);
            
            String entryName = String.format("fiche_%s_%02d_%d.pdf", 
                fiche.getEmploye().getNom(), 
                fiche.getMois(), 
                fiche.getAnnee());
            
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(pdfBytes);
            zos.closeEntry();
        }
    }
    
    logger.info("ZIP de {} fiches téléchargé pour employé {}", fiches.size(), employeId);
}

private byte[] generatePDFBytes(FichePaie fiche) throws IOException {
    // Extraire la logique de génération PDF existante
    // et retourner les bytes au lieu d'écrire directement dans la response
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // ... code de génération PDF ...
    return baos.toByteArray();
}
```

**Ajout du bouton dans `list.jsp` :**
```jsp
<c:if test="${utilisateur.role eq 'EMPLOYE' and not empty fichesPaie}">
    <a href="${pageContext.request.contextPath}/app/fiches-paie?action=downloadAllZip" 
       class="btn btn-secondary">
        <i class="fas fa-file-archive" style="margin-right: 8px;"></i>
        Télécharger toutes mes fiches (ZIP)
    </a>
</c:if>
```

**Imports nécessaires :**
```java
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
```

---

### 5. Édition/Création de fiche personnalisée (ADMIN/RH)

**Objectif :** Permettre à ADMIN/RH de créer ou modifier manuellement une fiche de paie.

#### Implémentation suggérée :

**Nouvelle page JSP :** `fiches-paie/edit.jsp`

**Nouvelle action dans `doGet()` :**
```java
case "edit":
    showEditForm(request, response);
    break;
case "create-custom":
    showCreateCustomForm(request, response);
    break;
```

**Nouvelle méthode :**
```java
private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    
    // Vérification rôle
    HttpSession session = request.getSession();
    Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
    
    if (utilisateur.getRole() != Utilisateur.Role.ADMIN && utilisateur.getRole() != Utilisateur.Role.RH) {
        request.setAttribute("error", "Accès non autorisé");
        listFichesPaie(request, response);
        return;
    }
    
    Long id = Long.parseLong(request.getParameter("id"));
    FichePaie fiche = fichePaieDAO.findById(id);
    
    if (fiche == null) {
        request.setAttribute("error", "Fiche non trouvée");
        listFichesPaie(request, response);
        return;
    }
    
    request.setAttribute("fiche", fiche);
    request.setAttribute("mode", "edit");
    request.getRequestDispatcher("/WEB-INF/jsp/fiches-paie/edit.jsp").forward(request, response);
}

private void showCreateCustomForm(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    
    // Vérification rôle
    HttpSession session = request.getSession();
    Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
    
    if (utilisateur.getRole() != Utilisateur.Role.ADMIN && utilisateur.getRole() != Utilisateur.Role.RH) {
        request.setAttribute("error", "Accès non autorisé");
        listFichesPaie(request, response);
        return;
    }
    
    List<Employe> employes = employeDAO.findActifs();
    request.setAttribute("employes", employes);
    request.setAttribute("mode", "create");
    request.getRequestDispatcher("/WEB-INF/jsp/fiches-paie/edit.jsp").forward(request, response);
}
```

**Nouvelle action dans `doPost()` :**
```java
case "update":
    updateFichePaie(request, response);
    break;
case "create-custom":
    createCustomFichePaie(request, response);
    break;
```

**Boutons dans `list.jsp` (pour ADMIN/RH) :**
```jsp
<c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH'}">
    <a href="${pageContext.request.contextPath}/app/fiches-paie?action=edit&id=${fiche.id}" 
       class="btn btn-sm btn-secondary" title="Éditer">
        <i class="fas fa-edit"></i>
    </a>
</c:if>
```

---

### 6. Chef département voit projets de son département

**Objectif :** Un CHEF_DEPT voit tous les projets de son département.

**Fichier :** `ProjetServlet.java`

**Modification dans `listProjets()` :**
```java
// Après la vérification EMPLOYE, ajouter :
if (utilisateur != null && utilisateur.getRole() == Utilisateur.Role.CHEF_DEPT) {
    Long employeId = (Long) request.getSession().getAttribute("employeId");
    if (employeId != null) {
        Employe chef = employeDAO.findById(employeId);
        if (chef != null && chef.getDepartement() != null) {
            Long deptId = chef.getDepartement().getId();
            logger.info("CHEF_DEPT détecté - Filtrage par département {}", deptId);
            
            // Filtrer les projets par département
            final Long finalDeptId = deptId;
            projets = projets.stream()
                .filter(p -> p.getDepartement() != null && 
                            p.getDepartement().getId().equals(finalDeptId))
                .collect(Collectors.toList());
        }
    }
}
```

---

### 7. Bloquer modification congés des autres

**Objectif :** Un employé ne peut modifier que SES PROPRES demandes de congé.

**Fichier :** `CongeAbsenceServlet.java`

**Modification dans `showEditForm()` :**
```java
private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    
    Long id = Long.parseLong(request.getParameter("id"));
    CongeAbsence conge = congeDAO.findById(id);
    
    if (conge == null) {
        request.setAttribute("error", "Demande de congé non trouvée");
        listConges(request, response);
        return;
    }
    
    // *** SÉCURITÉ : Vérifier les droits de modification ***
    HttpSession session = request.getSession();
    Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
    
    // Si ce n'est ni ADMIN ni RH
    if (utilisateur.getRole() != Utilisateur.Role.ADMIN && 
        utilisateur.getRole() != Utilisateur.Role.RH) {
        
        // Vérifier que c'est bien son propre congé
        Long employeId = (Long) session.getAttribute("employeId");
        if (employeId == null) {
            String email = utilisateur.getUsername();
            Employe employe = employeDAO.findByEmail(email);
            if (employe != null) {
                employeId = employe.getId();
                session.setAttribute("employeId", employe.getId());
            }
        }
        
        if (employeId == null || !conge.getEmploye().getId().equals(employeId)) {
            logger.warn("Tentative de modification d'un congé non autorisée par: {}", utilisateur.getUsername());
            request.setAttribute("error", "Vous ne pouvez modifier que vos propres demandes de congé");
            listConges(request, response);
            return;
        }
        
        // Vérifier que le congé n'est pas déjà approuvé/rejeté
        if (conge.getStatut() != StatutDemande.EN_ATTENTE) {
            request.setAttribute("error", "Vous ne pouvez plus modifier cette demande (statut: " + conge.getStatut() + ")");
            listConges(request, response);
            return;
        }
    }
    
    // ... reste du code ...
}
```

**Même chose dans `updateConge()` :**
```java
private void updateConge(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    
    Long id = Long.parseLong(request.getParameter("id"));
    CongeAbsence conge = congeDAO.findById(id);
    
    if (conge == null) {
        request.setAttribute("error", "Demande de congé non trouvée");
        listConges(request, response);
        return;
    }
    
    // *** MÊME VÉRIFICATION QUE CI-DESSUS ***
    HttpSession session = request.getSession();
    Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
    
    if (utilisateur.getRole() != Utilisateur.Role.ADMIN && 
        utilisateur.getRole() != Utilisateur.Role.RH) {
        
        Long employeId = (Long) session.getAttribute("employeId");
        if (employeId == null) {
            String email = utilisateur.getUsername();
            Employe employe = employeDAO.findByEmail(email);
            if (employe != null) {
                employeId = employe.getId();
            }
        }
        
        if (employeId == null || !conge.getEmploye().getId().equals(employeId)) {
            logger.warn("Tentative de modification d'un congé non autorisée");
            request.setAttribute("error", "Vous ne pouvez modifier que vos propres demandes");
            listConges(request, response);
            return;
        }
        
        if (conge.getStatut() != StatutDemande.EN_ATTENTE) {
            request.setAttribute("error", "Demande déjà traitée, modification impossible");
            listConges(request, response);
            return;
        }
    }
    
    // ... mise à jour ...
}
```

**Masquer bouton "Modifier" dans `list.jsp` :**
```jsp
<c:if test="${utilisateur.role eq 'ADMIN' or utilisateur.role eq 'RH' or 
             (sessionScope.employeId == conge.employe.id and conge.statut.name() == 'EN_ATTENTE')}">
    <a href="${pageContext.request.contextPath}/app/conges-absences?action=edit&id=${conge.id}" 
       class="btn btn-sm btn-secondary" title="Modifier">
        <i class="fas fa-edit"></i>
    </a>
</c:if>
```

---

## 📊 Résumé des modifications

| Fonctionnalité | Status | Fichiers modifiés |
|----------------|--------|-------------------|
| Données confidentielles masquées | ✅ FAIT | `employes/show.jsp` |
| Actions rapides masquées | ✅ FAIT | `employes/show.jsp` |
| Génération fiches bloquée | ✅ FAIT | `fiches-paie/list.jsp`, `FichePaieServlet.java` |
| Téléchargement ZIP fiches | ⏳ À FAIRE | `FichePaieServlet.java` |
| Édition/Création fiche custom | ⏳ À FAIRE | `FichePaieServlet.java`, nouvelle JSP |
| Chef dept voit ses projets | ⏳ À FAIRE | `ProjetServlet.java` |
| Bloquer modif congés autres | ⏳ À FAIRE | `CongeAbsenceServlet.java`, `list.jsp` |

---

## 🧪 Tests nécessaires

### Tests déjà effectuables :
1. ✅ Se connecter en EMPLOYE → Voir profil d'un collègue → Données confidentielles masquées
2. ✅ Se connecter en EMPLOYE → Essayer d'accéder à `/app/fiches-paie?action=generate` → Erreur
3. ✅ Se connecter en ADMIN → Voir tous les profils avec toutes les données

### Tests à effectuer après implémentation :
4. ⏳ EMPLOYE télécharge toutes ses fiches en ZIP
5. ⏳ ADMIN crée/édite une fiche personnalisée
6. ⏳ CHEF_DEPT voit uniquement les projets de son département
7. ⏳ EMPLOYE ne peut pas modifier un congé d'un collègue

---

## 📝 Notes techniques

### Variables de session importantes :
- `utilisateur` : Objet Utilisateur avec le rôle
- `employeId` : ID de l'employé connecté (performance)
- `username` : Email de l'utilisateur

### Vérifications de sécurité :
```java
// Vérifier si c'est son propre profil
sessionScope.employeId == employe.id

// Vérifier le rôle
utilisateur.getRole() == Utilisateur.Role.ADMIN
utilisateur.getRole() == Utilisateur.Role.RH
utilisateur.getRole() == Utilisateur.Role.EMPLOYE
```

### Logs de sécurité :
Toutes les tentatives d'accès non autorisé doivent être loggées :
```java
logger.warn("Tentative d'accès non autorisé par: {}", utilisateur.getUsername());
```

---

**Date de dernière mise à jour :** 29 octobre 2025  
**Status global :** 3/7 tâches complétées (43%)
