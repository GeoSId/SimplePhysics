package com.geosid.simplephysics.ui.experiments.week1.Day2

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
import androidx.compose.ui.graphics.drawscope.rotate
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

data class LimoneneDroplet(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1f,
    val radius: Float = 3.5f
)

data class RubberShard(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotationDeg: Float,
    var vRot: Float,
    var size: Float,
    var alpha: Float = 1f,
    val color: Color
)

@Composable
fun CitrusBalloonExperiment(
    modifier: Modifier = Modifier
) {
    // Balloon State
    var isPopped by remember { mutableStateOf(false) }
    var balloonColorIndex by remember { mutableStateOf(0) }
    val balloonColors = listOf(BalloonOrange, BalloonCyan, BalloonPurple, BalloonRed)
    val currentColor = balloonColors[balloonColorIndex % balloonColors.size]

    // Inflation scale (0f to 1f)
    var inflationScale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = inflationScale,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)
    )

    // Peel Position normalized
    var peelPos by remember { mutableStateOf(Offset(0.78f, 0.45f)) }
    var isSqueezing by remember { mutableStateOf(false) }

    // Droplets and explosion shards
    val droplets = remember { mutableStateListOf<LimoneneDroplet>() }
    val shards = remember { mutableStateListOf<RubberShard>() }
    var shockwaveRadius by remember { mutableStateOf(0f) }
    var shockwaveAlpha by remember { mutableStateOf(0f) }

    // Gentle balloon hovering bob
    val infiniteTransition = rememberInfiniteTransition()
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Simulation loop
    LaunchedEffect(isPopped, droplets.size, isSqueezing) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
            lastTime = now

            // Update droplets
            val dropIter = droplets.iterator()
            while (dropIter.hasNext()) {
                val d = dropIter.next()
                d.x += d.vx * dt
                d.y += d.vy * dt
                d.alpha -= 0.015f

                // Check collision with balloon
                if (!isPopped) {
                    val balloonCenter = Offset(0.38f, 0.48f)
                    val dx = d.x - balloonCenter.x
                    val dy = d.y - balloonCenter.y
                    val dist = sqrt(dx * dx + dy * dy)

                    // Balloon radius ~ 0.16f normalized
                    if (dist < 0.16f * animatedScale) {
                        // Limonene dissolved the polymer! POP!
                        isPopped = true
                        dropIter.remove()

                        // Trigger explosion shards
                        shockwaveRadius = 10f
                        shockwaveAlpha = 1f
                        shards.clear()
                        for (i in 0 until 28) {
                            val angle = Random.nextFloat() * 2 * PI.toFloat()
                            val speed = Random.nextFloat() * 450f + 150f
                            shards.add(
                                RubberShard(
                                    x = balloonCenter.x,
                                    y = balloonCenter.y,
                                    vx = cos(angle) * speed,
                                    vy = sin(angle) * speed,
                                    rotationDeg = Random.nextFloat() * 360f,
                                    vRot = Random.nextFloat() * 720f - 360f,
                                    size = Random.nextFloat() * 16f + 8f,
                                    color = currentColor
                                )
                            )
                        }
                    }
                }

                if (d.alpha <= 0f) {
                    dropIter.remove()
                }
            }

            // Update explosion shards & shockwave
            if (isPopped) {
                if (shockwaveAlpha > 0f) {
                    shockwaveRadius += 320f * dt
                    shockwaveAlpha = (shockwaveAlpha - 1.6f * dt).coerceAtLeast(0f)
                }

                val shardIter = shards.iterator()
                while (shardIter.hasNext()) {
                    val s = shardIter.next()
                    s.x += (s.vx / 1000f) * dt
                    s.y += (s.vy / 1000f) * dt
                    s.vy += 450f * dt // gravity on shards
                    s.rotationDeg += s.vRot * dt
                    s.alpha -= 0.6f * dt
                    if (s.alpha <= 0f) {
                        shardIter.remove()
                    }
                }
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = if (!isPopped)
            "🍊 Drag the orange peel near the balloon, then press 'Squeeze Peel'!"
        else
            "💥 Limonene oil dissolved the non-polar latex bonds, triggering cataclysmic snap-back!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            peelPos = Offset(
                                (peelPos.x + dragAmount.x / size.width).coerceIn(0.1f, 0.95f),
                                (peelPos.y + dragAmount.y / size.height).coerceIn(0.1f, 0.9f)
                            )
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                val balloonCenter = Offset(w * 0.38f, h * 0.48f + bobOffset)
                val balloonRadiusX = min(w * 0.22f, 110.dp.toPx()) * animatedScale
                val balloonRadiusY = min(h * 0.26f, 140.dp.toPx()) * animatedScale

                // 1. Draw Balloon or Explosion
                if (!isPopped) {
                    drawTautBalloon(
                        center = balloonCenter,
                        radiusX = balloonRadiusX,
                        radiusY = balloonRadiusY,
                        color = currentColor
                    )
                } else {
                    // Draw shockwave ring
                    if (shockwaveAlpha > 0f) {
                        drawCircle(
                            color = Color.White.copy(alpha = shockwaveAlpha),
                            radius = shockwaveRadius,
                            center = balloonCenter,
                            style = Stroke(width = 4f)
                        )
                    }

                    // Draw rubber shards
                    shards.forEach { s ->
                        val sx = s.x * w
                        val sy = s.y * h
                        rotate(s.rotationDeg, pivot = Offset(sx, sy)) {
                            drawRect(
                                color = s.color.copy(alpha = s.alpha.coerceIn(0f, 1f)),
                                topLeft = Offset(sx - s.size / 2f, sy - s.size / 2f),
                                size = Size(s.size, s.size * 0.6f)
                            )
                        }
                    }
                }

                // 2. Draw Limonene Droplets
                droplets.forEach { d ->
                    drawCircle(
                        color = CitrusZest.copy(alpha = d.alpha.coerceIn(0f, 1f)),
                        radius = d.radius,
                        center = Offset(d.x * w, d.y * h)
                    )
                }

                // 3. Draw Citrus Peel
                val peelPx = Offset(peelPos.x * w, peelPos.y * h)
                drawCitrusPeel(
                    center = peelPx,
                    isSqueezing = isSqueezing,
                    targetBalloon = balloonCenter
                )
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Polymer Dissolution Sensor",
                items = listOf(
                    "Balloon Membrane" to if (!isPopped) "Cross-linked Latex (Taut)" else "RUPTURED (Dissolved)",
                    "Chemical Agent" to "d-Limonene C₁₀H₁₆",
                    "Reaction Type" to "Solvent Attack ('Like Dissolves Like')",
                    "Status" to if (!isPopped) "INTACT" else "💥 EXPLODED"
                )
            )
        },
        controlsContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (!isPopped) {
                            isSqueezing = true
                            // Spawn 15-20 droplets aimed toward balloon
                            val balloonCenter = Offset(0.38f, 0.48f)
                            val dirX = balloonCenter.x - peelPos.x
                            val dirY = balloonCenter.y - peelPos.y
                            val len = sqrt(dirX * dirX + dirY * dirY).coerceAtLeast(0.01f)
                            val baseVx = (dirX / len) * 0.85f
                            val baseVy = (dirY / len) * 0.85f

                            for (i in 0 until 18) {
                                val spreadAngle = (Random.nextFloat() - 0.5f) * 0.35f
                                droplets.add(
                                    LimoneneDroplet(
                                        x = peelPos.x,
                                        y = peelPos.y,
                                        vx = baseVx + spreadAngle,
                                        vy = baseVy + (Random.nextFloat() - 0.5f) * 0.2f
                                    )
                                )
                            }
                        }
                    },
                    enabled = !isPopped,
                    colors = ButtonDefaults.buttonColors(containerColor = CitrusOrange, contentColor = ScienceDarkBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🍊 Squeeze Peel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        isPopped = false
                        balloonColorIndex++
                        inflationScale = 0.2f
                        inflationScale = 1.0f
                        droplets.clear()
                        shards.clear()
                        shockwaveAlpha = 0f
                        isSqueezing = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = ScienceDarkBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🎈 New Balloon", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                IconButton(
                    onClick = {
                        isPopped = false
                        inflationScale = 1f
                        droplets.clear()
                        shards.clear()
                        peelPos = Offset(0.78f, 0.45f)
                        isSqueezing = false
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(ScienceDarkSurfaceVariant, RoundedCornerShape(12.dp))
                ) {
                    ResetIcon(tint = CyanNeon, modifier = Modifier.size(20.dp))
                }
            }
        }
    )
}

