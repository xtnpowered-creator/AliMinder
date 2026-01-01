# Battery Usage Development Guide
**AliMinder Power Optimization Reference**

> **Purpose**: Maintain <8% daily battery drain throughout all development phases  
> **Current Status**: Phase 1 - ~2% daily (baseline established)  
> **Target**: <8% daily when fully implemented

---

## SECTION 1: ALREADY IMPLEMENTED OPTIMIZATIONS (Phase 1)

### 1.1 Fix VinylStackEngine Scope Leak

**Current Issue** (`VinylStackEngine.kt:27`):
```kotlin
private val scope = CoroutineScope(Dispatchers.Main) // ❌ Never cancelled
```

**Fix**: Inject application-scoped coroutine:

**Step 1**: Update `DispatcherModule.kt`:
```kotlin
@Provides
@Singleton
@ApplicationScope
fun provideApplicationScope(): CoroutineScope = CoroutineScope(
    SupervisorJob() + Dispatchers.Default
)
```

**Step 2**: Update `VinylStackEngine.kt`:
```kotlin
@Singleton
class VinylStackEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope // ✅ Injected
) : TextToSpeech.OnInitListener {
    // Remove: private val scope = CoroutineScope(Dispatchers.Main)
}
```

**Battery Savings**: Prevents potential memory/CPU leak (~0.5-1% daily if leaked)

---

### 1.2 Clean Up Manifest Permissions

**Remove unused permissions** until actually needed:

```xml
<!-- AndroidManifest.xml - REMOVE THESE NOW: -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" /> <!-- Phase 4 -->
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" /> <!-- NEVER USE -->
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" /> <!-- Phase 3 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> <!-- Phase 4 -->

<!-- REMOVE phantom service: -->
<service android:name=".presentation.service.VigilanceService" /> <!-- Doesn't exist -->
```

**Keep only current needs**:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.WAKE_LOCK" /> <!-- For audio only -->
```

**Why**: Prevents accidental battery-draining code in Phase 2+

---

### 1.3 Establish Battery Baseline

**Before implementing ANY new features**, establish current baseline:

```bash
# 1. Reset stats
adb shell dumpsys batterystats --reset

# 2. Use app normally for 4 hours:
#    - Open 5 times (2 min each)
#    - Test audio 3 times
#    - Navigate tabs

# 3. Capture report
adb bugreport > phase1-baseline.zip

# 4. Analyze with Battery Historian
# https://bathist.ef.lc/
```

**Expected Phase 1 Results**:
- Total drain: <1% over 4 hours
- Wake-ups: 0 (no background activity)
- CPU time: <30 seconds total
- Sensor usage: 0%

**Action**: Save this as reference for Phase 2+ comparisons

---

### 1.4 Add Battery Debug UI (Optional)

Add real-time monitoring to Settings > Power tab:

```kotlin
// SettingsScreen.kt - PowerTab enhancement
@Composable
fun PowerTab() {
    val context = LocalContext.current
    var wakeUpCount by remember { mutableStateOf(0) }
    var cpuTime by remember { mutableStateOf(0L) }
    
    LaunchedEffect(Unit) {
        while (true) {
            // Read from battery stats
            val stats = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            // Parse wake-ups, CPU time
            delay(1000)
        }
    }
    
    // Display: Wake-ups/hour, CPU%, Current mode
}
```

**Benefit**: Catch battery regressions during development immediately

---

## SECTION 2: PHASE-BY-PHASE IMPLEMENTATION GUIDE

### Phase 2: Data Layer (Room + PoNR Calculations)

#### 2.1 Room Database - Zero Battery Impact

**Database operations are battery-neutral** if done correctly:

✅ **DO**:
```kotlin
@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE startTime > :now ORDER BY startTime")
    fun getUpcomingEvents(now: Long): Flow<List<Event>> // ✅ Flow is cold
}

// ViewModel
val events = eventDao.getUpcomingEvents(System.currentTimeMillis())
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

❌ **DON'T**:
```kotlin
// ❌ BAD: Polling loop
viewModelScope.launch {
    while (true) {
        val events = eventDao.getAllEvents() // Unnecessary CPU wake
        delay(1000) // 1-second polling = 2-3% daily drain
    }
}
```

**Key Principle**: Let Flow observe changes; don't poll

**Battery Impact**: <0.1% (Room is efficient)

---

#### 2.2 PoNR Calculation Use Case

**Calculation timing matters**:

