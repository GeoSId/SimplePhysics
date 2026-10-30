package com.geosid.simplephysics.ui.experiments.week7.Day43

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
 * Thermodynamic process presets for Ideal Gas Piston compression/expansion.
 */
enum class ThermodynamicPreset(
    val label: String,
    val title: String,
    val isAdiabatic: Boolean,
    val defaultVolumeFrac: Float,
    val gamma: Float,
    val gasName: String,
    val desc: String
) {
    ADIABATIC_AIR(
        label = "Adiabatic Air",
        title = "Adiabatic Compression (Q = 0)",
        isAdiabatic = true,
        defaultVolumeFrac = 0.45f,
        gamma = 1.40f,
        gasName = "Air (Diatomic γ = 1.40)",
        desc = "Rapid compression prevents heat exchange: mechanical work directly spikes internal kinetic energy and temperature"
    ),
    ISOTHERMAL_BATH(
        label = "Isothermal Bath",
        title = "Isothermal Compression (Const T)",
        isAdiabatic = false,
        defaultVolumeFrac = 0.50f,
        gamma = 1.40f,
        gasName = "Air in Thermal Bath",
        desc = "Slow quasi-static compression in a heat bath maintains constant T = 300 K while pressure inversely doubles"
    ),
    DIESEL_STROKE(
        label = "Diesel Compression",
        title = "Diesel Engine Ignition Stroke",
        isAdiabatic = true,
        defaultVolumeFrac = 0.25f,
        gamma = 1.40f,
        gasName = "Fuel-Air Mix",
        desc = "High compression ratio spikes gas temperature above fuel auto-ignition threshold (~800 K) purely by mechanical work"
    ),
    MONATOMIC_HE(
        label = "Helium γ=1.67",
        title = "Monatomic Noble Gas",
        isAdiabatic = true,
        defaultVolumeFrac = 0.40f,
        gamma = 1.667f,
        gasName = "Helium (Monatomic γ = 5/3)",
        desc = "With only 3 translational degrees of freedom, monatomic helium exhibits much steeper adiabatic pressure/temperature spikes"
    )
}

data class GasMolecule(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color
)

