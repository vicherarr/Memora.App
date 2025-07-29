package com.vicherarr.memora.presentation.utils

/**
 * Manejo centralizado de errores
 * Proporciona mensajes de error localizados y contextuales
 */
object ErrorHandler {
    
    /**
     * Categorías de errores para manejo específico
     */
    sealed class ErrorCategory {
        object Network : ErrorCategory()
        object Authentication : ErrorCategory()
        object Validation : ErrorCategory()
        object NotFound : ErrorCategory()
        object Permission : ErrorCategory()
        object Storage : ErrorCategory()
        object Unknown : ErrorCategory()
    }
    
    /**
     * Información de error estructurada
     */
    data class ErrorInfo(
        val category: ErrorCategory,
        val userMessage: String,
        val actionMessage: String? = null,
        val isRetryable: Boolean = false,
        val debugMessage: String? = null
    )
    
    /**
     * Procesa una excepción y devuelve información de error estructurada
     */
    fun processError(throwable: Throwable, context: String = ""): ErrorInfo {
        return when {
            // Errores de red
            throwable.message?.contains("network", ignoreCase = true) == true ||
            throwable.message?.contains("connection", ignoreCase = true) == true -> {
                ErrorInfo(
                    category = ErrorCategory.Network,
                    userMessage = "Error de conexión",
                    actionMessage = "Verifica tu conexión a internet",
                    isRetryable = true,
                    debugMessage = throwable.message
                )
            }
            
            // Errores de autenticación
            throwable.message?.contains("401", ignoreCase = true) == true ||
            throwable.message?.contains("unauthorized", ignoreCase = true) == true -> {
                ErrorInfo(
                    category = ErrorCategory.Authentication,
                    userMessage = "Sesión expirada",
                    actionMessage = "Inicia sesión nuevamente",
                    isRetryable = false,
                    debugMessage = throwable.message
                )
            }
            
            // Errores de validación
            throwable.message?.contains("validation", ignoreCase = true) == true ||
            throwable.message?.contains("invalid", ignoreCase = true) == true -> {
                ErrorInfo(
                    category = ErrorCategory.Validation,
                    userMessage = "Datos inválidos",
                    actionMessage = "Revisa la información ingresada",
                    isRetryable = false,
                    debugMessage = throwable.message
                )
            }
            
            // Errores de no encontrado
            throwable.message?.contains("404", ignoreCase = true) == true ||
            throwable.message?.contains("not found", ignoreCase = true) == true -> {
                ErrorInfo(
                    category = ErrorCategory.NotFound,
                    userMessage = when (context) {
                        "note" -> "Nota no encontrada"
                        "user" -> "Usuario no encontrado"
                        else -> "Elemento no encontrado"
                    },
                    actionMessage = "El elemento puede haber sido eliminado",
                    isRetryable = false,
                    debugMessage = throwable.message
                )
            }
            
            // Errores de permisos
            throwable.message?.contains("403", ignoreCase = true) == true ||
            throwable.message?.contains("forbidden", ignoreCase = true) == true -> {
                ErrorInfo(
                    category = ErrorCategory.Permission,
                    userMessage = "Sin permisos",
                    actionMessage = "No tienes permisos para esta acción",
                    isRetryable = false,
                    debugMessage = throwable.message
                )
            }
            
            // Errores de almacenamiento
            throwable.message?.contains("storage", ignoreCase = true) == true ||
            throwable.message?.contains("disk", ignoreCase = true) == true -> {
                ErrorInfo(
                    category = ErrorCategory.Storage,
                    userMessage = "Error de almacenamiento",
                    actionMessage = "Verifica el espacio disponible",
                    isRetryable = true,
                    debugMessage = throwable.message
                )
            }
            
            // Error genérico
            else -> {
                ErrorInfo(
                    category = ErrorCategory.Unknown,
                    userMessage = when (context) {
                        "load" -> "Error al cargar datos"
                        "save" -> "Error al guardar"
                        "delete" -> "Error al eliminar"
                        "create" -> "Error al crear"
                        "update" -> "Error al actualizar"
                        else -> "Ha ocurrido un error"
                    },
                    actionMessage = "Inténtalo de nuevo",
                    isRetryable = true,
                    debugMessage = throwable.message
                )
            }
        }
    }
    
    /**
     * Mensajes específicos para operaciones de notas
     */
    object NotesErrors {
        fun loadError() = ErrorInfo(
            category = ErrorCategory.Unknown,
            userMessage = "Error al cargar las notas",
            actionMessage = "Verifica tu conexión e inténtalo de nuevo",
            isRetryable = true
        )
        
        fun saveError() = ErrorInfo(
            category = ErrorCategory.Unknown,
            userMessage = "Error al guardar la nota",
            actionMessage = "Inténtalo de nuevo",
            isRetryable = true
        )
        
        fun deleteError() = ErrorInfo(
            category = ErrorCategory.Unknown,
            userMessage = "Error al eliminar la nota",
            actionMessage = "Inténtalo de nuevo",
            isRetryable = true
        )
        
        fun notFoundError() = ErrorInfo(
            category = ErrorCategory.NotFound,
            userMessage = "Nota no encontrada",
            actionMessage = "La nota puede haber sido eliminada",
            isRetryable = false
        )
        
        fun validationError(field: String) = ErrorInfo(
            category = ErrorCategory.Validation,
            userMessage = when (field) {
                "content" -> "El contenido es obligatorio"
                "title" -> "El título es demasiado largo"
                else -> "Datos inválidos"
            },
            actionMessage = "Revisa la información ingresada",
            isRetryable = false
        )
    }
}