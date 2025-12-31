package com.aliminder.app.presentation.screens.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.screens.soundcheck.SoundCheckViewModel
import com.aliminder.app.presentation.theme.BorderDark
import com.aliminder.app.presentation.theme.TextSecondary
import com.aliminder.app.presentation.theme.aliMinderTopAppBarColors
import kotlinx.coroutines.delay

/**
 * Settings Screen with Tabs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val tabs = listOf("Accounts", "Filters", "PoNRs", "Audio", "About", "Power")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                "Settings",
                                fontSize = (MaterialTheme.typography.titleLarge.fontSize.value + 2).sp,
                                textAlign = TextAlign.Center,
                                color = TextSecondary
                            )
                        }
                    },
                    colors = aliMinderTopAppBarColors()
                )
                HorizontalDivider(thickness = 2.dp, color = BorderDark)
                
                // Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    divider = { HorizontalDivider(color = BorderDark) },
                    indicator = { tabPositions ->
                        Box(
                             Modifier
                                 .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                 .height(0.dp) // Make indicator invisible
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = title,
                                    color = if (selectedTabIndex == index) Color.White else TextSecondary
                                ) 
                            }
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = BorderDark)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTabIndex) {
                0 -> AccountsTab()
                1 -> FiltersTab()
                2 -> PoNRsTab()
                3 -> AudioTab()
                4 -> AboutTab()
                5 -> PowerTab()
            }
        }
    }
}

@Composable
fun AccountsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Linked Accounts", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Microsoft 365", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Not yet configured",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = false, onCheckedChange = {}, enabled = false)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Google Workspace", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Not yet configured",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = false, onCheckedChange = {}, enabled = false)
        }
    }
}

@Composable
fun FiltersTab() {
    var showShadow by remember { mutableStateOf(true) }
    var sortByPoNR by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Calendar Sources", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Show Shadow Calendar", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = showShadow, onCheckedChange = { showShadow = it })
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Sorting", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "By PoNR Proximity", style = MaterialTheme.typography.bodyLarge)
            RadioButton(selected = sortByPoNR, onClick = { sortByPoNR = true })
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "By Start Time", style = MaterialTheme.typography.bodyLarge)
            RadioButton(selected = !sortByPoNR, onClick = { sortByPoNR = false })
        }
    }
}

@Composable
fun PoNRsTab() {
    var defaultCommute by remember { mutableIntStateOf(20) }
    var defaultPrep by remember { mutableIntStateOf(15) }
    var defaultBuffer by remember { mutableIntStateOf(10) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Default Parameters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Default Commute", style = MaterialTheme.typography.bodyLarge)
            Text(text = "$defaultCommute min", style = MaterialTheme.typography.titleMedium)
        }
        Slider(
            value = defaultCommute.toFloat(),
            onValueChange = { defaultCommute = it.toInt() },
            valueRange = 0f..60f,
            steps = 11
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Default Grooming", style = MaterialTheme.typography.bodyLarge)
            Text(text = "$defaultPrep min", style = MaterialTheme.typography.titleMedium)
        }
        Slider(
            value = defaultPrep.toFloat(),
            onValueChange = { defaultPrep = it.toInt() },
            valueRange = 0f..30f,
            steps = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Default Buffer", style = MaterialTheme.typography.bodyLarge)
            Text(text = "$defaultBuffer min", style = MaterialTheme.typography.titleMedium)
        }
        Slider(
            value = defaultBuffer.toFloat(),
            onValueChange = { defaultBuffer = it.toInt() },
            valueRange = 0f..30f,
            steps = 5
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTab(
    viewModel: SoundCheckViewModel = hiltViewModel()
) {
    val engineStatus by viewModel.engineStatus.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val lastAction by viewModel.lastAction.collectAsState()
    val availableVoices by viewModel.availableVoices.collectAsState()
    val selectedVoice by viewModel.selectedVoice.collectAsState()
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
                            Divider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceDialog = false }) { Text("Close") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Vinyl Lab (Sound Check)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Last Action: $lastAction", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showVoiceDialog = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current Voice", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = selectedVoice, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${availableVoices.size} Voices Available", style = MaterialTheme.typography.bodySmall)
                    Text("Tap to change", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { viewModel.triggerPersona(PersonaStage.OPTIMISTIC) }, enabled = !isPlaying) { Text("Optimistic") }
            Button(onClick = { viewModel.triggerPersona(PersonaStage.WEARY) }, enabled = !isPlaying, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Text("Weary") }
        }
        Button(
            onClick = { viewModel.triggerPersona(PersonaStage.GRAVE) },
            enabled = !isPlaying,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Grave (Past PoNR)") }
        
        Button(
            onClick = { viewModel.testPanicMute() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
            modifier = Modifier.fillMaxWidth()
        ) { Text("TEST PANIC MUTE") }
    }
}

@Composable
fun AboutTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("About AliMinder", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "AliMinder v1.0.0", style = MaterialTheme.typography.bodyMedium)
        Text(text = "The Vigilance Sentinel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PowerTab() {
    val context = LocalContext.current
    var batteryLevel by remember { mutableIntStateOf(0) }
    var currentDraw by remember { mutableDoubleStateOf(0.0) }
    var screenState by remember { mutableStateOf("On") }
    var chargingState by remember { mutableStateOf("Unplugged") }

    // Receiver for screen and charging state
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> screenState = "On"
                Intent.ACTION_SCREEN_OFF -> screenState = "Off"
                Intent.ACTION_POWER_CONNECTED -> chargingState = "Charging"
                Intent.ACTION_POWER_DISCONNECTED -> chargingState = "Unplugged"
            }
        }
    }

    DisposableEffect(Unit) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(broadcastReceiver, filter)

        onDispose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }
    
    // Polling for battery stats
    LaunchedEffect(Unit) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        screenState = if (powerManager.isInteractive) "On" else "Off"
        
        while (true) {
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val currentNowMicro = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            currentDraw = currentNowMicro / 1000.0
            delay(250) // Refresh 4x/sec
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Power Monitor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Battery Level", style = MaterialTheme.typography.bodyLarge)
                    Text("$batteryLevel%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Current Draw", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${String.format("%.3f", currentDraw)} mA",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Screen State", style = MaterialTheme.typography.bodyLarge)
                    Text(screenState, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Charging State", style = MaterialTheme.typography.bodyLarge)
                    Text(chargingState, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            "Real-time power metrics for debugging. CPU usage and wake-ups require ADB shell access and are not shown here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
