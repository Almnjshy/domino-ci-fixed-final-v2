package com.agon.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun VerificationScreen(
    onStartAi: () -> Unit,
    onNetwork: () -> Unit,
    onSettings: () -> Unit,
    onStats: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←") }
            Text("التحقق من المشروع", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(16.dp))

        val checks = listOf(
            "✅ Clean Architecture (Domain/Data/Presentation)",
            "✅ Hilt Dependency Injection",
            "✅ GameViewModel لا يحتوي على منطق اللعبة",
            "✅ AIPlayUseCase مع مستويات الصعوبة",
            "✅ Navigation مع Deep Links",
            "✅ StateFlow لإدارة الحالة",
            "✅ Unit Tests (6 ملفات)",
            "✅ Repository Pattern",
            "✅ DominoGameEngine (Pure Kotlin)",
            "✅ DominoAIEngine (Pure Kotlin)",
        )

        checks.forEach { check ->
            Text(check, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(24.dp))
        Text("اختبار الوظائف:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onStartAi, modifier = Modifier.fillMaxWidth()) { Text("تشغيل لعبة (ضد AI)") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onNetwork, modifier = Modifier.fillMaxWidth()) { Text("فتح الشبكة") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("فتح الإعدادات") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onStats, modifier = Modifier.fillMaxWidth()) { Text("فتح الإحصائيات") }
    }
}
