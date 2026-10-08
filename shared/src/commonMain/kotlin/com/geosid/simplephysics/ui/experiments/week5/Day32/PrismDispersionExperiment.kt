package com.geosid.simplephysics.ui.experiments.week5.Day32

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
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
 * Optical presets for Day 32: Prism Dispersion & Newton's Rainbow.
 */
enum class PrismPreset(
    val label: String,
    val materialName: String,
    val cauchyA: Float,
    val cauchyB: Float, // in μm²
    val cauchyC: Float, // in μm⁴
    val apexAngleDeg: Float,
    val incidentAngleDeg: Float,
    val isWhiteLight: Boolean,
    val laserWavelengthNm: Float,
    val desc: String
) {
    CROWN_GLASS(
        label = "Crown Glass",
        materialName = "Borosilicate Crown (N-BK7)",
        cauchyA = 1.5046f,
        cauchyB = 0.00420f,
        cauchyC = 0.00015f,
        apexAngleDeg = 60.0f,
        incidentAngleDeg = 49.5f,
        isWhiteLight = true,
        laserWavelengthNm = 532f,
        desc = "Standard optical glass with clean balanced dispersion into Newton's seven spectral hues"
    ),
    FLINT_GLASS(
        label = "Dense Flint",
        materialName = "Heavy Flint (SF11)",
        cauchyA = 1.7300f,
        cauchyB = 0.01340f,
        cauchyC = 0.00060f,
        apexAngleDeg = 60.0f,
        incidentAngleDeg = 54.0f,
        isWhiteLight = true,
        laserWavelengthNm = 532f,
        desc = "High-lead dense glass with strong chromatic dispersion creating an exaggerated rainbow fan"
    ),
    MINIMUM_DEVIATION(
        label = "Min Deviation",
        materialName = "Symmetric Beam Passing",
        cauchyA = 1.5170f,
        cauchyB = 0.00400f,
        cauchyC = 0.00010f,
        apexAngleDeg = 60.0f,
        incidentAngleDeg = 49.4f,
        isWhiteLight = false,
        laserWavelengthNm = 589.3f, // Sodium D-line
        desc = "Symmetric ray path parallel to prism base minimizing net angular deviation"
    ),
    DIAMOND_PRISM(
        label = "Diamond",
        materialName = "Crystalline Carbon",
        cauchyA = 2.3800f,
        cauchyB = 0.01150f,
        cauchyC = 0.00050f,
        apexAngleDeg = 45.0f,
        incidentAngleDeg = 48.0f,
        isWhiteLight = true,
        laserWavelengthNm = 532f,
        desc = "Extreme refractive index producing dramatic bending and brilliant spectral fire"
    ),
    TOTAL_INTERNAL_REFLECTION(
        label = "TIR Trap",
        materialName = "Total Internal Reflection",
        cauchyA = 1.6200f,
        cauchyB = 0.00700f,
        cauchyC = 0.00020f,
        apexAngleDeg = 60.0f,
        incidentAngleDeg = 32.0f,
        isWhiteLight = false,
        laserWavelengthNm = 633f,
        desc = "Shallow entry angle forces internal incidence to exceed the critical angle threshold"
    )
}

/**
 * Maps a light wavelength in nanometers (380 - 780 nm) to a spectral RGB Color.
 */
