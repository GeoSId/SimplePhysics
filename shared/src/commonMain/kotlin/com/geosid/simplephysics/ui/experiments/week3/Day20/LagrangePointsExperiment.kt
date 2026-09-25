package com.geosid.simplephysics.ui.experiments.week3.Day20

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

/**
 * Astrodynamics presets for the Restricted Three-Body Problem.
 */
enum class LagrangePreset(
    val title: String,
    val icon: String,
    val mu: Float,
    val targetPoint: String,
    val description: String,
    val color: Color
) {
    SUN_EARTH(
        title = "Sun-Earth JWST",
        icon = "🔭",
        mu = 0.04f,
        targetPoint = "L2",
        description = "James Webb Space Telescope in halo orbit at L2 (1.5M km from Earth)",
        color = AmberVibrant
    ),
    SUN_JUPITER(
        title = "Jupiter Trojans",
        icon = "🪐",
        mu = 0.08f,
        targetPoint = "L4",
        description = "Thousands of Trojan asteroids trapped in stable tadpole orbits at L4/L5",
        color = CyanNeon
    ),
    EARTH_MOON(
        title = "Earth-Moon L1",
        icon = "🌕",
        mu = 0.14f,
        targetPoint = "L1",
        description = "Lunar Gateway space station parked between Earth and Moon",
        color = EmeraldNeon
    ),
    CHAOTIC_BINARY(
        title = "Heavy Binary",
        icon = "⚡",
        mu = 0.42f,
        targetPoint = "L4",
        description = "Mass ratio exceeds Routh limit (0.0385): L4/L5 become chaotically unstable",
        color = CoralNeon
    )
}

/**
 * Lagrange point definition and coordinates in rotating synodic frame.
 */
data class LagrangePoint(
    val name: String,
    val pos: Offset,
    val color: Color,
    val isStable: Boolean,
    val description: String
)

data class ProbeTrailPoint(
    val pos: Offset,
    val speed: Float,
    val alpha: Float
)

