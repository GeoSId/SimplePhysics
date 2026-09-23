package com.geosid.simplephysics.ui.experiments.week3.Day16

import androidx.compose.animation.core.*
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

/**
 * Standard gyroscope configurations demonstrating angular momentum & precession dynamics.
 */
enum class GyroPreset(
    val title: String,
    val icon: String,
    val massKg: Float,
    val spinRadS: Float,
    val tiltDeg: Float,
    val color: Color
) {
    BICYCLE("Bike Wheel", "🚲", 2.2f, 130f, 15f, CyanNeon),
    LAB_GYRO("Lab Gyro", "🌀", 0.6f, 260f, 25f, AmberVibrant),
    HEAVY_FLYWHEEL("Flywheel", "⚙️", 5.0f, 95f, 10f, PurpleNeon),
    SLOW_TOPPLE("Critical", "⚠️", 2.0f, 32f, 30f, CoralNeon)
}

@Composable
fun GyroscopicPrecessionExperiment(
    modifier: Modifier = Modifier
) {
    // 1. Gyroscope Physical Parameters & State
    var selectedPreset by remember { mutableStateOf<GyroPreset?>(GyroPreset.BICYCLE) }
    var wheelMassKg by remember { mutableStateOf(2.2f) }
    var spinSpeedRadS by remember { mutableStateOf(130f) }
    var axleTiltDeg by remember { mutableStateOf(15f) }
    var showNutation by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(true) }

    // Dynamic angles: precession angle φ and spin angle ψ
    var precessionAngleRad by remember { mutableStateOf(0f) }
    var spinAngleRad by remember { mutableStateOf(0f) }
    var nutationPhaseRad by remember { mutableStateOf(0f) }

    // Geometric constants of the apparatus
    val wheelRadiusM = 0.25f
    val axleLengthM = 0.22f
    val gravity = 9.81f

    // Moment of inertia: I_spin = 0.5 * M * R^2 (cylindrical disk / wheel)
    val momentOfInertia = 0.5f * wheelMassKg * wheelRadiusM * wheelRadiusM
    val angularMomentum = momentOfInertia * spinSpeedRadS

    // Inclination angle α from horizontal in radians
    val tiltRad = (axleTiltDeg * PI / 180f).toFloat()

    // Gravitational torque: τ = M * g * r * cos(α)
    val torqueGrav = wheelMassKg * gravity * axleLengthM * cos(tiltRad).coerceAtLeast(0.01f)

    // Precession angular velocity: Ω_p = τ / L = (M * g * r) / (I * ω)
    val precessionSpeedRadS = (torqueGrav / angularMomentum.coerceAtLeast(0.001f)).coerceIn(0.05f, 12f)
    val precessionPeriodSec = (2f * PI.toFloat()) / precessionSpeedRadS

    // Nutation frequency: ω_nut = (I_spin * ω) / I_perp
    val nutationSpeedRadS = (spinSpeedRadS * 0.85f).coerceIn(10f, 250f)

    // Physics integration loop driven by withFrameNanos
    LaunchedEffect(isRunning, precessionSpeedRadS, spinSpeedRadS, showNutation) {
        var lastTime = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.035f)
            lastTime = now

            if (isRunning) {
                // Update precession azimuth: dφ/dt = Ω_p
                val dPhi = precessionSpeedRadS * dt
                precessionAngleRad = (precessionAngleRad + dPhi) % (2f * PI.toFloat())

                // Update spin angle: dψ/dt = ω
                val dPsi = spinSpeedRadS * dt
                spinAngleRad = (spinAngleRad + dPsi) % (2f * PI.toFloat())

                if (showNutation) {
                    nutationPhaseRad = (nutationPhaseRad + nutationSpeedRadS * dt) % (2f * PI.toFloat())
                }
            }
        }
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "👉 Drag horizontally to rotate / steer precession; drag vertically to change axle tilt angle!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            selectedPreset = null
                            precessionAngleRad = (precessionAngleRad + dragAmount.x * 0.01f) % (2f * PI.toFloat())
                            axleTiltDeg = (axleTiltDeg - dragAmount.y * 0.25f).coerceIn(-40f, 65f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            isRunning = !isRunning
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.50f, h * 0.40f)

                // Effective tilt with nutation ripple
                val currentTiltDeg = if (showNutation) {
                    axleTiltDeg + sin(nutationPhaseRad) * 4.5f
                } else {
                    axleTiltDeg
                }
                val curTiltRad = (currentTiltDeg * PI / 180f).toFloat()

                // Draw background coordinate starfield & floor grid
                drawGyroFloor(center, w, h)

                // Draw central support pedestal and pivot
                drawPedestal(center)

                // 3D Oblique Projection Constants
                val pitch = 0.42f // camera pitch looking down
                val scalePx = min(w * 0.36f, min(h * 0.30f, 145.dp.toPx()))

                // Precession Circular Orbit Guideline (horizontal circle in 3D)
                drawPrecessionOrbit(
                    center = center,
                    axleR = scalePx * cos(curTiltRad),
                    zOffset = -scalePx * sin(curTiltRad) * pitch,
                    pitch = pitch
                )

                // Axle vector in 3D:
                // Axle reaches out at azimuth φ and inclination α
                val cosPhi = cos(precessionAngleRad)
                val sinPhi = sin(precessionAngleRad)
                val cosTilt = cos(curTiltRad)
                val sinTilt = sin(curTiltRad)

                // Axle tip 3D coordinates (relative to pivot):
                val wheelDistScale = 0.72f
                val wheelCenterScreen = Offset(
                    center.x + scalePx * wheelDistScale * cosTilt * cosPhi,
                    center.y + scalePx * wheelDistScale * (cosTilt * sinPhi * pitch - sinTilt * 0.85f)
                )
                val axleTipScreen = Offset(
                    center.x + scalePx * cosTilt * cosPhi,
                    center.y + scalePx * (cosTilt * sinPhi * pitch - sinTilt * 0.85f)
                )

                // 1. Draw Rigid Axle Bar
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.9f),
                    start = center,
                    end = axleTipScreen,
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
                // Axle highlight
                drawLine(
                    color = Color.White,
                    start = center,
                    end = axleTipScreen,
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )

                // 2. Draw Heavy Spinning Wheel at Wheel Center
                val wheelColor = selectedPreset?.color ?: CyanNeon
                drawSpinningFlywheel(
                    center = wheelCenterScreen,
                    radius = 38.dp.toPx(),
                    cosPhi = cosPhi,
                    sinPhi = sinPhi,
                    pitch = pitch,
                    spinAngle = spinAngleRad,
                    wheelColor = wheelColor
                )

                // 3. Draw Vector Arrows:
                // (a) Angular Momentum L (along axle from wheel center outward, CyanNeon)
                drawVectorArrow(
                    start = wheelCenterScreen,
                    end = Offset(
                        wheelCenterScreen.x + 48.dp.toPx() * cosTilt * cosPhi,
                        wheelCenterScreen.y + 48.dp.toPx() * (cosTilt * sinPhi * pitch - sinTilt * 0.85f)
                    ),
                    color = CyanNeon,
                    label = "L = Iω"
                )

                // (b) Gravitational Torque τ (perpendicular to axle in horizontal plane, CoralNeon)
                drawVectorArrow(
                    start = wheelCenterScreen,
                    end = Offset(
                        wheelCenterScreen.x - 42.dp.toPx() * sinPhi,
                        wheelCenterScreen.y + 42.dp.toPx() * cosPhi * pitch
                    ),
                    color = CoralNeon,
                    label = "τ = r × Mg"
                )

                // (c) Precession Angular Velocity Ω_p (straight up from pivot along z-axis, EmeraldNeon)
                drawVectorArrow(
                    start = center,
                    end = Offset(center.x, center.y - 54.dp.toPx()),
                    color = EmeraldNeon,
                    label = "Ω_p = τ / L"
                )

                // (d) Gravity Force Mg (pointing straight down from wheel, dashed CoralNeon)
                drawLine(
                    color = CoralNeon.copy(alpha = 0.55f),
                    start = wheelCenterScreen,
                    end = Offset(wheelCenterScreen.x, wheelCenterScreen.y + 36.dp.toPx()),
                    strokeWidth = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                    cap = StrokeCap.Round
                )

                // 4. Pivot Gimbal Ball
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, AmberVibrant, Color(0xFFE65100)),
                        center = Offset(center.x - 2f, center.y - 2f),
                        radius = 9.dp.toPx()
                    ),
                    radius = 8.dp.toPx(),
                    center = center
                )
            }
        },
        hudContent = {
            val omegaRpm = round(spinSpeedRadS * 60f / (2f * PI.toFloat())).toInt()
            val precDegS = round(precessionSpeedRadS * 180f / PI.toFloat() * 10f) / 10f
            val precPeriodStr = "${round(precessionPeriodSec * 10f) / 10f} s"
            val lDisplay = "${round(angularMomentum * 1000f) / 1000f} kg·m²/s"
            val tauDisplay = "${round(torqueGrav * 100f) / 100f} N·m"

            TransparentTelemetryHud(
                modifier = Modifier.fillMaxWidth(),
                title = "Day 16: Gyroscopic Precession",
                items = listOf(
                    "Spin Speed (ω)" to "$omegaRpm RPM (${round(spinSpeedRadS)} rad/s)",
                    "Precession (Ω_p)" to "$precDegS°/s (T = $precPeriodStr)",
                    "Angular Momentum (L)" to lDisplay,
                    "Gravitational Torque (τ)" to tauDisplay,
                    "Governing Law" to "τ = dL/dt = Ω_p × L"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GyroPreset.entries.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPreset = preset
                                wheelMassKg = preset.massKg
                                spinSpeedRadS = preset.spinRadS
                                axleTiltDeg = preset.tiltDeg
                            },
                            label = {
                                Text(
                                    text = "${preset.icon} ${preset.title}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = preset.color.copy(alpha = 0.25f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ScienceDarkSurface,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. Compact Sliders: Spin Speed & Axle Tilt
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Spin Speed (ω)",
                        value = spinSpeedRadS,
                        range = 25f..320f,
                        valueDisplay = "${round(spinSpeedRadS).toInt()} rad/s",
                        accentColor = CyanNeon,
                        onValueChange = {
                            selectedPreset = null
                            spinSpeedRadS = it
                        }
                    )

                    PhysicsSliderControl(
                        modifier = Modifier.weight(1f),
                        title = "Axle Tilt (α)",
                        value = axleTiltDeg,
                        range = -35f..60f,
                        valueDisplay = "${round(axleTiltDeg).toInt()}°",
                        accentColor = AmberVibrant,
                        onValueChange = {
                            selectedPreset = null
                            axleTiltDeg = it
                        }
                    )
                }

                // 3. Action Buttons & Nutation Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = showNutation,
                            onCheckedChange = { showNutation = it },
                            colors = CheckboxDefaults.colors(checkedColor = PurpleNeon)
                        )
                        Text(
                            text = "Nutation Wobble (Whip)",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
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
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isRunning) "⏸ Pause" else "▶ Run",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedPreset = GyroPreset.BICYCLE
                                wheelMassKg = 2.2f
                                spinSpeedRadS = 130f
                                axleTiltDeg = 15f
                                showNutation = false
                                precessionAngleRad = 0f
                                spinAngleRad = 0f
                                isRunning = true
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .background(ScienceDarkSurfaceVariant, RoundedCornerShape(8.dp))
                        ) {
                            ResetIcon(tint = CyanNeon, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    )
}

