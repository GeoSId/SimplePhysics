package com.geosid.simplephysics.ui.experiments.week2.Day13

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.geosid.simplephysics.ui.components.ExperimentHudCard
import com.geosid.simplephysics.ui.components.PhysicsSliderControl
import com.geosid.simplephysics.ui.components.ResetIcon
import com.geosid.simplephysics.ui.components.ResponsiveExperimentContainer
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Physical scenarios illustrating the stick-slip stiction phenomenon.
 */
enum class StickSlipPreset(
    val title: String,
    val icon: String,
    val defaultMass: Float,      // kg
    val defaultStiffness: Float, // N/m
    val defaultMuS: Float,       // Static friction coefficient
    val defaultMuK: Float,       // Kinetic friction coefficient
    val defaultSpeed: Float,     // Pull speed m/s
    val accentColor: Color
) {
    HEAVY_SLED(
        title = "Heavy Sled",
        icon = "🛷",
        defaultMass = 12.0f,
        defaultStiffness = 45f,
        defaultMuS = 0.70f,
        defaultMuK = 0.25f,
        defaultSpeed = 0.25f,
        accentColor = AmberVibrant
    ),
    BRAKE_SQUEAK(
        title = "Brake Squeak",
        icon = "🚗",
        defaultMass = 3.5f,
        defaultStiffness = 180f,
        defaultMuS = 0.90f,
        defaultMuK = 0.38f,
        defaultSpeed = 0.45f,
        accentColor = CoralNeon
    ),
    VIOLIN_BOW(
        title = "Violin Bow",
        icon = "🎻",
        defaultMass = 0.25f,
        defaultStiffness = 220f,
        defaultMuS = 0.85f,
        defaultMuK = 0.20f,
        defaultSpeed = 0.50f,
        accentColor = PurpleNeon
    ),
    TECTONIC_FAULT(
        title = "Tectonic Fault",
        icon = "🌋",
        defaultMass = 35.0f,
        defaultStiffness = 28f,
        defaultMuS = 1.05f,
        defaultMuK = 0.22f,
        defaultSpeed = 0.12f,
        accentColor = CyanNeon
    )
}

enum class FrictionPhase {
    STICK,
    SLIP
}

private data class SlipSpark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float, // 1.0 down to 0
    var color: Color
)

