package com.geosid.simplephysics.ui.experiments.week3.Day17

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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

/**
 * Visual reference frame modes for observing Coriolis dynamics.
 */
enum class ReferenceFrame(val label: String, val icon: String) {
    ROTATING("Carousel View (Rotating)", "🔄"),
    INERTIAL("Lab View (Inertial)", "🌐")
}

/**
 * Standard configurations demonstrating different Coriolis regimes.
 */
enum class CoriolisPreset(
    val title: String,
    val icon: String,
    val omegaRadS: Float,
    val launchSpeedMps: Float,
    val launchAngleDeg: Float,
    val isBalancedDish: Boolean,
    val hasPressureGrad: Boolean,
    val color: Color
) {
    OUTWARD_CANNON("Outward Shot", "🎯", 2.2f, 2.5f, 0f, false, false, CyanNeon),
    INERTIAL_CIRCLES("Inertial Circle", "🔄", 2.5f, 1.8f, 45f, true, false, AmberVibrant),
    CYCLONE_VORTEX("Weather Cyclone", "🌀", 1.8f, 1.2f, 90f, false, true, PurpleNeon),
    INWARD_SHOT("Rim to Center", "⏪", -2.0f, 2.2f, 180f, false, false, CoralNeon)
}

/**
 * Represents a historical trajectory breadcrumb point in both frames.
 */
data class TrajectoryPoint(
    val rotPos: Offset,
    val inertPos: Offset,
    val alpha: Float
)

