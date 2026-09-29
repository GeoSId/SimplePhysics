package com.geosid.simplephysics.ui.experiments.week4.Day23

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

enum class FourierWaveType(val displayName: String) {
    SQUARE("Square Wave"),
    SAWTOOTH("Sawtooth Wave"),
    TRIANGLE("Triangle Wave"),
    PULSE("Pulse Wave")
}

@Composable
fun FourierSeriesExperiment(
    modifier: Modifier = Modifier
) {
    var waveType by remember { mutableStateOf(FourierWaveType.SQUARE) }
    var harmonicCount by remember { mutableStateOf(5) } // N harmonics
    var speedMultiplier by remember { mutableStateOf(1.0f) }
    var isPaused by remember { mutableStateOf(false) }

    // Phase angle in radians
    var phaseTime by remember { mutableStateOf(0f) }

    // Waveform history buffer (scrolling y values)
    val waveHistory = remember { mutableStateListOf<Float>() }
    val maxHistoryPoints = 260

    // Animation loop
    LaunchedEffect(isPaused, speedMultiplier) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.035f)
            lastTime = now

            if (!isPaused) {
                val dPhase = 1.6f * speedMultiplier * dt
                phaseTime += dPhase

                // Calculate current y value at tip
                val currentY = calculateFourierTipY(phaseTime, waveType, harmonicCount)
                waveHistory.add(0, currentY)
                if (waveHistory.size > maxHistoryPoints) {
                    waveHistory.removeAt(waveHistory.size - 1)
                }
            }
        }
    }

    val gibbsText = if (harmonicCount > 12 && waveType == FourierWaveType.SQUARE) "Visible (~9% overshoot)" else "Minimal"

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "💡 Watch round rotating circles add up to form sharp square corners & harmonic waves!",
        canvasContent = {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val cy = h * 0.38f
                val cx = w * 0.22f
                val epicycleBaseRadius = min(w * 0.14f, h * 0.20f)

                // 1. Draw Rotating Epicycles & Compute Tip Position
                val tipPos = drawEpicycles(
                    centerX = cx,
                    centerY = cy,
                    baseRadius = epicycleBaseRadius,
                    phase = phaseTime,
                    waveType = waveType,
                    harmonicCount = harmonicCount
                )

                // 2. Draw Synthesized Wave Graph on the right
                val graphStartX = cx + epicycleBaseRadius * 1.4f + 25f
                val graphEndX = w - 16f
                drawWaveformGraph(
                    startX = graphStartX,
                    endX = graphEndX,
                    baselineY = cy,
                    tipY = tipPos.y,
                    waveHistory = waveHistory,
                    waveType = waveType,
                    baseRadius = epicycleBaseRadius
                )

                // 3. Laser Pointer line from epicycle tip to graph start
                drawLine(
                    color = CoralNeon.copy(alpha = 0.85f),
                    start = tipPos,
                    end = Offset(graphStartX, tipPos.y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )

                // Pointer dot at graph entry
                drawCircle(
                    color = CoralNeon,
                    radius = 5f,
                    center = Offset(graphStartX, tipPos.y)
                )

                // 4. Draw Fourier Frequency Spectrum Bars (FFT) under graph
                drawFourierSpectrum(
                    startX = graphStartX,
                    startY = cy + epicycleBaseRadius * 1.2f + 16f,
                    width = (graphEndX - graphStartX) * 0.95f,
                    height = 36f,
                    harmonicCount = harmonicCount,
                    waveType = waveType
                )
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Fourier Synthesis Telemetry",
                items = listOf(
                    "Target Wave" to waveType.displayName,
                    "Harmonics (N)" to "$harmonicCount",
                    "Speed (ω)" to "${round(speedMultiplier * 10) / 10f}x",
                    "Gibbs Ringing" to gibbsText,
                    "Basis Functions" to "Sinusoids e^(inωt)"
                ),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f)
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Waveform Preset Compact Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FourierWaveType.values().forEach { type ->
                        val isSel = waveType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CyanNeon else ScienceDarkSurfaceVariant)
                                .border(1.dp, if (isSel) CyanNeon else ScienceBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    waveType = type
                                    waveHistory.clear()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.displayName.replace(" Wave", ""),
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) ScienceDarkBg else TextSecondary
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
                                    title = "Harmonics (N)",
                                    value = harmonicCount.toFloat(),
                                    range = 1f..25f,
                                    valueDisplay = "N = $harmonicCount",
                                    accentColor = AmberVibrant,
                                    onValueChange = {
                                        harmonicCount = it.toInt()
                                        waveHistory.clear()
                                    }
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                PhysicsSliderControl(
                                    title = "Speed (ω)",
                                    value = speedMultiplier,
                                    range = 0.2f..2.5f,
                                    valueDisplay = "${round(speedMultiplier * 10) / 10f}x",
                                    accentColor = CyanNeon,
                                    onValueChange = { speedMultiplier = it }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { isPaused = !isPaused },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPaused) EmeraldNeon else CoralNeon,
                                    contentColor = ScienceDarkBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (isPaused) "▶ Resume" else "⏸ Pause",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = {
                                    waveType = FourierWaveType.SQUARE
                                    harmonicCount = 5
                                    speedMultiplier = 1.0f
                                    isPaused = false
                                    waveHistory.clear()
                                    phaseTime = 0f
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

// Pure Compose Drawing Helpers
private fun DrawScope.drawEpicycles(
    centerX: Float,
    centerY: Float,
    baseRadius: Float,
    phase: Float,
    waveType: FourierWaveType,
    harmonicCount: Int
): Offset {
    var prevX = centerX
    var prevY = centerY

    val circleColors = listOf(CyanNeon, AmberVibrant, PurpleNeon, EmeraldNeon, CoralNeon)

    for (i in 1..harmonicCount) {
        // Calculate n and radius r
        val (n, r) = getHarmonicParams(i, baseRadius, waveType)
        if (r < 1f) continue

        // Circle outline
        val col = circleColors[(i - 1) % circleColors.size]
        drawCircle(
            color = col.copy(alpha = 0.35f),
            radius = r,
            center = Offset(prevX, prevY),
            style = Stroke(width = 1.5f)
        )

        // Tip position
        val angle = n * phase
        val curX = prevX + r * cos(angle)
        val curY = prevY + r * sin(angle)

        // Radius arm line
        drawLine(
            color = col.copy(alpha = 0.85f),
            start = Offset(prevX, prevY),
            end = Offset(curX, curY),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        // Pivot center dot
        drawCircle(
            color = col,
            radius = 3f,
            center = Offset(prevX, prevY)
        )

        prevX = curX
        prevY = curY
    }

    // Final tip indicator
    drawCircle(
        color = Color.White,
        radius = 5f,
        center = Offset(prevX, prevY)
    )

    return Offset(prevX, prevY)
}

private fun DrawScope.drawWaveformGraph(
    startX: Float,
    endX: Float,
    baselineY: Float,
    tipY: Float,
    waveHistory: List<Float>,
    waveType: FourierWaveType,
    baseRadius: Float
) {
    // Axis line
    drawLine(
        color = ScienceBorder,
        start = Offset(startX, baselineY),
        end = Offset(endX, baselineY),
        strokeWidth = 2f
    )

    // Upper and lower bounds
    drawLine(
        color = ScienceBorder.copy(alpha = 0.4f),
        start = Offset(startX, baselineY - baseRadius * 1.2f),
        end = Offset(endX, baselineY - baseRadius * 1.2f),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
    )
    drawLine(
        color = ScienceBorder.copy(alpha = 0.4f),
        start = Offset(startX, baselineY + baseRadius * 1.2f),
        end = Offset(endX, baselineY + baseRadius * 1.2f),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
    )

    // Waveform curve
    if (waveHistory.size > 1) {
        val path = Path().apply {
            moveTo(startX, baselineY + waveHistory[0])
            val stepX = (endX - startX) / 220f
            for (i in 1 until waveHistory.size) {
                val px = startX + i * stepX
                if (px <= endX) {
                    lineTo(px, baselineY + waveHistory[i])
                }
            }
        }

        // Glowing waveform stroke
        drawPath(
            path = path,
            color = CyanNeon.copy(alpha = 0.35f),
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = path,
            color = CyanNeon,
            style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

private fun DrawScope.drawFourierSpectrum(
    startX: Float,
    startY: Float,
    width: Float,
    height: Float,
    harmonicCount: Int,
    waveType: FourierWaveType
) {
    // Card background
    drawRoundRect(
        color = ScienceDarkSurface.copy(alpha = 0.88f),
        topLeft = Offset(startX - 10f, startY - 20f),
        size = Size(width + 20f, height + 40f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
    )

    // Baseline
    val baseY = startY + height
    drawLine(Color.White.copy(alpha = 0.4f), Offset(startX, baseY), Offset(startX + width, baseY), 1.5f)

    // Bars
    val displayBars = min(harmonicCount, 16)
    val barW = (width / (displayBars * 1.6f)).coerceAtMost(16f)

    for (i in 1..displayBars) {
        val (_, r) = getHarmonicParams(i, height, waveType)
        val barH = (r * 0.85f).coerceIn(2f, height)
        val bx = startX + (i - 1) * (barW * 1.5f)

        drawRoundRect(
            color = AmberVibrant,
            topLeft = Offset(bx, baseY - barH),
            size = Size(barW, barH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
        )
    }
}

// Math calculation of radius & frequency for each Fourier harmonic
private fun getHarmonicParams(
    i: Int,
    baseRadius: Float,
    waveType: FourierWaveType
): Pair<Int, Float> {
    return when (waveType) {
        FourierWaveType.SQUARE -> {
            // Odd harmonics: 1, 3, 5, 7, ...
            val n = 2 * i - 1
            val r = (baseRadius * 4f / (n * PI.toFloat()))
            n to r
        }
        FourierWaveType.SAWTOOTH -> {
            // All harmonics: 1, 2, 3, 4, ...
            val n = i
            val r = (baseRadius * 2f / (n * PI.toFloat()))
            n to r
        }
        FourierWaveType.TRIANGLE -> {
            // Odd harmonics falling off as 1/n^2
            val n = 2 * i - 1
            val r = (baseRadius * 8f / (n * n * PI.toFloat() * PI.toFloat()))
            n to r
        }
        FourierWaveType.PULSE -> {
            // Harmonics with sinc envelope
            val n = i
            val duty = 0.25f
            val r = (baseRadius * 2f * sin(n * PI.toFloat() * duty) / (n * PI.toFloat())).coerceAtLeast(0f)
            n to r
        }
    }
}

private fun calculateFourierTipY(
    phase: Float,
    waveType: FourierWaveType,
    harmonicCount: Int
): Float {
    var sum = 0f
    for (i in 1..harmonicCount) {
        val (n, r) = getHarmonicParams(i, 80f, waveType)
        sum += r * sin(n * phase)
    }
    return sum
}
