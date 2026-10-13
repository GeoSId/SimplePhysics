package com.geosid.simplephysics.ui.experiments.week6.Day37

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
import kotlin.random.Random

/**
 * Optical and aerodynamic presets for Day 37: Toroidal Vortex Rings.
 */
enum class VortexRingPreset(
    val label: String,
    val title: String,
    val orificeRadiusCm: Float,
    val jetVelocityMs: Float,
    val isLeapfrogging: Boolean,
    val isWallCollision: Boolean,
    val hasTargetCandle: Boolean,
    val desc: String
) {
    VORTEX_CANNON(
        label = "Vortex Cannon",
        title = "Pneumatic Toroidal Smoke Ring Cannon",
        orificeRadiusCm = 6.0f,
        jetVelocityMs = 12.0f,
        isLeapfrogging = false,
        isWallCollision = false,
        hasTargetCandle = true,
        desc = "High-velocity air pulse shears at the circular nozzle perimeter, rolling into a coherent toroidal air bullet"
    ),
    LEAPFROGGING(
        label = "Leapfrogging",
        title = "Helmholtz Dual-Ring Leapfrogging Dance",
        orificeRadiusCm = 5.0f,
        jetVelocityMs = 9.0f,
        isLeapfrogging = true,
        isWallCollision = false,
        hasTargetCandle = false,
        desc = "Two coaxial vortex rings pass through each other periodically: rear ring contracts & accelerates through the leader"
    ),
    WALL_IMPACT(
        label = "Wall Rebound",
        title = "Solid Boundary Impingement & Radial Spreading",
        orificeRadiusCm = 6.5f,
        jetVelocityMs = 10.5f,
        isLeapfrogging = false,
        isWallCollision = true,
        hasTargetCandle = false,
        desc = "Vortex ring impinges on a flat wall, expands radially outward, and induces an opposite-sign secondary vortex"
    ),
    MICRO_VORTEX(
        label = "Micro Vortex",
        title = "High-Circulation Compact Smoke Core",
        orificeRadiusCm = 3.5f,
        jetVelocityMs = 16.0f,
        isLeapfrogging = false,
        isWallCollision = false,
        hasTargetCandle = true,
        desc = "Small aperture with intense impulse generates a razor-sharp, high-speed toroidal ring with minimal dispersion"
    )
}

/**
 * State of a single propagating toroidal vortex ring.
 */
data class ToroidalRing(
    var posX: Float,
    var posY: Float,
    var radiusPx: Float,
    var coreRadiusPx: Float,
    var circulationGamma: Float,
    var speedPxPerSec: Float,
    var life: Float = 1.0f,
    var id: Int = 0
)

/**
 * Visual tracer smoke particle inside the circulating torus.
 */
data class SmokeParticle(
    var relR: Float,
    var relZ: Float,
    var angleRad: Float,
    var spinRate: Float,
    var color: Color,
    var alpha: Float
)