// Drawing Helpers
private fun DrawScope.drawTautBalloon(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    color: Color
) {
    // 1. Balloon String
    val knotY = center.y + radiusY
    val stringPath = Path().apply {
        moveTo(center.x, knotY)
        quadraticTo(center.x + 15f, knotY + 45f, center.x - 10f, knotY + 90f)
        quadraticTo(center.x + 20f, knotY + 130f, center.x, knotY + 160f)
    }
    drawPath(
        path = stringPath,
        color = Color(0x88FFFFFF),
        style = Stroke(width = 2f, cap = StrokeCap.Round)
    )

    // 2. Balloon Oval Body
    val bodyPath = Path().apply {
        // Oval with slightly tapered bottom
        addOval(
            androidx.compose.ui.geometry.Rect(
                center = center,
                radius = radiusX
            )
        )
    }

    // 3D Spherical Radial Gradient
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                color,
                color.copy(red = color.red * 0.6f, green = color.green * 0.6f, blue = color.blue * 0.6f)
            ),
            center = Offset(center.x - radiusX * 0.35f, center.y - radiusY * 0.35f),
            radius = radiusX * 1.3f
        ),
        topLeft = Offset(center.x - radiusX, center.y - radiusY),
        size = Size(radiusX * 2f, radiusY * 2f)
    )

    // Specular Highlight Arc
    drawArc(
        color = Color.White.copy(alpha = 0.55f),
        startAngle = 200f,
        sweepAngle = 60f,
        useCenter = false,
        topLeft = Offset(center.x - radiusX * 0.85f, center.y - radiusY * 0.85f),
        size = Size(radiusX * 1.7f, radiusY * 1.7f),
        style = Stroke(width = 6f, cap = StrokeCap.Round)
    )

    // 3. Balloon Knot at Bottom
    val knotPath = Path().apply {
        moveTo(center.x - 8f, knotY)
        lineTo(center.x + 8f, knotY)
        lineTo(center.x + 12f, knotY + 14f)
        lineTo(center.x - 12f, knotY + 14f)
        close()
    }
    drawPath(path = knotPath, color = color)
}

