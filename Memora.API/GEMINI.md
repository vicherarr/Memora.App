# GEMINI.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Memora is a RESTful API backend for an Android mobile application that allows users to create, manage, and store personal notes with text, images, and videos. Built with ASP.NET Core 8.0 and Entity Framework Core for data persistence.

### Purpose
- Provide backend services for the "Memora" Android mobile application
- Enable users to create and manage personal notes with multimedia content
- Implement secure user authentication and data access controls
- Support file upload and storage for images and videos

## Development Commands

### Running the Application
```bash
dotnet run
```
The application will start on:
- HTTP: http://localhost:5003
- HTTPS: https://localhost:7241

### Building the Project
```bash
dotnet build
```

### Running with Docker
```bash
docker build -t memora .
docker run -p 8080:8080 -p 8081:8081 memora
```

### Restoring Dependencies
```bash
dotnet restore
```

### Database Migrations
```bash
# Add new migration
dotnet ef migrations add MigrationName

# Update database
dotnet ef database update
```

## Architecture & Technology Stack

- **Framework**: ASP.NET Core 8.0 Web API
- **Language**: C#
- **ORM**: Entity Framework Core 8
- **Database**: EF Core abstraction (supports SQL Server, PostgreSQL, SQLite)
- **Authentication**: JWT (JSON Web Tokens)
- **File Storage**: Direct storage in database as binary data (BLOB)
- **Error Handling**: Problem Details (RFC 7807) standard
- **Communication**: HTTPS with JSON data format

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

## Security Requirements

- All communication via HTTPS
- Password hashing with robust algorithms (Argon2/BCrypt)
- JWT-based authentication for protected endpoints
- Input validation and sanitization
- Protection against SQL injection (via EF Core)
- User data isolation (users can only access their own data)

## Performance & Scalability

- Response times under 500ms for GET requests
- File uploads up to 50MB per file (configurable) - stored as BLOB in database
- Pagination for list endpoints
- Database optimization for binary data storage and retrieval
- Docker containerization support
- Horizontal scaling capability
- Consider database size impact with binary file storage

## Configuration

### Swagger Documentation
- **Development**: Auto-generated API documentation at `/swagger` (always enabled)
- **Docker**: Enabled with `ASPNETCORE_ENVIRONMENT=Docker` environment variable
- **Production**: Disabled for security (override by setting environment to "Docker")
- **Features**: Comprehensive documentation with examples, OAuth2 flow, and interactive testing

### Other Configuration
- **HTTPS**: Required for all communications
- **JWT**: Token-based authentication system with 1-hour expiration
- **File Storage**: Direct database storage as binary data (BLOB)
- **User Secrets**: ID `3d6f68c0-06bf-43b1-b94a-c939b89cfd3e`

## Implementation Plan

### Phase 1: Project Foundation & Configuration
**Objective**: Set up the base infrastructure and dependencies

**Tasks**:
1. **Package Installation & Configuration**
   - Install Entity Framework Core packages
   - Install MediatR and MediatR.Extensions.Microsoft.DependencyInjection
   - Install JWT authentication packages
   - Install FluentValidation for request validation
   - Install AutoMapper for object mapping
   - Install BCrypt.Net-Next for password hashing

2. **Project Structure Setup**
   ```
   /Application
     /Common
       /Interfaces
       /Mappings
       /Behaviours
     /Features
       /Authentication
       /Notas
       /Archivos
   /Domain
     /Entities
     /Enums
   /Infrastructure
     /Data
     /Services
   /API
     /Controllers
     /Middleware
   ```

3. **Base Configuration**
   - Configure MediatR in Program.cs
   - Setup AutoMapper profiles
   - Configure FluentValidation pipeline behavior
   - Setup JWT authentication middleware
   - Configure Swagger with JWT support

### Phase 2: Domain Models & Database Setup
**Objective**: Create domain entities and database context

