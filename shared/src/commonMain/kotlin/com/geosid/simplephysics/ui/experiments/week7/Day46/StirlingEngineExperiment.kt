package com.geosid.simplephysics.ui.experiments.week7.Day46

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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

/**
 * DAY 46: Stirling Cycle Heat Engine
 *
 * Implements a high-precision, interactive thermodynamic Alpha/Beta Stirling Engine:
 * - Schmidt Analysis equation for closed gas volume & instantaneous pressure:
 *     P(θ) = (n · R) / [ V_H(θ)/T_H + V_C(θ)/T_C + V_D/T_D ]
 * - 4 Thermodynamic strokes: Isothermal Expansion -> Isochoric Cooling -> Isothermal Compression -> Isochoric Heating.
 * - Crankshaft kinematics with 90° phase offset between Hot and Cold cylinders.
 * - Flywheel rotational dynamics with net pressure torque, mechanical friction, and generator load:
 *     I · (dω/dt) = τ_pressure - τ_friction - τ_load
 * - Gas particle thermal kinetics, regenerator heat transfer mesh, and live P-V indicator diagram.
 */

private enum class StirlingPreset {
    HIGH_PERFORMANCE_BETA,
    CANDLE_TABLETOP,
    PALM_POWER_LTD,
    STALLED_LOAD_BRAKE
}

private data class StirlingGasMolecule(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var tempK: Float
)

