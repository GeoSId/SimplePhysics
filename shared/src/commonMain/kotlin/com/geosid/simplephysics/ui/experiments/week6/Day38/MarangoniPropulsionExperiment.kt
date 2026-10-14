package com.geosid.simplephysics.ui.experiments.week6.Day38

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

/**
 * Presets for Day 38: Marangoni Surface Tension Boat.
 */
enum class MarangoniPreset(
    val label: String,
    val title: String,
    val fuelName: String,
    val surfactantTensionMNm: Float, // mN/m (e.g. soap ~ 25 mN/m)
    val boatLengthCm: Float,
    val boatWidthCm: Float,
    val boatMassGrams: Float,
    val isCamphorOscillator: Boolean,
    val isTearsOfWine: Boolean,
    val desc: String
) {
    DISH_SOAP(
        label = "Dish Soap",
        title = "Classic Dish Soap Speedboat",
        fuelName = "Dish Detergent (Surfactant)",
        surfactantTensionMNm = 25.5f,
        boatLengthCm = 5.0f,
        boatWidthCm = 2.5f,
        boatMassGrams = 0.45f,
        isCamphorOscillator = false,
        isTearsOfWine = false,
        desc = "Liquid soap drops surface tension from 72.8 to 25.5 mN/m, generating an explosive capillary thrust surge"
    ),
    ISOPROPYL_ALCOHOL(
        label = "Alcohol Fuel",
        title = "Ethanol / Isopropyl Capillary Drive",
        fuelName = "70% Isopropanol (Alcohol)",
        surfactantTensionMNm = 22.0f,
        boatLengthCm = 4.5f,
        boatWidthCm = 2.2f,
        boatMassGrams = 0.38f,
        isCamphorOscillator = false,
        isTearsOfWine = false,
        desc = "Volatile alcohol produces an ultra-steep surface tension gradient before rapidly evaporating"
    ),
    CAMPHOR_DANCE(
        label = "Camphor Dance",
        title = "Asymmetric Camphor Crystal Motor",
        fuelName = "Camphor Crystal (Self-Oscillator)",
        surfactantTensionMNm = 40.0f,
        boatLengthCm = 3.5f,
        boatWidthCm = 2.0f,
        boatMassGrams = 0.25f,
        isCamphorOscillator = true,
        isTearsOfWine = false,
        desc = "Asymmetric dissolution of solid camphor produces continuous chaotic rotation and darting motions"
    ),
    OLIVE_OIL(
        label = "Olive Oil",
        title = "Lipid Monolayer Low-Gradient Cruise",
        fuelName = "Vegetable Lipid Oil",
        surfactantTensionMNm = 32.0f,
        boatLengthCm = 5.5f,
        boatWidthCm = 2.8f,
        boatMassGrams = 0.50f,
        isCamphorOscillator = false,
        isTearsOfWine = false,
        desc = "Moderate surface tension depression creates a smooth, prolonged gliding motion"
    ),
    TEARS_OF_WINE(
        label = "Tears of Wine",
        title = "Alcohol Meniscus Climbing Marangoni Flow",
        fuelName = "Wine Alcohol Marangoni Sheet",
        surfactantTensionMNm = 28.0f,
        boatLengthCm = 4.0f,
        boatWidthCm = 2.0f,
        boatMassGrams = 0.30f,
        isCamphorOscillator = false,
        isTearsOfWine = true,
        desc = "Differential evaporation creates vertical surface tension gradient that pumps fluid upward into falling tears"
    )
}

