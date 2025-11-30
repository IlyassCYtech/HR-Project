# Sécurité des Formulaires d'Employé - Project Final

## 📋 Vue d'ensemble

Cette documentation décrit la sécurisation complète des formulaires d'employé dans le projet **projectfinal** (version JEE sans Spring Boot), inspirée de la version Spring Boot (**projetfinalspringboot**).

---

## 🔐 Sécurités Implémentées

### 1. **Contrôle d'Accès (Authorization)**

#### Règles d'accès au formulaire d'ajout (`showAddForm`)
- ✅ **ADMIN** : Accès complet
- ✅ **RH** : Accès complet
- ❌ **EMPLOYE** : Accès refusé
- ❌ **CHEF_DEPT/CHEF_PROJET** : Accès refusé

```java
// Seuls ADMIN et RH peuvent créer des employés
if (utilisateur == null || 
    (utilisateur.getRole() != Utilisateur.Role.ADMIN && 
     utilisateur.getRole() != Utilisateur.Role.RH)) {
    // Redirection avec message d'erreur
}
```

#### Règles d'accès au formulaire de modification (`showEditForm`)
- ✅ **ADMIN** : Peut modifier n'importe quel employé
- ✅ **RH** : Peut modifier n'importe quel employé
- ✅ **EMPLOYE** : Peut modifier uniquement son propre profil (avec restrictions)
- ❌ Modification d'un autre employé : Accès refusé

```java
boolean isAdminOrRH = utilisateur != null && 
    (utilisateur.getRole() == Utilisateur.Role.ADMIN || 
     utilisateur.getRole() == Utilisateur.Role.RH);

boolean isSelf = utilisateur != null && utilisateur.getEmploye() != null && 
                 utilisateur.getEmploye().getId().equals(id);

// Si ce n'est ni admin/RH ni l'employé lui-même, refuser l'accès
if (!isAdminOrRH && !isSelf) {
    // Accès refusé
}
```

---

### 2. **Champs en Lecture Seule (Read-only Fields)**

Lorsqu'un employé modifie son propre profil (`isSelfEdit = true`), les champs suivants sont en **lecture seule** :

| Champ | Description | Raison |
|-------|-------------|--------|
| `nom` | Nom de famille | Données personnelles sensibles |
| `prenom` | Prénom | Données personnelles sensibles |
| `dateNaissance` | Date de naissance | Données personnelles sensibles |
| `dateEmbauche` | Date d'embauche | Données contractuelles |
| `dateFin` | Date de fin de contrat | Données contractuelles |
| `poste` | Poste occupé | Données professionnelles |
| `grade` | Grade | Décision RH |
| `salaireBase` | Salaire | Information confidentielle |
| `departementId` | Département | Décision RH |
| `managerId` | Manager | Décision RH |
| `statut` | Statut de l'employé | Décision RH |

**Un employé peut modifier :**
- Email (pour rester joignable)
- Téléphone (pour rester joignable)
- Adresse (mise à jour personnelle)

---

### 3. **Section Rôle Utilisateur**

#### Visibilité
La section "Compte Utilisateur" est visible **uniquement si** :
1. L'employé possède un compte utilisateur (`hasUtilisateur = true`)
2. **ET** l'utilisateur connecté est ADMIN ou RH (`isAdminOrRH = true`)

```jsp
<c:if test="${hasUtilisateur && isAdminOrRH}">
    <!-- Section modification du rôle -->
</c:if>
```

#### Modification du Rôle
- ✅ **ADMIN** : Peut changer le rôle d'un utilisateur vers n'importe quel rôle
- ✅ **RH** : Peut changer le rôle d'un utilisateur vers n'importe quel rôle
- ❌ **EMPLOYE** : Ne voit pas cette section

**Rôles disponibles :**
- `EMPLOYE`
- `RH`
- `ADMIN`
- `CHEF_DEPT`
- `CHEF_PROJET`

