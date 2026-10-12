package com.geosid.simplephysics.ui.experiments.week6.Day36

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
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
 * Presets for Day 36: The Magnus Effect (Curving Balls).
 */
enum class MagnusPreset(
    val label: String,
    val title: String,
    val spinRpm: Float,
    val speedMs: Float,
    val launchAngleDeg: Float,
    val massKg: Float,
    val radiusM: Float,
    val dragCoeff: Float,
    val desc: String
) {
    BANANA_KICK(
        label = "Banana Kick",
        title = "Roberto Carlos Free Kick (Soccer)",
        spinRpm = 620f,
        speedMs = 32f,
        launchAngleDeg = 14f,
        massKg = 0.43f,
        radiusM = 0.11f,
        dragCoeff = 0.25f,
        desc = "High-speed side-spin swerves violently around a 4-man defensive wall into the goal top corner"
    ),
    BASEBALL_CURVE(
        label = "Curveball",
        title = "12-6 Baseball Curveball",
        spinRpm = 2200f,
        speedMs = 38f,
        launchAngleDeg = 6f,
        massKg = 0.145f,
        radiusM = 0.037f,
        dragCoeff = 0.31f,
        desc = "Aggressive forward topspin accelerates downward trajectory drop, diving under the batter's swing"
    ),
    TENNIS_TOPSPIN(
        label = "Tennis Topspin",
        title = "Heavy Topspin Baseline Forehand",
        spinRpm = 2800f,
        speedMs = 28f,
        launchAngleDeg = 18f,
        massKg = 0.058f,
        radiusM = 0.033f,
        dragCoeff = 0.55f,
        desc = "Extreme forward spin dips ball downward rapidly inside the baseline with high net clearance"
    ),
    TENNIS_BACKSPIN(
        label = "Backspin Slice",
        title = "Floating Tennis Slice / Backspin",
        spinRpm = -1800f,
        speedMs = 22f,
        launchAngleDeg = 10f,
        massKg = 0.058f,
        radiusM = 0.033f,
        dragCoeff = 0.55f,
        desc = "Backward spin creates upward Magnus lift force counteracting gravity, causing the ball to float"
    ),
    WIND_TUNNEL(
        label = "Wind Tunnel",
        title = "Rotating Cylinder Flow & Streamlines",
        spinRpm = 1200f,
        speedMs = 20f,
        launchAngleDeg = 0f,
        massKg = 1.0f,
        radiusM = 0.15f,
        dragCoeff = 0.30f,
        desc = "Direct aerodynamic visualization of asymmetric boundary layer deflection and Kutta lift"
    )
}

/**
 * Trajectory point in 2D flight path.
 */
data class TrajectoryPoint(
    val pos: Offset,
    val velocity: Offset,
    val timeSec: Float
)