// ----------------------------------------------------------------------------
// 3D Canvas Rendering Helpers
// ----------------------------------------------------------------------------

private fun DrawScope.drawGyroFloor(center: Offset, w: Float, h: Float) {
    val floorY = center.y + 70.dp.toPx()
    // Floor grid lines
    val gridCount = 6
    for (i in -gridCount..gridCount) {
        val xOffset = i * 28.dp.toPx()
        drawLine(
            color = ScienceBorder.copy(alpha = 0.15f),
            start = Offset(center.x + xOffset * 1.6f, floorY + 45.dp.toPx()),
            end = Offset(center.x + xOffset * 0.4f, floorY - 30.dp.toPx()),
            strokeWidth = 0.8f
        )
    }
    // Concentric base ellipse rings on the floor
    drawOval(
        color = ScienceBorder.copy(alpha = 0.25f),
        topLeft = Offset(center.x - 90.dp.toPx(), floorY - 18.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(180.dp.toPx(), 36.dp.toPx()),
        style = Stroke(width = 1.2f)
    )
    drawOval(
        color = ScienceBorder.copy(alpha = 0.40f),
        topLeft = Offset(center.x - 45.dp.toPx(), floorY - 9.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(90.dp.toPx(), 18.dp.toPx()),
        style = Stroke(width = 1.6f)
    )
}

private fun DrawScope.drawPedestal(center: Offset) {
    val floorY = center.y + 70.dp.toPx()
    // Support column
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFB0BEC5), Color(0xFF37474F)),
            startY = center.y,
            endY = floorY
        ),
        start = center,
        end = Offset(center.x, floorY),
        strokeWidth = 7.dp.toPx(),
        cap = StrokeCap.Round
    )
    // Metallic edge highlight
    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = Offset(center.x - 1.5f, center.y),
        end = Offset(center.x - 1.5f, floorY),
        strokeWidth = 1.5f
    )
}

