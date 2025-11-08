# ArguMentor - Guide de Build

## 🚀 Quick Start

### Option 1 : Menu Interactif (Recommandé)
```cmd
buildhelper.bat
```
Interface menu pour toutes les opérations de build.

### Option 2 : Ligne de Commande
```cmd
autobuild.bat debug      # Build debug APK
autobuild.bat release    # Build release APK
autobuild.bat test       # Run unit tests
autobuild.bat all        # Clean + Build + Test
```

---

## 📋 Scripts Disponibles

### `autobuild.bat` - Build Automatisé
Script principal avec gestion intelligente des erreurs.

**Syntaxe :**
```cmd
autobuild.bat [clean|debug|release|test|all]
```

**Options :**
- `clean` - Nettoie les artefacts de build
- `debug` - Build APK debug (défaut)
- `release` - Build APK release
- `test` - Execute les tests unitaires
- `all` - Clean + Build + Tests

**Exemple :**
```cmd
autobuild.bat debug
```

**En cas d'erreur :**
Le script génère automatiquement :
1. `build_logs/error_report_YYYYMMDD_HHMMSS.txt` - Rapport détaillé
2. `build_logs/ai_prompt.txt` - Prompt formaté pour IA
3. `build_logs/build.log` - Log complet du build

### `clean.bat` - Nettoyage
Nettoie les caches et artefacts de build.

**Syntaxe :**
```cmd
clean.bat [quick|deep|nuclear]
```

**Niveaux :**
- `quick` - Nettoie build/ et .gradle/ (rapide)
- `deep` - Quick + caches IDE (.idea/, *.iml)
- `nuclear` - Deep + réinstalle Gradle wrapper (reset total)

**Exemple :**
```cmd
clean.bat deep
```

### `buildhelper.bat` - Menu Interactif
Interface menu pour toutes les opérations.

**Fonctionnalités :**
- Build Debug/Release
- Tests unitaires/instrumentés
- Nettoyage (Quick/Deep/Nuclear)
- Analyse qualité (Detekt)
- Visualisation erreurs
- Copie prompt IA dans presse-papier

---

## 🔧 Prérequis

### Logiciels Requis
1. **JDK 17** ou supérieur
   - Download : https://adoptium.net/
   - Ajouter au PATH

2. **Android SDK**
   - Via Android Studio ou
   - SDK Command Line Tools

3. **Variables d'environnement**
   ```cmd
   JAVA_HOME=C:\Program Files\Java\jdk-17
   ANDROID_HOME=C:\Users\<User>\AppData\Local\Android\Sdk
   ```

### Vérification
```cmd
java -version        # Doit afficher Java 17+
echo %JAVA_HOME%     # Doit pointer vers JDK
echo %ANDROID_HOME%  # Doit pointer vers SDK
```

---

## 📊 Gestion des Erreurs

### Système Intelligent

Lorsqu'une erreur survient, `autobuild.bat` :

1. **Capture** l'erreur complète
2. **Analyse** les logs Gradle
3. **Extrait** les lignes critiques
4. **Génère** deux rapports :

#### Rapport d'Erreur (`error_report_*.txt`)
```
============================================================================
ArguMentor - BUILD ERROR REPORT
============================================================================
Error: Compilation failed
Command: gradlew assembleDebug

ENVIRONMENT INFORMATION
- Java Version: 17.0.8
- ANDROID_HOME: C:\...\Android\Sdk

BUILD LOG EXCERPT (Last 100 lines)
[logs détaillés...]

COMMON GRADLE ERRORS
[erreurs filtrées...]

COMPILATION ERRORS
[erreurs Kotlin/Java...]
```

#### Prompt pour IA (`ai_prompt.txt`)
Format optimisé pour Claude/GPT/etc.
```markdown
I'm building an Android app called ArguMentor...

**Error Message:**
Compilation failed

**Build Command:**
gradlew assembleDebug

**Environment:**
- OS: Windows
- Build Tool: Gradle
- Language: Kotlin
- UI: Jetpack Compose
...

**Questions:**
1. What is causing this build error?
2. What are the exact steps to fix it?
...
```

### Workflow de Résolution

```
BUILD FAIL
    ↓
autobuild.bat génère rapports
    ↓
Option 1: Analyser error_report_*.txt
    ↓
Option 2: Copier ai_prompt.txt → Presse-papier
    ↓
Coller dans Claude/GPT
    ↓
Recevoir solution détaillée
    ↓
Appliquer correctifs
    ↓
Rebuild
```

---

## 🎯 Cas d'Usage

### Build Quotidien
```cmd
# Matin - Build propre
autobuild.bat all

# Développement - Build rapide
autobuild.bat debug

# Avant commit - Tests
autobuild.bat test
```