@Composable
fun MagnusEffectExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(MagnusPreset.BANANA_KICK) }

    // Physical Parameters
    var spinRpm by remember { mutableStateOf(currentPreset.spinRpm) }
    var speedMs by remember { mutableStateOf(currentPreset.speedMs) }
    var launchAngleDeg by remember { mutableStateOf(currentPreset.launchAngleDeg) }
    var airDensity by remember { mutableStateOf(1.225f) } // Standard sea-level air (kg/m³)

    // Simulation Display Mode: Flight Trajectory vs Wind Tunnel Streamlines
    var isWindTunnelMode by remember { mutableStateOf(false) }
    var showWallAndGoal by remember { mutableStateOf(true) }
    var showForces by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(true) }

    // Trajectory Animation State
    var ballProgress by remember { mutableStateOf(1.0f) }

    // Continuous rotation animation loop for cylinder/ball spin and streamline flow
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = max(80, (60_000f / max(abs(spinRpm), 30f)).toInt()),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val streamlineFlowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRunning) 1200 else 999999, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Ball flight launch animation
    LaunchedEffect(currentPreset, spinRpm, speedMs, launchAngleDeg) {
        ballProgress = 0f
        val startTime = withFrameNanos { it }
        val flightDurationMs = 1800L
        while (ballProgress < 1.0f) {
            val now = withFrameNanos { it }
            val elapsed = (now - startTime) / 1_000_000L
            ballProgress = (elapsed.toFloat() / flightDurationMs).coerceIn(0f, 1f)
        }
    }

    // Physical Quantities Calculations (SI Units)
    val spinRadPerSec = spinRpm * (2f * PI.toFloat() / 60f)
    val radiusM = currentPreset.radiusM
    val massKg = currentPreset.massKg
    val crossArea = PI.toFloat() * radiusM * radiusM
    val dragCoeff = currentPreset.dragCoeff

    // 1. Kutta-Joukowski circulation: Gamma = 2 * pi * R^2 * omega
    val circulationGamma = 2f * PI.toFloat() * radiusM * radiusM * spinRadPerSec

    // 2. Perpendicular Magnus lift force: F_M = rho * Gamma * v
    val magnusLiftForce = airDensity * circulationGamma * speedMs

    // 3. Spin ratio Sp = R * |omega| / v
    val spinRatio = (radiusM * abs(spinRadPerSec)) / max(speedMs, 1e-3f)

    // 4. Lift coefficient Cl = 2 * F_M / (rho * A * v^2)
    val liftCoeffCl = if (speedMs > 0.1f) {
        (2f * abs(magnusLiftForce)) / (airDensity * crossArea * speedMs * speedMs)
    } else 0f

    // 5. Bernoulli surface pressure difference: Delta P = 2 * rho * v * (R * omega)
    val surfaceVelocity = radiusM * spinRadPerSec
    val deltaPressure = 2f * airDensity * speedMs * abs(surfaceVelocity)

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Tune spin & speed sliders! Tap '⚽ Kick / Launch' to watch the Magnus force bend trajectory around the wall.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Re-trigger flight launch on tap
                            ballProgress = 0f
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Adjust spin rate smoothly by dragging vertically
                            val dRpm = -dragAmount.y * 12f
                            spinRpm = (spinRpm + dRpm).coerceIn(-3000f, 3000f)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                if (isWindTunnelMode) {
                    // Wind Tunnel Streamline & Potential Flow View
                    drawWindTunnelStreamlines(
                        w = w,
                        h = h,
                        radiusPx = min(w, h) * 0.14f,
                        spinRadPerSec = spinRadPerSec,
                        speedMs = speedMs,
                        rotationAngleDeg = if (spinRpm >= 0f) rotationAngleDeg else -rotationAngleDeg,
                        flowPhase = streamlineFlowPhase,
                        circulationGamma = circulationGamma,
                        magnusLiftForce = magnusLiftForce,
                        showForces = showForces
                    )
                } else {
                    // Projectile Trajectory Flight View (Soccer Field / Pitch)
                    drawTrajectoryFlightView(
                        w = w,
                        h = h,
                        currentPreset = currentPreset,
                        spinRadPerSec = spinRadPerSec,
                        speedMs = speedMs,
                        launchAngleDeg = launchAngleDeg,
                        airDensity = airDensity,
                        massKg = massKg,
                        radiusM = radiusM,
                        dragCoeff = dragCoeff,
                        ballProgress = ballProgress,
                        rotationAngleDeg = if (spinRpm >= 0f) rotationAngleDeg else -rotationAngleDeg,
                        showWallAndGoal = showWallAndGoal,
                        showForces = showForces
                    )
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 36: The Magnus Effect",
                items = listOf(
                    "Kutta-Joukowski Law" to "F_M = ρ · Γ · v",
                    "Circulation (Γ)" to "${round(circulationGamma * 100f) / 100f} m²/s",
                    "Magnus Lift Force" to "${round(abs(magnusLiftForce) * 10f) / 10f} N",
                    "Spin Ratio (R·ω/v)" to "${round(spinRatio * 100f) / 100f}",
                    "Lift Coeff (C_L)" to "${round(liftCoeffCl * 100f) / 100f}",
                    "Surface ΔP (Bernoulli)" to "${deltaPressure.roundToInt()} Pa"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Selection Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(MagnusPreset.values()) { preset ->
                        val isSelected = currentPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                currentPreset = preset
                                spinRpm = preset.spinRpm
                                speedMs = preset.speedMs
                                launchAngleDeg = preset.launchAngleDeg
                                isWindTunnelMode = (preset == MagnusPreset.WIND_TUNNEL)
                                ballProgress = 0f
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
                                containerColor = ScienceDarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) CyanNeon else ScienceBorder.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Paired Sliders Row 1: Spin Rate & Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Spin Rate (ω)",
                        value = spinRpm,
                        range = -3000f..3000f,
                        valueDisplay = "${spinRpm.roundToInt()} rpm",
                        accentColor = if (spinRpm >= 0f) CyanNeon else CoralNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            spinRpm = it
                            ballProgress = 0f
                        }
                    )

                    PhysicsSliderControl(
                        title = "Velocity (v₀)",
                        value = speedMs,
                        range = 5f..45f,
                        valueDisplay = "${speedMs.roundToInt()} m/s",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            speedMs = it
                            ballProgress = 0f
                        }
                    )
                }

                // Paired Sliders Row 2: Launch Angle & Air Density
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Launch Angle (θ)",
                        value = launchAngleDeg,
                        range = 0f..45f,
                        valueDisplay = "${launchAngleDeg.roundToInt()}°",
                        accentColor = EmeraldNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            launchAngleDeg = it
                            ballProgress = 0f
                        }
                    )

                    PhysicsSliderControl(
                        title = "Air Density (ρ)",
                        value = airDensity,
                        range = 0.2f..2.0f,
                        valueDisplay = "${round(airDensity * 100f) / 100f} kg/m³",
                        accentColor = PurpleNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { airDensity = it }
                    )
                }

                // Feature Toggle Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !isWindTunnelMode,
                        onClick = { isWindTunnelMode = !isWindTunnelMode },
                        label = {
                            Text(
                                if (isWindTunnelMode) "Wind Tunnel Flow" else "Pitch Trajectory",
                                fontSize = 9.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CyanNeon,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    FilterChip(
                        selected = showForces,
                        onClick = { showForces = !showForces },
                        label = { Text("Force Vectors", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldNeon.copy(alpha = 0.25f),
                            selectedLabelColor = EmeraldNeon,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    FilterChip(
                        selected = showWallAndGoal,
                        onClick = { showWallAndGoal = !showWallAndGoal },
                        label = { Text("Wall & Goal", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberVibrant.copy(alpha = 0.25f),
                            selectedLabelColor = AmberVibrant,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )
                }

                // Action Bar: Kick Button, Pause/Run, and Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { ballProgress = 0f },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚽ Kick / Launch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberVibrant else EmeraldNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            spinRpm = currentPreset.spinRpm
                            speedMs = currentPreset.speedMs
                            launchAngleDeg = currentPreset.launchAngleDeg
                            airDensity = 1.225f
                            ballProgress = 0f
                            isRunning = true
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

/**
 * 1. Aerodynamic Wind Tunnel Mode: Streamlines & Pressure Field.
 */
private fun DrawScope.drawWindTunnelStreamlines(
    w: Float,
    h: Float,
    radiusPx: Float,
    spinRadPerSec: Float,
    speedMs: Float,
    rotationAngleDeg: Float,
    flowPhase: Float,
    circulationGamma: Float,
    magnusLiftForce: Float,
    showForces: Boolean
) {
    // Elevated center
    val cx = w * 0.48f
    val cy = h * 0.40f

    // Background Wind Tunnel Chamber
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A)),
            startY = 0f,
            endY = h
        ),
        size = Size(w, h)
    )

    // Flow Velocity Grid / Markers
    val streamLinesCount = 14
    val streamSpacing = h * 0.65f / streamLinesCount

    // Gamma non-dimensional circulation factor for streamline bending
    val gammaFactor = (circulationGamma * 0.08f).coerceIn(-radiusPx * 1.2f, radiusPx * 1.2f)

    for (i in 0..streamLinesCount) {
        val yOffset0 = (i - streamLinesCount * 0.5f) * streamSpacing
        val path = Path()
        val steps = 60

        for (s in 0..steps) {
            val t = s / steps.toFloat()
            val x = w * (0.05f + 0.90f * t)
            val dx = x - cx

            // Potential flow around cylinder with circulation:
            // Streamlines deflect vertically proportional to circulation and proximity to cylinder
            val distFromCenter = sqrt(dx * dx + yOffset0 * yOffset0)
            val decay = (radiusPx * radiusPx) / max(distFromCenter * distFromCenter, radiusPx * radiusPx)
            val deflectionY = if (abs(dx) < radiusPx * 2.8f) {
                // Deflection downward on cylinder wake when spinning counter-clockwise
                -gammaFactor * decay * (1.0f - abs(dx) / (radiusPx * 2.8f))
            } else 0f

            // Add smooth flow curve around cylinder boundary
            val bump = if (distFromCenter < radiusPx * 1.6f && abs(yOffset0) > 1f) {
                val sign = sign(yOffset0)
                sign * (radiusPx * 1.1f - abs(yOffset0)).coerceAtLeast(0f) * exp(-(dx * dx) / (radiusPx * radiusPx))
            } else 0f

            val y = cy + yOffset0 + deflectionY + bump

            if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Color based on velocity / pressure difference:
        // Top side: higher speed -> Cyan / Emerald (low pressure)
        // Bottom side: lower speed -> Amber / Coral (high pressure)
        val lineColor = if (yOffset0 < 0) {
            lerp(CyanNeon, Color.White, 0.35f).copy(alpha = 0.55f)
        } else {
            lerp(CoralNeon, AmberVibrant, 0.4f).copy(alpha = 0.55f)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 1.6f)
        )

        // Tracer particles flowing along streamline
        val tracerX = w * (0.05f + 0.90f * ((flowPhase + i * 0.12f) % 1.0f))
        val tracerDx = tracerX - cx
        val tracerDist = sqrt(tracerDx * tracerDx + yOffset0 * yOffset0)
        if (tracerDist > radiusPx * 1.05f) {
            val tracerDecay = (radiusPx * radiusPx) / max(tracerDist * tracerDist, radiusPx * radiusPx)
            val tracerDeflect = if (abs(tracerDx) < radiusPx * 2.8f) -gammaFactor * tracerDecay else 0f
            val tracerY = cy + yOffset0 + tracerDeflect
            drawCircle(
                color = if (yOffset0 < 0) CyanNeon else AmberVibrant,
                radius = 2.8f,
                center = Offset(tracerX, tracerY)
            )
        }
    }

    // Rotating Cylinder / Ball Body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF334155), Color(0xFF1E293B)),
            center = Offset(cx, cy),
            radius = radiusPx
        ),
        radius = radiusPx,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = CyanNeon.copy(alpha = 0.8f),
        radius = radiusPx,
        center = Offset(cx, cy),
        style = Stroke(2.5f)
    )

    // Rotating Radial Spokes / Seams on Cylinder
    val spokeCount = 6
    for (k in 0 until spokeCount) {
        val ang = (rotationAngleDeg + k * (360f / spokeCount)) * (PI.toFloat() / 180f)
        val sx = cx + cos(ang) * radiusPx
        val sy = cy + sin(ang) * radiusPx
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(cx, cy),
            end = Offset(sx, sy),
            strokeWidth = 2f
        )
    }

    // Boundary Layer Spin Arrow
    drawArc(
        color = CyanNeon,
        startAngle = rotationAngleDeg,
        sweepAngle = 260f,
        useCenter = false,
        topLeft = Offset(cx - radiusPx * 1.25f, cy - radiusPx * 1.25f),
        size = Size(radiusPx * 2.5f, radiusPx * 2.5f),
        style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
    )

    // Force Vectors Overlay
    if (showForces) {
        val liftLen = (magnusLiftForce * 8.0f).coerceIn(-110f, 110f)

        // Magnus Lift Vector (Vertical, Cyan)
        if (abs(liftLen) > 3f) {
            val arrowEndY = cy - liftLen
            drawLine(
                color = CyanNeon,
                start = Offset(cx, cy),
                end = Offset(cx, arrowEndY),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )
            // Arrowhead
            val headSign = if (liftLen > 0) -1f else 1f
            val headPath = Path().apply {
                moveTo(cx, arrowEndY)
                lineTo(cx - 6f, arrowEndY - headSign * 10f)
                lineTo(cx + 6f, arrowEndY - headSign * 10f)
                close()
            }
            drawPath(headPath, CyanNeon)
        }

        // Drag Vector (Horizontal, Coral)
        val dragLen = 45f
        drawLine(
            color = CoralNeon,
            start = Offset(cx, cy),
            end = Offset(cx + dragLen, cy),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        val dragHead = Path().apply {
            moveTo(cx + dragLen, cy)
            lineTo(cx + dragLen - 8f, cy - 5f)
            lineTo(cx + dragLen - 8f, cy + 5f)
            close()
        }
        drawPath(dragHead, CoralNeon)
    }
}

