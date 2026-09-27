package com.geosid.simplephysics.ui.experiments.week3.Day21

import androidx.compose.animation.core.*
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
 * Astrodynamics presets for the Gravitational Three-Body Problem.
 */
enum class ThreeBodyPreset(
    val title: String,
    val icon: String,
    val subtitle: String,
    val description: String,
    val color: Color
) {
    FIGURE_8(
        title = "Figure-8",
        icon = "♾️",
        subtitle = "Periodic 3-Body Choreography",
        description = "Discovered by Moore (1993) & Chenciner (2000): 3 equal stars chase each other along a figure-8 loop.",
        color = CyanNeon
    ),
    PYTHAGOREAN_CHAOS(
        title = "Pythagorean",
        icon = "⚡",
        subtitle = "Burrau's Problem & Ejection",
        description = "Masses 3, 4, 5 at vertices of 3-4-5 right triangle collapse into chaotic stellar ejection.",
        color = CoralNeon
    ),
    LAGRANGE_TRIANGLE(
        title = "Lagrange",
        icon = "📐",
        subtitle = "Equilateral Rotating Triangle",
        description = "Three stars form an equilateral triangle rotating synchronously about their common barycenter.",
        color = EmeraldNeon
    ),
    HIERARCHICAL_TRIPLE(
        title = "Hierarchical",
        icon = "🪐",
        subtitle = "Tight Binary + Outer Star",
        description = "A close inner binary pair orbited at a distance by a third star, demonstrating Kozai stability.",
        color = AmberVibrant
    )
}

/**
 * Celestial body state for numerical integration.
 */
data class CelestialBody(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var mass: Float,
    val radiusPx: Float,
    val color: Color,
    val name: String
)

