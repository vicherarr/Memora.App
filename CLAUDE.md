# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Memora is a full-stack note-taking application consisting of:
1. **Backend API**: RESTful API built with ASP.NET Core 8.0 for managing personal notes with multimedia content
2. **Mobile App**: Cross-platform mobile application built with Kotlin Multiplatform for creating, viewing, and managing notes with an elegant, minimalist design (Package: com.vicherarr.memora)

### Purpose
- Provide a complete note-taking solution with multimedia support
- Enable users to create and manage personal notes with text, images, and videos
- Implement secure user authentication and data access controls
- Deliver an intuitive, aesthetically pleasing mobile user experience
- Support file upload and storage for images and videos across iOS and Android

## Development Commands

### Backend API (Memora.API)
```bash
# Navigate to API directory
cd Memora.API

# Run the application
dotnet run

# Build the project
dotnet build

# Run with Docker
docker build -t memora-api .
docker run -p 8080:8080 -p 8081:8081 memora-api

# Restore dependencies
dotnet restore

# Database migrations
dotnet ef migrations add MigrationName
dotnet ef database update
```

The API will start on:
- HTTP: http://localhost:5003
- HTTPS: https://localhost:7241

### Mobile App (Memora)
```bash
# Navigate to App directory
cd Memora

# Build the project
./gradlew build

# Run on Android
./gradlew :composeApp:installDebug

# Run on iOS (requires Xcode and Kotlin Multiplatform plugin)
./gradlew :iosApp:iosDeployIPhone15Debug

# Clean and rebuild
./gradlew clean build

# Run tests
./gradlew test

# Build release (Android)
./gradlew :composeApp:assembleRelease

# Build release (iOS)
./gradlew :iosApp:iosArchiveRelease
```

## Architecture & Technology Stack

### Backend API
- **Framework**: ASP.NET Core 8.0 Web API
- **Language**: C#
- **ORM**: Entity Framework Core 8
- **Database**: EF Core abstraction (supports SQL Server, PostgreSQL, SQLite)
- **Authentication**: JWT (JSON Web Tokens)
- **File Storage**: Direct storage in database as binary data (BLOB)
- **Error Handling**: Problem Details (RFC 7807) standard
- **Communication**: HTTPS with JSON data format

### Mobile App (Kotlin Multiplatform)
- **Framework**: Kotlin Multiplatform Mobile (KMP)
- **Language**: Kotlin
- **UI Framework**: Compose Multiplatform (shared UI for Android and iOS)
- **Target Platforms**: Android 7.0+ (API level 24), iOS 13+
- **Architecture Pattern**: MVVM with Repository pattern
- **Dependency Injection**: Koin for multiplatform DI
- **HTTP Client**: Ktor client for multiplatform networking
- **Navigation**: Compose Navigation (shared across platforms)
- **Local Storage**: SQLDelight for multiplatform database
- **State Management**: Compose State and ViewModel
- **Image Handling**: Platform-specific implementations with expect/actual
- **File System**: Platform-specific file handling
- **Connectivity**: Ktor client with platform-specific networking
- **Permissions**: Platform-specific permission handling

## Design System & UI Guidelines

### Color Palette (Material Design 3 Compatible)
```kotlin
// Primary Colors - Modern Purple/Violet Theme
val Primary = Color(0xFF6750A4)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFEADDFF)
val OnPrimaryContainer = Color(0xFF21005D)

// Secondary Colors
val Secondary = Color(0xFF625B71)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFE8DEF8)
val OnSecondaryContainer = Color(0xFF1D192B)

// Tertiary Colors
val Tertiary = Color(0xFF7D5260)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFFFD8E4)
val OnTertiaryContainer = Color(0xFF31111D)

// Surface Colors
val Surface = Color(0xFFFFFBFE)
val OnSurface = Color(0xFF1C1B1F)
val SurfaceVariant = Color(0xFFE7E0EC)
val OnSurfaceVariant = Color(0xFF49454F)
val SurfaceTint = Color(0xFF6750A4)

// Background Colors
val Background = Color(0xFFFFFBFE)
val OnBackground = Color(0xFF1C1B1F)

// Error Colors
val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

// Outline Colors
val Outline = Color(0xFF79747E)
val OutlineVariant = Color(0xFFCAC4D0)

// Dark Theme Colors
val PrimaryDark = Color(0xFFD0BCFF)
val OnPrimaryDark = Color(0xFF381E72)
val SurfaceDark = Color(0xFF1C1B1F)
val OnSurfaceDark = Color(0xFFE6E1E5)
val BackgroundDark = Color(0xFF1C1B1F)
val OnBackgroundDark = Color(0xFFE6E1E5)
```