/**
 * 2. Flight Trajectory View: Soccer pitch / ball curve simulator.
 */
private fun DrawScope.drawTrajectoryFlightView(
    w: Float,
    h: Float,
    currentPreset: MagnusPreset,
    spinRadPerSec: Float,
    speedMs: Float,
    launchAngleDeg: Float,
    airDensity: Float,
    massKg: Float,
    radiusM: Float,
    dragCoeff: Float,
    ballProgress: Float,
    rotationAngleDeg: Float,
    showWallAndGoal: Boolean,
    showForces: Boolean
) {
    // Pitch Horizon and Grass Field
    val pitchGroundY = h * 0.58f

    // Sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
            startY = 0f,
            endY = pitchGroundY
        ),
        topLeft = Offset.Zero,
        size = Size(w, pitchGroundY)
    )

    // Stadium Lights Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x33FFFFFF), Color.Transparent),
            center = Offset(w * 0.85f, pitchGroundY * 0.25f),
            radius = 70f
        ),
        radius = 70f,
        center = Offset(w * 0.85f, pitchGroundY * 0.25f)
    )

    // Green Grass Turf Field
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF134E23), Color(0xFF0D3317), Color(0xFF071C0D)),
            startY = pitchGroundY,
            endY = h
        ),
        topLeft = Offset(0f, pitchGroundY),
        size = Size(w, h - pitchGroundY)
    )

    // Penalty Arc / Field Markings
    drawLine(
        color = Color.White.copy(alpha = 0.35f),
        start = Offset(0f, pitchGroundY),
        end = Offset(w, pitchGroundY),
        strokeWidth = 2.0f
    )

    // Goal Post on Right (x = w * 0.90f)
    val goalX = w * 0.90f
    val goalTopY = pitchGroundY - 60f
    val goalW = 28f
    drawLine(Color.White, Offset(goalX, pitchGroundY), Offset(goalX, goalTopY), 3f)
    drawLine(Color.White, Offset(goalX, goalTopY), Offset(goalX + goalW, goalTopY), 2.5f)
    drawLine(Color.White, Offset(goalX + goalW, goalTopY), Offset(goalX + goalW, pitchGroundY), 2.5f)

    // Goal Net Grid
    for (ny in 0..4) {
        val yNet = goalTopY + ny * 15f
        drawLine(
            color = Color.White.copy(alpha = 0.20f),
            start = Offset(goalX, yNet),
            end = Offset(goalX + goalW, yNet),
            strokeWidth = 1f
        )
    }

    // Defensive Wall (x = w * 0.52f)
    if (showWallAndGoal) {
        val wallX = w * 0.52f
        val wallW = 38f
        val wallH = 48f
        val wallY = pitchGroundY - wallH

        // 4 Defending Players Silhouette
        for (p in 0..3) {
            val px = wallX - wallW * 0.5f + p * 11f
            // Body
            drawRoundRect(
                color = Color(0xFFC5283D),
                topLeft = Offset(px - 4f, wallY + 8f),
                size = Size(8f, wallH - 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
            )
            // Head
            drawCircle(Color(0xFFFFD166), 4.5f, Offset(px, wallY + 4f))
        }
    }

    // 2. Numerical Trajectory Integration (Runge-Kutta / Euler)
    val originX = w * 0.10f
    val originY = pitchGroundY - 4f

    val trajectory = calculateFlightTrajectory(
        origin = Offset(originX, originY),
        pitchGroundY = pitchGroundY,
        w = w,
        speedMs = speedMs,
        launchAngleDeg = launchAngleDeg,
        spinRadPerSec = spinRadPerSec,
        airDensity = airDensity,
        massKg = massKg,
        radiusM = radiusM,
        dragCoeff = dragCoeff
    )

    // Draw Complete Trajectory Path (Dotted Glowing Trail)
    if (trajectory.size >= 2) {
        val trailPath = Path()
        trajectory.forEachIndexed { idx, pt ->
            if (idx == 0) trailPath.moveTo(pt.pos.x, pt.pos.y) else trailPath.lineTo(pt.pos.x, pt.pos.y)
        }

        // Glow
        drawPath(
            path = trailPath,
            color = CyanNeon.copy(alpha = 0.25f),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
        // Core Trail
        drawPath(
            path = trailPath,
            color = CyanNeon,
            style = Stroke(
                width = 2.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        )
    }

    // Current Animated Ball Position
    val activeIdx = ((trajectory.size - 1) * ballProgress).toInt().coerceIn(0, max(0, trajectory.size - 1))
    val currentBallPt = trajectory.getOrNull(activeIdx)?.pos ?: Offset(originX, originY)
    val ballRadiusPx = (radiusM * 120f).coerceIn(8f, 16f)

    // Ball Shadow on Pitch
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(currentBallPt.x - ballRadiusPx * 1.1f, pitchGroundY - 3f),
        size = Size(ballRadiusPx * 2.2f, 6f)
    )

    // Ball Body
    drawCircle(
        color = Color.White,
        radius = ballRadiusPx,
        center = currentBallPt
    )
    drawCircle(
        color = CyanNeon,
        radius = ballRadiusPx,
        center = currentBallPt,
        style = Stroke(2f)
    )

    // Soccer Ball Hexagons / Seams
    for (angOffset in listOf(0f, 120f, 240f)) {
        val rad = (rotationAngleDeg + angOffset) * (PI.toFloat() / 180f)
        val hx = currentBallPt.x + cos(rad) * ballRadiusPx * 0.6f
        val hy = currentBallPt.y + sin(rad) * ballRadiusPx * 0.6f
        drawCircle(ScienceDarkBg, 3f, Offset(hx, hy))
    }

    // Magnus Force Vectors attached to moving ball
    if (showForces) {
        val liftVectorLen = (spinRadPerSec * 0.012f).coerceIn(-40f, 40f)
        // Perpendicular Magnus Lift (Cyan)
        drawLine(
            color = CyanNeon,
            start = currentBallPt,
            end = Offset(currentBallPt.x, currentBallPt.y - liftVectorLen),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Numerically integrates the 2D projectile equations of motion with Magnus force.
 */
private fun calculateFlightTrajectory(
    origin: Offset,
    pitchGroundY: Float,
    w: Float,
    speedMs: Float,
    launchAngleDeg: Float,
    spinRadPerSec: Float,
    airDensity: Float,
    massKg: Float,
    radiusM: Float,
    dragCoeff: Float
): List<TrajectoryPoint> {
    val points = mutableListOf<TrajectoryPoint>()

    val rad = launchAngleDeg * (PI.toFloat() / 180f)
    var vx = speedMs * cos(rad)
    var vy = -speedMs * sin(rad) // negative vy is upward

    var x = origin.x
    var y = origin.y
    var t = 0f
    val dt = 0.016f // 60 FPS numerical step
    val scalePxPerMeter = 16f
    val gravityMs2 = 9.81f
    val crossArea = PI.toFloat() * radiusM * radiusM
    val circulationGamma = 2f * PI.toFloat() * radiusM * radiusM * spinRadPerSec

    points.add(TrajectoryPoint(Offset(x, y), Offset(vx, vy), t))

    for (step in 0..120) {
        val vSpeed = sqrt(vx * vx + vy * vy)
        if (vSpeed < 0.1f) break

        val cosTheta = vx / vSpeed
        val sinTheta = vy / vSpeed

        // 1. Drag force opposite to velocity
        val dragForce = 0.5f * airDensity * dragCoeff * crossArea * vSpeed * vSpeed

        // 2. Kutta-Joukowski Magnus force perpendicular to velocity
        val magnusForce = airDensity * circulationGamma * vSpeed

        // Accelerations:
        // Magnus force is perpendicular: if vy is negative (upward) and vx > 0,
        // positive spin produces upward lift (negative y accel)
        val accelX = (-dragForce * cosTheta - magnusForce * sinTheta) / massKg
        val accelY = (gravityMs2 * massKg - dragForce * sinTheta + magnusForce * cosTheta) / massKg

        vx += accelX * dt
        vy += accelY * dt

        x += vx * dt * scalePxPerMeter
        y += vy * dt * scalePxPerMeter
        t += dt

        points.add(TrajectoryPoint(Offset(x, y), Offset(vx, vy), t))

        // Ground bounce or reach goal
        if (y >= pitchGroundY || x >= w * 0.95f) {
            break
        }
    }

    return points
}
