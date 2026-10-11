package com.geosid.simplephysics.ui.experiments.week5.Day35

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
import androidx.compose.ui.draw.clip
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
 * Optical presets for Day 35: Atmospheric Mirage & Curved Rays.
 */
enum class MiragePreset(
    val label: String,
    val title: String,
    val groundTempC: Float,
    val airTempC: Float,
    val boundaryHeightM: Float,
    val laserAngleDeg: Float,
    val isSuperior: Boolean,
    val isFataMorgana: Boolean,
    val isGrinFiber: Boolean,
    val desc: String
) {
    DESERT_INFERIOR(
        label = "Desert Mirage",
        title = "Inferior Mirage (Highway Asphalt)",
        groundTempC = 58.0f,
        airTempC = 26.0f,
        boundaryHeightM = 0.8f,
        laserAngleDeg = -2.8f,
        isSuperior = false,
        isFataMorgana = false,
        isGrinFiber = false,
        desc = "Superheated air near road bends sky light upward into driver eyes, mimicking reflective water puddles"
    ),
    ARCTIC_SUPERIOR(
        label = "Arctic Superior",
        title = "Superior Mirage (Cold Sea Inversion)",
        groundTempC = -4.0f,
        airTempC = 18.0f,
        boundaryHeightM = 1.2f,
        laserAngleDeg = 1.8f,
        isSuperior = true,
        isFataMorgana = false,
        isGrinFiber = false,
        desc = "Dense freezing boundary layer over polar ocean bends rays downward, projecting ships floating high in the sky"
    ),
    FATA_MORGANA(
        label = "Fata Morgana",
        title = "Fata Morgana (Multi-Layer Inversion)",
        groundTempC = 35.0f,
        airTempC = 8.0f,
        boundaryHeightM = 1.0f,
        laserAngleDeg = -1.2f,
        isSuperior = false,
        isFataMorgana = true,
        isGrinFiber = false,
        desc = "Alternating warm and cold atmospheric strata duct and compress rays, producing towering castle-like mirage illusions"
    ),
    GRIN_WAVEGUIDE(
        label = "GRIN Fiber",
        title = "Graded-Index Waveguide (Optical Core)",
        groundTempC = 20.0f,
        airTempC = 20.0f,
        boundaryHeightM = 1.0f,
        laserAngleDeg = 4.5f,
        isSuperior = false,
        isFataMorgana = false,
        isGrinFiber = true,
        desc = "Parabolic radial refractive index profile continually refocuses rays in harmonic sinusoidal oscillation"
    ),
    LASER_PROBE(
        label = "Laser Probe",
        title = "Interactive Laser Beam Profiler",
        groundTempC = 65.0f,
        airTempC = 20.0f,
        boundaryHeightM = 0.9f,
        laserAngleDeg = -3.5f,
        isSuperior = false,
        isFataMorgana = false,
        isGrinFiber = false,
        desc = "Steer laser launch angle to observe Fermat turning point and local curvature radius kappa = |grad n| / n"
    )
}

/**
 * Single simulated optical ray path in stratified atmosphere.
 */
data class OpticalRay(
    val points: List<Offset>,
    val virtualProjection: List<Offset>,
    val launchAngleDeg: Float,
    val turningPoint: Offset?,
    val isReflectedToObserver: Boolean,
    val color: Color
)

