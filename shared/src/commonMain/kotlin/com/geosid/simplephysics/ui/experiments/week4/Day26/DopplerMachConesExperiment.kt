package com.geosid.simplephysics.ui.experiments.week4.Day26

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
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

enum class MachPreset(val label: String, val mach: Float, val freq: Float) {
    SUBSONIC("Subsonic (M=0.65)", 0.65f, 3.0f),
    TRANSONIC("Mach 1 Barrier", 1.00f, 3.2f),
    SUPERSONIC("Supersonic (M=1.45)", 1.45f, 3.8f),
    HYPERSONIC("Hypersonic (M=2.20)", 2.20f, 4.2f)
}

data class SoundWavefront(
    val emitX: Float,
    val emitY: Float,
    val birthTime: Float
)

@Composable
fun DopplerMachConesExperiment(
    modifier: Modifier = Modifier
) {
    // Physical state & parameters
    var machNumber by remember { mutableStateOf(1.45f) } // M = v / c
    var sourceFreq by remember { mutableStateOf(3.5f) } // Wave emission frequency (Hz)
    var isRunning by remember { mutableStateOf(true) }
    var showMachCone by remember { mutableStateOf(true) }
    var showDopplerSpectrum by remember { mutableStateOf(true) }
    var selectedPreset by remember { mutableStateOf<MachPreset?>(MachPreset.SUPERSONIC) }

    // Simulation runtime variables
    val soundSpeedPx = 135f // c in simulation px/sec
    var simTime by remember { mutableStateOf(0f) }
    var jetX by remember { mutableStateOf(120f) }
    var lastEmitTime by remember { mutableStateOf(0f) }
    val wavefronts = remember { mutableStateListOf<SoundWavefront>() }

    // Interactive Observer position (normalized 0..1 coordinates)
    var observerNormX by remember { mutableStateOf(0.50f) }
    var observerNormY by remember { mutableStateOf(0.68f) }
    var boomFlashAlpha by remember { mutableStateOf(0f) }
    var wasInsideConeLastFrame by remember { mutableStateOf(false) }

    fun resetSimulation() {
        simTime = 0f
        jetX = 80f
        lastEmitTime = 0f
        wavefronts.clear()
        boomFlashAlpha = 0f
        wasInsideConeLastFrame = false
    }

    // High performance physical simulation loop
    LaunchedEffect(isRunning, machNumber, sourceFreq) {
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

                    // Update jet horizontal position
                    val jetSpeed = machNumber * soundSpeedPx
                    jetX += jetSpeed * dt
                    if (jetX > 1400f) {
                        jetX = -120f
                    }

                    // Emit new acoustic circular wavefront at source tip
                    val emitInterval = 1f / sourceFreq.coerceAtLeast(0.5f)
                    if (simTime - lastEmitTime >= emitInterval) {
                        wavefronts.add(SoundWavefront(emitX = jetX, emitY = 0f, birthTime = simTime))
                        lastEmitTime = simTime
                    }

                    // Prune wavefronts that expanded beyond screen visibility
                    if (wavefronts.size > 80) {
                        wavefronts.removeRange(0, 15)
                    }

                    // Sonic Boom flash decay
                    if (boomFlashAlpha > 0f) {
                        boomFlashAlpha = (boomFlashAlpha - dt * 2.2f).coerceAtLeast(0f)
                    }
                }
            }
        }
    }

    // Calculate core aerodynamic & acoustic metrics
    val machAngleDeg = if (machNumber >= 1.0f) {
        asin((1.0f / machNumber).coerceIn(0f, 1f)) * (180f / PI.toFloat())
    } else 0f

    val fAheadRatio = if (machNumber < 1.0f) {
        1.0f / (1.0f - machNumber)
    } else Float.POSITIVE_INFINITY

    val fBehindRatio = 1.0f / (1.0f + machNumber)

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "Drag the Observer on the ground or adjust Mach speed. Cross Mach 1.0 to generate a sonic boom shock cone!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            observerNormX = (observerNormX + dragAmount.x / size.width).coerceIn(0.06f, 0.94f)
                            observerNormY = (observerNormY + dragAmount.y / size.height).coerceIn(0.48f, 0.92f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            observerNormX = (offset.x / size.width).coerceIn(0.06f, 0.94f)
                            observerNormY = (offset.y / size.height).coerceIn(0.48f, 0.92f)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated apparatus altitude origin (cy = h * 0.38f)
                val flightY = h * 0.38f
                val groundY = h * 0.72f
                val observerPos = Offset(w * observerNormX, h * observerNormY)

                // 1. Scientific Coordinate Grid & Atmosphere Layers
                drawDopplerAtmosphere(w, h, flightY, groundY)

                // 2. Render Emitted Acoustic Wavefronts (Circles expanding from past emission coordinates)
                wavefronts.forEach { wf ->
                    val age = simTime - wf.birthTime
                    val radius = age * soundSpeedPx
                    if (radius > 0f && radius < w * 1.5f) {
                        val alpha = (1f - (radius / (w * 1.1f))).coerceIn(0.05f, 0.85f)
                        val strokeW = if (machNumber >= 1.0f && abs(radius - (jetX - wf.emitX)) < 15f) {
                            2.4.dp.toPx() // Shock boundary reinforcement
                        } else {
                            1.2.dp.toPx()
                        }

                        val waveColor = when {
                            machNumber >= 1.0f -> CoralNeon.copy(alpha = alpha * 0.9f)
                            machNumber > 0.8f -> AmberVibrant.copy(alpha = alpha * 0.85f)
                            else -> CyanNeon.copy(alpha = alpha * 0.75f)
                        }

                        drawCircle(
                            color = waveColor,
                            radius = radius,
                            center = Offset(wf.emitX, flightY),
                            style = Stroke(width = strokeW)
                        )
                    }
                }

                // 3. Supersonic Mach Shock Cone (Constructive interference tangent envelope)
                if (machNumber >= 1.0f && showMachCone) {
                    val muRad = asin(1.0f / machNumber)
                    val coneLength = w * 1.4f
                    val backDx = coneLength * cos(muRad)
                    val halfDy = coneLength * sin(muRad)

                    val conePath = Path().apply {
                        moveTo(jetX, flightY)
                        lineTo(jetX - backDx, flightY - halfDy)
                        lineTo(jetX - backDx, flightY + halfDy)
                        close()
                    }

                    // Shaded Mach Cone region (Zone of heard sonic energy)
                    drawPath(
                        path = conePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AmberVibrant.copy(alpha = 0.18f),
                                CoralNeon.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            startX = jetX,
                            endX = jetX - backDx
                        )
                    )

                    // Tangent Shock Wave Front lines (Glowing shock boundaries)
                    drawLine(
                        color = AmberVibrant,
                        start = Offset(jetX, flightY),
                        end = Offset(jetX - backDx, flightY - halfDy),
                        strokeWidth = 2.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = AmberVibrant,
                        start = Offset(jetX, flightY),
                        end = Offset(jetX - backDx, flightY + halfDy),
                        strokeWidth = 2.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Mach Angle μ Sector Arc Indicator
                    drawMachAngleIndicator(jetX, flightY, muRad)

                    // Transonic condensation vapor cone (Prandtl-Glauert Singularity)
                    if (machNumber in 0.96f..1.35f) {
                        drawPrandtlGlauertVaporCone(jetX, flightY, machNumber)
                    }
                }

                // 4. Check Observer Shock Intersection & Boom Trigger
                val obsDx = jetX - observerPos.x
                val obsDy = abs(observerPos.y - flightY)
                val isInsideCone = if (machNumber >= 1.0f) {
                    if (obsDx <= 0f) false
                    else {
                        val tanMu = 1.0f / sqrt(machNumber * machNumber - 1f)
                        obsDy <= obsDx * tanMu
                    }
                } else true

                if (machNumber >= 1.0f && isInsideCone && !wasInsideConeLastFrame) {
                    boomFlashAlpha = 1.0f // Boom impact triggered!
                }
                wasInsideConeLastFrame = isInsideCone

                // 5. Draw Interactive Observer Station (Ground Radar / Ear)
                drawAcousticObserver(
                    pos = observerPos,
                    groundY = groundY,
                    isInsideCone = isInsideCone,
                    machNumber = machNumber,
                    flashAlpha = boomFlashAlpha,
                    sourcePos = Offset(jetX, flightY),
                    fAheadRatio = fAheadRatio,
                    fBehindRatio = fBehindRatio
                )

                // 6. Draw Supersonic Aircraft (Needle nose, delta wings & afterburner)
                drawSupersonicJet(
                    x = jetX,
                    y = flightY,
                    mach = machNumber,
                    simTime = simTime
                )
            }
        },
        hudContent = {
            val regimeText = when {
                machNumber < 0.95f -> "SUBSONIC (M < 1)"
                machNumber <= 1.05f -> "TRANSONIC (M ≈ 1)"
                machNumber < 2.0f -> "SUPERSONIC (M > 1)"
                else -> "HYPERSONIC (M ≥ 2)"
            }

            val machAngleStr = if (machNumber >= 1.0f) {
                "${(round(machAngleDeg * 10f) / 10f)}° (sin μ = 1/M)"
            } else "None (Subsonic)"

            val aheadPitchStr = if (machNumber < 1.0f) {
                "${(round(fAheadRatio * 100f) / 100f)}x f₀ (+${((fAheadRatio - 1f) * 100).toInt()}%)"
            } else "Shock Barrier (∞)"

            val behindPitchStr = "${(round(fBehindRatio * 100f) / 100f)}x f₀ (-${((1f - fBehindRatio) * 100).toInt()}%)"

            val observerStatus = when {
                machNumber >= 1.0f && !wasInsideConeLastFrame -> "Zone of Silence (Ahead of Shock)"
                boomFlashAlpha > 0.4f -> "💥 SONIC BOOM IMPACT!"
                machNumber >= 1.0f -> "Inside Mach Cone (Hearing Craft)"
                else -> "Doppler Shift Active"
            }

            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f),
                title = "Doppler & Mach Cone Telemetry",
                items = listOf(
                    "Flight Regime" to regimeText,
                    "Mach Speed M" to "${(round(machNumber * 100f) / 100f)} M (${(machNumber * 343f).toInt()} m/s)",
                    "Mach Cone Angle μ" to machAngleStr,
                    "Apparent Pitch (Ahead)" to aheadPitchStr,
                    "Apparent Pitch (Behind)" to behindPitchStr,
                    "Observer Telemetry" to observerStatus
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Chips Row (Compact padding, 9-10sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MachPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AmberVibrant.copy(alpha = 0.22f) else ScienceDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AmberVibrant else ScienceBorder.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedPreset = preset
                                    machNumber = preset.mach
                                    sourceFreq = preset.freq
                                    resetSimulation()
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text(
                                    text = preset.label,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AmberVibrant else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Dual Sliders in compact Row (Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Mach Number M",
                        value = machNumber,
                        range = 0.0f..2.5f,
                        valueDisplay = "${(round(machNumber * 100f) / 100f)} M",
                        accentColor = AmberVibrant,
                        onValueChange = {
                            machNumber = it
                            selectedPreset = null
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PhysicsSliderControl(
                        title = "Source Freq f₀",
                        value = sourceFreq,
                        range = 1.5f..5.5f,
                        valueDisplay = "${(round(sourceFreq * 10f) / 10f)} Hz",
                        accentColor = CyanNeon,
                        onValueChange = {
                            sourceFreq = it
                            selectedPreset = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Action Buttons Row (Compact height 32..36dp)
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
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause Simulation" else "▶ Run Simulation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showMachCone = !showMachCone },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (showMachCone) AmberVibrant else ScienceBorder
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (showMachCone) "Cone: ON" else "Cone: OFF",
                            fontSize = 10.5.sp,
                            color = if (showMachCone) AmberVibrant else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            machNumber = 1.45f
                            sourceFreq = 3.5f
                            selectedPreset = MachPreset.SUPERSONIC
                            isRunning = true
                            observerNormX = 0.50f
                            observerNormY = 0.68f
                            resetSimulation()
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
// Pure Compose Vector Graphics & Atmosphere Rendering
// ---------------------------------------------------------------------------

private fun DrawScope.drawDopplerAtmosphere(w: Float, h: Float, flightY: Float, groundY: Float) {
    // Technical grid
    val gridSpacing = 36.dp.toPx()
    var gx = 0f
    while (gx < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.15f),
            start = Offset(gx, 0f),
            end = Offset(gx, h),
            strokeWidth = 0.8f
        )
        gx += gridSpacing
    }
    var gy = 0f
    while (gy < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.15f),
            start = Offset(0f, gy),
            end = Offset(w, gy),
            strokeWidth = 0.8f
        )
        gy += gridSpacing
    }

    // Flight Corridor Line
    drawLine(
        color = CyanNeon.copy(alpha = 0.25f),
        start = Offset(0f, flightY),
        end = Offset(w, flightY),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )

    // Ground Level Terrain
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(ScienceDarkSurfaceVariant.copy(alpha = 0.7f), ScienceDarkSurface.copy(alpha = 0.95f)),
            startY = groundY,
            endY = h
        ),
        topLeft = Offset(0f, groundY),
        size = Size(w, h - groundY)
    )
    drawLine(
        color = EmeraldNeon.copy(alpha = 0.6f),
        start = Offset(0f, groundY),
        end = Offset(w, groundY),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawSupersonicJet(x: Float, y: Float, mach: Float, simTime: Float) {
    val jetLen = 38.dp.toPx()
    val jetSpan = 22.dp.toPx()

    // 1. Afterburner Flame (Pulsing Amber & Cyan Shock Diamonds)
    val flameLen = if (mach >= 1.0f) (jetLen * 0.7f * mach).coerceAtMost(jetLen * 1.5f) else (jetLen * 0.35f)
    val pulse = sin(simTime * 35f) * 0.15f + 0.85f

    val flamePath = Path().apply {
        moveTo(x - jetLen * 0.5f, y - 3.dp.toPx())
        lineTo(x - jetLen * 0.5f - flameLen * pulse, y)
        lineTo(x - jetLen * 0.5f, y + 3.dp.toPx())
        close()
    }
    drawPath(
        path = flamePath,
        brush = Brush.horizontalGradient(
            colors = listOf(CoralNeon, AmberVibrant.copy(alpha = 0.8f), Color.Transparent),
            startX = x - jetLen * 0.5f,
            endX = x - jetLen * 0.5f - flameLen * pulse
        )
    )

    // 2. Jet Fuselage & Delta Wings
    val aircraftPath = Path().apply {
        moveTo(x + jetLen * 0.5f, y) // Needle Nose tip
        lineTo(x + jetLen * 0.1f, y - 3.5.dp.toPx())
        lineTo(x - jetLen * 0.25f, y - jetSpan * 0.5f) // Left Wingtip
        lineTo(x - jetLen * 0.2f, y - 2.5.dp.toPx())
        lineTo(x - jetLen * 0.5f, y - 2.5.dp.toPx()) // Tail
        lineTo(x - jetLen * 0.5f, y + 2.5.dp.toPx())
        lineTo(x - jetLen * 0.2f, y + 2.5.dp.toPx())
        lineTo(x - jetLen * 0.25f, y + jetSpan * 0.5f) // Right Wingtip
        lineTo(x + jetLen * 0.1f, y + 3.5.dp.toPx())
        close()
    }

    drawPath(aircraftPath, color = ScienceDarkSurfaceVariant)
    drawPath(
        path = aircraftPath,
        color = if (mach >= 1.0f) AmberVibrant else CyanNeon,
        style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Cockpit canopy
    drawOval(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(x + jetLen * 0.05f, y - 2.dp.toPx()),
        size = Size(jetLen * 0.22f, 4.dp.toPx())
    )

    // Velocity Vector Indicator Arrow
    val arrowLen = (18.dp.toPx() * mach.coerceAtLeast(0.5f)).coerceAtMost(36.dp.toPx())
    drawLine(
        color = AmberVibrant,
        start = Offset(x + jetLen * 0.5f, y),
        end = Offset(x + jetLen * 0.5f + arrowLen, y),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawMachAngleIndicator(x: Float, y: Float, muRad: Float) {
    val arcRadius = 55.dp.toPx()
    val muDeg = muRad * (180f / PI.toFloat())

    // Upper angle reference line
    val backX = x - arcRadius * cos(muRad)
    val upperY = y - arcRadius * sin(muRad)
    val lowerY = y + arcRadius * sin(muRad)

    drawArc(
        color = AmberVibrant.copy(alpha = 0.8f),
        startAngle = 180f - muDeg,
        sweepAngle = muDeg,
        useCenter = true,
        topLeft = Offset(x - arcRadius, y - arcRadius),
        size = Size(arcRadius * 2f, arcRadius * 2f),
        style = Stroke(width = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f))
    )

    drawArc(
        color = AmberVibrant.copy(alpha = 0.8f),
        startAngle = 180f,
        sweepAngle = muDeg,
        useCenter = true,
        topLeft = Offset(x - arcRadius, y - arcRadius),
        size = Size(arcRadius * 2f, arcRadius * 2f),
        style = Stroke(width = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f))
    )
}

private fun DrawScope.drawPrandtlGlauertVaporCone(x: Float, y: Float, mach: Float) {
    val coneW = 32.dp.toPx()
    val coneH = 46.dp.toPx()
    val cloudAlpha = ((1.2f - abs(mach - 1.05f)) * 0.55f).coerceIn(0f, 0.45f)

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = cloudAlpha), Color.Transparent),
            center = Offset(x - 4.dp.toPx(), y),
            radius = coneH * 0.65f
        ),
        topLeft = Offset(x - coneW * 0.5f - 4.dp.toPx(), y - coneH * 0.5f),
        size = Size(coneW, coneH)
    )
}

