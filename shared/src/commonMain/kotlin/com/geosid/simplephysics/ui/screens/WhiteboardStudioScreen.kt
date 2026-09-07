package com.geosid.simplephysics.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.model.ExperimentRegistry
import com.geosid.simplephysics.ui.components.PresenterMood
import com.geosid.simplephysics.ui.components.WhiteboardPresenter
import com.geosid.simplephysics.ui.expirementsRegistry.ExperimentScreenRegistry
import com.geosid.simplephysics.ui.theme.*
import kotlinx.coroutines.delay

data class ScriptStep(
    val title: String,
    val narration: String,
    val mood: PresenterMood,
    val durationSeconds: Int
)

@Composable
fun WhiteboardStudioScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val experiments = ExperimentRegistry.experiments
    var selectedExpId by remember { mutableStateOf("static_straw") }
    val currentExp = experiments.find { it.id == selectedExpId } ?: experiments.first()

    // 9:16 Shorts Safe Zone Overlay toggle
    var showShortsOverlay by remember { mutableStateOf(false) }

    // Auto-Script Presentation Player
    var isPresenting by remember { mutableStateOf(false) }
    var currentStepIndex by remember { mutableStateOf(0) }

    val scripts = remember(selectedExpId) {
        getScriptForExperiment(selectedExpId)
    }

    val currentStep = scripts.getOrNull(currentStepIndex) ?: scripts.first()

    // Script timeline ticker
    LaunchedEffect(isPresenting, selectedExpId) {
        if (isPresenting) {
            currentStepIndex = 0
            while (isPresenting && currentStepIndex < scripts.size) {
                val step = scripts[currentStepIndex]
                delay(step.durationSeconds * 1000L)
                if (currentStepIndex < scripts.size - 1) {
                    currentStepIndex++
                } else {
                    isPresenting = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Studio Toolbar
            Surface(
                color = ScienceDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = ScienceDarkSurfaceVariant, contentColor = TextPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("← Back to Catalog", fontSize = 13.sp)
                        }

                        Text(
                            text = "🎬 Whiteboard Video Studio",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        // 9:16 Overlay button
                        FilterChip(
                            selected = showShortsOverlay,
                            onClick = { showShortsOverlay = !showShortsOverlay },
                            label = { Text("📱 9:16 Shorts Guide", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberVibrant,
                                selectedLabelColor = ScienceDarkBg
                            )
                        )

                        // Play Script button
                        Button(
                            onClick = {
                                if (isPresenting) {
                                    isPresenting = false
                                } else {
                                    isPresenting = true
                                    currentStepIndex = 0
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPresenting) CoralNeon else EmeraldNeon,
                                contentColor = ScienceDarkBg
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (isPresenting) "⏹ Stop Presentation" else "▶ Run 30s Viral Script",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Experiment Selector Ribbon
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScienceDarkBg)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(experiments) { exp ->
                    val isSel = exp.id == selectedExpId
                    Surface(
                        color = if (isSel) CyanNeon.copy(alpha = 0.2f) else ScienceDarkSurfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) CyanNeon else Color.Transparent
                        ),
                        modifier = Modifier.clickable {
                            selectedExpId = exp.id
                            isPresenting = false
                            currentStepIndex = 0
                        }
                    ) {
                        Text(
                            text = exp.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) CyanNeon else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Main Whiteboard Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Whiteboard Frame & Canvas
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(8.dp, Color(0xFF94A3B8)), // Aluminum frame
                    shadowElevation = 14.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Whiteboard Marker Tray at bottom
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawWhiteboardGridAndTray()
                        }

                        // Split Row: Left = Presenter character; Right = Interactive Experiment
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 32.dp, top = 8.dp, start = 12.dp, end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Dr. Kelvin Presenter
                            WhiteboardPresenter(
                                mood = if (isPresenting) currentStep.mood else PresenterMood.EXPLAINING,
                                speechText = if (isPresenting) currentStep.narration else currentExp.teaser,
                                modifier = Modifier
                                    .weight(0.32f)
                                    .fillMaxHeight()
                                    .padding(horizontal = 8.dp)
                            )

                            // Right: The Physical Experiment
                            Box(
                                modifier = Modifier
                                    .weight(0.68f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ScienceDarkBg)
                            ) {
                                val expScreen = ExperimentScreenRegistry.getScreen(selectedExpId)
                                if (expScreen != null) {
                                    expScreen()
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "🔬 Experiment in development\nUnlocks on YouTube release day",
                                            color = TextSecondary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // 9:16 Shorts Safe Zone Overlay Guide (for creator screen recording)
                        if (showShortsOverlay) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val shortsW = h * (9f / 16f)
                                val left = (w - shortsW) / 2f
                                val right = left + shortsW

                                // Darken outside safe zone
                                drawRect(Color.Black.copy(alpha = 0.55f), Offset(0f, 0f), Size(left, h))
                                drawRect(Color.Black.copy(alpha = 0.55f), Offset(right, 0f), Size(w - right, h))

                                // Red border around 9:16 crop box
                                drawRect(
                                    color = CoralNeon,
                                    topLeft = Offset(left, 0f),
                                    size = Size(shortsW, h),
                                    style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Presentation Timeline Bar (when running script)
            if (isPresenting) {
                Surface(
                    color = ScienceDarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🎬 Step ${currentStepIndex + 1} of ${scripts.size}: ${currentStep.title}",
                            color = AmberVibrant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            scripts.forEachIndexed { idx, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(if (idx == currentStepIndex) 24.dp else 10.dp, 8.dp)
                                        .background(
                                            if (idx == currentStepIndex) CyanNeon else Color(0x55FFFFFF),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawWhiteboardGridAndTray() {
    val w = size.width
    val h = size.height

    // Faint grid dots for whiteboard technical graph feel
    val dotSpacing = 36f
    for (x in 20..(w - 20).toInt() step dotSpacing.toInt()) {
        for (y in 20..(h - 45).toInt() step dotSpacing.toInt()) {
            drawCircle(Color(0xFFCBD5E1), 1f, Offset(x.toFloat(), y.toFloat()))
        }
    }

    // Bottom Dry-Erase Aluminum Pen Tray
    val trayY = h - 22f
    drawRect(Color(0xFF64748B), Offset(20f, trayY), Size(w - 40f, 16f))
    drawRect(Color(0xFF94A3B8), Offset(20f, trayY), Size(w - 40f, 3f))

    // Markers on Tray (Black, Red, Blue, Green)
    val markers = listOf(Color(0xFF0F172A), Color(0xFFFF1744), Color(0xFF2979FF), Color(0xFF00E676))
    markers.forEachIndexed { i, col ->
        val mx = 60f + i * 35f
        drawRoundRect(col, Offset(mx, trayY - 4f), Size(24f, 6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))
    }

    // Felt Eraser
    drawRoundRect(Color(0xFF1E293B), Offset(220f, trayY - 6f), Size(42f, 9f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f))
    drawRoundRect(Color(0xFF475569), Offset(220f, trayY - 9f), Size(42f, 3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f))
}

// 30-Second Viral Short Video Scripts for Dr. Kelvin
private fun getScriptForExperiment(expId: String): List<ScriptStep> {
    return when (expId) {
        "fourier_series" -> listOf(
            ScriptStep("The 3-Second Hook", "Can you build a sharp square box using ONLY smooth round circles?!", PresenterMood.THINKING, 4),
            ScriptStep("The Demonstration", "Watch what happens when we chain 3, 5, then 15 rotating epicycles together!", PresenterMood.POINTING, 6),
            ScriptStep("The Science Secret", "This is Fourier Synthesis! But notice those sharp horns on the corners?", PresenterMood.EXCITED, 6),
            ScriptStep("The Gibbs Phenomenon", "That's the famous Gibbs Phenomenon! It never goes away even with infinite circles!", PresenterMood.SHOCKED, 7),
            ScriptStep("Call to Action", "Which wave should we build next? Sawtooth or Triangle? Download the app to try it!", PresenterMood.EXPLAINING, 7)
        )
        "citrus_balloon" -> listOf(
            ScriptStep("The 3-Second Hook", "Why does an orange peel explode a balloon without even touching it?!", PresenterMood.THINKING, 4),
            ScriptStep("The Chemical Squirt", "Watch what happens when I squeeze this citrus mist onto the inflated latex...", PresenterMood.POINTING, 6),
            ScriptStep("BOOM!", "BOOM! The solvent d-Limonene dissolves non-polar rubber chains in milliseconds!", PresenterMood.SHOCKED, 6),
            ScriptStep("Tensile Rupture", "Under high air tension, a microscopic weak spot unzips at the speed of sound!", PresenterMood.EXCITED, 7),
            ScriptStep("Try It At Home", "Try this at your next party with an orange peel! Follow for more science magic!", PresenterMood.EXPLAINING, 7)
        )
        "pencil_water_bag" -> listOf(
            ScriptStep("The 3-Second Hook", "Stabbing a water bag with 5 sharp pencils sounds like an immediate disaster...", PresenterMood.THINKING, 4),
            ScriptStep("The Puncture", "Watch closely as the pencil plunges straight through both sides... ZERO LEAKS!", PresenterMood.POINTING, 6),
            ScriptStep("Polymer Grip", "High-Density Polyethylene molecules act like tiny rubber bands sealing the tip!", PresenterMood.EXCITED, 7),
            ScriptStep("The Leak Twist", "BUT look what happens when you pull the pencil back out! A high-speed jet!", PresenterMood.SHOCKED, 7),
            ScriptStep("Challenge", "Can you push 10 pencils through? Test it yourself in the SimplePhysics app!", PresenterMood.EXPLAINING, 6)
        )
        "kepler_orbits" -> listOf(
            ScriptStep("The 3-Second Hook", "Planets DO NOT orbit in circles, and they definitely don't move at constant speed!", PresenterMood.THINKING, 5),
            ScriptStep("Whip-Fast Perihelion", "Notice how it slingshots whip-fast near the sun, then cruises slowly far away!", PresenterMood.POINTING, 7),
            ScriptStep("Kepler's 2nd Law", "This glowing wedge sweeps out the exact same area in equal intervals of time!", PresenterMood.EXCITED, 8),
            ScriptStep("Universal Gravity", "That's conservation of angular momentum at astronomical scales!", PresenterMood.EXPLAINING, 6)
        )
        else -> listOf(
            ScriptStep("The Hook", "Check out this incredible physics phenomenon in action!", PresenterMood.POINTING, 5),
            ScriptStep("The Science", "Watch how the equations of motion predict every move in real time!", PresenterMood.EXCITED, 8),
            ScriptStep("The Reveal", "Try changing the sliders to test the limits of physical laws!", PresenterMood.EXPLAINING, 8)
        )
    }
}
