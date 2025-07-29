package com.vicherarr.memora.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Resultado de validación
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
    object Empty : ValidationResult()
}

/**
 * Campo de texto con validación en tiempo real
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    validation: (String) -> ValidationResult,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true,
    isRequired: Boolean = false
) {
    val validationResult = remember(value) { validation(value) }
    val isError = validationResult is ValidationResult.Invalid
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(label)
                    if (isRequired) {
                        Text(
                            text = "*",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            },
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            enabled = enabled,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                when (validationResult) {
                    is ValidationResult.Valid -> {
                        if (value.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Válido",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    is ValidationResult.Invalid -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    is ValidationResult.Empty -> { /* No icon */ }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = when (validationResult) {
                    is ValidationResult.Valid -> if (value.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    is ValidationResult.Invalid -> MaterialTheme.colorScheme.tertiary
                    is ValidationResult.Empty -> MaterialTheme.colorScheme.outline
                },
                unfocusedBorderColor = when (validationResult) {
                    is ValidationResult.Valid -> MaterialTheme.colorScheme.outline
                    is ValidationResult.Invalid -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                    is ValidationResult.Empty -> MaterialTheme.colorScheme.outline
                },
                focusedLabelColor = when (validationResult) {
                    is ValidationResult.Valid -> if (value.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    is ValidationResult.Invalid -> MaterialTheme.colorScheme.onTertiaryContainer
                    is ValidationResult.Empty -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                unfocusedLabelColor = when (validationResult) {
                    is ValidationResult.Valid -> MaterialTheme.colorScheme.onSurfaceVariant
                    is ValidationResult.Invalid -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    is ValidationResult.Empty -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                errorBorderColor = MaterialTheme.colorScheme.tertiary,
                errorLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                errorLeadingIconColor = MaterialTheme.colorScheme.tertiary,
                errorTrailingIconColor = MaterialTheme.colorScheme.tertiary
            )
        )
        
        // Mostrar mensaje de error
        if (validationResult is ValidationResult.Invalid) {
            Text(
                text = validationResult.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Indicador de caracteres con límite
 */
@Composable
fun CharacterCounter(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    val isNearLimit = current > max * 0.8
    val isOverLimit = current > max
    
    val color = when {
        isOverLimit -> MaterialTheme.colorScheme.tertiary
        isNearLimit -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Text(
        text = "$current/$max",
        style = MaterialTheme.typography.bodySmall,
        color = color,
        fontWeight = if (isNearLimit) FontWeight.Medium else FontWeight.Normal,
        modifier = modifier
    )
}

/**
 * Resumen de validación del formulario
 */
@Composable
fun FormValidationSummary(
    validations: Map<String, ValidationResult>,
    modifier: Modifier = Modifier
) {
    val errors = validations.values.filterIsInstance<ValidationResult.Invalid>()
    
    if (errors.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Text(
                        text = "Corrige los siguientes errores:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                errors.forEach { error ->
                    Text(
                        text = "• ${error.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Indicador de fortaleza de validación
 */
@Composable
fun ValidationStrengthIndicator(
    validations: List<ValidationResult>,
    modifier: Modifier = Modifier
) {
    val validCount = validations.count { it is ValidationResult.Valid }
    val totalCount = validations.size
    val strength = if (totalCount > 0) validCount.toFloat() / totalCount else 0f
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Validez del formulario",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "$validCount/$totalCount",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = when {
                    strength == 1f -> MaterialTheme.colorScheme.primary
                    strength > 0.5f -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                }
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = strength,
            modifier = Modifier.fillMaxWidth(),
            color = when {
                strength == 1f -> MaterialTheme.colorScheme.primary
                strength > 0.5f -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.tertiary
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Validaciones comunes para formularios de notas
 */
object NoteValidations {
    fun titleValidation(title: String): ValidationResult {
        return when {
            title.length > 200 -> ValidationResult.Invalid("El título no puede exceder 200 caracteres")
            else -> ValidationResult.Valid
        }
    }
    
    fun contentValidation(content: String): ValidationResult {
        return when {
            content.isBlank() -> ValidationResult.Invalid("El contenido es obligatorio")
            content.length > 10000 -> ValidationResult.Invalid("El contenido no puede exceder 10,000 caracteres")
            content.length < 3 -> ValidationResult.Invalid("El contenido debe tener al menos 3 caracteres")
            else -> ValidationResult.Valid
        }
    }
    
    fun contentValidationOptional(content: String): ValidationResult {
        return when {
            content.isEmpty() -> ValidationResult.Empty
            content.length > 10000 -> ValidationResult.Invalid("El contenido no puede exceder 10,000 caracteres")
            else -> ValidationResult.Valid
        }
    }
}