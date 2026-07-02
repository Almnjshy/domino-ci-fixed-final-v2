package com.agon.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DemoScreen(
    onStartAi: () -> Unit,
    onCreateNetworkRoom: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("عرض تجريبي", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("اختبر مميزات التطبيق", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onStartAi, modifier = Modifier.fillMaxWidth()) { Text("العب ضد AI") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onCreateNetworkRoom, modifier = Modifier.fillMaxWidth()) { Text("أنشئ غرفة شبكية") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("العودة") }
    }
}
