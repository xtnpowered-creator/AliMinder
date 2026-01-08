package com.aliminder.app.presentation.screens.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.aliminder.app.domain.model.UserSettings
import com.aliminder.app.presentation.screens.soundcheck.SoundCheckViewModel
import com.aliminder.app.presentation.theme.BorderDark
import com.aliminder.app.presentation.theme.TextSecondary
import com.aliminder.app.presentation.theme.aliMinderTopAppBarColors
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Settings Screen with Tabs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val tabs = listOf("App", "API Test", "Accounts", "Filters", "PoNRs", "Audio", "About", "Power", "Restore")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val userSettings by settingsViewModel.userSettings.collectAsState()

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
                0 -> AppTab(userSettings, settingsViewModel)
                1 -> ApiTestScreen()
                2 -> AccountsTab()
                3 -> FiltersTab()
                4 -> PoNRsTab(userSettings, settingsViewModel)
                5 -> AudioTab()
                6 -> AboutTab()
                7 -> PowerTab()
                8 -> RestoreScreen() // Reuse the composable we created
            }
        }
    }
}

@Composable
fun AppTab(userSettings: UserSettings, viewModel: SettingsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("App Behavior", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))


        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Urgency Time Threshold Setting
        Text("Urgency Time Threshold", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Determines when duties switch from Optimistic (Green) to Weary (Yellow).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        val urgencyOptions = listOf(30, 60, 90)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            urgencyOptions.forEach { minutes ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.updateUrgencyTimeThreshold(minutes) }
                ) {
                    RadioButton(
                        selected = userSettings.urgencyTimeThreshold == minutes,
                        onClick = { viewModel.updateUrgencyTimeThreshold(minutes) }
                    )
                    Text(
                        text = "$minutes min",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        
        // Auto-Hide Overdue Setting
        Text("Auto-Hide Overdue Duties", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Duties overdue by this amount will be automatically hidden.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val autoHideOptions = listOf(30, 60, 120, 180)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            autoHideOptions.forEach { minutes ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.updateAutoHideOverdueMinutes(minutes) }
                ) {
                    RadioButton(
                        selected = userSettings.autoHideOverdueMinutes == minutes,
                        onClick = { viewModel.updateAutoHideOverdueMinutes(minutes) }
                    )
                    Text(
                        text = when(minutes) {
                            60 -> "1 hour"
                            120 -> "2 hours (default)"
                            180 -> "3 hours"
                            else -> "$minutes minutes"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Reset Demo Data
        Button(
            onClick = { viewModel.resetMockData() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Demo Data (Fix Dates)")
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
fun PoNRsTab(userSettings: UserSettings, viewModel: SettingsViewModel = hiltViewModel()) {

    var defaultBuffer by remember { mutableIntStateOf(userSettings.defaultBufferMinutes) }
    
    // Address Entry State
    var showAddressEntry by remember { mutableStateOf(false) }
    var addressEntryType by remember { mutableStateOf<String?>(null) } // "home" or "work"
    
    val currentLocation by viewModel.currentLocation.collectAsState()
    
    if (showAddressEntry && addressEntryType != null) {
        val currentAddress = if (addressEntryType == "home") userSettings.homeAddress else userSettings.workAddress
        
        com.aliminder.app.presentation.components.AddressEntryModal(
            title = if (addressEntryType == "home") "Set Home Address" else "Set Work Address",
            contextText = "Used for geofencing (leaving ${addressEntryType}) and commute calculations.",
            initialAddress = currentAddress,
            suggestedName = if (addressEntryType == "home") "Home" else "Work",
            biasLocation = currentLocation,
            onSave = { address ->
                if (addressEntryType == "home") {
                    viewModel.setHomeAddress(address)
                } else {
                    viewModel.setWorkAddress(address)
                }
                showAddressEntry = false
            },
            onDismiss = { showAddressEntry = false }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Addresses", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Locations for smart geofencing and commute estimates.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Home Address Card
        AddressCard(
            title = "Home Address",
            address = userSettings.homeAddress,
            onEdit = { 
                viewModel.fetchCurrentLocation()
                addressEntryType = "home"
                showAddressEntry = true 
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Work Address Card
        AddressCard(
            title = "Work Address",
            address = userSettings.workAddress,
            onEdit = { 
                viewModel.fetchCurrentLocation()
                addressEntryType = "work"
                showAddressEntry = true 
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Default Parameters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Travel time is calculated automatically via Google Maps (Routes API).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun AddressCard(
    title: String,
    address: com.aliminder.app.domain.model.Address?,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                if (address != null) {
                    Text(
                        text = address.toDisplayString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = "Not set",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
            
            androidx.compose.material3.IconButton(onClick = onEdit) {
                 androidx.compose.material3.Icon(Icons.Default.ChevronRight, contentDescription = "Edit")
            }
        }
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
                                        Text("✓", color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            )
                            HorizontalDivider() // Fixed: Divider -> HorizontalDivider
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
                    color = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp)) // Fixed: Divider -> HorizontalDivider
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
                    Text("Tap to change", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { viewModel.triggerPersona(PersonaStage.OPTIMISTIC) }, enabled = !isPlaying) { Text("Optimistic") }
            Button(onClick = { viewModel.triggerPersona(PersonaStage.WEARY) }, enabled = !isPlaying, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Text("Weary") }
        }
        Button(
            onClick = { viewModel.triggerPersona(PersonaStage.URGENT) }, // Updated to URGENT
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

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
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
    
    LaunchedEffect(Unit) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        screenState = if (powerManager.isInteractive) "On" else "Off"
        
        while (true) {
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val currentNowMicro = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            currentDraw = currentNowMicro / 1000.0
            delay(250) 
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
                        text = "${String.format(Locale.US, "%.3f", currentDraw)} mA", // Fixed: Added Locale.US
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
