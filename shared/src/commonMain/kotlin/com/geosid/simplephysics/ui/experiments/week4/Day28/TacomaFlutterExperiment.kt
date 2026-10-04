package com.geosid.simplephysics.ui.experiments.week4.Day28

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
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
import kotlin.random.Random

enum class TacomaPreset(
    val label: String,
    val windMps: Float,
    val isTruss: Boolean,
    val desc: String
) {
    CALM_BREEZE("Calm (10 mph)", 4.5f, false, "Stable laminar flow, decaying oscillations"),
    MODERATE("Moderate (25 mph)", 11.2f, false, "Light oscillation, structural damping holds"),
    TACOMA_COLLAPSE("Gale (42 mph)", 18.8f, false, "Catastrophic aeroelastic flutter collapse!"),
    RETROFIT_TRUSS("Modern Truss", 24.0f, true, "Open aerodynamic truss eliminates flutter")
}

data class WindStreamline(
    var x: Float,
    var y: Float,
    val speedFactor: Float,
    val length: Float,
    val baseY: Float
)

@Composable
fun TacomaFlutterExperiment(
    modifier: Modifier = Modifier
) {
    // Physical system constants & parameters
    val airDensity = 1.225f // kg/m^3
    val deckWidth = 11.9f // meters (B)
    val momentOfInertia = 45000f // kg*m^2 per meter of span
    val stiffnessCoeff = 18000f // torsional spring stiffness (N*m/rad)
    val dampingCoeff = 850f // structural damping c_theta
    val criticalWindSpeed = 15.5f // m/s (~35 mph critical flutter boundary)
    val nominalTension = 140000f // N
    val cableStiffness = 32000f // N/m
    val maxCableTension = 310000f // N yield limit
    val failureAngleRad = 35f * (PI.toFloat() / 180f) // ~0.61 rad

    // Dynamic state
    var windSpeed by remember { mutableStateOf(18.8f) } // m/s (42 mph)
    var isTrussRetrofit by remember { mutableStateOf(false) } // Open truss vs solid H-girder
    var isRunning by remember { mutableStateOf(true) }
    var showStreamlines by remember { mutableStateOf(true) }
    var showStressField by remember { mutableStateOf(true) }
    var selectedPreset by remember { mutableStateOf<TacomaPreset?>(TacomaPreset.TACOMA_COLLAPSE) }

    // Physical degree-of-freedom state (angle theta and angular velocity thetaDot)
    var theta by remember { mutableStateOf(0.06f) } // Initial small perturbation rad
    var thetaDot by remember { mutableStateOf(0.0f) } // rad/s
    var isCollapsed by remember { mutableStateOf(false) }
    var collapseTime by remember { mutableStateOf(0f) }
    var simTime by remember { mutableStateOf(0f) }

    // Wind particles for fluid animation
    val streamlines = remember {
        mutableStateListOf<WindStreamline>().apply {
            val rng = Random(42)
            repeat(45) {
                add(
                    WindStreamline(
                        x = rng.nextFloat() * 1000f,
                        y = rng.nextFloat() * 600f,
                        speedFactor = 0.8f + rng.nextFloat() * 0.4f,
                        length = 15f + rng.nextFloat() * 25f,
                        baseY = rng.nextFloat() * 600f
                    )
                )
            }
        }
    }

    fun resetSimulation() {
        theta = 0.05f
        thetaDot = 0f
        isCollapsed = false
        collapseTime = 0f
        simTime = 0f
        isRunning = true
    }

    // Aerodynamic coefficients based on deck geometry:
    // Solid H-Girder: positive A2* derivative -> negative aerodynamic damping!
    // Open Truss Retrofit: negative A2* -> positive aerodynamic damping (aerodynamically stable).
    val flutterCoeff = if (isTrussRetrofit) -0.015f else 0.048f
    val stallCoeff = if (isTrussRetrofit) 0.02f else 0.12f

    // High performance physical simulation loop
    LaunchedEffect(isRunning, windSpeed, isTrussRetrofit, isCollapsed) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) {
                    lastNanos = nanos
                    return@withFrameNanos
                }
                val rawDt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastNanos = nanos

                if (isRunning) {
                    simTime += rawDt

                    // Sub-stepped physics integration for rock-solid stability
                    val subSteps = 6
                    val dt = rawDt / subSteps

                    repeat(subSteps) {
                        if (!isCollapsed) {
                            // 1. Torsional equation of motion and self-excited aerodynamic torque
                            val aeroTorque = 0.5f * airDensity * windSpeed * windSpeed * deckWidth * deckWidth * (flutterCoeff * thetaDot + stallCoeff * sin(theta))
                            val angularAccel = (aeroTorque - dampingCoeff * thetaDot - stiffnessCoeff * theta) / momentOfInertia

                            thetaDot += angularAccel * dt
                            theta += thetaDot * dt

                            // Check collapse threshold
                            val cableTensionL = nominalTension + 0.5f * cableStiffness * deckWidth * abs(sin(theta))
                            if (abs(theta) >= failureAngleRad || cableTensionL >= maxCableTension) {
                                isCollapsed = true
                                collapseTime = simTime
                            }
                        } else {
                            // Catastrophic post-failure tumbling / damping
                            thetaDot += (-250f * thetaDot - 800f * sin(theta)) / momentOfInertia * dt
                            theta += thetaDot * dt
                        }
                    }
                }
            }
        }
    }

    // 2. Net damping and critical aeroelastic flutter threshold
    val netDamping = dampingCoeff - 0.5f * airDensity * windSpeed * windSpeed * deckWidth * deckWidth * flutterCoeff
    val isFluttering = windSpeed >= criticalWindSpeed && netDamping < 0f && !isTrussRetrofit

    // 3. Suspension cable tension and structural collapse threshold
    val cableTensionLeft = nominalTension + 0.5f * cableStiffness * deckWidth * sin(theta)
    val cableTensionRight = nominalTension - 0.5f * cableStiffness * deckWidth * sin(theta)
    val maxCurrentTension = max(cableTensionLeft, cableTensionRight)

    val thetaDeg = theta * (180f / PI.toFloat())
    val windMph = windSpeed * 2.23694f

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "Adjust crosswind velocity or drag the bridge deck. Exceed 35 mph to trigger catastrophic self-excited torsional flutter!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (!isCollapsed) {
                                // Direct user manipulation of twist angle
                                val dAngle = (dragAmount.y / size.height) * 1.5f
                                theta = (theta + dAngle).coerceIn(-0.65f, 0.65f)
                                thetaDot = 0f
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (!isCollapsed) {
                                // Impart small angular kick
                                thetaDot += 0.15f
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated apparatus altitude origin (cy = h * 0.40f)
                val cx = w * 0.50f
                val cy = h * 0.40f

                // 1. Atmospheric Sky & Sound/Water Background
                drawTacomaAtmosphere(w, h, cy, windSpeed)

                // 2. Dynamic Wind Streamlines & Vortex Shedding
                if (showStreamlines) {
                    drawWindStreamlines(
                        streamlines = streamlines,
                        w = w,
                        h = h,
                        cy = cy,
                        windSpeed = windSpeed,
                        theta = theta,
                        isCollapsed = isCollapsed,
                        isRunning = isRunning
                    )
                }

                // 3. Main Overhead Suspension Towers & Cables
                drawSuspensionSystem(
                    w = w,
                    cx = cx,
                    cy = cy,
                    theta = theta,
                    deckSpanPx = min(w * 0.58f, 380.dp.toPx()),
                    isCollapsed = isCollapsed,
                    cableTensionLeft = cableTensionLeft,
                    cableTensionRight = cableTensionRight,
                    maxCableTension = maxCableTension
                )

                // 4. Rotating H-Girder Road Deck (Torsional Cross-Section)
                drawTacomaBridgeDeck(
                    cx = cx,
                    cy = cy,
                    theta = theta,
                    deckSpanPx = min(w * 0.58f, 380.dp.toPx()),
                    isTruss = isTrussRetrofit,
                    isCollapsed = isCollapsed,
                    showStress = showStressField
                )

                // 5. Angular Tilt Gauge & Stress Indicators
                drawTorsionalAngleGauge(
                    cx = cx,
                    cy = cy,
                    theta = theta,
                    isFluttering = isFluttering,
                    isCollapsed = isCollapsed
                )

                // 6. Catastrophic Collapse Impact Warning
                if (isCollapsed) {
                    drawCollapseOverlay(w, h, cy, simTime - collapseTime)
                }
            }
        },
        hudContent = {
            val regimeText = when {
                isCollapsed -> "💥 STRUCTURAL COLLAPSE"
                isFluttering -> "⚠️ AEROELASTIC FLUTTER (RUNAWAY)"
                windSpeed > 14f -> "Borderline Stability"
                else -> "Aerodynamically Damped (Stable)"
            }

            val dampingText = if (netDamping < 0f && !isTrussRetrofit) {
                "${round(netDamping * 10f) / 10f} N·m·s (NEGATIVE DAMPING)"
            } else {
                "+${round(abs(netDamping) * 10f) / 10f} N·m·s (Positive / Stable)"
            }

            val tensionRatio = (maxCurrentTension / maxCableTension * 100).toInt()
            val tensionStr = "${(maxCurrentTension / 1000f).toInt()} kN ($tensionRatio% of Yield)"

            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f),
                title = "Tacoma Aeroelastic Telemetry",
                items = listOf(
                    "Flight / Flow Regime" to regimeText,
                    "Crosswind Velocity U" to "${round(windMph * 10f) / 10f} mph (${round(windSpeed * 10f) / 10f} m/s)",
                    "Deck Torsional Twist θ" to "${round(thetaDeg * 10f) / 10f}° (${round(theta * 100f) / 100f} rad)",
                    "Effective Damping c_eff" to dampingText,
                    "Peak Cable Tension" to tensionStr,
                    "Deck Aerodynamic Profile" to if (isTrussRetrofit) "Streamlined Truss (Stable)" else "Solid H-Girder (Bluff Body)"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TacomaPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AmberVibrant.copy(alpha = 0.22f) else ScienceDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AmberVibrant else ScienceBorder.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedPreset = preset
                                    windSpeed = preset.windMps
                                    isTrussRetrofit = preset.isTruss
                                    resetSimulation()
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text(
                                    text = preset.label,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AmberVibrant else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Dual Sliders in Compact Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Wind Velocity U",
                        value = windSpeed,
                        range = 0.0f..32.0f,
                        valueDisplay = "${(windSpeed * 2.237f).toInt()} mph",
                        accentColor = if (isFluttering) CoralNeon else CyanNeon,
                        onValueChange = {
                            windSpeed = it
                            selectedPreset = null
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PhysicsSliderControl(
                        title = "Torsional Angle θ",
                        value = theta,
                        range = -0.62f..0.62f,
                        valueDisplay = "${round(thetaDeg * 10f) / 10f}°",
                        accentColor = AmberVibrant,
                        onValueChange = {
                            if (!isCollapsed) {
                                theta = it
                                thetaDot = 0f
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Action Buttons Row (Compact height 32..36dp)
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
                            .weight(1.3f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isTrussRetrofit = !isTrussRetrofit
                            resetSimulation()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isTrussRetrofit) EmeraldNeon else ScienceBorder
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isTrussRetrofit) "Deck: Truss" else "Deck: H-Girder",
                            fontSize = 10.sp,
                            color = if (isTrussRetrofit) EmeraldNeon else TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = { showStreamlines = !showStreamlines },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (showStreamlines) CyanNeon else ScienceBorder
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.0f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (showStreamlines) "Wind: ON" else "Wind: OFF",
                            fontSize = 10.sp,
                            color = if (showStreamlines) CyanNeon else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { resetSimulation() },
                        modifier = Modifier
                            .size(34.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(8.dp))
                    ) {
                        ResetIcon(tint = AmberVibrant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    )
}

// -------------------------------------------------------------
// Canvas Rendering Helper Functions
// -------------------------------------------------------------

private fun DrawScope.drawTacomaAtmosphere(
    w: Float,
    h: Float,
    cy: Float,
    windSpeed: Float
) {
    // Puget Sound water surface below
    val waterY = h * 0.82f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(ScienceDarkBg, Color(0xFF0A1C2A)),
            startY = waterY,
            endY = h
        ),
        topLeft = Offset(0f, waterY),
        size = Size(w, h - waterY)
    )

    // Water tide boundary line
    drawLine(
        color = WaterDeep.copy(alpha = 0.45f),
        start = Offset(0f, waterY),
        end = Offset(w, waterY),
        strokeWidth = 1.2.dp.toPx()
    )

    // Scientific grid
    val gridColor = ScienceBorder.copy(alpha = 0.18f)
    val dy = 35.dp.toPx()
    for (i in -4..4) {
        val y = cy + i * dy
        drawLine(gridColor, Offset(0f, y), Offset(w, y), 0.8f)
    }
}

private fun DrawScope.drawWindStreamlines(
    streamlines: List<WindStreamline>,
    w: Float,
    h: Float,
    cy: Float,
    windSpeed: Float,
    theta: Float,
    isCollapsed: Boolean,
    isRunning: Boolean
) {
    val speedPx = windSpeed * 22f
    val dtApprox = 0.016f

    streamlines.forEach { s ->
        if (isRunning) {
            s.x += speedPx * s.speedFactor * dtApprox
            if (s.x > w + 40f) {
                s.x = -40f
            }
        }

        // Deflection around rotating deck
        val dxCenter = s.x - (w * 0.5f)
        val inWake = abs(dxCenter) < 140.dp.toPx()
        val defY = if (inWake && !isCollapsed) {
            sin(theta) * 35.dp.toPx() * (1f - abs(dxCenter) / 140.dp.toPx())
        } else 0f

        val py = s.baseY + defY
        val lineAlpha = (0.25f + (windSpeed / 35f) * 0.45f).coerceIn(0.1f, 0.7f)
        val lineColor = if (windSpeed > 15f && inWake && abs(theta) > 0.15f) {
            AmberVibrant.copy(alpha = lineAlpha)
        } else {
            CyanNeon.copy(alpha = lineAlpha)
        }

        drawLine(
            color = lineColor,
            start = Offset(s.x, py),
            end = Offset(s.x + s.length * (windSpeed / 15f).coerceAtLeast(0.6f), py),
            strokeWidth = 1.2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Wake vortex swirls behind the trailing edge
        if (dxCenter > 60.dp.toPx() && abs(theta) > 0.2f && !isCollapsed) {
            drawCircle(
                color = CoralNeon.copy(alpha = 0.22f),
                radius = 7.dp.toPx(),
                center = Offset(s.x + 10f, py),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawSuspensionSystem(
    w: Float,
    cx: Float,
    cy: Float,
    theta: Float,
    deckSpanPx: Float,
    isCollapsed: Boolean,
    cableTensionLeft: Float,
    cableTensionRight: Float,
    maxCableTension: Float
) {
    val towerTopY = cy - 140.dp.toPx()
    val halfSpan = deckSpanPx * 0.5f

    // Main Cable Anchor Points (Towers)
    val towerLeftX = cx - halfSpan * 1.05f
    val towerRightX = cx + halfSpan * 1.05f

    // Tower Pylons
    drawLine(
        color = ScienceBorder.copy(alpha = 0.7f),
        start = Offset(towerLeftX, towerTopY),
        end = Offset(towerLeftX, cy + 80.dp.toPx()),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = ScienceBorder.copy(alpha = 0.7f),
        start = Offset(towerRightX, towerTopY),
        end = Offset(towerRightX, cy + 80.dp.toPx()),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Main Overhead Parabolic Cable
    val cablePath = Path().apply {
        moveTo(towerLeftX, towerTopY)
        quadraticTo(cx, towerTopY + 45.dp.toPx(), towerRightX, towerTopY)
    }
    drawPath(
        path = cablePath,
        color = CyanNeon.copy(alpha = 0.8f),
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // Dynamic deck connection points
    val deckL = Offset(cx - halfSpan * cos(theta), cy - halfSpan * sin(theta))
    val deckR = Offset(cx + halfSpan * cos(theta), cy + halfSpan * sin(theta))

    val hangerTopL = Offset(cx - halfSpan * 0.85f, towerTopY + 36.dp.toPx())
    val hangerTopR = Offset(cx + halfSpan * 0.85f, towerTopY + 36.dp.toPx())

    // Left Suspender Cable
    val ratioL = (cableTensionLeft / maxCableTension).coerceIn(0f, 1.2f)
    val colorL = when {
        isCollapsed -> CoralNeon.copy(alpha = 0.4f)
        ratioL > 0.8f -> CoralNeon
        ratioL > 0.5f -> AmberVibrant
        else -> CyanNeon
    }

    if (!isCollapsed || theta < 0f) {
        drawLine(
            color = colorL,
            start = hangerTopL,
            end = deckL,
            strokeWidth = (if (ratioL > 0.8f) 3.5.dp else 2.dp).toPx(),
            cap = StrokeCap.Round
        )
    } else {
        // Snapped loose cable segment
        drawLine(
            color = CoralNeon,
            start = hangerTopL,
            end = Offset(hangerTopL.x - 15f, hangerTopL.y + 60.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Right Suspender Cable
    val ratioR = (cableTensionRight / maxCableTension).coerceIn(0f, 1.2f)
    val colorR = when {
        isCollapsed -> CoralNeon.copy(alpha = 0.4f)
        ratioR > 0.8f -> CoralNeon
        ratioR > 0.5f -> AmberVibrant
        else -> CyanNeon
    }

    if (!isCollapsed || theta > 0f) {
        drawLine(
            color = colorR,
            start = hangerTopR,
            end = deckR,
            strokeWidth = (if (ratioR > 0.8f) 3.5.dp else 2.dp).toPx(),
            cap = StrokeCap.Round
        )
    } else {
        // Snapped loose cable segment
        drawLine(
            color = CoralNeon,
            start = hangerTopR,
            end = Offset(hangerTopR.x + 15f, hangerTopR.y + 60.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawTacomaBridgeDeck(
    cx: Float,
    cy: Float,
    theta: Float,
    deckSpanPx: Float,
    isTruss: Boolean,
    isCollapsed: Boolean,
    showStress: Boolean
) {
    val halfSpan = deckSpanPx * 0.5f
    val girderH = 26.dp.toPx()

    rotate(degrees = theta * (180f / PI.toFloat()), pivot = Offset(cx, cy)) {
        val deckLeft = cx - halfSpan
        val deckTop = cy - girderH * 0.5f
        val deckRight = cx + halfSpan
        val deckBottom = cy + girderH * 0.5f

        if (!isTruss) {
            // Solid Plate Girder H-Section (Tacoma Narrows original design)
            // Road roadway plate
            drawRect(
                color = ScienceDarkSurfaceVariant,
                topLeft = Offset(deckLeft, cy - 3.dp.toPx()),
                size = Size(deckSpanPx, 6.dp.toPx())
            )
            // Center yellow road stripe
            drawLine(
                color = AmberVibrant,
                start = Offset(deckLeft + 20f, cy),
                end = Offset(deckRight - 20f, cy),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )

            // Windward & Leeward Vertical Steel Plate Girders (Solid 8-ft walls)
            val girderColor = if (isCollapsed) CoralNeon else CyanNeon
            // Left Girder (I-Flange)
            drawRect(
                color = girderColor,
                topLeft = Offset(deckLeft, deckTop),
                size = Size(8.dp.toPx(), girderH)
            )
            // Right Girder (I-Flange)
            drawRect(
                color = girderColor,
                topLeft = Offset(deckRight - 8.dp.toPx(), deckTop),
                size = Size(8.dp.toPx(), girderH)
            )

            // Guard rails
            drawLine(
                color = ScienceBorder,
                start = Offset(deckLeft, deckTop - 6.dp.toPx()),
                end = Offset(deckRight, deckTop - 6.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )
        } else {
            // Modern Open Aerodynamic Truss Girder
            drawRect(
                color = ScienceDarkSurfaceVariant,
                topLeft = Offset(deckLeft, cy - 2.dp.toPx()),
                size = Size(deckSpanPx, 4.dp.toPx())
            )

            // Diagonal truss lattice (allows wind to flow cleanly through!)
            val trussSteps = 12
            val stepW = deckSpanPx / trussSteps
            for (t in 0 until trussSteps) {
                val tx = deckLeft + t * stepW
                drawLine(
                    color = EmeraldNeon.copy(alpha = 0.75f),
                    start = Offset(tx, deckTop),
                    end = Offset(tx + stepW, deckBottom),
                    strokeWidth = 1.2.dp.toPx()
                )
                drawLine(
                    color = EmeraldNeon.copy(alpha = 0.75f),
                    start = Offset(tx + stepW, deckTop),
                    end = Offset(tx, deckBottom),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
            // Truss Chord lines
            drawLine(EmeraldNeon, Offset(deckLeft, deckTop), Offset(deckRight, deckTop), 2.dp.toPx())
            drawLine(EmeraldNeon, Offset(deckLeft, deckBottom), Offset(deckRight, deckBottom), 2.dp.toPx())
        }

        // Center neutral axis pin
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = Offset(cx, cy)
        )
    }
}

private fun DrawScope.drawTorsionalAngleGauge(
    cx: Float,
    cy: Float,
    theta: Float,
    isFluttering: Boolean,
    isCollapsed: Boolean
) {
    val r = 55.dp.toPx()

    // Gauge background arc
    drawArc(
        color = ScienceBorder.copy(alpha = 0.4f),
        startAngle = -45f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(cx - r, cy - r),
        size = Size(r * 2, r * 2),
        style = Stroke(width = 2.dp.toPx())
    )

    // Current angle sweep arc
    val currentDeg = theta * (180f / PI.toFloat())
    val arcColor = when {
        isCollapsed -> CoralNeon
        isFluttering -> AmberVibrant
        else -> CyanNeon
    }

    drawArc(
        color = arcColor,
        startAngle = 0f,
        sweepAngle = currentDeg,
        useCenter = false,
        topLeft = Offset(cx - r, cy - r),
        size = Size(r * 2, r * 2),
        style = Stroke(width = 3.5.dp.toPx())
    )
}

private fun DrawScope.drawCollapseOverlay(
    w: Float,
    h: Float,
    cy: Float,
    timeSinceCollapse: Float
) {
    val alpha = (sin(timeSinceCollapse * 8f) * 0.15f + 0.25f).coerceIn(0.1f, 0.4f)
    drawRect(
        color = CoralNeon.copy(alpha = alpha),
        topLeft = Offset(0f, 0f),
        size = Size(w, h)
    )

    // Structural failure warning line across screen
    drawLine(
        color = CoralNeon,
        start = Offset(0f, cy),
        end = Offset(w, cy),
        strokeWidth = 2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
    )
}