@Composable
fun StickSlipFrictionExperiment(
    modifier: Modifier = Modifier
) {
    // 1. Preset Selection
    var selectedPreset by remember { mutableStateOf(StickSlipPreset.HEAVY_SLED) }

    // 2. Physical Constants & Parameters
    var mass by remember { mutableStateOf(StickSlipPreset.HEAVY_SLED.defaultMass) }
    var stiffness by remember { mutableStateOf(StickSlipPreset.HEAVY_SLED.defaultStiffness) }
    var muStatic by remember { mutableStateOf(StickSlipPreset.HEAVY_SLED.defaultMuS) }
    var muKinetic by remember { mutableStateOf(StickSlipPreset.HEAVY_SLED.defaultMuK) }
    var pullSpeed by remember { mutableStateOf(StickSlipPreset.HEAVY_SLED.defaultSpeed) }

    // 3. Dynamic State Machine & Simulation State
    var isRunning by remember { mutableStateOf(true) }
    var phase by remember { mutableStateOf(FrictionPhase.STICK) }

    // Physical coordinates (meters relative to world)
    var xPuller by remember { mutableStateOf(0.40f) }   // Carriage position
    var xBlock by remember { mutableStateOf(0.0f) }     // Sled position
    var vBlock by remember { mutableStateOf(0.0f) }     // Sled velocity
    var groundScrollOffset by remember { mutableStateOf(0.0f) } // Visual scrolling conveyor
    var simTime by remember { mutableStateOf(0.0f) }

    // Telemetry and statistics
    var currentSpringForce by remember { mutableStateOf(0.0f) }
    var currentFrictionForce by remember { mutableStateOf(0.0f) }
    var slipCount by remember { mutableStateOf(0) }
    var lastSlipTime by remember { mutableStateOf(0.0f) }
    var slipFrequency by remember { mutableStateOf(0.0f) }
    var stictionBreakIntensity by remember { mutableStateOf(0.0f) }

    // Interactive user touch override
    var isDraggingBlock by remember { mutableStateOf(false) }

    // Real-time oscilloscope / sawtooth force history buffer
    val forceHistory = remember { mutableStateListOf<Float>() }

    // Visual slip spark particles
    val sparks = remember { mutableStateListOf<SlipSpark>() }

    // Normal force & friction limits
    val gravity = 9.81f
    val normalForce = mass * gravity
    val maxStaticFriction = muStatic * normalForce
    val kineticFriction = muKinetic * normalForce

    // Physics Integration Engine Loop
    LaunchedEffect(isRunning, mass, stiffness, muStatic, muKinetic, pullSpeed, isDraggingBlock) {
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

            if (isRunning && !isDraggingBlock) {
                // High-frequency sub-stepping (8 substeps per frame for sharp stick-slip transitions)
                val subSteps = 8
                val subDt = dt / subSteps
                val dampingCoeff = 0.8f * sqrt(mass * stiffness) * 0.12f // Light viscous drag

                for (s in 0 until subSteps) {
                    // Advance motorized puller
                    xPuller += pullSpeed * subDt
                    simTime += subDt

                    val springDelta = xPuller - xBlock
                    val springF = stiffness * springDelta

                    if (phase == FrictionPhase.STICK) {
                        vBlock = 0.0f
                        currentSpringForce = springF
                        currentFrictionForce = springF

                        // Check stiction threshold break condition
                        if (abs(springF) >= maxStaticFriction) {
                            phase = FrictionPhase.SLIP
                            slipCount++
                            if (lastSlipTime > 0.0f) {
                                val period = simTime - lastSlipTime
                                if (period > 0.01f) {
                                    slipFrequency = 1.0f / period
                                }
                            }
                            lastSlipTime = simTime
                            stictionBreakIntensity = 1.0f
                        }
                    }

                    if (phase == FrictionPhase.SLIP) {
                        currentSpringForce = springF
                        // Kinetic friction opposes relative velocity
                        val dir = if (abs(vBlock) > 0.001f) sign(vBlock) else sign(springF)
                        val frictionF = dir * kineticFriction
                        currentFrictionForce = frictionF

                        val netF = springF - frictionF - dampingCoeff * vBlock
                        val accel = netF / mass

                        val vOld = vBlock
                        vBlock += accel * subDt
                        xBlock += vBlock * subDt

                        // Detect deceleration through zero velocity / re-sticking condition
                        if (vOld * vBlock <= 0.0f || (abs(vBlock) < 0.015f && abs(springF) <= maxStaticFriction)) {
                            // Can static friction hold it now?
                            val newSpringF = stiffness * (xPuller - xBlock)
                            if (abs(newSpringF) <= maxStaticFriction) {
                                phase = FrictionPhase.STICK
                                vBlock = 0.0f
                            }
                        }
                    }
                }

                // Decay stiction break ripple intensity
                if (stictionBreakIntensity > 0f) {
                    stictionBreakIntensity = (stictionBreakIntensity - dt * 3.5f).coerceAtLeast(0f)
                }

                // Update visual conveyor scroll offset tracking block motion
                groundScrollOffset = (groundScrollOffset + (vBlock - pullSpeed * 0.5f) * dt * 450f)

                // Record force history for live oscilloscope sparkline
                if (forceHistory.isEmpty() || abs(currentSpringForce - forceHistory.last()) > 0.2f || forceHistory.size < 60) {
                    forceHistory.add(currentSpringForce)
                    if (forceHistory.size > 90) {
                        forceHistory.removeAt(0)
                    }
                }

                // Spawn and update sparks on slip
                if (phase == FrictionPhase.SLIP && Random.nextFloat() < 0.45f) {
                    sparks.add(
                        SlipSpark(
                            x = 0f,
                            y = 0f,
                            vx = (Random.nextFloat() - 0.7f) * 60f,
                            vy = -Random.nextFloat() * 45f - 15f,
                            life = 1.0f,
                            color = if (Random.nextBoolean()) AmberVibrant else CyanNeon
                        )
                    )
                }

                // Update spark particles
                val sparkIter = sparks.iterator()
                while (sparkIter.hasNext()) {
                    val sp = sparkIter.next()
                    sp.x += sp.vx * dt
                    sp.y += sp.vy * dt
                    sp.vy += 90f * dt // gravity
                    sp.life -= dt * 2.8f
                    if (sp.life <= 0f) {
                        sparkIter.remove()
                    }
                }
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag the block or puller to perturb the spring. Tune static/kinetic friction below!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDraggingBlock = true },
                            onDragEnd = { isDraggingBlock = false },
                            onDragCancel = { isDraggingBlock = false }
                        ) { change, dragAmount ->
                            change.consume()
                            // Dragging directly alters block position
                            val deltaMeters = dragAmount.x / (size.width * 0.6f)
                            xBlock += deltaMeters
                            vBlock = 0f
                            phase = FrictionPhase.STICK
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Tap to manually trigger slip / perturbation
                            phase = FrictionPhase.SLIP
                            vBlock += 0.4f
                            stictionBreakIntensity = 1.0f
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw technical dark lab background
                drawStickSlipChamberBackground(w, h)

                // Floor baseline
                val floorY = h * 0.64f

                // Draw conveyor / sliding table floor with scrolling texture
                drawSlidingFloor(
                    w = w,
                    floorY = floorY,
                    scrollOffset = groundScrollOffset,
                    isSlipping = phase == FrictionPhase.SLIP
                )

                // Coordinate Mapping: Scale meters to screen pixels
                val pxPerMeter = w * 0.38f
                val baseCenterX = w * 0.36f
                val blockX = (baseCenterX + xBlock * pxPerMeter).coerceIn(w * 0.12f, w * 0.60f)
                val pullerX = (baseCenterX + xPuller * pxPerMeter).coerceIn(blockX + 70f, w * 0.90f)

                val blockW = 82.dp.toPx()
                val blockH = 46.dp.toPx()
                val blockTop = floorY - blockH
                val blockCenter = Offset(blockX, blockTop + blockH * 0.5f)

                // Draw Motorized Puller Carriage on the right
                drawPullerCarriage(
                    pullerX = pullerX,
                    floorY = floorY,
                    pullSpeed = pullSpeed,
                    animPhase = simTime,
                    isRunning = isRunning
                )

                // Draw Elastic Helical Spring between block and puller
                drawHelicalSpring(
                    startX = blockX + blockW * 0.5f,
                    endX = pullerX - 22f,
                    centerY = blockCenter.y,
                    springForce = currentSpringForce,
                    maxStatic = maxStaticFriction
                )

                // Draw Sparks and Micro-Debris around contact edge during slip
                drawSparks(
                    sparks = sparks,
                    origin = Offset(blockX, floorY - 2f)
                )

                // Draw Sled Block with Mass & Stiction State Halo
                drawSledBlock(
                    blockX = blockX,
                    blockTop = blockTop,
                    blockW = blockW,
                    blockH = blockH,
                    mass = mass,
                    phase = phase,
                    breakIntensity = stictionBreakIntensity,
                    isDragging = isDraggingBlock
                )

                // Draw Free-Body Diagram (FBD) Force Vectors
                drawForceVectors(
                    blockCenter = blockCenter,
                    floorY = floorY,
                    springForce = currentSpringForce,
                    frictionForce = currentFrictionForce,
                    normalForce = normalForce,
                    phase = phase,
                    maxStatic = maxStaticFriction,
                    kineticFriction = kineticFriction
                )

                // Draw Microscopic Asperity / Contact Loupe (Zoom Inset on bottom left)
                drawMicroscopicAsperityInset(
                    w = w,
                    h = h,
                    phase = phase,
                    vBlock = vBlock,
                    simTime = simTime
                )

                // Draw In-Canvas Oscilloscope / Sawtooth Waveform Graph (top-left)
                drawSawtoothOscilloscope(
                    w = w,
                    h = h,
                    forceHistory = forceHistory,
                    maxStatic = maxStaticFriction,
                    kinetic = kineticFriction,
                    phase = phase
                )
            }
        },
        hudContent = {
            val forceTensionDisplay = "${round(currentSpringForce * 10f) / 10f} N"
            val staticLimitDisplay = "${round(maxStaticFriction * 10f) / 10f} N"
            val kineticLimitDisplay = "${round(kineticFriction * 10f) / 10f} N"
            val freqDisplay = if (slipFrequency > 0.05f) "${round(slipFrequency * 10f) / 10f} Hz" else "--"

            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 13: Stick-Slip Friction & Stiction",
                items = listOf(
                    "Phase State" to if (phase == FrictionPhase.STICK) "🔒 STUCK (Static Equilibrium)" else "⚡ SLIPPING (Kinetic Overshoot)",
                    "Spring Tension (Fs)" to "$forceTensionDisplay / $staticLimitDisplay",
                    "Thresholds" to "Fs_max: $staticLimitDisplay | Fk: $kineticLimitDisplay",
                    "Oscillation Rate" to "$freqDisplay ($slipCount slips)"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Preset Selection Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StickSlipPreset.entries.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPreset = preset
                                mass = preset.defaultMass
                                stiffness = preset.defaultStiffness
                                muStatic = preset.defaultMuS
                                muKinetic = preset.defaultMuK
                                pullSpeed = preset.defaultSpeed
                                xBlock = 0.0f
                                vBlock = 0.0f
                                xPuller = 0.35f
                                phase = FrictionPhase.STICK
                            },
                            label = {
                                Text(
                                    text = "${preset.icon} ${preset.title}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = preset.accentColor.copy(alpha = 0.25f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ScienceDarkSurface,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. Friction Coefficients Sliders (Side by Side)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Static Coeff (μs)",
                        value = muStatic,
                        range = 0.30f..1.30f,
                        valueDisplay = "${round(muStatic * 100f) / 100f}",
                        accentColor = AmberVibrant,
                        onValueChange = {
                            muStatic = it
                            if (muKinetic >= muStatic) {
                                muKinetic = (muStatic - 0.10f).coerceAtLeast(0.05f)
                            }
                        }
                    )

                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Kinetic Coeff (μk)",
                        value = muKinetic,
                        range = 0.05f..0.95f,
                        valueDisplay = "${round(muKinetic * 100f) / 100f}",
                        accentColor = CyanNeon,
                        onValueChange = {
                            muKinetic = it.coerceAtMost(muStatic - 0.05f)
                        }
                    )
                }

                // 3. Pull Speed & Spring Stiffness Sliders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Pull Velocity (v)",
                        value = pullSpeed,
                        range = 0.05f..1.20f,
                        valueDisplay = "${round(pullSpeed * 100f) / 100f} m/s",
                        accentColor = EmeraldNeon,
                        onValueChange = { pullSpeed = it }
                    )

                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Spring Stiffness (k)",
                        value = stiffness,
                        range = 15f..350f,
                        valueDisplay = "${round(stiffness).toInt()} N/m",
                        accentColor = PurpleNeon,
                        onValueChange = { stiffness = it }
                    )
                }

                // 4. Action Buttons (Run/Pause, Manual Yank, Reset)
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
                            text = if (isRunning) "⏸ Pause" else "▶ Run Simulation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            // Perturb: manual jerk/yank
                            phase = FrictionPhase.SLIP
                            vBlock += 0.5f
                            stictionBreakIntensity = 1.0f
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralNeon),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralNeon.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "⚡ Trigger Slip",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            mass = selectedPreset.defaultMass
                            stiffness = selectedPreset.defaultStiffness
                            muStatic = selectedPreset.defaultMuS
                            muKinetic = selectedPreset.defaultMuK
                            pullSpeed = selectedPreset.defaultSpeed
                            xBlock = 0.0f
                            vBlock = 0.0f
                            xPuller = 0.35f
                            phase = FrictionPhase.STICK
                            isRunning = true
                            forceHistory.clear()
                            sparks.clear()
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
// Custom Compose Canvas Rendering Pipelines
// ----------------------------------------------------------------------------

