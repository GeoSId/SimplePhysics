package com.geosid.simplephysics.ui.experiments.week1.Day7

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
import androidx.compose.ui.graphics.drawscope.rotate
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

enum class BallType(val title: String, val icon: String, val massGram: Float, val ballColor: Color) {
    PING_PONG("Ping-Pong", "🏓", 2.7f, Color(0xFFFFB74D)), // Lightweight plastic
    FOAM("Foam Ball", "⚪", 1.2f, Color(0xFFE0E0E0)), // Ultra lightweight
    WOODEN("Wooden Ball", "🪵", 20.0f, Color(0xFF8D6E63)) // Heavy
}

@Composable
fun BernoulliBallExperiment(
    modifier: Modifier = Modifier
) {
    var ballType by remember { mutableStateOf(BallType.PING_PONG) }
    var airSpeed by remember { mutableStateOf(16f) } // m/s (10 to 30)
    var tiltAngleDeg by remember { mutableStateOf(0f) } // degrees (-40 to +40)
    var isRunning by remember { mutableStateOf(true) }

    // Ball simulation physics state (relative to screen canvas)
    var ballPos by remember { mutableStateOf<Offset?>(null) }
    var ballVelocity by remember { mutableStateOf(Offset.Zero) }
    var isDraggingBall by remember { mutableStateOf(false) }

    // Continuous flow animation
    val infiniteTransition = rememberInfiniteTransition()
    val flowAnimPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Air & Ball Physics Constants
    val airDensity = 1.225f // kg/m^3
    val dynamicPressure = 0.5f * airDensity * airSpeed * airSpeed // Pa (q = 1/2 rho v^2)
    val ballRadiusM = 0.020f // 40mm diameter
    val ballArea = PI.toFloat() * ballRadiusM * ballRadiusM
    val cd = 0.47f // Sphere drag coefficient

    // Physics Loop
    LaunchedEffect(isRunning, airSpeed, tiltAngleDeg, ballType) {
        if (isRunning) {
            var lastNanos = withFrameNanos { it }
            while (isRunning) {
                val currentNanos = withFrameNanos { it }
                val dt = ((currentNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastNanos = currentNanos

                if (!isDraggingBall && ballPos != null) {
                    val pos = ballPos!!
                    val rad = tiltAngleDeg * (PI.toFloat() / 180f)
                    val streamDir = Offset(sin(rad), -cos(rad))
                    val streamNormal = Offset(cos(rad), sin(rad))

                    // Nozzle anchor is bottom center
                    val nozzleAnchor = Offset(0f, 0f) // Computed relative to nozzle
                    val toBall = pos - nozzleAnchor
                    val distAlongStream = toBall.x * streamDir.x + toBall.y * streamDir.y
                    val distPerpToStream = toBall.x * streamNormal.x + toBall.y * streamNormal.y

                    // Stream expands with distance
                    val jetWidth = (35f + distAlongStream * 0.18f).coerceAtLeast(30f)
                    val inJetCore = abs(distPerpToStream) < jetWidth * 1.5f && distAlongStream > 20f && distAlongStream < 480f

                    val g = 9.81f * 65f // scaled gravity
                    var forceX = 0f
                    var forceY = g * (ballType.massGram / 2.7f) // Gravity down

                    if (inJetCore) {
                        // 1. Bernoulli Suction Force: inward toward stream center (low pressure core)
                        val pressureGradient = -(distPerpToStream / jetWidth) * (dynamicPressure * 1.8f)
                        forceX += streamNormal.x * pressureGradient
                        forceY += streamNormal.y * pressureGradient

                        // 2. Aerodynamic Drag along the air jet
                        val speedAtHeight = (airSpeed * (120f / (120f + distAlongStream * 0.35f))).coerceAtLeast(4f)
                        val dragMag = 0.5f * airDensity * speedAtHeight * speedAtHeight * cd * ballArea * 1800f
                        forceX += streamDir.x * dragMag
                        forceY += streamDir.y * dragMag

                        // Aerodynamic turbulence wobble
                        val wobble = sin(distAlongStream * 0.1f + flowAnimPhase * 15f) * 4f
                        forceX += streamNormal.x * wobble
                    }

                    // Acceleration & Velocity Update
                    val m = (ballType.massGram / 2.7f).coerceAtLeast(0.4f)
                    val accelX = forceX / m
                    val accelY = forceY / m

                    val damping = 3.5f
                    ballVelocity = Offset(
                        ballVelocity.x + (accelX - damping * ballVelocity.x) * dt,
                        ballVelocity.y + (accelY - damping * ballVelocity.y) * dt
                    )

                    val newPos = Offset(pos.x + ballVelocity.x * dt, pos.y + ballVelocity.y * dt)
                    ballPos = newPos
                }
            }
        }
    }

    val streamRad = tiltAngleDeg * (PI.toFloat() / 180f)
    val streamNormal = Offset(cos(streamRad), sin(streamRad))
    val streamDir = Offset(sin(streamRad), -cos(streamRad))

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Tilt the blower nozzle or pull the ping-pong ball away with your finger! Watch Bernoulli's low-pressure core pull it back.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDraggingBall = true
                                ballPos = offset
                                ballVelocity = Offset.Zero
                            },
                            onDragEnd = {
                                isDraggingBall = false
                            },
                            onDragCancel = {
                                isDraggingBall = false
                            },
                            onDrag = { change, _ ->
                                ballPos = change.position
                                ballVelocity = Offset.Zero
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            ballPos = offset
                            ballVelocity = Offset.Zero
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val nozzlePos = Offset(w * 0.5f, h * 0.82f)

                // Initialize ball position at stable hover height if not set
                if (ballPos == null) {
                    val defaultHoverDist = 200f + (airSpeed - 16f) * 8f
                    ballPos = nozzlePos + Offset(sin(streamRad) * defaultHoverDist, -cos(streamRad) * defaultHoverDist)
                }

                val currentBallPos = ballPos!!
                val ballRadiusPx = min(w, h) * 0.050f

                // 1. Subtle Scientific Grid
                drawScientificGrid(w, h)

                // 2. Base Floor Shadow
                drawOval(
                    color = Color.Black.copy(alpha = 0.45f),
                    topLeft = Offset(nozzlePos.x - 70f, nozzlePos.y + 24f),
                    size = Size(140f, 18f)
                )

                // 3. Airflow Jet Column (Bernoulli stream cone)
                drawAirflowJet(
                    nozzlePos = nozzlePos,
                    tiltRad = streamRad,
                    airSpeed = airSpeed,
                    animPhase = flowAnimPhase,
                    ballPos = currentBallPos,
                    ballRadius = ballRadiusPx
                )

                // 4. Inward Bernoulli Pressure Differential Vectors
                val toBall = currentBallPos - nozzlePos
                val perpDist = toBall.x * streamNormal.x + toBall.y * streamNormal.y
                val alongDist = toBall.x * streamDir.x + toBall.y * streamDir.y
                val inJet = abs(perpDist) < 95f && alongDist > 20f && alongDist < 460f

                if (inJet) {
                    drawBernoulliPressureArrows(
                        ballPos = currentBallPos,
                        streamNormal = streamNormal,
                        perpDist = perpDist,
                        ballRadius = ballRadiusPx
                    )
                }

                // 5. Levitating Ping-Pong Ball (3D sphere with specular shine)
                drawLevitatingBall(
                    center = currentBallPos,
                    radius = ballRadiusPx,
                    ballType = ballType,
                    isTrapped = inJet
                )

                // 6. Hairdryer / Blower Nozzle at Base (rotates with tilt)
                drawBlowerNozzle(
                    nozzlePos = nozzlePos,
                    tiltDeg = tiltAngleDeg,
                    airSpeed = airSpeed
                )
            }
        },
        hudContent = {
            val toBall = (ballPos ?: Offset.Zero) - Offset(0f, 0f)
            val perpDist = toBall.x * streamNormal.x + toBall.y * streamNormal.y
            val isTrapped = abs(perpDist) < 90f

            TransparentTelemetryHud(
                modifier = Modifier.fillMaxWidth(),
                title = "Bernoulli & Coandă Telemetry",
                items = listOf(
                    "Airflow Speed (v)" to "${round(airSpeed * 10f) / 10f} m/s",
                    "Dynamic Pressure (q)" to "${(round(dynamicPressure * 10f) / 10f)} Pa (½ρv²)",
                    "Nozzle Tilt Angle" to "${tiltAngleDeg.toInt()}° (Coandă Lift)",
                    "Levitation State" to if (isTrapped) "✨ STABLE (TRAPPED IN JET)" else "⚠️ ESCAPED JET (FALLING)"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Ball Type Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BallType.values().forEach { type ->
                        FilterChip(
                            selected = ballType == type,
                            onClick = {
                                ballType = type
                                ballVelocity = Offset.Zero
                            },
                            label = {
                                Text(
                                    text = "${type.icon} ${type.title}",
                                    fontSize = 11.sp,
                                    fontWeight = if (ballType == type) FontWeight.Bold else FontWeight.Normal
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
                                selected = ballType == type,
                                borderColor = ScienceBorder.copy(alpha = 0.4f),
                                selectedBorderColor = CyanNeon
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Sliders: Airflow Velocity & Stream Tilt Angle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Airflow Speed (v)",
                            value = airSpeed,
                            range = 10f..30f,
                            valueDisplay = "${airSpeed.toInt()} m/s",
                            accentColor = CyanNeon,
                            onValueChange = { airSpeed = it }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Nozzle Tilt Angle",
                            value = tiltAngleDeg,
                            range = -40f..40f,
                            valueDisplay = "${tiltAngleDeg.toInt()}°",
                            accentColor = AmberVibrant,
                            onValueChange = { tiltAngleDeg = it }
                        )
                    }
                }

                // Transport Row
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
                            ballType = BallType.PING_PONG
                            airSpeed = 16f
                            tiltAngleDeg = 0f
                            ballPos = null
                            ballVelocity = Offset.Zero
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

// ---------------------------------------------------------------------------
// High-Fidelity Drawing Helpers
// ---------------------------------------------------------------------------

/**
 * Airflow jet column with diverging stream particles and Coandă curve
 */
private fun DrawScope.drawAirflowJet(
    nozzlePos: Offset,
    tiltRad: Float,
    airSpeed: Float,
    animPhase: Float,
    ballPos: Offset,
    ballRadius: Float
) {
    val streamDir = Offset(sin(tiltRad), -cos(tiltRad))
    val streamNormal = Offset(cos(tiltRad), sin(tiltRad))
    val streamLength = 460f

    // 1. Conical Translucent Air Jet Glow (High Velocity = Low Pressure Core)
    val leftBase = nozzlePos - streamNormal * 16f
    val rightBase = nozzlePos + streamNormal * 16f
    val leftTip = nozzlePos + streamDir * streamLength - streamNormal * 75f
    val rightTip = nozzlePos + streamDir * streamLength + streamNormal * 75f

    val conePath = Path().apply {
        moveTo(leftBase.x, leftBase.y)
        lineTo(leftTip.x, leftTip.y)
        lineTo(rightTip.x, rightTip.y)
        lineTo(rightBase.x, rightBase.y)
        close()
    }
    drawPath(
        path = conePath,
        brush = Brush.radialGradient(
            colors = listOf(CyanNeon.copy(alpha = 0.28f), CyanNeon.copy(alpha = 0.05f), Color.Transparent),
            center = nozzlePos + streamDir * (streamLength * 0.45f),
            radius = streamLength * 0.65f
        )
    )

    // 2. Animated Streamlines (flowing upward)
    val streamlineCount = 7
    for (i in 0 until streamlineCount) {
        val t = (i / (streamlineCount - 1).toFloat()) - 0.5f // -0.5 to +0.5
        val baseOffset = streamNormal * (t * 26f)
        val tipOffset = streamNormal * (t * 130f)

        val startPt = nozzlePos + baseOffset
        val endPt = nozzlePos + streamDir * streamLength + tipOffset

        // Dashed streamline moving upward
        val streamSpeedOffset = (animPhase * 50f * (airSpeed / 16f)) % 32f
        drawLine(
            color = CyanNeon.copy(alpha = 0.40f * (1f - abs(t) * 1.2f).coerceAtLeast(0.1f)),
            start = startPt,
            end = endPt,
            strokeWidth = 1.6f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), streamSpeedOffset)
        )
    }

    // 3. Coandă Effect Streamlines Wrapping Around Ball
    val toBall = ballPos - nozzlePos
    val perp = toBall.x * streamNormal.x + toBall.y * streamNormal.y
    val along = toBall.x * streamDir.x + toBall.y * streamDir.y

    if (abs(perp) < 70f && along > 40f && along < streamLength) {
        // Curve air tightly around the top of the sphere
        drawArc(
            color = CyanNeon.copy(alpha = 0.85f),
            startAngle = if (perp >= 0f) -120f else -60f,
            sweepAngle = if (perp >= 0f) 80f else -80f,
            useCenter = false,
            topLeft = Offset(ballPos.x - ballRadius * 1.35f, ballPos.y - ballRadius * 1.35f),
            size = Size(ballRadius * 2.7f, ballRadius * 2.7f),
            style = Stroke(width = 2.5f)
        )
    }
}

/**
 * Inward Pressure Differential Arrows (Bernoulli Suction)
 */
private fun DrawScope.drawBernoulliPressureArrows(
    ballPos: Offset,
    streamNormal: Offset,
    perpDist: Float,
    ballRadius: Float
) {
    // If displaced left (perpDist < 0), ambient pressure on left pushes RIGHT
    val pushDir = if (perpDist < 0f) streamNormal else -streamNormal
    val arrowLen = (abs(perpDist) * 0.85f + 14f).coerceIn(16f, 44f)
    val arrowStart = ballPos - pushDir * (ballRadius + arrowLen)
    val arrowEnd = ballPos - pushDir * (ballRadius + 4f)

    // Glowing inward arrow
    drawLine(
        color = AmberVibrant,
        start = arrowStart,
        end = arrowEnd,
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
    val head = Path().apply {
        moveTo(arrowEnd.x, arrowEnd.y)
        val normalHead = Offset(-pushDir.y, pushDir.x)
        val back = arrowEnd - pushDir * 6f
        lineTo(back.x + normalHead.x * 4f, back.y + normalHead.y * 4f)
        lineTo(back.x - normalHead.x * 4f, back.y - normalHead.y * 4f)
        close()
    }
    drawPath(head, AmberVibrant)
}

/**
 * Levitating Ping-Pong Ball (3D shaded sphere)
 */
private fun DrawScope.drawLevitatingBall(
    center: Offset,
    radius: Float,
    ballType: BallType,
    isTrapped: Boolean
) {
    val highlightOffset = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)

    // Base radial gradient
    val baseColors = when (ballType) {
        BallType.PING_PONG -> listOf(Color(0xFFFFF3E0), Color(0xFFFFB74D), Color(0xFFE65100))
        BallType.FOAM -> listOf(Color.White, Color(0xFFE0E0E0), Color(0xFF757575))
        BallType.WOODEN -> listOf(Color(0xFFD7CCC8), Color(0xFF8D6E63), Color(0xFF3E2723))
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = baseColors,
            center = highlightOffset,
            radius = radius * 1.3f
        ),
        radius = radius,
        center = center
    )

    // Specular highlight spot
    drawCircle(
        color = Color.White.copy(alpha = 0.90f),
        radius = radius * 0.22f,
        center = highlightOffset
    )

    // Outer rim glow if trapped in low-pressure stream
    drawCircle(
        color = if (isTrapped) CyanNeon.copy(alpha = 0.8f) else ScienceBorder.copy(alpha = 0.5f),
        radius = radius,
        center = center,
        style = Stroke(width = if (isTrapped) 2f else 1f)
    )
}

/**
 * Hairdryer Blower Nozzle on swivel base
 */
private fun DrawScope.drawBlowerNozzle(
    nozzlePos: Offset,
    tiltDeg: Float,
    airSpeed: Float
) {
    // Swivel Base Stand
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF607D8B), Color(0xFF263238))),
        topLeft = Offset(nozzlePos.x - 35f, nozzlePos.y + 12f),
        size = Size(70f, 16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Rotated Blower Barrel
    rotate(degrees = tiltDeg, pivot = nozzlePos) {
        val barrelW = 34f
        val barrelH = 48f

        // Blower Tube (Chrome metallic)
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF78909C), Color(0xFFECEFF1), Color(0xFF37474F))
            ),
            topLeft = Offset(nozzlePos.x - barrelW * 0.5f, nozzlePos.y - barrelH * 0.5f),
            size = Size(barrelW, barrelH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )

        // Nozzle Rim
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(CyanNeon, Color(0xFF006064)),
                center = Offset(nozzlePos.x, nozzlePos.y - barrelH * 0.5f),
                radius = barrelW * 0.5f
            ),
            topLeft = Offset(nozzlePos.x - barrelW * 0.5f, nozzlePos.y - barrelH * 0.5f - 4f),
            size = Size(barrelW, 8f)
        )

        // Exhaust Flow Turbine Glow
        drawCircle(
            color = CyanNeon.copy(alpha = (airSpeed / 30f).coerceIn(0.4f, 0.95f)),
            radius = 6f,
            center = Offset(nozzlePos.x, nozzlePos.y - barrelH * 0.5f)
        )
    }
}

/**
 * Coordinate Grid Background
 */
private fun DrawScope.drawScientificGrid(w: Float, h: Float) {
    val step = 36f
    var x = 0f
    while (x < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.12f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 0.6f
        )
        x += step
    }
    var y = 0f
    while (y < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.12f),
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
                text = "💨 $title".uppercase(),
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
                    text = "BERNOULLI",
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
