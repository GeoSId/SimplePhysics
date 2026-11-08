package com.geosid.simplephysics.ui.experiments.week7.Day45

import androidx.compose.animation.core.*
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
 * Physical scenarios / presets for the Leidenfrost effect.
 */
enum class LeidenfrostPreset(
    val label: String,
    val title: String,
    val surfaceTempC: Float,
    val dropletRadiusMm: Float,
    val panMaterial: String,
    val liquidName: String,
    val desc: String
) {
    LEIDENFROST_WATER(
        label = "True Leidenfrost",
        title = "Stable Film Boiling Hover (T > 220°C)",
        surfaceTempC = 260.0f,
        dropletRadiusMm = 3.2f,
        panMaterial = "Cast Iron Skillet",
        liquidName = "Water (100°C)",
        desc = "Surface temperature exceeds the Leidenfrost point (~193°C): continuous insulating vapor blanket supports droplet, cutting heat flux by 90%"
    ),
    TRANSITION_BOILING(
        label = "Violent Sizzle",
        title = "Transition Boiling Regime (T ≈ 150°C)",
        surfaceTempC = 150.0f,
        dropletRadiusMm = 3.2f,
        panMaterial = "Stainless Steel",
        liquidName = "Water (Boiling)",
        desc = "Below the Leidenfrost threshold: intermittent liquid-solid contact causes violent explosive nucleate boiling, hissing, and rapid flash evaporation"
    ),
    NITROGEN_FLOOR(
        label = "Liquid N₂ Floor",
        title = "Cryogenic Inverted Leidenfrost",
        surfaceTempC = 20.0f, // Room temp floor is ~216K above LN2 boiling point (77K)
        dropletRadiusMm = 4.0f,
        panMaterial = "Room Temp Tile",
        liquidName = "Liquid Nitrogen (77 K)",
        desc = "Spilled cryogenic nitrogen skates frictionlessly across room-temperature floors on an instant cushion of gaseous nitrogen vapor"
    ),
    ETHANOL_PAN(
        label = "Ethanol Drop",
        title = "Low Boiling-Point Volatile Fluid",
        surfaceTempC = 220.0f,
        dropletRadiusMm = 2.8f,
        panMaterial = "Polished Copper",
        liquidName = "Ethanol (Boils @ 78°C)",
        desc = "Ethanol exhibits a lower Leidenfrost point (~160°C); rapid vapor production drives erratic droplet self-propulsion and oscillations"
    )
}

/**
 * Escaping steam / vapor puff particle.
 */
private class SteamParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    val size: Float
)

