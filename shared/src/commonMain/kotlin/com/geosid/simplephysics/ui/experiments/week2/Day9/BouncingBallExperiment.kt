package com.geosid.simplephysics.ui.experiments.week2.Day9

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
fun BouncingBallExperiment(
    modifier: Modifier = Modifier
) {
    // Physical Parameters
    var restitution by remember { mutableStateOf(0.82f) } // e
    var airDragK by remember { mutableStateOf(0.04f) } // Viscous drag k
    val mass = 0.5f // kg
    val gravity = 9.81f // m/s^2

    // Dynamic State
    var ballY by remember { mutableStateOf(6.5f) } // Current height in meters (0 is floor)
    var ballVelocityY by remember { mutableStateOf(0f) } // m/s
    var isDragging by remember { mutableStateOf(false) }
    var bounceCount by remember { mutableStateOf(0) }
    var maxHeightReached by remember { mutableStateOf(6.5f) }

    // Energy
    val potentialEnergy = (mass * gravity * ballY).coerceAtLeast(0f)
    val kineticEnergy = (0.5f * mass * ballVelocityY * ballVelocityY)
    val totalEnergy = potentialEnergy + kineticEnergy

    // Simulation loop
    LaunchedEffect(isDragging, restitution, airDragK) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.035f)
            lastTime = now

            if (!isDragging) {
                // Sub-stepping
                val subSteps = 6
                val subDt = dt / subSteps

                for (i in 0 until subSteps) {
                    // Air drag: F_drag = -k * v * |v|
                    val dragForce = -airDragK * ballVelocityY * abs(ballVelocityY)
                    val accel = -gravity + (dragForce / mass)

                    ballVelocityY += accel * subDt
                    ballY += ballVelocityY * subDt

                    // Floor Collision
                    if (ballY <= 0f) {
                        ballY = 0f
                        if (abs(ballVelocityY) > 0.25f) {
                            // Rebound with coefficient of restitution
                            ballVelocityY = -ballVelocityY * restitution
                            bounceCount++
                        } else {
                            // At rest
                            ballVelocityY = 0f
                        }
                    }
                }
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👆 Drag the ball up and release to drop from any height, or adjust restitution below!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                ballVelocityY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // Convert drag px to meters (synchronized with elevated canvas origin)
                                val meterSpanPx = size.height * 0.44f
                                val pxPerMeter = meterSpanPx / 8.5f
                                ballY = (ballY - dragAmount.y / pxPerMeter).coerceIn(0f, 8.5f)
                                maxHeightReached = max(maxHeightReached, ballY)
                            },
                            onDragEnd = {
                                isDragging = false
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated Canvas Origin: floor at 0.58h so bottom 42% is completely clear for controls
                val floorY = h * 0.58f
                val maxMeterHeight = 8.5f
                val meterSpanPx = h * 0.44f
                val pxPerMeter = meterSpanPx / maxMeterHeight
                val ballCenterX = w * 0.35f
                val ballRadiusPx = min(w * 0.055f, h * 0.038f).coerceIn(18f, 28f)

                // 1. Draw Floor & Laboratory Surface
                drawLine(
                    color = ScienceBorder,
                    start = Offset(0f, floorY),
                    end = Offset(w, floorY),
                    strokeWidth = 3f
                )
                drawRect(
                    color = Color(0x10FFFFFF),
                    topLeft = Offset(0f, floorY),
                    size = Size(w, h - floorY)
                )

                // 2. Draw Height Measurement Ruler
                val rulerX = ballCenterX - (ballRadiusPx * 2.2f + 16f)
                drawLine(Color(0x55FFFFFF), Offset(rulerX, floorY), Offset(rulerX, floorY - 8f * pxPerMeter), 1.5f)
                for (m in 0..8) {
                    val my = floorY - m * pxPerMeter
                    val isMajor = m % 2 == 0
                    val tickW = if (isMajor) 16f else 10f
                    drawLine(
                        color = if (isMajor) CyanNeon else Color(0x88FFFFFF),
                        start = Offset(rulerX - tickW, my),
                        end = Offset(rulerX, my),
                        strokeWidth = if (isMajor) 2f else 1.2f
                    )
                }

                // 3. Draw Floor Contact Shadow (Expands and darkens as ball nears the floor)
                val shadowAlpha = (1f - (ballY / 4f)).coerceIn(0.08f, 0.70f)
                val shadowWidth = (ballRadiusPx * 2.2f * (1f + ballY * 0.15f)).coerceAtMost(w * 0.25f)
                drawOval(
                    color = Color.Black.copy(alpha = shadowAlpha),
                    topLeft = Offset(ballCenterX - shadowWidth / 2f, floorY - 4f),
                    size = Size(shadowWidth, 8f)
                )

                // 4. Ball Squash & Stretch Deformation
                val isImpact = ballY <= 0.05f && abs(ballVelocityY) > 0.5f
                val squashFactor = if (isImpact) {
                    (abs(ballVelocityY) / 14f).coerceIn(0.1f, 0.40f)
                } else {
                    0f
                }

                val currentRadiusX = ballRadiusPx * (1f + squashFactor * 0.8f)
                val currentRadiusY = ballRadiusPx * (1f - squashFactor)

                val ballCenterY = floorY - (ballY * pxPerMeter) - currentRadiusY

                // Impact Shockwave Ring on hard contact
                if (isImpact) {
                    drawCircle(
                        color = AmberVibrant.copy(alpha = 0.65f),
                        radius = ballRadiusPx * 1.5f,
                        center = Offset(ballCenterX, floorY),
                        style = Stroke(width = 2f)
                    )
                }

                // Draw Ball with high-specular 3D shader
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, AmberVibrant, Color(0xFFE65100), Color(0xFF3E2723)),
                        center = Offset(ballCenterX - 6f, ballCenterY - 6f),
                        radius = currentRadiusX * 1.2f
                    ),
                    topLeft = Offset(ballCenterX - currentRadiusX, ballCenterY - currentRadiusY),
                    size = Size(currentRadiusX * 2f, currentRadiusY * 2f)
                )

                // Draw Max Height Ghost Apex Marker
                if (maxHeightReached > 0.5f) {
                    val ghostY = floorY - maxHeightReached * pxPerMeter - ballRadiusPx
                    drawLine(
                        color = CyanNeon.copy(alpha = 0.5f),
                        start = Offset(ballCenterX - 35f, ghostY),
                        end = Offset(ballCenterX + 35f, ghostY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                    )
                }

                // 5. Draw Mechanical Energy Split Bar on Right (PE vs KE vs Total)
                val energyBarH = min(130f, h * 0.22f)
                drawEnergyBars(
                    x = w * 0.68f,
                    y = floorY - energyBarH - 25f,
                    kinetic = kineticEnergy,
                    potential = potentialEnergy,
                    total = totalEnergy,
                    maxHeightPx = energyBarH
                )
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Kinematics & Energy",
                items = listOf(
                    "Height (y)" to "${round(ballY * 100) / 100f} m",
                    "Speed (|v|)" to "${round(abs(ballVelocityY) * 10) / 10f} m/s",
                    "Bounces" to "$bounceCount",
                    "Total Energy" to "${round(totalEnergy * 10) / 10f} J",
                    "Energy Loss / Bounce" to "${((1f - restitution * restitution) * 100).toInt()}%"
                ),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f)
            )
        },
        controlsContent = {
            Surface(
                color = ScienceDarkSurfaceVariant.copy(alpha = 0.94f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Compact Preset Chips (32.dp height)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Superball" to 0.92f,
                            "Tennis" to 0.75f,
                            "Wood" to 0.50f,
                            "Putty" to 0.15f
                        ).forEach { (name, eVal) ->
                            val isSel = abs(restitution - eVal) < 0.02f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AmberVibrant else ScienceDarkSurface)
                                    .border(
                                        1.dp,
                                        if (isSel) AmberVibrant else ScienceBorder.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { restitution = eVal },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) ScienceDarkBg else TextPrimary
                                )
                            }
                        }
                    }

                    // Paired Sliders side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PhysicsSliderControl(
                            modifier = Modifier.weight(1f),
                            title = "Restitution (e)",
                            value = restitution,
                            range = 0.05f..0.98f,
                            valueDisplay = "e = ${round(restitution * 100) / 100f}",
                            accentColor = AmberVibrant,
                            onValueChange = { restitution = it }
                        )

                        PhysicsSliderControl(
                            modifier = Modifier.weight(1f),
                            title = "Air Drag (k)",
                            value = airDragK,
                            range = 0f..0.15f,
                            valueDisplay = "${round(airDragK * 100) / 100f}",
                            accentColor = CyanNeon,
                            onValueChange = { airDragK = it }
                        )
                    }

                    // Compact Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                ballY = 7.0f
                                ballVelocityY = 0f
                                bounceCount = 0
                            },
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberVibrant, contentColor = ScienceDarkBg),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🏀 Drop Ball (7m)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                ballY = 6.5f
                                ballVelocityY = 0f
                                bounceCount = 0
                                restitution = 0.82f
                                airDragK = 0.04f
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(ScienceDarkSurface, RoundedCornerShape(8.dp))
                                .border(1.dp, ScienceBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            ResetIcon(tint = CyanNeon, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    )
}

