package com.geosid.simplephysics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.model.PhysicsExperiment
import com.geosid.simplephysics.ui.theme.*
import kotlin.math.*

@Composable
fun ExperimentCard(
    experiment: PhysicsExperiment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, ScienceBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = ScienceDarkSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 100% Pure Compose Canvas Illustration Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(ScienceDarkSurfaceVariant)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawExperimentIllustration(experiment.id)
                }

                // Badge top-right
                val isUnlocked = experiment.isReleased
                val badgeColor = if (isUnlocked) CyanNeon else AmberVibrant
                Surface(
                    color = ScienceDarkBg.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isUnlocked) {
                            experiment.badge
                                .replace("• Locked", "• Unlocked")
                                .replace("• Gesperrt", "• Freigeschaltet")
                                .replace("• Κλειδωμένη", "• Ξεκλείδωτη")
                                .replace("• Bloqueado", "• Desbloqueado")
                                .replace("• Verrouillé", "• Débloqué")
                                .replace("• Bloccato", "• Sbloccato")
                        } else {
                            val lockedBadge = experiment.badge
                                .replace("• Unlocked", "• Locked")
                                .replace("• Freigeschaltet", "• Gesperrt")
                                .replace("• Ξεκλείδωτη", "• Κλειδωμένη")
                                .replace("• Desbloqueado", "• Bloqueado")
                                .replace("• Débloqué", "• Verrouillé")
                                .replace("• Sbloccato", "• Bloccato")
                            "🔒 $lockedBadge"
                        },
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Text Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = experiment.category.title.uppercase(),
                    color = PurpleNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = experiment.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = experiment.subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AmberVibrant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = experiment.teaser,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (experiment.isReleased) {
                        Text(
                            text = "EXPLORE SIMULATION →",
                            color = CyanNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Text(
                            text = "🔒 COMING SOON",
                            color = AmberVibrant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// Pure Compose Illustration Drawing
internal fun DrawScope.drawExperimentIllustration(id: String) {
    val w = size.width
    val h = size.height

    when (id) {
        "projectile_drag" -> {
            val groundY = h * 0.82f
            drawLine(EmeraldNeon, Offset(0f, groundY), Offset(w, groundY), 3f)

            // Cannon
            val cx = w * 0.18f
            drawCircle(Color(0xFF5D4037), 12f, Offset(cx, groundY - 12f))
            drawLine(Color(0xFF455A64), Offset(cx, groundY - 12f), Offset(cx + 26f, groundY - 32f), 8f, StrokeCap.Round)

            // Ideal vacuum path (dashed yellow)
            val vacPath = Path().apply {
                moveTo(cx + 20f, groundY - 26f)
                quadraticTo(w * 0.55f, groundY - 120f, w * 0.92f, groundY)
            }
            drawPath(vacPath, AmberVibrant.copy(alpha = 0.5f), style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))

            // Real Drag Path (cyan with steeper drop)
            val dragPath = Path().apply {
                moveTo(cx + 20f, groundY - 26f)
                cubicTo(w * 0.45f, groundY - 100f, w * 0.65f, groundY - 70f, w * 0.72f, groundY)
            }
            drawPath(dragPath, CyanNeon, style = Stroke(width = 3f, cap = StrokeCap.Round))

            // Projectile ball
            drawCircle(CoralNeon, 6f, Offset(w * 0.5f, groundY - 82f))

            // Target flag
            drawLine(Color.White, Offset(w * 0.72f, groundY), Offset(w * 0.72f, groundY - 24f), 2.5f)
            val flagPath = Path().apply {
                moveTo(w * 0.72f, groundY - 24f)
                lineTo(w * 0.72f + 14f, groundY - 18f)
                lineTo(w * 0.72f, groundY - 12f)
                close()
            }
            drawPath(flagPath, EmeraldNeon)
        }
        "bouncing_ball" -> {
            val groundY = h * 0.82f
            drawLine(ScienceBorder, Offset(0f, groundY), Offset(w, groundY), 3f)

            // Bouncing height decay curves (e=0.8)
            val bouncePath = Path().apply {
                moveTo(w * 0.15f, groundY)
                // Bounce 1
                cubicTo(w * 0.20f, groundY - 95f, w * 0.32f, groundY - 95f, w * 0.38f, groundY)
                // Bounce 2
                cubicTo(w * 0.43f, groundY - 60f, w * 0.53f, groundY - 60f, w * 0.58f, groundY)
                // Bounce 3
                cubicTo(w * 0.62f, groundY - 35f, w * 0.70f, groundY - 35f, w * 0.74f, groundY)
                // Bounce 4
                cubicTo(w * 0.77f, groundY - 18f, w * 0.83f, groundY - 18f, w * 0.86f, groundY)
            }
            drawPath(bouncePath, AmberVibrant, style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))

            // Ball at apex of bounce 1
            drawCircle(
                brush = Brush.radialGradient(listOf(Color.White, AmberVibrant, Color(0xFFE65100))),
                radius = 12f,
                center = Offset(w * 0.26f, groundY - 95f)
            )

            // Floor shadow
            drawOval(Color.Black.copy(alpha = 0.4f), Offset(w * 0.26f - 16f, groundY - 4f), Size(32f, 8f))
        }
        "newtons_cradle" -> {
            val cx = w * 0.5f
            val topY = h * 0.22f
            val stringL = h * 0.45f
            val ballR = 9f
            val groundY = topY + stringL + ballR * 2.2f

            // 1. Frame Base Shadow & Ground line
            drawOval(
                color = Color.Black.copy(alpha = 0.35f),
                topLeft = Offset(cx - 52f, groundY - 3f),
                size = Size(104f, 8f)
            )

            // 2. Top Chrome Frame Crossbar & Pillars
            val frameHalfW = 46f
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFB0BEC5), Color(0xFF37474F))),
                topLeft = Offset(cx - frameHalfW, topY - 3f),
                size = Size(frameHalfW * 2f, 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            // Left & Right Support Pillars
            drawLine(Color(0xFF78909C), Offset(cx - frameHalfW + 4f, topY), Offset(cx - frameHalfW + 4f, groundY), 3.5f, StrokeCap.Round)
            drawLine(Color(0xFF78909C), Offset(cx + frameHalfW - 4f, topY), Offset(cx + frameHalfW - 4f, groundY), 3.5f, StrokeCap.Round)

            // 3. Angles for 5 Balls (Left ball swung back, middle 3 vertical, right ball reacting)
            val angles = floatArrayOf(-0.65f, 0f, 0f, 0f, 0.22f)

            // 4. Motion Blur Trail for swung Left Ball
            val leftPivotX = cx - 2 * (ballR * 2f)
            for (trail in 1..3) {
                val tAngle = angles[0] + 0.08f * trail
                val tx = leftPivotX + stringL * sin(tAngle)
                val ty = topY + stringL * cos(tAngle)
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.15f / trail),
                    radius = ballR * 0.85f,
                    center = Offset(tx, ty)
                )
            }

            // 5. Impact Spark on Right side (Energy transfer wave)
            val contactX = cx + (ballR * 2f)
            drawCircle(
                color = CyanNeon.copy(alpha = 0.6f),
                radius = 6f,
                center = Offset(contactX, topY + stringL),
                style = Stroke(width = 1.5f)
            )

            // 6. Draw 5 Balls with Dual V-Strings and Metallic Shading
            for (i in 0 until 5) {
                val pivotX = cx + (i - 2) * (ballR * 2f)
                val angle = angles[i]
                val bx = pivotX + stringL * sin(angle)
                val by = topY + stringL * cos(angle)

                // Dual V-suspension strings
                val vSpread = ballR * 0.5f
                drawLine(Color.White.copy(alpha = 0.65f), Offset(pivotX - vSpread, topY), Offset(bx, by - ballR), 1.2f)
                drawLine(Color.White.copy(alpha = 0.65f), Offset(pivotX + vSpread, topY), Offset(bx, by - ballR), 1.2f)

                // Ball drop shadow
                drawOval(
                    color = Color.Black.copy(alpha = 0.3f),
                    topLeft = Offset(bx - ballR * 0.7f, groundY - 2f),
                    size = Size(ballR * 1.4f, 5f)
                )

                // Metallic Chrome Sphere Shader
                val hlOffset = Offset(bx - ballR * 0.32f, by - ballR * 0.32f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFE0E0E0), Color(0xFF78909C), Color(0xFF263238)),
                        center = hlOffset,
                        radius = ballR * 1.3f
                    ),
                    radius = ballR,
                    center = Offset(bx, by)
                )
                // Specular highlight spot
                drawCircle(Color.White.copy(alpha = 0.95f), ballR * 0.25f, hlOffset)
                // Chrome rim
                drawCircle(Color(0xFFCFD8DC), ballR, Offset(bx, by), style = Stroke(width = 1f))
            }
        }
        "brachistochrone" -> {
            val startX = w * 0.14f
            val startY = h * 0.22f
            val endX = w * 0.86f
            val endY = h * 0.78f

            // 1. Release Platform at Top-Left
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(startX - 12f, startY),
                end = Offset(startX + 4f, startY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // 2. Straight Incline (Slower comparison path - dashed Amber)
            val straightPath = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            drawPath(
                path = straightPath,
                color = AmberVibrant.copy(alpha = 0.45f),
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            )

            // 3. Lagging Ball on Straight Path (showing it loses the race)
            val straightT = 0.38f
            val sBallX = startX + (endX - startX) * straightT
            val sBallY = startY + (endY - startY) * straightT
            drawCircle(
                color = AmberVibrant.copy(alpha = 0.7f),
                radius = 4.5f,
                center = Offset(sBallX, sBallY - 4.5f)
            )

            // 4. Brachistochrone Cycloid Curve (The Quickest Descent - Glowing Cyan)
            val cycloidPath = Path().apply {
                moveTo(startX, startY)
                cubicTo(
                    startX + (endX - startX) * 0.18f, startY + (endY - startY) * 0.82f,
                    startX + (endX - startX) * 0.58f, endY + 6f,
                    endX, endY
                )
            }
            // Cyan outer glow
            drawPath(
                path = cycloidPath,
                color = CyanNeon.copy(alpha = 0.35f),
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )
            // Crisp foreground curve
            drawPath(
                path = cycloidPath,
                color = CyanNeon,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )

            // 5. Winning Marble on Cycloid (Racing ahead near finish line)
            val winX = startX + (endX - startX) * 0.72f
            val winY = endY - 6f
            // Motion blur / speed trail behind winning marble
            drawLine(
                color = CyanNeon.copy(alpha = 0.5f),
                start = Offset(winX - 16f, winY + 2f),
                end = Offset(winX - 2f, winY),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            // Winning marble glow & sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, CyanNeon, Color(0xFF007A99)),
                    center = Offset(winX - 1.5f, winY - 1.5f),
                    radius = 7.5f
                ),
                radius = 6.5f,
                center = Offset(winX, winY)
            )

            // 6. Finish Flag at Bottom-Right
            val poleHeight = 28f
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(endX, endY + 4f),
                end = Offset(endX, endY - poleHeight),
                strokeWidth = 2f
            )
            val flagPath = Path().apply {
                moveTo(endX, endY - poleHeight)
                lineTo(endX + 14f, endY - poleHeight + 7f)
                lineTo(endX, endY - poleHeight + 14f)
                close()
            }
            drawPath(flagPath, EmeraldNeon)
        }
        "terminal_velocity" -> {
            val cx = w * 0.5f
            val cy = h * 0.50f

            // 1. Upward Air Streamlines (Aerodynamic flow field)
            val streamCount = 5
            for (i in 0 until streamCount) {
                val sx = cx + (i - 2) * (w * 0.16f)
                val sy = cy + (if (i % 2 == 0) 18f else -12f)
                drawLine(
                    color = CyanNeon.copy(alpha = 0.35f),
                    start = Offset(sx, sy + 28f),
                    end = Offset(sx, sy - 28f),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }

            // 2. Parachute Canopy (Billowing Aerodynamic Arc)
            val canopyW = min(w * 0.44f, 105f)
            val canopyH = canopyW * 0.42f
            val canopyY = cy - 24f

            val canopyPath = Path().apply {
                moveTo(cx - canopyW * 0.5f, canopyY)
                cubicTo(
                    cx - canopyW * 0.4f, canopyY - canopyH,
                    cx + canopyW * 0.4f, canopyY - canopyH,
                    cx + canopyW * 0.5f, canopyY
                )
                close()
            }
            // Multi-color neon parachute canopy
            drawPath(
                path = canopyPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(CyanNeon, EmeraldNeon, AmberVibrant, CyanNeon),
                    startX = cx - canopyW * 0.5f,
                    endX = cx + canopyW * 0.5f
                )
            )
            drawPath(
                path = canopyPath,
                color = Color.White,
                style = Stroke(width = 2f)
            )

            // 3. Parachute Suspension Lines
            val harnessY = cy + 18f
            val lineCount = 4
            for (k in 0 until lineCount) {
                val t = k / (lineCount - 1).toFloat()
                val lineX = cx - canopyW * 0.42f + t * canopyW * 0.84f
                drawLine(
                    color = Color.White.copy(alpha = 0.65f),
                    start = Offset(cx, harnessY - 6f),
                    end = Offset(lineX, canopyY - 2f),
                    strokeWidth = 1.2f
                )
            }

            // 4. Skydiver Figure
            drawCircle(AmberVibrant, 4.5f, Offset(cx, harnessY - 8f)) // Helmet
            drawLine(Color.White, Offset(cx, harnessY - 4f), Offset(cx, harnessY + 8f), 3f, StrokeCap.Round) // Body
            drawLine(CyanNeon, Offset(cx, harnessY + 8f), Offset(cx - 4f, harnessY + 16f), 2.5f, StrokeCap.Round) // Left Leg
            drawLine(CyanNeon, Offset(cx, harnessY + 8f), Offset(cx + 4f, harnessY + 16f), 2.5f, StrokeCap.Round) // Right Leg

            // 5. Force Vectors: Upward Drag Fd (Cyan) & Downward Gravity Fg (Amber)
            val arrowOffset = canopyW * 0.60f
            val vectorX = cx + arrowOffset
            if (vectorX + 8f < w) {
                val vLen = 20f
                // Upward Drag Vector
                drawLine(CyanNeon, Offset(vectorX, cy + 2f), Offset(vectorX, cy - vLen), 2.5f, StrokeCap.Round)
                val upHead = Path().apply {
                    moveTo(vectorX, cy - vLen - 4f)
                    lineTo(vectorX - 3.5f, cy - vLen)
                    lineTo(vectorX + 3.5f, cy - vLen)
                    close()
                }
                drawPath(upHead, CyanNeon)

                // Downward Gravity Vector
                drawLine(AmberVibrant, Offset(vectorX, cy + 2f), Offset(vectorX, cy + 2f + vLen), 2.5f, StrokeCap.Round)
                val downHead = Path().apply {
                    moveTo(vectorX, cy + 2f + vLen + 4f)
                    lineTo(vectorX - 3.5f, cy + 2f + vLen)
                    lineTo(vectorX + 3.5f, cy + 2f + vLen)
                    close()
                }
                drawPath(downHead, AmberVibrant)
            }
        }
        "stick_slip_friction" -> {
            val floorY = h * 0.65f
            val floorHeight = 16f

            // 1. Base rail with hatch lines
            drawRect(
                color = ScienceDarkSurfaceVariant,
                topLeft = Offset(0f, floorY),
                size = Size(w, floorHeight)
            )
            drawLine(
                color = AmberVibrant.copy(alpha = 0.8f),
                start = Offset(0f, floorY),
                end = Offset(w, floorY),
                strokeWidth = 2f
            )
            val spacing = 16f
            var hx = 0f
            while (hx < w) {
                drawLine(
                    color = ScienceBorder.copy(alpha = 0.5f),
                    start = Offset(hx, floorY),
                    end = Offset(hx - 8f, floorY + floorHeight),
                    strokeWidth = 1f
                )
                hx += spacing
            }

            // 2. Sled Block (Mass m)
            val blockW = min(w * 0.28f, 72f)
            val blockH = 34f
            val blockX = w * 0.30f
            val blockTop = floorY - blockH

            // Block Body
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(ScienceDarkSurfaceVariant, ScienceDarkSurface),
                    startY = blockTop,
                    endY = floorY
                ),
                topLeft = Offset(blockX - blockW * 0.5f, blockTop),
                size = Size(blockW, blockH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = AmberVibrant,
                topLeft = Offset(blockX - blockW * 0.5f, blockTop),
                size = Size(blockW, blockH),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.8f)
            )

            // Block Grip/Detail lines
            drawLine(
                Color.White.copy(alpha = 0.25f),
                Offset(blockX - blockW * 0.35f, blockTop + blockH * 0.38f),
                Offset(blockX + blockW * 0.35f, blockTop + blockH * 0.38f),
                1.5f
            )
            drawLine(
                Color.White.copy(alpha = 0.25f),
                Offset(blockX - blockW * 0.35f, blockTop + blockH * 0.58f),
                Offset(blockX + blockW * 0.35f, blockTop + blockH * 0.58f),
                1.5f
            )

            // 3. Motor Puller Bracket
            val pullerX = w * 0.82f
            val pullerH = 38f
            val pullerW = 18f
            drawRoundRect(
                color = EmeraldNeon,
                topLeft = Offset(pullerX, floorY - pullerH),
                size = Size(pullerW, pullerH),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // 4. Helical Spring
            val springStartY = blockTop + blockH * 0.5f
            val springStartX = blockX + blockW * 0.5f
            val springEndX = pullerX
            val coils = 7
            val amp = 9f
            val path = Path()
            path.moveTo(springStartX, springStartY)
            val span = springEndX - springStartX
            val step = span / (coils * 2)
            for (i in 0 until coils * 2) {
                val cx = springStartX + (i + 0.5f) * step
                val cy = springStartY + (if (i % 2 == 0) -amp else amp)
                val ex = springStartX + (i + 1f) * step
                path.quadraticTo(cx, cy, ex, springStartY)
            }
            drawPath(path, CyanNeon, style = Stroke(width = 2.2f, cap = StrokeCap.Round))

            // 5. Force Arrows
            // Spring Tension Fs (Cyan, pulling right)
            val fsArrowStart = Offset(blockX + blockW * 0.2f, blockTop - 10f)
            val fsArrowEnd = Offset(fsArrowStart.x + 28f, fsArrowStart.y)
            drawLine(CyanNeon, fsArrowStart, fsArrowEnd, 2f, StrokeCap.Round)
            val fsHead = Path().apply {
                moveTo(fsArrowEnd.x + 4f, fsArrowEnd.y)
                lineTo(fsArrowEnd.x - 3f, fsArrowEnd.y - 3f)
                lineTo(fsArrowEnd.x - 3f, fsArrowEnd.y + 3f)
                close()
            }
            drawPath(fsHead, CyanNeon)

            // Friction Resistance Ff (Amber, opposing left)
            val ffArrowStart = Offset(blockX - blockW * 0.1f, floorY - 2f)
            val ffArrowEnd = Offset(ffArrowStart.x - 26f, ffArrowStart.y)
            drawLine(AmberVibrant, ffArrowStart, ffArrowEnd, 2f, StrokeCap.Round)
            val ffHead = Path().apply {
                moveTo(ffArrowEnd.x - 4f, ffArrowEnd.y)
                lineTo(ffArrowEnd.x + 3f, ffArrowEnd.y - 3f)
                lineTo(ffArrowEnd.x + 3f, ffArrowEnd.y + 3f)
                close()
            }
            drawPath(ffHead, AmberVibrant)

            // 6. Micro-Asperity Contact Sparks
            drawCircle(CoralNeon, 2.5f, Offset(blockX - blockW * 0.2f, floorY - 1f))
            drawCircle(AmberVibrant, 2f, Offset(blockX + blockW * 0.1f, floorY - 2f))
        }
        "compound_pulley" -> {
            val girderY = h * 0.12f
            val girderH = 10f

            // 1. Ceiling mount / Support girder
            drawRect(
                color = ScienceDarkSurfaceVariant,
                topLeft = Offset(w * 0.15f, girderY - girderH),
                size = Size(w * 0.70f, girderH)
            )
            drawLine(
                color = ScienceBorder,
                start = Offset(w * 0.15f, girderY),
                end = Offset(w * 0.85f, girderY),
                strokeWidth = 2f
            )

            // Girder rivets
            drawCircle(Color.White.copy(alpha = 0.4f), 2f, Offset(w * 0.22f, girderY - girderH * 0.5f))
            drawCircle(Color.White.copy(alpha = 0.4f), 2f, Offset(w * 0.78f, girderY - girderH * 0.5f))

            // 2. Pulley Sheaves Layout
            // Top Fixed Sheave (Gun Tackle setup: 2 strands supporting load)
            val topSheaveCenter = Offset(w * 0.42f, h * 0.28f)
            val sheaveRadius = 14f

            // Bottom Movable Sheave
            val bottomSheaveCenter = Offset(w * 0.42f, h * 0.56f)

            // Top bracket from girder to top axle
            drawLine(
                color = ScienceBorder.copy(alpha = 0.8f),
                start = Offset(topSheaveCenter.x, girderY),
                end = topSheaveCenter,
                strokeWidth = 3f
            )

            // Anchor point for dead-end rope on ceiling girder
            val ropeAnchor = Offset(w * 0.30f, girderY)
            drawCircle(AmberVibrant, 3f, ropeAnchor)

            // 3. Threaded Cable
            val ropePath = Path().apply {
                // Strand 1: from anchor down to movable sheave left
                moveTo(ropeAnchor.x, ropeAnchor.y)
                lineTo(bottomSheaveCenter.x - sheaveRadius, bottomSheaveCenter.y)
                // Wrap around bottom sheave (underneath)
                arcTo(
                    rect = Rect(
                        bottomSheaveCenter.x - sheaveRadius,
                        bottomSheaveCenter.y - sheaveRadius,
                        bottomSheaveCenter.x + sheaveRadius,
                        bottomSheaveCenter.y + sheaveRadius
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false
                )
                // Strand 2: from movable sheave right up to top sheave left
                lineTo(topSheaveCenter.x - sheaveRadius * 0.5f, topSheaveCenter.y)
                // Wrap over top sheave
                arcTo(
                    rect = Rect(
                        topSheaveCenter.x - sheaveRadius,
                        topSheaveCenter.y - sheaveRadius,
                        topSheaveCenter.x + sheaveRadius,
                        topSheaveCenter.y + sheaveRadius
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                // Pull rope dropping down on right
                lineTo(topSheaveCenter.x + sheaveRadius, h * 0.72f)
            }
            // Draw rope glow & line
            drawPath(ropePath, CyanNeon.copy(alpha = 0.35f), style = Stroke(width = 5f, cap = StrokeCap.Round))
            drawPath(ropePath, CyanNeon, style = Stroke(width = 2.2f, cap = StrokeCap.Round))

            // 4. Draw Top Sheave (Fixed)
            drawCircle(ScienceDarkSurfaceVariant, sheaveRadius, topSheaveCenter)
            drawCircle(CyanNeon, sheaveRadius, topSheaveCenter, style = Stroke(2f))
            drawCircle(Color.White.copy(alpha = 0.7f), 3f, topSheaveCenter)

            // 5. Draw Bottom Sheave (Movable)
            drawCircle(ScienceDarkSurfaceVariant, sheaveRadius, bottomSheaveCenter)
            drawCircle(AmberVibrant, sheaveRadius, bottomSheaveCenter, style = Stroke(2f))
            drawCircle(Color.White.copy(alpha = 0.7f), 3f, bottomSheaveCenter)

            // Movable Hook & Bracket
            val hookTop = bottomSheaveCenter.y + sheaveRadius
            val hookBottom = hookTop + 10f
            drawLine(AmberVibrant, Offset(bottomSheaveCenter.x, hookTop), Offset(bottomSheaveCenter.x, hookBottom), 2.5f)

            // 6. Suspended Cargo Box (1000 kg)
            val cargoW = 54f
            val cargoH = 26f
            val cargoTop = hookBottom + 2f
            val cargoLeft = bottomSheaveCenter.x - cargoW * 0.5f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(ScienceDarkSurfaceVariant, ScienceDarkSurface),
                    startY = cargoTop,
                    endY = cargoTop + cargoH
                ),
                topLeft = Offset(cargoLeft, cargoTop),
                size = Size(cargoW, cargoH),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = AmberVibrant,
                topLeft = Offset(cargoLeft, cargoTop),
                size = Size(cargoW, cargoH),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(1.8f)
            )

            // Cargo Weight stripe / markings
            drawLine(
                Color.White.copy(alpha = 0.25f),
                Offset(cargoLeft + 8f, cargoTop + cargoH * 0.5f),
                Offset(cargoLeft + cargoW - 8f, cargoTop + cargoH * 0.5f),
                1.5f
            )

            // 7. Force Vectors (Tension T & Pull Force F_in)
            val t1X = bottomSheaveCenter.x - sheaveRadius
            val t2X = bottomSheaveCenter.x + sheaveRadius * 0.5f
            val tY = bottomSheaveCenter.y - 14f

            listOf(t1X, t2X).forEach { tx ->
                drawLine(CyanNeon, Offset(tx, tY + 12f), Offset(tx, tY), 1.8f, StrokeCap.Round)
                val tHead = Path().apply {
                    moveTo(tx, tY - 3f)
                    lineTo(tx - 3f, tY + 3f)
                    lineTo(tx + 3f, tY + 3f)
                    close()
                }
                drawPath(tHead, CyanNeon)
            }

            // Gravity Arrow W = mg (pointing DOWN from cargo)
            val wStartY = cargoTop + cargoH + 2f
            val wEndY = min(wStartY + 14f, h - 4f)
            drawLine(CoralNeon, Offset(bottomSheaveCenter.x, wStartY), Offset(bottomSheaveCenter.x, wEndY), 2f, StrokeCap.Round)
            val wHead = Path().apply {
                moveTo(bottomSheaveCenter.x, wEndY + 3f)
                lineTo(bottomSheaveCenter.x - 3f, wEndY - 3f)
                lineTo(bottomSheaveCenter.x + 3f, wEndY - 3f)
                close()
            }
            drawPath(wHead, CoralNeon)

            // Effort Pull Arrow (Emerald Neon, pulling down on rope end)
            val pullX = topSheaveCenter.x + sheaveRadius
            val pullStartY = h * 0.72f
            val pullEndY = min(pullStartY + 16f, h - 4f)
            drawLine(EmeraldNeon, Offset(pullX, pullStartY), Offset(pullX, pullEndY), 2.2f, StrokeCap.Round)
            val pullHead = Path().apply {
                moveTo(pullX, pullEndY + 4f)
                lineTo(pullX - 3.5f, pullEndY - 3f)
                lineTo(pullX + 3.5f, pullEndY - 3f)
                close()
            }
            drawPath(pullHead, EmeraldNeon)
        }
        "plucked_string" -> {
            val baseY = h * 0.52f
            // Pegs
            drawRect(Color(0xFFB0BEC5), Offset(w * 0.12f, baseY - 25f), Size(8f, 50f))
            drawRect(Color(0xFFB0BEC5), Offset(w * 0.88f - 8f, baseY - 25f), Size(8f, 50f))

            // Standing harmonic wave (n=2 node in middle)
            val wavePath = Path().apply {
                moveTo(w * 0.12f + 8f, baseY)
                cubicTo(w * 0.25f, baseY - 45f, w * 0.38f, baseY - 45f, w * 0.50f, baseY)
                cubicTo(w * 0.62f, baseY + 45f, w * 0.75f, baseY + 45f, w * 0.88f - 8f, baseY)
            }
            drawPath(wavePath, CyanNeon.copy(alpha = 0.35f), style = Stroke(width = 8f))
            drawPath(wavePath, Color.White, style = Stroke(width = 3.5f))

            // Acoustic sound wave arc
            drawCircle(CyanNeon.copy(alpha = 0.25f), 35f, Offset(w * 0.5f, baseY), style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))
        }
        "fourier_series" -> {
            val cy = h * 0.52f
            val cx = w * 0.25f

            // Circle 1
            drawCircle(CyanNeon.copy(alpha = 0.4f), 32f, Offset(cx, cy), style = Stroke(width = 2f))
            // Vector 1 (at ~45 deg)
            val v1End = Offset(cx + 22f, cy - 22f)
            drawLine(CyanNeon, Offset(cx, cy), v1End, 2.5f, StrokeCap.Round)

            // Circle 2 (attached at tip of 1)
            drawCircle(AmberVibrant.copy(alpha = 0.5f), 12f, v1End, style = Stroke(width = 1.5f))
            val v2End = Offset(v1End.x + 9f, v1End.y + 8f)
            drawLine(AmberVibrant, v1End, v2End, 2f, StrokeCap.Round)

            // Laser projection line to wave
            val waveStartX = w * 0.48f
            drawLine(CoralNeon.copy(alpha = 0.7f), v2End, Offset(waveStartX, v2End.y), 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))

            // Synthesized Square/Stepped Wave on the right
            val wavePath = Path().apply {
                moveTo(waveStartX, v2End.y)
                val sqTop = cy - 26f
                val sqBot = cy + 26f
                lineTo(waveStartX + 15f, sqTop)
                lineTo(waveStartX + 45f, sqTop)
                lineTo(waveStartX + 50f, sqBot)
                lineTo(waveStartX + 80f, sqBot)
                lineTo(waveStartX + 85f, sqTop)
                lineTo(waveStartX + 105f, sqTop)
            }
            drawPath(wavePath, CyanNeon, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        "ideal_gas_piston" -> {
            val cyW = w * 0.45f
            val cyH = h * 0.72f
            val cx = (w - cyW) / 2f
            val cy = (h - cyH) / 2f

            // Cylinder
            drawRect(Color(0x55FFFFFF), Offset(cx, cy), Size(6f, cyH))
            drawRect(Color(0x55FFFFFF), Offset(cx + cyW - 6f, cy), Size(6f, cyH))
            drawRect(Color(0xFF455A64), Offset(cx - 4f, cy + cyH), Size(cyW + 8f, 10f))

            // Piston head halfway down
            val py = cy + cyH * 0.42f
            drawRect(Color(0xFFB0BEC5), Offset(cx + cyW / 2f - 4f, cy - 10f), Size(8f, py - cy + 10f))
            drawRoundRect(Color(0xFF78909C), Offset(cx + 6f, py), Size(cyW - 12f, 14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f))

            // Compressed gas glow
            drawRect(CoralNeon.copy(alpha = 0.28f), Offset(cx + 6f, py + 14f), Size(cyW - 12f, cy + cyH - py - 14f))

            // Bouncing particles
            drawCircle(CyanNeon, 3.5f, Offset(cx + cyW * 0.3f, py + 25f))
            drawCircle(AmberVibrant, 4f, Offset(cx + cyW * 0.7f, py + 35f))
            drawCircle(CoralNeon, 3.5f, Offset(cx + cyW * 0.5f, py + 48f))
        }
        "kepler_orbits" -> {
            val cx = w * 0.5f
            val cy = h * 0.5f
            val a = w * 0.36f
            val b = h * 0.30f

            // Orbit ellipse
            val path = Path().apply {
                val steps = 40
                for (i in 0..steps) {
                    val th = i * (2 * PI.toFloat() / steps)
                    val px = cx + a * cos(th)
                    val py = cy + b * sin(th)
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(path, Color(0x66FFFFFF), style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))

            // Sun at focus F1 (offset to left)
            val sunX = cx - a * 0.45f
            drawCircle(AmberVibrant.copy(alpha = 0.35f), 18f, Offset(sunX, cy))
            drawCircle(Color(0xFFFFB300), 10f, Offset(sunX, cy))

            // Planet with velocity vector
            val planetX = cx + a * 0.95f
            val planetY = cy
            drawCircle(CyanNeon, 6f, Offset(planetX, planetY))
            drawLine(EmeraldNeon, Offset(planetX, planetY), Offset(planetX, planetY - 24f), 2.5f, StrokeCap.Round)

            // Equal area swept wedge (Kepler's 2nd Law)
            val wedgePath = Path().apply {
                moveTo(sunX, cy)
                lineTo(sunX + a * 0.4f, cy - b * 0.8f)
                lineTo(sunX + a * 0.1f, cy - b * 0.9f)
                close()
            }
            drawPath(wedgePath, AmberVibrant.copy(alpha = 0.3f))
        }
        "gyroscopic_precession" -> {
            val cx = w * 0.44f
            val cy = h * 0.52f
            val floorY = h * 0.82f

            // 1. Floor perspective grid / base oval
            drawOval(
                color = ScienceBorder.copy(alpha = 0.35f),
                topLeft = Offset(cx - 55f, floorY - 10f),
                size = Size(110f, 20f),
                style = Stroke(width = 1.5f)
            )

            // 2. Vertical Support Column & Pedestal
            drawLine(
                brush = Brush.verticalGradient(listOf(Color(0xFFB0BEC5), Color(0xFF37474F))),
                start = Offset(cx, cy),
                end = Offset(cx, floorY),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            // 3. Precession Horizontal Orbit Ring (dashed Cyan)
            val orbitRx = 68f
            val orbitRy = 22f
            drawOval(
                color = CyanNeon.copy(alpha = 0.35f),
                topLeft = Offset(cx - orbitRx, cy - orbitRy - 12f),
                size = Size(orbitRx * 2f, orbitRy * 2f),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
            )

            // 4. Tilted Axle
            val wheelCenterX = cx + 48f
            val wheelCenterY = cy - 14f
            val axleTipX = cx + 68f
            val axleTipY = cy - 20f

            // Axle shaft
            drawLine(
                color = Color(0xFFCFD8DC),
                start = Offset(cx, cy),
                end = Offset(axleTipX, axleTipY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(cx, cy),
                end = Offset(axleTipX, axleTipY),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )

            // 5. Spinning Flywheel / Wheel (tilted ellipse)
            val wheelR = 26f
            val wheelThickness = 12f
            // Outer rim with gradient
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(CyanNeon.copy(alpha = 0.2f), CyanNeon, Color.White),
                    center = Offset(wheelCenterX, wheelCenterY),
                    radius = wheelR
                ),
                topLeft = Offset(wheelCenterX - wheelThickness * 0.5f, wheelCenterY - wheelR),
                size = Size(wheelThickness, wheelR * 2f),
                style = Stroke(width = 3.5f)
            )
            // Spokes
            drawLine(CyanNeon.copy(alpha = 0.7f), Offset(wheelCenterX, wheelCenterY - wheelR + 2f), Offset(wheelCenterX, wheelCenterY + wheelR - 2f), 1.5f)
            drawLine(CyanNeon.copy(alpha = 0.7f), Offset(wheelCenterX - wheelThickness * 0.4f, wheelCenterY - wheelR * 0.5f), Offset(wheelCenterX + wheelThickness * 0.4f, wheelCenterY + wheelR * 0.5f), 1.5f)
            drawLine(CyanNeon.copy(alpha = 0.7f), Offset(wheelCenterX - wheelThickness * 0.4f, wheelCenterY + wheelR * 0.5f), Offset(wheelCenterX + wheelThickness * 0.4f, wheelCenterY - wheelR * 0.5f), 1.5f)

            // Central wheel hub
            drawCircle(Color.White, 3.5f, Offset(wheelCenterX, wheelCenterY))

            // 6. Vector Arrows:
            // L: Angular Momentum along axle (CyanNeon)
            drawLine(CyanNeon, Offset(wheelCenterX, wheelCenterY), Offset(axleTipX + 18f, axleTipY - 6f), 2.5f, StrokeCap.Round)
            val lHead = Path().apply {
                val tx = axleTipX + 18f
                val ty = axleTipY - 6f
                moveTo(tx, ty)
                lineTo(tx - 6f, ty + 2f)
                lineTo(tx - 4f, ty - 5f)
                close()
            }
            drawPath(lHead, CyanNeon)

            // τ: Gravitational Torque horizontal perpendicular (CoralNeon)
            val tauEndX = wheelCenterX + 4f
            val tauEndY = wheelCenterY + 22f
            drawLine(CoralNeon, Offset(wheelCenterX, wheelCenterY), Offset(tauEndX, tauEndY), 2.5f, StrokeCap.Round)
            val tauHead = Path().apply {
                moveTo(tauEndX, tauEndY)
                lineTo(tauEndX - 4f, tauEndY - 6f)
                lineTo(tauEndX + 4f, tauEndY - 4f)
                close()
            }
            drawPath(tauHead, CoralNeon)

            // Ω_p: Precession Velocity straight UP from pivot (EmeraldNeon)
            drawLine(EmeraldNeon, Offset(cx, cy), Offset(cx, cy - 38f), 2.5f, StrokeCap.Round)
            val omegaHead = Path().apply {
                moveTo(cx, cy - 38f)
                lineTo(cx - 4f, cy - 30f)
                lineTo(cx + 4f, cy - 30f)
                close()
            }
            drawPath(omegaHead, EmeraldNeon)

            // 7. Pivot Gimbal Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, AmberVibrant, Color(0xFFE65100)),
                    center = Offset(cx - 1.5f, cy - 1.5f),
                    radius = 6f
                ),
                radius = 5.5f,
                center = Offset(cx, cy)
            )
        }
        "coriolis_effect" -> {
            val cx = w * 0.48f
            val cy = h * 0.52f
            val rx = w * 0.42f
            val ry = h * 0.38f

            // 1. Perspective Turntable Platter (tilted oval)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF263238), Color(0xFF161E22), Color(0xFF0B0F12)),
                    center = Offset(cx, cy),
                    radius = rx
                ),
                topLeft = Offset(cx - rx, cy - ry),
                size = Size(rx * 2f, ry * 2f)
            )
            // Turntable Rim Ring
            drawOval(
                color = ScienceBorder.copy(alpha = 0.45f),
                topLeft = Offset(cx - rx, cy - ry),
                size = Size(rx * 2f, ry * 2f),
                style = Stroke(width = 1.5f)
            )
            // Concentric Range Ring
            drawOval(
                color = CyanNeon.copy(alpha = 0.25f),
                topLeft = Offset(cx - rx * 0.55f, cy - ry * 0.55f),
                size = Size(rx * 1.1f, ry * 1.1f),
                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
            )

            // 2. Rotating Turntable Spoke Crosshairs
            drawLine(ScienceBorder.copy(alpha = 0.35f), Offset(cx - rx * 0.9f, cy), Offset(cx + rx * 0.9f, cy), 1f)
            drawLine(ScienceBorder.copy(alpha = 0.35f), Offset(cx, cy - ry * 0.9f), Offset(cx, cy + ry * 0.9f), 1f)

            // 3. Rotation Direction Indicator (CCW curved arc in AmberVibrant)
            val rotPath = Path().apply {
                val rInd = rx * 0.75f
                val rIndY = ry * 0.75f
                moveTo(cx + rInd * 0.6f, cy - rIndY * 0.8f)
                quadraticTo(
                    cx - rInd * 0.2f, cy - rIndY * 1.1f,
                    cx - rInd * 0.85f, cy - rIndY * 0.3f
                )
            }
            drawPath(rotPath, AmberVibrant.copy(alpha = 0.7f), style = Stroke(width = 1.5f, cap = StrokeCap.Round))
            val rotHead = Path().apply {
                val hx = cx - rx * 0.85f * 0.75f
                val hy = cy - ry * 0.3f * 0.75f
                moveTo(hx, hy)
                lineTo(hx + 4f, hy - 5f)
                lineTo(hx + 6f, hy + 2f)
                close()
            }
            drawPath(rotHead, AmberVibrant)

            // 4. Inertial Straight Line Path (what external observer sees)
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(cx, cy),
                end = Offset(cx + rx * 0.82f, cy - ry * 0.78f),
                strokeWidth = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))
            )

            // 5. Coriolis Deflected Curved Trajectory (AmberVibrant glow + solid)
            val curvePath = Path().apply {
                moveTo(cx, cy)
                cubicTo(
                    cx + rx * 0.32f, cy - ry * 0.65f,
                    cx + rx * 0.75f, cy - ry * 0.45f,
                    cx + rx * 0.82f, cy + ry * 0.25f
                )
            }
            drawPath(curvePath, AmberVibrant.copy(alpha = 0.25f), style = Stroke(width = 4f, cap = StrokeCap.Round))
            drawPath(curvePath, AmberVibrant, style = Stroke(width = 2f, cap = StrokeCap.Round))

            // 6. Moving Particle on Trajectory
            val pX = cx + rx * 0.68f
            val pY = cy - ry * 0.18f

            drawCircle(CyanNeon.copy(alpha = 0.3f), 8f, Offset(pX, pY))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, CyanNeon, Color(0xFF00838F)),
                    center = Offset(pX, pY),
                    radius = 4f
                ),
                radius = 4f,
                center = Offset(pX, pY)
            )

            // 7. Dynamic Force / Velocity Vectors at the Particle:
            // Velocity vector v_rel (CyanNeon)
            val vEndX = pX + 16f
            val vEndY = pY + 14f
            drawLine(CyanNeon, Offset(pX, pY), Offset(vEndX, vEndY), 2f, StrokeCap.Round)
            val vHead = Path().apply {
                moveTo(vEndX, vEndY)
                lineTo(vEndX - 4f, vEndY - 1f)
                lineTo(vEndX - 1f, vEndY - 4f)
                close()
            }
            drawPath(vHead, CyanNeon)

            // Coriolis Acceleration vector a_cor perpendicular to the right (CoralNeon)
            val aCorEndX = pX - 12f
            val aCorEndY = pY + 12f
            drawLine(CoralNeon, Offset(pX, pY), Offset(aCorEndX, aCorEndY), 2f, StrokeCap.Round)
            val aCorHead = Path().apply {
                moveTo(aCorEndX, aCorEndY)
                lineTo(aCorEndX + 4f, aCorEndY)
                lineTo(aCorEndX + 1f, aCorEndY - 4f)
                close()
            }
            drawPath(aCorHead, CoralNeon)

            // Centrifugal Acceleration vector a_cent radially outward (PurpleNeon)
            val aCentEndX = pX + 14f
            val aCentEndY = pY - 6f
            drawLine(PurpleNeon, Offset(pX, pY), Offset(aCentEndX, aCentEndY), 1.8f, StrokeCap.Round)

            // 8. Turntable Center Spindle
            drawCircle(Color.White, 3.5f, Offset(cx, cy))
            drawCircle(CyanNeon, 2f, Offset(cx, cy))
        }
        "pencil_water_bag" -> {
            // Draw mini water bag with pencil
            val bagW = w * 0.45f
            val bagH = h * 0.7f
            val bx = (w - bagW) / 2f
            val by = (h - bagH) / 2f

            drawRoundRect(
                color = WaterBlue.copy(alpha = 0.5f),
                topLeft = Offset(bx, by + bagH * 0.25f),
                size = Size(bagW, bagH * 0.75f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
            )
            drawRoundRect(
                color = Color(0x66FFFFFF),
                topLeft = Offset(bx, by),
                size = Size(bagW, bagH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f),
                style = Stroke(width = 2.5f)
            )

            // Piercing Yellow Pencil
            drawLine(
                color = AmberVibrant,
                start = Offset(bx - 30f, by + bagH * 0.55f - 10f),
                end = Offset(bx + bagW + 30f, by + bagH * 0.55f + 10f),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
        }
        "disappearing_glass" -> {
            // Beaker with half-disappearing tube & laser
            val bw = w * 0.42f
            val bh = h * 0.7f
            val bx = (w - bw) / 2f
            val by = (h - bh) / 2f

            drawRect(
                color = AmberVibrant.copy(alpha = 0.35f),
                topLeft = Offset(bx, by + bh * 0.3f),
                size = Size(bw, bh * 0.7f)
            )
            drawRoundRect(
                color = Color(0x88FFFFFF),
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
                style = Stroke(width = 2.5f)
            )

            // Inner tube: Top visible, bottom invisible (n-matched)
            val tw = bw * 0.3f
            val tx = bx + (bw - tw) / 2f
            drawLine(Color.White, Offset(tx, by - 15f), Offset(tx, by + bh * 0.3f), 2.5f)
            drawLine(Color.White, Offset(tx + tw, by - 15f), Offset(tx + tw, by + bh * 0.3f), 2.5f)

            // Green Laser line straight across
            drawLine(EmeraldNeon, Offset(bx - 25f, by + bh * 0.6f), Offset(bx + bw + 25f, by + bh * 0.6f), 3f)
        }
        "static_straw" -> {
            // Bottle with balanced straw & spark
            val cx = w * 0.5f
            val cy = h * 0.55f

            // Bottle neck
            drawLine(Color(0x66FFFFFF), Offset(cx, cy), Offset(cx, cy + 50f), 24f)
            // Cap
            drawCircle(AmberVibrant, 7f, Offset(cx, cy))
            // Straw angled
            drawLine(StrawPlastic, Offset(cx - 70f, cy - 15f), Offset(cx + 70f, cy + 15f), 7f, StrokeCap.Round)
            // Wand tip with sparks
            drawCircle(CyanNeon.copy(alpha = 0.4f), 18f, Offset(cx + 85f, cy + 20f))
            drawCircle(CyanNeon, 6f, Offset(cx + 85f, cy + 20f))
        }
        "citrus_balloon" -> {
            // Balloon & orange peel
            val cx = w * 0.42f
            val cy = h * 0.48f

            // Balloon
            drawOval(
                brush = Brush.radialGradient(listOf(Color.White, BalloonOrange, Color(0xFFBF360C))),
                topLeft = Offset(cx - 36f, cy - 45f),
                size = Size(72f, 90f)
            )
            // Peel arc
            drawArc(
                color = CitrusOrange,
                startAngle = 120f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(w * 0.68f, cy - 25f),
                size = Size(40f, 50f),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
            // Droplet mist
            drawCircle(CitrusZest, 2.5f, Offset(w * 0.58f, cy - 10f))
            drawCircle(CitrusZest, 3f, Offset(w * 0.55f, cy + 5f))
            drawCircle(CitrusZest, 2.5f, Offset(w * 0.52f, cy - 4f))
        }
        "cartesian_diver" -> {
            val bw = min(w * 0.34f, 80f)
            val bh = h * 0.76f
            val bx = (w - bw) / 2f
            val by = h * 0.12f
            val cornerR = 12f
            val neckW = bw * 0.42f
            val neckH = 14f

            // 1. Water Fill inside Bottle
            val waterPath = Path().apply {
                moveTo(bx + (bw - neckW) / 2f, by + neckH)
                quadraticTo(bx + bw, by + neckH + 8f, bx + bw - 4f, by + bh * 0.45f) // Right waist squeeze
                quadraticTo(bx + bw - 6f, by + bh * 0.55f, bx + bw, by + bh - cornerR)
                quadraticTo(bx + bw, by + bh, bx + bw - cornerR, by + bh)
                lineTo(bx + cornerR, by + bh)
                quadraticTo(bx, by + bh, bx, by + bh - cornerR)
                quadraticTo(bx + 6f, by + bh * 0.55f, bx + 4f, by + bh * 0.45f) // Left waist squeeze
                quadraticTo(bx, by + neckH + 8f, bx + (bw - neckW) / 2f, by + neckH)
                close()
            }
            drawPath(
                path = waterPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00ACC1).copy(alpha = 0.45f), Color(0xFF01579B).copy(alpha = 0.80f)),
                    startY = by + neckH,
                    endY = by + bh
                )
            )

            // 2. Clear Plastic Bottle Outline
            drawPath(
                path = waterPath,
                color = Color.White.copy(alpha = 0.6f),
                style = Stroke(width = 2f)
            )

            // 3. Blue Bottle Cap
            drawRoundRect(
                color = Color(0xFF1976D2),
                topLeft = Offset(bx + (bw - neckW - 4f) / 2f, by),
                size = Size(neckW + 4f, neckH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )

            // 4. Squeeze Hand Grip Cues (Coral neon squeeze pinch lines)
            val squeezeY = by + bh * 0.50f
            drawLine(CoralNeon, Offset(bx - 8f, squeezeY), Offset(bx - 2f, squeezeY), 2.5f, StrokeCap.Round)
            drawLine(CoralNeon, Offset(bx + bw + 8f, squeezeY), Offset(bx + bw + 2f, squeezeY), 2.5f, StrokeCap.Round)

            // 5. Eyedropper Cartesian Diver inside Water
            val dx = w * 0.5f
            val dy = by + bh * 0.44f
            val diverH = 34f
            val diverW = 12f

            // Red bulb on top
            drawCircle(Color(0xFFE53935), 4.5f, Offset(dx, dy - diverH * 0.5f))

            // Glass barrel
            drawRoundRect(
                color = Color.White.copy(alpha = 0.75f),
                topLeft = Offset(dx - diverW * 0.5f, dy - diverH * 0.35f),
                size = Size(diverW, diverH * 0.65f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
                style = Stroke(width = 1.2f)
            )

            // Compressed Cyan Air Bubble inside Diver
            val bubbleH = diverH * 0.32f
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color.White, CyanNeon)),
                topLeft = Offset(dx - diverW * 0.5f + 1.5f, dy - diverH * 0.35f + 1.5f),
                size = Size(diverW - 3f, bubbleH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
            )

            // Brass ballast coil at bottom
            drawLine(Color(0xFFFFB300), Offset(dx - 4f, dy + diverH * 0.28f), Offset(dx + 4f, dy + diverH * 0.28f), 2f, StrokeCap.Round)
            drawLine(Color(0xFFFFB300), Offset(dx - 4f, dy + diverH * 0.36f), Offset(dx + 4f, dy + diverH * 0.36f), 2f, StrokeCap.Round)

            // 6. Upward Buoyant Force Vector (Fb) & Downward Weight Vector (W)
            val vecX = dx + diverW + 6f
            // Fb arrow (Cyan)
            drawLine(CyanNeon, Offset(vecX, dy - 1f), Offset(vecX, dy - 14f), 2f, StrokeCap.Round)
            // W arrow (Amber)
            drawLine(AmberVibrant, Offset(vecX, dy + 1f), Offset(vecX, dy + 14f), 2f, StrokeCap.Round)

            // 7. Rising tiny bubbles
            drawCircle(Color.White.copy(alpha = 0.6f), 1.5f, Offset(dx - 4f, dy - diverH * 0.6f))
            drawCircle(Color.White.copy(alpha = 0.4f), 1f, Offset(dx + 3f, dy - diverH * 0.8f))
        }
        "oobleck" -> {
            val cx = w * 0.5f
            val cy = h * 0.56f
            val dishRx = min(w * 0.42f, 95f)
            val dishRy = dishRx * 0.56f

            // 1. Drop shadow under dish
            drawOval(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(cx - dishRx * 0.95f, cy - dishRy * 0.8f + 10f),
                size = Size(dishRx * 1.9f, dishRy * 1.6f)
            )

            // 2. Glass Petri Dish Base & Rim
            drawOval(
                color = Color(0xFF263238),
                topLeft = Offset(cx - dishRx * 1.05f, cy - dishRy * 1.05f),
                size = Size(dishRx * 2.1f, dishRy * 2.1f)
            )
            drawOval(
                color = Color.White.copy(alpha = 0.55f),
                topLeft = Offset(cx - dishRx * 1.02f, cy - dishRy * 1.02f),
                size = Size(dishRx * 2.04f, dishRy * 2.04f),
                style = Stroke(width = 2.5f)
            )

            // 3. Oobleck Fluid Pool (gradient from minty turquoise to chalky cyan)
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA), Color(0xFF00ACC1)),
                    startX = cx - dishRx,
                    endX = cx + dishRx
                ),
                topLeft = Offset(cx - dishRx * 0.92f, cy - dishRy * 0.92f),
                size = Size(dishRx * 1.84f, dishRy * 1.84f)
            )

            // 4. Left Impact Zone: Punch Strike & Crystalline Fracture Web (Solid State)
            val impactX = cx - dishRx * 0.38f
            val impactY = cy - dishRy * 0.12f

            // Impact Fist / Hammer Head
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFCFD8DC), Color(0xFF455A64))),
                topLeft = Offset(impactX - 12f, impactY - 26f),
                size = Size(24f, 18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            drawLine(Color(0xFF8D6E63), Offset(impactX, impactY - 26f), Offset(impactX - 10f, impactY - 44f), 5f, StrokeCap.Round)

            // Solid Stress Fracture Rays radiating from impact point
            val rays = 5
            for (i in 0 until rays) {
                val angle = i * (PI.toFloat() / (rays - 1)) + 0.3f
                val crackLen = 22f
                val ex = impactX + cos(angle) * crackLen
                val ey = impactY + sin(angle) * crackLen * 0.6f
                drawLine(Color.White, Offset(impactX, impactY), Offset(ex, ey), 1.8f, StrokeCap.Round)
            }
            // Impact shockwave ring
            drawOval(
                color = CyanNeon.copy(alpha = 0.8f),
                topLeft = Offset(impactX - 16f, impactY - 9f),
                size = Size(32f, 18f),
                style = Stroke(width = 1.5f)
            )

            // 5. Right Zone: Smooth Viscous Ripples & Melting Drips (Liquid State)
            val dripX = cx + dishRx * 0.42f
            val dripY = cy + dishRy * 0.20f
            // Viscous concentric liquid rings
            drawOval(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(dripX - 15f, dripY - 8f),
                size = Size(30f, 16f),
                style = Stroke(width = 1.2f)
            )
            drawOval(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(dripX - 22f, dripY - 12f),
                size = Size(44f, 24f),
                style = Stroke(width = 1f)
            )

            // Liquid teardrop dripping downward
            drawCircle(Color(0xFF80DEEA), 3f, Offset(dripX + 8f, cy + dishRy * 0.85f))
            drawCircle(Color(0xFF80DEEA), 2f, Offset(dripX + 8f, cy + dishRy * 0.85f + 8f))
        }
        "bernoulli_ball" -> {
            val nozzleX = w * 0.36f
            val nozzleY = h * 0.82f
            val tiltAngleRad = 26f * (PI.toFloat() / 180f)
            val streamDir = Offset(sin(tiltAngleRad), -cos(tiltAngleRad))
            val streamNorm = Offset(cos(tiltAngleRad), sin(tiltAngleRad))
            val streamLen = 135f

            // 1. Tilted Airflow Jet Cone (CyanNeon glow)
            val conePath = Path().apply {
                val bLeft = Offset(nozzleX - streamNorm.x * 12f, nozzleY - streamNorm.y * 12f)
                val bRight = Offset(nozzleX + streamNorm.x * 12f, nozzleY + streamNorm.y * 12f)
                val tLeft = Offset(nozzleX + streamDir.x * streamLen - streamNorm.x * 32f, nozzleY + streamDir.y * streamLen - streamNorm.y * 32f)
                val tRight = Offset(nozzleX + streamDir.x * streamLen + streamNorm.x * 32f, nozzleY + streamDir.y * streamLen + streamNorm.y * 32f)

                moveTo(bLeft.x, bLeft.y)
                lineTo(tLeft.x, tLeft.y)
                lineTo(tRight.x, tRight.y)
                lineTo(bRight.x, bRight.y)
                close()
            }
            drawPath(
                path = conePath,
                brush = Brush.radialGradient(
                    colors = listOf(CyanNeon.copy(alpha = 0.35f), CyanNeon.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(nozzleX + streamDir.x * (streamLen * 0.5f), nozzleY + streamDir.y * (streamLen * 0.5f)),
                    radius = streamLen * 0.7f
                )
            )

            // 2. Air Streamlines (Flowing upward in jet)
            for (i in -1..1) {
                val offset = streamNorm * (i * 12f)
                val sStart = Offset(nozzleX + offset.x, nozzleY + offset.y)
                val sEnd = Offset(nozzleX + streamDir.x * streamLen + offset.x * 2.2f, nozzleY + streamDir.y * streamLen + offset.y * 2.2f)
                drawLine(
                    color = CyanNeon.copy(alpha = 0.6f),
                    start = sStart,
                    end = sEnd,
                    strokeWidth = 1.6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }

            // 3. Levitating Ping-Pong Ball (Hovering trapped in stream)
            val ballDist = streamLen * 0.62f
            val bx = nozzleX + streamDir.x * ballDist
            val by = nozzleY + streamDir.y * ballDist
            val ballR = 11f

            // Coandă effect curved streamline hugging top of ball
            drawArc(
                color = CyanNeon,
                startAngle = -110f,
                sweepAngle = 75f,
                useCenter = false,
                topLeft = Offset(bx - ballR * 1.35f, by - ballR * 1.35f),
                size = Size(ballR * 2.7f, ballR * 2.7f),
                style = Stroke(width = 2f)
            )

            // 3D Ping-Pong Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF3E0), Color(0xFFFFB74D), Color(0xFFE65100)),
                    center = Offset(bx - ballR * 0.3f, by - ballR * 0.35f),
                    radius = ballR * 1.25f
                ),
                radius = ballR,
                center = Offset(bx, by)
            )
            // White specular highlight
            drawCircle(Color.White.copy(alpha = 0.95f), ballR * 0.25f, Offset(bx - ballR * 0.3f, by - ballR * 0.35f))
            // Outer neon rim
            drawCircle(CyanNeon.copy(alpha = 0.7f), ballR, Offset(bx, by), style = Stroke(width = 1.2f))

            // 4. Inward Bernoulli Restoring Pressure Arrow
            val arrowStart = Offset(bx + streamNorm.x * (ballR + 14f), by + streamNorm.y * (ballR + 14f))
            val arrowEnd = Offset(bx + streamNorm.x * (ballR + 3f), by + streamNorm.y * (ballR + 3f))
            drawLine(AmberVibrant, arrowStart, arrowEnd, 2f, StrokeCap.Round)

            // 5. Tilted Hairdryer Blower Barrel & Stand at Base
            rotate(degrees = 26f, pivot = Offset(nozzleX, nozzleY)) {
                // Metal Barrel
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF78909C), Color(0xFFECEFF1), Color(0xFF37474F))),
                    topLeft = Offset(nozzleX - 11f, nozzleY - 18f),
                    size = Size(22f, 36f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                // Nozzle rim with blue LED turbine glow
                drawOval(CyanNeon, topLeft = Offset(nozzleX - 11f, nozzleY - 21f), size = Size(22f, 6f))
            }
            // Stand base
            drawRoundRect(
                color = Color(0xFF37474F),
                topLeft = Offset(nozzleX - 18f, nozzleY + 8f),
                size = Size(36f, 10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
        }
        else -> {
            // Futuristic laboratory wireframe for upcoming experiments
            val cx = w * 0.5f
            val cy = h * 0.5f
            drawCircle(CyanNeon.copy(alpha = 0.12f), 45f, Offset(cx, cy))
            drawCircle(
                color = CyanNeon.copy(alpha = 0.4f),
                radius = 32f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
            )
            // Tilted atomic / orbital ellipse
            drawArc(
                color = PurpleNeon.copy(alpha = 0.6f),
                startAngle = 20f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(cx - 38f, cy - 20f),
                size = Size(76f, 40f),
                style = Stroke(width = 2f)
            )
            // Core spark
            drawCircle(AmberVibrant, 6f, Offset(cx, cy))
            drawCircle(Color.White, 2.5f, Offset(cx, cy))
        }
    }
}
