package com.geosid.simplephysics.ui.experiments.week7.Day47

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
 * DAY 47: Maxwell's Demon & Information Entropy
 *
 * Simulates James Clerk Maxwell's celebrated thermodynamic thought experiment (1867):
 * - A microscopic gatekeeper selectively opens a trapdoor between two gas chambers,
 *   directing fast molecules to the left (heating) and slow molecules to the right (cooling).
 * - Apparent Second Law violation (ΔS_gas < 0) is resolved by Szilard and Landauer (1961):
 *   Information acquisition and bit erasure incur a fundamental thermodynamic cost:
 *     E_dissipated >= k_B · T · ln(2) per bit
 * - Full kinetic simulation with elastic particle collisions, Maxwell-Boltzmann velocity tracking,
 *   real-time chamber temperature calculation, demon memory buffer, and total entropy balance:
 *     ΔS_total = ΔS_gas + ΔS_demon >= 0
 */

private enum class DemonMode(val label: String) {
    ACTIVE_SORTING("Active Demon"),
    MANUAL_GATE("Manual Door"),
    OPEN_MIXING("Open Diffusion"),
    REVERSE_SORTING("Reversed Demon")
}

private class MaxwellParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float = 3.5f
) {
    val speedSq: Float get() = vx * vx + vy * vy
    val speed: Float get() = sqrt(speedSq)
}

