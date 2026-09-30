package com.geosid.simplephysics.ui.experiments.week2.Day11

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.ui.components.ExperimentHudCard
import com.geosid.simplephysics.ui.components.PhysicsSliderControl
import com.geosid.simplephysics.ui.components.ResetIcon
import com.geosid.simplephysics.ui.components.ResponsiveExperimentContainer
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*

enum class BrachistochroneMode(val title: String, val icon: String) {
    RACE("3-Track Race", "🏁"),
    TAUTOCHRONE("Tautochrone Demo", "⏱️")
}

@Composable
fun BrachistochroneExperiment(
    modifier: Modifier = Modifier
) {
    // Mode & Environment Settings
    var mode by remember { mutableStateOf(BrachistochroneMode.RACE) }
    var gravity by remember { mutableStateOf(9.81f) }

    // Simulation Engine State
    var isRacing by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var raceTime by remember { mutableStateOf(0f) }

    // Progress along track (0.0 to 1.0)
    var progCycloid by remember { mutableStateOf(0f) }
    var progStraight by remember { mutableStateOf(0f) }
    var progArc by remember { mutableStateOf(0f) }

    // Velocities (m/s)
    var speedCycloid by remember { mutableStateOf(0f) }
    var speedStraight by remember { mutableStateOf(0f) }
    var speedArc by remember { mutableStateOf(0f) }

    // Finish Times (seconds)
    var finishTimeCycloid by remember { mutableStateOf<Float?>(null) }
    var finishTimeStraight by remember { mutableStateOf<Float?>(null) }
    var finishTimeArc by remember { mutableStateOf<Float?>(null) }

    // Tautochrone 3-Ball Progresses (different start heights on cycloid)
    var progTauto1 by remember { mutableStateOf(0.70f) } // starts near bottom (70% along)
    var progTauto2 by remember { mutableStateOf(0.35f) } // starts midway (35% along)
    var progTauto3 by remember { mutableStateOf(0.00f) } // starts at very top (0% along)
    var tautoArrivalConfirmed by remember { mutableStateOf(false) }

    // Physics Simulation Loop (60-120 FPS frame ticker)
    LaunchedEffect(isRacing, isPaused, gravity, mode) {
        if (isRacing && !isPaused) {
            var lastNanos = withFrameNanos { it }
            while (isRacing) {
                val currentNanos = withFrameNanos { it }
                val dt = ((currentNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastNanos = currentNanos

                raceTime += dt

                // Real physics integration for standard race mode
                if (mode == BrachistochroneMode.RACE) {
                    // Track dimensions scale (effective drop H = 10m, length L = 16m)
                    val dropH = 10f
                    val lengthL = 15.7f
                    val rampLen = sqrt(dropH * dropH + lengthL * lengthL)

                    // 1. Cycloid Ball
                    if (progCycloid < 1f) {
                        // Cycloid parametric: theta in [0, pi]
                        val theta = progCycloid * PI.toFloat()
                        // Depth y = dropH * 0.5 * (1 - cos(theta))
                        val depthY = dropH * 0.5f * (1f - cos(theta))
                        speedCycloid = sqrt(max(0.05f, 2f * gravity * depthY))
                        // Arc length ds/dtheta = 2R sin(theta/2) -> ds = 4R = 2 * dropH
                        val totalArcLength = dropH * 2f
                        val dProg = (speedCycloid / totalArcLength) * dt
                        progCycloid = min(1f, progCycloid + max(dProg, 0.25f * dt))
                        if (progCycloid >= 1f && finishTimeCycloid == null) {
                            finishTimeCycloid = raceTime
                        }
                    }

                    // 2. Straight Ramp Ball
                    if (progStraight < 1f) {
                        // Constant acceleration down incline: a = g * sin(alpha) = g * (H / sqrt(H^2 + L^2))
                        val accel = gravity * (dropH / rampLen)
                        speedStraight += accel * dt
                        progStraight = min(1f, progStraight + (speedStraight / rampLen) * dt)
                        if (progStraight >= 1f && finishTimeStraight == null) {
                            finishTimeStraight = raceTime
                        }
                    }

                    // 3. Circular Arc Ball
                    if (progArc < 1f) {
                        // Circular arc dips down moderately fast
                        val depthY = dropH * sin(progArc * (PI.toFloat() / 2f))
                        speedArc = sqrt(max(0.05f, 2f * gravity * depthY))
                        val arcLen = rampLen * 1.08f
                        val dProg = (speedArc / arcLen) * dt
                        progArc = min(1f, progArc + max(dProg, 0.2f * dt))
                        if (progArc >= 1f && finishTimeArc == null) {
                            finishTimeArc = raceTime
                        }
                    }

                    if (progCycloid >= 1f && progStraight >= 1f && progArc >= 1f) {
                        isRacing = false
                    }
                } else {
                    // Tautochrone mode: 3 balls released from 3 heights on cycloid
                    val dropH = 10f
                    val totalArcLength = dropH * 2f

                    if (progTauto1 < 1f) {
                        val theta = progTauto1 * PI.toFloat()
                        val depth = dropH * 0.5f * (1f - cos(theta)) - dropH * 0.5f * (1f - cos(0.70f * PI.toFloat()))
                        val v = sqrt(max(0.04f, 2f * gravity * max(0f, depth)))
                        progTauto1 = min(1f, progTauto1 + max((v / totalArcLength) * dt, 0.35f * dt))
                    }
                    if (progTauto2 < 1f) {
                        val theta = progTauto2 * PI.toFloat()
                        val depth = dropH * 0.5f * (1f - cos(theta)) - dropH * 0.5f * (1f - cos(0.35f * PI.toFloat()))
                        val v = sqrt(max(0.04f, 2f * gravity * max(0f, depth)))
                        progTauto2 = min(1f, progTauto2 + max((v / totalArcLength) * dt, 0.35f * dt))
                    }
                    if (progTauto3 < 1f) {
                        val theta = progTauto3 * PI.toFloat()
                        val depth = dropH * 0.5f * (1f - cos(theta))
                        val v = sqrt(max(0.04f, 2f * gravity * max(0f, depth)))
                        progTauto3 = min(1f, progTauto3 + max((v / totalArcLength) * dt, 0.35f * dt))
                    }

                    if (progTauto1 >= 1f && progTauto2 >= 1f && progTauto3 >= 1f) {
                        tautoArrivalConfirmed = true
                        isRacing = false
                    }
                }
            }
        }
    }

    // UI Container
    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = if (mode == BrachistochroneMode.RACE)
            "👉 Press 'Release & Race' to watch Johann Bernoulli's 1696 contest: The Cycloid beats the straight line!"
        else
            "👉 Tautochrone property: Balls released from different heights on a cycloid reach the bottom at the exact same instant!",
        canvasContent = {
            // Main Track Simulation Viewport - Clean, spacious, and completely unobstructed
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Tap canvas to quickly start or reset race
                            if (!isRacing && progCycloid >= 1f) {
                                progCycloid = 0f
                                progStraight = 0f
                                progArc = 0f
                                speedCycloid = 0f
                                speedStraight = 0f
                                speedArc = 0f
                                finishTimeCycloid = null
                                finishTimeStraight = null
                                finishTimeArc = null
                                raceTime = 0f
                            }
                            isRacing = !isRacing
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated Canvas Origin: Start at 0.14h, finish at 0.58h (leaving bottom 42% clear for controls)
                val startX = w * 0.10f
                val startY = h * 0.14f
                val endX = w * 0.90f
                val endY = h * 0.58f
                val trackW = endX - startX
                val trackH = endY - startY

                // Draw Background Atmospheric Grid
                drawBrachistochroneBackdrop(w, h)

                if (mode == BrachistochroneMode.RACE) {
                    // Draw 3 Race Tracks: Straight Line (Amber), Circular Arc (Purple), Cycloid (Cyan)
                    drawTrackStraightLine(startX, startY, endX, endY)
                    drawTrackCircularArc(startX, startY, trackW, trackH)
                    drawTrackCycloid(startX, startY, trackW, trackH)

                    // Draw Start Gate & Finish Gate
                    drawStartAndFinishGates(startX, startY, endX, endY)

                    // Draw Rolling Balls along each track
                    // 1. Straight Line Ball (Amber)
                    val straightPos = Offset(startX + progStraight * trackW, startY + progStraight * trackH)
                    drawRollingMarble(straightPos, AmberVibrant, "Straight")

                    // 2. Circular Arc Ball (Purple)
                    val arcX = startX + progArc * trackW
                    val arcY = startY + trackH * sin(progArc * (PI.toFloat() / 2f))
                    drawRollingMarble(Offset(arcX, arcY), PurpleNeon, "Arc")

                    // 3. Cycloid Ball (Cyan)
                    val thetaCyc = progCycloid * PI.toFloat()
                    val cycX = startX + (trackW / PI.toFloat()) * (thetaCyc - sin(thetaCyc))
                    val cycY = startY + (trackH * 0.5f) * (1f - cos(thetaCyc))
                    drawRollingMarble(Offset(cycX, cycY), CyanNeon, "Cycloid 🏆")

                    // Finish Line Stopwatches / Badges
                    drawFinishTimeTags(
                        endX = endX,
                        endY = endY,
                        tCyc = finishTimeCycloid,
                        tArc = finishTimeArc,
                        tStraight = finishTimeStraight
                    )
                } else {
                    // TAUTOCHRONE MODE: Single Cycloid Track with 3 Balls released at different heights
                    drawTrackCycloid(startX, startY, trackW, trackH, isHighlighted = true)
                    drawStartAndFinishGates(startX, startY, endX, endY)

                    // Ball 1 (Green/Cyan)
                    val th1 = progTauto1 * PI.toFloat()
                    val x1 = startX + (trackW / PI.toFloat()) * (th1 - sin(th1))
                    val y1 = startY + (trackH * 0.5f) * (1f - cos(th1))
                    drawRollingMarble(Offset(x1, y1), EmeraldNeon, "Low Start")

                    // Ball 2 (Amber)
                    val th2 = progTauto2 * PI.toFloat()
                    val x2 = startX + (trackW / PI.toFloat()) * (th2 - sin(th2))
                    val y2 = startY + (trackH * 0.5f) * (1f - cos(th2))
                    drawRollingMarble(Offset(x2, y2), AmberVibrant, "Mid Start")

                    // Ball 3 (Coral)
                    val th3 = progTauto3 * PI.toFloat()
                    val x3 = startX + (trackW / PI.toFloat()) * (th3 - sin(th3))
                    val y3 = startY + (trackH * 0.5f) * (1f - cos(th3))
                    drawRollingMarble(Offset(x3, y3), CoralNeon, "High Start")
                }
            }
        },
        hudContent = {
            val cycText = finishTimeCycloid?.let { "${round(it * 100f) / 100f}s 🥇 (Winner!)" }
                ?: if (isRacing) "${round(raceTime * 100f) / 100f}s (Racing)" else "Ready"
            val arcText = finishTimeArc?.let { "${round(it * 100f) / 100f}s (2nd)" }
                ?: if (isRacing) "${round(raceTime * 100f) / 100f}s" else "Ready"
            val straightText = finishTimeStraight?.let { "${round(it * 100f) / 100f}s (3rd)" }
                ?: if (isRacing) "${round(raceTime * 100f) / 100f}s" else "Ready"

            val timeSavedPct = if (finishTimeCycloid != null && finishTimeStraight != null && finishTimeStraight!! > 0.01f) {
                val saved = ((finishTimeStraight!! - finishTimeCycloid!!) / finishTimeStraight!!) * 100f
                "${round(saved * 10f) / 10f}% Faster than Straight Line"
            } else {
                "Calculating..."
            }

            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = if (mode == BrachistochroneMode.RACE) "Brachistochrone Descent Race" else "Tautochrone Isochrone Property",
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f),
                items = if (mode == BrachistochroneMode.RACE) {
                    listOf(
                        "Governing Principle" to "t = ∫ ds / √(2gy) ⇒ Cycloid Minimum",
                        "Cycloid Track (Cyan)" to cycText,
                        "Circular Arc (Purple)" to arcText,
                        "Straight Line (Amber)" to straightText,
                        "Cycloid Advantage" to timeSavedPct,
                        "Gravity (g)" to "${round(gravity * 10f) / 10f} m/s²"
                    )
                } else {
                    listOf(
                        "Tautochrone Law" to "T = π √(R / g) (Invariant to release height)",
                        "Low Drop Ball (Emerald)" to if (progTauto1 >= 1f) "Arrived at bottom" else "${round(progTauto1 * 100).toInt()}%",
                        "Mid Drop Ball (Amber)" to if (progTauto2 >= 1f) "Arrived at bottom" else "${round(progTauto2 * 100).toInt()}%",
                        "High Drop Ball (Coral)" to if (progTauto3 >= 1f) "Arrived at bottom" else "${round(progTauto3 * 100).toInt()}%",
                        "Isochrone Result" to if (tautoArrivalConfirmed) "Simultaneous Arrival! ✨" else "Racing...",
                        "Gravity (g)" to "${round(gravity * 10f) / 10f} m/s²"
                    )
                }
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Mode Selector (Compact 32.dp buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrachistochroneMode.entries.forEach { m ->
                        val isSelected = mode == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyanNeon.copy(alpha = 0.25f) else ScienceDarkSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) CyanNeon else ScienceBorder.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    mode = m
                                    isRacing = false
                                    progCycloid = 0f
                                    progStraight = 0f
                                    progArc = 0f
                                    speedCycloid = 0f
                                    speedStraight = 0f
                                    speedArc = 0f
                                    finishTimeCycloid = null
                                    finishTimeStraight = null
                                    finishTimeArc = null
                                    progTauto1 = 0.70f
                                    progTauto2 = 0.35f
                                    progTauto3 = 0.00f
                                    tautoArrivalConfirmed = false
                                    raceTime = 0f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${m.icon} ${m.title}",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyanNeon else TextPrimary
                            )
                        }
                    }
                }

                // 2. Gravity Environment Presets (Compact 32.dp buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "Earth (9.8)" to 9.81f,
                        "Moon (1.6)" to 1.62f,
                        "Mars (3.7)" to 3.71f
                    ).forEach { (name, gVal) ->
                        val isSel = abs(gravity - gVal) < 0.1f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) AmberVibrant.copy(alpha = 0.25f) else ScienceDarkSurface)
                                .border(
                                    1.dp,
                                    if (isSel) AmberVibrant else ScienceBorder.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { gravity = gVal },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) AmberVibrant else TextPrimary
                            )
                        }
                    }
                }

                // 3. Gravity Fine-Tuning Slider
                PhysicsSliderControl(
                    title = "Gravitational Acceleration (g)",
                    value = gravity,
                    range = 1.0f..25.0f,
                    valueDisplay = "${round(gravity * 10f) / 10f} m/s²",
                    accentColor = AmberVibrant,
                    onValueChange = { gravity = it }
                )

                // 4. Action Buttons Row: Release & Race, Pause/Resume, Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (!isRacing && (progCycloid >= 1f || tautoArrivalConfirmed)) {
                                progCycloid = 0f
                                progStraight = 0f
                                progArc = 0f
                                speedCycloid = 0f
                                speedStraight = 0f
                                speedArc = 0f
                                finishTimeCycloid = null
                                finishTimeStraight = null
                                finishTimeArc = null
                                progTauto1 = 0.70f
                                progTauto2 = 0.35f
                                progTauto3 = 0.00f
                                tautoArrivalConfirmed = false
                                raceTime = 0f
                            }
                            isRacing = true
                            isPaused = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isRacing) "🏁 Racing..." else "🏁 Release & Race",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { isPaused = !isPaused },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) EmeraldNeon else AmberVibrant,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isPaused) "▶ Resume" else "⏸ Pause",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            isRacing = false
                            isPaused = false
                            progCycloid = 0f
                            progStraight = 0f
                            progArc = 0f
                            speedCycloid = 0f
                            speedStraight = 0f
                            speedArc = 0f
                            finishTimeCycloid = null
                            finishTimeStraight = null
                            finishTimeArc = null
                            progTauto1 = 0.70f
                            progTauto2 = 0.35f
                            progTauto3 = 0.00f
                            tautoArrivalConfirmed = false
                            raceTime = 0f
                            gravity = 9.81f
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
    )
}

