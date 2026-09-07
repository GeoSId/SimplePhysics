package com.geosid.simplephysics.ui.experiments.week1.Day1

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
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

@Composable
fun StaticStrawExperiment(
    modifier: Modifier = Modifier
) {
    // Wand position normalized (0f to 1f)
    var wandPos by remember { mutableStateOf(Offset(0.72f, 0.38f)) }

    // Charge level on wand (-100% to 0% to +100%)
    var wandCharge by remember { mutableStateOf(-0.85f) } // Negative static electrons by default

    // Straw dynamic rotational physics state
    var strawAngleRad by remember { mutableStateOf(0.3f) }
    var strawAngularVelocity by remember { mutableStateOf(0f) }

    // Wool rubbing animation indicator
    var isRubbing by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val sparkPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Physics update frame loop
    LaunchedEffect(wandPos, wandCharge) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
            lastTime = now

            // Pivot of straw is at screen center (approx x=0.5, y=0.5)
            val pivot = Offset(0.5f, 0.52f)
            val dx = wandPos.x - pivot.x
            val dy = wandPos.y - pivot.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(0.08f)

            // Target angle towards wand
            val targetAngle = atan2(dy, dx)

            // Straw is symmetric (two ends), so choose closest end (targetAngle or targetAngle + PI)
            var diff1 = (targetAngle - strawAngleRad) % (2 * PI.toFloat())
            if (diff1 > PI) diff1 -= (2 * PI).toFloat()
            if (diff1 < -PI) diff1 += (2 * PI).toFloat()

            var diff2 = (targetAngle + PI.toFloat() - strawAngleRad) % (2 * PI.toFloat())
            if (diff2 > PI) diff2 -= (2 * PI).toFloat()
            if (diff2 < -PI) diff2 += (2 * PI).toFloat()

            val angularDiff = if (abs(diff1) < abs(diff2)) diff1 else diff2

            // Electrostatic Torque: tau = k * Q * sin(angularDiff) / (dist^2)
            val chargeMag = abs(wandCharge)
            val torqueStrength = 4.5f * chargeMag / (dist * dist * 12f + 1f)
            val torque = torqueStrength * angularDiff

            // Rotational damping / air resistance
            val damping = 2.8f

            // Angular acceleration: alpha = torque - damping * omega
            val alpha = torque - damping * strawAngularVelocity
            strawAngularVelocity += alpha * dt
            strawAngleRad += strawAngularVelocity * dt
        }
    }

    val chargePercent = (abs(wandCharge) * 100).toInt()
    val polarityStr = if (wandCharge < -0.05f) "Negative (- electrons)" else if (wandCharge > 0.05f) "Positive (+ ions)" else "Neutral (Discharged)"
    val distPx = sqrt((wandPos.x - 0.5f).pow(2) + (wandPos.y - 0.52f).pow(2))
    val forceLevel = if (chargePercent == 0) "0 N" else "${round(chargePercent / (distPx * 10 + 1)) / 10f} mN"

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag the wand around the bottle! Notice how the straw tracks it without touching.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            wandPos = Offset(
                                (wandPos.x + dragAmount.x / size.width).coerceIn(0.05f, 0.95f),
                                (wandPos.y + dragAmount.y / size.height).coerceIn(0.12f, 0.88f)
                            )
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                val pivotPx = Offset(w * 0.5f, h * 0.52f)
                val wandPx = Offset(w * wandPos.x, h * wandPos.y)

                // 1. Draw Lab Table Surface
                val tableTopY = h * 0.82f
                drawLine(
                    color = ScienceBorder,
                    start = Offset(0f, tableTopY),
                    end = Offset(w, tableTopY),
                    strokeWidth = 3f
                )

                // 2. Draw Glass Bottle Stand
                drawGlassBottlePivot(pivotPx, tableTopY)

                // 3. Draw Electric Force Field Lines between wand & straw
                if (abs(wandCharge) > 0.1f) {
                    drawElectrostaticField(
                        wandTip = wandPx,
                        strawPivot = pivotPx,
                        strawAngle = strawAngleRad,
                        strawLength = min(w * 0.55f, 320.dp.toPx()),
                        sparkPhase = sparkPhase,
                        isPositive = wandCharge > 0
                    )
                }

                // 4. Draw Balanced Plastic Straw on Pivot
                val strawLength = min(w * 0.55f, 320.dp.toPx())
                drawBalancedStraw(pivotPx, strawAngleRad, strawLength)

                // 5. Draw Draggable Charged Wand
                drawChargedWand(
                    wandPos = wandPx,
                    charge = wandCharge,
                    sparkPhase = sparkPhase
                )
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Coulomb Electrostatic Field",
                items = listOf(
                    "Wand Charge" to "$chargePercent%",
                    "Polarity" to polarityStr,
                    "Induced Torque" to forceLevel,
                    "Coupling" to if (chargePercent > 10) "ACTIVE INDUCTION" else "ZERO CHARGE"
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
                        wandCharge = -1.0f
                        isRubbing = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberVibrant, contentColor = ScienceDarkBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("⚡ Rub Wool", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        wandCharge = 0f
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScienceDarkSurfaceVariant, contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ScienceBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ground", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                }

                IconButton(
                    onClick = {
                        wandPos = Offset(0.72f, 0.38f)
                        wandCharge = -0.85f
                        strawAngleRad = 0.3f
                        strawAngularVelocity = 0f
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

// Drawing Helpers for Electrostatics
private fun DrawScope.drawGlassBottlePivot(pivot: Offset, tableY: Float) {
    val bottleW = 75f
    val neckW = 26f
    val bottleH = tableY - pivot.y

    // Glass bottle outline
    val path = Path().apply {
        // Cap dome
        moveTo(pivot.x - neckW / 2f, pivot.y)
        quadraticTo(pivot.x, pivot.y - 12f, pivot.x + neckW / 2f, pivot.y)
        // Neck down
        lineTo(pivot.x + neckW / 2f, pivot.y + 40f)
        // Shoulder
        quadraticTo(pivot.x + bottleW / 2f, pivot.y + 70f, pivot.x + bottleW / 2f, pivot.y + 90f)
        // Body down
        lineTo(pivot.x + bottleW / 2f, tableY)
        lineTo(pivot.x - bottleW / 2f, tableY)
        // Body left up
        lineTo(pivot.x - bottleW / 2f, pivot.y + 90f)
        quadraticTo(pivot.x - bottleW / 2f, pivot.y + 70f, pivot.x - neckW / 2f, pivot.y + 40f)
        close()
    }

    // Glass fill & border
    drawPath(path = path, color = Color(0x1800E5FF))
    drawPath(path = path, color = Color(0x66FFFFFF), style = Stroke(width = 2.5f))

    // Rounded Cap Pivot Pin
    drawCircle(
        color = Color(0xFFFFB300),
        radius = 8f,
        center = Offset(pivot.x, pivot.y - 5f)
    )
}

private fun DrawScope.drawBalancedStraw(pivot: Offset, angleRad: Float, length: Float) {
    val thickness = 14f
    val halfL = length / 2f

    rotate(degrees = (angleRad * 180f / PI).toFloat(), pivot = pivot) {
        // Straw Body (Red and White striped drinking straw)
        val strawRect = Path().apply {
            addRoundRect(
                RoundRect(
                    left = pivot.x - halfL,
                    top = pivot.y - thickness / 2f,
                    right = pivot.x + halfL,
                    bottom = pivot.y + thickness / 2f,
                    radiusX = 4f,
                    radiusY = 4f
                )
            )
        }
        // Base white color
        drawPath(path = strawRect, color = Color.White)

        // Red spiral stripes across straw
        val stripeCount = 14
        val step = length / stripeCount
        for (i in 0 until stripeCount step 2) {
            val sx = pivot.x - halfL + i * step
            drawRect(
                color = StrawPlastic,
                topLeft = Offset(sx, pivot.y - thickness / 2f),
                size = Size(step * 0.7f, thickness)
            )
        }

        // Straw outline
        drawPath(
            path = strawRect,
            color = Color(0x88000000),
            style = Stroke(width = 1.5f)
        )

        // Balance pin center dimple
        drawCircle(
            color = Color.Black,
            radius = 3f,
            center = pivot
        )
    }
}

private fun DrawScope.drawChargedWand(wandPos: Offset, charge: Float, sparkPhase: Float) {
    val wandL = 180f
    val wandThick = 20f

    // Draw wand angled at 45 degrees
    val angle = -40f * (PI / 180f).toFloat()
    val cosA = cos(angle)
    val sinA = sin(angle)

    val tip = wandPos
    val base = Offset(tip.x + cosA * wandL, tip.y + sinA * wandL)

    // Wand Body (Dark acrylic plastic rod)
    drawLine(
        color = RodPlastic,
        start = base,
        end = tip,
        strokeWidth = wandThick,
        cap = StrokeCap.Round
    )

    // Glossy highlight
    drawLine(
        color = Color(0x44FFFFFF),
        start = Offset(base.x - 3f, base.y - 3f),
        end = Offset(tip.x - 3f, tip.y - 3f),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    // If charged, draw glowing charge aura & (-) particles
    if (abs(charge) > 0.1f) {
        val chargeColor = if (charge < 0) CyanNeon else AmberVibrant

        // Glow circle at tip
        drawCircle(
            color = chargeColor.copy(alpha = 0.25f + sin(sparkPhase * 2 * PI.toFloat()) * 0.1f),
            radius = 34f,
            center = tip
        )
        drawCircle(
            color = chargeColor.copy(alpha = 0.6f),
            radius = 16f,
            center = tip,
            style = Stroke(width = 2.5f)
        )

        // Draw (-) or (+) symbols around tip
        val symbol = if (charge < 0) "-" else "+"
        // Minus line
        drawLine(
            color = Color.White,
            start = Offset(tip.x - 7f, tip.y),
            end = Offset(tip.x + 7f, tip.y),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        if (charge > 0) {
            drawLine(
                color = Color.White,
                start = Offset(tip.x, tip.y - 7f),
                end = Offset(tip.x, tip.y + 7f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawElectrostaticField(
    wandTip: Offset,
    strawPivot: Offset,
    strawAngle: Float,
    strawLength: Float,
    sparkPhase: Float,
    isPositive: Boolean
) {
    // End A of straw
    val halfL = strawLength / 2f
    val endA = Offset(strawPivot.x + cos(strawAngle) * halfL, strawPivot.y + sin(strawAngle) * halfL)
    val endB = Offset(strawPivot.x - cos(strawAngle) * halfL, strawPivot.y - sin(strawAngle) * halfL)

    val distA = (wandTip - endA).getDistance()
    val distB = (wandTip - endB).getDistance()
    val nearestEnd = if (distA < distB) endA else endB
    val dist = min(distA, distB)

    // If within interaction range (< 400px), draw electrical arc lines
    if (dist < 450f) {
        val fieldAlpha = ((450f - dist) / 450f).coerceIn(0.1f, 0.85f)
        val arcColor = if (isPositive) AmberVibrant.copy(alpha = fieldAlpha) else CyanNeon.copy(alpha = fieldAlpha)

        // Dotted curved electric field line
        val midPoint = Offset(
            (wandTip.x + nearestEnd.x) / 2f + sin(sparkPhase * 2 * PI.toFloat()) * 12f,
            (wandTip.y + nearestEnd.y) / 2f + cos(sparkPhase * 2 * PI.toFloat()) * 12f
        )

        val arcPath = Path().apply {
            moveTo(wandTip.x, wandTip.y)
            quadraticTo(midPoint.x, midPoint.y, nearestEnd.x, nearestEnd.y)
        }

        drawPath(
            path = arcPath,
            color = arcColor,
            style = Stroke(
                width = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), sparkPhase * 22f)
            )
        )

        // Induced opposite charge indicator at the straw tip
        drawCircle(
            color = if (isPositive) CyanNeon.copy(alpha = fieldAlpha) else AmberVibrant.copy(alpha = fieldAlpha),
            radius = 8f,
            center = nearestEnd,
            style = Stroke(width = 2f)
        )
    }
}
