package com.geosid.simplephysics.ui.experiments.week5.Day29

import androidx.compose.foundation.BorderStroke
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
import kotlin.random.Random

enum class FountainPreset(
    val label: String,
    val nLiquid: Float,
    val speed: Float,
    val laserAngleDeg: Float,
    val desc: String
) {
    PERFECT_GUIDE("Total Trapping", 1.333f, 4.4f, 2.5f, "100% Total Internal Reflection in water jet"),
    LEAKING_ARC("Bending Leakage", 1.333f, 2.7f, 12.0f, "Steep curvature drops incident angle below critical"),
    GLYCEROL_STREAM("Glycerol (n=1.47)", 1.473f, 3.8f, 6.0f, "Higher refractive index traps steeper angles"),
    STEEP_ESCAPE("Straight Escape", 1.333f, 4.0f, -18.0f, "Laser shoots directly out through upper boundary")
}

enum class LaserColor(val label: String, val color: Color, val wavelengthNm: Int) {
    RUBY_RED("Red 650nm", CoralNeon, 650),
    EMERALD_GREEN("Green 532nm", EmeraldNeon, 532),
    CYAN_BLUE("Cyan 488nm", CyanNeon, 488),
    VIOLET("Violet 405nm", PurpleNeon, 405)
}

data class OpticalBounce(
    val pt: Offset,
    val normal: Offset,
    val incidentAngleRad: Float,
    val isTIR: Boolean,
    val escapeDir: Offset?,
    val intensity: Float
)

data class StreamBubble(
    var t: Float,
    val speedMultiplier: Float,
    val lateralOffset: Float,
    val radius: Float
)