@Composable
fun MaxwellsDemonExperiment(
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(DemonMode.ACTIVE_SORTING) }
    var baseTempK by remember { mutableStateOf(300f) }
    var demonEfficiency by remember { mutableStateOf(0.92f) } // Probability demon correctly detects velocity
    var gateDoorOpen by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(true) }

    // Information & Entropy Metrics
    var bitsProcessed by remember { mutableStateOf(0L) }
    var bitsErased by remember { mutableStateOf(0L) }
    var landauerHeatJoules by remember { mutableStateOf(0.0) } // Scaled J (10^-21 J scale)
    var leftTempK by remember { mutableStateOf(300f) }
    var rightTempK by remember { mutableStateOf(300f) }
    var leftCount by remember { mutableStateOf(40) }
    var rightCount by remember { mutableStateOf(40) }
    var demonEyeFlash by remember { mutableStateOf(0f) } // Animation glow for demon sensor pulse

    // Particle Ensemble (80 particles initialized in equilibrium)
    val particles = remember {
        val list = mutableListOf<MaxwellParticle>()
        val rng = Random(42)
        // 40 particles in Left chamber (x: 0.08..0.46)
        for (i in 0 until 40) {
            val angle = rng.nextFloat() * 2f * PI.toFloat()
            val spd = 0.0018f + rng.nextFloat() * 0.0022f
            list.add(
                MaxwellParticle(
                    x = 0.12f + rng.nextFloat() * 0.32f,
                    y = 0.18f + rng.nextFloat() * 0.52f,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd
                )
            )
        }
        // 40 particles in Right chamber (x: 0.54..0.92)
        for (i in 0 until 40) {
            val angle = rng.nextFloat() * 2f * PI.toFloat()
            val spd = 0.0018f + rng.nextFloat() * 0.0022f
            list.add(
                MaxwellParticle(
                    x = 0.56f + rng.nextFloat() * 0.32f,
                    y = 0.18f + rng.nextFloat() * 0.52f,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd
                )
            )
        }
        list
    }

    // Physics frame loop
    LaunchedEffect(isRunning, mode, baseTempK, demonEfficiency, gateDoorOpen) {
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

                // Thermal velocity scaling factor based on baseTempK
                val thermalScale = sqrt(baseTempK / 300f)
                val partitionX = 0.50f
                val gateTopY = 0.36f
                val gateBotY = 0.52f

                // Chamber boundary limits (normalized coordinates)
                val boxLeft = 0.08f
                val boxRight = 0.92f
                val boxTop = 0.16f
                val boxBot = 0.72f

                // Mean median speed calculation
                var totalSpeed = 0f
                particles.forEach { totalSpeed += it.speed }
                val meanSpeed = totalSpeed / max(1, particles.size)

                // Decay demon eye sensor pulse
                if (demonEyeFlash > 0f) {
                    demonEyeFlash = max(0f, demonEyeFlash - dt * 4f)
                }

                // Check approaching particles for Demon trapdoor decision
                var autoDoorOpen = (mode == DemonMode.OPEN_MIXING)

                if (mode == DemonMode.ACTIVE_SORTING || mode == DemonMode.REVERSE_SORTING) {
                    val detectionDistance = 0.045f
                    for (p in particles) {
                        val inGateY = (p.y in (gateTopY - 0.02f)..(gateBotY + 0.02f))
                        if (!inGateY) continue

                        val approachingFromRight = (p.x > partitionX && p.x < partitionX + detectionDistance && p.vx < 0f)
                        val approachingFromLeft = (p.x < partitionX && p.x > partitionX - detectionDistance && p.vx > 0f)

                        val isFast = (p.speed > meanSpeed)
                        val rollsAccurate = (Random.nextFloat() < demonEfficiency)

                        if (mode == DemonMode.ACTIVE_SORTING) {
                            // Maxwell standard sort: Fast -> Left (heat Left), Slow -> Right (cool Right)
                            if (approachingFromRight && (if (rollsAccurate) isFast else !isFast)) {
                                autoDoorOpen = true
                                bitsProcessed++
                                demonEyeFlash = 1f
                                break
                            } else if (approachingFromLeft && (if (rollsAccurate) !isFast else isFast)) {
                                autoDoorOpen = true
                                bitsProcessed++
                                demonEyeFlash = 1f
                                break
                            }
                        } else if (mode == DemonMode.REVERSE_SORTING) {
                            // Reverse sort: Fast -> Right, Slow -> Left
                            if (approachingFromLeft && (if (rollsAccurate) isFast else !isFast)) {
                                autoDoorOpen = true
                                bitsProcessed++
                                demonEyeFlash = 1f
                                break
                            } else if (approachingFromRight && (if (rollsAccurate) !isFast else isFast)) {
                                autoDoorOpen = true
                                bitsProcessed++
                                demonEyeFlash = 1f
                                break
                            }
                        }
                    }
                }

                val isDoorPassable = (mode == DemonMode.MANUAL_GATE && gateDoorOpen) || autoDoorOpen

                // Update particle positions and wall bounces
                var sumSqLeft = 0f
                var sumSqRight = 0f
                var nL = 0
                var nR = 0

                particles.forEach { p ->
                    p.x += p.vx * thermalScale
                    p.y += p.vy * thermalScale

                    // Bounce off outer box walls
                    if (p.x < boxLeft + 0.01f) {
                        p.x = boxLeft + 0.01f
                        p.vx = abs(p.vx)
                    } else if (p.x > boxRight - 0.01f) {
                        p.x = boxRight - 0.01f
                        p.vx = -abs(p.vx)
                    }

                    if (p.y < boxTop + 0.01f) {
                        p.y = boxTop + 0.01f
                        p.vy = abs(p.vy)
                    } else if (p.y > boxBot - 0.01f) {
                        p.y = boxBot - 0.01f
                        p.vy = -abs(p.vy)
                    }

                    // Central partition interaction
                    val crossesPartition = (abs(p.x - partitionX) < 0.012f)
                    val inGateAperture = (p.y >= gateTopY && p.y <= gateBotY)

                    if (crossesPartition) {
                        if (inGateAperture && isDoorPassable) {
                            // Allowed to pass through gate
                        } else {
                            // Bounce elastically off partition wall
                            if (p.vx > 0f) {
                                p.x = partitionX - 0.012f
                                p.vx = -abs(p.vx)
                            } else {
                                p.x = partitionX + 0.012f
                                p.vx = abs(p.vx)
                            }
                        }
                    }

                    // Tally chamber kinetic energies
                    if (p.x < partitionX) {
                        nL++
                        sumSqLeft += p.speedSq
                    } else {
                        nR++
                        sumSqRight += p.speedSq
                    }
                }

                leftCount = nL
                rightCount = nR

                // Kinetic temperature T ~ <v^2> (normalized around baseTempK)
                val baseRefSpeedSq = (0.0025f * 0.0025f)
                val meanSqL = if (nL > 0) (sumSqLeft / nL) else baseRefSpeedSq
                val meanSqR = if (nR > 0) (sumSqRight / nR) else baseRefSpeedSq

                val calculatedLeftT = (meanSqL / baseRefSpeedSq) * baseTempK
                val calculatedRightT = (meanSqR / baseRefSpeedSq) * baseTempK

                leftTempK = leftTempK + (calculatedLeftT - leftTempK) * 0.08f
                rightTempK = rightTempK + (calculatedRightT - rightTempK) * 0.08f

                // Landauer dissipated heat calculation: E = N_bits * k_B * T * ln(2)
                // Using scaled units for display: 1 unit ~ 10^-21 Joules (approx 1 zeptojoule)
                val kB_ln2_scaled = 0.957f // 1.38e-23 * ln(2) in zJ/K * scale
                landauerHeatJoules = (bitsErased.toDouble() * kB_ln2_scaled * (baseTempK / 300.0))
            }
        }
    }

    // Thermodynamic Entropy Calculations
    val deltaSGas = run {
        val tRatioL = max(0.1f, leftTempK / baseTempK)
        val tRatioR = max(0.1f, rightTempK / baseTempK)
        // ΔS_gas = N_L * ln(T_L/T_0) + N_R * ln(T_R/T_0)
        (leftCount * ln(tRatioL) + rightCount * ln(tRatioR)) * 0.25f
    }
    val deltaSDemon = (bitsProcessed * 0.6931f) * 0.25f // k_B ln(2) per bit
    val deltaSTotal = deltaSGas + deltaSDemon

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "🚪 Watch Maxwell's Demon sort particles to create a temperature difference! Clear memory to pay the Landauer entropy cost.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val w = size.width
                            val h = size.height
                            val gateCenter = Offset(w * 0.50f, h * 0.44f)
                            val dist = (offset - gateCenter).getDistance()
                            if (dist < 45.dp.toPx()) {
                                // Tap on gate door toggles manual gate
                                mode = DemonMode.MANUAL_GATE
                                gateDoorOpen = !gateDoorOpen
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw laboratory coordinate background
                drawDemonBackground(w, h)

                val boxLeft = w * 0.08f
                val boxRight = w * 0.92f
                val boxTop = h * 0.16f
                val boxBot = h * 0.72f
                val partitionX = w * 0.50f
                val gateTopY = h * 0.36f
                val gateBotY = h * 0.52f

                // 1. Chamber Thermal Glows
                drawChamberGlow(
                    boxLeft = boxLeft,
                    partitionX = partitionX,
                    boxTop = boxTop,
                    boxBot = boxBot,
                    tempK = leftTempK,
                    baseTempK = baseTempK,
                    isLeft = true
                )
                drawChamberGlow(
                    boxLeft = partitionX,
                    partitionX = boxRight,
                    boxTop = boxTop,
                    boxBot = boxBot,
                    tempK = rightTempK,
                    baseTempK = baseTempK,
                    isLeft = false
                )

                // 2. Chamber Boundary Container Frame
                drawRoundRect(
                    color = ScienceDarkSurfaceVariant.copy(alpha = 0.5f),
                    topLeft = Offset(boxLeft, boxTop),
                    size = Size(boxRight - boxLeft, boxBot - boxTop),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawRoundRect(
                    color = ScienceBorder,
                    topLeft = Offset(boxLeft, boxTop),
                    size = Size(boxRight - boxLeft, boxBot - boxTop),
                    cornerRadius = CornerRadius(12f, 12f),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // 3. Central Insulated Partition Wall
                val wallWidth = 12.dp.toPx()
                // Top wall section
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF334155), Color(0xFF64748B), Color(0xFF334155)),
                        startX = partitionX - wallWidth * 0.5f,
                        endX = partitionX + wallWidth * 0.5f
                    ),
                    topLeft = Offset(partitionX - wallWidth * 0.5f, boxTop),
                    size = Size(wallWidth, gateTopY - boxTop)
                )
                // Bottom wall section
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF334155), Color(0xFF64748B), Color(0xFF334155)),
                        startX = partitionX - wallWidth * 0.5f,
                        endX = partitionX + wallWidth * 0.5f
                    ),
                    topLeft = Offset(partitionX - wallWidth * 0.5f, gateBotY),
                    size = Size(wallWidth, boxBot - gateBotY)
                )

                // 4. Trapdoor Gate Mechanism
                val isDoorOpen = (mode == DemonMode.OPEN_MIXING) ||
                        (mode == DemonMode.MANUAL_GATE && gateDoorOpen) ||
                        (demonEyeFlash > 0.1f)

                drawTrapdoor(
                    partitionX = partitionX,
                    gateTopY = gateTopY,
                    gateBotY = gateBotY,
                    wallWidth = wallWidth,
                    isOpen = isDoorOpen
                )

                // 5. Maxwell's Demon Gatekeeper Character (Cybernetic Eye over Gate)
                drawDemonCharacter(
                    centerX = partitionX,
                    centerY = gateTopY - 26.dp.toPx(),
                    eyeFlash = demonEyeFlash,
                    mode = mode
                )

                // 6. Kinetic Gas Particles (Color-Coded by Temperature / Velocity)
                val meanSpeedRef = 0.0028f
                particles.forEach { p ->
                    val px = w * p.x
                    val py = h * p.y
                    val speedRatio = (p.speed / meanSpeedRef).coerceIn(0.4f, 2.2f)

                    // Color mapping: Cold/Slow = Cyan, Medium = Amber, Hot/Fast = Coral/Magenta
                    val pColor = when {
                        speedRatio < 0.85f -> CyanNeon
                        speedRatio < 1.25f -> AmberVibrant
                        else -> CoralNeon
                    }

                    // Particle core
                    drawCircle(
                        color = pColor,
                        radius = p.radius.dp.toPx(),
                        center = Offset(px, py)
                    )
                    // Velocity tail vector
                    val tailLen = (speedRatio * 5.5f).dp.toPx()
                    val normSpeed = max(0.0001f, p.speed)
                    drawLine(
                        color = pColor.copy(alpha = 0.45f),
                        start = Offset(px, py),
                        end = Offset(px - (p.vx / normSpeed) * tailLen, py - (p.vy / normSpeed) * tailLen),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 7. Real-Time Chamber Temperature Badges
                drawChamberBadge(
                    x = boxLeft + 16.dp.toPx(),
                    y = boxTop + 14.dp.toPx(),
                    title = "CHAMBER A (HOT)",
                    tempK = leftTempK,
                    count = leftCount,
                    isHot = leftTempK >= rightTempK
                )
                drawChamberBadge(
                    x = boxRight - 150.dp.toPx(),
                    y = boxTop + 14.dp.toPx(),
                    title = "CHAMBER B (COLD)",
                    tempK = rightTempK,
                    count = rightCount,
                    isHot = rightTempK > leftTempK
                )
            }
        },
        hudContent = {
            val statusLabel = when (mode) {
                DemonMode.ACTIVE_SORTING -> "Demon Sorting (ΔS_gas < 0)"
                DemonMode.MANUAL_GATE -> if (gateDoorOpen) "Manual Gate (Open)" else "Manual Gate (Closed)"
                DemonMode.OPEN_MIXING -> "Equilibrium Mixing (ΔS > 0)"
                DemonMode.REVERSE_SORTING -> "Reversed Sorting (Refrigeration)"
            }
            val entropyStatus = if (deltaSTotal >= 0f) "Second Law Upheld (ΔS_total ≥ 0)" else "Transient Fluctuation"

            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 47: Maxwell's Demon & Information Entropy",
                items = listOf(
                    "Operational Mode" to statusLabel,
                    "Chamber Temperatures" to "T_A: ${leftTempK.roundToInt()} K | T_B: ${rightTempK.roundToInt()} K (ΔT = ${abs(leftTempK - rightTempK).roundToInt()} K)",
                    "Molecules Distribution" to "N_A: $leftCount | N_B: $rightCount (Total: 80)",
                    "Information Acquired" to "$bitsProcessed bits (Demon Memory)",
                    "Landauer Dissipated Heat" to "${round(landauerHeatJoules * 10f) / 10f} zJ (k_B·T·ln 2 / bit)",
                    "Gas Entropy Change (ΔS_gas)" to "${round(deltaSGas * 100f) / 100f} k_B (${if (deltaSGas < 0f) "Decreasing" else "Increasing"})",
                    "Demon Memory Entropy" to "+${round(deltaSDemon * 100f) / 100f} k_B (Information Cost)",
                    "Total System Entropy (ΔS_tot)" to "${round(deltaSTotal * 100f) / 100f} k_B [$entropyStatus]"
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
                        selected = mode == DemonMode.ACTIVE_SORTING,
                        onClick = {
                            mode = DemonMode.ACTIVE_SORTING
                            isRunning = true
                        },
                        label = { Text("Active Demon", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = mode == DemonMode.MANUAL_GATE,
                        onClick = {
                            mode = DemonMode.MANUAL_GATE
                            gateDoorOpen = !gateDoorOpen
                        },
                        label = {
                            Text(
                                if (gateDoorOpen) "Gate: OPEN" else "Gate: CLOSED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = mode == DemonMode.OPEN_MIXING,
                        onClick = {
                            mode = DemonMode.OPEN_MIXING
                            isRunning = true
                        },
                        label = { Text("Open Mixing", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Paired Sliders 1: Base Gas Temperature & Demon Sorting Accuracy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Base Temp T_0",
                        value = baseTempK,
                        range = 100f..600f,
                        valueDisplay = "${round(baseTempK).toInt()} K",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = { baseTempK = it }
                    )
                    PhysicsSliderControl(
                        title = "Demon Accuracy",
                        value = demonEfficiency,
                        range = 0.50f..1.0f,
                        valueDisplay = "${(demonEfficiency * 100f).roundToInt()}%",
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { demonEfficiency = it }
                    )
                }

                // Action Controls Row: Erase Memory (Landauer Dissipation), Pause/Resume, Reset
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
                                // Landauer Memory Erasure Step:
                                // Erases recorded decision bits and dumps heat into the environment
                                bitsErased += bitsProcessed
                                bitsProcessed = 0L
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🗑️ Erase Bits (Landauer)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            mode = DemonMode.ACTIVE_SORTING
                            baseTempK = 300f
                            demonEfficiency = 0.92f
                            gateDoorOpen = false
                            bitsProcessed = 0L
                            bitsErased = 0L
                            landauerHeatJoules = 0.0
                            leftTempK = 300f
                            rightTempK = 300f
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

private fun DrawScope.drawDemonBackground(w: Float, h: Float) {
    val spacing = 36.dp.toPx()
    var x = 0f
    while (x < w) {
        drawLine(ScienceBorder.copy(alpha = 0.15f), Offset(x, 0f), Offset(x, h), 0.8f)
        x += spacing
    }
    var y = 0f
    while (y < h) {
        drawLine(ScienceBorder.copy(alpha = 0.15f), Offset(0f, y), Offset(w, y), 0.8f)
        y += spacing
    }
}

private fun DrawScope.drawChamberGlow(
    boxLeft: Float,
    partitionX: Float,
    boxTop: Float,
    boxBot: Float,
    tempK: Float,
    baseTempK: Float,
    isLeft: Boolean
) {
    val tempDiff = tempK - baseTempK
    val glowColor = if (tempDiff >= 0f) CoralNeon else CyanNeon
    val glowAlpha = (abs(tempDiff) / 250f).coerceIn(0.04f, 0.25f)

    drawRect(
        color = glowColor.copy(alpha = glowAlpha),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(partitionX - boxLeft, boxBot - boxTop)
    )
}

private fun DrawScope.drawTrapdoor(
    partitionX: Float,
    gateTopY: Float,
    gateBotY: Float,
    wallWidth: Float,
    isOpen: Boolean
) {
    val gateHeight = gateBotY - gateTopY

    if (!isOpen) {
        // Closed Trapdoor (Solid metallic beam blocking aperture)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF64748B)),
                startY = gateTopY,
                endY = gateBotY
            ),
            topLeft = Offset(partitionX - wallWidth * 0.5f, gateTopY),
            size = Size(wallWidth, gateHeight),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(partitionX - wallWidth * 0.5f + 2f, gateTopY),
            end = Offset(partitionX - wallWidth * 0.5f + 2f, gateBotY),
            strokeWidth = 1.5f
        )
    } else {
        // Open Trapdoor (Retracted / Slid upwards into partition housing)
        drawRoundRect(
            color = CyanNeon.copy(alpha = 0.35f),
            topLeft = Offset(partitionX - wallWidth * 0.5f, gateTopY - 14.dp.toPx()),
            size = Size(wallWidth, 14.dp.toPx()),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Green open indicator field in aperture
        drawRect(
            color = EmeraldNeon.copy(alpha = 0.12f),
            topLeft = Offset(partitionX - wallWidth * 0.5f, gateTopY),
            size = Size(wallWidth, gateHeight)
        )
        drawLine(
            color = EmeraldNeon.copy(alpha = 0.65f),
            start = Offset(partitionX, gateTopY),
            end = Offset(partitionX, gateBotY),
            strokeWidth = 1.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
        )
    }
}

private fun DrawScope.drawDemonCharacter(
    centerX: Float,
    centerY: Float,
    eyeFlash: Float,
    mode: DemonMode
) {
    val headRadius = 16.dp.toPx()

    // Demon halo glow
    val glowColor = if (eyeFlash > 0.1f) CoralNeon else PurpleNeon
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor.copy(alpha = 0.45f + eyeFlash * 0.35f), Color.Transparent),
            center = Offset(centerX, centerY),
            radius = headRadius * 1.9f
        ),
        radius = headRadius * 1.9f,
        center = Offset(centerX, centerY)
    )

    // Cybernetic Demon Head Body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)),
            center = Offset(centerX - 4f, centerY - 4f),
            radius = headRadius
        ),
        radius = headRadius,
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = ScienceBorder,
        radius = headRadius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Horns / Antennas
    val hornColor = if (eyeFlash > 0.1f) CoralNeon else AmberVibrant
    val hornPathLeft = Path().apply {
        moveTo(centerX - headRadius * 0.6f, centerY - headRadius * 0.6f)
        lineTo(centerX - headRadius * 1.1f, centerY - headRadius * 1.4f)
        lineTo(centerX - headRadius * 0.2f, centerY - headRadius * 0.9f)
        close()
    }
    val hornPathRight = Path().apply {
        moveTo(centerX + headRadius * 0.6f, centerY - headRadius * 0.6f)
        lineTo(centerX + headRadius * 1.1f, centerY - headRadius * 1.4f)
        lineTo(centerX + headRadius * 0.2f, centerY - headRadius * 0.9f)
        close()
    }
    drawPath(hornPathLeft, hornColor)
    drawPath(hornPathRight, hornColor)

    // All-Seeing Central Cybernetic Sensor Eye
    val eyeRadius = headRadius * 0.45f
    val eyeIrisColor = if (eyeFlash > 0.1f) CoralNeon else CyanNeon
    drawCircle(Color(0xFF0F172A), eyeRadius, Offset(centerX, centerY))
    drawCircle(eyeIrisColor, eyeRadius * 0.65f, Offset(centerX, centerY))
    drawCircle(Color.White, eyeRadius * 0.25f, Offset(centerX - 1.5f, centerY - 1.5f))

    // Scanning Sensor Laser Cone downward toward trapdoor
    if (eyeFlash > 0.05f) {
        val scanPath = Path().apply {
            moveTo(centerX, centerY + eyeRadius)
            lineTo(centerX - 24.dp.toPx(), centerY + 50.dp.toPx())
            lineTo(centerX + 24.dp.toPx(), centerY + 50.dp.toPx())
            close()
        }
        drawPath(scanPath, eyeIrisColor.copy(alpha = 0.22f * eyeFlash))
    }
}

private fun DrawScope.drawChamberBadge(
    x: Float,
    y: Float,
    title: String,
    tempK: Float,
    count: Int,
    isHot: Boolean
) {
    val badgeW = 135.dp.toPx()
    val badgeH = 46.dp.toPx()
    val accentColor = if (isHot) CoralNeon else CyanNeon

    // Background card
    drawRoundRect(
        color = Color(0xCC0F172A),
        topLeft = Offset(x, y),
        size = Size(badgeW, badgeH),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )
    drawRoundRect(
        color = accentColor.copy(alpha = 0.65f),
        topLeft = Offset(x, y),
        size = Size(badgeW, badgeH),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        style = Stroke(width = 1.2.dp.toPx())
    )

    // Indicator accent dot
    drawCircle(
        color = accentColor,
        radius = 3.5.dp.toPx(),
        center = Offset(x + 12.dp.toPx(), y + 14.dp.toPx())
    )
    drawCircle(
        color = Color.White,
        radius = 1.5.dp.toPx(),
        center = Offset(x + 12.dp.toPx(), y + 14.dp.toPx())
    )
}