@Composable
fun LeidenfrostEffectExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(LeidenfrostPreset.LEIDENFROST_WATER) }

    // Physical Parameters
    var surfaceTempC by remember { mutableStateOf(currentPreset.surfaceTempC) }
    var dropletRadiusMm by remember { mutableStateOf(currentPreset.dropletRadiusMm) }
    var substrateTiltDeg by remember { mutableStateOf(0.0f) }

    // Display Toggles
    var showVaporFilm by remember { mutableStateOf(true) }
    var showHeatFluxCurve by remember { mutableStateOf(true) }
    var showForceVectors by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(true) }

    fun applyPreset(preset: LeidenfrostPreset) {
        currentPreset = preset
        surfaceTempC = preset.surfaceTempC
        dropletRadiusMm = preset.dropletRadiusMm
        substrateTiltDeg = 0.0f
    }

    // Physical Constants & Leidenfrost Thermodynamics
    val tLeidenfrostC = 193.0f // Leidenfrost point for water on polished metal
    val tBoilingC = 100.0f
    val isLeidenfrost = surfaceTempC >= tLeidenfrostC
    val isNucleateBoiling = surfaceTempC in (tBoilingC..tLeidenfrostC)
    val isSubcooled = surfaceTempC < tBoilingC

    // Vapor Film Thickness (δ): δ ≈ [ (k_v · μ_v · (T_s - T_sat)) / (ρ_v · (ρ_l - ρ_v) · g · h_fg) ]^(1/4)
    // Scales roughly with (T_s - T_sat)^(1/3)
    val deltaTemp = (surfaceTempC - tBoilingC).coerceAtLeast(0f)
    val vaporThicknessUm = if (isLeidenfrost) {
        (40f + 6f * deltaTemp.pow(0.35f)).coerceIn(50f, 160f)
    } else if (isNucleateBoiling) {
        (40f * (surfaceTempC - tBoilingC) / (tLeidenfrostC - tBoilingC)).coerceIn(0f, 40f)
    } else {
        0.0f
    }

    // Heat Flux q" (Nukiyama curve):
    // Spikes dramatically at critical heat flux (CHF ≈ 130-150°C, nucleate), then plummets at Leidenfrost point
    val heatFluxKWm2 = when {
        isSubcooled -> 5f * (surfaceTempC / 100f)
        isNucleateBoiling -> {
            // Rapid spike to Critical Heat Flux (CHF ~ 1200 kW/m^2)
            val frac = (surfaceTempC - tBoilingC) / (tLeidenfrostC - tBoilingC)
            val peak = sin(frac * PI.toFloat())
            150f + 1100f * peak
        }
        else -> {
            // Film boiling: thermal conduction through vapor film q = k_v * ΔT / δ
            // k_v ≈ 0.035 W/(m·K), δ ≈ 100 µm -> q ≈ 0.035 * 160 / 100e-6 ≈ 56 kW/m^2
            val k_vapor = 0.035f // W/(m·K)
            val q = (k_vapor * deltaTemp / (max(vaporThicknessUm, 10f) * 1e-6f)) * 1e-3f // kW/m^2
            q.coerceIn(40f, 280f)
        }
    }

    // Droplet Evaporation Lifetime (Leidenfrost droplet lives ~10-20x longer than sizzling droplet)
    val dropletMassMg = (4f / 3f) * PI.toFloat() * (dropletRadiusMm * 1e-3f).pow(3) * 1000f * 1e6f
    val evaporationLifetimeSec = if (isLeidenfrost) {
        (dropletMassMg * 0.12f).coerceIn(15f, 180f)
    } else if (isNucleateBoiling) {
        (dropletMassMg * 0.012f).coerceIn(1.5f, 15f)
    } else {
        (dropletMassMg * 0.8f).coerceIn(100f, 600f)
    }

    // Droplet Kinematics on Substrate
    var dropletPosX by remember { mutableStateOf(0.48f) }
    var dropletVelX by remember { mutableStateOf(0.0f) }
    var dropletWobblePhase by remember { mutableStateOf(0.0f) }

    // Steam Puffs Particle Field
    val steamParticles = remember { mutableStateListOf<SteamParticle>() }

    // High-Precision Physics Step Loop
    LaunchedEffect(surfaceTempC, dropletRadiusMm, substrateTiltDeg, isRunning) {
        if (!isRunning) return@LaunchedEffect
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.035f)
            lastTime = now

            // Droplet shape oscillation / wobble (Rayleigh droplet frequency)
            dropletWobblePhase = (dropletWobblePhase + dt * (if (isLeidenfrost) 14f else 8f)) % (2 * PI.toFloat())

            // Acceleration along inclined substrate
            // Tilt gravity component: g * sin(theta)
            val gAlongPan = 9.81f * sin(substrateTiltDeg * PI.toFloat() / 180f)
            // Friction coefficient: ~0.001 on vapor cushion, ~0.15 on wet metal
            val frictionMu = if (isLeidenfrost) 0.002f else 0.12f

            // Aerodynamic self-propulsion / stochastic vapor venting kick
            val vaporVentingKick = if (isLeidenfrost) {
                (Random.nextFloat() - 0.5f) * 0.25f
            } else if (isNucleateBoiling) {
                (Random.nextFloat() - 0.5f) * 1.2f // Violent sputtering
            } else 0f

            val totalAccel = gAlongPan * 0.45f + vaporVentingKick - (dropletVelX * frictionMu * 30f)
            dropletVelX = (dropletVelX + totalAccel * dt).coerceIn(-1.5f, 1.5f)

            // Update droplet position
            dropletPosX = (dropletPosX + dropletVelX * dt * 0.25f).coerceIn(0.18f, 0.78f)
            if (dropletPosX <= 0.185f || dropletPosX >= 0.775f) {
                dropletVelX = -dropletVelX * (if (isLeidenfrost) 0.85f else 0.30f)
            }

            // Spawn escaping vapor steam particles
            if (isNucleateBoiling || isLeidenfrost) {
                val spawnCount = if (isNucleateBoiling) 3 else 1
                for (s in 0 until spawnCount) {
                    val pSpeedX = (Random.nextFloat() - 0.5f) * 60f + (dropletVelX * 20f)
                    val pSpeedY = -(Random.nextFloat() * 80f + 40f)
                    steamParticles.add(
                        SteamParticle(
                            x = dropletPosX + (Random.nextFloat() - 0.5f) * 0.03f,
                            y = 0.58f,
                            vx = pSpeedX,
                            vy = pSpeedY,
                            alpha = if (isNucleateBoiling) 0.85f else 0.45f,
                            size = Random.nextFloat() * 4.5f + 3f
                        )
                    )
                }
            }

            // Update steam particles
            val iter = steamParticles.iterator()
            while (iter.hasNext()) {
                val sp = iter.next()
                sp.x += (sp.vx / 400f) * dt
                sp.y += (sp.vy / 400f) * dt
                sp.alpha -= dt * 1.5f
                if (sp.alpha <= 0.02f || sp.y < 0.25f) {
                    iter.remove()
                }
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag droplet or tilt the pan. Observe the Leidenfrost vapor cushion, thermal insulation, and heat flux drop!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            dropletPosX = (dropletPosX + dragAmount.x / size.width).coerceIn(0.18f, 0.78f)
                            dropletVelX = dragAmount.x * 0.08f
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            dropletPosX = (offset.x / size.width).coerceIn(0.18f, 0.78f)
                            dropletVelX = 0f
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated Canvas Layout: Apparatus center cy ≈ 0.42f * h
                val panBaseY = h * 0.58f
                val panLeftX = w * 0.10f
                val panRightX = w * 0.88f
                val panWidth = panRightX - panLeftX

                // Tilt transform geometry
                val tiltRad = substrateTiltDeg * (PI.toFloat() / 180f)
                val panMidX = (panLeftX + panRightX) / 2f

                // 1. Scientific Coordinate Grid (Subtle)
                val gridSpacing = 36.dp.toPx()
                var gx = 0f
                while (gx < w) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.18f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, h),
                        strokeWidth = 0.7f
                    )
                    gx += gridSpacing
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.18f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 0.7f
                    )
                    gy += gridSpacing
                }

                // 2. Heated Metal Pan / Substrate
                val panTiltYLeft = panBaseY - sin(tiltRad) * (panMidX - panLeftX)
                val panTiltYRight = panBaseY + sin(tiltRad) * (panRightX - panMidX)

                // Pan Heat Glow (Color ramps from metallic gray to fiery orange-red above 250°C)
                val heatFraction = (surfaceTempC / 320f).coerceIn(0f, 1f)
                val panSurfaceColor = lerp(Color(0xFF64748B), CoralNeon, heatFraction)
                val panGlowColor = CoralNeon.copy(alpha = 0.25f * heatFraction)

                // Substrate Surface Heat Glow Aura
                drawLine(
                    color = panGlowColor,
                    start = Offset(panLeftX, panTiltYLeft),
                    end = Offset(panRightX, panTiltYRight),
                    strokeWidth = 16.dp.toPx()
                )

                // Metal Pan Plate
                val panPath = Path().apply {
                    moveTo(panLeftX, panTiltYLeft)
                    lineTo(panRightX, panTiltYRight)
                    lineTo(panRightX, panTiltYRight + 16.dp.toPx())
                    lineTo(panLeftX, panTiltYLeft + 16.dp.toPx())
                    close()
                }
                drawPath(
                    path = panPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(panSurfaceColor, Color(0xFF1E293B)),
                        startY = panBaseY - 10f,
                        endY = panBaseY + 20f
                    )
                )
                // Pan Top Surface Rim
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(panLeftX, panTiltYLeft),
                    end = Offset(panRightX, panTiltYRight),
                    strokeWidth = 2.4f
                )

                // 3. Droplet Position & Geometry
                val dropPxX = panLeftX + (dropletPosX - 0.10f) / 0.78f * panWidth
                val dropBasePanY = panBaseY + sin(tiltRad) * (dropPxX - panMidX)
                val vaporGapPx = if (isLeidenfrost) (vaporThicknessUm * 0.12f).dp.toPx().coerceIn(6.dp.toPx(), 18.dp.toPx()) else 0f
                val dropCenterY = dropBasePanY - vaporGapPx - (dropletRadiusMm * 4.2f).dp.toPx()

                // Droplet shape: Flattened oblate spheroid with surface tension wobble
                val wobble = sin(dropletWobblePhase) * 0.08f
                val radiusXPx = (dropletRadiusMm * 4.8f * (1f + wobble)).dp.toPx()
                val radiusYPx = (dropletRadiusMm * 3.8f * (1f - wobble)).dp.toPx()

                // 4. Insulating Vapor Film Blanket (Microscopic steam layer)
                if (showVaporFilm && isLeidenfrost) {
                    val filmWidth = radiusXPx * 1.8f
                    val filmPath = Path().apply {
                        val filmTopY = dropBasePanY - vaporGapPx
                        val filmBotY = dropBasePanY
                        moveTo(dropPxX - filmWidth * 0.5f, filmBotY)
                        quadraticTo(dropPxX, filmTopY - 2f, dropPxX + filmWidth * 0.5f, filmBotY)
                        close()
                    }
                    // Glowing Cyan-White Vapor Film
                    drawPath(
                        path = filmPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(CyanNeon.copy(alpha = 0.55f), Color.White.copy(alpha = 0.25f)),
                            startY = dropBasePanY - vaporGapPx,
                            endY = dropBasePanY
                        )
                    )
                    // Vapor pressure arrows under droplet
                    for (i in -2..2) {
                        val vx = dropPxX + i * (filmWidth * 0.18f)
                        val vy = dropBasePanY - 2f
                        drawLine(
                            color = CyanNeon,
                            start = Offset(vx, vy),
                            end = Offset(vx, vy - vaporGapPx * 0.8f),
                            strokeWidth = 1.4f
                        )
                    }
                }

                // 5. Escaping Steam Particles (Rising Puffs)
                steamParticles.forEach { sp ->
                    val spx = sp.x * w
                    val spy = sp.y * h
                    drawCircle(
                        color = Color.White.copy(alpha = sp.alpha),
                        radius = sp.size,
                        center = Offset(spx, spy)
                    )
                }

                // 6. Water Droplet (Fluid body with refraction highlight & wetting/hovering profile)
                if (!isLeidenfrost && isNucleateBoiling) {
                    // Sizzling, sputtering, semi-flattened boiling drop
                    val sputterWobble = (Random.nextFloat() - 0.5f) * 4f
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF7DD3FC), CyanNeon, Color(0xFF0284C7)),
                            center = Offset(dropPxX - radiusXPx * 0.25f, dropCenterY - radiusYPx * 0.25f),
                            radius = radiusXPx
                        ),
                        topLeft = Offset(dropPxX - radiusXPx + sputterWobble, dropCenterY - radiusYPx * 0.8f),
                        size = Size(radiusXPx * 2f, radiusYPx * 1.6f)
                    )
                } else if (isSubcooled) {
                    // Puddle wetting contact with pan
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFBAE6FD), CyanNeon),
                            center = Offset(dropPxX, dropCenterY),
                            radius = radiusXPx * 1.2f
                        ),
                        topLeft = Offset(dropPxX - radiusXPx * 1.3f, dropBasePanY - radiusYPx * 0.8f),
                        size = Size(radiusXPx * 2.6f, radiusYPx * 0.8f)
                    )
                } else {
                    // True Leidenfrost: Perfect non-wetting hovering sphere / oblate bead
                    // Outer droplet glow
                    drawOval(
                        color = CyanNeon.copy(alpha = 0.25f),
                        topLeft = Offset(dropPxX - radiusXPx - 3.dp.toPx(), dropCenterY - radiusYPx - 3.dp.toPx()),
                        size = Size((radiusXPx + 3.dp.toPx()) * 2f, (radiusYPx + 3.dp.toPx()) * 2f)
                    )
                    // Liquid core with specular highlight
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0369A1)),
                            center = Offset(dropPxX - radiusXPx * 0.30f, dropCenterY - radiusYPx * 0.30f),
                            radius = radiusXPx
                        ),
                        topLeft = Offset(dropPxX - radiusXPx, dropCenterY - radiusYPx),
                        size = Size(radiusXPx * 2f, radiusYPx * 2f)
                    )
                    // Glassy Specular Highlight Arc
                    drawArc(
                        color = Color.White.copy(alpha = 0.75f),
                        startAngle = 200f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(dropPxX - radiusXPx * 0.75f, dropCenterY - radiusYPx * 0.75f),
                        size = Size(radiusXPx * 1.5f, radiusYPx * 1.5f),
                        style = Stroke(width = 2.0f, cap = StrokeCap.Round)
                    )
                }

                // 7. Physical Force Vectors
                if (showForceVectors && isLeidenfrost) {
                    // Vapor Lift Force (Upward green arrow)
                    val liftLen = 28.dp.toPx()
                    drawLine(
                        color = EmeraldNeon,
                        start = Offset(dropPxX, dropCenterY),
                        end = Offset(dropPxX, dropCenterY - liftLen),
                        strokeWidth = 2.4f,
                        cap = StrokeCap.Round
                    )
                    val lHead = Path().apply {
                        moveTo(dropPxX, dropCenterY - liftLen - 4f)
                        lineTo(dropPxX - 4f, dropCenterY - liftLen + 4f)
                        lineTo(dropPxX + 4f, dropCenterY - liftLen + 4f)
                        close()
                    }
                    drawPath(lHead, EmeraldNeon)

                    // Gravity Force (Downward coral arrow)
                    drawLine(
                        color = CoralNeon,
                        start = Offset(dropPxX, dropCenterY),
                        end = Offset(dropPxX, dropCenterY + liftLen),
                        strokeWidth = 2.4f,
                        cap = StrokeCap.Round
                    )
                    val gHead = Path().apply {
                        moveTo(dropPxX, dropCenterY + liftLen + 4f)
                        lineTo(dropPxX - 4f, dropCenterY + liftLen - 4f)
                        lineTo(dropPxX + 4f, dropCenterY + liftLen - 4f)
                        close()
                    }
                    drawPath(gHead, CoralNeon)
                }

                // 8. Mini Nukiyama Boiling Heat Flux Curve (Inset Card on Top Right)
                if (showHeatFluxCurve) {
                    val curveCardW = min(w * 0.38f, 180.dp.toPx())
                    val curveCardH = min(h * 0.28f, 110.dp.toPx())
                    val curveCardLeft = w * 0.58f
                    val curveCardTop = h * 0.08f

                    // Card background
                    drawRoundRect(
                        color = ScienceDarkSurface.copy(alpha = 0.88f),
                        topLeft = Offset(curveCardLeft, curveCardTop),
                        size = Size(curveCardW, curveCardH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                    drawRoundRect(
                        color = ScienceBorder.copy(alpha = 0.35f),
                        topLeft = Offset(curveCardLeft, curveCardTop),
                        size = Size(curveCardW, curveCardH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                        style = Stroke(1.0f)
                    )

                    // Curve Plot Axes
                    val plotOrigin = Offset(curveCardLeft + 18.dp.toPx(), curveCardTop + curveCardH - 14.dp.toPx())
                    val plotW = curveCardW - 28.dp.toPx()
                    val plotH = curveCardH - 24.dp.toPx()

                    drawLine(Color.White.copy(alpha = 0.6f), plotOrigin, Offset(plotOrigin.x + plotW, plotOrigin.y), 1.2f)
                    drawLine(Color.White.copy(alpha = 0.6f), plotOrigin, Offset(plotOrigin.x, plotOrigin.y - plotH), 1.2f)

                    // Nukiyama Curve: (1) Free convection -> (2) Nucleate boiling peak -> (3) Leidenfrost drop -> (4) Film boiling radiation
                    val nukiyamaPath = Path().apply {
                        val steps = 30
                        for (st in 0..steps) {
                            val temp = 80f + (st / steps.toFloat()) * 220f
                            val qVal = when {
                                temp < 100f -> 10f * (temp / 100f)
                                temp < 193f -> {
                                    val f = (temp - 100f) / 93f
                                    sin(f * PI.toFloat()) * 1100f + 150f
                                }
                                else -> 80f + (temp - 193f) * 0.6f
                            }
                            val px = plotOrigin.x + (st / steps.toFloat()) * plotW
                            val py = plotOrigin.y - (qVal / 1300f).coerceIn(0f, 1f) * plotH
                            if (st == 0) moveTo(px, py) else lineTo(px, py)
                        }
                    }
                    drawPath(
                        path = nukiyamaPath,
                        color = AmberVibrant,
                        style = Stroke(width = 1.8f, cap = StrokeCap.Round)
                    )

                    // Current Temperature Operating Point
                    val curTempFrac = ((surfaceTempC - 80f) / 220f).coerceIn(0f, 1f)
                    val ptX = plotOrigin.x + curTempFrac * plotW
                    val ptY = plotOrigin.y - (heatFluxKWm2 / 1300f).coerceIn(0f, 1f) * plotH
                    drawCircle(
                        color = if (isLeidenfrost) CyanNeon else CoralNeon,
                        radius = 4.0.dp.toPx(),
                        center = Offset(ptX, ptY)
                    )
                    drawCircle(Color.White, 2.0.dp.toPx(), Offset(ptX, ptY))
                }
            }
        },
        hudContent = {
            // Rule 13: Transparent HUD Card with clean SI metrics
            val regimeLabel = when {
                isLeidenfrost -> "Leidenfrost Film Boiling (Hovering)"
                isNucleateBoiling -> "Transition / Nucleate Boiling (Sizzling)"
                else -> "Subcooled Contact (Wetting)"
            }
            val gapLabel = if (isLeidenfrost) "${round(vaporThicknessUm).toInt()} µm (Cushion)" else "0 µm (Contact)"

            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 45: The Leidenfrost Effect",
                items = listOf(
                    "Boiling Regime" to regimeLabel,
                    "Surface Temp (T_pan)" to "${round(surfaceTempC).toInt()} °C (Threshold: ${tLeidenfrostC.toInt()} °C)",
                    "Vapor Film Gap (δ)" to gapLabel,
                    "Heat Flux (q\")" to "${round(heatFluxKWm2).toInt()} kW/m² (${if (isLeidenfrost) "90% Insulated" else "Peak Flux"})",
                    "Droplet Radius (R)" to "${round(dropletRadiusMm * 10f) / 10f} mm (${currentPreset.liquidName})",
                    "Evaporation Time" to "${round(evaporationLifetimeSec).toInt()} s (Prolonged Lifetime)",
                    "Substrate Tilt (θ)" to "${round(substrateTiltDeg).toInt()}° (Frictionless Glide)"
                )
            )
        },
        controlsContent = {
            // Rule 15: Compact Controls Deck with paired sliders, scenario chips, and 0 nested verticalScroll
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Selection Chips
                Text(
                    text = "SELECT THERMODYNAMIC REGIME",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LeidenfrostPreset.values().forEach { preset ->
                        val isSelected = currentPreset == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyanNeon.copy(alpha = 0.22f) else ScienceDarkSurfaceVariant)
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

                // Paired Sliders: Pan Surface Temperature & Droplet Radius
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Pan Surface Temp",
                        value = surfaceTempC,
                        range = 50f..320f,
                        valueDisplay = "${round(surfaceTempC).toInt()} °C",
                        accentColor = CoralNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { surfaceTempC = it }
                    )

                    PhysicsSliderControl(
                        title = "Droplet Radius",
                        value = dropletRadiusMm,
                        range = 1.0f..6.0f,
                        valueDisplay = "${round(dropletRadiusMm * 10f) / 10f} mm",
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { dropletRadiusMm = it }
                    )
                }

                // Substrate Tilt Angle Slider
                PhysicsSliderControl(
                    title = "Substrate Tilt Angle (Frictionless Roll)",
                    value = substrateTiltDeg,
                    range = -15f..15f,
                    valueDisplay = "${round(substrateTiltDeg).toInt()}°",
                    accentColor = AmberVibrant,
                    onValueChange = { substrateTiltDeg = it }
                )

                // Visual Layer Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = showVaporFilm,
                        onClick = { showVaporFilm = !showVaporFilm },
                        label = { Text("Vapor Film δ", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CyanNeon
                        )
                    )
                    FilterChip(
                        selected = showHeatFluxCurve,
                        onClick = { showHeatFluxCurve = !showHeatFluxCurve },
                        label = { Text("Boiling Curve", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberVibrant.copy(alpha = 0.25f),
                            selectedLabelColor = AmberVibrant
                        )
                    )
                    FilterChip(
                        selected = showForceVectors,
                        onClick = { showForceVectors = !showForceVectors },
                        label = { Text("Lift / Gravity", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldNeon.copy(alpha = 0.25f),
                            selectedLabelColor = EmeraldNeon
                        )
                    )
                }

                // Action Controls: Play/Pause, Snap to Leidenfrost Point, and Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberVibrant else CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1.0f)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Freeze Drop" else "▶ Run Flow",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            surfaceTempC = if (surfaceTempC < 193f) 250f else 140f
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ScienceDarkSurfaceVariant,
                            contentColor = CoralNeon
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1.0f)
                    ) {
                        Text(
                            text = if (surfaceTempC < 193f) "🔥 Jump to 250°C" else "❄ Cool to 140°C",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            applyPreset(LeidenfrostPreset.LEIDENFROST_WATER)
                            dropletPosX = 0.48f
                            dropletVelX = 0f
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
