package com.geosid.simplephysics.ui.experiments.week6.Day40

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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

/**
 * Presets for Day 40: Hydraulic Press Force Multiplier.
 */
enum class HydraulicPreset(
    val label: String,
    val title: String,
    val targetName: String,
    val inputDiameterMm: Float,
    val outputDiameterMm: Float,
    val defaultInputForceN: Float,
    val crushThresholdN: Float,
    val maxDeformationFrac: Float,
    val desc: String
) {
    SODA_CAN(
        label = "Soda Can",
        title = "Aluminum Soda Can Crush",
        targetName = "Aluminum Can",
        inputDiameterMm = 20f,
        outputDiameterMm = 100f, // Area ratio = (100/20)^2 = 25x
        defaultInputForceN = 40f, // Output = 1,000 N (~100 kg)
        crushThresholdN = 350f,
        maxDeformationFrac = 0.82f,
        desc = "A 25x hydraulic advantage multiplies 40 N hand force into 1,000 N, crushing thin aluminum into a pancake"
    ),
    RUBBER_BALL(
        label = "Rubber Ball",
        title = "Elastic Rubber Ball Squeeze",
        targetName = "Solid Rubber Sphere",
        inputDiameterMm = 25f,
        outputDiameterMm = 125f, // Area ratio = 25x
        defaultInputForceN = 60f, // Output = 1,500 N
        crushThresholdN = 800f,
        maxDeformationFrac = 0.65f,
        desc = "High fluid pressure flattens the rubber sphere elastically, storing strain energy before rebounding"
    ),
    STEEL_BLOCK(
        label = "Steel Block",
        title = "Industrial Forged Steel Yield",
        targetName = "Structural Steel Cube",
        inputDiameterMm = 15f,
        outputDiameterMm = 150f, // Area ratio = (150/15)^2 = 100x
        defaultInputForceN = 120f, // Output = 12,000 N (~1.22 metric tons)
        crushThresholdN = 6000f,
        maxDeformationFrac = 0.45f,
        desc = "A massive 100x force multiplier exerts over 12 kN, exceeding the plastic yield stress of structural steel"
    ),
    CAR_JACK(
        label = "Car Jack",
        title = "Automotive Hydraulic Bottle Jack",
        targetName = "Vehicle Chassis Load (1.5 Tons)",
        inputDiameterMm = 18f,
        outputDiameterMm = 90f, // Area ratio = 25x
        defaultInputForceN = 80f, // Output = 2,000 N per stroke
        crushThresholdN = 12000f,
        maxDeformationFrac = 0.50f,
        desc = "Repeated strokes pump high-pressure hydraulic oil beneath the heavy ram to hoist vehicle axles"
    ),
    DIAMOND_CELL(
        label = "Diamond Anvil",
        title = "Extreme High-Pressure Anvil Cell",
        targetName = "Microcrystalline Sample",
        inputDiameterMm = 10f,
        outputDiameterMm = 160f, // Area ratio = 256x
        defaultInputForceN = 150f, // Output = 38,400 N (~3.9 tons)
        crushThresholdN = 25000f,
        maxDeformationFrac = 0.90f,
        desc = "Sub-millimeter input piston drives massive pressure amplification into the gigapascal regime"
    )
}