private fun DrawScope.drawAcousticObserver(
    pos: Offset,
    groundY: Float,
    isInsideCone: Boolean,
    machNumber: Float,
    flashAlpha: Float,
    sourcePos: Offset,
    fAheadRatio: Float,
    fBehindRatio: Float
) {
    // 1. Sonic Boom Impact Shock Ring at Observer
    if (flashAlpha > 0f) {
        val boomRadius = (1f - flashAlpha) * 65.dp.toPx() + 15.dp.toPx()
        drawCircle(
            color = CoralNeon.copy(alpha = flashAlpha * 0.8f),
            radius = boomRadius,
            center = pos,
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = flashAlpha * 0.6f),
            radius = boomRadius * 0.6f,
            center = pos
        )
    }

    // 2. Ground Station / Listening Post Base
    drawLine(
        color = ScienceBorder.copy(alpha = 0.5f),
        start = Offset(pos.x, pos.y),
        end = Offset(pos.x, groundY),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
    )

    // 3. Observer Radar / Ear Dome
    val haloColor = when {
        flashAlpha > 0.3f -> CoralNeon
        machNumber >= 1.0f && !isInsideCone -> TextSecondary.copy(alpha = 0.4f)
        else -> EmeraldNeon
    }

    drawCircle(
        color = haloColor.copy(alpha = 0.22f),
        radius = 16.dp.toPx(),
        center = pos
    )
    drawCircle(
        color = haloColor,
        radius = 7.dp.toPx(),
        center = pos
    )
    drawCircle(
        color = Color.White,
        radius = 2.5.dp.toPx(),
        center = pos
    )

    // 4. Acoustic line-of-sight ray to aircraft
    val rayColor = if (machNumber >= 1.0f && !isInsideCone) {
        TextSecondary.copy(alpha = 0.25f)
    } else {
        CyanNeon.copy(alpha = 0.45f)
    }
    drawLine(
        color = rayColor,
        start = pos,
        end = sourcePos,
        strokeWidth = 1.2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
    )
}
