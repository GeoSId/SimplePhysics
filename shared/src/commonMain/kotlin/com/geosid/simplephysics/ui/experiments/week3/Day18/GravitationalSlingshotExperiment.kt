package com.geosid.simplephysics.ui.experiments.week3.Day18

import androidx.compose.animation.core.*
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontFamily
import com.geosid.simplephysics.ui.components.ExperimentHudCard
import com.geosid.simplephysics.ui.components.PhysicsSliderControl
import com.geosid.simplephysics.ui.components.ResetIcon
import com.geosid.simplephysics.ui.components.ResponsiveExperimentContainer
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*

/**
 * Physical scenarios for the Gravitational Slingshot simulation.
 */
enum class SlingshotPreset(
    val title: String,
    val icon: String,
    val planetSpeed: Float,
    val probeSpeed: Float,
    val impactParamOffset: Float,
    val description: String,
    val color: Color
) {
    VOYAGER_BOOST(
        title = "Voyager Boost",
        icon = "🚀",
        planetSpeed = 1.4f,
        probeSpeed = 1.6f,
        impactParamOffset = 42f,
        description = "Trailing flyby accelerates probe into interstellar space (+2 V_p)",
        color = CyanNeon
    ),
    PARKER_BRAKE(
        title = "Solar Brake",
        icon = "☀️",
        planetSpeed = 1.4f,
        probeSpeed = 2.0f,
        impactParamOffset = -42f,
        description = "Leading flyby sheds orbital energy to dive closer to the Sun",
        color = AmberVibrant
    ),
    OBERTH_BURN(
        title = "Oberth Burn",
        icon = "🔥",
        planetSpeed = 1.2f,
        probeSpeed = 1.5f,
        impactParamOffset = 36f,
        description = "Rocket fires at periapsis: extreme kinetic energy multiplication",
        color = CoralNeon
    ),
    HYPERBOLIC_PASS(
        title = "Free Flyby",
        icon = "🪐",
        planetSpeed = 1.0f,
        probeSpeed = 1.8f,
        impactParamOffset = 55f,
        description = "Symmetric hyperbolic deflection in planetary gravity field",
        color = EmeraldNeon
    )
}

/**
 * Reference frame for observing the gravitational slingshot encounter.
 */
enum class FrameOfReference(val label: String, val icon: String) {
    SUN("Heliocentric (Sun Frame)", "☀️"),
    PLANET("Planet-Centric (Rest Frame)", "🪐")
}

data class TrailPoint(
    val sunPos: Offset,
    val planetRelPos: Offset,
    val speed: Float,
    val isBurn: Boolean = false
)