@Composable
fun AtmosphericMirageExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(MiragePreset.DESERT_INFERIOR) }

    // Physical Controls
    var groundTempC by remember { mutableStateOf(currentPreset.groundTempC) }
    var airTempC by remember { mutableStateOf(currentPreset.airTempC) }
    var boundaryHeightM by remember { mutableStateOf(currentPreset.boundaryHeightM) }
    var laserAngleDeg by remember { mutableStateOf(currentPreset.laserAngleDeg) }

    // Simulation Features & Visualization Options
    var showVirtualRays by remember { mutableStateOf(true) }
    var showIsotherms by remember { mutableStateOf(true) }
    var showShimmer by remember { mutableStateOf(true) }
    var isLaserMode by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(true) }

    // Interactive Drag Source / Laser Position (normalized within [0..1])
    var sourcePosNorm by remember { mutableStateOf(Offset(0.12f, 0.42f)) }

    // Real-time animation loop for heat shimmer ripple and laser wavelets
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRunning) 2400 else 999999, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Compute key physical metrics for HUD
    // Gladstone-Dale relation: n(T) = 1 + (n0 - 1) * (T0 / T)
    val t0K = 288.15f // 15°C reference standard air
    val n0 = 1.000293f
    val tGroundK = (groundTempC + 273.15f).coerceAtLeast(200f)
    val tAirK = (airTempC + 273.15f).coerceAtLeast(200f)
    val nGround = 1.0f + (n0 - 1.0f) * (t0K / tGroundK)
    val nAir = 1.0f + (n0 - 1.0f) * (t0K / tAirK)
    val deltaN = nAir - nGround
    val gradientDnDy = if (boundaryHeightM > 0.05f) deltaN / boundaryHeightM else 0f
    val localCurvatureRadiusM = if (abs(gradientDnDy) > 1e-7f) abs(1.0f / gradientDnDy) else 9999f

    // Critical grazing angle for total refraction/turning: cos(theta_crit) = n_ground / n_air
    val cosRatio = (min(nGround, nAir) / max(nGround, nAir)).coerceIn(0f, 1f)
    val critAngleDeg = acos(cosRatio) * (180f / PI.toFloat())

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag laser source or slider to tilt rays. Observe how thermal gradients curve light via Fermat's principle!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newX = (sourcePosNorm.x + dragAmount.x / size.width).coerceIn(0.06f, 0.35f)
                            val newY = (sourcePosNorm.y + dragAmount.y / size.height).coerceIn(0.15f, 0.55f)
                            sourcePosNorm = Offset(newX, newY)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            val newX = (tapOffset.x / size.width).coerceIn(0.06f, 0.35f)
                            val newY = (tapOffset.y / size.height).coerceIn(0.15f, 0.55f)
                            sourcePosNorm = Offset(newX, newY)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated apparatus horizon: Keep ground around 0.58f of canvas height
                val groundY = h * 0.58f
                val skyHeight = groundY
                val groundDepth = h - groundY

                // 1. Atmospheric Sky & Background Rendering
                drawAtmosphericBackground(
                    w = w,
                    skyHeight = skyHeight,
                    groundY = groundY,
                    groundDepth = groundDepth,
                    currentPreset = currentPreset,
                    groundTempC = groundTempC,
                    airTempC = airTempC,
                    shimmerPhase = shimmerPhase,
                    showShimmer = showShimmer
                )

                // 2. Isotherm Density Contours / Refractive Gradient Visualization
                if (showIsotherms) {
                    drawIsothermContours(
                        w = w,
                        groundY = groundY,
                        boundaryHeightPx = groundY * (boundaryHeightM / 2.0f).coerceIn(0.15f, 0.85f),
                        currentPreset = currentPreset,
                        groundTempC = groundTempC,
                        airTempC = airTempC
                    )
                }

                // 3. Coordinate reference grid lines
                drawAtmosphericGrid(w, h, groundY)

                // 4. Distant Object Setup (Desert Palm Tree or Arctic Ship or Laser Mount)
                val objectX = w * sourcePosNorm.x
                val objectBaseY = groundY
                val objectTopY = groundY * sourcePosNorm.y

                // 5. Observer Camera / Eye Station (Right side)
                val observerX = w * 0.88f
                val observerEyeY = groundY - groundY * 0.28f

                // Draw Distant Target Object (Real physical object on left)
                drawDistantTargetObject(
                    objectX = objectX,
                    groundY = groundY,
                    currentPreset = currentPreset,
                    isLaserMode = isLaserMode,
                    shimmerPhase = shimmerPhase
                )

                // Draw Observer Station on the right
                drawObserverStation(
                    observerX = observerX,
                    observerEyeY = observerEyeY,
                    groundY = groundY
                )

                // 6. Ray Tracing Numerical Integration
                val rayList = calculateRayPaths(
                    w = w,
                    h = h,
                    groundY = groundY,
                    sourcePos = Offset(objectX, objectTopY),
                    observerPos = Offset(observerX, observerEyeY),
                    currentPreset = currentPreset,
                    groundTempC = groundTempC,
                    airTempC = airTempC,
                    boundaryHeightM = boundaryHeightM,
                    laserAngleDeg = laserAngleDeg,
                    isLaserMode = isLaserMode,
                    shimmerPhase = if (showShimmer && isRunning) shimmerPhase else 0f
                )

                // 7. Render Curved Optical Rays & Virtual Image Projections
                drawCurvedRaysAndMirage(
                    rays = rayList,
                    groundY = groundY,
                    showVirtualRays = showVirtualRays,
                    currentPreset = currentPreset,
                    objectX = objectX,
                    observerX = observerX
                )

                // 8. Interactive Laser Emitter Handle & Angle Reticle
                drawInteractiveHandle(
                    pos = Offset(objectX, objectTopY),
                    angleDeg = laserAngleDeg,
                    isLaserMode = isLaserMode
                )
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 35: Atmospheric Mirage & Curved Rays",
                items = listOf(
                    "Ray Curvature ODE" to "d²y/dx² = (1/n) · (dn/dy)",
                    "Snell Invariant" to "n(y) · cos(θ) = const",
                    "ΔT (Ground - Air)" to "${round((groundTempC - airTempC) * 10f) / 10f} °C",
                    "Index Gradient (dn/dy)" to "${(gradientDnDy * 1e5f).roundToInt()} × 10⁻⁵ m⁻¹",
                    "Radius of Curvature" to if (localCurvatureRadiusM < 5000f) "${localCurvatureRadiusM.roundToInt()} m" else "∞ (Flat)",
                    "Critical Grazing Angle" to "${round(critAngleDeg * 100f) / 100f}°"
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
                    items(MiragePreset.values()) { preset ->
                        val isSelected = currentPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                currentPreset = preset
                                groundTempC = preset.groundTempC
                                airTempC = preset.airTempC
                                boundaryHeightM = preset.boundaryHeightM
                                laserAngleDeg = preset.laserAngleDeg
                                isLaserMode = (preset == MiragePreset.LASER_PROBE || preset == MiragePreset.GRIN_WAVEGUIDE)
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

                // Paired Sliders Row 1: Ground Temp & Air Temp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Ground Temp (T_g)",
                        value = groundTempC,
                        range = -10f..70f,
                        valueDisplay = "${groundTempC.roundToInt()}°C",
                        accentColor = if (groundTempC > airTempC) CoralNeon else CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { groundTempC = it }
                    )

                    PhysicsSliderControl(
                        title = "Air Temp (T_air)",
                        value = airTempC,
                        range = -10f..45f,
                        valueDisplay = "${airTempC.roundToInt()}°C",
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { airTempC = it }
                    )
                }

                // Paired Sliders Row 2: Boundary Layer Height & Ray Launch Angle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Boundary Layer (h)",
                        value = boundaryHeightM,
                        range = 0.2f..2.0f,
                        valueDisplay = "${round(boundaryHeightM * 10f) / 10f} m",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = { boundaryHeightM = it }
                    )

                    PhysicsSliderControl(
                        title = "Ray Launch Angle (θ)",
                        value = laserAngleDeg,
                        range = -8.0f..8.0f,
                        valueDisplay = "${round(laserAngleDeg * 10f) / 10f}°",
                        accentColor = EmeraldNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { laserAngleDeg = it }
                    )
                }

                // Compact Feature Toggle Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showVirtualRays,
                        onClick = { showVirtualRays = !showVirtualRays },
                        label = { Text("Virtual Rays", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberVibrant.copy(alpha = 0.25f),
                            selectedLabelColor = AmberVibrant,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    FilterChip(
                        selected = showIsotherms,
                        onClick = { showIsotherms = !showIsotherms },
                        label = { Text("Isotherms", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CyanNeon,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    FilterChip(
                        selected = isLaserMode,
                        onClick = { isLaserMode = !isLaserMode },
                        label = { Text(if (isLaserMode) "Laser Mode" else "Object Mode", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldNeon.copy(alpha = 0.25f),
                            selectedLabelColor = EmeraldNeon,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )
                }

                // Bottom Action Bar: Play/Pause and Reset Buttons
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
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause Shimmer" else "▶ Run Shimmer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            groundTempC = currentPreset.groundTempC
                            airTempC = currentPreset.airTempC
                            boundaryHeightM = currentPreset.boundaryHeightM
                            laserAngleDeg = currentPreset.laserAngleDeg
                            sourcePosNorm = Offset(0.12f, 0.42f)
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
 * 1. Background sky and ground drawing with heat shimmer ripple.
 */
private fun DrawScope.drawAtmosphericBackground(
    w: Float,
    skyHeight: Float,
    groundY: Float,
    groundDepth: Float,
    currentPreset: MiragePreset,
    groundTempC: Float,
    airTempC: Float,
    shimmerPhase: Float,
    showShimmer: Boolean
) {
    val isInferior = groundTempC > airTempC
    val isArctic = currentPreset == MiragePreset.ARCTIC_SUPERIOR

    // Sky Gradient
    val skyColors = when {
        isArctic -> listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77)) // Twilight Polar Sky
        currentPreset == MiragePreset.GRIN_WAVEGUIDE -> listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
        else -> listOf(Color(0xFF0B2545), Color(0xFF134074), Color(0xFF8DA9C4)) // Desert Blazing Sky
    }

    drawRect(
        brush = Brush.verticalGradient(
            colors = skyColors,
            startY = 0f,
            endY = skyHeight
        ),
        topLeft = Offset.Zero,
        size = Size(w, skyHeight)
    )

    // Sun / Polar Glow in Sky
    if (!isArctic && currentPreset != MiragePreset.GRIN_WAVEGUIDE) {
        val sunCenter = Offset(w * 0.18f, skyHeight * 0.22f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AmberVibrant.copy(alpha = 0.8f), AmberVibrant.copy(alpha = 0.15f), Color.Transparent),
                center = sunCenter,
                radius = 45f
            ),
            radius = 45f,
            center = sunCenter
        )
        drawCircle(Color(0xFFFFEEAA), 14f, sunCenter)
    }

    // Ground Surface: Asphalt Road or Icy Arctic Water
    val groundColors = when {
        isArctic -> listOf(Color(0xFF0B3954), Color(0xFF087E8B), Color(0xFF05202A)) // Frozen Arctic Ocean
        currentPreset == MiragePreset.GRIN_WAVEGUIDE -> listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        else -> listOf(Color(0xFF2B2D42), Color(0xFF1F202E), Color(0xFF14141E)) // Hot Asphalt Highway
    }

    drawRect(
        brush = Brush.verticalGradient(
            colors = groundColors,
            startY = groundY,
            endY = groundY + groundDepth
        ),
        topLeft = Offset(0f, groundY),
        size = Size(w, groundDepth)
    )

    // Asphalt Texture / Road Markings or Sea Ice Floes
    if (!isArctic && currentPreset != MiragePreset.GRIN_WAVEGUIDE) {
        // Horizon road surface highlight
        drawLine(
            color = if (isInferior) CoralNeon.copy(alpha = 0.6f) else ScienceBorder,
            start = Offset(0f, groundY),
            end = Offset(w, groundY),
            strokeWidth = 2.5f
        )

        // Highway dashed center line
        val dashY = groundY + groundDepth * 0.45f
        var rx = 0f
        while (rx < w) {
            drawLine(
                color = AmberVibrant.copy(alpha = 0.35f),
                start = Offset(rx, dashY),
                end = Offset(rx + 24f, dashY),
                strokeWidth = 2.0f
            )
            rx += 48f
        }
    } else if (isArctic) {
        // Ice Floes along ocean horizon
        drawLine(
            color = CyanNeon.copy(alpha = 0.75f),
            start = Offset(0f, groundY),
            end = Offset(w, groundY),
            strokeWidth = 2.0f
        )
        for (i in 0..8) {
            val fx = w * (0.08f + i * 0.11f)
            val fw = w * 0.06f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(fx, groundY + 4f),
                size = Size(fw, 5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
            )
        }
    }

    // Heat Shimmer Ripple Wavelets directly above road (Inferior Mirage water puddle illusion)
    if (showShimmer && isInferior && currentPreset != MiragePreset.GRIN_WAVEGUIDE) {
        val rippleY = groundY - 6f
        val shimmerPath = Path()
        val segments = 80
        for (i in 0..segments) {
            val t = i / segments.toFloat()
            val sx = w * t
            val sy = rippleY + sin(shimmerPhase * 2f + t * 24f) * 3.5f
            if (i == 0) shimmerPath.moveTo(sx, sy) else shimmerPath.lineTo(sx, sy)
        }
        drawPath(
            path = shimmerPath,
            color = CyanNeon.copy(alpha = 0.45f),
            style = Stroke(width = 2.2f)
        )

        // Shimmering reflective water puddle illusion patch
        val puddleW = w * 0.45f
        val puddleX = w * 0.38f
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(CyanNeon.copy(alpha = 0.35f), Color(0x228DA9C4), Color.Transparent),
                center = Offset(puddleX + puddleW * 0.5f, groundY + 2f),
                radius = puddleW * 0.5f
            ),
            topLeft = Offset(puddleX, groundY - 4f),
            size = Size(puddleW, 14f)
        )
    }
}

