package com.geosid.simplephysics.ui.experiments.week5.Day33

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
 * Optical presets for Day 33: Polarization & The 3-Filter Paradox.
 */
enum class PolarizationPreset(
    val label: String,
    val theta1Deg: Float,
    val theta2Deg: Float,
    val theta3Deg: Float,
    val filter2Active: Boolean,
    val desc: String
) {
    PARADOX_45(
        label = "45° Paradox",
        theta1Deg = 0.0f,
        theta2Deg = 45.0f,
        theta3Deg = 90.0f,
        filter2Active = true,
        desc = "Inserting a 45° filter between two crossed 90° dark filters restores 12.5% transmitted light"
    ),
    CROSSED_BLACKOUT(
        label = "Crossed Blackout",
        theta1Deg = 0.0f,
        theta2Deg = 45.0f,
        theta3Deg = 90.0f,
        filter2Active = false,
        desc = "Two crossed 90° polarizing sheets block 100% of incident light, resulting in total darkness"
    ),
    PARALLEL_MAX(
        label = "All Parallel",
        theta1Deg = 0.0f,
        theta2Deg = 0.0f,
        theta3Deg = 0.0f,
        filter2Active = true,
        desc = "All filters aligned vertically passing maximum 50% unpolarized input light"
    ),
    SWEPT_MIDDLE(
        label = "60° Oblique",
        theta1Deg = 0.0f,
        theta2Deg = 60.0f,
        theta3Deg = 90.0f,
        filter2Active = true,
        desc = "Middle filter oriented at 60° yielding intermediate projection cos²(60°)·cos²(30°) ≈ 9.4%"
    ),
    QUANTUM_TRIPLE(
        label = "3-Step 30°",
        theta1Deg = 0.0f,
        theta2Deg = 30.0f,
        theta3Deg = 60.0f,
        filter2Active = true,
        desc = "Gradual rotation through 30° steps minimizing projection loss (Zeno quantum turning)"
    )
}