// ---------------------------------------------------------------------------
// High-Fidelity Canvas Drawing Functions for Brachistochrone Tracks
// ---------------------------------------------------------------------------

private fun DrawScope.drawBrachistochroneBackdrop(w: Float, h: Float) {
    // Subtle science coordinate grid
    val gridStep = 45.dp.toPx()
    var x = 0f
    while (x < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.18f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 0.8f
        )
        x += gridStep
    }
    var y = 0f
    while (y < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.18f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 0.8f
        )
        y += gridStep
    }
}

private fun DrawScope.drawTrackStraightLine(startX: Float, startY: Float, endX: Float, endY: Float) {
    // Straight Ramp Track (Amber)
    drawLine(
        color = AmberVibrant.copy(alpha = 0.25f),
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 8.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = AmberVibrant,
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawTrackCircularArc(startX: Float, startY: Float, trackW: Float, trackH: Float) {
    // Circular Arc Track (Purple)
    val steps = 60
    val path = Path()
    for (i in 0..steps) {
        val t = i / steps.toFloat()
        val x = startX + t * trackW
        val y = startY + trackH * sin(t * (PI.toFloat() / 2f))
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(
        path = path,
        color = PurpleNeon.copy(alpha = 0.25f),
        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
    )
    drawPath(
        path = path,
        color = PurpleNeon,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawTrackCycloid(
    startX: Float,
    startY: Float,
    trackW: Float,
    trackH: Float,
    isHighlighted: Boolean = false
) {
    // The Mathematically Proven Cycloid Curve (Cyan)
    val steps = 80
    val path = Path()
    for (i in 0..steps) {
        val t = i / steps.toFloat()
        val theta = t * PI.toFloat()
        val x = startX + (trackW / PI.toFloat()) * (theta - sin(theta))
        val y = startY + (trackH * 0.5f) * (1f - cos(theta))
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    // Outer Glow
    drawPath(
        path = path,
        color = CyanNeon.copy(alpha = if (isHighlighted) 0.40f else 0.25f),
        style = Stroke(width = if (isHighlighted) 12.dp.toPx() else 8.dp.toPx(), cap = StrokeCap.Round)
    )
    // Core Track
    drawPath(
        path = path,
        color = CyanNeon,
        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawStartAndFinishGates(startX: Float, startY: Float, endX: Float, endY: Float) {
    // Start Platform Marker (Point A)
    drawCircle(Color.White, 7.dp.toPx(), Offset(startX, startY))
    drawCircle(CyanNeon, 12.dp.toPx(), Offset(startX, startY), style = Stroke(2.dp.toPx()))

    // Finish Gate Post & Checkered Flag Line (Point B)
    drawLine(
        color = Color.White,
        start = Offset(endX, endY - 25.dp.toPx()),
        end = Offset(endX, endY + 10.dp.toPx()),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Triangular Checkered Finish Flag
    val flagPath = Path().apply {
        moveTo(endX, endY - 25.dp.toPx())
        lineTo(endX + 16.dp.toPx(), endY - 17.dp.toPx())
        lineTo(endX, endY - 9.dp.toPx())
        close()
    }
    drawPath(path = flagPath, color = EmeraldNeon)
}

private fun DrawScope.drawRollingMarble(pos: Offset, color: Color, label: String) {
    val r = 9.dp.toPx()

    // Outer Glow
    drawCircle(
        color = color.copy(alpha = 0.35f),
        radius = r * 1.8f,
        center = pos
    )
    // 3D Sphere Body with specular highlight
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, color, color.copy(alpha = 0.8f)),
            center = Offset(pos.x - r * 0.3f, pos.y - r * 0.3f),
            radius = r
        ),
        radius = r,
        center = pos
    )
    // Rim
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = r,
        center = pos,
        style = Stroke(1.5f)
    )
}

private fun DrawScope.drawFinishTimeTags(
    endX: Float,
    endY: Float,
    tCyc: Float?,
    tArc: Float?,
    tStraight: Float?
) {
    // If cycloid has crossed finish line, display finish winner badge
    if (tCyc != null) {
        drawCircle(
            color = EmeraldNeon.copy(alpha = 0.25f),
            radius = 24.dp.toPx(),
            center = Offset(endX, endY)
        )
        drawCircle(
            color = EmeraldNeon,
            radius = 24.dp.toPx(),
            center = Offset(endX, endY),
            style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
        )
    }
}