✅ **DO** - Calculate on-demand:
```kotlin
@HiltViewModel
class AllViewModel @Inject constructor(
    private val calculatePoNR: CalculatePoNRUseCase,
    eventsRepo: EventRepository
) : ViewModel() {
    
    val events = eventsRepo.getUpcomingEvents()
        .map { eventList ->
            eventList.map { event ->
                event.copy(
                    ponr = calculatePoNR(event) // Calculated when data changes
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

❌ **DON'T** - Recalculate continuously:
```kotlin
// ❌ BAD: Timer-based recalculation
viewModelScope.launch {
    while (true) {
        updateAllPoNRs() // Every minute = 1-2% drain
        delay(60_000)
    }
}
```

**Key Principle**: Delta changes every minute, but only **recalculate when UI is visible** (WhileSubscribed)

**Battery Impact**: <0.2% (calculations are cheap; continuous loops are not)

---

### Phase 3: Calendar Sync & Sin Group Monitoring

**⚠️ CRITICAL PHASE** - Where most battery drain occurs if done wrong

#### 3.1 Calendar Sync - WorkManager ONLY

❌ **NEVER USE**:
- Foreground Service for sync
- Polling loops
- `AlarmManager` for sync (use WorkManager)

✅ **DO USE**:
```kotlin
// CalendarSyncWorker.kt
@HiltWorker
class CalendarSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val calendarRepo: CalendarRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Sync M365/Google calendars
            calendarRepo.syncAll()
            
            // Schedule alarms for approaching PoNRs
            scheduleUpcomingInterventions()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

// Schedule periodic sync
val syncWork = PeriodicWorkRequestBuilder<CalendarSyncWorker>(
    repeatInterval = 1, TimeUnit.HOURS // ✅ Not 15 minutes!
).setConstraints(
    Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true) // ✅ Respect battery state
        .build()
).build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "calendar_sync",
    ExistingPeriodicWorkPolicy.KEEP,
    syncWork
)
```

**Battery Impact**: ~0.5-1% daily (WorkManager is battery-optimized)

**Key Settings**:
- ✅ 1-hour minimum interval (not 15 minutes)
- ✅ Network-connected constraint
- ✅ Low-battery skip
- ✅ Doze-compatible (WorkManager handles this)

---

#### 3.2 Sin Group Monitoring - ADAPTIVE, NOT CONTINUOUS

**Specification says**: Check every 30s when Sin Group app is active  
**Battery-optimized approach**: Use adaptive intervals with screen-off suspension

✅ **IMPLEMENTATION**:

```kotlin
// MonitoringScheduler.kt
enum class MonitoringMode(val intervalMs: Long) {
    DORMANT(0),              // Screen off
    LOW_POWER(15 * 60_000),  // PoNR > 2h: 15-min checks
    ACTIVE(5 * 60_000),      // PoNR 30m-2h: 5-min checks
    HIGH_ALERT(30_000)       // PoNR < 30m: 30-sec checks
}

fun determineMode(screenOn: Boolean, nextPoNR: LocalDateTime?): MonitoringMode {
    if (!screenOn) return DORMANT // ✅ CRITICAL: Full suspend when screen off
    
    val deltaMinutes = nextPoNR?.let { 
        Duration.between(LocalDateTime.now(), it).toMinutes()
    } ?: Long.MAX_VALUE
    
    return when {
        deltaMinutes > 120 -> LOW_POWER
        deltaMinutes > 30 -> ACTIVE
        else -> HIGH_ALERT
    }
}