#### Logique Backend
```java
// Dans updateEmploye()
if (isAdminOrRH) {
    String utilisateurRole = request.getParameter("utilisateurRole");
    if (utilisateurRole != null && !utilisateurRole.isEmpty()) {
        Utilisateur employeUtilisateur = utilisateurDAO.findByEmployeId(id);
        if (employeUtilisateur != null) {
            Utilisateur.Role newRole = Utilisateur.Role.valueOf(utilisateurRole);
            employeUtilisateur.setRole(newRole);
            utilisateurDAO.update(employeUtilisateur);
        }
    }
}
```

---

### 4. **Validation des Inputs (Frontend)**

#### Validation Nom/Prénom
- **Pattern** : `/^[a-zA-ZÀ-ÿ\s\-]+$/` (lettres, espaces, tirets, accents)
- **Longueur** : 2-50 caractères
- **Message d'erreur** : "Le nom/prénom ne doit contenir que des lettres, espaces et tirets."

#### Validation Matricule
- **Pattern** : `[A-Za-z0-9_-]{3,20}` (alphanumérique, tirets, underscores)
- **Longueur** : 3-20 caractères
- **Unique** : Vérifié côté backend
- **Read-only** : En mode modification

#### Validation Téléphone
- **Format** : Minimum 10 chiffres (espaces et caractères spéciaux ignorés)
- **Optionnel** : Peut être vide
- **Message d'erreur** : "Numéro de téléphone invalide (10 chiffres minimum)."

#### Validation Date de Naissance
- **Min** : 1950-01-01
- **Max** : 2007-12-31 (au moins 18 ans)
- **Validation** : L'employé doit avoir au moins 18 ans à la date d'embauche
- **Message d'erreur** : "L'employé doit être majeur (18 ans) à la date d'embauche."

#### Validation Dates Embauche/Fin
- **Date de fin** : Doit être postérieure à la date d'embauche
- **Message d'erreur** : "La date de fin doit être postérieure à la date d'embauche."

#### Validation Salaire
- **Min** : 0.01
- **Max** : 999999.99
- **Step** : 0.01
- **Format** : Décimal avec 2 décimales

---

### 5. **Attributs de Sécurité passés à la Vue**

```java
// Attributs ajoutés dans showAddForm() et showEditForm()
request.setAttribute("canViewEmploye", isAdminOrRH || isSelf);
request.setAttribute("isAdminOrRH", isAdminOrRH);
request.setAttribute("isSelfEdit", isSelf && !isAdminOrRH);
request.setAttribute("hasUtilisateur", employeUtilisateur != null);
request.setAttribute("employeUtilisateur", employeUtilisateur);
```

| Attribut | Type | Description |
|----------|------|-------------|
| `canViewEmploye` | boolean | L'utilisateur peut-il voir/modifier le formulaire ? |
| `isAdminOrRH` | boolean | L'utilisateur est-il ADMIN ou RH ? |
| `isSelfEdit` | boolean | L'employé modifie-t-il son propre profil ? |
| `hasUtilisateur` | boolean | L'employé a-t-il un compte utilisateur ? |
| `employeUtilisateur` | Utilisateur | L'objet utilisateur associé à l'employé |

---

## 🆕 Nouvelles Méthodes DAO

### UtilisateurDAO.findByEmployeId()

```java
/**
 * Trouve un utilisateur par l'ID de l'employé associé
 * @param employeId L'ID de l'employé
 * @return L'utilisateur trouvé ou null
 */
Utilisateur findByEmployeId(Long employeId);
```

**Implémentation :**
```java
@Override
public Utilisateur findByEmployeId(Long employeId) {
    return TransactionUtil.executeInTransaction(session -> {
        Query<Utilisateur> query = session.createQuery(
            "SELECT u FROM Utilisateur u LEFT JOIN FETCH u.employe e WHERE e.id = :employeId", 
            Utilisateur.class);
        query.setParameter("employeId", employeId);
        List<Utilisateur> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    });
}
```

---

## 📝 Structure JSP Sécurisée

