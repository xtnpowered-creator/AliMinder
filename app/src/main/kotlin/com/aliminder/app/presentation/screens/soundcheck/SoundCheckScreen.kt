package com.aliminder.app.presentation.screens.soundcheck

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.components.AliMinderTopAppBar
import com.aliminder.app.presentation.screens.settings.SettingsViewModel
import com.aliminder.app.presentation.theme.BorderDark
import com.aliminder.app.presentation.theme.TextSecondary
import com.aliminder.app.presentation.theme.aliMinderTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundCheckScreen(
    viewModel: SoundCheckViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel() // Inject SettingsViewModel
) {
    val engineStatus by viewModel.engineStatus.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val lastAction by viewModel.lastAction.collectAsState()
    val availableVoices by viewModel.availableVoices.collectAsState()
    val selectedVoice by viewModel.selectedVoice.collectAsState()
    
    // Get settings for dynamic top bar color
    val userSettings by settingsViewModel.userSettings.collectAsState()

    var showVoiceDialog by remember { mutableStateOf(false) }

    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = { Text("Select Voice") },
            text = {
                Box(modifier = Modifier.height(300.dp)) {
                    LazyColumn {
                        items(availableVoices) { voice ->
                            ListItem(
                                headlineContent = { Text(voice) },
                                modifier = Modifier.clickable {
                                    viewModel.onVoiceSelected(voice)
                                    showVoiceDialog = false
                                },
                                trailingContent = {
                                    if (voice == selectedVoice) {
                                        Text("✓", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AliMinderTopAppBar(
                title = "Vinyl Lab (Sound Check)",
                overallStage = PersonaStage.OPTIMISTIC, // Static for this screen or bind to something? For now static.
                useDynamicColor = userSettings.useDynamicTitleBarColor
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Monitor
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ENGINE MONITOR", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = engineStatus,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text("Last Action: $lastAction", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            // Voice Selector
            Text("Voice Selection", style = MaterialTheme.typography.titleMedium)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showVoiceDialog = true }, // Make whole card clickable
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Voice", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedVoice, 
                        style = MaterialTheme.typography.bodyLarge, 
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${availableVoices.size} Voices Available", style = MaterialTheme.typography.bodySmall)
                        Text("Tap to change", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Controls
            Text("Persona Triggers", style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.triggerPersona(PersonaStage.OPTIMISTIC) },
                    enabled = !isPlaying
                ) {
                    Text("Optimistic")
                }
                
                Button(
                    onClick = { viewModel.triggerPersona(PersonaStage.WEARY) },
                    enabled = !isPlaying,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Weary")
                }
            }
            
            Button(
                onClick = { viewModel.triggerPersona(PersonaStage.URGENT) }, // Fixed GRAVE -> URGENT
                enabled = !isPlaying,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Urgent (Past PoNR)")
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Safety Systems", style = MaterialTheme.typography.titleMedium)
            
            Button(
                onClick = { viewModel.testPanicMute() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("TEST PANIC MUTE (Flip/Wave)")
            }
        }
    }
}
