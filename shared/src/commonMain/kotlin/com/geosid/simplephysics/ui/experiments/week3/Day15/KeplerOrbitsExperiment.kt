package com.geosid.simplephysics.ui.experiments.week3.Day15

import androidx.compose.animation.core.*
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

/**
 * Astronomical orbital presets illustrating Kepler's Laws.
 */
enum class PlanetPreset(
    val title: String,
    val icon: String,
    val eccentricity: Float,
    val color: Color
) {
    CIRCULAR("Circle", "⚪", 0.00f, Color.White),
    EARTH("Earth", "🌍", 0.02f, CyanNeon),
    MARS("Mars", "🔴", 0.09f, CoralNeon),
    PLUTO("Pluto", "🪐", 0.25f, AmberVibrant),
    COMET("Comet", "☄️", 0.75f, PurpleNeon)
}

@Composable
fun KeplerOrbitsExperiment(
    modifier: Modifier = Modifier
) {
    // 1. Orbital State & Controls
    var selectedPreset by remember { mutableStateOf<PlanetPreset?>(PlanetPreset.PLUTO) }
    var eccentricity by remember { mutableStateOf(0.48f) }
    var orbitSpeedMultiplier by remember { mutableStateOf(1.0f) }
    var showAreaSectors by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(true) }

    // Gravitational simulation kinematic state: Eccentric Anomaly E (radians)
    var eccentricAnomalyRad by remember { mutableStateOf(0f) }

    // Central star corona pulsation
    val infiniteTransition = rememberInfiniteTransition()
    val starGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Orbital integration loop: Kepler's Equation differential form dE/dt = omega / (1 - e * cos(E))
    LaunchedEffect(isRunning, eccentricity, orbitSpeedMultiplier) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.035f)
            lastTime = now

            if (isRunning) {
                val baseOmega = 1.25f * orbitSpeedMultiplier
                val denom = (1f - eccentricity * cos(eccentricAnomalyRad)).coerceAtLeast(0.04f)
                val dE = (baseOmega / denom) * dt
                eccentricAnomalyRad = (eccentricAnomalyRad + dE) % (2f * PI.toFloat())
            }
        }
    }

    // Normalized physical values: semi-major axis a = 1.0 AU
    val rCurrentNorm = 1f - eccentricity * cos(eccentricAnomalyRad)
    val vCurrentNorm = sqrt((2f / rCurrentNorm - 1f).coerceAtLeast(0.01f))

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag horizontally across canvas to tune orbital eccentricity (e), tap canvas to pause/play!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            selectedPreset = null
                            eccentricity = (eccentricity + dragAmount.x * 0.002f).coerceIn(0.0f, 0.88f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            isRunning = !isRunning
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw Deep Space Starfield & Coordinate Grid
                drawStarfield(w, h)

                val semiMajorA = min(w * 0.38f, 210.dp.toPx())
                val focalDistC = semiMajorA * eccentricity
                val semiMinorB = semiMajorA * sqrt((1f - eccentricity * eccentricity).coerceAtLeast(0.01f))

                // Center of the Ellipse
                val ellipseCenter = Offset(w * 0.50f, h * 0.46f)
                // Focus F1 (Sun position: placed to the left by c = a * e)
                val sunPos = Offset(ellipseCenter.x - focalDistC, ellipseCenter.y)

                // 1. Draw Orbital Ellipse Track with Major Axis and Apsides (Perihelion & Aphelion)
                drawOrbitalEllipse(
                    center = ellipseCenter,
                    a = semiMajorA,
                    b = semiMinorB,
                    sunPos = sunPos,
                    eccentricity = eccentricity
                )

                // 2. Planet Position: Exactly on the ellipse curve at all times via eccentric anomaly
                // Parametric: x = center.x - a * cos(E), y = center.y + b * sin(E)
                // At E = 0: x = center.x - a (closest to Sun at center.x - c, Perihelion!)
                // At E = pi: x = center.x + a (farthest from Sun, Aphelion!)
                val planetPos = Offset(
                    ellipseCenter.x - semiMajorA * cos(eccentricAnomalyRad),
                    ellipseCenter.y + semiMinorB * sin(eccentricAnomalyRad)
                )

                // 3. Kepler's 2nd Law: Equal Areas Swept Sector
                if (showAreaSectors) {
                    drawSweptAreaSector(
                        sunPos = sunPos,
                        ellipseCenter = ellipseCenter,
                        currentE = eccentricAnomalyRad,
                        semiMajorA = semiMajorA,
                        semiMinorB = semiMinorB,
                        eccentricity = eccentricity
                    )
                }

                // 4. Radius Vector from Sun to Planet (Dashed Cyan line)
                drawLine(
                    color = CyanNeon.copy(alpha = 0.55f),
                    start = sunPos,
                    end = planetPos,
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )

                // 5. Tangent Orbital Velocity Vector (Emerald Neon)
                val dX = semiMajorA * sin(eccentricAnomalyRad)
                val dY = semiMinorB * cos(eccentricAnomalyRad)
                val tanLen = sqrt(dX * dX + dY * dY).coerceAtLeast(0.01f)
                val vUnit = Offset(dX / tanLen, dY / tanLen)
                val vArrowLen = (32.dp.toPx() * vCurrentNorm).coerceIn(12.dp.toPx(), 75.dp.toPx())
                val vArrowEnd = Offset(
                    planetPos.x + vUnit.x * vArrowLen,
                    planetPos.y + vUnit.y * vArrowLen
                )
                drawLine(
                    color = EmeraldNeon,
                    start = planetPos,
                    end = vArrowEnd,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                val vHead = Path().apply {
                    val perp = Offset(-vUnit.y * 5f, vUnit.x * 5f)
                    moveTo(vArrowEnd.x, vArrowEnd.y)
                    lineTo(vArrowEnd.x - vUnit.x * 9f + perp.x, vArrowEnd.y - vUnit.y * 9f + perp.y)
                    lineTo(vArrowEnd.x - vUnit.x * 9f - perp.x, vArrowEnd.y - vUnit.y * 9f - perp.y)
                    close()
                }
                drawPath(vHead, EmeraldNeon)

                // 6. Gravitational Force Vector (Coral/Red, pointing directly toward Sun)
                val toSunX = sunPos.x - planetPos.x
                val toSunY = sunPos.y - planetPos.y
                val rLen = sqrt(toSunX * toSunX + toSunY * toSunY).coerceAtLeast(0.01f)
                val fgUnit = Offset(toSunX / rLen, toSunY / rLen)
                val fgLen = (24.dp.toPx() / (rCurrentNorm * rCurrentNorm)).coerceIn(8.dp.toPx(), 45.dp.toPx())
                val fgEnd = Offset(planetPos.x + fgUnit.x * fgLen, planetPos.y + fgUnit.y * fgLen)
                drawLine(
                    color = CoralNeon.copy(alpha = 0.85f),
                    start = planetPos,
                    end = fgEnd,
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )

                // 7. Central Gravitational Star (Sun at Focus F1)
                drawSun(sunPos, starGlow)

                // 8. Planet Orb with Atmospheric Glare
                val planetColor = selectedPreset?.color ?: CyanNeon
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, planetColor, BlueLaser),
                        center = Offset(planetPos.x - 3f, planetPos.y - 3f),
                        radius = 12f
                    ),
                    radius = 10f,
                    center = planetPos
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = 13f,
                    center = planetPos,
                    style = Stroke(1.2f)
                )
            }
        },
        hudContent = {
            val rDisplay = "${round(rCurrentNorm * 100) / 100f} AU"
            val vDisplay = "${round(vCurrentNorm * 100) / 100f} v₀"
            val periDisplay = "${round((1f - eccentricity) * 100) / 100f} AU"
            val aphDisplay = "${round((1f + eccentricity) * 100) / 100f} AU"

            TransparentTelemetryHud(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 15: Kepler Planetary Orbits",
                items = listOf(
                    "Orbit Geometry" to if (eccentricity < 0.05f) "Circular (e ≈ 0)" else "Elliptical (e = ${round(eccentricity * 100) / 100f})",
                    "Orbital Radius (r)" to "$rDisplay (Peri: $periDisplay | Aph: $aphDisplay)",
                    "Orbital Velocity (v)" to "$vDisplay (Vis-Viva Law)",
                    "Kepler's 2nd Law" to "dA/dt = L/(2m) = Const",
                    "Angular Momentum" to "L = r × p (Conserved)"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Preset Celestial Bodies Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlanetPreset.entries.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPreset = preset
                                eccentricity = preset.eccentricity
                            },
                            label = {
                                Text(
                                    text = "${preset.icon} ${preset.title}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = preset.color.copy(alpha = 0.25f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ScienceDarkSurface,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. Compact Sliders: Eccentricity & Simulation Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Orbit Eccentricity (e)",
                        value = eccentricity,
                        range = 0.0f..0.88f,
                        valueDisplay = "${round(eccentricity * 100) / 100f}",
                        accentColor = AmberVibrant,
                        onValueChange = {
                            selectedPreset = null
                            eccentricity = it
                        }
                    )

                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Simulation Speed",
                        value = orbitSpeedMultiplier,
                        range = 0.3f..3.0f,
                        valueDisplay = "${round(orbitSpeedMultiplier * 10) / 10f}x",
                        accentColor = CyanNeon,
                        onValueChange = { orbitSpeedMultiplier = it }
                    )
                }

                // 3. Action Buttons & Equal-Area Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = showAreaSectors,
                            onCheckedChange = { showAreaSectors = it },
                            colors = CheckboxDefaults.colors(checkedColor = AmberVibrant)
                        )
                        Text(
                            text = "Equal Area Wedge (Kepler II)",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isRunning = !isRunning },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) AmberVibrant else CyanNeon,
                                contentColor = ScienceDarkBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isRunning) "⏸ Pause" else "▶ Run",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedPreset = PlanetPreset.PLUTO
                                eccentricity = 0.25f
                                orbitSpeedMultiplier = 1.0f
                                eccentricAnomalyRad = 0f
                                isRunning = true
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .background(ScienceDarkSurfaceVariant, RoundedCornerShape(8.dp))
                        ) {
                            ResetIcon(tint = CyanNeon, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    )
}