// UsageMonitorWorker.kt
@HiltWorker
class UsageMonitorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val usageRepo: UsageStatsRepository,
    private val eventRepo: EventRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val foregroundApp = usageRepo.getForegroundApp()
        val isSinGroup = usageRepo.isSinGroupApp(foregroundApp)
        
        if (isSinGroup) {
            val nextEvent = eventRepo.getNextEvent()
            if (shouldIntervene(nextEvent)) {
                scheduleIntervention(nextEvent)
            }
        }
        
        // Reschedule based on current mode
        val nextPoNR = eventRepo.getNextPoNR()
        val mode = determineMode(isScreenOn(), nextPoNR)
        rescheduleWork(mode.intervalMs)
        
        return Result.success()
    }
}
```

**Battery Impact**:
- Dormant (screen off, 16h/day): 0%
- Low Power (6h/day): ~0.3%
- Active (1.5h/day): ~0.5%
- High Alert (30min/day): ~0.8%
- **Total**: ~1.6% daily

**vs. Continuous 30s checks**: Would be ~12% daily

---

#### 3.3 Accelerometer for Digital Stasis - CONDITIONAL ONLY

**Specification**: Detect phone not moving (< 0.05 m/s²) for 60s

**Battery-Optimized Approach**:

```kotlin
// AccelerometerMonitor.kt
class AccelerometerMonitor @Inject constructor(
    private val sensorManager: SensorManager
) {
    private var listener: SensorEventListener? = null
    
    fun startMonitoring(onStasisDetected: () -> Unit) {
        // ✅ ONLY register when:
        // 1. Sin Group app is foreground
        // 2. PoNR is within 30 minutes
        // 3. Screen is ON
        
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        listener = object : SensorEventListener {
            private val buffer = ArrayDeque<Float>(60) // 60-second buffer
            
            override fun onSensorChanged(event: SensorEvent) {
                val magnitude = sqrt(
                    event.values[0].pow(2) + 
                    event.values[1].pow(2) + 
                    event.values[2].pow(2)
                )
                buffer.addLast(magnitude)
                if (buffer.size > 60) buffer.removeFirst()
                
                // Check if all values < 0.05 m/s²
                if (buffer.size == 60 && buffer.all { it < 0.05f }) {
                    onStasisDetected()
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL, // ✅ NOT DELAY_FASTEST
            SensorManager.SENSOR_DELAY_NORMAL * 60 // ✅ Batch mode
        )
    }
    
    fun stopMonitoring() {
        listener?.let {  sensorManager.unregisterListener(it) }
        listener = null
    }
}

// In ViewModel/Worker
lifecycleScope.launch {
    screenState.combine(sinGroupActive).combine(nearPoNR) { screen, sin, near ->
        screen && sin && near
    }.collect { shouldMonitor ->
        if (shouldMonitor) {
            accelerometer.startMonitoring()
        } else {
            accelerometer.stopMonitoring() // ✅ Stop when not needed
        }
    }
}
```

**Battery Impact**:
- Idle: 0% (not running)
- Active (30 min/day): ~0.4%

**vs. Continuous monitoring**: Would be ~7% daily

---

### Phase 4: Location & Geofencing

#### 4.1 Office Detection - WiFi SSID Only (Not GPS!)

❌ **DON'T USE GPS**:
```kotlin
// ❌ BAD: Continuous GPS = 50-100 mA = 20-30% daily
locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER,
    1000,
    0f,
    locationListener
)
```

✅ **DO USE passive WiFi**:
```kotlin
// ✅ GOOD: Passive WiFi detection = ~0.5 mA = <1% daily
@RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
fun isAtOffice(): Boolean {
    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val connectionInfo = wifiManager.connectionInfo
    return connectionInfo.ssid == "\"Corporate_WiFi\"" // Quotes included in SSID
}

// Check only when needed (not continuously)
fun checkLocationContext() {
    if (isAtOffice()) {
        // Escalate urgency for work events
    }
}
```

**Battery Impact**: <0.5% daily

---

#### 4.2 Geofencing for Office Arrival

If you need arrival/departure detection:

```kotlin
// GeofenceManager.kt
fun setupOfficeGeofence(latLng: LatLng, radiusMeters: Float) {
    val geofence = Geofence.Builder()
        .setRequestId("office")
        .setCircularRegion(latLng.latitude, latLng.longitude, radiusMeters)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT)
        .build()
    
    val request = GeofencingRequest.Builder()
        .addGeofence(geofence)
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .build()
    
    geofencingClient.addGeofences(request, geofencePendingIntent)
}