@Composable
fun GravitationalSlingshotExperiment(
    modifier: Modifier = Modifier
) {
    // 1. Simulation Parameters
    var selectedPreset by remember { mutableStateOf<SlingshotPreset?>(SlingshotPreset.VOYAGER_BOOST) }
    var activeFrame by remember { mutableStateOf(FrameOfReference.SUN) }
    var planetSpeedParam by remember { mutableStateOf(1.4f) }
    var probeSpeedParam by remember { mutableStateOf(1.6f) }
    var impactOffsetParam by remember { mutableStateOf(42f) } // positive = trailing (speed gain), negative = leading (brake)
    var isOberthActive by remember { mutableStateOf(false) }
    var hasFiredOberthThisPass by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(true) }

    // 2. Dynamic Physical States (Normalized coordinates & units)
    var planetX by remember { mutableStateOf(-120f) }
    var probePos by remember { mutableStateOf(Offset(-220f, 42f)) }
    var probeVel by remember { mutableStateOf(Offset(1.6f * 65f, 0f)) }
    var closestApproachDist by remember { mutableStateOf(Float.MAX_VALUE) }
    var maxObservedSpeed by remember { mutableStateOf(0f) }
    var exitSpeed by remember { mutableStateOf(0f) }
    var periapsisPassed by remember { mutableStateOf(false) }

    // Trail breadcrumbs
    val trailPoints = remember { mutableStateListOf<TrailPoint>() }

    // Visual pulsation for planet atmosphere & thruster
    val infiniteTransition = rememberInfiniteTransition()
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Function to re-launch / reset probe
    val launchProbe = { vIn: Float, vPlanet: Float, impactOffset: Float, doBurn: Boolean ->
        planetX = -140f
        probePos = Offset(-240f, impactOffset)
        probeVel = Offset(vIn * 65f, 0f)
        trailPoints.clear()
        closestApproachDist = Float.MAX_VALUE
        maxObservedSpeed = vIn * 65f
        exitSpeed = 0f
        periapsisPassed = false
        hasFiredOberthThisPass = false
        isOberthActive = doBurn
    }

    // Initialize with preset
    LaunchedEffect(Unit) {
        val p = SlingshotPreset.VOYAGER_BOOST
        launchProbe(p.probeSpeed, p.planetSpeed, p.impactParamOffset, false)
    }

    // 3. High-Precision Symplectic / Velocity Verlet Physics Loop (60-120 FPS)
    LaunchedEffect(isRunning, planetSpeedParam, probeSpeedParam, impactOffsetParam, isOberthActive) {
        var lastTime = withFrameNanos { it }
        val gmPlanet = 850000f // Gravitational parameter G * M_planet
        val planetRadiusPx = 30f
        val planetSpeedPxS = planetSpeedParam * 55f

        while (true) {
            val now = withFrameNanos { it }
            val rawDt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
            lastTime = now

            if (isRunning) {
                // Sub-stepping for orbital accuracy near periapsis
                val subSteps = 6
                val dt = rawDt / subSteps

                for (step in 0 until subSteps) {
                    // Update moving planet position (Sun frame moves along +X axis)
                    planetX += planetSpeedPxS * dt
                    val currentPlanetPos = Offset(planetX, 0f)

                    // Vector from planet to probe
                    val rVec = probePos - currentPlanetPos
                    val rDistSq = rVec.x * rVec.x + rVec.y * rVec.y
                    val rDist = sqrt(rDistSq).coerceAtLeast(planetRadiusPx * 0.9f)

                    // Track closest approach
                    if (rDist < closestApproachDist) {
                        closestApproachDist = rDist
                    } else if (rDist > closestApproachDist + 5f && !periapsisPassed) {
                        periapsisPassed = true
                    }

                    // 1. Planetary gravitational acceleration (inverse-square central field)
                    val aGravMag = gmPlanet / (rDistSq + 120f)
                    val aGravX = -rVec.x / rDist * aGravMag
                    val aGravY = -rVec.y / rDist * aGravMag

                    // 2. Velocity integration & Oberth thruster burn at periapsis
                    var dvBurnX = 0f
                    var dvBurnY = 0f
                    var isBurningNow = false

                    if (isOberthActive && !hasFiredOberthThisPass && rDist <= closestApproachDist + 8f) {
                        val currentSpeed = probeVel.getDistance()
                        if (currentSpeed > 0.1f) {
                            val boostMagnitude = 75f // Oberth impulse Δv
                            dvBurnX = (probeVel.x / currentSpeed) * boostMagnitude
                            dvBurnY = (probeVel.y / currentSpeed) * boostMagnitude
                            hasFiredOberthThisPass = true
                            isBurningNow = true
                        }
                    }

                    probeVel = Offset(
                        probeVel.x + aGravX * dt + dvBurnX,
                        probeVel.y + aGravY * dt + dvBurnY
                    )

                    // Position integration
                    probePos += probeVel * dt

                    // Update speed statistics
                    val currentSpeed = probeVel.getDistance()
                    if (currentSpeed > maxObservedSpeed) {
                        maxObservedSpeed = currentSpeed
                    }
                    if (probePos.x > planetX + 160f) {
                        exitSpeed = currentSpeed
                    }

                    // Record trail history
                    if (step == 0) {
                        val relPos = probePos - currentPlanetPos
                        trailPoints.add(
                            TrailPoint(
                                sunPos = probePos,
                                planetRelPos = relPos,
                                speed = currentSpeed,
                                isBurn = isBurningNow
                            )
                        )
                        if (trailPoints.size > 280) {
                            trailPoints.removeAt(0)
                        }
                    }

                    // Auto-loop when probe travels past encounter zone
                    if (probePos.x > 320f || probePos.x < -360f || abs(probePos.y) > 280f) {
                        launchProbe(probeSpeedParam, planetSpeedParam, impactOffsetParam, selectedPreset == SlingshotPreset.OBERTH_BURN)
                        break
                    }
                }
            }
        }
    }

    // Telemetry computations
    val vInNormalized = probeSpeedParam
    val vPlanetNormalized = planetSpeedParam
    val vCurrentMps = round(probeVel.getDistance() / 15f * 10f) / 10f
    val vMaxTheoretical = round((vInNormalized + 2f * vPlanetNormalized) * 10f) / 10f
    val currentBoostPercent = if (vInNormalized > 0f) {
        val initialSpeedPx = vInNormalized * 65f
        val currentSpeedPx = probeVel.getDistance()
        round(((currentSpeedPx - initialSpeedPx) / initialSpeedPx) * 100f).toInt()
    } else 0

    val deflectionEstimateDeg = run {
        val b = abs(impactOffsetParam).coerceAtLeast(10f)
        val deltaRad = 2f * atan2(850000f, (b * probeVel.getDistanceSquared() * 0.003f))
        (deltaRad * 180f / PI.toFloat()).coerceIn(0f, 180f).toInt()
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag to adjust impact parameter b. Watch probe gain up to +2 V_planet from moving Jupiter!",
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 18: Gravitational Slingshot & Oberth Effect",
                items = listOf(
                    "Formula" to "v_{max} = v_{in} + 2 v_{planet}",
                    "Probe Speed (v)" to "$vCurrentMps km/s (${if (currentBoostPercent >= 0) "+$currentBoostPercent%" else "$currentBoostPercent%"})",
                    "Max Theoretical" to "$vMaxTheoretical km/s (v_in + 2v_p)",
                    "Deflection Angle (δ)" to "$deflectionEstimateDeg°",
                    "Status" to if (isOberthActive && hasFiredOberthThisPass) "OBERTH BOOST FIRED" else if (periapsisPassed) "DEPARTURE PHASE" else "APPROACH PHASE"
                )
            )
        },
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            impactOffsetParam = (impactOffsetParam + dragAmount.y * 0.45f).coerceIn(-90f, 90f)
                            selectedPreset = null
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val cy = size.height * 0.40f
                            impactOffsetParam = (offset.y - cy).coerceIn(-90f, 90f)
                            selectedPreset = null
                            launchProbe(probeSpeedParam, planetSpeedParam, impactOffsetParam, isOberthActive)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.50f, h * 0.40f)

                // 1. Deep Space Stellar Coordinate Grid & Starfield
                drawSpaceStarfield(w, h, center)

                // 2. Render Reference Frame
                val currentPlanetCenter = if (activeFrame == FrameOfReference.SUN) {
                    Offset(center.x + planetX, center.y)
                } else {
                    center // In Planet Frame, the planet is fixed at canvas center
                }

                val currentProbeCenter = if (activeFrame == FrameOfReference.SUN) {
                    Offset(center.x + probePos.x, center.y + probePos.y)
                } else {
                    Offset(center.x + (probePos.x - planetX), center.y + probePos.y)
                }

                // Draw Gravitational Influence Sphere (SOI)
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.08f),
                    radius = 95.dp.toPx(),
                    center = currentPlanetCenter
                )
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.35f),
                    radius = 95.dp.toPx(),
                    center = currentPlanetCenter,
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                )

                // 3. Draw Trajectory Ribbon Trail
                if (trailPoints.size > 1) {
                    val trailPath = Path()
                    var isFirst = true

                    for (pt in trailPoints) {
                        val ptScreen = if (activeFrame == FrameOfReference.SUN) {
                            Offset(center.x + pt.sunPos.x, center.y + pt.sunPos.y)
                        } else {
                            Offset(center.x + pt.planetRelPos.x, center.y + pt.planetRelPos.y)
                        }

                        if (isFirst) {
                            trailPath.moveTo(ptScreen.x, ptScreen.y)
                            isFirst = false
                        } else {
                            trailPath.lineTo(ptScreen.x, ptScreen.y)
                        }
                    }

                    // Dynamic multi-color speed gradient
                    drawPath(
                        path = trailPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                CyanNeon.copy(alpha = 0.2f),
                                AmberVibrant.copy(alpha = 0.6f),
                                CoralNeon
                            )
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 4. Draw Impact Parameter Guideline
                if (activeFrame == FrameOfReference.SUN) {
                    val aimY = center.y + impactOffsetParam
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(center.x - 260f, aimY),
                        end = Offset(center.x + 260f, aimY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                // 5. Draw Planet Jupiter (Atmospheric Bands & Core Glow)
                drawJupiterPlanet(
                    center = currentPlanetCenter,
                    radius = 28.dp.toPx() * glowPulse,
                    vPlanetPx = if (activeFrame == FrameOfReference.SUN) planetSpeedParam * 55f else 0f
                )

                // 6. Draw Spacecraft Probe & Rocket Plume
                drawSpacecraftProbe(
                    probePos = currentProbeCenter,
                    velocity = probeVel,
                    isBurning = hasFiredOberthThisPass && !periapsisPassed
                )

                // 7. Vector Arrows (Velocity & Gravitational Pull)
                val probeSpeed = probeVel.getDistance()
                if (probeSpeed > 1f) {
                    // Velocity Vector (Cyan)
                    val vScaled = (probeVel / probeSpeed) * min(48.dp.toPx(), probeSpeed * 0.35f)
                    drawVectorArrow(
                        start = currentProbeCenter,
                        end = currentProbeCenter + vScaled,
                        color = CyanNeon,
                        label = "v = ${round(probeSpeed / 15f * 10f) / 10f}"
                    )

                    // Gravitational Pull Vector toward Planet (CoralNeon)
                    val toPlanet = currentPlanetCenter - currentProbeCenter
                    val distP = toPlanet.getDistance().coerceAtLeast(1f)
                    val gArrow = (toPlanet / distP) * min(36.dp.toPx(), 4500f / distP)
                    drawVectorArrow(
                        start = currentProbeCenter,
                        end = currentProbeCenter + gArrow,
                        color = CoralNeon,
                        label = "F_g"
                    )
                }
            }
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Presets Row
                Text(
                    text = "AEROSPACE MISSIONS & GRAVITY ASSIST SCENARIOS",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SlingshotPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedPreset = preset
                                    planetSpeedParam = preset.planetSpeed
                                    probeSpeedParam = preset.probeSpeed
                                    impactOffsetParam = preset.impactParamOffset
                                    isOberthActive = (preset == SlingshotPreset.OBERTH_BURN)
                                    launchProbe(preset.probeSpeed, preset.planetSpeed, preset.impactParamOffset, isOberthActive)
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

                // 2. Reference Frame & Oberth Burn Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            activeFrame = if (activeFrame == FrameOfReference.SUN) FrameOfReference.PLANET else FrameOfReference.SUN
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeFrame == FrameOfReference.SUN) AmberVibrant.copy(alpha = 0.25f) else CyanNeon.copy(alpha = 0.25f),
                            contentColor = if (activeFrame == FrameOfReference.SUN) AmberVibrant else CyanNeon
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(32.dp)
                    ) {
                        Text(
                            text = "${activeFrame.icon} ${activeFrame.label}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            isOberthActive = !isOberthActive
                            if (isOberthActive) {
                                selectedPreset = SlingshotPreset.OBERTH_BURN
                                launchProbe(probeSpeedParam, planetSpeedParam, impactOffsetParam, true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOberthActive) CoralNeon.copy(alpha = 0.30f) else ScienceDarkSurfaceVariant,
                            contentColor = if (isOberthActive) CoralNeon else TextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    ) {
                        Text(
                            text = if (isOberthActive) "🔥 Oberth Active" else "🚀 Enable Oberth Burn",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3. Paired Sliders: Planet Velocity & Probe Inward Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Planet Speed V_p",
                        value = planetSpeedParam,
                        range = 0.2f..2.5f,
                        valueDisplay = "${round(planetSpeedParam * 10f) / 10f} km/s",
                        accentColor = AmberVibrant,
                        onValueChange = {
                            planetSpeedParam = it
                            selectedPreset = null
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PhysicsSliderControl(
                        title = "Probe Speed v_in",
                        value = probeSpeedParam,
                        range = 0.8f..3.0f,
                        valueDisplay = "${round(probeSpeedParam * 10f) / 10f} km/s",
                        accentColor = CyanNeon,
                        onValueChange = {
                            probeSpeedParam = it
                            selectedPreset = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Impact Parameter Offset Slider (Trailing vs Leading flyby)
                PhysicsSliderControl(
                    title = "Impact Parameter b (Trailing + / Leading -)",
                    value = impactOffsetParam,
                    range = -80f..80f,
                    valueDisplay = "${impactOffsetParam.toInt()} px (${if (impactOffsetParam >= 0) "Boost" else "Brake"})",
                    accentColor = if (impactOffsetParam >= 0) CyanNeon else CoralNeon,
                    onValueChange = {
                        impactOffsetParam = it
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
                            launchProbe(probeSpeedParam, planetSpeedParam, impactOffsetParam, isOberthActive)
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
                            text = "🚀 Launch Probe",
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
                            val p = SlingshotPreset.VOYAGER_BOOST
                            selectedPreset = p
                            planetSpeedParam = p.planetSpeed
                            probeSpeedParam = p.probeSpeed
                            impactOffsetParam = p.impactParamOffset
                            activeFrame = FrameOfReference.SUN
                            isOberthActive = false
                            isRunning = true
                            launchProbe(p.probeSpeed, p.planetSpeed, p.impactParamOffset, false)
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
// Canvas Drawing Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawSpaceStarfield(w: Float, h: Float, center: Offset) {
    // Subtle interstellar grid
    val gridStep = 45.dp.toPx()
    var gx = 0f
    while (gx < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.18f),
            start = Offset(gx, 0f),
            end = Offset(gx, h),
            strokeWidth = 0.7f
        )
        gx += gridStep
    }
    var gy = 0f
    while (gy < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.18f),
            start = Offset(0f, gy),
            end = Offset(w, gy),
            strokeWidth = 0.7f
        )
        gy += gridStep
    }

    // Static distant star dots
    val stars = listOf(
        Offset(w * 0.12f, h * 0.15f), Offset(w * 0.28f, h * 0.08f),
        Offset(w * 0.75f, h * 0.12f), Offset(w * 0.88f, h * 0.25f),
        Offset(w * 0.18f, h * 0.65f), Offset(w * 0.35f, h * 0.72f),
        Offset(w * 0.65f, h * 0.68f), Offset(w * 0.82f, h * 0.58f)
    )
    for (s in stars) {
        drawCircle(Color.White.copy(alpha = 0.45f), 1.5.dp.toPx(), s)
    }
}

private fun DrawScope.drawJupiterPlanet(center: Offset, radius: Float, vPlanetPx: Float) {
    // Gravitational glow corona
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                AmberVibrant.copy(alpha = 0.35f),
                Color(0xFFE65100).copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.8f
        ),
        radius = radius * 1.8f,
        center = center
    )

    // Spherical base gradient (Jupiter giant planet)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFCC80),
                Color(0xFFEF6C00),
                Color(0xFF4E342E)
            ),
            center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
            radius = radius * 1.4f
        ),
        radius = radius,
        center = center
    )

    // Atmospheric cloud bands
    val bandHeight = radius * 0.28f
    drawLine(
        color = Color(0xFFD7CCC8).copy(alpha = 0.65f),
        start = Offset(center.x - radius * 0.85f, center.y - bandHeight),
        end = Offset(center.x + radius * 0.85f, center.y - bandHeight),
        strokeWidth = 3.dp.toPx()
    )
    drawLine(
        color = Color(0xFFBF360C).copy(alpha = 0.65f),
        start = Offset(center.x - radius * 0.95f, center.y),
        end = Offset(center.x + radius * 0.95f, center.y),
        strokeWidth = 4.dp.toPx()
    )
    drawLine(
        color = Color(0xFFFFE0B2).copy(alpha = 0.55f),
        start = Offset(center.x - radius * 0.85f, center.y + bandHeight),
        end = Offset(center.x + radius * 0.85f, center.y + bandHeight),
        strokeWidth = 3.dp.toPx()
    )

    // Jupiter Great Red Spot (cyclonic storm)
    drawOval(
        color = CoralNeon.copy(alpha = 0.85f),
        topLeft = Offset(center.x + radius * 0.25f, center.y + bandHeight * 0.35f),
        size = Size(radius * 0.38f, radius * 0.22f)
    )

    // Planet velocity arrow (when moving in Sun frame)
    if (abs(vPlanetPx) > 1f) {
        val arrowLen = 42.dp.toPx()
        val startPt = Offset(center.x, center.y - radius - 10.dp.toPx())
        val endPt = Offset(center.x + arrowLen, center.y - radius - 10.dp.toPx())
        drawVectorArrow(startPt, endPt, AmberVibrant, "V_p = 1.4")
    }
}

