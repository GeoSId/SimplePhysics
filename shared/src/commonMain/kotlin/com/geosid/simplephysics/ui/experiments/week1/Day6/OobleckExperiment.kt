package com.geosid.simplephysics.ui.experiments.week1.Day6

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
import kotlin.time.Clock

enum class OobleckAction(val title: String, val icon: String) {
    TOUCH_SWIPE("Free Touch", "👆"),
    FAST_STRIKE("Fast Strike", "🥊"),
    SLOW_DIP("Slow Dip", "🥄"),
    KNEAD_MELT("Roll & Melt", "✊")
}

data class Ripple(
    val x: Float,
    val y: Float,
    var radius: Float = 5f,
    var alpha: Float = 0.8f,
    val isSolidCrack: Boolean = false
)

@Composable
fun OobleckExperiment(
    modifier: Modifier = Modifier
) {
    var selectedAction by remember { mutableStateOf(OobleckAction.TOUCH_SWIPE) }
    var starchConcentration by remember { mutableStateOf(0.65f) } // 55% to 75% mass ratio
    var isRunning by remember { mutableStateOf(true) }

    // Live fluid state variables
    var shearRate by remember { mutableStateOf(0f) } // 1/s
    var apparentViscosity by remember { mutableStateOf(0.12f) } // Pa·s
    var shearStress by remember { mutableStateOf(0f) } // kPa
    var jammingFraction by remember { mutableStateOf(0f) } // 0.0 (liquid) to 1.0 (hardened solid)

    // Interactive pointer state
    var touchPos by remember { mutableStateOf<Offset?>(null) }
    var lastTouchPos by remember { mutableStateOf<Offset?>(null) }
    var lastTouchTime by remember { mutableStateOf(0L) }
    var touchDepth by remember { mutableStateOf(0f) } // 0.0 (surface) to 1.0 (bottom)

    // Animation state for automated demonstrations (Strike / Dip / Knead)
    var demoPhase by remember { mutableStateOf(0f) } // 0f to 1f periodic
    var demoToolY by remember { mutableStateOf(0f) }
    var demoToolRadius by remember { mutableStateOf(26f) }
    val ripples = remember { mutableStateListOf<Ripple>() }

    // Physics Engine & Animation Loop
    LaunchedEffect(isRunning, selectedAction, starchConcentration) {
        if (isRunning) {
            var lastNanos = withFrameNanos { it }
            while (isRunning) {
                val currentNanos = withFrameNanos { it }
                val dt = ((currentNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastNanos = currentNanos

                // Dilatancy exponent n: n > 1 indicates shear-thickening (dilatant)
                val powerLawN = 1.4f + starchConcentration * 1.0f // 1.95 to 2.15
                val consistencyIndexK = 0.08f + starchConcentration * 0.15f

                when (selectedAction) {
                    OobleckAction.TOUCH_SWIPE -> {
                        // User interacts directly via pointer gestures; decay shear rate when idle
                        shearRate = max(0f, shearRate - dt * 25f)
                        if (shearRate < 1f) {
                            // Slowly sink into liquid if touching
                            if (touchPos != null) {
                                touchDepth = min(0.85f, touchDepth + dt * 0.4f)
                            } else {
                                touchDepth = max(0f, touchDepth - dt * 0.6f)
                            }
                        } else {
                            // High shear resists penetration!
                            touchDepth = max(0.05f, touchDepth - dt * 1.5f)
                        }
                    }

                    OobleckAction.FAST_STRIKE -> {
                        // Rapid hammer punch downward every 1.5s
                        demoPhase = (demoPhase + dt * 0.85f) % 1f
                        if (demoPhase < 0.25f) {
                            // Rapid downward strike (v ~ 6 m/s)
                            val strikeProgress = demoPhase / 0.25f
                            demoToolY = strikeProgress
                            shearRate = 65f * (1f + starchConcentration)
                            if (strikeProgress > 0.85f && ripples.size < 6) {
                                ripples.add(Ripple(0f, 0f, isSolidCrack = true))
                            }
                        } else if (demoPhase < 0.45f) {
                            // Elastic solid rebound! (Resisted by jammed cornstarch network)
                            val reboundProgress = (demoPhase - 0.25f) / 0.20f
                            demoToolY = 1f - reboundProgress * 0.7f
                            shearRate = max(5f, shearRate - dt * 40f)
                        } else {
                            // Retracting to top
                            demoToolY = 0.3f * (1f - (demoPhase - 0.45f) / 0.55f)
                            shearRate = max(0f, shearRate - dt * 15f)
                            touchDepth = 0.02f
                        }
                    }

                    OobleckAction.SLOW_DIP -> {
                        // Gentle probe descent (v ~ 0.1 m/s)
                        demoPhase = (demoPhase + dt * 0.35f) % 1f
                        if (demoPhase < 0.50f) {
                            // Slow smooth penetration
                            demoToolY = demoPhase / 0.50f
                            shearRate = 1.2f // very low shear
                            touchDepth = demoToolY * 0.75f
                            if (ripples.size < 4 && (demoPhase * 10).toInt() % 2 == 0) {
                                ripples.add(Ripple(0f, 0f, isSolidCrack = false))
                            }
                        } else {
                            // Slow smooth extraction
                            demoToolY = 1f - (demoPhase - 0.50f) / 0.50f
                            shearRate = 1.5f
                            touchDepth = demoToolY * 0.75f
                        }
                    }

                    OobleckAction.KNEAD_MELT -> {
                        // Rapid kneading cycle (solid ball) followed by holding still (liquid melt)
                        demoPhase = (demoPhase + dt * 0.30f) % 1f
                        if (demoPhase < 0.55f) {
                            // High agitation rolling: solidifies into a ball
                            shearRate = 45f + sin(demoPhase * 40f) * 15f
                            demoToolRadius = 36f
                        } else {
                            // Hand stops: fluid relaxes, melts and drips downward
                            shearRate = max(0f, shearRate - dt * 30f)
                            demoToolRadius = 36f + (demoPhase - 0.55f) * 40f
                        }
                    }
                }

                // Apparent Viscosity: eta = K * gamma^(n - 1)
                apparentViscosity = if (shearRate < 0.1f) {
                    consistencyIndexK
                } else {
                    consistencyIndexK * (shearRate.toDouble().pow((powerLawN - 1).toDouble())).toFloat()
                }

                // Shear Stress: tau = eta * gamma
                shearStress = (apparentViscosity * shearRate) / 1000f // kPa

                // Jamming fraction: 0 (liquid) to 1 (jammed solid)
                val targetJamming = (shearRate / 25f).coerceIn(0f, 1f)
                jammingFraction += (targetJamming - jammingFraction) * min(1f, dt * 8f)

                // Update & decay ripples
                val iterator = ripples.iterator()
                while (iterator.hasNext()) {
                    val rip = iterator.next()
                    rip.radius += dt * if (rip.isSolidCrack) 65f else 35f
                    rip.alpha -= dt * if (rip.isSolidCrack) 1.2f else 0.6f
                    if (rip.alpha <= 0f) iterator.remove()
                }
            }
        }
    }

    val fluidStateText = when {
        jammingFraction > 0.55f -> "🧱 SOLID (JAMMED)"
        jammingFraction > 0.20f -> "⚡ SHEAR-THICKENING"
        else -> "💧 LIQUID (RELAXED)"
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag slowly to sink like a liquid, or strike/swipe rapidly to watch the fluid freeze solid into a rigid force network!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                touchPos = offset
                                lastTouchPos = offset
                                lastTouchTime = Clock.System.now().toEpochMilliseconds()
                            },
                            onDragEnd = {
                                touchPos = null
                                lastTouchPos = null
                            },
                            onDragCancel = {
                                touchPos = null
                                lastTouchPos = null
                            },
                            onDrag = { change, _ ->
                                val now = Clock.System.now().toEpochMilliseconds()
                                val dt = max(1L, now - lastTouchTime) / 1000f
                                lastTouchTime = now

                                lastTouchPos?.let { last ->
                                    val dist = hypot(change.position.x - last.x, change.position.y - last.y)
                                    val speedPxSec = dist / dt
                                    // Scale to shear rate (1/s)
                                    val instantaneousShear = speedPxSec * 0.045f
                                    shearRate = max(shearRate, instantaneousShear).coerceIn(0f, 120f)

                                    // Add fracture ripples if high shear, smooth ripples if low shear
                                    if (ripples.size < 8 && dist > 12f) {
                                        ripples.add(
                                            Ripple(
                                                x = change.position.x,
                                                y = change.position.y,
                                                isSolidCrack = instantaneousShear > 18f
                                            )
                                        )
                                    }
                                }
                                touchPos = change.position
                                lastTouchPos = change.position
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // Rapid tap generates instantaneous high shear impact
                            shearRate = 75f
                            ripples.add(Ripple(offset.x, offset.y, isSolidCrack = true))
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.5f, h * 0.50f)

                // 1. Subtle Scientific Grid
                drawScientificGrid(w, h)

                // 2. Dish Dimensions
                val dishRadiusX = min(w * 0.44f, 220.dp.toPx())
                val dishRadiusY = dishRadiusX * 0.72f

                // 3. Drop Shadow under Petri Dish
                drawOval(
                    color = Color.Black.copy(alpha = 0.50f),
                    topLeft = Offset(center.x - dishRadiusX * 0.95f, center.y - dishRadiusY * 0.85f + 16f),
                    size = Size(dishRadiusX * 1.9f, dishRadiusY * 1.8f)
                )

                // 4. Lab Petri Dish Base & Outer Rim
                drawPetriDishRim(
                    center = center,
                    rx = dishRadiusX,
                    ry = dishRadiusY
                )

                // 5. Oobleck Fluid Basin (Color shifts from liquid milky turquoise to solid chalky cyan)
                drawOobleckSurface(
                    center = center,
                    rx = dishRadiusX * 0.92f,
                    ry = dishRadiusY * 0.92f,
                    jammingFraction = jammingFraction
                )

                // 6. Fluid Ripples & Solid Crystalline Fracture Cracks
                drawRipplesAndCracks(
                    center = center,
                    ripples = ripples
                )

                // 7. Interactive Finger Probe / Demo Tools
                when (selectedAction) {
                    OobleckAction.TOUCH_SWIPE -> {
                        touchPos?.let { pos ->
                            drawTouchFinger(
                                position = pos,
                                depth = touchDepth,
                                jamming = jammingFraction
                            )
                        }
                    }

                    OobleckAction.FAST_STRIKE -> {
                        val strikeY = center.y - dishRadiusY * 0.4f + demoToolY * (dishRadiusY * 0.45f)
                        drawHammerTool(
                            centerX = center.x,
                            centerY = strikeY,
                            isImpacted = demoToolY > 0.85f,
                            jamming = jammingFraction
                        )
                    }

                    OobleckAction.SLOW_DIP -> {
                        val probeY = center.y - dishRadiusY * 0.4f + demoToolY * (dishRadiusY * 0.45f)
                        drawSpoonProbe(
                            centerX = center.x,
                            centerY = probeY,
                            depth = touchDepth
                        )
                    }

                    OobleckAction.KNEAD_MELT -> {
                        drawKneadBallAndDrip(
                            center = center,
                            phase = demoPhase,
                            jamming = jammingFraction
                        )
                    }
                }
            }
        },
        hudContent = {
            TransparentTelemetryHud(
                modifier = Modifier.fillMaxWidth(),
                title = "Ostwald–de Waele Dilatant Telemetry",
                items = listOf(
                    "Shear Rate (γ̇)" to "${round(shearRate * 10f) / 10f} s⁻¹",
                    "Apparent Viscosity (η)" to "${round(apparentViscosity * 100f) / 100f} Pa·s",
                    "Shear Stress (τ)" to "${round(shearStress * 100f) / 100f} kPa",
                    "Fluid State" to fluidStateText
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action Mode Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OobleckAction.values().forEach { action ->
                        FilterChip(
                            selected = selectedAction == action,
                            onClick = {
                                selectedAction = action
                                demoPhase = 0f
                                ripples.clear()
                                isRunning = true
                            },
                            label = {
                                Text(
                                    text = "${action.icon} ${action.title}",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedAction == action) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.20f),
                                selectedLabelColor = CyanNeon,
                                containerColor = ScienceDarkSurfaceVariant.copy(alpha = 0.5f),
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedAction == action,
                                borderColor = ScienceBorder.copy(alpha = 0.4f),
                                selectedBorderColor = CyanNeon
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Cornstarch Concentration Slider
                PhysicsSliderControl(
                    title = "Cornstarch Concentration (Dilatant Factor)",
                    value = starchConcentration,
                    range = 0.50f..0.75f,
                    valueDisplay = "${(starchConcentration * 100).toInt()}% (n ≈ ${round((1.4f + starchConcentration) * 100f) / 100f})",
                    accentColor = CyanNeon,
                    onValueChange = {
                        starchConcentration = it
                    }
                )

                // Transport Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberVibrant else CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            selectedAction = OobleckAction.TOUCH_SWIPE
                            starchConcentration = 0.65f
                            shearRate = 0f
                            touchDepth = 0f
                            jammingFraction = 0f
                            ripples.clear()
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

// ---------------------------------------------------------------------------
// High-Fidelity Non-Newtonian Drawing Helpers
// ---------------------------------------------------------------------------

/**
 * Petri dish lab rim
 */
private fun DrawScope.drawPetriDishRim(
    center: Offset,
    rx: Float,
    ry: Float
) {
    // Glass Rim Outer Glow
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF37474F), Color(0xFF263238)),
            center = center,
            radius = rx * 1.08f
        ),
        topLeft = Offset(center.x - rx * 1.05f, center.y - ry * 1.05f),
        size = Size(rx * 2.1f, ry * 2.1f)
    )

    // Glass Bevel Rim
    drawOval(
        color = Color.White.copy(alpha = 0.45f),
        topLeft = Offset(center.x - rx * 1.02f, center.y - ry * 1.02f),
        size = Size(rx * 2.04f, ry * 2.04f),
        style = Stroke(width = 3.5f)
    )
}

/**
 * Oobleck fluid surface with dynamic jamming color shift
 */
private fun DrawScope.drawOobleckSurface(
    center: Offset,
    rx: Float,
    ry: Float,
    jammingFraction: Float
) {
    // Color transitions from milky minty fluid to chalky solid white under high shear
    val liquidColorStart = Color(0xFF80DEEA) // Minty translucent turquoise
    val liquidColorEnd = Color(0xFF00838F)
    val solidColorStart = Color(0xFFE0F7FA) // Chalky dense white/cyan
    val solidColorEnd = Color(0xFF4DD0E1)

    val color1 = lerp(liquidColorStart, solidColorStart, jammingFraction)
    val color2 = lerp(liquidColorEnd, solidColorEnd, jammingFraction)

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(color1, color2),
            center = Offset(center.x - rx * 0.2f, center.y - ry * 0.25f),
            radius = rx * 1.15f
        ),
        topLeft = Offset(center.x - rx, center.y - ry),
        size = Size(rx * 2f, ry * 2f)
    )

    // Fluid surface gloss / specular highlight
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.55f * (1f - jammingFraction * 0.4f)), Color.Transparent),
            start = Offset(center.x - rx * 0.7f, center.y - ry * 0.7f),
            end = Offset(center.x + rx * 0.4f, center.y)
        ),
        topLeft = Offset(center.x - rx * 0.82f, center.y - ry * 0.82f),
        size = Size(rx * 1.64f, ry * 0.8f)
    )
}