@Composable
fun CoriolisEffectExperiment(
    modifier: Modifier = Modifier
) {
    // 1. Physical Parameters
    var selectedPreset by remember { mutableStateOf<CoriolisPreset?>(CoriolisPreset.OUTWARD_CANNON) }
    var omegaRadS by remember { mutableStateOf(2.2f) }              // Rotation rate Ω (rad/s)
    var launchSpeedMps by remember { mutableStateOf(2.5f) }         // Initial launch speed v0 (m/s)
    var launchAngleDeg by remember { mutableStateOf(0f) }           // Launch angle (degrees)
    var isBalancedDish by remember { mutableStateOf(false) }         // Parabolic dish cancels centrifugal force
    var hasPressureGrad by remember { mutableStateOf(false) }        // Inward pressure gradient (cyclone)
    var activeFrame by remember { mutableStateOf(ReferenceFrame.ROTATING) }
    var isRunning by remember { mutableStateOf(true) }

    // 2. Interactive Aiming Vector
    var isAiming by remember { mutableStateOf(false) }
    var aimStart by remember { mutableStateOf(Offset.Zero) }
    var aimCurrent by remember { mutableStateOf(Offset.Zero) }

    // 3. Dynamic Simulation State
    // Turntable has physical radius R = 3.0 meters
    val turntableRadiusM = 3.0f

    // Particle state in rotating frame (coordinates relative to turntable center in meters)
    var ballPosRot by remember { mutableStateOf(Offset(0.05f, 0f)) }
    var ballVelRot by remember { mutableStateOf(Offset(2.5f, 0f)) }

    // Turntable absolute rotation angle in radians: θ_frame(t) = ∫ Ω dt
    var turntableAngleRad by remember { mutableStateOf(0f) }

    // Particle state in inertial frame
    var ballPosInert by remember { mutableStateOf(Offset(0.05f, 0f)) }
    var ballVelInert by remember { mutableStateOf(Offset(2.5f, 0f)) }

    // Trajectory trail history
    val trailPoints = remember { mutableStateListOf<TrajectoryPoint>() }

    // Reset / Launch helper
    fun launchParticle(speed: Float = launchSpeedMps, angleDeg: Float = launchAngleDeg, startAtCenter: Boolean = true) {
        val rad = (angleDeg * PI / 180f).toFloat()
        val startM = if (startAtCenter) {
            Offset(0.08f * cos(rad), 0.08f * sin(rad))
        } else {
            Offset(2.6f * cos(rad), 2.6f * sin(rad))
        }
        val initialDir = if (startAtCenter) Offset(cos(rad), sin(rad)) else Offset(-cos(rad), -sin(rad))
        val vRot = Offset(initialDir.x * speed, initialDir.y * speed)

        ballPosRot = startM
        ballVelRot = vRot

        // Compute inertial position and velocity at launch instant
        val cosT = cos(turntableAngleRad)
        val sinT = sin(turntableAngleRad)
        ballPosInert = Offset(
            startM.x * cosT - startM.y * sinT,
            startM.x * sinT + startM.y * cosT
        )
        // v_inert = v_rot + Ω x r
        val omegaTangX = -omegaRadS * startM.y
        val omegaTangY = omegaRadS * startM.x
        val vTotRotX = vRot.x + omegaTangX
        val vTotRotY = vRot.y + omegaTangY
        ballVelInert = Offset(
            vTotRotX * cosT - vTotRotY * sinT,
            vTotRotX * sinT + vTotRotY * cosT
        )

        trailPoints.clear()
    }

    // 4. Continuous Physics Integration Loop
    LaunchedEffect(isRunning, omegaRadS, isBalancedDish, hasPressureGrad) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.035f)
            lastTime = now

            if (isRunning) {
                // Advance turntable frame rotation
                turntableAngleRad = (turntableAngleRad + omegaRadS * dt) % (2f * PI.toFloat())

                // Sub-stepping for ultra-stable Runge-Kutta / Verlet integration
                val subSteps = 8
                val subDt = dt / subSteps

                for (step in 0 until subSteps) {
                    val r = sqrt(ballPosRot.x * ballPosRot.x + ballPosRot.y * ballPosRot.y)

                    // If ball falls off the turntable edge, re-launch
                    if (r > turntableRadiusM * 1.08f) {
                        launchParticle(launchSpeedMps, launchAngleDeg, selectedPreset != CoriolisPreset.INWARD_SHOT)
                        break
                    }

                    // 1. Coriolis Acceleration: a_cor = -2 (Ω x v_rel) = (2 Ω v_y, -2 Ω v_x)
                    val aCorX = 2f * omegaRadS * ballVelRot.y
                    val aCorY = -2f * omegaRadS * ballVelRot.x

                    // 2. Centrifugal Acceleration: a_cent = Ω² r (canceled if balanced parabolic dish)
                    val aCentX = if (isBalancedDish) 0f else (omegaRadS * omegaRadS * ballPosRot.x)
                    val aCentY = if (isBalancedDish) 0f else (omegaRadS * omegaRadS * ballPosRot.y)

                    // 3. Pressure Gradient / Restoring force (active in cyclone preset)
                    val kPressure = if (hasPressureGrad) 2.2f else 0f
                    val aPressX = -kPressure * ballPosRot.x
                    val aPressY = -kPressure * ballPosRot.y

                    // 4. Air / Surface drag
                    val drag = if (hasPressureGrad) 0.35f else 0.04f
                    val aDragX = -drag * ballVelRot.x
                    val aDragY = -drag * ballVelRot.y

                    // Net acceleration in rotating frame
                    val aNetX = aCorX + aCentX + aPressX + aDragX
                    val aNetY = aCorY + aCentY + aPressY + aDragY

                    // Update rotating velocity & position
                    ballVelRot = Offset(ballVelRot.x + aNetX * subDt, ballVelRot.y + aNetY * subDt)
                    ballPosRot = Offset(ballPosRot.x + ballVelRot.x * subDt, ballPosRot.y + ballVelRot.y * subDt)

                    // Update inertial position (rotated by current turntable angle)
                    val currentAngle = turntableAngleRad + (step + 1) * (omegaRadS * subDt)
                    val cosA = cos(currentAngle)
                    val sinA = sin(currentAngle)
                    ballPosInert = Offset(
                        ballPosRot.x * cosA - ballPosRot.y * sinA,
                        ballPosRot.x * sinA + ballPosRot.y * cosA
                    )
                }

                // Append trail history
                trailPoints.add(
                    TrajectoryPoint(
                        rotPos = ballPosRot,
                        inertPos = ballPosInert,
                        alpha = 1f
                    )
                )
                if (trailPoints.size > 220) {
                    trailPoints.removeAt(0)
                }
            }
        }
    }

    // Live Metrics Calculations
    val speedRel = sqrt(ballVelRot.x * ballVelRot.x + ballVelRot.y * ballVelRot.y)
    val aCoriolisMag = 2f * abs(omegaRadS) * speedRel
    val currentR = sqrt(ballPosRot.x * ballPosRot.x + ballPosRot.y * ballPosRot.y)
    val aCentrifugalMag = if (isBalancedDish) 0f else (omegaRadS * omegaRadS * currentR)
    val hemisphereLabel = when {
        omegaRadS > 0.05f -> "North (CCW • Right Deflection)"
        omegaRadS < -0.05f -> "South (CW • Left Deflection)"
        else -> "Equator (Zero Rotation)"
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag across turntable to re-aim and shoot! Toggle between Carousel View and Lab View.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isAiming = true
                                aimStart = offset
                                aimCurrent = offset
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                aimCurrent = change.position
                            },
                            onDragEnd = {
                                isAiming = false
                                val dragDelta = aimCurrent - aimStart
                                val dragDist = dragDelta.getDistance()
                                if (dragDist > 15f) {
                                    val angle = (atan2(dragDelta.y, dragDelta.x) * 180f / PI).toFloat()
                                    val mappedSpeed = (dragDist * 0.025f).coerceIn(0.6f, 6.0f)
                                    launchSpeedMps = mappedSpeed
                                    launchAngleDeg = angle
                                    selectedPreset = null
                                    launchParticle(mappedSpeed, angle, startAtCenter = true)
                                }
                            },
                            onDragCancel = { isAiming = false }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            launchParticle(launchSpeedMps, launchAngleDeg, selectedPreset != CoriolisPreset.INWARD_SHOT)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.5f, h * 0.38f)
                val canvasTurntableR = min(w * 0.40f, h * 0.32f).coerceAtLeast(80f)
                val metersToPixels = canvasTurntableR / turntableRadiusM

                // 1. Draw Outer Starfield / Laboratory Bench Background
                drawCoriolisLabBackground(w, h)

                // 2. Draw Rotating Carousel Turntable Platter
                val diskRotationRad = if (activeFrame == ReferenceFrame.ROTATING) 0f else turntableAngleRad
                drawTurntablePlatter(
                    center = center,
                    radiusPx = canvasTurntableR,
                    rotationRad = diskRotationRad,
                    isBalancedDish = isBalancedDish,
                    omegaRadS = omegaRadS
                )

                // 3. Draw Trajectory Ribbon Trails
                drawCoriolisTrails(
                    center = center,
                    scale = metersToPixels,
                    trail = trailPoints,
                    frame = activeFrame
                )

                // 4. Draw Aiming Vector Arrow when dragging
                if (isAiming) {
                    val aimDelta = aimCurrent - aimStart
                    val aimLength = aimDelta.getDistance()
                    if (aimLength > 10f) {
                        drawLine(
                            color = AmberVibrant,
                            start = aimStart,
                            end = aimCurrent,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(AmberVibrant, 7.dp.toPx(), aimCurrent)
                    }
                }

                // 5. Draw Active Particle & Dynamic Physics Vectors
                val activePosM = if (activeFrame == ReferenceFrame.ROTATING) ballPosRot else ballPosInert
                val particleCenterScreen = Offset(
                    center.x + activePosM.x * metersToPixels,
                    center.y + activePosM.y * metersToPixels
                )

                // Particle outer glow & body
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.35f),
                    radius = 16.dp.toPx(),
                    center = particleCenterScreen
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, CyanNeon, Color(0xFF0097A7)),
                        center = particleCenterScreen,
                        radius = 8.dp.toPx()
                    ),
                    radius = 8.dp.toPx(),
                    center = particleCenterScreen
                )

                // 6. Real-time Physical Vectors (Velocity, Coriolis, Centrifugal)
                if (activeFrame == ReferenceFrame.ROTATING) {
                    // Velocity Vector v_rel (CyanNeon)
                    val vScreenLen = (ballVelRot.getDistance() * 16.dp.toPx()).coerceIn(12.dp.toPx(), 70.dp.toPx())
                    if (speedRel > 0.05f) {
                        val vDir = Offset(ballVelRot.x / speedRel, ballVelRot.y / speedRel)
                        val vEnd = particleCenterScreen + vDir * vScreenLen
                        drawVectorArrow(
                            start = particleCenterScreen,
                            end = vEnd,
                            color = CyanNeon,
                            label = "v_rel"
                        )

                        // Coriolis Acceleration Vector a_cor = -2 (Ω x v) (CoralNeon)
                        // Perpendicular to velocity: points right if Ω > 0
                        val aCorDir = Offset(vDir.y * sign(omegaRadS), -vDir.x * sign(omegaRadS))
                        val aCorLen = (aCoriolisMag * 5.dp.toPx()).coerceIn(10.dp.toPx(), 65.dp.toPx())
                        val aCorEnd = particleCenterScreen + aCorDir * aCorLen
                        drawVectorArrow(
                            start = particleCenterScreen,
                            end = aCorEnd,
                            color = CoralNeon,
                            label = "a_cor"
                        )

                        // Centrifugal Acceleration Vector a_cent = Ω² r (PurpleNeon)
                        if (!isBalancedDish && currentR > 0.1f) {
                            val rDir = Offset(ballPosRot.x / currentR, ballPosRot.y / currentR)
                            val aCentLen = (aCentrifugalMag * 4.dp.toPx()).coerceIn(8.dp.toPx(), 55.dp.toPx())
                            val aCentEnd = particleCenterScreen + rDir * aCentLen
                            drawVectorArrow(
                                start = particleCenterScreen,
                                end = aCentEnd,
                                color = PurpleNeon,
                                label = "a_cent"
                            )
                        }
                    }
                } else {
                    // Lab frame: velocity in inertial space (AmberVibrant)
                    val vInertSpeed = ballVelInert.getDistance()
                    if (vInertSpeed > 0.05f) {
                        val vDir = Offset(ballVelInert.x / vInertSpeed, ballVelInert.y / vInertSpeed)
                        val vEnd = particleCenterScreen + vDir * (vInertSpeed * 16.dp.toPx()).coerceIn(12.dp.toPx(), 70.dp.toPx())
                        drawVectorArrow(
                            start = particleCenterScreen,
                            end = vEnd,
                            color = AmberVibrant,
                            label = "v_inertial"
                        )
                    }
                }

                // 7. Center Origin Spindle Hub
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFB0BEC5), Color(0xFF263238)),
                        center = center,
                        radius = 12.dp.toPx()
                    ),
                    radius = 9.dp.toPx(),
                    center = center
                )
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 17: Coriolis Effect",
                items = listOf(
                    "Formula" to "\\mathbf{a}_{coriolis} = -2 (\\mathbf{\\Omega} \\times \\mathbf{v}_{rel})",
                    "Angular Speed Ω" to "${round(omegaRadS * 100f) / 100f} rad/s",
                    "Relative Speed |v|" to "${round(speedRel * 100f) / 100f} m/s",
                    "Coriolis |a_cor|" to "${round(aCoriolisMag * 100f) / 100f} m/s²",
                    "Centrifugal |a_cent|" to if (isBalancedDish) "0.00 m/s² (Canceled)" else "${round(aCentrifugalMag * 100f) / 100f} m/s²",
                    "Hemisphere / Sense" to hemisphereLabel,
                    "Reference Frame" to activeFrame.label
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Presets Row
                Text(
                    text = "PHYSICAL REGIMES & SCENARIOS",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CoriolisPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedPreset = preset
                                    omegaRadS = preset.omegaRadS
                                    launchSpeedMps = preset.launchSpeedMps
                                    launchAngleDeg = preset.launchAngleDeg
                                    isBalancedDish = preset.isBalancedDish
                                    hasPressureGrad = preset.hasPressureGrad
                                    launchParticle(preset.launchSpeedMps, preset.launchAngleDeg, preset != CoriolisPreset.INWARD_SHOT)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) preset.color.copy(alpha = 0.22f) else ScienceDarkSurfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, preset.color) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = preset.icon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = preset.title,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) preset.color else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 2. Reference Frame & Surface Mode Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Frame Toggle
                    Button(
                        onClick = {
                            activeFrame = if (activeFrame == ReferenceFrame.ROTATING) ReferenceFrame.INERTIAL else ReferenceFrame.ROTATING
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeFrame == ReferenceFrame.ROTATING) CyanNeon.copy(alpha = 0.25f) else AmberVibrant.copy(alpha = 0.25f),
                            contentColor = if (activeFrame == ReferenceFrame.ROTATING) CyanNeon else AmberVibrant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    ) {
                        Text(
                            text = "${activeFrame.icon} ${if (activeFrame == ReferenceFrame.ROTATING) "Rotating Frame" else "Lab Frame"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Surface Mode Toggle
                    Button(
                        onClick = {
                            isBalancedDish = !isBalancedDish
                            selectedPreset = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBalancedDish) EmeraldNeon.copy(alpha = 0.25f) else PurpleNeon.copy(alpha = 0.25f),
                            contentColor = if (isBalancedDish) EmeraldNeon else PurpleNeon
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    ) {
                        Text(
                            text = if (isBalancedDish) "🥣 Balanced Dish" else "💿 Flat Turntable",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3. Physics Sliders: Paired Omega & Speed, plus Launch Direction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Turntable Ω",
                        value = omegaRadS,
                        range = -5.0f..5.0f,
                        valueDisplay = "${round(omegaRadS * 10f) / 10f} rad/s",
                        accentColor = CyanNeon,
                        onValueChange = {
                            omegaRadS = it
                            selectedPreset = null
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PhysicsSliderControl(
                        title = "Launch Speed v₀",
                        value = launchSpeedMps,
                        range = 0.5f..5.0f,
                        valueDisplay = "${round(launchSpeedMps * 10f) / 10f} m/s",
                        accentColor = AmberVibrant,
                        onValueChange = {
                            launchSpeedMps = it
                            selectedPreset = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                PhysicsSliderControl(
                    title = "Launch Direction θ",
                    value = launchAngleDeg,
                    range = 0f..360f,
                    valueDisplay = "${launchAngleDeg.toInt()}°",
                    accentColor = CoralNeon,
                    onValueChange = {
                        launchAngleDeg = it
                        selectedPreset = null
                    }
                )

                // 4. Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            launchParticle(launchSpeedMps, launchAngleDeg, selectedPreset != CoriolisPreset.INWARD_SHOT)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = "🚀 Launch Ball",
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
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
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
                            omegaRadS = 2.2f
                            launchSpeedMps = 2.5f
                            launchAngleDeg = 0f
                            isBalancedDish = false
                            hasPressureGrad = false
                            activeFrame = ReferenceFrame.ROTATING
                            selectedPreset = CoriolisPreset.OUTWARD_CANNON
                            isRunning = true
                            launchParticle(2.5f, 0f, startAtCenter = true)
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

// ─────────────────────────────────────────────────────────────────────────────
// Canvas Drawing Helper Functions
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawCoriolisLabBackground(w: Float, h: Float) {
    // Subtle laboratory grid background
    val gridStep = 45.dp.toPx()
    var gx = 0f
    while (gx < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.2f),
            start = Offset(gx, 0f),
            end = Offset(gx, h),
            strokeWidth = 0.8f
        )
        gx += gridStep
    }
    var gy = 0f
    while (gy < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.2f),
            start = Offset(0f, gy),
            end = Offset(w, gy),
            strokeWidth = 0.8f
        )
        gy += gridStep
    }
}

private fun DrawScope.drawTurntablePlatter(
    center: Offset,
    radiusPx: Float,
    rotationRad: Float,
    isBalancedDish: Boolean,
    omegaRadS: Float
) {
    // 1. Outer Turntable Rim & Drop Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.55f),
        radius = radiusPx + 8.dp.toPx(),
        center = center + Offset(0f, 6.dp.toPx())
    )

    // Outer boundary ring (metallic brushed brass / gunmetal)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF263238),
                Color(0xFF1E282D),
                Color(0xFF10171A)
            ),
            center = center,
            radius = radiusPx
        ),
        radius = radiusPx,
        center = center
    )

    // Inner turntable surface disc (subtle dish gradient if balanced)
    val discColors = if (isBalancedDish) {
        listOf(
            Color(0xFF0D2530),
            Color(0xFF091820),
            Color(0xFF050D12)
        )
    } else {
        listOf(
            Color(0xFF161C22),
            Color(0xFF101519),
            Color(0xFF0A0D10)
        )
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = discColors,
            center = center,
            radius = radiusPx * 0.95f
        ),
        radius = radiusPx * 0.95f,
        center = center
    )

    // Outer glowing rim border
    drawCircle(
        color = if (isBalancedDish) EmeraldNeon.copy(alpha = 0.45f) else CyanNeon.copy(alpha = 0.35f),
        radius = radiusPx * 0.95f,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    // Concentric Range Rings (1m, 2m, 3m distances)
    for (i in 1..3) {
        val rNorm = i / 3.0f
        drawCircle(
            color = ScienceBorder.copy(alpha = 0.3f),
            radius = radiusPx * 0.95f * rNorm,
            center = center,
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )
        )
    }

    // 2. Radial Spokes & Degree Notches (Rotating with the platter)
    val numSpokes = 12
    for (i in 0 until numSpokes) {
        val angle = rotationRad + i * (2 * PI.toFloat() / numSpokes)
        val spokeEnd = Offset(
            center.x + cos(angle) * (radiusPx * 0.93f),
            center.y + sin(angle) * (radiusPx * 0.93f)
        )
        drawLine(
            color = ScienceBorder.copy(alpha = 0.25f),
            start = center,
            end = spokeEnd,
            strokeWidth = 1.dp.toPx()
        )

        // Perimeter tick marks
        val tickStart = Offset(
            center.x + cos(angle) * (radiusPx * 0.88f),
            center.y + sin(angle) * (radiusPx * 0.88f)
        )
        drawLine(
            color = CyanNeon.copy(alpha = 0.4f),
            start = tickStart,
            end = spokeEnd,
            strokeWidth = 1.8f
        )
    }

    // 3. Curved Rotation Sense Indicator Arrow
    if (abs(omegaRadS) > 0.1f) {
        val arrowR = radiusPx * 0.78f
        val startAng = rotationRad + 0.2f
        val sweepAng = if (omegaRadS > 0) 0.6f else -0.6f
        val rotArrowPath = Path().apply {
            val steps = 20
            for (s in 0..steps) {
                val a = startAng + sweepAng * (s / steps.toFloat())
                val pt = Offset(center.x + cos(a) * arrowR, center.y + sin(a) * arrowR)
                if (s == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
            }
        }
        drawPath(
            path = rotArrowPath,
            color = AmberVibrant.copy(alpha = 0.6f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawCoriolisTrails(
    center: Offset,
    scale: Float,
    trail: List<TrajectoryPoint>,
    frame: ReferenceFrame
) {
    if (trail.size < 2) return

    val path = Path()
    for (i in trail.indices) {
        val pt = trail[i]
        val posM = if (frame == ReferenceFrame.ROTATING) pt.rotPos else pt.inertPos
        val screenPos = Offset(center.x + posM.x * scale, center.y + posM.y * scale)
        if (i == 0) {
            path.moveTo(screenPos.x, screenPos.y)
        } else {
            path.lineTo(screenPos.x, screenPos.y)
        }
    }

    // Glowing Trail
    drawPath(
        path = path,
        color = AmberVibrant.copy(alpha = 0.25f),
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
    )
    drawPath(
        path = path,
        color = AmberVibrant.copy(alpha = 0.85f),
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawVectorArrow(
    start: Offset,
    end: Offset,
    color: Color,
    label: String
) {
    val delta = end - start
    val len = delta.getDistance()
    if (len < 6f) return

    // Shaft
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )

    // Arrowhead
    val angle = atan2(delta.y, delta.x)
    val headLen = 8.dp.toPx()
    val headAng = 0.45f
    val headPath = Path().apply {
        moveTo(end.x, end.y)
        lineTo(
            end.x - headLen * cos(angle - headAng),
            end.y - headLen * sin(angle - headAng)
        )
        lineTo(
            end.x - headLen * cos(angle + headAng),
            end.y - headLen * sin(angle + headAng)
        )
        close()
    }
    drawPath(headPath, color)
}