private fun DrawScope.drawStickSlipChamberBackground(w: Float, h: Float) {
    // Gradient dark chamber backdrop
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(ScienceDarkBg, ScienceDarkSurface, ScienceDarkBg),
            startY = 0f,
            endY = h
        )
    )

    // Scientific grid lines
    val grid = 36.dp.toPx()
    var gx = 0f
    while (gx < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.18f),
            start = Offset(gx, 0f),
            end = Offset(gx, h),
            strokeWidth = 0.8f
        )
        gx += grid
    }
    var gy = 0f
    while (gy < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.18f),
            start = Offset(0f, gy),
            end = Offset(w, gy),
            strokeWidth = 0.8f
        )
        gy += grid
    }
}

private fun DrawScope.drawSlidingFloor(
    w: Float,
    floorY: Float,
    scrollOffset: Float,
    isSlipping: Boolean
) {
    val floorHeight = 22.dp.toPx()

    // Base floor rail block
    drawRect(
        color = ScienceDarkSurfaceVariant,
        topLeft = Offset(0f, floorY),
        size = Size(w, floorHeight)
    )

    // Glowing top sliding contact surface
    val surfaceGlow = if (isSlipping) CyanNeon.copy(alpha = 0.8f) else AmberVibrant.copy(alpha = 0.6f)
    drawLine(
        color = surfaceGlow,
        start = Offset(0f, floorY),
        end = Offset(w, floorY),
        strokeWidth = 2.5f
    )

    // Scrolling hatch grooves (indicates rough friction surface & motion)
    val spacing = 18f
    val normScroll = (scrollOffset % spacing + spacing) % spacing
    var hx = -spacing + normScroll
    while (hx < w + spacing) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.45f),
            start = Offset(hx, floorY),
            end = Offset(hx - 10f, floorY + floorHeight),
            strokeWidth = 1.2f
        )
        hx += spacing
    }

    // Depth shadow beneath rail
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent),
            startY = floorY + floorHeight,
            endY = floorY + floorHeight + 14f
        ),
        topLeft = Offset(0f, floorY + floorHeight),
        size = Size(w, 14f)
    )
}