@Composable
fun VortexRingsExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(VortexRingPreset.VORTEX_CANNON) }

    // Physical Parameters
    var orificeRadiusCm by remember { mutableStateOf(currentPreset.orificeRadiusCm) }
    var jetVelocityMs by remember { mutableStateOf(currentPreset.jetVelocityMs) }
    var fluidViscosity by remember { mutableStateOf(0.18f) } // Viscous decay rate
    var airDensity by remember { mutableStateOf(1.225f) } // kg/m³

    // Simulation Display Toggles
    var showStreamlines by remember { mutableStateOf(true) }
    var showVorticityCores by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(true) }

    // Piston pulse trigger state
    var pulseTrigger by remember { mutableStateOf(0) }
    var candleExtinguished by remember { mutableStateOf(false) }

    // Swirl rotation animation loop for continuous core circulation
    val infiniteTransition = rememberInfiniteTransition()
    val coreSpinPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRunning) 800 else 999999, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Candle flame flicker animation
    val flameFlicker by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Physical calculations in SI units
    val orificeRadiusM = orificeRadiusCm / 100f
    val ringRadiusM = orificeRadiusM * 1.15f // Initial slug roll-up expansion
    val coreRadiusM = 0.22f * ringRadiusM

    // 1. Kelvin's circulation theorem: Gamma = 2 * pi * R * v_jet
    val circulationGamma = 2f * PI.toFloat() * orificeRadiusM * jetVelocityMs

    // 2. Kelvin-Hicks self-induced translation velocity:
    // U = (Gamma / (4 * pi * R)) * [ln(8 * R / a) - 1/4]
    val logRatio = ln((8f * ringRadiusM) / max(coreRadiusM, 1e-4f)) - 0.25f
    val translationSpeedMs = (circulationGamma / (4f * PI.toFloat() * ringRadiusM)) * logRatio

    // 3. Hydrodynamic Impulse: P = pi * rho * Gamma * R^2
    val hydrodynamicImpulse = PI.toFloat() * airDensity * circulationGamma * ringRadiusM * ringRadiusM

    // 4. Kinetic Energy: E = 0.5 * rho * Gamma^2 * R * [ln(8R/a) - 7/4]
    val energyJoules = 0.5f * airDensity * circulationGamma * circulationGamma * ringRadiusM *
            (ln((8f * ringRadiusM) / max(coreRadiusM, 1e-4f)) - 1.75f).coerceAtLeast(0.1f)

    // Interactive Animated Ring States
    var ring1Progress by remember { mutableStateOf(0.0f) }
    var ring2Progress by remember { mutableStateOf(0.0f) }

    // High frequency physics stepping
    LaunchedEffect(currentPreset, pulseTrigger, isRunning) {
        if (!isRunning) return@LaunchedEffect
        ring1Progress = 0f
        ring2Progress = if (currentPreset.isLeapfrogging) -0.28f else -1.0f
        candleExtinguished = false

        val startTime = withFrameNanos { it }
        val durationMs = 2800L

        while (true) {
            val now = withFrameNanos { it }
            val elapsed = (now - startTime) / 1_000_000L
            val p1 = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
            ring1Progress = p1

            if (currentPreset.isLeapfrogging) {
                val p2 = ((elapsed.toFloat() - 450f) / durationMs).coerceIn(0f, 1f)
                ring2Progress = p2
            }

            if (p1 > 0.82f && currentPreset.hasTargetCandle) {
                candleExtinguished = true
            }

            if (p1 >= 1.0f) {
                // Auto-loop
                break
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Tap '💨 Fire Vortex Ring' to blast a toroidal smoke bullet! Watch Kelvin's circulation preserve the donut core.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            pulseTrigger++
                            candleExtinguished = false
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            jetVelocityMs = (jetVelocityMs + dragAmount.x * 0.05f).coerceIn(4f, 25f)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated apparatus center: cy at 0.40 * h
                val cy = h * 0.40f
                val cannonX = w * 0.12f
                val cannonRadiusPx = orificeRadiusCm * 6.5f

                // 1. Dark Atmospheric Wind Tunnel Background
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF090D16), Color(0xFF111827), Color(0xFF0F172A)),
                        startY = 0f,
                        endY = h
                    ),
                    size = Size(w, h)
                )

                // Scientific Alignment Grid
                drawAtmosphericGrid(w, h, cy)

                // 2. Draw Vortex Cannon Nozzle (Left)
                drawVortexCannon(
                    cannonX = cannonX,
                    cy = cy,
                    cannonRadiusPx = cannonRadiusPx,
                    isFiring = ring1Progress in 0.01f..0.22f
                )

                // 3. Draw Target Candle Flame or Solid Impact Wall (Right)
                if (currentPreset.isWallCollision) {
                    val wallX = w * 0.88f
                    drawImpactWall(wallX = wallX, h = h, cy = cy)
                } else if (currentPreset.hasTargetCandle) {
                    val candleX = w * 0.86f
                    drawTargetCandle(
                        candleX = candleX,
                        candleBaseY = cy + 45f,
                        isExtinguished = candleExtinguished,
                        flameFlicker = flameFlicker
                    )
                }

                // 4. Render Propagating Toroidal Vortex Rings
                if (currentPreset.isLeapfrogging) {
                    // Leapfrogging Dual-Ring Simulation
                    drawLeapfroggingRings(
                        w = w,
                        cy = cy,
                        cannonX = cannonX,
                        ring1Progress = ring1Progress,
                        ring2Progress = ring2Progress,
                        baseRadiusPx = cannonRadiusPx * 1.15f,
                        coreSpinPhase = coreSpinPhase,
                        showStreamlines = showStreamlines,
                        showVorticityCores = showVorticityCores
                    )
                } else {
                    // Single High-Velocity Toroidal Ring
                    drawSingleToroidalRing(
                        w = w,
                        cy = cy,
                        cannonX = cannonX,
                        progress = ring1Progress,
                        baseRadiusPx = cannonRadiusPx * 1.15f,
                        coreSpinPhase = coreSpinPhase,
                        showStreamlines = showStreamlines,
                        showVorticityCores = showVorticityCores,
                        isWallCollision = currentPreset.isWallCollision
                    )
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 37: Toroidal Vortex Rings",
                items = listOf(
                    "Kelvin's Theorem" to "DΓ/Dt = 0  (Circulation Invariant)",
                    "Circulation (Γ)" to "${round(circulationGamma * 100f) / 100f} m²/s",
                    "Propagation Speed (U)" to "${round(translationSpeedMs * 10f) / 10f} m/s",
                    "Hydrodynamic Impulse" to "${round(hydrodynamicImpulse * 1000f) / 10f} mN·s",
                    "Kinetic Energy" to "${round(energyJoules * 1000f) / 10f} mJ",
                    "Ring Aspect (R/a)" to "${round((ringRadiusM / coreRadiusM) * 10f) / 10f}"
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
                    items(VortexRingPreset.values()) { preset ->
                        val isSelected = currentPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                currentPreset = preset
                                orificeRadiusCm = preset.orificeRadiusCm
                                jetVelocityMs = preset.jetVelocityMs
                                pulseTrigger++
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

                // Paired Sliders Row 1: Orifice Radius & Jet Velocity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Orifice Radius (R₀)",
                        value = orificeRadiusCm,
                        range = 2.5f..9.0f,
                        valueDisplay = "${round(orificeRadiusCm * 10f) / 10f} cm",
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            orificeRadiusCm = it
                            pulseTrigger++
                        }
                    )

                    PhysicsSliderControl(
                        title = "Piston Pulse Speed (v)",
                        value = jetVelocityMs,
                        range = 4.0f..25.0f,
                        valueDisplay = "${round(jetVelocityMs * 10f) / 10f} m/s",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            jetVelocityMs = it
                            pulseTrigger++
                        }
                    )
                }

                // Paired Sliders Row 2: Air Viscosity & Density
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Viscous Decay (ν)",
                        value = fluidViscosity,
                        range = 0.05f..0.50f,
                        valueDisplay = "${round(fluidViscosity * 100f) / 100f}",
                        accentColor = CoralNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { fluidViscosity = it }
                    )

                    PhysicsSliderControl(
                        title = "Fluid Density (ρ)",
                        value = airDensity,
                        range = 0.4f..2.0f,
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
                        selected = showStreamlines,
                        onClick = { showStreamlines = !showStreamlines },
                        label = { Text("Streamlines", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CyanNeon,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    FilterChip(
                        selected = showVorticityCores,
                        onClick = { showVorticityCores = !showVorticityCores },
                        label = { Text("Vorticity Cores", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurpleNeon.copy(alpha = 0.25f),
                            selectedLabelColor = PurpleNeon,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )
                }

                // Action Bar: Fire Button, Pause/Run, and Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            pulseTrigger++
                            candleExtinguished = false
                        },
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
                            text = "💨 Fire Vortex Ring",
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
                            orificeRadiusCm = currentPreset.orificeRadiusCm
                            jetVelocityMs = currentPreset.jetVelocityMs
                            fluidViscosity = 0.18f
                            airDensity = 1.225f
                            pulseTrigger++
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
 * Coordinate reference grid.
 */
private fun DrawScope.drawAtmosphericGrid(w: Float, h: Float, cy: Float) {
    // Optical / Fluid Axis of Symmetry
    drawLine(
        color = ScienceBorder.copy(alpha = 0.45f),
        start = Offset(0f, cy),
        end = Offset(w, cy),
        strokeWidth = 1.2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
    )
}

/**
 * 1. Vortex Cannon Nozzle Body and Piston.
 */
private fun DrawScope.drawVortexCannon(
    cannonX: Float,
    cy: Float,
    cannonRadiusPx: Float,
    isFiring: Boolean
) {
    val chamberW = 50f
    val chamberTop = cy - cannonRadiusPx
    val chamberBottom = cy + cannonRadiusPx

    // Chamber Housing
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF334155)),
            startX = cannonX - chamberW,
            endX = cannonX
        ),
        topLeft = Offset(cannonX - chamberW, chamberTop),
        size = Size(chamberW, chamberBottom - chamberTop)
    )
    drawRect(
        color = ScienceBorder,
        topLeft = Offset(cannonX - chamberW, chamberTop),
        size = Size(chamberW, chamberBottom - chamberTop),
        style = Stroke(2f)
    )

    // Nozzle Lip Orifice (Sharp Edges that roll up the shear layer)
    drawRoundRect(
        color = CyanNeon,
        topLeft = Offset(cannonX - 4f, chamberTop - 4f),
        size = Size(8f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
    )
    drawRoundRect(
        color = CyanNeon,
        topLeft = Offset(cannonX - 4f, chamberBottom - 6f),
        size = Size(8f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
    )

    // Firing Impulse Glow
    if (isFiring) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CyanNeon.copy(alpha = 0.65f), Color.Transparent),
                center = Offset(cannonX + 6f, cy),
                radius = cannonRadiusPx * 0.9f
            ),
            radius = cannonRadiusPx * 0.9f,
            center = Offset(cannonX + 6f, cy)
        )
    }
}