private fun DrawScope.drawCitrusPeel(
    center: Offset,
    isSqueezing: Boolean,
    targetBalloon: Offset
) {
    // Compute angle facing the balloon
    val angle = atan2(targetBalloon.y - center.y, targetBalloon.x - center.x)

    rotate(degrees = (angle * 180f / PI).toFloat(), pivot = center) {
        val arcR = 48f
        val bendFactor = if (isSqueezing) 1.4f else 1.0f

        // Orange Outer Rind (Curved arc)
        val rindPath = Path().apply {
            moveTo(-arcR, -20f * bendFactor)
            quadraticTo(0f, 30f * bendFactor, arcR, -20f * bendFactor)
            lineTo(arcR - 8f, -28f * bendFactor)
            quadraticTo(0f, 18f * bendFactor, -arcR + 8f, -28f * bendFactor)
            close()
        }
        drawPath(path = rindPath, color = CitrusOrange)

        // White Pith Layer inside
        val pithPath = Path().apply {
            moveTo(-arcR + 8f, -28f * bendFactor)
            quadraticTo(0f, 18f * bendFactor, arcR - 8f, -28f * bendFactor)
            lineTo(arcR - 14f, -34f * bendFactor)
            quadraticTo(0f, 10f * bendFactor, -arcR + 14f, -34f * bendFactor)
            close()
        }
        drawPath(path = pithPath, color = Color(0xFFFFF9C4))

        // Pores on outer zest
        drawCircle(color = Color(0xFFE65100), radius = 2f, center = Offset(-18f, 5f * bendFactor))
        drawCircle(color = Color(0xFFE65100), radius = 2f, center = Offset(0f, 15f * bendFactor))
        drawCircle(color = Color(0xFFE65100), radius = 2f, center = Offset(18f, 5f * bendFactor))
    }
}