@Composable
fun LaserFountainExperiment(
    modifier: Modifier = Modifier
) {
    // Optical & Fluid physical constants
    val nAir = 1.000f
    val gravity = 9.81f
    val maxGuidanceCurvature = 0.085f

    // Dynamic state
    var nLiquid by remember { mutableStateOf(1.333f) } // Water refractive index
    var streamSpeed by remember { mutableStateOf(4.2f) } // m/s exit speed
    var laserAngleDeg by remember { mutableStateOf(3.0f) } // Injection angle degrees
    var selectedColor by remember { mutableStateOf(LaserColor.RUBY_RED) }
    var selectedPreset by remember { mutableStateOf<FountainPreset?>(FountainPreset.PERFECT_GUIDE) }
    var isRunning by remember { mutableStateOf(true) }
    var showNormals by remember { mutableStateOf(true) }
    var showCatchBasin by remember { mutableStateOf(true) }
    var simTime by remember { mutableStateOf(0f) }

    // Stream micro-bubble particles for fluid flow visualization
    val bubbles = remember {
        mutableStateListOf<StreamBubble>().apply {
            val rng = Random(101)
            repeat(28) {
                add(
                    StreamBubble(
                        t = rng.nextFloat(),
                        speedMultiplier = 0.85f + rng.nextFloat() * 0.35f,
                        lateralOffset = (rng.nextFloat() - 0.5f) * 0.7f,
                        radius = 1.2f + rng.nextFloat() * 2.0f
                    )
                )
            }
        }
    }

    // High performance continuous physics & flow loop
    LaunchedEffect(isRunning, streamSpeed) {
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
                    // Advance fluid streamline bubbles along parabolic path
                    val flowRate = (streamSpeed / 3.5f) * 0.55f * dt
                    for (b in bubbles) {
                        b.t += flowRate * b.speedMultiplier
                        if (b.t > 1.0f) {
                            b.t -= 1.0f
                        }
                    }
                }
            }
        }
    }

    // 1. Snell's law critical angle for total internal reflection
    val criticalAngleRad = asin((nAir / nLiquid).coerceIn(0f, 1f))
    val criticalAngleDeg = criticalAngleRad * (180f / PI.toFloat())

    // 2. Parabolic water jet trajectory and boundary normal
    // Simulated coordinate anchors for preview calculations
    val sampleJetX = 120f
    val sampleNozzleX = 40f
    val sampleNozzleY = 100f
    val jetY = sampleNozzleY + 0.5f * gravity * (sampleJetX - sampleNozzleX).pow(2) / (streamSpeed * streamSpeed)
    val streamSlope = gravity * (sampleJetX - sampleNozzleX) / (streamSpeed * streamSpeed)

    // Ray tracing evaluation for telemetry
    val incidenceAngleRad = (PI.toFloat() * 0.5f - abs(atan(streamSlope) - laserAngleDeg * (PI.toFloat() / 180f))).coerceIn(0.1f, 1.55f)
    val isTIR = incidenceAngleRad >= criticalAngleRad
    val refractedAngleRad = if (!isTIR) asin((nLiquid / nAir * sin(incidenceAngleRad)).coerceIn(0f, 1f)) else 0f

    // 3. Optical power transmission and critical angle boundary
    val reflectionCoeff = if (isTIR) 1.0f else (sin(incidenceAngleRad - refractedAngleRad) / sin(incidenceAngleRad + refractedAngleRad)).pow(2)
    val streamCurvature = gravity / (streamSpeed * streamSpeed)
    val isLightTrapped = isTIR && streamCurvature <= maxGuidanceCurvature

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "Adjust stream velocity or tilt the laser. When the light strikes the water surface above 48.8°, total internal reflection bends the laser inside the water!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Drag vertically to tilt laser angle
                            val dAngle = (dragAmount.y / 8f)
                            laserAngleDeg = (laserAngleDeg + dAngle).coerceIn(-25f, 25f)
                            selectedPreset = null
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // Tap changes laser color or pulses beam
                            val colors = LaserColor.values()
                            val nextIdx = (selectedColor.ordinal + 1) % colors.size
                            selectedColor = colors[nextIdx]
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Layout anchors: elevated apparatus origin leaving bottom clear for controls
                val tankW = w * 0.16f
                val tankH = h * 0.44f
                val tankX = w * 0.04f
                val tankY = h * 0.08f

                val nozzleX = tankX + tankW
                val nozzleY = tankY + tankH * 0.65f
                val nozzleH = 26.dp.toPx()
                val basinTopY = h * 0.68f

                // 1. Acrylic Supply Reservoir Tank & Water Volume
                drawTankAndNozzle(
                    tankX = tankX,
                    tankY = tankY,
                    tankW = tankW,
                    tankH = tankH,
                    nozzleX = nozzleX,
                    nozzleY = nozzleY,
                    nozzleH = nozzleH,
                    nLiquid = nLiquid,
                    simTime = simTime
                )

                // 2. Parabolic Water Stream Geometry & Shading
                val jetLength = (w * 0.90f - nozzleX).coerceAtLeast(100f)
                val streamPointsTop = mutableListOf<Offset>()
                val streamPointsBottom = mutableListOf<Offset>()
                val steps = 90
                val gScale = (gravity * 16.5f) / (streamSpeed * streamSpeed)

                for (i in 0..steps) {
                    val s = i / steps.toFloat()
                    val dx = s * jetLength
                    val currX = nozzleX + dx
                    val currSlope = gScale * dx / jetLength
                    val yCenter = nozzleY + 0.5f * gScale * (dx * dx) / jetLength

                    // Stream width tapers slightly under acceleration (continuity equation)
                    val currentThickness = (nozzleH * (0.95f - 0.28f * s)).coerceAtLeast(10.dp.toPx())
                    val halfThick = currentThickness * 0.5f

                    // Perpendicular offset vector to center trajectory
                    val normalAngle = atan(currSlope) + PI.toFloat() * 0.5f
                    val nx = cos(normalAngle) * halfThick
                    val ny = sin(normalAngle) * halfThick

                    streamPointsTop.add(Offset(currX - nx, yCenter - ny))
                    streamPointsBottom.add(Offset(currX + nx, yCenter + ny))
                }

                // Render glowing translucent water jet
                drawWaterStream(
                    streamPointsTop = streamPointsTop,
                    streamPointsBottom = streamPointsBottom,
                    nozzleX = nozzleX,
                    nozzleY = nozzleY,
                    basinTopY = basinTopY
                )

                // Render dynamic stream micro-bubbles
                drawStreamBubbles(
                    bubbles = bubbles,
                    streamPointsTop = streamPointsTop,
                    streamPointsBottom = streamPointsBottom
                )

                // 3. Laser Emitter Device & Aiming reticle
                val laserX = tankX - 16.dp.toPx()
                val laserY = nozzleY
                drawLaserEmitter(
                    laserX = laserX,
                    laserY = laserY,
                    laserAngleDeg = laserAngleDeg,
                    laserColor = selectedColor.color
                )

                // 4. Optical Ray Tracing & Total Internal Reflection
                val bounces = traceLaserRay(
                    startX = nozzleX,
                    startY = nozzleY,
                    initialAngleDeg = laserAngleDeg,
                    streamPointsTop = streamPointsTop,
                    streamPointsBottom = streamPointsBottom,
                    nLiquid = nLiquid,
                    nAir = nAir,
                    criticalAngleRad = criticalAngleRad
                )

                // Render the laser beam path, internal bounces, and escaping leakage rays
                drawLaserRays(
                    startX = nozzleX,
                    startY = nozzleY,
                    bounces = bounces,
                    laserColor = selectedColor.color,
                    showNormals = showNormals,
                    criticalAngleRad = criticalAngleRad,
                    simTime = simTime
                )

                // 5. Catch Basin & Glowing Collected Water
                if (showCatchBasin && streamPointsBottom.isNotEmpty()) {
                    val streamEndX = streamPointsBottom.last().x
                    drawCatchBasin(
                        streamEndX = streamEndX,
                        basinTopY = basinTopY,
                        laserColor = selectedColor.color,
                        isTrapped = bounces.all { it.isTIR },
                        simTime = simTime
                    )
                }
            }
        },
        hudContent = {
            val trappedCount = 100 // Visual clarity
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f),
                title = "Optical Waveguide Telemetry",
                items = listOf(
                    "Critical Angle θc" to "${round(criticalAngleDeg * 10f) / 10f}°",
                    "Incidence Angle" to "${round(incidenceAngleRad * (180f / PI.toFloat()) * 10f) / 10f}°",
                    "Guide Mode" to if (isTIR) "100% TRAPPED (TIR)" else "OPTICAL LEAKAGE",
                    "Medium n" to "${round(nLiquid * 1000f) / 1000f}",
                    "Curvature Index" to "${round(streamCurvature * 1000f) / 10f}"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset mode selector chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FountainPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPreset = preset
                                nLiquid = preset.nLiquid
                                streamSpeed = preset.speed
                                laserAngleDeg = preset.laserAngleDeg
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
                                borderColor = if (isSelected) CyanNeon else ScienceBorder.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Paired Sliders: Laser Angle & Water Stream Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Laser Injection Angle",
                            value = laserAngleDeg,
                            range = -20f..20f,
                            valueDisplay = "${round(laserAngleDeg * 10f) / 10f}°",
                            accentColor = selectedColor.color,
                            onValueChange = {
                                laserAngleDeg = it
                                selectedPreset = null
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Water Stream Velocity",
                            value = streamSpeed,
                            range = 2.0f..6.0f,
                            valueDisplay = "${round(streamSpeed * 10f) / 10f} m/s",
                            accentColor = CyanNeon,
                            onValueChange = {
                                streamSpeed = it
                                selectedPreset = null
                            }
                        )
                    }
                }

                // Second row: Refractive Index & Laser Color selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Liquid Refraction Index (n)",
                            value = nLiquid,
                            range = 1.15f..1.55f,
                            valueDisplay = "${round(nLiquid * 100f) / 100f}",
                            accentColor = AmberVibrant,
                            onValueChange = {
                                nLiquid = it
                                selectedPreset = null
                            }
                        )
                    }

                    // Compact Color Selection Chips
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LaserColor.values().forEach { lc ->
                            val isChosen = selectedColor == lc
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clickable { selectedColor = lc },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) lc.color.copy(alpha = 0.3f) else ScienceDarkSurfaceVariant,
                                 border = BorderStroke(1.dp, if (isChosen) lc.color else ScienceBorder.copy(alpha = 0.35f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = lc.label.split(" ").first(),
                                        fontSize = 9.sp,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isChosen) lc.color else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Controls Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberVibrant.copy(alpha = 0.25f) else CyanNeon.copy(alpha = 0.25f),
                            contentColor = if (isRunning) AmberVibrant else CyanNeon
                        ),
                        border = BorderStroke(1.dp, if (isRunning) AmberVibrant else CyanNeon),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause Flow" else "▶ Run Flow",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showNormals = !showNormals },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (showNormals) EmeraldNeon else TextSecondary
                        ),
                        border = BorderStroke(1.dp, if (showNormals) EmeraldNeon.copy(alpha = 0.8f) else ScienceBorder.copy(alpha = 0.35f)),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (showNormals) "Normals: ON" else "Normals: OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = {
                            nLiquid = 1.333f
                            streamSpeed = 4.2f
                            laserAngleDeg = 3.0f
                            selectedColor = LaserColor.RUBY_RED
                            selectedPreset = FountainPreset.PERFECT_GUIDE
                            showNormals = true
                            isRunning = true
                            simTime = 0f
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, ScienceBorder.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    ) {
                        ResetIcon(tint = CyanNeon, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    )
}