@Composable
fun Polarization3FilterExperiment(
    modifier: Modifier = Modifier
) {
    // Optical & Physical Parameters
    var theta1Deg by remember { mutableStateOf(0.0f) }   // Filter 1 angle (0° = vertical)
    var theta2Deg by remember { mutableStateOf(45.0f) }  // Filter 2 angle
    var theta3Deg by remember { mutableStateOf(90.0f) }  // Filter 3 angle (90° = horizontal)
    var filter2Active by remember { mutableStateOf(true) } // Toggle middle filter presence
    var inputIntensity by remember { mutableStateOf(1.0f) } // Initial source intensity I_0
    var selectedPreset by remember { mutableStateOf<PolarizationPreset?>(PolarizationPreset.PARADOX_45) }
    var isRunning by remember { mutableStateOf(true) }
    var simTime by remember { mutableStateOf(0f) }

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

    // 1. Initial polarization and first interface Malus transmission
    val i1 = 0.5f * inputIntensity
    val deltaTheta12Rad = (theta2Deg - theta1Deg) * PI.toFloat() / 180f
    val i2 = if (filter2Active) i1 * cos(deltaTheta12Rad).pow(2) else i1

    // 2. Sequential Malus projection through analyzer filter 3
    val deltaThetaExitRad = if (filter2Active) {
        (theta3Deg - theta2Deg) * PI.toFloat() / 180f
    } else {
        (theta3Deg - theta1Deg) * PI.toFloat() / 180f
    }
    val i3 = i2 * cos(deltaThetaExitRad).pow(2)

    // 3. Peak 3-filter paradox transmission at 45 degree angle
    val transmittedFraction = i3 / inputIntensity
    val isParadoxActive = filter2Active && abs(theta3Deg - theta1Deg - 90f) < 2f && transmittedFraction > 0.001f
    val eFieldAmp = sqrt(transmittedFraction)

    // Live display metrics
    val i1Percent = i1 * 100f
    val i2Percent = i2 * 100f
    val i3Percent = i3 * 100f
    val eFieldPercent = eFieldAmp * 100f

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag Filter 2 dial to rotate its polarization axis. Toggle 'Remove Filter' to observe the quantum 3-filter paradox!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val pos = change.position
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val f2X = w * 0.50f
                            val cy = h * 0.40f

                            // If dragging near Filter 2, adjust its angle
                            val dx = pos.x - f2X
                            val dy = pos.y - cy
                            if (hypot(dx, dy) < 140.dp.toPx()) {
                                var angle = atan2(dy, dx) * 180f / PI.toFloat() + 90f
                                if (angle < 0f) angle += 180f
                                if (angle >= 180f) angle -= 180f
                                theta2Deg = round(angle.coerceIn(0f, 180f) * 10f) / 10f
                                selectedPreset = null
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val f2X = w * 0.50f
                            val cy = h * 0.40f
                            val dx = pos.x - f2X
                            val dy = pos.y - cy
                            if (hypot(dx, dy) < 70.dp.toPx()) {
                                // Tap middle filter to toggle presence
                                filter2Active = !filter2Active
                                selectedPreset = null
                            }
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

                // 2. Optical Rail Axis (Elevated origin per AGY rules)
                val cy = h * 0.40f
                val railY = cy + 65.dp.toPx()
                drawLine(
                    color = ScienceBorder.copy(alpha = 0.6f),
                    start = Offset(w * 0.05f, railY),
                    end = Offset(w * 0.95f, railY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Optical axis central line
                drawLine(
                    color = TextMuted.copy(alpha = 0.35f),
                    start = Offset(w * 0.05f, cy),
                    end = Offset(w * 0.95f, cy),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // 3. Station Positions along the Rail
                val srcX = w * 0.08f
                val f1X = w * 0.26f
                val f2X = w * 0.50f
                val f3X = w * 0.74f
                val detX = w * 0.90f
                val filterRadius = min(w * 0.085f, 44.dp.toPx())

                // 4. Light Source Emitter (Left)
                drawCircle(ScienceDarkSurfaceVariant, 18.dp.toPx(), Offset(srcX, cy))
                drawCircle(Color.White.copy(alpha = 0.9f), 10.dp.toPx(), Offset(srcX, cy))
                drawCircle(AmberVibrant, 18.dp.toPx(), Offset(srcX, cy), style = Stroke(2.dp.toPx()))

                // Radiating unpolarized arrows at source
                val numSrcSpokes = 8
                for (s in 0 until numSrcSpokes) {
                    val spokeAngle = (s * (PI.toFloat() / 4f)) + (simTime * 2f)
                    val sLen = 26.dp.toPx()
                    drawLine(
                        color = AmberVibrant.copy(alpha = 0.7f),
                        start = Offset(srcX, cy),
                        end = Offset(srcX + cos(spokeAngle) * sLen, cy + sin(spokeAngle) * sLen),
                        strokeWidth = 1.4f
                    )
                }

                // 5. Draw Traveling Electromagnetic Wave Sections
                // Section 0: Source -> Filter 1 (Unpolarized beam)
                val beam0Color = AmberVibrant.copy(alpha = 0.65f)
                drawLine(
                    color = beam0Color.copy(alpha = 0.25f),
                    start = Offset(srcX + 18.dp.toPx(), cy),
                    end = Offset(f1X - filterRadius, cy),
                    strokeWidth = 10.dp.toPx()
                )
                drawLine(
                    color = beam0Color,
                    start = Offset(srcX + 18.dp.toPx(), cy),
                    end = Offset(f1X - filterRadius, cy),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Section 1: Filter 1 -> Filter 2 (Polarized along theta1)
                val beam1Width = (3.dp.toPx() * (i1 / inputIntensity)).coerceAtLeast(1.5f)
                val beam1Color = CyanNeon
                val s1Start = Offset(f1X + filterRadius, cy)
                val s1End = Offset(if (filter2Active) f2X - filterRadius else f3X - filterRadius, cy)
                drawLine(
                    color = beam1Color.copy(alpha = 0.20f),
                    start = s1Start,
                    end = s1End,
                    strokeWidth = 8.dp.toPx()
                )
                drawLine(
                    color = beam1Color.copy(alpha = 0.85f),
                    start = s1Start,
                    end = s1End,
                    strokeWidth = beam1Width,
                    cap = StrokeCap.Round
                )

                // Sine wave oscillation along theta1
                val steps1 = 28
                val path1 = Path()
                for (i in 0..steps1) {
                    val t = i / steps1.toFloat()
                    val px = s1Start.x + (s1End.x - s1Start.x) * t
                    val waveAmp = 12.dp.toPx() * sin(simTime * 10f - t * 4 * PI.toFloat())
                    val theta1Rad = theta1Deg * PI.toFloat() / 180f
                    val py = cy - waveAmp * cos(theta1Rad)
                    if (i == 0) path1.moveTo(px, py) else path1.lineTo(px, py)
                }
                drawPath(path1, beam1Color.copy(alpha = 0.6f), style = Stroke(1.8f))

                // Section 2: Filter 2 -> Filter 3 (If Filter 2 active)
                if (filter2Active) {
                    val s2Start = Offset(f2X + filterRadius, cy)
                    val s2End = Offset(f3X - filterRadius, cy)
                    val beam2Color = PurpleNeon
                    val beam2Width = (3.dp.toPx() * (i2 / inputIntensity)).coerceAtLeast(1.2f)
                    if (i2 > 0.005f) {
                        drawLine(
                            color = beam2Color.copy(alpha = 0.20f),
                            start = s2Start,
                            end = s2End,
                            strokeWidth = 8.dp.toPx()
                        )
                        drawLine(
                            color = beam2Color.copy(alpha = 0.85f),
                            start = s2Start,
                            end = s2End,
                            strokeWidth = beam2Width,
                            cap = StrokeCap.Round
                        )
                        val steps2 = 28
                        val path2 = Path()
                        for (i in 0..steps2) {
                            val t = i / steps2.toFloat()
                            val px = s2Start.x + (s2End.x - s2Start.x) * t
                            val waveAmp = 12.dp.toPx() * (i2 / inputIntensity).coerceAtMost(1f) * sin(simTime * 10f - t * 4 * PI.toFloat())
                            val theta2Rad = theta2Deg * PI.toFloat() / 180f
                            val py = cy - waveAmp * cos(theta2Rad)
                            if (i == 0) path2.moveTo(px, py) else path2.lineTo(px, py)
                        }
                        drawPath(path2, beam2Color.copy(alpha = 0.7f), style = Stroke(1.8f))
                    }
                }

                // Section 3: Filter 3 -> Detection Screen
                val s3Start = Offset(f3X + filterRadius, cy)
                val s3End = Offset(detX, cy)
                val beam3Color = if (isParadoxActive) EmeraldNeon else CyanNeon
                if (i3 > 0.002f) {
                    val finalBeamWidth = (4.dp.toPx() * (i3 / inputIntensity) * 4f).coerceIn(1.5f, 10.dp.toPx())
                    drawLine(
                        color = beam3Color.copy(alpha = 0.35f),
                        start = s3Start,
                        end = s3End,
                        strokeWidth = finalBeamWidth * 2f
                    )
                    drawLine(
                        color = beam3Color,
                        start = s3Start,
                        end = s3End,
                        strokeWidth = finalBeamWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.9f),
                        start = s3Start,
                        end = s3End,
                        strokeWidth = 1.2f
                    )
                } else {
                    // Dark blocked ray (Total Blackout indicator)
                    drawLine(
                        color = CoralNeon.copy(alpha = 0.4f),
                        start = s3Start,
                        end = s3End,
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
                    )
                }

                // 6. Draw Polarizer Filters (Disks with Transmission Slits)
                fun drawPolarizerFilter(
                    centerX: Float,
                    angleDeg: Float,
                    label: String,
                    accentColor: Color,
                    isActive: Boolean
                ) {
                    val alpha = if (isActive) 1.0f else 0.25f
                    val center = Offset(centerX, cy)

                    // Mount Stand
                    drawLine(
                        color = ScienceDarkSurfaceVariant,
                        start = Offset(centerX, cy + filterRadius),
                        end = Offset(centerX, railY),
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = ScienceBorder.copy(alpha = alpha),
                        start = Offset(centerX, cy + filterRadius),
                        end = Offset(centerX, railY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Outer Rim Housing
                    drawCircle(
                        color = ScienceDarkSurface,
                        radius = filterRadius,
                        center = center
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = 0.15f * alpha),
                        radius = filterRadius,
                        center = center
                    )
                    drawCircle(
                        color = if (isActive) accentColor else TextMuted.copy(alpha = 0.4f),
                        radius = filterRadius,
                        center = center,
                        style = Stroke(width = 2.2f)
                    )

                    // Polarizing Micro-Grating Slits (Oriented along angleDeg)
                    if (isActive) {
                        val angleRad = angleDeg * PI.toFloat() / 180f
                        val ux = -sin(angleRad)
                        val uy = cos(angleRad)
                        val numLines = 7
                        val lineSpacing = filterRadius * 0.26f

                        for (step in -(numLines / 2)..(numLines / 2)) {
                            val offsetDist = step * lineSpacing
                            val perpX = cos(angleRad) * offsetDist
                            val perpY = sin(angleRad) * offsetDist

                            val halfChord = sqrt((filterRadius * filterRadius - offsetDist * offsetDist).coerceAtLeast(0f)) * 0.88f
                            val p1 = Offset(center.x + perpX - ux * halfChord, center.y + perpY - uy * halfChord)
                            val p2 = Offset(center.x + perpX + ux * halfChord, center.y + perpY + uy * halfChord)

                            drawLine(
                                color = accentColor.copy(alpha = 0.5f),
                                start = p1,
                                end = p2,
                                strokeWidth = 1.4f
                            )
                        }

                        // Central transmission indicator needle
                        val needleLen = filterRadius * 0.95f
                        drawLine(
                            color = Color.White.copy(alpha = 0.85f),
                            start = Offset(center.x - ux * needleLen, center.y - uy * needleLen),
                            end = Offset(center.x + ux * needleLen, center.y + uy * needleLen),
                            strokeWidth = 2.4f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Dial Angle Marker Top Dot
                    val dotAngleRad = angleDeg * PI.toFloat() / 180f
                    val dotX = center.x - sin(dotAngleRad) * filterRadius
                    val dotY = center.y + cos(dotAngleRad) * filterRadius
                    drawCircle(
                        color = if (isActive) accentColor else TextMuted,
                        radius = 3.5.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }

                // Draw Filter 1 (Initial Polarizer, Cyan)
                drawPolarizerFilter(f1X, theta1Deg, "Polarizer 1", CyanNeon, true)

                // Draw Filter 2 (Middle Paradox Filter, Purple)
                drawPolarizerFilter(f2X, theta2Deg, "Middle Filter 2", PurpleNeon, filter2Active)

                // Filter 2 Drag Ring if active
                if (filter2Active) {
                    val ringPulse = (sin(simTime * 4f) * 0.5f + 0.5f) * 4.dp.toPx()
                    drawCircle(
                        color = PurpleNeon.copy(alpha = 0.35f),
                        radius = filterRadius + 6.dp.toPx() + ringPulse,
                        center = Offset(f2X, cy),
                        style = Stroke(1.4f)
                    )
                } else {
                    // "Bypassed" badge marker over Filter 2
                    drawLine(
                        color = CoralNeon.copy(alpha = 0.7f),
                        start = Offset(f2X - filterRadius * 0.6f, cy - filterRadius * 0.6f),
                        end = Offset(f2X + filterRadius * 0.6f, cy + filterRadius * 0.6f),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = CoralNeon.copy(alpha = 0.7f),
                        start = Offset(f2X - filterRadius * 0.6f, cy + filterRadius * 0.6f),
                        end = Offset(f2X + filterRadius * 0.6f, cy - filterRadius * 0.6f),
                        strokeWidth = 2.5f
                    )
                }

                // Draw Filter 3 (Analyzer, Amber)
                drawPolarizerFilter(f3X, theta3Deg, "Analyzer 3", AmberVibrant, true)

                // 7. Output Photodetector / Phosphor Screen (Far Right)
                val screenW = 14.dp.toPx()
                val screenH = 68.dp.toPx()
                drawRect(
                    color = ScienceDarkSurfaceVariant,
                    topLeft = Offset(detX, cy - screenH * 0.5f),
                    size = Size(screenW, screenH)
                )
                drawRect(
                    color = ScienceBorder,
                    topLeft = Offset(detX, cy - screenH * 0.5f),
                    size = Size(screenW, screenH),
                    style = Stroke(1.5f)
                )

                // Screen illuminated spot
                val spotRadius = (screenW * 0.45f).coerceAtLeast(3f)
                val spotAlpha = (i3 / inputIntensity * 3.5f).coerceIn(0.05f, 1.0f)
                if (i3 > 0.001f) {
                    drawCircle(
                        color = (if (isParadoxActive) EmeraldNeon else CyanNeon).copy(alpha = spotAlpha),
                        radius = spotRadius * 2.2f,
                        center = Offset(detX + screenW * 0.5f, cy)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = spotAlpha),
                        radius = spotRadius,
                        center = Offset(detX + screenW * 0.5f, cy)
                    )
                } else {
                    // Blackout indicator
                    drawCircle(
                        color = CoralNeon.copy(alpha = 0.4f),
                        radius = 4.dp.toPx(),
                        center = Offset(detX + screenW * 0.5f, cy)
                    )
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 33: Polarization & The 3-Filter Paradox",
                items = listOf(
                    "Malus's Law" to "I = I_0 · cos²(θ_1) · cos²(θ_2)",
                    "Source Input I_0" to "${round(inputIntensity * 100f)}%",
                    "After Filter 1 (θ_1)" to "${round(i1Percent * 10f) / 10f}% (${round(theta1Deg)}°)",
                    "After Filter 2 (θ_2)" to if (filter2Active) "${round(i2Percent * 10f) / 10f}% (${round(theta2Deg)}°)" else "REMOVED (Bypassed)",
                    "Transmitted Output I_3" to "${round(i3Percent * 10f) / 10f}% (${round(theta3Deg)}°)",
                    "Paradox Status" to if (isParadoxActive) "LIGHT REAPPEARS (+${round(i3Percent * 10f) / 10f}%)" else if (i3Percent < 0.1f) "CROSSED BLACKOUT (0.0%)" else "TRANSMITTING"
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
                        PolarizationPreset.values().forEach { preset ->
                            val isSelected = selectedPreset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPreset = preset
                                    theta1Deg = preset.theta1Deg
                                    theta2Deg = preset.theta2Deg
                                    theta3Deg = preset.theta3Deg
                                    filter2Active = preset.filter2Active
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

                    // Mode Toggle: Insert Middle Filter vs Remove Middle Filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                filter2Active = true
                                selectedPreset = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (filter2Active) PurpleNeon else ScienceDarkSurfaceVariant,
                                contentColor = if (filter2Active) ScienceDarkBg else TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text(
                                text = "✨ Insert Middle Filter 2",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                filter2Active = false
                                selectedPreset = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!filter2Active) CoralNeon else ScienceDarkSurfaceVariant,
                                contentColor = if (!filter2Active) ScienceDarkBg else TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text(
                                text = "🚫 Remove Filter 2 (Crossed)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Sliders Row 1: Filter 1 Angle & Filter 3 Angle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Polarizer 1 (θ_1)",
                                value = theta1Deg,
                                range = 0.0f..180.0f,
                                valueDisplay = "${round(theta1Deg)}°",
                                accentColor = CyanNeon,
                                onValueChange = {
                                    theta1Deg = it
                                    selectedPreset = null
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Analyzer 3 (θ_3)",
                                value = theta3Deg,
                                range = 0.0f..180.0f,
                                valueDisplay = "${round(theta3Deg)}°",
                                accentColor = AmberVibrant,
                                onValueChange = {
                                    theta3Deg = it
                                    selectedPreset = null
                                }
                            )
                        }
                    }

                    // Sliders Row 2: Middle Filter 2 Angle & Source Intensity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Middle Filter 2 (θ_2)",
                                value = theta2Deg,
                                range = 0.0f..180.0f,
                                valueDisplay = if (filter2Active) "${round(theta2Deg)}°" else "Bypassed",
                                accentColor = PurpleNeon,
                                onValueChange = {
                                    theta2Deg = it
                                    filter2Active = true
                                    selectedPreset = null
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Source Power I_0",
                                value = inputIntensity,
                                range = 0.2f..1.0f,
                                valueDisplay = "${round(inputIntensity * 100f)}%",
                                accentColor = EmeraldNeon,
                                onValueChange = {
                                    inputIntensity = it
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
                                val defaultP = PolarizationPreset.PARADOX_45
                                selectedPreset = defaultP
                                theta1Deg = defaultP.theta1Deg
                                theta2Deg = defaultP.theta2Deg
                                theta3Deg = defaultP.theta3Deg
                                filter2Active = defaultP.filter2Active
                                inputIntensity = 1.0f
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