private fun DrawScope.drawPullerCarriage(
    pullerX: Float,
    floorY: Float,
    pullSpeed: Float,
    animPhase: Float,
    isRunning: Boolean
) {
    val carriageW = 44.dp.toPx()
    val carriageH = 52.dp.toPx()
    val carriageTop = floorY - carriageH

    // Carriage Body
    drawRoundRect(
        color = ScienceDarkSurface,
        topLeft = Offset(pullerX, carriageTop),
        size = Size(carriageW, carriageH),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = EmeraldNeon.copy(alpha = 0.8f),
        topLeft = Offset(pullerX, carriageTop),
        size = Size(carriageW, carriageH),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 1.8f)
    )

    // Wheels
    val wheelRadius = 6.dp.toPx()
    val w1 = Offset(pullerX + 10f, floorY - wheelRadius)
    val w2 = Offset(pullerX + carriageW - 10f, floorY - wheelRadius)
    drawCircle(Color.Black, wheelRadius, w1)
    drawCircle(EmeraldNeon, wheelRadius, w1, style = Stroke(1.5f))
    drawCircle(Color.Black, wheelRadius, w2)
    drawCircle(EmeraldNeon, wheelRadius, w2, style = Stroke(1.5f))

    // Puller Hook bracket (where spring attaches)
    drawCircle(Color.White, 4f, Offset(pullerX, carriageTop + carriageH * 0.45f))

    // Motion velocity arrow pointing right
    val arrowStartX = pullerX + carriageW * 0.2f
    val arrowEndX = pullerX + carriageW * 0.8f
    val arrowY = carriageTop + carriageH * 0.45f
    drawLine(
        color = EmeraldNeon,
        start = Offset(arrowStartX, arrowY),
        end = Offset(arrowEndX, arrowY),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
    val arrowHead = Path().apply {
        moveTo(arrowEndX + 4f, arrowY)
        lineTo(arrowEndX - 4f, arrowY - 4f)
        lineTo(arrowEndX - 4f, arrowY + 4f)
        close()
    }
    drawPath(arrowHead, EmeraldNeon)
}