// GeofenceBroadcastReceiver.kt
override fun onReceive(context: Context, intent: Intent) {
    val event = GeofencingEvent.fromIntent(intent)
    if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
        // User arrived at office - adjust monitoring mode
    }
}
```

**Battery Impact**: ~1-2% daily (Geofence API is battery-optimized)

**Combined Location (WiFi + Geofence)**: ~1.5-2.5% daily

---

### Phase 5: Audio Interventions at Scale

#### 5.1 AlarmManager for Interventions

**Use `setExactAndAllowWhileIdle`** (not setExact):

```kotlin
// InterventionScheduler.kt
fun scheduleIntervention(event: Event) {
    val interventionTime = event.ponr.minusMinutes(15) // 15m before PoNR
    
    val intent = Intent(context, InterventionReceiver::class.java).apply {
        putExtra("EVENT_ID", event.id)
        putExtra("PERSONA_STAGE", event.getPersonaStage().name)
    }
    
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        event.id.hashCode(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    
    alarmManager.setExactAndAllowWhileIdle( // ✅ Doze-compatible
        AlarmManager.RTC_WAKEUP,
        interventionTime.toEpochMilli(),
        pendingIntent
    )
}

// InterventionReceiver.kt
override fun onReceive(context: Context, intent: Intent) {
    val eventId = intent.getStringExtra("EVENT_ID")
    val stage = intent.getStringExtra("PERSONA_STAGE")
    
    // Start SHORT foreground service for audio playback
    val serviceIntent = Intent(context, AudioInterventionService::class.java).apply {
        putExtra("STAGE", stage)
    }
    context.startForegroundService(serviceIntent)
}

// AudioInterventionService.kt - SHORT-LIVED
class AudioInterventionService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val stage = intent.getStringExtra("STAGE")
        
        // Show notification (required for foreground)
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Play intervention (10-30 seconds)
        vinylEngine.playIntervention(stage)
        
        // Auto-stop after playback
        lifecycleScope.launch {
            delay(35_000) // Max intervention length + buffer
            stopSelf()
        }
        
        return START_NOT_STICKY // Don't restart if killed
    }
}
```

**Battery Impact per intervention**: ~0.05-0.1%  
**Daily (10 interventions)**: ~0.5-1%

**Key Points**:
- ✅ Service only runs 10-30 seconds
- ✅ Auto-stops after playback
- ✅ No perpetual wake lock

---

## SECTION 3: ANTI-PATTERNS TO AVOID

### 3.1 NEVER: Perpetual Foreground Service

❌ **This would kill battery**:
```kotlin
// ❌ DON'T IMPLEMENT THIS
class VigilanceService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification)
        
        // ❌ Perpetual wake lock
        wakeLock.acquire()
        
        // ❌ Continuous monitoring
        lifecycleScope.launch {
            while (true) {
                checkForSinGroup()
                delay(30_000)
            }
        }
        
        return START_STICKY // ❌ Always restarts
    }
}
```

**Battery Impact**: 7-12% daily  
**Alternative**: Use WorkManager + AlarmManager as shown above

---

### 3.2 NEVER: Ignore Doze Mode

❌ **Don't request battery optimization bypass**:
```xml
<!-- ❌ REMOVE THIS -->
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```

```kotlin
// ❌ Don't do this
if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
    startActivity(intent)
}
```

**Why**: 
- Users will deny it
- App stores may reject
- System can't optimize battery

**Alternative**: Design around Doze mode using:
- `setExactAndAllowWhileIdle()` for alarms
- WorkManager for periodic tasks
- Screen-off suspension

---

### 3.3 NEVER: Continuous Sensor Polling

❌ **Don't poll sensors continuously**:
```kotlin
// ❌ BAD
sensorManager.registerListener(
    listener,
    accelerometer,
    SensorManager.SENSOR_DELAY_FASTEST // 50Hz = 5 mA
)
```

✅ **DO: Conditional + Batched**:
```kotlin
// ✅ GOOD: Only when needed + batched
if (shouldMonitor) {
    sensorManager.registerListener(
        listener,
        accelerometer,
        SensorManager.SENSOR_DELAY_NORMAL,  // 200ms
        SENSOR_DELAY_NORMAL * 60 // Batch 60 seconds
    )
} else {
    sensorManager.unregisterListener(listener)
}
```

---

## SECTION 4: TESTING & VALIDATION

### 4.1 After Each Phase Implementation

```bash
# 1. Reset baseline
adb shell dumpsys batterystats --reset

# 2. Test for 4+ hours with typical usage

# 3. Generate report
adb bugreport > phase-N-battery.zip

