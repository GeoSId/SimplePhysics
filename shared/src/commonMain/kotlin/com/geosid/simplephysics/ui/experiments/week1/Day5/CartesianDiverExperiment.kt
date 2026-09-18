package com.geosid.simplephysics.ui.experiments.week1.Day5

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.ui.components.PhysicsSliderControl
import com.geosid.simplephysics.ui.components.ResetIcon
import com.geosid.simplephysics.ui.components.ResponsiveExperimentContainer
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*

@Composable
fun CartesianDiverExperiment(
    modifier: Modifier = Modifier
) {
    // Bottle Squeeze Pressure: 1.0 atm (ambient) to 3.5 atm (heavily squeezed)
    var appliedPressureAtm by remember { mutableStateOf(1.0f) }
    var targetPressureAtm by remember { mutableStateOf(1.0f) }
    var isPressureLocked by remember { mutableStateOf(false) }
    var isTouchingBottle by remember { mutableStateOf(false) }

    // Diver Dynamic Physics
    // Depth from 0.12f (surface) to 0.86f (bottom)
    var diverDepth by remember { mutableStateOf(0.12f) }
    var diverVelocityY by remember { mutableStateOf(0f) }

    // Boyle's Law: V = V0 * (P0 / P)
    val bubbleVolumeFraction = (1.0f / appliedPressureAtm).coerceIn(0.28f, 1.0f)

    // Archimedes' Principle:
    // Neutral buoyancy occurs when Fb = W (around P = 1.65 atm)
    val neutralVolume = 1.0f / 1.65f
    // Net force: positive = upwards (accelerates to top), negative = downwards (sinks)
    val netBuoyancyForce = (bubbleVolumeFraction - neutralVolume) * 18f

    // Smooth elastic pressure response and physics integration loop
    LaunchedEffect(isPressureLocked) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
            lastTime = now

            // If not locked and user isn't actively touching, bottle returns to ambient 1.0 atm
            if (!isPressureLocked && !isTouchingBottle) {
                targetPressureAtm = 1.0f
            }

            // Smooth spring return for pliable plastic bottle
            appliedPressureAtm += (targetPressureAtm - appliedPressureAtm) * min(1f, dt * 10f)

            // Dynamic diver physics
            val accelerationY = -netBuoyancyForce
            val damping = 3.2f

            diverVelocityY += (accelerationY - damping * diverVelocityY) * dt
            diverDepth = (diverDepth + diverVelocityY * dt).coerceIn(0.12f, 0.86f)

            // Boundary stops
            if (diverDepth <= 0.12f && diverVelocityY < 0f) {
                diverVelocityY = 0f
            } else if (diverDepth >= 0.86f && diverVelocityY > 0f) {
                diverVelocityY = 0f
            }
        }
    }

    // Dynamic micro-bubbles animation
    val infiniteTransition = rememberInfiniteTransition()
    val bubbleAnimPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val buoyancyStatus = when {
        netBuoyancyForce > 0.6f -> "⬆️ FLOATING (Fb > W)"
        netBuoyancyForce < -0.6f -> "⬇️ SINKING (W > Fb)"
        else -> "⚖️ HOVERING (Fb ≈ W)"
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "💧 Press and drag down on the bottle to squeeze! Watch the air bubble compress via Boyle's Law (P₁V₁ = P₂V₂) and make the diver sink.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isTouchingBottle = true
                            },
                            onDragEnd = {
                                isTouchingBottle = false
                            },
                            onDragCancel = {
                                isTouchingBottle = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // Dragging down increases pressure (squeezing bottle)
                                targetPressureAtm = (targetPressureAtm + dragAmount.y * 0.012f).coerceIn(1.0f, 3.5f)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isTouchingBottle = true
                                targetPressureAtm = 2.8f
                                tryAwaitRelease()
                                isTouchingBottle = false
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height

                // Bottle dimensions
                val bottleW = min(w * 0.42f, 230.dp.toPx())
                val bottleH = min(h * 0.86f, 520.dp.toPx())
                val bottleLeft = (w - bottleW) / 2f
                val bottleTop = h * 0.07f
                val bottleBottom = bottleTop + bottleH

                // Inward deformation factor from squeeze pressure
                val squeezeIndent = (appliedPressureAtm - 1.0f) * 14f

                // 1. Coordinate Grid Background
                drawScientificGrid(w, h)

                // 2. Floor Shadow under Bottle
                drawOval(
                    color = Color.Black.copy(alpha = 0.4f),
                    topLeft = Offset(bottleLeft + 12f, bottleBottom - 8f),
                    size = Size(bottleW - 24f, 18f)
                )

                // 3. Water inside the Bottle with depth gradient
                drawBottleWater(
                    left = bottleLeft,
                    top = bottleTop,
                    width = bottleW,
                    height = bottleH,
                    squeezeIndent = squeezeIndent
                )

                // 4. Diver Y Position
                val diverY = bottleTop + (bottleH * diverDepth)
                val diverX = w * 0.5f

                // 5. Rising Micro Bubbles around Diver
                drawRisingMicroBubbles(
                    centerX = diverX,
                    startY = diverY,
                    animPhase = bubbleAnimPhase,
                    isSinking = netBuoyancyForce < -0.5f
                )

                // 6. Cartesian Diver Body & Trapped Compressed Air Bubble
                drawCartesianDiver(
                    centerX = diverX,
                    centerY = diverY,
                    bubbleScale = bubbleVolumeFraction
                )

                // 7. Dynamic Force Vectors (Archimedes Fb vs Gravity W)
                drawForceVectors(
                    centerX = diverX + 28f,
                    centerY = diverY,
                    buoyancyFraction = bubbleVolumeFraction,
                    netBuoyancy = netBuoyancyForce
                )

                // 8. Outer Pliable Plastic Bottle Shell, Cap, & Grip Highlights
                drawBottlePlasticShell(
                    left = bottleLeft,
                    top = bottleTop,
                    width = bottleW,
                    height = bottleH,
                    squeezeIndent = squeezeIndent,
                    isSqueezing = appliedPressureAtm > 1.2f
                )
            }
        },
        hudContent = {
            TransparentTelemetryHud(
                modifier = Modifier.fillMaxWidth(),
                title = "Pascal & Boyle Telemetry",
                items = listOf(
                    "Internal Pressure (P)" to "${round(appliedPressureAtm * 10) / 10f} atm (${(appliedPressureAtm * 101.3f).toInt()} kPa)",
                    "Air Bubble Volume (V)" to "${(bubbleVolumeFraction * 100).toInt()}% (Boyle's Law)",
                    "Buoyant Force (Fb)" to if (netBuoyancyForce > 0.6f) "Fb > W (Net Upward)" else if (netBuoyancyForce < -0.6f) "Fb < W (Net Downward)" else "Fb ≈ W (Equilibrium)",
                    "Diver Status" to buoyancyStatus
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preset Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Release (1.0 atm)
                    FilterChip(
                        selected = abs(targetPressureAtm - 1.0f) < 0.1f,
                        onClick = {
                            targetPressureAtm = 1.0f
                            isPressureLocked = false
                        },
                        label = { Text("🌊 Float (1.0 atm)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.20f),
                            selectedLabelColor = CyanNeon,
                            containerColor = ScienceDarkSurfaceVariant.copy(alpha = 0.5f),
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = abs(targetPressureAtm - 1.0f) < 0.1f,
                            borderColor = ScienceBorder.copy(alpha = 0.4f),
                            selectedBorderColor = CyanNeon
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )

                    // Neutral Hover (1.65 atm)
                    FilterChip(
                        selected = abs(targetPressureAtm - 1.65f) < 0.15f,
                        onClick = {
                            targetPressureAtm = 1.65f
                            isPressureLocked = true
                        },
                        label = { Text("⚖️ Hover (1.65 atm)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberVibrant.copy(alpha = 0.20f),
                            selectedLabelColor = AmberVibrant,
                            containerColor = ScienceDarkSurfaceVariant.copy(alpha = 0.5f),
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = abs(targetPressureAtm - 1.65f) < 0.15f,
                            borderColor = ScienceBorder.copy(alpha = 0.4f),
                            selectedBorderColor = AmberVibrant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )

                    // Full Squeeze (2.8 atm)
                    FilterChip(
                        selected = abs(targetPressureAtm - 2.8f) < 0.2f,
                        onClick = {
                            targetPressureAtm = 2.8f
                            isPressureLocked = true
                        },
                        label = { Text("✊ Sink (2.8 atm)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralNeon.copy(alpha = 0.20f),
                            selectedLabelColor = CoralNeon,
                            containerColor = ScienceDarkSurfaceVariant.copy(alpha = 0.5f),
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = abs(targetPressureAtm - 2.8f) < 0.2f,
                            borderColor = ScienceBorder.copy(alpha = 0.4f),
                            selectedBorderColor = CoralNeon
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Squeeze Pressure Slider
                PhysicsSliderControl(
                    title = "Hydrostatic Squeeze Pressure",
                    value = appliedPressureAtm,
                    range = 1.0f..3.5f,
                    valueDisplay = "${round(appliedPressureAtm * 10) / 10f} atm",
                    accentColor = if (appliedPressureAtm > 1.8f) CoralNeon else CyanNeon,
                    onValueChange = {
                        targetPressureAtm = it
                        isPressureLocked = true
                    }
                )

                // Actions & Reset Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isPressureLocked = !isPressureLocked
                            if (!isPressureLocked) targetPressureAtm = 1.0f
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPressureLocked) AmberVibrant else CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isPressureLocked) "🔓 Pressure Locked" else "🔄 Spring Rebound Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            targetPressureAtm = 1.0f
                            appliedPressureAtm = 1.0f
                            diverVelocityY = 0f
                            isPressureLocked = false
                            isTouchingBottle = false
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(10.dp))
                    ) {
                        ResetIcon(tint = CyanNeon, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    )
}

// ---------------------------------------------------------------------------
// High-Fidelity Drawing Helpers
// ---------------------------------------------------------------------------

/**
 * Real-time Free-Body Diagram Vectors:
 * Cyan Upward Arrow = Buoyancy Fb (length proportional to bubble volume)
 * Amber Downward Arrow = Weight W (constant)
 */
private fun DrawScope.drawForceVectors(
    centerX: Float,
    centerY: Float,
    buoyancyFraction: Float,
    netBuoyancy: Float
) {
    val weightLen = 30f // W = mg (fixed baseline)
    val fbLen = (weightLen * (buoyancyFraction / (1.0f / 1.65f))).coerceIn(10f, 52f)

    // 1. Upward Buoyant Force Vector (Fb)
    val fbStart = Offset(centerX, centerY - 2f)
    val fbEnd = Offset(centerX, centerY - 2f - fbLen)
    drawLine(
        color = CyanNeon,
        start = fbStart,
        end = fbEnd,
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
    val fbHead = Path().apply {
        moveTo(fbEnd.x, fbEnd.y - 5f)
        lineTo(fbEnd.x - 4f, fbEnd.y)
        lineTo(fbEnd.x + 4f, fbEnd.y)
        close()
    }
    drawPath(fbHead, CyanNeon)

    // 2. Downward Weight Vector (W = mg)
    val wStart = Offset(centerX, centerY + 2f)
    val wEnd = Offset(centerX, centerY + 2f + weightLen)
    drawLine(
        color = AmberVibrant,
        start = wStart,
        end = wEnd,
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
    val wHead = Path().apply {
        moveTo(wEnd.x, wEnd.y + 5f)
        lineTo(wEnd.x - 4f, wEnd.y)
        lineTo(wEnd.x + 4f, wEnd.y)
        close()
    }
    drawPath(wHead, AmberVibrant)
}

/**
 * Draws water volume inside the pliable bottle with squeeze deformation
 */
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
            colors = listOf(
                Color(0xFF00ACC1).copy(alpha = 0.35f),
                Color(0xFF0288D1).copy(alpha = 0.55f),
                Color(0xFF01579B).copy(alpha = 0.75f)
            ),
            startY = top + neckH,
            endY = top + height
        )
    )

    // Water surface meniscus
    val surfaceY = top + neckH + 4f
    drawLine(
        color = Color.White.copy(alpha = 0.65f),
        start = Offset(left + (width - neckW) / 2f + 2f, surfaceY),
        end = Offset(left + (width + neckW) / 2f - 2f, surfaceY),
        strokeWidth = 2f
    )
}

/**
 * Draws outer plastic bottle shell with ribs and blue cap
 */
private fun DrawScope.drawBottlePlasticShell(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    squeezeIndent: Float,
    isSqueezing: Boolean
) {
    val cornerR = 26f
    val neckW = width * 0.35f
    val neckH = 45f

    // Bottle Outline
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

    // Outer plastic rim
    drawPath(
        path = path,
        color = if (isSqueezing) CoralNeon.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f),
        style = Stroke(width = if (isSqueezing) 3.5f else 2.5f)
    )

    // Bottle Rib Lines
    for (i in 1..3) {
        val ry = top + height * (0.42f + i * 0.12f)
        val inset = if (i == 2) squeezeIndent * 1.4f else squeezeIndent * 0.8f
        drawLine(
            color = Color.White.copy(alpha = 0.25f),
            start = Offset(left + 8f + inset, ry),
            end = Offset(left + width - 8f - inset, ry),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
    }

    // Screw Cap
    val capW = neckW + 8f
    val capH = 22f
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1565C0))),
        topLeft = Offset(left + (width - capW) / 2f, top - 6f),
        size = Size(capW, capH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )
    for (i in 1..4) {
        val gx = left + (width - capW) / 2f + i * (capW / 5f)
        drawLine(
            color = Color(0xFF0D47A1),
            start = Offset(gx, top - 6f),
            end = Offset(gx, top - 6f + capH),
            strokeWidth = 1.8f
        )
    }
}

/**
 * Glass Eyedropper Diver with Trapped Air Bubble
 */
private fun DrawScope.drawCartesianDiver(
    centerX: Float,
    centerY: Float,
    bubbleScale: Float
) {
    val diverW = 32f
    val diverH = 75f

    // 1. Red Rubber Bulb on Top
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F), Color(0xFF850000)),
            center = Offset(centerX - 3f, centerY - diverH / 2f - 3f),
            radius = 16f
        ),
        radius = 12f,
        center = Offset(centerX, centerY - diverH / 2f)
    )
    // Rubber bulb neck collar
    drawRect(
        color = Color(0xFFB71C1C),
        topLeft = Offset(centerX - 7f, centerY - diverH / 2f + 8f),
        size = Size(14f, 4f)
    )

    // 2. Clear Glass Barrel
    val barrelPath = Path().apply {
        moveTo(centerX - diverW / 2f, centerY - diverH / 2f + 10f)
        lineTo(centerX + diverW / 2f, centerY - diverH / 2f + 10f)
        lineTo(centerX + diverW / 2f, centerY + diverH * 0.25f)
        // Taper down to nozzle
        lineTo(centerX + 6f, centerY + diverH / 2f)
        lineTo(centerX - 6f, centerY + diverH / 2f)
        lineTo(centerX - diverW / 2f, centerY + diverH * 0.25f)
        close()
    }

    // Glass Fill
    drawPath(path = barrelPath, color = Color(0x33FFFFFF))

    // 3. Trapped Air Bubble inside Diver (Compresses according to Boyle's Law)
    val bubbleHeight = (diverH * 0.55f * bubbleScale).coerceAtLeast(8f)
    val bubbleTop = centerY - diverH / 2f + 12f

    val bubblePath = Path().apply {
        moveTo(centerX - diverW / 2f + 4f, bubbleTop)
        lineTo(centerX + diverW / 2f - 4f, bubbleTop)
        lineTo(centerX + diverW / 2f - 4f, bubbleTop + bubbleHeight)
        // Meniscus curve
        quadraticTo(centerX, bubbleTop + bubbleHeight + 5f, centerX - diverW / 2f + 4f, bubbleTop + bubbleHeight)
        close()
    }

    drawPath(
        path = bubblePath,
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.9f), CyanNeon.copy(alpha = 0.7f)),
            startY = bubbleTop,
            endY = bubbleTop + bubbleHeight
        )
    )

    // Glass specular reflection highlight
    drawLine(
        color = Color.White.copy(alpha = 0.65f),
        start = Offset(centerX - diverW / 2f + 3f, centerY - diverH / 2f + 14f),
        end = Offset(centerX - diverW / 2f + 3f, centerY + diverH * 0.20f),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )

    // Glass Barrel Outline
    drawPath(
        path = barrelPath,
        color = Color.White.copy(alpha = 0.85f),
        style = Stroke(width = 2f)
    )

    // Brass/Copper Ballast Wire Wrapped around Nozzle (adds weight so it barely floats)
    for (i in 0..3) {
        val wy = centerY + diverH * 0.32f + i * 4.5f
        drawLine(
            color = Color(0xFFFFB300),
            start = Offset(centerX - 8f, wy),
            end = Offset(centerX + 8f, wy),
            strokeWidth = 2.8f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Animated micro bubbles streaming from diver bottom
 */
private fun DrawScope.drawRisingMicroBubbles(
    centerX: Float,
    startY: Float,
    animPhase: Float,
    isSinking: Boolean
) {
    val count = 4
    for (i in 0 until count) {
        val progress = (animPhase + i * (1f / count)) % 1f
        val by = startY + 30f - progress * 70f
        val bx = centerX + sin(progress * PI.toFloat() * 4f + i) * 6f
        val r = 1.5f + progress * 2f
        drawCircle(
            color = Color.White.copy(alpha = (1f - progress) * (if (isSinking) 0.6f else 0.3f)),
            radius = r,
            center = Offset(bx, by)
        )
    }
}

/**
 * Coordinate Grid Background
 */
private fun DrawScope.drawScientificGrid(w: Float, h: Float) {
    val step = 36f
    var x = 0f
    while (x < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.12f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 0.6f
        )
        x += step
    }
    var y = 0f
    while (y < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.12f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 0.6f
        )
        y += step
    }
}

/**
 * Completely Transparent Telemetry HUD
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
                text = "💧 $title".uppercase(),
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
                    text = "BOYLE'S LAW",
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