/**
 * 2. Target Candle Flame.
 */
private fun DrawScope.drawTargetCandle(
    candleX: Float,
    candleBaseY: Float,
    isExtinguished: Boolean,
    flameFlicker: Float
) {
    val candleW = 14f
    val candleH = 40f
    val candleTop = candleBaseY - candleH

    // Candle Wax Pillar
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
            startX = candleX - candleW * 0.5f,
            endX = candleX + candleW * 0.5f
        ),
        topLeft = Offset(candleX - candleW * 0.5f, candleTop),
        size = Size(candleW, candleH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
    )

    // Wick
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(candleX, candleTop),
        end = Offset(candleX, candleTop - 8f),
        strokeWidth = 2f
    )

    if (!isExtinguished) {
        // Glowing Flame
        val flameY = candleTop - 16f
        val flameR = 10f * flameFlicker
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AmberVibrant, CoralNeon, Color.Transparent),
                center = Offset(candleX, flameY),
                radius = flameR * 2.2f
            ),
            radius = flameR * 2.2f,
            center = Offset(candleX, flameY)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, AmberVibrant),
                center = Offset(candleX, flameY),
                radius = flameR
            ),
            radius = flameR,
            center = Offset(candleX, flameY)
        )
    } else {
        // Smoke wisps when extinguished
        val smokePath = Path().apply {
            moveTo(candleX, candleTop - 8f)
            cubicTo(
                candleX + 6f, candleTop - 18f,
                candleX - 4f, candleTop - 28f,
                candleX + 8f, candleTop - 38f
            )
        }
        drawPath(
            path = smokePath,
            color = Color.White.copy(alpha = 0.45f),
            style = Stroke(width = 1.5f)
        )
    }
}

