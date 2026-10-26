package com.geosid.simplephysics.ui.experiments.week6.Day42

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

/**
 * Physical presets for Kármán Vortex Street shedding.
 */
enum class VortexPreset(
    val label: String,
    val title: String,
    val velocityMS: Float,
    val diameterMm: Float,
    val densityKgM3: Float,
    val viscosityPaS: Float,
    val mediumName: String,
    val desc: String
) {
    WIND_TUNNEL(
        label = "Lab Cylinder",
        title = "Aero Wind Tunnel Cylinder",
        velocityMS = 3.2f,
        diameterMm = 25f,
        densityKgM3 = 1.204f,
        viscosityPaS = 1.82e-5f,
        mediumName = "Air (20°C)",
        desc = "Canonical fluid dynamics benchmark: Re ≈ 5,300 with clean, regular alternating vortex street"
    ),
    AEOLIAN_HARP(
        label = "Singing Wire",
        title = "Aeolian Acoustic Resonance",
        velocityMS = 12.0f,
        diameterMm = 1.8f,
        densityKgM3 = 1.204f,
        viscosityPaS = 1.82e-5f,
        mediumName = "Air (Wind)",
        desc = "High-frequency vortex shedding (f ≈ 1,300 Hz) excites singing acoustic hum in overhead telephone wires"
    ),
    BRIDGE_PIER(
        label = "Bridge Pier",
        title = "Hydraulic River Pier Wake",
        velocityMS = 1.5f,
        diameterMm = 45f, // Scaled display diameter
        densityKgM3 = 998f,
        viscosityPaS = 1.002e-3f,
        mediumName = "River Water",
        desc = "Turbulent water wake shedding enormous counter-rotating vortices that induce lateral structural loads"
    ),
    ISLAND_WAKE(
        label = "Island Clouds",
        title = "Mesoscale Planetary Vortex Street",
        velocityMS = 8.5f,
        diameterMm = 35f,
        densityKgM3 = 1.225f,
        viscosityPaS = 1.81e-5f,
        mediumName = "Atmosphere",
        desc = "Giant planetary vortex streets formed in marine stratocumulus clouds downwind of mountainous islands"
    ),
    CREEPING_LAMINAR(
        label = "Attached Eddies",
        title = "Sub-Critical Symmetric Regime",
        velocityMS = 0.08f,
        diameterMm = 20f,
        densityKgM3 = 1260f,
        viscosityPaS = 1.412f,
        mediumName = "Glycerin",
        desc = "Re ≈ 1.4 < 47: Below the critical Hopf bifurcation threshold. Stable symmetric twin eddies remain attached without shedding"
    )
}

/**
 * Tracer particle for flow streakline visualization.
 */
private class WakeParticle(
    var x: Float,
    var y: Float,
    var age: Float,
    val maxAge: Float,
    val baselineY: Float,
    val seed: Float
)