**Tasks**:
1. **Domain Entities** (`/Domain/Entities/`)
   - `Usuario.cs`: User entity with properties and relationships
   - `Nota.cs`: Note entity with validation attributes
   - `ArchivoAdjunto.cs`: File attachment entity with binary data
   - `TipoDeArchivo.cs`: File type enumeration

2. **Database Context** (`/Infrastructure/Data/`)
   - `MemoraDbContext.cs`: EF Core context with DbSets
   - Entity configurations for relationships and constraints
   - Database seeding configuration

3. **Database Migration**
   - Create initial migration with all entities
   - Configure indexes for performance (UserId, NotaId, etc.)
   - Set up proper foreign key constraints

### Phase 3: Authentication System with MediatR
**Objective**: Implement user registration and login functionality

**MediatR Commands/Queries**:
```csharp
// Commands
RegisterUserCommand: { NombreUsuario, CorreoElectronico, Contrasena }
LoginUserCommand: { CorreoElectronico, Contrasena }

// Handlers
RegisterUserCommandHandler: IRequestHandler<RegisterUserCommand, UsuarioDto>
LoginUserCommandHandler: IRequestHandler<LoginUserCommand, LoginResponseDto>
```

**Tasks**:
1. **Application Layer** (`/Application/Features/Authentication/`)
   - Commands: `RegisterUserCommand`, `LoginUserCommand`
   - Command Handlers with business logic
   - DTOs: `UsuarioDto`, `LoginResponseDto`, `RegisterUserDto`
   - Validators: `RegisterUserCommandValidator`, `LoginUserCommandValidator`

2. **Infrastructure Services** (`/Infrastructure/Services/`)
   - `IJwtTokenService` and `JwtTokenService` implementation
   - `IPasswordHashService` and `PasswordHashService` implementation
   - User repository pattern implementation

3. **API Controller** (`/API/Controllers/`)
   - `AutenticacionController` with register and login endpoints
   - Request/response DTOs mapping
   - Proper error handling with Problem Details

### Phase 4: Notes Management Features
**Objective**: Complete CRUD operations for notes using MediatR

**MediatR Commands/Queries**:
```csharp
// Queries
GetUserNotasQuery: { UsuarioId, PageNumber, PageSize }
GetNotaByIdQuery: { NotaId, UsuarioId }

// Commands
CreateNotaCommand: { Titulo, Contenido, UsuarioId }
UpdateNotaCommand: { NotaId, Titulo, Contenido, UsuarioId }
DeleteNotaCommand: { NotaId, UsuarioId }
```

**Tasks**:
1. **Application Layer** (`/Application/Features/Notas/`)
   - Queries: `GetUserNotasQuery`, `GetNotaByIdQuery`
   - Commands: `CreateNotaCommand`, `UpdateNotaCommand`, `DeleteNotaCommand`
   - Query/Command Handlers with authorization logic
   - DTOs: `NotaDto`, `NotaDetailDto`, `CreateNotaDto`, `UpdateNotaDto`
   - Validators for all commands and queries
   - AutoMapper profiles for entity-DTO mapping

2. **Authorization Logic**
   - Ensure users can only access their own notes
   - Implement resource-based authorization

3. **API Controller** (`/API/Controllers/`)
   - `NotasController` with all CRUD endpoints
   - Pagination support for GET endpoints
   - JWT authorization attributes

### Phase 5: File Attachment Management
**Objective**: Handle file uploads and downloads with database storage

**MediatR Commands/Queries**:
```csharp
// Queries
GetArchivoByIdQuery: { ArchivoId, UsuarioId }

// Commands
UploadArchivoCommand: { NotaId, UsuarioId, FileData, FileName, ContentType }
DeleteArchivoCommand: { ArchivoId, UsuarioId }
```

