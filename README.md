# MGB Heights – Society Management App

## 📱 Overview
MGB Heights is a production-ready, single-society residential management application built with Kotlin Multiplatform (KMP). Business logic is shared across Android and iOS, with native UI layers on each platform.

**App Name:** MGB Heights (hardcoded)  
**Platform:** Android (XML UI) + iOS (SwiftUI)  
**Architecture:** Clean Architecture + MVVM  

## 🏗 Project Structure

```
MGBHeights/
├── shared/                          # KMP shared module
│   └── src/
│       ├── commonMain/             # Shared business logic
│       │   └── kotlin/com/mgbheights/shared/
│       │       ├── domain/
│       │       │   ├── model/      # Domain models
│       │       │   ├── repository/ # Repository interfaces
│       │       │   └── usecase/    # Use cases
│       │       └── util/           # Utilities
│       ├── androidMain/            # Android-specific implementations
│       └── iosMain/                # iOS-specific implementations
│
├── androidApp/                      # Android application
│   └── src/main/
│       ├── java/com/mgbheights/android/
│       │   ├── MgbHeightsApp.kt   # Application class
│       │   ├── MainActivity.kt     # Single activity
│       │   ├── data/
│       │   │   ├── local/         # Room DB, DAOs, Entities
│       │   │   ├── mapper/        # Entity ↔ Domain mappers
│       │   │   ├── remote/        # Firebase service classes
│       │   │   └── repository/    # Repository implementations
│       │   ├── di/                # Hilt DI modules
│       │   ├── service/           # FCM service
│       │   └── ui/
│       │       ├── adapter/       # RecyclerView adapters
│       │       ├── auth/          # Login & OTP
│       │       ├── dashboard/     # Dashboard
│       │       ├── maintenance/   # Bills & payments
│       │       ├── visitor/       # Visitor management
│       │       ├── notice/        # Notices/announcements
│       │       ├── complaint/     # Complaints
│       │       └── profile/       # User profile
│       └── res/
│           ├── layout/            # XML layouts
│           ├── navigation/        # Nav graph
│           ├── menu/              # Bottom nav menu
│           ├── values/            # Colors, dimens, strings, themes
│           ├── drawable/          # Icons & backgrounds
│           └── xml/               # Network security config
│
├── iosApp/                          # iOS application
│   └── iosApp/
│       ├── MGBHeightsApp.swift    # App entry point
│       ├── ContentView.swift      # Root navigation
│       ├── ViewModels/            # iOS ViewModels
│       ├── Views/                 # SwiftUI views
│       └── Assets.xcassets/       # Colors & images
│
└── firebase/                       # Firebase config
    ├── firestore.rules            # Firestore security rules
    ├── firestore.indexes.json     # Firestore indexes
    └── storage.rules              # Storage security rules
```

## 👥 User Roles

| Role | Access Level |
|------|-------------|
| **Admin** | Full access: user management, billing, analytics, reports |
| **Resident** | House profile, bills, payments, visitors, complaints, notices |
| **Tenant** | View house details, view dues, make payments, download receipts |
| **Security Guard** | Visitor registration, photo capture, entry/exit logs |
| **Worker** | Assigned jobs, work status, earnings, duty toggle |

## 🔧 Setup Guide

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Xcode 15+ (for iOS)
- JDK 17
- Firebase project configured

### Android Setup

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   ```

2. **Add Firebase config:**
   - Download `google-services.json` from Firebase Console
   - Place in `androidApp/` directory

3. **Configure Razorpay:**
   - Get API key from Razorpay Dashboard
   - Add to `local.properties`:
     ```
     razorpay.key.id=your_key_id
     razorpay.key.secret=your_key_secret
     ```

4. **Build & Run:**
   ```bash
   ./gradlew :androidApp:assembleDebug
   ```

### iOS Setup

1. **Open in Xcode:**
   ```bash
   open iosApp/iosApp.xcodeproj
   ```

2. **Add Firebase config:**
   - Download `GoogleService-Info.plist` from Firebase Console
   - Add to the iosApp target

3. **Install dependencies via CocoaPods/SPM:**
   - Firebase Auth
   - Firebase Firestore
   - Firebase Storage
   - Firebase Messaging
   - Razorpay

4. **Build shared KMP framework:**
   ```bash
   ./gradlew :shared:assembleXCFramework
   ```

5. **Build & Run from Xcode**

### Firebase Setup

1. **Create Firebase project** named "MGB Heights"

2. **Enable services:**
   - Authentication → Phone Sign-in
   - Cloud Firestore
   - Cloud Storage
   - Cloud Messaging

3. **Deploy security rules:**
   ```bash
   firebase deploy --only firestore:rules,storage
   ```

4. **Deploy Firestore indexes:**
   ```bash
   firebase deploy --only firestore:indexes
   ```

## 🏛 Architecture

### Shared Module (KMP)
- **Domain Models:** Pure Kotlin data classes with serialization
- **Repository Interfaces:** Contracts for data operations
- **Use Cases:** Business logic orchestration
- **Validators:** Input validation
- **Constants:** App-wide constants
- **DateTimeUtil:** Cross-platform date/time formatting

### Android App
- **MVVM:** ViewModels expose state via LiveData
- **Room Database:** Offline-first with local caching
- **Dagger Hilt:** Dependency injection
- **Navigation Component:** Single-activity with fragments
- **CameraX:** Visitor photo capture
- **Coil:** Image loading & caching

### iOS App
- **SwiftUI:** Declarative UI
- **Combine/ObservableObject:** Reactive state management
- **Shared KMP Framework:** Business logic from shared module

## 📊 Firebase Collections

| Collection | Description |
|-----------|-------------|
| `users` | User profiles & roles |
| `flats` | Flat/house details |
| `maintenance_bills` | Monthly maintenance bills |
| `payments` | Payment records |
| `notices` | Announcements |
| `complaints` | Service requests |
| `visitors` | Visitor entries |
| `workers` | Worker profiles |
| `work_orders` | Work assignments |
| `security_logs` | Security events |
| `audit_logs` | System audit trail |

## 🔐 Security Rules
- Role-based access control via Firestore rules
- Users can only read/write their own data
- Admins have elevated privileges
- Audit logs are append-only
- File uploads restricted by type and size

## 🎨 Theme
- **Primary Color:** `#EC3713`
- **Background Light:** `#F8F6F6`
- **Background Dark:** `#221310`
- **Material 3** default components
- System dark/light mode support
- No custom theming

## 📄 License
Proprietary – MGB Heights Society