/**
 * Propagating ripples or crystalline fracture cracks
 */
private fun DrawScope.drawRipplesAndCracks(
    center: Offset,
    ripples: List<Ripple>
) {
    for (rip in ripples) {
        val rx = if (rip.x != 0f) rip.x else center.x
        val ry = if (rip.y != 0f) rip.y else center.y

        if (rip.isSolidCrack) {
            // Crystalline fracture crack rays radiating from impact point
            val rays = 6
            for (i in 0 until rays) {
                val angle = i * (2 * PI.toFloat() / rays) + (i % 2) * 0.2f
                val crackLen = rip.radius * 0.85f
                val endX = rx + cos(angle) * crackLen
                val endY = ry + sin(angle) * crackLen * 0.65f

                drawLine(
                    color = Color.White.copy(alpha = rip.alpha),
                    start = Offset(rx, ry),
                    end = Offset(endX, endY),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
            // Impact shock ring
            drawOval(
                color = CyanNeon.copy(alpha = rip.alpha * 0.7f),
                topLeft = Offset(rx - rip.radius, ry - rip.radius * 0.65f),
                size = Size(rip.radius * 2f, rip.radius * 1.3f),
                style = Stroke(width = 2f)
            )
        } else {
            // Smooth viscous fluid ring
            drawOval(
                color = Color.White.copy(alpha = rip.alpha * 0.45f),
                topLeft = Offset(rx - rip.radius, ry - rip.radius * 0.65f),
                size = Size(rip.radius * 2f, rip.radius * 1.3f),
                style = Stroke(width = 1.8f)
            )
        }
    }
}

/**
 * Interactive finger probe
 */
private fun DrawScope.drawTouchFinger(
    position: Offset,
    depth: Float,
    jamming: Float
) {
    val fingerR = 18f
    // Fluid meniscus indentation around finger
    drawOval(
        color = Color.Black.copy(alpha = 0.25f + depth * 0.35f),
        topLeft = Offset(position.x - fingerR * 1.4f, position.y - fingerR * 0.9f),
        size = Size(fingerR * 2.8f, fingerR * 1.8f)
    )

    // Finger / Stylus head
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFCCBC),
                Color(0xFFFF8A65),
                Color(0xFFD84315)
            ),
            center = Offset(position.x - 3f, position.y - 4f),
            radius = fingerR * 1.2f
        ),
        radius = fingerR * (1f - depth * 0.25f),
        center = position
    )

    // Jamming stress indicator halo around touch
    if (jamming > 0.15f) {
        drawCircle(
            color = CyanNeon.copy(alpha = jamming * 0.8f),
            radius = fingerR * 1.6f,
            center = position,
            style = Stroke(width = 2.5f)
        )
    }
}

