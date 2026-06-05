package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import kotlinx.coroutines.delay
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AdherenceLogEntity
import com.example.data.MedicationEntity
import com.example.data.ProfileEntity
import com.example.ui.theme.Localization
import com.example.viewmodel.MedicationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Sleek, modern, and high-contrast color palette fully aligned with the Minimal Dark Developer design system
var currentIsLightMode = false

val ElderlyBgColor: Color
    get() = if (currentIsLightMode) Color(0xFFF8FAFC) else Color(0xFF0B1326) // Surface (#0b1326) / Soft Slate

val ElderlyCardBg: Color
    get() = if (currentIsLightMode) Color(0xFFFFFFFF) else Color(0xFF151D30) // Surface card background contrast, zero elevation

val HighContrastWhite: Color
    get() = if (currentIsLightMode) Color(0xFF0F172A) else Color(0xFFDAE2FD) // On-surface (#dae2fd) / Coal Text on Light

val ElderlyAccentYellow: Color
    get() = if (currentIsLightMode) Color(0xFFD97706) else Color(0xFFF59E0B) // Supporting Amber Indicator

val ElderlyAccentGreen: Color
    get() = if (currentIsLightMode) Color(0xFF16A34A) else Color(0xFF10B981) // High-visibility Action Green

val ElderlyAccentRed: Color
    get() = if (currentIsLightMode) Color(0xFFDC2626) else Color(0xFFFFB4AB) // Error (#ffb4ab) / Hard Warning Red

val ElderlyAccentBlue: Color
    get() = if (currentIsLightMode) Color(0xFF2665FD) else Color(0xFF2665FD) // Primary CTA brand blue (#2665fd)

val ElderlyAccentTeal: Color
    get() = if (currentIsLightMode) Color(0xFF0F766E) else Color(0xFF475569) // Supporting Supporting UI Secondary Slate (#475569)

// Minimal Dark Developer design system geometry
val AppCornerRadius = 8.dp
val AppCardShape = RoundedCornerShape(8.dp)
val AppButtonShape = RoundedCornerShape(8.dp)
val AppInputShape = RoundedCornerShape(8.dp)

val AppBorderStroke: BorderStroke
    get() = BorderStroke(1.dp, if (currentIsLightMode) Color(0xFFE2E8F0) else Color(0xFF475569).copy(alpha = 0.5f))


// Extension modifier to draw custom L-shape viewfinder camera brackets at four corners
fun Modifier.drawViewfinderCorners(
    color: Color,
    strokeWidth: Float = 4f,
    length: Float = 20f
) = this.drawBehind {
    val w = size.width
    val h = size.height
    
    // Top-left corner
    drawLine(color, Offset(0f, 0f), Offset(length, 0f), strokeWidth = strokeWidth)
    drawLine(color, Offset(0f, 0f), Offset(0f, length), strokeWidth = strokeWidth)
    
    // Top-right corner
    drawLine(color, Offset(w, 0f), Offset(w - length, 0f), strokeWidth = strokeWidth)
    drawLine(color, Offset(w, 0f), Offset(w, length), strokeWidth = strokeWidth)
    
    // Bottom-left corner
    drawLine(color, Offset(0f, h), Offset(length, h), strokeWidth = strokeWidth)
    drawLine(color, Offset(0f, h), Offset(0f, h - length), strokeWidth = strokeWidth)
    
    // Bottom-right corner
    drawLine(color, Offset(w, h), Offset(w - length, h), strokeWidth = strokeWidth)
    drawLine(color, Offset(w, h), Offset(w, h - length), strokeWidth = strokeWidth)
}

// Extension modifier to draw custom camera viewfinder crosshairs around central target nodes
fun Modifier.drawViewfinderCrosshair(color: Color) = this.drawBehind {
    val w = size.width
    val h = size.height
    // Left tick
    drawLine(color.copy(0.4f), Offset(w * 0.12f, h / 2f), Offset(w * 0.28f, h / 2f), strokeWidth = 2.5f)
    // Right tick
    drawLine(color.copy(0.4f), Offset(w * 0.72f, h / 2f), Offset(w * 0.88f, h / 2f), strokeWidth = 2.5f)
    // Top tick
    drawLine(color.copy(0.4f), Offset(w / 2f, h * 0.12f), Offset(w / 2f, h * 0.28f), strokeWidth = 2.5f)
    // Bottom tick
    drawLine(color.copy(0.4f), Offset(w / 2f, h * 0.72f), Offset(w / 2f, h * 0.88f), strokeWidth = 2.5f)
}

