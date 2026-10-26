# 📝 MyNotes — Application Android Moderne de Prise de Notes

Une application Android moderne, fluide et élégante de prise de notes connectée en temps réel avec **Firebase**, développée avec **Jetpack Compose** et **Material Design 3**.

---

## 🌟 Fonctionnalités

### 🔐 Authentification & Sécurité
- **Connexion & Inscription** avec Firebase Authentication (Email / Mot de passe).
- **Validation en temps réel** des champs (format email, longueur et confirmation de mot de passe).
- **Affichage / Masquage du mot de passe** avec indicateurs interactifs.
- **Indicateurs de chargement** et bannières d'erreur animées.
- **Synchronisation automatique** de l'état de session utilisateur (`AuthStateListener`).

### 📋 Gestion des Notes (CRUD Temps Réel)
- **Création rapide** de notes via un bouton d'action flottant étendu (*Extended FAB*).
- **Édition complète** des notes (titre, contenu, catégorie, couleur, épinglage).
- **Suppression sécurisée** avec boîte de dialogue de confirmation pour éviter les pertes accidentelles.
- **Mise à jour instantanée** dans Cloud Firestore avec écoute en temps réel (`addSnapshotListener`).
- **Rétrocompatibilité totale** avec les notes précédemment créées (gestion multi-formats des dates et valeurs par défaut automatiques).

### 🎨 Organisation & Personnalisation
- **📌 Épinglage prioritaire** : Épinglez vos notes importantes pour les afficher dans une section dédiée en haut de l'écran.
- **🏷️ Catégories prédéfinies** : Classez vos notes (*Général*, *Personnel*, *Travail*, *Idées*, *Études*, *Urgent*).
- **🎨 Palette de couleurs personnalisable** : Choisissez parmi 9 teintes soigneusement adaptées au mode clair et au mode sombre.
- **📊 Statistiques instantanées** : Nombre de caractères et nombre de mots calculés en direct.
- **🕒 Horodatage précis** : Affichage automatique de la date de création ou de dernière modification (*ex: 26 oct. 2026, 14:30*).

### 🔍 Recherche & Navigation Fluide
- **Recherche instantanée** : Filtrez vos notes en temps réel par mots-clés dans les titres et contenus.
- **Filtres par Catégorie** : Filtrez rapidement grâce à une rangée de *chips* interactifs défilants (*Tous*, *Épinglés*, etc.).
- **Bascule Vue Grille / Vue Liste** : Passez d'une vue liste à une vue grille à 2 colonnes en un clic.
- **📋 Copie rapide** : Copiez le contenu d'une note dans le presse-papier en un clic depuis la carte de note.
- **Dialogues de confirmation** : Sécurisation de la déconnexion et de la suppression.

---

## 🏗️ Architecture & Technologies

L'application respecte les recommandations officielles d'architecture Android de Google :

- **Pattern Architectural** : **MVVM** (*Model-View-ViewModel*) + **Repository Pattern**.
- **Interface Utilisateur** : **Jetpack Compose** (100% déclaratif) & **Material Design 3**.
- **Gestion de l'État** : `StateFlow`, `MutableStateFlow` et `combine()` avec `viewModelScope`.
- **Backend as a Service (BaaS)** :
  - **Firebase Authentication** : Gestion des comptes et sessions.
  - **Cloud Firestore** : Base de données NoSQL synchronisée en temps réel sous `users/{userId}/notes`.
- **Thème & Design System** :
  - Mode clair et mode sombre complets avec palette harmonieuse.
  - Typographie nette et hiérarchisée.

---

## 📁 Structure du Projet

```text
com.example.mynotes/
├── MainActivity.kt               # Point d'entrée avec gestion des transitions et écoute Auth
├── model/
│   └── Note.kt                   # Modèle de données Note, calculs, constantes & palettes
├── repository/
│   └── NoteRepository.kt         # Couche d'accès Firestore (sécurisée & rétro-compatible)
├── viewmodel/
│   └── NotesViewModel.kt         # Gestion des flux (recherche, filtres, CRUD, états)
├── ui/
│   └── theme/
│       ├── Color.kt              # Palettes de couleurs (modes clair & sombre)
│       ├── Theme.kt              # Configuration MaterialTheme 3
│       └── Type.kt               # Typographie de l'application
└── uii/
    ├── ConfirmationDialog.kt     # Dialogue générique de confirmation (suppression / logout)
    ├── EditNoteDialog.kt         # Dialogue de création et édition avec sélecteurs riches
    ├── LoginScreen.kt            # Écran de connexion moderne avec validations
    ├── NoteCard.kt               # Carte de note Material 3 avec badges et actions
    ├── NotesScreen.kt            # Écran principal (TopAppBar, recherche, filtres, grille/liste)
    └── RegisterScreen.kt         # Écran d'inscription sécurisé
```

---

## 🚀 Installation & Exécution

### Prérequis
- **Android Studio** (version Ladybug / Meerkat ou plus récente recommandée).
- **JDK 17 ou 21** (le projet utilise le JBR inclus dans Android Studio).
- **Android SDK** : `compileSdk = 36`, `minSdk = 24`, `targetSdk = 35`.
- Un projet **Firebase** configuré avec Authentication (Email/Password) et Firestore Database.

### Configuration Firebase
1. Téléchargez votre fichier `google-services.json` depuis la console Firebase.
2. Placez-le dans le répertoire `app/` du projet :
   ```text
   MyNotes/app/google-services.json
   ```

### Compilation & Lancement
Dans un terminal ou depuis Android Studio :

```powershell
# Compiler et vérifier le projet
.\gradlew assembleDebug

# Installer sur un appareil ou émulateur connecté
.\gradlew installDebug
```

---

## 🔒 Règles de Sécurité Firestore Recommandées

Pour garantir que chaque utilisateur n'accède qu'à ses propres notes, configurez vos règles Firestore comme suit dans la console Firebase :

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/notes/{noteId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## 📄 Licence
Ce projet est libre d'utilisation et d'adaptation.