/**
 * Hammer / Fist tool for fast strike
 */
private fun DrawScope.drawHammerTool(
    centerX: Float,
    centerY: Float,
    isImpacted: Boolean,
    jamming: Float
) {
    val hammerW = 48f
    val hammerH = 32f

    // Hammer Metal Head
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White, Color(0xFFCFD8DC), Color(0xFF455A64))
        ),
        topLeft = Offset(centerX - hammerW * 0.5f, centerY - hammerH),
        size = Size(hammerW, hammerH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Impact shockwave flash
    if (isImpacted) {
        drawCircle(
            color = Color.White,
            radius = 28f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 4f)
        )
    }

    // Wooden Handle
    drawLine(
        color = Color(0xFF8D6E63),
        start = Offset(centerX, centerY - hammerH),
        end = Offset(centerX, centerY - hammerH - 55f),
        strokeWidth = 10f,
        cap = StrokeCap.Round
    )
}

/**
 * Spoon / sphere probe for slow sinking
 */
private fun DrawScope.drawSpoonProbe(
    centerX: Float,
    centerY: Float,
    depth: Float
) {
    val probeR = 16f

    // Chrome spherical probe
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFFB0BEC5), Color(0xFF37474F)),
            center = Offset(centerX - 4f, centerY - 4f),
            radius = probeR * 1.2f
        ),
        radius = probeR,
        center = Offset(centerX, centerY)
    )

    // Depth displacement wake
    if (depth > 0.1f) {
        drawOval(
            color = Color(0xFF006064).copy(alpha = depth * 0.6f),
            topLeft = Offset(centerX - probeR * 1.3f, centerY - probeR * 0.6f),
            size = Size(probeR * 2.6f, probeR * 1.2f),
            style = Stroke(width = 2f)
        )
    }

    // Probe handle rod
    drawLine(
        color = Color(0xFFCFD8DC),
        start = Offset(centerX, centerY - probeR),
        end = Offset(centerX, centerY - probeR - 65f),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
}