@Composable
fun StirlingEngineExperiment(
    modifier: Modifier = Modifier
) {
    // Thermodynamic state variables
    var hotTempK by remember { mutableStateOf(780f) }       // Hot reservoir temperature (K)
    var coldTempK by remember { mutableStateOf(295f) }      // Cold reservoir temperature (K)
    var loadTorqueNm by remember { mutableStateOf(0.12f) }  // External generator braking load (N*m)
    var regeneratorEff by remember { mutableStateOf(0.85f) } // Regenerator thermal effectiveness (0..1)
    var isRunning by remember { mutableStateOf(true) }
    var showPVDiagram by remember { mutableStateOf(true) }

    // Kinematic & Dynamic State
    var crankAngleRad by remember { mutableStateOf(0f) }
    var angularVelocityRadS by remember { mutableStateOf(18f) }
    var indicatedWorkJ by remember { mutableStateOf(4.2f) }
    var mechanicalPowerW by remember { mutableStateOf(12.5f) }
    var currentPressureKPa by remember { mutableStateOf(125f) }
    var totalVolumeCm3 by remember { mutableStateOf(85f) }

    // Touch gesture tracking for manual flywheel spin
    var lastDragAngle by remember { mutableStateOf<Float?>(null) }

    // Microscopic gas molecules shuttling between cylinders
    val gasMolecules = remember {
        List(42) { i ->
            val isHotSide = (i % 2 == 0)
            StirlingGasMolecule(
                x = if (isHotSide) 0.25f + (i % 10) * 0.02f else 0.70f + (i % 10) * 0.02f,
                y = 0.42f + (i % 5) * 0.015f,
                vx = (sin(i.toFloat()) * 0.012f),
                vy = (cos(i.toFloat()) * 0.008f),
                tempK = if (isHotSide) 750f else 300f
            )
        }
    }

    // Engine Geometry Constants
    val strokeCm = 3.6f
    val boreCm = 3.2f
    val pistonAreaCm2 = (PI.toFloat() * (boreCm / 2f).pow(2))
    val clearanceVolCm3 = 12f
    val deadVolCm3 = 18f
    val phaseOffsetRad = (PI / 2.0).toFloat() // 90-degree phase shift
    val flywheelInertia = 0.018f // kg * m^2
    val gasConstantMolesNR = 0.038f // n * R product in J/K

    // Physics frame loop
    LaunchedEffect(isRunning, hotTempK, coldTempK, loadTorqueNm, regeneratorEff) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nowNanos ->
                if (lastNanos == 0L) {
                    lastNanos = nowNanos
                    return@withFrameNanos
                }
                val dt = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastNanos = nowNanos

                if (!isRunning) return@withFrameNanos

                // 1. Instantaneous Hot & Cold Piston Displacements (0..1 normalized)
                val hotDisp = (1f - cos(crankAngleRad)) * 0.5f
                val coldDisp = (1f - cos(crankAngleRad - phaseOffsetRad)) * 0.5f

                val vHotCm3 = clearanceVolCm3 + pistonAreaCm2 * strokeCm * hotDisp
                val vColdCm3 = clearanceVolCm3 + pistonAreaCm2 * strokeCm * coldDisp
                val vTotal = vHotCm3 + vColdCm3 + deadVolCm3
                totalVolumeCm3 = vTotal

                // 2. Schmidt Equation for Closed Engine Pressure
                val tRegenLogMean = max(1f, (hotTempK - coldTempK) / max(0.01f, ln(hotTempK / max(coldTempK, 1f))))
                val effectiveTRegen = coldTempK + (tRegenLogMean - coldTempK) * regeneratorEff
                val denom = (vHotCm3 / max(hotTempK, 1f)) + (vColdCm3 / max(coldTempK, 1f)) + (deadVolCm3 / max(effectiveTRegen, 1f))

                // Pressure in kPa (approx 100 kPa base atm)
                val pressureKPa = max(40f, (gasConstantMolesNR / max(denom, 0.001f)) * 1000f)
                currentPressureKPa = pressureKPa

                // 3. Pressure Torques on Crankshaft
                val pDiffKPa = pressureKPa - 101.3f // Gauge pressure
                val forceScale = 0.00045f // Geometric torque conversion factor
                val torqueHot = pDiffKPa * sin(crankAngleRad) * forceScale
                val torqueCold = pDiffKPa * sin(crankAngleRad - phaseOffsetRad) * forceScale
                val torqueGas = torqueHot + torqueCold

                // Mechanical losses & generator load
                val frictionTorque = 0.015f + angularVelocityRadS * 0.0018f
                val effectiveLoad = loadTorqueNm * (angularVelocityRadS / 10f).coerceAtMost(1f)
                val netTorque = torqueGas - frictionTorque - effectiveLoad

                // 4. Flywheel Angular Acceleration (I * alpha = tau)
                val alpha = netTorque / flywheelInertia
                angularVelocityRadS = max(0f, angularVelocityRadS + alpha * dt)
                crankAngleRad = (crankAngleRad + angularVelocityRadS * dt) % (2f * PI.toFloat())

                // 5. Work Per Cycle & Power
                val rpm = (angularVelocityRadS * 60f) / (2f * PI.toFloat())
                val carnotEff = max(0f, 1f - (coldTempK / max(hotTempK, 1f)))
                val thermalEff = carnotEff * regeneratorEff * 0.62f
                val deltaVMaxMin = pistonAreaCm2 * strokeCm * 1.4f * 1e-6f // m^3
                val pMeanPa = pressureKPa * 1e3f
                indicatedWorkJ = max(0.1f, pMeanPa * deltaVMaxMin * (hotTempK - coldTempK) / max(hotTempK, 1f) * 0.85f)
                mechanicalPowerW = max(0f, (netTorque + effectiveLoad) * angularVelocityRadS * 0.75f)

                // 6. Thermal Gas Particle Motion
                gasMolecules.forEachIndexed { idx, mol ->
                    val flowDirection = sin(crankAngleRad) // +1: moving Hot -> Cold, -1: Cold -> Hot
                    val targetX = if (flowDirection > 0) 0.68f else 0.28f
                    mol.x += (targetX - mol.x) * 0.035f + mol.vx * (angularVelocityRadS * 0.06f + 0.4f)
                    mol.y += mol.vy
                    if (mol.y < 0.38f || mol.y > 0.46f) mol.vy = -mol.vy
                    mol.x = mol.x.coerceIn(0.18f, 0.78f)

                    // Thermal relaxation
                    if (mol.x < 0.36f) {
                        mol.tempK += (hotTempK - mol.tempK) * 0.1f
                    } else if (mol.x > 0.60f) {
                        mol.tempK += (coldTempK - mol.tempK) * 0.1f
                    } else {
                        // In regenerator
                        val midT = (hotTempK + coldTempK) * 0.5f
                        mol.tempK += (midT - mol.tempK) * 0.05f
                    }
                }
            }
        }
    }

    val carnotEfficiency = max(0f, 1f - (coldTempK / max(hotTempK, 1f)))
    val currentRpm = (angularVelocityRadS * 60f) / (2f * PI.toFloat())

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "🔥 Watch heat convert to mechanical shaft power! Adjust temperatures, brake the flywheel with load, and inspect the real-time P-V cycle.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val w = size.width
                                val h = size.height
                                val flywheelCenter = Offset(w * 0.50f, h * 0.62f)
                                val d = offset - flywheelCenter
                                lastDragAngle = atan2(d.y, d.x)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val w = size.width
                                val h = size.height
                                val flywheelCenter = Offset(w * 0.50f, h * 0.62f)
                                val d = change.position - flywheelCenter
                                val currentAngle = atan2(d.y, d.x)
                                lastDragAngle?.let { prev ->
                                    var delta = currentAngle - prev
                                    if (delta > PI) delta -= (2 * PI).toFloat()
                                    if (delta < -PI) delta += (2 * PI).toFloat()
                                    // Manual spin imparted to flywheel
                                    crankAngleRad = (crankAngleRad + delta) % (2f * PI.toFloat())
                                    angularVelocityRadS = (angularVelocityRadS + delta * 25f).coerceIn(0f, 85f)
                                }
                                lastDragAngle = currentAngle
                            },
                            onDragEnd = { lastDragAngle = null },
                            onDragCancel = { lastDragAngle = null }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw laboratory coordinate background
                drawEngineBackground(w, h)

                // 1. Hot Expansion Cylinder (Left, Red Glow)
                val hotCylLeft = w * 0.12f
                val hotCylTop = h * 0.28f
                val cylWidth = w * 0.26f
                val cylHeight = h * 0.16f
                val hotPistonDisp = (1f - cos(crankAngleRad)) * 0.5f

                drawHotCylinder(
                    x = hotCylLeft,
                    y = hotCylTop,
                    width = cylWidth,
                    height = cylHeight,
                    tempK = hotTempK,
                    pistonDisp = hotPistonDisp
                )

                // 2. Cold Compression Cylinder (Right, Blue Fins)
                val coldCylLeft = w * 0.62f
                val coldCylTop = h * 0.28f
                val coldPistonDisp = (1f - cos(crankAngleRad - phaseOffsetRad)) * 0.5f

                drawColdCylinder(
                    x = coldCylLeft,
                    y = coldCylTop,
                    width = cylWidth,
                    height = cylHeight,
                    tempK = coldTempK,
                    pistonDisp = coldPistonDisp
                )

                // 3. Central Regenerator & Transfer Duct
                drawRegeneratorDuct(
                    startX = hotCylLeft + cylWidth,
                    endX = coldCylLeft,
                    centerY = hotCylTop + cylHeight * 0.5f,
                    ductHeight = cylHeight * 0.40f,
                    effectiveness = regeneratorEff,
                    hotTempK = hotTempK,
                    coldTempK = coldTempK
                )

                // 4. Microscopic Gas Particle Stream
                gasMolecules.forEach { mol ->
                    val px = w * mol.x
                    val py = h * mol.y
                    val tempNorm = ((mol.tempK - coldTempK) / max(hotTempK - coldTempK, 1f)).coerceIn(0f, 1f)
                    val particleColor = lerp(CyanNeon, CoralNeon, tempNorm)
                    drawCircle(
                        color = particleColor.copy(alpha = 0.85f),
                        radius = 2.8.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                // 5. Crank Mechanism & Central Flywheel (Elevated origin cy ≈ 0.44h for compact composition)
                val flywheelCenter = Offset(w * 0.50f, h * 0.65f)
                val flywheelRadius = min(w * 0.22f, h * 0.20f)
                val crankRadius = flywheelRadius * 0.40f

                // Hot Piston Pin position
                val hotPistonPinX = hotCylLeft + cylWidth * 0.25f + (cylWidth * 0.55f) * (1f - hotPistonDisp)
                val hotPistonPinY = hotCylTop + cylHeight * 0.5f
                val hotCrankPin = Offset(
                    flywheelCenter.x + crankRadius * cos(crankAngleRad),
                    flywheelCenter.y + crankRadius * sin(crankAngleRad)
                )

                // Cold Piston Pin position
                val coldPistonPinX = coldCylLeft + (cylWidth * 0.55f) * coldPistonDisp + cylWidth * 0.20f
                val coldPistonPinY = coldCylTop + cylHeight * 0.5f
                val coldCrankPin = Offset(
                    flywheelCenter.x + crankRadius * cos(crankAngleRad - phaseOffsetRad),
                    flywheelCenter.y + crankRadius * sin(crankAngleRad - phaseOffsetRad)
                )

                // Draw Connecting Rods
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(hotPistonPinX, hotPistonPinY),
                    end = hotCrankPin,
                    strokeWidth = 4.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(Color.White, 4.dp.toPx(), Offset(hotPistonPinX, hotPistonPinY))
                drawCircle(AmberVibrant, 4.dp.toPx(), hotCrankPin)

                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(coldPistonPinX, coldPistonPinY),
                    end = coldCrankPin,
                    strokeWidth = 4.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(Color.White, 4.dp.toPx(), Offset(coldPistonPinX, coldPistonPinY))
                drawCircle(AmberVibrant, 4.dp.toPx(), coldCrankPin)

                // Draw Rotating Flywheel
                drawFlywheel(
                    center = flywheelCenter,
                    radius = flywheelRadius,
                    angleRad = crankAngleRad,
                    rpm = currentRpm
                )

                // 6. Inset P-V Indicator Diagram Card (Upper Corner or Toggle)
                if (showPVDiagram) {
                    drawPVIndicatorDiagram(
                        w = w,
                        h = h,
                        currentPressureKPa = currentPressureKPa,
                        currentVolumeCm3 = totalVolumeCm3,
                        hotTempK = hotTempK,
                        coldTempK = coldTempK,
                        clearanceVol = clearanceVolCm3 * 2f + deadVolCm3,
                        maxVol = clearanceVolCm3 * 2f + deadVolCm3 + pistonAreaCm2 * strokeCm * 1.5f
                    )
                }
            }
        },
        hudContent = {
            val statusLabel = when {
                currentRpm > 120f -> "Rapid Power Stroke"
                currentRpm > 15f -> "Steady Idling"
                currentRpm > 0.5f -> "Low-Speed Coasting"
                else -> "Stalled (Thermal/Load Stall)"
            }
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 46: Stirling Cycle Heat Engine",
                items = listOf(
                    "Engine State" to statusLabel,
                    "Rotational Speed" to "${currentRpm.roundToInt()} RPM (${round(angularVelocityRadS * 10f) / 10f} rad/s)",
                    "Mechanical Shaft Power" to "${round(mechanicalPowerW * 10f) / 10f} W",
                    "Indicated Work (W_net)" to "${round(indicatedWorkJ * 10f) / 10f} J (∮ P dV)",
                    "Instantaneous Pressure P(θ)" to "${currentPressureKPa.roundToInt()} kPa",
                    "Carnot Efficiency Limit" to "${(carnotEfficiency * 100f).roundToInt()}%",
                    "Stirling Thermal Efficiency" to "${((carnotEfficiency * regeneratorEff * 0.62f) * 100f).roundToInt()}%",
                    "Braking Load Torque" to "${round(loadTorqueNm * 100f) / 100f} N·m"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preset selection chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = hotTempK > 750f && loadTorqueNm < 0.2f,
                        onClick = {
                            hotTempK = 850f
                            coldTempK = 293f
                            loadTorqueNm = 0.08f
                            regeneratorEff = 0.90f
                            angularVelocityRadS = 35f
                            isRunning = true
                        },
                        label = { Text("High Temp (850K)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = hotTempK in 500f..650f,
                        onClick = {
                            hotTempK = 560f
                            coldTempK = 295f
                            loadTorqueNm = 0.10f
                            regeneratorEff = 0.82f
                            angularVelocityRadS = 22f
                            isRunning = true
                        },
                        label = { Text("Candle Flame", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = hotTempK < 350f,
                        onClick = {
                            hotTempK = 312f
                            coldTempK = 293f
                            loadTorqueNm = 0.02f
                            regeneratorEff = 0.95f
                            angularVelocityRadS = 9f
                            isRunning = true
                        },
                        label = { Text("Palm Heat (LTD)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Paired Sliders 1: Hot Temp & Cold Temp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Hot Temp T_H",
                        value = hotTempK,
                        range = 310f..950f,
                        valueDisplay = "${round(hotTempK).toInt()} K",
                        accentColor = CoralNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { hotTempK = it }
                    )
                    PhysicsSliderControl(
                        title = "Cold Temp T_C",
                        value = coldTempK,
                        range = 260f..350f,
                        valueDisplay = "${round(coldTempK).toInt()} K",
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { coldTempK = it }
                    )
                }

                // Paired Sliders 2: Load Torque & Regenerator Efficiency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Load Torque τ_load",
                        value = loadTorqueNm,
                        range = 0.0f..0.8f,
                        valueDisplay = "${round(loadTorqueNm * 100f) / 100f} N·m",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = { loadTorqueNm = it }
                    )
                    PhysicsSliderControl(
                        title = "Regen Eff ε",
                        value = regeneratorEff,
                        range = 0.20f..0.98f,
                        valueDisplay = "${(regeneratorEff * 100f).roundToInt()}%",
                        accentColor = PurpleNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { regeneratorEff = it }
                    )
                }

                // Action controls row (Kick starter, run toggle, reset)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = {
                                angularVelocityRadS = if (angularVelocityRadS < 5f) 25f else angularVelocityRadS + 15f
                                isRunning = true
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Kick Starter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { isRunning = !isRunning },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isRunning) "Pause" else "Resume", fontSize = 12.sp)
                        }
                    }

                    IconButton(
                        onClick = {
                            hotTempK = 780f
                            coldTempK = 295f
                            loadTorqueNm = 0.12f
                            regeneratorEff = 0.85f
                            crankAngleRad = 0f
                            angularVelocityRadS = 22f
                            isRunning = true
                        }
                    ) {
                        ResetIcon()
                    }
                }
            }
        }
    )
}

