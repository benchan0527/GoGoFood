# GoGoFood

A comprehensive food ordering and restaurant management system for Android, designed to streamline operations for restaurants with multiple branches. The system includes separate applications for customers, kitchen staff, and servers/administrators.

## 📱 Overview

GoGoFood is a multi-application Android system that enables:
- **Customers** to browse menus, place orders, and track order status in real-time
- **Kitchen Staff** to view and manage orders through a Kitchen Display System (KDS)
- **Servers/Administrators** to manage table orders, menus, and restaurant operations through a Point of Sale (POS) system

## ✨ Features

### Customer Application
- 🍽️ **Menu Browsing**: View menu items by category with time-based availability
- 🛒 **Shopping Cart**: Add items with customizations and modifiers
- 📍 **Restaurant Selection**: Choose from multiple restaurant branches
- 🎨 **Item Customization**: Select sizes, add-ons, sides, and drink options
- 📦 **Order Placement**: Place orders for dine-in or takeaway
- 📊 **Order Tracking**: Real-time order status updates (Pending → Preparing → Ready)
- 📜 **Order History**: View past and current orders

### Kitchen Display System (KDS)
- 📋 **Real-time Order Queue**: View all pending orders with real-time updates
- ⏱️ **Order Details**: See order items, quantities, and special instructions
- ✅ **Status Updates**: Update order status (Preparing → Ready)
- 🏢 **Multi-branch Support**: Filter orders by restaurant access permissions
- 🔄 **Auto-refresh**: Automatic updates when new orders arrive

### Point of Sale (POS) / Server Application
- 🪑 **Table Management**: Create and manage table orders
- 📝 **Order Entry**: Add menu items to table orders
- ✏️ **Order Modification**: Modify existing orders (add/remove items, change quantities)
- 🔍 **Order Search**: Search and view all active orders
- 👥 **Admin Panel**: Manage menus, users, and restaurant settings
- 🔐 **Role-based Access**: Different permission levels (Manager, Server, Kitchen Staff)

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

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd GoGoFood
```

### 2. Firebase Setup

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project
3. Download `google-services.json`
4. Place `google-services.json` in the `app/` directory
5. Ensure your Firebase project has Firestore enabled

### 3. Configure Firebase Security Rules

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

### 4. Import Sample Data

1. Build and run the app
2. Login as an administrator (see [Admin Login Guide](#admin-login))
3. Navigate to "Test Data" activity
4. Click "导入完整示例数据" (Import Complete Sample Data)
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
- **Phone**: `+1234567894`
- **Email**: manager01@restaurant.com
- **Permissions**: Full access (menu_edit, report_view, inventory_manage, order_manage, user_manage)

### ADMIN002 - Server
- **Admin ID**: `ADMIN002`
- **Phone**: `+1234567892`
- **Email**: server01@restaurant.com
- **Permissions**: order_manage, table_manage

### ADMIN003 - Kitchen Staff
- **Admin ID**: `ADMIN003`
- **Phone**: `+1234567893`
- **Email**: chef01@restaurant.com
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
├── information/                    # Documentation and sample data
│   ├── ADMIN_LOGIN_GUIDE.md
│   ├── FIREBASE_DATA_IMPORT_README.md
│   ├── SPRINT_1_COMPLETION_REPORT.md
│   ├── SPRINT_2_COMPLETION_ANALYSIS.md
│   └── firebase_sample_data.json
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml
```

## 🎯 User Stories & Features

### Sprint 1 (Completed ✅)
- **UC-1**: Customer can view the menu and place an order
- **UC-7**: Kitchen staff can see a real-time list of new orders

### Sprint 2 (Mostly Completed ⚠️)
- **UC-3**: Customer can track their order status ✅
- **UC-4**: Server can create a table order ✅
- **UC-5**: Server can modify an existing order ✅
- **UC-8**: Kitchen staff can update an order's status ✅

### Key Features Implemented
- Real-time order synchronization across all applications
- Multi-restaurant branch support with permission-based filtering
- Comprehensive menu management with categories and modifiers
- Order status tracking with visual indicators
- Shopping cart with item customization
- Admin panel with role-based permissions

## 🔐 Permissions & Roles

The system supports role-based access control:

- **Manager**: Full system access
- **Server**: Order and table management
- **Kitchen Staff**: Order viewing and status updates
- **Customer**: Order placement and tracking

Permissions are managed through the `PermissionManager` utility class and enforced throughout the application.

## 📊 Data Models

### Core Models
- **User**: Customer accounts
- **Admin**: Staff accounts with permissions
- **Order**: Order information with items and status
- **MenuItem**: Menu items with categories and modifiers
- **Table**: Restaurant table information
- **Branch**: Restaurant branch/location
- **ItemModifier**: Customization options for menu items

## 🧪 Testing

Currently, the project includes:
- Basic test structure (JUnit, Espresso)
- Manual testing during development

**Recommended Testing**:
- Unit tests for data models and business logic
- Integration tests for Firebase operations
- UI tests for critical user flows

## 🐛 Known Issues & Limitations

- Password authentication for admin accounts not yet implemented
- Table map visualization (GUI #14) not yet implemented
- Order search screen (GUI #16) not yet implemented
- No pagination for large order lists
- Limited offline support

## 📝 Development Notes

### Firebase Configuration
- Ensure `google-services.json` is properly configured
- Firestore security rules should be set appropriately for your environment
- Sample data can be imported through the TestDataActivity

### Code Style
- Code follows Android best practices
- All code is in English
- Proper separation of concerns (models, services, activities)

## 🤝 Contributing

This is a university group project (COMP3500). For contributions:
1. Follow the existing code style
2. Ensure all features are tested
3. Update documentation as needed

## 📄 License

This project is part of a university assignment (COMP3500). See assignment documentation in the `information/` directory.

## 📚 Additional Documentation

- [Admin Login Guide](information/ADMIN_LOGIN_GUIDE.md)
- [Firebase Data Import Guide](information/FIREBASE_DATA_IMPORT_README.md)
- [Sprint 1 Completion Report](information/SPRINT_1_COMPLETION_REPORT.md)
- [Sprint 2 Completion Analysis](information/SPRINT_2_COMPLETION_ANALYSIS.md)

## 🆘 Troubleshooting

### Common Issues

**Issue**: Cannot login as admin
- **Solution**: Ensure sample data has been imported (see Firebase Setup section)
- Verify Admin ID or phone number is correct
- Check Firebase security rules allow read access

**Issue**: Orders not appearing in Kitchen View
- **Solution**: Verify admin account has proper restaurant access permissions
- Check Firebase connection and Firestore rules
- Ensure order status is "pending" or "preparing"

**Issue**: Build errors related to Firebase
- **Solution**: Verify `google-services.json` is in the correct location
- Ensure Firebase plugins are properly configured in `build.gradle.kts`
- Sync Gradle files in Android Studio

**Issue**: Sample data import fails
- **Solution**: Check network connection
- Verify Firebase project configuration
- Check Logcat for detailed error messages
- Ensure Firebase security rules allow writes

## 📞 Support

For issues related to:
- **Firebase Setup**: See Firebase documentation and `FIREBASE_DATA_IMPORT_README.md`
- **Admin Access**: See `ADMIN_LOGIN_GUIDE.md`
- **Development**: See sprint completion reports in `information/` directory

---

**Project**: GoGoFood - Food Ordering System  
**Course**: COMP3500  
**Platform**: Android  
**Version**: 1.0





