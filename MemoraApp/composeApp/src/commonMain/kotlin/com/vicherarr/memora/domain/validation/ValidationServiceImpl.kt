package com.vicherarr.memora.domain.validation

/**
 * Implementation of ValidationService
 * Follows Single Responsibility Principle - only contains validation logic
 * Centralized validation rules to follow DRY principle
 */
class ValidationServiceImpl : ValidationService {
    
    override fun validateEmail(email: String): ValidationResult {
        val trimmedEmail = email.trim()
        
        return when {
            trimmedEmail.isBlank() -> ValidationResult.Invalid("El correo electrónico es requerido")
            !isValidEmailFormat(trimmedEmail) -> ValidationResult.Invalid("El correo electrónico no tiene un formato válido")
            else -> ValidationResult.Valid
        }
    }
    
    override fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid("La contraseña es requerida")
            password.length < 6 -> ValidationResult.Invalid("La contraseña debe tener al menos 6 caracteres")
            !isValidPasswordStrength(password) -> ValidationResult.Invalid("La contraseña debe contener al menos una letra y un número")
            else -> ValidationResult.Valid
        }
    }
    
    override fun validateName(name: String): ValidationResult {
        val trimmedName = name.trim()
        
        return when {
            trimmedName.isBlank() -> ValidationResult.Invalid("El nombre es requerido")
            trimmedName.length < 3 -> ValidationResult.Invalid("El nombre debe tener al menos 3 caracteres")
            trimmedName.length > 100 -> ValidationResult.Invalid("El nombre es demasiado largo (máximo 100 caracteres)")
            else -> ValidationResult.Valid
        }
    }
    
    override fun validateNoteContent(content: String): ValidationResult {
        val trimmedContent = content.trim()
        
        return when {
            trimmedContent.isBlank() -> ValidationResult.Invalid("El contenido de la nota es requerido")
            trimmedContent.length > 10000 -> ValidationResult.Invalid("El contenido es demasiado largo (máximo 10,000 caracteres)")
            else -> ValidationResult.Valid
        }
    }
    
    override fun validateNoteTitle(title: String): ValidationResult {
        val trimmedTitle = title.trim()
        
        return when {
            trimmedTitle.length > 200 -> ValidationResult.Invalid("El título es demasiado largo (máximo 200 caracteres)")
            else -> ValidationResult.Valid
        }
    }
    
    private fun isValidEmailFormat(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
    
    private fun isValidPasswordStrength(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
}