// -----------------------------------------------------------------------------
// Canvas Drawing Helpers
// -----------------------------------------------------------------------------

private fun DrawScope.drawEngineBackground(w: Float, h: Float) {
    // Subtle technical grid
    val spacing = 36.dp.toPx()
    var x = 0f
    while (x < w) {
        drawLine(ScienceBorder.copy(alpha = 0.18f), Offset(x, 0f), Offset(x, h), 0.8f)
        x += spacing
    }
    var y = 0f
    while (y < h) {
        drawLine(ScienceBorder.copy(alpha = 0.18f), Offset(0f, y), Offset(w, y), 0.8f)
        y += spacing
    }
}

private fun DrawScope.drawHotCylinder(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    tempK: Float,
    pistonDisp: Float
) {
    val corner = 8f

    // Flame / Heat glow under cylinder
    val glowIntensity = ((tempK - 273f) / 600f).coerceIn(0.1f, 1f)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, CoralNeon.copy(alpha = 0.45f * glowIntensity), Color(0xFFFF5722)),
            startY = y + height,
            endY = y + height + 24.dp.toPx()
        ),
        topLeft = Offset(x, y + height),
        size = Size(width * 0.70f, 24.dp.toPx())
    )

    // Heated cylinder casing
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF7F1D1D), Color(0xFF991B1B), Color(0xFF1E293B)),
            startX = x,
            endX = x + width
        ),
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(corner, corner)
    )
    drawRoundRect(
        color = CoralNeon.copy(alpha = 0.75f),
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = 2.dp.toPx())
    )

    // Hot Expansion Piston head
    val pistonWidth = width * 0.22f
    val pistonLeft = x + (width - pistonWidth - 12f) * (1f - pistonDisp)
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF475569)),
            startX = pistonLeft,
            endX = pistonLeft + pistonWidth
        ),
        topLeft = Offset(pistonLeft, y + 4f),
        size = Size(pistonWidth, height - 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Piston rings
    drawLine(Color(0xFF0F172A), Offset(pistonLeft + 6f, y + 4f), Offset(pistonLeft + 6f, y + height - 4f), 2f)
    drawLine(Color(0xFF0F172A), Offset(pistonLeft + 14f, y + 4f), Offset(pistonLeft + 14f, y + height - 4f), 2f)

    // Heater burner elements (Orange heating coils)
    val coils = 4
    for (i in 0 until coils) {
        val cx = x + 16f + i * 20f
        drawLine(
            color = AmberVibrant.copy(alpha = 0.85f * glowIntensity),
            start = Offset(cx, y + height + 2f),
            end = Offset(cx + 10f, y + height + 16f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawColdCylinder(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    tempK: Float,
    pistonDisp: Float
) {
    val corner = 8f

    // Cooling sink glow
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, CyanNeon.copy(alpha = 0.25f)),
            startY = y - 18.dp.toPx(),
            endY = y
        ),
        topLeft = Offset(x + width * 0.30f, y - 18.dp.toPx()),
        size = Size(width * 0.70f, 18.dp.toPx())
    )

    // Cold cylinder casing
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF0284C7)),
            startX = x,
            endX = x + width
        ),
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(corner, corner)
    )
    drawRoundRect(
        color = CyanNeon.copy(alpha = 0.75f),
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = 2.dp.toPx())
    )

    // Aluminum Cooling fins on cold cylinder top & bottom
    val fins = 6
    for (i in 0 until fins) {
        val fx = x + width * 0.35f + i * 16f
        drawLine(
            color = CyanNeon.copy(alpha = 0.65f),
            start = Offset(fx, y - 12.dp.toPx()),
            end = Offset(fx, y),
            strokeWidth = 2.5f
        )
        drawLine(
            color = CyanNeon.copy(alpha = 0.65f),
            start = Offset(fx, y + height),
            end = Offset(fx, y + height + 12.dp.toPx()),
            strokeWidth = 2.5f
        )
    }

    // Cold Compression Piston head
    val pistonWidth = width * 0.22f
    val pistonLeft = x + 12f + (width - pistonWidth - 24f) * pistonDisp
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF475569), Color(0xFF94A3B8), Color(0xFFE2E8F0)),
            startX = pistonLeft,
            endX = pistonLeft + pistonWidth
        ),
        topLeft = Offset(pistonLeft, y + 4f),
        size = Size(pistonWidth, height - 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawLine(Color(0xFF0F172A), Offset(pistonLeft + 6f, y + 4f), Offset(pistonLeft + 6f, y + height - 4f), 2f)
    drawLine(Color(0xFF0F172A), Offset(pistonLeft + 14f, y + 4f), Offset(pistonLeft + 14f, y + height - 4f), 2f)
}

