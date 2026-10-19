package com.geosid.simplephysics.ui.experiments.week6.Day39

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
 * Presets for Day 39: Venturi Tube & Pressure Drop.
 */
enum class VenturiPreset(
    val label: String,
    val title: String,
    val fluidName: String,
    val fluidDensityKgM3: Float,
    val inletVelocityMs: Float,
    val inletDiameterMm: Float,
    val throatRatio: Float, // D2 / D1
    val inletPressureKPa: Float,
    val isLiquid: Boolean,
    val hasSuctionTube: Boolean,
    val hasCavitation: Boolean,
    val desc: String
) {
    WATER_METER(
        label = "Water Meter",
        title = "Laboratory Venturi Flowmeter",
        fluidName = "Water (Incompressible Liquid)",
        fluidDensityKgM3 = 1000f,
        inletVelocityMs = 1.4f,
        inletDiameterMm = 50f,
        throatRatio = 0.50f, // A1 / A2 = 4.0
        inletPressureKPa = 140f,
        isLiquid = true,
        hasSuctionTube = false,
        hasCavitation = false,
        desc = "Liquid accelerates 4x through the throat, creating an observable hydrostatic column height drop Δh"
    ),
    CARBURETOR_JET(
        label = "Carburetor",
        title = "Automotive Carburetor Fuel Jet",
        fluidName = "Intake Air Stream",
        fluidDensityKgM3 = 1.225f,
        inletVelocityMs = 16f,
        inletDiameterMm = 45f,
        throatRatio = 0.38f, // A1 / A2 ~ 6.9
        inletPressureKPa = 101.3f,
        isLiquid = false,
        hasSuctionTube = true,
        hasCavitation = false,
        desc = "High-speed air depression in the venturi throat suctions fuel from a reservoir, atomizing it into the engine"
    ),
    PERFUME_ATOMIZER(
        label = "Atomizer",
        title = "Perfume / Paint Spray Atomizer",
        fluidName = "Compressed Air Jet",
        fluidDensityKgM3 = 1.225f,
        inletVelocityMs = 24f,
        inletDiameterMm = 36f,
        throatRatio = 0.33f,
        inletPressureKPa = 115f,
        isLiquid = false,
        hasSuctionTube = true,
        hasCavitation = false,
        desc = "Squeezing the bulb rushes air past a vertical riser tube, drawing liquid upward and shearing it into fine droplets"
    ),
    CAVITATION_CHOKE(
        label = "Cavitation",
        title = "Hydrodynamic Cavitation Choke",
        fluidName = "High-Speed Pressurized Water",
        fluidDensityKgM3 = 1000f,
        inletVelocityMs = 3.6f,
        inletDiameterMm = 60f,
        throatRatio = 0.30f, // A1 / A2 ~ 11.1
        inletPressureKPa = 160f,
        isLiquid = true,
        hasSuctionTube = false,
        hasCavitation = true,
        desc = "Throat pressure drops below water vapor pressure (2.3 kPa), violently boiling water into transient vapor cavities"
    ),
    WIND_TUNNEL(
        label = "Wind Tunnel",
        title = "Subsonic Contraction Test Section",
        fluidName = "Smooth Airflow",
        fluidDensityKgM3 = 1.225f,
        inletVelocityMs = 10f,
        inletDiameterMm = 55f,
        throatRatio = 0.60f,
        inletPressureKPa = 101.3f,
        isLiquid = false,
        hasSuctionTube = false,
        hasCavitation = false,
        desc = "Convergent-divergent duct accelerates airflow smoothly into an aerodynamic test chamber with low turbulence"
    )
}

/**
 * Tracer particle tracking streamline motion through the tube.
 */
private data class VenturiParticle(
    var normX: Float,
    var normYOffset: Float, // -1f (top) to +1f (bottom)
    val color: Color,
    val sizePx: Float,
    val isCavitation: Boolean = false
)