// ----------------------------------------------------------------------------
// High-Precision Canvas Rendering Pipeline
// ----------------------------------------------------------------------------

private fun DrawScope.drawStarfield(w: Float, h: Float) {
    val grid = 44.dp.toPx()
    var gx = 0f
    while (gx < w) {
        drawLine(ScienceBorder.copy(alpha = 0.12f), Offset(gx, 0f), Offset(gx, h), 0.6f)
        gx += grid
    }
    var gy = 0f
    while (gy < h) {
        drawLine(ScienceBorder.copy(alpha = 0.12f), Offset(0f, gy), Offset(w, gy), 0.6f)
        gy += grid
    }

    val stars = listOf(
        Offset(w * 0.12f, h * 0.18f) to 1.5f,
        Offset(w * 0.28f, h * 0.78f) to 1.2f,
        Offset(w * 0.72f, h * 0.14f) to 1.8f,
        Offset(w * 0.85f, h * 0.62f) to 1.4f,
        Offset(w * 0.64f, h * 0.88f) to 1.6f,
        Offset(w * 0.08f, h * 0.52f) to 1.0f,
        Offset(w * 0.92f, h * 0.32f) to 1.2f,
        Offset(w * 0.45f, h * 0.10f) to 1.5f
    )
    for ((pos, r) in stars) {
        drawCircle(Color.White.copy(alpha = 0.55f), r, pos)
    }
}

