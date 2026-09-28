package com.geosid.simplephysics.ui.experiments.week4.Day22

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

@Composable
fun PluckedStringExperiment(
    modifier: Modifier = Modifier
) {
    val nodeCount = 72

    // Wave parameters
    var waveSpeedC by remember { mutableStateOf(1.0f) } // c factor
    var dampingFactor by remember { mutableStateOf(0.015f) } // gamma

    // Wave arrays: current, previous, and next displacements
    var uCurrent by remember { mutableStateOf(FloatArray(nodeCount)) }
    var uPrev by remember { mutableStateOf(FloatArray(nodeCount)) }
    var isPlucking by remember { mutableStateOf(false) }

    // Simulation loop (Finite Difference Wave Integrator)
    LaunchedEffect(isPlucking, waveSpeedC, dampingFactor) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.035f)
            lastTime = now

            if (!isPlucking) {
                // Perform multiple sub-steps for numerical stability
                val subSteps = 8
                val courantR = (0.75f * waveSpeedC).coerceAtMost(0.92f)
                val r2 = courantR * courantR

                val curr = uCurrent.copyOf()
                val prev = uPrev.copyOf()
                val next = FloatArray(nodeCount)

                for (s in 0 until subSteps) {
                    for (i in 1 until nodeCount - 1) {
                        // 1D wave equation: u_tt = c^2 u_xx - damping * u_t
                        val laplacian = curr[i + 1] - 2f * curr[i] + curr[i - 1]
                        val velocity = curr[i] - prev[i]
                        next[i] = 2f * curr[i] - prev[i] + r2 * laplacian - dampingFactor * velocity
                    }
                    // Fixed boundary conditions
                    next[0] = 0f
                    next[nodeCount - 1] = 0f

                    for (i in 0 until nodeCount) {
                        prev[i] = curr[i]
                        curr[i] = next[i]
                    }
                }

                uPrev = prev
                uCurrent = curr
            }
        }
    }

    val maxAmp = round((uCurrent.maxOfOrNull { abs(it) } ?: 0f) * 10) / 10f
    val energyNorm = round(uCurrent.sumOf { (it * it).toDouble() }.toFloat() * 10) / 10f

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag across the string to pluck it, or select a harmonic preset to watch wave reflections & standing nodes!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isPlucking = true
                                pluckStringAt(offset.x, offset.y, size.width.toFloat(), size.height.toFloat(), nodeCount, uCurrent, uPrev)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val pos = change.position
                                pluckStringAt(pos.x, pos.y, size.width.toFloat(), size.height.toFloat(), nodeCount, uCurrent, uPrev)
                            },
                            onDragEnd = {
                                isPlucking = false
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height

                val stringLeftX = w * 0.10f
                val stringRightX = w * 0.90f
                val stringLength = stringRightX - stringLeftX
                val baselineY = h * 0.40f

                // 1. Draw Wooden Instrument Soundboard Base
                drawRoundRect(
                    color = Color(0xFF3E2723),
                    topLeft = Offset(stringLeftX - 30f, baselineY - 80f),
                    size = Size(stringLength + 60f, 160f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f)
                )
                drawRoundRect(
                    color = Color(0xFF4E342E),
                    topLeft = Offset(stringLeftX - 25f, baselineY - 75f),
                    size = Size(stringLength + 50f, 150f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
                )

                // Central Sound Hole
                drawCircle(
                    color = Color(0xFF1B0000),
                    radius = 38f,
                    center = Offset(w * 0.5f, baselineY)
                )
                drawCircle(
                    color = AmberVibrant.copy(alpha = 0.6f),
                    radius = 40f,
                    center = Offset(w * 0.5f, baselineY),
                    style = Stroke(width = 2.5f)
                )

                // 2. Fixed End Pegs (Nuts / Bridges)
                drawRect(Color(0xFFE0E0E0), Offset(stringLeftX - 8f, baselineY - 35f), Size(8f, 70f))
                drawRect(Color(0xFFE0E0E0), Offset(stringRightX, baselineY - 35f), Size(8f, 70f))

                // 3. Draw Vibrating Glowing String
                val stringPath = Path().apply {
                    moveTo(stringLeftX, baselineY)
                    val stepX = stringLength / (nodeCount - 1)
                    for (i in 0 until nodeCount) {
                        val sx = stringLeftX + i * stepX
                        val sy = baselineY + uCurrent[i]
                        lineTo(sx, sy)
                    }
                }

                // Outer string glow
                drawPath(
                    path = stringPath,
                    color = CyanNeon.copy(alpha = 0.4f),
                    style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Core string line
                drawPath(
                    path = stringPath,
                    color = Color.White,
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // 4. Acoustic Radiation Concentric Ripples
                val maxDisp = uCurrent.maxOfOrNull { abs(it) } ?: 0f
                if (maxDisp > 4f) {
                    val waveCount = 3
                    for (wIdx in 1..waveCount) {
                        val r = 48f + wIdx * 25f + (maxDisp * 0.4f)
                        drawCircle(
                            color = CyanNeon.copy(alpha = (0.25f / wIdx).coerceIn(0.05f, 0.3f)),
                            radius = r,
                            center = Offset(w * 0.5f, baselineY),
                            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                        )
                    }
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                title = "Acoustic Wave Telemetry",
                items = listOf(
                    "Peak Displacement" to "$maxAmp px",
                    "Wave Speed (c)" to "${round(waveSpeedC * 100) / 100f}x",
                    "Mode State" to if (isPlucking) "Plucking..." else if (maxAmp > 1f) "Vibrating Harmonics" else "At Rest",
                    "Energy" to "$energyNorm a.u."
                ),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f)
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Harmonic Preset Compact Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    listOf(
                        "n=1 (Fundamental)" to 1,
                        "n=2 (2nd Harmonic)" to 2,
                        "n=3 (3rd Harmonic)" to 3,
                        "Pluck (Triangle)" to 0
                    ).forEach { (label, mode) ->
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ScienceDarkSurfaceVariant)
                                .border(1.dp, ScienceBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    val newArr = FloatArray(nodeCount)
                                    for (i in 0 until nodeCount) {
                                        val xNorm = i.toFloat() / (nodeCount - 1)
                                        if (mode == 0) {
                                            newArr[i] = if (xNorm < 0.35f) (xNorm / 0.35f) * 55f else ((1f - xNorm) / 0.65f) * 55f
                                        } else {
                                            newArr[i] = sin(mode * PI.toFloat() * xNorm) * 50f
                                        }
                                    }
                                    uCurrent = newArr
                                    uPrev = newArr.copyOf()
                                }
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanNeon
                            )
                        }
                    }
                }

                Surface(
                    color = ScienceDarkSurfaceVariant.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                PhysicsSliderControl(
                                    title = "Wave Speed (c)",
                                    value = waveSpeedC,
                                    range = 0.4f..1.2f,
                                    valueDisplay = "${round(waveSpeedC * 10) / 10f}",
                                    accentColor = CyanNeon,
                                    onValueChange = { waveSpeedC = it }
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                PhysicsSliderControl(
                                    title = "Damping (γ)",
                                    value = dampingFactor,
                                    range = 0.002f..0.05f,
                                    valueDisplay = "${round(dampingFactor * 1000) / 1000f}",
                                    accentColor = PurpleNeon,
                                    onValueChange = { dampingFactor = it }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎸 Drag finger/mouse on string to pluck!",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )

                            IconButton(
                                onClick = {
                                    uCurrent = FloatArray(nodeCount)
                                    uPrev = FloatArray(nodeCount)
                                    waveSpeedC = 1.0f
                                    dampingFactor = 0.015f
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ScienceDarkSurface, RoundedCornerShape(8.dp))
                            ) {
                                ResetIcon(tint = CyanNeon, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}

private fun pluckStringAt(
    touchX: Float,
    touchY: Float,
    width: Float,
    height: Float,
    nodeCount: Int,
    uCurrent: FloatArray,
    uPrev: FloatArray
) {
    val stringLeftX = width * 0.10f
    val stringRightX = width * 0.90f
    val stringLength = stringRightX - stringLeftX
    val baselineY = height * 0.40f

    val pluckNode = (((touchX - stringLeftX) / stringLength) * (nodeCount - 1)).toInt().coerceIn(1, nodeCount - 2)
    val displacement = (touchY - baselineY).coerceIn(-90f, 90f)

    for (i in 0 until nodeCount) {
        val amp = if (i <= pluckNode) {
            (i.toFloat() / pluckNode) * displacement
        } else {
            ((nodeCount - 1 - i).toFloat() / (nodeCount - 1 - pluckNode)) * displacement
        }
        uCurrent[i] = amp
        uPrev[i] = amp
    }
}
