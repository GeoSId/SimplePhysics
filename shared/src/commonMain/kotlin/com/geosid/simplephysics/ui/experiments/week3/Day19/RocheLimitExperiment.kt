package com.geosid.simplephysics.ui.experiments.week3.Day19

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
import kotlin.random.Random

/**
 * Astrodynamics presets for Roche Limit tidal disruption.
 */
enum class RochePreset(
    val title: String,
    val icon: String,
    val distanceRatio: Float,
    val densityRatio: Float,
    val isFluid: Boolean,
    val description: String,
    val color: Color
) {
    SATURN_RING(
        title = "Saturn Rings",
        icon = "🪐",
        distanceRatio = 2.05f,
        densityRatio = 1.0f,
        isFluid = true,
        description = "Icy moon crosses Roche limit (2.44 R) and shreds into bright rings",
        color = AmberVibrant
    ),
    STABLE_TITAN(
        title = "Stable Moon",
        icon = "⚪",
        distanceRatio = 3.35f,
        densityRatio = 1.0f,
        isFluid = true,
        description = "Safe orbit outside Roche limit: stable moon with gentle tidal bulge",
        color = CyanNeon
    ),
    DENSE_ASTEROID(
        title = "Dense Rock",
        icon = "🪨",
        distanceRatio = 1.65f,
        densityRatio = 0.55f,
        isFluid = false,
        description = "High density & rigidity shrinks Roche limit, penetrating closer",
        color = EmeraldNeon
    ),
    COMET_SL9(
        title = "Comet SL-9",
        icon = "☄️",
        distanceRatio = 1.40f,
        densityRatio = 1.50f,
        isFluid = true,
        description = "Shoemaker-Levy 9 tidal disruption: string of pearls in planetary field",
        color = CoralNeon
    )
}

/**
 * Disruption physics regime: fluid/rubble pile vs rigid cohesive rock.
 */
enum class MoonRigidity(val label: String, val coeff: Float, val icon: String) {
    FLUID_RUBBLE("Fluid / Rubble Pile (2.44)", 2.44f, "💧"),
    RIGID_SOLID("Rigid Solid Moon (1.26)", 1.26f, "💎")
}

/**
 * Debris constituent particle of the moon.
 */
data class RingDebrisParticle(
    val id: Int,
    val localRadialOffset: Float,
    val localTangentialOffset: Float,
    var orbitalRadiusPx: Float,
    var orbitalAngleRad: Float,
    val color: Color,
    val sizePx: Float,
    val shearRateFactor: Float
)

