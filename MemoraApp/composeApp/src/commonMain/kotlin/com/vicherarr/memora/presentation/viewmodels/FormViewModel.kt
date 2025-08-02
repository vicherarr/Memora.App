package com.vicherarr.memora.presentation.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple ViewModel for form state management
 */
class FormViewModel : BaseViewModel() {
    
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()
    
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    fun updateName(newName: String) {
        _name.value = newName
    }
    
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }
    
    fun clearForm() {
        _name.value = ""
        _email.value = ""
    }
}