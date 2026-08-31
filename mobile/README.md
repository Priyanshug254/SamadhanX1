# SamadhanX Citizen Mobile Application (Flutter)

Production-grade cross-platform Flutter mobile application engineered for **Citizens, Community Organizations, Gram Panchayats, and Urban Local Bodies (ULBs)** to crowdsource societal problems, upload multimedia evidence, capture GPS coordinates, monitor real-time AI categorization and priority scoring, track departmental resolution workflows, and receive real push notifications via Firebase Cloud Messaging (FCM).

---

## 🏛️ Ecosystem Architecture & Core Flows

```
Citizen / Community
       │
       ▼ (Mobile App: Camera, GPS, Forms)
┌─────────────────────────────────────────────────────────────┐
│  Flutter Presentation Layer (Provider + Material 3 GovTech) │
│  - Splash Session Check                                     │
│  - Secure JWT Storage (flutter_secure_storage)              │
│  - Citizen Problem Reporting Screen                         │
│  - Public Crowdsource Feed & Domain Filtering               │
│  - Real-Time Activity & Timeline Stepper                    │
│  - Push Notification Listener & Deep Linking (FCM)          │
└──────────────────────────────┬──────────────────────────────┘
                               │ REST / Multipart HTTP (Dio)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│           SamadhanX Spring Boot Backend (v3.3.4)             │
│  - Multi-Factor AI Categorization & Priority Scoring (0-100)│
│  - Spatial Deduplication Engine                             │
│  - Automatic Department Routing                             │
│  - Academic Innovation Escalation (SIH PS 26043)            │
│  - FCM Push Notification Engine                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📱 Feature Highlights

1. **Authentication & Session Lifecycle**:
   - Clean registration and login with BCrypt password security and JWT token rotation.
   - Encrypted token storage with auto 401 session logout.
2. **Citizen Problem Submission**:
   - Structured multi-field problem intake: Title, description, domain selector, affected population.
   - Genuine device GPS coordinate capture via `geolocator`.
   - Multimedia evidence upload via `image_picker` (Camera & Gallery) and `file_picker` (PDF lab reports, documents) directly to the backend storage controller (`/api/v1/files/upload`).
   - Clear distinction between **Citizen Assessment** (Severity & Urgency) and **SamadhanX Server-Calculated Priority & AI Intelligence**.
3. **Tracking & Resolution Lifecycle**:
   - Unique national tracking number generator (e.g. `SMX-2026-08-XXXXX`).
   - Public challenge lookup without requiring login.
   - Real-time audit timeline showing department triage and academic innovation milestones.
   - Community endorsement / upvoting.
4. **Real Push Notifications (Firebase Cloud Messaging)**:
   - FCM token registration with backend on login (`/api/v1/notifications/device-tokens`).
   - Foreground notification presentation via `flutter_local_notifications`.
   - Background and terminated state notification handling.
   - Deep-linking directly to `ChallengeDetailScreen` on notification click.

---

## 🛠️ Tech Stack & Directory Structure

- **Framework**: Flutter 3 (Dart 3)
- **State Management**: Provider
- **Networking**: Dio (with AuthInterceptor and global ApiException handler)
- **Security**: flutter_secure_storage
- **Notifications**: firebase_core, firebase_messaging, flutter_local_notifications
- **Device Features**: geolocator, image_picker, file_picker

```
mobile/
├── android/app/src/main/
│   └── AndroidManifest.xml          # Permissions (Location, Camera, Storage, Notifications)
├── lib/
│   ├── core/
│   │   ├── constants/               # API endpoints, GovTech design tokens (AppColors)
│   │   ├── network/                 # ApiClient, AuthInterceptor, ApiException
│   │   ├── services/                # NotificationService (FCM), LocationService, MediaService
│   │   ├── storage/                 # SecureStorageService
│   │   ├── theme/                   # Material 3 GovTech Theme & Typography
│   │   └── utils/                   # Validators, Formatters
│   ├── data/
│   │   ├── models/                  # ApiResponse, Auth, Domain, Challenge, Notification models
│   │   └── repositories/            # AuthRepository, DomainRepository, ChallengeRepository
│   ├── presentation/
│   │   ├── navigation/              # MainNavigationScreen (Bottom Nav)
│   │   ├── screens/                 # Splash, Auth, Home, Submit, Detail, MyChallenges, Track, Notifications, Profile
│   │   └── widgets/                 # CustomButton, CustomTextField, StatusBadge, PriorityMeter, TimelineStepper, etc.
│   ├── providers/                   # AuthProvider, DomainProvider, ChallengeProvider, NotificationProvider
│   └── main.dart                    # Application entry point & service wiring
└── test/
    ├── models_test.dart             # Unit tests for data serialization & models
    └── widget_test.dart             # Widget tests for GovTech UI components
```

---

## 🚀 Getting Started

### 1. Prerequisites
- Flutter SDK `^3.5.0`
- Android Studio / VS Code with Flutter extension
- Running SamadhanX Backend at `http://10.0.2.2:8080` (Android Emulator) or `http://localhost:8080` (Web / iOS Simulator)

### 2. Configure Backend URL
To change the backend base URL, edit `lib/core/constants/api_endpoints.dart`:
```dart
static const String baseUrl = 'http://10.0.2.2:8080'; // Android emulator
// static const String baseUrl = 'http://localhost:8080'; // iOS / Web
```

### 3. Firebase Cloud Messaging Setup (Optional for Development)
1. Place your `google-services.json` inside `android/app/`.
2. Ensure Firebase Messaging is enabled in your Firebase Console.
3. The app is engineered to automatically fall back gracefully if Firebase credentials are absent in local test environments.

### 4. Run Tests & App
```bash
# Get dependencies
flutter pub get

# Static code analysis
flutter analyze

# Run unit and widget test suite
flutter test

# Launch mobile application
flutter run
```

---

## 📡 Integrated Spring Boot Backend APIs

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/v1/auth/login` | `POST` | Authenticate citizen and retrieve JWT |
| `/api/v1/auth/register` | `POST` | Register new citizen account |
| `/api/v1/users/me` | `GET` | Retrieve authenticated user profile |
| `/api/v1/domains` | `GET` | Fetch societal domains taxonomy |
| `/api/v1/challenges` | `POST` | Submit societal challenge with GPS & evidence |
| `/api/v1/challenges` | `GET` | Search & filter crowdsourced challenges |
| `/api/v1/challenges/my-submissions` | `GET` | Fetch citizen's submitted challenges |
| `/api/v1/challenges/{id}` | `GET` | Get challenge details with timeline |
| `/api/v1/challenges/tracking/{trackingNumber}` | `GET` | Public tracking number lookup |
| `/api/v1/challenges/{id}/endorse` | `POST` | Endorse/upvote challenge |
| `/api/v1/files/upload` | `POST` | Multipart evidence file upload |
| `/api/v1/files/{fileName}` | `GET` | Static file download/view |
| `/api/v1/notifications/device-tokens` | `POST` | Register FCM device token |
| `/api/v1/notifications/device-tokens/{token}` | `DELETE` | Unregister FCM token on logout |
| `/api/v1/notifications` | `GET` | Paginated in-app notification records |
| `/api/v1/notifications/unread-count` | `GET` | Get unread alerts count |
| `/api/v1/notifications/{id}/read` | `PATCH` | Mark alert as read |
| `/api/v1/notifications/read-all` | `PATCH` | Mark all alerts as read |
