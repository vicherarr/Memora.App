package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.Image
import memora.composeapp.generated.resources.Res
import memora.composeapp.generated.resources.ic_logo
import org.jetbrains.compose.resources.painterResource

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vicherarr.memora.presentation.viewmodels.SplashNavigationState
import com.vicherarr.memora.presentation.viewmodels.SplashViewModel
import org.koin.compose.koinInject

/**
 * Splash Screen
 * 
 * Initial screen that verifies authentication and navigates accordingly.
 * Following MVVM pattern with Clean Architecture.
 */
class SplashScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: SplashViewModel = koinInject()
        
        val navigationState by viewModel.navigationState.collectAsState()
        
        // Handle navigation based on authentication state
        LaunchedEffect(navigationState) {
            when (navigationState) {
                is SplashNavigationState.NavigateToMain -> {
                    navigator.replace(MainScreen())
                }
                is SplashNavigationState.NavigateToWelcome -> {
                    navigator.replace(WelcomeScreen())
                }
                is SplashNavigationState.Loading -> {
                    // Stay on splash screen
                }
            }
        }
        
        // Animated splash screen UI
        val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
        
        // Logo animation - combination of scale, rotation and alpha for elegant effect
        val logoScale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logo_scale"
        )
        
        val logoRotation by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logo_rotation"
        )
        
        val logoAlpha by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logo_alpha"
        )
        
        // Multiple animations for all memory orbs - each with unique rhythm
        val centralOrbScale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "central_orb_scale"
        )
        
        val orb1Scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb1_scale"
        )
        
        val orb2Scale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb2_scale"
        )
        
        val orb3Scale by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = EaseInOutBack),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb3_scale"
        )
        
        val orb4Scale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb4_scale"
        )
        
        val titleAlpha by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "title_alpha"
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.05f)
                        ),
                        radius = 800f
                    )
                )
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            // Background memory orbs - each with unique animations representing stored memories
            Box(
                modifier = Modifier
                    .offset(x = (-80).dp, y = (-120).dp)
                    .size(60.dp)
                    .scale(orb1Scale)
                    .alpha(0.6f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            
            Box(
                modifier = Modifier
                    .offset(x = 100.dp, y = (-80).dp)
                    .size(40.dp)
                    .scale(orb2Scale)
                    .alpha(0.5f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            
            Box(
                modifier = Modifier
                    .offset(x = (-60).dp, y = 140.dp)
                    .size(35.dp)
                    .scale(orb3Scale)
                    .alpha(0.4f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            
            Box(
                modifier = Modifier
                    .offset(x = 90.dp, y = 120.dp)
                    .size(25.dp)
                    .scale(orb4Scale)
                    .alpha(0.5f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            
            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo container with circular background
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Logo animado
                    Image(
                        painter = painterResource(Res.drawable.ic_logo),
                        contentDescription = "Memora Logo",
                        modifier = Modifier
                            .size(120.dp)
                            .scale(logoScale)
                            .rotate(logoRotation)
                            .alpha(logoAlpha)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App title with breathing animation
                Text(
                    text = "Memora",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(titleAlpha),
                    fontSize = 36.sp,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tagline emphasizing memory preservation
                Text(
                    text = "Preserva tus recuerdos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(titleAlpha * 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Animated loading dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .scale(orb1Scale * 0.5f)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .scale(orb2Scale * 0.4f)
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .scale(orb3Scale * 0.6f)
                            .background(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}