@Composable
fun RocheLimitExperiment(
    modifier: Modifier = Modifier
) {
    // 1. Physical Parameters
    var selectedPreset by remember { mutableStateOf<RochePreset?>(RochePreset.SATURN_RING) }
    var distanceRatioParam by remember { mutableStateOf(2.05f) } // d / R_planet
    var densityRatioParam by remember { mutableStateOf(1.0f) } // rho_M / rho_m
    var rigidityMode by remember { mutableStateOf(MoonRigidity.FLUID_RUBBLE) }
    var isRunning by remember { mutableStateOf(true) }
    var simSpeed by remember { mutableStateOf(1.0f) }

    // 2. Physical Dynamic Quantities
    val rocheCoeff = rigidityMode.coeff
    val rocheLimitRatio = remember(rocheCoeff, densityRatioParam) {
        rocheCoeff * (densityRatioParam.toDouble().pow(1.0 / 3.0)).toFloat()
    }
    val isDisrupting = distanceRatioParam <= rocheLimitRatio

    // Ratio of differential tidal force to internal self-gravitational cohesion
    val tidalStressRatio = remember(distanceRatioParam, rocheLimitRatio) {
        val r = (rocheLimitRatio / distanceRatioParam.coerceAtLeast(0.8f)).toDouble().pow(3.0).toFloat()
        round(r * 100f) / 100f
    }

    // Dynamic orbital angle of the moon center of mass
    var moonOrbitAngle by remember { mutableStateOf(0f) }
    // Disruption factor: 0f (intact solid moon) to 1f (fully pulverized annular ring)
    var disruptionProgress by remember { mutableStateOf(0f) }

    // Particle debris system (96 particles)
    val particles = remember {
        val list = mutableListOf<RingDebrisParticle>()
        val rng = Random(42)
        val particleColors = listOf(
            Color(0xFFE0F7FA), // Ice white-cyan
            CyanNeon,
            AmberVibrant,
            Color(0xFFFFECB3), // Warm pearl
            CoralNeon,
            Color(0xFFFFF9C4)  // Pale gold
        )
        for (i in 0 until 96) {
            val rFrac = sqrt(rng.nextFloat())
            val theta = rng.nextFloat() * 2f * PI.toFloat()
            val lx = rFrac * cos(theta)
            val ly = rFrac * sin(theta)
            val color = particleColors[rng.nextInt(particleColors.size)]
            val size = 1.8f + rng.nextFloat() * 1.8f
            val shearFactor = 1.0f - (lx * 0.12f)
            list.add(
                RingDebrisParticle(
                    id = i,
                    localRadialOffset = lx,
                    localTangentialOffset = ly,
                    orbitalRadiusPx = 0f,
                    orbitalAngleRad = 0f,
                    color = color,
                    sizePx = size,
                    shearRateFactor = shearFactor
                )
            )
        }
        list
    }

    val resetMoonState = {
        moonOrbitAngle = 0f
        disruptionProgress = if (distanceRatioParam <= rocheLimitRatio) 0.05f else 0f
        particles.forEach { p ->
            p.orbitalAngleRad = 0f
            p.orbitalRadiusPx = 0f
        }
    }

    val applyPreset = { preset: RochePreset ->
        selectedPreset = preset
        distanceRatioParam = preset.distanceRatio
        densityRatioParam = preset.densityRatio
        rigidityMode = if (preset.isFluid) MoonRigidity.FLUID_RUBBLE else MoonRigidity.RIGID_SOLID
        resetMoonState()
    }

    val infiniteTransition = rememberInfiniteTransition()
    val coronaPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // 3. High-Precision Physical Integration Loop (Keplerian differential shear)
    LaunchedEffect(isRunning, distanceRatioParam, densityRatioParam, rigidityMode, simSpeed) {
        var lastTime = withFrameNanos { it }

        while (true) {
            val now = withFrameNanos { it }
            val rawDt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
            lastTime = now

            if (isRunning) {
                val dt = rawDt * simSpeed

                // Kepler Third Law: Omega = sqrt(G*M / d^3)
                val baseOmega = 1.0f * (2.0f / distanceRatioParam.coerceAtLeast(0.8f)).toDouble().pow(1.5).toFloat()
                moonOrbitAngle = (moonOrbitAngle + baseOmega * dt) % (2f * PI.toFloat())

                // Disruption evolution
                if (distanceRatioParam <= rocheLimitRatio) {
                    disruptionProgress = (disruptionProgress + 0.35f * dt).coerceAtMost(1f)
                } else {
                    disruptionProgress = (disruptionProgress - 0.50f * dt).coerceAtLeast(0f)
                }

                // Advance each individual particle according to Keplerian differential shear
                particles.forEach { p ->
                    val particleRadialOffsetFrac = p.localRadialOffset * 0.15f
                    val effectiveRadiusRatio = (distanceRatioParam * (1.0f + particleRadialOffsetFrac)).coerceAtLeast(0.9f)
                    val particleOmega = 1.0f * (2.0f / effectiveRadiusRatio).toDouble().pow(1.5).toFloat()

                    if (disruptionProgress > 0.01f) {
                        val shearAdvance = (particleOmega - baseOmega) * disruptionProgress * 2.8f
                        p.orbitalAngleRad = (p.orbitalAngleRad + (baseOmega + shearAdvance) * dt) % (2f * PI.toFloat())
                    } else {
                        p.orbitalAngleRad = moonOrbitAngle
                    }
                }
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag planet or orbit slider to move moon across the Roche limit. Watch tidal forces shred it into rings!",
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 19: Roche Limit & Tidal Disruption",
                items = listOf(
                    "Formula" to "d_{roche} = ${if (rigidityMode == MoonRigidity.FLUID_RUBBLE) "2.44" else "1.26"} R \\left(\\frac{\\rho_M}{\\rho_m}\\right)^{1/3}",
                    "Orbit Distance (d)" to "${round(distanceRatioParam * 100f) / 100f} R_p",
                    "Roche Limit (d_R)" to "${round(rocheLimitRatio * 100f) / 100f} R_p",
                    "Tidal Stress (F_t / F_g)" to "${tidalStressRatio}x ${if (isDisrupting) "(RUPTURE)" else "(STABLE)"}",
                    "State" to if (disruptionProgress >= 0.85f) "PLANETARY RING DISK" else if (isDisrupting) "TIDAL SHREDDING (${(disruptionProgress * 100).toInt()}%)" else "COHESIVE MOON"
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
                            val delta = dragAmount.x / 180f
                            distanceRatioParam = (distanceRatioParam + delta).coerceIn(1.10f, 3.85f)
                            selectedPreset = null
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val w = size.width
                            val h = size.height
                            val planetCenter = Offset(w * 0.50f, h * 0.39f)
                            val distPx = (offset - planetCenter).getDistance()
                            val planetRadiusPx = min(w * 0.16f, h * 0.13f)
                            val tappedRatio = (distPx / planetRadiusPx).coerceIn(1.10f, 3.85f)
                            distanceRatioParam = tappedRatio
                            selectedPreset = null
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated center origin to keep bottom 35% completely clear for controls
                val planetCenter = Offset(w * 0.50f, h * 0.39f)
                val planetRadiusPx = min(w * 0.16f, h * 0.13f)
                val moonRadiusPx = planetRadiusPx * 0.28f

                // Deep space background starfield and grid
                drawSpaceBackground(w, h, planetCenter)

                // 1. Roche Limit Boundary Circle (Dashed Coral Warning Radius)
                val rocheRadiusPx = planetRadiusPx * rocheLimitRatio
                drawCircle(
                    color = CoralNeon.copy(alpha = 0.08f),
                    radius = rocheRadiusPx,
                    center = planetCenter
                )
                drawCircle(
                    color = CoralNeon.copy(alpha = 0.65f),
                    radius = rocheRadiusPx,
                    center = planetCenter,
                    style = Stroke(
                        width = 1.8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                )

                // 2. Active Moon Orbital Trajectory Guide
                val orbitRadiusPx = planetRadiusPx * distanceRatioParam
                drawCircle(
                    color = if (isDisrupting) CoralNeon.copy(alpha = 0.30f) else CyanNeon.copy(alpha = 0.30f),
                    radius = orbitRadiusPx,
                    center = planetCenter,
                    style = Stroke(
                        width = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                )

                // 3. Central Gas Giant Planet (Saturn-like with atmospheric bands & corona)
                drawGasGiantPlanet(planetCenter, planetRadiusPx, coronaPulse)

                // 4. Moon and Ring Debris Simulation
                val moonCenter = Offset(
                    planetCenter.x + cos(moonOrbitAngle) * orbitRadiusPx,
                    planetCenter.y + sin(moonOrbitAngle) * orbitRadiusPx
                )

                if (disruptionProgress < 0.95f) {
                    val moonAlpha = (1f - disruptionProgress).coerceIn(0.15f, 1f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = moonAlpha),
                                Color(0xFFB0BEC5).copy(alpha = moonAlpha),
                                Color(0xFF37474F).copy(alpha = moonAlpha)
                            ),
                            center = Offset(moonCenter.x - moonRadiusPx * 0.3f, moonCenter.y - moonRadiusPx * 0.3f),
                            radius = moonRadiusPx * 1.2f
                        ),
                        radius = moonRadiusPx * (1f - disruptionProgress * 0.45f),
                        center = moonCenter
                    )

                    // Draw differential tidal force vectors
                    if (disruptionProgress < 0.60f) {
                        val unitToPlanet = (planetCenter - moonCenter) / orbitRadiusPx
                        val unitAwayPlanet = -unitToPlanet
                        val vectorLen = moonRadiusPx * (1.2f + tidalStressRatio * 0.6f).coerceAtMost(3.2f)

                        val nearSidePt = moonCenter + unitToPlanet * moonRadiusPx
                        val nearSideEnd = nearSidePt + unitToPlanet * vectorLen
                        drawLine(
                            color = CoralNeon.copy(alpha = 0.85f),
                            start = nearSidePt,
                            end = nearSideEnd,
                            strokeWidth = 2.0f,
                            cap = StrokeCap.Round
                        )

                        val farSidePt = moonCenter + unitAwayPlanet * moonRadiusPx
                        val farSideEnd = farSidePt + unitAwayPlanet * vectorLen
                        drawLine(
                            color = CoralNeon.copy(alpha = 0.85f),
                            start = farSidePt,
                            end = farSideEnd,
                            strokeWidth = 2.0f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 5. Render Individual Ring Debris Particles with Keplerian Shear
                particles.forEach { p ->
                    val particlePos: Offset = if (disruptionProgress < 0.05f) {
                        val angle = moonOrbitAngle
                        val cosA = cos(angle)
                        val sinA = sin(angle)
                        val lx = p.localRadialOffset * moonRadiusPx * 0.85f
                        val ly = p.localTangentialOffset * moonRadiusPx * 0.85f
                        Offset(
                            moonCenter.x + (lx * cosA - ly * sinA),
                            moonCenter.y + (lx * sinA + ly * cosA)
                        )
                    } else {
                        val particleR = orbitRadiusPx + (p.localRadialOffset * moonRadiusPx * (1.0f + disruptionProgress * 2.2f))
                        val angle = if (disruptionProgress >= 0.95f) p.orbitalAngleRad else {
                            val shearedAngle = p.orbitalAngleRad
                            moonOrbitAngle * (1f - disruptionProgress) + shearedAngle * disruptionProgress
                        }
                        Offset(
                            planetCenter.x + cos(angle) * particleR,
                            planetCenter.y + sin(angle) * particleR
                        )
                    }

                    drawCircle(
                        color = p.color.copy(alpha = 0.85f),
                        radius = p.sizePx,
                        center = particlePos
                    )
                }

                // 6. Roche Limit & Orbit Distance Annotations (Top right HUD in canvas)
                val statusColor = if (isDisrupting) CoralNeon else CyanNeon
                drawCircle(
                    color = statusColor,
                    radius = 4.dp.toPx(),
                    center = Offset(w * 0.08f, h * 0.04f)
                )
            }
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Selector Chips (compact)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RochePreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { applyPreset(preset) },
                            color = if (isSelected) preset.color.copy(alpha = 0.22f) else ScienceDarkSurfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.2.dp, preset.color) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = preset.icon,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = preset.title,
                                    color = if (isSelected) preset.color else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Rigidity Selector (Fluid Rubble vs Rigid Solid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MoonRigidity.values().forEach { mode ->
                        val isSelected = rigidityMode == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    rigidityMode = mode
                                    selectedPreset = null
                                },
                            color = if (isSelected) AmberVibrant.copy(alpha = 0.20f) else ScienceDarkSurfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AmberVibrant) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = mode.icon, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = mode.label,
                                    color = if (isSelected) AmberVibrant else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Paired Sliders: Orbital Distance (d/R) & Density Ratio (rho_M / rho_m)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Orbit Distance (d/R)",
                            value = distanceRatioParam,
                            range = 1.10f..3.85f,
                            valueDisplay = "${round(distanceRatioParam * 100f) / 100f} R",
                            accentColor = if (isDisrupting) CoralNeon else CyanNeon,
                            onValueChange = {
                                distanceRatioParam = it
                                selectedPreset = null
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Density Ratio (ρ_M/ρ_m)",
                            value = densityRatioParam,
                            range = 0.50f..2.50f,
                            valueDisplay = "${round(densityRatioParam * 100f) / 100f}x",
                            accentColor = AmberVibrant,
                            onValueChange = {
                                densityRatioParam = it
                                selectedPreset = null
                            }
                        )
                    }
                }

                // Action Buttons: Run/Pause & Reset
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
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run Simulation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { resetMoonState() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScienceDarkSurfaceVariant,
                            contentColor = CyanNeon
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = "🔄 Reform Moon",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            applyPreset(RochePreset.SATURN_RING)
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

// ─────────────────────────────────────────────────────────────────────────────
// Canvas Visual Drawing Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawSpaceBackground(w: Float, h: Float, center: Offset) {
    val gridStep = 42.dp.toPx()
    var gx = 0f
    while (gx < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.16f),
            start = Offset(gx, 0f),
            end = Offset(gx, h),
            strokeWidth = 0.7f
        )
        gx += gridStep
    }
    var gy = 0f
    while (gy < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.16f),
            start = Offset(0f, gy),
            end = Offset(w, gy),
            strokeWidth = 0.7f
        )
        gy += gridStep
    }

    val stars = listOf(
        Offset(w * 0.10f, h * 0.12f), Offset(w * 0.22f, h * 0.07f),
        Offset(w * 0.82f, h * 0.10f), Offset(w * 0.90f, h * 0.22f),
        Offset(w * 0.15f, h * 0.62f), Offset(w * 0.32f, h * 0.70f),
        Offset(w * 0.72f, h * 0.65f), Offset(w * 0.88f, h * 0.54f),
        Offset(w * 0.50f, h * 0.05f), Offset(w * 0.06f, h * 0.35f)
    )
    stars.forEachIndexed { i, pt ->
        val radius = if (i % 3 == 0) 1.6f else 1.1f
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = radius,
            center = pt
        )
    }
}