@Composable
fun HydraulicPressExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(HydraulicPreset.SODA_CAN) }

    // Physical Parameters
    var inputForceN by remember { mutableStateOf(currentPreset.defaultInputForceN) }
    var inputDiameterMm by remember { mutableStateOf(currentPreset.inputDiameterMm) }
    var outputDiameterMm by remember { mutableStateOf(currentPreset.outputDiameterMm) }
    var inputStrokeFrac by remember { mutableStateOf(0.40f) } // 0f (fully up) to 1f (fully down)

    // Simulation Toggles
    var showPressureField by remember { mutableStateOf(true) }
    var showForceVectors by remember { mutableStateOf(true) }
    var isAutoCycling by remember { mutableStateOf(true) }

    fun applyPreset(preset: HydraulicPreset) {
        currentPreset = preset
        inputForceN = preset.defaultInputForceN
        inputDiameterMm = preset.inputDiameterMm
        outputDiameterMm = preset.outputDiameterMm
        inputStrokeFrac = 0.35f
    }

    // Geometry Calculations
    val d1M = inputDiameterMm * 1e-3f
    val d2M = outputDiameterMm * 1e-3f
    val a1M2 = PI.toFloat() * (d1M * 0.5f) * (d1M * 0.5f)
    val a2M2 = PI.toFloat() * (d2M * 0.5f) * (d2M * 0.5f)

    // 1. Pascal's Law: Equal Pressure throughout closed fluid
    // P1 = F1 / A1
    val systemPressurePa = inputForceN / max(a1M2, 1e-7f)
    val systemPressureKPa = systemPressurePa / 1000f
    val systemPressureBar = systemPressurePa / 100_000f

    // 2. Ideal Mechanical Advantage: MA = A2 / A1 = (D2 / D1)^2
    val mechanicalAdvantage = a2M2 / max(a1M2, 1e-7f)

    // 3. Output Force: F2 = F1 * (A2 / A1) = P * A2
    val outputForceN = inputForceN * mechanicalAdvantage
    val outputForceKg = outputForceN / 9.81f

    // 4. Incompressible Work & Displacement Conservation: A1 * d1 = A2 * d2
    val maxStrokeD1M = 0.12f // 12 cm maximum plunger travel
    val currentD1M = maxStrokeD1M * inputStrokeFrac
    val currentD1Cm = currentD1M * 100f
    val currentD2M = currentD1M * (a1M2 / a2M2)
    val currentD2Mm = currentD2M * 1000f

    val workJoules = inputForceN * currentD1M // W1 = W2 = F * d

    // Crush Deformation Dynamics
    val forceRatio = outputForceN / max(currentPreset.crushThresholdN, 1f)
    val deformationProgress = (forceRatio * inputStrokeFrac).coerceIn(0f, 1f)
    val actualCrushFrac = deformationProgress * currentPreset.maxDeformationFrac

    // Animation transition for Auto-Cycle pump
    val infiniteTransition = rememberInfiniteTransition()
    val pumpCycle by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAutoCycling) 2400 else 999999, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(pumpCycle, isAutoCycling) {
        if (isAutoCycling) {
            inputStrokeFrac = pumpCycle
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag input plunger down to crush the target! Pascal's law multiplies output force by (D₂/D₁)².",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            isAutoCycling = false
                            // Vertical drag moves input plunger
                            inputStrokeFrac = (inputStrokeFrac + dragAmount.y * 0.005f).coerceIn(0.05f, 0.95f)
                            // Horizontal drag adjusts input hand force
                            inputForceN = (inputForceN + dragAmount.x * 0.5f).coerceIn(5f, 300f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val xNorm = offset.x / size.width
                            if (xNorm < 0.40f) {
                                // Tap plunger: stroke down
                                isAutoCycling = false
                                inputStrokeFrac = if (inputStrokeFrac > 0.6f) 0.2f else 0.85f
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Apparatus Center: Elevated origin at cy ~ 0.42h
                val cy = h * 0.42f

                // Coordinate grid background
                val gridSpacing = 36.dp.toPx()
                var gx = 0f
                while (gx < w) {
                    drawLine(ScienceBorder.copy(alpha = 0.18f), Offset(gx, 0f), Offset(gx, h), 0.8f)
                    gx += gridSpacing
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(ScienceBorder.copy(alpha = 0.18f), Offset(0f, gy), Offset(w, gy), 0.8f)
                    gy += gridSpacing
                }

                // 1. Hydraulic Cylinders Layout Coordinates
                val leftColX = w * 0.22f  // Narrow Input Plunger
                val rightColX = w * 0.70f // Wide Output Ram

                val leftCylinderW = 34f
                val rightCylinderW = 92f

                val manifoldBaseY = cy + 70f
                val manifoldPipeH = 24f

                val leftCylinderTopY = cy - 65f
                val rightCylinderTopY = cy - 40f

                // 2. Hydraulic Oil Reservoir & Pressure Glow (U-Tube Manifold)
                val oilPath = Path().apply {
                    // Left column fluid
                    val leftFluidTopY = leftCylinderTopY + 20f + (inputStrokeFrac * 45f)
                    val rightFluidTopY = rightCylinderTopY + 15f - (currentD2Mm * 1.8f).coerceAtMost(25f)

                    moveTo(leftColX - leftCylinderW * 0.5f + 2f, leftFluidTopY)
                    lineTo(leftColX + leftCylinderW * 0.5f - 2f, leftFluidTopY)
                    lineTo(leftColX + leftCylinderW * 0.5f - 2f, manifoldBaseY)
                    // Bottom bridge
                    lineTo(rightColX - rightCylinderW * 0.5f + 2f, manifoldBaseY)
                    // Right column fluid
                    lineTo(rightColX - rightCylinderW * 0.5f + 2f, rightFluidTopY)
                    lineTo(rightColX + rightCylinderW * 0.5f - 2f, rightFluidTopY)
                    lineTo(rightColX + rightCylinderW * 0.5f - 2f, manifoldBaseY + manifoldPipeH)
                    // Bottom wall
                    lineTo(leftColX - leftCylinderW * 0.5f + 2f, manifoldBaseY + manifoldPipeH)
                    close()
                }

                // Pressure-dependent chromatic oil shader:
                // Normal amber hydraulic oil -> deep magenta/coral under intense pressure
                val pressureIntensity = (systemPressureBar / 25f).coerceIn(0f, 1f)
                val oilColors = if (showPressureField) {
                    listOf(
                        Color(0xFFF59E0B).copy(alpha = 0.55f),
                        lerp(Color(0xFFD97706), CoralNeon, pressureIntensity),
                        lerp(Color(0xFFB45309), PurpleNeon, pressureIntensity)
                    )
                } else {
                    listOf(
                        Color(0xFF0284C7).copy(alpha = 0.6f),
                        Color(0xFF0369A1).copy(alpha = 0.7f)
                    )
                }

                drawPath(
                    path = oilPath,
                    brush = Brush.verticalGradient(
                        colors = oilColors,
                        startY = leftCylinderTopY,
                        endY = manifoldBaseY + manifoldPipeH
                    )
                )

                // Fluid Pressure Transmission Wave Particles (Pascal's Principle)
                val numIsobars = 7
                for (k in 0..numIsobars) {
                    val t = k / numIsobars.toFloat()
                    val px = (leftColX + leftCylinderW * 0.5f) + t * ((rightColX - rightCylinderW * 0.5f) - (leftColX + leftCylinderW * 0.5f))
                    val py = manifoldBaseY + manifoldPipeH * 0.5f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f + 0.35f * sin(t * 10f + pumpCycle * 6f)),
                        radius = 2.5f,
                        center = Offset(px, py)
                    )
                }

                // 3. Structural Cylinder Steel Walls & Frames
                // Manifold Outer Walls
                val wallColor = Color(0xFF64748B)
                val wallStroke = 2.4f

                // Left Input Cylinder Tube
                drawRect(
                    color = Color.White.copy(alpha = 0.65f),
                    topLeft = Offset(leftColX - leftCylinderW * 0.5f, leftCylinderTopY),
                    size = Size(leftCylinderW, manifoldBaseY - leftCylinderTopY + manifoldPipeH),
                    style = Stroke(wallStroke)
                )

                // Right Output Cylinder Tube
                drawRect(
                    color = Color.White.copy(alpha = 0.65f),
                    topLeft = Offset(rightColX - rightCylinderW * 0.5f, rightCylinderTopY),
                    size = Size(rightCylinderW, manifoldBaseY - rightCylinderTopY + manifoldPipeH),
                    style = Stroke(wallStroke)
                )

                // Bottom Bridge Tube Connectors
                drawLine(
                    color = Color.White.copy(alpha = 0.65f),
                    start = Offset(leftColX + leftCylinderW * 0.5f, manifoldBaseY),
                    end = Offset(rightColX - rightCylinderW * 0.5f, manifoldBaseY),
                    strokeWidth = wallStroke
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.65f),
                    start = Offset(leftColX + leftCylinderW * 0.5f, manifoldBaseY + manifoldPipeH),
                    end = Offset(rightColX - rightCylinderW * 0.5f, manifoldBaseY + manifoldPipeH),
                    strokeWidth = wallStroke
                )

                // Heavy Base Support Bed Plate
                val bedY = manifoldBaseY + manifoldPipeH + 4f
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF334155), Color(0xFF1E293B)),
                        startY = bedY,
                        endY = bedY + 14f
                    ),
                    topLeft = Offset(w * 0.12f, bedY),
                    size = Size(w * 0.76f, 14f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawRoundRect(
                    color = ScienceBorder.copy(alpha = 0.75f),
                    topLeft = Offset(w * 0.12f, bedY),
                    size = Size(w * 0.76f, 14f),
                    cornerRadius = CornerRadius(3f, 3f),
                    style = Stroke(1.2f)
                )

                // 4. Narrow Input Plunger Assembly (Left)
                val leftPistonY = leftCylinderTopY + 20f + (inputStrokeFrac * 45f)
                val leftPistonH = 12f
                val plungerRodTopY = leftPistonY - 55f

                // Plunger Rod
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFCBD5E1), Color(0xFFF1F5F9), Color(0xFF94A3B8)),
                        startX = leftColX - 5f,
                        endX = leftColX + 5f
                    ),
                    topLeft = Offset(leftColX - 5f, plungerRodTopY),
                    size = Size(10f, 55f)
                )
                // Plunger Seal Head
                drawRoundRect(
                    color = AmberVibrant,
                    topLeft = Offset(leftColX - leftCylinderW * 0.5f + 3f, leftPistonY - leftPistonH),
                    size = Size(leftCylinderW - 6f, leftPistonH),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                // Handle Grip Bar on Top
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(leftColX - 22f, plungerRodTopY - 7f),
                    size = Size(44f, 8f),
                    cornerRadius = CornerRadius(3f, 3f)
                )

                // 5. Output Press Frame & Crushing Anvil (Right)
                val pressUpperBeamY = cy - 130f
                val pressAnvilY = cy - 70f
                val columnH = manifoldBaseY - pressUpperBeamY

                // Vertical Rigid Steel Pillars
                val pillarW = 10f
                val pillar1X = rightColX - rightCylinderW * 0.5f - 18f
                val pillar2X = rightColX + rightCylinderW * 0.5f + 8f

                drawRect(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF475569), Color(0xFF94A3B8), Color(0xFF334155))),
                    topLeft = Offset(pillar1X, pressUpperBeamY),
                    size = Size(pillarW, columnH)
                )
                drawRect(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF475569), Color(0xFF94A3B8), Color(0xFF334155))),
                    topLeft = Offset(pillar2X, pressUpperBeamY),
                    size = Size(pillarW, columnH)
                )

                // Top Crosshead Reaction Beam (Resists massive crushing load)
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF64748B), Color(0xFF334155))),
                    topLeft = Offset(pillar1X - 6f, pressUpperBeamY - 14f),
                    size = Size((pillar2X - pillar1X) + pillarW + 12f, 18f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.5f),
                    topLeft = Offset(pillar1X - 6f, pressUpperBeamY - 14f),
                    size = Size((pillar2X - pillar1X) + pillarW + 12f, 18f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(1.2f)
                )

                // Stationary Top Anvil Die
                val topDieW = 54f
                val topDieH = 14f
                val topDieBottomY = pressUpperBeamY + 4f + topDieH
                drawRect(
                    color = Color(0xFFCBD5E1),
                    topLeft = Offset(rightColX - topDieW * 0.5f, pressUpperBeamY + 4f),
                    size = Size(topDieW, topDieH)
                )

                // 6. Wide Output Ram Assembly (Right Piston & Moving Lower Die)
                val rightPistonY = rightCylinderTopY + 15f - (currentD2Mm * 1.8f).coerceAtMost(25f)
                val ramRodTopY = topDieBottomY + 38f - (actualCrushFrac * 22f)

                // Heavy Hydraulic Cylinder Piston Body
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF94A3B8), Color(0xFFE2E8F0), Color(0xFF64748B)),
                        startX = rightColX - rightCylinderW * 0.5f + 4f,
                        endX = rightColX + rightCylinderW * 0.5f - 4f
                    ),
                    topLeft = Offset(rightColX - rightCylinderW * 0.5f + 4f, ramRodTopY),
                    size = Size(rightCylinderW - 8f, rightPistonY - ramRodTopY + 12f),
                    cornerRadius = CornerRadius(3f, 3f)
                )

                // Moving Lower Anvil Plate
                val lowerDieW = 60f
                val lowerDieH = 12f
                drawRoundRect(
                    color = Color(0xFFCBD5E1),
                    topLeft = Offset(rightColX - lowerDieW * 0.5f, ramRodTopY - lowerDieH),
                    size = Size(lowerDieW, lowerDieH),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                drawRoundRect(
                    color = ScienceBorder,
                    topLeft = Offset(rightColX - lowerDieW * 0.5f, ramRodTopY - lowerDieH),
                    size = Size(lowerDieW, lowerDieH),
                    cornerRadius = CornerRadius(2f, 2f),
                    style = Stroke(1.2f)
                )

                // 7. Crushable Target Object (Trapped between dies)
                val crushZoneTop = topDieBottomY
                val crushZoneBottom = ramRodTopY - lowerDieH
                val targetHeight = (crushZoneBottom - crushZoneTop).coerceAtLeast(4f)
                val targetCenterY = (crushZoneTop + crushZoneBottom) * 0.5f

                when (currentPreset) {
                    HydraulicPreset.SODA_CAN -> {
                        // Aluminum Beverage Can: squishes into folded ridges
                        val canW = 28f + (actualCrushFrac * 22f)
                        val canColor = CoralNeon
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                listOf(canColor.copy(alpha = 0.8f), Color.White, canColor),
                                startX = rightColX - canW * 0.5f,
                                endX = rightColX + canW * 0.5f
                            ),
                            topLeft = Offset(rightColX - canW * 0.5f, crushZoneTop + 1f),
                            size = Size(canW, targetHeight - 2f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                        // Metal accordion crimp lines
                        if (actualCrushFrac > 0.15f) {
                            val folds = 3
                            for (f in 1..folds) {
                                val fy = crushZoneTop + (targetHeight / (folds + 1)) * f
                                drawLine(Color(0xFF1E293B), Offset(rightColX - canW * 0.45f, fy), Offset(rightColX + canW * 0.45f, fy), 1.6f)
                            }
                        }
                    }
                    HydraulicPreset.RUBBER_BALL -> {
                        // Elastic Rubber Sphere: bulges laterally into an ellipse
                        val ballRx = 14f + (actualCrushFrac * 18f)
                        val ballRy = (targetHeight * 0.5f).coerceAtLeast(3f)
                        drawOval(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFFFFEE58), Color(0xFFF57F17), Color(0xFFE65100)),
                                center = Offset(rightColX - 4f, targetCenterY - 4f),
                                radius = ballRx * 1.1f
                            ),
                            topLeft = Offset(rightColX - ballRx, targetCenterY - ballRy),
                            size = Size(ballRx * 2f, ballRy * 2f)
                        )
                    }
                    HydraulicPreset.STEEL_BLOCK -> {
                        // Forged Steel Block: solid gray cube with glowing stress fractures
                        val blockW = 32f + (actualCrushFrac * 10f)
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF94A3B8), Color(0xFF475569)),
                                startY = crushZoneTop,
                                endY = crushZoneBottom
                            ),
                            topLeft = Offset(rightColX - blockW * 0.5f, crushZoneTop),
                            size = Size(blockW, targetHeight)
                        )
                        // Glowing red/orange high-strain fracture sparks
                        if (forceRatio > 0.7f) {
                            drawLine(AmberVibrant, Offset(rightColX - 6f, crushZoneTop + 2f), Offset(rightColX + 4f, targetCenterY), 1.8f)
                            drawLine(CoralNeon, Offset(rightColX + 4f, targetCenterY), Offset(rightColX - 2f, crushZoneBottom - 2f), 1.8f)
                        }
                    }
                    HydraulicPreset.CAR_JACK -> {
                        // Car Chassis Beam & Tire Hub being hoisted
                        val beamW = 62f
                        drawRoundRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(rightColX - beamW * 0.5f, crushZoneTop + 4f),
                            size = Size(beamW, 14f),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                        drawCircle(Color(0xFF1E293B), 16f, Offset(rightColX, crushZoneTop - 8f))
                        drawCircle(Color(0xFF94A3B8), 8f, Offset(rightColX, crushZoneTop - 8f))
                    }
                    HydraulicPreset.DIAMOND_CELL -> {
                        // Diamond Micro-Anvil faceted gem tip
                        val diamondPath = Path().apply {
                            moveTo(rightColX, crushZoneTop)
                            lineTo(rightColX + 16f, targetCenterY)
                            lineTo(rightColX, crushZoneBottom)
                            lineTo(rightColX - 16f, targetCenterY)
                            close()
                        }
                        drawPath(diamondPath, CyanNeon.copy(alpha = 0.85f))
                        drawCircle(Color.White, 3f, Offset(rightColX, targetCenterY))
                    }
                }

                // 8. Dynamic Force Vectors (F1 Input vs F2 Output)
                if (showForceVectors) {
                    // F1 Input Downward Vector
                    val f1Len = (inputForceN * 0.22f).coerceIn(14f, 42f)
                    val f1Y = plungerRodTopY - 14f
                    drawLine(AmberVibrant, Offset(leftColX, f1Y - f1Len), Offset(leftColX, f1Y), 2.8f, StrokeCap.Round)
                    val f1Head = Path().apply {
                        moveTo(leftColX, f1Y + 3f)
                        lineTo(leftColX - 3.5f, f1Y - 4f)
                        lineTo(leftColX + 3.5f, f1Y - 4f)
                        close()
                    }
                    drawPath(f1Head, AmberVibrant)

                    // F2 Output Upward Crush Force Vector (Massive Cyan vector!)
                    val f2Len = (outputForceN * 0.005f + 25f).coerceIn(24f, 65f)
                    val f2Y = ramRodTopY + 18f
                    drawLine(CyanNeon, Offset(rightColX, f2Y + f2Len), Offset(rightColX, f2Y), 3.4f, StrokeCap.Round)
                    val f2Head = Path().apply {
                        moveTo(rightColX, f2Y - 4f)
                        lineTo(rightColX - 5f, f2Y + 4f)
                        lineTo(rightColX + 5f, f2Y + 4f)
                        close()
                    }
                    drawPath(f2Head, CyanNeon)
                }
            }
        },
        hudContent = {
            // Rule: Transparent HUD card so physical apparatus shines through!
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 40: Hydraulic Press Force Multiplier",
                items = listOf(
                    "Pascal's Principle" to "P₁ = P₂ = F/A",
                    "Mechanical Advantage (MA)" to "${round(mechanicalAdvantage * 10f) / 10f}x",
                    "Input Force (F₁)" to "${round(inputForceN)} N",
                    "Output Force (F₂)" to "${round(outputForceN)} N (${round(outputForceKg)} kg)",
                    "System Pressure (P)" to "${round(systemPressureBar * 10f) / 10f} bar (${round(systemPressureKPa)} kPa)",
                    "Input Stroke (d₁)" to "${round(currentD1Cm * 10f) / 10f} cm",
                    "Output Lift (d₂)" to "${round(currentD2Mm * 100f) / 100f} mm",
                    "Work Conserved (W)" to "${round(workJoules * 100f) / 100f} J"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(HydraulicPreset.values()) { preset ->
                        val isSelected = currentPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { applyPreset(preset) },
                            label = {
                                Text(
                                    text = preset.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.22f),
                                selectedLabelColor = CyanNeon,
                                containerColor = ScienceDarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = ScienceBorder,
                                selectedBorderColor = CyanNeon
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                // Slider Pair 1: Input Force F1 & Output Ram Diameter D2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Input Force F₁",
                        value = inputForceN,
                        range = 10f..250f,
                        valueDisplay = "${round(inputForceN)} N",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            isAutoCycling = false
                            inputForceN = it
                        }
                    )
                    PhysicsSliderControl(
                        title = "Ram Diameter D₂",
                        value = outputDiameterMm,
                        range = 40f..200f,
                        valueDisplay = "${round(outputDiameterMm)} mm",
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { outputDiameterMm = it }
                    )
                }

                // Slider Pair 2: Plunger Stroke Position & Input Diameter D1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Plunger Stroke d₁",
                        value = inputStrokeFrac,
                        range = 0.05f..0.95f,
                        valueDisplay = "${round(inputStrokeFrac * 100f)}%",
                        accentColor = EmeraldNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            isAutoCycling = false
                            inputStrokeFrac = it
                        }
                    )
                    PhysicsSliderControl(
                        title = "Plunger Diameter D₁",
                        value = inputDiameterMm,
                        range = 10f..40f,
                        valueDisplay = "${round(inputDiameterMm)} mm",
                        accentColor = PurpleNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { inputDiameterMm = it }
                    )
                }

                // Control Action Toggles & Reset Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Auto-Cycle Pump Toggle
                    Button(
                        onClick = { isAutoCycling = !isAutoCycling },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAutoCycling) AmberVibrant.copy(alpha = 0.25f) else CyanNeon.copy(alpha = 0.25f),
                            contentColor = if (isAutoCycling) AmberVibrant else CyanNeon
                        ),
                        border = BorderStroke(1.dp, if (isAutoCycling) AmberVibrant else CyanNeon),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1f)
                    ) {
                        Text(
                            text = if (isAutoCycling) "⏸ Pause Pump" else "▶ Auto Pump",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Pressure Field Toggle
                    FilledTonalButton(
                        onClick = { showPressureField = !showPressureField },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (showPressureField) CoralNeon.copy(alpha = 0.20f) else ScienceDarkSurfaceVariant,
                            contentColor = if (showPressureField) CoralNeon else TextSecondary
                        ),
                        border = BorderStroke(1.dp, if (showPressureField) CoralNeon.copy(alpha = 0.6f) else ScienceBorder),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1f)
                    ) {
                        Text(
                            text = if (showPressureField) "🔥 Pressure" else "💧 Oil",
                            fontSize = 10.sp
                        )
                    }

                    // Force Vectors Toggle
                    FilledTonalButton(
                        onClick = { showForceVectors = !showForceVectors },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (showForceVectors) CyanNeon.copy(alpha = 0.20f) else ScienceDarkSurfaceVariant,
                            contentColor = if (showForceVectors) CyanNeon else TextSecondary
                        ),
                        border = BorderStroke(1.dp, if (showForceVectors) CyanNeon.copy(alpha = 0.6f) else ScienceBorder),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1f)
                    ) {
                        Text(
                            text = if (showForceVectors) "⚡ Vectors" else "⚡ Vectors OFF",
                            fontSize = 10.sp
                        )
                    }

                    // Reset Button
                    IconButton(
                        onClick = { applyPreset(currentPreset) },
                        modifier = Modifier
                            .size(34.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, ScienceBorder.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    ) {
                        ResetIcon(tint = CyanNeon, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    )
}
