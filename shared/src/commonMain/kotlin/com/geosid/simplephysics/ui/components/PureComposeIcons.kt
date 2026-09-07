package com.geosid.simplephysics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BackArrowIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.12f
        // Line
        drawLine(
            color = tint,
            start = Offset(w * 0.8f, h * 0.5f),
            end = Offset(w * 0.25f, h * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrow head
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.25f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.5f, h * 0.75f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ResetIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.11f
        val arcRadius = w * 0.35f
        val center = Offset(w * 0.5f, h * 0.52f)

        // Arc (about 270 degrees)
        drawArc(
            color = tint,
            startAngle = 40f,
            sweepAngle = 280f,
            useCenter = false,
            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
            size = Size(arcRadius * 2f, arcRadius * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // Arrow head at start
        val path = Path().apply {
            moveTo(w * 0.76f, h * 0.42f)
            lineTo(w * 0.88f, h * 0.65f)
            lineTo(w * 0.64f, h * 0.68f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ScienceFlaskIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.1f

        val path = Path().apply {
            // Lip
            moveTo(w * 0.38f, h * 0.18f)
            lineTo(w * 0.62f, h * 0.18f)
            // Neck
            moveTo(w * 0.42f, h * 0.18f)
            lineTo(w * 0.42f, h * 0.42f)
            // Body
            lineTo(w * 0.18f, h * 0.82f)
            // Bottom
            quadraticTo(w * 0.5f, h * 0.88f, w * 0.82f, h * 0.82f)
            // Body right
            lineTo(w * 0.58f, h * 0.42f)
            // Neck right
            lineTo(w * 0.58f, h * 0.18f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Fluid line inside
        drawLine(
            color = tint.copy(alpha = 0.6f),
            start = Offset(w * 0.28f, h * 0.68f),
            end = Offset(w * 0.72f, h * 0.68f),
            strokeWidth = stroke * 0.8f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun InfoLightbulbIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.1f

        // Bulb circle
        val bulbCenter = Offset(w * 0.5f, h * 0.42f)
        val radius = w * 0.26f
        drawCircle(
            color = tint,
            radius = radius,
            center = bulbCenter,
            style = Stroke(width = stroke)
        )

        // Base
        drawLine(
            color = tint,
            start = Offset(w * 0.4f, h * 0.75f),
            end = Offset(w * 0.6f, h * 0.75f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.45f, h * 0.85f),
            end = Offset(w * 0.55f, h * 0.85f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // Filament rays
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.08f),
            end = Offset(w * 0.5f, h * 0.14f),
            strokeWidth = stroke * 0.8f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun CloseIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.12f
        drawLine(
            color = tint,
            start = Offset(w * 0.25f, h * 0.25f),
            end = Offset(w * 0.75f, h * 0.75f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.75f, h * 0.25f),
            end = Offset(w * 0.25f, h * 0.75f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun MagicWandSparkleIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.1f

        // Wand stick
        drawLine(
            color = tint,
            start = Offset(w * 0.2f, h * 0.8f),
            end = Offset(w * 0.65f, h * 0.35f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // Sparkle star at top right
        val cx = w * 0.78f
        val cy = h * 0.22f
        val r = w * 0.16f
        drawLine(
            color = tint,
            start = Offset(cx - r, cy),
            end = Offset(cx + r, cy),
            strokeWidth = stroke * 0.8f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(cx, cy - r),
            end = Offset(cx, cy + r),
            strokeWidth = stroke * 0.8f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun CheckmarkIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.12f
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.74f)
            lineTo(w * 0.8f, h * 0.28f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun GlobeIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.1f
        val r = w * 0.4f
        val center = Offset(w * 0.5f, h * 0.5f)
        drawCircle(
            color = tint,
            radius = r,
            center = center,
            style = Stroke(width = stroke)
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.1f, h * 0.5f),
            end = Offset(w * 0.9f, h * 0.5f),
            strokeWidth = stroke * 0.8f,
            cap = StrokeCap.Round
        )
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.32f, h * 0.1f),
            size = Size(w * 0.36f, h * 0.8f),
            style = Stroke(width = stroke * 0.8f)
        )
    }
}