@Composable
fun MarangoniPropulsionExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(MarangoniPreset.DISH_SOAP) }

    // Physical Parameters
    var waterTensionMNm by remember { mutableStateOf(72.8f) } // Pure water at 20°C (mN/m)
    var surfactantTensionMNm by remember { mutableStateOf(currentPreset.surfactantTensionMNm) }
    var boatWidthCm by remember { mutableStateOf(currentPreset.boatWidthCm) }
    var boatMassGrams by remember { mutableStateOf(currentPreset.boatMassGrams) }

    // Simulation Display Toggles
    var showSurfaceTensionGradient by remember { mutableStateOf(true) }
    var showMarangoniStreamlines by remember { mutableStateOf(true) }
    var showForceVectors by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(true) }

    // Dynamic Simulation State
    var boatPosNormX by remember { mutableStateOf(0.15f) }
    var boatPosNormY by remember { mutableStateOf(0.42f) }
    var boatSpeedMs by remember { mutableStateOf(0.0f) }
    var surfactantSpreadRadiusPx by remember { mutableStateOf(0.0f) }
    var isSurfactantActive by remember { mutableStateOf(true) }
    var launchTrigger by remember { mutableStateOf(0) }

    // Animation for water ripple waves and surfactant dissolution
    val infiniteTransition = rememberInfiniteTransition()
    val ripplePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRunning) 1400 else 999999, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Derived Physical Quantities (SI Units)
    val deltaGamma = (waterTensionMNm - surfactantTensionMNm).coerceAtLeast(0f) * 1e-3f // N/m
    val boatWidthM = (boatWidthCm / 100f).coerceAtLeast(0.005f)
    val boatMassKg = (boatMassGrams / 1000f).coerceAtLeast(0.0001f)
    val waterDensity = 1000f // kg/m³
    val dragCoeff = 0.85f // Form drag for floating bluff paper boat
    val submergedAreaM2 = (boatWidthCm * 0.01f) * (0.002f) // ~2mm draft depth
    val viscousShearCoeff = 0.012f // Viscous drag proportionality

    // 1. Net Marangoni Capillary Force: F_net = Delta_gamma * width
    val rawCapillaryForce = deltaGamma * boatWidthM

    // Dynamic flight integration loop
    LaunchedEffect(currentPreset, launchTrigger, isRunning) {
        if (!isRunning) return@LaunchedEffect
        boatPosNormX = 0.14f
        boatPosNormY = 0.42f
        boatSpeedMs = 0f
        surfactantSpreadRadiusPx = 6f
        isSurfactantActive = true

        val startTime = withFrameNanos { it }
        var lastTime = startTime

        while (boatPosNormX < 0.88f) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            lastTime = now

            // Surfactant spreading and surface saturation decay
            surfactantSpreadRadiusPx += 65f * dt
            val saturationFactor = (1.0f - (surfactantSpreadRadiusPx / 350f)).coerceIn(0.05f, 1.0f)
            val activeCapillaryForce = rawCapillaryForce * saturationFactor

            // 2. Hydrodynamic drag force: F_drag = 0.5 * rho * C_d * A * v^2 + viscous
            val dragForce = 0.5f * waterDensity * dragCoeff * submergedAreaM2 * boatSpeedMs * boatSpeedMs +
                    viscousShearCoeff * boatSpeedMs

            // Acceleration & velocity update
            val acceleration = (activeCapillaryForce - dragForce) / boatMassKg
            boatSpeedMs = (boatSpeedMs + acceleration * dt).coerceAtLeast(0f)

            // Convert velocity to canvas coordinate displacement
            val dxNorm = (boatSpeedMs * dt * 0.45f)
            boatPosNormX = (boatPosNormX + dxNorm).coerceIn(0.05f, 0.90f)

            if (currentPreset.isCamphorOscillator) {
                // Chaotic darting and spinning oscillations
                val wobble = sin(now * 1e-8f) * 0.002f
                boatPosNormY = (boatPosNormY + wobble).coerceIn(0.25f, 0.60f)
            }
        }
    }

    // Terminal velocity estimate: v_term = sqrt(F_cap / (0.5 * rho * Cd * A))
    val estimatedTerminalVelocity = sqrt(rawCapillaryForce / max(0.5f * waterDensity * dragCoeff * submergedAreaM2, 1e-6f))

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Tap '💧 Drop Soap' to blast the boat forward! Surface tension gradient pulls clean water ahead.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            launchTrigger++
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            boatPosNormX = (boatPosNormX + dragAmount.x / size.width).coerceIn(0.08f, 0.88f)
                            boatPosNormY = (boatPosNormY + dragAmount.y / size.height).coerceIn(0.20f, 0.65f)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Water Basin Dimensions (Elevated origin: cy at 0.42 * h)
                val cy = h * 0.42f
                val basinLeft = w * 0.05f
                val basinRight = w * 0.95f
                val basinTop = h * 0.12f
                val basinBottom = h * 0.72f
                val basinW = basinRight - basinLeft
                val basinH = basinBottom - basinTop

                val boatX = w * boatPosNormX
                val boatY = h * boatPosNormY

                // 1. Water Basin Surface Heatmap (Surface Tension Field)
                drawWaterBasin(
                    basinLeft = basinLeft,
                    basinTop = basinTop,
                    basinW = basinW,
                    basinH = basinH,
                    boatX = boatX,
                    boatY = boatY,
                    surfactantSpreadRadiusPx = surfactantSpreadRadiusPx,
                    waterTensionMNm = waterTensionMNm,
                    surfactantTensionMNm = surfactantTensionMNm,
                    showSurfaceTensionGradient = showSurfaceTensionGradient,
                    ripplePhase = ripplePhase
                )

                // 2. Marangoni Interfacial Outward Streamlines (Flow from low to high gamma)
                if (showMarangoniStreamlines) {
                    drawMarangoniWakeStreamlines(
                        boatX = boatX,
                        boatY = boatY,
                        spreadRadiusPx = surfactantSpreadRadiusPx,
                        ripplePhase = ripplePhase
                    )
                }

                // 3. Floating Miniature Paper Boat Model
                drawMiniatureBoat(
                    boatX = boatX,
                    boatY = boatY,
                    lengthPx = currentPreset.boatLengthCm * 14f,
                    widthPx = currentPreset.boatWidthCm * 14f,
                    preset = currentPreset,
                    speedMs = boatSpeedMs
                )

                // 4. Force Vectors (Forward Capillary Pull vs Rear Viscous Drag)
                if (showForceVectors) {
                    drawCapillaryForceVectors(
                        boatX = boatX,
                        boatY = boatY,
                        capillaryForceN = rawCapillaryForce,
                        dragForceN = 0.5f * waterDensity * dragCoeff * submergedAreaM2 * boatSpeedMs * boatSpeedMs
                    )
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 38: Marangoni Surface Tension Boat",
                items = listOf(
                    "Marangoni Shear Law" to "τ = dγ/dx = μ · (∂u/∂z)",
                    "Clean Water (γ_front)" to "${round(waterTensionMNm * 10f) / 10f} mN/m",
                    "Surfactant (γ_rear)" to "${round(surfactantTensionMNm * 10f) / 10f} mN/m",
                    "Capillary Force (F_net)" to "${round(rawCapillaryForce * 1_000_000f) / 1000f} mN",
                    "Boat Speed (v)" to "${round(boatSpeedMs * 100f * 10f) / 10f} cm/s",
                    "Terminal Velocity (v_max)" to "${round(estimatedTerminalVelocity * 100f) / 10f} cm/s"
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
                    items(MarangoniPreset.values()) { preset ->
                        val isSelected = currentPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                currentPreset = preset
                                surfactantTensionMNm = preset.surfactantTensionMNm
                                boatWidthCm = preset.boatWidthCm
                                boatMassGrams = preset.boatMassGrams
                                launchTrigger++
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

                // Paired Sliders Row 1: Water Tension & Surfactant Tension
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Water Tension (γ_water)",
                        value = waterTensionMNm,
                        range = 50.0f..75.0f,
                        valueDisplay = "${round(waterTensionMNm * 10f) / 10f} mN/m",
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            waterTensionMNm = it
                            launchTrigger++
                        }
                    )

                    PhysicsSliderControl(
                        title = "Soap Tension (γ_soap)",
                        value = surfactantTensionMNm,
                        range = 15.0f..45.0f,
                        valueDisplay = "${round(surfactantTensionMNm * 10f) / 10f} mN/m",
                        accentColor = CoralNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            surfactantTensionMNm = it
                            launchTrigger++
                        }
                    )
                }

                // Paired Sliders Row 2: Boat Width & Boat Mass
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Boat Width (w)",
                        value = boatWidthCm,
                        range = 1.0f..5.0f,
                        valueDisplay = "${round(boatWidthCm * 10f) / 10f} cm",
                        accentColor = EmeraldNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            boatWidthCm = it
                            launchTrigger++
                        }
                    )

                    PhysicsSliderControl(
                        title = "Boat Mass (m)",
                        value = boatMassGrams,
                        range = 0.1f..2.0f,
                        valueDisplay = "${round(boatMassGrams * 100f) / 100f} g",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = { boatMassGrams = it }
                    )
                }

                // Feature Toggle Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showSurfaceTensionGradient,
                        onClick = { showSurfaceTensionGradient = !showSurfaceTensionGradient },
                        label = { Text("Surface Tension Map", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CyanNeon,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    FilterChip(
                        selected = showMarangoniStreamlines,
                        onClick = { showMarangoniStreamlines = !showMarangoniStreamlines },
                        label = { Text("Marangoni Outflow", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CoralNeon,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    FilterChip(
                        selected = showForceVectors,
                        onClick = { showForceVectors = !showForceVectors },
                        label = { Text("Capillary Forces", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldNeon.copy(alpha = 0.25f),
                            selectedLabelColor = EmeraldNeon,
                            containerColor = ScienceDarkSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.height(26.dp)
                    )
                }

                // Action Bar: Drop Soap / Launch, Flush Basin, Pause/Run, and Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { launchTrigger++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "💧 Drop Soap / Launch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            surfactantSpreadRadiusPx = 0f
                            boatPosNormX = 0.14f
                            boatSpeedMs = 0f
                            launchTrigger++
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScienceDarkSurfaceVariant,
                            contentColor = CyanNeon
                        ),
                        border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🌊 Flush Water",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
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
                            .weight(0.9f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            waterTensionMNm = 72.8f
                            surfactantTensionMNm = currentPreset.surfactantTensionMNm
                            boatWidthCm = currentPreset.boatWidthCm
                            boatMassGrams = currentPreset.boatMassGrams
                            launchTrigger++
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
 * 1. Water Basin Surface Heatmap (Surface Tension Field).
 */
private fun DrawScope.drawWaterBasin(
    basinLeft: Float,
    basinTop: Float,
    basinW: Float,
    basinH: Float,
    boatX: Float,
    boatY: Float,
    surfactantSpreadRadiusPx: Float,
    waterTensionMNm: Float,
    surfactantTensionMNm: Float,
    showSurfaceTensionGradient: Boolean,
    ripplePhase: Float
) {
    // Basin Outer Rim
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(basinLeft - 4f, basinTop - 4f),
        size = Size(basinW + 8f, basinH + 8f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
    )
    drawRoundRect(
        color = ScienceBorder.copy(alpha = 0.75f),
        topLeft = Offset(basinLeft - 4f, basinTop - 4f),
        size = Size(basinW + 8f, basinH + 8f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f),
        style = Stroke(2f)
    )

    // Base Clean Water Surface (Deep Oceanic Cyan, gamma ~ 72.8 mN/m)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF09233B), Color(0xFF0B3354), Color(0xFF071C2E)),
            startY = basinTop,
            endY = basinTop + basinH
        ),
        topLeft = Offset(basinLeft, basinTop),
        size = Size(basinW, basinH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
    )

    // Low Surface Tension Surfactant Monolayer Pool (Behind stern of boat)
    if (showSurfaceTensionGradient && surfactantSpreadRadiusPx > 4f) {
        val sternX = boatX - 25f
        val spreadR = surfactantSpreadRadiusPx.coerceIn(8f, basinW * 0.45f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    CoralNeon.copy(alpha = 0.45f), // Low surface tension core (25 mN/m)
                    AmberVibrant.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(sternX, boatY),
                radius = spreadR
            ),
            radius = spreadR,
            center = Offset(sternX, boatY)
        )
    }

    // Gentle Water Ripples along the tank
    val rippleCount = 5
    for (r in 0 until rippleCount) {
        val rx = basinLeft + (basinW * ((r / rippleCount.toFloat() + ripplePhase * 0.05f) % 1.0f))
        drawLine(
            color = CyanNeon.copy(alpha = 0.15f),
            start = Offset(rx, basinTop + 10f),
            end = Offset(rx, basinTop + basinH - 10f),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 16f), ripplePhase * 5f)
        )
    }
}

/**
 * 2. Marangoni Outward Flow Streamlines (Surface fluid pulled from low to high gamma).
 */
private fun DrawScope.drawMarangoniWakeStreamlines(
    boatX: Float,
    boatY: Float,
    spreadRadiusPx: Float,
    ripplePhase: Float
) {
    if (spreadRadiusPx < 8f) return

    val sternX = boatX - 22f
    val streamAngles = listOf(-135f, -90f, -45f, 0f, 45f, 90f, 135f, 180f)

    for (ang in streamAngles) {
        val rad = ang * (PI.toFloat() / 180f)
        val r0 = 12f
        val r1 = (spreadRadiusPx * 0.85f).coerceIn(18f, 140f)

        val startPt = Offset(sternX + cos(rad) * r0, boatY + sin(rad) * r0)
        val endPt = Offset(sternX + cos(rad) * r1, boatY + sin(rad) * r1)

        drawLine(
            color = CoralNeon.copy(alpha = 0.55f),
            start = startPt,
            end = endPt,
            strokeWidth = 1.6f,
            cap = StrokeCap.Round
        )

        // Outward Arrowhead (Pointing away from low surface tension center)
        val headPath = Path().apply {
            moveTo(endPt.x, endPt.y)
            val perpX = -sin(rad) * 4f
            val perpY = cos(rad) * 4f
            val backX = -cos(rad) * 6f
            val backY = -sin(rad) * 6f
            lineTo(endPt.x + backX + perpX, endPt.y + backY + perpY)
            lineTo(endPt.x + backX - perpX, endPt.y + backY - perpY)
            close()
        }
        drawPath(headPath, CoralNeon.copy(alpha = 0.7f))
    }
}

/**
 * 3. Miniature Paper Boat Model.
 */
private fun DrawScope.drawMiniatureBoat(
    boatX: Float,
    boatY: Float,
    lengthPx: Float,
    widthPx: Float,
    preset: MarangoniPreset,
    speedMs: Float
) {
    val halfW = widthPx * 0.5f
    val halfL = lengthPx * 0.5f

    // Boat Hull Path (Sleek pointed bow on right, notched stern on left)
    val bowX = boatX + halfL
    val sternX = boatX - halfL
    val topSideY = boatY - halfW
    val bottomSideY = boatY + halfW
    val notchDepthX = sternX + lengthPx * 0.22f

    val hullPath = Path().apply {
        // Pointed Bow (Front)
        moveTo(bowX, boatY)
        lineTo(boatX, topSideY)
        lineTo(sternX, topSideY)
        // Rear Notch (Where soap droplet sits)
        lineTo(sternX, boatY - halfW * 0.40f)
        lineTo(notchDepthX, boatY)
        lineTo(sternX, boatY + halfW * 0.40f)
        lineTo(sternX, bottomSideY)
        lineTo(boatX, bottomSideY)
        close()
    }

    // Hull Body (Lightweight paper card)
    drawPath(
        path = hullPath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFFFFFFFF)),
            startX = sternX,
            endX = bowX
        )
    )
    drawPath(
        path = hullPath,
        color = ScienceBorder,
        style = Stroke(1.8f)
    )

    // Soap Droplet Reservoir in the rear notch
    drawCircle(
        color = CoralNeon,
        radius = 4.5f,
        center = Offset(notchDepthX - 3f, boatY)
    )
    drawCircle(
        color = Color.White,
        radius = 2.0f,
        center = Offset(notchDepthX - 3f, boatY)
    )

    // Camphor Crystal flake on boat
    if (preset.isCamphorOscillator) {
        drawRect(
            color = Color.White,
            topLeft = Offset(boatX - 6f, boatY - 5f),
            size = Size(10f, 10f)
        )
    }

    // Bow Wavelet / Spray on Front
    if (speedMs > 0.04f) {
        val sprayPath = Path().apply {
            moveTo(bowX, boatY)
            lineTo(bowX + 12f, boatY - 10f)
            moveTo(bowX, boatY)
            lineTo(bowX + 12f, boatY + 10f)
        }
        drawPath(
            path = sprayPath,
            color = CyanNeon.copy(alpha = 0.65f),
            style = Stroke(2f, cap = StrokeCap.Round)
        )
    }
}