private fun DrawScope.drawGasGiantPlanet(center: Offset, planetR: Float, coronaPulse: Float) {
    // 1. Outer Atmospheric Corona Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                AmberVibrant.copy(alpha = 0.25f * coronaPulse),
                Color(0xFFEF6C00).copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = center,
            radius = planetR * 1.8f
        ),
        radius = planetR * 1.8f,
        center = center
    )

    // 2. Planet Sphere with 3D Shading
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFE082),
                Color(0xFFFFA000),
                Color(0xFFE65100),
                Color(0xFF3E2723)
            ),
            center = Offset(center.x - planetR * 0.35f, center.y - planetR * 0.35f),
            radius = planetR * 1.35f
        ),
        radius = planetR,
        center = center
    )

    // 3. Atmospheric Cloud Bands
    val bands = listOf(
        -0.60f to Color(0xFFFFECB3).copy(alpha = 0.45f),
        -0.30f to Color(0xFFD7CCC8).copy(alpha = 0.55f),
        -0.05f to Color(0xFFBF360C).copy(alpha = 0.60f),
        0.20f to Color(0xFFFFD54F).copy(alpha = 0.50f),
        0.48f to Color(0xFF8D6E63).copy(alpha = 0.45f)
    )

    bands.forEach { (yFrac, color) ->
        val bandY = center.y + planetR * yFrac
        val halfW = sqrt((planetR * planetR - (planetR * yFrac) * (planetR * yFrac)).coerceAtLeast(0f))
        if (halfW > 2f) {
            drawLine(
                color = color,
                start = Offset(center.x - halfW * 0.96f, bandY),
                end = Offset(center.x + halfW * 0.96f, bandY),
                strokeWidth = 2.4f,
                cap = StrokeCap.Round
            )
        }
    }
}