private fun wavelengthToColor(wavelengthNm: Float): Color {
    val gamma = 0.80f
    val r: Float
    val g: Float
    val b: Float
    when {
        wavelengthNm in 380f..440f -> {
            r = -(wavelengthNm - 440f) / (440f - 380f)
            g = 0.0f
            b = 1.0f
        }
        wavelengthNm in 440f..490f -> {
            r = 0.0f
            g = (wavelengthNm - 440f) / (490f - 440f)
            b = 1.0f
        }
        wavelengthNm in 490f..510f -> {
            r = 0.0f
            g = 1.0f
            b = -(wavelengthNm - 510f) / (510f - 490f)
        }
        wavelengthNm in 510f..580f -> {
            r = (wavelengthNm - 510f) / (580f - 510f)
            g = 1.0f
            b = 0.0f
        }
        wavelengthNm in 580f..645f -> {
            r = 1.0f
            g = -(wavelengthNm - 645f) / (645f - 580f)
            b = 0.0f
        }
        wavelengthNm in 645f..780f -> {
            r = 1.0f
            g = 0.0f
            b = 0.0f
        }
        else -> {
            r = 0f; g = 0f; b = 0f
        }
    }
    val factor = when {
        wavelengthNm in 380f..420f -> 0.35f + 0.65f * (wavelengthNm - 380f) / (420f - 380f)
        wavelengthNm in 420f..700f -> 1.0f
        wavelengthNm in 700f..780f -> 0.35f + 0.65f * (780f - wavelengthNm) / (780f - 700f)
        else -> 0.0f
    }
    return Color(
        red = (r * factor).pow(gamma).coerceIn(0f, 1f),
        green = (g * factor).pow(gamma).coerceIn(0f, 1f),
        blue = (b * factor).pow(gamma).coerceIn(0f, 1f)
    )
}

/**
 * Traced ray data representing a single spectral wavelength traveling through the prism.
 */
private data class TracedSpectralRay(
    val wavelengthNm: Float,
    val color: Color,
    val nLambda: Float,
    val thetaR1Rad: Float,
    val thetaI2Rad: Float,
    val thetaR2Rad: Float,
    val deviationRad: Float,
    val isTIR: Boolean,
    val entryPt: Offset,
    val face2HitPt: Offset,
    val exitPt: Offset,
    val screenHitPt: Offset?
)