/**
 * 2. Isotherm density bands and refractive index gradient visualization.
 */
private fun DrawScope.drawIsothermContours(
    w: Float,
    groundY: Float,
    boundaryHeightPx: Float,
    currentPreset: MiragePreset,
    groundTempC: Float,
    airTempC: Float
) {
    val layers = 5
    for (i in 0 until layers) {
        val fraction = i / layers.toFloat()
        val y = groundY - boundaryHeightPx * fraction
        val tempAtY = groundTempC + (airTempC - groundTempC) * fraction

        val contourColor = if (groundTempC > airTempC) {
            // Hot near ground (Coral/Amber) transitioning to Cool (Cyan) aloft
            lerp(CoralNeon, CyanNeon, fraction).copy(alpha = 0.22f)
        } else {
            // Cold near ground (Cyan/Purple) transitioning to Warm (Amber) aloft
            lerp(CyanNeon, AmberVibrant, fraction).copy(alpha = 0.22f)
        }

        drawLine(
            color = contourColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    }
}

/**
 * 3. Coordinate reference grid.
 */
private fun DrawScope.drawAtmosphericGrid(w: Float, h: Float, groundY: Float) {
    // Ground Horizon Line
    drawLine(
        color = ScienceBorder.copy(alpha = 0.6f),
        start = Offset(0f, groundY),
        end = Offset(w, groundY),
        strokeWidth = 1.5f
    )
}

/**
 * 4. Distant Target Object (Palm Tree in desert, Sailboat in Arctic, or Laser Emitter).
 */
private fun DrawScope.drawDistantTargetObject(
    objectX: Float,
    groundY: Float,
    currentPreset: MiragePreset,
    isLaserMode: Boolean,
    shimmerPhase: Float
) {
    if (isLaserMode) return

    val isArctic = currentPreset == MiragePreset.ARCTIC_SUPERIOR

    if (isArctic) {
        // Draw Distant Polar Cargo / Sailboat
        val hullW = 38f
        val hullH = 12f
        val hullY = groundY - hullH
        val shipPath = Path().apply {
            moveTo(objectX - hullW * 0.5f, hullY)
            lineTo(objectX + hullW * 0.5f, hullY)
            lineTo(objectX + hullW * 0.35f, groundY)
            lineTo(objectX - hullW * 0.35f, groundY)
            close()
        }
        drawPath(shipPath, Color(0xFFC0D6DF))
        drawPath(shipPath, Color.White, style = Stroke(1.5f))

        // Mast & Sail
        drawLine(Color.White, Offset(objectX, hullY), Offset(objectX, hullY - 32f), 2.5f)
        val sailPath = Path().apply {
            moveTo(objectX, hullY - 30f)
            lineTo(objectX + 16f, hullY - 8f)
            lineTo(objectX, hullY - 8f)
            close()
        }
        drawPath(sailPath, CyanNeon.copy(alpha = 0.85f))
    } else {
        // Draw Desert Palm Tree
        val trunkBaseY = groundY
        val trunkTopY = groundY - 55f
        // Curved trunk
        val trunkPath = Path().apply {
            moveTo(objectX - 3f, trunkBaseY)
            cubicTo(
                objectX - 6f, trunkBaseY - 20f,
                objectX + 4f, trunkBaseY - 40f,
                objectX, trunkTopY
            )
            lineTo(objectX + 4f, trunkTopY)
            cubicTo(
                objectX + 8f, trunkBaseY - 40f,
                objectX + 2f, trunkBaseY - 20f,
                objectX + 3f, trunkBaseY
            )
            close()
        }
        drawPath(trunkPath, Color(0xFF8B5A2B))

        // Palm Fronds / Leaves
        val frondAngles = listOf(-60f, -30f, 0f, 30f, 60f, -80f, 80f)
        for (ang in frondAngles) {
            val rad = ang * (PI.toFloat() / 180f)
            val fx = objectX + sin(rad) * 26f
            val fy = trunkTopY - cos(rad) * 14f + abs(sin(rad)) * 8f
            drawLine(
                color = EmeraldNeon,
                start = Offset(objectX, trunkTopY),
                end = Offset(fx, fy),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * 5. Observer Camera / Eye Station on Right.
 */
private fun DrawScope.drawObserverStation(
    observerX: Float,
    observerEyeY: Float,
    groundY: Float
) {
    // Observer Tripod / Stand
    drawLine(ScienceBorder, Offset(observerX, groundY), Offset(observerX, observerEyeY + 12f), 2.5f)
    drawLine(ScienceBorder, Offset(observerX - 10f, groundY), Offset(observerX, observerEyeY + 14f), 1.5f)
    drawLine(ScienceBorder, Offset(observerX + 10f, groundY), Offset(observerX, observerEyeY + 14f), 1.5f)

    // Camera / Telescope Reticle Eye
    drawCircle(ScienceDarkSurfaceVariant, 10f, Offset(observerX, observerEyeY))
    drawCircle(CyanNeon, 10f, Offset(observerX, observerEyeY), style = Stroke(1.8f))
    drawCircle(Color.White, 4f, Offset(observerX, observerEyeY))

    // Sightline Cone
    drawLine(
        color = CyanNeon.copy(alpha = 0.35f),
        start = Offset(observerX - 10f, observerEyeY),
        end = Offset(observerX - 45f, observerEyeY - 14f),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
    )
    drawLine(
        color = CyanNeon.copy(alpha = 0.35f),
        start = Offset(observerX - 10f, observerEyeY),
        end = Offset(observerX - 45f, observerEyeY + 14f),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
    )
}

/**
 * 6. Numerical Ray Tracing Engine via Fermat's Principle / Continuous Snell's Law.
 */
private fun calculateRayPaths(
    w: Float,
    h: Float,
    groundY: Float,
    sourcePos: Offset,
    observerPos: Offset,
    currentPreset: MiragePreset,
    groundTempC: Float,
    airTempC: Float,
    boundaryHeightM: Float,
    laserAngleDeg: Float,
    isLaserMode: Boolean,
    shimmerPhase: Float
): List<OpticalRay> {
    val rays = mutableListOf<OpticalRay>()

    // Set up ray launch angles based on mode
    val launchAnglesDeg: List<Float> = if (isLaserMode) {
        // Multi-ray bundle centered at laserAngleDeg
        listOf(
            laserAngleDeg - 1.2f,
            laserAngleDeg - 0.6f,
            laserAngleDeg,
            laserAngleDeg + 0.6f,
            laserAngleDeg + 1.2f
        )
    } else {
        // Rays originating from object top and heading downward toward ground or observer
        listOf(
            -0.5f,
            -1.4f,
            -2.2f,
            -3.0f,
            -3.8f,
            -4.6f,
            0.5f
        )
    }

    val t0K = 288.15f
    val n0 = 1.000293f
    val tGroundK = (groundTempC + 273.15f).coerceAtLeast(200f)
    val tAirK = (airTempC + 273.15f).coerceAtLeast(200f)
    val boundaryPx = (groundY * (boundaryHeightM / 2.0f)).coerceIn(30f, groundY * 0.85f)

    // Ray tracing step configuration
    val ds = 3.5f // step distance along ray in pixels
    val maxSteps = 450

    val rayColors = listOf(
        CyanNeon,
        AmberVibrant,
        CoralNeon,
        EmeraldNeon,
        Color(0xFF7000FF),
        Color(0xFFFF007F),
        Color.White
    )

    launchAnglesDeg.forEachIndexed { idx, initAngleDeg ->
        var x = sourcePos.x
        var y = sourcePos.y
        var thetaRad = initAngleDeg * (PI.toFloat() / 180f)

        val pts = mutableListOf<Offset>()
        pts.add(Offset(x, y))

        var turningPt: Offset? = null
        var prevDy = sin(thetaRad)
        var hasTurned = false

        for (step in 0 until maxSteps) {
            // Physical altitude y above ground (0 at groundY, positive aloft)
            val altitudePx = (groundY - y).coerceAtLeast(0.1f)

            // Local Refractive Index n(y) & Vertical Gradient dn/dy
            val (nLocal, dnDyLocal) = calculateAtmosphericIndexAndGradient(
                altitudePx = altitudePx,
                boundaryPx = boundaryPx,
                t0K = t0K,
                n0 = n0,
                tGroundK = tGroundK,
                tAirK = tAirK,
                currentPreset = currentPreset,
                groundY = groundY,
                y = y,
                shimmerPhase = shimmerPhase
            )

            // Step 1: Continuous Snell's law invariant: n(y) * cos(theta) = constant
            // Step 2: Ray curvature ODE: d_theta / ds = (1 / n) * (dn / dy) * cos(theta)
            val dThetaDs = (1.0f / nLocal) * dnDyLocal * cos(thetaRad)

            // Update angle and position (Euler / Midpoint step)
            thetaRad += dThetaDs * ds
            val dx = cos(thetaRad) * ds
            val dy = -sin(thetaRad) * ds // Screen coords: negative dy is upward in altitude

            x += dx
            y += dy
            pts.add(Offset(x, y))

            // Detect turning point (where vertical component changes sign)
            val currentDy = dy
            if (!hasTurned && (prevDy * currentDy <= 0f) && step > 5) {
                turningPt = Offset(x, y)
                hasTurned = true
            }
            prevDy = currentDy

            // Check boundaries
            if (y >= groundY) {
                // Grazing ground hit
                break
            }
            if (x >= w || y <= 0f || x < 0f) {
                break
            }
        }

        // Virtual Ray Backward Projection (Tangential extension from ray end)
        val virtualPts = mutableListOf<Offset>()
        if (pts.size >= 4 && hasTurned) {
            val lastPt = pts.last()
            val secondLastPt = pts[pts.size - 4]
            val arrivalDir = (lastPt - secondLastPt)
            val arrivalNorm = if (arrivalDir.getDistance() > 1e-4f) arrivalDir / arrivalDir.getDistance() else Offset(1f, 0f)

            // Backward straight-line projection into mirage virtual image location
            val projDist = (lastPt.x - sourcePos.x) * 1.05f
            val projStart = lastPt
            val projEnd = Offset(
                lastPt.x - arrivalNorm.x * projDist,
                lastPt.y - arrivalNorm.y * projDist
            )
            virtualPts.add(projStart)
            virtualPts.add(projEnd)
        }

        val col = rayColors[idx % rayColors.size]
        rays.add(
            OpticalRay(
                points = pts,
                virtualProjection = virtualPts,
                launchAngleDeg = initAngleDeg,
                turningPoint = turningPt,
                isReflectedToObserver = hasTurned,
                color = col
            )
        )
    }

    return rays
}

/**
 * Calculates local refractive index and its vertical gradient at height y.
 */
private fun calculateAtmosphericIndexAndGradient(
    altitudePx: Float,
    boundaryPx: Float,
    t0K: Float,
    n0: Float,
    tGroundK: Float,
    tAirK: Float,
    currentPreset: MiragePreset,
    groundY: Float,
    y: Float,
    shimmerPhase: Float
): Pair<Float, Float> {
    if (currentPreset == MiragePreset.GRIN_WAVEGUIDE) {
        // Parabolic GRIN fiber profile: n(y) = n0 * (1 - 0.5 * (y - yc)^2 / a^2)
        val yc = groundY * 0.42f
        val a = groundY * 0.35f
        val deltaY = (y - yc)
        val nLocal = n0 * (1.0f - 0.0004f * (deltaY / a).pow(2))
        val dnDy = -n0 * 0.0008f * (deltaY / (a * a))
        return Pair(nLocal, dnDy)
    }

    // Exponential thermal boundary layer: T(y) = T_air + (T_ground - T_air) * exp(-y / h)
    val decay = exp(-altitudePx / boundaryPx)
    val localTempK = tAirK + (tGroundK - tAirK) * decay

    // Gladstone-Dale Law: n(y) = 1 + (n0 - 1) * (T0 / T(y))
    val nLocal = 1.0f + (n0 - 1.0f) * (t0K / localTempK)

    // dn/dT = -(n0 - 1) * T0 / T^2
    // dT/dy = -(T_ground - T_air) / boundaryPx * exp(-y / boundaryPx)
    // dn/dy = (dn/dT) * (dT/dy)
    val dnDt = -(n0 - 1.0f) * (t0K / (localTempK * localTempK))
    val dtDy = -(tGroundK - tAirK) / boundaryPx * decay
    var dnDy = dnDt * dtDy

    // Heat shimmer ripple modulation near ground
    if (shimmerPhase != 0f && altitudePx < boundaryPx * 0.4f) {
        val rippleMod = 1.0f + 0.15f * sin(shimmerPhase * 2f + altitudePx * 0.1f)
        dnDy *= rippleMod
    }

    return Pair(nLocal, dnDy)
}

/**
 * 7. Draws the curved optical rays and their virtual tangent projections.
 */
private fun DrawScope.drawCurvedRaysAndMirage(
    rays: List<OpticalRay>,
    groundY: Float,
    showVirtualRays: Boolean,
    currentPreset: MiragePreset,
    objectX: Float,
    observerX: Float
) {
    rays.forEach { ray ->
        if (ray.points.size >= 2) {
            val rayPath = Path()
            ray.points.forEachIndexed { i, pt ->
                if (i == 0) rayPath.moveTo(pt.x, pt.y) else rayPath.lineTo(pt.x, pt.y)
            }

            // Glow backing
            drawPath(
                path = rayPath,
                color = ray.color.copy(alpha = 0.25f),
                style = Stroke(width = 5.5f, cap = StrokeCap.Round)
            )

            // Sharp core ray
            drawPath(
                path = rayPath,
                color = ray.color,
                style = Stroke(width = 2.2f, cap = StrokeCap.Round)
            )

            // Turning point indicator
            ray.turningPoint?.let { turn ->
                drawCircle(ray.color, 4f, turn)
                drawCircle(Color.White, 2f, turn)
            }
        }

        // Virtual Ray Backward Projection (Mirage Image Tangent)
        if (showVirtualRays && ray.virtualProjection.size >= 2) {
            val pStart = ray.virtualProjection[0]
            val pEnd = ray.virtualProjection[1]

            drawLine(
                color = AmberVibrant.copy(alpha = 0.75f),
                start = pStart,
                end = pEnd,
                strokeWidth = 1.8f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            // Apparent Virtual Miraged Object at projected position
            drawCircle(
                color = AmberVibrant,
                radius = 4.5f,
                center = pEnd
            )
        }
    }

    // Draw Inverted Virtual Object Mirage Image (Palm Tree upside down or Ship high in sky)
    if (showVirtualRays && currentPreset != MiragePreset.GRIN_WAVEGUIDE) {
        drawVirtualMirageImage(
            objectX = objectX,
            groundY = groundY,
            currentPreset = currentPreset
        )
    }
}

/**
 * Draws the inverted / displaced virtual image created by the mirage.
 */
private fun DrawScope.drawVirtualMirageImage(
    objectX: Float,
    groundY: Float,
    currentPreset: MiragePreset
) {
    val isArctic = currentPreset == MiragePreset.ARCTIC_SUPERIOR

    if (isArctic) {
        // Superior Mirage: Floating Ship High Above Horizon
        val floatingY = groundY - 68f
        val hullW = 34f
        val hullH = 10f

        val shipPath = Path().apply {
            moveTo(objectX - hullW * 0.5f, floatingY)
            lineTo(objectX + hullW * 0.5f, floatingY)
            lineTo(objectX + hullW * 0.35f, floatingY + hullH)
            lineTo(objectX - hullW * 0.35f, floatingY + hullH)
            close()
        }
        drawPath(shipPath, AmberVibrant.copy(alpha = 0.55f))
        drawPath(shipPath, AmberVibrant, style = Stroke(1.2f))

        // Virtual label hint
        drawLine(
            color = AmberVibrant.copy(alpha = 0.45f),
            start = Offset(objectX, floatingY + hullH + 4f),
            end = Offset(objectX, groundY - 14f),
            strokeWidth = 1.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )
    } else {
        // Inferior Mirage: Inverted Palm Tree beneath ground level (Reflective Water Illusion)
        val invertedTopY = groundY + 38f
        val trunkBaseY = groundY

        val trunkPath = Path().apply {
            moveTo(objectX - 2.5f, trunkBaseY)
            lineTo(objectX - 3.5f, invertedTopY)
            lineTo(objectX + 3.5f, invertedTopY)
            lineTo(objectX + 2.5f, trunkBaseY)
            close()
        }
        drawPath(trunkPath, Color(0x668B5A2B))

        // Inverted Fronds
        val frondAngles = listOf(-50f, -25f, 0f, 25f, 50f)
        for (ang in frondAngles) {
            val rad = ang * (PI.toFloat() / 180f)
            val fx = objectX + sin(rad) * 20f
            val fy = invertedTopY + cos(rad) * 12f
            drawLine(
                color = EmeraldNeon.copy(alpha = 0.55f),
                start = Offset(objectX, invertedTopY),
                end = Offset(fx, fy),
                strokeWidth = 2.2f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * 8. Interactive Handle and Launch Angle Reticle.
 */
private fun DrawScope.drawInteractiveHandle(
    pos: Offset,
    angleDeg: Float,
    isLaserMode: Boolean
) {
    // Halo Glow
    drawCircle(
        color = CyanNeon.copy(alpha = 0.25f),
        radius = 18f,
        center = pos
    )

    // Inner Node
    drawCircle(
        color = CyanNeon,
        radius = 7f,
        center = pos
    )
    drawCircle(
        color = Color.White,
        radius = 3f,
        center = pos
    )

    // Angle Direction Arrow
    val rad = angleDeg * (PI.toFloat() / 180f)
    val arrowLen = 28f
    val arrowEnd = Offset(
        pos.x + cos(rad) * arrowLen,
        pos.y - sin(rad) * arrowLen
    )
    drawLine(
        color = AmberVibrant,
        start = pos,
        end = arrowEnd,
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
}
