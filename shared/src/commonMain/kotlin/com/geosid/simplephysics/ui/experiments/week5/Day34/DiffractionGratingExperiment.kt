package com.geosid.simplephysics.ui.experiments.week5.Day34

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.ui.components.ExperimentHudCard
import com.geosid.simplephysics.ui.components.PhysicsSliderControl
import com.geosid.simplephysics.ui.components.ResetIcon
import com.geosid.simplephysics.ui.components.ResponsiveExperimentContainer
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*

/**
 * Elemental gas discharge spectra presets for Day 34: Diffraction Grating Spectrometer.
 */
enum class GratingSpectralPreset(
    val label: String,
    val sourceName: String,
    val spectralLinesNm: List<Float>,
    val linesPerMm: Float,
    val desc: String
) {
    HYDROGEN_BALMER(
        label = "Hydrogen Balmer",
        sourceName = "Atomic Hydrogen (H Discharge Tube)",
        spectralLinesNm = listOf(656.3f, 486.1f, 434.0f, 410.2f), // H-alpha, beta, gamma, delta
        linesPerMm = 600f,
        desc = "Fundamental Balmer emission lines: Red H-alpha (656nm), Cyan H-beta (486nm), Violet (434nm)"
    ),
    SODIUM_DOUBLET(
        label = "Sodium Doublet",
        sourceName = "Sodium Vapor Lamp (Na)",
        spectralLinesNm = listOf(589.0f, 589.6f), // D2 and D1 lines
        linesPerMm = 1200f,
        desc = "Famous yellow Sodium D-lines separated by only 0.6nm, resolved cleanly at high ruling density"
    ),
    MERCURY_ARC(
        label = "Mercury Arc",
        sourceName = "Mercury Discharge Lamp (Hg)",
        spectralLinesNm = listOf(579.1f, 577.0f, 546.1f, 435.8f, 404.7f),
        linesPerMm = 600f,
        desc = "Classic calibration spectrum with vibrant green (546nm), yellow doublet (578nm), and violet lines"
    ),
    CALIBRATION_LASERS(
        label = "RGB Lasers",
        sourceName = "Tri-Color Laser Array",
        spectralLinesNm = listOf(632.8f, 532.0f, 445.0f), // He-Ne Red, DPSS Green, Diode Blue
        linesPerMm = 800f,
        desc = "Coherent monochromatic lasers showing razor-sharp diffraction diffraction orders"
    ),
    CONTINUOUS_WHITE(
        label = "White Light Ribbon",
        sourceName = "Incandescent Tungsten Lamp",
        spectralLinesNm = listOf(700f, 650f, 610f, 570f, 530f, 490f, 450f, 410f),
        linesPerMm = 500f,
        desc = "Continuous blackbody light source fanning out into complete visible rainbow orders"
    )
}

/**
 * Maps a light wavelength in nanometers (380 - 780 nm) to a spectral RGB Color.
 */
private fun wavelengthToColor(wavelengthNm: Float): Color {
    val gamma = 0.80f
    val r: Float
    val g: Float
    val b: Float
    when {
        wavelengthNm in 380f..440f -> {
            r = -(wavelengthNm - 440f) / (440f - 380f)
            g = 0.0f
            b = 1.0f
        }
        wavelengthNm in 440f..490f -> {
            r = 0.0f
            g = (wavelengthNm - 440f) / (490f - 440f)
            b = 1.0f
        }
        wavelengthNm in 490f..510f -> {
            r = 0.0f
            g = 1.0f
            b = -(wavelengthNm - 510f) / (510f - 490f)
        }
        wavelengthNm in 510f..580f -> {
            r = (wavelengthNm - 510f) / (580f - 510f)
            g = 1.0f
            b = 0.0f
        }
        wavelengthNm in 580f..645f -> {
            r = 1.0f
            g = -(wavelengthNm - 645f) / (645f - 580f)
            b = 0.0f
        }
        wavelengthNm in 645f..780f -> {
            r = 1.0f
            g = 0.0f
            b = 0.0f
        }
        else -> {
            r = 0f; g = 0f; b = 0f
        }
    }
    val factor = when {
        wavelengthNm in 380f..420f -> 0.35f + 0.65f * (wavelengthNm - 380f) / (420f - 380f)
        wavelengthNm in 420f..700f -> 1.0f
        wavelengthNm in 700f..780f -> 0.35f + 0.65f * (780f - wavelengthNm) / (780f - 700f)
        else -> 0.0f
    }
    return Color(
        red = (r * factor).pow(gamma).coerceIn(0f, 1f),
        green = (g * factor).pow(gamma).coerceIn(0f, 1f),
        blue = (b * factor).pow(gamma).coerceIn(0f, 1f)
    )
}

