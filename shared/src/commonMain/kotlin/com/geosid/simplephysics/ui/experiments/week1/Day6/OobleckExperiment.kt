package com.geosid.simplephysics.ui.experiments.week1.Day6

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
import androidx.compose.ui.draw.clip
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

@Composable
fun OobleckExperiment(
    modifier: Modifier = Modifier
) {
    // Dynamic physics state
    var paramPrimary by remember { mutableStateOf(0.5f) }
    var paramSecondary by remember { mutableStateOf(0.7f) }
    var isRunning by remember { mutableStateOf(true) }
    var touchPos by remember { mutableStateOf(Offset(0.5f, 0.5f)) }

    val infiniteTransition = rememberInfiniteTransition()
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRunning) 3000 else 999999, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val liveMetric1 = (paramPrimary * 100).toInt()
    val liveMetric2 = round((paramSecondary * 9.81f) * 100f) / 100f

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag to interact with Day 6: Non-Newtonian Oobleck. Tune physical parameters below!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            touchPos = Offset(
                                (touchPos.x + dragAmount.x / size.width).coerceIn(0.05f, 0.95f),
                                (touchPos.y + dragAmount.y / size.height).coerceIn(0.1f, 0.9f)
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            touchPos = Offset(
                                (offset.x / size.width).coerceIn(0.05f, 0.95f),
                                (offset.y / size.height).coerceIn(0.1f, 0.9f)
                            )
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.5f, h * 0.48f)
                val activePt = Offset(w * touchPos.x, h * touchPos.y)

                // 1. Scientific coordinate grid
                val gridSpacing = 40.dp.toPx()
                var gx = 0f
                while (gx < w) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.25f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, h),
                        strokeWidth = 0.8f
                    )
                    gx += gridSpacing
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.25f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 0.8f
                    )
                    gy += gridSpacing
                }

                // 2. Physical Center & Equilibrium field
                drawCircle(
                    color = PurpleNeon.copy(alpha = 0.15f),
                    radius = 90.dp.toPx() * paramPrimary,
                    center = center
                )
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.5f),
                    radius = 45.dp.toPx() * paramSecondary,
                    center = center,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), animPhase * 10f))
                )

                // 3. Dynamic Waveform / Trajectory Streamline
                val wavePath = Path()
                val steps = 60
                for (i in 0..steps) {
                    val t = i / steps.toFloat()
                    val x = w * (0.1f + 0.8f * t)
                    val envelope = sin(t * PI.toFloat())
                    val yOffset = sin(animPhase + t * 4 * PI.toFloat()) * 38.dp.toPx() * paramPrimary * envelope
                    val y = center.y + yOffset
                    if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                }
                drawPath(
                    path = wavePath,
                    color = CyanNeon,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // 4. Interaction Vector & Target Node
                drawLine(
                    color = AmberVibrant.copy(alpha = 0.8f),
                    start = center,
                    end = activePt,
                    strokeWidth = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )

                // Active Node with Glow
                drawCircle(
                    color = AmberVibrant.copy(alpha = 0.25f),
                    radius = 24.dp.toPx(),
                    center = activePt
                )
                drawCircle(
                    color = AmberVibrant,
                    radius = 8.dp.toPx(),
                    center = activePt
                )

                // Orbiting / Particle Dynamics
                val orbitCount = 6
                for (k in 0 until orbitCount) {
                    val angle = animPhase + k * (2 * PI.toFloat() / orbitCount)
                    val r = 70.dp.toPx() * paramSecondary
                    val px = center.x + cos(angle) * r
                    val py = center.y + sin(angle) * r * 0.65f
                    drawCircle(
                        color = if (k % 2 == 0) CyanNeon else PurpleNeon,
                        radius = 4.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 6: Non-Newtonian Oobleck",
                items = listOf(
                    "Formula" to "\\tau = K \\left(\\frac{du}{dy}\\right)^n \\quad (n > 1)",
                    "Parameter" to "$liveMetric1%",
                    "Kinetic Index" to "$liveMetric2",
                    "State" to if (isRunning) "ACTIVE SIMULATION" else "PAUSED"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PhysicsSliderControl(
                    title = "Primary Amplitude / Intensity",
                    value = paramPrimary,
                    range = 0.1f..1.0f,
                    valueDisplay = "$liveMetric1%",
                    accentColor = CyanNeon,
                    onValueChange = { paramPrimary = it }
                )

                PhysicsSliderControl(
                    title = "Secondary Frequency / Coupling",
                    value = paramSecondary,
                    range = 0.2f..1.5f,
                    valueDisplay = "${round(paramSecondary * 10f) / 10f}x",
                    accentColor = AmberVibrant,
                    onValueChange = { paramSecondary = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberVibrant else CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(12.dp),
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
                            paramPrimary = 0.5f
                            paramSecondary = 0.7f
                            isRunning = true
                            touchPos = Offset(0.5f, 0.5f)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(12.dp))
                    ) {
                        ResetIcon(tint = CyanNeon, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    )
}
