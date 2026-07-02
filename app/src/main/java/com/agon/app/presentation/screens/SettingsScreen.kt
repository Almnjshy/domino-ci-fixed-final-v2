package com.agon.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agon.app.domain.model.AppSettings
import com.agon.app.domain.model.GameMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    hasChanges: Boolean,
    isLoading: Boolean,
    error: String?,
    showResetConfirmation: Boolean,
    saveSuccess: Boolean,
    onVolumeChange: (Float) -> Unit,
    onEffectsToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onModeChange: (GameMode) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onShowResetConfirmation: () -> Unit,
    onDismissResetConfirmation: () -> Unit,
    onDismissSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←") }
            Text("الإعدادات", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(16.dp))

        Text("الصوت", style = MaterialTheme.typography.titleMedium)
        Slider(value = settings.volume, onValueChange = onVolumeChange, valueRange = 0f..1f)
        Text("المستوى: ${(settings.volume * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("المؤثرات الصوتية")
            Switch(checked = settings.effectsEnabled, onCheckedChange = onEffectsToggle)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("الاهتزاز")
            Switch(checked = settings.vibrationEnabled, onCheckedChange = onVibrationToggle)
        }

        Spacer(Modifier.height(16.dp))
        Text("وضع اللعب المفضل", style = MaterialTheme.typography.titleMedium)
        GameMode.entries.forEach { mode ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = settings.preferredMode == mode, onClick = { onModeChange(mode) })
                Text(mode.name)
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSave, enabled = hasChanges && !isLoading, modifier = Modifier.weight(1f)) { Text("حفظ") }
            OutlinedButton(onClick = onShowResetConfirmation, modifier = Modifier.weight(1f)) { Text("إعادة تعيين") }
        }

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onClearError) { Text("إغلاق") }
        }
        if (saveSuccess) {
            Spacer(Modifier.height(8.dp))
            Text("تم الحفظ بنجاح ✓", color = MaterialTheme.colorScheme.primary)
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(2000); onDismissSaveSuccess() }
        }
        if (isLoading) { Spacer(Modifier.height(8.dp)); CircularProgressIndicator() }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissResetConfirmation,
            title = { Text("إعادة تعيين الإعدادات") },
            text = { Text("هل تريد إعادة جميع الإعدادات للقيم الافتراضية؟") },
            confirmButton = { Button(onClick = onReset) { Text("نعم، إعادة تعيين") } },
            dismissButton = { TextButton(onClick = onDismissResetConfirmation) { Text("إلغاء") } }
        )
    }
}