private fun DrawScope.drawOrbitalEllipse(
    center: Offset,
    a: Float,
    b: Float,
    sunPos: Offset,
    eccentricity: Float
) {
    // 1. Ellipse Orbital Track (Dashed)
    val path = Path().apply {
        val steps = 80
        for (i in 0..steps) {
            val th = i * (2 * PI.toFloat() / steps)
            val px = center.x - a * cos(th)
            val py = center.y + b * sin(th)
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }

    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.35f),
        style = Stroke(width = 2.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
    )

    // 2. Major Axis Line (connecting Perihelion to Aphelion through Sun and Center)
    drawLine(
        color = ScienceBorder.copy(alpha = 0.4f),
        start = Offset(center.x - a - 12f, center.y),
        end = Offset(center.x + a + 12f, center.y),
        strokeWidth = 1.2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
    )

    // 3. Perihelion (closest) and Aphelion (farthest) Markers
    val perihelionPos = Offset(center.x - a, center.y)
    val aphelionPos = Offset(center.x + a, center.y)

    drawCircle(EmeraldNeon, 3.5f, perihelionPos)
    drawCircle(CoralNeon, 3.5f, aphelionPos)

    // 4. Center of Ellipse Marker
    drawCircle(Color.White.copy(alpha = 0.4f), 2.5f, center)
}

private fun DrawScope.drawSweptAreaSector(
    sunPos: Offset,
    ellipseCenter: Offset,
    currentE: Float,
    semiMajorA: Float,
    semiMinorB: Float,
    eccentricity: Float
) {
    // Sector spans a time-equivalent angle deltaE ~ deltaM / (1 - e cos E)
    val spanE = (0.42f / (1f - eccentricity * cos(currentE)).coerceAtLeast(0.1f)).coerceIn(0.15f, 1.2f)
    val steps = 24

    val sectorPath = Path().apply {
        moveTo(sunPos.x, sunPos.y)
        for (i in 0..steps) {
            val eVal = (currentE - spanE) + (i.toFloat() / steps) * spanE
            val px = ellipseCenter.x - semiMajorA * cos(eVal)
            val py = ellipseCenter.y + semiMinorB * sin(eVal)
            lineTo(px, py)
        }
        close()
    }

    drawPath(
        path = sectorPath,
        color = AmberVibrant.copy(alpha = 0.32f)
    )
}

private fun DrawScope.drawSun(sunPos: Offset, glow: Float) {
    // Corona Glow
    drawCircle(
        color = AmberVibrant.copy(alpha = 0.25f * glow),
        radius = 34f * glow,
        center = sunPos
    )
    drawCircle(
        color = Color(0xFFFFD54F).copy(alpha = 0.45f),
        radius = 22f,
        center = sunPos
    )
    // Core Sun
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFFFFEB3B), Color(0xFFFF6D00)),
            center = sunPos,
            radius = 16f
        ),
        radius = 16f,
        center = sunPos
    )
}

/**
 * Completely transparent HUD displaying live physics metrics
 * so the user stays 100% focused on the experiment with no opaque card blocking the screen.
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

