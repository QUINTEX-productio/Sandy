package com.example.posturedection

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.posturedection.ui.theme.PostureDectionTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class MobileFlowState {
    SPLASH,
    AUTH_SIGNIN,
    AUTH_SIGNUP,
    SURVEY,
    WORKSPACE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PostureDectionTheme(darkTheme = true, dynamicColor = false) {
                SandyMobileApp()
            }
        }
    }
}

@Composable
fun SandyMobileApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var settings by remember { mutableStateOf(SandyLocalStore.loadSettings(context)) }
    var profile by remember { mutableStateOf(SandyLocalStore.loadProfile(context)) }
    var devices by remember { mutableStateOf(SandyLocalStore.loadDevices(context)) }
    var analyticsMap by remember { mutableStateOf(SandyLocalStore.loadDailyAnalytics(context)) }
    var mediaList by remember { mutableStateOf(SandyLocalStore.loadRecordedMedia(context)) }

    var flowState by remember {
        mutableStateOf(if (profile.signedIn) MobileFlowState.WORKSPACE else MobileFlowState.SPLASH)
    }
    var pendingSurveyProfile by remember { mutableStateOf(profile) }
    var showTutorial by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var pfpMenuOpen by remember { mutableStateOf(false) }

    // Backend live state (kept off the minimal Live Interface front)
    var yoloReady by remember { mutableStateOf(false) }
    var qwenReady by remember { mutableStateOf(false) }
    var bleStatusText by remember { mutableStateOf("") }
    var isMonitoring by remember { mutableStateOf(false) }
    var liveBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var liveReading by remember { mutableStateOf("WAITING") }
    var selectedDeviceId by remember {
        mutableStateOf(devices.firstOrNull { it.status == DeviceConnState.CONNECTED }?.id ?: "")
    }
    var slouchStartMs by remember { mutableLongStateOf(0L) }
    var lastAlertMs by remember { mutableLongStateOf(0L) }
    var lastSnapMs by remember { mutableLongStateOf(0L) }

    val accent = settings.theme.color

    // Record daily YOLO telemetry & auto-snapshot silently in backend
    fun recordYoloObservation(isBad: Boolean, bmp: Bitmap?) {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val now = System.currentTimeMillis()
        var slouchTriggered = false

        if (isBad) {
            if (slouchStartMs == 0L) slouchStartMs = now
            val elapsedSec = (now - slouchStartMs) / 1000L
            if (elapsedSec >= settings.slouchAlertSeconds && now - lastAlertMs > 12_000L) {
                slouchTriggered = true
                lastAlertMs = now
                if (settings.chimeAndVibrate) triggerMobileSlouchAlert(context)
            }
        } else {
            slouchStartMs = 0L
        }

        val cur = analyticsMap[todayKey] ?: DailyAnalyticsRecord(todayKey, 0, 0, 0, 0, 92)
        val g = cur.goodFrames + if (isBad) 0 else 1
        val b = cur.badFrames + if (isBad) 1 else 0
        val s = cur.slouchEvents + if (slouchTriggered) 1 else 0
        val total = (g + b).coerceAtLeast(1)
        val updatedRec = cur.copy(
            goodFrames = g,
            badFrames = b,
            slouchEvents = s,
            backScore = (g * 100) / total
        )
        val nextMap = analyticsMap.toMutableMap().apply { put(todayKey, updatedRec) }
        analyticsMap = nextMap
        SandyLocalStore.saveDailyAnalytics(context, nextMap)

        if (settings.autoRecordMedia && bmp != null && (slouchTriggered || now - lastSnapMs > 20_000L)) {
            lastSnapMs = now
            val activeName = devices.firstOrNull { it.id == selectedDeviceId }?.name ?: "Sandy Sensor"
            SandyLocalStore.saveRecordedSnapshot(
                context = context,
                bitmap = bmp,
                posture = if (isBad) "BAD" else "GOOD",
                deviceName = activeName
            )?.let { entry ->
                mediaList = listOf(entry) + mediaList
            }
        }
    }

    // Real Android BLE Client for ESP32-S3 hardware
    val bleClient = remember {
        PostureBleClient(
            context = context,
            onStatusChange = { status ->
                bleStatusText = status
                if (status.contains("Connected", ignoreCase = true)) {
                    val existing = devices.firstOrNull { it.protocol.contains("BLE", true) }
                    val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
                    devices = if (existing != null) {
                        devices.map { if (it.id == existing.id) it.copy(status = DeviceConnState.CONNECTED, lastConnected = nowStr) else it }
                    } else {
                        val added = PostureDeviceItem(
                            id = "ble_${System.currentTimeMillis()}",
                            name = "PostureAI ESP32-S3",
                            model = "ESP32-S3-WROOM-1-N16R8",
                            dateMade = "2026-10",
                            firmware = "v2.4.0-sandy",
                            protocol = "Bluetooth LE 5.0",
                            hardwareId = "BLE-ESP32S3",
                            status = DeviceConnState.CONNECTED,
                            lastConnected = nowStr
                        )
                        selectedDeviceId = added.id
                        listOf(added) + devices
                    }
                    SandyLocalStore.saveDevices(context, devices)
                }
            },
            onFrameReceived = { bmp ->
                liveBitmap = bmp
            },
            onDetectionReceived = { box ->
                val isBad = box.label.contains("bad", ignoreCase = true)
                liveReading = if (isBad) "BAD" else "GOOD"
                recordYoloObservation(isBad, liveBitmap)
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose { bleClient.disconnect() }
    }

    val blePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) {
            bleClient.startScanAndConnect()
        } else {
            Toast.makeText(context, "Bluetooth permission needed to scan ESP32.", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestBleScan() {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        blePermissionLauncher.launch(perms)
    }

    // Silent background YOLOv5 polling loop while Live Interface is running
    LaunchedEffect(isMonitoring, selectedDeviceId) {
        if (!isMonitoring) {
            liveReading = "WAITING"
            slouchStartMs = 0L
            return@LaunchedEffect
        }
        if (settings.autoWakeColab) {
            scope.launch {
                PostureNetworkClient.wakeupBackend(BackendConfig.COLAB_API_URL, "yolo")
                yoloReady = true
            }
        }
        while (isMonitoring) {
            val res = PostureNetworkClient.fetchLatestPostureFrame()
            if (res.bitmap != null) {
                liveBitmap = res.bitmap
            }
            val top = res.detections.firstOrNull()
            if (top != null) {
                val isBad = top.label.contains("bad", ignoreCase = true)
                liveReading = if (isBad) "BAD" else "GOOD"
                yoloReady = true
                recordYoloObservation(isBad, res.bitmap ?: liveBitmap)
            }
            delay(1300L)
        }
    }

    // Auto-wake Colab on entering workspace
    LaunchedEffect(flowState) {
        if (flowState == MobileFlowState.WORKSPACE && settings.autoWakeColab) {
            launch {
                PostureNetworkClient.wakeupBackend(BackendConfig.COLAB_API_URL, "yolo")
                yoloReady = true
            }
            launch {
                PostureNetworkClient.wakeupBackend(BackendConfig.QWEN_API_URL, "qwen")
                qwenReady = true
            }
        }
    }

    // Render current flow screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBg)
            .statusBarsPadding()
    ) {
        when (flowState) {
            MobileFlowState.SPLASH -> SandySplashScreen(
                accent = accent,
                onSignInClick = { flowState = MobileFlowState.AUTH_SIGNIN },
                onSignUpClick = { flowState = MobileFlowState.AUTH_SIGNUP }
            )

            MobileFlowState.AUTH_SIGNIN, MobileFlowState.AUTH_SIGNUP -> SandyAuthScreen(
                accent = accent,
                isSignUpInitial = flowState == MobileFlowState.AUTH_SIGNUP,
                onBack = { flowState = MobileFlowState.SPLASH },
                onSignInSuccess = { signedInProfile ->
                    profile = signedInProfile
                    SandyLocalStore.saveProfile(context, signedInProfile)
                    flowState = MobileFlowState.WORKSPACE
                },
                onSignUpContinueToSurvey = { newProfile ->
                    pendingSurveyProfile = newProfile
                    flowState = MobileFlowState.SURVEY
                }
            )

            MobileFlowState.SURVEY -> SandyOnboardingSurveyScreen(
                accent = accent,
                initialProfile = pendingSurveyProfile,
                onComplete = { completedProfile ->
                    profile = completedProfile
                    SandyLocalStore.saveProfile(context, completedProfile)
                    flowState = MobileFlowState.WORKSPACE
                    showTutorial = true // Show interactive tutorial right after Sign Up + Survey!
                }
            )

            MobileFlowState.WORKSPACE -> {
                val sections = SandySection.entries
                val pagerState = rememberPagerState(initialPage = 0, pageCount = { sections.size })
                val activeIndex = pagerState.currentPage

                fun navigateTo(section: SandySection) {
                    val idx = sections.indexOf(section)
                    if (idx >= 0) {
                        scope.launch { pagerState.animateScrollToPage(idx) }
                    }
                    if (settings.autoWakeColab) {
                        if (section == SandySection.INTERFACE) {
                            scope.launch { PostureNetworkClient.wakeupBackend(BackendConfig.COLAB_API_URL, "yolo") }
                        } else if (section == SandySection.CHATBOT || section == SandySection.REPORT) {
                            scope.launch { PostureNetworkClient.wakeupBackend(BackendConfig.QWEN_API_URL, "qwen") }
                        }
                    }
                }

                // Animated circle Y-offset matching the chosen left sidebar icon!
                val itemSlotHeight = 56.dp
                val circleOffsetY by animateDpAsState(
                    targetValue = itemSlotHeight * activeIndex,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.78f),
                    label = "railCircle"
                )

                val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val todayScore = analyticsMap[todayKey]?.takeIf { it.goodFrames + it.badFrames > 0 }?.backScore

                Row(modifier = Modifier.fillMaxSize()) {
                    // COMPACT LEFT VERTICAL SIDEBAR RAIL WITH ANIMATED GLOWING CIRCLE
                    Column(
                        modifier = Modifier
                            .width(58.dp)
                            .fillMaxHeight()
                            .background(Color(0xE6070B18))
                            .border(BorderStroke(1.dp, Color(0x14FFFFFF)))
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mini Sandy Logo at top of rail -> returns to Home Menu
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { navigateTo(SandySection.MENU) },
                            contentAlignment = Alignment.Center
                        ) {
                            SandyLogoBadge(accent = accent, size = 36.dp)
                        }

                        Spacer(Modifier.height(16.dp))

                        // Icon stack with smoothly gliding glowing circle behind the active icon
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(itemSlotHeight * sections.size),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Animated glowing circle indicator
                            Box(
                                modifier = Modifier
                                    .offset(y = circleOffsetY + 5.dp)
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(accent.copy(alpha = 0.30f), accent.copy(alpha = 0.08f))
                                        )
                                    )
                                    .border(1.8.dp, accent, CircleShape)
                            )

                            // Vertical icon buttons
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                sections.forEachIndexed { idx, sec ->
                                    val selected = idx == activeIndex
                                    Column(
                                        modifier = Modifier
                                            .size(width = 50.dp, height = itemSlotHeight)
                                            .clickable { navigateTo(sec) },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = sec.iconGlyph,
                                            color = if (selected) accent else TextSoft,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = sec.label,
                                            color = if (selected) TextHi else TextDim,
                                            fontSize = 8.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.weight(1f))
                        Text("ALPHA", color = TextDim, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }

                    // MAIN CONTENT AREA: SMOOTH VERTICAL SCROLL PAGER SYNCED WITH SIDEBAR CIRCLE
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        VerticalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (sections[page]) {
                                SandySection.MENU -> SandyMenuSection(
                                    accent = accent,
                                    profile = profile,
                                    devices = devices,
                                    yoloReady = yoloReady,
                                    qwenReady = qwenReady,
                                    onNavigate = { navigateTo(it) },
                                    onReplayTutorial = { showTutorial = true }
                                )

                                SandySection.DEVICES -> SandyDevicesSection(
                                    accent = accent,
                                    devices = devices,
                                    bleStatusText = bleStatusText,
                                    onScanBle = { requestBleScan() },
                                    onAddManualDevice = { name, model, dateMade, streamUrl ->
                                        val devId = "dev_${System.currentTimeMillis()}"
                                        val pairingItem = PostureDeviceItem(
                                            id = devId,
                                            name = name,
                                            model = model.ifBlank { "ESP32-S3-WROOM-1-N16R8" },
                                            dateMade = dateMade.ifBlank { "2026-10" },
                                            streamUrl = streamUrl,
                                            status = DeviceConnState.PAIRING,
                                            lastConnected = "Pairing..."
                                        )
                                        devices = listOf(pairingItem) + devices
                                        SandyLocalStore.saveDevices(context, devices)
                                        scope.launch {
                                            delay(1200L)
                                            val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
                                            devices = devices.map {
                                                if (it.id == devId) it.copy(status = DeviceConnState.CONNECTED, lastConnected = nowStr) else it
                                            }
                                            selectedDeviceId = devId
                                            SandyLocalStore.saveDevices(context, devices)
                                        }
                                    },
                                    onToggleConnect = { id ->
                                        val target = devices.firstOrNull { it.id == id } ?: return@SandyDevicesSection
                                        if (target.status == DeviceConnState.CONNECTED) {
                                            if (selectedDeviceId == id && isMonitoring) isMonitoring = false
                                            devices = devices.map {
                                                if (it.id == id) it.copy(status = DeviceConnState.DISCONNECTED) else it
                                            }
                                            SandyLocalStore.saveDevices(context, devices)
                                        } else {
                                            devices = devices.map {
                                                if (it.id == id) it.copy(status = DeviceConnState.PAIRING) else it
                                            }
                                            scope.launch {
                                                delay(1000L)
                                                val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
                                                devices = devices.map {
                                                    if (it.id == id) it.copy(status = DeviceConnState.CONNECTED, lastConnected = nowStr) else it
                                                }
                                                selectedDeviceId = id
                                                SandyLocalStore.saveDevices(context, devices)
                                            }
                                        }
                                    },
                                    onRenameDevice = { id, newName ->
                                        devices = devices.map { if (it.id == id) it.copy(name = newName) else it }
                                        SandyLocalStore.saveDevices(context, devices)
                                    },
                                    onDeleteDevice = { id ->
                                        if (selectedDeviceId == id && isMonitoring) isMonitoring = false
                                        devices = devices.filterNot { it.id == id }
                                        if (selectedDeviceId == id) {
                                            selectedDeviceId = devices.firstOrNull { it.status == DeviceConnState.CONNECTED }?.id ?: ""
                                        }
                                        SandyLocalStore.saveDevices(context, devices)
                                    }
                                )

                                SandySection.INTERFACE -> {
                                    val connected = devices.filter { it.status == DeviceConnState.CONNECTED }
                                    SandyLiveInterfaceSection(
                                        accent = accent,
                                        isMonitoring = isMonitoring,
                                        liveBitmap = liveBitmap,
                                        liveReading = liveReading,
                                        connectedDevices = connected,
                                        selectedDeviceId = selectedDeviceId,
                                        onSelectDevice = { selectedDeviceId = it },
                                        onToggleMonitor = {
                                            if (isMonitoring) {
                                                isMonitoring = false
                                            } else {
                                                if (connected.isEmpty()) {
                                                    Toast.makeText(context, "Connect a Bluetooth device in Devices first.", Toast.LENGTH_SHORT).show()
                                                    navigateTo(SandySection.DEVICES)
                                                } else {
                                                    if (selectedDeviceId.isBlank()) selectedDeviceId = connected.first().id
                                                    isMonitoring = true
                                                }
                                            }
                                        }
                                    )
                                }

                                SandySection.CHATBOT -> SandyChatbotSection(
                                    accent = accent,
                                    profile = profile,
                                    todayScore = todayScore,
                                    onWakeQwen = {
                                        if (settings.autoWakeColab) {
                                            scope.launch {
                                                PostureNetworkClient.wakeupBackend(BackendConfig.QWEN_API_URL, "qwen")
                                                qwenReady = true
                                            }
                                        }
                                    }
                                )

                                SandySection.REPORT -> SandyReportSection(
                                    accent = accent,
                                    profile = profile,
                                    analyticsMap = analyticsMap,
                                    mediaList = mediaList,
                                    onSyncAnalytics = {
                                        scope.launch {
                                            val remote = PostureNetworkClient.fetchYoloAnalyticsDays()
                                            if (remote.isNotEmpty()) {
                                                val merged = analyticsMap.toMutableMap()
                                                remote.forEach { r -> if (r.date.isNotBlank()) merged[r.date] = r }
                                                analyticsMap = merged
                                                SandyLocalStore.saveDailyAnalytics(context, merged)
                                                Toast.makeText(context, "Synced ${remote.size} YOLO days", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Local report up to date", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onDeleteMedia = { id ->
                                        mediaList = SandyLocalStore.deleteRecordedMedia(context, id)
                                    }
                                )

                                SandySection.SETTINGS -> SandySettingsSection(
                                    settings = settings,
                                    onUpdateSettings = { updated ->
                                        settings = updated
                                        SandyLocalStore.saveSettings(context, updated)
                                    },
                                    onReplayTutorial = { showTutorial = true }
                                )
                            }
                        }

                        // TOP-RIGHT CIRCULAR USER PROFILE BAR (EXACT WEBSITE STYLE: ONLY USER PROFILE & SIGN OUT)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(accent.copy(alpha = 0.28f), NeonMagenta.copy(alpha = 0.26f))
                                        )
                                    )
                                    .border(1.8.dp, accent, CircleShape)
                                    .clickable { pfpMenuOpen = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(profile.initials, color = TextHi, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            DropdownMenu(
                                expanded = pfpMenuOpen,
                                onDismissRequest = { pfpMenuOpen = false },
                                modifier = Modifier.background(Color(0xFF0A0F1E))
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                    Text(
                                        profile.displayName.ifBlank { "Sandy User" },
                                        color = TextHi,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "@${profile.username.ifBlank { "sandy_user" }}",
                                        color = accent,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                HorizontalDivider(color = Color(0x1FFFFFFF))
                                DropdownMenuItem(
                                    text = { Text("User Profile", color = TextHi, fontSize = 13.sp) },
                                    onClick = {
                                        pfpMenuOpen = false
                                        showProfileDialog = true
                                    }
                                )
                                HorizontalDivider(color = Color(0x1FFFFFFF))
                                DropdownMenuItem(
                                    text = { Text("Sign Out", color = NeonRed, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        pfpMenuOpen = false
                                        isMonitoring = false
                                        bleClient.disconnect()
                                        profile = profile.copy(signedIn = false, idToken = "")
                                        SandyLocalStore.saveProfile(context, profile)
                                        flowState = MobileFlowState.SPLASH
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showTutorial) {
            SandyTutorialDialog(accent = accent, onDismiss = { showTutorial = false })
        }

        if (showProfileDialog) {
            SandyProfileDialog(
                accent = accent,
                profile = profile,
                onDismiss = { showProfileDialog = false },
                onSave = { updated ->
                    profile = updated
                    SandyLocalStore.saveProfile(context, updated)
                    Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

private fun triggerMobileSlouchAlert(context: Context) {
    runCatching {
        val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 260)
    }
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(240, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib?.vibrate(VibrationEffect.createOneShot(240, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib?.vibrate(240)
            }
        }
    }
}
