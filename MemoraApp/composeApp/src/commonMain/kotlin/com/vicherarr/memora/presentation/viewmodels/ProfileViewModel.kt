package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.config.BuildConfiguration
import com.vicherarr.memora.domain.models.AppInfo
import com.vicherarr.memora.domain.models.UserProfile
import com.vicherarr.memora.domain.models.UserStatistics
import com.vicherarr.memora.domain.repository.UserRepository
import com.vicherarr.memora.domain.usecase.ExitAppUseCase
import com.vicherarr.memora.presentation.states.BaseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Profile UI State - Single Source of Truth
 * 
 * Immutable data class representing the complete state of the Profile screen.
 * Following MVVM pattern with reactive state management.
 */
data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val userStatistics: UserStatistics? = null,
    val appInfo: AppInfo = createDefaultAppInfo(),
    val isRefreshing: Boolean = false,
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null
) : BaseUiState {
    companion object {
        private fun createDefaultAppInfo(): AppInfo {
            return AppInfo(
                versionName = BuildConfiguration.versionName,
                versionCode = BuildConfiguration.versionCode,
                buildType = BuildConfiguration.buildType,
                termsUrl = "https://memora.app/terms",
                privacyUrl = "https://memora.app/privacy",
                supportEmail = "soporte@memora.app"
            )
        }
    }
}

/**
 * Profile ViewModel
 * 
 * Handles profile screen business logic and state management.
 * Following MVVM pattern with Clean Architecture principles.
 * Single Responsibility: Only manages profile-related UI state.
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val exitAppUseCase: ExitAppUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        // Load initial data when ViewModel is created
        loadProfileData()
    }
    
    /**
     * Load all profile data
     * 
     * Orchestrates loading of user profile and statistics.
     * Following Single Responsibility Principle.
     */
    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                // Load user profile and statistics in parallel
                loadUserProfile()
                loadUserStatistics()
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error loading profile data"
                )
            }
        }
    }
    
    /**
     * Refresh profile data
     * 
     * Pull-to-refresh functionality with visual indicator.
     * Following Open/Closed Principle - can be extended for specific refresh logic.
     */
    fun refreshProfileData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                errorMessage = null
            )
            
            try {
                // Refresh user profile and statistics
                loadUserProfile()
                loadUserStatistics()
                
                _uiState.value = _uiState.value.copy(isRefreshing = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = e.message ?: "Error refreshing profile data"
                )
            }
        }
    }
    
    /**
     * Logout current user
     * 
     * Handles logout business logic with proper error handling.
     * Following Single Responsibility Principle.
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            userRepository.logout()
                .onSuccess {
                    // Logout successful - exit app completely
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userProfile = null,
                        userStatistics = null
                    )
                    // Exit app using use case (Clean Architecture)
                    exitAppUseCase.execute()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error during logout"
                    )
                }
        }
    }
    
    /**
     * Clear error message
     * 
     * Allows UI to dismiss error states.
     * Following Interface Segregation Principle.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    
    
    /**
     * Load user profile from repository
     * 
     * Private method following Single Responsibility Principle.
     * Handles user profile loading logic.
     */
    private suspend fun loadUserProfile() {
        userRepository.getCurrentUserProfile()
            .onSuccess { profile ->
                _uiState.value = _uiState.value.copy(userProfile = profile)
            }
            .onFailure { exception ->
                // Don't overwrite loading state here, let the caller handle it
                throw exception
            }
    }
    
    /**
     * Load user statistics from repository
     * 
     * Private method following Single Responsibility Principle.
     * Handles statistics loading logic.
     */
    private suspend fun loadUserStatistics() {
        userRepository.getUserStatistics()
            .onSuccess { statistics ->
                _uiState.value = _uiState.value.copy(userStatistics = statistics)
            }
            .onFailure { exception ->
                // Don't overwrite loading state here, let the caller handle it
                throw exception
            }
    }
    
    /**
     * Check if user data is available
     * 
     * Utility method for UI state checks.
     * Following Interface Segregation Principle.
     */
    fun hasUserData(): Boolean {
        val currentState = _uiState.value
        return currentState.userProfile != null && currentState.userStatistics != null
    }
    
    /**
     * Get formatted app version for display
     * 
     * Business logic for version formatting.
     * Following Single Responsibility Principle.
     */
    fun getFormattedAppVersion(): String {
        val appInfo = _uiState.value.appInfo
        return "${appInfo.versionName} (${appInfo.versionCode}) - ${appInfo.buildType}"
    }
}