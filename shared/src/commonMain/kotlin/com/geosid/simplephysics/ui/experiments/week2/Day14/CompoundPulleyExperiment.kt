package com.geosid.simplephysics.ui.experiments.week2.Day14

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.ui.components.PhysicsSliderControl
import com.geosid.simplephysics.ui.components.ResetIcon
import com.geosid.simplephysics.ui.components.ResponsiveExperimentContainer
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*

/**
 * Mechanical Advantage configurations for the pulley system.
 */
enum class PulleyConfig(
    val title: String,
    val segments: Int, // n (Ideal Mechanical Advantage)
    val fixedSheaves: Int,
    val movableSheaves: Int,
    val description: String,
    val color: Color
) {
    SINGLE_FIXED(
        title = "1× Fixed",
        segments = 1,
        fixedSheaves = 1,
        movableSheaves = 0,
        description = "Direction changer only. Input Force = Load Weight.",
        color = AmberVibrant
    ),
    SINGLE_MOVABLE(
        title = "2× Gun Tackle",
        segments = 2,
        fixedSheaves = 1,
        movableSheaves = 1,
        description = "1 moving pulley. Halves the required pulling force!",
        color = CyanNeon
    ),
    BLOCK_AND_TACKLE(
        title = "4× Double Luff",
        segments = 4,
        fixedSheaves = 2,
        movableSheaves = 2,
        description = "Classic block & tackle. Effort is divided by 4!",
        color = EmeraldNeon
    ),
    CRANE_RIG(
        title = "8× Heavy Crane",
        segments = 8,
        fixedSheaves = 4,
        movableSheaves = 4,
        description = "Heavy industrial crane rig. Effort divided by 8!",
        color = PurpleNeon
    )
}

/**
 * Heavy cargo loads to lift with the pulley system.
 */
enum class PulleyCargo(
    val title: String,
    val icon: String,
    val massKg: Float,
    val color: Color
) {
    WEIGHT_50(
        title = "50 kg Barbell",
        icon = "🏋️",
        massKg = 50f,
        color = AmberVibrant
    ),
    PIANO_400(
        title = "Grand Piano",
        icon = "🎹",
        massKg = 380f,
        color = CyanNeon
    ),
    ENGINE_800(
        title = "V8 Engine",
        icon = "🚗",
        massKg = 750f,
        color = CoralNeon
    ),
    VAULT_2500(
        title = "Gold Vault",
        icon = "🏦",
        massKg = 2200f,
        color = PurpleNeon
    )
}

