package com.geosid.simplephysics.ui.experiments.week1.Day5

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
fun CartesianDiverExperiment(
    modifier: Modifier = Modifier
) {
    // Bottle Squeeze Pressure: 1.0 atm (uncompressed) to 3.5 atm (heavily squeezed)
    var appliedPressureAtm by remember { mutableStateOf(1.0f) }

    // Diver Dynamic Physics
    // Depth from 0.08f (top surface) to 0.88f (bottom of bottle)
    var diverDepth by remember { mutableStateOf(0.12f) }
    var diverVelocityY by remember { mutableStateOf(0f) }

    // Boyle's Law: V = V0 * (P0 / P)
    val bubbleVolumeFraction = (1.0f / appliedPressureAtm).coerceIn(0.28f, 1.0f)

    // Neutral buoyancy occurs around P = 1.65 atm
    // Buoyancy net force: F_net = k * (V - V_neutral)
    val neutralVolume = 1.0f / 1.65f
    val netBuoyancyForce = (bubbleVolumeFraction - neutralVolume) * 18f

    // Physics integration loop
    LaunchedEffect(appliedPressureAtm) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
            lastTime = now

            // Acceleration downwards is -netBuoyancyForce
            // If netBuoyancyForce > 0, diver accelerates upwards (depth decreases)
            val accelerationY = -netBuoyancyForce
            val damping = 3.2f

            diverVelocityY += (accelerationY - damping * diverVelocityY) * dt
            diverDepth = (diverDepth + diverVelocityY * dt).coerceIn(0.12f, 0.86f)

            // Bounce / stop at boundaries
            if (diverDepth <= 0.12f && diverVelocityY < 0f) {
                diverVelocityY = 0f
            } else if (diverDepth >= 0.86f && diverVelocityY > 0f) {
                diverVelocityY = 0f
            }
        }
    }

    val buoyancyStatus = when {
        netBuoyancyForce > 0.8f -> "⬆️ FLOATING (Fb > W)"
        netBuoyancyForce < -0.8f -> "⬇️ SINKING (W > Fb)"
        else -> "⚖️ NEUTRAL HOVER (Fb = W)"
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "💧 Drag the slider or squeeze the bottle directly on screen! Watch the air bubble compress and sink.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                appliedPressureAtm = (appliedPressureAtm + dragAmount.y * 0.008f).coerceIn(1.0f, 3.5f)
                            },
                            onDragEnd = {
                                appliedPressureAtm = 1.0f
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height

                // Bottle dimensions
                val bottleW = min(w * 0.40f, 220.dp.toPx())
                val bottleH = min(h * 0.88f, 500.dp.toPx())
                val bottleLeft = (w - bottleW) / 2f
                val bottleTop = h * 0.06f
                val bottleBottom = bottleTop + bottleH

                // Squeeze deformation factor (indents bottle sides slightly)
                val squeezeIndent = (appliedPressureAtm - 1.0f) * 12f

                // 1. Draw Water inside Bottle
                drawBottleWater(
                    left = bottleLeft,
                    top = bottleTop,
                    width = bottleW,
                    height = bottleH,
                    squeezeIndent = squeezeIndent
                )

                // 2. Draw Cartesian Diver inside Water
                val diverY = bottleTop + (bottleH * diverDepth)
                drawCartesianDiver(
                    centerX = w / 2f,
                    centerY = diverY,
                    bubbleScale = bubbleVolumeFraction
                )

                // 3. Draw Outer Bottle Plastic Shell & Cap
                drawBottlePlasticShell(
                    left = bottleLeft,
                    top = bottleTop,
                    width = bottleW,
                    height = bottleH,
                    squeezeIndent = squeezeIndent
                )
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Pascal & Boyle Telemetry",
                items = listOf(
                    "Internal Pressure" to "${round(appliedPressureAtm * 10) / 10f} atm",
                    "Air Bubble Volume" to "${(bubbleVolumeFraction * 100).toInt()}%",
                    "Diver State" to buoyancyStatus,
                    "Depth" to "${((diverDepth - 0.12f) / 0.74f * 100).toInt()}%"
                )
            )
        },
        controlsContent = {
            Surface(
                color = ScienceDarkSurfaceVariant.copy(alpha = 0.94f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    PhysicsSliderControl(
                        title = "Hydrostatic Squeeze Pressure",
                        value = appliedPressureAtm,
                        range = 1.0f..3.5f,
                        valueDisplay = "${round(appliedPressureAtm * 10) / 10f} atm",
                        accentColor = if (appliedPressureAtm > 1.8f) CoralNeon else CyanNeon,
                        onValueChange = { appliedPressureAtm = it }
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { appliedPressureAtm = 2.8f },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralNeon, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("✊ Full Squeeze", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { appliedPressureAtm = 1.65f },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberVibrant, contentColor = ScienceDarkBg),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("⚖️ Neutral", fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                appliedPressureAtm = 1.0f
                                diverVelocityY = 0f
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(ScienceDarkSurface, RoundedCornerShape(8.dp))
                        ) {
                            ResetIcon(tint = CyanNeon, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    )
}

// Drawing Helpers
private fun DrawScope.drawBottleWater(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    squeezeIndent: Float
) {
    val cornerR = 26f
    val neckW = width * 0.35f
    val neckH = 45f

    val path = Path().apply {
        moveTo(left + (width - neckW) / 2f, top + neckH)
        // Right shoulder
        quadraticTo(left + width, top + neckH + 20f, left + width - squeezeIndent, top + height * 0.4f)
        // Right waist
        quadraticTo(left + width - squeezeIndent * 1.5f, top + height * 0.55f, left + width, top + height - cornerR)
        // Bottom right corner
        quadraticTo(left + width, top + height, left + width - cornerR, top + height)
        // Bottom edge
        lineTo(left + cornerR, top + height)
        // Bottom left corner
        quadraticTo(left, top + height, left, top + height - cornerR)
        // Left waist
        quadraticTo(left + squeezeIndent * 1.5f, top + height * 0.55f, left + squeezeIndent, top + height * 0.4f)
        // Left shoulder
        quadraticTo(left, top + neckH + 20f, left + (width - neckW) / 2f, top + neckH)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(WaterBlue.copy(alpha = 0.45f), WaterDeep.copy(alpha = 0.75f)),
            startY = top + neckH,
            endY = top + height
        )
    )
}

private fun DrawScope.drawBottlePlasticShell(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    squeezeIndent: Float
) {
    val cornerR = 26f
    val neckW = width * 0.35f
    val neckH = 45f

    // 1. Bottle Outer Outline
    val path = Path().apply {
        moveTo(left + (width - neckW) / 2f, top + neckH)
        quadraticTo(left + width, top + neckH + 20f, left + width - squeezeIndent, top + height * 0.4f)
        quadraticTo(left + width - squeezeIndent * 1.5f, top + height * 0.55f, left + width, top + height - cornerR)
        quadraticTo(left + width, top + height, left + width - cornerR, top + height)
        lineTo(left + cornerR, top + height)
        quadraticTo(left, top + height, left, top + height - cornerR)
        quadraticTo(left + squeezeIndent * 1.5f, top + height * 0.55f, left + squeezeIndent, top + height * 0.4f)
        quadraticTo(left, top + neckH + 20f, left + (width - neckW) / 2f, top + neckH)
        lineTo(left + (width - neckW) / 2f, top)
        lineTo(left + (width + neckW) / 2f, top)
        lineTo(left + (width + neckW) / 2f, top + neckH)
    }

    drawPath(
        path = path,
        color = Color(0x66FFFFFF),
        style = Stroke(width = 3.5f)
    )

    // 2. Plastic Screw Cap
    val capW = neckW + 8f
    val capH = 22f
    drawRoundRect(
        color = Color(0xFF1E88E5),
        topLeft = Offset(left + (width - capW) / 2f, top - 8f),
        size = Size(capW, capH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )
    // Cap ribbed grooves
    for (i in 1..4) {
        val gx = left + (width - capW) / 2f + i * (capW / 5f)
        drawLine(
            color = Color(0xFF1565C0),
            start = Offset(gx, top - 8f),
            end = Offset(gx, top - 8f + capH),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawCartesianDiver(
    centerX: Float,
    centerY: Float,
    bubbleScale: Float
) {
    val diverW = 32f
    val diverH = 75f

    // Diver is a glass eyedropper with a rubber bulb at top
    // 1. Rubber Bulb on Top
    drawCircle(
        color = Color(0xFFD32F2F),
        radius = 12f,
        center = Offset(centerX, centerY - diverH / 2f)
    )

    // 2. Clear Glass Barrel
    val barrelPath = Path().apply {
        moveTo(centerX - diverW / 2f, centerY - diverH / 2f + 8f)
        lineTo(centerX + diverW / 2f, centerY - diverH / 2f + 8f)
        lineTo(centerX + diverW / 2f, centerY + diverH * 0.25f)
        // Taper down to nozzle
        lineTo(centerX + 6f, centerY + diverH / 2f)
        lineTo(centerX - 6f, centerY + diverH / 2f)
        lineTo(centerX - diverW / 2f, centerY + diverH * 0.25f)
        close()
    }

    // Glass Barrel Fill
    drawPath(path = barrelPath, color = Color(0x33FFFFFF))

    // 3. Trapped Air Bubble inside Diver! (Expands/Compresses with Boyle's Law!)
    val bubbleHeight = (diverH * 0.55f * bubbleScale).coerceAtLeast(10f)
    val bubbleTop = centerY - diverH / 2f + 10f

    val bubblePath = Path().apply {
        moveTo(centerX - diverW / 2f + 4f, bubbleTop)
        lineTo(centerX + diverW / 2f - 4f, bubbleTop)
        lineTo(centerX + diverW / 2f - 4f, bubbleTop + bubbleHeight)
        // Bubble meniscus curved bottom
        quadraticTo(centerX, bubbleTop + bubbleHeight + 6f, centerX - diverW / 2f + 4f, bubbleTop + bubbleHeight)
        close()
    }

    drawPath(
        path = bubblePath,
        color = Color(0x88FFFFFF)
    )
    drawPath(
        path = bubblePath,
        color = CyanNeon.copy(alpha = 0.7f),
        style = Stroke(width = 2f)
    )

    // Glass outline
    drawPath(
        path = barrelPath,
        color = Color.White.copy(alpha = 0.85f),
        style = Stroke(width = 2.5f)
    )

    // Metal ballast wire wrapped around bottom nozzle (adds weight so it barely floats)
    for (i in 0..2) {
        val wy = centerY + diverH * 0.35f + i * 5f
        drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(centerX - 9f, wy),
            end = Offset(centerX + 9f, wy),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}
