# GoGoFood

A comprehensive food ordering and restaurant management system for Android, designed to streamline operations for restaurants with multiple branches. The system includes separate applications for customers, kitchen staff, and servers/administrators.

## 🛠️ Tech Stack

- **Language**: Java 11
- **Platform**: Android (API 24+)
- **Backend**: Firebase
  - Firebase Firestore (Database)
  - Firebase Authentication
  - Firebase Performance Monitoring
- **UI Libraries**:
  - Material Design Components
  - RecyclerView & CardView
  - SwipeRefreshLayout
- **Image Loading**: Glide 4.16.0
- **Build System**: Gradle with Kotlin DSL
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)

## 📋 Prerequisites

- Android Studio (latest version recommended)
- JDK 11 or higher
- Android SDK (API 24+)
- Firebase account and project
- `google-services.json` file configured for your Firebase project

##  Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/benchan0527/comp3500-14
cd GoGoFood
```

```android studio
clone https://github.com/benchan0527/comp3500-14
```

### 2. Firebase Setup(optional)

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project
3. Download `google-services.json`
4. Place `google-services.json` in the `app/` directory
5. Ensure your Firebase project has Firestore enabled

### 3. Configure Firebase Security Rules(optional)

For development, you can use these permissive rules (update for production):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true; // Development only - restrict in production
    }
  }
}
```

### 4. Import Sample Data(optional)

1. Build and run the app
2. Login as an administrator (see [Admin Login Guide](#admin-login))
3. Navigate to "Test Data" activity
4. Click (Import Complete Sample Data)
5. Wait for import to complete

For detailed import instructions, see [`information/FIREBASE_DATA_IMPORT_README.md`](information/FIREBASE_DATA_IMPORT_README.md)

### 5. Build and Run

```bash
# Using Gradle wrapper
./gradlew assembleDebug

# Or open in Android Studio and click Run
```

## 👤 Admin Login

The system includes three sample administrator accounts:

### ADMIN001 - Manager
- **Admin ID**: `ADMIN001`
- **Permissions**: Full access (menu_edit, report_view, inventory_manage, order_manage, user_manage)

### ADMIN002 - Server
- **Admin ID**: `ADMIN002`
- **Permissions**: order_manage, table_manage

### ADMIN003 - Kitchen Staff
- **Admin ID**: `ADMIN003`
- **Permissions**: order_view, order_update

**Login Steps**:
1. Open the app
2. Click "Admin Login" button
3. Enter Admin ID (e.g., `ADMIN001`) or phone number
4. Password field is optional (not yet implemented)

For detailed login instructions, see [`information/ADMIN_LOGIN_GUIDE.md`](information/ADMIN_LOGIN_GUIDE.md)

## 📁 Project Structure

```
GoGoFood/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/group14/foodordering/
│   │   │   │   ├── model/          # Data models
│   │   │   │   │   ├── Admin.java
│   │   │   │   │   ├── Order.java
│   │   │   │   │   ├── MenuItem.java
│   │   │   │   │   └── ...
│   │   │   │   ├── service/        # Firebase services
│   │   │   │   │   └── FirebaseDatabaseService.java
│   │   │   │   ├── util/           # Utility classes
│   │   │   │   │   ├── FirebaseDataImporter.java
│   │   │   │   │   ├── PermissionManager.java
│   │   │   │   │   └── ...
│   │   │   │   ├── CustomerMainActivity.java
│   │   │   │   ├── MenuActivity.java
│   │   │   │   ├── KitchenViewActivity.java
│   │   │   │   ├── TableOrderActivity.java
│   │   │   │   └── ...
│   │   │   ├── res/                # Resources (layouts, strings, etc.)
│   │   │   └── AndroidManifest.xml
│   │   └── test/                   # Unit tests
│   ├── build.gradle.kts
│   └── google-services.json
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml
```
