package com.geosid.simplephysics.ui.experiments.week6.Day41

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
 * Presets for Day 41: Capillary Action & Meniscus Rise.
 */
enum class CapillaryPreset(
    val label: String,
    val title: String,
    val liquidName: String,
    val surfaceTensionMNm: Float, // mN/m = 1e-3 N/m
    val contactAngleDeg: Float,   // degrees
    val densityKgM3: Float,       // kg/m^3
    val primaryRadiusMm: Float,   // mm
    val isDepression: Boolean,
    val liquidColor: Color,
    val desc: String
) {
    WATER_GLASS(
        label = "Water & Glass",
        title = "Pure Water on Clean Glass",
        liquidName = "Water (H₂O)",
        surfaceTensionMNm = 72.8f,
        contactAngleDeg = 0f,
        densityKgM3 = 1000f,
        primaryRadiusMm = 0.5f,
        isDepression = false,
        liquidColor = Color(0xFF0284C7),
        desc = "Strong polar adhesion creates zero contact angle, pulling a tall concave water column upward against gravity"
    ),
    SOAPY_WATER(
        label = "Soapy Water",
        title = "Surfactant Detergent Solution",
        liquidName = "Water + Surfactant",
        surfaceTensionMNm = 28.0f,
        contactAngleDeg = 20f,
        densityKgM3 = 1000f,
        primaryRadiusMm = 0.5f,
        isDepression = false,
        liquidColor = Color(0xFF06B6D4),
        desc = "Surfactants disrupt hydrogen bonding, slashing surface tension by 60% and halving the capillary rise height"
    ),
    ETHANOL(
        label = "Ethanol",
        title = "Ethanol / Isopropyl Alcohol",
        liquidName = "Pure Ethanol",
        surfaceTensionMNm = 22.3f,
        contactAngleDeg = 0f,
        densityKgM3 = 789f,
        primaryRadiusMm = 0.5f,
        isDepression = false,
        liquidColor = Color(0xFFA855F7),
        desc = "Low density partially compensates for modest surface tension, displaying rapid wetting and climbing"
    ),
    MERCURY(
        label = "Mercury",
        title = "Liquid Mercury (Capillary Depression)",
        liquidName = "Mercury (Hg)",
        surfaceTensionMNm = 486.0f,
        contactAngleDeg = 138f,
        densityKgM3 = 13546f,
        primaryRadiusMm = 0.5f,
        isDepression = true,
        liquidColor = Color(0xFF94A3B8),
        desc = "Strong metallic cohesion exceeds glass adhesion (θ = 138°), forming a convex dome pushed DOWN below the basin"
    ),
    TREE_XYLEM(
        label = "Tree Xylem",
        title = "Botanical Micro-Capillary Tube",
        liquidName = "Sap Solution",
        surfaceTensionMNm = 72.0f,
        contactAngleDeg = 5f,
        densityKgM3 = 1000f,
        primaryRadiusMm = 0.15f,
        isDepression = false,
        liquidColor = EmeraldNeon,
        desc = "Microscopic diameter generates massive capillary pull, helping draw water up towering redwood trees"
    )
}

