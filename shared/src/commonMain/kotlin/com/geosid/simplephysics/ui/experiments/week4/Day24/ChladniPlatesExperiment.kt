package com.geosid.simplephysics.ui.experiments.week4.Day24

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

// Sand particle on normalized plate [-1, 1] x [-1, 1]
class ChladniSandParticle(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f
)

@Composable
fun ChladniPlatesExperiment(
    modifier: Modifier = Modifier
) {
    // Mode parameters
    var modeM by remember { mutableStateOf(1f) }
    var modeN by remember { mutableStateOf(3f) }
    var alphaCoupling by remember { mutableStateOf(1.0f) } // Chladni linear combination weight
    var driveAmplitude by remember { mutableStateOf(0.70f) }
    var isRunning by remember { mutableStateOf(true) }

    // Particle pool of sand grains
    val particleCount = 800
    val sandParticles = remember {
        List(particleCount) {
            ChladniSandParticle(
                x = Random.nextFloat() * 1.8f - 0.9f,
                y = Random.nextFloat() * 1.8f - 0.9f
            )
        }
    }

    // Function to re-sprinkle sand uniformly
    val sprinkleSand = {
        for (p in sandParticles) {
            p.x = Random.nextFloat() * 1.8f - 0.9f
            p.y = Random.nextFloat() * 1.8f - 0.9f
            p.vx = (Random.nextFloat() - 0.5f) * 0.01f
            p.vy = (Random.nextFloat() - 0.5f) * 0.01f
        }
    }

    // Interactive finger disturbance point
    var touchPos by remember { mutableStateOf<Offset?>(null) }

    // 60 FPS physics tick
    var frameTick by remember { mutableStateOf(0L) }
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        var lastTime = withFrameNanos { it }
        while (isRunning) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
                lastTime = now

                val m = round(modeM)
                val n = round(modeN)
                val alpha = alphaCoupling
                val amp = driveAmplitude

                val pi = PI.toFloat()
                val mPi = m * pi * 0.5f
                val nPi = n * pi * 0.5f

                // Physics update on particles
                for (p in sandParticles) {
                    val x = p.x
                    val y = p.y

                    // w(x,y) = cos(m*pi*x/2)*cos(n*pi*y/2) - alpha*cos(n*pi*x/2)*cos(m*pi*y/2)
                    val cosMx = cos(mPi * x)
                    val sinMx = sin(mPi * x)
                    val cosNy = cos(nPi * y)
                    val sinNy = sin(nPi * y)

                    val cosNx = cos(nPi * x)
                    val sinNx = sin(nPi * x)
                    val cosMy = cos(mPi * y)
                    val sinMy = sin(mPi * y)

                    val term1 = cosMx * cosNy
                    val term2 = cosNx * cosMy
                    val w = term1 - alpha * term2

                    // Derivatives dw/dx and dw/dy
                    val dwDx = -mPi * sinMx * cosNy + alpha * nPi * sinNx * cosMy
                    val dwDy = -nPi * cosMx * sinNy + alpha * mPi * cosNx * sinMy

                    // Gradient of w^2: grad(w^2) = 2 * w * grad(w)
                    val gradX = 2f * w * dwDx
                    val gradY = 2f * w * dwDy

                    // Acoustic radiation drift away from antinodes toward nodal lines:
                    // F_drift = -beta * grad(w^2)
                    val kick = amp * 0.0035f
                    val absW = abs(w)

                    // Sand bounces when plate acceleration exceeds threshold
                    if (absW > 0.04f) {
                        val jitter = (Random.nextFloat() - 0.5f) * 0.004f * absW * amp
                        p.vx += -gradX * kick + jitter
                        p.vy += -gradY * kick + jitter
                    }

                    // Damping (friction on plate)
                    p.vx *= 0.88f
                    p.vy *= 0.88f

                    // Integrate position
                    p.x += p.vx
                    p.y += p.vy

                    // Plate boundaries [-0.95, 0.95]
                    if (p.x < -0.95f) { p.x = -0.95f; p.vx = -p.vx * 0.5f }
                    if (p.x > 0.95f) { p.x = 0.95f; p.vx = -p.vx * 0.5f }
                    if (p.y < -0.95f) { p.y = -0.95f; p.vy = -p.vy * 0.5f }
                    if (p.y > 0.95f) { p.y = 0.95f; p.vy = -p.vy * 0.5f }
                }

                frameTick++
            }
        }
    }

    // Scientific metrics
    val curM = round(modeM).toInt()
    val curN = round(modeN).toInt()
    val resonantFreq = 54 * (curM * curM + curN * curN) // Chladni empirical frequency
    val clusteredCount = sandParticles.count {
        val mPi = curM * PI.toFloat() * 0.5f
        val nPi = curN * PI.toFloat() * 0.5f
        val w = cos(mPi * it.x) * cos(nPi * it.y) - alphaCoupling * cos(nPi * it.x) * cos(mPi * it.y)
        abs(w) < 0.12f
    }
    val clusterPercent = (clusteredCount * 100) / particleCount

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Tap or drag on plate to perturb sand. Change modal numbers (m, n) to reveal new cymatic geometries!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                touchPos = offset
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                touchPos = change.position
                            },
                            onDragEnd = {
                                touchPos = null
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            touchPos = offset
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated plate center & geometry (leaving bottom 35-40% clear)
                val cx = w * 0.50f
                val cy = h * 0.38f
                val plateSize = min(w * 0.74f, h * 0.44f)
                val halfPlate = plateSize * 0.50f
                val plateLeft = cx - halfPlate
                val plateTop = cy - halfPlate

                // 1. Subtle Coordinate Grid
                val gridSpacing = 40.dp.toPx()
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

                // 2. Chladni Plate Body (Brushed Dark Steel / Carbon Fiber)
                drawRoundRect(
                    color = CyanNeon.copy(alpha = 0.08f),
                    topLeft = Offset(plateLeft - 8f, plateTop - 8f),
                    size = Size(plateSize + 16f, plateSize + 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f)
                )
                drawRoundRect(
                    color = Color(0xFF141923),
                    topLeft = Offset(plateLeft, plateTop),
                    size = Size(plateSize, plateSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                )
                drawRoundRect(
                    color = ScienceBorder.copy(alpha = 0.6f),
                    topLeft = Offset(plateLeft, plateTop),
                    size = Size(plateSize, plateSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Corner mounting bolts
                val boltOffset = 10.dp.toPx()
                val boltRadius = 3.5.dp.toPx()
                val bolts = listOf(
                    Offset(plateLeft + boltOffset, plateTop + boltOffset),
                    Offset(plateLeft + plateSize - boltOffset, plateTop + boltOffset),
                    Offset(plateLeft + boltOffset, plateTop + plateSize - boltOffset),
                    Offset(plateLeft + plateSize - boltOffset, plateTop + plateSize - boltOffset)
                )
                for (bolt in bolts) {
                    drawCircle(Color(0xFF455A64), boltRadius, bolt)
                    drawCircle(Color(0xFFCFD8DC), boltRadius * 0.5f, bolt)
                }

                // 3. Vibration Mode Heatmap Background (Subtle antinode glow)
                val gridSteps = 24
                val stepPx = plateSize / gridSteps
                val mPi = curM * PI.toFloat() * 0.5f
                val nPi = curN * PI.toFloat() * 0.5f
                for (ix in 0 until gridSteps) {
                    val nx = -1f + 2f * (ix + 0.5f) / gridSteps
                    val cosMx = cos(mPi * nx)
                    val cosNx = cos(nPi * nx)
                    for (iy in 0 until gridSteps) {
                        val ny = -1f + 2f * (iy + 0.5f) / gridSteps
                        val wVal = cosMx * cos(nPi * ny) - alphaCoupling * cosNx * cos(mPi * ny)
                        val intensity = (abs(wVal) * 0.5f).coerceIn(0f, 1f)
                        if (intensity > 0.15f) {
                            val px = plateLeft + ix * stepPx
                            val py = plateTop + iy * stepPx
                            drawRect(
                                color = PurpleNeon.copy(alpha = intensity * 0.16f * driveAmplitude),
                                topLeft = Offset(px, py),
                                size = Size(stepPx, stepPx)
                            )
                        }
                    }
                }

                // 4. Theoretical Nodal Contour Lines (Subtle Cyan Guide)
                val contourSteps = 40
                val cStep = 2f / contourSteps
                var cxVal = -1f
                while (cxVal <= 1f) {
                    var cyVal = -1f
                    while (cyVal <= 1f) {
                        val wVal = cos(mPi * cxVal) * cos(nPi * cyVal) - alphaCoupling * cos(nPi * cxVal) * cos(mPi * cyVal)
                        if (abs(wVal) < 0.08f) {
                            val px = cx + cxVal * halfPlate
                            val py = cy + cyVal * halfPlate
                            drawCircle(
                                color = CyanNeon.copy(alpha = 0.22f),
                                radius = 1.2.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                        cyVal += cStep
                    }
                    cxVal += cStep
                }

                // 5. Sand Particles (Bouncing & Congregated into Nodal Lines)
                val sandRadius = 1.8.dp.toPx()
                for (p in sandParticles) {
                    val sx = cx + p.x * halfPlate
                    val sy = cy + p.y * halfPlate

                    val wVal = cos(mPi * p.x) * cos(nPi * p.y) - alphaCoupling * cos(nPi * p.x) * cos(mPi * p.y)
                    val isNodal = abs(wVal) < 0.12f

                    val particleColor = if (isNodal) {
                        AmberVibrant
                    } else {
                        CoralNeon.copy(alpha = 0.75f)
                    }

                    drawCircle(
                        color = particleColor,
                        radius = sandRadius,
                        center = Offset(sx, sy)
                    )
                }

                // 6. Central Excitation / Vibration Driver Post
                drawCircle(
                    color = Color(0xFF263238),
                    radius = 12.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.6f),
                    radius = 8.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.8.dp.toPx())
                )
                drawCircle(
                    color = AmberVibrant,
                    radius = 3.dp.toPx(),
                    center = Offset(cx, cy)
                )

                // 7. Touch Interactivity Disturbance
                touchPos?.let { tap ->
                    drawCircle(
                        color = AmberVibrant.copy(alpha = 0.35f),
                        radius = 24.dp.toPx(),
                        center = tap
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = tap
                    )
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 24: Chladni Plates Cymatics",
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f),
                items = listOf(
                    "Biharmonic Wave" to "D·∇⁴w + ρh·(∂²w/∂t²) = 0",
                    "Nodal Mode (m, n)" to "($curM, $curN)",
                    "Eigenfrequency" to "$resonantFreq Hz",
                    "Nodal Clustered" to "$clusterPercent%",
                    "Coupling α" to "${round(alphaCoupling * 100) / 100f}"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Mode Chips Row (compact 32.dp height)
                Text(
                    text = "RESONANT MODES (m, n)",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        Triple(1f, 1f, "1,1 Ring"),
                        Triple(1f, 3f, "1,3 Star"),
                        Triple(2f, 2f, "2,2 Cross"),
                        Triple(3f, 3f, "3,3 Grid"),
                        Triple(2f, 4f, "2,4 Flower"),
                        Triple(3f, 5f, "3,5 Mandala")
                    )
                    for ((m, n, label) in presets) {
                        val isSelected = round(modeM) == m && round(modeN) == n
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) CyanNeon.copy(alpha = 0.25f)
                                    else ScienceDarkSurfaceVariant
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CyanNeon else ScienceBorder.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    modeM = m
                                    modeN = n
                                    sprinkleSand()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) CyanNeon else TextPrimary,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Sliders Row 1: Mode m and Mode n paired side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Mode m",
                            value = modeM,
                            range = 1f..6f,
                            valueDisplay = "${curM}",
                            accentColor = CyanNeon,
                            onValueChange = { modeM = it }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Mode n",
                            value = modeN,
                            range = 1f..6f,
                            valueDisplay = "${curN}",
                            accentColor = AmberVibrant,
                            onValueChange = { modeN = it }
                        )
                    }
                }

                // Sliders Row 2: Vibration Drive and Coupling alpha paired side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Drive Amp",
                            value = driveAmplitude,
                            range = 0.2f..1.0f,
                            valueDisplay = "${round(driveAmplitude * 100).toInt()}%",
                            accentColor = PurpleNeon,
                            onValueChange = { driveAmplitude = it }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Coupling α",
                            value = alphaCoupling,
                            range = -1.0f..1.0f,
                            valueDisplay = "${round(alphaCoupling * 100) / 100f}",
                            accentColor = CoralNeon,
                            onValueChange = { alphaCoupling = it }
                        )
                    }
                }

                // Action Buttons Row: Sprinkle Sand, Run/Pause, Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = sprinkleSand,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralNeon.copy(alpha = 0.2f),
                            contentColor = CoralNeon
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralNeon.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✨ Sprinkle Sand",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberVibrant else CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            modeM = 1f
                            modeN = 3f
                            alphaCoupling = 1.0f
                            driveAmplitude = 0.70f
                            isRunning = true
                            sprinkleSand()
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