```jsp
<!-- Sécurité : accès réservé RH, ADMIN ou l'employé lui-même -->
<c:if test="${canViewEmploye}">
    <!-- Formulaire avec champs conditionnels -->
    <form id="employeForm" method="POST">
        <!-- Champs avec ${isSelfEdit ? 'readonly' : ''} -->
        
        <!-- Section Rôle Utilisateur (visible uniquement si hasUtilisateur && isAdminOrRH) -->
        <c:if test="${hasUtilisateur && isAdminOrRH}">
            <div class="card">
                <select name="utilisateurRole">
                    <option value="EMPLOYE">EMPLOYE</option>
                    <option value="RH">RH</option>
                    <option value="ADMIN">ADMIN</option>
                </select>
            </div>
        </c:if>
    </form>
</c:if>

<!-- Message accès refusé -->
<c:if test="${!canViewEmploye}">
    <div class="alert alert-danger">
        Accès refusé : vous n'avez pas les droits pour modifier ce profil employé.
    </div>
</c:if>

<!-- JavaScript de validation -->
<script>
    // Validation en temps réel et à la soumission
</script>
```

---

## 🔄 Comparaison avec la Version Spring Boot

| Fonctionnalité | Spring Boot | JEE (projectfinal) |
|----------------|-------------|-------------------|
| Contrôle d'accès | Spring Security | Servlet + session |
| Rôles utilisateur | @PreAuthorize | Vérification manuelle |
| Champs read-only | Thymeleaf `th:readonly` | JSP `${isSelfEdit ? 'readonly' : ''}` |
| Validation frontend | HTML5 + JS | HTML5 + JS (identique) |
| Modification rôle | ✅ | ✅ |
| Section utilisateur | ✅ | ✅ |

---

## ✅ Tests de Sécurité Recommandés

### Scénarios à Tester

1. **Accès au formulaire d'ajout**
   - ✅ ADMIN peut accéder
   - ✅ RH peut accéder
   - ❌ EMPLOYE ne peut pas accéder

2. **Modification d'un employé**
   - ✅ ADMIN peut modifier n'importe quel employé
   - ✅ RH peut modifier n'importe quel employé
   - ✅ EMPLOYE peut modifier son propre profil
   - ❌ EMPLOYE ne peut pas modifier un autre employé

3. **Champs en lecture seule**
   - ✅ Un employé ne peut pas modifier son salaire
   - ✅ Un employé ne peut pas modifier son grade
   - ✅ Un employé peut modifier son email et téléphone

4. **Modification du rôle**
   - ✅ ADMIN peut changer le rôle d'un utilisateur
   - ✅ RH peut changer le rôle d'un utilisateur
   - ❌ EMPLOYE ne voit pas la section rôle

5. **Validation des inputs**
   - ✅ Nom avec caractères spéciaux rejeté
   - ✅ Téléphone avec moins de 10 chiffres rejeté
   - ✅ Date de naissance d'un mineur rejetée
   - ✅ Date de fin antérieure à la date d'embauche rejetée

---

## 📚 Fichiers Modifiés

1. **Servlet**
   - `EmployeServlet.java`
     - `showAddForm()` : Ajout contrôle d'accès
     - `showEditForm()` : Ajout contrôle d'accès + attributs sécurité
     - `updateEmploye()` : Ajout gestion rôle utilisateur

2. **DAO**
   - `UtilisateurDAO.java` : Ajout méthode `findByEmployeId()`
   - `UtilisateurDAOImpl.java` : Implémentation `findByEmployeId()`

3. **JSP**
   - `employes/form.jsp`
     - Ajout attributs `id` sur les inputs
     - Ajout validation HTML5 (pattern, minlength, maxlength, min, max)
     - Ajout champs read-only conditionnels `${isSelfEdit ? 'readonly' : ''}`
     - Ajout section "Compte Utilisateur" avec modification du rôle
     - Ajout JavaScript de validation
     - Ajout message "Accès refusé"

---

## 🎯 Conclusion

La sécurité des formulaires d'employé est maintenant **complète et conforme** à la version Spring Boot :

✅ **Contrôle d'accès granulaire** (ADMIN, RH, EMPLOYE)  
✅ **Champs en lecture seule** pour l'auto-édition  
✅ **Section modification du rôle** (visible seulement pour ADMIN/RH)  
✅ **Validation côté client** (JavaScript en temps réel)  
✅ **Validation côté serveur** (vérification unicité email/matricule)  
✅ **Messages d'erreur explicites**  

---

**Date de création** : 30 novembre 2025  
**Auteur** : GitHub Copilot  
**Version** : 1.0