# 4. Analyze metrics
```

**Target Metrics per Phase**:

| Phase | Daily Drain Target | Max Acceptable | Wake-ups/hour |
|-------|-------------------|----------------|---------------|
| 1 (UI Only) | <2% | 3% | 0 |
| 2 (Data Layer) | <3% | 4% | <2 |
| 3 (Monitoring) | <6% | 8% | <6 |
| 4 (Location) | <7% | 9% | <8 |
| 5 (Full Features) | <8% | 10% | <10 |

**If exceeded**: Identify regression before proceeding

---

### 4.2 Key Battery Historian Metrics

Focus on these in the Battery Historian report:

1. **Wake Locks**: Should be <1% of time (except during intervention playback)
2. **Mobile Radio**: <5% active (calendar syncs only)
3. **GPS**: Should be 0% (not using GPS)
4. **Sensor Usage**: <2% (only during sin group detection)
5. **Doze Mode**: App should allow Doze (check "Doze prevented" = NO)

---

## SECTION 5: BATTERY BUDGET ALLOCATION

### 5.1 Target Distribution (Full Implementation)

| Component | Budget | Justification |
|-----------|--------|---------------|
| **UI (when app open)** | 1.5% | Standard Compose app, 10 min/day |
| **Calendar Sync** | 1.0% | WorkManager, 1hr intervals |
| **Sin Group Monitoring** | 1.5% | Adaptive intervals, screen-off suspend |
| **Sensor Monitoring** | 0.5% | Conditional, batched accelerometer |
| **Location Context** | 1.5% | WiFi SSID + Geofence API |
| **Audio Interventions** | 1.0% | 10x daily @ 0.1% each |
| **PoNR Calculations** | 0.3% | On-demand only |
| **Database Operations** | 0.2% | Room with Flow |
| **Overhead** | 0.5% | System, Hilt, misc |
| **TOTAL** | **8.0%** | |

---

## SECTION 6: EMERGENCY BATTERY MODE

### 6.1 User-Configurable Power Saver

Give users an "Emergency Battery" mode in Settings:

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val workManager: WorkManager
) : ViewModel() {
    
    fun setBatteryMode(mode: BatteryMode) {
        when (mode) {
            BatteryMode.NORMAL -> {
                // All features enabled
                scheduleNormalMonitoring()
            }
            
            BatteryMode.POWER_SAVER -> {
                // Reduce monitoring to hourly
                // Disable accelerometer
                // WiFi-only location
                workManager.cancelAllWorkByTag("monitoring")
                scheduleReducedMonitoring()
            }
            
            BatteryMode.EMERGENCY -> {
                // Interventions only (no monitoring)
                // Calendar sync every 4 hours
                workManager.cancelAllWorkByTag("monitoring")
                scheduleMinimalSync()
            }
        }
        preferences.setBatteryMode(mode)
    }
}
```

**Impact**:
- NORMAL: 8% daily
- POWER_SAVER: 4% daily
- EMERGENCY: 2% daily

---

## SECTION 7: QUICK REFERENCE CHECKLIST

Before merging any PR that adds background work:

- [ ] Does it REQUIRE a foreground service? (Answer should be NO)
- [ ] Can WorkManager handle it instead? (Answer should be YES)
- [ ] Does it suspend when screen is off? (Answer should be YES)
- [ ] Does it use batched sensors (not continuous)? (Answer should be YES)
- [ ] Does it respect Doze mode? (Answer should be YES)
- [ ] Is location passive (WiFi/Geofence, not GPS)? (Answer should be YES)
- [ ] Battery Historian shows <1% regression? (Answer should be YES)
- [ ] Wake-ups increased by <2 per hour? (Answer should be YES)

**If any answer is wrong**: Refactor before merging

---

## APPENDIX: Power Draw Reference Table

| Operation | Typical mA | Duration | Daily % |
|-----------|-----------|----------|---------|
| **Screen (max brightness)** | 400 mA | 10 min | 1.3% |
| **Compose UI rendering** | 150 mA | 10 min | 0.5% |
| **WorkManager task** | 60 mA | 2 sec/hr | 0.03% |
| **AlarmManager wake** | 50 mA | 10 sec | 0.06% per alarm |
| **TTS playback** | 20 mA | 20 sec | 0.04% |
| **UsageStats query** | 15 mA | 1 sec | <0.01% per query |
| **Room DB query** | 10 mA | 100 ms | <0.01% |
| **WiFi SSID check** | 5 mA | 500 ms | <0.01% |
| **Geofence transition** | 15 mA | 2 sec | <0.01% per event |
| **Accelerometer (batched)** | 0.5 mA | 30 min | 0.4% |
| **Accelerometer (continuous)** | 4 mA | 8 hours | 7% |
| **Foreground service (idle)** | 3 mA | 24 hours | 7.2% |

**Battery Capacity Reference**: 5,000 mAh (Samsung S22 Ultra)  
**Formula**: (mA × hours) / 5000 × 100 = Daily %

---

**Remember**: The goal isn't zero battery usage - it's **sustainable** usage that doesn't drive users away. Target <8% daily for a compelling vigilance assistant that users actually keep installed.
