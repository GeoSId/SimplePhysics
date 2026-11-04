package com.geosid.simplephysics.ui.experiments.week7.Day44

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
 * Physical scenarios / presets for Brownian Motion.
 */
enum class BrownianPreset(
    val label: String,
    val title: String,
    val temperatureK: Float,
    val particleRadiusUm: Float,
    val mediumViscosityCP: Float,
    val mediumName: String,
    val desc: String
) {
    POLLEN_WATER(
        label = "Pollen Grain",
        title = "Robert Brown's Canonical 1827 Observation",
        temperatureK = 293.15f, // 20°C
        particleRadiusUm = 2.5f,
        mediumViscosityCP = 1.002f, // Water at 20°C
        mediumName = "Room Temp Water",
        desc = "Suspended pollen grains bombarded by trillions of thermal water molecules, exhibiting erratic random walks"
    ),
    SMOKE_AIR(
        label = "Smoke in Air",
        title = "Aerosol Microscopic Diffusion",
        temperatureK = 298.15f,
        particleRadiusUm = 0.5f,
        mediumViscosityCP = 0.0182f, // Air
        mediumName = "Atmospheric Air",
        desc = "Ultra-low viscosity gas produces rapid stochastic mean-free-path collisions and rapid dispersion"
    ),
    FAT_MILK(
        label = "Milk Fat",
        title = "Lipid Droplet Suspension in Cold Water",
        temperatureK = 277.15f, // 4°C
        particleRadiusUm = 1.2f,
        mediumViscosityCP = 1.567f, // Cold water
        mediumName = "Chilled Water (4°C)",
        desc = "Lower thermal energy and higher fluid viscosity slow mean square displacement, demonstrating T/η scaling"
    ),
    COLLOID_GLYCERIN(
        label = "Viscous Honey",
        title = "High-Viscosity Drag Suppression",
        temperatureK = 310.15f, // 37°C
        particleRadiusUm = 3.0f,
        mediumViscosityCP = 120.0f, // 80% Glycerin
        mediumName = "Glycerin Mixture",
        desc = "Enormous Stokes friction dampens thermal velocity fluctuations, confining diffusion to tiny localized jiggles"
    )
}

/**
 * Microscopic thermal solvent molecule (e.g. water H2O).
 */
private class SolventMolecule(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color
)