@Composable
fun IdealGasPistonExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(ThermodynamicPreset.ADIABATIC_AIR) }

    // Process Mode: true = Adiabatic (Q=0, T changes), false = Isothermal (T constant)
    var isAdiabatic by remember { mutableStateOf(currentPreset.isAdiabatic) }

    // Volume fraction: 0.22f (high compression) to 0.95f (expanded)
    var volumeFraction by remember { mutableStateOf(currentPreset.defaultVolumeFrac) }

    // Ratio of specific heats Cp/Cv
    var gamma by remember { mutableStateOf(currentPreset.gamma) }

    // Simulation running state
    var isRunning by remember { mutableStateOf(true) }

    fun applyPreset(preset: ThermodynamicPreset) {
        currentPreset = preset
        isAdiabatic = preset.isAdiabatic
        volumeFraction = preset.defaultVolumeFrac
        gamma = preset.gamma
    }

    // Baseline conditions: T0 = 300K, P0 = 1.0 atm at V0 = 1.0, n = 1.0 mol
    val t0K = 300f
    val p0Atm = 1.0f

    // Derived Thermodynamic Variables
    val temperatureK = if (isAdiabatic) {
        t0K * (1.0f / volumeFraction).pow(gamma - 1f)
    } else {
        t0K
    }

    val pressureAtm = if (isAdiabatic) {
        p0Atm * (1.0f / volumeFraction).pow(gamma)
    } else {
        p0Atm / volumeFraction
    }

    // Work done on the gas W_on = - \int P dV (positive when compressed from V=1.0)
    // For adiabatic diatomic/monatomic gas: W_on = ΔU = n*Cv*ΔT = (n*R/(gamma-1)) * (T - T0)
    // 1 mol * R (8.314) / (gamma - 1)
    val cvJoulePerMolK = 8.314f / (gamma - 1f)
    val workJoules = if (isAdiabatic) {
        cvJoulePerMolK * (temperatureK - t0K)
    } else {
        // Isothermal: W_on = - nRT * ln(V/V0) = nRT * ln(1/V)
        8.314f * t0K * ln(1.0f / volumeFraction)
    }

    // Internal Energy ΔU = Q + W_on (First Law of Thermodynamics)
    val deltaUJoules = if (isAdiabatic) workJoules else 0f
    val heatQJoules = if (isAdiabatic) 0f else -workJoules

    // Gas Molecules
    val moleculeCount = 38
    val molecules = remember {
        val list = mutableStateListOf<GasMolecule>()
        val colors = listOf(CyanNeon, CoralNeon, AmberVibrant, EmeraldNeon)
        for (i in 0 until moleculeCount) {
            val speed = 250f
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            list.add(
                GasMolecule(
                    x = Random.nextFloat() * 0.90f + 0.05f,
                    y = Random.nextFloat() * 0.90f + 0.05f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = colors[i % colors.size]
                )
            )
        }
        list
    }

    // Particle simulation loop
    LaunchedEffect(volumeFraction, temperatureK, isRunning) {
        if (!isRunning) return@LaunchedEffect
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.035f)
            lastTime = now

            // RMS molecular speed factor scales with sqrt(T / T0)
            val speedScale = sqrt(temperatureK / t0K)

            molecules.forEach { m ->
                m.x += (m.vx * speedScale / 380f) * dt
                m.y += (m.vy * speedScale / 380f) * dt

                // Elastic collision with side walls (x: 0 to 1)
                if (m.x <= 0.04f) {
                    m.x = 0.04f
                    m.vx = abs(m.vx)
                } else if (m.x >= 0.96f) {
                    m.x = 0.96f
                    m.vx = -abs(m.vx)
                }

                // Elastic collision with bottom floor (y = 1) and top piston head (y = 1 - volumeFraction)
                val pistonY = 1.0f - volumeFraction
                if (m.y <= pistonY + 0.04f) {
                    m.y = pistonY + 0.04f
                    m.vy = abs(m.vy)
                } else if (m.y >= 0.96f) {
                    m.y = 0.96f
                    m.vy = -abs(m.vy)
                }
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag the metal piston vertically to compress gas molecules. Track real-time p-V work, temperature, and indicator curves!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaV = -dragAmount.y / (size.height * 0.50f)
                            volumeFraction = (volumeFraction + deltaV).coerceIn(0.22f, 0.95f)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated Origin Layout Rules (Rule 14 in AGENT.md):
                // Elevated canvas origin cy ≈ 0.40f * h, keeping the bottom 30-35% clear
                val cylW = min(w * 0.38f, 210.dp.toPx())
                val cylH = min(h * 0.70f, 290.dp.toPx())
                val cylLeft = w * 0.06f
                val cylBottom = h * 0.84f
                val cylTop = cylBottom - cylH

                // Piston geometry
                val gasHeight = cylH * volumeFraction
                val pistonHeadY = cylBottom - gasHeight

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

                // 2. Cylinder Glass Chamber Walls & Heavy Base
                drawCylinder(cylLeft, cylTop, cylW, cylH, cylBottom)

                // 3. Gas Chamber Thermal Glow (Scales from Cyan at 300K to Vibrant Red/Amber at >700K)
                val thermalProgress = ((temperatureK - 300f) / 450f).coerceIn(0f, 1f)
                val heatColor = lerp(
                    CyanNeon.copy(alpha = 0.14f),
                    CoralNeon.copy(alpha = 0.42f),
                    thermalProgress
                )
                drawRect(
                    color = heatColor,
                    topLeft = Offset(cylLeft + 6f, pistonHeadY),
                    size = Size(cylW - 12f, gasHeight)
                )

                // 4. Gas Molecules with Velocity Trails
                molecules.forEach { m ->
                    val px = cylLeft + 6f + m.x * (cylW - 12f)
                    val py = cylBottom - (1f - m.y) * cylH
                    if (py in (pistonHeadY + 4f)..cylBottom) {
                        // Velocity indicator trail
                        val speedNorm = sqrt(temperatureK / 300f)
                        val trailLen = 8f * speedNorm
                        val trailEnd = Offset(px - (m.vx / 380f) * trailLen, py - (m.vy / 380f) * trailLen)
                        drawLine(
                            color = m.color.copy(alpha = 0.45f),
                            start = Offset(px, py),
                            end = trailEnd,
                            strokeWidth = 1.6f
                        )

                        // Molecule Core
                        drawCircle(
                            color = m.color,
                            radius = 4.2f,
                            center = Offset(px, py)
                        )
                    }
                }

                // 5. Piston Head, Shaft & Dynamic Push Handle
                drawPiston(cylLeft, pistonHeadY, cylW, cylTop)

                // Force Vector on Piston Face (Gas Pressure Push Back)
                val pVecLen = (pressureAtm * 12f).coerceIn(10f, 65f)
                val pVecX = cylLeft + cylW * 0.5f
                drawLine(
                    color = AmberVibrant,
                    start = Offset(pVecX, pistonHeadY),
                    end = Offset(pVecX, pistonHeadY - pVecLen),
                    strokeWidth = 2.4f,
                    cap = StrokeCap.Round
                )
                val pHead = Path().apply {
                    moveTo(pVecX, pistonHeadY - pVecLen - 4f)
                    lineTo(pVecX - 4f, pistonHeadY - pVecLen + 4f)
                    lineTo(pVecX + 4f, pistonHeadY - pVecLen + 4f)
                    close()
                }
                drawPath(pHead, AmberVibrant)

                // 6. Real-Time p-V Indicator Diagram (Right Viewport)
                val pVLeft = w * 0.49f
                val pVTop = h * 0.10f
                val pVSize = min(w * 0.45f, h * 0.72f)
                drawPVDiagram(
                    left = pVLeft,
                    top = pVTop,
                    size = pVSize,
                    currentV = volumeFraction,
                    currentP = pressureAtm,
                    isAdiabatic = isAdiabatic,
                    gamma = gamma
                )
            }
        },
        hudContent = {
            // Rule 13: Transparent HUD card so physical apparatus & trajectories shine through cleanly
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 43: Ideal Gas Piston & p-V Work",
                items = listOf(
                    "Process Mode" to if (isAdiabatic) "Adiabatic (Q = 0, γ = $gamma)" else "Isothermal (Const T = 300 K)",
                    "Pressure (p)" to "${round(pressureAtm * 100f) / 100f} atm (${round(pressureAtm * 101.325f).toInt()} kPa)",
                    "Volume (V)" to "${(volumeFraction * 100).toInt()}% (V/V₀ = ${round(volumeFraction * 100f) / 100f})",
                    "Temperature (T)" to "${round(temperatureK).toInt()} K (${(round(temperatureK) - 273.15f).toInt()} °C)",
                    "Work Done on Gas (W)" to "${round(workJoules).toInt()} J",
                    "Internal Energy (ΔU)" to "${round(deltaUJoules).toInt()} J",
                    "Heat Exchange (Q)" to if (isAdiabatic) "0 J (Insulated)" else "${round(heatQJoules).toInt()} J"
                )
            )
        },
        controlsContent = {
            // Rule 15: Compact Controls Deck with paired controls, compact padding, 0 nested verticalScroll
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Selector Chips
                Text(
                    text = "SELECT THERMODYNAMIC PROCESS",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThermodynamicPreset.entries.forEach { preset ->
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

                // Process Mode Quick Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = isAdiabatic,
                        onClick = { isAdiabatic = true },
                        label = { Text("Adiabatic (Q = 0, T Var)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CoralNeon
                        )
                    )
                    FilterChip(
                        selected = !isAdiabatic,
                        onClick = { isAdiabatic = false },
                        label = { Text("Isothermal (Const T)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CyanNeon
                        )
                    )
                }

                // Volume Compression Slider
                PhysicsSliderControl(
                    title = "Piston Compression / Volume (V)",
                    value = volumeFraction,
                    range = 0.22f..0.95f,
                    valueDisplay = "${(volumeFraction * 100).toInt()}% V₀",
                    accentColor = if (isAdiabatic) CoralNeon else CyanNeon,
                    onValueChange = { volumeFraction = it }
                )

                // Action Controls: Fast Compression, Pause/Run, and Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            volumeFraction = if (volumeFraction > 0.40f) 0.26f else 0.85f
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralNeon,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1.1f)
                    ) {
                        Text(
                            text = if (volumeFraction > 0.40f) "⚡ Slam Piston" else "💨 Expand Piston",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalButton(
                        onClick = { isRunning = !isRunning },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ScienceDarkSurfaceVariant,
                            contentColor = CyanNeon
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(0.9f)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Freeze" else "▶ Run",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            applyPreset(ThermodynamicPreset.ADIABATIC_AIR)
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

// Drawing Helpers
private fun DrawScope.drawCylinder(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    bottom: Float
) {
    val wallThick = 6f
    // Left & Right Glass Walls
    drawRect(Color(0x66FFFFFF), Offset(left, top), Size(wallThick, height))
    drawRect(Color(0x66FFFFFF), Offset(left + width - wallThick, top), Size(wallThick, height))
    // Heavy Bottom Base
    drawRoundRect(
        color = Color(0xFF455A64),
        topLeft = Offset(left - 8f, bottom),
        size = Size(width + 16f, 16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )

    // Tick marks and Volume Labels on cylinder
    for (i in 1..4) {
        val ty = bottom - (height * 0.2f * i)
        drawLine(Color(0x88FFFFFF), Offset(left + wallThick, ty), Offset(left + wallThick + 12f, ty), 1.5f)
    }
}

private fun DrawScope.drawPiston(
    cylLeft: Float,
    pistonY: Float,
    cylWidth: Float,
    cylTop: Float
) {
    val pistonHeadH = 20f
    val shaftW = 16f
    val centerX = cylLeft + cylWidth / 2f

    // Piston Shaft
    drawRect(
        color = Color(0xFFB0BEC5),
        topLeft = Offset(centerX - shaftW / 2f, pistonY - 80f),
        size = Size(shaftW, 80f)
    )

    // Piston Handle at Top
    drawRoundRect(
        color = Color(0xFF37474F),
        topLeft = Offset(centerX - 30f, pistonY - 94f),
        size = Size(60f, 14f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )

    // Metal Piston Head with Gasket Rings
    drawRoundRect(
        color = Color(0xFF78909C),
        topLeft = Offset(cylLeft + 6f, pistonY),
        size = Size(cylWidth - 12f, pistonHeadH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
    )
    // Black Rubber O-Ring Seals
    drawLine(Color.Black, Offset(cylLeft + 6f, pistonY + 5f), Offset(cylLeft + cylWidth - 6f, pistonY + 5f), 2.2f)
    drawLine(Color.Black, Offset(cylLeft + 6f, pistonY + 14f), Offset(cylLeft + cylWidth - 6f, pistonY + 14f), 2.2f)
}

private fun DrawScope.drawPVDiagram(
    left: Float,
    top: Float,
    size: Float,
    currentV: Float,
    currentP: Float,
    isAdiabatic: Boolean,
    gamma: Float
) {
    // Transparent Card background matching HUD rules
    drawRoundRect(
        color = ScienceDarkSurface.copy(alpha = 0.85f),
        topLeft = Offset(left - 10f, top - 15f),
        size = Size(size + 20f, size + 30f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
    )
    drawRoundRect(
        color = ScienceBorder.copy(alpha = 0.35f),
        topLeft = Offset(left - 10f, top - 15f),
        size = Size(size + 20f, size + 30f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
        style = Stroke(width = 1.2f)
    )

    // Axes
    val origin = Offset(left + 22f, top + size - 12f)
    val maxPxW = size - 36f
    val maxPxH = size - 36f

    // P-axis (vertical) & V-axis (horizontal)
    drawLine(Color.White.copy(alpha = 0.7f), origin, Offset(origin.x, origin.y - maxPxH), 1.8f)
    drawLine(Color.White.copy(alpha = 0.7f), origin, Offset(origin.x + maxPxW, origin.y), 1.8f)

    // Axis Arrows
    drawLine(Color.White.copy(alpha = 0.7f), Offset(origin.x, origin.y - maxPxH), Offset(origin.x - 3f, origin.y - maxPxH + 6f), 1.5f)
    drawLine(Color.White.copy(alpha = 0.7f), Offset(origin.x, origin.y - maxPxH), Offset(origin.x + 3f, origin.y - maxPxH + 6f), 1.5f)
    drawLine(Color.White.copy(alpha = 0.7f), Offset(origin.x + maxPxW, origin.y), Offset(origin.x + maxPxW - 6f, origin.y - 3f), 1.5f)
    drawLine(Color.White.copy(alpha = 0.7f), Offset(origin.x + maxPxW, origin.y), Offset(origin.x + maxPxW - 6f, origin.y + 3f), 1.5f)

    // Indicator Curves: Plot both Isotherm (Cyan) and Adiabat (Coral) for comparison
    val steps = 30

    // Reference Isotherm (T = 300K, P = 1 / V)
    val isothermPath = Path().apply {
        for (i in 0..steps) {
            val vNorm = 0.22f + (i.toFloat() / steps) * 0.73f
            val pNorm = 1.0f / vNorm
            val px = origin.x + (vNorm - 0.22f) / 0.73f * maxPxW
            val py = origin.y - (pNorm / 6.0f).coerceIn(0f, 1f) * maxPxH
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
    }
    drawPath(
        path = isothermPath,
        color = CyanNeon.copy(alpha = if (isAdiabatic) 0.35f else 0.90f),
        style = Stroke(
            width = if (!isAdiabatic) 2.4f else 1.2f,
            pathEffect = if (isAdiabatic) PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f) else null
        )
    )

    // Adiabat Curve (P = 1 / V^gamma)
    val adiabatPath = Path().apply {
        for (i in 0..steps) {
            val vNorm = 0.22f + (i.toFloat() / steps) * 0.73f
            val pNorm = (1.0f / vNorm).pow(gamma)
            val px = origin.x + (vNorm - 0.22f) / 0.73f * maxPxW
            val py = origin.y - (pNorm / 6.0f).coerceIn(0f, 1f) * maxPxH
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
    }
    drawPath(
        path = adiabatPath,
        color = CoralNeon.copy(alpha = if (isAdiabatic) 0.95f else 0.35f),
        style = Stroke(
            width = if (isAdiabatic) 2.4f else 1.2f,
            pathEffect = if (!isAdiabatic) PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f) else null
        )
    )

    // Current thermodynamic state point
    val curPx = origin.x + (currentV - 0.22f) / 0.73f * maxPxW
    val curPy = origin.y - (currentP / 6.0f).coerceIn(0f, 1f) * maxPxH

    // Work area indicator (Fill under curve to origin)
    val activeColor = if (isAdiabatic) CoralNeon else CyanNeon
    drawLine(
        color = activeColor.copy(alpha = 0.5f),
        start = Offset(curPx, origin.y),
        end = Offset(curPx, curPy),
        strokeWidth = 1.2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
    )

    drawCircle(color = activeColor.copy(alpha = 0.30f), radius = 10f, center = Offset(curPx, curPy))
    drawCircle(color = AmberVibrant, radius = 5.5f, center = Offset(curPx, curPy))
    drawCircle(color = Color.White, radius = 2.5f, center = Offset(curPx, curPy))
}
