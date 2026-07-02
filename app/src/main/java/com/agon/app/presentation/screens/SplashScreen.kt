package com.agon.app.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen(
    progress: Float,
    isLoading: Boolean,
    error: String?,
    onReady: () -> Unit,
    onRetry: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 1f else 0f,
        animationSpec = tween(500), label = "alpha"
    )

    LaunchedEffect(isLoading, error) {
        if (!isLoading && error == null) onReady()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🁣", fontSize = 80.sp, modifier = Modifier.alpha(alpha))
            Spacer(Modifier.height(16.dp))
            Text("دومينو", style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onRetry) { Text("إعادة المحاولة") }
            } else {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.width(200.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
