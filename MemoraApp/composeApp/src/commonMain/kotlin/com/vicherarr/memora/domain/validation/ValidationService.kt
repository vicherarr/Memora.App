package com.vicherarr.memora.domain.validation

/**
 * Result of a validation operation
 * Sealed class following SOLID principles for extensibility
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
    
    val isValid: Boolean get() = this is Valid
    val errorMessage: String? get() = (this as? Invalid)?.message
}

/**
 * Service interface for input validation
 * Follows Interface Segregation Principle - focused only on validation
 * Follows Single Responsibility Principle - only validates data
 */
interface ValidationService {
    fun validateEmail(email: String): ValidationResult
    fun validatePassword(password: String): ValidationResult
    fun validateName(name: String): ValidationResult
    fun validateNoteContent(content: String): ValidationResult
    fun validateNoteTitle(title: String): ValidationResult
}