@Composable
fun VenturiTubeExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(VenturiPreset.WATER_METER) }

    // Physical Parameters
    var inletVelocityMs by remember { mutableStateOf(currentPreset.inletVelocityMs) }
    var throatRatio by remember { mutableStateOf(currentPreset.throatRatio) }
    var inletPressureKPa by remember { mutableStateOf(currentPreset.inletPressureKPa) }
    var fluidDensityKgM3 by remember { mutableStateOf(currentPreset.fluidDensityKgM3) }

    // Display Toggles
    var showStreamlines by remember { mutableStateOf(true) }
    var showManometers by remember { mutableStateOf(true) }
    var showPressureHeatmap by remember { mutableStateOf(true) }
    var showVelocityVectors by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(true) }

    // Reset when switching presets
    fun applyPreset(preset: VenturiPreset) {
        currentPreset = preset
        inletVelocityMs = preset.inletVelocityMs
        throatRatio = preset.throatRatio
        inletPressureKPa = preset.inletPressureKPa
        fluidDensityKgM3 = preset.fluidDensityKgM3
    }

    // Dynamic Tracer Particles
    val particles = remember {
        mutableStateListOf<VenturiParticle>().apply {
            repeat(50) {
                add(
                    VenturiParticle(
                        normX = Random.nextFloat(),
                        normYOffset = (Random.nextFloat() * 2f - 1f) * 0.85f,
                        color = if (Random.nextBoolean()) CyanNeon else Color.White.copy(alpha = 0.75f),
                        sizePx = Random.nextFloat() * 2.5f + 1.8f
                    )
                )
            }
        }
    }

    // Suction droplets for carburetor/atomizer
    val suctionDroplets = remember {
        mutableStateListOf<Offset>()
    }

    // Cavitation vapor bubbles
    val cavitationBubbles = remember {
        mutableStateListOf<VenturiParticle>()
    }

    // Mathematical Calculations
    val d1M = currentPreset.inletDiameterMm * 1e-3f
    val d2M = d1M * throatRatio
    val area1M2 = PI.toFloat() * (d1M * 0.5f) * (d1M * 0.5f)
    val area2M2 = PI.toFloat() * (d2M * 0.5f) * (d2M * 0.5f)
    val areaRatio = area1M2 / max(area2M2, 1e-7f) // (D1/D2)^2

    // 1. Mass Continuity Equation: Q = A1 * v1 = A2 * v2
    val volumeFlowRateM3S = area1M2 * inletVelocityMs
    val volumeFlowRateLMin = volumeFlowRateM3S * 60_000f
    val throatVelocityMs = inletVelocityMs * areaRatio

    // 2. Bernoulli's Equation: P1 + 0.5 * rho * v1^2 = P2 + 0.5 * rho * v2^2
    // Delta P = P1 - P2 = 0.5 * rho * (v2^2 - v1^2)
    val dynamicPressure1Pa = 0.5f * fluidDensityKgM3 * inletVelocityMs * inletVelocityMs
    val dynamicPressure2Pa = 0.5f * fluidDensityKgM3 * throatVelocityMs * throatVelocityMs
    val deltaPPa = dynamicPressure2Pa - dynamicPressure1Pa
    val deltaPKPa = deltaPPa / 1000f

    val p1Pa = inletPressureKPa * 1000f
    val throatPressurePa = max(0f, p1Pa - deltaPPa)
    val throatPressureKPa = throatPressurePa / 1000f

    // Cavitation threshold for water at 20°C: P_vapor ≈ 2.34 kPa
    val vaporPressurePa = 2340f
    val isCavitationActive = currentPreset.isLiquid && (throatPressurePa <= vaporPressurePa || currentPreset.hasCavitation)

    // Diffuser recovery (slight viscous head loss ~ 8%)
    val recoveryEfficiency = 0.92f
    val p3Pa = p1Pa - (1f - recoveryEfficiency) * deltaPPa
    val p3KPa = p3Pa / 1000f

    // 3. Manometer Hydrostatic Column Heights: h = (P - Patm) / (rho * g)
    val atmPa = 101_325f
    val g = 9.81f
    val h1M = if (currentPreset.isLiquid) ((p1Pa - atmPa) / (fluidDensityKgM3 * g)).coerceIn(0.05f, 2.5f) else (p1Pa / 100_000f) * 0.45f
    val h2M = if (currentPreset.isLiquid) ((throatPressurePa - atmPa) / (fluidDensityKgM3 * g)).coerceIn(0.01f, 2.5f) else (throatPressurePa / 100_000f) * 0.45f
    val h3M = if (currentPreset.isLiquid) ((p3Pa - atmPa) / (fluidDensityKgM3 * g)).coerceIn(0.04f, 2.5f) else (p3Pa / 100_000f) * 0.45f
    val deltaHM = (h1M - h2M).coerceAtLeast(0f)

    // Animation / Integration Loop
    val infiniteTransition = rememberInfiniteTransition()
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRunning) 2000 else 999999, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(isRunning, inletVelocityMs, throatRatio) {
        if (!isRunning) return@LaunchedEffect
        var lastTime = withFrameNanos { it }

        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            lastTime = now

            // Step particles through tube
            for (i in particles.indices) {
                val p = particles[i]
                // Local speed factor as function of x: 1.0 at ends, areaRatio at throat (x ~ 0.50)
                val distFromThroat = abs(p.normX - 0.50f)
                val localAreaRatio = if (distFromThroat < 0.15f) {
                    val throatFactor = (1f - (distFromThroat / 0.15f)).coerceIn(0f, 1f)
                    1f + (areaRatio - 1f) * (throatFactor * throatFactor)
                } else if (distFromThroat < 0.35f) {
                    val taperFactor = ((0.35f - distFromThroat) / 0.20f).coerceIn(0f, 1f)
                    1f + (areaRatio - 1f) * 0.15f * taperFactor
                } else {
                    1f
                }

                val speedNorm = (inletVelocityMs * 0.25f) * localAreaRatio
                p.normX += speedNorm * dt
                if (p.normX > 1.05f) {
                    p.normX = -0.05f
                    p.normYOffset = (Random.nextFloat() * 2f - 1f) * 0.85f
                }
            }

            // Step cavitation bubbles
            if (isCavitationActive) {
                if (cavitationBubbles.size < 35 && Random.nextFloat() < 0.45f) {
                    cavitationBubbles.add(
                        VenturiParticle(
                            normX = 0.46f + Random.nextFloat() * 0.08f,
                            normYOffset = (Random.nextFloat() * 2f - 1f) * 0.75f,
                            color = Color.White.copy(alpha = 0.85f),
                            sizePx = Random.nextFloat() * 3.5f + 1.5f,
                            isCavitation = true
                        )
                    )
                }
                val it = cavitationBubbles.iterator()
                while (it.hasNext()) {
                    val b = it.next()
                    b.normX += (throatVelocityMs * 0.28f) * dt
                    if (b.normX > 0.72f || Random.nextFloat() < 0.04f) {
                        it.remove() // Vapor bubble collapses in higher pressure diffuser!
                    }
                }
            } else {
                cavitationBubbles.clear()
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag to adjust constriction or flow rate. Observe dynamic pressure drop and suction!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Vertical drag tunes throat ratio
                            throatRatio = (throatRatio - dragAmount.y * 0.003f).coerceIn(0.25f, 0.85f)
                            // Horizontal drag tunes inlet flow velocity
                            inletVelocityMs = (inletVelocityMs + dragAmount.x * 0.05f).coerceIn(0.2f, 35f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val xNorm = offset.x / size.width
                            if (xNorm in 0.40f..0.60f) {
                                // Tap throat: pinch tighter
                                throatRatio = if (throatRatio > 0.40f) 0.32f else 0.55f
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated apparatus center: cy ~ 0.40h (Leaves bottom 35% for controls deck)
                val cy = h * 0.40f

                // 1. Fluid Pipe Dimensions & Coordinates
                val pipeStartX = w * 0.06f
                val pipeEndX = w * 0.94f
                val pipeLen = pipeEndX - pipeStartX

                // Pipe radius profiles: R1 at ends, R2 at throat
                val r1Px = min(h * 0.16f, 52f)
                val r2Px = r1Px * throatRatio.coerceIn(0.25f, 0.90f)

                val x1Inlet = pipeStartX + pipeLen * 0.22f
                val xThroat = pipeStartX + pipeLen * 0.50f
                val x3Outlet = pipeStartX + pipeLen * 0.78f

                val throatHalfLen = pipeLen * 0.08f
                val convStart = pipeStartX + pipeLen * 0.28f
                val convEnd = xThroat - throatHalfLen
                val diffStart = xThroat + throatHalfLen
                val diffEnd = pipeStartX + pipeLen * 0.72f

                // Helper to get half-height of tube at any x
                fun getTubeRadiusAt(x: Float): Float {
                    return when {
                        x <= convStart -> r1Px
                        x in convStart..convEnd -> {
                            val t = (x - convStart) / (convEnd - convStart)
                            // Smooth cosine taper
                            val smoothT = (1f - cos(t * PI.toFloat())) * 0.5f
                            r1Px + (r2Px - r1Px) * smoothT
                        }
                        x in convEnd..diffStart -> r2Px
                        x in diffStart..diffEnd -> {
                            val t = (x - diffStart) / (diffEnd - diffStart)
                            val smoothT = (1f - cos(t * PI.toFloat())) * 0.5f
                            r2Px + (r1Px - r2Px) * smoothT
                        }
                        else -> r1Px
                    }
                }

                // 2. Draw Subtle Scientific Grid
                val gridSpacing = 36.dp.toPx()
                var gx = 0f
                while (gx < w) {
                    drawLine(ScienceBorder.copy(alpha = 0.2f), Offset(gx, 0f), Offset(gx, h), 0.8f)
                    gx += gridSpacing
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(ScienceBorder.copy(alpha = 0.2f), Offset(0f, gy), Offset(w, gy), 0.8f)
                    gy += gridSpacing
                }

                // 3. Fluid Body Fill & Dynamic Pressure Gradient
                val numSegments = 80
                val tubeTopPath = Path()
                val tubeBottomPath = Path()
                val fluidPolyPath = Path()

                for (i in 0..numSegments) {
                    val t = i / numSegments.toFloat()
                    val x = pipeStartX + t * pipeLen
                    val r = getTubeRadiusAt(x)
                    val yTop = cy - r
                    val yBottom = cy + r

                    if (i == 0) {
                        tubeTopPath.moveTo(x, yTop)
                        tubeBottomPath.moveTo(x, yBottom)
                        fluidPolyPath.moveTo(x, yTop)
                    } else {
                        tubeTopPath.lineTo(x, yTop)
                        tubeBottomPath.lineTo(x, yBottom)
                        fluidPolyPath.lineTo(x, yTop)
                    }
                }
                // Return along bottom for complete polygon
                for (i in numSegments downTo 0) {
                    val t = i / numSegments.toFloat()
                    val x = pipeStartX + t * pipeLen
                    val r = getTubeRadiusAt(x)
                    fluidPolyPath.lineTo(x, cy + r)
                }
                fluidPolyPath.close()

                // Fluid background fill with horizontal pressure gradient
                val fluidColors = if (showPressureHeatmap) {
                    listOf(
                        Color(0xFF0284C7).copy(alpha = 0.35f), // High pressure, low speed (Inlet)
                        Color(0xFF0369A1).copy(alpha = 0.40f),
                        Color(0xFF6366F1).copy(alpha = 0.45f), // Accelerating
                        Color(0xFFA855F7).copy(alpha = 0.60f), // Throat: Ultra-low pressure, maximum kinetic energy
                        Color(0xFF6366F1).copy(alpha = 0.45f),
                        Color(0xFF0284C7).copy(alpha = 0.35f)  // Outlet: Pressure recovery
                    )
                } else {
                    listOf(
                        CyanNeon.copy(alpha = 0.25f),
                        CyanNeon.copy(alpha = 0.35f),
                        CyanNeon.copy(alpha = 0.25f)
                    )
                }

                drawPath(
                    path = fluidPolyPath,
                    brush = Brush.horizontalGradient(
                        colors = fluidColors,
                        startX = pipeStartX,
                        endX = pipeEndX
                    )
                )

                // 4. Streamlines (Crowding together in throat)
                if (showStreamlines) {
                    val numStreamlines = 5
                    for (s in 0 until numStreamlines) {
                        val normOffset = (s / (numStreamlines - 1).toFloat()) * 1.6f - 0.8f
                        val streamPath = Path()
                        for (i in 0..numSegments) {
                            val t = i / numSegments.toFloat()
                            val x = pipeStartX + t * pipeLen
                            val r = getTubeRadiusAt(x)
                            val y = cy + normOffset * r
                            if (i == 0) streamPath.moveTo(x, y) else streamPath.lineTo(x, y)
                        }
                        val streamColor = if (abs(normOffset) < 0.2f) AmberVibrant else CyanNeon
                        drawPath(
                            path = streamPath,
                            color = streamColor.copy(alpha = 0.40f),
                            style = Stroke(
                                width = 1.4f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), pulsePhase * 8f)
                            )
                        )
                    }
                }

                // 5. Tracer Particles moving through tube
                for (p in particles) {
                    val px = pipeStartX + p.normX * pipeLen
                    if (px in pipeStartX..pipeEndX) {
                        val r = getTubeRadiusAt(px)
                        val py = cy + p.normYOffset * r
                        drawCircle(p.color, p.sizePx, Offset(px, py))
                        // Specular glow
                        drawCircle(p.color.copy(alpha = 0.3f), p.sizePx * 2f, Offset(px, py))
                    }
                }

                // 6. Cavitation Bubbles in Throat & Diffuser
                if (isCavitationActive) {
                    for (b in cavitationBubbles) {
                        val bx = pipeStartX + b.normX * pipeLen
                        val r = getTubeRadiusAt(bx)
                        val by = cy + b.normYOffset * r
                        drawCircle(Color.White, b.sizePx, Offset(bx, by))
                        drawCircle(CyanNeon.copy(alpha = 0.6f), b.sizePx * 1.6f, Offset(bx, by), style = Stroke(0.8f))
                    }
                    // Cavitation mist cloud at throat
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.35f), PurpleNeon.copy(alpha = 0.20f), Color.Transparent),
                            center = Offset(xThroat, cy),
                            radius = r2Px * 1.2f
                        ),
                        topLeft = Offset(xThroat - throatHalfLen, cy - r2Px),
                        size = Size(throatHalfLen * 2f, r2Px * 2f)
                    )
                }

                // 7. Carburetor / Atomizer Bottom Suction Tube
                if (currentPreset.hasSuctionTube) {
                    val tubeW = 8f
                    val riserBottomY = cy + r1Px + 55f
                    // Glass suction tube
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF334155), Color(0xFF64748B), Color(0xFF334155)),
                            startX = xThroat - tubeW * 0.5f,
                            endX = xThroat + tubeW * 0.5f
                        ),
                        topLeft = Offset(xThroat - tubeW * 0.5f, cy + r2Px),
                        size = Size(tubeW, riserBottomY - (cy + r2Px))
                    )
                    // Rising fuel/perfume column inside suction tube
                    val fuelRiseY = cy + r2Px + 4f
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(AmberVibrant, CoralNeon),
                            startY = fuelRiseY,
                            endY = riserBottomY
                        ),
                        topLeft = Offset(xThroat - tubeW * 0.5f + 1.5f, fuelRiseY),
                        size = Size(tubeW - 3f, riserBottomY - fuelRiseY)
                    )
                    // Fuel reservoir beaker
                    val beakerW = 44f
                    val beakerH = 30f
                    drawRoundRect(
                        color = ScienceBorder.copy(alpha = 0.65f),
                        topLeft = Offset(xThroat - beakerW * 0.5f, riserBottomY),
                        size = Size(beakerW, beakerH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                        style = Stroke(1.5f)
                    )
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(AmberVibrant.copy(alpha = 0.6f), CoralNeon.copy(alpha = 0.8f)),
                            startY = riserBottomY + 6f,
                            endY = riserBottomY + beakerH
                        ),
                        topLeft = Offset(xThroat - beakerW * 0.5f + 2f, riserBottomY + 6f),
                        size = Size(beakerW - 4f, beakerH - 8f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                    )
                    // Atomized droplet spray fan entering throat
                    for (k in 0..6) {
                        val sprayAngle = (-60f + k * 18f) * (PI.toFloat() / 180f)
                        val sprayDist = 12f + (k % 3) * 6f
                        val sx = xThroat + cos(sprayAngle) * sprayDist + 6f
                        val sy = (cy + r2Px - 2f) + sin(sprayAngle) * 8f
                        drawCircle(AmberVibrant, 2f, Offset(sx, sy))
                    }
                }

                // 8. Vertical Manometer Piezometer Columns (P1, P2, P3)
                if (showManometers) {
                    val manoW = 14f
                    val manoMaxH = h * 0.26f
                    val manoTopY = cy - r1Px - manoMaxH

                    val manometers = listOf(
                        Triple(x1Inlet, h1M, "P₁ (Inlet)"),
                        Triple(xThroat, h2M, "P₂ (Throat)"),
                        Triple(x3Outlet, h3M, "P₃ (Outlet)")
                    )

                    manometers.forEach { (mx, hVal, label) ->
                        val tubeBottom = cy - getTubeRadiusAt(mx)
                        // Glass tube outline
                        drawRect(
                            color = Color.White.copy(alpha = 0.45f),
                            topLeft = Offset(mx - manoW * 0.5f, manoTopY),
                            size = Size(manoW, tubeBottom - manoTopY),
                            style = Stroke(1.4f)
                        )

                        // Liquid column height
                        val normH = (hVal / 1.6f).coerceIn(0.08f, 0.95f)
                        val liquidColH = (tubeBottom - manoTopY) * normH
                        val liquidTopY = tubeBottom - liquidColH

                        // Meniscus fill
                        val colColor = if (mx == xThroat) AmberVibrant else CyanNeon
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(colColor.copy(alpha = 0.85f), colColor.copy(alpha = 0.50f)),
                                startY = liquidTopY,
                                endY = tubeBottom
                            ),
                            topLeft = Offset(mx - manoW * 0.5f + 1.5f, liquidTopY),
                            size = Size(manoW - 3f, liquidColH)
                        )
                        // Curved meniscus surface
                        drawOval(
                            color = Color.White,
                            topLeft = Offset(mx - manoW * 0.5f + 2f, liquidTopY - 2f),
                            size = Size(manoW - 4f, 4f)
                        )

                        // Measurement tick marks
                        for (m in 1..4) {
                            val my = tubeBottom - (tubeBottom - manoTopY) * (m * 0.20f)
                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = Offset(mx - manoW * 0.5f - 4f, my),
                                end = Offset(mx - manoW * 0.5f, my),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // Delta h differential marker line between P1 and P2
                    val h1LiquidTopY = (cy - r1Px) - (cy - r1Px - manoTopY) * (h1M / 1.6f).coerceIn(0.08f, 0.95f)
                    val h2LiquidTopY = (cy - r2Px) - (cy - r2Px - manoTopY) * (h2M / 1.6f).coerceIn(0.08f, 0.95f)
                    val diffDash = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

                    drawLine(CoralNeon.copy(alpha = 0.7f), Offset(x1Inlet, h1LiquidTopY), Offset(xThroat + 25f, h1LiquidTopY), 1.2f, pathEffect = diffDash)
                    drawLine(CoralNeon.copy(alpha = 0.7f), Offset(xThroat, h2LiquidTopY), Offset(xThroat + 25f, h2LiquidTopY), 1.2f, pathEffect = diffDash)

                    // Vertical Δh double arrow
                    val arrowX = xThroat + 20f
                    if (abs(h2LiquidTopY - h1LiquidTopY) > 8f) {
                        drawLine(CoralNeon, Offset(arrowX, h1LiquidTopY), Offset(arrowX, h2LiquidTopY), 1.8f)
                        drawCircle(CoralNeon, 2.5f, Offset(arrowX, h1LiquidTopY))
                        drawCircle(CoralNeon, 2.5f, Offset(arrowX, h2LiquidTopY))
                    }
                }

                // 9. Venturi Tube Structural Wall Outlines
                drawPath(
                    path = tubeTopPath,
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(width = 2.4f, cap = StrokeCap.Round)
                )
                drawPath(
                    path = tubeBottomPath,
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(width = 2.4f, cap = StrokeCap.Round)
                )

                // Flange collars at tube entry and exit
                val flangeH = r1Px * 2.3f
                drawRoundRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(pipeStartX - 5f, cy - flangeH * 0.5f),
                    size = Size(8f, flangeH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
                drawRoundRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(pipeEndX - 3f, cy - flangeH * 0.5f),
                    size = Size(8f, flangeH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )

                // 10. Velocity Vectors (Short at inlet, long at throat)
                if (showVelocityVectors) {
                    val vecY = cy
                    // Inlet vector
                    val v1Len = (inletVelocityMs * 8f).coerceIn(16f, 45f)
                    drawLine(CyanNeon, Offset(x1Inlet - v1Len * 0.5f, vecY), Offset(x1Inlet + v1Len * 0.5f, vecY), 2.4f, StrokeCap.Round)
                    val arrHead1 = Path().apply {
                        moveTo(x1Inlet + v1Len * 0.5f + 3f, vecY)
                        lineTo(x1Inlet + v1Len * 0.5f - 4f, vecY - 3.5f)
                        lineTo(x1Inlet + v1Len * 0.5f - 4f, vecY + 3.5f)
                        close()
                    }
                    drawPath(arrHead1, CyanNeon)

                    // Throat vector (Much longer!)
                    val v2Len = (throatVelocityMs * 8f).coerceIn(32f, 95f)
                    drawLine(AmberVibrant, Offset(xThroat - v2Len * 0.5f, vecY), Offset(xThroat + v2Len * 0.5f, vecY), 2.8f, StrokeCap.Round)
                    val arrHead2 = Path().apply {
                        moveTo(xThroat + v2Len * 0.5f + 4f, vecY)
                        lineTo(xThroat + v2Len * 0.5f - 5f, vecY - 4f)
                        lineTo(xThroat + v2Len * 0.5f - 5f, vecY + 4f)
                        close()
                    }
                    drawPath(arrHead2, AmberVibrant)
                }
            }
        },
        hudContent = {
            // Rule: Transparent HUD card so canvas and flow shine through!
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 39: Venturi Tube & Pressure Drop",
                items = listOf(
                    "Continuity Law" to "A₁·v₁ = A₂·v₂ (${round(areaRatio * 10f) / 10f}x)",
                    "Inlet Velocity v₁" to "${round(inletVelocityMs * 10f) / 10f} m/s",
                    "Throat Velocity v₂" to "${round(throatVelocityMs * 10f) / 10f} m/s",
                    "Inlet Pressure P₁" to "${round(inletPressureKPa * 10f) / 10f} kPa",
                    "Throat Pressure P₂" to "${round(throatPressureKPa * 10f) / 10f} kPa",
                    "Bernoulli Drop ΔP" to "${round(deltaPKPa * 10f) / 10f} kPa",
                    "Manometer Head Δh" to "${round(deltaHM * 100f) / 100f} m",
                    "Volume Flow Q" to "${round(volumeFlowRateLMin * 10f) / 10f} L/min"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(VenturiPreset.values()) { preset ->
                        val isSelected = currentPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { applyPreset(preset) },
                            label = {
                                Text(
                                    text = preset.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.22f),
                                selectedLabelColor = CyanNeon,
                                containerColor = ScienceDarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = ScienceBorder,
                                selectedBorderColor = CyanNeon
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                // Slider Pair 1: Inlet Velocity v1 & Throat Constriction Ratio D2/D1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Inlet Velocity v₁",
                        value = inletVelocityMs,
                        range = 0.5f..30f,
                        valueDisplay = "${round(inletVelocityMs * 10f) / 10f} m/s",
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { inletVelocityMs = it }
                    )
                    PhysicsSliderControl(
                        title = "Throat Ratio D₂/D₁",
                        value = throatRatio,
                        range = 0.25f..0.85f,
                        valueDisplay = "${round(throatRatio * 100f) / 100f}",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = { throatRatio = it }
                    )
                }

                // Slider Pair 2: Inlet Pressure P1 & Fluid Density rho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Inlet Pressure P₁",
                        value = inletPressureKPa,
                        range = 90f..220f,
                        valueDisplay = "${round(inletPressureKPa * 10f) / 10f} kPa",
                        accentColor = EmeraldNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { inletPressureKPa = it }
                    )
                    PhysicsSliderControl(
                        title = "Density ρ",
                        value = fluidDensityKgM3,
                        range = 1f..1200f,
                        valueDisplay = "${round(fluidDensityKgM3)} kg/m³",
                        accentColor = PurpleNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { fluidDensityKgM3 = it }
                    )
                }

                // Control Action Toggles & Reset Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play / Pause Toggle
                    Button(
                        onClick = { isRunning = !isRunning },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberVibrant.copy(alpha = 0.25f) else CyanNeon.copy(alpha = 0.25f),
                            contentColor = if (isRunning) AmberVibrant else CyanNeon
                        ),
                        border = BorderStroke(1.dp, if (isRunning) AmberVibrant else CyanNeon),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1f)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run Flow",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Streamlines Toggle
                    FilledTonalButton(
                        onClick = { showStreamlines = !showStreamlines },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (showStreamlines) CyanNeon.copy(alpha = 0.20f) else ScienceDarkSurfaceVariant,
                            contentColor = if (showStreamlines) CyanNeon else TextSecondary
                        ),
                        border = BorderStroke(1.dp, if (showStreamlines) CyanNeon.copy(alpha = 0.6f) else ScienceBorder),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1f)
                    ) {
                        Text(
                            text = if (showStreamlines) "〰 Lines ON" else "〰 Lines OFF",
                            fontSize = 10.sp
                        )
                    }

                    // Manometer Toggle
                    FilledTonalButton(
                        onClick = { showManometers = !showManometers },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (showManometers) AmberVibrant.copy(alpha = 0.20f) else ScienceDarkSurfaceVariant,
                            contentColor = if (showManometers) AmberVibrant else TextSecondary
                        ),
                        border = BorderStroke(1.dp, if (showManometers) AmberVibrant.copy(alpha = 0.6f) else ScienceBorder),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1f)
                    ) {
                        Text(
                            text = if (showManometers) "📊 Manometer" else "📊 Hidden",
                            fontSize = 10.sp
                        )
                    }

                    // Reset Button
                    IconButton(
                        onClick = { applyPreset(currentPreset) },
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
