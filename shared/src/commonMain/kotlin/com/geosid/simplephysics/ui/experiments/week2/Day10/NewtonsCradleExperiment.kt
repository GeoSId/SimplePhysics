package com.geosid.simplephysics.ui.experiments.week2.Day10

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.drawscope.Stroke
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

private const val NUM_BALLS = 5
private const val GRAVITY = 9.81f
private const val STRING_LENGTH = 0.38f // meters
private const val BALL_MASS = 0.12f // kg each

enum class CradlePreset(val title: String, val icon: String, val angles: FloatArray) {
    ONE_BALL("1 Ball", "⚪", floatArrayOf(-0.75f, 0f, 0f, 0f, 0f)),
    TWO_BALLS("2 Balls", "⚪⚪", floatArrayOf(-0.75f, -0.75f, 0f, 0f, 0f)),
    THREE_BALLS("3 Balls", "⚪⚪⚪", floatArrayOf(-0.75f, -0.75f, -0.75f, 0f, 0f)),
    DUEL("Duel (1 vs 1)", "⚔️", floatArrayOf(-0.75f, 0f, 0f, 0f, 0.75f))
}

@Composable
fun NewtonsCradleExperiment(
    modifier: Modifier = Modifier
) {
    // Physical state for each of the 5 balls
    val angles = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f) }
    val angularVelocities = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f) }
    val impulseFlashes = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f) }

    // Simulation controls
    var isRunning by remember { mutableStateOf(true) }
    var restitution by remember { mutableStateOf(0.99f) } // Elasticity: 0.80 to 1.00
    var damping by remember { mutableStateOf(0.002f) } // Air damping factor
    var collisionCount by remember { mutableStateOf(0) }
    var draggedBallIndex by remember { mutableStateOf<Int?>(null) }
    var selectedPreset by remember { mutableStateOf<CradlePreset?>(CradlePreset.ONE_BALL) }

    // Initialize with 1 Ball preset pulled back
    LaunchedEffect(Unit) {
        val preset = CradlePreset.ONE_BALL
        for (i in 0 until NUM_BALLS) {
            angles[i] = preset.angles[i]
            angularVelocities[i] = 0f
            impulseFlashes[i] = 0f
        }
    }

    // High-precision physics simulation loop
    LaunchedEffect(isRunning, restitution, damping) {
        if (isRunning) {
            var lastNanos = withFrameNanos { it }
            while (isRunning) {
                val currentNanos = withFrameNanos { it }
                val dt = ((currentNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastNanos = currentNanos

                // Sub-stepping for ultra-stable collision resolution
                val subSteps = 16
                val dtSub = dt / subSteps

                for (step in 0 until subSteps) {
                    // 1. Angular gravity and damping integration
                    for (i in 0 until NUM_BALLS) {
                        if (draggedBallIndex != i) {
                            val alpha = -(GRAVITY / STRING_LENGTH) * sin(angles[i]) - (damping * 60f) * angularVelocities[i]
                            angularVelocities[i] += alpha * dtSub
                            angles[i] += angularVelocities[i] * dtSub
                        }
                    }

                    // 2. Collision detection and momentum transfer (sweep left-to-right and right-to-left)
                    for (pass in 0 until 2) {
                        val indices = if (pass == 0) 0 until (NUM_BALLS - 1) else (NUM_BALLS - 2) downTo 0
                        for (i in indices) {
                            val next = i + 1
                            if (angles[i] > angles[next]) {
                                val relV = angularVelocities[i] - angularVelocities[next]
                                if (relV > 0f) {
                                    // 1D elastic collision between equal mass balls
                                    val u1 = angularVelocities[i]
                                    val u2 = angularVelocities[next]

                                    val v1 = (u1 + u2 - restitution * (u1 - u2)) / 2f
                                    val v2 = (u1 + u2 + restitution * (u1 - u2)) / 2f

                                    angularVelocities[i] = v1
                                    angularVelocities[next] = v2

                                    // Trigger impulse shockwave
                                    impulseFlashes[i] = 1f
                                    impulseFlashes[next] = 1f
                                    collisionCount++
                                }

                                // Separate overlapping spheres
                                val mid = (angles[i] + angles[next]) / 2f
                                angles[i] = mid
                                angles[next] = mid
                            }
                        }
                    }
                }

                // Decay impulse visual flashes
                for (i in 0 until NUM_BALLS) {
                    if (impulseFlashes[i] > 0f) {
                        impulseFlashes[i] = max(0f, impulseFlashes[i] - dt * 4f)
                    }
                }
            }
        }
    }

    // Telemetry calculations
    var totalMomentum = 0f
    var totalKineticEnergy = 0f
    for (i in 0 until NUM_BALLS) {
        val linearV = abs(angularVelocities[i] * STRING_LENGTH)
        totalMomentum += BALL_MASS * linearV
        totalKineticEnergy += 0.5f * BALL_MASS * linearV * linearV
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag any ball outward with your finger to pull and release, or tap presets below to test momentum transfer!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val w = size.width
                                val h = size.height
                                val cradleCenter = Offset(w * 0.5f, h * 0.22f)
                                val ballRadiusPx = min(w, h) * 0.052f
                                val stringLenPx = min(w, h) * 0.44f

                                // Find closest ball to touch
                                var bestDist = Float.MAX_VALUE
                                var bestIndex = -1
                                for (i in 0 until NUM_BALLS) {
                                    val pivotX = cradleCenter.x + (i - 2) * (ballRadiusPx * 2f)
                                    val bx = pivotX + stringLenPx * sin(angles[i])
                                    val by = cradleCenter.y + stringLenPx * cos(angles[i])
                                    val dist = hypot(offset.x - bx, offset.y - by)
                                    if (dist < bestDist && dist < ballRadiusPx * 2.5f) {
                                        bestDist = dist
                                        bestIndex = i
                                    }
                                }

                                if (bestIndex != -1) {
                                    draggedBallIndex = bestIndex
                                    selectedPreset = null
                                }
                            },
                            onDragEnd = {
                                draggedBallIndex = null
                            },
                            onDragCancel = {
                                draggedBallIndex = null
                            },
                            onDrag = { change, _ ->
                                draggedBallIndex?.let { idx ->
                                    val w = size.width
                                    val h = size.height
                                    val cradleCenter = Offset(w * 0.5f, h * 0.22f)
                                    val ballRadiusPx = min(w, h) * 0.052f
                                    val pivotX = cradleCenter.x + (idx - 2) * (ballRadiusPx * 2f)

                                    val dx = change.position.x - pivotX
                                    val dy = max(10f, change.position.y - cradleCenter.y)
                                    val targetAngle = atan2(dx, dy).coerceIn(-1.15f, 1.15f)

                                    angles[idx] = targetAngle
                                    angularVelocities[idx] = 0f

                                    // Push neighboring balls if moving outward
                                    if (targetAngle < 0f) {
                                        for (k in 0 until idx) {
                                            if (angles[k] > targetAngle) {
                                                angles[k] = targetAngle
                                                angularVelocities[k] = 0f
                                            }
                                        }
                                    } else {
                                        for (k in (idx + 1) until NUM_BALLS) {
                                            if (angles[k] < targetAngle) {
                                                angles[k] = targetAngle
                                                angularVelocities[k] = 0f
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Tap to toggle run/pause
                            isRunning = !isRunning
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                val cradleCenter = Offset(w * 0.5f, h * 0.22f)
                val ballRadiusPx = min(w, h) * 0.052f
                val stringLenPx = min(w, h) * 0.44f
                val frameHalfWidth = ballRadiusPx * 5.8f
                val groundY = cradleCenter.y + stringLenPx + ballRadiusPx * 2.2f

                // 1. Subtle Scientific Grid
                drawScientificGrid(w, h)

                // 2. Floor Shadow under Cradle
                drawOval(
                    color = Color.Black.copy(alpha = 0.35f),
                    topLeft = Offset(cradleCenter.x - frameHalfWidth * 0.9f, groundY - 6f),
                    size = Size(frameHalfWidth * 1.8f, 16f)
                )

                // 3. Overhead Support Frame (Chrome metallic bar with pillars)
                drawCradleFrame(
                    cradleCenter = cradleCenter,
                    halfWidth = frameHalfWidth,
                    groundY = groundY
                )

                // 4. Calculate individual ball positions
                val ballCenters = Array(NUM_BALLS) { Offset.Zero }
                val pivotPoints = Array(NUM_BALLS) { Offset.Zero }

                for (i in 0 until NUM_BALLS) {
                    val pivotX = cradleCenter.x + (i - 2) * (ballRadiusPx * 2f)
                    val pivot = Offset(pivotX, cradleCenter.y)
                    pivotPoints[i] = pivot

                    val theta = angles[i]
                    val bx = pivotX + stringLenPx * sin(theta)
                    val by = cradleCenter.y + stringLenPx * cos(theta)
                    ballCenters[i] = Offset(bx, by)
                }

                // 5. Draw Dual V-Suspension Strings for each ball
                for (i in 0 until NUM_BALLS) {
                    val pivot = pivotPoints[i]
                    val bCenter = ballCenters[i]
                    val vSpread = ballRadiusPx * 0.55f

                    // Left string of the V
                    drawLine(
                        color = Color.White.copy(alpha = 0.70f),
                        start = Offset(pivot.x - vSpread, pivot.y),
                        end = Offset(bCenter.x, bCenter.y - ballRadiusPx),
                        strokeWidth = 1.4f,
                        cap = StrokeCap.Round
                    )

                    // Right string of the V
                    drawLine(
                        color = Color.White.copy(alpha = 0.70f),
                        start = Offset(pivot.x + vSpread, pivot.y),
                        end = Offset(bCenter.x, bCenter.y - ballRadiusPx),
                        strokeWidth = 1.4f,
                        cap = StrokeCap.Round
                    )

                    // Top mount screw/peg
                    drawCircle(
                        color = Color(0xFFCFD8DC),
                        radius = 2.5f,
                        center = Offset(pivot.x - vSpread, pivot.y)
                    )
                    drawCircle(
                        color = Color(0xFFCFD8DC),
                        radius = 2.5f,
                        center = Offset(pivot.x + vSpread, pivot.y)
                    )
                }

                // 6. Draw 3D Chrome Steel Spheres & Impulse Waves
                for (i in 0 until NUM_BALLS) {
                    val bCenter = ballCenters[i]
                    val flash = impulseFlashes[i]
                    val speed = abs(angularVelocities[i])

                    // Motion speed trail if moving fast
                    if (speed > 1.2f) {
                        val trailDir = -sign(angularVelocities[i])
                        for (t in 1..3) {
                            val trailAngle = angles[i] + trailDir * (0.04f * t)
                            val tx = pivotPoints[i].x + stringLenPx * sin(trailAngle)
                            val ty = cradleCenter.y + stringLenPx * cos(trailAngle)
                            drawCircle(
                                color = CyanNeon.copy(alpha = 0.18f / t),
                                radius = ballRadiusPx * (1f - 0.08f * t),
                                center = Offset(tx, ty)
                            )
                        }
                    }

                    // Collision Impulse Shockwave Ring
                    if (flash > 0.05f) {
                        val ringRadius = ballRadiusPx + (1f - flash) * 22f
                        drawCircle(
                            color = CyanNeon.copy(alpha = flash * 0.8f),
                            radius = ringRadius,
                            center = bCenter,
                            style = Stroke(width = 2.5f)
                        )
                    }

                    // Dynamic Ball Shadow on Floor
                    val shadowAlpha = (0.35f * (1f - (bCenter.y - (cradleCenter.y + stringLenPx)) / 80f)).coerceIn(0.08f, 0.45f)
                    drawOval(
                        color = Color.Black.copy(alpha = shadowAlpha),
                        topLeft = Offset(bCenter.x - ballRadiusPx * 0.7f, groundY - 4f),
                        size = Size(ballRadiusPx * 1.4f, 8f)
                    )

                    // High-Fidelity 3D Metallic Sphere Shader
                    drawChromeSphere(
                        center = bCenter,
                        radius = ballRadiusPx,
                        isDragged = draggedBallIndex == i
                    )
                }
            }
        },
        hudContent = {
            TransparentTelemetryHud(
                modifier = Modifier.fillMaxWidth(),
                title = "Conservation of Momentum & Energy",
                items = listOf(
                    "Total Momentum (P)" to "${(round(totalMomentum * 1000f) / 1000f)} kg·m/s",
                    "Kinetic Energy (Ek)" to "${(round(totalKineticEnergy * 1000f) / 1000f)} J",
                    "Elasticity (e)" to "${(round(restitution * 1000f) / 10f)}% (${if (restitution >= 0.98f) "Hardened Steel" else "Inelastic"})",
                    "Impacts Count" to "$collisionCount"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preset Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CradlePreset.values().forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = {
                                selectedPreset = preset
                                for (i in 0 until NUM_BALLS) {
                                    angles[i] = preset.angles[i]
                                    angularVelocities[i] = 0f
                                    impulseFlashes[i] = 0f
                                }
                                isRunning = true
                            },
                            label = {
                                Text(
                                    text = "${preset.icon} ${preset.title}",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedPreset == preset) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.20f),
                                selectedLabelColor = CyanNeon,
                                containerColor = ScienceDarkSurfaceVariant.copy(alpha = 0.5f),
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedPreset == preset,
                                borderColor = ScienceBorder.copy(alpha = 0.4f),
                                selectedBorderColor = CyanNeon
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Sliders Row (Elasticity Restitution & Air Damping)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Elasticity (e)",
                            value = restitution,
                            range = 0.85f..1.00f,
                            valueDisplay = "${(round(restitution * 1000f) / 10f)}%",
                            accentColor = CyanNeon,
                            onValueChange = {
                                restitution = it
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Air Damping",
                            value = damping,
                            range = 0.000f..0.010f,
                            valueDisplay = if (damping == 0f) "None" else "${round(damping * 10000f) / 10f}x",
                            accentColor = AmberVibrant,
                            onValueChange = {
                                damping = it
                            }
                        )
                    }
                }

                // Transport Controls
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
                            text = if (isRunning) "⏸ Pause" else "▶ Run",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            selectedPreset = CradlePreset.ONE_BALL
                            for (i in 0 until NUM_BALLS) {
                                angles[i] = CradlePreset.ONE_BALL.angles[i]
                                angularVelocities[i] = 0f
                                impulseFlashes[i] = 0f
                            }
                            restitution = 0.99f
                            damping = 0.002f
                            collisionCount = 0
                            isRunning = true
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(10.dp))
                    ) {
                        ResetIcon(tint = CyanNeon, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    )
}

/**
 * High-performance 3D Chrome Metallic Sphere Drawing
 */
private fun DrawScope.drawChromeSphere(
    center: Offset,
    radius: Float,
    isDragged: Boolean
) {
    // 1. Base metallic radial gradient
    val highlightOffset = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                Color(0xFFE0E0E0),
                Color(0xFF90A4AE),
                Color(0xFF455A64),
                Color(0xFF1C2833)
            ),
            center = highlightOffset,
            radius = radius * 1.25f
        ),
        radius = radius,
        center = center
    )

    // 2. Specular intense white highlight spot
    drawCircle(
        color = Color.White.copy(alpha = 0.95f),
        radius = radius * 0.22f,
        center = highlightOffset
    )

    // 3. Crisp chrome outer rim
    drawCircle(
        color = if (isDragged) AmberVibrant else Color(0xFFCFD8DC),
        radius = radius,
        center = center,
        style = Stroke(width = if (isDragged) 2f else 1f)
    )

    // 4. Subtle bottom reflected light
    drawArc(
        color = Color.White.copy(alpha = 0.35f),
        startAngle = 45f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 0.85f, center.y - radius * 0.85f),
        size = Size(radius * 1.7f, radius * 1.7f),
        style = Stroke(width = 1.2f)
    )
}