// -------------------------------------------------------------
// Canvas Graphics & Optics Helper Functions
// -------------------------------------------------------------

private fun DrawScope.drawTankAndNozzle(
    tankX: Float,
    tankY: Float,
    tankW: Float,
    tankH: Float,
    nozzleX: Float,
    nozzleY: Float,
    nozzleH: Float,
    nLiquid: Float,
    simTime: Float
) {
    // Acrylic glass reservoir body
    drawRoundRect(
        color = ScienceDarkSurfaceVariant.copy(alpha = 0.85f),
        topLeft = Offset(tankX, tankY),
        size = Size(tankW, tankH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
    )
    drawRoundRect(
        color = CyanNeon.copy(alpha = 0.35f),
        topLeft = Offset(tankX, tankY),
        size = Size(tankW, tankH),
        style = Stroke(width = 1.5.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
    )

    // Liquid volume inside tank with undulating meniscus
    val waterH = tankH * 0.78f
    val waterTopY = tankY + (tankH - waterH)
    val waterPath = Path().apply {
        moveTo(tankX + 2f, waterTopY)
        val waveW = tankW - 4f
        for (i in 0..10) {
            val wx = tankX + 2f + (i / 10f) * waveW
            val wy = waterTopY + sin(simTime * 3f + i * 0.8f) * 2f
            lineTo(wx, wy)
        }
        lineTo(tankX + tankW - 2f, tankY + tankH - 2f)
        lineTo(tankX + 2f, tankY + tankH - 2f)
        close()
    }
    drawPath(
        path = waterPath,
        color = CyanNeon.copy(alpha = 0.20f)
    )

    // Measurement scale marks on acrylic tank
    val marks = 5
    for (m in 1..marks) {
        val my = tankY + (tankH / (marks + 1)) * m
        drawLine(
            color = ScienceBorder.copy(alpha = 0.6f),
            start = Offset(tankX + 4f, my),
            end = Offset(tankX + 14f, my),
            strokeWidth = 1f
        )
    }

    // Chrome/Brass Horizontal Orifice Nozzle
    val nozzleLength = 18.dp.toPx()
    drawRect(
        color = Color(0xFF455A64),
        topLeft = Offset(nozzleX - 4f, nozzleY - nozzleH * 0.5f),
        size = Size(nozzleLength, nozzleH)
    )
    drawRect(
        color = AmberVibrant.copy(alpha = 0.85f),
        topLeft = Offset(nozzleX - 4f, nozzleY - nozzleH * 0.5f),
        size = Size(nozzleLength, nozzleH),
        style = Stroke(width = 1.2f)
    )
}

private fun DrawScope.drawWaterStream(
    streamPointsTop: List<Offset>,
    streamPointsBottom: List<Offset>,
    nozzleX: Float,
    nozzleY: Float,
    basinTopY: Float
) {
    if (streamPointsTop.isEmpty() || streamPointsBottom.isEmpty()) return

    val streamPath = Path().apply {
        moveTo(streamPointsTop.first().x, streamPointsTop.first().y)
        for (pt in streamPointsTop) {
            lineTo(pt.x, pt.y)
        }
        for (i in streamPointsBottom.indices.reversed()) {
            val pt = streamPointsBottom[i]
            lineTo(pt.x, pt.y)
        }
        close()
    }

    // Soft water body glow
    drawPath(
        path = streamPath,
        color = CyanNeon.copy(alpha = 0.24f)
    )

    // Crisp illuminated stream boundary curves
    val topBorderPath = Path().apply {
        moveTo(streamPointsTop.first().x, streamPointsTop.first().y)
        for (pt in streamPointsTop) lineTo(pt.x, pt.y)
    }
    drawPath(
        path = topBorderPath,
        color = CyanNeon.copy(alpha = 0.75f),
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
    )

    val bottomBorderPath = Path().apply {
        moveTo(streamPointsBottom.first().x, streamPointsBottom.first().y)
        for (pt in streamPointsBottom) lineTo(pt.x, pt.y)
    }
    drawPath(
        path = bottomBorderPath,
        color = CyanNeon.copy(alpha = 0.65f),
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawStreamBubbles(
    bubbles: List<StreamBubble>,
    streamPointsTop: List<Offset>,
    streamPointsBottom: List<Offset>
) {
    val count = streamPointsTop.size
    if (count < 2) return

    for (b in bubbles) {
        val idxF = b.t * (count - 1)
        val idx0 = idxF.toInt().coerceIn(0, count - 2)
        val frac = idxF - idx0

        val topPt = Offset(
            streamPointsTop[idx0].x * (1f - frac) + streamPointsTop[idx0 + 1].x * frac,
            streamPointsTop[idx0].y * (1f - frac) + streamPointsTop[idx0 + 1].y * frac
        )
        val botPt = Offset(
            streamPointsBottom[idx0].x * (1f - frac) + streamPointsBottom[idx0 + 1].x * frac,
            streamPointsBottom[idx0].y * (1f - frac) + streamPointsBottom[idx0 + 1].y * frac
        )

        val posX = (topPt.x + botPt.x) * 0.5f + (botPt.x - topPt.x) * b.lateralOffset
        val posY = (topPt.y + botPt.y) * 0.5f + (botPt.y - topPt.y) * b.lateralOffset

        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = b.radius,
            center = Offset(posX, posY)
        )
    }
}

private fun DrawScope.drawLaserEmitter(
    laserX: Float,
    laserY: Float,
    laserAngleDeg: Float,
    laserColor: Color
) {
    val emitterW = 28.dp.toPx()
    val emitterH = 14.dp.toPx()

    // Tiltable diode housing
    val angleRad = laserAngleDeg * (PI.toFloat() / 180f)
    val cosA = cos(angleRad)
    val sinA = sin(angleRad)

    drawCircle(
        color = laserColor.copy(alpha = 0.35f),
        radius = 16.dp.toPx(),
        center = Offset(laserX, laserY)
    )
    drawCircle(
        color = laserColor,
        radius = 5.dp.toPx(),
        center = Offset(laserX, laserY)
    )

    // Collimator barrel pointing toward nozzle
    val barrelEndX = laserX + cosA * emitterW
    val barrelEndY = laserY + sinA * emitterW
    drawLine(
        color = Color(0xFFCFD8DC),
        start = Offset(laserX, laserY),
        end = Offset(barrelEndX, barrelEndY),
        strokeWidth = emitterH,
        cap = StrokeCap.Round
    )
    drawLine(
        color = laserColor,
        start = Offset(laserX, laserY),
        end = Offset(barrelEndX, barrelEndY),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )
}

private fun traceLaserRay(
    startX: Float,
    startY: Float,
    initialAngleDeg: Float,
    streamPointsTop: List<Offset>,
    streamPointsBottom: List<Offset>,
    nLiquid: Float,
    nAir: Float,
    criticalAngleRad: Float
): List<OpticalBounce> {
    val bounces = mutableListOf<OpticalBounce>()
    if (streamPointsTop.size < 4 || streamPointsBottom.size < 4) return bounces

    var rayX = startX
    var rayY = startY
    var rayAngle = initialAngleDeg * (PI.toFloat() / 180f)
    var currIntensity = 1.0f

    val maxBounces = 12
    var nextSurfaceIsTop = rayAngle < 0f // heading towards top if negative

    for (b in 0 until maxBounces) {
        val targetPoints = if (nextSurfaceIsTop) streamPointsTop else streamPointsBottom
        var hitIndex = -1
        var minDistance = Float.POSITIVE_INFINITY

        val dirX = cos(rayAngle)
        val dirY = sin(rayAngle)

        // Find collision with target boundary curve
        for (i in 0 until targetPoints.size - 1) {
            val p1 = targetPoints[i]
            val p2 = targetPoints[i + 1]

            // Ray-segment intersection
            val segDx = p2.x - p1.x
            val segDy = p2.y - p1.y
            val denom = dirX * segDy - dirY * segDx

            if (abs(denom) > 1e-5f) {
                val t = ((p1.x - rayX) * segDy - (p1.y - rayY) * segDx) / denom
                val u = ((p1.x - rayX) * dirY - (p1.y - rayY) * dirX) / denom

                if (t > 4f && u in 0f..1f && t < minDistance) {
                    minDistance = t
                    hitIndex = i
                }
            }
        }

        if (hitIndex == -1 || minDistance == Float.POSITIVE_INFINITY) {
            break // Exited stream end
        }

        val hitPt = Offset(rayX + dirX * minDistance, rayY + dirY * minDistance)
        val p1 = targetPoints[hitIndex]
        val p2 = targetPoints[hitIndex + 1]

        // Surface tangent & inward unit normal
        val segLen = hypot(p2.x - p1.x, p2.y - p1.y)
        val tx = (p2.x - p1.x) / segLen
        val ty = (p2.y - p1.y) / segLen

        // Normal points inwards (downward for top surface, upward for bottom surface)
        val inwardNx = if (nextSurfaceIsTop) -ty else ty
        val inwardNy = if (nextSurfaceIsTop) tx else -tx

        // Incident ray vector dot inward normal
        val dotProd = -(dirX * inwardNx + dirY * inwardNy).coerceIn(-1f, 1f)
        val incidentAngle = acos(abs(dotProd))

        val isTIR = incidentAngle >= criticalAngleRad

        var escapeDir: Offset? = null
        if (!isTIR) {
            // Light refracts and escapes into air!
            val sinThetaT = (nLiquid / nAir) * sin(incidentAngle)
            if (sinThetaT <= 1f) {
                val thetaT = asin(sinThetaT)
                val outwardNx = -inwardNx
                val outwardNy = -inwardNy
                val escAngle = atan2(dirY, dirX) + (thetaT - incidentAngle) * (if (nextSurfaceIsTop) -1f else 1f)
                escapeDir = Offset(cos(escAngle), sin(escAngle))
            }
        }

        bounces.add(
            OpticalBounce(
                pt = hitPt,
                normal = Offset(inwardNx, inwardNy),
                incidentAngleRad = incidentAngle,
                isTIR = isTIR,
                escapeDir = escapeDir,
                intensity = currIntensity
            )
        )

        if (!isTIR) {
            // Optical power drops significantly
            currIntensity *= 0.35f
        }

        // Specular reflection vector: d' = d - 2*(d·N)*N
        val dDotN = dirX * inwardNx + dirY * inwardNy
        val reflX = dirX - 2f * dDotN * inwardNx
        val reflY = dirY - 2f * dDotN * inwardNy

        rayX = hitPt.x + inwardNx * 1.5f
        rayY = hitPt.y + inwardNy * 1.5f
        rayAngle = atan2(reflY, reflX)
        nextSurfaceIsTop = !nextSurfaceIsTop
    }

    return bounces
}

private fun DrawScope.drawLaserRays(
    startX: Float,
    startY: Float,
    bounces: List<OpticalBounce>,
    laserColor: Color,
    showNormals: Boolean,
    criticalAngleRad: Float,
    simTime: Float
) {
    if (bounces.isEmpty()) return

    var prevPt = Offset(startX, startY)

    for (b in bounces) {
        // High intensity core laser beam segment
        drawLine(
            color = laserColor.copy(alpha = (0.90f * b.intensity).coerceIn(0.1f, 1f)),
            start = prevPt,
            end = b.pt,
            strokeWidth = 2.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Outer neon glow
        drawLine(
            color = laserColor.copy(alpha = (0.35f * b.intensity).coerceIn(0.05f, 0.4f)),
            start = prevPt,
            end = b.pt,
            strokeWidth = 7.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Reflection point spark
        drawCircle(
            color = if (b.isTIR) Color.White else AmberVibrant,
            radius = 3.5.dp.toPx(),
            center = b.pt
        )
        drawCircle(
            color = laserColor.copy(alpha = 0.5f),
            radius = 8.dp.toPx(),
            center = b.pt
        )

        // Escaping leakage ray radiating into air
        if (!b.isTIR && b.escapeDir != null) {
            val escLen = 70.dp.toPx()
            val escEnd = Offset(b.pt.x + b.escapeDir.x * escLen, b.pt.y + b.escapeDir.y * escLen)

            // Warning pulsed escape beam
            val leakGlow = 0.7f + 0.3f * sin(simTime * 8f)
            drawLine(
                color = CoralNeon.copy(alpha = leakGlow),
                start = b.pt,
                end = escEnd,
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = CoralNeon.copy(alpha = 0.3f * leakGlow),
                start = b.pt,
                end = escEnd,
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Leakage indicator badge
            drawCircle(CoralNeon, 4.dp.toPx(), escEnd)
        }

        // Surface normal vectors & angle arcs
        if (showNormals) {
            val nLen = 22.dp.toPx()
            val nEnd = Offset(b.pt.x + b.normal.x * nLen, b.pt.y + b.normal.y * nLen)
            drawLine(
                color = EmeraldNeon.copy(alpha = 0.75f),
                start = b.pt,
                end = nEnd,
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f))
            )
        }

        prevPt = b.pt
    }
}

private fun DrawScope.drawCatchBasin(
    streamEndX: Float,
    basinTopY: Float,
    laserColor: Color,
    isTrapped: Boolean,
    simTime: Float
) {
    val basinW = 75.dp.toPx()
    val basinH = 38.dp.toPx()
    val basinX = streamEndX - basinW * 0.5f

    // Acrylic/Metal Catch Basin Body
    drawRoundRect(
        color = ScienceDarkSurfaceVariant.copy(alpha = 0.90f),
        topLeft = Offset(basinX, basinTopY),
        size = Size(basinW, basinH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
    )
    drawRoundRect(
        color = ScienceBorder.copy(alpha = 0.5f),
        topLeft = Offset(basinX, basinTopY),
        size = Size(basinW, basinH),
        style = Stroke(width = 1.5.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
    )

    // Pooling water in basin illuminated by trapped laser light
    val poolGlow = if (isTrapped) laserColor.copy(alpha = 0.45f) else CyanNeon.copy(alpha = 0.20f)
    drawRoundRect(
        color = poolGlow,
        topLeft = Offset(basinX + 3f, basinTopY + 6f),
        size = Size(basinW - 6f, basinH - 8f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
    )

    if (isTrapped) {
        // Bright focal pool glow spot
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = 5.dp.toPx(),
            center = Offset(streamEndX, basinTopY + 12f)
        )
        drawCircle(
            color = laserColor.copy(alpha = 0.6f),
            radius = 14.dp.toPx(),
            center = Offset(streamEndX, basinTopY + 12f)
        )
    }
}