private fun DrawScope.drawPrecessionOrbit(
    center: Offset,
    axleR: Float,
    zOffset: Float,
    pitch: Float
) {
    val rX = axleR * 0.72f
    val rY = rX * pitch
    drawOval(
        color = CyanNeon.copy(alpha = 0.25f),
        topLeft = Offset(center.x - rX, center.y + zOffset - rY),
        size = androidx.compose.ui.geometry.Size(rX * 2f, rY * 2f),
        style = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
    )
}

private fun DrawScope.drawSpinningFlywheel(
    center: Offset,
    radius: Float,
    cosPhi: Float,
    sinPhi: Float,
    pitch: Float,
    spinAngle: Float,
    wheelColor: Color
) {
    // Wheel is viewed as an ellipse whose aspect ratio depends on view angle
    val viewNormal = abs(sinPhi) * pitch + abs(cosPhi) * 0.85f
    val rx = radius * (0.35f + 0.65f * viewNormal)
    val ry = radius

    // 1. Wheel Outer Rim with Neon Glow
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(wheelColor.copy(alpha = 0.15f), wheelColor.copy(alpha = 0.7f), Color.White),
            center = center,
            radius = radius
        ),
        topLeft = Offset(center.x - rx, center.y - ry),
        size = androidx.compose.ui.geometry.Size(rx * 2f, ry * 2f),
        style = Stroke(width = 4.dp.toPx())
    )

    // 2. Rotating Wheel Spokes
    val spokeCount = 6
    for (i in 0 until spokeCount) {
        val spokeTh = spinAngle + i * (PI.toFloat() / spokeCount)
        val sx = rx * cos(spokeTh)
        val sy = ry * sin(spokeTh)
        drawLine(
            color = wheelColor.copy(alpha = 0.65f),
            start = Offset(center.x - sx, center.y - sy),
            end = Offset(center.x + sx, center.y + sy),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }

    // 3. Central Wheel Hub
    drawCircle(
        color = Color.White,
        radius = 5.dp.toPx(),
        center = center
    )
    drawCircle(
        color = wheelColor,
        radius = 3.dp.toPx(),
        center = center
    )
}

private fun DrawScope.drawVectorArrow(
    start: Offset,
    end: Offset,
    color: Color,
    label: String
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.01f)
    val uX = dx / len
    val uY = dy / len

    // Main shaft
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    // Arrowhead
    val headSize = 8.dp.toPx()
    val perpX = -uY * headSize * 0.5f
    val perpY = uX * headSize * 0.5f
    val tipPath = Path().apply {
        moveTo(end.x, end.y)
        lineTo(end.x - uX * headSize + perpX, end.y - uY * headSize + perpY)
        lineTo(end.x - uX * headSize - perpX, end.y - uY * headSize - perpY)
        close()
    }
    drawPath(tipPath, color)
}

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
                text = "📊 $title".uppercase(),
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
                    text = "LIVE",
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