private fun DrawScope.drawHelicalSpring(
    startX: Float,
    endX: Float,
    centerY: Float,
    springForce: Float,
    maxStatic: Float
) {
    val totalLength = (endX - startX).coerceAtLeast(30f)
    val coils = 11
    val amplitude = 14.dp.toPx()

    // Stress color: Shifts from Cyan (relaxed) -> Amber -> Coral (near stiction limit)
    val stressRatio = (abs(springForce) / maxStatic.coerceAtLeast(1f)).coerceIn(0f, 1.2f)
    val springColor = when {
        stressRatio > 0.95f -> CoralNeon
        stressRatio > 0.60f -> AmberVibrant
        else -> CyanNeon
    }

    val path = Path()
    path.moveTo(startX, centerY)
    val leadIn = 12f
    path.lineTo(startX + leadIn, centerY)

    val activeSpan = totalLength - 2 * leadIn
    val step = activeSpan / (coils * 2)

    for (i in 0 until coils * 2) {
        val cx = startX + leadIn + (i + 0.5f) * step
        val cy = centerY + (if (i % 2 == 0) -amplitude else amplitude)
        val ex = startX + leadIn + (i + 1.0f) * step
        val ey = centerY
        path.quadraticTo(cx, cy, ex, ey)
    }
    path.lineTo(endX, centerY)

    // Outer glow under tension
    if (stressRatio > 0.7f) {
        drawPath(
            path = path,
            color = springColor.copy(alpha = 0.35f),
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    // Core spring coil wire
    drawPath(
        path = path,
        color = springColor,
        style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Connection rivets
    drawCircle(Color.White, 3.5f, Offset(startX, centerY))
    drawCircle(Color.White, 3.5f, Offset(endX, centerY))
}

private fun DrawScope.drawSledBlock(
    blockX: Float,
    blockTop: Float,
    blockW: Float,
    blockH: Float,
    mass: Float,
    phase: FrictionPhase,
    breakIntensity: Float,
    isDragging: Boolean
) {
    val blockLeft = blockX - blockW * 0.5f
    val corner = 8f

    // Stiction break seismic ripple ring
    if (breakIntensity > 0.05f) {
        val rippleRadius = (blockW * 0.7f) + (1f - breakIntensity) * 45f
        drawCircle(
            color = CoralNeon.copy(alpha = breakIntensity * 0.6f),
            radius = rippleRadius,
            center = Offset(blockX, blockTop + blockH * 0.5f),
            style = Stroke(width = 2.5f * breakIntensity)
        )
    }

    // Dynamic state border glow
    val borderColor = when {
        isDragging -> BlueLaser
        phase == FrictionPhase.STICK -> AmberVibrant
        else -> CyanNeon
    }

    // Block Body with Metallic Bevel
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(ScienceDarkSurfaceVariant, ScienceDarkSurface),
            startY = blockTop,
            endY = blockTop + blockH
        ),
        topLeft = Offset(blockLeft, blockTop),
        size = Size(blockW, blockH),
        cornerRadius = CornerRadius(corner, corner)
    )

    drawRoundRect(
        color = borderColor,
        topLeft = Offset(blockLeft, blockTop),
        size = Size(blockW, blockH),
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = if (phase == FrictionPhase.SLIP || isDragging) 2.5f else 1.6f)
    )

    // Grip texture lines on mass block
    val gripY = blockTop + blockH * 0.35f
    drawLine(
        color = Color.White.copy(alpha = 0.25f),
        start = Offset(blockLeft + 14f, gripY),
        end = Offset(blockLeft + blockW - 14f, gripY),
        strokeWidth = 2f
    )
    drawLine(
        color = Color.White.copy(alpha = 0.25f),
        start = Offset(blockLeft + 14f, gripY + 8f),
        end = Offset(blockLeft + blockW - 14f, gripY + 8f),
        strokeWidth = 2f
    )

    // State Indicator Badge (Center dot)
    val indicatorColor = if (phase == FrictionPhase.STICK) AmberVibrant else CyanNeon
    drawCircle(
        color = indicatorColor,
        radius = 4.5f,
        center = Offset(blockX, blockTop + blockH * 0.72f)
    )
}