private fun DrawScope.drawRegeneratorDuct(
    startX: Float,
    endX: Float,
    centerY: Float,
    ductHeight: Float,
    effectiveness: Float,
    hotTempK: Float,
    coldTempK: Float
) {
    val ductWidth = endX - startX
    val ductTop = centerY - ductHeight * 0.5f

    // Transfer pipe
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(CoralNeon.copy(alpha = 0.40f), PurpleNeon.copy(alpha = 0.30f), CyanNeon.copy(alpha = 0.40f)),
            startX = startX,
            endX = endX
        ),
        topLeft = Offset(startX, ductTop),
        size = Size(ductWidth, ductHeight)
    )
    drawRect(
        color = ScienceBorder.copy(alpha = 0.85f),
        topLeft = Offset(startX, ductTop),
        size = Size(ductWidth, ductHeight),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Regenerator wire mesh matrix (Central copper porous regenerator)
    val meshWidth = ductWidth * 0.36f
    val meshLeft = startX + (ductWidth - meshWidth) * 0.5f
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFB45309), Color(0xFFD97706), Color(0xFF78350F)),
            startX = meshLeft,
            endX = meshLeft + meshWidth
        ),
        topLeft = Offset(meshLeft, ductTop + 2f),
        size = Size(meshWidth, ductHeight - 4f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // Mesh lines
    var mx = meshLeft + 4f
    while (mx < meshLeft + meshWidth - 2f) {
        drawLine(
            color = AmberVibrant.copy(alpha = 0.55f * effectiveness),
            start = Offset(mx, ductTop + 2f),
            end = Offset(mx, ductTop + ductHeight - 2f),
            strokeWidth = 1.2f
        )
        mx += 5f
    }
}