/**
 * 3. Solid Impact Wall for Collision Experiment.
 */
private fun DrawScope.drawImpactWall(wallX: Float, h: Float, cy: Float) {
    val wallW = 16f
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF334155), Color(0xFF1E293B)),
            startX = wallX,
            endX = wallX + wallW
        ),
        topLeft = Offset(wallX, cy - 90f),
        size = Size(wallW, 180f)
    )
    drawRect(
        color = ScienceBorder,
        topLeft = Offset(wallX, cy - 90f),
        size = Size(wallW, 180f),
        style = Stroke(2f)
    )
}

/**
 * 4. Single Toroidal Ring Propagation & Visualization.
 */
private fun DrawScope.drawSingleToroidalRing(
    w: Float,
    cy: Float,
    cannonX: Float,
    progress: Float,
    baseRadiusPx: Float,
    coreSpinPhase: Float,
    showStreamlines: Boolean,
    showVorticityCores: Boolean,
    isWallCollision: Boolean
) {
    if (progress <= 0f) return

    val startX = cannonX + 10f
    val endX = if (isWallCollision) w * 0.88f - 14f else w * 0.86f
    val rx = startX + (endX - startX) * progress

    // When approaching wall, ring expands radially (Biot-Savart image vortex reflection)
    val ringRadius = if (isWallCollision && progress > 0.70f) {
        val wallFactor = (progress - 0.70f) / 0.30f
        baseRadiusPx * (1.0f + wallFactor * 1.1f)
    } else {
        baseRadiusPx * (1.0f + progress * 0.15f) // Slight gradual laminar expansion
    }

    val coreRadius = (ringRadius * 0.24f).coerceIn(6f, 22f)

    // Upper Core (Positive counter-clockwise vorticity)
    val upperCoreCenter = Offset(rx, cy - ringRadius)
    // Lower Core (Negative clockwise vorticity)
    val lowerCoreCenter = Offset(rx, cy + ringRadius)

    // Central Forward Air Jet Streaming Between Cores
    if (showStreamlines) {
        drawCentralJetStream(rx, cy, ringRadius, progress)
    }

    // Outer Toroidal Envelope (Semi-transparent smoke boundary)
    val envelopePath = Path().apply {
        moveTo(rx - coreRadius * 1.4f, cy - ringRadius)
        cubicTo(
            rx - coreRadius * 0.5f, cy - ringRadius * 1.3f,
            rx + coreRadius * 0.5f, cy - ringRadius * 1.3f,
            rx + coreRadius * 1.4f, cy - ringRadius
        )
        lineTo(rx + coreRadius * 1.4f, cy + ringRadius)
        cubicTo(
            rx + coreRadius * 0.5f, cy + ringRadius * 1.3f,
            rx - coreRadius * 0.5f, cy + ringRadius * 1.3f,
            rx - coreRadius * 1.4f, cy + ringRadius
        )
        close()
    }
    drawPath(
        path = envelopePath,
        brush = Brush.horizontalGradient(
            colors = listOf(CyanNeon.copy(alpha = 0.10f), PurpleNeon.copy(alpha = 0.18f), CyanNeon.copy(alpha = 0.10f)),
            startX = rx - coreRadius * 1.5f,
            endX = rx + coreRadius * 1.5f
        )
    )

    // Toroidal Vorticity Cores (Counter-rotating vortex pair)
    if (showVorticityCores) {
        drawVortexCore(upperCoreCenter, coreRadius, coreSpinPhase, isCounterClockwise = true)
        drawVortexCore(lowerCoreCenter, coreRadius, coreSpinPhase, isCounterClockwise = false)
    }

    // Induced Velocity Vector Field Streamlines around torus
    if (showStreamlines) {
        drawToroidalCirculationLoops(upperCoreCenter, lowerCoreCenter, coreRadius, coreSpinPhase)
    }
}