@Composable
fun LagrangePointsExperiment(
    modifier: Modifier = Modifier
) {
    // 1. Physical Simulation Parameters
    var selectedPreset by remember { mutableStateOf<LagrangePreset?>(LagrangePreset.SUN_EARTH) }
    var muParam by remember { mutableStateOf(0.04f) } // Mass ratio M2 / (M1 + M2)
    var isRunning by remember { mutableStateOf(true) }
    var simSpeed by remember { mutableStateOf(1.0f) }
    var showEquipotential by remember { mutableStateOf(true) }
    var showInertialFrame by remember { mutableStateOf(false) }

    // Routh critical mass ratio for triangular stability
    val routhCriticalMu = 0.03852f
    val isL4L5Stable = muParam <= routhCriticalMu

    // Compute exact positions of L1..L5 using Newton-Raphson
    val lagrangePoints = remember(muParam) {
        val mu = muParam.toDouble()
        val m1 = 1.0 - mu
        val m2 = mu

        // Helper function for collinear points: f(x) = x - m1*(x+mu)/|x+mu|^3 - m2*(x - (1-mu))/|x - (1-mu)|^3
        fun fCollinear(x: Double): Double {
            val r1 = abs(x + mu)
            val r2 = abs(x - (1.0 - mu))
            val term1 = if (r1 > 1e-6) m1 * (x + mu) / (r1 * r1 * r1) else 0.0
            val term2 = if (r2 > 1e-6) m2 * (x - (1.0 - mu)) / (r2 * r2 * r2) else 0.0
            return x - term1 - term2
        }

        fun dfCollinear(x: Double): Double {
            val h = 1e-5
            return (fCollinear(x + h) - fCollinear(x - h)) / (2.0 * h)
        }

        fun solveCollinear(initialGuess: Double): Double {
            var x = initialGuess
            for (i in 0 until 12) {
                val f = fCollinear(x)
                val df = dfCollinear(x)
                if (abs(df) < 1e-9) break
                val dx = f / df
                x -= dx
                if (abs(dx) < 1e-7) break
            }
            return x
        }

        // Hill radius estimate: r_H = (mu / 3)^(1/3)
        val rH = (mu / 3.0).pow(1.0 / 3.0)
        val xL1 = solveCollinear(1.0 - mu - rH * 0.95).toFloat()
        val xL2 = solveCollinear(1.0 - mu + rH * 1.05).toFloat()
        val xL3 = solveCollinear(-1.0 - 0.4 * mu).toFloat()

        val xL45 = (0.5 - mu).toFloat()
        val yL45 = (sqrt(3.0) / 2.0).toFloat()

        listOf(
            LagrangePoint("L1", Offset(xL1, 0f), CoralNeon, false, "Sub-secondary saddle point (SOHO, DSCOVR)"),
            LagrangePoint("L2", Offset(xL2, 0f), AmberVibrant, false, "Anti-secondary saddle point (JWST, Gaia)"),
            LagrangePoint("L3", Offset(xL3, 0f), PurpleNeon, false, "Counter-primary point (behind Sun)"),
            LagrangePoint("L4", Offset(xL45, yL45), if (isL4L5Stable) CyanNeon else CoralNeon, isL4L5Stable, "Leading triangular point (+60° Trojan)"),
            LagrangePoint("L5", Offset(xL45, -yL45), if (isL4L5Stable) EmeraldNeon else CoralNeon, isL4L5Stable, "Trailing triangular point (-60° Trojan)")
        )
    }

    // 2. Dynamic Probe State in Synodic Frame
    // Initialize near L2 for JWST halo orbit
    var probePos by remember { mutableStateOf(Offset(0f, 0f)) }
    var probeVel by remember { mutableStateOf(Offset(0f, 0f)) }
    var inertialRotationAngle by remember { mutableStateOf(0f) }

    val trail = remember { mutableStateListOf<ProbeTrailPoint>() }

    // Spawn probe at target Lagrange point or preset
    val launchAtLagrange = { targetName: String ->
        val lp = lagrangePoints.find { it.name == targetName } ?: lagrangePoints[1]
        trail.clear()
        when (targetName) {
            "L2" -> {
                // JWST halo orbit around L2
                probePos = Offset(lp.pos.x + 0.02f, lp.pos.y + 0.04f)
                probeVel = Offset(0.04f, -0.03f)
            }
            "L4" -> {
                // Trojan tadpole orbit around L4
                probePos = Offset(lp.pos.x + 0.05f, lp.pos.y - 0.03f)
                probeVel = Offset(-0.04f, 0.05f)
            }
            "L1" -> {
                // SOHO halo orbit around L1
                probePos = Offset(lp.pos.x - 0.02f, lp.pos.y + 0.03f)
                probeVel = Offset(0.03f, 0.04f)
            }
            else -> {
                probePos = Offset(lp.pos.x + 0.03f, lp.pos.y)
                probeVel = Offset(0f, 0.03f)
            }
        }
    }

    // Initialize with preset
    LaunchedEffect(Unit) {
        launchAtLagrange("L2")
    }

    val applyPreset = { preset: LagrangePreset ->
        selectedPreset = preset
        muParam = preset.mu
        launchAtLagrange(preset.targetPoint)
    }

    // Physical acceleration function in rotating synodic frame
    fun computeSynodicAcceleration(pos: Offset, vel: Offset, mu: Float): Offset {
        val x = pos.x
        val y = pos.y
        val vx = vel.x
        val vy = vel.y

        val m1 = 1f - mu
        val m2 = mu

        // Primary (M1) at (-mu, 0), Secondary (M2) at (1 - mu, 0)
        val dx1 = x + mu
        val dy1 = y
        val r1Sq = dx1 * dx1 + dy1 * dy1 + 1e-4f
        val r1 = sqrt(r1Sq)
        val r1Cube = r1 * r1Sq

        val dx2 = x - (1f - mu)
        val dy2 = y
        val r2Sq = dx2 * dx2 + dy2 * dy2 + 1e-4f
        val r2 = sqrt(r2Sq)
        val r2Cube = r2 * r2Sq

        // Gravitational forces
        val fgx = -m1 * dx1 / r1Cube - m2 * dx2 / r2Cube
        val fgy = -m1 * dy1 / r1Cube - m2 * dy2 / r2Cube

        // Centrifugal force (omega = 1): F_cent = (x, y)
        // Coriolis acceleration: a_cor = (2*vy, -2*vx)
        val ax = fgx + x + 2f * vy
        val ay = fgy + y - 2f * vx

        return Offset(ax, ay)
    }

    // 3. High-Precision Symplectic / Runge-Kutta 4 Numerical Physics Loop
    LaunchedEffect(isRunning, muParam, simSpeed) {
        var lastTime = withFrameNanos { it }

        while (true) {
            val now = withFrameNanos { it }
            val rawDt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
            lastTime = now

            if (isRunning) {
                val totalDt = rawDt * simSpeed
                // Sub-stepping for orbital precision near massive primaries
                val subSteps = 8
                val dt = totalDt / subSteps

                for (step in 0 until subSteps) {
                    // RK4 integration for rotating frame
                    val p0 = probePos
                    val v0 = probeVel

                    val a0 = computeSynodicAcceleration(p0, v0, muParam)

                    val p1 = Offset(p0.x + 0.5f * v0.x * dt, p0.y + 0.5f * v0.y * dt)
                    val v1 = Offset(v0.x + 0.5f * a0.x * dt, v0.y + 0.5f * a0.y * dt)
                    val a1 = computeSynodicAcceleration(p1, v1, muParam)

                    val p2 = Offset(p0.x + 0.5f * v1.x * dt, p0.y + 0.5f * v1.y * dt)
                    val v2 = Offset(v0.x + 0.5f * a1.x * dt, v0.y + 0.5f * a1.y * dt)
                    val a2 = computeSynodicAcceleration(p2, v2, muParam)

                    val p3 = Offset(p0.x + v2.x * dt, p0.y + v2.y * dt)
                    val v3 = Offset(v0.x + a2.x * dt, v0.y + a2.y * dt)
                    val a3 = computeSynodicAcceleration(p3, v3, muParam)

                    val nextPos = Offset(
                        p0.x + (dt / 6f) * (v0.x + 2f * v1.x + 2f * v2.x + v3.x),
                        p0.y + (dt / 6f) * (v0.y + 2f * v1.y + 2f * v2.y + v3.y)
                    )
                    val nextVel = Offset(
                        v0.x + (dt / 6f) * (a0.x + 2f * a1.x + 2f * a2.x + a3.x),
                        v0.y + (dt / 6f) * (a0.y + 2f * a1.y + 2f * a2.y + a3.y)
                    )

                    probePos = nextPos
                    probeVel = nextVel
                }

                // Advance sidereal inertial frame angle
                inertialRotationAngle = (inertialRotationAngle + totalDt * 1.0f) % (2f * PI.toFloat())

                // Record trail point
                val currentSpeed = probeVel.getDistance()
                trail.add(ProbeTrailPoint(probePos, currentSpeed, 1f))
                if (trail.size > 280) {
                    trail.removeAt(0)
                }
            }
        }
    }

    // Live Metrics: Jacobi Constant C_J = 2*Phi_eff - v^2
    val jacobiConstant = remember(probePos, probeVel, muParam) {
        val x = probePos.x
        val y = probePos.y
        val m1 = 1f - muParam
        val m2 = muParam
        val r1 = sqrt((x + muParam) * (x + muParam) + y * y).coerceAtLeast(0.02f)
        val r2 = sqrt((x - (1f - muParam)) * (x - (1f - muParam)) + y * y).coerceAtLeast(0.02f)
        val vSq = probeVel.x * probeVel.x + probeVel.y * probeVel.y
        val phiEff = (m1 / r1) + (m2 / r2) + 0.5f * (x * x + y * y)
        val cJ = 2f * phiEff - vSq
        round(cJ * 100f) / 100f
    }

    // Distance to closest Lagrange point
    val nearestLPoint = remember(probePos, lagrangePoints) {
        lagrangePoints.minByOrNull { (it.pos - probePos).getDistance() } ?: lagrangePoints[0]
    }
    val distToNearestL = round((probePos - nearestLPoint.pos).getDistance() * 1000f) / 1000f

    val infiniteTransition = rememberInfiniteTransition()
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag or tap to reposition the spacecraft probe. Observe Coriolis-guided halo and tadpole orbits!",
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 20: Lagrange Equilibrium Points (L1-L5)",
                items = listOf(
                    "Formula" to "\\nabla \\Phi_{eff}(\\mathbf{r}) = \\mathbf{0}, \\quad C_J = 2\\Phi_{eff} - v^2",
                    "Mass Ratio (μ)" to "${round(muParam * 1000f) / 1000f} (${if (isL4L5Stable) "L4/L5 STABLE" else "CHAOTIC"})",
                    "Nearest L-Point" to "${nearestLPoint.name} (Δr = $distToNearestL AU)",
                    "Jacobi Constant (C_J)" to "$jacobiConstant",
                    "Frame" to if (showInertialFrame) "INERTIAL (SIDEREAL)" else "SYNODIC (CO-ROTATING)"
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
                            val w = size.width
                            val h = size.height
                            val scale = min(w * 0.36f, h * 0.32f)
                            val dX = dragAmount.x / scale
                            val dY = dragAmount.y / scale
                            probePos = Offset(probePos.x + dX, probePos.y + dY)
                            probeVel = Offset(0f, 0f)
                            trail.clear()
                            selectedPreset = null
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val w = size.width
                            val h = size.height
                            val barycenter = Offset(w * 0.50f, h * 0.40f)
                            val scale = min(w * 0.36f, h * 0.32f)
                            val normX = (offset.x - barycenter.x) / scale
                            val normY = (offset.y - barycenter.y) / scale
                            probePos = Offset(normX, normY)
                            probeVel = Offset(0f, 0.02f)
                            trail.clear()
                            selectedPreset = null
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated center: barycenter at (w * 0.50f, h * 0.40f)
                val barycenter = Offset(w * 0.50f, h * 0.40f)
                val scale = min(w * 0.36f, h * 0.32f)

                // Background starfield & grid
                drawStarfieldBackground(w, h)

                // Rotation transform if viewing in sidereal inertial frame
                val rotAngle = if (showInertialFrame) inertialRotationAngle else 0f
                fun toScreen(synodicPt: Offset): Offset {
                    val x = synodicPt.x
                    val y = synodicPt.y
                    val cosA = cos(rotAngle)
                    val sinA = sin(rotAngle)
                    val rx = x * cosA - y * sinA
                    val ry = x * sinA + y * cosA
                    return Offset(barycenter.x + rx * scale, barycenter.y + ry * scale)
                }

                // 1. Draw Equipotential Contour Lines (Roche Lobes & Hill Spheres)
                if (showEquipotential) {
                    drawEquipotentialContours(barycenter, scale, muParam, rotAngle)
                }

                // 2. Primary Celestial Body M1 (Sun)
                val p1Pos = toScreen(Offset(-muParam, 0f))
                val r1Px = 14.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF59D), AmberVibrant, Color(0xFFE65100)),
                        center = p1Pos,
                        radius = r1Px * 1.5f
                    ),
                    radius = r1Px,
                    center = p1Pos
                )
                drawCircle(
                    color = AmberVibrant.copy(alpha = 0.25f * glowPulse),
                    radius = r1Px * 2.2f,
                    center = p1Pos
                )

                // 3. Secondary Celestial Body M2 (Earth/Moon/Jupiter)
                val p2Pos = toScreen(Offset(1f - muParam, 0f))
                val r2Px = (7.dp.toPx() * (1f + muParam * 1.2f)).coerceAtLeast(6.dp.toPx())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF80D8FF), CyanNeon, BlueLaser),
                        center = p2Pos,
                        radius = r2Px * 1.4f
                    ),
                    radius = r2Px,
                    center = p2Pos
                )
                // Secondary orbital path circle
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.20f),
                    radius = (1f - muParam) * scale,
                    center = barycenter,
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                )

                // 4. Draw Equilateral Triangle Lines connecting M1, M2, L4, L5
                val l4Screen = toScreen(lagrangePoints[3].pos)
                val l5Screen = toScreen(lagrangePoints[4].pos)
                val dashedLine = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                drawLine(ScienceBorder.copy(alpha = 0.5f), p1Pos, l4Screen, 1f, pathEffect = dashedLine)
                drawLine(ScienceBorder.copy(alpha = 0.5f), p2Pos, l4Screen, 1f, pathEffect = dashedLine)
                drawLine(ScienceBorder.copy(alpha = 0.5f), p1Pos, l5Screen, 1f, pathEffect = dashedLine)
                drawLine(ScienceBorder.copy(alpha = 0.5f), p2Pos, l5Screen, 1f, pathEffect = dashedLine)

                // 5. Render All 5 Lagrange Points
                lagrangePoints.forEach { lp ->
                    val lpScreen = toScreen(lp.pos)
                    val isNearest = lp.name == nearestLPoint.name

                    // Halo glow
                    drawCircle(
                        color = lp.color.copy(alpha = if (isNearest) 0.35f * glowPulse else 0.15f),
                        radius = (if (isNearest) 14.dp else 9.dp).toPx(),
                        center = lpScreen
                    )
                    // Core point
                    drawCircle(
                        color = lp.color,
                        radius = (if (isNearest) 4.5.dp else 3.dp).toPx(),
                        center = lpScreen
                    )
                    // Dashed target ring for active point
                    if (isNearest) {
                        drawCircle(
                            color = lp.color.copy(alpha = 0.8f),
                            radius = 16.dp.toPx(),
                            center = lpScreen,
                            style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f)))
                        )
                    }
                }

                // 6. Spacecraft Probe Trail
                if (trail.size > 1) {
                    val trailPath = Path()
                    trail.forEachIndexed { idx, pt ->
                        val screenPt = toScreen(pt.pos)
                        if (idx == 0) trailPath.moveTo(screenPt.x, screenPt.y)
                        else trailPath.lineTo(screenPt.x, screenPt.y)
                    }
                    drawPath(
                        path = trailPath,
                        brush = Brush.linearGradient(
                            colors = listOf(CyanNeon.copy(alpha = 0.1f), AmberVibrant.copy(alpha = 0.8f), Color.White),
                            start = toScreen(trail.first().pos),
                            end = toScreen(trail.last().pos)
                        ),
                        style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                    )
                }

                // 7. Active Spacecraft Probe (Satellite / JWST)
                val probeScreen = toScreen(probePos)
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = probeScreen
                )
                drawCircle(
                    color = AmberVibrant.copy(alpha = 0.5f * glowPulse),
                    radius = 9.dp.toPx(),
                    center = probeScreen
                )

                // Velocity vector arrow
                val velMag = probeVel.getDistance()
                if (velMag > 0.001f) {
                    val vArrowEnd = toScreen(Offset(probePos.x + probeVel.x * 0.8f, probePos.y + probeVel.y * 0.8f))
                    drawLine(
                        color = EmeraldNeon,
                        start = probeScreen,
                        end = vArrowEnd,
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
            }
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Scenario Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LagrangePreset.values().forEach { preset ->
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
                                Text(text = preset.icon, fontSize = 13.sp)
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

                // Quick Lagrange Point Launch Chips (L1 - L5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("L1", "L2", "L3", "L4", "L5").forEach { lName ->
                        val isTarget = nearestLPoint.name == lName
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    launchAtLagrange(lName)
                                },
                            color = if (isTarget) CyanNeon.copy(alpha = 0.22f) else ScienceDarkSurfaceVariant,
                            border = if (isTarget) androidx.compose.foundation.BorderStroke(1.dp, CyanNeon) else null
                        ) {
                            Text(
                                text = "🚀 $lName",
                                color = if (isTarget) CyanNeon else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Paired Sliders: Mass Ratio μ & Simulation Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Mass Ratio (μ = M2 / M_tot)",
                            value = muParam,
                            range = 0.01f..0.48f,
                            valueDisplay = "${round(muParam * 1000f) / 1000f}",
                            accentColor = if (isL4L5Stable) CyanNeon else CoralNeon,
                            onValueChange = {
                                muParam = it
                                selectedPreset = null
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Simulation Time Rate",
                            value = simSpeed,
                            range = 0.2f..3.0f,
                            valueDisplay = "${round(simSpeed * 10f) / 10f}x",
                            accentColor = AmberVibrant,
                            onValueChange = { simSpeed = it }
                        )
                    }
                }

                // Action Buttons: Run/Pause, Frame Toggle, Reset
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
                            .weight(1.1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run Sim",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showInertialFrame = !showInertialFrame },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScienceDarkSurfaceVariant,
                            contentColor = if (showInertialFrame) AmberVibrant else CyanNeon
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (showInertialFrame) "🔄 Rotating" else "🌐 Sidereal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            applyPreset(LagrangePreset.SUN_EARTH)
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

private fun DrawScope.drawStarfieldBackground(w: Float, h: Float) {
    val gridStep = 45.dp.toPx()
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
        Offset(w * 0.10f, h * 0.12f), Offset(w * 0.22f, h * 0.08f),
        Offset(w * 0.85f, h * 0.10f), Offset(w * 0.90f, h * 0.25f),
        Offset(w * 0.15f, h * 0.65f), Offset(w * 0.30f, h * 0.72f),
        Offset(w * 0.70f, h * 0.68f), Offset(w * 0.88f, h * 0.58f),
        Offset(w * 0.52f, h * 0.05f), Offset(w * 0.06f, h * 0.40f)
    )
    stars.forEachIndexed { i, pt ->
        val radius = if (i % 3 == 0) 1.5f else 1.0f
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = radius,
            center = pt
        )
    }
}

