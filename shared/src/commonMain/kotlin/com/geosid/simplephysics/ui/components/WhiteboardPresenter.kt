package com.geosid.simplephysics.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*

enum class PresenterMood {
    EXPLAINING,
    EXCITED,
    SHOCKED,
    THINKING,
    POINTING
}

@Composable
fun WhiteboardPresenter(
    mood: PresenterMood = PresenterMood.EXPLAINING,
    speechText: String = "",
    modifier: Modifier = Modifier
) {
    // Idle breathing & blinking animation
    val infiniteTransition = rememberInfiniteTransition()
    val breathOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val blinkPhase by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3200
                1f at 0
                1f at 3000
                0.05f at 3100
                1f at 3200
            }
        )
    )

    val handWave by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Bubble if text is present
        if (speechText.isNotEmpty()) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1E293B)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .widthIn(max = 240.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔬 DR. KELVIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = speechText,
                        color = Color(0xFF0F172A),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Animated Character Body (100% Compose Canvas!)
        Canvas(modifier = Modifier.size(160.dp, 200.dp)) {
            drawScientistCharacter(
                mood = mood,
                breathOffset = breathOffset,
                blinkFactor = blinkPhase,
                handWaveAngle = handWave
            )
        }
    }
}

private fun DrawScope.drawScientistCharacter(
    mood: PresenterMood,
    breathOffset: Float,
    blinkFactor: Float,
    handWaveAngle: Float
) {
    val cx = size.width * 0.5f
    val headY = size.height * 0.32f + breathOffset
    val headRadius = 32f

    // 1. Lab Coat Torso
    val coatTopY = headY + headRadius * 0.8f
    val coatBottomY = size.height * 0.94f
    val coatPath = Path().apply {
        moveTo(cx - 24f, coatTopY)
        lineTo(cx + 24f, coatTopY)
        lineTo(cx + 34f, coatBottomY)
        lineTo(cx - 34f, coatBottomY)
        close()
    }
    // White Lab Coat
    drawPath(coatPath, color = Color.White)
    drawPath(coatPath, color = Color(0xFF1E293B), style = Stroke(width = 3.5f))

    // Coat Collar & Buttons
    drawLine(Color(0xFF0288D1), Offset(cx, coatTopY), Offset(cx, coatBottomY), 2.5f)
    drawCircle(Color(0xFF0F172A), 3.5f, Offset(cx, coatTopY + 25f))
    drawCircle(Color(0xFF0F172A), 3.5f, Offset(cx, coatTopY + 45f))
    drawCircle(Color(0xFF0F172A), 3.5f, Offset(cx, coatTopY + 65f))

    // Pocket Protector with pens
    drawRect(Color(0xFFE2E8F0), Offset(cx + 10f, coatTopY + 22f), Size(16f, 18f))
    drawRect(Color(0xFF1E293B), Offset(cx + 10f, coatTopY + 22f), Size(16f, 18f), style = Stroke(width = 1.5f))
    drawLine(Color(0xFFFF1744), Offset(cx + 13f, coatTopY + 16f), Offset(cx + 13f, coatTopY + 24f), 2f)
    drawLine(Color(0xFF2979FF), Offset(cx + 18f, coatTopY + 14f), Offset(cx + 18f, coatTopY + 24f), 2f)

    // 2. Wild Scientist Hair (Back)
    val hairColor = Color(0xFFE0E0E0)
    for (i in -3..3) {
        val hairAngle = i * 22f
        rotate(hairAngle, pivot = Offset(cx, headY)) {
            drawCircle(hairColor, 16f, Offset(cx, headY - headRadius - 4f))
            drawCircle(Color(0xFF1E293B), 16f, Offset(cx, headY - headRadius - 4f), style = Stroke(width = 2.5f))
        }
    }

    // 3. Head (Skin tone)
    val skinColor = Color(0xFFFFCC80)
    drawCircle(skinColor, headRadius, Offset(cx, headY))
    drawCircle(Color(0xFF1E293B), headRadius, Offset(cx, headY), style = Stroke(width = 3.5f))

    // 4. Safety Goggles
    val goggleRadius = 14f
    val goggleLeft = Offset(cx - 14f, headY - 4f)
    val goggleRight = Offset(cx + 14f, headY - 4f)

    // Strap
    drawLine(Color(0xFF334155), Offset(cx - headRadius, headY - 4f), Offset(cx + headRadius, headY - 4f), 5f)

    // Goggle rims & glass
    listOf(goggleLeft, goggleRight).forEach { center ->
        drawCircle(Color(0xBB00E5FF), goggleRadius, center)
        drawCircle(Color(0xFF0F172A), goggleRadius, center, style = Stroke(width = 3.5f))
        // Glass specular shine
        drawLine(Color.White, Offset(center.x - 6f, center.y - 6f), Offset(center.x + 4f, center.y + 4f), 2.5f)
    }
    // Goggle bridge
    drawLine(Color(0xFF0F172A), Offset(cx - 5f, headY - 4f), Offset(cx + 5f, headY - 4f), 4f)

    // 5. Eyes inside goggles
    val eyeLeft = Offset(cx - 14f, headY - 4f)
    val eyeRight = Offset(cx + 14f, headY - 4f)

    if (mood == PresenterMood.SHOCKED) {
        // Wide open pupils
        drawCircle(Color(0xFF0F172A), 5f, eyeLeft)
        drawCircle(Color(0xFF0F172A), 5f, eyeRight)
    } else {
        // Regular eyes with blink
        val eyeHeight = 5f * blinkFactor
        drawOval(Color(0xFF0F172A), Offset(eyeLeft.x - 3.5f, eyeLeft.y - eyeHeight), Size(7f, eyeHeight * 2f))
        drawOval(Color(0xFF0F172A), Offset(eyeRight.x - 3.5f, eyeRight.y - eyeHeight), Size(7f, eyeHeight * 2f))
    }

    // 6. Mouth based on mood
    when (mood) {
        PresenterMood.SHOCKED -> {
            // Big open 'O' mouth
            drawCircle(Color(0xFFD32F2F), 7f, Offset(cx, headY + 16f))
            drawCircle(Color(0xFF1E293B), 7f, Offset(cx, headY + 16f), style = Stroke(width = 2.5f))
        }
        PresenterMood.EXCITED -> {
            // Big smile
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(cx - 12f, headY + 10f),
                size = Size(24f, 14f),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
        PresenterMood.THINKING -> {
            // Squiggly line / tilted mouth
            drawLine(Color(0xFF1E293B), Offset(cx - 8f, headY + 18f), Offset(cx + 8f, headY + 14f), 3f, StrokeCap.Round)
        }
        else -> {
            // Friendly speaking smile
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx - 10f, headY + 12f),
                size = Size(20f, 10f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }
    }

    // 7. Left Arm / Hand (Resting or Holding Clip)
    drawLine(Color.White, Offset(cx - 24f, coatTopY + 8f), Offset(cx - 42f, coatTopY + 45f), strokeWidth = 9f, cap = StrokeCap.Round)
    drawLine(Color(0xFF1E293B), Offset(cx - 24f, coatTopY + 8f), Offset(cx - 42f, coatTopY + 45f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawCircle(skinColor, 7f, Offset(cx - 42f, coatTopY + 45f))

    // 8. Right Arm / Hand (Holding Whiteboard Marker / Laser Pointer!)
    val shoulderR = Offset(cx + 24f, coatTopY + 8f)
    val armAngle = when (mood) {
        PresenterMood.POINTING -> -45f + handWaveAngle * 0.3f
        PresenterMood.EXCITED -> -65f + handWaveAngle * 0.5f
        PresenterMood.SHOCKED -> -20f
        else -> -35f + handWaveAngle * 0.2f
    }

    rotate(armAngle, pivot = shoulderR) {
        val elbow = Offset(shoulderR.x + 30f, shoulderR.y - 15f)
        val hand = Offset(elbow.x + 28f, elbow.y - 20f)

        // Upper arm & forearm
        drawLine(Color.White, shoulderR, elbow, strokeWidth = 10f, cap = StrokeCap.Round)
        drawLine(Color(0xFF1E293B), shoulderR, elbow, strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(Color.White, elbow, hand, strokeWidth = 9f, cap = StrokeCap.Round)
        drawLine(Color(0xFF1E293B), elbow, hand, strokeWidth = 2.5f, cap = StrokeCap.Round)

        // Hand
        drawCircle(skinColor, 7f, hand)

        // Whiteboard Marker (Red or Blue cap)
        drawLine(Color(0xFF263238), Offset(hand.x - 4f, hand.y + 4f), Offset(hand.x + 24f, hand.y - 18f), 6f, StrokeCap.Round)
        // Red Marker Tip
        drawCircle(Color(0xFFFF1744), 4f, Offset(hand.x + 24f, hand.y - 18f))

        // Laser pointer light beam
        if (mood == PresenterMood.POINTING || mood == PresenterMood.EXCITED) {
            drawLine(
                color = CoralNeon.copy(alpha = 0.7f),
                start = Offset(hand.x + 24f, hand.y - 18f),
                end = Offset(hand.x + 120f, hand.y - 45f),
                strokeWidth = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )
        }
    }
}
