# 🔧 SOLUTION - Erreur 500 : ClassNotFoundException ConditionalTagSupport

## ❌ Problème

```
java.lang.ClassNotFoundException: jakarta.servlet.jsp.jstl.core.ConditionalTagSupport
```

**Cause** : Le jar `jakarta.servlet.jsp.jstl-api-3.0.1.jar` n'est **PAS déployé** dans Tomcat par Eclipse, alors qu'il est présent dans le pom.xml et dans le WAR construit par Maven.

## ✅ Vérification effectuée

```powershell
# Le WAR Maven contient bien les 2 jars JSTL :
c:\projectfinal\target\gestion-rh\WEB-INF\lib\jakarta.servlet.jsp.jstl-3.0.1.jar       ✅
c:\projectfinal\target\gestion-rh\WEB-INF\lib\jakarta.servlet.jsp.jstl-api-3.0.1.jar  ✅
```

**Mais Eclipse ne déploie QUE le jar d'implémentation (sans l'API)** → Erreur au runtime.

---

## 🚀 SOLUTION : Forcer le redéploiement dans Eclipse

### Étape 1 : Arrêter Tomcat
1. Dans Eclipse, ouvrir l'onglet **Servers** (en bas)
2. Clic droit sur **Tomcat v10.1** → **Stop**
3. Attendre que le serveur soit complètement arrêté

### Étape 2 : Clean du projet
1. Clic droit sur le projet **projectfinal** dans l'explorateur
2. **Maven** → **Update Project** (cocher "Force Update of Snapshots/Releases")
3. **Project** → **Clean...** → Sélectionner `projectfinal` → **Clean**

### Étape 3 : Supprimer l'ancien déploiement
1. Dans l'onglet **Servers**, clic droit sur **Tomcat v10.1**
2. **Add and Remove...** 
3. Retirer `projectfinal` de la liste "Configured" (le passer dans "Available")
4. **Finish**

### Étape 4 : Redéployer proprement
1. Clic droit sur **Tomcat v10.1** → **Add and Remove...**
2. Ajouter `projectfinal` de "Available" vers "Configured"
3. **Finish**

### Étape 5 : Vérifier le déploiement
Avant de démarrer Tomcat, vérifier que les 2 jars sont présents :

```powershell
Get-ChildItem "C:\Users\Administrateur\eclipse-workspace24_9\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\projectfinal\WEB-INF\lib" | Where-Object {$_.Name -like "*jstl*"}
```

**Résultat attendu** :
```
jakarta.servlet.jsp.jstl-3.0.1.jar        ✅
jakarta.servlet.jsp.jstl-api-3.0.1.jar   ✅
```

### Étape 6 : Démarrer Tomcat
1. Clic droit sur **Tomcat v10.1** → **Start**
2. Attendre le message : `Le démarrage du serveur a pris [XXX] millisecondes`

### Étape 7 : Tester
1. Ouvrir http://localhost:8080/gestion-rh/
2. Se connecter avec `claire.durand` / `claire123`
3. ✅ Le dashboard doit s'afficher **SANS erreur 500**

---

## 🔍 Pourquoi ça marche maintenant ?

Eclipse utilise parfois un **cache de déploiement** qui ne se synchronise pas toujours avec les changements du pom.xml. En forçant :
- Le clean
- La suppression/réajout du projet sur Tomcat
- Le redéploiement complet

→ Eclipse reconstruit le répertoire `wtpwebapps/projectfinal/WEB-INF/lib/` avec **TOUTES** les dépendances du pom.xml.

---

## 🛠️ Alternative : Copie manuelle (temporaire)

Si le redéploiement ne fonctionne pas immédiatement, vous pouvez copier manuellement le jar manquant :

```powershell
# 1. Arrêter Tomcat dans Eclipse

# 2. Copier le jar
Copy-Item `
  "C:\Users\Administrateur\eclipse-workspace24_9\projectfinal\target\gestion-rh\WEB-INF\lib\jakarta.servlet.jsp.jstl-api-3.0.1.jar" `
  "C:\Users\Administrateur\eclipse-workspace24_9\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\projectfinal\WEB-INF\lib\"

# 3. Redémarrer Tomcat
```

⚠️ **Attention** : Cette copie sera écrasée au prochain redéploiement Eclipse. Préférez toujours la méthode officielle (Clean + Add/Remove).

---

## 📊 Récapitulatif des modifications

| Fichier | Status | Commentaire |
|---------|--------|-------------|
| `pom.xml` | ✅ OK | Les 2 dépendances JSTL sont déclarées |
| `target/gestion-rh/WEB-INF/lib/` | ✅ OK | Maven construit le WAR avec les 2 jars |
| `.metadata/.../wtpwebapps/.../lib/` | ❌ INCOMPLET | Eclipse ne déploie qu'1 seul jar |

**Solution** : Forcer Eclipse à redéployer proprement.

---

## ✅ Test de validation

Après redéploiement, vérifier dans les logs Tomcat :

```
2025-10-29 XX:XX:XX [http-nio-8080-exec-X] DEBUG c.gestionrh.servlet.DashboardServlet - Affichage du tableau de bord
```

✅ **SANS** l'erreur `ClassNotFoundException: ConditionalTagSupport`

---

## 📝 Notes

- Ce problème est **spécifique à Eclipse WTP** (Web Tools Platform)
- Maven CLI (`mvn clean package`) génère toujours le bon WAR
- Le déploiement manuel du WAR dans Tomcat fonctionne aussi
- C'est le mécanisme de déploiement incrémental d'Eclipse qui pose problème