### Debugging Build Issues
```cmd
# 1. Nettoyer
clean.bat deep

# 2. Rebuild
autobuild.bat debug

# 3. Si erreur → Copier prompt IA
type build_logs\ai_prompt.txt | clip

# 4. Coller dans ChatGPT/Claude
```

### Avant Release
```cmd
# 1. Nettoyer complet
clean.bat nuclear

# 2. Build release
autobuild.bat release

# 3. Tests complets
gradlew test connectedAndroidTest

# 4. Analyse qualité
gradlew detekt
```

---

## 📁 Structure des Logs

```
build_logs/
├── error_report_20250108_143022.txt  # Rapport d'erreur horodaté
├── ai_prompt.txt                      # Dernier prompt IA
└── build.log                          # Log complet Gradle
```

**Rétention :** Les logs sont conservés indéfiniment.
**Nettoyage manuel :** `rd /s /q build_logs`

---

## ⚡ Raccourcis Utiles

### Copie Rapide du Prompt IA
```cmd
type build_logs\ai_prompt.txt | clip
```
Le prompt est maintenant dans votre presse-papier !

### Voir Dernière Erreur
```cmd
type build_logs\error_report_*.txt | more
```

### Build + Ouvrir APK
```cmd
autobuild.bat debug && explorer app\build\outputs\apk\debug
```

---

## 🐛 Troubleshooting

### Erreur : "Java not found"
**Solution :**
```cmd
# Installer JDK 17
# Puis définir JAVA_HOME
setx JAVA_HOME "C:\Program Files\Java\jdk-17"
```

### Erreur : "ANDROID_HOME not set"
**Solution :**
```cmd
setx ANDROID_HOME "C:\Users\<User>\AppData\Local\Android\Sdk"
```

### Erreur : "Gradle daemon timeout"
**Solution :**
```cmd
# Arrêter tous les daemons
gradlew --stop

# Nettoyer et rebuild
clean.bat deep
autobuild.bat debug
```

### Erreur : "Out of memory"
**Solution :**
Éditer `gradle.properties` :
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
```

---

## 📖 Exemples Complets

### Exemple 1 : Premier Build
```cmd
# 1. Vérifier prérequis
java -version
echo %JAVA_HOME%
echo %ANDROID_HOME%

# 2. Build
autobuild.bat debug

# 3. Installer sur device
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Exemple 2 : Build Failed
```cmd
# Build échoue
C:\ArguMentor> autobuild.bat debug
...
BUILD FAILED!
Error report saved to: build_logs\error_report_20250108_143022.txt

# Copier prompt IA
C:\ArguMentor> type build_logs\ai_prompt.txt | clip

# Coller dans Claude
[Ctrl+V dans Claude.ai]

# Appliquer solution reçue
# Rebuild
C:\ArguMentor> autobuild.bat debug
...
BUILD SUCCESS!
```

### Exemple 3 : CI/CD
```cmd
# Script Jenkins/CI
clean.bat quick
autobuild.bat all
if %errorlevel% neq 0 (
    echo Build failed, uploading logs...
    upload-logs.bat
    exit /b 1
)
```

---

## 🔐 Variables d'Environnement

### Obligatoires
```cmd
JAVA_HOME=C:\Program Files\Java\jdk-17
```

### Recommandées
```cmd
ANDROID_HOME=C:\Users\<User>\AppData\Local\Android\Sdk
ANDROID_SDK_ROOT=%ANDROID_HOME%
```

### Optionnelles
```cmd
GRADLE_USER_HOME=C:\gradle-cache  # Cache personnalisé
```

---

## 📞 Support

En cas de problème persistant :

1. Générer rapport complet :
   ```cmd
   autobuild.bat debug
   type build_logs\ai_prompt.txt | clip
   ```

2. Ouvrir issue GitHub avec :
   - Prompt IA copié
   - Version Java (`java -version`)
   - Version Gradle (`gradlew --version`)
   - OS et version

3. Consulter AI (Claude/GPT) avec le prompt généré

---

## ✅ Checklist Avant Build

- [ ] JDK 17+ installé
- [ ] JAVA_HOME défini
- [ ] Android SDK installé
- [ ] ANDROID_HOME défini
- [ ] Gradle wrapper présent (`gradlew.bat`)
- [ ] Connexion internet (pour dépendances)

---

## 🎓 Conseils Pro

1. **Build quotidien :** Utilisez `buildhelper.bat` (menu)
2. **Build CI :** Utilisez `autobuild.bat all`
3. **Debug :** Toujours commencer par `clean.bat deep`
4. **Errors :** Copier `ai_prompt.txt` directement dans Claude
5. **Release :** Toujours `clean.bat nuclear` avant release build

---

## 📚 Références

- [Gradle Build Guide](https://docs.gradle.org/)
- [Android Build Guide](https://developer.android.com/build)
- [ArguMentor README](README.md)

---

**Dernière mise à jour :** 2025-01-08
**Version :** 1.0.0
