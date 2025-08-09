# Plan de Implementación: Sincronización de DB con KMP, iCloud y Google Drive

## 1. Resumen Ejecutivo

Este documento describe la estrategia y el plan de implementación para un sistema de persistencia y sincronización de datos basado en los servicios de almacenamiento en la nube del usuario: **iCloud Drive para iOS** y **Google Drive para Android**.

El sistema se desarrollará sobre una arquitectura **Kotlin Multiplatform (KMP)** para maximizar la reutilización de código. El objetivo es almacenar un único archivo de base de datos **SQLite** en la nube, permitiendo que la aplicación Memora sincronice las notas de forma eficiente y profesional, gestionando conflictos y garantizando la integridad de los datos.

## 2. Estrategia Principal: Sincronización de Deltas

La estrategia se basa en una **sincronización a nivel de registros (deltas)**, no a nivel de archivo completo. El archivo de la base de datos en la nube actúa como un "vehículo" para transportar los cambios, minimizando la transferencia de datos y permitiendo una fusión inteligente.

### Conceptos Clave:

1.  **Fuente Única de Verdad**: El archivo de la base de datos en la nube se considera la fuente de verdad maestra.
2.  **Copia Local**: Cada dispositivo mantiene una copia local completa de la base de datos SQLite para permitir el funcionamiento sin conexión.
3.  **Motor de Sincronización (Sync Engine)**: Un **componente lógico interno de la aplicación**, escrito en KMP, responsable de orquestar todo el proceso. No es un producto de terceros.
4.  **Resolución de Conflictos**: La estrategia por defecto será **"la última escritura gana" (Last Write Wins)**, basada en un timestamp `FechaModificacion` (UTC) en cada registro.

## 3. Adaptación del Esquema de la Base de Datos (SQLDelight)

Usaremos SQLDelight en KMP para definir el esquema y las consultas en código común.

```sql
-- En /commonMain/sqldelight/com/memora/db/Nota.sq

CREATE TABLE Nota (
    Id TEXT NOT NULL PRIMARY KEY,
    Titulo TEXT,
    Contenido TEXT NOT NULL,
    FechaModificacion INTEGER NOT NULL, -- Guardamos como Unix Timestamp UTC
    Eliminado INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE MetadatosSincronizacion (
    Clave TEXT PRIMARY KEY,
    Valor TEXT NOT NULL
);

-- Consultas necesarias para el Sync Engine
selectNotasModificadasDespuesDe:
SELECT * FROM Nota WHERE FechaModificacion > ?;

selectNotasEliminadasDespuesDe:
SELECT Id FROM Nota WHERE Eliminado = 1 AND FechaModificacion > ?;

getUltimaSincronizacion:
SELECT Valor FROM MetadatosSincronizacion WHERE Clave = 'UltimaSincronizacionExitosaUTC';
```

## 4. Arquitectura Detallada del Motor de Sincronización en KMP

El motor se construirá dentro de la aplicación siguiendo los principios de KMP para compartir la máxima cantidad de código.

### 4.1. `commonMain` - El Núcleo Compartido (~80% del código)

Contendrá toda la lógica de negocio agnóstica a la plataforma.

*   **SQLDelight:** Definición del esquema (`.sq`) y generación de las interfaces de base de datos.
*   **`DatabaseMerger.kt`:** Una clase pura en Kotlin que contiene la lógica SQL para fusionar dos bases de datos (la local y la remota temporal) basándose en el algoritmo de deltas.
*   **`SyncEngine.kt`:** El orquestador principal. Su lógica (llamar a descargar, fusionar, subir) es código 100% común. Depende de la interfaz `CloudStorageProvider`.
*   **Declaraciones `expect`:** El puente hacia el código específico de la plataforma.
    ```kotlin
    // package com.memora.sync.provider

    // La interfaz que define el contrato para el almacenamiento en la nube
    expect interface CloudStorageProvider {
        suspend fun autenticar()
        suspend fun descargarDB(): ByteArray?
        suspend fun subirDB(data: ByteArray)
        suspend fun obtenerMetadatosRemotos(): Long? // Timestamp de modificación del archivo
    }

    // Una fábrica para obtener la implementación correcta en cada plataforma
    expect fun getCloudStorageProvider(): CloudStorageProvider
    ```

### 4.2. `androidMain` - Implementación para Android