private fun DrawScope.drawFlywheel(
    center: Offset,
    radius: Float,
    angleRad: Float,
    rpm: Float
) {
    // Outer heavy rim
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
    drawCircle(
        color = ScienceBorder,
        radius = radius,
        center = center,
        style = Stroke(width = 4.dp.toPx())
    )
    drawCircle(
        color = Color(0xFF64748B),
        radius = radius * 0.88f,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    // 4 Rotating Spokes
    rotate(degrees = angleRad * (180f / PI.toFloat()), pivot = center) {
        val spokeCount = 6
        for (i in 0 until spokeCount) {
            val spokeAngle = i * (2f * PI.toFloat() / spokeCount)
            val endPt = Offset(
                center.x + (radius * 0.88f) * cos(spokeAngle),
                center.y + (radius * 0.88f) * sin(spokeAngle)
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.75f), Color(0xFF94A3B8)),
                    start = center,
                    end = endPt
                ),
                start = center,
                end = endPt,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Heavy counterweight on rim
        drawArc(
            color = AmberVibrant.copy(alpha = 0.85f),
            startAngle = -35f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = Offset(center.x - radius * 0.85f, center.y - radius * 0.85f),
            size = Size(radius * 1.70f, radius * 1.70f),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Hub & Axle Bearing
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE2E8F0), Color(0xFF475569)),
            center = center,
            radius = radius * 0.22f
        ),
        radius = radius * 0.22f,
        center = center
    )
    drawCircle(Color.White, 3.dp.toPx(), center)
}