### Typography Styles
```kotlin
// Material Design 3 Typography
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)
```

### Design Principles
1. **Material Design 3**: Follow Google's Material Design 3 guidelines
2. **Platform Consistency**: Respect platform-specific UI conventions
3. **Minimalism**: Clean, uncluttered interface with purposeful elements
4. **Accessibility**: Support screen readers, high contrast, and large text
5. **Responsiveness**: Adaptive layouts for different screen sizes and orientations
6. **Performance**: Smooth animations and fast interactions

### Component Patterns
- **Cards**: Material Design elevated cards with appropriate shadows
- **Buttons**: FAB, elevated, filled, outlined, and text button variants
- **Navigation**: Bottom navigation bar with Compose Navigation
- **Forms**: Material Design text fields with floating labels
- **Lists**: LazyColumn/LazyRow with proper item composables
- **Media**: Image and video viewers with zoom and playback controls

### Layout Guidelines
- **Spacing**: 8dp base unit (4dp, 8dp, 16dp, 24dp, 32dp, 48dp, 64dp)
- **Margins**: Consistent 16dp margins from screen edges
- **Card Spacing**: 8dp between cards, 16dp from screen edges
- **Touch Targets**: Minimum 48dp touch target size
- **Safe Areas**: Respect device safe areas and notches

## Data Models

### Core Entities

#### Usuario (User)
```csharp
- Id: Guid (Primary Key)
- NombreUsuario: string (Required, Unique)
- CorreoElectronico: string (Required, Unique)
- ContrasenaHash: string (Required)
- FechaCreacion: DateTime (Required)
- Notas: ICollection<Nota> (Navigation)
```

#### Nota (Note)
```csharp
- Id: Guid (Primary Key)
- Titulo: string (Optional, Max 200 chars)
- Contenido: string (Required)
- FechaCreacion: DateTime (Required)
- FechaModificacion: DateTime (Required)
- UsuarioId: Guid (Foreign Key)
- Usuario: Usuario (Navigation)
- ArchivosAdjuntos: ICollection<ArchivoAdjunto> (Navigation)
```

#### ArchivoAdjunto (Attachment)
```csharp
- Id: Guid (Primary Key)
- DatosArchivo: byte[] (Required) - Binary file data
- NombreOriginal: string (Required)
- TipoArchivo: TipoDeArchivo (Required)
- TipoMime: string (Required) - MIME type (image/jpeg, video/mp4, etc.)
- TamanoBytes: long (Required) - File size in bytes
- FechaSubida: DateTime (Required)
- NotaId: Guid (Foreign Key)
- Nota: Nota (Navigation)
```

#### TipoDeArchivo Enum
```csharp
public enum TipoDeArchivo
{
    Imagen = 1,
    Video = 2
}
```

## API Endpoints

### Authentication (`/api/autenticacion`)
- `POST /api/autenticacion/registrar` - Register new user
- `POST /api/autenticacion/login` - User login with JWT token

### Notes (`/api/notas`) - Requires JWT Authentication
- `GET /api/notas` - Get paginated user notes
- `GET /api/notas/{id}` - Get specific note with attachments
- `POST /api/notas` - Create new note
- `PUT /api/notas/{id}` - Update existing note
- `DELETE /api/notas/{id}` - Delete note and attachments