@Composable
fun CompoundPulleyExperiment(
    modifier: Modifier = Modifier
) {
    // 1. Selection State
    var selectedConfig by remember { mutableStateOf(PulleyConfig.BLOCK_AND_TACKLE) }
    var selectedCargo by remember { mutableStateOf(PulleyCargo.ENGINE_800) }

    // 2. Tunable Parameters
    var mass by remember { mutableStateOf(PulleyCargo.ENGINE_800.massKg) }
    var efficiency by remember { mutableStateOf(0.95f) } // 95% bearing efficiency
    var winchSpeed by remember { mutableStateOf(0.35f) } // m/s rope pull speed

    // 3. Dynamic Kinematic Simulation State
    var isRunning by remember { mutableStateOf(true) }
    var isMotorReversed by remember { mutableStateOf(false) } // true = lowering, false = hoisting
    var loadHeight by remember { mutableStateOf(0.20f) }      // 0.0 (ground) to 1.8 (top limit) meters
    var ropePulledTotal by remember { mutableStateOf(0.80f) }  // cumulative rope pulled
    var sheaveRotationAngle by remember { mutableStateOf(0.0f) }
    var isDraggingLoad by remember { mutableStateOf(false) }

    // Physical Calculations
    val gravity = 9.81f
    val n = selectedConfig.segments
    val loadWeightN = mass * gravity
    val idealInputForceN = loadWeightN / n
    val realInputForceN = idealInputForceN / efficiency.coerceAtLeast(0.5f)

    // Work / Energy Conservation
    val workOutputJoules = loadWeightN * loadHeight
    val workInputJoules = realInputForceN * ropePulledTotal

    // Continuous Frame Loop for Winch & Animation
    LaunchedEffect(isRunning, isMotorReversed, winchSpeed, selectedConfig, mass, isDraggingLoad) {
        var lastNanos = 0L
        while (true) {
            val currentNanos = withFrameNanos { it }
            if (lastNanos == 0L) {
                lastNanos = currentNanos
                continue
            }
            val dtRaw = (currentNanos - lastNanos) / 1_000_000_000f
            val dt = dtRaw.coerceIn(0.001f, 0.040f)
            lastNanos = currentNanos

            if (isRunning && !isDraggingLoad) {
                val dir = if (isMotorReversed) -1.0f else 1.0f
                val ropeDelta = winchSpeed * dt * dir
                val loadDelta = ropeDelta / n

                val maxHeight = 1.65f
                val minHeight = 0.05f
                val newHeight = (loadHeight + loadDelta).coerceIn(minHeight, maxHeight)

                // Auto-reverse at bounds
                if (newHeight >= maxHeight && !isMotorReversed) {
                    isMotorReversed = true
                } else if (newHeight <= minHeight && isMotorReversed) {
                    isMotorReversed = false
                }

                val actualLoadStep = newHeight - loadHeight
                loadHeight = newHeight
                ropePulledTotal = (ropePulledTotal + actualLoadStep * n).coerceAtLeast(0f)
                sheaveRotationAngle += actualLoadStep * n * 24f
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag the suspended load to raise/lower, toggle pulley rigs (1x to 8x), or tune controls below!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDraggingLoad = true },
                            onDragEnd = { isDraggingLoad = false },
                            onDragCancel = { isDraggingLoad = false }
                        ) { change, dragAmount ->
                            change.consume()
                            val deltaH = -dragAmount.y / (size.height * 0.45f)
                            val newH = (loadHeight + deltaH).coerceIn(0.05f, 1.65f)
                            val step = newH - loadHeight
                            loadHeight = newH
                            ropePulledTotal = (ropePulledTotal + step * n).coerceAtLeast(0f)
                            sheaveRotationAngle += step * n * 24f
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Tap toggles hoist direction
                            isMotorReversed = !isMotorReversed
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw workshop / industrial crane backdrop
                drawPulleyWorkshopBackground(w, h)

                // Ceiling Girder Y baseline
                val ceilingY = 38.dp.toPx()
                val floorY = h * 0.88f

                // Pulley geometry
                val centerX = w * 0.46f
                val sheaveRadius = 20.dp.toPx()
                val maxLoadDrop = floorY - ceilingY - 140.dp.toPx()

                // Normalized load position (0.0 at floor, 1.0 near ceiling)
                val normH = (loadHeight / 1.65f).coerceIn(0f, 1f)
                val movableBlockY = floorY - 95.dp.toPx() - (normH * maxLoadDrop)

                // 1. Draw Overhead Steel I-Beam Girder
                drawCeilingGirder(w = w, ceilingY = ceilingY)

                // 2. Draw Top (Fixed) Pulley Bracket & Sheaves
                drawUpperPulleyBlock(
                    centerX = centerX,
                    ceilingY = ceilingY,
                    radius = sheaveRadius,
                    count = selectedConfig.fixedSheaves,
                    rotAngle = sheaveRotationAngle,
                    color = selectedConfig.color
                )

                // 3. Draw Bottom (Movable) Pulley Block & Hook
                val movableSheaveCount = selectedConfig.movableSheaves
                val lowerBlockY = if (movableSheaveCount > 0) movableBlockY else ceilingY + 50f
                if (movableSheaveCount > 0) {
                    drawLowerPulleyBlock(
                        centerX = centerX,
                        blockY = lowerBlockY,
                        radius = sheaveRadius,
                        count = movableSheaveCount,
                        rotAngle = -sheaveRotationAngle,
                        color = selectedConfig.color
                    )
                }

                // 4. Draw Continuous Threaded Cable / Rope
                drawPulleyRopeSystem(
                    w = w,
                    centerX = centerX,
                    topY = ceilingY + 28f,
                    bottomY = lowerBlockY,
                    sheaveRadius = sheaveRadius,
                    config = selectedConfig,
                    rotAngle = sheaveRotationAngle,
                    ropeTensionN = realInputForceN
                )

                // 5. Draw Suspended Cargo Load with Chains & Mass Label
                val cargoAttachY = if (movableSheaveCount > 0) lowerBlockY + sheaveRadius + 32f else lowerBlockY + 120f
                drawSuspendedCargo(
                    centerX = centerX,
                    attachY = cargoAttachY,
                    cargo = selectedCargo,
                    mass = mass,
                    loadWeightN = loadWeightN,
                    isDragging = isDraggingLoad
                )

                // 6. Draw Free-Body Tension & Gravity Vectors
                drawPulleyForceVectors(
                    centerX = centerX,
                    topY = ceilingY + 28f,
                    bottomY = lowerBlockY,
                    cargoY = cargoAttachY,
                    n = n,
                    tensionN = realInputForceN,
                    weightN = loadWeightN,
                    sheaveRadius = sheaveRadius
                )
            }
        },
        hudContent = {
            val fInDisplay = "${round(realInputForceN).toInt()} N (${round(realInputForceN / 9.81f * 10f) / 10f} kgf)"
            val weightDisplay = "${round(loadWeightN).toInt()} N (${round(mass).toInt()} kg)"
            val heightDisplay = "${round(loadHeight * 100f) / 100f} m"
            val ropeDisplay = "${round(ropePulledTotal * 100f) / 100f} m"

            TransparentTelemetryHud(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 14: Compound Pulley System",
                items = listOf(
                    "Mechanical Advantage" to "${selectedConfig.segments}× (${selectedConfig.title})",
                    "Input Pull Force (Fin)" to "$fInDisplay  [Effort / ${selectedConfig.segments}]",
                    "Total Load Weight (W)" to "$weightDisplay",
                    "Motion Kinematics" to "Lift: $heightDisplay | Rope Pulled: $ropeDisplay"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Pulley Configuration Selection Chips (1x, 2x, 4x, 8x)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulleyConfig.entries.forEach { cfg ->
                        val isSelected = selectedConfig == cfg
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedConfig = cfg },
                            label = {
                                Text(
                                    text = cfg.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cfg.color.copy(alpha = 0.25f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ScienceDarkSurface,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. Cargo Load Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulleyCargo.entries.forEach { cargo ->
                        val isSelected = selectedCargo == cargo
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCargo = cargo
                                mass = cargo.massKg
                            },
                            label = {
                                Text(
                                    text = "${cargo.icon} ${cargo.title}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cargo.color.copy(alpha = 0.25f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ScienceDarkSurface,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Compact Sliders: Mass & Winch Hoist Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Mass (m)",
                        value = mass,
                        range = 10f..3000f,
                        valueDisplay = "${round(mass).toInt()} kg",
                        accentColor = AmberVibrant,
                        onValueChange = { mass = it }
                    )

                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Winch Speed (v)",
                        value = winchSpeed,
                        range = 0.05f..1.20f,
                        valueDisplay = "${round(winchSpeed * 100f) / 100f} m/s",
                        accentColor = CyanNeon,
                        onValueChange = { winchSpeed = it }
                    )
                }

                // 4. Action Buttons (Hoist/Lower Toggle, Direction, Reset)
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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause Winch" else "▶ Run Winch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { isMotorReversed = !isMotorReversed },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isMotorReversed) CoralNeon else EmeraldNeon),
                        border = androidx.compose.foundation.BorderStroke(1.dp, (if (isMotorReversed) CoralNeon else EmeraldNeon).copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isMotorReversed) "⬇ Lowering" else "⬆ Hoisting",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            selectedConfig = PulleyConfig.BLOCK_AND_TACKLE
                            selectedCargo = PulleyCargo.ENGINE_800
                            mass = PulleyCargo.ENGINE_800.massKg
                            loadHeight = 0.20f
                            ropePulledTotal = 0.80f
                            isRunning = true
                            isMotorReversed = false
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(10.dp))
                    ) {
                        ResetIcon(tint = CyanNeon, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    )
}

// ----------------------------------------------------------------------------
// Custom Compose Canvas Rendering Pipelines for Pulleys
// ----------------------------------------------------------------------------

private fun DrawScope.drawPulleyWorkshopBackground(w: Float, h: Float) {
    // Gradient dark industrial workshop backdrop
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(ScienceDarkBg, ScienceDarkSurface, ScienceDarkBg),
            startY = 0f,
            endY = h
        )
    )

    // Scientific grid
    val grid = 36.dp.toPx()
    var gx = 0f
    while (gx < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.15f),
            start = Offset(gx, 0f),
            end = Offset(gx, h),
            strokeWidth = 0.8f
        )
        gx += grid
    }
    var gy = 0f
    while (gy < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.15f),
            start = Offset(0f, gy),
            end = Offset(w, gy),
            strokeWidth = 0.8f
        )
        gy += grid
    }

    // Floor base
    val floorY = h * 0.88f
    drawLine(ScienceBorder.copy(alpha = 0.8f), Offset(0f, floorY), Offset(w, floorY), 2.5f)
    drawRect(
        color = ScienceDarkSurfaceVariant.copy(alpha = 0.6f),
        topLeft = Offset(0f, floorY),
        size = Size(w, h - floorY)
    )
}

