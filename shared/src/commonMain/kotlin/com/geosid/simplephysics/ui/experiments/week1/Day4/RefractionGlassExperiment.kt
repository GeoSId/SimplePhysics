package com.geosid.simplephysics.ui.experiments.week1.Day4

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
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
fun RefractionGlassExperiment(
    modifier: Modifier = Modifier
) {
    // Glass is Pyrex: n = 1.474
    val pyrexIndex = 1.474f

    // Current liquid index
    var liquidIndex by remember { mutableStateOf(1.333f) } // Water by default
    var liquidName by remember { mutableStateOf("Water (n=1.33)") }

    // Glass tube vertical offset (0f is high, 1f is fully submerged)
    var tubeSubmergedFraction by remember { mutableStateOf(0.65f) }

    // Laser rays toggle
    var showLaserRays by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition()
    val laserPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Calculate boundary visibility reflection R = ((n1 - n2)/(n1 + n2))^2
    val deltaN = abs(liquidIndex - pyrexIndex)
    val rFresnel = ((liquidIndex - pyrexIndex) / (liquidIndex + pyrexIndex)).pow(2)
    // Normalized visibility factor in liquid from 0f (invisible) to 1f (distinct)
    val submergedVisibility = (deltaN / 0.474f).coerceIn(0f, 1f)

    val matchPercent = ((1f - (deltaN / 0.474f).coerceIn(0f, 1f)) * 100).toInt()
    val visibilityText = when {
        matchPercent > 96 -> "✨ 100% INVISIBLE (Perfect Match!)"
        matchPercent > 70 -> "Subtle Ghost (Near Match)"
        else -> "Clearly Visible"
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            tubeSubmergedFraction = (tubeSubmergedFraction + dragAmount.y / (size.height * 0.45f)).coerceIn(0.15f, 0.90f)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Beaker dimensions
                val beakerWidth = min(w * 0.70f, 380.dp.toPx())
                val beakerHeight = min(h * 0.55f, 440.dp.toPx())
                val beakerLeft = (w - beakerWidth) / 2f
                val beakerTop = h * 0.22f
                val beakerBottom = beakerTop + beakerHeight

                // Liquid level inside beaker
                val liquidHeight = beakerHeight * 0.75f
                val liquidTop = beakerBottom - liquidHeight

                // Tube dimensions
                val tubeWidth = beakerWidth * 0.28f
                val tubeHeight = beakerHeight * 0.90f
                val tubeLeft = beakerLeft + (beakerWidth - tubeWidth) / 2f
                val tubeTop = beakerTop - tubeHeight * (1f - tubeSubmergedFraction)

                // 1. Draw Liquid Fill in Beaker
                drawLiquidInBeaker(
                    left = beakerLeft,
                    top = liquidTop,
                    width = beakerWidth,
                    height = liquidHeight,
                    index = liquidIndex
                )

                // 2. Draw Pyrex Glass Tube (part above liquid, and part below liquid with refractive fading)
                drawPyrexTube(
                    left = tubeLeft,
                    top = tubeTop,
                    width = tubeWidth,
                    height = tubeHeight,
                    liquidTop = liquidTop,
                    submergedVisibility = submergedVisibility
                )

                // 3. Draw Beaker Walls, Lip, and Graduation Marks
                drawBeakerGlass(
                    left = beakerLeft,
                    top = beakerTop,
                    width = beakerWidth,
                    height = beakerHeight,
                    bottom = beakerBottom
                )

                // 4. Draw Laser Rays (Optics Demonstration)
                if (showLaserRays) {
                    drawLaserRays(
                        beakerLeft = beakerLeft,
                        beakerWidth = beakerWidth,
                        tubeLeft = tubeLeft,
                        tubeWidth = tubeWidth,
                        liquidTop = liquidTop,
                        liquidBottom = beakerBottom,
                        liquidIndex = liquidIndex,
                        glassIndex = pyrexIndex,
                        alphaPulse = laserPulse
                    )
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                title = "Refractometer & Optics",
                items = listOf(
                    "Liquid Index (n)" to "${(round(liquidIndex * 1000) / 1000f)}",
                    "Pyrex Glass (n)" to "1.474",
                    "Index Match" to "$matchPercent%",
                    "Visibility" to visibilityText
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Liquid Preset Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf(
                        Triple("Air", 1.000f, "Air (n=1.00)"),
                        Triple("Water", 1.333f, "Water (n=1.33)"),
                        Triple("Glycerin / Oil", 1.474f, "Vegetable Oil (n=1.474)"),
                        Triple("Dense Glass", 1.620f, "Flint Fluid (n=1.62)")
                    ).forEach { (label, idx, full) ->
                        val isSelected = abs(liquidIndex - idx) < 0.01f
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                liquidIndex = idx
                                liquidName = full
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.padding(horizontal = 3.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (idx == 1.474f) AmberVibrant else CyanNeon,
                                selectedLabelColor = ScienceDarkBg
                            )
                        )
                    }
                }

                // Slider for continuous index adjustment
                Surface(
                    color = ScienceDarkSurfaceVariant.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        PhysicsSliderControl(
                            title = "Liquid Refractive Index (n)",
                            value = liquidIndex,
                            range = 1.000f..1.650f,
                            valueDisplay = "n = ${round(liquidIndex * 1000) / 1000f}",
                            accentColor = if (abs(liquidIndex - pyrexIndex) < 0.02f) EmeraldNeon else CyanNeon,
                            onValueChange = {
                                liquidIndex = it
                                liquidName = "Custom (n=${round(it * 1000) / 1000f})"
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Toggle Lasers
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = showLaserRays,
                                    onCheckedChange = { showLaserRays = it },
                                    colors = CheckboxDefaults.colors(checkedColor = EmeraldNeon)
                                )
                                Text("Show Laser Beams", color = TextPrimary, fontSize = 12.sp)
                            }

                            // Reset
                            IconButton(
                                onClick = {
                                    liquidIndex = 1.333f
                                    liquidName = "Water (n=1.33)"
                                    tubeSubmergedFraction = 0.65f
                                    showLaserRays = true
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ScienceDarkSurface, RoundedCornerShape(8.dp))
                            ) {
                                ResetIcon(tint = CyanNeon, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}

// Drawing Helpers for Beaker, Tube & Optics
private fun DrawScope.drawLiquidInBeaker(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    index: Float
) {
    // Tint color based on fluid type
    val fluidColor = when {
        index < 1.15f -> Color.Transparent // Air
        index < 1.40f -> WaterBlue.copy(alpha = 0.35f) // Water
        index in 1.45f..1.50f -> AmberVibrant.copy(alpha = 0.35f) // Vegetable Oil / Glycerin
        else -> PurpleNeon.copy(alpha = 0.35f) // Dense Fluid
    }

    if (index > 1.1f) {
        val cornerR = 20f
        val liquidPath = Path().apply {
            moveTo(left + 6f, top)
            lineTo(left + width - 6f, top)
            lineTo(left + width - 6f, top + height - cornerR)
            quadraticTo(left + width - 6f, top + height - 6f, left + width - cornerR, top + height - 6f)
            lineTo(left + cornerR, top + height - 6f)
            quadraticTo(left + 6f, top + height - 6f, left + 6f, top + height - cornerR)
            close()
        }
        drawPath(path = liquidPath, color = fluidColor)

        // Meniscus surface line
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(left + 8f, top),
            end = Offset(left + width - 8f, top),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawPyrexTube(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    liquidTop: Float,
    submergedVisibility: Float
) {
    val cornerR = width / 2f

    // 1. Portion of tube ABOVE liquid (Always fully visible)
    val aboveHeight = (liquidTop - top).coerceAtLeast(0f)
    if (aboveHeight > 0f) {
        val abovePath = Path().apply {
            moveTo(left, top)
            lineTo(left + width, top)
            lineTo(left + width, min(top + height, liquidTop))
            lineTo(left, min(top + height, liquidTop))
            close()
        }
        drawPath(
            path = abovePath,
            color = Color(0x1AFFFFFF)
        )
        // Outline above
        drawLine(Color.White.copy(alpha = 0.8f), Offset(left, top), Offset(left, min(top + height, liquidTop)), 2.5f)
        drawLine(Color.White.copy(alpha = 0.8f), Offset(left + width, top), Offset(left + width, min(top + height, liquidTop)), 2.5f)
        // Lip
        drawLine(Color.White.copy(alpha = 0.9f), Offset(left - 4f, top), Offset(left + width + 4f, top), 3.5f, cap = StrokeCap.Round)
    }

    // 2. Portion of tube SUBMERGED in liquid (Visibility scales down to 0 at n=1.474!)
    val submergedTop = max(top, liquidTop)
    val submergedBottom = top + height
    if (submergedBottom > submergedTop && submergedVisibility > 0.01f) {
        val visAlpha = (submergedVisibility * 0.85f).coerceIn(0f, 0.85f)
        val submergedPath = Path().apply {
            moveTo(left, submergedTop)
            lineTo(left + width, submergedTop)
            lineTo(left + width, submergedBottom - cornerR)
            quadraticTo(left + width, submergedBottom, left + width / 2f, submergedBottom)
            quadraticTo(left, submergedBottom, left, submergedBottom - cornerR)
            close()
        }

        // Submerged fill
        drawPath(
            path = submergedPath,
            color = Color.White.copy(alpha = visAlpha * 0.15f)
        )

        // Submerged glass boundary outlines
        drawPath(
            path = submergedPath,
            color = Color.White.copy(alpha = visAlpha),
            style = Stroke(width = 2.5f * submergedVisibility)
        )

        // Internal reflection highlight
        drawLine(
            color = Color.White.copy(alpha = visAlpha * 0.6f),
            start = Offset(left + width * 0.25f, submergedTop + 10f),
            end = Offset(left + width * 0.25f, submergedBottom - 20f),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawBeakerGlass(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    bottom: Float
) {
    val cornerR = 20f
    val path = Path().apply {
        // Spout on top-left
        moveTo(left - 12f, top - 4f)
        lineTo(left, top)
        lineTo(left, bottom - cornerR)
        quadraticTo(left, bottom, left + cornerR, bottom)
        lineTo(left + width - cornerR, bottom)
        quadraticTo(left + width, bottom, left + width, bottom - cornerR)
        lineTo(left + width, top)
        // Top right lip
        lineTo(left + width + 6f, top)
    }

    // Beaker outline
    drawPath(
        path = path,
        color = Color(0xBBFFFFFF),
        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Beaker Volume Graduation Marks (e.g. 100ml, 200ml, 300ml, 400ml)
    for (i in 1..4) {
        val gy = bottom - (height * 0.16f * i)
        val tickLength = if (i % 2 == 0) 24f else 16f
        drawLine(
            color = Color(0x88FFFFFF),
            start = Offset(left + 8f, gy),
            end = Offset(left + 8f + tickLength, gy),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawLaserRays(
    beakerLeft: Float,
    beakerWidth: Float,
    tubeLeft: Float,
    tubeWidth: Float,
    liquidTop: Float,
    liquidBottom: Float,
    liquidIndex: Float,
    glassIndex: Float,
    alphaPulse: Float
) {
    val rayColor = EmeraldNeon.copy(alpha = alphaPulse)
    val rayStroke = 3f

    // 3 parallel laser beams at different vertical levels inside the liquid
    val rayCount = 3
    val stepY = (liquidBottom - liquidTop) / (rayCount + 1)

    for (i in 1..rayCount) {
        val y = liquidTop + i * stepY

        // Beam enters from left of screen
        val x0 = beakerLeft - 60f
        val xBeakerLeft = beakerLeft + 6f
        val xTubeLeft = tubeLeft
        val xTubeRight = tubeLeft + tubeWidth
        val xBeakerRight = beakerLeft + beakerWidth - 6f
        val xExit = beakerLeft + beakerWidth + 60f

        // Snell's Law deviation factor: if liquidIndex == glassIndex, deviation is ZERO!
        val indexDiff = glassIndex - liquidIndex
        val refractionBend = indexDiff * 18f // Bending angle offset in pixels

        val rayPath = Path().apply {
            moveTo(x0, y)
            lineTo(xBeakerLeft, y)
            lineTo(xTubeLeft, y)

            // When passing through tube:
            if (abs(indexDiff) < 0.015f) {
                // Completely straight line through glass!
                lineTo(xTubeRight, y)
            } else {
                // Refracted curve / zig-zag through cylinder
                lineTo(xTubeLeft + tubeWidth * 0.5f, y + refractionBend)
                lineTo(xTubeRight, y)
            }

            lineTo(xBeakerRight, y)
            lineTo(xExit, y)
        }

        drawPath(
            path = rayPath,
            color = rayColor,
            style = Stroke(width = rayStroke, cap = StrokeCap.Round)
        )

        // Laser emitter diode icon at left
        drawCircle(
            color = EmeraldNeon,
            radius = 5f,
            center = Offset(x0, y)
        )
    }
}