/**
 * 5. Leapfrogging Dual-Ring Simulation (Helmholtz Periodic Dance).
 */
private fun DrawScope.drawLeapfroggingRings(
    w: Float,
    cy: Float,
    cannonX: Float,
    ring1Progress: Float,
    ring2Progress: Float,
    baseRadiusPx: Float,
    coreSpinPhase: Float,
    showStreamlines: Boolean,
    showVorticityCores: Boolean
) {
    val travelDist = w * 0.72f

    // Ring 1 (Leader, then follower as they leapfrog)
    if (ring1Progress > 0f) {
        // Leapfrog oscillation: radius and velocity modulate periodically
        val cycle = ring1Progress * 4f * PI.toFloat()
        val r1 = baseRadiusPx * (1.0f + 0.35f * sin(cycle))
        val x1 = cannonX + 10f + travelDist * ring1Progress + 15f * cos(cycle)

        val c1 = (r1 * 0.22f).coerceIn(6f, 18f)
        val upper1 = Offset(x1, cy - r1)
        val lower1 = Offset(x1, cy + r1)

        drawVortexCore(upper1, c1, coreSpinPhase, isCounterClockwise = true, tint = CyanNeon)
        drawVortexCore(lower1, c1, coreSpinPhase, isCounterClockwise = false, tint = CyanNeon)
    }

    // Ring 2 (Follower, contracts and shoots through Ring 1)
    if (ring2Progress > 0f) {
        val cycle = ring2Progress * 4f * PI.toFloat() + PI.toFloat() // Out of phase
        val r2 = baseRadiusPx * (1.0f + 0.35f * sin(cycle))
        val x2 = cannonX + 10f + travelDist * ring2Progress + 15f * cos(cycle)

        val c2 = (r2 * 0.22f).coerceIn(6f, 18f)
        val upper2 = Offset(x2, cy - r2)
        val lower2 = Offset(x2, cy + r2)

        drawVortexCore(upper2, c2, coreSpinPhase * 1.2f, isCounterClockwise = true, tint = AmberVibrant)
        drawVortexCore(lower2, c2, coreSpinPhase * 1.2f, isCounterClockwise = false, tint = AmberVibrant)
    }
}

