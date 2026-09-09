package com.geosid.simplephysics.ui.experiments.week1.Day3

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.geosid.simplephysics.ui.components.ResetIcon
import com.geosid.simplephysics.ui.components.ResponsiveExperimentContainer
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

data class PuncturePencil(
    val id: Int,
    var x: Float, // horizontal center relative to bag
    var y: Float, // vertical position
    var angleDeg: Float,
    val color: Color,
    var isInserted: Boolean = true,
    var isRemoved: Boolean = false
)

data class WaterLeakParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1f,
    val size: Float = 4f
)

@Composable
fun PencilWaterBagExperiment(
    modifier: Modifier = Modifier
) {
    // Water level from 0f to 1f
    var waterLevel by remember { mutableStateOf(0.78f) }
    var leakActive by remember { mutableStateOf(false) }

    // Active pencils
    val pencils = remember {
        mutableStateListOf(
            PuncturePencil(1, 0.5f, 0.45f, -8f, Color(0xFFFFB300), isInserted = true),
            PuncturePencil(2, 0.5f, 0.60f, 12f, Color(0xFF00E5FF), isInserted = true),
            PuncturePencil(3, 0.5f, 0.72f, -5f, Color(0xFFE040FB), isInserted = true)
        )
    }

    // Leaking particles
    val leakParticles = remember { mutableStateListOf<WaterLeakParticle>() }

    // Wave animation
    val infiniteTransition = rememberInfiniteTransition()
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Simulation loop for water leak
    LaunchedEffect(leakActive, waterLevel) {
        if (leakActive && waterLevel > 0.05f) {
            while (true) {
                withFrameNanos {
                    // Drain water gradually
                    waterLevel = max(0.05f, waterLevel - 0.0015f)

                    // Find removed pencils to spawn leak streams
                    pencils.filter { it.isRemoved }.forEach { p ->
                        if (Random.nextFloat() < 0.7f) {
                            val side = if (Random.nextBoolean()) -1f else 1f
                            leakParticles.add(
                                WaterLeakParticle(
                                    x = p.x + side * 0.2f,
                                    y = p.y,
                                    vx = side * Random.nextFloat() * 6f + side * 3f,
                                    vy = -Random.nextFloat() * 2f
                                )
                            )
                        }
                    }

                    // Update particles
                    val iterator = leakParticles.iterator()
                    while (iterator.hasNext()) {
                        val part = iterator.next()
                        part.x += part.vx * 0.002f
                        part.y += part.vy * 0.002f
                        part.vy += 0.35f // gravity
                        part.alpha -= 0.025f
                        if (part.alpha <= 0f) {
                            iterator.remove()
                        }
                    }
                }
            }
        }
    }

    val insertedCount = pencils.count { it.isInserted && !it.isRemoved }
    val sealStatus = if (leakActive) "⚠️ LEAKING" else if (insertedCount > 0) "✅ 100% SEALED" else "NO PENCILS"

    ResponsiveExperimentContainer(
        modifier = modifier,
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Move last inserted pencil if dragged
                            val active = pencils.lastOrNull { !it.isRemoved }
                            if (active != null) {
                                active.y = (active.y + dragAmount.y / size.height).coerceIn(0.3f, 0.85f)
                                active.angleDeg = (active.angleDeg + dragAmount.x * 0.05f).coerceIn(-25f, 25f)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Dimensions of the bag
                val bagWidth = min(w * 0.75f, 420.dp.toPx())
                val bagHeight = min(h * 0.65f, 540.dp.toPx())
                val bagLeft = (w - bagWidth) / 2f
                val bagTop = h * 0.14f
                val bagBottom = bagTop + bagHeight

                // Draw top suspension bar & clips
                drawSuspensionHanger(bagLeft, bagTop, bagWidth)

                // Draw bag outline & water fill
                drawWaterBag(
                    bagLeft = bagLeft,
                    bagTop = bagTop,
                    bagWidth = bagWidth,
                    bagHeight = bagHeight,
                    waterLevel = waterLevel,
                    wavePhase = wavePhase
                )

                // Draw leak particles
                leakParticles.forEach { p ->
                    val px = bagLeft + p.x * bagWidth
                    val py = bagTop + p.y * bagHeight
                    drawCircle(
                        color = WaterBlue.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                        radius = p.size,
                        center = Offset(px, py)
                    )
                }

                // Draw Pencils
                pencils.forEach { pencil ->
                    if (pencil.isInserted && !pencil.isRemoved) {
                        val centerY = bagTop + pencil.y * bagHeight
                        val centerX = bagLeft + pencil.x * bagWidth
                        drawPiercingPencil(
                            centerX = centerX,
                            centerY = centerY,
                            bagWidth = bagWidth,
                            angleDeg = pencil.angleDeg,
                            pencilColor = pencil.color
                        )

                        // Draw polymer tight seal highlight
                        val sealOffsetLeft = Offset(bagLeft + 15f, centerY)
                        val sealOffsetRight = Offset(bagLeft + bagWidth - 15f, centerY)
                        drawCircle(
                            color = CyanNeon.copy(alpha = 0.8f),
                            radius = 8f,
                            center = sealOffsetLeft,
                            style = Stroke(width = 3f)
                        )
                        drawCircle(
                            color = CyanNeon.copy(alpha = 0.8f),
                            radius = 8f,
                            center = sealOffsetRight,
                            style = Stroke(width = 3f)
                        )
                    } else if (pencil.isRemoved) {
                        // Draw hole with water leak jet
                        val centerY = bagTop + pencil.y * bagHeight
                        drawCircle(
                            color = Color(0xFFFF5252),
                            radius = 7f,
                            center = Offset(bagLeft + 15f, centerY)
                        )
                        drawCircle(
                            color = Color(0xFFFF5252),
                            radius = 7f,
                            center = Offset(bagLeft + bagWidth - 15f, centerY)
                        )
                    }
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                title = "Polymer Stress Monitor",
                items = listOf(
                    "Pencils Pierced" to "$insertedCount",
                    "Seal Integrity" to sealStatus,
                    "Water Volume" to "${(waterLevel * 100).toInt()}%"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Helper instruction banner
                Surface(
                    color = ScienceDarkSurfaceVariant.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (!leakActive)
                            "👆 Drag to tilt pencils, or tap buttons to add pencils / pull them out!"
                        else
                            "💧 Seal broken! Polymer hole cannot close; water drains under gravity!",
                        color = if (leakActive) CoralNeon else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                // Button bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val newId = pencils.size + 1
                            val yPos = 0.35f + (pencils.size % 4) * 0.12f
                            val colors = listOf(Color(0xFFFFB300), Color(0xFF00E5FF), Color(0xFFE040FB), Color(0xFF00E676), Color(0xFFFF5252))
                            pencils.add(
                                PuncturePencil(
                                    id = newId,
                                    x = 0.5f,
                                    y = yPos,
                                    angleDeg = Random.nextFloat() * 20f - 10f,
                                    color = colors[newId % colors.size],
                                    isInserted = true
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = ScienceDarkBg),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text("+ Poke Pencil", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        modifier = Modifier.weight(1.1f),
                        onClick = {
                            val active = pencils.lastOrNull { !it.isRemoved }
                            if (active != null) {
                                active.isRemoved = true
                                leakActive = true
                            }
                        },
                        enabled = pencils.any { !it.isRemoved },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralNeon, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text("Pull Out Pencil ⚡", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = {
                            waterLevel = 0.78f
                            leakActive = false
                            leakParticles.clear()
                            pencils.clear()
                            pencils.addAll(
                                listOf(
                                    PuncturePencil(1, 0.5f, 0.45f, -8f, Color(0xFFFFB300), isInserted = true),
                                    PuncturePencil(2, 0.5f, 0.60f, 12f, Color(0xFF00E5FF), isInserted = true)
                                )
                            )
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(12.dp))
                    ) {
                        ResetIcon(tint = CyanNeon, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    )
}

// Canvas Drawing Helpers
private fun DrawScope.drawSuspensionHanger(bagLeft: Float, bagTop: Float, bagWidth: Float) {
    // Top Rod
    drawLine(
        color = Color(0xFF78909C),
        start = Offset(bagLeft - 20f, bagTop - 25f),
        end = Offset(bagLeft + bagWidth + 20f, bagTop - 25f),
        strokeWidth = 10f,
        cap = StrokeCap.Round
    )

    // Two Hanging Metal Binder Clips
    val clip1X = bagLeft + bagWidth * 0.22f
    val clip2X = bagLeft + bagWidth * 0.78f

    listOf(clip1X, clip2X).forEach { cx ->
        // Clip body
        drawRoundRect(
            color = Color(0xFF263238),
            topLeft = Offset(cx - 16f, bagTop - 25f),
            size = Size(32f, 35f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
        // Silver wire handles
        drawArc(
            color = Color(0xFFCFD8DC),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - 10f, bagTop - 42f),
            size = Size(20f, 24f),
            style = Stroke(width = 3f)
        )
    }
}

private fun DrawScope.drawWaterBag(
    bagLeft: Float,
    bagTop: Float,
    bagWidth: Float,
    bagHeight: Float,
    waterLevel: Float,
    wavePhase: Float
) {
    val cornerR = 24f

    // 1. Draw Transparent Plastic Bag Background
    val bagPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = bagLeft,
                top = bagTop,
                right = bagLeft + bagWidth,
                bottom = bagTop + bagHeight,
                radiusX = cornerR,
                radiusY = cornerR
            )
        )
    }

    drawPath(
        path = bagPath,
        color = BagPlasticFill
    )

    // 2. Draw Wavy Water Fill inside Bag
    val waterTopY = bagTop + bagHeight * (1f - waterLevel)
    val waterPath = Path().apply {
        moveTo(bagLeft, bagTop + bagHeight - cornerR)
        // bottom left corner
        quadraticTo(bagLeft, bagTop + bagHeight, bagLeft + cornerR, bagTop + bagHeight)
        // bottom edge
        lineTo(bagLeft + bagWidth - cornerR, bagTop + bagHeight)
        // bottom right corner
        quadraticTo(bagLeft + bagWidth, bagTop + bagHeight, bagLeft + bagWidth, bagTop + bagHeight - cornerR)
        // right side up to water line
        lineTo(bagLeft + bagWidth, waterTopY)

        // Wavy top surface
        val steps = 20
        val stepW = bagWidth / steps
        for (i in steps downTo 0) {
            val wx = bagLeft + i * stepW
            val wy = waterTopY + sin(wavePhase + i * 0.45f) * 6f
            lineTo(wx, wy)
        }
        close()
    }

    // Water gradient
    drawPath(
        path = waterPath,
        brush = Brush.verticalGradient(
            colors = listOf(WaterBlue.copy(alpha = 0.55f), WaterDeep.copy(alpha = 0.85f)),
            startY = waterTopY,
            endY = bagTop + bagHeight
        )
    )

    // Water surface light reflection line
    val surfacePath = Path().apply {
        val steps = 20
        val stepW = bagWidth / steps
        moveTo(bagLeft, waterTopY + sin(wavePhase) * 6f)
        for (i in 1..steps) {
            val wx = bagLeft + i * stepW
            val wy = waterTopY + sin(wavePhase + i * 0.45f) * 6f
            lineTo(wx, wy)
        }
    }
    drawPath(
        path = surfacePath,
        color = Color(0xCCFFFFFF),
        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
    )

    // Floating bubbles
    drawCircle(
        color = Color(0x66FFFFFF),
        radius = 5f,
        center = Offset(bagLeft + bagWidth * 0.35f, waterTopY + 40f + sin(wavePhase * 2f) * 4f)
    )
    drawCircle(
        color = Color(0x55FFFFFF),
        radius = 7f,
        center = Offset(bagLeft + bagWidth * 0.65f, waterTopY + 80f + cos(wavePhase * 1.5f) * 6f)
    )
    drawCircle(
        color = Color(0x44FFFFFF),
        radius = 4f,
        center = Offset(bagLeft + bagWidth * 0.5f, waterTopY + 130f + sin(wavePhase) * 5f)
    )

    // 3. Zip-lock red/blue zipper strip near top
    val zipperY = bagTop + 24f
    drawLine(
        color = Color(0xFFFF1744).copy(alpha = 0.8f),
        start = Offset(bagLeft + 10f, zipperY),
        end = Offset(bagLeft + bagWidth - 10f, zipperY),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF2979FF).copy(alpha = 0.8f),
        start = Offset(bagLeft + 10f, zipperY + 6f),
        end = Offset(bagLeft + bagWidth - 10f, zipperY + 6f),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // Bag outer border with glossy highlight
    drawPath(
        path = bagPath,
        color = BagPlasticBorder,
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawPiercingPencil(
    centerX: Float,
    centerY: Float,
    bagWidth: Float,
    angleDeg: Float,
    pencilColor: Color
) {
    val pencilLength = bagWidth * 1.45f
    val pencilThickness = 16f
    val rad = angleDeg * (PI / 180f).toFloat()

    val cosA = cos(rad)
    val sinA = sin(rad)

    // Vector along pencil length
    val dx = cosA * (pencilLength / 2f)
    val dy = sinA * (pencilLength / 2f)

    val startTip = Offset(centerX + dx, centerY + dy)
    val endEraser = Offset(centerX - dx, centerY - dy)

    // Normal vector for thickness
    val nx = -sinA * (pencilThickness / 2f)
    val ny = cosA * (pencilThickness / 2f)

    // 1. Draw Wooden Pencil Body
    val bodyTip = Offset(centerX + dx * 0.82f, centerY + dy * 0.82f)
    val bodyEraser = Offset(centerX - dx * 0.85f, centerY - dy * 0.85f)

    val bodyPath = Path().apply {
        moveTo(bodyTip.x + nx, bodyTip.y + ny)
        lineTo(bodyEraser.x + nx, bodyEraser.y + ny)
        lineTo(bodyEraser.x - nx, bodyEraser.y - ny)
        lineTo(bodyTip.x - nx, bodyTip.y - ny)
        close()
    }
    drawPath(path = bodyPath, color = pencilColor)

    // Center faceted stripe
    drawLine(
        color = Color(0x33000000),
        start = bodyTip,
        end = bodyEraser,
        strokeWidth = 3f
    )

    // 2. Sharpened Wood Cone
    val woodConePath = Path().apply {
        moveTo(bodyTip.x + nx, bodyTip.y + ny)
        lineTo(startTip.x, startTip.y)
        lineTo(bodyTip.x - nx, bodyTip.y - ny)
        close()
    }
    drawPath(path = woodConePath, color = Color(0xFFD7CCC8))

    // 3. Graphite Lead Tip
    val graphiteStart = Offset(centerX + dx * 0.94f, centerY + dy * 0.94f)
    val leadPath = Path().apply {
        moveTo(graphiteStart.x + nx * 0.35f, graphiteStart.y + ny * 0.35f)
        lineTo(startTip.x, startTip.y)
        lineTo(graphiteStart.x - nx * 0.35f, graphiteStart.y - ny * 0.35f)
        close()
    }
    drawPath(path = leadPath, color = Color(0xFF212121))

    // 4. Metal Ferrule
    val ferruleEnd = Offset(centerX - dx * 0.92f, centerY - dy * 0.92f)
    val ferrulePath = Path().apply {
        moveTo(bodyEraser.x + nx * 1.05f, bodyEraser.y + ny * 1.05f)
        lineTo(ferruleEnd.x + nx * 1.05f, ferruleEnd.y + ny * 1.05f)
        lineTo(ferruleEnd.x - nx * 1.05f, ferruleEnd.y - ny * 1.05f)
        lineTo(bodyEraser.x - nx * 1.05f, bodyEraser.y - ny * 1.05f)
        close()
    }
    drawPath(path = ferrulePath, color = Color(0xFFB0BEC5))

    // 5. Pink Eraser
    val eraserPath = Path().apply {
        moveTo(ferruleEnd.x + nx, ferruleEnd.y + ny)
        lineTo(endEraser.x + nx, endEraser.y + ny)
        lineTo(endEraser.x - nx, endEraser.y - ny)
        lineTo(ferruleEnd.x - nx, ferruleEnd.y - ny)
        close()
    }
    drawPath(path = eraserPath, color = Color(0xFFFF8A80))
}
