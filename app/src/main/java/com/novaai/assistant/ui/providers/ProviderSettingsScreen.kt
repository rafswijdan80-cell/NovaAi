package com.novaai.assistant.ui.providers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novaai.assistant.ai.ProviderId
import com.novaai.assistant.ai.RoutingConfig
import com.novaai.assistant.ai.SelectionMode

@Composable
fun ProviderSettingsScreen(
    onBack: () -> Unit,
    viewModel: ProviderViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val config = state.config

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Pilihan AI",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = onBack) {
                Text("Kembali")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Mode pemilihan",
            style = MaterialTheme.typography.labelLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = config.mode == SelectionMode.AUTO,
                onClick = { viewModel.setMode(SelectionMode.AUTO) },
                label = { Text("Otomatis") }
            )
            FilterChip(
                selected = config.mode == SelectionMode.MANUAL,
                onClick = { viewModel.setMode(SelectionMode.MANUAL) },
                label = { Text("Manual") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pemberitahuan privasi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Pertanyaan hanya dikirim ke provider setelah kamu menekan Kirim. " +
                        "Provider eksternal dapat menerima isi pertanyaanmu. " +
                        "Jangan kirim kata sandi atau data rahasia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ProviderId.entries.forEach { id ->
            ProviderCard(
                id = id,
                config = config,
                status = state.statuses[id] ?: "-",
                onEnabledChange = { enabled -> viewModel.setEnabled(id, enabled) },
                onSetPrimary = { viewModel.setPrimary(id) },
                onToggleBackup = {
                    viewModel.setBackup(if (config.backup == id) null else id)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (state.showGeminiConsent) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissGeminiConsent() },
            title = { Text("Aktifkan Gemini Tambahan?") },
            text = {
                Text(
                    "Endpoint ini memakai metode GET. Isi pertanyaanmu akan dikirim lewat URL " +
                        "dan dapat tercatat di log server atau riwayat browser. " +
                        "Endpoint ini belum diverifikasi oleh NOVA AI. " +
                        "Jangan kirim kata sandi, data pribadi, atau informasi rahasia."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmGeminiConsent() }) {
                    Text("Saya setuju")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissGeminiConsent() }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ProviderCard(
    id: ProviderId,
    config: RoutingConfig,
    status: String,
    onEnabledChange: (Boolean) -> Unit,
    onSetPrimary: () -> Unit,
    onToggleBackup: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = id.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Status: $status",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = id in config.enabled,
                    onCheckedChange = { onEnabledChange(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = config.primary == id,
                    onClick = onSetPrimary,
                    label = { Text("Utama") }
                )
                FilterChip(
                    selected = config.backup == id,
                    onClick = onToggleBackup,
                    label = { Text("Cadangan") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val note = when (id) {
                ProviderId.OFFICIAL_PRIMARY ->
                    "Belum ada backend atau kredensial. Provider ini belum bisa dipakai."
                ProviderId.GEMINI_TAMBAHAN ->
                    "Endpoint GET: isi pertanyaan dapat tercatat di URL atau log server."
            }
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
