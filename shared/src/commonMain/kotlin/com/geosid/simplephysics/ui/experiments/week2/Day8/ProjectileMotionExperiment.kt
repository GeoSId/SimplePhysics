package com.geosid.simplephysics.ui.experiments.week2.Day8

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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

data class MuzzleSmoke(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float = 0.9f
)

@Composable
fun ProjectileMotionExperiment(
    modifier: Modifier = Modifier
) {
    // Tunable Launch Parameters
    var launchAngleDeg by remember { mutableStateOf(45f) }
    var launchSpeed by remember { mutableStateOf(45f) } // m/s
    var dragCoefficient by remember { mutableStateOf(0.35f) } // Cd (0 = vacuum, ~0.47 for sphere)
    var windSpeed by remember { mutableStateOf(-5.0f) } // m/s (+ tailwind, - headwind)
    var showVacuumComparison by remember { mutableStateOf(true) }

    // Physical Constants
    val gravity = 9.81f // m/s^2
    val airDensity = 1.225f // kg/m^3
    val projectileMass = 1.2f // kg
    val projectileArea = 0.015f // m^2 (approx 14 cm diameter sphere)

    // Simulation State
    var isFlying by remember { mutableStateOf(false) }
    var posX by remember { mutableStateOf(0f) } // meters
    var posY by remember { mutableStateOf(0f) } // meters
    var velX by remember { mutableStateOf(0f) } // m/s
    var velY by remember { mutableStateOf(0f) } // m/s
    var flightTime by remember { mutableStateOf(0f) } // seconds
    var maxHeight by remember { mutableStateOf(0f) } // meters
    var targetHit by remember { mutableStateOf(false) }

    // Target position on ground
    val targetX = 95f // meters

    // Trajectory History
    val trajectoryPoints = remember { mutableStateListOf<Offset>() }
    val smokeParticles = remember { mutableStateListOf<MuzzleSmoke>() }

    // Wind animation streaks
    val infiniteTransition = rememberInfiniteTransition()
    val windStreakPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Physics step loop (Euler-Cromer integration)
    LaunchedEffect(isFlying) {
        if (isFlying) {
            var lastTime = withFrameNanos { it }
            while (isFlying) {
                val now = withFrameNanos { it }
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.035f)
                lastTime = now

                // Multiple sub-steps for high numerical precision
                val subSteps = 5
                val subDt = dt / subSteps

                for (i in 0 until subSteps) {
                    flightTime += subDt

                    // Relative velocity against air with wind
                    val relVx = velX - windSpeed
                    val relVy = velY
                    val relSpeed = sqrt(relVx * relVx + relVy * relVy)

                    // Aerodynamic drag force: F_drag = 0.5 * rho * Cd * A * v_rel^2
                    val fDrag = 0.5f * airDensity * dragCoefficient * projectileArea * relSpeed * relSpeed

                    // Drag acceleration components opposite to relative velocity
                    val aDragX = if (relSpeed > 0.001f) -(fDrag / projectileMass) * (relVx / relSpeed) else 0f
                    val aDragY = if (relSpeed > 0.001f) -(fDrag / projectileMass) * (relVy / relSpeed) else 0f

                    // Net accelerations
                    val ax = aDragX
                    val ay = -gravity + aDragY

                    // Euler-Cromer update
                    velX += ax * subDt
                    velY += ay * subDt
                    posX += velX * subDt
                    posY += velY * subDt

                    if (posY > maxHeight) {
                        maxHeight = posY
                    }

                    // Check ground impact
                    if (posY <= 0f) {
                        posY = 0f
                        isFlying = false

                        // Check target hit (within +/- 5m of target)
                        if (abs(posX - targetX) < 5.5f) {
                            targetHit = true
                        }
                        break
                    }
                }

                // Record path point
                trajectoryPoints.add(Offset(posX, posY))

                // Update muzzle smoke
                val smokeIter = smokeParticles.iterator()
                while (smokeIter.hasNext()) {
                    val s = smokeIter.next()
                    s.x += s.vx * dt
                    s.y += s.vy * dt
                    s.radius += 18f * dt
                    s.alpha -= 1.2f * dt
                    if (s.alpha <= 0f) {
                        smokeIter.remove()
                    }
                }
            }
        }
    }

    val speedMag = sqrt(velX * velX + velY * velY)
    val kineticEnergy = 0.5f * projectileMass * speedMag * speedMag

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag on the sky to aim, or use the sliders below to adjust launch parameters!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Drag up/down to adjust cannon angle
                            if (!isFlying) {
                                launchAngleDeg = (launchAngleDeg - dragAmount.y * 0.25f).coerceIn(10f, 85f)
                                launchSpeed = (launchSpeed + dragAmount.x * 0.1f).coerceIn(15f, 75f)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Scale meters to pixels
                val groundY = h * 0.78f
                val cannonOriginX = w * 0.12f
                val scalePxPerMeter = min(w * 0.78f / 160f, h * 0.65f / 80f)

                // 1. Draw Sky & Ground Grid with Meter Distance Markers
                drawGroundAndGrid(
                    groundY = groundY,
                    cannonX = cannonOriginX,
                    scale = scalePxPerMeter,
                    targetMeters = targetX
                )

                // 2. Draw Wind Streaks
                if (abs(windSpeed) > 0.5f) {
                    drawWindStreaks(
                        groundY = groundY,
                        windSpeed = windSpeed,
                        phase = windStreakPhase
                    )
                }

                // 3. Draw Ideal Vacuum Parabola for comparison (if enabled)
                if (showVacuumComparison) {
                    drawVacuumParabola(
                        cannonX = cannonOriginX,
                        groundY = groundY,
                        v0 = launchSpeed,
                        angleDeg = launchAngleDeg,
                        g = gravity,
                        scale = scalePxPerMeter
                    )
                }

                // 4. Draw Real Air-Drag Trajectory Trace
                if (trajectoryPoints.size > 1) {
                    val path = Path().apply {
                        val first = trajectoryPoints.first()
                        moveTo(cannonOriginX + first.x * scalePxPerMeter, groundY - first.y * scalePxPerMeter)
                        for (i in 1 until trajectoryPoints.size) {
                            val pt = trajectoryPoints[i]
                            lineTo(cannonOriginX + pt.x * scalePxPerMeter, groundY - pt.y * scalePxPerMeter)
                        }
                    }
                    drawPath(
                        path = path,
                        color = CyanNeon,
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Dotted trail dots
                    trajectoryPoints.forEachIndexed { idx, pt ->
                        if (idx % 6 == 0) {
                            drawCircle(
                                color = AmberVibrant,
                                radius = 3.5f,
                                center = Offset(cannonOriginX + pt.x * scalePxPerMeter, groundY - pt.y * scalePxPerMeter)
                            )
                        }
                    }
                }

                // 5. Draw Target Bullseye Flag at targetX
                val targetPxX = cannonOriginX + targetX * scalePxPerMeter
                drawTargetFlag(targetPxX, groundY, targetHit)

                // 6. Draw Cannon
                drawCannon(
                    originX = cannonOriginX,
                    groundY = groundY,
                    angleDeg = launchAngleDeg
                )

                // 7. Draw Muzzle Smoke
                smokeParticles.forEach { s ->
                    drawCircle(
                        color = Color.White.copy(alpha = s.alpha.coerceIn(0f, 1f)),
                        radius = s.radius,
                        center = Offset(s.x, s.y)
                    )
                }

                // 8. Draw Flying Projectile
                if (isFlying || trajectoryPoints.isNotEmpty()) {
                    val projPxX = cannonOriginX + posX * scalePxPerMeter
                    val projPxY = groundY - posY * scalePxPerMeter

                    // Glowing projectile core
                    drawCircle(
                        color = CoralNeon,
                        radius = 9f,
                        center = Offset(projPxX, projPxY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = Offset(projPxX, projPxY)
                    )

                    // Velocity vector arrow (tangent to motion)
                    if (isFlying) {
                        val vMag = sqrt(velX * velX + velY * velY)
                        if (vMag > 1f) {
                            val vArrowLen = min(vMag * 0.8f, 35f)
                            val vNormX = velX / vMag
                            val vNormY = -velY / vMag // invert Y for screen
                            drawLine(
                                color = AmberVibrant,
                                start = Offset(projPxX, projPxY),
                                end = Offset(projPxX + vNormX * vArrowLen, projPxY + vNormY * vArrowLen),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Trajectory Telemetry",
                items = listOf(
                    "Distance (Range)" to "${round(posX * 10) / 10f} m",
                    "Max Altitude" to "${round(maxHeight * 10) / 10f} m",
                    "Flight Time" to "${round(flightTime * 100) / 100f} s",
                    "Velocity |v|" to "${round(speedMag * 10) / 10f} m/s",
                    "Kinetic Energy" to "${round(kineticEnergy)} J",
                    "Target (95m)" to if (targetHit) "🎯 DIRECT HIT!" else if (posY == 0f && posX > 0f) "MISSED" else "IN FLIGHT"
                )
            )
        },
        controlsContent = {
            Surface(
                color = ScienceDarkSurfaceVariant.copy(alpha = 0.94f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Row 1: Sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PhysicsSliderControl(
                            modifier = Modifier.weight(1f),
                            title = "Launch Angle",
                            value = launchAngleDeg,
                            range = 10f..85f,
                            valueDisplay = "${launchAngleDeg.toInt()}°",
                            accentColor = AmberVibrant,
                            onValueChange = { if (!isFlying) launchAngleDeg = it }
                        )

                        PhysicsSliderControl(
                            modifier = Modifier.weight(1f),
                            title = "Muzzle Speed",
                            value = launchSpeed,
                            range = 15f..75f,
                            valueDisplay = "${launchSpeed.toInt()} m/s",
                            accentColor = CyanNeon,
                            onValueChange = { if (!isFlying) launchSpeed = it }
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PhysicsSliderControl(
                            modifier = Modifier.weight(1f),
                            title = "Drag Coeff (Cd)",
                            value = dragCoefficient,
                            range = 0f..0.8f,
                            valueDisplay = "${round(dragCoefficient * 100) / 100f}",
                            accentColor = if (dragCoefficient == 0f) TextMuted else PurpleNeon,
                            onValueChange = { if (!isFlying) dragCoefficient = it }
                        )

                        PhysicsSliderControl(
                            modifier = Modifier.weight(1f),
                            title = "Crosswind",
                            value = windSpeed,
                            range = -20f..20f,
                            valueDisplay = "${round(windSpeed)} m/s",
                            accentColor = if (windSpeed < 0) CoralNeon else EmeraldNeon,
                            onValueChange = { windSpeed = it }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Row 2: Action Buttons & Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = showVacuumComparison,
                                onCheckedChange = { showVacuumComparison = it },
                                colors = CheckboxDefaults.colors(checkedColor = AmberVibrant)
                            )
                            Text("Vacuum Parabola", color = TextSecondary, fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    // Reset coordinates & fire!
                                    posX = 0f
                                    posY = 0f
                                    val rad = launchAngleDeg * (PI / 180f).toFloat()
                                    velX = launchSpeed * cos(rad)
                                    velY = launchSpeed * sin(rad)
                                    flightTime = 0f
                                    maxHeight = 0f
                                    targetHit = false
                                    trajectoryPoints.clear()
                                    trajectoryPoints.add(Offset(0f, 0f))
                                    isFlying = true

                                    // Spawn muzzle smoke
                                    smokeParticles.clear()
                                    for (i in 0 until 8) {
                                        smokeParticles.add(
                                            MuzzleSmoke(
                                                x = 100f + Random.nextFloat() * 20f,
                                                y = 400f + Random.nextFloat() * 20f,
                                                vx = Random.nextFloat() * 40f - 20f,
                                                vy = -Random.nextFloat() * 30f,
                                                radius = Random.nextFloat() * 12f + 8f
                                            )
                                        )
                                    }
                                },
                                enabled = !isFlying,
                                colors = ButtonDefaults.buttonColors(containerColor = CoralNeon, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🚀 FIRE!", fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = {
                                    isFlying = false
                                    posX = 0f
                                    posY = 0f
                                    velX = 0f
                                    velY = 0f
                                    flightTime = 0f
                                    maxHeight = 0f
                                    targetHit = false
                                    trajectoryPoints.clear()
                                    smokeParticles.clear()
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(ScienceDarkSurface, RoundedCornerShape(8.dp))
                            ) {
                                ResetIcon(tint = CyanNeon, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}

// Drawing Helpers
private fun DrawScope.drawGroundAndGrid(
    groundY: Float,
    cannonX: Float,
    scale: Float,
    targetMeters: Float
) {
    val w = size.width

    // Ground Grass / Soil Line
    drawLine(
        color = EmeraldNeon.copy(alpha = 0.7f),
        start = Offset(0f, groundY),
        end = Offset(w, groundY),
        strokeWidth = 4f
    )

    // Sub-surface soil hatching
    drawRect(
        color = Color(0x2200E676),
        topLeft = Offset(0f, groundY),
        size = Size(w, size.height - groundY)
    )

    // Distance tick markers every 20 meters
    for (m in 0..200 step 20) {
        val mx = cannonX + m * scale
        if (mx <= w) {
            drawLine(
                color = ScienceBorder,
                start = Offset(mx, groundY),
                end = Offset(mx, groundY + 12f),
                strokeWidth = 2f
            )
            // Vertical grid line up into the sky
            drawLine(
                color = ScienceBorder.copy(alpha = 0.35f),
                start = Offset(mx, 40f),
                end = Offset(mx, groundY),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
            )
        }
    }
}

private fun DrawScope.drawVacuumParabola(
    cannonX: Float,
    groundY: Float,
    v0: Float,
    angleDeg: Float,
    g: Float,
    scale: Float
) {
    val rad = angleDeg * (PI / 180f).toFloat()
    val cosA = cos(rad)
    val sinA = sin(rad)

    // Range in vacuum: R_vac = (v0^2 * sin(2*theta)) / g
    val rVac = (v0 * v0 * sin(2f * rad)) / g
    val steps = 40
    val dx = rVac / steps

    val vacPath = Path().apply {
        moveTo(cannonX, groundY)
        for (i in 1..steps) {
            val x = i * dx
            // y(x) = x * tan(theta) - (g * x^2) / (2 * v0^2 * cos^2(theta))
            val y = x * tan(rad) - (g * x * x) / (2f * v0 * v0 * cosA * cosA)
            if (y >= 0f) {
                lineTo(cannonX + x * scale, groundY - y * scale)
            }
        }
    }

    drawPath(
        path = vacPath,
        color = AmberVibrant.copy(alpha = 0.65f),
        style = Stroke(
            width = 2.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )
    )
}

private fun DrawScope.drawCannon(
    originX: Float,
    groundY: Float,
    angleDeg: Float
) {
    val barrelLength = 48f
    val barrelWidth = 18f
    val wheelRadius = 16f

    // Wheel carriage
    drawCircle(
        color = Color(0xFF5D4037),
        radius = wheelRadius,
        center = Offset(originX, groundY - wheelRadius)
    )
    drawCircle(
        color = Color(0xFF3E2723),
        radius = wheelRadius,
        center = Offset(originX, groundY - wheelRadius),
        style = Stroke(width = 3f)
    )

    // Rotating Cannon Barrel
    val pivot = Offset(originX, groundY - wheelRadius)
    rotate(degrees = -angleDeg, pivot = pivot) {
        // Barrel rectangle
        drawRoundRect(
            color = Color(0xFF455A64),
            topLeft = Offset(pivot.x, pivot.y - barrelWidth / 2f),
            size = Size(barrelLength, barrelWidth),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
        // Muzzle ring
        drawRoundRect(
            color = Color(0xFF263238),
            topLeft = Offset(pivot.x + barrelLength - 6f, pivot.y - (barrelWidth + 4f) / 2f),
            size = Size(8f, barrelWidth + 4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
        )
    }

    // Wheel axle pin
    drawCircle(
        color = AmberVibrant,
        radius = 5f,
        center = pivot
    )
}

private fun DrawScope.drawTargetFlag(
    targetPxX: Float,
    groundY: Float,
    isHit: Boolean
) {
    val poleH = 45f

    // Pole
    drawLine(
        color = Color.White,
        start = Offset(targetPxX, groundY),
        end = Offset(targetPxX, groundY - poleH),
        strokeWidth = 3f
    )

    // Triangular Flag
    val flagColor = if (isHit) EmeraldNeon else CoralNeon
    val flagPath = Path().apply {
        moveTo(targetPxX, groundY - poleH)
        lineTo(targetPxX + 26f, groundY - poleH + 12f)
        lineTo(targetPxX, groundY - poleH + 24f)
        close()
    }
    drawPath(path = flagPath, color = flagColor)

    // Ground Target Bullseye ring
    drawCircle(
        color = flagColor.copy(alpha = 0.5f),
        radius = 18f,
        center = Offset(targetPxX, groundY),
        style = Stroke(width = 2.5f)
    )
    drawCircle(
        color = flagColor,
        radius = 5f,
        center = Offset(targetPxX, groundY)
    )
}

private fun DrawScope.drawWindStreaks(
    groundY: Float,
    windSpeed: Float,
    phase: Float
) {
    val dir = if (windSpeed > 0) 1f else -1f
    val streakCount = 6
    val streakColor = CyanNeon.copy(alpha = 0.28f)

    for (i in 0 until streakCount) {
        val y = 80f + i * 50f
        val baseX = ((phase + i * 0.18f) % 1f) * size.width
        val len = min(abs(windSpeed) * 4f, 70f)

        drawLine(
            color = streakColor,
            start = Offset(baseX, y),
            end = Offset(baseX + dir * len, y),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}