/**
 * Draws a single rotating vortex core with swirling velocity filaments.
 */
private fun DrawScope.drawVortexCore(
    center: Offset,
    coreRadius: Float,
    spinPhase: Float,
    isCounterClockwise: Boolean,
    tint: Color = PurpleNeon
) {
    // Outer core glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(tint.copy(alpha = 0.55f), Color.Transparent),
            center = center,
            radius = coreRadius * 1.8f
        ),
        radius = coreRadius * 1.8f,
        center = center
    )

    // Dense circulating core
    drawCircle(
        color = tint.copy(alpha = 0.85f),
        radius = coreRadius,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = coreRadius * 0.35f,
        center = center
    )

    // Swirling spiral streamlines inside core
    val spiralArms = 3
    val dir = if (isCounterClockwise) 1f else -1f
    for (a in 0 until spiralArms) {
        val startAng = spinPhase * dir + a * (2 * PI.toFloat() / spiralArms)
        val p1 = Offset(
            center.x + cos(startAng) * (coreRadius * 0.35f),
            center.y + sin(startAng) * (coreRadius * 0.35f)
        )
        val p2 = Offset(
            center.x + cos(startAng + dir * 0.9f) * (coreRadius * 0.9f),
            center.y + sin(startAng + dir * 0.9f) * (coreRadius * 0.9f)
        )
        drawLine(
            color = Color.White.copy(alpha = 0.75f),
            start = p1,
            end = p2,
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draws the high-speed forward air jet surging through the hole of the torus.
 */
private fun DrawScope.drawCentralJetStream(
    rx: Float,
    cy: Float,
    ringRadius: Float,
    progress: Float
) {
    // Forward jet vectors through center
    val jetLen = ringRadius * 1.2f
    for (dy in listOf(-ringRadius * 0.45f, 0f, ringRadius * 0.45f)) {
        val start = Offset(rx - jetLen * 0.5f, cy + dy)
        val end = Offset(rx + jetLen * 0.5f, cy + dy)
        drawLine(
            color = CyanNeon.copy(alpha = 0.45f),
            start = start,
            end = end,
            strokeWidth = 2.0f,
            cap = StrokeCap.Round
        )
        // Arrowhead
        val headPath = Path().apply {
            moveTo(end.x, end.y)
            lineTo(end.x - 6f, end.y - 3.5f)
            lineTo(end.x - 6f, end.y + 3.5f)
            close()
        }
        drawPath(headPath, CyanNeon.copy(alpha = 0.6f))
    }
}

/**
 * Draws circular circulation loops around the vortex cores.
 */
private fun DrawScope.drawToroidalCirculationLoops(
    upperCenter: Offset,
    lowerCenter: Offset,
    coreRadius: Float,
    spinPhase: Float
) {
    val loopR = coreRadius * 1.5f

    // Upper core counter-clockwise circulation arc
    drawArc(
        color = CyanNeon.copy(alpha = 0.55f),
        startAngle = (spinPhase * (180f / PI.toFloat())),
        sweepAngle = 260f,
        useCenter = false,
        topLeft = Offset(upperCenter.x - loopR, upperCenter.y - loopR),
        size = Size(loopR * 2, loopR * 2),
        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
    )

    // Lower core clockwise circulation arc
    drawArc(
        color = CyanNeon.copy(alpha = 0.55f),
        startAngle = (-spinPhase * (180f / PI.toFloat())),
        sweepAngle = 260f,
        useCenter = false,
        topLeft = Offset(lowerCenter.x - loopR, lowerCenter.y - loopR),
        size = Size(loopR * 2, loopR * 2),
        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
    )
}