@Composable
fun CapillaryActionExperiment(
    modifier: Modifier = Modifier
) {
    // Current Preset
    var currentPreset by remember { mutableStateOf(CapillaryPreset.WATER_GLASS) }

    // Physical Parameters
    var surfaceTensionMNm by remember { mutableStateOf(currentPreset.surfaceTensionMNm) }
    var contactAngleDeg by remember { mutableStateOf(currentPreset.contactAngleDeg) }
    var densityKgM3 by remember { mutableStateOf(currentPreset.densityKgM3) }
    var primaryRadiusMm by remember { mutableStateOf(currentPreset.primaryRadiusMm) }

    // Simulation Toggles
    var showMultiTubes by remember { mutableStateOf(true) }
    var showForceVectors by remember { mutableStateOf(true) }
    var showLaplaceDeficit by remember { mutableStateOf(true) }
    var climbProgress by remember { mutableStateOf(1.0f) }
    var isRunning by remember { mutableStateOf(true) }

    fun applyPreset(preset: CapillaryPreset) {
        currentPreset = preset
        surfaceTensionMNm = preset.surfaceTensionMNm
        contactAngleDeg = preset.contactAngleDeg
        densityKgM3 = preset.densityKgM3
        primaryRadiusMm = preset.primaryRadiusMm
        climbProgress = 0.1f
    }

    // Mathematical Physics Calculations (Jurin's Law)
    val gammaNPerM = surfaceTensionMNm * 1e-3f // N/m
    val thetaRad = contactAngleDeg * (PI.toFloat() / 180f)
    val cosTheta = cos(thetaRad)
    val g = 9.80665f // m/s^2
    val radiusM = primaryRadiusMm * 1e-3f

    // 1. Jurin's Law: h = (2 * gamma * cos(theta)) / (rho * g * r)
    val equilibriumHeightM = (2f * gammaNPerM * cosTheta) / max(densityKgM3 * g * radiusM, 1e-8f)
    val equilibriumHeightMm = equilibriumHeightM * 1000f
    val currentHeightMm = equilibriumHeightMm * climbProgress

    // 2. Young-Laplace Pressure Jump: Delta P = (2 * gamma * cos(theta)) / r
    val laplacePressurePa = (2f * gammaNPerM * cosTheta) / max(radiusM, 1e-6f)

    // 3. Capillary Upward Force: F = 2 * pi * r * gamma * cos(theta)
    val capillaryForceN = 2f * PI.toFloat() * radiusM * gammaNPerM * cosTheta
    val capillaryForceMicroN = capillaryForceN * 1_000_000f

    val isWetting = contactAngleDeg < 90f

    // Dynamic climbing transition (Washburn-like climbing animation)
    LaunchedEffect(currentPreset, surfaceTensionMNm, contactAngleDeg, primaryRadiusMm, isRunning) {
        if (!isRunning) return@LaunchedEffect
        climbProgress = 0.05f
        val startTime = withFrameNanos { it }
        while (climbProgress < 0.999f) {
            val now = withFrameNanos { it }
            val dt = ((now - startTime) / 1_000_000_000f)
            // Asymptotic exponential approach to equilibrium
            val rate = 3.5f
            climbProgress = (1f - exp(-rate * dt)).coerceIn(0.05f, 1f)
            if (climbProgress >= 0.995f) {
                climbProgress = 1.0f
                break
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag slider to narrow tube radius: capillary rise skyrockets inversely with 1/r (Jurin's Law)!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Horizontal drag adjusts tube radius
                            primaryRadiusMm = (primaryRadiusMm + dragAmount.x * 0.005f).coerceIn(0.10f, 2.50f)
                            // Vertical drag adjusts contact angle
                            contactAngleDeg = (contactAngleDeg - dragAmount.y * 0.4f).coerceIn(0f, 150f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val xNorm = offset.x / size.width
                            if (xNorm in 0.35f..0.65f) {
                                // Tap center tube: toggle re-dip climb
                                climbProgress = 0.05f
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated apparatus center: liquid basin surface at cy = h * 0.52f
                val basinSurfaceY = h * 0.52f
                val basinBottomY = h * 0.76f
                val basinLeftX = w * 0.08f
                val basinRightX = w * 0.92f
                val basinW = basinRightX - basinLeftX

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

                // 1. Transparent Glass Basin Reservoir Fill
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            currentPreset.liquidColor.copy(alpha = 0.55f),
                            currentPreset.liquidColor.copy(alpha = 0.80f),
                            Color(0xFF0F172A)
                        ),
                        startY = basinSurfaceY,
                        endY = basinBottomY
                    ),
                    topLeft = Offset(basinLeftX, basinSurfaceY),
                    size = Size(basinW, basinBottomY - basinSurfaceY),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Basin Outer Glass Rim
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.7f),
                    topLeft = Offset(basinLeftX - 3f, basinSurfaceY - 2f),
                    size = Size(basinW + 6f, basinBottomY - basinSurfaceY + 4f),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(2.2f)
                )

                // Basin Fluid Level Meniscus Line
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(basinLeftX, basinSurfaceY),
                    end = Offset(basinRightX, basinSurfaceY),
                    strokeWidth = 1.8f
                )

                // 2. Multi-Tube Array or Single Tube Focus
                // Define capillary tubes array: Radii in mm
                val tubesRadii = if (showMultiTubes) {
                    listOf(0.20f, 0.40f, primaryRadiusMm, 1.20f, 2.00f)
                } else {
                    listOf(primaryRadiusMm)
                }

                val tubeTopY = h * 0.12f
                val tubeBottomY = basinBottomY - 14f

                // Scale factor: pixels per mm of capillary rise
                val pxPerMm = 3.6f

                val tubeCenters = if (showMultiTubes) {
                    val count = tubesRadii.size
                    List(count) { i -> basinLeftX + basinW * ((i + 1) / (count + 1).toFloat()) }
                } else {
                    listOf(w * 0.50f)
                }

                // Draw Hyperbolic Envelope Curve if multi-tubes active
                if (showMultiTubes && tubesRadii.size > 1) {
                    val envelopePath = Path()
                    var firstPt = true
                    for (i in tubesRadii.indices) {
                        val rMm = tubesRadii[i]
                        val rM = rMm * 1e-3f
                        val hEqM = (2f * gammaNPerM * cosTheta) / max(densityKgM3 * g * rM, 1e-8f)
                        val hEqMm = hEqM * 1000f * climbProgress
                        val menY = (basinSurfaceY - hEqMm * pxPerMm).coerceIn(tubeTopY + 10f, basinBottomY - 10f)
                        val tx = tubeCenters[i]
                        if (firstPt) {
                            envelopePath.moveTo(tx, menY)
                            firstPt = false
                        } else {
                            envelopePath.lineTo(tx, menY)
                        }
                    }
                    drawPath(
                        path = envelopePath,
                        color = AmberVibrant.copy(alpha = 0.45f),
                        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
                    )
                }

                // Draw each glass capillary tube
                for (i in tubesRadii.indices) {
                    val rMm = tubesRadii[i]
                    val isPrimary = (rMm == primaryRadiusMm)
                    val tx = tubeCenters[i]

                    // Visual tube inner width
                    val tubeInnerW = (rMm * 24f).coerceIn(8f, 48f)
                    val tubeWallThick = 2.4f

                    // Capillary height for this tube
                    val rM = rMm * 1e-3f
                    val hEqM = (2f * gammaNPerM * cosTheta) / max(densityKgM3 * g * rM, 1e-8f)
                    val hEqMm = hEqM * 1000f * climbProgress
                    val meniscusCenterY = (basinSurfaceY - hEqMm * pxPerMm).coerceIn(tubeTopY + 12f, basinBottomY - 8f)

                    // Liquid column inside tube
                    val columnTopY = min(meniscusCenterY, basinSurfaceY)
                    val columnBottomY = max(meniscusCenterY, basinSurfaceY)

                    // Column fluid fill
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                currentPreset.liquidColor.copy(alpha = if (isPrimary) 0.90f else 0.70f),
                                currentPreset.liquidColor.copy(alpha = 0.55f)
                            ),
                            startY = columnTopY,
                            endY = basinBottomY - 6f
                        ),
                        topLeft = Offset(tx - tubeInnerW * 0.5f, columnTopY),
                        size = Size(tubeInnerW, (basinBottomY - 6f) - columnTopY)
                    )

                    // Meniscus Curvature (Concave vs Convex Dome)
                    val meniscusH = (tubeInnerW * 0.35f * abs(cosTheta)).coerceIn(2f, 14f)
                    val meniscusPath = Path().apply {
                        val leftX = tx - tubeInnerW * 0.5f
                        val rightX = tx + tubeInnerW * 0.5f

                        if (isWetting) {
                            // Concave meniscus: dips down in the middle
                            moveTo(leftX, meniscusCenterY - meniscusH * 0.5f)
                            quadraticTo(tx, meniscusCenterY + meniscusH * 0.5f, rightX, meniscusCenterY - meniscusH * 0.5f)
                        } else {
                            // Convex meniscus (Mercury): bulges up in the middle
                            moveTo(leftX, meniscusCenterY + meniscusH * 0.5f)
                            quadraticTo(tx, meniscusCenterY - meniscusH * 0.5f, rightX, meniscusCenterY + meniscusH * 0.5f)
                        }
                    }
                    drawPath(
                        path = meniscusPath,
                        color = Color.White,
                        style = Stroke(width = 2.0f, cap = StrokeCap.Round)
                    )

                    // Glass tube walls (Transparent cylinder with neon edge)
                    val glassColor = if (isPrimary) CyanNeon else Color.White.copy(alpha = 0.6f)
                    // Left wall
                    drawLine(
                        color = glassColor,
                        start = Offset(tx - tubeInnerW * 0.5f, tubeTopY),
                        end = Offset(tx - tubeInnerW * 0.5f, tubeBottomY),
                        strokeWidth = tubeWallThick,
                        cap = StrokeCap.Round
                    )
                    // Right wall
                    drawLine(
                        color = glassColor,
                        start = Offset(tx + tubeInnerW * 0.5f, tubeTopY),
                        end = Offset(tx + tubeInnerW * 0.5f, tubeBottomY),
                        strokeWidth = tubeWallThick,
                        cap = StrokeCap.Round
                    )

                    // Tube Top Rim Collar
                    drawOval(
                        color = glassColor,
                        topLeft = Offset(tx - tubeInnerW * 0.5f - 1.5f, tubeTopY - 3f),
                        size = Size(tubeInnerW + 3f, 6f),
                        style = Stroke(1.2f)
                    )

                    // Primary Tube Highlights & Measurement Scale
                    if (isPrimary) {
                        // Vertical Height Dimension Line & Arrow
                        val dimLineX = tx + tubeInnerW * 0.5f + 14f
                        val diffDash = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)

                        // Baseline at basin level
                        drawLine(Color.White.copy(alpha = 0.5f), Offset(tx - tubeInnerW * 0.5f, basinSurfaceY), Offset(dimLineX + 8f, basinSurfaceY), 1f, pathEffect = diffDash)
                        // Meniscus level line
                        drawLine(Color.White.copy(alpha = 0.5f), Offset(tx, meniscusCenterY), Offset(dimLineX + 8f, meniscusCenterY), 1f, pathEffect = diffDash)

                        // Double arrow for h
                        if (abs(basinSurfaceY - meniscusCenterY) > 8f) {
                            val arrowCol = if (isWetting) CyanNeon else CoralNeon
                            drawLine(arrowCol, Offset(dimLineX, basinSurfaceY), Offset(dimLineX, meniscusCenterY), 1.8f)
                            drawCircle(arrowCol, 2.2f, Offset(dimLineX, basinSurfaceY))
                            drawCircle(arrowCol, 2.2f, Offset(dimLineX, meniscusCenterY))
                        }

                        // Surface Tension Contact Angle Vector Arrows on Meniscus Edge
                        if (showForceVectors) {
                            val arrowLen = 18f
                            val leftEdgeX = tx - tubeInnerW * 0.5f
                            val rightEdgeX = tx + tubeInnerW * 0.5f
                            val edgeY = if (isWetting) meniscusCenterY - meniscusH * 0.5f else meniscusCenterY + meniscusH * 0.5f

                            // Angle theta measured from vertical wall
                            val sign = if (isWetting) -1f else 1f
                            val vecAngle = (thetaRad * sign)

                            // Left tension vector
                            val lEndX = leftEdgeX + sin(vecAngle) * arrowLen
                            val lEndY = edgeY - cos(vecAngle) * arrowLen
                            drawLine(AmberVibrant, Offset(leftEdgeX, edgeY), Offset(lEndX, lEndY), 2.2f, StrokeCap.Round)

                            // Right tension vector
                            val rEndX = rightEdgeX - sin(vecAngle) * arrowLen
                            val rEndY = edgeY - cos(vecAngle) * arrowLen
                            drawLine(AmberVibrant, Offset(rightEdgeX, edgeY), Offset(rEndX, rEndY), 2.2f, StrokeCap.Round)
                        }

                        // Laplace Pressure Deficit Indicator
                        if (showLaplaceDeficit && abs(laplacePressurePa) > 10f) {
                            val badgeY = if (isWetting) meniscusCenterY + 18f else meniscusCenterY - 18f
                            drawCircle(
                                color = if (isWetting) CyanNeon.copy(alpha = 0.25f) else CoralNeon.copy(alpha = 0.25f),
                                radius = 7f,
                                center = Offset(tx, badgeY)
                            )
                            drawCircle(
                                color = if (isWetting) CyanNeon else CoralNeon,
                                radius = 2.5f,
                                center = Offset(tx, badgeY)
                            )
                        }
                    }
                }
            }
        },
        hudContent = {
            // Rule: Transparent HUD card so physical apparatus shines through!
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 41: Capillary Action & Meniscus Rise",
                items = listOf(
                    "Jurin's Law" to "h = (2·γ·cos θ) / (ρ·g·r)",
                    "Capillary Rise (h)" to "${round(currentHeightMm * 10f) / 10f} mm",
                    "Tube Radius (r)" to "${round(primaryRadiusMm * 100f) / 100f} mm",
                    "Surface Tension (γ)" to "${round(surfaceTensionMNm * 10f) / 10f} mN/m",
                    "Contact Angle (θ)" to "${round(contactAngleDeg)}° (${if (isWetting) "Wetting" else "Non-Wetting"})",
                    "Laplace Pressure (ΔP)" to "${round(laplacePressurePa)} Pa",
                    "Capillary Force (F)" to "${round(capillaryForceMicroN * 10f) / 10f} µN",
                    "Fluid Density (ρ)" to "${round(densityKgM3)} kg/m³"
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
                    items(CapillaryPreset.values()) { preset ->
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

                // Slider Pair 1: Tube Radius r & Surface Tension gamma
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Tube Radius (r)",
                        value = primaryRadiusMm,
                        range = 0.10f..2.50f,
                        valueDisplay = "${round(primaryRadiusMm * 100f) / 100f} mm",
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { primaryRadiusMm = it }
                    )
                    PhysicsSliderControl(
                        title = "Surface Tension (γ)",
                        value = surfaceTensionMNm,
                        range = 20f..500f,
                        valueDisplay = "${round(surfaceTensionMNm * 10f) / 10f} mN/m",
                        accentColor = AmberVibrant,
                        modifier = Modifier.weight(1f),
                        onValueChange = { surfaceTensionMNm = it }
                    )
                }

                // Slider Pair 2: Contact Angle theta & Fluid Density rho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Contact Angle (θ)",
                        value = contactAngleDeg,
                        range = 0f..150f,
                        valueDisplay = "${round(contactAngleDeg)}°",
                        accentColor = EmeraldNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { contactAngleDeg = it }
                    )
                    PhysicsSliderControl(
                        title = "Density (ρ)",
                        value = densityKgM3,
                        range = 700f..14000f,
                        valueDisplay = "${round(densityKgM3)} kg/m³",
                        accentColor = PurpleNeon,
                        modifier = Modifier.weight(1f),
                        onValueChange = { densityKgM3 = it }
                    )
                }

                // Control Action Toggles & Reset Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dip / Climb Action Button
                    Button(
                        onClick = { climbProgress = 0.05f },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1.1f)
                    ) {
                        Text(
                            text = "💧 Re-Dip Tube",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Multi-Tube Array Toggle
                    FilledTonalButton(
                        onClick = { showMultiTubes = !showMultiTubes },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (showMultiTubes) CyanNeon.copy(alpha = 0.20f) else ScienceDarkSurfaceVariant,
                            contentColor = if (showMultiTubes) CyanNeon else TextSecondary
                        ),
                        border = BorderStroke(1.dp, if (showMultiTubes) CyanNeon.copy(alpha = 0.6f) else ScienceBorder),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1f)
                    ) {
                        Text(
                            text = if (showMultiTubes) "📊 5 Tubes" else "📊 1 Tube",
                            fontSize = 10.sp
                        )
                    }

                    // Force Vectors Toggle
                    FilledTonalButton(
                        onClick = { showForceVectors = !showForceVectors },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (showForceVectors) AmberVibrant.copy(alpha = 0.20f) else ScienceDarkSurfaceVariant,
                            contentColor = if (showForceVectors) AmberVibrant else TextSecondary
                        ),
                        border = BorderStroke(1.dp, if (showForceVectors) AmberVibrant.copy(alpha = 0.6f) else ScienceBorder),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).weight(1f)
                    ) {
                        Text(
                            text = if (showForceVectors) "⚡ Vectors" else "⚡ OFF",
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