@Composable
fun MedicationAppMainScreen(viewModel: MedicationViewModel) {
    val currentViewState by viewModel.currentView.collectAsStateWithLifecycle()
    val activeAlarmState by viewModel.activeAlarm.collectAsStateWithLifecycle()
    val profileState by viewModel.profile.collectAsStateWithLifecycle()
    val profileNonNull = profileState ?: ProfileEntity()

    // Dynamically synchronize the global theme helper variable with the saved settings flow
    currentIsLightMode = profileNonNull.isLightMode

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ElderlyBgColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Render either the Patient Screen or the Caregiver Dashboard
            Crossfade(targetState = currentViewState, label = "ViewCrossfade") { state ->
                when (state) {
                    "PATIENT" -> PatientView(viewModel = viewModel, profile = profileNonNull)
                    "CAREGIVER" -> CaregiverDashboard(viewModel = viewModel, profile = profileNonNull)
                }
            }

            // Always overlap the Full Screen Alert Panel if an alarm triggers
            AnimatedVisibility(
                visible = activeAlarmState != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                activeAlarmState?.let { alarm ->
                    ActiveAlarmFullScreen(
                        viewModel = viewModel,
                        medication = alarm,
                        lang = profileNonNull.language
                    )
                }
            }
        }
    }
}

