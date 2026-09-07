package com.geosid.simplephysics.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pure-Compose splash screen — no images, no drawables.
 *
 * Flow:
 * 1. Logo + text fade/scale in over ~700 ms.
 * 2. Outer rings pulse infinitely via InfiniteTransition.
 * 3. After [SPLASH_DURATION_MS] ms the [onFinished] lambda fires,
 *    letting App.kt replace this screen with HomeScreen via AnimatedContent.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {

    // ── Entrance animation ──────────────────────────────────────────────────
    val enterAlpha = remember { Animatable(0f) }
    val enterScale = remember { Animatable(0.72f) }

    // ── Infinite pulsing rings ──────────────────────────────────────────────
    val pulse = rememberInfiniteTransition(label = "pulse")

    // Outer ring grows from 1x → 1.35x then reverses
    val ringScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    // Ring fades in sync with scale (bright when small, dim when large)
    val ringAlpha by pulse.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    // Slow orbit angle for the decorative amber particle
    val orbitAngle by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "orbit"
    )

    // ── Drive entrance + auto-navigate ─────────────────────────────────────
    LaunchedEffect(Unit) {
        // Fade + scale in simultaneously
        launch { enterAlpha.animateTo(1f, tween(700, easing = EaseOut)) }
        launch { enterScale.animateTo(1f, tween(700, easing = EaseOutBack)) }
        // Wait total splash duration then hand off to App navigation
        delay(SPLASH_DURATION_MS)
        onFinished()
    }

    // ── Layout ──────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScienceDarkBg),
        contentAlignment = Alignment.Center
    ) {
        // Subtle full-size radial glow behind the logo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyanNeon.copy(alpha = 0.07f), Color.Transparent),
                    center = center,
                    radius = size.minDimension * 0.55f
                ),
                radius = size.minDimension * 0.55f
            )
        }

        // Animated column (entrance: fade + scale in)
        Column(
            modifier = Modifier
                .graphicsLayer {
                    alpha = enterAlpha.value
                    scaleX = enterScale.value
                    scaleY = enterScale.value
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Logo canvas ─────────────────────────────────────────────────
            Canvas(modifier = Modifier.size(130.dp)) {
                val cx = size.width / 2
                val cy = size.height / 2
                val logoRadius = size.minDimension * 0.38f

                // Pulsing outer ring
                drawCircle(
                    color = CyanNeon.copy(alpha = ringAlpha),
                    radius = logoRadius * ringScale,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Static secondary ring
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.18f),
                    radius = logoRadius * 1.18f,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Glowing badge background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow, Color.Transparent),
                        center = Offset(cx, cy),
                        radius = logoRadius * 1.1f
                    ),
                    radius = logoRadius * 1.1f
                )

                // Solid inner badge circle
                drawCircle(color = ScienceDarkSurfaceVariant, radius = logoRadius)
                drawCircle(
                    color = ScienceBorder,
                    radius = logoRadius,
                    style = Stroke(width = 1.5f.dp.toPx())
                )

                // Flask icon drawn with basic primitives
                drawFlaskIcon(cx = cx, cy = cy, radius = logoRadius)

                // Orbiting amber dot
                rotate(degrees = orbitAngle, pivot = Offset(cx, cy)) {
                    drawCircle(
                        color = AmberVibrant,
                        radius = 4.dp.toPx(),
                        center = Offset(cx + logoRadius * 1.18f, cy)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // App title
            Text(
                text = "SIMPLE PHYSICS",
                color = CyanNeon,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Interactive Physics Laboratory",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // Blinking dots loading indicator
            ThreeDotsLoader()
        }
    }
}

// ── Components ──────────────────────────────────────────────────────────────

/**
 * Three sequentially blinking dots — a minimalist loading indicator.
 */
@Composable
private fun ThreeDotsLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    // Returns an alpha [0,1] that peaks at 300 ms, returns to 0 at 600 ms,
    // then stays dark until the 1200 ms cycle repeats.
    @Composable
    fun dotAlpha(delayMs: Int): Float = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at 0
                1f at 300
                0f at 600
                0f at 1200
            },
            initialStartOffset = StartOffset(delayMs)
        ),
        label = "dot$delayMs"
    ).value

    val a1 = dotAlpha(0)
    val a2 = dotAlpha(200)
    val a3 = dotAlpha(400)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(a1, a2, a3).forEach { alpha ->
            Canvas(modifier = Modifier.size(7.dp)) {
                // Dim base + bright flash when active
                drawCircle(color = CyanNeon.copy(alpha = 0.25f + alpha * 0.75f))
            }
        }
    }
}

// ── Canvas drawing helpers ──────────────────────────────────────────────────

/**
 * Draws a simple flask icon using only Canvas primitives.
 * All sizes are proportional to [radius] so the icon scales with the badge.
 */
private fun DrawScope.drawFlaskIcon(cx: Float, cy: Float, radius: Float) {
    val u = radius * 0.045f          // 1 unit = 4.5 % of badge radius

    // Flask neck (thin rectangle)
    val neckW = u * 4.5f
    val neckH = u * 5f
    val neckLeft = cx - neckW / 2
    val neckTop  = cy - u * 8f

    drawIntoCanvas { canvas ->
        canvas.drawRect(
            left   = neckLeft,
            top    = neckTop,
            right  = neckLeft + neckW,
            bottom = neckTop + neckH,
            paint  = Paint().apply { color = CyanNeon.copy(alpha = 0.9f) }
        )
    }

    // Flask body (oval)
    val bodyW = u * 11f
    val bodyH = u * 9f
    val bodyTop = cy - u * 3f
    drawOval(
        color = CyanNeon.copy(alpha = 0.85f),
        topLeft = Offset(cx - bodyW / 2, bodyTop),
        size = Size(bodyW, bodyH)
    )

    // Liquid inside body (lighter, smaller oval)
    drawOval(
        color = CyanNeon.copy(alpha = 0.25f),
        topLeft = Offset(cx - bodyW / 2 + u, bodyTop + u * 2.5f),
        size = Size(bodyW - u * 2, bodyH - u * 3f)
    )

    // Highlight line on neck
    drawLine(
        color = Color.White.copy(alpha = 0.4f),
        start = Offset(neckLeft + u, neckTop + u),
        end = Offset(neckLeft + u, neckTop + neckH - u),
        strokeWidth = u * 0.9f,
        cap = StrokeCap.Round
    )

    // Small bubbles in the liquid
    listOf(
        Offset(cx - u * 2f,   cy + u * 2.5f),
        Offset(cx + u * 1.5f, cy + u * 3.8f),
        Offset(cx,             cy + u * 1f)
    ).forEach { pos ->
        drawCircle(color = Color.White.copy(alpha = 0.35f), radius = u * 1.1f, center = pos)
    }
}

private const val SPLASH_DURATION_MS = 2500L
