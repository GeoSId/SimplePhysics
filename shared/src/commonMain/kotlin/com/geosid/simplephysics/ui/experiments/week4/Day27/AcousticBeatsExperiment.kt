package com.geosid.simplephysics.ui.experiments.week4.Day27

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
import androidx.compose.ui.geometry.CornerRadius
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
import com.geosid.simplephysics.audio.rememberAcousticTonePlayer
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*

enum class BeatsPreset(val label: String, val f1: Float, val f2: Float, val desc: String) {
    UNISON("Unison (0 Hz)", 440.0f, 440.0f, "Steady stationary pitch"),
    SLOW_BEAT("Tuning Beat (2 Hz)", 440.0f, 442.0f, "Audible slow volume thrum"),
    FAST_FLUTTER("Fast Flutter (6 Hz)", 440.0f, 446.0f, "Rapid tremolo flutter"),
    ROUGHNESS("Roughness (15 Hz)", 440.0f, 455.0f, "Helmholtz sensory roughness")
}

@Composable
fun AcousticBeatsExperiment(
    modifier: Modifier = Modifier
) {
    // Physical state & acoustic source frequencies
    var f1 by remember { mutableStateOf(440.0f) } // Primary tone A4 (Hz)
    var f2 by remember { mutableStateOf(442.0f) } // Secondary tone (Hz)
    var amp1 by remember { mutableStateOf(1.0f) } // Primary amplitude
    var amp2 by remember { mutableStateOf(1.0f) } // Secondary amplitude
    var isRunning by remember { mutableStateOf(true) }
    var showComponents by remember { mutableStateOf(true) }
    var showEnvelope by remember { mutableStateOf(true) }
    var showPhasor by remember { mutableStateOf(true) }
    var selectedPreset by remember { mutableStateOf<BeatsPreset?>(BeatsPreset.SLOW_BEAT) }

    // Live acoustic tone synthesizer
    var isAudioOn by remember { mutableStateOf(false) }
    var masterVolume by remember { mutableStateOf(0.30f) }
    var isBinaural by remember { mutableStateOf(false) }
    val audioPlayer = rememberAcousticTonePlayer()

    LaunchedEffect(isAudioOn, isRunning, f1, f2, amp1, amp2, masterVolume, isBinaural) {
        audioPlayer.update(f1, f2, amp1, amp2, masterVolume, isBinaural)
        if (isAudioOn && isRunning) {
            audioPlayer.play()
        } else {
            audioPlayer.pause()
        }
    }

    // Simulation runtime clock
    var simTime by remember { mutableStateOf(0f) }
    var touchCursorNormX by remember { mutableStateOf<Float?>(null) }

    fun resetSimulation() {
        f1 = 440.0f
        f2 = 442.0f
        amp1 = 1.0f
        amp2 = 1.0f
        simTime = 0f
        isRunning = true
        selectedPreset = BeatsPreset.SLOW_BEAT
        touchCursorNormX = null
    }

    // High performance physical simulation loop
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

    // 1. Classical acoustic beat frequency and carrier pitch
    val beatFreq = abs(f1 - f2)
    val carrierFreq = (f1 + f2) / 2f
    val beatPeriodSec = if (beatFreq > 0.05f) 1.0f / beatFreq else Float.POSITIVE_INFINITY

    // 2. Wave superposition and modulated envelope
    val envelopeAmp = 2f * amp1 * abs(cos(PI.toFloat() * (f1 - f2) * simTime))
    val carrierOsc = sin(2 * PI.toFloat() * carrierFreq * simTime)
    val yCombined = envelopeAmp * carrierOsc

    // 3. Acoustic intensity and sound power modulation
    val intensityNorm = ((yCombined / (amp1 + amp2))).pow(2)
    val peakPowerRatio = ((amp1 + amp2) / (amp1.coerceAtLeast(0.1f))).pow(2)

    val instantaneousPhaseDiff = (2 * PI.toFloat() * (f1 - f2) * simTime) % (2 * PI.toFloat())
    val normalizedPhase = (instantaneousPhaseDiff / (2 * PI.toFloat()) + 1f) % 1f
    val phaseCoherence = cos(PI.toFloat() * (f1 - f2) * simTime).pow(2)

    val auditoryPerception = when {
        beatFreq < 0.1f -> "Pure Unison (No Beats)"
        beatFreq <= 1.0f -> "Gentle Slow Swell (${round(beatFreq * 10f) / 10f} Hz)"
        beatFreq <= 4.0f -> "Classic Tuning Tremolo (${round(beatFreq * 10f) / 10f} Hz)"
        beatFreq <= 10.0f -> "Rapid Throbbing Flutter (${round(beatFreq * 10f) / 10f} Hz)"
        beatFreq <= 20.0f -> "Helmholtz Sensory Roughness (${beatFreq.toInt()} Hz)"
        else -> "Dual Tone Dissonance (${beatFreq.toInt()} Hz)"
    }

    ResponsiveExperimentContainer(
        modifier = modifier,
        instructions = "Adjust the two acoustic frequencies to hear and see acoustic beat modulation, constructive reinforcement, and nodal silence.",
        canvasContent = {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            touchCursorNormX = (change.position.x / size.width).coerceIn(0.08f, 0.95f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            touchCursorNormX = (offset.x / size.width).coerceIn(0.08f, 0.95f)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Elevated apparatus altitude origin (cy = h * 0.40f)
                val cy = h * 0.40f
                val leftX = w * 0.14f
                val rightX = w * 0.96f
                val waveSpan = rightX - leftX

                // 1. Scientific Coordinate Grid & Oscilloscope Reticle
                drawBeatsOscilloscopeGrid(w, h, cy, leftX, rightX)

                // 2. Dual Vibrating Acoustic Transducers / Tuning Forks on Left
                drawDualTuningForks(
                    cy = cy,
                    f1 = f1,
                    f2 = f2,
                    simTime = simTime,
                    isRunning = isRunning,
                    isAudioOn = isAudioOn
                )

                // 3. Oscilloscope Waveforms (Time & Space domain)
                val waveScale = min(w * 0.22f, h * 0.18f)
                val numSamples = 300

                // Simulated spatial display parameters:
                // We map x coordinate across screen to a local phase window
                val visibleCycles = 4.0f
                val baseOmega1 = 2 * PI.toFloat() * (f1 - 440f + 5f) // visually scaled frequency
                val baseOmega2 = 2 * PI.toFloat() * (f2 - 440f + 5f)
                val beatVisualFreq = (f1 - f2) * 0.6f

                val pathWave1 = Path()
                val pathWave2 = Path()
                val pathSum = Path()
                val pathEnvUpper = Path()
                val pathEnvLower = Path()

                for (i in 0..numSamples) {
                    val progress = i / numSamples.toFloat()
                    val px = leftX + progress * waveSpan

                    // Spatial phase argument
                    val phaseSpatial = progress * visibleCycles * 2 * PI.toFloat()
                    val tArg = simTime * 3.5f - phaseSpatial * 0.25f

                    // Component wave values
                    val w1 = amp1 * sin(tArg * (1f + beatVisualFreq * 0.15f) + phaseSpatial)
                    val w2 = amp2 * sin(tArg * (1f - beatVisualFreq * 0.15f) + phaseSpatial)
                    val wSum = w1 + w2

                    // Envelope calculated from theoretical beat modulation
                    val env = sqrt(amp1 * amp1 + amp2 * amp2 + 2 * amp1 * amp2 * cos(beatVisualFreq * tArg + phaseSpatial * 0.3f))

                    val py1 = cy + w1 * waveScale * 0.35f
                    val py2 = cy + w2 * waveScale * 0.35f
                    val pySum = cy + wSum * waveScale * 0.55f
                    val pyEnvUp = cy - env * waveScale * 0.55f
                    val pyEnvDown = cy + env * waveScale * 0.55f

                    if (i == 0) {
                        pathWave1.moveTo(px, py1)
                        pathWave2.moveTo(px, py2)
                        pathSum.moveTo(px, pySum)
                        pathEnvUpper.moveTo(px, pyEnvUp)
                        pathEnvLower.moveTo(px, pyEnvDown)
                    } else {
                        pathWave1.lineTo(px, py1)
                        pathWave2.lineTo(px, py2)
                        pathSum.lineTo(px, pySum)
                        pathEnvUpper.lineTo(px, pyEnvUp)
                        pathEnvLower.lineTo(px, pyEnvDown)
                    }
                }

                // Draw Component Waves in background if enabled
                if (showComponents) {
                    // Wave 1: Cyan Neon
                    drawPath(
                        path = pathWave1,
                        color = CyanNeon.copy(alpha = 0.45f),
                        style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Wave 2: Coral Neon
                    drawPath(
                        path = pathWave2,
                        color = CoralNeon.copy(alpha = 0.45f),
                        style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Draw Outer Modulated Envelope (Dashed Neon Green/Cyan)
                if (showEnvelope) {
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), simTime * 25f)
                    drawPath(
                        path = pathEnvUpper,
                        color = EmeraldNeon.copy(alpha = 0.70f),
                        style = Stroke(width = 1.6.dp.toPx(), pathEffect = dashEffect)
                    )
                    drawPath(
                        path = pathEnvLower,
                        color = EmeraldNeon.copy(alpha = 0.70f),
                        style = Stroke(width = 1.6.dp.toPx(), pathEffect = dashEffect)
                    )
                }

                // Draw Combined Superposed Wave (Glow + Solid Amber)
                drawPath(
                    path = pathSum,
                    color = AmberVibrant.copy(alpha = 0.25f),
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = pathSum,
                    color = AmberVibrant,
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                )

                // 4. Constructive / Destructive Nodes Highlighting
                val nodeCount = 5
                for (n in 0..nodeCount) {
                    val nodeProgress = (n / nodeCount.toFloat() + (simTime * beatFreq * 0.15f) % 0.2f).coerceIn(0.05f, 0.95f)
                    val nx = leftX + nodeProgress * waveSpan
                    val localEnv = abs(cos(PI.toFloat() * (f1 - f2) * (simTime - nodeProgress * 0.5f)))

                    if (localEnv > 0.85f) {
                        // Constructive Antinode (Loudest peak)
                        drawCircle(
                            color = AmberVibrant.copy(alpha = 0.35f),
                            radius = 8.dp.toPx(),
                            center = Offset(nx, cy)
                        )
                        drawCircle(
                            color = AmberVibrant,
                            radius = 3.5.dp.toPx(),
                            center = Offset(nx, cy)
                        )
                    } else if (localEnv < 0.18f && beatFreq > 0.5f) {
                        // Destructive Node (Silence zone)
                        drawCircle(
                            color = CoralNeon.copy(alpha = 0.4f),
                            radius = 6.dp.toPx(),
                            center = Offset(nx, cy),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        drawCircle(
                            color = CoralNeon,
                            radius = 2.5.dp.toPx(),
                            center = Offset(nx, cy)
                        )
                    }
                }

                // 5. Interactive Touch Cursor & Instantaneous Readout
                touchCursorNormX?.let { normX ->
                    val cursorX = w * normX
                    drawLine(
                        color = Color.White.copy(alpha = 0.65f),
                        start = Offset(cursorX, cy - waveScale * 0.9f),
                        end = Offset(cursorX, cy + waveScale * 0.9f),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                    )
                    drawCircle(
                        color = CyanNeon,
                        radius = 4.dp.toPx(),
                        center = Offset(cursorX, cy)
                    )
                }

                // 6. Phasor Sum Circle Gauge (Top Right Corner)
                if (showPhasor) {
                    drawPhasorGauge(
                        w = w,
                        cy = h * 0.16f,
                        f1 = f1,
                        f2 = f2,
                        amp1 = amp1,
                        amp2 = amp2,
                        simTime = simTime
                    )
                }

                // 7. Instantaneous Acoustic Power VU Meter (Right Margin)
                drawAcousticPowerMeter(
                    rightX = w * 0.98f,
                    cy = cy,
                    height = waveScale * 1.5f,
                    powerNorm = phaseCoherence
                )
            }
        },
        hudContent = {
            val beatFreqStr = if (beatFreq < 0.05f) "0.00 Hz (Unison)" else "${round(beatFreq * 100f) / 100f} Hz"
            val beatPeriodStr = if (beatFreq < 0.05f) "∞ (Continuous)" else "${(beatPeriodSec * 1000).toInt()} ms"
            val carrierPitchStr = "${round(carrierFreq * 10f) / 10f} Hz"
            val phaseStr = when {
                phaseCoherence > 0.85f -> "In-Phase (Constructive Max)"
                phaseCoherence < 0.15f && beatFreq > 0.1f -> "Anti-Phase (Destructive Min)"
                else -> "Transitioning (${(phaseCoherence * 100).toInt()}%)"
            }
            val envelopeMaxAmp = "${round((amp1 + amp2) * 100f) / 100f}x"
            val envelopeMinAmp = "${round(abs(amp1 - amp2) * 100f) / 100f}x"

            ExperimentHudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Transparent,
                borderColor = ScienceBorder.copy(alpha = 0.35f),
                title = "Acoustic Beat Interference Telemetry",
                items = listOf(
                    "Beat Frequency f_beat" to beatFreqStr,
                    "Beat Period T_beat" to beatPeriodStr,
                    "Carrier Pitch f_c" to carrierPitchStr,
                    "Interference State" to phaseStr,
                    "Envelope Peak / Trough" to "$envelopeMaxAmp / $envelopeMinAmp",
                    "Audio Synthesizer" to if (!isAudioOn) "Muted (OFF)" else if (isBinaural) "Binaural Stereo (L=f₁, R=f₂)" else "Superposition Mix (${(masterVolume * 100).toInt()}%)",
                    "Auditory Perception" to auditoryPerception
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
                    BeatsPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AmberVibrant.copy(alpha = 0.22f) else ScienceDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AmberVibrant else ScienceBorder.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedPreset = preset
                                    f1 = preset.f1
                                    f2 = preset.f2
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text(
                                    text = preset.label,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AmberVibrant else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Dual Frequency Sliders in Compact Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Frequency f₁ (Tone A)",
                        value = f1,
                        range = 420.0f..460.0f,
                        valueDisplay = "${round(f1 * 10f) / 10f} Hz",
                        accentColor = CyanNeon,
                        onValueChange = {
                            f1 = it
                            selectedPreset = null
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PhysicsSliderControl(
                        title = "Frequency f₂ (Tone B)",
                        value = f2,
                        range = 420.0f..460.0f,
                        valueDisplay = "${round(f2 * 10f) / 10f} Hz",
                        accentColor = CoralNeon,
                        onValueChange = {
                            f2 = it
                            selectedPreset = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dual Amplitude Sliders in Compact Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PhysicsSliderControl(
                        title = "Amplitude A₁",
                        value = amp1,
                        range = 0.2f..1.0f,
                        valueDisplay = "${(amp1 * 100).toInt()}%",
                        accentColor = CyanNeon.copy(alpha = 0.8f),
                        onValueChange = {
                            amp1 = it
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PhysicsSliderControl(
                        title = "Amplitude A₂",
                        value = amp2,
                        range = 0.2f..1.0f,
                        valueDisplay = "${(amp2 * 100).toInt()}%",
                        accentColor = CoralNeon.copy(alpha = 0.8f),
                        onValueChange = {
                            amp2 = it
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Audio Synthesizer Control Deck (Sound Toggle, Stereo/Binaural Mode, Volume)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isAudioOn = !isAudioOn },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAudioOn) EmeraldNeon else ScienceDarkSurfaceVariant,
                            contentColor = if (isAudioOn) ScienceDarkBg else TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.15f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isAudioOn) "🔊 Sound: ON" else "🔇 Sound: OFF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { isBinaural = !isBinaural },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isBinaural) PurpleNeon else ScienceBorder
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isBinaural) "🎧 Binaural" else "📻 Mono",
                            fontSize = 10.sp,
                            color = if (isBinaural) PurpleNeon else TextSecondary
                        )
                    }

                    PhysicsSliderControl(
                        title = "Volume",
                        value = masterVolume,
                        range = 0.05f..1.0f,
                        valueDisplay = "${(masterVolume * 100).toInt()}%",
                        accentColor = if (isAudioOn) EmeraldNeon else TextMuted,
                        onValueChange = { masterVolume = it },
                        modifier = Modifier.weight(1.5f)
                    )
                }

                // Action Buttons Row (Compact height 32..36dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberVibrant else CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isRunning) "⏸ Pause" else "▶ Run",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showComponents = !showComponents },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (showComponents) CyanNeon else ScienceBorder
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (showComponents) "Tones: ON" else "Tones: OFF",
                            fontSize = 10.sp,
                            color = if (showComponents) CyanNeon else TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = { showEnvelope = !showEnvelope },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (showEnvelope) EmeraldNeon else ScienceBorder
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (showEnvelope) "Env: ON" else "Env: OFF",
                            fontSize = 10.sp,
                            color = if (showEnvelope) EmeraldNeon else TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = { showPhasor = !showPhasor },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (showPhasor) PurpleNeon else ScienceBorder
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.0f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (showPhasor) "Phasor" else "Phasor",
                            fontSize = 10.sp,
                            color = if (showPhasor) PurpleNeon else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { resetSimulation() },
                        modifier = Modifier
                            .size(34.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(8.dp))
                    ) {
                        ResetIcon(tint = AmberVibrant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    )
}

// -------------------------------------------------------------
// Canvas Helper Functions
// -------------------------------------------------------------

private fun DrawScope.drawBeatsOscilloscopeGrid(
    w: Float,
    h: Float,
    cy: Float,
    leftX: Float,
    rightX: Float
) {
    val gridColor = ScienceBorder.copy(alpha = 0.22f)
    val axisColor = ScienceBorder.copy(alpha = 0.55f)

    // Horizontal grid lines (+2A, +A, 0, -A, -2A)
    val dy = 32.dp.toPx()
    for (step in -2..2) {
        val y = cy + step * dy
        val color = if (step == 0) axisColor else gridColor
        val strokeW = if (step == 0) 1.5f else 0.8f
        drawLine(
            color = color,
            start = Offset(leftX, y),
            end = Offset(rightX, y),
            strokeWidth = strokeW,
            pathEffect = if (step != 0) PathEffect.dashPathEffect(floatArrayOf(4f, 6f)) else null
        )
    }

    // Vertical time division tick marks
    val stepX = (rightX - leftX) / 8f
    for (i in 0..8) {
        val gx = leftX + i * stepX
        drawLine(
            color = gridColor,
            start = Offset(gx, cy - dy * 2.2f),
            end = Offset(gx, cy + dy * 2.2f),
            strokeWidth = 0.8f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))
        )
    }
}

private fun DrawScope.drawDualTuningForks(
    cy: Float,
    f1: Float,
    f2: Float,
    simTime: Float,
    isRunning: Boolean,
    isAudioOn: Boolean
) {
    val forkX = 28.dp.toPx()
    val tineLen = 22.dp.toPx()
    val handleLen = 16.dp.toPx()
    val gap = 12.dp.toPx()

    // Upper Fork (Tone 1, CyanNeon)
    val upperY = cy - 28.dp.toPx()
    val vib1 = if (isRunning) sin(2 * PI.toFloat() * (f1 - 435f) * simTime) * 3.5f else 0f
    drawTuningFork(forkX, upperY, tineLen, handleLen, gap, vib1, CyanNeon, isAudioOn)

    // Lower Fork (Tone 2, CoralNeon)
    val lowerY = cy + 28.dp.toPx()
    val vib2 = if (isRunning) sin(2 * PI.toFloat() * (f2 - 435f) * simTime) * 3.5f else 0f
    drawTuningFork(forkX, lowerY, tineLen, handleLen, gap, vib2, CoralNeon, isAudioOn)
}

private fun DrawScope.drawTuningFork(
    x: Float,
    y: Float,
    tineLen: Float,
    handleLen: Float,
    gap: Float,
    vibration: Float,
    accentColor: Color,
    isAudioOn: Boolean
) {
    val halfGap = gap * 0.5f

    // Handle (metallic stem)
    drawLine(
        color = ScienceBorder,
        start = Offset(x - handleLen, y),
        end = Offset(x, y),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )

    // U-base connecting tines
    val uPath = Path().apply {
        moveTo(x + tineLen, y - halfGap - vibration)
        lineTo(x, y - halfGap)
        quadraticTo(x - 5f, y, x, y + halfGap)
        lineTo(x + tineLen, y + halfGap + vibration)
    }
    drawPath(
        path = uPath,
        color = accentColor,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    // Acoustic emission arcs propagating rightward
    val arcCount = if (isAudioOn) 3 else 2
    for (arc in 1..arcCount) {
        val arcR = tineLen * (0.8f + arc * 0.55f)
        val arcAlpha = if (isAudioOn) (0.75f / arc) else (0.45f / arc)
        drawArc(
            color = accentColor.copy(alpha = arcAlpha),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(x + tineLen - arcR * 0.3f, y - arcR),
            size = Size(arcR * 2, arcR * 2),
            style = Stroke(width = (if (isAudioOn) 2.2.dp else 1.4.dp).toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawPhasorGauge(
    w: Float,
    cy: Float,
    f1: Float,
    f2: Float,
    amp1: Float,
    amp2: Float,
    simTime: Float
) {
    val center = Offset(w * 0.88f, cy)
    val gaugeRadius = 26.dp.toPx()

    // Circular background track
    drawCircle(
        color = ScienceDarkSurfaceVariant,
        radius = gaugeRadius,
        center = center
    )
    drawCircle(
        color = ScienceBorder.copy(alpha = 0.6f),
        radius = gaugeRadius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // Rotating Phasor Vectors
    val deltaOmega = 2 * PI.toFloat() * (f1 - f2)
    val angle1 = (simTime * deltaOmega * 0.5f)
    val angle2 = (-simTime * deltaOmega * 0.5f)

    val r1 = (gaugeRadius * 0.42f) * amp1
    val r2 = (gaugeRadius * 0.42f) * amp2

    val p1 = Offset(center.x + cos(angle1) * r1, center.y + sin(angle1) * r1)
    val pRes = Offset(p1.x + cos(angle2) * r2, p1.y + sin(angle2) * r2)

    // Phasor 1 (Cyan)
    drawLine(
        color = CyanNeon.copy(alpha = 0.8f),
        start = center,
        end = p1,
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Phasor 2 (Coral, tail connected to head of Phasor 1)
    drawLine(
        color = CoralNeon.copy(alpha = 0.8f),
        start = p1,
        end = pRes,
        strokeWidth = 1.8.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Resultant Phasor (Amber glowing vector from center to pRes)
    drawLine(
        color = AmberVibrant,
        start = center,
        end = pRes,
        strokeWidth = 2.4.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawCircle(
        color = AmberVibrant,
        radius = 2.5.dp.toPx(),
        center = pRes
    )
}

private fun DrawScope.drawAcousticPowerMeter(
    rightX: Float,
    cy: Float,
    height: Float,
    powerNorm: Float
) {
    val meterW = 5.dp.toPx()
    val halfH = height * 0.5f
    val meterX = rightX - meterW - 4.dp.toPx()

    // Background track
    drawRoundRect(
        color = ScienceDarkSurfaceVariant,
        topLeft = Offset(meterX, cy - halfH),
        size = Size(meterW, height),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // Active power level bar
    val activeH = height * powerNorm.coerceIn(0.04f, 1.0f)
    val barColor = when {
        powerNorm > 0.75f -> AmberVibrant
        powerNorm > 0.35f -> EmeraldNeon
        else -> CyanNeon.copy(alpha = 0.5f)
    }

    drawRoundRect(
        color = barColor,
        topLeft = Offset(meterX, cy + halfH - activeH),
        size = Size(meterW, activeH),
        cornerRadius = CornerRadius(3f, 3f)
    )
}