private fun DrawScope.drawSparks(sparks: List<SlipSpark>, origin: Offset) {
    sparks.forEach { sp ->
        val pos = Offset(origin.x + sp.x, origin.y + sp.y)
        drawCircle(
            color = sp.color.copy(alpha = sp.life),
            radius = 2.5f * sp.life,
            center = pos
        )
    }
}

private fun DrawScope.drawForceVectors(
    blockCenter: Offset,
    floorY: Float,
    springForce: Float,
    frictionForce: Float,
    normalForce: Float,
    phase: FrictionPhase,
    maxStatic: Float,
    kineticFriction: Float
) {
    val scale = 2.2f // pixels per Newton

    // 1. Spring Force Vector Fs (Pulls right, Cyan)
    val fsLen = (springForce * scale).coerceIn(-120f, 160f)
    if (abs(fsLen) > 4f) {
        val arrowStart = Offset(blockCenter.x + 35f, blockCenter.y)
        val arrowEnd = Offset(arrowStart.x + fsLen, arrowStart.y)
        drawLine(CyanNeon, arrowStart, arrowEnd, strokeWidth = 3f, cap = StrokeCap.Round)
        drawVectorArrowHead(arrowEnd, 0f, CyanNeon)
    }

    // 2. Friction Force Vector Ff (Opposes motion, pushes left along contact surface)
    val ffLen = (frictionForce * scale).coerceIn(-160f, 160f)
    if (abs(ffLen) > 4f) {
        val arrowStart = Offset(blockCenter.x - 20f, floorY - 3f)
        val arrowEnd = Offset(arrowStart.x - ffLen, arrowStart.y)
        val fColor = if (phase == FrictionPhase.STICK) AmberVibrant else CoralNeon
        drawLine(fColor, arrowStart, arrowEnd, strokeWidth = 3f, cap = StrokeCap.Round)
        drawVectorArrowHead(arrowEnd, PI.toFloat(), fColor)
    }

    // 3. Normal Force N (Upward, Green) & Weight W (Downward, TextMuted)
    val nLen = min(normalForce * 0.35f, 50f)
    val nStart = Offset(blockCenter.x, blockCenter.y - 20f)
    val nEnd = Offset(blockCenter.x, nStart.y - nLen)
    drawLine(EmeraldNeon, nStart, nEnd, strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawVectorArrowHead(nEnd, -PI.toFloat() * 0.5f, EmeraldNeon)

    val wStart = Offset(blockCenter.x, floorY + 4f)
    val wEnd = Offset(blockCenter.x, wStart.y + nLen)
    drawLine(TextMuted, wStart, wEnd, strokeWidth = 2f, cap = StrokeCap.Round)
    drawVectorArrowHead(wEnd, PI.toFloat() * 0.5f, TextMuted)
}

private fun DrawScope.drawVectorArrowHead(tip: Offset, angleRad: Float, color: Color) {
    val headLen = 7f
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        val a1 = angleRad + 2.6f
        val a2 = angleRad - 2.6f
        lineTo(tip.x + cos(a1) * headLen, tip.y + sin(a1) * headLen)
        lineTo(tip.x + cos(a2) * headLen, tip.y + sin(a2) * headLen)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawMicroscopicAsperityInset(
    w: Float,
    h: Float,
    phase: FrictionPhase,
    vBlock: Float,
    simTime: Float
) {
    val insetW = 130.dp.toPx()
    val insetH = 65.dp.toPx()
    val insetX = 14.dp.toPx()
    val insetY = h - insetH - 14.dp.toPx()

    // Inset Background Frame
    drawRoundRect(
        color = ScienceDarkSurface.copy(alpha = 0.92f),
        topLeft = Offset(insetX, insetY),
        size = Size(insetW, insetH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = ScienceBorder,
        topLeft = Offset(insetX, insetY),
        size = Size(insetW, insetH),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(1.2f)
    )

    // Loupe boundary clipping
    clipRect(insetX + 2f, insetY + 2f, insetX + insetW - 2f, insetY + insetH - 2f) {
        val midY = insetY + insetH * 0.55f

        // Top teeth (Block asperities)
        val toothCount = 7
        val toothSpan = insetW / (toothCount - 1)
        val slipShift = if (phase == FrictionPhase.SLIP) (simTime * 80f) % toothSpan else 0f

        val topPath = Path()
        for (i in -1..toothCount + 1) {
            val tx = insetX + i * toothSpan + slipShift
            val ty = if (i % 2 == 0) midY - 6f else midY + 4f
            if (i == -1) topPath.moveTo(tx, ty) else topPath.lineTo(tx, ty)
        }
        drawPath(topPath, AmberVibrant, style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Bottom teeth (Substrate asperities - stationary)
        val botPath = Path()
        for (i in -1..toothCount + 1) {
            val bx = insetX + i * toothSpan
            val by = if (i % 2 == 0) midY + 4f else midY - 6f
            if (i == -1) botPath.moveTo(bx, by) else botPath.lineTo(bx, by)
        }
        drawPath(botPath, CyanNeon, style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Contact junction weld sparks
        if (phase == FrictionPhase.STICK) {
            drawCircle(AmberVibrant, 3f, Offset(insetX + insetW * 0.45f, midY))
            drawCircle(AmberVibrant, 3f, Offset(insetX + insetW * 0.65f, midY))
        }
    }
}

private fun DrawScope.drawSawtoothOscilloscope(
    w: Float,
    h: Float,
    forceHistory: List<Float>,
    maxStatic: Float,
    kinetic: Float,
    phase: FrictionPhase
) {
    val chartW = min(w * 0.42f, 210.dp.toPx())
    val chartH = 68.dp.toPx()
    val chartX = 14.dp.toPx()
    val chartY = 14.dp.toPx()

    // Translucent Oscilloscope Card
    drawRoundRect(
        color = ScienceDarkSurface.copy(alpha = 0.90f),
        topLeft = Offset(chartX, chartY),
        size = Size(chartW, chartH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = ScienceBorder.copy(alpha = 0.6f),
        topLeft = Offset(chartX, chartY),
        size = Size(chartW, chartH),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(1.0f)
    )

    clipRect(chartX + 2f, chartY + 2f, chartX + chartW - 2f, chartY + chartH - 2f) {
        val maxScale = (maxStatic * 1.35f).coerceAtLeast(10f)

        // Static Threshold line (Fs_max in Amber dashed)
        val staticY = chartY + chartH - (maxStatic / maxScale) * chartH
        drawLine(
            color = AmberVibrant.copy(alpha = 0.7f),
            start = Offset(chartX, staticY),
            end = Offset(chartX + chartW, staticY),
            strokeWidth = 1.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
        )

        // Kinetic Friction line (Fk in Cyan dashed)
        val kineticY = chartY + chartH - (kinetic / maxScale) * chartH
        drawLine(
            color = CyanNeon.copy(alpha = 0.7f),
            start = Offset(chartX, kineticY),
            end = Offset(chartX + chartW, kineticY),
            strokeWidth = 1.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
        )

        // Real-time Sawtooth Force Curve
        if (forceHistory.size >= 2) {
            val wavePath = Path()
            val stepX = chartW / (forceHistory.size - 1)

            forceHistory.forEachIndexed { idx, fVal ->
                val px = chartX + idx * stepX
                val py = (chartY + chartH - (fVal.coerceAtLeast(0f) / maxScale) * chartH).coerceIn(chartY, chartY + chartH)
                if (idx == 0) wavePath.moveTo(px, py) else wavePath.lineTo(px, py)
            }

            drawPath(
                path = wavePath,
                color = if (phase == FrictionPhase.SLIP) CoralNeon else CyanNeon,
                style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

