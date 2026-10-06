package com.geosid.simplephysics.ui.experiments.week5.Day30

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.drawscope.Fill
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

enum class SoapFilmPreset(
    val label: String,
    val filmIndex: Float,
    val baseThicknessNm: Float,
    val incidentAngleDeg: Float,
    val isMonochromatic: Boolean,
    val desc: String
) {
    SOAP_BUBBLE("Soap Bubble", 1.333f, 380f, 15f, false, "Vertical soap film thinning under gravity (n=1.33)"),
    OIL_SLICK("Oil Slick", 1.450f, 260f, 20f, false, "Hydrocarbon film with high refractive index (n=1.45)"),
    BLACK_FILM("Black Film Limit", 1.333f, 25f, 0f, false, "Sub-30nm film before rupture: 100% destructive interference"),
    SODIUM_MONOCHROME("Sodium D (589nm)", 1.333f, 450f, 0f, true, "Monochromatic sodium doublet producing sharp fringes")
}

@Composable
fun ThinFilmInterferenceExperiment(
    modifier: Modifier = Modifier
) {
    // Optical & Fluid State
    var filmIndex by remember { mutableStateOf(1.333f) } // Refractive index of soap water
    var filmThicknessNm by remember { mutableStateOf(380f) } // Base thickness in nanometers
    var incidentAngleDeg by remember { mutableStateOf(15f) } // Angle of incidence in degrees
    var isMonochromatic by remember { mutableStateOf(false) } // Monochromatic 589nm vs White Light
    var drainageRate by remember { mutableStateOf(1.0f) } // Gravitational drainage multiplier
    var selectedPreset by remember { mutableStateOf<SoapFilmPreset?>(SoapFilmPreset.SOAP_BUBBLE) }
    var isRunning by remember { mutableStateOf(true) }
    var showRayDiagram by remember { mutableStateOf(true) }
    var simTime by remember { mutableStateOf(0f) }

    // Interactive ripple perturbation from touch
    var rippleX by remember { mutableStateOf(0.5f) }
    var rippleY by remember { mutableStateOf(0.5f) }
    var rippleAmp by remember { mutableStateOf(0f) }

    // Continuous dynamic animation loop for film drainage & fluid ripples
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
                    if (rippleAmp > 0.001f) {
                        rippleAmp = (rippleAmp - dt * 0.8f).coerceAtLeast(0f)
                    }
                }
            }
        }
    }

    // Physical Calculations for telemetry & code synchronization
    val wavelengthNm = if (isMonochromatic) 589f else 550f
    val incidentAngleRad = incidentAngleDeg * (PI.toFloat() / 180f)
    val sinRefracted = (sin(incidentAngleRad) / filmIndex).coerceIn(-1f, 1f)
    val refractedAngleRad = asin(sinRefracted)

    val minThicknessNm = 10f
    val maxThicknessNm = 850f
    val filmHeightPx = 300f
    val coordY = 150f

    // 1. Optical path difference with reflection phase inversion
    val optPathDiff = 2f * filmIndex * filmThicknessNm * cos(refractedAngleRad)
    val orderM = floor((optPathDiff / wavelengthNm) - 0.5f).toInt().coerceAtLeast(0)
    val isConstructive = abs((optPathDiff / wavelengthNm) - (orderM + 0.5f)) < 0.15f

    // 2. Multi-wavelength spectral reflectance and interference intensity
    val phaseShiftRad = (2f * PI.toFloat() * optPathDiff / wavelengthNm) + PI.toFloat()
    val reflectedIntensity = cos(phaseShiftRad * 0.5f).pow(2)

    // 3. Gravitational drainage wedge thickness and zero-order black film
    val localThicknessNm = minThicknessNm + drainageRate * (coordY / filmHeightPx) * maxThicknessNm
    val isBlackFilm = localThicknessNm < 30f && reflectedIntensity < 0.05f

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag across the soap film to induce surface tension waves. Observe rainbow fringes and Newton's black film!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val pos = change.position
                            rippleX = (pos.x / size.width).coerceIn(0f, 1f)
                            rippleY = (pos.y / size.height).coerceIn(0f, 1f)
                            rippleAmp = 1.0f
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            rippleX = (pos.x / size.width).coerceIn(0f, 1f)
                            rippleY = (pos.y / size.height).coerceIn(0f, 1f)
                            rippleAmp = 1.0f
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated apparatus origin (leaves bottom 32% clear for control deck)
                val appCenterY = h * 0.38f

                // Coordinate Grid Background
                drawScientificGrid(w, h)

                // Layout allocation: Soap Film Loop on Left, Ray Diagram Inset on Right
                val loopW = if (showRayDiagram) min(w * 0.44f, 220.dp.toPx()) else min(w * 0.65f, 320.dp.toPx())
                val loopH = min(h * 0.52f, 280.dp.toPx())
                val loopX = if (showRayDiagram) w * 0.28f else w * 0.50f
                val loopY = appCenterY

                // Draw Soap Film Wire Frame & Iridescent Drainage Film
                drawSoapFilmLoop(
                    cx = loopX,
                    cy = loopY,
                    loopW = loopW,
                    loopH = loopH,
                    baseThicknessNm = filmThicknessNm,
                    filmIndex = filmIndex,
                    refractedAngleRad = refractedAngleRad,
                    isMonochromatic = isMonochromatic,
                    simTime = simTime,
                    drainageRate = drainageRate,
                    rippleX = rippleX,
                    rippleY = rippleY,
                    rippleAmp = rippleAmp
                )

                // Draw Ray Tracing Cross-Section Inset if enabled
                if (showRayDiagram) {
                    val insetW = min(w * 0.42f, 200.dp.toPx())
                    val insetH = loopH
                    val insetX = w * 0.74f
                    val insetY = appCenterY

                    drawRayDiagramInset(
                        cx = insetX,
                        cy = insetY,
                        width = insetW,
                        height = insetH,
                        thicknessNm = filmThicknessNm,
                        filmIndex = filmIndex,
                        incidentAngleDeg = incidentAngleDeg,
                        refractedAngleRad = refractedAngleRad,
                        isMonochromatic = isMonochromatic,
                        optPathDiff = optPathDiff,
                        simTime = simTime
                    )
                }

                // Spectrum Reflectance Bar at Bottom of Apparatus
                val specBarW = min(w * 0.88f, 440.dp.toPx())
                val specBarH = 14.dp.toPx()
                val specBarX = (w - specBarW) * 0.5f
                val specBarY = loopY + loopH * 0.5f + 16.dp.toPx()

                drawSpectralReflectanceBar(
                    x = specBarX,
                    y = specBarY,
                    width = specBarW,
                    height = specBarH,
                    filmThicknessNm = filmThicknessNm,
                    filmIndex = filmIndex,
                    refractedAngleRad = refractedAngleRad,
                    isMonochromatic = isMonochromatic
                )
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f),
                title = "Thin-Film Optical Telemetry",
                items = listOf(
                    "Optical Path Diff" to "${round(optPathDiff * 10f) / 10f} nm",
                    "Center Thickness d" to "${round(filmThicknessNm)} nm",
                    "Interference Mode" to if (filmThicknessNm < 30f) "NEWTON'S BLACK FILM" else if (isConstructive) "CONSTRUCTIVE (Bright)" else "DESTRUCTIVE (Dark)",
                    "Refractive Index n" to "${round(filmIndex * 1000f) / 1000f}",
                    "Fringe Order m" to "$orderM (λ=550nm)"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preset Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SoapFilmPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPreset = preset
                                filmIndex = preset.filmIndex
                                filmThicknessNm = preset.baseThicknessNm
                                incidentAngleDeg = preset.incidentAngleDeg
                                isMonochromatic = preset.isMonochromatic
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

                // First Row: Thickness & Refractive Index Sliders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Film Thickness (d)",
                            value = filmThicknessNm,
                            range = 10f..850f,
                            valueDisplay = "${round(filmThicknessNm)} nm",
                            accentColor = CyanNeon,
                            onValueChange = {
                                filmThicknessNm = it
                                selectedPreset = null
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Refractive Index (n)",
                            value = filmIndex,
                            range = 1.15f..1.60f,
                            valueDisplay = "${round(filmIndex * 100f) / 100f}",
                            accentColor = AmberVibrant,
                            onValueChange = {
                                filmIndex = it
                                selectedPreset = null
                            }
                        )
                    }
                }

                // Second Row: Incident Angle & Monochromatic Light Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PhysicsSliderControl(
                            title = "Incident Angle (θi)",
                            value = incidentAngleDeg,
                            range = 0f..60f,
                            valueDisplay = "${round(incidentAngleDeg)}°",
                            accentColor = EmeraldNeon,
                            onValueChange = {
                                incidentAngleDeg = it
                                selectedPreset = null
                            }
                        )
                    }

                    // Illumination Source Selector
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clickable {
                                    isMonochromatic = false
                                    selectedPreset = null
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isMonochromatic) CyanNeon.copy(alpha = 0.3f) else ScienceDarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (!isMonochromatic) CyanNeon else ScienceBorder.copy(alpha = 0.35f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "White Light",
                                    fontSize = 10.sp,
                                    fontWeight = if (!isMonochromatic) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!isMonochromatic) CyanNeon else TextSecondary
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clickable {
                                    isMonochromatic = true
                                    selectedPreset = null
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isMonochromatic) AmberVibrant.copy(alpha = 0.3f) else ScienceDarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (isMonochromatic) AmberVibrant else ScienceBorder.copy(alpha = 0.35f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Sodium (589nm)",
                                    fontSize = 10.sp,
                                    fontWeight = if (isMonochromatic) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isMonochromatic) AmberVibrant else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Action Controls Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberVibrant.copy(alpha = 0.25f) else CyanNeon.copy(alpha = 0.25f),
                            contentColor = if (isRunning) AmberVibrant else CyanNeon
                        ),
                        border = BorderStroke(1.dp, if (isRunning) AmberVibrant else CyanNeon),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause Flow" else "▶ Run Flow",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showRayDiagram = !showRayDiagram },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (showRayDiagram) EmeraldNeon else TextSecondary
                        ),
                        border = BorderStroke(1.dp, if (showRayDiagram) EmeraldNeon.copy(alpha = 0.8f) else ScienceBorder.copy(alpha = 0.35f)),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (showRayDiagram) "Ray Inset: ON" else "Ray Inset: OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = {
                            filmIndex = 1.333f
                            filmThicknessNm = 380f
                            incidentAngleDeg = 15f
                            isMonochromatic = false
                            drainageRate = 1.0f
                            selectedPreset = SoapFilmPreset.SOAP_BUBBLE
                            showRayDiagram = true
                            isRunning = true
                            simTime = 0f
                            rippleAmp = 0f
                        },
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