private fun DrawScope.drawCeilingGirder(w: Float, ceilingY: Float) {
    val girderH = 20.dp.toPx()
    // Industrial steel beam
    drawRect(
        color = ScienceDarkSurfaceVariant,
        topLeft = Offset(0f, ceilingY - girderH),
        size = Size(w, girderH)
    )
    drawRect(
        color = ScienceBorder,
        topLeft = Offset(0f, ceilingY - girderH),
        size = Size(w, girderH),
        style = Stroke(1.5f)
    )

    // Rivets along beam
    val spacing = 28.dp.toPx()
    var rx = 14.dp.toPx()
    while (rx < w) {
        drawCircle(Color.White.copy(alpha = 0.35f), 2.5f, Offset(rx, ceilingY - girderH * 0.5f))
        rx += spacing
    }
}

private fun DrawScope.drawUpperPulleyBlock(
    centerX: Float,
    ceilingY: Float,
    radius: Float,
    count: Int,
    rotAngle: Float,
    color: Color
) {
    val blockW = (count * 20.dp.toPx() + 18.dp.toPx()).coerceAtLeast(42.dp.toPx())
    val blockH = 16.dp.toPx()

    // Ceiling Anchor Bracket
    val mountLeft = centerX - blockW * 0.5f
    drawRoundRect(
        color = ScienceBorder,
        topLeft = Offset(mountLeft, ceilingY),
        size = Size(blockW, blockH),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Sheaves
    for (i in 0 until count) {
        val offset = (i - (count - 1) * 0.5f) * (radius * 1.05f)
        val sx = centerX + offset
        val sy = ceilingY + blockH + radius * 0.7f

        // Axle hanger
        drawLine(ScienceBorder, Offset(sx, ceilingY + blockH), Offset(sx, sy), 3f)

        // Pulley Sheave Wheel
        drawSheaveWheel(Offset(sx, sy), radius, rotAngle * (if (i % 2 == 0) 1f else -1f), color)
    }
}

private fun DrawScope.drawLowerPulleyBlock(
    centerX: Float,
    blockY: Float,
    radius: Float,
    count: Int,
    rotAngle: Float,
    color: Color
) {
    // Sheaves in floating block
    for (i in 0 until count) {
        val offset = (i - (count - 1) * 0.5f) * (radius * 1.05f)
        val sx = centerX + offset
        val sy = blockY

        drawSheaveWheel(Offset(sx, sy), radius, rotAngle * (if (i % 2 == 0) 1f else -1f), color)
    }

    // Heavy Lower Block Frame holding the sheaves
    val blockW = (count * 20.dp.toPx() + 18.dp.toPx()).coerceAtLeast(36.dp.toPx())
    val blockH = 14.dp.toPx()
    val frameY = blockY + radius + 2f
    drawRoundRect(
        color = ScienceDarkSurfaceVariant,
        topLeft = Offset(centerX - blockW * 0.5f, frameY),
        size = Size(blockW, blockH),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = color.copy(alpha = 0.8f),
        topLeft = Offset(centerX - blockW * 0.5f, frameY),
        size = Size(blockW, blockH),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(1.5f)
    )

    // Industrial Crane Hook underneath
    val hookY = frameY + blockH
    drawLine(Color.White, Offset(centerX, hookY), Offset(centerX, hookY + 12f), 3.5f, StrokeCap.Round)
    val hookPath = Path().apply {
        moveTo(centerX, hookY + 12f)
        quadraticTo(centerX + 12f, hookY + 22f, centerX, hookY + 28f)
        quadraticTo(centerX - 12f, hookY + 28f, centerX - 10f, hookY + 18f)
    }
    drawPath(hookPath, AmberVibrant, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
}

private fun DrawScope.drawSheaveWheel(center: Offset, radius: Float, rotAngle: Float, color: Color) {
    // Outer rim groove
    drawCircle(ScienceDarkSurfaceVariant, radius, center)
    drawCircle(color.copy(alpha = 0.85f), radius, center, style = Stroke(width = 2.8f))
    drawCircle(ScienceBorder, radius * 0.65f, center, style = Stroke(width = 1.2f))

    // Rotating spokes
    val spokes = 4
    for (s in 0 until spokes) {
        val a = rotAngle + s * (PI.toFloat() / spokes)
        val cosA = cos(a) * radius * 0.65f
        val sinA = sin(a) * radius * 0.65f
        drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(center.x - cosA, center.y - sinA),
            end = Offset(center.x + cosA, center.y + sinA),
            strokeWidth = 1.5f
        )
    }

    // Center Axle Pin
    drawCircle(Color.Black, 3.5f, center)
    drawCircle(Color.White, 1.8f, center)
}

private fun DrawScope.drawPulleyRopeSystem(
    w: Float,
    centerX: Float,
    topY: Float,
    bottomY: Float,
    sheaveRadius: Float,
    config: PulleyConfig,
    rotAngle: Float,
    ropeTensionN: Float
) {
    val ropeColor = CyanNeon
    val ropeStroke = 2.4f

    val n = config.segments
    val topCount = config.fixedSheaves
    val botCount = config.movableSheaves

    if (n == 1) {
        // Single fixed: Rope comes from load, over top sheave, down to puller
        val sheaveCenter = Offset(centerX, topY + sheaveRadius * 0.7f)
        val leftX = sheaveCenter.x - sheaveRadius
        val rightX = sheaveCenter.x + sheaveRadius

        // Vertical rope to cargo
        drawLine(ropeColor, Offset(leftX, bottomY + 120f), Offset(leftX, sheaveCenter.y), ropeStroke, StrokeCap.Round)
        // Arc over sheave
        val arcPath = Path().apply {
            moveTo(leftX, sheaveCenter.y)
            quadraticTo(sheaveCenter.x, sheaveCenter.y - sheaveRadius, rightX, sheaveCenter.y)
        }
        drawPath(arcPath, ropeColor, style = Stroke(ropeStroke))
        // Free rope angled down to puller hand
        val pullX = w * 0.78f
        val pullY = bottomY + 80f
        drawLine(ropeColor, Offset(rightX, sheaveCenter.y), Offset(pullX, pullY), ropeStroke, StrokeCap.Round)
        drawPullerGrip(Offset(pullX, pullY), ropeTensionN)
        return
    }

    // Multi-sheave threaded rope (n = 2, 4, 8)
    val span = sheaveRadius * 1.05f
    val strandCount = n

    for (s in 0 until strandCount) {
        val strandOffset = (s - (strandCount - 1) * 0.5f) * (span * 0.85f)
        val sx = centerX + strandOffset

        // Vertical supporting strand
        drawLine(
            color = ropeColor,
            start = Offset(sx, topY + 14f),
            end = Offset(sx, bottomY),
            strokeWidth = ropeStroke,
            cap = StrokeCap.Round
        )
    }

    // Top and bottom loops
    for (i in 0 until topCount) {
        val offset = (i - (topCount - 1) * 0.5f) * span
        val sx = centerX + offset
        val sy = topY + 14f
        val loopPath = Path().apply {
            moveTo(sx - sheaveRadius * 0.5f, sy)
            quadraticTo(sx, sy - sheaveRadius * 0.7f, sx + sheaveRadius * 0.5f, sy)
        }
        drawPath(loopPath, ropeColor, style = Stroke(ropeStroke))
    }

    for (i in 0 until botCount) {
        val offset = (i - (botCount - 1) * 0.5f) * span
        val sx = centerX + offset
        val sy = bottomY
        val loopPath = Path().apply {
            moveTo(sx - sheaveRadius * 0.5f, sy)
            quadraticTo(sx, sy + sheaveRadius * 0.7f, sx + sheaveRadius * 0.5f, sy)
        }
        drawPath(loopPath, ropeColor, style = Stroke(ropeStroke))
    }

    // Free rope leading to user puller / winch on the right
    val lastStrandX = centerX + ((strandCount - 1) * 0.5f) * (span * 0.85f)
    val pullerX = w * 0.80f
    val pullerY = bottomY + 60f
    drawLine(ropeColor, Offset(lastStrandX, topY + 14f), Offset(pullerX, pullerY), ropeStroke, StrokeCap.Round)
    drawPullerGrip(Offset(pullerX, pullerY), ropeTensionN)
}

private fun DrawScope.drawPullerGrip(tip: Offset, tensionN: Float) {
    // Pulling Handle / Glove with Downward Arrow
    drawCircle(AmberVibrant, 5.5f, tip)
    drawCircle(Color.Black, 2.5f, tip)

    val arrowLen = 26f
    val arrowEnd = Offset(tip.x + 8f, tip.y + arrowLen)
    drawLine(AmberVibrant, tip, arrowEnd, strokeWidth = 2.5f, cap = StrokeCap.Round)
    val head = Path().apply {
        moveTo(arrowEnd.x + 3f, arrowEnd.y + 4f)
        lineTo(arrowEnd.x - 5f, arrowEnd.y - 2f)
        lineTo(arrowEnd.x + 3f, arrowEnd.y - 6f)
        close()
    }
    drawPath(head, AmberVibrant)
}

private fun DrawScope.drawSuspendedCargo(
    centerX: Float,
    attachY: Float,
    cargo: PulleyCargo,
    mass: Float,
    loadWeightN: Float,
    isDragging: Boolean
) {
    val cargoW = 86.dp.toPx()
    val cargoH = 58.dp.toPx()
    val cargoTop = attachY + 16f
    val cargoLeft = centerX - cargoW * 0.5f

    // Slings / Chains connecting hook to cargo
    drawLine(Color.White.copy(alpha = 0.6f), Offset(centerX, attachY), Offset(cargoLeft + 10f, cargoTop), 2f)
    drawLine(Color.White.copy(alpha = 0.6f), Offset(centerX, attachY), Offset(cargoLeft + cargoW - 10f, cargoTop), 2f)

    // Cargo Body
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(ScienceDarkSurfaceVariant, ScienceDarkSurface),
            startY = cargoTop,
            endY = cargoTop + cargoH
        ),
        topLeft = Offset(cargoLeft, cargoTop),
        size = Size(cargoW, cargoH),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Glowing border (Blue if dragging, else cargo accent)
    val borderColor = if (isDragging) BlueLaser else cargo.color
    drawRoundRect(
        color = borderColor,
        topLeft = Offset(cargoLeft, cargoTop),
        size = Size(cargoW, cargoH),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = if (isDragging) 2.6f else 1.8f)
    )

    // Industrial Hazard Stripes along bottom edge
    val stripeH = 8.dp.toPx()
    val stripeY = cargoTop + cargoH - stripeH
    drawRect(
        color = Color.Black.copy(alpha = 0.4f),
        topLeft = Offset(cargoLeft, stripeY),
        size = Size(cargoW, stripeH)
    )
    var sx = cargoLeft
    while (sx < cargoLeft + cargoW) {
        drawLine(
            color = AmberVibrant.copy(alpha = 0.7f),
            start = Offset(sx, stripeY),
            end = Offset(sx - 6f, stripeY + stripeH),
            strokeWidth = 2f
        )
        sx += 12f
    }

    // Mass Label Emblem in center
    drawCircle(ScienceDarkSurface, 12.dp.toPx(), Offset(centerX, cargoTop + cargoH * 0.42f))
    drawCircle(borderColor, 12.dp.toPx(), Offset(centerX, cargoTop + cargoH * 0.42f), style = Stroke(1.2f))
}