/**
 * Draws concentric equipotential contour loops representing the Roche lobes and Hill spheres.
 */
private fun DrawScope.drawEquipotentialContours(
    center: Offset,
    scale: Float,
    mu: Float,
    rotAngle: Float
) {
    val cosA = cos(rotAngle)
    val sinA = sin(rotAngle)

    // Concentric contours around primary and secondary
    val m1Pos = Offset(-mu, 0f)
    val m2Pos = Offset(1f - mu, 0f)

    fun toScreen(pt: Offset): Offset {
        val rx = pt.x * cosA - pt.y * sinA
        val ry = pt.x * sinA + pt.y * cosA
        return Offset(center.x + rx * scale, center.y + ry * scale)
    }

    // Inner Roche lobe loops around M1 and M2
    val hillRadius = ((mu / 3f).toDouble().pow(1.0 / 3.0)).toFloat()
    val m2Screen = toScreen(m2Pos)
    val m1Screen = toScreen(m1Pos)

    // Hill Sphere of M2
    drawCircle(
        color = CyanNeon.copy(alpha = 0.18f),
        radius = hillRadius * scale,
        center = m2Screen,
        style = Stroke(width = 1.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f)))
    )

    // Equipotential contour circles around barycenter
    listOf(0.75f, 1.0f, 1.25f).forEach { rNorm ->
        drawCircle(
            color = ScienceBorder.copy(alpha = 0.22f),
            radius = rNorm * scale,
            center = center,
            style = Stroke(width = 0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
        )
    }
}
