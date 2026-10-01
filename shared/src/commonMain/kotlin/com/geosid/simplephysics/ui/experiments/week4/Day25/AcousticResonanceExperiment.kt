package com.geosid.simplephysics.ui.experiments.week4.Day25

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.ui.components.ExperimentHudCard
import com.geosid.simplephysics.ui.components.PhysicsSliderControl
import com.geosid.simplephysics.ui.components.ResetIcon
import com.geosid.simplephysics.ui.components.ResponsiveExperimentContainer
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

enum class GlassMaterial(val label: String, val qFactor: Float, val tensileLimitMpa: Float) {
    LEAD_CRYSTAL("Crystal (Q=2200)", 2200f, 38f),
    BOROSILICATE("Boro (Q=850)", 850f, 52f),
    SODA_LIME("Soda-Lime (Q=280)", 280f, 70f)
}

data class GlassShard(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var vRot: Float,
    val size: Float,
    val points: List<Offset>,
    val color: Color
)

@Composable
fun AcousticResonanceExperiment(
    modifier: Modifier = Modifier
) {
    // Physical constants & system parameters
    val naturalFreq = 556.0f // Natural fundamental eigenfrequency f0 (Hz)

    var driveFreq by remember { mutableStateOf(556.0f) } // Driving audio frequency (Hz)
    var soundLevelDb by remember { mutableStateOf(118f) } // Sound pressure level (dB)
    var selectedMaterial by remember { mutableStateOf(GlassMaterial.LEAD_CRYSTAL) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isSlowMotion by remember { mutableStateOf(true) } // Strobe slow-motion to see rim flexure

    // Dynamic state
    var currentRimDispMm by remember { mutableStateOf(0f) }
    var stressMpa by remember { mutableStateOf(0f) }
    var isShattered by remember { mutableStateOf(false) }
    var shatterTime by remember { mutableStateOf(0f) }
    var timeElapsed by remember { mutableStateOf(0f) }

    // Particle shards for catastrophic shatter
    val shards = remember { mutableStateListOf<GlassShard>() }

    fun resetSimulation() {
        isShattered = false
        shatterTime = 0f
        currentRimDispMm = 0f
        stressMpa = 0f
        shards.clear()
    }

    // High frequency frame animation & physical integration loop
    LaunchedEffect(isSpeakerOn, driveFreq, soundLevelDb, selectedMaterial, isShattered) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) {
                    lastNanos = nanos
                    return@withFrameNanos
                }
                val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastNanos = nanos

                timeElapsed += dt

                if (!isShattered) {
                    // Lorentzian frequency response calculation
                    val q = selectedMaterial.qFactor
                    val deltaF = abs(driveFreq - naturalFreq)
                    val fRatio = driveFreq / naturalFreq
                    // Normalized denominator of driven damped harmonic oscillator
                    val denom = sqrt((1f - fRatio * fRatio).pow(2) + (fRatio / q).pow(2))

                    // Sound pressure amplitude (converting dB to relative driving force)
                    // 100 dB = reference 1.0, +6 dB doubles pressure
                    val pressureFactor = 2.0f.pow((soundLevelDb - 100f) / 6f) * if (isSpeakerOn) 1f else 0f
                    val targetAmpMm = (0.0012f * pressureFactor) / max(0.0004f, denom)

                    // Ring-up time constant tau = Q / (pi * f0)
                    val tau = max(0.08f, q / (PI.toFloat() * naturalFreq))
                    val alpha = (dt / tau).coerceIn(0f, 1f)
                    currentRimDispMm += (targetAmpMm - currentRimDispMm) * alpha

                    // Hoop stress: sigma = Young's Modulus * strain (E ~ 65 GPa for glass)
                    // Strain ~ deltaR / R0; R0 ~ 40mm
                    val strain = (currentRimDispMm / 40.0f)
                    stressMpa = strain * 650f // Scaled MPa index

                    // Tensile fracture check
                    if (stressMpa >= selectedMaterial.tensileLimitMpa) {
                        isShattered = true
                        shatterTime = timeElapsed

                        // Spawn flying shards
                        shards.clear()
                        val numShards = 36
                        for (i in 0 until numShards) {
                            val theta = (i.toFloat() / numShards) * 2f * PI.toFloat()
                            val speed = Random.nextFloat() * 220f + 120f
                            val shardPts = listOf(
                                Offset(0f, 0f),
                                Offset((Random.nextFloat() * 14f - 7f), (Random.nextFloat() * 16f + 6f)),
                                Offset((Random.nextFloat() * 16f + 6f), (Random.nextFloat() * 14f - 7f))
                            )
                            shards.add(
                                GlassShard(
                                    x = 0f,
                                    y = 0f,
                                    vx = cos(theta) * speed + (Random.nextFloat() * 40f - 20f),
                                    vy = sin(theta) * speed - 60f + (Random.nextFloat() * 40f - 20f),
                                    rotation = Random.nextFloat() * 360f,
                                    vRot = (Random.nextFloat() * 500f - 250f),
                                    size = Random.nextFloat() * 6f + 4f,
                                    points = shardPts,
                                    color = if (Random.nextBoolean()) CyanNeon.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }
                } else {
                    // Update flying shards with gravity & air resistance
                    for (shard in shards) {
                        shard.vy += 380f * dt // Gravity
                        shard.vx *= (1f - 0.25f * dt) // Drag
                        shard.x += shard.vx * dt
                        shard.y += shard.vy * dt
                        shard.rotation += shard.vRot * dt
                    }
                }
            }
        }
    }

    val detuning = driveFreq - naturalFreq
    val isNearResonance = abs(detuning) < (naturalFreq / selectedMaterial.qFactor * 2.5f)

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "Tune the acoustic speaker to exactly 556 Hz. When resonant hoop stress exceeds tensile strength, the crystal shatters!",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                val w = size.width
                val h = size.height

                // Elevated center origin to keep lower 35-40% clear for control deck
                val cx = w * 0.58f
                val cy = h * 0.38f
                val glassR = min(w * 0.24f, h * 0.20f)

                // 1. Technical Coordinate Grid Backdrop
                drawScientificGrid(w, h)

                // 2. Speaker Acoustic Transducer (Left Side)
                val speakerX = w * 0.14f
                val speakerY = cy
                drawAcousticSpeaker(
                    x = speakerX,
                    y = speakerY,
                    isOn = isSpeakerOn,
                    freq = driveFreq,
                    amplitude = soundLevelDb,
                    time = timeElapsed,
                    targetX = cx
                )

                if (!isShattered) {
                    // 3. Resonant Sound Pressure Waves propagating toward glass
                    if (isSpeakerOn) {
                        drawAcousticSoundWaves(
                            startX = speakerX + 24.dp.toPx(),
                            startY = speakerY,
                            endX = cx - glassR * 0.8f,
                            freq = driveFreq,
                            spl = soundLevelDb,
                            time = timeElapsed,
                            isResonant = isNearResonance
                        )
                    }

                    // 4. Intact Crystal Wine Glass with Quadrupole Rim Flexure
                    // Visual phase (slow-motion strobe vs true frequency)
                    val visualFreq = if (isSlowMotion) 2.2f else (driveFreq * 0.05f)
                    val flexPhase = timeElapsed * visualFreq * 2f * PI.toFloat()
                    val rimDeflectionPx = (currentRimDispMm * 8.5f).coerceIn(0f, glassR * 0.45f)

                    drawCrystalWineGlass(
                        cx = cx,
                        cy = cy,
                        radius = glassR,
                        deflectionPx = rimDeflectionPx,
                        phase = flexPhase,
                        stressFraction = (stressMpa / selectedMaterial.tensileLimitMpa).coerceIn(0f, 1f),
                        material = selectedMaterial
                    )

                    // 5. Quadrupole Standing Nodal Indicators (4 nodes, 4 antinodes)
                    if (currentRimDispMm > 0.15f) {
                        drawNodalMarkers(
                            cx = cx,
                            cy = cy,
                            radius = glassR,
                            deflectionPx = rimDeflectionPx,
                            phase = flexPhase
                        )
                    }
                } else {
                    // 6. Catastrophic Glass Shatter & Flying Shards
                    drawShatteredGlass(
                        cx = cx,
                        cy = cy,
                        glassR = glassR,
                        shards = shards
                    )
                }
            }
        },
        hudContent = {
            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f),
                title = "Acoustic Resonance Telemetry",
                items = listOf(
                    "Driving Frequency" to "${(round(driveFreq * 10f) / 10f)} Hz",
                    "Natural Eigenmode f₀" to "556.0 Hz",
                    "Detuning Δf" to "${if (detuning >= 0) "+" else ""}${(round(detuning * 10f) / 10f)} Hz",
                    "Q-Factor" to "${selectedMaterial.qFactor.toInt()}",
                    "Rim Amplitude" to "${(round(currentRimDispMm * 100f) / 100f)} mm",
                    "Hoop Stress" to "${(round(stressMpa * 10f) / 10f)} / ${selectedMaterial.tensileLimitMpa.toInt()} MPa",
                    "State" to if (isShattered) "💥 FRACTURED" else if (isNearResonance) "⚡ IN RESONANCE" else "OFF-PEAK"
                )
            )
        },
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Material Selector Chips (Compact 32.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GlassMaterial.values().forEach { mat ->
                        val isSelected = selectedMaterial == mat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyanNeon.copy(alpha = 0.22f) else ScienceDarkSurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CyanNeon else ScienceBorder.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedMaterial = mat
                                    resetSimulation()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mat.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanNeon else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Dual Sliders in a Row to conserve vertical space
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1.15f)) {
                        PhysicsSliderControl(
                            title = "Speaker Tone (f)",
                            value = driveFreq,
                            range = 540.0f..572.0f,
                            valueDisplay = "${(round(driveFreq * 10f) / 10f)} Hz",
                            accentColor = if (isNearResonance) AmberVibrant else CyanNeon,
                            onValueChange = {
                                driveFreq = it
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(0.85f)) {
                        PhysicsSliderControl(
                            title = "Volume (SPL)",
                            value = soundLevelDb,
                            range = 95f..135f,
                            valueDisplay = "${soundLevelDb.toInt()} dB",
                            accentColor = CoralNeon,
                            onValueChange = { soundLevelDb = it }
                        )
                    }
                }

                // Action Buttons Row (34.dp height)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lock to Resonance Button
                    Button(
                        onClick = {
                            driveFreq = naturalFreq
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (driveFreq == naturalFreq) AmberVibrant else ScienceDarkSurfaceVariant,
                            contentColor = if (driveFreq == naturalFreq) ScienceDarkBg else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = "🎯 Lock 556 Hz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Speaker ON/OFF Button
                    Button(
                        onClick = { isSpeakerOn = !isSpeakerOn },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpeakerOn) CyanNeon else ScienceDarkSurfaceVariant,
                            contentColor = if (isSpeakerOn) ScienceDarkBg else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isSpeakerOn) "🔊 Sound ON" else "🔇 Muted",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Strobe Slow-Mo Toggle
                    Button(
                        onClick = { isSlowMotion = !isSlowMotion },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSlowMotion) PurpleNeon.copy(alpha = 0.4f) else ScienceDarkSurfaceVariant,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isSlowMotion) "⏱️ Strobe Slow" else "⚡ Real Speed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }

                    // Reset / Restore Glass Button
                    IconButton(
                        onClick = {
                            resetSimulation()
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ScienceDarkSurfaceVariant)
                            .border(1.dp, ScienceBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        ResetIcon(tint = CyanNeon, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas Drawing Helper Functions
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawScientificGrid(w: Float, h: Float) {
    val gridSpacing = 36.dp.toPx()
    var gx = 0f
    while (gx < w) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.18f),
            start = Offset(gx, 0f),
            end = Offset(gx, h),
            strokeWidth = 0.7f
        )
        gx += gridSpacing
    }
    var gy = 0f
    while (gy < h) {
        drawLine(
            color = ScienceBorder.copy(alpha = 0.18f),
            start = Offset(0f, gy),
            end = Offset(w, gy),
            strokeWidth = 0.7f
        )
        gy += gridSpacing
    }
}

private fun DrawScope.drawAcousticSpeaker(
    x: Float,
    y: Float,
    isOn: Boolean,
    freq: Float,
    amplitude: Float,
    time: Float,
    targetX: Float
) {
    val speakerH = 75.dp.toPx()
    val speakerW = 32.dp.toPx()

    // Speaker Cabinet
    drawRoundRect(
        color = Color(0xFF1E2638),
        topLeft = Offset(x - speakerW * 0.9f, y - speakerH * 0.5f),
        size = Size(speakerW * 0.9f, speakerH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
        style = Fill
    )
    drawRoundRect(
        color = ScienceBorder.copy(alpha = 0.6f),
        topLeft = Offset(x - speakerW * 0.9f, y - speakerH * 0.5f),
        size = Size(speakerW * 0.9f, speakerH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
        style = Stroke(width = 1.2.dp.toPx())
    )

    // Speaker Cone (Conical Horn)
    val hornPath = Path().apply {
        moveTo(x - speakerW * 0.2f, y - speakerH * 0.18f)
        lineTo(x + speakerW * 0.6f, y - speakerH * 0.42f)
        lineTo(x + speakerW * 0.6f, y + speakerH * 0.42f)
        lineTo(x - speakerW * 0.2f, y + speakerH * 0.18f)
        close()
    }
    drawPath(hornPath, color = Color(0xFF2A364F))
    drawPath(hornPath, color = CyanNeon.copy(alpha = 0.8f), style = Stroke(width = 1.5.dp.toPx()))

    // Speaker Diaphragm Dome
    val domeR = 12.dp.toPx()
    drawCircle(
        color = if (isOn) CoralNeon else Color(0xFF455A64),
        radius = domeR,
        center = Offset(x + speakerW * 0.1f, y)
    )

    // Acoustic Output Beam Glow
    if (isOn) {
        val glowBrush = Brush.radialGradient(
            colors = listOf(AmberVibrant.copy(alpha = 0.45f), Color.Transparent),
            center = Offset(x + speakerW * 0.6f, y),
            radius = 35.dp.toPx()
        )
        drawCircle(brush = glowBrush, radius = 35.dp.toPx(), center = Offset(x + speakerW * 0.6f, y))
    }
}

private fun DrawScope.drawAcousticSoundWaves(
    startX: Float,
    startY: Float,
    endX: Float,
    freq: Float,
    spl: Float,
    time: Float,
    isResonant: Boolean
) {
    val numArcs = 7
    val waveSpan = endX - startX
    val wavelength = waveSpan / 4.5f

    for (i in 0 until numArcs) {
        val progress = ((time * 3.5f + i.toFloat() / numArcs) % 1.0f)
        val arcX = startX + progress * waveSpan
        val alpha = (sin(progress * PI.toFloat())).coerceIn(0f, 1f) * (spl / 135f)
        val arcH = (30.dp.toPx() + progress * 60.dp.toPx())

        val arcPath = Path().apply {
            moveTo(arcX, startY - arcH)
            quadraticTo(arcX + 16.dp.toPx(), startY, arcX, startY + arcH)
        }

        drawPath(
            path = arcPath,
            color = if (isResonant) CoralNeon.copy(alpha = alpha * 0.95f) else CyanNeon.copy(alpha = alpha * 0.7f),
            style = Stroke(
                width = if (isResonant) 3.dp.toPx() else 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

private fun DrawScope.drawCrystalWineGlass(
    cx: Float,
    cy: Float,
    radius: Float,
    deflectionPx: Float,
    phase: Float,
    stressFraction: Float,
    material: GlassMaterial
) {
    val stemLength = radius * 1.35f
    val footWidth = radius * 1.1f
    val bowlH = radius * 1.15f
    val rimH = radius * 0.35f

    // 1. Base Foot
    val footY = cy + bowlH + stemLength
    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = Offset(cx - footWidth * 0.5f, footY),
        end = Offset(cx + footWidth * 0.5f, footY),
        strokeWidth = 3.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = CyanNeon.copy(alpha = 0.7f),
        start = Offset(cx - footWidth * 0.5f, footY),
        end = Offset(cx + footWidth * 0.5f, footY),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 2. Slender Stem
    drawLine(
        color = Color.White.copy(alpha = 0.6f),
        start = Offset(cx, cy + bowlH),
        end = Offset(cx, footY),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 3. Glass Bowl Body
    val bowlPath = Path().apply {
        moveTo(cx - radius, cy)
        cubicTo(
            cx - radius * 1.05f, cy + bowlH * 0.7f,
            cx - radius * 0.3f, cy + bowlH,
            cx, cy + bowlH
        )
        cubicTo(
            cx + radius * 0.3f, cy + bowlH,
            cx + radius * 1.05f, cy + bowlH * 0.7f,
            cx + radius, cy
        )
    }
    // Subtle glass body fill
    drawPath(
        path = bowlPath,
        color = CyanNeon.copy(alpha = 0.08f)
    )
    drawPath(
        path = bowlPath,
        color = Color.White.copy(alpha = 0.55f),
        style = Stroke(width = 2.dp.toPx())
    )

    // 4. Deforming Rim (Top Rim Perspective / Quadrupole Eigenmode n=2)
    // r(theta) = R0 + deltaR * cos(2*theta) * cos(phase)
    val rimSteps = 72
    val rimPath = Path()
    val instantaneousDeflection = deflectionPx * cos(phase)

    for (i in 0..rimSteps) {
        val theta = (i.toFloat() / rimSteps) * 2f * PI.toFloat()
        // Quadrupole mode n=2: 4 nodes, 4 antinodes
        val deltaR = instantaneousDeflection * cos(2f * theta)
        val r = radius + deltaR

        // Rim is an ellipse viewed in oblique perspective
        val px = cx + r * cos(theta)
        val py = cy + (r * sin(theta)) * 0.35f // 3D oblique tilt

        if (i == 0) rimPath.moveTo(px, py) else rimPath.lineTo(px, py)
    }
    rimPath.close()

    // Stress Color Gradient: Green/Cyan -> Amber -> Coral/Red near failure
    val stressColor = when {
        stressFraction > 0.85f -> CoralNeon
        stressFraction > 0.55f -> AmberVibrant
        else -> CyanNeon
    }

    // Glowing Rim Aura
    drawPath(
        path = rimPath,
        color = stressColor.copy(alpha = 0.3f * (stressFraction + 0.3f)),
        style = Stroke(width = 6.dp.toPx())
    )
    // Sharp Crystal Rim Edge
    drawPath(
        path = rimPath,
        color = Color.White,
        style = Stroke(width = 2.2.dp.toPx())
    )
    drawPath(
        path = rimPath,
        color = stressColor,
        style = Stroke(width = 1.2.dp.toPx())
    )

    // 5. Stress Micro-Cracks flashing if stress > 85%
    if (stressFraction > 0.85f) {
        val crackAngle = 0f
        val crackR = radius + instantaneousDeflection
        val crackX = cx + crackR * cos(crackAngle)
        val crackY = cy
        drawLine(
            color = CoralNeon,
            start = Offset(crackX, crackY - 6.dp.toPx()),
            end = Offset(crackX, crackY + 14.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.White,
            start = Offset(crackX, crackY + 5.dp.toPx()),
            end = Offset(crackX - 8.dp.toPx(), crackY + 18.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

private fun DrawScope.drawNodalMarkers(
    cx: Float,
    cy: Float,
    radius: Float,
    deflectionPx: Float,
    phase: Float
) {
    // 4 Stationary Nodes at theta = 45, 135, 225, 315 deg (Zero displacement)
    val nodeAngles = listOf(PI.toFloat() * 0.25f, PI.toFloat() * 0.75f, PI.toFloat() * 1.25f, PI.toFloat() * 1.75f)
    for (theta in nodeAngles) {
        val nx = cx + radius * cos(theta)
        val ny = cy + (radius * sin(theta)) * 0.35f
        drawCircle(
            color = EmeraldNeon,
            radius = 3.dp.toPx(),
            center = Offset(nx, ny)
        )
    }

    // 4 Antinodes at theta = 0, 90, 180, 270 deg (Maximum displacement)
    val antinodeAngles = listOf(0f, PI.toFloat() * 0.5f, PI.toFloat(), PI.toFloat() * 1.5f)
    val instantaneousDeflection = deflectionPx * cos(phase)
    for (theta in antinodeAngles) {
        val deltaR = instantaneousDeflection * cos(2f * theta)
        val r = radius + deltaR
        val ax = cx + r * cos(theta)
        val ay = cy + (r * sin(theta)) * 0.35f
        drawCircle(
            color = CoralNeon,
            radius = 3.5.dp.toPx(),
            center = Offset(ax, ay)
        )
    }
}

private fun DrawScope.drawShatteredGlass(
    cx: Float,
    cy: Float,
    glassR: Float,
    shards: List<GlassShard>
) {
    // Broken Stem Base Remains
    val stemLength = glassR * 1.35f
    val footWidth = glassR * 1.1f
    val bowlH = glassR * 1.15f
    val footY = cy + bowlH + stemLength

    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = Offset(cx - footWidth * 0.5f, footY),
        end = Offset(cx + footWidth * 0.5f, footY),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
    // Jagged broken stem tip
    val brokenStemPath = Path().apply {
        moveTo(cx, footY)
        lineTo(cx, cy + bowlH + stemLength * 0.6f)
        lineTo(cx - 3.dp.toPx(), cy + bowlH + stemLength * 0.5f)
        lineTo(cx + 2.dp.toPx(), cy + bowlH + stemLength * 0.45f)
    }
    drawPath(brokenStemPath, color = Color.White.copy(alpha = 0.4f), style = Stroke(width = 2.5.dp.toPx()))

    // Flying Shards & Fragments
    for (shard in shards) {
        val shardX = cx + shard.x
        val shardY = cy + shard.y

        val rad = shard.rotation * PI.toFloat() / 180f
        val cosR = cos(rad)
        val sinR = sin(rad)

        val polyPath = Path()
        shard.points.forEachIndexed { idx, pt ->
            val rotatedX = shardX + (pt.x * cosR - pt.y * sinR)
            val rotatedY = shardY + (pt.x * sinR + pt.y * cosR)
            if (idx == 0) polyPath.moveTo(rotatedX, rotatedY) else polyPath.lineTo(rotatedX, rotatedY)
        }
        polyPath.close()

        drawPath(polyPath, color = shard.color)
        drawPath(polyPath, color = Color.White, style = Stroke(width = 1.dp.toPx()))
    }

    // Central Fracture Flash Shockwave
    drawCircle(
        color = CoralNeon.copy(alpha = 0.2f),
        radius = glassR * 0.6f,
        center = Offset(cx, cy)
    )
}