@Composable
fun PrismDispersionExperiment(
    modifier: Modifier = Modifier
) {
    // Optical & Physical Parameters
    var cauchyA by remember { mutableStateOf(1.5046f) }
    var cauchyB by remember { mutableStateOf(0.00420f) } // in μm²
    var cauchyC by remember { mutableStateOf(0.00015f) } // in μm⁴
    var apexAngleDeg by remember { mutableStateOf(60.0f) }
    var incidentAngleDeg by remember { mutableStateOf(49.5f) }
    var isWhiteLightMode by remember { mutableStateOf(true) }
    var laserWavelengthNm by remember { mutableStateOf(532.0f) }
    var showNormalsAndArcs by remember { mutableStateOf(true) }
    var showDeviatedAngles by remember { mutableStateOf(true) }
    var selectedPreset by remember { mutableStateOf<PrismPreset?>(PrismPreset.CROWN_GLASS) }
    var isRunning by remember { mutableStateOf(true) }
    var simTime by remember { mutableStateOf(0f) }

    // Interactive Drag Handle Offset (Relative to left side)
    var dragHandleOffset by remember { mutableStateOf<Offset?>(null) }

    // Animation frame loop
    LaunchedEffect(isRunning) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) {
                    lastNanos = nanos
                    return@withFrameNanos
                }
                val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastNanos = nanos
                if (isRunning) {
                    simTime += dt
                }
            }
        }
    }

    // Physical Calculations for HUD & Telemetry
    val apexAngleRad = apexAngleDeg * PI.toFloat() / 180f
    val thetaI1Rad = incidentAngleDeg * PI.toFloat() / 180f

    // 1. Cauchy's dispersion equation: wavelength-dependent refractive index
    val lambdaUm = laserWavelengthNm / 1000f
    val nLambda = cauchyA + (cauchyB / (lambdaUm * lambdaUm)) + (cauchyC / (lambdaUm.pow(4)))

    // 2. Dual-interface refraction: entry angle and internal prism apex constraint
    val sinThetaR1 = sin(thetaI1Rad) / nLambda
    val thetaR1Rad = asin(sinThetaR1.coerceIn(-1f, 1f))
    val thetaI2Rad = apexAngleRad - thetaR1Rad

    // 3. Exit angle, total chromatic deviation and critical angle TIR check
    val sinThetaR2 = nLambda * sin(thetaI2Rad)
    val isTIR = sinThetaR2 >= 1f
    val thetaR2Rad = if (!isTIR) asin(sinThetaR2.coerceIn(-1f, 1f)) else 0f
    val deviationAngleRad = thetaI1Rad + thetaR2Rad - apexAngleRad

    // Spectral extremes for white light dispersion readout
    val lambdaRedUm = 0.700f
    val nRed = cauchyA + (cauchyB / (lambdaRedUm * lambdaRedUm)) + (cauchyC / (lambdaRedUm.pow(4)))
    val sinR1Red = sin(thetaI1Rad) / nRed
    val r1Red = asin(sinR1Red.coerceIn(-1f, 1f))
    val i2Red = apexAngleRad - r1Red
    val sinR2Red = nRed * sin(i2Red)
    val r2Red = if (sinR2Red < 1f) asin(sinR2Red) else 0f
    val devRedDeg = (thetaI1Rad + r2Red - apexAngleRad) * 180f / PI.toFloat()

    val lambdaVioletUm = 0.400f
    val nViolet = cauchyA + (cauchyB / (lambdaVioletUm * lambdaVioletUm)) + (cauchyC / (lambdaVioletUm.pow(4)))
    val sinR1Violet = sin(thetaI1Rad) / nViolet
    val r1Violet = asin(sinR1Violet.coerceIn(-1f, 1f))
    val i2Violet = apexAngleRad - r1Violet
    val sinR2Violet = nViolet * sin(i2Violet)
    val r2Violet = if (sinR2Violet < 1f) asin(sinR2Violet) else 0f
    val devVioletDeg = (thetaI1Rad + r2Violet - apexAngleRad) * 180f / PI.toFloat()
    val angularDispersionDeg = abs(devVioletDeg - devRedDeg)

    // Critical angle for glass-to-air boundary
    val criticalAngleDeg = (asin((1f / nLambda).coerceIn(0f, 1f)) * 180f / PI.toFloat())

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag the laser emitter or angle handle to steer light into the prism. Observe chromatic dispersion & TIR!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val pos = change.position
                            dragHandleOffset = pos

                            // Compute incident angle based on touch position relative to prism entry
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val cx = w * 0.44f
                            val cy = h * 0.40f
                            val prismSide = min(w * 0.36f, h * 0.32f)
                            val prismH = prismSide * cos(apexAngleRad * 0.5f)
                            val v1Y = cy + prismH * 0.45f
                            val v0Y = cy - prismH * 0.55f
                            val entryY = v1Y + (v0Y - v1Y) * 0.46f
                            val prismLeft = cx - prismSide * sin(apexAngleRad * 0.5f) * 0.5f
                            val entryX = prismLeft + (cx - prismLeft) * 0.46f

                            val dx = entryX - pos.x
                            val dy = entryY - pos.y
                            val touchAngle = atan2(dy, dx) * 180f / PI.toFloat()
                            val targetIncDeg = (touchAngle + (90f - apexAngleDeg * 0.5f)).coerceIn(28f, 75f)
                            incidentAngleDeg = round(targetIncDeg * 10f) / 10f
                            selectedPreset = null
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            dragHandleOffset = pos
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val cx = w * 0.44f
                            val cy = h * 0.40f
                            val prismSide = min(w * 0.36f, h * 0.32f)
                            val prismH = prismSide * cos(apexAngleRad * 0.5f)
                            val v1Y = cy + prismH * 0.45f
                            val v0Y = cy - prismH * 0.55f
                            val entryY = v1Y + (v0Y - v1Y) * 0.46f
                            val prismLeft = cx - prismSide * sin(apexAngleRad * 0.5f) * 0.5f
                            val entryX = prismLeft + (cx - prismLeft) * 0.46f

                            val dx = entryX - pos.x
                            val dy = entryY - pos.y
                            val touchAngle = atan2(dy, dx) * 180f / PI.toFloat()
                            val targetIncDeg = (touchAngle + (90f - apexAngleDeg * 0.5f)).coerceIn(28f, 75f)
                            incidentAngleDeg = round(targetIncDeg * 10f) / 10f
                            selectedPreset = null
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // 1. Scientific coordinate grid & Optical Axis Bench
                val gridSpacing = 40.dp.toPx()
                var gx = 0f
                while (gx < w) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.20f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, h),
                        strokeWidth = 0.8f
                    )
                    gx += gridSpacing
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(
                        color = ScienceBorder.copy(alpha = 0.20f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 0.8f
                    )
                    gy += gridSpacing
                }

                // 2. Optical Apparatus Geometry (Elevated origin per AGY rules)
                val cx = w * 0.44f
                val cy = h * 0.40f
                val prismSide = min(w * 0.36f, h * 0.32f)

                val prismH = prismSide * cos(apexAngleRad * 0.5f)
                val halfBase = prismSide * sin(apexAngleRad * 0.5f)

                // Vertices: V0 = Apex, V1 = Left Base, V2 = Right Base
                val v0 = Offset(cx, cy - prismH * 0.55f)
                val v1 = Offset(cx - halfBase, cy + prismH * 0.45f)
                val v2 = Offset(cx + halfBase, cy + prismH * 0.45f)

                // Face 1: Left Face V1 -> V0
                val f1Vec = v0 - v1
                val f1Len = hypot(f1Vec.x, f1Vec.y)
                val f1Unit = Offset(f1Vec.x / f1Len, f1Vec.y / f1Len)
                // Outward normal pointing into air to the left
                val n1Outward = Offset(f1Unit.y, -f1Unit.x)
                val n1Inward = Offset(-n1Outward.x, -n1Outward.y)

                // Face 2: Right Face V0 -> V2
                val f2Vec = v2 - v0
                val f2Len = hypot(f2Vec.x, f2Vec.y)
                val f2Unit = Offset(f2Vec.x / f2Len, f2Vec.y / f2Len)
                // Outward normal pointing into air to the right
                val n2Outward = Offset(-f2Unit.y, f2Unit.x)

                // Entry point on Face 1
                val entryFrac = 0.46f
                val pEntry = v1 + f1Vec * entryFrac

                // Detection Screen on Right
                val screenX = w * 0.88f
                val screenWidthPx = w * 0.045f
                val screenTop = h * 0.08f
                val screenBottom = h * 0.76f

                // Direction of incoming incident ray
                val phiN1Inward = atan2(n1Inward.y, n1Inward.x)
                val phiInc = phiN1Inward - thetaI1Rad
                val dInc = Offset(cos(phiInc), sin(phiInc))

                val beamLength = (pEntry.x - w * 0.06f).coerceAtLeast(100f)
                val sourcePt = pEntry - dInc * beamLength

                // 3. Spectral Ray Tracing
                val wavelengthsToTrace: List<Float> = if (isWhiteLightMode) {
                    listOf(700f, 650f, 610f, 575f, 530f, 490f, 450f, 405f)
                } else {
                    listOf(laserWavelengthNm)
                }

                val tracedRays = mutableListOf<TracedSpectralRay>()

                for (wl in wavelengthsToTrace) {
                    val lUm = wl / 1000f
                    val nVal = cauchyA + (cauchyB / (lUm * lUm)) + (cauchyC / (lUm.pow(4)))
                    val color = if (isWhiteLightMode) wavelengthToColor(wl) else wavelengthToColor(laserWavelengthNm)

                    // Snell at face 1
                    val sinR1 = (sin(thetaI1Rad) / nVal).coerceIn(-1f, 1f)
                    val r1Rad = asin(sinR1)
                    val phiR1 = phiN1Inward - r1Rad
                    val dR1 = Offset(cos(phiR1), sin(phiR1))

                    // Ray line intersection with Face 2 (V0 -> V2)
                    // pEntry + t * dR1 = v0 + u * f2Vec
                    val det = f2Vec.x * dR1.y - f2Vec.y * dR1.x
                    var face2Hit = v0 + f2Vec * 0.5f
                    var hitsFace2 = false

                    if (abs(det) > 1e-5f) {
                        val tRay = (f2Vec.x * (pEntry.y - v0.y) - f2Vec.y * (pEntry.x - v0.x)) / det
                        val uFace = (dR1.x * (pEntry.y - v0.y) - dR1.y * (pEntry.x - v0.x)) / det
                        if (tRay > 0f && uFace in 0f..1.1f) {
                            face2Hit = v0 + f2Vec * uFace.coerceIn(0f, 1f)
                            hitsFace2 = true
                        }
                    }

                    // Face 2 incidence angle: theta_i2 = alpha - theta_r1
                    val thetaI2 = (apexAngleRad - r1Rad).coerceIn(0f, PI.toFloat())
                    val sinR2 = nVal * sin(thetaI2)
                    val tirFlag = sinR2 >= 1f

                    var exitEndPt = face2Hit
                    var screenPt: Offset? = null
                    var r2Rad = 0f
                    var devRad = 0f

                    if (!tirFlag && hitsFace2) {
                        r2Rad = asin(sinR2.coerceIn(-1f, 1f))
                        val phiN2Out = atan2(n2Outward.y, n2Outward.x)
                        val phiExit = phiN2Out + r2Rad
                        val dExit = Offset(cos(phiExit), sin(phiExit))
                        devRad = thetaI1Rad + r2Rad - apexAngleRad

                        // Intersection with screen at screenX
                        if (dExit.x > 0.05f) {
                            val tScreen = (screenX - face2Hit.x) / dExit.x
                            val yScreen = face2Hit.y + dExit.y * tScreen
                            val clampedYScreen = yScreen.coerceIn(screenTop, screenBottom)
                            screenPt = Offset(screenX, clampedYScreen)
                            exitEndPt = screenPt
                        } else {
                            exitEndPt = face2Hit + dExit * (w * 0.4f)
                        }
                    } else if (tirFlag && hitsFace2) {
                        // Total Internal Reflection: bounces down towards prism base
                        val phiN2Out = atan2(n2Outward.y, n2Outward.x)
                        val phiTir = phiN2Out + PI.toFloat() - thetaI2
                        val dTir = Offset(cos(phiTir), sin(phiTir))
                        exitEndPt = face2Hit + dTir * (prismH * 0.7f)
                    }

                    tracedRays.add(
                        TracedSpectralRay(
                            wavelengthNm = wl,
                            color = color,
                            nLambda = nVal,
                            thetaR1Rad = r1Rad,
                            thetaI2Rad = thetaI2,
                            thetaR2Rad = r2Rad,
                            deviationRad = devRad,
                            isTIR = tirFlag,
                            entryPt = pEntry,
                            face2HitPt = face2Hit,
                            exitPt = exitEndPt,
                            screenHitPt = screenPt
                        )
                    )
                }

                // 4. Draw Detection Screen (Right)
                drawRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(screenX, screenTop),
                    size = Size(screenWidthPx, screenBottom - screenTop)
                )
                drawRect(
                    color = ScienceBorder.copy(alpha = 0.6f),
                    topLeft = Offset(screenX, screenTop),
                    size = Size(screenWidthPx, screenBottom - screenTop),
                    style = Stroke(width = 1.2f)
                )

                // Screen Phosphor Rainbow Dispersion Band
                if (isWhiteLightMode && tracedRays.isNotEmpty()) {
                    val validHits = tracedRays.mapNotNull { it.screenHitPt }
                    if (validHits.size >= 2) {
                        val minY = validHits.minOf { it.y }
                        val maxY = validHits.maxOf { it.y }
                        val bandH = (maxY - minY).coerceAtLeast(16f)

                        // Smooth gradient along screen surface
                        val rainbowStops = listOf(
                            0.0f to Color(0xFFFF2A2A), // Red
                            0.18f to Color(0xFFFF9100), // Orange
                            0.36f to Color(0xFFFFEA00), // Yellow
                            0.54f to Color(0xFF00E676), // Green
                            0.72f to Color(0xFF00E5FF), // Cyan
                            0.88f to Color(0xFF2979FF), // Blue
                            1.0f to Color(0xFFB388FF)  // Violet
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                colorStops = rainbowStops.toTypedArray(),
                                startY = minY - 8f,
                                endY = maxY + 8f
                            ),
                            topLeft = Offset(screenX + 2f, minY - 6f),
                            size = Size(screenWidthPx - 4f, bandH + 12f)
                        )
                        // Ambient glow on screen face
                        drawRect(
                            color = Color.White.copy(alpha = 0.25f),
                            topLeft = Offset(screenX + 2f, minY - 6f),
                            size = Size(2f, bandH + 12f)
                        )
                    }
                } else if (!isWhiteLightMode && tracedRays.isNotEmpty()) {
                    val hit = tracedRays.first().screenHitPt
                    if (hit != null) {
                        val laserCol = wavelengthToColor(laserWavelengthNm)
                        drawCircle(
                            color = laserCol.copy(alpha = 0.9f),
                            radius = 6.dp.toPx(),
                            center = hit
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = hit
                        )
                    }
                }

                // 5. Draw Glass Prism Body with Realistic Caustic & Refractive Polish
                val prismPath = Path().apply {
                    moveTo(v0.x, v0.y)
                    lineTo(v2.x, v2.y)
                    lineTo(v1.x, v1.y)
                    close()
                }

                // Prism Translucent Fill
                drawPath(
                    path = prismPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x1F00E5FF),
                            Color(0x0F141C2E),
                            Color(0x2800E5FF)
                        ),
                        start = v0,
                        end = Offset(cx, cy + prismH)
                    )
                )

                // Glass Apex Polish & Highlights
                drawPath(
                    path = prismPath,
                    color = ScienceBorder.copy(alpha = 0.7f),
                    style = Stroke(width = 2.2f)
                )
                drawLine(
                    color = GlassHighlight.copy(alpha = 0.45f),
                    start = v1,
                    end = v0,
                    strokeWidth = 2.8f
                )
                drawLine(
                    color = GlassHighlight.copy(alpha = 0.35f),
                    start = v0,
                    end = v2,
                    strokeWidth = 2.8f
                )

                // 6. Draw Normal Lines & Angular Annotations
                if (showNormalsAndArcs) {
                    val normalLen = 42.dp.toPx()
                    // Normal at Face 1 Entry Point
                    val n1Start = pEntry - n1Outward * normalLen
                    val n1End = pEntry + n1Outward * normalLen
                    drawLine(
                        color = TextMuted.copy(alpha = 0.5f),
                        start = n1Start,
                        end = n1End,
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // Incident Angle Arc at Face 1
                    val arcR = 24.dp.toPx()
                    val startAngleDeg = (atan2(n1Inward.y, n1Inward.x) * 180f / PI.toFloat()) - incidentAngleDeg
                    drawArc(
                        color = AmberVibrant.copy(alpha = 0.7f),
                        startAngle = startAngleDeg,
                        sweepAngle = incidentAngleDeg,
                        useCenter = false,
                        topLeft = Offset(pEntry.x - arcR, pEntry.y - arcR),
                        size = Size(arcR * 2f, arcR * 2f),
                        style = Stroke(width = 1.5f)
                    )

                    // Normal at Face 2 Hit Point
                    if (tracedRays.isNotEmpty()) {
                        val refRay = tracedRays[tracedRays.size / 2]
                        val f2Hit = refRay.face2HitPt
                        val n2Start = f2Hit - n2Outward * normalLen
                        val n2End = f2Hit + n2Outward * normalLen
                        drawLine(
                            color = TextMuted.copy(alpha = 0.45f),
                            start = n2Start,
                            end = n2End,
                            strokeWidth = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }
                }

                // 7. Draw Incident Light Beam (Left Source -> Prism Entry)
                if (isWhiteLightMode) {
                    // Collimated White Light Beam with subtle glow
                    drawLine(
                        color = Color.White.copy(alpha = 0.20f),
                        start = sourcePt,
                        end = pEntry,
                        strokeWidth = 8.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.95f),
                        start = sourcePt,
                        end = pEntry,
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                } else {
                    // Monochromatic Laser Beam
                    val laserColor = wavelengthToColor(laserWavelengthNm)
                    drawLine(
                        color = laserColor.copy(alpha = 0.35f),
                        start = sourcePt,
                        end = pEntry,
                        strokeWidth = 7.dp.toPx()
                    )
                    drawLine(
                        color = laserColor,
                        start = sourcePt,
                        end = pEntry,
                        strokeWidth = 3.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.85f),
                        start = sourcePt,
                        end = pEntry,
                        strokeWidth = 1.2.dp.toPx()
                    )
                }

                // 8. Draw Refracted Internal Rays and Exiting Spectral Fan
                for (ray in tracedRays) {
                    // Internal ray inside glass (Entry -> Face 2)
                    drawLine(
                        color = ray.color.copy(alpha = 0.85f),
                        start = ray.entryPt,
                        end = ray.face2HitPt,
                        strokeWidth = if (isWhiteLightMode) 2.2f else 3.2f,
                        cap = StrokeCap.Round
                    )

                    // Exiting ray outside glass
                    if (!ray.isTIR) {
                        drawLine(
                            color = ray.color.copy(alpha = 0.90f),
                            start = ray.face2HitPt,
                            end = ray.exitPt,
                            strokeWidth = if (isWhiteLightMode) 2.0f else 3.2f,
                            cap = StrokeCap.Round
                        )
                        // Subtle core highlight on exit ray
                        if (!isWhiteLightMode) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.75f),
                                start = ray.face2HitPt,
                                end = ray.exitPt,
                                strokeWidth = 1.2f
                            )
                        }
                    } else {
                        // Total internal reflection ray path
                        drawLine(
                            color = CoralNeon.copy(alpha = 0.9f),
                            start = ray.face2HitPt,
                            end = ray.exitPt,
                            strokeWidth = 2.4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    }
                }

                // 9. Collimator Emitter Housing & Interactive Drag Handle
                val emitterRadius = 14.dp.toPx()
                drawCircle(
                    color = ScienceDarkSurfaceVariant,
                    radius = emitterRadius,
                    center = sourcePt
                )
                drawCircle(
                    color = if (isWhiteLightMode) Color.White else wavelengthToColor(laserWavelengthNm),
                    radius = emitterRadius * 0.55f,
                    center = sourcePt
                )
                drawCircle(
                    color = ScienceBorder,
                    radius = emitterRadius,
                    center = sourcePt,
                    style = Stroke(width = 1.8f)
                )

                // Drag indicator ring
                val pulseRing = (sin(simTime * 4f) * 0.5f + 0.5f) * 6.dp.toPx()
                drawCircle(
                    color = AmberVibrant.copy(alpha = 0.35f),
                    radius = emitterRadius + pulseRing,
                    center = sourcePt,
                    style = Stroke(width = 1.5f)
                )

                // 10. TIR Warning Banner when critical angle exceeded
                if (isTIR) {
                    drawCircle(
                        color = CoralNeon.copy(alpha = 0.25f),
                        radius = 28.dp.toPx(),
                        center = pEntry
                    )
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 32: Prism Dispersion Rainbow",
                items = listOf(
                    "Cauchy Equation" to "n(λ) = A + B / λ² + C / λ⁴",
                    "Incident Angle θ_i" to "${round(incidentAngleDeg * 10f) / 10f}°",
                    "Apex Angle α" to "${round(apexAngleDeg * 10f) / 10f}°",
                    "Refractive Index n" to if (isWhiteLightMode) "${round(nRed * 1000f) / 1000f} (Red) .. ${round(nViolet * 1000f) / 1000f} (Violet)" else "${round(nLambda * 1000f) / 1000f}",
                    "Dispersion Δδ" to "${round(angularDispersionDeg * 100f) / 100f}°",
                    "Critical Angle θ_c" to "${round(criticalAngleDeg * 10f) / 10f}°",
                    "Optical State" to if (isTIR) "TOTAL INTERNAL REFLECTION (TIR)" else "TRANSMITTING SPECTRUM"
                ),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f)
            )
        },
        controlsContent = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ScienceDarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ScienceBorder.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrismPreset.values().forEach { preset ->
                            val isSelected = selectedPreset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPreset = preset
                                    cauchyA = preset.cauchyA
                                    cauchyB = preset.cauchyB
                                    cauchyC = preset.cauchyC
                                    apexAngleDeg = preset.apexAngleDeg
                                    incidentAngleDeg = preset.incidentAngleDeg
                                    isWhiteLightMode = preset.isWhiteLight
                                    laserWavelengthNm = preset.laserWavelengthNm
                                },
                                label = {
                                    Text(
                                        text = preset.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                                    selectedLabelColor = CyanNeon,
                                    containerColor = ScienceDarkSurfaceVariant.copy(alpha = 0.6f),
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = ScienceBorder.copy(alpha = 0.35f),
                                    selectedBorderColor = CyanNeon
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    // Mode Toggle: White Light vs Monochromatic Laser
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                isWhiteLightMode = true
                                selectedPreset = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWhiteLightMode) CyanNeon else ScienceDarkSurfaceVariant,
                                contentColor = if (isWhiteLightMode) ScienceDarkBg else TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text(
                                text = "🌈 White Light Rainbow",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                isWhiteLightMode = false
                                selectedPreset = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isWhiteLightMode) AmberVibrant else ScienceDarkSurfaceVariant,
                                contentColor = if (!isWhiteLightMode) ScienceDarkBg else TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text(
                                text = "🔬 Monochromatic Laser",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Sliders Row 1: Incident Angle & Prism Apex Angle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Incident Angle θ_i",
                                value = incidentAngleDeg,
                                range = 30.0f..75.0f,
                                valueDisplay = "${round(incidentAngleDeg * 10f) / 10f}°",
                                accentColor = AmberVibrant,
                                onValueChange = {
                                    incidentAngleDeg = it
                                    selectedPreset = null
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PhysicsSliderControl(
                                title = "Apex Angle α",
                                value = apexAngleDeg,
                                range = 40.0f..70.0f,
                                valueDisplay = "${round(apexAngleDeg * 10f) / 10f}°",
                                accentColor = CyanNeon,
                                onValueChange = {
                                    apexAngleDeg = it
                                    selectedPreset = null
                                }
                            )
                        }
                    }

                    // Sliders Row 2: Cauchy B Dispersion & Wavelength (or Glass Index A)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isWhiteLightMode) {
                            Box(modifier = Modifier.weight(1f)) {
                                PhysicsSliderControl(
                                    title = "Cauchy Dispersion B",
                                    value = cauchyB,
                                    range = 0.00200f..0.02000f,
                                    valueDisplay = "${round(cauchyB * 100000f) / 100f}k",
                                    accentColor = PurpleNeon,
                                    onValueChange = {
                                        cauchyB = it
                                        selectedPreset = null
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                PhysicsSliderControl(
                                    title = "Base Index A",
                                    value = cauchyA,
                                    range = 1.3000f..2.4000f,
                                    valueDisplay = "${round(cauchyA * 100f) / 100f}",
                                    accentColor = EmeraldNeon,
                                    onValueChange = {
                                        cauchyA = it
                                        selectedPreset = null
                                    }
                                )
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f)) {
                                PhysicsSliderControl(
                                    title = "Laser Wavelength λ",
                                    value = laserWavelengthNm,
                                    range = 380.0f..750.0f,
                                    valueDisplay = "${round(laserWavelengthNm)} nm",
                                    accentColor = wavelengthToColor(laserWavelengthNm),
                                    onValueChange = {
                                        laserWavelengthNm = it
                                        selectedPreset = null
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                PhysicsSliderControl(
                                    title = "Cauchy Dispersion B",
                                    value = cauchyB,
                                    range = 0.00200f..0.02000f,
                                    valueDisplay = "${round(cauchyB * 100000f) / 100f}k",
                                    accentColor = PurpleNeon,
                                    onValueChange = {
                                        cauchyB = it
                                        selectedPreset = null
                                    }
                                )
                            }
                        }
                    }

                    // Action Controls: Pause/Run & Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isRunning = !isRunning },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) AmberVibrant else CyanNeon,
                                contentColor = ScienceDarkBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                        ) {
                            Text(
                                text = if (isRunning) "⏸ Pause" else "▶ Run",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                val defaultP = PrismPreset.CROWN_GLASS
                                selectedPreset = defaultP
                                cauchyA = defaultP.cauchyA
                                cauchyB = defaultP.cauchyB
                                cauchyC = defaultP.cauchyC
                                apexAngleDeg = defaultP.apexAngleDeg
                                incidentAngleDeg = defaultP.incidentAngleDeg
                                isWhiteLightMode = defaultP.isWhiteLight
                                laserWavelengthNm = defaultP.laserWavelengthNm
                                isRunning = true
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(ScienceDarkSurfaceVariant, RoundedCornerShape(8.dp))
                        ) {
                            ResetIcon(tint = CyanNeon, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    )
}