### File Attachments - Requires JWT Authentication
- `POST /api/notas/{notaId}/archivos` - Upload files to note (stores binary data in database)
- `GET /api/archivos/{archivoId}` - Download/retrieve file data from database
- `DELETE /api/archivos/{archivoId}` - Delete specific attachment from database

## Mobile App Features

### Core Features
1. **User Authentication**
   - Registration with email validation
   - Secure login with biometric authentication (fingerprint/face)
   - JWT token management with secure storage
   - Auto-logout on token expiration
   - Offline authentication caching

2. **Note Management**
   - Create notes with rich text content
   - Edit notes with real-time preview
   - Delete notes with confirmation dialog
   - Search and filter notes locally and remotely
   - Pull-to-refresh for note synchronization
   - Offline note creation and editing

3. **Multimedia Support**
   - Camera integration for photo capture
   - Photo library access for image selection
   - Video recording and selection
   - File type validation and compression
   - Thumbnail generation for images
   - Full-screen media viewing

4. **Cross-Platform Features**
   - Native platform integrations (sharing, files)
   - Adaptive UI for different screen sizes
   - Platform-specific navigation patterns
   - Hardware back button support (Android)
   - Swipe gestures for note actions
   - Dark/light theme support

### Screen Structure
```
App (NavHost)
├── LoginScreen            - User authentication
├── RegisterScreen         - New user registration
├── MainScreen             - Bottom navigation container
│   ├── NotesScreen        - Main notes overview (Tab)
│   ├── SearchScreen       - Search and filter notes (Tab)
│   └── ProfileScreen      - User profile and settings (Tab)
├── NoteDetailScreen       - View specific note
├── NoteEditScreen         - Create/edit note
├── MediaViewerScreen      - Full-screen media viewing
└── SettingsScreen         - App settings and preferences
```

### MVVM Architecture
```kotlin
// ViewModels (shared/commonMain)
class BaseViewModel : ViewModel()
class LoginViewModel : BaseViewModel()
class NotesViewModel : BaseViewModel()
class NoteDetailViewModel : BaseViewModel()
class NoteEditViewModel : BaseViewModel()
class MediaViewModel : BaseViewModel()
class SettingsViewModel : BaseViewModel()

// Models (shared/commonMain)
data class User(...)
data class Note(...)
data class Attachment(...)
data class AppSettings(...)

// Services/Repositories (shared/commonMain)
interface AuthenticationRepository
interface NotesRepository
interface MediaRepository
interface StorageRepository
interface SyncRepository

// Platform-specific implementations (expect/actual)
expected class CameraService
expected class BiometricAuthService
expected class SecureStorageService
```

### Local Data Storage
- **SQLDelight**: Multiplatform SQLite database for local note storage
- **Secure Storage**: Platform-specific secure storage for JWT tokens
- **File System**: Platform-specific file handling for media caching
- **DataStore**: Multiplatform preferences for user settings

## Security Requirements

- All communication via HTTPS with certificate pinning
- JWT token secure storage using platform keychain
- Biometric authentication integration where available
- Password hashing with robust algorithms (Argon2/BCrypt)
- Input validation and sanitization
- Protection against SQL injection (via EF Core)
- User data isolation (users can only access their own data)
- Secure file handling with type and size validation
- App transport security compliance
- Runtime application self-protection (RASP)

## Performance & Scalability

### Backend
- Response times under 500ms for GET requests
- File uploads up to 50MB per file (configurable)
- Pagination for list endpoints
- Database optimization for binary data storage
- Docker containerization support

### Mobile App
- App startup time under 3 seconds
- Smooth 60fps animations and scrolling
- Lazy loading for images and large lists
- Background synchronization
- Efficient memory management
- Battery optimization
- Network request optimization with caching
- Image compression and resizing

## Configuration