// Beautiful futuristic telemetry bar inspired by Dribbble GPS Map Camera Apps
@Composable
fun SatelliteTelemetryHud(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .drawViewfinderCorners(ElderlyAccentBlue.copy(alpha = 0.8f), strokeWidth = 3f, length = 30f)
            .border(AppBorderStroke, AppCardShape),
        colors = CardDefaults.cardColors(containerColor = Color(0x990A111E)),
        shape = AppCardShape
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ElderlyAccentGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GPS LOCK: COLD/STABLE (9 SATS)",
                        color = ElderlyAccentGreen,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "ACCURACY: 1.8m",
                    color = ElderlyAccentBlue,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAT: 23.8103° N  |  LON: 90.4125° E",
                    color = HighContrastWhite.copy(0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ALT: 24m",
                    color = ElderlyAccentYellow,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// 1. ELDERLY PATIENT SCREEN
// ==========================================
@Composable
fun PatientView(viewModel: MedicationViewModel, profile: ProfileEntity) {
    val meds by viewModel.medications.collectAsStateWithLifecycle()
    val logs by viewModel.adherenceLogs.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lang = profile.language

    var isHoldingSos by remember { mutableStateOf(false) }
    var sosProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isHoldingSos) {
        if (isHoldingSos) {
            val totalTimeMs = 3000L
            val intervalMs = 25L
            var elapsed = 0L
            while (elapsed < totalTimeMs && isHoldingSos) {
                delay(intervalMs)
                elapsed += intervalMs
                sosProgress = (elapsed.toFloat() / totalTimeMs).coerceAtMost(1f)
            }
            if (elapsed >= totalTimeMs && isHoldingSos) {
                val phoneUri = Uri.parse("tel:${profile.emergencyPhone}")
                val dialIntent = Intent(Intent.ACTION_DIAL, phoneUri)
                try {
                    context.startActivity(dialIntent)
                } catch (e: Exception) {
                    android.widget.Toast
                        .makeText(
                            context,
                            "Calling Caregiver SOS: ${profile.emergencyPhone}",
                            android.widget.Toast.LENGTH_LONG
                        )
                        .show()
                }
                isHoldingSos = false
                sosProgress = 0f
            }
        } else {
            sosProgress = 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Top Accessible Localized Header Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .drawViewfinderCorners(ElderlyAccentBlue.copy(alpha = 0.7f), strokeWidth = 3f, length = 38f)
                .border(AppBorderStroke, AppCardShape),
            colors = CardDefaults.cardColors(containerColor = ElderlyCardBg.copy(alpha = 0.55f)),
            shape = AppCardShape
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Localization.getString("app_title", lang),
                        color = ElderlyAccentYellow,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${profile.name} (${profile.age} ${Localization.getString("years", lang)})",
                        color = HighContrastWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Switch role button - beautiful modern pill button instead of heavy box
                Button(
                    onClick = { viewModel.toggleView() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElderlyAccentBlue,
                        contentColor = HighContrastWhite
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    shape = AppButtonShape,
                    modifier = Modifier.height(48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = HighContrastWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.getString("caregiver_view", lang).take(12),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Language & Theme Selector Panel - Sleek modern capsule container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .drawViewfinderCorners(ElderlyAccentBlue.copy(alpha = 0.5f), strokeWidth = 3f, length = 32f)
                .border(AppBorderStroke, AppCardShape),
            colors = CardDefaults.cardColors(containerColor = ElderlyCardBg.copy(alpha = 0.35f)),
            shape = AppCardShape
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Localization.getString("language_select", lang),
                        color = HighContrastWhite.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (profile.isLightMode) "HIGH CONTRAST LIGHT" else "HIGH CONTRAST DARK",
                        color = ElderlyAccentYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf(
                            Triple("EN", "ENG", Color(0xFF6366F1).copy(0.4f)),
                            Triple("BN", "বাংলা", Color(0xFFFF9933).copy(0.4f)),
                            Triple("HI", "हिन्दी", Color(0xFF10B981).copy(0.4f))
                        ).forEach { (code, label, border) ->
                            val isSelected = lang == code
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(
                                        if (isSelected) ElderlyAccentYellow else Color(0xFF152238)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) HighContrastWhite else border,
                                        shape = RoundedCornerShape(100.dp)
                                    )
                                    .clickable { viewModel.setLanguage(code) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else HighContrastWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color(0xFF1E2D4A))
                    )

                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier
                            .background(
                                if (profile.isLightMode) Color(0xFF2563EB).copy(0.1f) else Color(0xFF1E2D4A).copy(0.6f),
                                CircleShape
                            )
                            .border(1.dp, if (profile.isLightMode) Color(0xFF2563EB).copy(0.4f) else Color(0xFF1E2D4A), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (profile.isLightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme Mode",
                            tint = if (profile.isLightMode) Color(0xFF2563EB) else ElderlyAccentYellow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        SatelliteTelemetryHud(modifier = Modifier.padding(bottom = 16.dp))

        // Main accessible screen body inside scroll
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Find active next scheduled medicine for prominent layout
            val nextMed = meds.filter { it.isActive }.minByOrNull { med ->
                val nowCal = Calendar.getInstance()
                val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
                val medMinutes = med.hour * 60 + med.minute
                if (medMinutes > currentMinutes) medMinutes - currentMinutes else (1440 - currentMinutes) + medMinutes
            }

            if (nextMed != null) {
                item {
                    // Next Pill Hero Display Banner with beautiful left-hand accent and ambient card gradient
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawViewfinderCorners(ElderlyAccentYellow, strokeWidth = 4f, length = 48f)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(ElderlyAccentYellow.copy(0.4f), ElderlyAccentBlue.copy(0.4f))
                                    )
                                ),
                                AppCardShape
                            ),
                        colors = CardDefaults.cardColors(containerColor = ElderlyCardBg.copy(alpha = 0.9f)),
                        shape = AppCardShape
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            // Left accent ribbon of glowing colors
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .fillMaxHeight()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(ElderlyAccentYellow, ElderlyAccentBlue)
                                        )
                                    )
                            )
                            
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .background(Color(0xFF0F1A2C), RoundedCornerShape(100.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationImportant,
                                        contentDescription = null,
                                        tint = ElderlyAccentYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "UPCOMING ALERT TODAY",
                                        color = ElderlyAccentYellow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = nextMed.name,
                                    color = HighContrastWhite,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = nextMed.dosage,
                                    color = ElderlyAccentYellow,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .background(ElderlyAccentBlue.copy(0.15f), RoundedCornerShape(12.dp))
                                        .border(1.dp, ElderlyAccentBlue.copy(0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = ElderlyAccentBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d:%02d", nextMed.hour, nextMed.minute),
                                        color = HighContrastWhite,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Standard listings of medicines scheduled for elder inspection
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp, 20.dp)
                            .background(ElderlyAccentBlue, RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.getString("active_schedules", lang),
                        color = HighContrastWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            val activeMeds = meds.filter { it.isActive }
            if (activeMeds.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElderlyCardBg.copy(alpha = 0.6f), AppCardShape)
                            .border(AppBorderStroke, AppCardShape)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active medicines scheduled. Please add some from Caregiver settings.",
                            color = Color.LightGray.copy(0.8f),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(activeMeds) { med ->
                    // Determine if taken today matching scheduled hour/minute
                    val isTakenToday = logs.any { log ->
                        log.medicationId == med.id &&
                        log.status == "TAKEN" &&
                        isTimestampToday(log.scheduledTimestamp)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawViewfinderCorners(
                                color = if (isTakenToday) ElderlyAccentGreen.copy(0.6f) else ElderlyAccentBlue.copy(0.4f),
                                strokeWidth = 3f,
                                length = 28f
                            )
                            .border(
                                width = 1.dp,
                                color = if (isTakenToday) ElderlyAccentGreen.copy(0.8f) else (if (currentIsLightMode) Color(0xFFE2E8F0) else Color(0xFF475569).copy(alpha = 0.5f)),
                                shape = AppCardShape
                            ),
                        colors = CardDefaults.cardColors(containerColor = ElderlyCardBg.copy(alpha = 0.7f)),
                        shape = AppCardShape
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left-hand Swiss style indicator strip mapping status
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight()
                                    .background(if (isTakenToday) ElderlyAccentGreen else ElderlyAccentBlue)
                            )
                            
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isTakenToday) ElderlyAccentGreen.copy(0.12f) else ElderlyAccentBlue.copy(0.08f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isTakenToday) ElderlyAccentGreen.copy(0.3f) else ElderlyAccentBlue.copy(0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isTakenToday) Icons.Default.CheckCircle else Icons.Default.MedicalServices,
                                            contentDescription = null,
                                            tint = if (isTakenToday) ElderlyAccentGreen else ElderlyAccentYellow,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = med.name,
                                            color = HighContrastWhite,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = med.dosage,
                                            color = ElderlyAccentYellow,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = Color.LightGray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = String.format(Locale.getDefault(), "%02d:%02d", med.hour, med.minute),
                                                color = Color.LightGray,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                                
                                // Visual badge indicating status and TTS announcements
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.announceMedication(med, lang) },
                                        modifier = Modifier
                                            .background(ElderlyAccentBlue.copy(0.12f), CircleShape)
                                            .border(1.dp, ElderlyAccentBlue.copy(0.4f), CircleShape)
                                            .size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Read out loud",
                                            tint = ElderlyAccentBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    if (isTakenToday) {
                                        Box(
                                            modifier = Modifier
                                                .background(ElderlyAccentGreen.copy(0.15f), RoundedCornerShape(100.dp))
                                                .border(1.dp, ElderlyAccentGreen, RoundedCornerShape(100.dp))
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = Localization.getString("status_taken", lang),
                                                color = ElderlyAccentGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF0F1A2C), RoundedCornerShape(100.dp))
                                                .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(100.dp))
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "PENDING",
                                                color = HighContrastWhite.copy(0.6f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large High-Contrast circular 'SOS' emergency button on the dashboard
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(150.dp)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown()
                                isHoldingSos = true
                                var upOrCancel = false
                                while (!upOrCancel) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.all { !it.pressed }) {
                                        upOrCancel = true
                                    }
                                }
                                isHoldingSos = false
                            }
                        }
                    }
                    .graphicsLayer {
                        val scale = if (isHoldingSos) 0.94f + (0.06f * (1f - sosProgress)) else 1.0f
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                // Background Glow/Ring filling up as progress indicator
                CircularProgressIndicator(
                    progress = sosProgress,
                    modifier = Modifier.size(146.dp),
                    color = ElderlyAccentYellow,
                    strokeWidth = 8.dp,
                    trackColor = if (currentIsLightMode) Color(0xFFE2E8F0) else Color(0xFF1E2D4A).copy(alpha = 0.5f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Large Circular Red SOS central button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    ElderlyAccentRed,
                                    ElderlyAccentRed.copy(0.85f)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                width = if (isHoldingSos) 4.dp else 2.dp,
                                color = if (isHoldingSos) ElderlyAccentYellow else HighContrastWhite
                            ),
                            CircleShape
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Emergency SOS Icon",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SOS",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Informative, localized high-contrast helpful instruction labels
            Text(
                text = "EMERGENCY CALL CAREGIVER",
                color = ElderlyAccentRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            
            Text(
                text = if (isHoldingSos) {
                    "HOLDING... ${((3000 - (sosProgress * 3000).toInt()) / 1000).coerceAtLeast(1)}s"
                } else {
                    Localization.getString("sos_hold_prompt", lang).uppercase()
                },
                color = if (isHoldingSos) ElderlyAccentYellow else HighContrastWhite.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ==========================================
// 2. ACTIVE REMINDER FULL SCREEN OUTLET
// ==========================================
@Composable
fun ActiveAlarmFullScreen(
    viewModel: MedicationViewModel,
    medication: MedicationEntity,
    lang: String
) {
    val speechTxt by viewModel.speechResultText.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val speechErr by viewModel.speechError.collectAsStateWithLifecycle()

    // Breathing pulse scale animation representing active sirens
    val infiniteTransition = rememberInfiniteTransition(label = "FlashingSirens")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = if (isListening) {
                        listOf(Color(0xFF071F17), Color(0xFF0F121C))
                    } else {
                        listOf(Color(0xFF220D15), Color(0xFF0E101A))
                    }
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Red/Gold Pulsing Status Ringing Board
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .drawViewfinderCorners(
                        if (isListening) ElderlyAccentGreen else ElderlyAccentRed,
                        strokeWidth = 3f,
                        length = 40f
                    )
                    .border(
                        BorderStroke(
                            1.dp, 
                            if (isListening) ElderlyAccentGreen else ElderlyAccentRed
                        ), 
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = (if (isListening) ElderlyAccentGreen else ElderlyAccentRed).copy(0.12f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (isListening) ElderlyAccentGreen else ElderlyAccentRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationImportant,
                            contentDescription = "Alert siren ringing",
                            tint = Color.Black,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = (if (isListening) "LISTENING FOR VOICE CONFIRM" else Localization.getString("active_alarm_status", lang)).uppercase(),
                        color = if (isListening) ElderlyAccentGreen else ElderlyAccentRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Midsection displaying targets
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = medication.name,
                    color = HighContrastWhite,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${Localization.getString("dosage_alert", lang)} ${medication.dosage}",
                    color = ElderlyAccentYellow,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            // Voice Controls / Confirmation Module
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Localization.getString("voice_confirm_prompt", lang),
                    color = HighContrastWhite.copy(0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "\"${medication.customVoicePhrase}\" ${Localization.getString("voice_say", lang)} \"taken\"",
                    color = ElderlyAccentYellow,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Giant pulsing voice trigger or microphone icon with beautiful glow ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .drawViewfinderCrosshair(if (isListening) ElderlyAccentGreen else ElderlyAccentYellow)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = if (isListening) {
                                    listOf(ElderlyAccentGreen, ElderlyAccentGreen.copy(0.3f), Color.Transparent)
                                } else {
                                    listOf(ElderlyAccentYellow, ElderlyAccentYellow.copy(0.3f), Color.Transparent)
                                }
                            )
                        )
                        .clickable {
                            if (isListening) {
                                viewModel.stopListeningVoice()
                            } else {
                                viewModel.startListeningVoice()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                if (isListening) {
                                    scaleX = scale
                                    scaleY = scale
                                }
                            }
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(if (isListening) ElderlyAccentGreen else ElderlyAccentYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice confirm mic",
                            tint = Color.Black,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isListening) {
                    Text(
                        text = Localization.getString("voice_listening", lang),
                        color = ElderlyAccentGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                if (speechTxt.isNotEmpty()) {
                    Text(
                        text = "\"$speechTxt\"",
                        color = ElderlyAccentGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (speechErr != null) {
                    Text(
                        text = speechErr ?: "",
                        color = ElderlyAccentRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                // Interactive Simulator confirmation button with glassmorphic style
                Button(
                    onClick = { viewModel.simulateVoiceConfirm() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E2D4A),
                        contentColor = HighContrastWhite
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2C3E5B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "📢 SIMULATE VOICE CONFIRMATION",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Giant gorgeous manual override emergency confirm button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clickable { viewModel.confirmActiveDoseTaken("MANUAL") }
                    .border(
                        BorderStroke(1.dp, Color(0xFF34D399)), 
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = ElderlyAccentGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = Localization.getString("manual_confirm_btn", lang).uppercase(),
                        color = Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. CAREGIVER OFFICE PANEL / DASHBOARD
// ==========================================
@Composable
fun CaregiverDashboard(viewModel: MedicationViewModel, profile: ProfileEntity) {
    val context = LocalContext.current
    val meds by viewModel.medications.collectAsStateWithLifecycle()
    val logs by viewModel.adherenceLogs.collectAsStateWithLifecycle()
    val syncState by viewModel.syncStatus.collectAsStateWithLifecycle()
    val notifications by viewModel.caregiverNotifications.collectAsStateWithLifecycle()
    val lang = profile.language

    // State form holding new medication
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("") }
    var medHour by remember { mutableStateOf("") }
    var medMinute by remember { mutableStateOf("") }
    var customPhrase by remember { mutableStateOf("") }

    // Patient profile updates state
    var editName by remember { mutableStateOf(profile.name) }
    var editAge by remember { mutableStateOf(profile.age.toString()) }
    var editPhone by remember { mutableStateOf(profile.emergencyPhone) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ElderlyBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Navigation / Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Localization.getString("caregiver_control_title", lang),
                        color = HighContrastWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Synchronized Admin Suite",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { viewModel.toggleView() },
                    colors = ButtonDefaults.buttonColors(containerColor = ElderlyAccentGreen),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessibilityNew, 
                        contentDescription = null, 
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.getString("patient_view", lang),
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Divider(color = Color(0xFF1E2D4A), thickness = 1.dp)
        }

        // Synchronized cloud monitor summary card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawViewfinderCorners(ElderlyAccentBlue.copy(0.6f), strokeWidth = 3f, length = 36f)
                    .border(BorderStroke(1.dp, Color(0xFF1E2D4A).copy(0.6f)), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = ElderlyCardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = Localization.getString("sync_dashboard", lang),
                        color = HighContrastWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (syncState == "Synced") ElderlyAccentGreen else ElderlyAccentYellow)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${Localization.getString("sync_status_label", lang)} $syncState",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { viewModel.triggerForceSync() },
                            colors = ButtonDefaults.buttonColors(containerColor = ElderlyAccentBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(38.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync, 
                                contentDescription = null, 
                                tint = HighContrastWhite, 
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.getString("sync", lang), 
                                color = HighContrastWhite, 
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calculations on total adherence rate
                    val takenLogs = logs.filter { it.status == "TAKEN" }.size
                    val totalTracked = logs.size
                    val adherenceRate = if (totalTracked > 0) {
                        (takenLogs * 100) / totalTracked
                    } else {
                        100
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Scheduled Reminders:", color = Color.Gray, fontSize = 14.sp)
                        Text(text = "${meds.size}", color = HighContrastWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Patient Adherence Score:", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "$adherenceRate%",
                            color = if (adherenceRate > 80) ElderlyAccentGreen else ElderlyAccentYellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Today's High-Contrast Medicine Schedule and Status (Simplified Dashboard for Caregivers)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawViewfinderCorners(ElderlyAccentYellow.copy(0.7f), strokeWidth = 3f, length = 36f)
                    .border(BorderStroke(2.dp, ElderlyAccentYellow.copy(0.8f)), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = ElderlyCardBg),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S SCHEDULE & STATUS",
                                color = ElderlyAccentYellow,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Caregiver High-Contrast Scan Panel",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = ElderlyAccentYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val activeTodayMeds = meds.filter { it.isActive }
                    if (activeTodayMeds.isEmpty()) {
                        Text(
                            text = "No medications scheduled for today.",
                            color = Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            activeTodayMeds.forEach { med ->
                                val todayLog = logs.firstOrNull { log ->
                                    log.medicationId == med.id && isTimestampToday(log.scheduledTimestamp)
                                }
                                val status = todayLog?.status ?: "PENDING"
                                val statusColor = when (status) {
                                    "TAKEN" -> ElderlyAccentGreen
                                    "MISSED" -> ElderlyAccentRed
                                    else -> ElderlyAccentBlue
                                }
                                val statusBg = statusColor.copy(alpha = 0.15f)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0C1322), RoundedCornerShape(16.dp))
                                        .border(BorderStroke(1.5.dp, statusColor.copy(0.5f)), RoundedCornerShape(16.dp))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = med.name,
                                            color = HighContrastWhite,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = ElderlyAccentYellow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = String.format(Locale.getDefault(), "%02d:%02d", med.hour, med.minute),
                                                color = ElderlyAccentYellow,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = med.dosage,
                                                color = Color.LightGray,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        if (status == "TAKEN" && todayLog?.takenTimestamp != null) {
                                            Text(
                                                text = "Taken at: " + formatStamp(todayLog.takenTimestamp),
                                                color = ElderlyAccentGreen,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Audible TTS Trigger Button for Caregiver accessibility
                                        IconButton(
                                            onClick = { viewModel.announceMedication(med, lang) },
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .background(Color(0xFF1E2D4A).copy(0.6f), CircleShape)
                                                .size(38.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Read out loud",
                                                tint = ElderlyAccentBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(statusBg, RoundedCornerShape(100.dp))
                                                .border(1.5.dp, statusColor, RoundedCornerShape(100.dp))
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = status,
                                                color = statusColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Alerts Push-Notification panel (Simulating Missed Dose Alerter)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawViewfinderCorners(ElderlyAccentRed.copy(0.6f), strokeWidth = 3f, length = 36f)
                    .border(BorderStroke(1.dp, ElderlyAccentRed.copy(0.4f)), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = ElderlyAccentRed.copy(0.08f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning, 
                            contentDescription = null, 
                            tint = ElderlyAccentRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.getString("instant_alerts", lang),
                            color = ElderlyAccentRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (notifications.isEmpty()) {
                        Text(
                            text = Localization.getString("no_alerts", lang),
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            notifications.take(3).forEach { note ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(ElderlyAccentRed)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = note,
                                        color = HighContrastWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // System Permissions Configuration Checklist (Post notifications, Speak record, Overlay)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFF1E2D4A)), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = ElderlyCardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = Localization.getString("settings_permissions", lang),
                        color = ElderlyAccentYellow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Localization.getString("perm_instructions", lang),
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission 1: Notification channel setup trigger
                    PermissionCheckRow(
                        title = Localization.getString("permissions_btn_post", lang),
                        isAllowed = true, // represented as enabled
                        onConfigure = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            }
                        },
                        lang = lang
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Permission 2: Draw over lockscreen / overlay
                    PermissionCheckRow(
                        title = Localization.getString("permissions_btn_overlay", lang),
                        isAllowed = Settings.canDrawOverlays(context),
                        onConfigure = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        lang = lang
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Permission 3: Exact Alarm schedule
                    PermissionCheckRow(
                        title = Localization.getString("permissions_btn_alarm", lang),
                        isAllowed = true, // defaulted
                        onConfigure = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                val intent = Intent().apply {
                                    action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        lang = lang
                    )
                }
            }
        }

        // Patient Profile setup Form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(AppBorderStroke, AppCardShape),
                colors = CardDefaults.cardColors(containerColor = ElderlyCardBg),
                shape = AppCardShape
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Patient Profile Settings",
                        color = HighContrastWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(Localization.getString("patient_name", lang)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElderlyAccentBlue,
                            focusedLabelColor = ElderlyAccentBlue,
                            unfocusedLabelColor = Color.Gray,
                            unfocusedBorderColor = if (currentIsLightMode) Color(0xFFE2E8F0) else Color(0xFF475569).copy(alpha = 0.5f),
                            unfocusedTextColor = HighContrastWhite,
                            focusedTextColor = HighContrastWhite,
                            focusedContainerColor = Color(0xFF0C1322),
                            unfocusedContainerColor = Color(0xFF0C1322)
                        ),
                        shape = AppInputShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editAge,
                            onValueChange = { editAge = it },
                            label = { Text(Localization.getString("patient_age", lang)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElderlyAccentBlue,
                                focusedLabelColor = ElderlyAccentBlue,
                                unfocusedLabelColor = Color.Gray,
                                unfocusedBorderColor = if (currentIsLightMode) Color(0xFFE2E8F0) else Color(0xFF475569).copy(alpha = 0.5f),
                                unfocusedTextColor = HighContrastWhite,
                                focusedTextColor = HighContrastWhite,
                                focusedContainerColor = Color(0xFF0C1322),
                                unfocusedContainerColor = Color(0xFF0C1322)
                             ),
                            shape = AppInputShape,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text(Localization.getString("emergency_phone", lang)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElderlyAccentBlue,
                                focusedLabelColor = ElderlyAccentBlue,
                                unfocusedLabelColor = Color.Gray,
                                unfocusedBorderColor = if (currentIsLightMode) Color(0xFFE2E8F0) else Color(0xFF475569).copy(alpha = 0.5f),
                                unfocusedTextColor = HighContrastWhite,
                                focusedTextColor = HighContrastWhite,
                                focusedContainerColor = Color(0xFF0C1322),
                                unfocusedContainerColor = Color(0xFF0C1322)
                            ),
                            shape = AppInputShape,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val ageInt = editAge.toIntOrNull() ?: profile.age
                            viewModel.updateProfile(editName, ageInt, editPhone)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElderlyAccentBlue),
                        shape = AppButtonShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = Localization.getString("save_profile", lang),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Add Medication scheduler Form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawViewfinderCorners(ElderlyAccentBlue.copy(0.8f), strokeWidth = 3f, length = 36f)
                    .border(BorderStroke(2.dp, Color(0xFF1E2D4A)), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = ElderlyCardBg),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "NEW SCHEDULE CREATOR",
                        color = ElderlyAccentBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Enter details in large, high-contrast inputs below",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Medicine Name",
                        color = HighContrastWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        placeholder = { Text("e.g. Paracetamol", fontSize = 16.sp, color = Color.DarkGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElderlyAccentYellow,
                            focusedLabelColor = ElderlyAccentYellow,
                            unfocusedBorderColor = Color(0xFF2C3E5B),
                            unfocusedTextColor = HighContrastWhite,
                            focusedTextColor = HighContrastWhite,
                            focusedContainerColor = Color(0xFF050912),
                            unfocusedContainerColor = Color(0xFF050912)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Dosage",
                        color = HighContrastWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = medDosage,
                        onValueChange = { medDosage = it },
                        placeholder = { Text("e.g. 1 Tablet, 75mg morning", fontSize = 16.sp, color = Color.DarkGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElderlyAccentYellow,
                            focusedLabelColor = ElderlyAccentYellow,
                            unfocusedBorderColor = Color(0xFF2C3E5B),
                            unfocusedTextColor = HighContrastWhite,
                            focusedTextColor = HighContrastWhite,
                            focusedContainerColor = Color(0xFF050912),
                            unfocusedContainerColor = Color(0xFF050912)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Scheduled Time of Day",
                        color = HighContrastWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Morning", 8, 30),
                            Triple("Midday", 13, 0),
                            Triple("Evening", 18, 0),
                            Triple("Night", 21, 0)
                        ).forEach { (label, h, m) ->
                            val selected = medHour == h.toString() && medMinute == String.format(Locale.getDefault(), "%02d", m)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected) {
                                            ElderlyAccentYellow
                                        } else {
                                            Color(0xFF1E2D4A).copy(0.4f)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) HighContrastWhite else Color(0xFF2C3E5B),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        medHour = h.toString()
                                        medMinute = String.format(Locale.getDefault(), "%02d", m)
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.Black else HighContrastWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d:%02d", h, m),
                                        color = if (selected) Color.Black else Color.LightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hour (0-23)",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = medHour,
                                onValueChange = { if (it.length <= 2) medHour = it },
                                placeholder = { Text("12", fontSize = 16.sp, color = Color.DarkGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElderlyAccentYellow,
                                    unfocusedBorderColor = Color(0xFF2C3E5B),
                                    unfocusedTextColor = HighContrastWhite,
                                    focusedTextColor = HighContrastWhite,
                                    focusedContainerColor = Color(0xFF050912),
                                    unfocusedContainerColor = Color(0xFF050912)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Minute (0-59)",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = medMinute,
                                onValueChange = { if (it.length <= 2) medMinute = it },
                                placeholder = { Text("00", fontSize = 16.sp, color = Color.DarkGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElderlyAccentYellow,
                                    unfocusedBorderColor = Color(0xFF2C3E5B),
                                    unfocusedTextColor = HighContrastWhite,
                                    focusedTextColor = HighContrastWhite,
                                    focusedContainerColor = Color(0xFF050912),
                                    unfocusedContainerColor = Color(0xFF050912)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Voice Target Confirmation Word",
                        color = HighContrastWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = customPhrase,
                        onValueChange = { customPhrase = it },
                        placeholder = { Text("e.g. taken / khayesi / le liya", fontSize = 16.sp, color = Color.DarkGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElderlyAccentYellow,
                            focusedLabelColor = ElderlyAccentYellow,
                            unfocusedBorderColor = Color(0xFF2C3E5B),
                            unfocusedTextColor = HighContrastWhite,
                            focusedTextColor = HighContrastWhite,
                            focusedContainerColor = Color(0xFF050912),
                            unfocusedContainerColor = Color(0xFF050912)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val h = medHour.toIntOrNull() ?: 12
                            val m = medMinute.toIntOrNull() ?: 0
                            if (medName.isNotEmpty() && medDosage.isNotEmpty()) {
                                viewModel.addMedication(medName, medDosage, h, m, customPhrase)
                                // clear
                                medName = ""
                                medDosage = ""
                                medHour = ""
                                medMinute = ""
                                customPhrase = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElderlyAccentGreen,
                            disabledContainerColor = ElderlyAccentGreen.copy(0.3f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        enabled = medName.isNotEmpty() && medDosage.isNotEmpty()
                    ) {
                        Text(
                            text = Localization.getString("add_med_button", lang).uppercase(),
                            color = if (medName.isNotEmpty() && medDosage.isNotEmpty()) Color.Black else Color.DarkGray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // List of Active Schedules managed by Caregiver
        item {
            Text(
                text = "Manage Scheduled Medications",
                color = HighContrastWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (meds.isEmpty()) {
            item {
                Text(
                    text = "No medications scheduled yet.", 
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            items(meds) { med ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color(0xFF1E2D4A)), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = ElderlyCardBg),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(18.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = med.name,
                                color = HighContrastWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = med.dosage,
                                color = ElderlyAccentYellow,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "Reminds at %02d:%02d", med.hour, med.minute),
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "Code word: \"${med.customVoicePhrase}\"",
                                color = ElderlyAccentGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = med.isActive,
                                onCheckedChange = { viewModel.setMedicationActiveState(med, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ElderlyAccentGreen,
                                    checkedTrackColor = ElderlyAccentGreen.copy(0.3f),
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = { viewModel.deleteMedication(med) },
                                modifier = Modifier
                                    .background(ElderlyAccentRed.copy(0.12f), CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete, 
                                    contentDescription = "Delete medication", 
                                    tint = ElderlyAccentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Historical Adherence Logs
        item {
            Text(
                text = Localization.getString("adherence_history", lang),
                color = HighContrastWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val sortedLogs = logs
        if (sortedLogs.isEmpty()) {
            item {
                Text(
                    text = "No historic log data recorded.", 
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            items(sortedLogs) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color(0xFF1E2D4A)), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = ElderlyCardBg),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.medName,
                                color = HighContrastWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${Localization.getString("logged_at", lang)} ${formatStamp(log.scheduledTimestamp)}",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (log.takenTimestamp != null) {
                                Text(
                                    text = "${Localization.getString("taken_at", lang)} ${formatStamp(log.takenTimestamp)}",
                                    color = ElderlyAccentGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "${Localization.getString("confirmed_via", lang)} ${log.confirmationType}",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = when (log.status) {
                                        "TAKEN" -> ElderlyAccentGreen.copy(0.12f)
                                        "MISSED" -> ElderlyAccentRed.copy(0.12f)
                                        else -> Color.Gray.copy(0.12f)
                                    },
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = when (log.status) {
                                        "TAKEN" -> ElderlyAccentGreen
                                        "MISSED" -> ElderlyAccentRed
                                        else -> Color.Gray
                                    },
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = log.status,
                                color = when (log.status) {
                                    "TAKEN" -> ElderlyAccentGreen
                                    "MISSED" -> ElderlyAccentRed
                                    else -> Color.Gray
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionCheckRow(
    title: String,
    isAllowed: Boolean,
    onConfigure: () -> Unit,
    lang: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0C1322), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color(0xFF1E2D4A)), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = HighContrastWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(if (isAllowed) ElderlyAccentGreen else Color(0xFFFF9800))
                .clickable { if (!isAllowed) onConfigure() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isAllowed) Localization.getString("state_allowed", lang) else Localization.getString("state_configure", lang),
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// Helper time matching functions
private fun isTimestampToday(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun formatStamp(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
