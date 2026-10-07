package com.geosid.simplephysics.ui.experiments.week5.Day31

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.drawscope.Fill
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
 * Optical presets for Young's Double-Slit experiment.
 */
enum class DoubleSlitPreset(
    val label: String,
    val wavelengthNm: Float,
    val slitSeparationUm: Float,
    val slitWidthUm: Float,
    val screenDistanceM: Float,
    val isQuantumMode: Boolean,
    val desc: String
) {
    HELIUM_NEON(
        label = "He-Ne Red (633nm)",
        wavelengthNm = 632.8f,
        slitSeparationUm = 50f,
        slitWidthUm = 10f,
        screenDistanceM = 1.2f,
        isQuantumMode = false,
        desc = "Standard red Helium-Neon laboratory laser with clean high-contrast fringes"
    ),
    EMERALD_LASER(
        label = "DPSS Green (532nm)",
        wavelengthNm = 532.0f,
        slitSeparationUm = 40f,
        slitWidthUm = 8f,
        screenDistanceM = 1.0f,
        isQuantumMode = false,
        desc = "High-visibility frequency-doubled Nd:YAG laser with tighter fringe spacing"
    ),
    VIOLET_LASER(
        label = "Violet Diode (405nm)",
        wavelengthNm = 405.0f,
        slitSeparationUm = 30f,
        slitWidthUm = 6f,
        screenDistanceM = 0.8f,
        isQuantumMode = false,
        desc = "Short ultraviolet-edge wavelength producing dense, narrow interference fringes"
    ),
    QUANTUM_PHOTONS(
        label = "Single Photon Mode",
        wavelengthNm = 532.0f,
        slitSeparationUm = 45f,
        slitWidthUm = 9f,
        screenDistanceM = 1.0f,
        isQuantumMode = true,
        desc = "Discrete quantum particles hitting the detector plate one by one"
    )
}

/**
 * Maps a light wavelength in nanometers (380 - 780 nm) to a spectral RGB Color.
 */
fun wavelengthToColor(wavelengthNm: Float): Color {
    val gamma = 0.80f
    val r: Float
    val g: Float
    val b: Float
    when {
        wavelengthNm in 380f..440f -> {
            r = -(wavelengthNm - 440f) / (440f - 380f)
            g = 0.0f
            b = 1.0f
        }
        wavelengthNm in 440f..490f -> {
            r = 0.0f
            g = (wavelengthNm - 440f) / (490f - 440f)
            b = 1.0f
        }
        wavelengthNm in 490f..510f -> {
            r = 0.0f
            g = 1.0f
            b = -(wavelengthNm - 510f) / (510f - 490f)
        }
        wavelengthNm in 510f..580f -> {
            r = (wavelengthNm - 510f) / (580f - 510f)
            g = 1.0f
            b = 0.0f
        }
        wavelengthNm in 580f..645f -> {
            r = 1.0f
            g = -(wavelengthNm - 645f) / (645f - 580f)
            b = 0.0f
        }
        wavelengthNm in 645f..780f -> {
            r = 1.0f
            g = 0.0f
            b = 0.0f
        }
        else -> {
            r = 0f; g = 0f; b = 0f
        }
    }
    val factor = when {
        wavelengthNm in 380f..420f -> 0.35f + 0.65f * (wavelengthNm - 380f) / (420f - 380f)
        wavelengthNm in 420f..700f -> 1.0f
        wavelengthNm in 700f..780f -> 0.35f + 0.65f * (780f - wavelengthNm) / (780f - 700f)
        else -> 0.0f
    }
    return Color(
        red = (r * factor).pow(gamma).coerceIn(0f, 1f),
        green = (g * factor).pow(gamma).coerceIn(0f, 1f),
        blue = (b * factor).pow(gamma).coerceIn(0f, 1f)
    )
}

/**
 * Data class representing an accumulated photon hit on the screen in Quantum mode.
 */
data class ScreenPhotonHit(
    val normalizedY: Float,
    val birthTime: Float
)