**Tasks**:
1. **Application Layer** (`/Application/Features/Archivos/`)
   - Commands: `UploadArchivoCommand`, `DeleteArchivoCommand`
   - Queries: `GetArchivoByIdQuery`
   - Handlers with file validation and processing
   - DTOs: `ArchivoAdjuntoDto`, `UploadArchivoDto`
   - File validation (size, type, MIME validation)

2. **File Processing Services** (`/Infrastructure/Services/`)
   - `IFileProcessingService` for file validation and processing
   - MIME type validation and file type detection
   - File size validation and compression if needed

3. **API Controller** (`/API/Controllers/`)
   - File upload endpoint with multipart/form-data support
   - File download endpoint with proper Content-Type headers
   - Streaming support for large files

### Phase 6: Validation & Error Handling
**Objective**: Implement comprehensive validation and centralized error handling

**Tasks**:
1. **FluentValidation Rules**
   - User registration validation (email format, password strength)
   - Note validation (title length, content requirements)
   - File validation (size limits, allowed types, MIME validation)

2. **MediatR Pipeline Behaviors** (`/Application/Common/Behaviours/`)
   - `ValidationBehaviour<TRequest, TResponse>`: Automatic validation
   - `LoggingBehaviour<TRequest, TResponse>`: Request/response logging
   - `PerformanceBehaviour<TRequest, TResponse>`: Performance monitoring

3. **Global Exception Handling** (`/API/Middleware/`)
   - `GlobalExceptionHandlingMiddleware`
   - Problem Details (RFC 7807) implementation
   - Custom exception types for business logic violations

### Phase 7: Security & Performance Optimizations
**Objective**: Ensure security best practices and optimize performance

**Tasks**:
1. **Security Enhancements**
   - Input sanitization for all user inputs
   - SQL injection prevention through EF Core
   - XSS protection for text content
   - Rate limiting for API endpoints
   - CORS configuration for mobile app

2. **Performance Optimizations**
   - Database query optimization with proper includes
   - Pagination implementation with efficient queries
   - Caching strategy for frequently accessed data
   - Database indexes for search performance
   - Memory optimization for large file handling

3. **Monitoring & Logging**
   - Application Insights or Serilog configuration
   - Performance counters and metrics
   - Health checks implementation

### Phase 8: Testing Strategy
**Objective**: Comprehensive testing coverage

**Tasks**:
1. **Unit Tests**
   - MediatR handler unit tests with mocked dependencies
   - Validation logic tests
   - Service layer tests
   - Domain entity tests

2. **Integration Tests**
   - API endpoint integration tests
   - Database integration tests with in-memory provider
   - Authentication flow tests
   - File upload/download tests

3. **Test Data & Fixtures**
   - Test data builders for entities
   - Database seeding for tests
   - Mock file data for attachment tests

### Phase 9: Documentation & Deployment
**Objective**: Complete documentation and deployment preparation

**Tasks**:
1. **API Documentation**
   - Comprehensive Swagger/OpenAPI documentation
   - Request/response examples
   - Authentication documentation
   - Error response documentation

2. **Deployment Configuration**
   - Docker configuration optimization
   - Environment-specific appsettings
   - Database migration scripts
   - CI/CD pipeline setup

## Development Notes

- Uses implicit usings and nullable reference types enabled
- Follow RESTful principles and Problem Details error format
- Implement centralized error handling with MediatR pipeline behaviors
- All endpoints except authentication require JWT authorization
- Store complete file data as binary (byte[]) directly in database
- Controllers should validate user ownership of resources
- Use CQRS pattern with MediatR for clean separation of concerns
- Implement repository pattern for data access abstraction
- Follow clean architecture principles with proper dependency injection

## Git Commit Instructions

- When committing changes, always use "Víctor León Herrera Arribas <vicherarr@gmail.com>" as co-author
- Update PROGRESOPROYECTO.md after completing each phase to track progress
- Follow conventional commit format: feat/fix/docs/refactor etc.
- **NEVER include "Generated with Claude Code" or any Claude-related text in commit messages**