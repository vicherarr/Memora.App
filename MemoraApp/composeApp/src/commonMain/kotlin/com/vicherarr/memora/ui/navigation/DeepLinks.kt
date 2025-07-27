package com.vicherarr.memora.ui.navigation

/**
 * Configuración de Deep Links para la aplicación
 * Permite navegación directa a pantallas específicas desde URLs externas
 */
object DeepLinks {
    
    // Base URI para la aplicación
    const val BASE_URI = "memora://app"
    
    // Deep links para autenticación
    object Auth {
        const val LOGIN = "$BASE_URI/auth/login"
        const val REGISTER = "$BASE_URI/auth/register"
    }
    
    // Deep links para funcionalidades principales
    object Main {
        const val NOTES = "$BASE_URI/notes"
        const val NOTE_DETAIL = "$BASE_URI/notes/{noteId}"
        const val NOTE_EDIT = "$BASE_URI/notes/edit/{noteId}"
        const val NOTE_CREATE = "$BASE_URI/notes/create"
        const val SEARCH = "$BASE_URI/search"
        const val PROFILE = "$BASE_URI/profile"
        const val SETTINGS = "$BASE_URI/settings"
    }
    
    // Deep links para multimedia
    object Media {
        const val CAMERA = "$BASE_URI/camera"
        const val MEDIA_PICKER = "$BASE_URI/media/picker"
        const val MEDIA_VIEWER = "$BASE_URI/media/{mediaId}"
    }
    
    /**
     * Funciones helper para generar deep links dinámicos
     */
    fun createNoteDetailLink(noteId: String): String {
        return "$BASE_URI/notes/$noteId"
    }
    
    fun createNoteEditLink(noteId: String): String {
        return "$BASE_URI/notes/edit/$noteId"
    }
    
    fun createMediaViewerLink(mediaId: String): String {
        return "$BASE_URI/media/$mediaId"
    }
}

/**
 * Intent filters que deben ser agregados al AndroidManifest.xml:
 * 
 * <activity android:name=".MainActivity">
 *     <!-- Deep link intent filter -->
 *     <intent-filter android:autoVerify="true">
 *         <action android:name="android.intent.action.VIEW" />
 *         <category android:name="android.intent.category.DEFAULT" />
 *         <category android:name="android.intent.category.BROWSABLE" />
 *         <data android:scheme="memora" 
 *               android:host="app" />
 *     </intent-filter>
 *     
 *     <!-- Para URLs web (futuro) -->
 *     <intent-filter android:autoVerify="true">
 *         <action android:name="android.intent.action.VIEW" />
 *         <category android:name="android.intent.category.DEFAULT" />
 *         <category android:name="android.intent.category.BROWSABLE" />
 *         <data android:scheme="https"
 *               android:host="memora.app" />
 *     </intent-filter>
 * </activity>
 */