private fun DrawScope.drawSpacecraftProbe(
    probePos: Offset,
    velocity: Offset,
    isBurning: Boolean
) {
    val speed = velocity.getDistance()
    val angle = atan2(velocity.y, velocity.x) * 180f / PI.toFloat()

    // Outer glow
    drawCircle(
        color = if (isBurning) CoralNeon.copy(alpha = 0.45f) else CyanNeon.copy(alpha = 0.35f),
        radius = 12.dp.toPx(),
        center = probePos
    )

    // Spacecraft Body (pointed triangular craft rotated along velocity)
    rotate(degrees = angle, pivot = probePos) {
        // Rocket exhaust plume if burning
        if (isBurning || speed > 120f) {
            val plumeLength = if (isBurning) 24.dp.toPx() else 14.dp.toPx()
            val plumePath = Path().apply {
                moveTo(probePos.x - 6.dp.toPx(), probePos.y)
                lineTo(probePos.x - 6.dp.toPx() - plumeLength, probePos.y - 4.dp.toPx())
                lineTo(probePos.x - 6.dp.toPx() - plumeLength * 1.3f, probePos.y)
                lineTo(probePos.x - 6.dp.toPx() - plumeLength, probePos.y + 4.dp.toPx())
                close()
            }
            drawPath(
                path = plumePath,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, CoralNeon, AmberVibrant.copy(alpha = 0.1f)),
                    start = Offset(probePos.x - 6.dp.toPx(), probePos.y),
                    end = Offset(probePos.x - 6.dp.toPx() - plumeLength * 1.3f, probePos.y)
                )
            )
        }

        // Spacecraft hull
        val craftPath = Path().apply {
            moveTo(probePos.x + 8.dp.toPx(), probePos.y)
            lineTo(probePos.x - 6.dp.toPx(), probePos.y - 5.dp.toPx())
            lineTo(probePos.x - 3.dp.toPx(), probePos.y)
            lineTo(probePos.x - 6.dp.toPx(), probePos.y + 5.dp.toPx())
            close()
        }
        drawPath(craftPath, Color.White)
        drawPath(craftPath, CyanNeon, style = Stroke(width = 1.2.dp.toPx()))

        // Solar panels
        drawLine(
            color = CyanNeon,
            start = Offset(probePos.x - 2.dp.toPx(), probePos.y - 8.dp.toPx()),
            end = Offset(probePos.x - 2.dp.toPx(), probePos.y + 8.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun DrawScope.drawVectorArrow(start: Offset, end: Offset, color: Color, label: String) {
    val dir = end - start
    val len = dir.getDistance()
    if (len < 4f) return

    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 2.2f,
        cap = StrokeCap.Round
    )

    // Arrowhead
    val angle = atan2(dir.y, dir.x)
    val headLen = 8.dp.toPx()
    val headAngle = 28f * PI.toFloat() / 180f

    val p1 = Offset(
        end.x - headLen * cos(angle - headAngle),
        end.y - headLen * sin(angle - headAngle)
    )
    val p2 = Offset(
        end.x - headLen * cos(angle + headAngle),
        end.y - headLen * sin(angle + headAngle)
    )

    val headPath = Path().apply {
        moveTo(end.x, end.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }
    drawPath(headPath, color)
}