@Composable
fun BrownianMotionExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(BrownianPreset.POLLEN_WATER) }

    // Physical Parameters
    var temperatureK by remember { mutableStateOf(currentPreset.temperatureK) }
    var particleRadiusUm by remember { mutableStateOf(currentPreset.particleRadiusUm) }
    var mediumViscosityCP by remember { mutableStateOf(currentPreset.mediumViscosityCP) }

    // Display Toggles
    var showSolventMolecules by remember { mutableStateOf(true) }
    var showDisplacementRadius by remember { mutableStateOf(true) }
    var showCollisionVectors by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(true) }

    fun applyPreset(preset: BrownianPreset) {
        currentPreset = preset
        temperatureK = preset.temperatureK
        particleRadiusUm = preset.particleRadiusUm
        mediumViscosityCP = preset.mediumViscosityCP
    }

    // Physical Constants & Einstein-Smoluchowski Calculations
    val kb = 1.380649e-23f // J/K
    val radiusM = particleRadiusUm * 1e-6f
    val etaPaS = mediumViscosityCP * 1e-3f // Pa·s = kg/(m·s)

    // Stokes Friction Coefficient: gamma = 6 * pi * eta * r
    val stokesDragGamma = 6f * PI.toFloat() * etaPaS * radiusM

    // Einstein Diffusion Coefficient: D = (k_B * T) / (6 * pi * eta * r)
    val diffusionCoefficientM2S = (kb * temperatureK) / max(stokesDragGamma, 1e-25f)
    val diffusionCoefficientUm2S = diffusionCoefficientM2S * 1e12f

    // Root-Mean-Square displacement rate in 2D: <r^2> = 4 * D * t -> sqrt(<r^2>/t) = sqrt(4D)
    val rmsVelocityUmS = sqrt(4f * max(diffusionCoefficientUm2S, 1e-12f))

    // Interactive Brownian Particle State
    var particleRelPos by remember { mutableStateOf(Offset(0.50f, 0.44f)) }
    var particleVel by remember { mutableStateOf(Offset.Zero) }
    val initialOrigin = remember { mutableStateOf(Offset(0.50f, 0.44f)) }

    // Trajectory History for Random Walk
    val trajectory = remember { mutableStateListOf<Offset>() }
    var elapsedTimeSec by remember { mutableStateOf(0f) }

    // Thermal Solvent Molecules
    val solventCount = 75
    val solvents = remember {
        List(solventCount) { i ->
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 200f + 100f
            SolventMolecule(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                color = if (i % 2 == 0) CyanNeon.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.40f)
            )
        }
    }

    // Cumulative net collision force impulse for visualization
    var impulseVector by remember { mutableStateOf(Offset.Zero) }

    // Reset simulation history
    fun resetTrajectory() {
        particleRelPos = Offset(0.50f, 0.44f)
        initialOrigin.value = Offset(0.50f, 0.44f)
        particleVel = Offset.Zero
        trajectory.clear()
        trajectory.add(Offset(0.50f, 0.44f))
        elapsedTimeSec = 0f
        impulseVector = Offset.Zero
    }

    // High-Precision Physics Step Loop
    LaunchedEffect(temperatureK, particleRadiusUm, mediumViscosityCP, isRunning) {
        if (!isRunning) return@LaunchedEffect
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.035f)
            lastTime = now

            elapsedTimeSec += dt

            // Thermal molecular speed scaling factor: sqrt(T / 293K)
            val thermalSpeedScale = sqrt(temperatureK / 293.15f)

            // Random stochastic thermal kick (Einstein-Ornstein-Uhlenbeck white noise)
            // Kick magnitude scales with sqrt(2 * D * dt) or thermal energy / damping
            val kickIntensity = (rmsVelocityUmS * 0.08f * thermalSpeedScale / max(mediumViscosityCP.pow(0.4f), 0.1f)).coerceIn(0.002f, 0.06f)
            val kickAngle = Random.nextFloat() * 2f * PI.toFloat()
            val kickForce = Offset(cos(kickAngle) * kickIntensity, sin(kickAngle) * kickIntensity)
            impulseVector = kickForce * 15f

            // Update particle velocity with damping and thermal Langevin noise
            val damping = (1f - (stokesDragGamma * 2e7f * dt)).coerceIn(0.70f, 0.96f)
            particleVel = (particleVel * damping) + (kickForce * 3.5f)

            // Update position
            val newX = (particleRelPos.x + particleVel.x * dt).coerceIn(0.08f, 0.92f)
            val newY = (particleRelPos.y + particleVel.y * dt).coerceIn(0.12f, 0.76f)
            particleRelPos = Offset(newX, newY)

            // Append to trajectory history (limit to 300 points)
            if (trajectory.isEmpty() || (trajectory.last() - particleRelPos).getDistance() > 0.006f) {
                trajectory.add(particleRelPos)
                if (trajectory.size > 300) {
                    trajectory.removeAt(0)
                }
            }

            // Animate solvent molecules
            solvents.forEach { s ->
                s.x = (s.x + (s.vx * thermalSpeedScale / 450f) * dt + 1f) % 1f
                s.y = (s.y + (s.vy * thermalSpeedScale / 450f) * dt + 1f) % 1f
            }
        }
    }

    // Displacement from origin
    val currentDxUm = (particleRelPos.x - initialOrigin.value.x) * 100f
    val currentDyUm = (particleRelPos.y - initialOrigin.value.y) * 100f
    val displacementRUm = sqrt(currentDxUm * currentDxUm + currentDyUm * currentDyUm)
    val theoreticalRmsRUm = sqrt(4f * diffusionCoefficientUm2S * max(elapsedTimeSec, 0.01f))

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag pollen grain or tap anywhere. Observe stochastic random walk, Einstein diffusion radius, and molecular collisions!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            particleRelPos = Offset(
                                (particleRelPos.x + dragAmount.x / size.width).coerceIn(0.08f, 0.92f),
                                (particleRelPos.y + dragAmount.y / size.height).coerceIn(0.12f, 0.76f)
                            )
                            trajectory.add(particleRelPos)
                            if (trajectory.size > 300) trajectory.removeAt(0)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            particleRelPos = Offset(
                                (offset.x / size.width).coerceIn(0.08f, 0.92f),
                                (offset.y / size.height).coerceIn(0.12f, 0.76f)
                            )
                            trajectory.add(particleRelPos)
                            if (trajectory.size > 300) trajectory.removeAt(0)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated Canvas Origin layout (cy ≈ 0.44f * h)
                val originPx = Offset(w * initialOrigin.value.x, h * initialOrigin.value.y)
                val particlePx = Offset(w * particleRelPos.x, h * particleRelPos.y)
                val pRadiusPx = (particleRadiusUm * 8.5f).dp.toPx().coerceIn(12.dp.toPx(), 36.dp.toPx())

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

                // 2. Einstein Theoretical Diffusion Radius Circle: <r^2> = 4Dt
                if (showDisplacementRadius) {
                    val scaleUmToPx = (w * 0.008f)
                    val theoreticalRadiusPx = (theoreticalRmsRUm * scaleUmToPx).coerceIn(4f, w * 0.48f)

                    // Diffusion Probability Cloud Halo
                    drawCircle(
                        color = CyanNeon.copy(alpha = 0.08f),
                        radius = theoreticalRadiusPx,
                        center = originPx
                    )
                    // Dashed Outer Frontier
                    drawCircle(
                        color = CyanNeon.copy(alpha = 0.40f),
                        radius = theoreticalRadiusPx,
                        center = originPx,
                        style = Stroke(
                            width = 1.4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    )

                    // Origin Crosshair Anchor
                    drawLine(Color.White.copy(alpha = 0.4f), Offset(originPx.x - 8f, originPx.y), Offset(originPx.x + 8f, originPx.y), 1.2f)
                    drawLine(Color.White.copy(alpha = 0.4f), Offset(originPx.x, originPx.y - 8f), Offset(originPx.x, originPx.y + 8f), 1.2f)
                    drawCircle(CyanNeon, 2.5f, originPx)
                }

                // 3. Random Walk Trajectory Line with Gradient Decay
                if (trajectory.size > 1) {
                    val path = Path()
                    trajectory.forEachIndexed { i, pt ->
                        val px = pt.x * w
                        val py = pt.y * h
                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }

                    drawPath(
                        path = path,
                        color = AmberVibrant.copy(alpha = 0.75f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // Displacement Vector: From Initial Origin to Current Particle Position
                drawLine(
                    color = CoralNeon.copy(alpha = 0.65f),
                    start = originPx,
                    end = particlePx,
                    strokeWidth = 1.8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                )

                // 4. Background Solvent Molecules (Fast Microscopic Bombers)
                if (showSolventMolecules) {
                    solvents.forEach { s ->
                        val sx = s.x * w
                        val sy = s.y * h
                        // Velocity indicator tail
                        val tailX = sx - (s.vx / 350f) * 6f
                        val tailY = sy - (s.vy / 350f) * 6f
                        drawLine(
                            color = s.color.copy(alpha = 0.35f),
                            start = Offset(tailX, tailY),
                            end = Offset(sx, sy),
                            strokeWidth = 1.2f
                        )
                        drawCircle(
                            color = s.color,
                            radius = 2.0f,
                            center = Offset(sx, sy)
                        )
                    }
                }

                // 5. Stochastic Momentum Impulse Vector (Net Thermal Impact)
                if (showCollisionVectors) {
                    val impLen = impulseVector.getDistance().coerceIn(8f, 50f)
                    val impAngle = atan2(impulseVector.y, impulseVector.x)
                    val impEnd = Offset(
                        particlePx.x + cos(impAngle) * (pRadiusPx + impLen),
                        particlePx.y + sin(impAngle) * (pRadiusPx + impLen)
                    )
                    drawLine(
                        color = CoralNeon,
                        start = particlePx,
                        end = impEnd,
                        strokeWidth = 2.8f,
                        cap = StrokeCap.Round
                    )
                    val head = Path().apply {
                        val hAngle = impAngle + PI.toFloat()
                        moveTo(impEnd.x, impEnd.y)
                        lineTo(impEnd.x + cos(hAngle + 0.45f) * 8f, impEnd.y + sin(hAngle + 0.45f) * 8f)
                        lineTo(impEnd.x + cos(hAngle - 0.45f) * 8f, impEnd.y + sin(hAngle - 0.45f) * 8f)
                        close()
                    }
                    drawPath(head, CoralNeon)
                }

                // 6. Suspended Brownian Grain (Pollen / Colloid / Fat Droplet)
                // Outer Glow & Hydration Shell
                drawCircle(
                    color = AmberVibrant.copy(alpha = 0.16f),
                    radius = pRadiusPx + 8.dp.toPx(),
                    center = particlePx
                )
                drawCircle(
                    color = AmberVibrant.copy(alpha = 0.30f),
                    radius = pRadiusPx + 3.dp.toPx(),
                    center = particlePx
                )
                // Solid Particle Body with Radial Shading
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFDE047), AmberVibrant, Color(0xFFD97706)),
                        center = particlePx.copy(x = particlePx.x - pRadiusPx * 0.3f, y = particlePx.y - pRadiusPx * 0.3f),
                        radius = pRadiusPx
                    ),
                    radius = pRadiusPx,
                    center = particlePx
                )
                // Surface Rim
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = pRadiusPx,
                    center = particlePx,
                    style = Stroke(width = 2.0f)
                )

                // Particle Core Center
                drawCircle(
                    color = Color.White,
                    radius = 3.0f,
                    center = particlePx
                )
            }
        },
        hudContent = {
            // Rule 13: Transparent HUD Card with clean live metrics
            val dDisplay = "${round(diffusionCoefficientUm2S * 100f) / 100f} µm²/s"
            val rDisplay = "${round(displacementRUm * 10f) / 10f} µm"
            val rmsDisplay = "${round(theoreticalRmsRUm * 10f) / 10f} µm"

            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 44: Brownian Motion & Random Walk",
                items = listOf(
                    "Einstein Relation" to "⟨r²⟩ = 4·D·t,  D = (k_B·T)/(6π·η·r)",
                    "Diffusion Coeff (D)" to dDisplay,
                    "Displacement (|r|)" to "$rDisplay (RMS: $rmsDisplay)",
                    "Temperature (T)" to "${round(temperatureK).toInt()} K (${(round(temperatureK) - 273.15f).toInt()} °C)",
                    "Particle Radius (r)" to "${round(particleRadiusUm * 10f) / 10f} µm",
                    "Fluid Viscosity (η)" to "${round(mediumViscosityCP * 100f) / 100f} cP (${currentPreset.mediumName})",
                    "Elapsed Time (t)" to "${round(elapsedTimeSec * 10f) / 10f} s (${trajectory.size} steps)"
                )
            )
        },
        controlsContent = {
            // Rule 15: Compact Controls Deck with paired sliders, quick toggles, and 0 nested verticalScroll
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Selection Chips
                Text(
                    text = "SELECT BROWNIAN SYSTEM",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BrownianPreset.values().forEach { preset ->
                        val isSelected = currentPreset == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AmberVibrant.copy(alpha = 0.22f) else ScienceDarkSurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) AmberVibrant else ScienceBorder.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    applyPreset(preset)
                                    resetTrajectory()
                                }
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.label,
                                color = if (isSelected) AmberVibrant else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Paired Sliders: Temperature T & Particle Radius r
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Temperature T",
                        value = temperatureK,
                        range = 270f..373f,
                        valueDisplay = "${round(temperatureK).toInt()} K",
                        accentColor = CoralNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { temperatureK = it }
                    )

                    PhysicsSliderControl(
                        title = "Grain Radius r",
                        value = particleRadiusUm,
                        range = 0.2f..6.0f,
                        valueDisplay = "${round(particleRadiusUm * 10f) / 10f} µm",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = { particleRadiusUm = it }
                    )
                }

                // Viscosity Slider
                PhysicsSliderControl(
                    title = "Medium Viscosity η",
                    value = mediumViscosityCP,
                    range = 0.015f..150.0f,
                    valueDisplay = "${round(mediumViscosityCP * 10f) / 10f} cP",
                    accentColor = CyanNeon,
                    onValueChange = { mediumViscosityCP = it }
                )

                // Visual Layer Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = showSolventMolecules,
                        onClick = { showSolventMolecules = !showSolventMolecules },
                        label = { Text("Solvent H₂O", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CyanNeon
                        )
                    )
                    FilterChip(
                        selected = showDisplacementRadius,
                        onClick = { showDisplacementRadius = !showDisplacementRadius },
                        label = { Text("Einstein 4Dt", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CoralNeon
                        )
                    )
                    FilterChip(
                        selected = showCollisionVectors,
                        onClick = { showCollisionVectors = !showCollisionVectors },
                        label = { Text("Thermal Kick", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberVibrant.copy(alpha = 0.25f),
                            selectedLabelColor = AmberVibrant
                        )
                    )
                }

                // Action Controls: Play/Pause, Re-center, and Reset
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
                        modifier = Modifier.height(34.dp).weight(1.1f)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Freeze Walk" else "▶ Run Walk",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalButton(
                        onClick = { resetTrajectory() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ScienceDarkSurfaceVariant,
                            contentColor = CyanNeon
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(0.9f)
                    ) {
                        Text(
                            text = "📍 Clear Trace",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            applyPreset(BrownianPreset.POLLEN_WATER)
                            resetTrajectory()
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