// -------------------------------------------------------------
// Canvas Graphics & Optics Helper Functions
// -------------------------------------------------------------

private fun DrawScope.drawScientificGrid(w: Float, h: Float) {
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
}

private fun DrawScope.drawSoapFilmLoop(
    cx: Float,
    cy: Float,
    loopW: Float,
    loopH: Float,
    baseThicknessNm: Float,
    filmIndex: Float,
    refractedAngleRad: Float,
    isMonochromatic: Boolean,
    simTime: Float,
    drainageRate: Float,
    rippleX: Float,
    rippleY: Float,
    rippleAmp: Float
) {
    val halfW = loopW * 0.5f
    val halfH = loopH * 0.5f
    val loopRect = Size(loopW, loopH)
    val loopTopLeft = Offset(cx - halfW, cy - halfH)

    // Handle rod extending down from frame
    val handleH = 45.dp.toPx()
    drawLine(
        color = Color(0xFF78909C),
        start = Offset(cx, cy + halfH),
        end = Offset(cx, cy + halfH + handleH),
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = AmberVibrant.copy(alpha = 0.7f),
        start = Offset(cx, cy + halfH),
        end = Offset(cx, cy + halfH + handleH),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Wire Loop Outer Rim Shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = loopTopLeft.copy(y = loopTopLeft.y + 4f),
        size = loopRect,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f)
    )

    // Vertical Drainage Soap Film Interior Bands
    val sliceCount = 65
    val sliceH = loopH / sliceCount

    for (i in 0 until sliceCount) {
        val normY = i.toFloat() / sliceCount // 0 (top) to 1 (bottom)

        // Gravitational thinning: thickness increases down the wedge
        val gradientFactor = 0.08f + 1.84f * normY.pow(1.35f)
        val dynamicDrain = 1.0f + 0.05f * sin(simTime * 2.5f + normY * 10f) * drainageRate

        // Surface ripple displacement
        val distToRipple = abs(normY - rippleY)
        val ripplePerturb = if (rippleAmp > 0f) rippleAmp * 60f * sin(distToRipple * 20f - simTime * 12f) * exp(-distToRipple * 4f) else 0f

        val localD = (baseThicknessNm * gradientFactor * dynamicDrain + ripplePerturb).coerceAtLeast(5f)

        // Spectral color for this slice
        val sliceColor = computeThinFilmColor(
            thicknessNm = localD,
            filmIndex = filmIndex,
            cosThetaT = cos(refractedAngleRad),
            isMonochromatic = isMonochromatic
        )

        val sy = loopTopLeft.y + i * sliceH
        drawRect(
            color = sliceColor,
            topLeft = Offset(loopTopLeft.x + 3f, sy),
            size = Size(loopW - 6f, sliceH + 0.5f)
        )
    }

    // Shimmering Meniscus Sheen overlay
    val sheenGlow = 0.12f + 0.06f * sin(simTime * 3f)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = sheenGlow * 1.5f),
                Color.Transparent,
                Color.White.copy(alpha = sheenGlow * 0.8f)
            ),
            startY = loopTopLeft.y,
            endY = loopTopLeft.y + loopH
        ),
        topLeft = loopTopLeft,
        size = loopRect,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
    )

    // Metal Wire Loop Frame
    drawRoundRect(
        color = ScienceDarkSurfaceVariant,
        topLeft = loopTopLeft,
        size = loopRect,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f),
        style = Stroke(width = 4.5.dp.toPx())
    )
    drawRoundRect(
        color = CyanNeon.copy(alpha = 0.65f),
        topLeft = loopTopLeft,
        size = loopRect,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Top Rupture Warning Tag if near black film limit
    if (baseThicknessNm < 40f) {
        val warningY = loopTopLeft.y + 12.dp.toPx()
        drawCircle(CoralNeon, 4.dp.toPx(), Offset(cx, warningY))
        drawCircle(Color.White, 2.dp.toPx(), Offset(cx, warningY))
    }
}