private fun DrawScope.drawPVIndicatorDiagram(
    w: Float,
    h: Float,
    currentPressureKPa: Float,
    currentVolumeCm3: Float,
    hotTempK: Float,
    coldTempK: Float,
    clearanceVol: Float,
    maxVol: Float
) {
    val cardW = w * 0.32f
    val cardH = h * 0.24f
    val cardLeft = w * 0.65f
    val cardTop = h * 0.05f

    // Card background
    drawRoundRect(
        color = Color(0xDD0F172A),
        topLeft = Offset(cardLeft, cardTop),
        size = Size(cardW, cardH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = ScienceBorder.copy(alpha = 0.55f),
        topLeft = Offset(cardLeft, cardTop),
        size = Size(cardW, cardH),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1f)
    )

    // Axes
    val ax0 = cardLeft + 22.dp.toPx()
    val ay0 = cardTop + cardH - 16.dp.toPx()
    val ax1 = cardLeft + cardW - 10.dp.toPx()
    val ay1 = cardTop + 14.dp.toPx()
    drawLine(Color.White.copy(alpha = 0.35f), Offset(ax0, ay0), Offset(ax1, ay0), 1.2f) // Volume axis
    drawLine(Color.White.copy(alpha = 0.35f), Offset(ax0, ay0), Offset(ax0, ay1), 1.2f) // Pressure axis

    // P-V Stirling loop (1 -> 2 -> 3 -> 4)
    // 1: (V_min, P_max) - hot compression end
    // 2: (V_max, P_int_hot) - hot expansion end
    // 3: (V_max, P_min) - cold isochoric cool
    // 4: (V_min, P_int_cold) - cold compression
    val vMinX = ax0 + (ax1 - ax0) * 0.22f
    val vMaxX = ax0 + (ax1 - ax0) * 0.85f
    val pMaxY = ay1 + (ay0 - ay1) * 0.15f
    val pMedHotY = ay1 + (ay0 - ay1) * 0.45f
    val pMinY = ay1 + (ay0 - ay1) * 0.88f
    val pMedColdY = ay1 + (ay0 - ay1) * 0.58f

    val loopPath = Path().apply {
        moveTo(vMinX, pMaxY)
        // 1 -> 2: Isothermal expansion (high T_H)
        quadraticTo(
            (vMinX + vMaxX) * 0.5f, (pMaxY + pMedHotY) * 0.5f - 4f,
            vMaxX, pMedHotY
        )
        // 2 -> 3: Isochoric cooling in regenerator (V = const)
        lineTo(vMaxX, pMinY)
        // 3 -> 4: Isothermal compression (low T_C)
        quadraticTo(
            (vMinX + vMaxX) * 0.5f, (pMinY + pMedColdY) * 0.5f + 4f,
            vMinX, pMedColdY
        )
        // 4 -> 1: Isochoric heating in regenerator (V = const)
        close()
    }

    // Shaded loop work area (W_net = oint P dV)
    drawPath(
        path = loopPath,
        color = CoralNeon.copy(alpha = 0.15f)
    )
    drawPath(
        path = loopPath,
        color = CoralNeon,
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
    )

    // Current Operating State Point (V(t), P(t))
    val vNorm = ((currentVolumeCm3 - clearanceVol) / max(maxVol - clearanceVol, 1f)).coerceIn(0f, 1f)
    val pNorm = ((currentPressureKPa - 60f) / 160f).coerceIn(0f, 1f)
    val ptX = ax0 + (ax1 - ax0) * (0.22f + vNorm * 0.63f)
    val ptY = ay0 - (ay0 - ay1) * (0.12f + pNorm * 0.76f)

    drawCircle(CyanNeon.copy(alpha = 0.35f), 7.dp.toPx(), Offset(ptX, ptY))
    drawCircle(CyanNeon, 3.5.dp.toPx(), Offset(ptX, ptY))
    drawCircle(Color.White, 1.8.dp.toPx(), Offset(ptX, ptY))
}