### Backend Configuration
- **Swagger Documentation**: Auto-generated API documentation at `/swagger`
- **HTTPS**: Required for all communications
- **JWT**: Token-based authentication system with 1-hour expiration
- **File Storage**: Direct database storage as binary data (BLOB)
- **User Secrets**: ID `3d6f68c0-06bf-43b1-b94a-c939b89cfd3e`

### Mobile App Configuration
- **App Settings**: API base URL, timeouts, feature flags
- **Platform Configurations**: iOS Info.plist, Android Manifest
- **Permissions**: Camera, photo library, storage, network (platform-specific)
- **SSL Pinning**: Certificate validation for Ktor client
- **Crash Reporting**: Multiplatform crash reporting

## Implementation Plan

### Phase 1: Kotlin Multiplatform Project Setup
**Objective**: Create and configure the Kotlin Multiplatform project

**Tasks**:
1. **Project Creation**
   - Use Kotlin Multiplatform wizard (https://kmp.jetbrains.com/)
   - Configure for Android + iOS with Shared UI (Compose Multiplatform)
   - Set up project in Android Studio
   - Verify builds for both platforms

2. **Project Structure Setup**
   ```
   Memora.App/
   ├── composeApp/              # Shared UI with Compose Multiplatform
   │   ├── src/
   │   │   ├── androidMain/kotlin/  # Android-specific UI code
   │   │   ├── commonMain/kotlin/   # Shared UI code
   │   │   └── iosMain/kotlin/      # iOS-specific UI code
   │   └── build.gradle.kts
   ├── shared/                  # Shared business logic
   │   ├── src/
   │   │   ├── androidMain/kotlin/  # Android-specific implementations
   │   │   ├── commonMain/kotlin/   # Shared code
   │   │   └── iosMain/kotlin/      # iOS-specific implementations
   │   └── build.gradle.kts
   ├── iosApp/                  # iOS Xcode project
   └── build.gradle.kts
   ```

3. **Initial Dependencies**
   ```kotlin
   // Core KMP dependencies
   implementation("org.jetbrains.compose.ui:ui:$compose_version")
   implementation("org.jetbrains.compose.foundation:foundation:$compose_version")
   implementation("org.jetbrains.compose.material3:material3:$compose_version")
   
   // Coroutines
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
   
   // Serialization
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
   ```

### Phase 2: Architecture and Dependencies Setup
**Objective**: Configure core architecture and dependencies

### Phase 3: Design System & Base Components
**Objective**: Implement Material Design 3 with Compose Multiplatform

**Tasks**:
1. **Theme Setup** (`composeApp/src/commonMain/kotlin/theme/`)
   - `Color.kt`: Material Design 3 color palette
   - `Typography.kt`: Text styles and font definitions
   - `Theme.kt`: Light and dark theme configuration
   - `Shapes.kt`: Component shapes and corner radius

2. **Base Components** (`composeApp/src/commonMain/kotlin/components/`)
   - `MemoraButton`: Material button variants (Filled, Outlined, Text)
   - `MemoraTextField`: Material text field with floating labels
   - `MemoraCard`: Elevated card container
   - `LoadingIndicator`: Loading states and progress indicators
   - `MediaViewer`: Image and video display composables
   - `EmptyState`: Empty state illustrations

3. **Navigation Setup**
   - Configure Compose Navigation with NavHost
   - Set up bottom navigation bar
   - Implement deep linking
   - Add navigation transitions

### Phase 4: Authentication System
**Objective**: Implement secure user authentication

**Tasks**:
1. **Authentication Repository** (`shared/src/commonMain/kotlin/repository/`)
   - Login and registration API calls with Ktor
   - JWT token management with secure storage
   - Token refresh logic
   - Logout and session management

2. **Authentication ViewModels** (`shared/src/commonMain/kotlin/presentation/`)
   - `LoginViewModel`: Login form logic with validation
   - `RegisterViewModel`: Registration form logic
   - `AuthenticationViewModel`: Common authentication functionality

3. **Authentication Screens** (`composeApp/src/commonMain/kotlin/screens/`)
   - `LoginScreen`: Material Design login form with Compose
   - `RegisterScreen`: Registration form with validation
   - `WelcomeScreen`: Onboarding and welcome screen

4. **Platform-Specific Security** (expect/actual pattern)
   - `SecureStorage`: Platform-specific secure token storage
   - `BiometricAuth`: Fingerprint/Face ID authentication
   - Certificate pinning for Ktor client
   - Input validation and sanitization

### Phase 5: Notes Management
**Objective**: Implement core note-taking functionality

**Tasks**:
1. **Notes Repository** (`shared/src/commonMain/kotlin/repository/`)
   - CRUD operations for notes with Ktor
   - Local database synchronization with SQLDelight
   - Search and filtering logic
   - Offline support with queue management

2. **Local Database** (`shared/src/commonMain/kotlin/database/`)
   - SQLDelight database setup for multiplatform
   - Local note storage and retrieval
   - Sync status tracking
   - Data migration handling

3. **Notes ViewModels** (`shared/src/commonMain/kotlin/presentation/`)
   - `NotesViewModel`: Notes list management with pagination
   - `NoteDetailViewModel`: Individual note display
   - `NoteEditViewModel`: Note creation and editing
   - `SearchViewModel`: Search and filter functionality

4. **Notes Screens** (`composeApp/src/commonMain/kotlin/screens/`)
   - `NotesScreen`: Main notes list with LazyColumn
   - `NoteDetailScreen`: Note viewing with media support
   - `NoteEditScreen`: Rich text editor with Compose
   - `SearchScreen`: Search interface with filters

### Phase 6: Media Management
**Objective**: Implement multimedia file handling

**Tasks**:
1. **Media Repository** (`shared/src/commonMain/kotlin/repository/`)
   - File upload operations with Ktor
   - File compression and optimization
   - Thumbnail generation logic
   - Media metadata handling

2. **Platform-Specific Media** (expect/actual pattern)
   - `CameraService`: Camera integration for photo/video capture
   - `MediaPicker`: Photo library access and selection
   - `FileManager`: File system operations
   - `PermissionManager`: Camera and storage permissions

3. **Media ViewModels** (`shared/src/commonMain/kotlin/presentation/`)
   - `MediaPickerViewModel`: Media selection logic
   - `MediaViewerViewModel`: Full-screen media viewing
   - `CameraViewModel`: Camera capture functionality

4. **Media Screens** (`composeApp/src/commonMain/kotlin/screens/`)
   - `MediaPickerScreen`: Media selection interface
   - `MediaViewerScreen`: Full-screen image/video viewer
   - `CameraScreen`: Custom camera interface with Compose

### Phase 7: Offline Support & Synchronization
**Objective**: Enable offline functionality with sync

**Tasks**:
1. **Sync Repository** (`shared/src/commonMain/kotlin/repository/`)
   - Background synchronization logic with coroutines
   - Conflict resolution strategies
   - Queue management for offline actions
   - Network connectivity monitoring

2. **Offline Features**
   - Local note creation and editing with SQLDelight
   - Offline media storage
   - Sync indicators and status in UI
   - Background sync on app resume

3. **Platform-Specific Connectivity** (expect/actual)
   - `NetworkMonitor`: Network status monitoring
   - `BackgroundSync`: Platform-specific background tasks
   - Retry mechanisms for failed requests
   - User notifications for sync status

### Phase 8: User Experience Enhancements
**Objective**: Polish and advanced UX features

**Tasks**:
1. **Animations & Transitions**
   - Screen transition animations with Compose
   - Loading state animations
   - Pull-to-refresh animations
   - Micro-interactions and ripple effects

2. **Accessibility**
   - Screen reader support with semantics
   - High contrast theme support
   - Large text support with scalable fonts
   - Focus management and keyboard navigation

3. **Performance Optimizations**
   - Image lazy loading and caching with Coil
   - LazyColumn/LazyRow optimization
   - Memory management optimization
   - Battery usage optimization

4. **Platform-Specific Features** (expect/actual)
   - iOS: Haptic feedback integration
   - Android: Material You dynamic colors
   - Native sharing capabilities
   - Platform-specific animations and gestures

### Phase 9: Settings & Customization
**Objective**: User preferences and app customization

**Tasks**:
1. **Settings Repository** (`shared/src/commonMain/kotlin/repository/`)
   - User preference management with DataStore
   - Theme selection (light/dark/auto)
   - Sync settings and intervals
   - Privacy and security settings

2. **Settings ViewModels** (`shared/src/commonMain/kotlin/presentation/`)
   - `SettingsViewModel`: Main settings management
   - `ProfileViewModel`: User profile management
   - `SecurityViewModel`: Security settings

3. **Settings Screens** (`composeApp/src/commonMain/kotlin/screens/`)
   - `SettingsScreen`: Main settings interface with Compose
   - `ProfileScreen`: User profile editing
   - `SecurityScreen`: Security and privacy settings

### Phase 10: Testing & Quality Assurance
**Objective**: Comprehensive testing across platforms

**Tasks**:
1. **Unit Tests** (`shared/src/commonTest/kotlin/`)
   - ViewModel logic testing with kotlin.test
   - Repository layer testing
   - Model validation testing
   - Use case testing

2. **Integration Tests**
   - API integration testing with Ktor mock engine
   - Database operations testing with SQLDelight
   - Authentication flow testing
   - Media handling testing

3. **UI Tests** (`composeApp/src/commonTest/kotlin/`)
   - Compose UI testing
   - Screen navigation testing
   - User interaction testing
   - Accessibility testing

4. **Platform Testing**
   - Android device testing (multiple API levels and screen sizes)
   - iOS device testing (iPhone and iPad various models)
   - Performance testing on both platforms

### Phase 11: Deployment & Distribution
**Objective**: Prepare for app store distribution

**Tasks**:
1. **Release Preparation**
   - Code signing certificates setup
   - App store metadata and screenshots
   - Privacy policy and terms of service
   - App icons and adaptive icons (Android)

2. **Platform-Specific Packaging**
   - Android: AAB generation with Gradle for Google Play Store
   - iOS: IPA generation with Xcode for Apple App Store
   - ProGuard/R8 optimization for Android
   - iOS app optimization and bitcode

3. **CI/CD Pipeline**
   - GitHub Actions or similar for automated builds
   - Automated testing on both platforms
   - Fastlane for deployment automation
   - TestFlight (iOS) and Internal Testing (Android) setup

## Development Notes

### Backend
- Uses implicit usings and nullable reference types enabled
- Follow RESTful principles and Problem Details error format
- Implement centralized error handling with MediatR pipeline behaviors
- All endpoints except authentication require JWT authorization
- Store complete file data as binary (byte[]) directly in database
- Use CQRS pattern with MediatR for clean separation of concerns

### Mobile App
- Use MVVM pattern with Kotlin Multiplatform ViewModels
- Implement proper error handling and user feedback
- Follow Material Design 3 guidelines with Compose Multiplatform
- Handle platform-specific requirements with expect/actual pattern
- Optimize for performance and battery usage
- Implement proper lifecycle management with Compose
- Use Koin for dependency injection across platforms
- Handle device orientation changes with Compose adaptive layouts

## Git Commit Instructions

- When committing changes, always use "Víctor León Herrera Arribas <vicherarr@gmail.com>" as co-author
- Update PROGRESOPROYECTO.md after completing each phase to track progress
- Follow conventional commit format: feat/fix/docs/refactor etc.
- **NEVER include "Generated with Claude Code" or any Claude-related text in commit messages**
- Separate commits for API and App changes when possible
- Use descriptive commit messages that explain the "why" not just the "what"