/**
 * Kneading into solid ball then melting and dripping
 */
private fun DrawScope.drawKneadBallAndDrip(
    center: Offset,
    phase: Float,
    jamming: Float
) {
    if (phase < 0.55f) {
        // Firm solid ball being rolled
        val ballR = 34f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFFE0F7FA), Color(0xFF0097A7)),
                center = Offset(center.x - 6f, center.y - 6f),
                radius = ballR * 1.3f
            ),
            radius = ballR,
            center = center
        )
        // Surface shear fracture lines on the ball
        drawLine(Color.White, Offset(center.x - 14f, center.y - 10f), Offset(center.x + 8f, center.y - 12f), 2f)
        drawLine(Color.White, Offset(center.x - 8f, center.y + 10f), Offset(center.x + 12f, center.y + 8f), 2f)
    } else {
        // Relaxing and melting downward like liquid cream
        val meltProgress = (phase - 0.55f) / 0.45f
        val puddleW = 40f + meltProgress * 45f
        val puddleH = 22f + meltProgress * 15f

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF80DEEA), Color(0xFF00838F)),
                center = center,
                radius = puddleW
            ),
            topLeft = Offset(center.x - puddleW * 0.5f, center.y - puddleH * 0.5f),
            size = Size(puddleW, puddleH)
        )

        // Drips running downward
        for (i in -1..1) {
            val dripX = center.x + i * 16f
            val dripY = center.y + puddleH * 0.5f + meltProgress * 28f + (i * 4f)
            drawCircle(
                color = Color(0xFF80DEEA),
                radius = 3.5f,
                center = Offset(dripX, dripY)
            )
        }
    }
}

/**
 * Coordinate Grid Background
 */
private fun DrawScope.drawScientificGrid(w: Float, h: Float) {
    val step = 36f
    var x = 0f
    while (x < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.12f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 0.6f
        )
        x += step
    }
    var y = 0f
    while (y < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.12f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 0.6f
        )
        y += step
    }
}

/**
 * Transparent Telemetry HUD
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
                text = "🥣 $title".uppercase(),
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
                    text = "POWER LAW",
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