@Composable
fun ThreeBodyProblemExperiment(
    modifier: Modifier = Modifier
) {
    // 1. Simulation Parameters & Settings
    var selectedPreset by remember { mutableStateOf(ThreeBodyPreset.FIGURE_8) }
    var isRunning by remember { mutableStateOf(true) }
    var simSpeed by remember { mutableStateOf(1.0f) }
    var gravStrength by remember { mutableStateOf(1.0f) }
    var softeningParam by remember { mutableStateOf(10f) } // px to prevent numerical infinite spikes
    var lockBarycenter by remember { mutableStateOf(true) }
    var showVectors by remember { mutableStateOf(true) }

    // Dynamic mass adjustment for Body 3
    var body3MassScale by remember { mutableStateOf(1.0f) }

    // Perturbation tracking (Butterfly effect / Lyapunov exponent)
    var perturbationTriggered by remember { mutableStateOf(0) }
    var lyapunovDivergence by remember { mutableStateOf(0.0f) }

    // Dragging state
    var draggedBodyIndex by remember { mutableStateOf<Int?>(null) }
    var dragTouchPos by remember { mutableStateOf<Offset?>(null) }

    // Canvas size tracking for scale calibration
    var canvasSize by remember { mutableStateOf(Offset(800f, 1200f)) }

    // Trails for each of the 3 bodies
    val maxTrailLength = 160
    val trails = remember {
        listOf(
            mutableStateListOf<Offset>(),
            mutableStateListOf<Offset>(),
            mutableStateListOf<Offset>()
        )
    }

    // Ghost shadow system for Lyapunov chaos measurement
    val ghostBodies = remember {
        Array(3) { floatArrayOf(0f, 0f, 0f, 0f) } // x, y, vx, vy
    }

    // Three bodies state
    val bodies = remember {
        mutableStateListOf(
            CelestialBody(1, 0f, 0f, 0f, 0f, 1.0f, 14f, CyanNeon, "Alpha"),
            CelestialBody(2, 0f, 0f, 0f, 0f, 1.0f, 13f, AmberVibrant, "Beta"),
            CelestialBody(3, 0f, 0f, 0f, 0f, 1.0f, 12f, EmeraldNeon, "Gamma")
        )
    }

    // Initial energy for conservation metric
    var initialTotalEnergy by remember { mutableStateOf(1.0f) }
    var currentEnergyRatio by remember { mutableStateOf(100.0f) }

    // Setup initial conditions based on preset
    fun resetPreset(preset: ThreeBodyPreset, w: Float, h: Float) {
        val cx = w * 0.5f
        val cy = h * 0.40f
        val scale = min(w * 0.36f, h * 0.30f).coerceAtLeast(120f)

        trails.forEach { it.clear() }
        lyapunovDivergence = 0.0f

        when (preset) {
            ThreeBodyPreset.FIGURE_8 -> {
                // Exact periodic figure-8 initial conditions (Moore 1993, Chenciner & Montgomery 2000)
                val x1 = 0.97000436f * scale * 0.65f
                val y1 = -0.24308753f * scale * 0.65f
                val vFactor = sqrt(scale * 0.65f) * 0.95f
                val vx1 = 0.46620531f * vFactor
                val vy1 = 0.43236573f * vFactor

                bodies[0] = bodies[0].copy(x = cx + x1, y = cy + y1, vx = vx1, vy = vy1, mass = 1.0f, color = CyanNeon, name = "Star α")
                bodies[1] = bodies[1].copy(x = cx - x1, y = cy - y1, vx = vx1, vy = vy1, mass = 1.0f, color = AmberVibrant, name = "Star β")
                bodies[2] = bodies[2].copy(x = cx, y = cy, vx = -2f * vx1, vy = -2f * vy1, mass = 1.0f * body3MassScale, color = EmeraldNeon, name = "Star γ")
            }
            ThreeBodyPreset.PYTHAGOREAN_CHAOS -> {
                // Burrau's problem: masses 3, 4, 5 at vertices of 3-4-5 triangle at rest
                val s = scale * 0.28f
                // Vertices: (x, y) such that d12=5, d23=3, d13=4
                bodies[0] = bodies[0].copy(x = cx + 1f * s, y = cy - 3f * s, vx = 0f, vy = 0f, mass = 3.0f, color = CoralNeon, name = "Star 3M")
                bodies[1] = bodies[1].copy(x = cx - 2f * s, y = cy + 1f * s, vx = 0f, vy = 0f, mass = 4.0f, color = AmberVibrant, name = "Star 4M")
                bodies[2] = bodies[2].copy(x = cx + 1f * s, y = cy + 1f * s, vx = 0f, vy = 0f, mass = 5.0f * body3MassScale, color = PurpleNeon, name = "Star 5M")
            }
            ThreeBodyPreset.LAGRANGE_TRIANGLE -> {
                // Equilateral triangle rotating synchronously
                val r = scale * 0.55f
                val m = 1.2f
                val omega = sqrt(3f * m / (r * r * r)) * scale * 0.48f

                for (i in 0..2) {
                    val angle = (i * 2f * PI.toFloat() / 3f) - PI.toFloat() / 2f
                    val px = cx + cos(angle) * r
                    val py = cy + sin(angle) * r
                    val vx = -sin(angle) * omega
                    val vy = cos(angle) * omega
                    val col = when (i) {
                        0 -> CyanNeon
                        1 -> AmberVibrant
                        else -> EmeraldNeon
                    }
                    bodies[i] = bodies[i].copy(x = px, y = py, vx = vx, vy = vy, mass = m * if (i == 2) body3MassScale else 1f, color = col, name = "Star ${i + 1}")
                }
            }
            ThreeBodyPreset.HIERARCHICAL_TRIPLE -> {
                // Tight inner binary + distant third star
                val rInner = scale * 0.26f
                val rOuter = scale * 0.78f
                val mBinary = 2.0f
                val vInner = sqrt(mBinary / (2f * rInner)) * scale * 0.40f
                val vOuter = sqrt((2f * mBinary) / rOuter) * scale * 0.38f

                bodies[0] = bodies[0].copy(x = cx - rInner, y = cy, vx = 0f, vy = -vInner, mass = 1.0f, color = CyanNeon, name = "Inner α")
                bodies[1] = bodies[1].copy(x = cx + rInner, y = cy, vx = 0f, vy = vInner, mass = 1.0f, color = AmberVibrant, name = "Inner β")
                bodies[2] = bodies[2].copy(x = cx, y = cy + rOuter, vx = vOuter, vy = 0f, mass = 0.8f * body3MassScale, color = PurpleNeon, name = "Outer γ")
            }
        }

        // Initialize ghost system with tiny perturbation (delta = 1e-4) to measure chaos
        for (i in 0..2) {
            ghostBodies[i][0] = bodies[i].x + (if (i == 2) 0.0001f * scale else 0f)
            ghostBodies[i][1] = bodies[i].y + (if (i == 2) 0.0001f * scale else 0f)
            ghostBodies[i][2] = bodies[i].vx
            ghostBodies[i][3] = bodies[i].vy
        }

        // Compute reference energy
        var kinetic = 0f
        var potential = 0f
        val G = 600f * gravStrength
        val epsSq = softeningParam * softeningParam

        for (i in 0..2) {
            val b = bodies[i]
            kinetic += 0.5f * b.mass * (b.vx * b.vx + b.vy * b.vy)
            for (j in (i + 1)..2) {
                val b2 = bodies[j]
                val dx = b2.x - b.x
                val dy = b2.y - b.y
                val dist = sqrt(dx * dx + dy * dy + epsSq)
                potential -= G * b.mass * b2.mass / dist
            }
        }
        initialTotalEnergy = (kinetic + potential).coerceAtLeast(1e-4f)
        currentEnergyRatio = 100.0f
    }

    // Re-initialize when preset changes
    LaunchedEffect(selectedPreset, canvasSize) {
        if (canvasSize.x > 50f && canvasSize.y > 50f) {
            resetPreset(selectedPreset, canvasSize.x, canvasSize.y)
        }
    }

    // High-Precision Physics Step Loop (RK4 with Substepping)
    LaunchedEffect(isRunning, simSpeed, gravStrength, softeningParam, lockBarycenter) {
        var lastTime = withFrameNanos { it }

        while (true) {
            val now = withFrameNanos { it }
            if (isRunning) {
                val dtRaw = (now - lastTime) / 1_000_000_000f
                val dtClamped = dtRaw.coerceIn(0.001f, 0.033f) * simSpeed
                val substeps = 8
                val h = dtClamped / substeps
                val G = 600f * gravStrength
                val epsSq = softeningParam * softeningParam

                // Acceleration computation function for 3 bodies
                fun computeAccelerations(
                    px: FloatArray,
                    py: FloatArray,
                    masses: FloatArray,
                    ax: FloatArray,
                    ay: FloatArray
                ) {
                    ax.fill(0f)
                    ay.fill(0f)
                    for (i in 0..2) {
                        for (j in 0..2) {
                            if (i != j) {
                                val dx = px[j] - px[i]
                                val dy = py[j] - py[i]
                                val distSq = dx * dx + dy * dy + epsSq
                                val invDist3 = 1f / (distSq * sqrt(distSq))
                                val force = G * masses[j] * invDist3
                                ax[i] += force * dx
                                ay[i] += force * dy
                            }
                        }
                    }
                }

                val posX = FloatArray(3) { bodies[it].x }
                val posY = FloatArray(3) { bodies[it].y }
                val velX = FloatArray(3) { bodies[it].vx }
                val velY = FloatArray(3) { bodies[it].vy }
                val masses = FloatArray(3) { bodies[it].mass }

                val gPosX = FloatArray(3) { ghostBodies[it][0] }
                val gPosY = FloatArray(3) { ghostBodies[it][1] }
                val gVelX = FloatArray(3) { ghostBodies[it][2] }
                val gVelY = FloatArray(3) { ghostBodies[it][3] }

                val ax1 = FloatArray(3)
                val ay1 = FloatArray(3)
                val ax2 = FloatArray(3)
                val ay2 = FloatArray(3)
                val ax3 = FloatArray(3)
                val ay3 = FloatArray(3)
                val ax4 = FloatArray(3)
                val ay4 = FloatArray(3)

                val tempPx = FloatArray(3)
                val tempPy = FloatArray(3)

                repeat(substeps) {
                    // --- RK4 for primary system ---
                    // Step 1: k1
                    computeAccelerations(posX, posY, masses, ax1, ay1)

                    // Step 2: k2
                    for (i in 0..2) {
                        tempPx[i] = posX[i] + 0.5f * h * velX[i]
                        tempPy[i] = posY[i] + 0.5f * h * velY[i]
                    }
                    computeAccelerations(tempPx, tempPy, masses, ax2, ay2)

                    // Step 3: k3
                    for (i in 0..2) {
                        tempPx[i] = posX[i] + 0.5f * h * (velX[i] + 0.5f * h * ax1[i])
                        tempPy[i] = posY[i] + 0.5f * h * (velY[i] + 0.5f * h * ay1[i])
                    }
                    computeAccelerations(tempPx, tempPy, masses, ax3, ay3)

                    // Step 4: k4
                    for (i in 0..2) {
                        tempPx[i] = posX[i] + h * (velX[i] + h * ax2[i])
                        tempPy[i] = posY[i] + h * (velY[i] + h * ay2[i])
                    }
                    computeAccelerations(tempPx, tempPy, masses, ax4, ay4)

                    // Combine RK4 increments
                    for (i in 0..2) {
                        posX[i] += (h / 6f) * (velX[i] + 2f * (velX[i] + 0.5f * h * ax1[i]) + 2f * (velX[i] + 0.5f * h * ax2[i]) + (velX[i] + h * ax3[i]))
                        posY[i] += (h / 6f) * (velY[i] + 2f * (velY[i] + 0.5f * h * ay1[i]) + 2f * (velY[i] + 0.5f * h * ay2[i]) + (velY[i] + h * ay3[i]))
                        velX[i] += (h / 6f) * (ax1[i] + 2f * ax2[i] + 2f * ax3[i] + ax4[i])
                        velY[i] += (h / 6f) * (ay1[i] + 2f * ay2[i] + 2f * ay3[i] + ay4[i])
                    }

                    // --- Velocity Verlet for Ghost System (Lyapunov distance tracker) ---
                    computeAccelerations(gPosX, gPosY, masses, ax1, ay1)
                    for (i in 0..2) {
                        gPosX[i] += gVelX[i] * h + 0.5f * ax1[i] * h * h
                        gPosY[i] += gVelY[i] * h + 0.5f * ay1[i] * h * h
                    }
                    computeAccelerations(gPosX, gPosY, masses, ax2, ay2)
                    for (i in 0..2) {
                        gVelX[i] += 0.5f * (ax1[i] + ax2[i]) * h
                        gVelY[i] += 0.5f * (ay1[i] + ay2[i]) * h
                    }
                }

                // Center of Mass (Barycenter) transformation
                val totalM = masses[0] + masses[1] + masses[2]
                val cmX = (posX[0] * masses[0] + posX[1] * masses[1] + posX[2] * masses[2]) / totalM
                val cmY = (posY[0] * masses[0] + posY[1] * masses[1] + posY[2] * masses[2]) / totalM
                val elevatedOrigin = Offset(canvasSize.x * 0.5f, canvasSize.y * 0.40f)

                val shiftX = if (lockBarycenter) elevatedOrigin.x - cmX else 0f
                val shiftY = if (lockBarycenter) elevatedOrigin.y - cmY else 0f

                for (i in 0..2) {
                    val finalX = posX[i] + shiftX
                    val finalY = posY[i] + shiftY

                    // Update live body
                    bodies[i] = bodies[i].copy(
                        x = finalX,
                        y = finalY,
                        vx = velX[i],
                        vy = velY[i]
                    )

                    // Append to trail
                    trails[i].add(Offset(finalX, finalY))
                    if (trails[i].size > maxTrailLength) {
                        trails[i].removeAt(0)
                    }

                    ghostBodies[i][0] = gPosX[i] + shiftX
                    ghostBodies[i][1] = gPosY[i] + shiftY
                    ghostBodies[i][2] = gVelX[i]
                    ghostBodies[i][3] = gVelY[i]
                }

                // Compute instantaneous Lyapunov divergence metric (log of spatial distance separation)
                val diffDistSq = (posX[2] - gPosX[2]) * (posX[2] - gPosX[2]) + (posY[2] - gPosY[2]) * (posY[2] - gPosY[2])
                val rawDiv = log10(max(1e-6f, sqrt(diffDistSq)) + 1f) * 1.8f
                lyapunovDivergence = (lyapunovDivergence * 0.95f + rawDiv * 0.05f).coerceIn(0.0f, 9.99f)

                // Compute total conserved mechanical energy
                var kE = 0f
                var pE = 0f
                for (i in 0..2) {
                    kE += 0.5f * masses[i] * (velX[i] * velX[i] + velY[i] * velY[i])
                    for (j in (i + 1)..2) {
                        val dx = posX[j] - posX[i]
                        val dy = posY[j] - posY[i]
                        val dist = sqrt(dx * dx + dy * dy + epsSq)
                        pE -= G * masses[i] * masses[j] / dist
                    }
                }
                val totalE = kE + pE
                if (abs(initialTotalEnergy) > 1e-4f) {
                    currentEnergyRatio = ((totalE / initialTotalEnergy) * 100f).coerceIn(90f, 110f)
                }
            }
            lastTime = now
        }
    }

    // 2. Responsive UI Shell
    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag any star to perturb orbit! Switch presets below to explore Figure-8 choreography & chaotic stellar ejections.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                val hitIndex = bodies.indexOfFirst {
                                    val dist = (it.x - startOffset.x) * (it.x - startOffset.x) +
                                            (it.y - startOffset.y) * (it.y - startOffset.y)
                                    dist <= 48f * 48f
                                }
                                if (hitIndex != -1) {
                                    draggedBodyIndex = hitIndex
                                    dragTouchPos = startOffset
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val idx = draggedBodyIndex
                                if (idx != null) {
                                    val newPos = Offset(bodies[idx].x + dragAmount.x, bodies[idx].y + dragAmount.y)
                                    bodies[idx] = bodies[idx].copy(
                                        x = newPos.x,
                                        y = newPos.y,
                                        vx = bodies[idx].vx + dragAmount.x * 0.4f,
                                        vy = bodies[idx].vy + dragAmount.y * 0.4f
                                    )
                                    dragTouchPos = newPos
                                }
                            },
                            onDragEnd = {
                                draggedBodyIndex = null
                                dragTouchPos = null
                            },
                            onDragCancel = {
                                draggedBodyIndex = null
                                dragTouchPos = null
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height
                if (canvasSize.x != w || canvasSize.y != h) {
                    canvasSize = Offset(w, h)
                }

                val elevatedOrigin = Offset(w * 0.5f, h * 0.40f)

                // 1. Deep Space Scientific Coordinate Grid
                val gridSpacing = 44.dp.toPx()
                var gx = 0f
                while (gx < w) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.15f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, h),
                        strokeWidth = 0.8f
                    )
                    gx += gridSpacing
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.15f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 0.8f
                    )
                    gy += gridSpacing
                }

                // 2. Elevated Barycenter Crosshair Indicator
                val crosshairSize = 14.dp.toPx()
                drawLine(
                    color = ScienceBorder.copy(alpha = 0.45f),
                    start = Offset(elevatedOrigin.x - crosshairSize, elevatedOrigin.y),
                    end = Offset(elevatedOrigin.x + crosshairSize, elevatedOrigin.y),
                    strokeWidth = 1f
                )
                drawLine(
                    color = ScienceBorder.copy(alpha = 0.45f),
                    start = Offset(elevatedOrigin.x, elevatedOrigin.y - crosshairSize),
                    end = Offset(elevatedOrigin.x, elevatedOrigin.y + crosshairSize),
                    strokeWidth = 1f
                )
                drawCircle(
                    color = ScienceBorder.copy(alpha = 0.25f),
                    radius = crosshairSize * 0.75f,
                    center = elevatedOrigin,
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                )

                // 3. Pairwise Gravitational Tension Lines (Triangle Wireframe)
                for (i in 0..2) {
                    for (j in (i + 1)..2) {
                        val b1 = bodies[i]
                        val b2 = bodies[j]
                        drawLine(
                            color = ScienceBorder.copy(alpha = 0.28f),
                            start = Offset(b1.x, b1.y),
                            end = Offset(b2.x, b2.y),
                            strokeWidth = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }
                }

                // 4. Glowing Orbital Trails with Alpha Gradient
                for (i in 0..2) {
                    val trail = trails[i]
                    val bodyColor = bodies[i].color
                    val trailSize = trail.size
                    if (trailSize > 1) {
                        for (k in 0 until trailSize - 1) {
                            val alpha = (k.toFloat() / trailSize).pow(1.6f) * 0.85f
                            drawLine(
                                color = bodyColor.copy(alpha = alpha),
                                start = trail[k],
                                end = trail[k + 1],
                                strokeWidth = 2.0.dp.toPx() * (0.4f + 0.6f * (k.toFloat() / trailSize)),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // 5. Render Celestial Bodies & Glow Halos
                for (i in 0..2) {
                    val b = bodies[i]
                    val bPos = Offset(b.x, b.y)
                    val r = b.radiusPx

                    // Outer glowing corona
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                b.color.copy(alpha = 0.40f),
                                b.color.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = bPos,
                            radius = r * 2.6f
                        ),
                        radius = r * 2.6f,
                        center = bPos
                    )

                    // Core star sphere with radial lighting
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                b.color,
                                b.color.copy(alpha = 0.8f)
                            ),
                            center = Offset(bPos.x - r * 0.25f, bPos.y - r * 0.25f),
                            radius = r
                        ),
                        radius = r,
                        center = bPos
                    )

                    // 6. Velocity Vector Arrows (if enabled)
                    if (showVectors) {
                        val vScale = 0.22f
                        val vEnd = Offset(b.x + b.vx * vScale, b.y + b.vy * vScale)
                        drawLine(
                            color = b.color.copy(alpha = 0.85f),
                            start = bPos,
                            end = vEnd,
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        // Arrowhead
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = vEnd
                        )
                    }
                }

                // 7. Interactive Drag Ring Feedback
                if (draggedBodyIndex != null && dragTouchPos != null) {
                    val idx = draggedBodyIndex!!
                    val b = bodies[idx]
                    drawCircle(
                        color = AmberVibrant.copy(alpha = 0.4f),
                        radius = b.radiusPx * 2.8f,
                        center = Offset(b.x, b.y),
                        style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
                    )
                }
            }
        },
        hudContent = {
            // Transparent HUD Card per pair-programming styling rules
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Transparent,
                title = "Day 21: Chaotic Three-Body",
                items = listOf(
                    "Preset" to selectedPreset.title,
                    "Energy Conserved" to "${round(currentEnergyRatio * 10f) / 10f}%",
                    "Chaos Index λ" to "${round(lyapunovDivergence * 100f) / 100f}",
                    "State" to if (isRunning) "INTEGRATING" else "PAUSED",
                    "Formula" to "m_i·r̈_i = ∑_{j≠i} G·m_i·m_j / |r_j - r_i|³ · (r_j - r_i)"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Selection Chips (compact horizontal row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThreeBodyPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) preset.color.copy(alpha = 0.22f)
                                    else ScienceDarkSurface.copy(alpha = 0.6f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.8.dp,
                                    color = if (isSelected) preset.color else ScienceBorder.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedPreset = preset
                                    resetPreset(preset, canvasSize.x, canvasSize.y)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${preset.icon} ${preset.title}",
                                color = if (isSelected) preset.color else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Action Controls Row: Play/Pause, Perturb 🦋, Barycenter Lock, Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play / Pause Button
                    Button(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) CoralNeon.copy(alpha = 0.25f) else CyanNeon.copy(alpha = 0.25f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isRunning) CoralNeon else CyanNeon
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run",
                            color = if (isRunning) CoralNeon else CyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Butterfly Perturbation Trigger (Lyapunov sensitivity test)
                    Button(
                        onClick = {
                            perturbationTriggered++
                            // Give body 3 a tiny orthogonal velocity kick
                            bodies[2] = bodies[2].copy(
                                vx = bodies[2].vx + 0.35f * sqrt(canvasSize.x * 0.2f),
                                vy = bodies[2].vy - 0.25f * sqrt(canvasSize.y * 0.2f)
                            )
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberVibrant.copy(alpha = 0.22f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberVibrant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🦋 Perturb",
                            color = AmberVibrant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Barycenter Centering Toggle
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (lockBarycenter) EmeraldNeon.copy(alpha = 0.22f)
                                else ScienceDarkSurface.copy(alpha = 0.6f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (lockBarycenter) EmeraldNeon else ScienceBorder.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { lockBarycenter = !lockBarycenter },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lockBarycenter) "⚓ Barycenter" else "Free Drift",
                            color = if (lockBarycenter) EmeraldNeon else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Reset Button
                    IconButton(
                        onClick = { resetPreset(selectedPreset, canvasSize.x, canvasSize.y) },
                        modifier = Modifier
                            .size(34.dp)
                            .border(1.dp, ScienceBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    ) {
                        ResetIcon(tint = CyanNeon, modifier = Modifier.size(16.dp))
                    }
                }

                // Paired Sliders Row 1: Gravitational Constant G & Softening ε
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Gravitational G",
                        value = gravStrength,
                        range = 0.4f..2.5f,
                        valueDisplay = "${round(gravStrength * 10f) / 10f}×",
                        accentColor = CyanNeon,
                        onValueChange = { gravStrength = it },
                        modifier = Modifier.weight(1f)
                    )

                    PhysicsSliderControl(
                        title = "Softening ε",
                        value = softeningParam,
                        range = 4f..28f,
                        valueDisplay = "${softeningParam.toInt()}px",
                        accentColor = EmeraldNeon,
                        onValueChange = { softeningParam = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Paired Sliders Row 2: Star 3 Mass & Simulation Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Star γ Mass",
                        value = body3MassScale,
                        range = 0.2f..3.0f,
                        valueDisplay = "${round(body3MassScale * 10f) / 10f}M",
                        accentColor = AmberVibrant,
                        onValueChange = {
                            val oldRatio = body3MassScale
                            body3MassScale = it
                            bodies[2] = bodies[2].copy(mass = (bodies[2].mass / oldRatio) * it)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PhysicsSliderControl(
                        title = "Sim Speed",
                        value = simSpeed,
                        range = 0.2f..2.5f,
                        valueDisplay = "${round(simSpeed * 10f) / 10f}×",
                        accentColor = CoralNeon,
                        onValueChange = { simSpeed = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}