@Composable
fun YoungsDoubleSlitExperiment(
    modifier: Modifier = Modifier
) {
    // Optical & Physical Parameters
    var wavelengthNm by remember { mutableStateOf(532.0f) } // 380 to 750 nm
    var slitSeparationUm by remember { mutableStateOf(45.0f) } // Slit spacing d (15 to 100 μm)
    var slitWidthUm by remember { mutableStateOf(9.0f) } // Aperture width a (2 to 25 μm)
    var screenDistanceM by remember { mutableStateOf(1.0f) } // Screen distance L (0.4 to 2.5 m)
    var slit1Open by remember { mutableStateOf(true) } // Top slit toggle
    var slit2Open by remember { mutableStateOf(true) } // Bottom slit toggle
    var isQuantumMode by remember { mutableStateOf(false) } // Wave vs Photon counting mode
    var showIntensityCurve by remember { mutableStateOf(true) } // Theoretical I(θ) plot overlay
    var showRays by remember { mutableStateOf(true) } // Geometric optical path rays
    var selectedPreset by remember { mutableStateOf<DoubleSlitPreset?>(DoubleSlitPreset.EMERALD_LASER) }
    var isRunning by remember { mutableStateOf(true) }
    var simTime by remember { mutableStateOf(0f) }

    // Interactive Virtual Detector Probe Position (relative 0.0 .. 1.0)
    var probePosRel by remember { mutableStateOf(Offset(0.92f, 0.50f)) }

    // Quantum photon hits list (accumulated over time)
    val photonHits = remember { mutableStateListOf<ScreenPhotonHit>() }
    var photonEmitTimer by remember { mutableStateOf(0f) }

    // Animation frame loop
    LaunchedEffect(isRunning) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) {
                    lastNanos = nanos
                    return@withFrameNanos
                }
                val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastNanos = nanos

                if (isRunning) {
                    simTime += dt

                    // In Quantum mode: sample photon hits according to probability I(y)
                    if (isQuantumMode && (slit1Open || slit2Open)) {
                        photonEmitTimer += dt
                        if (photonEmitTimer >= 0.025f) { // emit ~40 photons/sec
                            photonEmitTimer = 0f
                            // Rejection sampling for intensity distribution
                            for (attempt in 0..15) {
                                val testY = Random.nextFloat() * 2f - 1f // -1.0 to +1.0 relative
                                val maxTheta = 0.025f // radians
                                val theta = testY * maxTheta

                                val k = (2f * PI.toFloat()) / (wavelengthNm * 1e-9f)
                                val d = slitSeparationUm * 1e-6f
                                val a = slitWidthUm * 1e-6f

                                val beta = 0.5f * k * d * sin(theta)
                                val alpha = 0.5f * k * a * sin(theta)

                                val sincAlpha = if (abs(alpha) < 1e-4f) 1f else (sin(alpha) / alpha)
                                val diffractionEnvelope = sincAlpha.pow(2)

                                val prob = when {
                                    slit1Open && slit2Open -> cos(beta).pow(2) * diffractionEnvelope
                                    slit1Open || slit2Open -> 0.5f * diffractionEnvelope
                                    else -> 0f
                                }

                                if (Random.nextFloat() <= prob) {
                                    photonHits.add(ScreenPhotonHit(testY, simTime))
                                    if (photonHits.size > 750) {
                                        photonHits.removeAt(0)
                                    }
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Physical Calculations for HUD & Code Step Visualization
    val laserColor = wavelengthToColor(wavelengthNm)
    val wavelengthM = wavelengthNm * 1e-9f
    val slitSeparationM = slitSeparationUm * 1e-6f
    val slitWidthM = slitWidthUm * 1e-6f

    // 1. Geometric path difference and constructive condition
    // For probe angle θ
    val probeYRel = (probePosRel.y - 0.5f) * 2f // -1.0 to +1.0
    val maxScreenHalfWidthM = 0.035f * screenDistanceM // ~3.5cm screen height
    val probeYLinearM = probeYRel * maxScreenHalfWidthM
    val thetaProbeRad = atan2(probeYLinearM, screenDistanceM)

    val pathDiffM = slitSeparationM * sin(thetaProbeRad)
    val pathDiffWavelengths = if (wavelengthM > 0f) pathDiffM / wavelengthM else 0f
    val orderM = round(pathDiffWavelengths).toInt()
    val isNearConstructive = abs(pathDiffWavelengths - orderM.toFloat()) < 0.12f

    // 2. Fringe spacing on screen Δy = λ·L / d
    val fringeSpacingM = (wavelengthM * screenDistanceM) / slitSeparationM
    val fringeSpacingMm = fringeSpacingM * 1000f

    // 3. Total Intensity at probe
    val betaProbe = (PI.toFloat() * slitSeparationM * sin(thetaProbeRad)) / wavelengthM
    val alphaProbe = (PI.toFloat() * slitWidthM * sin(thetaProbeRad)) / wavelengthM
    val sincAlphaProbe = if (abs(alphaProbe) < 1e-4f) 1f else (sin(alphaProbe) / alphaProbe)
    val diffractionFactor = sincAlphaProbe.pow(2)

    val totalIntensity = when {
        slit1Open && slit2Open -> cos(betaProbe).pow(2) * diffractionFactor
        slit1Open || slit2Open -> 0.25f * diffractionFactor
        else -> 0f
    }.coerceIn(0f, 1f)

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag across the wavefield or screen to move the virtual detector probe. Observe wave interference, diffraction envelope, and quantum photon build-up!",
        canvasContent = {
            // Interactive 2D Wavefield & Screen Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B12))
                    .border(BorderStroke(1.dp, ScienceBorder.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val relX = (change.position.x / size.width).coerceIn(0.05f, 0.98f)
                            val relY = (change.position.y / size.height).coerceIn(0.05f, 0.95f)
                            probePosRel = Offset(relX, relY)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val relX = (offset.x / size.width).coerceIn(0.05f, 0.98f)
                            val relY = (offset.y / size.height).coerceIn(0.05f, 0.95f)
                            probePosRel = Offset(relX, relY)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val barrierX = w * 0.22f
                    val screenX = w * 0.82f
                    val centerY = h * 0.5f

                    // Slit positions in pixels
                    val slitGapPx = (slitSeparationUm / 60f) * (h * 0.24f)
                    val s1Y = centerY - slitGapPx * 0.5f
                    val s2Y = centerY + slitGapPx * 0.5f

                    // 1. Grid Background
                    val gridSpacing = 28f
                    for (gx in 0..(w / gridSpacing).toInt()) {
                        drawLine(
                            color = Color(0xFF101726).copy(alpha = 0.45f),
                            start = Offset(gx * gridSpacing, 0f),
                            end = Offset(gx * gridSpacing, h),
                            strokeWidth = 0.8f
                        )
                    }
                    for (gy in 0..(h / gridSpacing).toInt()) {
                        drawLine(
                            color = Color(0xFF101726).copy(alpha = 0.45f),
                            start = Offset(0f, gy * gridSpacing),
                            end = Offset(w, gy * gridSpacing),
                            strokeWidth = 0.8f
                        )
                    }

                    // 2. Incident Collimated Laser Beam (Planar wavefronts from left)
                    val waveSpeedPx = 65f
                    val wavelengthPx = 22f
                    val beamPhase = (simTime * waveSpeedPx) % wavelengthPx

                    for (x in 0..(barrierX / wavelengthPx).toInt() + 1) {
                        val waveX = x * wavelengthPx + beamPhase
                        if (waveX < barrierX) {
                            drawLine(
                                color = laserColor.copy(alpha = 0.40f),
                                start = Offset(waveX, h * 0.18f),
                                end = Offset(waveX, h * 0.82f),
                                strokeWidth = 2.0f
                            )
                        }
                    }

                    // Laser Source Gun Icon / Aperture on the Left
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(laserColor.copy(alpha = 0.7f), laserColor.copy(alpha = 0.15f)),
                            startX = 0f,
                            endX = barrierX
                        ),
                        topLeft = Offset(0f, h * 0.20f),
                        size = Size(barrierX, h * 0.60f)
                    )

                    // 3. Diffracted Waves between Slits and Screen
                    if (!isQuantumMode) {
                        val maxWaveR = screenX - barrierX
                        val numRings = 14
                        val slitWavePhase = (simTime * waveSpeedPx) % wavelengthPx

                        // Wavefronts from Slit 1 (if open)
                        if (slit1Open) {
                            for (rIndex in 1..numRings) {
                                val r = rIndex * wavelengthPx + slitWavePhase
                                if (r in 4f..maxWaveR) {
                                    val alpha = (1f - (r / maxWaveR)).coerceIn(0f, 1f) * 0.45f
                                    drawArc(
                                        color = laserColor.copy(alpha = alpha),
                                        startAngle = -85f,
                                        sweepAngle = 170f,
                                        useCenter = false,
                                        topLeft = Offset(barrierX - r, s1Y - r),
                                        size = Size(r * 2f, r * 2f),
                                        style = Stroke(width = 1.8f)
                                    )
                                }
                            }
                        }

                        // Wavefronts from Slit 2 (if open)
                        if (slit2Open) {
                            for (rIndex in 1..numRings) {
                                val r = rIndex * wavelengthPx + slitWavePhase
                                if (r in 4f..maxWaveR) {
                                    val alpha = (1f - (r / maxWaveR)).coerceIn(0f, 1f) * 0.45f
                                    drawArc(
                                        color = laserColor.copy(alpha = alpha),
                                        startAngle = -85f,
                                        sweepAngle = 170f,
                                        useCenter = false,
                                        topLeft = Offset(barrierX - r, s2Y - r),
                                        size = Size(r * 2f, r * 2f),
                                        style = Stroke(width = 1.8f)
                                    )
                                }
                            }
                        }

                        // Interference Nodal / Antinodal Constructive Ray Beams
                        if (slit1Open && slit2Open && showRays) {
                            val orders = listOf(-2, -1, 0, 1, 2)
                            for (m in orders) {
                                val screenFringeY = centerY + m * ((fringeSpacingM / maxScreenHalfWidthM) * (h * 0.40f))
                                if (screenFringeY in 10f..(h - 10f)) {
                                    drawLine(
                                        color = laserColor.copy(alpha = 0.25f),
                                        start = Offset(barrierX, centerY),
                                        end = Offset(screenX, screenFringeY),
                                        strokeWidth = 1.2f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Slit Barrier Wall (Dark metal mask with apertures)
                    val barrierWidth = 10f
                    val slitApertureH = (slitWidthUm / 10f).coerceIn(4f, 14f)
                    val barrierWallColor = Color(0xFF1E2638)
                    val barrierBorder = Color(0xFF4A5568)

                    // Barrier top section
                    drawRect(barrierWallColor, Offset(barrierX - barrierWidth * 0.5f, 0f), Size(barrierWidth, s1Y - slitApertureH * 0.5f))
                    drawRect(barrierBorder, Offset(barrierX - 1f, 0f), Size(2f, s1Y - slitApertureH * 0.5f))

                    // Barrier middle section between slits
                    val midTop = s1Y + slitApertureH * 0.5f
                    val midBottom = s2Y - slitApertureH * 0.5f
                    if (midBottom > midTop) {
                        drawRect(barrierWallColor, Offset(barrierX - barrierWidth * 0.5f, midTop), Size(barrierWidth, midBottom - midTop))
                        drawRect(barrierBorder, Offset(barrierX - 1f, midTop), Size(2f, midBottom - midTop))
                    }

                    // Barrier bottom section
                    drawRect(barrierWallColor, Offset(barrierX - barrierWidth * 0.5f, s2Y + slitApertureH * 0.5f), Size(barrierWidth, h - (s2Y + slitApertureH * 0.5f)))
                    drawRect(barrierBorder, Offset(barrierX - 1f, s2Y + slitApertureH * 0.5f), Size(2f, h - (s2Y + slitApertureH * 0.5f)))

                    // Illuminated apertures
                    if (slit1Open) {
                        drawCircle(laserColor, 4.5f, Offset(barrierX, s1Y))
                        drawCircle(Color.White, 2.0f, Offset(barrierX, s1Y))
                    } else {
                        // Closed Slit 1 shutter
                        drawRect(CoralNeon.copy(alpha = 0.8f), Offset(barrierX - 4f, s1Y - slitApertureH * 0.5f), Size(8f, slitApertureH))
                    }

                    if (slit2Open) {
                        drawCircle(laserColor, 4.5f, Offset(barrierX, s2Y))
                        drawCircle(Color.White, 2.0f, Offset(barrierX, s2Y))
                    } else {
                        // Closed Slit 2 shutter
                        drawRect(CoralNeon.copy(alpha = 0.8f), Offset(barrierX - 4f, s2Y - slitApertureH * 0.5f), Size(8f, slitApertureH))
                    }

                    // 5. Detection Phosphor Screen on the Right
                    val screenWidthPx = w * 0.08f
                    drawRect(
                        color = Color(0xFF0F1522),
                        topLeft = Offset(screenX, 0f),
                        size = Size(screenWidthPx, h)
                    )
                    drawRect(
                        color = ScienceBorder,
                        topLeft = Offset(screenX, 0f),
                        size = Size(screenWidthPx, h),
                        style = Stroke(width = 1.2f)
                    )

                    // Continuous Optical Fringe Bands on the Screen
                    if (!isQuantumMode) {
                        val numSlices = 180
                        val sliceH = h / numSlices.toFloat()
                        val k = (2f * PI.toFloat()) / (wavelengthNm * 1e-9f)
                        val d = slitSeparationUm * 1e-6f
                        val a = slitWidthUm * 1e-6f

                        for (i in 0 until numSlices) {
                            val sliceY = i * sliceH + sliceH * 0.5f
                            val relY = (sliceY - centerY) / (h * 0.40f)
                            val theta = relY * (maxScreenHalfWidthM / screenDistanceM)

                            val beta = 0.5f * k * d * sin(theta)
                            val alpha = 0.5f * k * a * sin(theta)
                            val sincAlpha = if (abs(alpha) < 1e-4f) 1f else (sin(alpha) / alpha)
                            val envelope = sincAlpha.pow(2)

                            val intensity = when {
                                slit1Open && slit2Open -> cos(beta).pow(2) * envelope
                                slit1Open || slit2Open -> 0.35f * envelope
                                else -> 0f
                            }.coerceIn(0f, 1f)

                            if (intensity > 0.01f) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            laserColor.copy(alpha = intensity * 0.85f),
                                            Color.White.copy(alpha = intensity * 0.95f),
                                            laserColor.copy(alpha = intensity * 0.85f)
                                        ),
                                        startX = screenX,
                                        endX = screenX + screenWidthPx
                                    ),
                                    topLeft = Offset(screenX + 2f, i * sliceH),
                                    size = Size(screenWidthPx - 4f, sliceH + 0.5f)
                                )
                            }
                        }
                    } else {
                        // Quantum Mode: Draw discrete photon hits
                        for (hit in photonHits) {
                            val hitY = centerY + hit.normalizedY * (h * 0.40f)
                            val hitX = screenX + 4f + Random(hit.hashCode()).nextFloat() * (screenWidthPx - 8f)
                            val age = simTime - hit.birthTime
                            val alpha = (1f - (age * 0.05f)).coerceIn(0.2f, 1f)

                            drawCircle(laserColor.copy(alpha = alpha * 0.85f), 2.2f, Offset(hitX, hitY))
                            drawCircle(Color.White.copy(alpha = alpha), 1.0f, Offset(hitX, hitY))
                        }
                    }

                    // 6. Theoretical Intensity Curve Graph Overlay on the Screen
                    if (showIntensityCurve) {
                        val curveXBase = screenX + screenWidthPx + 4f
                        val maxCurveW = (w - curveXBase - 6f).coerceAtLeast(20f)
                        val curvePoints = mutableListOf<Offset>()
                        val numGraphSteps = 160
                        val k = (2f * PI.toFloat()) / (wavelengthNm * 1e-9f)
                        val d = slitSeparationUm * 1e-6f
                        val a = slitWidthUm * 1e-6f

                        for (step in 0..numGraphSteps) {
                            val curY = (step / numGraphSteps.toFloat()) * h
                            val relY = (curY - centerY) / (h * 0.40f)
                            val theta = relY * (maxScreenHalfWidthM / screenDistanceM)

                            val beta = 0.5f * k * d * sin(theta)
                            val alpha = 0.5f * k * a * sin(theta)
                            val sincAlpha = if (abs(alpha) < 1e-4f) 1f else (sin(alpha) / alpha)
                            val envelope = sincAlpha.pow(2)

                            val intensity = when {
                                slit1Open && slit2Open -> cos(beta).pow(2) * envelope
                                slit1Open || slit2Open -> 0.35f * envelope
                                else -> 0f
                            }.coerceIn(0f, 1f)

                            val plotX = curveXBase + intensity * maxCurveW
                            curvePoints.add(Offset(plotX, curY))
                        }

                        if (curvePoints.size > 1) {
                            for (idx in 0 until curvePoints.size - 1) {
                                drawLine(
                                    color = AmberVibrant.copy(alpha = 0.85f),
                                    start = curvePoints[idx],
                                    end = curvePoints[idx + 1],
                                    strokeWidth = 1.8f
                                )
                            }
                        }
                    }

                    // 7. Virtual Probe & Ray Tracing Indicator
                    val probePxX = probePosRel.x * w
                    val probePxY = probePosRel.y * h

                    // Rays connecting slits to probe
                    if (showRays && slit1Open) {
                        drawLine(
                            color = CyanNeon.copy(alpha = 0.55f),
                            start = Offset(barrierX, s1Y),
                            end = Offset(probePxX, probePxY),
                            strokeWidth = 1.2f,
                            cap = StrokeCap.Round
                        )
                    }
                    if (showRays && slit2Open) {
                        drawLine(
                            color = CoralNeon.copy(alpha = 0.55f),
                            start = Offset(barrierX, s2Y),
                            end = Offset(probePxX, probePxY),
                            strokeWidth = 1.2f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Probe Target Reticle
                    val probeColor = if (isNearConstructive) EmeraldNeon else CoralNeon
                    drawCircle(probeColor.copy(alpha = 0.3f), 12f, Offset(probePxX, probePxY))
                    drawCircle(probeColor, 4.5f, Offset(probePxX, probePxY))
                    drawCircle(Color.White, 1.8f, Offset(probePxX, probePxY))

                    drawLine(
                        color = probeColor.copy(alpha = 0.7f),
                        start = Offset(probePxX - 16f, probePxY),
                        end = Offset(probePxX + 16f, probePxY),
                        strokeWidth = 1.2f
                    )
                    drawLine(
                        color = probeColor.copy(alpha = 0.7f),
                        start = Offset(probePxX, probePxY - 16f),
                        end = Offset(probePxX, probePxY + 16f),
                        strokeWidth = 1.2f
                    )
                }

                // Interactive HUD Badge Overlay at Top-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Surface(
                        color = ScienceDarkSurface.copy(alpha = 0.90f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ScienceBorder.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(laserColor)
                                )
                                Text(
                                    text = "${round(wavelengthNm)} nm",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isNearConstructive) "• CONSTRUCTIVE" else "• DESTRUCTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNearConstructive) EmeraldNeon else CoralNeon
                                )
                            }
                            Text(
                                text = "Δr = ${round(abs(pathDiffM) * 1e9f)} nm (${round(abs(pathDiffWavelengths) * 100f) / 100f} λ)",
                                fontSize = 10.sp,
                                color = CyanNeon
                            )
                            Text(
                                text = "Intensity = ${round(totalIntensity * 100f)}%",
                                fontSize = 10.sp,
                                color = AmberVibrant
                            )
                        }
                    }
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Double-Slit Optical Telemetry",
                items = listOf(
                    "Fringe Spacing Δy" to "${round(fringeSpacingMm * 10f) / 10f} mm",
                    "Path Difference Δr" to "${round(abs(pathDiffWavelengths) * 100f) / 100f} λ",
                    "Interference Mode" to if (isNearConstructive) "CONSTRUCTIVE (Bright)" else "DESTRUCTIVE (Dark)",
                    "Wavelength λ" to "${round(wavelengthNm)} nm",
                    "Slit Spacing d" to "${round(slitSeparationUm)} μm",
                    "Diffraction Sinc²" to "${round(diffractionFactor * 100f)}%"
                )
            )
        },
        controlsContent = {
            // Interactive Controls Panel (Sliders, Slit Toggles, Presets)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ScienceDarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ScienceBorder.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DoubleSlitPreset.values().forEach { preset ->
                            val isSelected = selectedPreset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPreset = preset
                                    wavelengthNm = preset.wavelengthNm
                                    slitSeparationUm = preset.slitSeparationUm
                                    slitWidthUm = preset.slitWidthUm
                                    screenDistanceM = preset.screenDistanceM
                                    isQuantumMode = preset.isQuantumMode
                                    slit1Open = true
                                    slit2Open = true
                                    photonHits.clear()
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
                                    containerColor = ScienceDarkSurfaceVariant.copy(alpha = 0.6f),
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = ScienceBorder.copy(alpha = 0.35f),
                                    selectedBorderColor = CyanNeon
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    // Sliders Row 1: Wavelength & Slit Separation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Wavelength (λ)",
                                value = wavelengthNm,
                                range = 380f..750f,
                                valueDisplay = "${round(wavelengthNm)} nm",
                                accentColor = laserColor,
                                onValueChange = {
                                    wavelengthNm = it
                                    selectedPreset = null
                                }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Slit Spacing (d)",
                                value = slitSeparationUm,
                                range = 15f..100f,
                                valueDisplay = "${round(slitSeparationUm)} μm",
                                accentColor = CyanNeon,
                                onValueChange = {
                                    slitSeparationUm = it
                                    selectedPreset = null
                                }
                            )
                        }
                    }

                    // Sliders Row 2: Slit Width & Screen Distance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Slit Width (a)",
                                value = slitWidthUm,
                                range = 2f..25f,
                                valueDisplay = "${round(slitWidthUm)} μm",
                                accentColor = AmberVibrant,
                                onValueChange = {
                                    slitWidthUm = it
                                    selectedPreset = null
                                }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Distance (L)",
                                value = screenDistanceM,
                                range = 0.4f..2.5f,
                                valueDisplay = "${round(screenDistanceM * 10f) / 10f} m",
                                accentColor = EmeraldNeon,
                                onValueChange = {
                                    screenDistanceM = it
                                    selectedPreset = null
                                }
                            )
                        }
                    }

                    // Toggles Row: Slit 1 / Slit 2 Open, Quantum Mode, Clear Photons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Slit 1 Toggle
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clickable {
                                    slit1Open = !slit1Open
                                    selectedPreset = null
                                },
                            shape = RoundedCornerShape(6.dp),
                            color = if (slit1Open) CyanNeon.copy(alpha = 0.25f) else ScienceDarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (slit1Open) CyanNeon else ScienceBorder.copy(alpha = 0.4f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (slit1Open) "Slit 1: Open" else "Slit 1: Closed",
                                    fontSize = 10.sp,
                                    fontWeight = if (slit1Open) FontWeight.Bold else FontWeight.Normal,
                                    color = if (slit1Open) CyanNeon else TextSecondary
                                )
                            }
                        }

                        // Slit 2 Toggle
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clickable {
                                    slit2Open = !slit2Open
                                    selectedPreset = null
                                },
                            shape = RoundedCornerShape(6.dp),
                            color = if (slit2Open) CoralNeon.copy(alpha = 0.25f) else ScienceDarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (slit2Open) CoralNeon else ScienceBorder.copy(alpha = 0.4f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (slit2Open) "Slit 2: Open" else "Slit 2: Closed",
                                    fontSize = 10.sp,
                                    fontWeight = if (slit2Open) FontWeight.Bold else FontWeight.Normal,
                                    color = if (slit2Open) CoralNeon else TextSecondary
                                )
                            }
                        }

                        // Quantum Mode Toggle
                        Surface(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(32.dp)
                                .clickable {
                                    isQuantumMode = !isQuantumMode
                                    if (isQuantumMode) photonHits.clear()
                                    selectedPreset = null
                                },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isQuantumMode) PurpleNeon.copy(alpha = 0.3f) else ScienceDarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (isQuantumMode) PurpleNeon else ScienceBorder.copy(alpha = 0.4f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isQuantumMode) "Mode: Quantum" else "Mode: Wave",
                                    fontSize = 10.sp,
                                    fontWeight = if (isQuantumMode) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isQuantumMode) PurpleNeon else TextSecondary
                                )
                            }
                        }

                        // Reset / Clear Button
                        IconButton(
                            onClick = {
                                photonHits.clear()
                                probePosRel = Offset(0.92f, 0.50f)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(ScienceDarkSurfaceVariant, RoundedCornerShape(6.dp))
                        ) {
                            ResetIcon(tint = TextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    )
}