// Energy Bar Graphs in Compose Canvas
private fun DrawScope.drawEnergyBars(
    x: Float,
    y: Float,
    kinetic: Float,
    potential: Float,
    total: Float,
    maxHeightPx: Float = 120f
) {
    val barW = 20f
    val gap = 8f
    val cardW = barW * 3 + gap * 2 + 24f
    val cardH = maxHeightPx + 36f
    val maxEnergy = 45f // Scale factor

    // Background Card
    drawRoundRect(
        color = ScienceDarkSurface.copy(alpha = 0.85f),
        topLeft = Offset(x - 12f, y - 12f),
        size = Size(cardW, cardH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
    )
    drawRoundRect(
        color = ScienceBorder.copy(alpha = 0.45f),
        topLeft = Offset(x - 12f, y - 12f),
        size = Size(cardW, cardH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
        style = Stroke(width = 1f)
    )

    // Bar 1: Potential Energy (Cyan)
    val epH = (potential / maxEnergy * maxHeightPx).coerceIn(0f, maxHeightPx)
    drawRoundRect(
        color = CyanNeon,
        topLeft = Offset(x, y + maxHeightPx - epH),
        size = Size(barW, epH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
    )

    // Bar 2: Kinetic Energy (Emerald)
    val ekH = (kinetic / maxEnergy * maxHeightPx).coerceIn(0f, maxHeightPx)
    drawRoundRect(
        color = EmeraldNeon,
        topLeft = Offset(x + barW + gap, y + maxHeightPx - ekH),
        size = Size(barW, ekH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
    )

    // Bar 3: Total Energy (Amber)
    val etotH = (total / maxEnergy * maxHeightPx).coerceIn(0f, maxHeightPx)
    drawRoundRect(
        color = AmberVibrant,
        topLeft = Offset(x + (barW + gap) * 2f, y + maxHeightPx - etotH),
        size = Size(barW, etotH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
    )
}