/**
 * Chrome Support Bar and Dual Pillars Frame
 */
private fun DrawScope.drawCradleFrame(
    cradleCenter: Offset,
    halfWidth: Float,
    groundY: Float
) {
    val barHeight = 8f
    val barTop = cradleCenter.y - barHeight * 0.5f

    // Top Chrome Crossbar
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White, Color(0xFFB0BEC5), Color(0xFF37474F))
        ),
        topLeft = Offset(cradleCenter.x - halfWidth - 16f, barTop),
        size = Size((halfWidth + 16f) * 2f, barHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Left Pillar
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF78909C), Color(0xFFECEFF1), Color(0xFF455A64))
        ),
        start = Offset(cradleCenter.x - halfWidth - 10f, barTop),
        end = Offset(cradleCenter.x - halfWidth - 10f, groundY),
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )

    // Right Pillar
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF78909C), Color(0xFFECEFF1), Color(0xFF455A64))
        ),
        start = Offset(cradleCenter.x + halfWidth + 10f, barTop),
        end = Offset(cradleCenter.x + halfWidth + 10f, groundY),
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )

    // Frame Feet at base
    drawRoundRect(
        color = Color(0xFF37474F),
        topLeft = Offset(cradleCenter.x - halfWidth - 22f, groundY - 4f),
        size = Size(24f, 8f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = Color(0xFF37474F),
        topLeft = Offset(cradleCenter.x + halfWidth - 2f, groundY - 4f),
        size = Size(24f, 8f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
    )
}

/**
 * Subtle Background Coordinate Grid
 */
private fun DrawScope.drawScientificGrid(w: Float, h: Float) {
    val step = 36f
    var x = 0f
    while (x < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.15f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 0.6f
        )
        x += step
    }
    var y = 0f
    while (y < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.15f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 0.6f
        )
        y += step
    }
}

/**
 * Transparent Telemetry HUD
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
                text = "⚡ $title".uppercase(),
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
                    text = "CONSERVED",
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