@Composable
fun DiffractionGratingExperiment(
    modifier: Modifier = Modifier
) {
    // Physical Parameters
    var linesPerMm by remember { mutableStateOf(600.0f) }      // N (lines/mm)
    var screenDistanceMm by remember { mutableStateOf(500.0f) } // L (mm)
    var maxOrderToShow by remember { mutableStateOf(2) }        // m max = 1 or 2
    var selectedPreset by remember { mutableStateOf<GratingSpectralPreset?>(GratingSpectralPreset.HYDROGEN_BALMER) }
    var activeLines by remember { mutableStateOf(GratingSpectralPreset.HYDROGEN_BALMER.spectralLinesNm) }
    var isRunning by remember { mutableStateOf(true) }
    var simTime by remember { mutableStateOf(0f) }

    // Interactive Virtual Goniometer Detector Probe (relative Y on screen: 0.0 .. 1.0)
    var probeRelY by remember { mutableStateOf(0.50f) }

    // Animation frame loop
    LaunchedEffect(isRunning) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) {
                    lastNanos = nanos
                    return@withFrameNanos
                }
                val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastNanos = nanos
                if (isRunning) {
                    simTime += dt
                }
            }
        }
    }

    // Physical Calculations for HUD & Telemetry
    val grooveSpacingNm = 1_000_000f / linesPerMm
    val grooveSpacingUm = 1_000f / linesPerMm
    val orderM = 1
    val wavelengthNm = activeLines.firstOrNull() ?: 589f
    val totalGrooveCount = (linesPerMm * 10f).toInt() // across 10mm beam spot
    val numSlitsActive = 8

    // 1. Grating equation: angular deflection for m-th diffraction order
    val sinTheta = (orderM * wavelengthNm) / grooveSpacingNm
    val isDiffracted = abs(sinTheta) <= 1.0f
    val thetaRad = if (isDiffracted) asin(sinTheta) else 0f

    // 2. Linear screen displacement and angular dispersion rate
    val screenPosY = screenDistanceMm * tan(thetaRad)
    val angularDispersion = if (cos(thetaRad) > 1e-4f) orderM / (grooveSpacingNm * 1e-6f * cos(thetaRad)) else 0f

    // 3. Multi-slit chromatic resolving power and peak interference intensity
    val resolvingPower = orderM * totalGrooveCount
    val beta = (PI.toFloat() * grooveSpacingNm * sin(thetaRad)) / wavelengthNm
    val intensityFactor = if (abs(sin(beta)) < 1e-3f) 1f else (sin(numSlitsActive * beta) / (numSlitsActive * sin(beta))).pow(2)

    val thetaDeg = thetaRad * 180f / PI.toFloat()

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag the detector probe on the right screen to measure spectral emission peaks. Tune groove density and elemental sources below!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val pos = change.position
                            probeRelY = (pos.y / size.height.toFloat()).coerceIn(0.08f, 0.92f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            probeRelY = (pos.y / size.height.toFloat()).coerceIn(0.08f, 0.92f)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // 1. Scientific coordinate grid
                val gridSpacing = 40.dp.toPx()
                var gx = 0f
                while (gx < w) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.20f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, h),
                        strokeWidth = 0.8f
                    )
                    gx += gridSpacing
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.20f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 0.8f
                    )
                    gy += gridSpacing
                }

                // 2. Optical Apparatus Layout (Elevated origin per AGY rules)
                val cy = h * 0.40f
                val srcX = w * 0.08f
                val gratingX = w * 0.36f
                val screenX = w * 0.88f
                val screenWidth = w * 0.045f
                val screenTop = h * 0.08f
                val screenBottom = h * 0.74f

                // Central optical axis line
                drawLine(
                    color = TextMuted.copy(alpha = 0.35f),
                    start = Offset(srcX, cy),
                    end = Offset(screenX + screenWidth, cy),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // 3. Collimated Light Source / Gas Discharge Tube Emitter (Left)
                val emitterRadius = 14.dp.toPx()
                drawCircle(ScienceDarkSurfaceVariant, emitterRadius, Offset(srcX, cy))
                drawCircle(Color.White, emitterRadius * 0.55f, Offset(srcX, cy))
                drawCircle(CyanNeon, emitterRadius, Offset(srcX, cy), style = Stroke(2.dp.toPx()))

                // Collimated multi-spectral incident beam hitting grating
                activeLines.forEachIndexed { idx, wl ->
                    val lineCol = wavelengthToColor(wl)
                    val beamOffset = (idx - activeLines.size / 2f) * 1.5f
                    drawLine(
                        color = lineCol.copy(alpha = 0.65f),
                        start = Offset(srcX + emitterRadius, cy + beamOffset),
                        end = Offset(gratingX - 4f, cy + beamOffset),
                        strokeWidth = 2.5f
                    )
                }
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(srcX + emitterRadius, cy),
                    end = Offset(gratingX - 4f, cy),
                    strokeWidth = 6.dp.toPx()
                )

                // 4. Precision Diffraction Grating (Vertical bar with micro-ruling marks)
                val gratingH = 80.dp.toPx()
                val gratingW = 8.dp.toPx()
                drawRect(
                    color = ScienceDarkSurface,
                    topLeft = Offset(gratingX - gratingW * 0.5f, cy - gratingH * 0.5f),
                    size = Size(gratingW, gratingH)
                )
                drawRect(
                    color = AmberVibrant,
                    topLeft = Offset(gratingX - gratingW * 0.5f, cy - gratingH * 0.5f),
                    size = Size(gratingW, gratingH),
                    style = Stroke(1.8f)
                )

                // Grating Ruling Lines Pattern
                val numRulingLines = 14
                val rulingSpacing = gratingH / numRulingLines.toFloat()
                for (r in 0..numRulingLines) {
                    val ry = cy - gratingH * 0.5f + r * rulingSpacing
                    drawLine(
                        color = AmberVibrant.copy(alpha = 0.7f),
                        start = Offset(gratingX - gratingW * 0.5f + 1f, ry),
                        end = Offset(gratingX + gratingW * 0.5f - 1f, ry),
                        strokeWidth = 1f
                    )
                }

                // 5. Diffracted Orders & Rays Projection
                // Zero-order (m = 0): Undeviated central line
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(gratingX + gratingW * 0.5f, cy),
                    end = Offset(screenX, cy),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )

                // Higher orders m = ±1, ±2
                val orders = if (maxOrderToShow == 1) listOf(-1, 1) else listOf(-2, -1, 1, 2)
                for (m in orders) {
                    for (wl in activeLines) {
                        val sinTh = (m * wl) / grooveSpacingNm
                        if (abs(sinTh) <= 0.95f) {
                            val th = asin(sinTh)
                            val rayColor = wavelengthToColor(wl)
                            val maxAngularSpanY = (screenBottom - screenTop) * 0.45f
                            // Physical screen intersection Y relative to center
                            val targetScreenY = cy + tan(th) * (screenX - gratingX) * 0.85f

                            if (targetScreenY in (screenTop - 4f)..(screenBottom + 4f)) {
                                drawLine(
                                    color = rayColor.copy(alpha = 0.75f / abs(m)),
                                    start = Offset(gratingX + gratingW * 0.5f, cy),
                                    end = Offset(screenX, targetScreenY),
                                    strokeWidth = (2.2f / abs(m)).coerceAtLeast(1.2f),
                                    cap = StrokeCap.Round
                                )

                                // Spectral line spot on screen
                                drawRect(
                                    color = rayColor.copy(alpha = 0.95f),
                                    topLeft = Offset(screenX + 1.5f, targetScreenY - 1.5f),
                                    size = Size(screenWidth - 3f, 3f)
                                )
                                drawRect(
                                    color = Color.White.copy(alpha = 0.8f),
                                    topLeft = Offset(screenX + 3f, targetScreenY - 0.8f),
                                    size = Size(screenWidth - 6f, 1.6f)
                                )
                            }
                        }
                    }
                }

                // 6. Detection Phosphor Screen (Right)
                drawRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(screenX, screenTop),
                    size = Size(screenWidth, screenBottom - screenTop)
                )
                drawRect(
                    color = ScienceBorder.copy(alpha = 0.7f),
                    topLeft = Offset(screenX, screenTop),
                    size = Size(screenWidth, screenBottom - screenTop),
                    style = Stroke(1.4f)
                )

                // Central white spot for m = 0
                drawRect(
                    color = Color.White,
                    topLeft = Offset(screenX + 2f, cy - 2.5f),
                    size = Size(screenWidth - 4f, 5f)
                )

                // 7. Interactive Goniometer Probe & Wavelength Reticle
                val probeY = probeRelY * h
                drawLine(
                    color = CyanNeon.copy(alpha = 0.75f),
                    start = Offset(screenX - 25.dp.toPx(), probeY),
                    end = Offset(screenX + screenWidth + 15.dp.toPx(), probeY),
                    strokeWidth = 1.6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                )
                drawCircle(
                    color = CyanNeon,
                    radius = 5.dp.toPx(),
                    center = Offset(screenX + screenWidth * 0.5f, probeY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(screenX + screenWidth * 0.5f, probeY)
                )
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 34: Diffraction Grating Spectrometer",
                items = listOf(
                    "Grating Equation" to "d · sin(θ_m) = m · λ",
                    "Ruling Pitch N" to "${round(linesPerMm)} lines/mm (d = ${round(grooveSpacingUm * 100f) / 100f} μm)",
                    "Principal Wavelength λ" to "${round(wavelengthNm * 10f) / 10f} nm",
                    "Deflection Angle θ_1" to "${round(thetaDeg * 10f) / 10f}° (m = 1)",
                    "Dispersion Rate D" to "${round(angularDispersion * 100f) / 100f} rad/μm",
                    "Resolving Power R" to "$resolvingPower (m = 1)"
                ),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f)
            )
        },
        controlsContent = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ScienceDarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ScienceBorder.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GratingSpectralPreset.values().forEach { preset ->
                            val isSelected = selectedPreset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPreset = preset
                                    linesPerMm = preset.linesPerMm
                                    activeLines = preset.spectralLinesNm
                                },
                                label = {
                                    Text(
                                        text = preset.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                                    selectedLabelColor = CyanNeon,
                                    containerColor = ScienceDarkSurfaceVariant.copy(alpha = 0.6f),
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = ScienceBorder.copy(alpha = 0.35f),
                                    selectedBorderColor = CyanNeon
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    // Mode Toggle: Order m = ±1 vs Order m = ±2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { maxOrderToShow = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (maxOrderToShow == 1) CyanNeon else ScienceDarkSurfaceVariant,
                                contentColor = if (maxOrderToShow == 1) ScienceDarkBg else TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text(
                                text = "🎯 Orders m = 0, ±1",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { maxOrderToShow = 2 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (maxOrderToShow == 2) AmberVibrant else ScienceDarkSurfaceVariant,
                                contentColor = if (maxOrderToShow == 2) ScienceDarkBg else TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text(
                                text = "✨ High Orders m = 0, ±1, ±2",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Sliders Row 1: Lines per mm & Screen Distance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Ruling Density N",
                                value = linesPerMm,
                                range = 300.0f..1200.0f,
                                valueDisplay = "${round(linesPerMm)} l/mm",
                                accentColor = AmberVibrant,
                                onValueChange = {
                                    linesPerMm = it
                                    selectedPreset = null
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Screen Distance L",
                                value = screenDistanceMm,
                                range = 300.0f..800.0f,
                                valueDisplay = "${round(screenDistanceMm)} mm",
                                accentColor = CyanNeon,
                                onValueChange = {
                                    screenDistanceMm = it
                                    selectedPreset = null
                                }
                            )
                        }
                    }

                    // Action Controls: Pause/Run & Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isRunning = !isRunning },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) AmberVibrant else CyanNeon,
                                contentColor = ScienceDarkBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                        ) {
                            Text(
                                text = if (isRunning) "⏸ Pause" else "▶ Run",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                val defaultP = GratingSpectralPreset.HYDROGEN_BALMER
                                selectedPreset = defaultP
                                linesPerMm = defaultP.linesPerMm
                                activeLines = defaultP.spectralLinesNm
                                screenDistanceMm = 500f
                                maxOrderToShow = 2
                                isRunning = true
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(ScienceDarkSurfaceVariant, RoundedCornerShape(8.dp))
                        ) {
                            ResetIcon(tint = CyanNeon, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    )
}