private fun DrawScope.drawPulleyForceVectors(
    centerX: Float,
    topY: Float,
    bottomY: Float,
    cargoY: Float,
    n: Int,
    tensionN: Float,
    weightN: Float,
    sheaveRadius: Float
) {
    val span = sheaveRadius * 1.05f
    val strandCount = n
    val arrowLen = 22f

    // 1. Upward Tension Arrows on each supporting strand (Cyan)
    for (s in 0 until strandCount) {
        val strandOffset = (s - (strandCount - 1) * 0.5f) * (span * 0.85f)
        val sx = centerX + strandOffset
        val midY = (topY + bottomY) * 0.5f

        // Upward tension arrow
        val tip = Offset(sx, midY - arrowLen * 0.5f)
        val base = Offset(sx, midY + arrowLen * 0.5f)
        drawLine(CyanNeon, base, tip, strokeWidth = 2.2f, cap = StrokeCap.Round)
        val head = Path().apply {
            moveTo(tip.x, tip.y - 3f)
            lineTo(tip.x - 3f, tip.y + 3f)
            lineTo(tip.x + 3f, tip.y + 3f)
            close()
        }
        drawPath(head, CyanNeon)
    }

    // 2. Downward Gravity Force Vector W (Coral/Red)
    val wStart = Offset(centerX, cargoY + 76f)
    val wEnd = Offset(centerX, wStart.y + 36f)
    drawLine(CoralNeon, wStart, wEnd, strokeWidth = 3f, cap = StrokeCap.Round)
    val wHead = Path().apply {
        moveTo(wEnd.x, wEnd.y + 4f)
        lineTo(wEnd.x - 4f, wEnd.y - 4f)
        lineTo(wEnd.x + 4f, wEnd.y - 4f)
        close()
    }
    drawPath(wHead, CoralNeon)
}

/**
 * Completely transparent HUD displaying live physics metrics
 * so the user stays 100% focused on the experiment with no opaque card blocking the screen.
 */
@Composable
private fun TransparentTelemetryHud(
    modifier: Modifier = Modifier,
    title: String,
    items: List<Pair<String, String>>
) {
    Column(
        modifier = modifier
            .background(Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 $title".uppercase(),
                color = CyanNeon.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Surface(
                color = CyanNeon.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "LIVE",
                    color = CyanNeon,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = ScienceBorder.copy(alpha = 0.35f), thickness = 0.8.dp)
        Spacer(Modifier.height(4.dp))
        items.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = TextSecondary.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


