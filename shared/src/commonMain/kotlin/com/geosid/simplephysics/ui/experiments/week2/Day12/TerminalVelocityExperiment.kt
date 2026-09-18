package com.geosid.simplephysics.ui.experiments.week2.Day12

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.ui.components.PhysicsSliderControl
import com.geosid.simplephysics.ui.components.ResetIcon
import com.geosid.simplephysics.ui.components.ResponsiveExperimentContainer
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Object presets for the Terminal Velocity drop simulation.
 */
enum class TerminalVelocityObject(
    val title: String,
    val icon: String,
    val defaultMass: Float, // kg
    val defaultArea: Float, // m^2
    val defaultCd: Float,   // Drag coefficient
    val color: Color
) {
    SKYDIVERR("Skydiver", "🪂", 80f, 0.70f, 1.20f, CyanNeon),
    BOWLING_BALL("Bowling Ball", "🎳", 7.2f, 0.038f, 0.47f, PurpleNeon),
    BASEBALL("Baseball", "⚾", 0.145f, 0.0042f, 0.30f, CoralNeon),
    COFFEE_FILTER("Feather", "🪶", 0.002f, 0.012f, 1.00f, AmberVibrant)
}

/**
 * Medium / fluid density presets.
 */
enum class MediumType(
    val title: String,
    val density: Float // kg/m^3
) {
    AIR_SEA_LEVEL("Air (1.2 kg/m³)", 1.225f),
    WATER("Water (1000 kg/m³)", 1000.0f),
    VACUUM("Vacuum (0 kg/m³)", 0.000f)
}

private data class AirStreamline(
    var relX: Float,
    var phaseOffset: Float,
    var lengthFactor: Float,
    var speedMultiplier: Float
)