/**
 * 4. Capillary Force Vectors (Forward Marangoni pull vs Drag).
 */
private fun DrawScope.drawCapillaryForceVectors(
    boatX: Float,
    boatY: Float,
    capillaryForceN: Float,
    dragForceN: Float
) {
    val bowX = boatX + 35f
    val sternX = boatX - 35f

    // Forward Capillary Pull Vector F_net (Clean water pulling ahead, CyanNeon)
    val capLen = (capillaryForceN * 45_000f).coerceIn(24f, 90f)
    drawLine(
        color = CyanNeon,
        start = Offset(bowX, boatY),
        end = Offset(bowX + capLen, boatY),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )
    val capHead = Path().apply {
        moveTo(bowX + capLen, boatY)
        lineTo(bowX + capLen - 7f, boatY - 4.5f)
        lineTo(bowX + capLen - 7f, boatY + 4.5f)
        close()
    }
    drawPath(capHead, CyanNeon)

    // Backward Viscous Drag Vector F_drag (CoralNeon)
    val dragLen = (dragForceN * 60_000f).coerceIn(12f, 70f)
    drawLine(
        color = CoralNeon,
        start = Offset(sternX, boatY),
        end = Offset(sternX - dragLen, boatY),
        strokeWidth = 2.4f,
        cap = StrokeCap.Round
    )
    val dragHead = Path().apply {
        moveTo(sternX - dragLen, boatY)
        lineTo(sternX - dragLen + 6f, boatY - 4f)
        lineTo(sternX - dragLen + 6f, boatY + 4f)
        close()
    }
    drawPath(dragHead, CoralNeon)
}