Se proporcionan las implementaciones `actual` para las `expect` de `commonMain`.

*   **Dependencias:** Se añaden las librerías de `play-services-auth` y `google-api-services-drive`.
*   **`GoogleDriveStorageProvider.kt`:**
    *   Clase `actual` que implementa `CloudStorageProvider`.
    *   Contiene el código que utiliza el SDK de Google para iniciar sesión, solicitar permisos y realizar operaciones de subida/bajada de archivos en el `AppDataFolder` del usuario.
    *   Maneja el `Context` de Android, necesario para estas operaciones.
*   **Fábrica `actual`:**
    ```kotlin
    actual fun getCloudStorageProvider(): CloudStorageProvider {
        // Se obtiene el contexto de Android y se instancia el proveedor.
        return GoogleDriveStorageProvider(AndroidAppContext.get())
    }
    ```

### 4.3. `iosMain` - Implementación para iOS

Se cumple el contrato `expect` para el ecosistema de Apple.

*   **`iCloudStorageProvider.kt`:**
    *   Clase `actual` que implementa `CloudStorageProvider`.
    *   Contiene el código que interactúa con las APIs nativas de iOS (`Foundation`, `CloudKit`).
    *   Usa `NSFileManager` y `NSMetadataQuery` para localizar y gestionar el archivo de la base de datos en el "Ubiquity Container" privado de la aplicación en iCloud Drive.
    *   Convierte tipos de datos nativos como `NSData` a `ByteArray` de Kotlin.
*   **Fábrica `actual`:**
    ```kotlin
    actual fun getCloudStorageProvider(): CloudStorageProvider {
        return iCloudStorageProvider()
    }
    ```

## 5. Fases de Implementación con KMP

1.  **Fase 1: Configuración del Proyecto KMP.**
    *   Configurar un nuevo proyecto KMP con los módulos `androidApp` y `iosApp`.
    *   Integrar SQLDelight y configurar la generación de código para la base de datos.

2.  **Fase 2: Desarrollo del Núcleo Común (`commonMain`).**
    *   Definir el esquema de la base de datos en los archivos `.sq`.
    *   Implementar la clase `DatabaseMerger` con toda la lógica de fusión SQL.
    *   Definir las declaraciones `expect` para `CloudStorageProvider`.
    *   Implementar la clase `SyncEngine` que orquesta el proceso.

3.  **Fase 3: Implementación del Proveedor de Android (`androidMain`).**
    *   Crear la clase `GoogleDriveStorageProvider` que implementa `actual`-mente la interfaz.
    *   Integrar el flujo de autenticación de Google Sign-In en la UI de Android.

4.  **Fase 4: Implementación del Proveedor de iOS (`iosMain`).**
    *   Crear la clase `iCloudStorageProvider` con la implementación `actual`.
    *   Configurar las capacidades de iCloud en el proyecto de Xcode.

5.  **Fase 5: Integración con la UI (ViewModel Compartido).**
    *   Crear un `ViewModel` en `commonMain`.
    *   Este `ViewModel` obtendrá el `SyncEngine` usando la fábrica `getCloudStorageProvider()`.
    *   La UI (Compose en Android, SwiftUI en iOS) llamará a las funciones del `ViewModel` para iniciar la sincronización y observar su estado (cargando, éxito, error).

## 6. Pruebas Exhaustivas

*   **Unit Tests (en `commonTest`):** Probar la lógica de `DatabaseMerger` y `SyncEngine` usando un proveedor de nube falso (mock) para asegurar que el algoritmo de fusión es correcto.
*   **Integration Tests (en `androidTest` y `iosTest`):** Probar el flujo completo en cada plataforma, interactuando con los servicios reales de Google Drive y iCloud.

## 7. Consideraciones de Seguridad y Riesgos

*   **Seguridad**: Los datos están protegidos por la seguridad de las cuentas de Apple/Google. La transmisión de datos es gestionada por sus SDKs, que usan HTTPS.
*   **Consumo de Batería/Datos**: El enfoque de deltas es eficiente, pero la sincronización en segundo plano debe gestionarse con cuidado (ej. usando WorkManager en Android y Background Tasks en iOS).
*   **Evolución del Esquema**: Las futuras migraciones del esquema de SQLDelight deben planificarse cuidadosamente para mantener la compatibilidad entre dispositivos con diferentes versiones de la app.