@Composable
fun TerminalVelocityExperiment(
    modifier: Modifier = Modifier
) {
    // Physical Selection State
    var selectedObject by remember { mutableStateOf(TerminalVelocityObject.SKYDIVERR) }
    var selectedMedium by remember { mutableStateOf(MediumType.AIR_SEA_LEVEL) }
    var isParachuteDeployed by remember { mutableStateOf(false) }

    // Tunable Physical Parameters
    var mass by remember { mutableStateOf(TerminalVelocityObject.SKYDIVERR.defaultMass) }
    var area by remember { mutableStateOf(TerminalVelocityObject.SKYDIVERR.defaultArea) }
    var dragCoeff by remember { mutableStateOf(TerminalVelocityObject.SKYDIVERR.defaultCd) }

    // Simulation Engine State
    var isRunning by remember { mutableStateOf(true) }
    var velocity by remember { mutableStateOf(0f) } // m/s
    var distanceFallen by remember { mutableStateOf(0f) } // meters
    var simTime by remember { mutableStateOf(0f) } // seconds

    // Touch offset on canvas (normalized -0.3f to 0.3f for bobbing/dragging)
    var dragOffsetY by remember { mutableStateOf(0f) }

    // Velocity history buffer for in-canvas asymptotic curve sparkline
    val velocityHistory = remember { mutableStateListOf<Float>() }

    // Dynamic Airflow Streamlines
    val streamlines = remember {
        List(30) {
            AirStreamline(
                relX = Random.nextFloat(),
                phaseOffset = Random.nextFloat(),
                lengthFactor = 0.6f + Random.nextFloat() * 0.8f,
                speedMultiplier = 0.8f + Random.nextFloat() * 0.4f
            )
        }
    }

    // Effective physics properties factoring in parachute deployment
    val effectiveArea = if (isParachuteDeployed) 25.0f else area
    val effectiveCd = if (isParachuteDeployed) 1.75f else dragCoeff
    val effectiveMass = if (isParachuteDeployed && selectedObject == TerminalVelocityObject.SKYDIVERR) mass + 5f else mass
    val gravity = 9.81f
    val rho = selectedMedium.density

    // Force Calculations
    val fg = effectiveMass * gravity
    val fd = 0.5f * rho * effectiveCd * effectiveArea * (velocity * velocity)
    val fNet = fg - fd
    val acceleration = fNet / effectiveMass

    // Theoretical Terminal Velocity: vt = sqrt( 2 * m * g / (rho * A * Cd) )
    val terminalVelocity = remember(effectiveMass, gravity, rho, effectiveArea, effectiveCd) {
        if (rho > 0.0001f && effectiveCd > 0.001f && effectiveArea > 0.0001f) {
            sqrt((2f * effectiveMass * gravity) / (rho * effectiveArea * effectiveCd))
        } else {
            Float.POSITIVE_INFINITY
        }
    }

    // High-Precision Physics Integration Loop (Euler-Cromer)
    LaunchedEffect(isRunning, effectiveMass, effectiveArea, effectiveCd, rho) {
        var lastNanos = withFrameNanos { it }
        while (true) {
            val currentNanos = withFrameNanos { it }
            val dt = ((currentNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
            lastNanos = currentNanos

            if (isRunning) {
                // Sub-stepping for numerical stability
                val subSteps = 4
                val subDt = dt / subSteps
                for (i in 0 until subSteps) {
                    val currentFd = 0.5f * rho * effectiveCd * effectiveArea * (velocity * velocity)
                    val netF = fg - currentFd
                    val a = netF / effectiveMass

                    velocity = (velocity + a * subDt).coerceAtLeast(0f)
                    distanceFallen += velocity * subDt
                    simTime += subDt
                }

                // Sample velocity history every ~100ms for sparkline
                if (velocityHistory.isEmpty() || abs(velocity - velocityHistory.last()) > 0.05f || velocityHistory.size < 50) {
                    velocityHistory.add(velocity)
                    if (velocityHistory.size > 80) {
                        velocityHistory.removeAt(0)
                    }
                }
            }
        }
    }

    // Equilibrium Detection (within 3.5% of Terminal Velocity)
    val isNearEquilibrium = terminalVelocity.isFinite() &&
            velocity > 0.5f &&
            abs(velocity - terminalVelocity) / terminalVelocity < 0.035f

    val isBraking = fd > fg * 1.08f

    // UI Container
    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag to reposition object, tap canvas to toggle parachute, or tune controls below!",
        canvasContent = {
            // Main Aerodynamic Chamber Canvas - completely clean and unobstructed!
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            dragOffsetY = (dragOffsetY + dragAmount.y / size.height).coerceIn(-0.3f, 0.3f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (selectedObject == TerminalVelocityObject.SKYDIVERR) {
                                isParachuteDeployed = !isParachuteDeployed
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw Atmospheric / Wind Tunnel Chamber Background
                drawChamberBackground(w, h, selectedMedium)

                // Draw Vertical Speed & Altitude Scale on left wall
                drawChamberWallScale(w, h, distanceFallen, velocity, terminalVelocity)

                // Draw Dynamic Upward Air Streamlines (speed matches falling speed)
                drawAirStreamlines(
                    w = w,
                    h = h,
                    streamlines = streamlines,
                    speed = velocity,
                    simTime = simTime,
                    isParachute = isParachuteDeployed && selectedObject == TerminalVelocityObject.SKYDIVERR
                )

                // Object Position: Center with interactive touch offset and micro-turbulence bobbing
                val turbulenceBob = if (velocity > 1f) sin(simTime * 14f) * min(velocity * 0.12f, 4f) else 0f
                val objCenter = Offset(w * 0.48f, h * 0.45f + dragOffsetY * h + turbulenceBob)

                // Draw Aerodynamic Wake Vortices behind falling body
                if (velocity > 3f) {
                    drawWakeVortices(objCenter, velocity, terminalVelocity, simTime)
                }

                // Draw Free-Body Diagram (FBD) Force Vectors
                drawForceVectors(
                    center = objCenter,
                    fg = fg,
                    fd = fd,
                    isEquilibrium = isNearEquilibrium,
                    isBraking = isBraking
                )

                // Draw Falling Object Model
                drawFallingObject(
                    center = objCenter,
                    obj = selectedObject,
                    isParachuteDeployed = isParachuteDeployed,
                    animPhase = simTime,
                    velocity = velocity
                )
            }
        },
        hudContent = {
            val speedDisplay = "${round(velocity * 10f) / 10f} m/s (${round(velocity * 3.6f).toInt()} km/h)"
            val vtDisplay = if (terminalVelocity.isInfinite()) {
                "∞ (No Drag in Vacuum)"
            } else {
                "${round(terminalVelocity * 10f) / 10f} m/s (${round(terminalVelocity * 3.6f).toInt()} km/h)"
            }
            val equilibriumPct = if (terminalVelocity.isFinite() && terminalVelocity > 0.01f) {
                "${min(round((velocity / terminalVelocity) * 100f).toInt(), 999)}%"
            } else {
                "N/A"
            }

            TransparentTelemetryHud(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 12: Terminal Velocity & Drag Equilibrium",
                items = listOf(
                    "Governing Law" to "v_t = √( 2mg / (ρ · A · C_d) )",
                    "Current Speed (v)" to speedDisplay,
                    "Terminal Speed (v_t)" to vtDisplay,
                    "Speed / v_t Ratio" to equilibriumPct,
                    "Downward Gravity (F_g)" to "${fg.roundToInt()} N",
                    "Upward Drag (F_d)" to "${fd.roundToInt()} N",
                    "Net Acceleration (a)" to "${round(acceleration * 100f) / 100f} m/s²",
                    "Distance Fallen" to "${distanceFallen.roundToInt()} m"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Object Presets Row (Clean FilterChips)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TerminalVelocityObject.entries.forEach { obj ->
                        val isSelected = selectedObject == obj
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedObject = obj
                                mass = obj.defaultMass
                                area = obj.defaultArea
                                dragCoeff = obj.defaultCd
                                isParachuteDeployed = false
                            },
                            label = {
                                Text(
                                    text = "${obj.icon} ${obj.title}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = obj.color.copy(alpha = 0.25f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ScienceDarkSurface,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. Parachute Deployment Action (Only when Skydiver is selected)
                if (selectedObject == TerminalVelocityObject.SKYDIVERR) {
                    Button(
                        onClick = { isParachuteDeployed = !isParachuteDeployed },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isParachuteDeployed) CoralNeon else EmeraldNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isParachuteDeployed) "✂️ Cut Parachute (Freefall Mode)" else "🪂 Deploy Parachute (Braking Mode)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // 3. Fluid Medium Row (Clean FilterChips)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MediumType.entries.forEach { med ->
                        val isMedSelected = selectedMedium == med
                        FilterChip(
                            selected = isMedSelected,
                            onClick = { selectedMedium = med },
                            label = {
                                Text(
                                    text = med.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isMedSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                                selectedLabelColor = CyanNeon,
                                containerColor = ScienceDarkSurface,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. Compact Sliders (Side-by-Side)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Mass (m)",
                        value = mass,
                        range = 0.001f..150f,
                        valueDisplay = if (mass < 0.1f) "${round(mass * 1000f).toInt()} g" else "${round(mass * 10f) / 10f} kg",
                        accentColor = AmberVibrant,
                        onValueChange = { mass = it }
                    )

                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Drag Area (A)",
                        value = area,
                        range = 0.001f..2.5f,
                        valueDisplay = "${round(area * 1000f) / 1000f} m²",
                        accentColor = CyanNeon,
                        onValueChange = {
                            area = it
                            if (isParachuteDeployed) isParachuteDeployed = false
                        }
                    )
                }

                // 5. Clean Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            velocity = 0f
                            distanceFallen = 0f
                            simTime = 0f
                            velocityHistory.clear()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Text(
                            text = "↺ Drop from Top",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberVibrant else EmeraldNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            selectedObject = TerminalVelocityObject.SKYDIVERR
                            selectedMedium = MediumType.AIR_SEA_LEVEL
                            isParachuteDeployed = false
                            mass = TerminalVelocityObject.SKYDIVERR.defaultMass
                            area = TerminalVelocityObject.SKYDIVERR.defaultArea
                            dragCoeff = TerminalVelocityObject.SKYDIVERR.defaultCd
                            velocity = 0f
                            distanceFallen = 0f
                            simTime = 0f
                            velocityHistory.clear()
                            dragOffsetY = 0f
                            isRunning = true
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(10.dp))
                    ) {
                        ResetIcon(tint = CyanNeon, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    )
}

/**
 * Completely transparent HUD card displaying live physics metrics
 * so the user stays 100% focused on the experiment.
 */
@Composable
private fun TransparentTelemetryHud(
    modifier: Modifier = Modifier,
    title: String,
    items: List<Pair<String, String>>
) {
    Column(
        modifier = modifier
            .background(Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 $title".uppercase(),
                color = CyanNeon.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Surface(
                color = CyanNeon.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "LIVE",
                    color = CyanNeon,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = ScienceBorder.copy(alpha = 0.35f), thickness = 0.8.dp)
        Spacer(Modifier.height(4.dp))
        items.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = TextSecondary.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// High-Fidelity Canvas Drawing Functions
// ---------------------------------------------------------------------------

private fun DrawScope.drawChamberBackground(w: Float, h: Float, medium: MediumType) {
    // Atmospheric / Chamber Backdrop Gradient
    val topColor = when (medium) {
        MediumType.WATER -> Color(0xFF07192C)
        MediumType.VACUUM -> Color(0xFF05070D)
        MediumType.AIR_SEA_LEVEL -> Color(0xFF0C1322)
    }
    val bottomColor = when (medium) {
        MediumType.WATER -> Color(0xFF0B2E4D)
        MediumType.VACUUM -> Color(0xFF090D17)
        MediumType.AIR_SEA_LEVEL -> Color(0xFF142036)
    }

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(topColor, bottomColor),
            startY = 0f,
            endY = h
        )
    )

    // Vertical Wind Chamber Guideway Columns
    val leftWallX = w * 0.12f
    val rightWallX = w * 0.88f

    drawLine(
        color = ScienceBorder.copy(alpha = 0.5f),
        start = Offset(leftWallX, 0f),
        end = Offset(leftWallX, h),
        strokeWidth = 2f
    )
    drawLine(
        color = ScienceBorder.copy(alpha = 0.5f),
        start = Offset(rightWallX, 0f),
        end = Offset(rightWallX, h),
        strokeWidth = 2f
    )

    // Subtle chamber perspective ribs
    val ribStep = h / 8f
    for (i in 1..7) {
        val y = i * ribStep
        drawLine(
            color = ScienceBorder.copy(alpha = 0.15f),
            start = Offset(leftWallX, y),
            end = Offset(rightWallX, y),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f)
        )
    }
}

private fun DrawScope.drawChamberWallScale(
    w: Float,
    h: Float,
    distance: Float,
    speed: Float,
    vt: Float
) {
    val wallX = w * 0.12f
    val tickStep = 35.dp.toPx()
    val count = (h / tickStep).toInt()

    for (i in 0..count) {
        val y = i * tickStep
        val isMajor = (i % 3 == 0)
        val tickLen = if (isMajor) 14.dp.toPx() else 7.dp.toPx()

        drawLine(
            color = if (isMajor) CyanNeon.copy(alpha = 0.7f) else ScienceBorder,
            start = Offset(wallX, y),
            end = Offset(wallX + tickLen, y),
            strokeWidth = if (isMajor) 2f else 1f
        )
    }

    // Vertical Velocity Tape Indicator on the far left
    val tapeWidth = 6.dp.toPx()
    val tapeLeft = w * 0.04f
    val maxTapeSpeed = if (vt.isFinite() && vt > 1f) vt * 1.25f else 100f
    val fillFraction = (speed / maxTapeSpeed).coerceIn(0f, 1f)

    drawRoundRect(
        color = ScienceDarkSurfaceVariant,
        topLeft = Offset(tapeLeft, h * 0.2f),
        size = Size(tapeWidth, h * 0.6f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    val fillHeight = (h * 0.6f) * fillFraction
    drawRoundRect(
        color = CyanNeon,
        topLeft = Offset(tapeLeft, h * 0.8f - fillHeight),
        size = Size(tapeWidth, fillHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Target Terminal Velocity tick mark on the speed tape
    if (vt.isFinite()) {
        val vtFraction = (vt / maxTapeSpeed).coerceIn(0f, 1f)
        val vtY = h * 0.8f - (h * 0.6f) * vtFraction
        drawLine(
            color = EmeraldNeon,
            start = Offset(tapeLeft - 3.dp.toPx(), vtY),
            end = Offset(tapeLeft + tapeWidth + 3.dp.toPx(), vtY),
            strokeWidth = 2.5f
        )
    }
}

private fun DrawScope.drawAirStreamlines(
    w: Float,
    h: Float,
    streamlines: List<AirStreamline>,
    speed: Float,
    simTime: Float,
    isParachute: Boolean
) {
    val leftBound = w * 0.16f
    val rightBound = w * 0.84f
    val chamberWidth = rightBound - leftBound

    // If speed is very low, particles move gently; as speed increases, they rush upwards fast
    val upwardFlowRate = if (speed < 0.1f) 0.15f else speed * 0.045f

    streamlines.forEach { line ->
        val x = leftBound + line.relX * chamberWidth
        val cycle = ((simTime * upwardFlowRate * line.speedMultiplier + line.phaseOffset) % 1f)
        val y = h - cycle * (h + 100f)

        val streakLen = (18f + speed * 1.8f * line.lengthFactor).coerceIn(15f, 110f)
        val alpha = if (speed < 1f) 0.18f else (0.25f + min(speed * 0.012f, 0.45f))

        // Lateral diversion around object
        val objX = w * 0.48f
        val objY = h * 0.45f
        val dx = x - objX
        val dy = y - objY
        val distToObj = sqrt(dx * dx + dy * dy)
        val deflectionRadius = if (isParachute) 110.dp.toPx() else 45.dp.toPx()

        val shiftedX = if (distToObj < deflectionRadius) {
            val factor = (1f - distToObj / deflectionRadius)
            val push = if (dx >= 0) factor * 32f else -factor * 32f
            x + push
        } else {
            x
        }

        drawLine(
            color = CyanNeon.copy(alpha = alpha),
            start = Offset(shiftedX, y),
            end = Offset(shiftedX, y - streakLen),
            strokeWidth = if (speed > 20f) 2.2f else 1.4f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawWakeVortices(
    center: Offset,
    speed: Float,
    vt: Float,
    simTime: Float
) {
    val wakeCount = 4
    val intensity = min(speed / 40f, 1f)
    for (i in 0 until wakeCount) {
        val t = (simTime * 6f + i * 0.8f) % 2.5f
        val y = center.y - 30.dp.toPx() - t * 35.dp.toPx()
        val spread = 20.dp.toPx() + t * 24.dp.toPx()
        val vortexAlpha = (1f - t / 2.5f).coerceIn(0f, 1f) * intensity * 0.4f

        drawCircle(
            color = PurpleNeon.copy(alpha = vortexAlpha),
            radius = 6.dp.toPx() + t * 8.dp.toPx(),
            center = Offset(center.x - spread * 0.5f, y),
            style = Stroke(width = 1.2f)
        )
        drawCircle(
            color = CyanNeon.copy(alpha = vortexAlpha),
            radius = 6.dp.toPx() + t * 8.dp.toPx(),
            center = Offset(center.x + spread * 0.5f, y),
            style = Stroke(width = 1.2f)
        )
    }
}

private fun DrawScope.drawForceVectors(
    center: Offset,
    fg: Float,
    fd: Float,
    isEquilibrium: Boolean,
    isBraking: Boolean
) {
    // Arrow baseline scale (70 dp represents equilibrium length for Fg)
    val baseLength = 70.dp.toPx()
    val fgLength = baseLength
    val fdLength = (baseLength * (fd / max(1f, fg))).coerceIn(4f, baseLength * 2.2f)

    // 1. Downward Gravity Vector (Fg) in Amber
    val fgEnd = Offset(center.x, center.y + fgLength)
    drawLine(
        color = AmberVibrant,
        start = center,
        end = fgEnd,
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )
    // Arrowhead pointing down
    val headSize = 9.dp.toPx()
    val arrowFgPath = Path().apply {
        moveTo(fgEnd.x, fgEnd.y + headSize)
        lineTo(fgEnd.x - headSize * 0.7f, fgEnd.y)
        lineTo(fgEnd.x + headSize * 0.7f, fgEnd.y)
        close()
    }
    drawPath(path = arrowFgPath, color = AmberVibrant)

    // 2. Upward Drag Vector (Fd) in Cyan/Emerald
    val dragColor = when {
        isBraking -> CoralNeon
        isEquilibrium -> EmeraldNeon
        else -> CyanNeon
    }
    val fdEnd = Offset(center.x, center.y - fdLength)
    drawLine(
        color = dragColor,
        start = center,
        end = fdEnd,
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )
    // Arrowhead pointing up
    val arrowFdPath = Path().apply {
        moveTo(fdEnd.x, fdEnd.y - headSize)
        lineTo(fdEnd.x - headSize * 0.7f, fdEnd.y)
        lineTo(fdEnd.x + headSize * 0.7f, fdEnd.y)
        close()
    }
    drawPath(path = arrowFdPath, color = dragColor)

    // Center of Mass node
    drawCircle(
        color = Color.White,
        radius = 5.dp.toPx(),
        center = center
    )
    drawCircle(
        color = if (isEquilibrium) EmeraldNeon else CyanNeon,
        radius = 8.dp.toPx(),
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    // Glowing Equilibrium Ring when Fd ≈ Fg
    if (isEquilibrium) {
        drawCircle(
            color = EmeraldNeon.copy(alpha = 0.25f),
            radius = 32.dp.toPx(),
            center = center
        )
        drawCircle(
            color = EmeraldNeon,
            radius = 32.dp.toPx(),
            center = center,
            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )
    }
}

private fun DrawScope.drawFallingObject(
    center: Offset,
    obj: TerminalVelocityObject,
    isParachuteDeployed: Boolean,
    animPhase: Float,
    velocity: Float
) {
    if (isParachuteDeployed && obj == TerminalVelocityObject.SKYDIVERR) {
        // Draw Parachute Canopy & Suspension Lines + Skydiver
        val canopyCenterY = center.y - 65.dp.toPx()
        val canopyWidth = 96.dp.toPx()
        val canopyHeight = 36.dp.toPx()

        // Suspension Lines from skydiver to canopy rim
        val lineCount = 6
        for (i in 0 until lineCount) {
            val t = i / (lineCount - 1).toFloat()
            val rimX = center.x - canopyWidth * 0.5f + t * canopyWidth
            val rimY = canopyCenterY + 4.dp.toPx()
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(center.x, center.y - 6.dp.toPx()),
                end = Offset(rimX, rimY),
                strokeWidth = 1.2f
            )
        }

        // Billowing Canopy Arc Path
        val canopyPath = Path().apply {
            moveTo(center.x - canopyWidth * 0.5f, canopyCenterY + 4.dp.toPx())
            cubicTo(
                center.x - canopyWidth * 0.4f, canopyCenterY - canopyHeight,
                center.x + canopyWidth * 0.4f, canopyCenterY - canopyHeight,
                center.x + canopyWidth * 0.5f, canopyCenterY + 4.dp.toPx()
            )
            close()
        }
        drawPath(
            path = canopyPath,
            brush = Brush.horizontalGradient(
                colors = listOf(CyanNeon, EmeraldNeon, AmberVibrant, CoralNeon, CyanNeon)
            )
        )
        drawPath(
            path = canopyPath,
            color = Color.White.copy(alpha = 0.9f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Skydiver figure below canopy
        drawSkydiverFigure(center, animPhase, isUpright = true)
    } else {
        when (obj) {
            TerminalVelocityObject.SKYDIVERR -> {
                // Freefall Belly-to-Earth Skydiver
                drawSkydiverFigure(center, animPhase, isUpright = false)
            }
            TerminalVelocityObject.BOWLING_BALL -> {
                // Shiny Bowling Ball with finger holes
                val r = 24.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6B46C1), Color(0xFF1E1138), Color(0xFF0F071D)),
                        center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
                        radius = r * 1.2f
                    ),
                    radius = r,
                    center = center
                )
                drawCircle(color = Color(0x66FFFFFF), radius = r, center = center, style = Stroke(1.5f))

                // 3 Finger Holes
                val holeR = 2.8.dp.toPx()
                drawCircle(Color.Black, holeR, Offset(center.x - 5.dp.toPx(), center.y - 4.dp.toPx()))
                drawCircle(Color.Black, holeR, Offset(center.x + 5.dp.toPx(), center.y - 4.dp.toPx()))
                drawCircle(Color.Black, holeR, Offset(center.x, center.y + 6.dp.toPx()))
            }
            TerminalVelocityObject.BASEBALL -> {
                // Baseball with red seam stitches
                val r = 18.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
                        center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                        radius = r
                    ),
                    radius = r,
                    center = center
                )
                drawCircle(color = Color(0xFF94A3B8), radius = r, center = center, style = Stroke(1.5f))

                // Curved red seams
                drawArc(
                    color = CoralNeon,
                    startAngle = 120f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(center.x - r * 0.8f, center.y - r * 0.8f),
                    size = Size(r * 1.6f, r * 1.6f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = CoralNeon,
                    startAngle = -60f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(center.x - r * 0.8f, center.y - r * 0.8f),
                    size = Size(r * 1.6f, r * 1.6f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
            }
            TerminalVelocityObject.COFFEE_FILTER -> {
                // Fluted, lightweight paper filter cup
                val filterW = 34.dp.toPx()
                val filterH = 18.dp.toPx()
                val filterPath = Path().apply {
                    moveTo(center.x - filterW * 0.5f, center.y - filterH * 0.5f)
                    lineTo(center.x - filterW * 0.28f, center.y + filterH * 0.5f)
                    lineTo(center.x + filterW * 0.28f, center.y + filterH * 0.5f)
                    lineTo(center.x + filterW * 0.5f, center.y - filterH * 0.5f)
                    close()
                }
                drawPath(path = filterPath, color = AmberVibrant.copy(alpha = 0.85f))
                drawPath(path = filterPath, color = Color.White, style = Stroke(width = 1.5f))

                // Ruffled pleats
                for (k in -2..2) {
                    val px = center.x + k * 5.dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(px, center.y - filterH * 0.3f),
                        end = Offset(px * 0.95f + center.x * 0.05f, center.y + filterH * 0.4f),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSkydiverFigure(
    center: Offset,
    animPhase: Float,
    isUpright: Boolean
) {
    if (isUpright) {
        // Upright skydiver suspended under parachute
        val headR = 5.dp.toPx()
        // Head / Helmet
        drawCircle(color = CyanNeon, radius = headR, center = Offset(center.x, center.y - 12.dp.toPx()))
        // Torso
        drawLine(
            color = Color.White,
            start = Offset(center.x, center.y - 7.dp.toPx()),
            end = Offset(center.x, center.y + 8.dp.toPx()),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Arms holding risers
        drawLine(
            color = Color.White,
            start = Offset(center.x - 10.dp.toPx(), center.y - 8.dp.toPx()),
            end = Offset(center.x + 10.dp.toPx(), center.y - 8.dp.toPx()),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
        // Legs dangling
        drawLine(
            color = TextSecondary,
            start = Offset(center.x, center.y + 8.dp.toPx()),
            end = Offset(center.x - 5.dp.toPx(), center.y + 20.dp.toPx()),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = TextSecondary,
            start = Offset(center.x, center.y + 8.dp.toPx()),
            end = Offset(center.x + 5.dp.toPx(), center.y + 20.dp.toPx()),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    } else {
        // Belly-to-earth horizontal freefall skydiver arch
        val torsoLen = 22.dp.toPx()

        // Torso
        drawLine(
            color = CyanNeon,
            start = Offset(center.x - torsoLen * 0.5f, center.y),
            end = Offset(center.x + torsoLen * 0.5f, center.y),
            strokeWidth = 7.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Aerodynamic Helmet
        drawCircle(
            color = AmberVibrant,
            radius = 6.dp.toPx(),
            center = Offset(center.x - torsoLen * 0.65f, center.y)
        )
        // Visor glare
        drawCircle(
            color = Color.White,
            radius = 2.2.dp.toPx(),
            center = Offset(center.x - torsoLen * 0.7f, center.y - 1.5.dp.toPx())
        )

        // Arms spread out forward/lateral
        val flutter = sin(animPhase * 16f) * 1.5.dp.toPx()
        drawLine(
            color = Color.White,
            start = Offset(center.x - 4.dp.toPx(), center.y),
            end = Offset(center.x - 16.dp.toPx(), center.y - 12.dp.toPx() + flutter),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(center.x - 4.dp.toPx(), center.y),
            end = Offset(center.x - 16.dp.toPx(), center.y + 12.dp.toPx() - flutter),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Legs bent at knees arched upwards
        drawLine(
            color = PurpleNeon,
            start = Offset(center.x + 8.dp.toPx(), center.y),
            end = Offset(center.x + 18.dp.toPx(), center.y - 11.dp.toPx()),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = PurpleNeon,
            start = Offset(center.x + 8.dp.toPx(), center.y),
            end = Offset(center.x + 18.dp.toPx(), center.y + 11.dp.toPx()),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