@Composable
fun KarmanVortexExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(VortexPreset.WIND_TUNNEL) }

    // Physical Parameters
    var flowVelocityMS by remember { mutableStateOf(currentPreset.velocityMS) }
    var cylinderDiameterMm by remember { mutableStateOf(currentPreset.diameterMm) }
    var fluidDensityKgM3 by remember { mutableStateOf(currentPreset.densityKgM3) }
    var fluidViscosityPaS by remember { mutableStateOf(currentPreset.viscosityPaS) }

    // Display Toggles
    var showStreamlines by remember { mutableStateOf(true) }
    var showVorticityCores by remember { mutableStateOf(true) }
    var showLiftVectors by remember { mutableStateOf(true) }
    var showSpacingRatio by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(true) }

    fun applyPreset(preset: VortexPreset) {
        currentPreset = preset
        flowVelocityMS = preset.velocityMS
        cylinderDiameterMm = preset.diameterMm
        fluidDensityKgM3 = preset.densityKgM3
        fluidViscosityPaS = preset.viscosityPaS
    }

    // Physical Calculations
    val diameterM = cylinderDiameterMm * 1e-3f
    val reynolds = (fluidDensityKgM3 * flowVelocityMS * diameterM) / max(fluidViscosityPaS, 1e-7f)

    // Critical Reynolds thresholds
    val isAttachedEddy = reynolds < 47f
    val isTurbulent = reynolds > 300f

    // Strouhal Number empirical calculation (Fey et al. 1998 formulation)
    val strouhal = if (isAttachedEddy) {
        0.0f
    } else {
        (0.198f * (1f - (19.7f / max(reynolds, 20f)))).coerceIn(0.16f, 0.22f)
    }

    // Shedding Frequency f = St * (v / D) in Hz
    val sheddingFrequencyHz = if (isAttachedEddy) {
        0.0f
    } else {
        strouhal * (flowVelocityMS / max(diameterM, 1e-5f))
    }

    // Dynamic Lift & Drag coefficients
    val clMax = if (isAttachedEddy) 0.05f else 0.45f
    val cdMean = if (reynolds < 100f) 1.5f else 1.15f
    val dynamicPressurePa = 0.5f * fluidDensityKgM3 * flowVelocityMS * flowVelocityMS
    val frontalAreaM2 = diameterM * 1.0f // 1m cylinder span
    val maxLiftForceN = clMax * dynamicPressurePa * frontalAreaM2
    val meanDragForceN = cdMean * dynamicPressurePa * frontalAreaM2

    // von Kármán spacing ratio: h / l ≈ 0.281
    val karmanSpacingRatio = 0.281f

    // Animation Phase
    val infiniteTransition = rememberInfiniteTransition()
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (!isRunning) 9999999 else if (isAttachedEddy) 4000 else ((1000f / max(sheddingFrequencyHz, 0.5f)).toInt()).coerceIn(400, 3000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    // Lift Force Oscillation
    val currentLiftForceN = maxLiftForceN * sin(animPhase)

    // Interactive Drag Offset for Cylinder Position
    var cylinderRelPos by remember { mutableStateOf(Offset(0.22f, 0.44f)) }

    // Particle field for streaklines
    val particles = remember {
        List(140) { i ->
            val by = 0.15f + (i % 28) * (0.58f / 28f)
            WakeParticle(
                x = Random.nextFloat(),
                y = by + (Random.nextFloat() - 0.5f) * 0.03f,
                age = Random.nextFloat(),
                maxAge = 1.0f,
                baselineY = by,
                seed = Random.nextFloat() * 100f
            )
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag cylinder to reposition. Observe alternating vortex shedding, Strouhal frequency, and oscillating lift force!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            cylinderRelPos = Offset(
                                (cylinderRelPos.x + dragAmount.x / size.width).coerceIn(0.12f, 0.45f),
                                (cylinderRelPos.y + dragAmount.y / size.height).coerceIn(0.25f, 0.65f)
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            cylinderRelPos = Offset(
                                (offset.x / size.width).coerceIn(0.12f, 0.45f),
                                (offset.y / size.height).coerceIn(0.25f, 0.65f)
                            )
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Cylinder geometry on Canvas
                val cylCenter = Offset(w * cylinderRelPos.x, h * cylinderRelPos.y)
                val cylRadius = (cylinderDiameterMm * 0.9f).dp.toPx().coerceIn(16.dp.toPx(), 44.dp.toPx())
                val cylDiameterPx = cylRadius * 2f

                // 1. Scientific Background Grid
                val gridSpacing = 36.dp.toPx()
                var gx = 0f
                while (gx < w) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.20f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, h),
                        strokeWidth = 0.7f
                    )
                    gx += gridSpacing
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.20f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 0.7f
                    )
                    gy += gridSpacing
                }

                // 2. Kármán Double Row Envelope & Spacing Ratio Guides
                if (showSpacingRatio && !isAttachedEddy) {
                    val vortexRowHalfHeight = cylRadius * 1.6f
                    val topRowY = cylCenter.y - vortexRowHalfHeight
                    val botRowY = cylCenter.y + vortexRowHalfHeight

                    // Upper & Lower Street Centerlines
                    drawLine(
                        color = CyanNeon.copy(alpha = 0.25f),
                        start = Offset(cylCenter.x + cylRadius, topRowY),
                        end = Offset(w * 0.96f, topRowY),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                    drawLine(
                        color = AmberVibrant.copy(alpha = 0.25f),
                        start = Offset(cylCenter.x + cylRadius, botRowY),
                        end = Offset(w * 0.96f, botRowY),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    // Wake Centerline
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.35f),
                        start = Offset(cylCenter.x, cylCenter.y),
                        end = Offset(w * 0.98f, cylCenter.y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Spacing Indicator Callout: h / l ≈ 0.281
                    val indX = cylCenter.x + cylRadius * 4.2f
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(indX, topRowY),
                        end = Offset(indX, botRowY),
                        strokeWidth = 1.2f
                    )
                    drawCircle(CyanNeon, 2.5f, Offset(indX, topRowY))
                    drawCircle(AmberVibrant, 2.5f, Offset(indX, botRowY))
                }

                // 3. Flow Streamlines past cylinder
                if (showStreamlines) {
                    val streamCount = 14
                    for (s in 0 until streamCount) {
                        val frac = s / (streamCount - 1f)
                        val baselineY = cylCenter.y + (frac - 0.5f) * (cylDiameterPx * 4.2f)
                        val streamPath = Path()
                        val steps = 50

                        for (step in 0..steps) {
                            val x = (step / steps.toFloat()) * w
                            val dx = x - cylCenter.x
                            val dy = baselineY - cylCenter.y
                            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)

                            // Upstream potential flow deflection
                            val deflectionY = if (dist < cylRadius * 2.8f) {
                                val factor = (cylRadius * cylRadius) / (dist * dist)
                                dy * (1f + factor * 0.65f)
                            } else {
                                dy
                            }

                            // Downstream Kármán vortex wave modulation
                            val wakeModulation = if (x > cylCenter.x + cylRadius && !isAttachedEddy) {
                                val downstreamDist = x - (cylCenter.x + cylRadius)
                                val decay = exp(-downstreamDist / (w * 0.6f))
                                val wavelength = cylDiameterPx * 4.4f
                                val waveK = 2f * PI.toFloat() / wavelength
                                val waveAmp = (cylRadius * 0.75f) * decay * sin(downstreamDist * waveK - animPhase)
                                waveAmp * exp(-abs(dy) / (cylRadius * 2.2f))
                            } else 0f

                            val y = cylCenter.y + deflectionY + wakeModulation
                            if (step == 0) streamPath.moveTo(x, y) else streamPath.lineTo(x, y)
                        }

                        drawPath(
                            path = streamPath,
                            color = CyanNeon.copy(alpha = 0.22f),
                            style = Stroke(
                                width = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), -animPhase * 20f)
                            )
                        )
                    }
                }

                // 4. Moving Tracer Streakline Particles
                for (p in particles) {
                    // Update particle x position based on flow
                    val px = ((p.x * w + (animPhase / (2f * PI.toFloat())) * (flowVelocityMS * 18f).dp.toPx() + p.seed * 2f) % w)
                    val pBaseline = cylCenter.y + (p.baselineY - 0.44f) * (cylDiameterPx * 4f)
                    val dx = px - cylCenter.x
                    val dy = pBaseline - cylCenter.y
                    val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)

                    // Deflect around cylinder if upstream
                    val curY = if (dx < 0 && dist < cylRadius * 1.4f) {
                        cylCenter.y + sign(dy) * (cylRadius * 1.15f + abs(dy) * 0.4f)
                    } else if (px > cylCenter.x + cylRadius && !isAttachedEddy) {
                        // Swirl into vortex street behind cylinder
                        val downstreamDist = px - (cylCenter.x + cylRadius)
                        val wavelength = cylDiameterPx * 4.4f
                        val waveK = 2f * PI.toFloat() / wavelength
                        val waveAmp = (cylRadius * 0.85f) * sin(downstreamDist * waveK - animPhase)
                        pBaseline + waveAmp * exp(-abs(dy) / (cylRadius * 1.8f))
                    } else {
                        pBaseline
                    }

                    // Particle color depends on upper vs lower wake (vorticity sign)
                    val pColor = if (curY < cylCenter.y) CyanNeon else AmberVibrant
                    val alpha = if (px < cylCenter.x - cylRadius) 0.40f else 0.75f
                    drawCircle(
                        color = pColor.copy(alpha = alpha),
                        radius = 2.2.dp.toPx(),
                        center = Offset(px, curY)
                    )
                }

                // 5. Alternating Kármán Vortex Cores Downstream
                if (showVorticityCores && !isAttachedEddy) {
                    val wavelength = cylDiameterPx * 4.4f
                    val vortexHalfHeight = cylRadius * 1.45f
                    val numVortices = 5

                    for (k in 0 until numVortices) {
                        // Alternate top and bottom with half-period phase offset (π)
                        val isTop = (k % 2 == 0)
                        val basePhase = animPhase + k * PI.toFloat()
                        val downstreamX = cylCenter.x + cylRadius * 1.8f + (k * (wavelength * 0.5f)) + (animPhase / (2f * PI.toFloat())) * (wavelength * 0.5f)
                        if (downstreamX > w * 0.98f) continue

                        val coreY = if (isTop) cylCenter.y - vortexHalfHeight else cylCenter.y + vortexHalfHeight
                        val coreCenter = Offset(downstreamX, coreY)
                        val coreColor = if (isTop) CyanNeon else AmberVibrant
                        val spinDir = if (isTop) 1f else -1f // CCW (+z) on top, CW (-z) on bottom
                        val decayFrac = (1f - (downstreamX - cylCenter.x) / (w * 0.85f)).coerceIn(0.2f, 1f)

                        // Core Glow Halo
                        drawCircle(
                            color = coreColor.copy(alpha = 0.14f * decayFrac),
                            radius = (cylRadius * 1.25f) * decayFrac,
                            center = coreCenter
                        )
                        drawCircle(
                            color = coreColor.copy(alpha = 0.35f * decayFrac),
                            radius = (cylRadius * 0.65f) * decayFrac,
                            center = coreCenter
                        )

                        // Swirling Spiral Filament Arms
                        val spiralArms = 3
                        for (arm in 0 until spiralArms) {
                            val armOffsetAngle = (arm * 2f * PI.toFloat() / spiralArms) + (basePhase * spinDir * 2f)
                            val spiralPath = Path()
                            val armSteps = 16
                            for (st in 0..armSteps) {
                                val r = (st / armSteps.toFloat()) * (cylRadius * 1.0f) * decayFrac
                                val theta = armOffsetAngle + spinDir * (st / armSteps.toFloat()) * 3.2f
                                val sx = coreCenter.x + r * cos(theta)
                                val sy = coreCenter.y + r * sin(theta)
                                if (st == 0) spiralPath.moveTo(sx, sy) else spiralPath.lineTo(sx, sy)
                            }
                            drawPath(
                                path = spiralPath,
                                color = coreColor.copy(alpha = 0.70f * decayFrac),
                                style = Stroke(width = 1.6f, cap = StrokeCap.Round)
                            )
                        }

                        // Core Center Dot & Vorticity Sign Symbol (+ or -)
                        drawCircle(
                            color = Color.White,
                            radius = 2.4.dp.toPx() * decayFrac,
                            center = coreCenter
                        )
                    }
                } else if (isAttachedEddy) {
                    // Attached symmetric eddy recirculations behind cylinder
                    val eddyLen = cylRadius * 1.6f
                    val topEddy = Offset(cylCenter.x + cylRadius * 0.9f, cylCenter.y - cylRadius * 0.45f)
                    val botEddy = Offset(cylCenter.x + cylRadius * 0.9f, cylCenter.y + cylRadius * 0.45f)

                    drawOval(
                        color = CyanNeon.copy(alpha = 0.25f),
                        topLeft = Offset(topEddy.x - eddyLen * 0.4f, topEddy.y - cylRadius * 0.35f),
                        size = Size(eddyLen, cylRadius * 0.7f),
                        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), animPhase * 6f))
                    )
                    drawOval(
                        color = AmberVibrant.copy(alpha = 0.25f),
                        topLeft = Offset(botEddy.x - eddyLen * 0.4f, botEddy.y - cylRadius * 0.35f),
                        size = Size(eddyLen, cylRadius * 0.7f),
                        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), -animPhase * 6f))
                    )
                }

                // 6. Cylinder Body (Bluff Obstacle)
                // Outer Shadow & Boundary Layer
                drawCircle(
                    color = Color.Black.copy(alpha = 0.65f),
                    radius = cylRadius + 5.dp.toPx(),
                    center = cylCenter
                )
                // Metallic Surface Gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF475569), Color(0xFF1E293B), Color(0xFF0F172A)),
                        center = cylCenter.copy(x = cylCenter.x - cylRadius * 0.35f, y = cylCenter.y - cylRadius * 0.35f),
                        radius = cylRadius
                    ),
                    radius = cylRadius,
                    center = cylCenter
                )
                // Border Rim
                drawCircle(
                    color = ScienceBorder,
                    radius = cylRadius,
                    center = cylCenter,
                    style = Stroke(width = 2.4f)
                )

                // Separation Points Markers (±82° from stagnation line)
                val sepAngleRad = 82f * (PI.toFloat() / 180f)
                val topSep = Offset(cylCenter.x + cylRadius * cos(PI.toFloat() - sepAngleRad), cylCenter.y - cylRadius * sin(sepAngleRad))
                val botSep = Offset(cylCenter.x + cylRadius * cos(PI.toFloat() - sepAngleRad), cylCenter.y + cylRadius * sin(sepAngleRad))
                drawCircle(CoralNeon, 3.5.dp.toPx(), topSep)
                drawCircle(CoralNeon, 3.5.dp.toPx(), botSep)

                // 7. Oscillating Lift Vector (F_L) & Drag Vector (F_D)
                if (showLiftVectors) {
                    val liftArrowScale = 0.35f
                    val liftArrowLenPx = (currentLiftForceN * liftArrowScale * 80f).coerceIn(-cylRadius * 2.5f, cylRadius * 2.5f)
                    val liftEnd = Offset(cylCenter.x, cylCenter.y - liftArrowLenPx)

                    // Lift Vector Arrow
                    val liftColor = if (liftArrowLenPx >= 0) CyanNeon else CoralNeon
                    drawLine(
                        color = liftColor,
                        start = cylCenter,
                        end = liftEnd,
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Arrowhead
                    val headSign = if (liftArrowLenPx >= 0) 1f else -1f
                    val arrowHead = Path().apply {
                        moveTo(liftEnd.x, liftEnd.y - headSign * 8f)
                        lineTo(liftEnd.x - 6f, liftEnd.y + headSign * 2f)
                        lineTo(liftEnd.x + 6f, liftEnd.y + headSign * 2f)
                        close()
                    }
                    drawPath(arrowHead, liftColor)

                    // Drag Vector Arrow (Horizontal Downstream)
                    val dragArrowLenPx = (meanDragForceN * 0.25f * 80f).coerceIn(12f, cylRadius * 2.2f)
                    val dragEnd = Offset(cylCenter.x + cylRadius + dragArrowLenPx, cylCenter.y)
                    drawLine(
                        color = AmberVibrant,
                        start = Offset(cylCenter.x + cylRadius, cylCenter.y),
                        end = dragEnd,
                        strokeWidth = 2.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    val dragHead = Path().apply {
                        moveTo(dragEnd.x + 8f, dragEnd.y)
                        lineTo(dragEnd.x - 2f, dragEnd.y - 5f)
                        lineTo(dragEnd.x - 2f, dragEnd.y + 5f)
                        close()
                    }
                    drawPath(dragHead, AmberVibrant)
                }

                // Upstream Inflow Indicator Arrow
                val inflowY = cylCenter.y
                val inflowStartX = w * 0.03f
                val inflowEndX = w * 0.11f
                drawLine(
                    color = CyanNeon.copy(alpha = 0.8f),
                    start = Offset(inflowStartX, inflowY),
                    end = Offset(inflowEndX, inflowY),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                val inHead = Path().apply {
                    moveTo(inflowEndX + 6f, inflowY)
                    lineTo(inflowEndX - 2f, inflowY - 4f)
                    lineTo(inflowEndX - 2f, inflowY + 4f)
                    close()
                }
                drawPath(inHead, CyanNeon.copy(alpha = 0.8f))
            }
        },
        hudContent = {
            val freqDisplay = if (isAttachedEddy) "0.0 Hz (Attached)" else "${round(sheddingFrequencyHz * 10f) / 10f} Hz"
            val regimeText = if (isAttachedEddy) "Subcritical (Re < 47)" else if (isTurbulent) "Turbulent Wake" else "Laminar Street"
            val liftDisplay = "${round(currentLiftForceN * 1000f) / 1000f} N"

            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 42: Kármán Vortex Street",
                items = listOf(
                    "Shedding f" to freqDisplay,
                    "Reynolds Re" to "${reynolds.toInt()} ($regimeText)",
                    "Strouhal St" to "${round(strouhal * 1000f) / 1000f}",
                    "Oscillating F_L" to liftDisplay,
                    "Spacing Ratio" to "h / l ≈ $karmanSpacingRatio"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preset Selector Chips
                Text(
                    text = "SELECT SCENARIO PRESET",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VortexPreset.values().forEach { preset ->
                        val isSelected = currentPreset == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyanNeon.copy(alpha = 0.25f) else ScienceDarkSurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CyanNeon else ScienceBorder.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { applyPreset(preset) }
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.label,
                                color = if (isSelected) CyanNeon else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Paired Sliders: Velocity and Diameter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Flow Velocity v",
                        value = flowVelocityMS,
                        range = 0.05f..15.0f,
                        valueDisplay = "${round(flowVelocityMS * 10f) / 10f} m/s",
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { flowVelocityMS = it }
                    )

                    PhysicsSliderControl(
                        title = "Cylinder Diameter D",
                        value = cylinderDiameterMm,
                        range = 1.0f..60.0f,
                        valueDisplay = "${round(cylinderDiameterMm * 10f) / 10f} mm",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = { cylinderDiameterMm = it }
                    )
                }

                // Display Mode Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = showStreamlines,
                        onClick = { showStreamlines = !showStreamlines },
                        label = { Text("Streamlines", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CyanNeon
                        )
                    )
                    FilterChip(
                        selected = showVorticityCores,
                        onClick = { showVorticityCores = !showVorticityCores },
                        label = { Text("Vortex Cores", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberVibrant.copy(alpha = 0.25f),
                            selectedLabelColor = AmberVibrant
                        )
                    )
                    FilterChip(
                        selected = showLiftVectors,
                        onClick = { showLiftVectors = !showLiftVectors },
                        label = { Text("Lift F_L", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CoralNeon
                        )
                    )
                    FilterChip(
                        selected = showSpacingRatio,
                        onClick = { showSpacingRatio = !showSpacingRatio },
                        label = { Text("h/l Ratio", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurpleNeon.copy(alpha = 0.25f),
                            selectedLabelColor = PurpleNeon
                        )
                    )
                }

                // Action Controls: Play/Pause and Reset
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
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause Flow" else "▶ Run Simulation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            applyPreset(VortexPreset.WIND_TUNNEL)
                            cylinderRelPos = Offset(0.22f, 0.44f)
                            isRunning = true
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