private fun DrawScope.drawRayDiagramInset(
    cx: Float,
    cy: Float,
    width: Float,
    height: Float,
    thicknessNm: Float,
    filmIndex: Float,
    incidentAngleDeg: Float,
    refractedAngleRad: Float,
    isMonochromatic: Boolean,
    optPathDiff: Float,
    simTime: Float
) {
    val halfW = width * 0.5f
    val halfH = height * 0.5f
    val insetRect = Size(width, height)
    val insetTopLeft = Offset(cx - halfW, cy - halfH)

    // Inset Background Card
    drawRoundRect(
        color = ScienceDarkSurfaceVariant.copy(alpha = 0.85f),
        topLeft = insetTopLeft,
        size = insetRect,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
    )
    drawRoundRect(
        color = ScienceBorder.copy(alpha = 0.45f),
        topLeft = insetTopLeft,
        size = insetRect,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f),
        style = Stroke(width = 1.2.dp.toPx())
    )

    // Film Slab Representation inside Inset
    val slabTopY = cy - height * 0.12f
    val slabBottomY = cy + height * 0.22f
    val slabH = slabBottomY - slabTopY

    // Slab interior volume
    drawRect(
        color = CyanNeon.copy(alpha = 0.12f),
        topLeft = Offset(insetTopLeft.x + 8f, slabTopY),
        size = Size(width - 16f, slabH)
    )

    // Top & Bottom Interfaces
    drawLine(
        color = CyanNeon.copy(alpha = 0.7f),
        start = Offset(insetTopLeft.x + 8f, slabTopY),
        end = Offset(insetTopLeft.x + width - 8f, slabTopY),
        strokeWidth = 2f
    )
    drawLine(
        color = CyanNeon.copy(alpha = 0.7f),
        start = Offset(insetTopLeft.x + 8f, slabBottomY),
        end = Offset(insetTopLeft.x + width - 8f, slabBottomY),
        strokeWidth = 2f
    )

    // Interface 1 (Air to Film, n1 < n2 => π phase shift)
    val hit1X = cx - width * 0.15f
    val hit1Y = slabTopY

    // Incident Ray Incoming
    val rayLen = width * 0.38f
    val inAngleRad = incidentAngleDeg * (PI.toFloat() / 180f)
    val inStartX = hit1X - rayLen * sin(inAngleRad)
    val inStartY = hit1Y - rayLen * cos(inAngleRad)

    val beamColor = if (isMonochromatic) AmberVibrant else Color.White
    drawLine(
        color = beamColor.copy(alpha = 0.9f),
        start = Offset(inStartX, inStartY),
        end = Offset(hit1X, hit1Y),
        strokeWidth = 2.4.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Surface Normal at Interface 1
    val normLen = 22.dp.toPx()
    drawLine(
        color = ScienceBorder.copy(alpha = 0.8f),
        start = Offset(hit1X, hit1Y - normLen),
        end = Offset(hit1X, hit1Y + normLen),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f))
    )

    // Reflected Ray 1 (Phase Shift = π)
    val r1EndX = hit1X + rayLen * sin(inAngleRad)
    val r1EndY = hit1Y - rayLen * cos(inAngleRad)
    drawLine(
        color = CoralNeon.copy(alpha = 0.85f),
        start = Offset(hit1X, hit1Y),
        end = Offset(r1EndX, r1EndY),
        strokeWidth = 2.0.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Marker for π phase shift at interface 1
    drawCircle(CoralNeon, 3.dp.toPx(), Offset(hit1X, hit1Y))

    // Refracted Ray into Film (Snell's Law: n1 sinθ1 = n2 sinθ2)
    val refX = hit1X + slabH * tan(refractedAngleRad)
    val refY = slabBottomY
    drawLine(
        color = EmeraldNeon.copy(alpha = 0.9f),
        start = Offset(hit1X, hit1Y),
        end = Offset(refX, refY),
        strokeWidth = 1.8.dp.toPx()
    )

    // Reflection at Interface 2 (Film to Air, n2 > n3 => 0 phase shift)
    val hit2X = refX + slabH * tan(refractedAngleRad)
    val hit2Y = slabTopY
    drawLine(
        color = EmeraldNeon.copy(alpha = 0.9f),
        start = Offset(refX, refY),
        end = Offset(hit2X, hit2Y),
        strokeWidth = 1.8.dp.toPx()
    )
    drawCircle(EmeraldNeon, 2.5.dp.toPx(), Offset(refX, refY))

    // Exiting Reflected Ray 2 (Interferes with Ray 1)
    val r2EndX = hit2X + rayLen * sin(inAngleRad)
    val r2EndY = hit2Y - rayLen * cos(inAngleRad)
    drawLine(
        color = EmeraldNeon.copy(alpha = 0.85f),
        start = Offset(hit2X, hit2Y),
        end = Offset(r2EndX, r2EndY),
        strokeWidth = 2.0.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Superposition wave interference glow between R1 and R2
    val midInterferenceX = (r1EndX + r2EndX) * 0.5f
    val midInterferenceY = (r1EndY + r2EndY) * 0.5f
    val pulseAlpha = 0.4f + 0.3f * sin(simTime * 6f)
    drawCircle(
        color = CyanNeon.copy(alpha = pulseAlpha),
        radius = 8.dp.toPx(),
        center = Offset(midInterferenceX, midInterferenceY)
    )
}

private fun DrawScope.drawSpectralReflectanceBar(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    filmThicknessNm: Float,
    filmIndex: Float,
    refractedAngleRad: Float,
    isMonochromatic: Boolean
) {
    // Background Frame
    drawRoundRect(
        color = ScienceDarkSurfaceVariant.copy(alpha = 0.7f),
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )

    val segments = 80
    val segW = width / segments
    val cosThetaT = cos(refractedAngleRad)
    val opd = 2f * filmIndex * filmThicknessNm * cosThetaT

    for (s in 0 until segments) {
        val lambdaNm = 400f + (s.toFloat() / segments) * 300f // 400nm to 700nm

        val phase = (2f * PI.toFloat() * opd / lambdaNm) + PI.toFloat()
        val intensity = cos(phase * 0.5f).pow(2)

        val wavelengthColor = if (isMonochromatic) {
            val dist = abs(lambdaNm - 589f)
            if (dist < 15f) AmberVibrant.copy(alpha = intensity) else Color.Transparent
        } else {
            spectralWavelengthToColor(lambdaNm).copy(alpha = intensity.coerceIn(0.05f, 1f))
        }

        drawRect(
            color = wavelengthColor,
            topLeft = Offset(x + s * segW, y),
            size = Size(segW + 0.5f, height)
        )
    }

    drawRoundRect(
        color = ScienceBorder.copy(alpha = 0.4f),
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
        style = Stroke(width = 1f)
    )
}

// -------------------------------------------------------------
// Physical Color & Spectral Synthesis
// -------------------------------------------------------------

private fun computeThinFilmColor(
    thicknessNm: Float,
    filmIndex: Float,
    cosThetaT: Float,
    isMonochromatic: Boolean
): Color {
    val opd = 2f * filmIndex * thicknessNm * cosThetaT

    // Newton's Zero-Order Black Film Limit (d < 30nm => destructive for all visible λ)
    if (thicknessNm < 28f) {
        val blackAlpha = (thicknessNm / 28f).pow(2) * 0.15f
        return Color(0xFF10141A).copy(alpha = 0.85f - blackAlpha)
    }

    if (isMonochromatic) {
        val lambdaNm = 589f // Sodium D line
        val phase = (2f * PI.toFloat() * opd / lambdaNm) + PI.toFloat()
        val intensity = cos(phase * 0.5f).pow(2)
        return AmberVibrant.copy(alpha = intensity.coerceIn(0.08f, 1f))
    }

    // Multi-wavelength RGB synthesis (Red 650nm, Green 532nm, Blue 450nm)
    val rPhase = (2f * PI.toFloat() * opd / 650f) + PI.toFloat()
    val gPhase = (2f * PI.toFloat() * opd / 532f) + PI.toFloat()
    val bPhase = (2f * PI.toFloat() * opd / 450f) + PI.toFloat()

    val ir = cos(rPhase * 0.5f).pow(2)
    val ig = cos(gPhase * 0.5f).pow(2)
    val ib = cos(bPhase * 0.5f).pow(2)

    val red = (0.90f * ir + 0.15f * ig).coerceIn(0f, 1f)
    val green = (0.85f * ig + 0.12f * ib).coerceIn(0f, 1f)
    val blue = (0.95f * ib + 0.10f * ir).coerceIn(0f, 1f)

    return Color(red = red, green = green, blue = blue, alpha = 0.88f)
}

private fun spectralWavelengthToColor(lambdaNm: Float): Color {
    val (r, g, b) = when {
        lambdaNm < 440f -> Pair(0.4f + 0.6f * (lambdaNm - 380f) / 60f, 0.0f) to 1.0f
        lambdaNm < 490f -> Pair(0.0f, (lambdaNm - 440f) / 50f) to 1.0f
        lambdaNm < 510f -> Pair(0.0f, 1.0f) to (-(lambdaNm - 510f) / 20f)
        lambdaNm < 580f -> Pair((lambdaNm - 510f) / 70f, 1.0f) to 0.0f
        lambdaNm < 645f -> Pair(1.0f, -(lambdaNm - 645f) / 65f) to 0.0f
        else -> Pair(1.0f, 0.0f) to 0.0f
    }.let { (rg, b) -> Triple(rg.first, rg.second, b) }

    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f))
}
