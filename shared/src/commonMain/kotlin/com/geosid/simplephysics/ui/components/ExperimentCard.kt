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
            val groundY = h * 0.76f
            // Lab floor line & subtle floor fill
            drawLine(ScienceBorder, Offset(0f, groundY), Offset(w, groundY), 2.5f)
            drawRect(
                color = Color(0x12FFFFFF),
                topLeft = Offset(0f, groundY),
                size = Size(w, h - groundY)
            )

            // Geometric bouncing trajectory curves
            val h1 = h * 0.44f
            val h2 = h * 0.28f
            val h3 = h * 0.16f
            val h4 = h * 0.08f

            val bouncePath = Path().apply {
                moveTo(w * 0.12f, groundY)
                // Bounce 1
                cubicTo(w * 0.16f, groundY - h1, w * 0.30f, groundY - h1, w * 0.36f, groundY)
                // Bounce 2
                cubicTo(w * 0.41f, groundY - h2, w * 0.53f, groundY - h2, w * 0.58f, groundY)
                // Bounce 3
                cubicTo(w * 0.62f, groundY - h3, w * 0.72f, groundY - h3, w * 0.76f, groundY)
                // Bounce 4
                cubicTo(w * 0.79f, groundY - h4, w * 0.87f, groundY - h4, w * 0.90f, groundY)
            }
            drawPath(
                bouncePath,
                AmberVibrant.copy(alpha = 0.85f),
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
            )

            // Impact shockwave ring at contact point 1
            val impactX = w * 0.36f
            drawCircle(
                color = CoralNeon.copy(alpha = 0.7f),
                radius = 10f,
                center = Offset(impactX, groundY),
                style = Stroke(width = 1.5f)
            )
            // Impact spark lines
            drawLine(CoralNeon, Offset(impactX - 12f, groundY - 6f), Offset(impactX - 6f, groundY - 2f), 1.5f)
            drawLine(CoralNeon, Offset(impactX + 12f, groundY - 6f), Offset(impactX + 6f, groundY - 2f), 1.5f)

            // Apex 1: Primary 3D Ball
            val apex1X = w * 0.24f
            val apex1Y = groundY - h1
            drawOval(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = Offset(apex1X - 14f, groundY - 3f),
                size = Size(28f, 6f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, AmberVibrant, Color(0xFFE65100), Color(0xFF261208)),
                    center = Offset(apex1X - 3f, apex1Y - 3f),
                    radius = 14f
                ),
                radius = 11f,
                center = Offset(apex1X, apex1Y)
            )

            // Apex 2: Ghost Ball (Cyan)
            val apex2X = w * 0.48f
            val apex2Y = groundY - h2
            drawOval(
                color = Color.Black.copy(alpha = 0.25f),
                topLeft = Offset(apex2X - 11f, groundY - 3f),
                size = Size(22f, 5f)
            )
            drawCircle(
                color = CyanNeon.copy(alpha = 0.65f),
                radius = 8.5f,
                center = Offset(apex2X, apex2Y),
                style = Stroke(width = 1.8f)
            )
            drawCircle(
                color = CyanNeon.copy(alpha = 0.2f),
                radius = 8.5f,
                center = Offset(apex2X, apex2Y)
            )
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
            val baseY = h * 0.50f
            val leftX = w * 0.10f
            val rightX = w * 0.90f
            val stringLen = rightX - leftX

            // 1. Soundboard base
            drawRoundRect(
                color = Color(0xFF3E2723),
                topLeft = Offset(leftX - 10f, baseY - 36f),
                size = Size(stringLen + 20f, 72f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
            )
            drawRoundRect(
                color = Color(0xFF4E342E),
                topLeft = Offset(leftX - 8f, baseY - 33f),
                size = Size(stringLen + 16f, 66f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f)
            )

            // Sound hole
            drawCircle(
                color = Color(0xFF1B0000),
                radius = 24f,
                center = Offset(w * 0.5f, baseY)
            )
            drawCircle(
                color = AmberVibrant.copy(alpha = 0.7f),
                radius = 25f,
                center = Offset(w * 0.5f, baseY),
                style = Stroke(width = 1.5f)
            )

            // 2. Fixed End Pegs (Nuts)
            drawRect(Color(0xFFECEFF1), Offset(leftX - 6f, baseY - 20f), Size(6f, 40f))
            drawRect(Color(0xFFECEFF1), Offset(rightX, baseY - 20f), Size(6f, 40f))

            // 3. Standing Harmonic Wave (n=2 mode with center node)
            val wavePath = Path().apply {
                moveTo(leftX, baseY)
                cubicTo(leftX + stringLen * 0.22f, baseY - 32f, leftX + stringLen * 0.28f, baseY - 32f, w * 0.50f, baseY)
                cubicTo(leftX + stringLen * 0.72f, baseY + 32f, leftX + stringLen * 0.78f, baseY + 32f, rightX, baseY)
            }
            // Antiphase envelope (dashed reflection)
            val envelopePath = Path().apply {
                moveTo(leftX, baseY)
                cubicTo(leftX + stringLen * 0.22f, baseY + 32f, leftX + stringLen * 0.28f, baseY + 32f, w * 0.50f, baseY)
                cubicTo(leftX + stringLen * 0.72f, baseY - 32f, leftX + stringLen * 0.78f, baseY - 32f, rightX, baseY)
            }

            // Antiphase envelope dashed
            drawPath(
                path = envelopePath,
                color = CoralNeon.copy(alpha = 0.45f),
                style = Stroke(width = 1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
            )

            // Outer string glow & core
            drawPath(wavePath, CyanNeon.copy(alpha = 0.45f), style = Stroke(width = 7f, cap = StrokeCap.Round))
            drawPath(wavePath, Color.White, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

            // Central standing wave stationary node
            drawCircle(AmberVibrant, 4f, Offset(w * 0.5f, baseY))
            drawCircle(Color.White, 2f, Offset(w * 0.5f, baseY))

            // Acoustic sound radiation ripples
            drawCircle(
                color = CyanNeon.copy(alpha = 0.30f),
                radius = 35f,
                center = Offset(w * 0.5f, baseY),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            )
            drawCircle(
                color = CyanNeon.copy(alpha = 0.18f),
                radius = 48f,
                center = Offset(w * 0.5f, baseY),
                style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
            )
        }
        "fourier_series" -> {
            val cy = h * 0.46f
            val cx = w * 0.22f

            // Epicycle 1 (Fundamental n=1)
            val r1 = 30f
            drawCircle(CyanNeon.copy(alpha = 0.35f), r1, Offset(cx, cy), style = Stroke(width = 1.8f))
            val v1End = Offset(cx + 21f, cy - 21f)
            drawLine(CyanNeon, Offset(cx, cy), v1End, 2.5f, StrokeCap.Round)
            drawCircle(CyanNeon, 3f, Offset(cx, cy))

            // Epicycle 2 (Harmonic n=3)
            val r2 = 11f
            drawCircle(AmberVibrant.copy(alpha = 0.45f), r2, v1End, style = Stroke(width = 1.4f))
            val v2End = Offset(v1End.x + 8f, v1End.y + 7f)
            drawLine(AmberVibrant, v1End, v2End, 2f, StrokeCap.Round)
            drawCircle(AmberVibrant, 2.5f, v1End)

            // Epicycle 3 (Harmonic n=5)
            val r3 = 6f
            drawCircle(PurpleNeon.copy(alpha = 0.5f), r3, v2End, style = Stroke(width = 1.2f))
            val v3End = Offset(v2End.x + 4f, v2End.y - 4f)
            drawLine(PurpleNeon, v2End, v3End, 1.5f, StrokeCap.Round)
            drawCircle(Color.White, 3f, v3End)

            // Laser projection line to synthesized wave
            val waveStartX = w * 0.44f
            val waveEndX = w * 0.92f
            drawLine(
                color = CoralNeon.copy(alpha = 0.85f),
                start = v3End,
                end = Offset(waveStartX, v3End.y),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            )
            drawCircle(CoralNeon, 4f, Offset(waveStartX, v3End.y))

            // Horizontal baseline
            drawLine(ScienceBorder.copy(alpha = 0.5f), Offset(waveStartX, cy), Offset(waveEndX, cy), 1f)

            // Synthesized Square Wave with Gibbs Phenomenon overshoot ripples
            val wavePath = Path().apply {
                moveTo(waveStartX, cy - 25f)
                // Gibbs ripple top
                cubicTo(waveStartX + 6f, cy - 32f, waveStartX + 12f, cy - 22f, waveStartX + 18f, cy - 25f)
                lineTo(waveStartX + 38f, cy - 25f)
                // Fall
                lineTo(waveStartX + 42f, cy + 25f)
                // Gibbs ripple bottom
                cubicTo(waveStartX + 48f, cy + 32f, waveStartX + 54f, cy + 22f, waveStartX + 60f, cy + 25f)
                lineTo(waveStartX + 80f, cy + 25f)
                // Rise
                lineTo(waveStartX + 84f, cy - 25f)
                cubicTo(waveStartX + 90f, cy - 32f, waveStartX + 96f, cy - 22f, waveStartX + 102f, cy - 25f)
                lineTo(waveEndX, cy - 25f)
            }
            drawPath(wavePath, CyanNeon.copy(alpha = 0.4f), style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(wavePath, CyanNeon, style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Mini FFT Harmonic Bars on bottom
            val fftStartX = waveStartX + 20f
            val fftBaseY = h * 0.82f
            drawLine(Color.White.copy(alpha = 0.3f), Offset(fftStartX - 5f, fftBaseY), Offset(waveEndX, fftBaseY), 1f)
            val barHeights = listOf(22f, 0f, 13f, 0f, 8f, 0f, 5f)
            val barW = 6f
            for (idx in barHeights.indices) {
                val bh = barHeights[idx]
                if (bh > 0f) {
                    val bx = fftStartX + idx * (barW + 4f)
                    drawRoundRect(
                        color = if (idx == 0) CyanNeon else AmberVibrant,
                        topLeft = Offset(bx, fftBaseY - bh),
                        size = Size(barW, bh),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
                    )
                }
            }
        }
        "chladni_plates" -> {
            val cx = w * 0.5f
            val cy = h * 0.5f
            val plateSize = min(w * 0.74f, h * 0.76f)
            val hp = plateSize * 0.5f
            val pLeft = cx - hp
            val pTop = cy - hp

            // 1. Plate outer shadow & metal body
            drawRoundRect(
                color = CyanNeon.copy(alpha = 0.12f),
                topLeft = Offset(pLeft - 4f, pTop - 4f),
                size = Size(plateSize + 8f, plateSize + 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
            )
            drawRoundRect(
                color = Color(0xFF141923),
                topLeft = Offset(pLeft, pTop),
                size = Size(plateSize, plateSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
            )
            drawRoundRect(
                color = ScienceBorder.copy(alpha = 0.85f),
                topLeft = Offset(pLeft, pTop),
                size = Size(plateSize, plateSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f),
                style = Stroke(width = 2f)
            )

            // Corner bolts
            val bOff = 6f
            val bRad = 2.5f
            val bolts = listOf(
                Offset(pLeft + bOff, pTop + bOff),
                Offset(pLeft + plateSize - bOff, pTop + bOff),
                Offset(pLeft + bOff, pTop + plateSize - bOff),
                Offset(pLeft + plateSize - bOff, pTop + plateSize - bOff)
            )
            for (b in bolts) {
                drawCircle(Color(0xFF90A4AE), bRad, b)
            }

            // 2. Vibration Antinode Patches (Purple glow)
            val patchR = hp * 0.40f
            drawCircle(PurpleNeon.copy(alpha = 0.22f), patchR, Offset(cx - hp * 0.5f, cy - hp * 0.5f))
            drawCircle(PurpleNeon.copy(alpha = 0.22f), patchR, Offset(cx + hp * 0.5f, cy - hp * 0.5f))
            drawCircle(PurpleNeon.copy(alpha = 0.22f), patchR, Offset(cx - hp * 0.5f, cy + hp * 0.5f))
            drawCircle(PurpleNeon.copy(alpha = 0.22f), patchR, Offset(cx + hp * 0.5f, cy + hp * 0.5f))

            // 3. Chladni Nodal Lines (Geometric Curves)
            val ringPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(cx - hp * 0.45f, cy - hp * 0.45f, cx + hp * 0.45f, cy + hp * 0.45f))
            }
            drawPath(ringPath, CyanNeon.copy(alpha = 0.45f), style = Stroke(width = 1.5f))

            // Hyperbolic nodal lines connecting to edges
            val hyperPath = Path().apply {
                moveTo(pLeft + 8f, cy)
                cubicTo(cx - hp * 0.45f, cy - hp * 0.2f, cx - hp * 0.2f, pTop + hp * 0.45f, cx, pTop + 8f)
                moveTo(cx + hp * 0.45f, cy - hp * 0.2f)
                cubicTo(cx + hp * 0.45f, cy - hp * 0.2f, cx + hp * 0.2f, pTop + hp * 0.45f, cx, pTop + 8f)
                moveTo(pLeft + 8f, cy)
                cubicTo(cx - hp * 0.45f, cy + hp * 0.2f, cx - hp * 0.2f, cy + hp * 0.45f, cx, pTop + plateSize - 8f)
                moveTo(pLeft + plateSize - 8f, cy)
                cubicTo(cx + hp * 0.45f, cy + hp * 0.2f, cx + hp * 0.2f, cy + hp * 0.45f, cx, pTop + plateSize - 8f)
                moveTo(pLeft + plateSize - 8f, cy)
                cubicTo(cx + hp * 0.45f, cy - hp * 0.2f, cx + hp * 0.2f, pTop + hp * 0.45f, cx, pTop + 8f)
            }
            drawPath(hyperPath, CyanNeon.copy(alpha = 0.5f), style = Stroke(width = 1.5f))

            // 4. Clustered Sand Grains (AmberVibrant points along nodal lines)
            val sandCoords = listOf(
                // Ring particles
                0.0f to 0.45f, 0.32f to 0.32f, 0.45f to 0.0f, 0.32f to -0.32f,
                0.0f to -0.45f, -0.32f to -0.32f, -0.45f to 0.0f, -0.32f to 0.32f,
                0.15f to 0.42f, 0.42f to 0.15f, 0.42f to -0.15f, 0.15f to -0.42f,
                -0.15f to -0.42f, -0.42f to -0.15f, -0.42f to 0.15f, -0.15f to 0.42f,
                // Hyperbolic branch particles
                -0.65f to 0.12f, -0.65f to -0.12f, 0.65f to 0.12f, 0.65f to -0.12f,
                0.12f to -0.65f, -0.12f to -0.65f, 0.12f to 0.65f, -0.12f to 0.65f,
                -0.80f to 0.05f, -0.80f to -0.05f, 0.80f to 0.05f, 0.80f to -0.05f,
                0.05f to -0.80f, -0.05f to -0.80f, 0.05f to 0.80f, -0.05f to 0.80f,
                // Scatter near nodes
                -0.38f to 0.25f, 0.38f to 0.25f, -0.38f to -0.25f, 0.38f to -0.25f
            )
            for ((nx, ny) in sandCoords) {
                val sx = cx + nx * hp
                val sy = cy + ny * hp
                drawCircle(AmberVibrant, 1.8f, Offset(sx, sy))
                drawCircle(Color.White.copy(alpha = 0.7f), 0.8f, Offset(sx, sy))
            }

            // 5. Central Vibration Exciter Post
            drawCircle(Color(0xFF263238), 9f, Offset(cx, cy))
            drawCircle(CyanNeon, 6f, Offset(cx, cy), style = Stroke(width = 1.5f))
            drawCircle(AmberVibrant, 2.5f, Offset(cx, cy))
        }
        "acoustic_resonance_shatter" -> {
            val cx = w * 0.62f
            val cy = h * 0.36f
            val glassR = min(w * 0.22f, h * 0.20f)
            val stemLen = glassR * 1.35f
            val footW = glassR * 1.1f
            val bowlH = glassR * 1.15f
            val footY = cy + bowlH + stemLen

            // 1. Acoustic Speaker Transducer on Left
            val spkX = w * 0.16f
            val spkY = cy + bowlH * 0.2f
            val spkH = h * 0.38f
            val spkW = w * 0.10f

            // Cabinet & Horn
            drawRoundRect(
                color = Color(0xFF1E2638),
                topLeft = Offset(spkX - spkW, spkY - spkH * 0.5f),
                size = Size(spkW, spkH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
            )
            val horn = Path().apply {
                moveTo(spkX, spkY - spkH * 0.16f)
                lineTo(spkX + spkW * 0.9f, spkY - spkH * 0.42f)
                lineTo(spkX + spkW * 0.9f, spkY + spkH * 0.42f)
                lineTo(spkX, spkY + spkH * 0.16f)
                close()
            }
            drawPath(horn, color = Color(0xFF2A364F))
            drawPath(horn, color = CyanNeon.copy(alpha = 0.8f), style = Stroke(width = 1.5f))
            drawCircle(CoralNeon, 5f, Offset(spkX + spkW * 0.3f, spkY))

            // 2. Sound Waves Expanding Toward Glass
            for (i in 1..4) {
                val waveX = spkX + spkW * 0.9f + i * (w * 0.08f)
                val waveH = spkH * (0.3f + i * 0.2f)
                val arcPath = Path().apply {
                    moveTo(waveX, spkY - waveH * 0.5f)
                    quadraticTo(waveX + 8f, spkY, waveX, spkY + waveH * 0.5f)
                }
                val alpha = (1f - i * 0.18f).coerceIn(0.2f, 0.9f)
                drawPath(arcPath, color = if (i % 2 == 0) CoralNeon.copy(alpha = alpha) else AmberVibrant.copy(alpha = alpha), style = Stroke(width = 2f, cap = StrokeCap.Round))
            }

            // 3. Crystal Wine Glass (Foot, Stem, Bowl)
            // Foot
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(cx - footW * 0.5f, footY),
                end = Offset(cx + footW * 0.5f, footY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = CyanNeon,
                start = Offset(cx - footW * 0.5f, footY),
                end = Offset(cx + footW * 0.5f, footY),
                strokeWidth = 1.2f,
                cap = StrokeCap.Round
            )
            // Stem
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(cx, cy + bowlH),
                end = Offset(cx, footY),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            // Bowl
            val bowlPath = Path().apply {
                moveTo(cx - glassR, cy)
                cubicTo(
                    cx - glassR * 1.05f, cy + bowlH * 0.7f,
                    cx - glassR * 0.3f, cy + bowlH,
                    cx, cy + bowlH
                )
                cubicTo(
                    cx + glassR * 0.3f, cy + bowlH,
                    cx + glassR * 1.05f, cy + bowlH * 0.7f,
                    cx + glassR, cy
                )
            }
            drawPath(bowlPath, color = CyanNeon.copy(alpha = 0.12f))
            drawPath(bowlPath, color = Color.White.copy(alpha = 0.6f), style = Stroke(width = 1.8f))

            // 4. Quadrupole Elliptical Deformed Rim
            val rimPath = Path()
            val steps = 60
            val deflection = glassR * 0.28f
            for (i in 0..steps) {
                val theta = (i.toFloat() / steps) * 2f * PI.toFloat()
                val deltaR = deflection * cos(2f * theta)
                val r = glassR + deltaR
                val px = cx + r * cos(theta)
                val py = cy + (r * sin(theta)) * 0.35f
                if (i == 0) rimPath.moveTo(px, py) else rimPath.lineTo(px, py)
            }
            rimPath.close()

            // Glowing rim & fracture stress
            drawPath(rimPath, color = CoralNeon.copy(alpha = 0.35f), style = Stroke(width = 5f))
            drawPath(rimPath, color = Color.White, style = Stroke(width = 2f))
            drawPath(rimPath, color = CoralNeon, style = Stroke(width = 1f))

            // Micro-cracks along maximum tensile stress point
            val crackX = cx + glassR + deflection
            val crackY = cy
            drawLine(CoralNeon, Offset(crackX, crackY - 6f), Offset(crackX - 4f, crackY + 12f), strokeWidth = 1.8f)
            drawLine(Color.White, Offset(crackX - 4f, crackY + 4f), Offset(crackX + 5f, crackY + 16f), strokeWidth = 1.2f)

            // Nodal & Antinodal Markers
            drawCircle(EmeraldNeon, 2.5f, Offset(cx + glassR * 0.707f, cy + (glassR * 0.707f) * 0.35f))
            drawCircle(EmeraldNeon, 2.5f, Offset(cx - glassR * 0.707f, cy + (glassR * 0.707f) * 0.35f))
            drawCircle(CoralNeon, 3f, Offset(cx + (glassR + deflection), cy))
            drawCircle(CoralNeon, 3f, Offset(cx - (glassR + deflection), cy))
        }
        "doppler_mach_cones" -> {
            val cy = h * 0.42f
            val jetX = w * 0.70f
            val mach = 1.45f
            val muRad = asin(1f / mach) // ~43.6 deg

            // 1. Shaded Mach Shock Wave Cone
            val coneLen = w * 0.85f
            val backDx = coneLen * cos(muRad)
            val halfDy = coneLen * sin(muRad)

            val conePath = Path().apply {
                moveTo(jetX, cy)
                lineTo(jetX - backDx, cy - halfDy)
                lineTo(jetX - backDx, cy + halfDy)
                close()
            }
            drawPath(
                path = conePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(AmberVibrant.copy(alpha = 0.22f), CoralNeon.copy(alpha = 0.06f), Color.Transparent),
                    startX = jetX,
                    endX = jetX - backDx
                )
            )

            // 2. Tangent Shock Front Boundary Lines (Glowing Amber)
            drawLine(
                color = AmberVibrant,
                start = Offset(jetX, cy),
                end = Offset(jetX - backDx, cy - halfDy),
                strokeWidth = 2.2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = AmberVibrant,
                start = Offset(jetX, cy),
                end = Offset(jetX - backDx, cy + halfDy),
                strokeWidth = 2.2f,
                cap = StrokeCap.Round
            )

            // 3. Emitted Circular Acoustic Wavefronts Tangent to Cone
            val numWaves = 4
            for (i in 1..numWaves) {
                val pastDist = i * (w * 0.13f)
                val emitX = jetX - pastDist
                val waveRadius = pastDist / mach
                val alpha = (1f - i * 0.20f).coerceIn(0.25f, 0.9f)
                drawCircle(
                    color = CyanNeon.copy(alpha = alpha * 0.75f),
                    radius = waveRadius,
                    center = Offset(emitX, cy),
                    style = Stroke(width = 1.5f)
                )
                // Past emission point markers
                drawCircle(Color.White.copy(alpha = 0.5f), 1.5f, Offset(emitX, cy))
            }

            // 4. Ground Plane & Observer Station
            val groundY = h * 0.82f
            drawLine(
                color = EmeraldNeon.copy(alpha = 0.45f),
                start = Offset(0f, groundY),
                end = Offset(w, groundY),
                strokeWidth = 1.5f
            )
            val obsX = w * 0.32f
            // Observer radar post
            drawLine(ScienceBorder, Offset(obsX, groundY - 14f), Offset(obsX, groundY), 1.8f)
            drawCircle(EmeraldNeon, 4.5f, Offset(obsX, groundY - 14f))
            drawCircle(CoralNeon.copy(alpha = 0.7f), 9f, Offset(obsX, groundY - 14f), style = Stroke(width = 1.2f))

            // 5. Supersonic Jet Vector
            val jetLen = 28f
            val jetSpan = 16f
            // Afterburner Flame
            val flamePath = Path().apply {
                moveTo(jetX - jetLen * 0.45f, cy - 2f)
                lineTo(jetX - jetLen * 0.85f, cy)
                lineTo(jetX - jetLen * 0.45f, cy + 2f)
                close()
            }
            drawPath(flamePath, CoralNeon)

            // Aircraft Fuselage & Delta Wings
            val jetPath = Path().apply {
                moveTo(jetX + jetLen * 0.5f, cy) // Needle nose
                lineTo(jetX + jetLen * 0.1f, cy - 2.5f)
                lineTo(jetX - jetLen * 0.25f, cy - jetSpan * 0.5f) // Wing tip left
                lineTo(jetX - jetLen * 0.2f, cy - 2f)
                lineTo(jetX - jetLen * 0.45f, cy - 2f)
                lineTo(jetX - jetLen * 0.45f, cy + 2f)
                lineTo(jetX - jetLen * 0.2f, cy + 2f)
                lineTo(jetX - jetLen * 0.25f, cy + jetSpan * 0.5f) // Wing tip right
                lineTo(jetX + jetLen * 0.1f, cy + 2.5f)
                close()
            }
            drawPath(jetPath, ScienceDarkSurfaceVariant)
            drawPath(jetPath, AmberVibrant, style = Stroke(width = 1.5f))
            drawCircle(Color.White, 1.2f, Offset(jetX + jetLen * 0.15f, cy))
        }
        "acoustic_beats" -> {
            val cy = h * 0.48f
            val leftX = w * 0.18f
            val rightX = w * 0.94f
            val waveSpan = rightX - leftX

            // 1. Dual Tuning Forks on left
            val forkX = w * 0.10f
            val handleLen = w * 0.05f
            val tineLen = w * 0.07f
            val tineGap = h * 0.12f

            // Fork 1 (Cyan)
            val yFork1 = cy - h * 0.22f
            drawLine(ScienceBorder, Offset(forkX - handleLen, yFork1), Offset(forkX, yFork1), strokeWidth = 2.5f, cap = StrokeCap.Round)
            val forkPath1 = Path().apply {
                moveTo(forkX + tineLen, yFork1 - tineGap * 0.5f)
                lineTo(forkX, yFork1 - tineGap * 0.5f)
                quadraticTo(forkX - 3f, yFork1, forkX, yFork1 + tineGap * 0.5f)
                lineTo(forkX + tineLen, yFork1 + tineGap * 0.5f)
            }
            drawPath(forkPath1, CyanNeon, style = Stroke(width = 2f, cap = StrokeCap.Round))
            drawArc(
                color = CyanNeon.copy(alpha = 0.5f),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(forkX + tineLen - 8f, yFork1 - 10f),
                size = Size(20f, 20f),
                style = Stroke(width = 1.2f)
            )

            // Fork 2 (Coral)
            val yFork2 = cy + h * 0.22f
            drawLine(ScienceBorder, Offset(forkX - handleLen, yFork2), Offset(forkX, yFork2), strokeWidth = 2.5f, cap = StrokeCap.Round)
            val forkPath2 = Path().apply {
                moveTo(forkX + tineLen, yFork2 - tineGap * 0.5f)
                lineTo(forkX, yFork2 - tineGap * 0.5f)
                quadraticTo(forkX - 3f, yFork2, forkX, yFork2 + tineGap * 0.5f)
                lineTo(forkX + tineLen, yFork2 + tineGap * 0.5f)
            }
            drawPath(forkPath2, CoralNeon, style = Stroke(width = 2f, cap = StrokeCap.Round))
            drawArc(
                color = CoralNeon.copy(alpha = 0.5f),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(forkX + tineLen - 8f, yFork2 - 10f),
                size = Size(20f, 20f),
                style = Stroke(width = 1.2f)
            )

            // 2. Center Equilibrium Axis
            drawLine(
                color = ScienceBorder.copy(alpha = 0.45f),
                start = Offset(leftX, cy),
                end = Offset(rightX, cy),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            )

            // 3. Modulated Beat Waveform & Envelope
            val amp = h * 0.28f
            val numPts = 120
            val pathSum = Path()
            val pathEnvUpper = Path()
            val pathEnvLower = Path()

            for (i in 0..numPts) {
                val frac = i / numPts.toFloat()
                val x = leftX + frac * waveSpan
                val env = abs(cos(frac * 2.0f * PI.toFloat()))
                val carrier = sin(frac * 16.0f * PI.toFloat())
                val ySum = cy + env * carrier * amp
                val yEnvUp = cy - env * amp
                val yEnvDown = cy + env * amp

                if (i == 0) {
                    pathSum.moveTo(x, ySum)
                    pathEnvUpper.moveTo(x, yEnvUp)
                    pathEnvLower.moveTo(x, yEnvDown)
                } else {
                    pathSum.lineTo(x, ySum)
                    pathEnvUpper.lineTo(x, yEnvUp)
                    pathEnvLower.lineTo(x, yEnvDown)
                }
            }

            // Draw Dashed Envelope
            val envDash = PathEffect.dashPathEffect(floatArrayOf(5f, 4f))
            drawPath(pathEnvUpper, EmeraldNeon.copy(alpha = 0.75f), style = Stroke(width = 1.4f, pathEffect = envDash))
            drawPath(pathEnvLower, EmeraldNeon.copy(alpha = 0.75f), style = Stroke(width = 1.4f, pathEffect = envDash))

            // Draw Combined Beat Waveform with glow
            drawPath(pathSum, AmberVibrant.copy(alpha = 0.25f), style = Stroke(width = 4f, cap = StrokeCap.Round))
            drawPath(pathSum, AmberVibrant, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

            // 4. Constructive Antinode & Destructive Node Highlights
            drawCircle(AmberVibrant.copy(alpha = 0.35f), 6f, Offset(leftX, cy))
            drawCircle(AmberVibrant, 2.5f, Offset(leftX, cy))
            val midX = leftX + 0.5f * waveSpan
            drawCircle(AmberVibrant.copy(alpha = 0.35f), 6f, Offset(midX, cy))
            drawCircle(AmberVibrant, 2.5f, Offset(midX, cy))
            drawCircle(AmberVibrant.copy(alpha = 0.35f), 6f, Offset(rightX, cy))
            drawCircle(AmberVibrant, 2.5f, Offset(rightX, cy))

            val node1X = leftX + 0.25f * waveSpan
            drawCircle(CoralNeon, 2.5f, Offset(node1X, cy))
            drawCircle(CoralNeon.copy(alpha = 0.3f), 5f, Offset(node1X, cy), style = Stroke(width = 1f))
            val node2X = leftX + 0.75f * waveSpan
            drawCircle(CoralNeon, 2.5f, Offset(node2X, cy))
            drawCircle(CoralNeon.copy(alpha = 0.3f), 5f, Offset(node2X, cy), style = Stroke(width = 1f))
        }
        "tacoma_flutter" -> {
            val cx = w * 0.50f
            val cy = h * 0.52f
            val deckSpan = w * 0.65f
            val halfSpan = deckSpan * 0.5f
            val towerTopY = h * 0.16f
            val tiltDeg = 24f
            val tiltRad = tiltDeg * (PI.toFloat() / 180f)

            // 1. Water surface below
            val waterY = h * 0.84f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(ScienceDarkBg, Color(0xFF0A1C2A)),
                    startY = waterY,
                    endY = h
                ),
                topLeft = Offset(0f, waterY),
                size = Size(w, h - waterY)
            )
            drawLine(WaterDeep.copy(alpha = 0.5f), Offset(0f, waterY), Offset(w, waterY), 1.2f)

            // 2. Wind streamlines blowing across
            val windY1 = cy - 22f
            val windY2 = cy + 18f
            drawLine(CyanNeon.copy(alpha = 0.45f), Offset(w * 0.08f, windY1), Offset(cx - halfSpan * 0.7f, windY1), 1.5f, cap = StrokeCap.Round)
            drawLine(CyanNeon.copy(alpha = 0.35f), Offset(w * 0.05f, windY2), Offset(cx - halfSpan * 0.8f, windY2), 1.5f, cap = StrokeCap.Round)

            // Vortex swirls shedding off trailing edge
            drawArc(
                color = CoralNeon.copy(alpha = 0.7f),
                startAngle = 40f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(cx + halfSpan * 0.75f, cy - 25f),
                size = Size(18f, 18f),
                style = Stroke(width = 1.4f)
            )
            drawArc(
                color = AmberVibrant.copy(alpha = 0.65f),
                startAngle = 180f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(cx + halfSpan * 0.9f, cy + 8f),
                size = Size(16f, 16f),
                style = Stroke(width = 1.4f)
            )

            // 3. Overhead Suspension Towers & Main Cable
            val towerLeftX = cx - halfSpan * 1.05f
            val towerRightX = cx + halfSpan * 1.05f
            drawLine(ScienceBorder.copy(alpha = 0.75f), Offset(towerLeftX, towerTopY), Offset(towerLeftX, waterY), 2.8f, cap = StrokeCap.Round)
            drawLine(ScienceBorder.copy(alpha = 0.75f), Offset(towerRightX, towerTopY), Offset(towerRightX, waterY), 2.8f, cap = StrokeCap.Round)

            // Main Overhead Catenary Cable
            val cablePath = Path().apply {
                moveTo(towerLeftX, towerTopY)
                quadraticTo(cx, towerTopY + 28f, towerRightX, towerTopY)
            }
            drawPath(cablePath, CyanNeon.copy(alpha = 0.85f), style = Stroke(width = 2f, cap = StrokeCap.Round))

            // 4. Dynamic Suspender Hangers
            val deckLeftPt = Offset(cx - halfSpan * cos(tiltRad), cy - halfSpan * sin(tiltRad))
            val deckRightPt = Offset(cx + halfSpan * cos(tiltRad), cy + halfSpan * sin(tiltRad))
            val hangerLeftTop = Offset(cx - halfSpan * 0.82f, towerTopY + 20f)
            val hangerRightTop = Offset(cx + halfSpan * 0.82f, towerTopY + 20f)

            // Left cable (under high tension, glowing Coral/Amber)
            drawLine(CoralNeon, hangerLeftTop, deckLeftPt, strokeWidth = 2.4f, cap = StrokeCap.Round)
            // Right cable (slack, loose curved path)
            val slackPath = Path().apply {
                moveTo(hangerRightTop.x, hangerRightTop.y)
                quadraticTo(hangerRightTop.x + 8f, (hangerRightTop.y + deckRightPt.y) * 0.5f, deckRightPt.x, deckRightPt.y)
            }
            drawPath(slackPath, CyanNeon.copy(alpha = 0.5f), style = Stroke(width = 1.4f, cap = StrokeCap.Round))

            // 5. Tilted Torsional Deck (H-Girder Section)
            rotate(degrees = tiltDeg, pivot = Offset(cx, cy)) {
                val girderH = 16f
                // Road plate
                drawRect(
                    color = ScienceDarkSurfaceVariant,
                    topLeft = Offset(cx - halfSpan, cy - 2f),
                    size = Size(deckSpan, 4f)
                )
                // Center road dash
                drawLine(
                    color = AmberVibrant,
                    start = Offset(cx - halfSpan + 10f, cy),
                    end = Offset(cx + halfSpan - 10f, cy),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                )
                // Left I-Girder vertical flange
                drawRect(CoralNeon, Offset(cx - halfSpan, cy - girderH * 0.5f), Size(4.5f, girderH))
                // Right I-Girder vertical flange
                drawRect(CyanNeon, Offset(cx + halfSpan - 4.5f, cy - girderH * 0.5f), Size(4.5f, girderH))
                // Center pivot pin
                drawCircle(Color.White, 2f, Offset(cx, cy))
            }

            // 6. Torsional Angle Indicator Arc
            drawArc(
                color = AmberVibrant.copy(alpha = 0.85f),
                startAngle = 0f,
                sweepAngle = tiltDeg,
                useCenter = false,
                topLeft = Offset(cx - 25f, cy - 25f),
                size = Size(50f, 50f),
                style = Stroke(width = 1.6f)
            )
        }
        "laser_light_fountain" -> drawLaserFountainIllustration(w, h)
        "thin_film_interference" -> drawThinFilmIllustration(w, h)
        "youngs_double_slit" -> drawYoungsDoubleSlitIllustration(w, h)
        "prism_dispersion" -> drawPrismDispersionIllustration(w, h)
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
        "gravitational_slingshot" -> {
            val px = w * 0.52f
            val py = h * 0.44f
            val planetR = min(w, h) * 0.18f

            // 1. Gravitational Sphere of Influence (SOI)
            val soiR = planetR * 2.3f
            drawCircle(
                color = CyanNeon.copy(alpha = 0.08f),
                radius = soiR,
                center = Offset(px, py)
            )
            drawCircle(
                color = CyanNeon.copy(alpha = 0.35f),
                radius = soiR,
                center = Offset(px, py),
                style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
            )

            // 2. Giant Planet (Jupiter) with Corona & Cloud Bands
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AmberVibrant.copy(alpha = 0.4f), Color(0xFFEF6C00).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(px, py),
                    radius = planetR * 1.7f
                ),
                radius = planetR * 1.7f,
                center = Offset(px, py)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFCC80), Color(0xFFEF6C00), Color(0xFF4E342E)),
                    center = Offset(px - planetR * 0.3f, py - planetR * 0.3f),
                    radius = planetR * 1.3f
                ),
                radius = planetR,
                center = Offset(px, py)
            )
            // Atmospheric cloud bands
            drawLine(
                color = Color(0xFFD7CCC8).copy(alpha = 0.6f),
                start = Offset(px - planetR * 0.85f, py - planetR * 0.28f),
                end = Offset(px + planetR * 0.85f, py - planetR * 0.28f),
                strokeWidth = 2f
            )
            drawLine(
                color = Color(0xFFBF360C).copy(alpha = 0.7f),
                start = Offset(px - planetR * 0.95f, py + planetR * 0.05f),
                end = Offset(px + planetR * 0.95f, py + planetR * 0.05f),
                strokeWidth = 2.5f
            )
            // Planet velocity vector V_p (AmberVibrant)
            val vpLen = 22f
            drawLine(AmberVibrant, Offset(px, py - planetR - 4f), Offset(px + vpLen, py - planetR - 4f), 2f, StrokeCap.Round)
            val vpHead = Path().apply {
                moveTo(px + vpLen + 3f, py - planetR - 4f)
                lineTo(px + vpLen - 2f, py - planetR - 7f)
                lineTo(px + vpLen - 2f, py - planetR - 1f)
                close()
            }
            drawPath(vpHead, AmberVibrant)

            // 3. Slingshot Hyperbolic Trajectory Path (Trailing pass -> massive boost)
            val slingPath = Path().apply {
                moveTo(w * 0.12f, h * 0.82f)
                cubicTo(
                    w * 0.36f, h * 0.80f,
                    px - planetR * 1.1f, py + planetR * 1.2f,
                    px + planetR * 0.3f, py + planetR * 1.05f
                )
                cubicTo(
                    px + planetR * 1.25f, py + planetR * 0.85f,
                    w * 0.72f, h * 0.40f,
                    w * 0.88f, h * 0.18f
                )
            }
            drawPath(slingPath, CoralNeon.copy(alpha = 0.35f), style = Stroke(width = 4.5f, cap = StrokeCap.Round))
            drawPath(
                slingPath,
                brush = Brush.linearGradient(
                    colors = listOf(CyanNeon, AmberVibrant, CoralNeon),
                    start = Offset(w * 0.12f, h * 0.82f),
                    end = Offset(w * 0.88f, h * 0.18f)
                ),
                style = Stroke(width = 2.2f, cap = StrokeCap.Round)
            )

            // 4. Spacecraft Probe & Rocket Flame
            val probeX = w * 0.72f
            val probeY = h * 0.38f
            drawCircle(CoralNeon.copy(alpha = 0.4f), 7f, Offset(probeX, probeY))
            drawCircle(Color.White, 3f, Offset(probeX, probeY))

            // Rocket flame plume
            drawLine(CoralNeon, Offset(probeX - 8f, probeY + 7f), Offset(probeX - 16f, probeY + 14f), 2.5f, StrokeCap.Round)
            drawLine(AmberVibrant, Offset(probeX - 6f, probeY + 5f), Offset(probeX - 12f, probeY + 10f), 1.5f, StrokeCap.Round)

            // Boosted Outward Velocity Vector (CoralNeon)
            val vOutEndX = probeX + 22f
            val vOutEndY = probeY - 19f
            drawLine(CoralNeon, Offset(probeX, probeY), Offset(vOutEndX, vOutEndY), 2.2f, StrokeCap.Round)
            val vOutHead = Path().apply {
                moveTo(vOutEndX + 2f, vOutEndY - 2f)
                lineTo(vOutEndX - 4f, vOutEndY - 1f)
                lineTo(vOutEndX - 1f, vOutEndY + 4f)
                close()
            }
            drawPath(vOutHead, CoralNeon)

            // Centripetal Gravitational Pull F_g toward Jupiter (PurpleNeon)
            drawLine(PurpleNeon, Offset(probeX, probeY), Offset(probeX - 14f, probeY + 2f), 1.6f, StrokeCap.Round)
        }
        "roche_limit" -> {
            val px = w * 0.42f
            val py = h * 0.50f
            val planetR = min(w, h) * 0.21f

            // 1. Roche Limit Boundary (Dashed Coral Warning Radius)
            val rocheR = planetR * 2.15f
            drawCircle(
                color = CoralNeon.copy(alpha = 0.08f),
                radius = rocheR,
                center = Offset(px, py)
            )
            drawCircle(
                color = CoralNeon.copy(alpha = 0.45f),
                radius = rocheR,
                center = Offset(px, py),
                style = Stroke(width = 1.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
            )

            // 2. Back section of Planetary Rings (drawn behind planet)
            val ringInner = planetR * 1.35f
            val ringOuter = planetR * 2.30f
            val ringCenter = Offset(px, py)

            // Ring back arcs (upper half)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(AmberVibrant.copy(alpha = 0.55f), CyanNeon.copy(alpha = 0.35f), Color.Transparent),
                    center = ringCenter,
                    radius = ringOuter
                ),
                topLeft = Offset(px - ringOuter, py - ringOuter * 0.38f),
                size = Size(ringOuter * 2f, ringOuter * 0.76f),
                style = Stroke(width = 6f)
            )

            // 3. Gas Giant Planet with Corona & Atmospheric Cloud Bands
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AmberVibrant.copy(alpha = 0.35f), Color(0xFFEF6C00).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(px, py),
                    radius = planetR * 1.6f
                ),
                radius = planetR * 1.6f,
                center = Offset(px, py)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFE082), Color(0xFFFFA000), Color(0xFFBF360C), Color(0xFF3E2723)),
                    center = Offset(px - planetR * 0.3f, py - planetR * 0.3f),
                    radius = planetR * 1.3f
                ),
                radius = planetR,
                center = Offset(px, py)
            )
            // Atmospheric cloud bands
            drawLine(
                color = Color(0xFFFFECB3).copy(alpha = 0.5f),
                start = Offset(px - planetR * 0.85f, py - planetR * 0.25f),
                end = Offset(px + planetR * 0.85f, py - planetR * 0.25f),
                strokeWidth = 2.2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFBF360C).copy(alpha = 0.6f),
                start = Offset(px - planetR * 0.95f, py + planetR * 0.05f),
                end = Offset(px + planetR * 0.95f, py + planetR * 0.05f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // 4. Front section of Planetary Rings (drawn in front of planet)
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(AmberVibrant.copy(alpha = 0.75f), Color(0xFFFFECB3), CyanNeon.copy(alpha = 0.65f)),
                    start = Offset(px - ringOuter, py),
                    end = Offset(px + ringOuter, py)
                ),
                topLeft = Offset(px - ringOuter, py - ringOuter * 0.38f),
                size = Size(ringOuter * 2f, ringOuter * 0.76f),
                style = Stroke(width = 3.5f)
            )
            drawOval(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(px - ringInner, py - ringInner * 0.38f),
                size = Size(ringInner * 2f, ringInner * 0.76f),
                style = Stroke(width = 1.2f)
            )

            // 5. Tidally Disrupted Moon & Particle Debris Stream
            val moonX = px + planetR * 1.70f
            val moonY = py + planetR * 0.35f
            val moonR = planetR * 0.26f

            // Elongated prolate moon body
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFB0BEC5), Color(0xFF37474F)),
                    center = Offset(moonX - 2f, moonY - 2f),
                    radius = moonR * 1.2f
                ),
                topLeft = Offset(moonX - moonR * 1.3f, moonY - moonR * 0.75f),
                size = Size(moonR * 2.6f, moonR * 1.5f)
            )

            // Differential tidal vectors on moon
            drawLine(CoralNeon, Offset(moonX - moonR * 1.3f, moonY), Offset(moonX - moonR * 2.1f, moonY), 1.8f, StrokeCap.Round)
            drawLine(CoralNeon, Offset(moonX + moonR * 1.3f, moonY), Offset(moonX + moonR * 2.1f, moonY), 1.8f, StrokeCap.Round)

            // Escaping sheared particles forming ring arc
            val debrisList = listOf(
                Offset(moonX - 18f, moonY - 7f) to CyanNeon,
                Offset(moonX - 32f, moonY - 14f) to AmberVibrant,
                Offset(moonX - 48f, moonY - 20f) to Color.White,
                Offset(moonX + 16f, moonY + 8f) to CoralNeon,
                Offset(moonX + 30f, moonY + 14f) to AmberVibrant,
                Offset(moonX + 44f, moonY + 18f) to CyanNeon,
                Offset(moonX - 62f, moonY - 24f) to AmberVibrant
            )
            debrisList.forEach { (pt, col) ->
                drawCircle(color = col, radius = 2.2f, center = pt)
                drawCircle(color = col.copy(alpha = 0.4f), radius = 4.5f, center = pt)
            }
        }
        "lagrange_points" -> {
            val cx = w * 0.46f
            val cy = h * 0.50f
            val scale = min(w, h) * 0.38f

            // 1. Orbital circular path of Secondary around Barycenter
            val rEarth = scale * 0.62f
            drawCircle(
                color = CyanNeon.copy(alpha = 0.22f),
                radius = rEarth,
                center = Offset(cx, cy),
                style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
            )

            // Equipotential contour loop around primary and secondary
            drawCircle(
                color = ScienceBorder.copy(alpha = 0.35f),
                radius = scale * 0.90f,
                center = Offset(cx, cy),
                style = Stroke(width = 0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
            )

            // 2. Equilateral triangle connecting Sun, Earth, L4, L5
            val sunPos = Offset(cx - scale * 0.08f, cy)
            val earthPos = Offset(cx + scale * 0.62f, cy)
            val l4Pos = Offset(cx + scale * 0.27f, cy - scale * 0.54f)
            val l5Pos = Offset(cx + scale * 0.27f, cy + scale * 0.54f)

            val dashBorder = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            drawLine(ScienceBorder.copy(alpha = 0.45f), sunPos, l4Pos, 1f, pathEffect = dashBorder)
            drawLine(ScienceBorder.copy(alpha = 0.45f), earthPos, l4Pos, 1f, pathEffect = dashBorder)
            drawLine(ScienceBorder.copy(alpha = 0.45f), sunPos, l5Pos, 1f, pathEffect = dashBorder)
            drawLine(ScienceBorder.copy(alpha = 0.45f), earthPos, l5Pos, 1f, pathEffect = dashBorder)

            // 3. Primary Body M1 (Sun)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AmberVibrant.copy(alpha = 0.35f), Color(0xFFEF6C00).copy(alpha = 0.1f), Color.Transparent),
                    center = sunPos,
                    radius = 24f
                ),
                radius = 24f,
                center = sunPos
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF9C4), AmberVibrant, Color(0xFFE65100)),
                    center = Offset(sunPos.x - 3f, sunPos.y - 3f),
                    radius = 14f
                ),
                radius = 12f,
                center = sunPos
            )

            // 4. Secondary Body M2 (Earth)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF80D8FF), CyanNeon, BlueLaser),
                    center = Offset(earthPos.x - 2f, earthPos.y - 2f),
                    radius = 8f
                ),
                radius = 6.5f,
                center = earthPos
            )
            // Earth Moon / Hill Sphere
            drawCircle(
                color = CyanNeon.copy(alpha = 0.35f),
                radius = scale * 0.14f,
                center = earthPos,
                style = Stroke(width = 0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f)))
            )

            // 5. The Five Lagrange Points with Halo Glows
            // L1 (Between Sun and Earth)
            val l1Pos = Offset(cx + scale * 0.42f, cy)
            drawCircle(CoralNeon.copy(alpha = 0.25f), 7f, l1Pos)
            drawCircle(CoralNeon, 2.5f, l1Pos)

            // L2 (Beyond Earth) — JWST Halo Orbit
            val l2Pos = Offset(cx + scale * 0.80f, cy)
            drawOval(
                color = AmberVibrant.copy(alpha = 0.65f),
                topLeft = Offset(l2Pos.x - 7f, l2Pos.y - 12f),
                size = Size(14f, 24f),
                style = Stroke(width = 1.2f)
            )
            drawCircle(AmberVibrant.copy(alpha = 0.4f), 8f, l2Pos)
            drawCircle(AmberVibrant, 3f, l2Pos)
            // Tiny JWST satellite dot
            drawCircle(Color.White, 2f, Offset(l2Pos.x + 5f, l2Pos.y - 8f))

            // L3 (Counter-Sun)
            val l3Pos = Offset(cx - scale * 0.72f, cy)
            drawCircle(PurpleNeon.copy(alpha = 0.25f), 6f, l3Pos)
            drawCircle(PurpleNeon, 2.5f, l3Pos)

            // L4 (Leading Trojan Camp +60°)
            drawCircle(CyanNeon.copy(alpha = 0.35f), 9f, l4Pos)
            drawCircle(CyanNeon, 3.5f, l4Pos)
            // Trojan asteroid swarm around L4
            val trojansL4 = listOf(
                Offset(l4Pos.x - 6f, l4Pos.y - 4f),
                Offset(l4Pos.x + 8f, l4Pos.y - 2f),
                Offset(l4Pos.x - 3f, l4Pos.y + 6f),
                Offset(l4Pos.x + 5f, l4Pos.y + 5f)
            )
            trojansL4.forEach { drawCircle(CyanNeon.copy(alpha = 0.75f), 1.4f, it) }

            // L5 (Trailing Trojan Camp -60°)
            drawCircle(EmeraldNeon.copy(alpha = 0.35f), 9f, l5Pos)
            drawCircle(EmeraldNeon, 3.5f, l5Pos)
            val trojansL5 = listOf(
                Offset(l5Pos.x - 5f, l5Pos.y + 5f),
                Offset(l5Pos.x + 7f, l5Pos.y + 3f),
                Offset(l5Pos.x - 2f, l5Pos.y - 6f)
            )
            trojansL5.forEach { drawCircle(EmeraldNeon.copy(alpha = 0.75f), 1.4f, it) }
        }
        "three_body_problem" -> {
            val cx = w * 0.50f
            val cy = h * 0.50f
            val scaleX = w * 0.38f
            val scaleY = h * 0.28f

            // 1. Draw glowing Figure-8 lemniscate trajectory
            val path = Path()
            val steps = 80
            for (i in 0..steps) {
                val t = i.toFloat() / steps * 2f * PI.toFloat()
                val px = cx + sin(t) * scaleX
                val py = cy + sin(t) * cos(t) * scaleY * 1.55f
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()

            // Outer soft glow of trail
            drawPath(
                path = path,
                color = CyanNeon.copy(alpha = 0.18f),
                style = Stroke(width = 5.5f, cap = StrokeCap.Round)
            )
            // Core luminous trail
            drawPath(
                path = path,
                color = CyanNeon.copy(alpha = 0.65f),
                style = Stroke(width = 1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
            )

            // 2. Three Stars along the Figure-8 Choreography
            val phases = listOf(
                Triple(0.40f * PI.toFloat(), CyanNeon, "Alpha"),
                Triple(1.07f * PI.toFloat(), CoralNeon, "Beta"),
                Triple(1.74f * PI.toFloat(), EmeraldNeon, "Gamma")
            )

            val starPositions = phases.map { (t, _, _) ->
                val px = cx + sin(t) * scaleX
                val py = cy + sin(t) * cos(t) * scaleY * 1.55f
                Offset(px, py)
            }

            // 3. Faint mutual gravitational tension lines
            val dashTension = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))
            for (i in 0..2) {
                val next = (i + 1) % 3
                drawLine(
                    color = ScienceBorder.copy(alpha = 0.40f),
                    start = starPositions[i],
                    end = starPositions[next],
                    strokeWidth = 1f,
                    pathEffect = dashTension
                )
            }

            // Center of Mass crosshair
            val crosshair = 8f
            drawLine(ScienceBorder.copy(alpha = 0.5f), Offset(cx - crosshair, cy), Offset(cx + crosshair, cy), 1f)
            drawLine(ScienceBorder.copy(alpha = 0.5f), Offset(cx, cy - crosshair), Offset(cx, cy + crosshair), 1f)

            // 4. Render Stars with Radial Glow Coronas & Velocity Vectors
            phases.forEachIndexed { idx, (t, color, _) ->
                val pos = starPositions[idx]
                val r = 7f

                // Outer corona glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.45f), color.copy(alpha = 0.10f), Color.Transparent),
                        center = pos,
                        radius = r * 2.8f
                    ),
                    radius = r * 2.8f,
                    center = pos
                )

                // Luminous star core
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, color, color.copy(alpha = 0.7f)),
                        center = Offset(pos.x - r * 0.25f, pos.y - r * 0.25f),
                        radius = r
                    ),
                    radius = r,
                    center = pos
                )

                // Instantaneous Tangent Velocity Vector
                val vx = cos(t) * scaleX
                val vy = (cos(t) * cos(t) - sin(t) * sin(t)) * scaleY * 1.55f
                val vLen = sqrt(vx * vx + vy * vy).coerceAtLeast(1e-4f)
                val vNorm = Offset(vx / vLen, vy / vLen) * 16f

                drawLine(
                    color = color.copy(alpha = 0.85f),
                    start = pos,
                    end = pos + vNorm,
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
                drawCircle(Color.White, 1.8f, pos + vNorm)
            }
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

private fun DrawScope.drawLaserFountainIllustration(w: Float, h: Float) {
    // 1. Water Tank on Upper Left
    val tankLeft = w * 0.08f
    val tankTop = h * 0.16f
    val tankWidth = w * 0.16f
    val tankHeight = h * 0.45f
    drawRoundRect(
        color = ScienceDarkSurfaceVariant.copy(alpha = 0.85f),
        topLeft = Offset(tankLeft, tankTop),
        size = Size(tankWidth, tankHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )
    drawRoundRect(
        color = CyanNeon.copy(alpha = 0.4f),
        topLeft = Offset(tankLeft, tankTop),
        size = Size(tankWidth, tankHeight),
        style = Stroke(width = 1.5f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )
    // Liquid volume inside reservoir
    drawRect(
        color = CyanNeon.copy(alpha = 0.22f),
        topLeft = Offset(tankLeft + 2f, tankTop + tankHeight * 0.25f),
        size = Size(tankWidth - 4f, tankHeight * 0.75f - 2f)
    )

    // 2. Brass/Steel Horizontal Nozzle
    val nozzleX = tankLeft + tankWidth
    val nozzleY = tankTop + tankHeight * 0.65f
    val nozzleH = 12f
    drawRect(
        color = Color(0xFF455A64),
        topLeft = Offset(nozzleX, nozzleY - nozzleH * 0.5f),
        size = Size(10f, nozzleH)
    )
    drawRect(
        color = AmberVibrant.copy(alpha = 0.8f),
        topLeft = Offset(nozzleX, nozzleY - nozzleH * 0.5f),
        size = Size(10f, nozzleH),
        style = Stroke(width = 1f)
    )

    // 3. Arcing Parabolic Water Stream
    val startJetX = nozzleX + 10f
    val streamPath = Path().apply {
        moveTo(startJetX, nozzleY - 5f)
        cubicTo(
            startJetX + w * 0.25f, nozzleY - 3f,
            startJetX + w * 0.52f, nozzleY + h * 0.22f,
            startJetX + w * 0.68f, h * 0.78f
        )
        lineTo(startJetX + w * 0.68f + 8f, h * 0.80f)
        cubicTo(
            startJetX + w * 0.52f + 8f, nozzleY + h * 0.25f,
            startJetX + w * 0.25f, nozzleY + 7f,
            startJetX, nozzleY + 5f
        )
        close()
    }
    drawPath(streamPath, CyanNeon.copy(alpha = 0.22f))
    drawPath(streamPath, CyanNeon.copy(alpha = 0.65f), style = Stroke(width = 1.5f))

    // 4. Laser Emitter Diode (Left of Tank)
    drawLine(
        color = Color(0xFFB0BEC5),
        start = Offset(tankLeft - 14f, nozzleY),
        end = Offset(tankLeft - 2f, nozzleY),
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )
    drawCircle(CoralNeon, 3.5f, Offset(tankLeft - 14f, nozzleY))

    // 5. Total Internal Reflection Laser Path (Bouncing Inside Stream)
    val p0 = Offset(startJetX - 6f, nozzleY)
    val p1 = Offset(startJetX + w * 0.16f, nozzleY + 2f)
    val p2 = Offset(startJetX + w * 0.32f, nozzleY + h * 0.08f)
    val p3 = Offset(startJetX + w * 0.46f, nozzleY + h * 0.25f)
    val p4 = Offset(startJetX + w * 0.58f, nozzleY + h * 0.45f)
    val p5 = Offset(startJetX + w * 0.68f, h * 0.78f)

    val laserBounces = listOf(p0, p1, p2, p3, p4, p5)
    for (i in 0 until laserBounces.size - 1) {
        drawLine(
            color = CoralNeon.copy(alpha = 0.35f),
            start = laserBounces[i],
            end = laserBounces[i + 1],
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = CoralNeon,
            start = laserBounces[i],
            end = laserBounces[i + 1],
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )
    }

    // TIR Reflection Sparks & Normal Ticks
    listOf(p1, p2, p3, p4).forEach { pt ->
        drawCircle(Color.White, 2f, pt)
        drawCircle(CoralNeon.copy(alpha = 0.6f), 4.5f, pt)
    }

    // 6. Catch Basin at Bottom Right
    val basinX = startJetX + w * 0.62f
    val basinY = h * 0.77f
    drawRoundRect(
        color = ScienceDarkSurfaceVariant,
        topLeft = Offset(basinX, basinY),
        size = Size(w * 0.18f, h * 0.14f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )
    drawRoundRect(
        color = ScienceBorder.copy(alpha = 0.5f),
        topLeft = Offset(basinX, basinY),
        size = Size(w * 0.18f, h * 0.14f),
        style = Stroke(width = 1.2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )
    drawCircle(CoralNeon.copy(alpha = 0.6f), 6f, Offset(basinX + w * 0.08f, basinY + 6f))
    drawCircle(Color.White.copy(alpha = 0.85f), 2.5f, Offset(basinX + w * 0.08f, basinY + 6f))
}

private fun DrawScope.drawThinFilmIllustration(w: Float, h: Float) {
    // 1. Wire Frame Loop (Rectangular Oval)
    val loopW = w * 0.46f
    val loopH = h * 0.68f
    val loopLeft = w * 0.16f
    val loopTop = h * 0.12f

    // Frame handle
    drawLine(
        color = Color(0xFF78909C),
        start = Offset(loopLeft + loopW * 0.5f, loopTop + loopH),
        end = Offset(loopLeft + loopW * 0.5f, loopTop + loopH + h * 0.16f),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = AmberVibrant.copy(alpha = 0.7f),
        start = Offset(loopLeft + loopW * 0.5f, loopTop + loopH),
        end = Offset(loopLeft + loopW * 0.5f, loopTop + loopH + h * 0.16f),
        strokeWidth = 1.2f,
        cap = StrokeCap.Round
    )

    // Film interior clip path
    val filmRect = Size(loopW, loopH)
    val filmTopLeft = Offset(loopLeft, loopTop)

    // 2. Gravitational Thinning Iridescent Color Bands
    val bands = listOf(
        Color(0xFF11141A) to 0.10f, // Top: Newton's Black Film (destructive for all λ)
        Color(0xFFE2E8F0) to 0.08f, // Silver-White fringe
        AmberVibrant to 0.12f,      // First-order Gold / Yellow
        PurpleNeon to 0.12f,        // Magenta / Violet band
        CyanNeon to 0.14f,          // Cyan / Sky Blue fringe
        EmeraldNeon to 0.14f,       // Emerald Green fringe
        CoralNeon to 0.15f,         // Coral Red band
        CyanNeon.copy(alpha = 0.75f) to 0.15f
    )

    var currY = loopTop + 2f
    for ((bandColor, relHeight) in bands) {
        val bHeight = loopH * relHeight
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(bandColor.copy(alpha = 0.85f), bandColor.copy(alpha = 0.60f)),
                startY = currY,
                endY = currY + bHeight
            ),
            topLeft = Offset(loopLeft + 2f, currY),
            size = Size(loopW - 4f, bHeight)
        )
        currY += bHeight
    }

    // Shimmering surface sheen
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent, Color.White.copy(alpha = 0.15f)),
            startY = loopTop,
            endY = loopTop + loopH
        ),
        topLeft = filmTopLeft,
        size = filmRect,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
    )

    // Wire Frame Rim
    drawRoundRect(
        color = ScienceDarkSurfaceVariant,
        topLeft = filmTopLeft,
        size = filmRect,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f),
        style = Stroke(width = 3.5f)
    )
    drawRoundRect(
        color = CyanNeon.copy(alpha = 0.75f),
        topLeft = filmTopLeft,
        size = filmRect,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f),
        style = Stroke(width = 1.4f)
    )

    // 3. Ray Tracing Inset & Reflection Geometry on Right
    val rayStartX = w * 0.94f
    val rayStartY = h * 0.14f
    val hitX = loopLeft + loopW * 0.70f
    val hitY = loopTop + loopH * 0.38f

    drawLine(
        color = Color.White.copy(alpha = 0.9f),
        start = Offset(rayStartX, rayStartY),
        end = Offset(hitX, hitY),
        strokeWidth = 2.0f,
        cap = StrokeCap.Round
    )

    val r1End = Offset(w * 0.96f, hitY - h * 0.15f)
    val r2End = Offset(w * 0.94f, hitY - h * 0.05f)

    drawLine(
        color = CoralNeon.copy(alpha = 0.85f),
        start = Offset(hitX, hitY),
        end = r1End,
        strokeWidth = 1.8f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = EmeraldNeon.copy(alpha = 0.85f),
        start = Offset(hitX + 5f, hitY + 3f),
        end = r2End,
        strokeWidth = 1.8f,
        cap = StrokeCap.Round
    )

    drawCircle(Color.White, 2.5f, Offset(hitX, hitY))
    drawCircle(CyanNeon.copy(alpha = 0.6f), 5.5f, Offset(hitX, hitY))
    drawCircle(AmberVibrant, 2f, r1End)
    drawCircle(CyanNeon, 2f, r2End)
}

private fun DrawScope.drawYoungsDoubleSlitIllustration(w: Float, h: Float) {
    val slitBarrierX = w * 0.28f
    val screenX = w * 0.88f
    val slit1Y = h * 0.40f
    val slit2Y = h * 0.60f
    val barrierTop = h * 0.15f
    val barrierBottom = h * 0.85f

    // 1. Incoming Coherent Laser Waves (Planar wavefronts from left)
    val laserColor = EmeraldNeon // 532nm Green Laser
    for (i in 0..3) {
        val waveX = w * 0.06f + i * (slitBarrierX - w * 0.08f) / 3f
        drawLine(
            color = laserColor.copy(alpha = 0.45f),
            start = Offset(waveX, barrierTop + 4f),
            end = Offset(waveX, barrierBottom - 4f),
            strokeWidth = 1.5f
        )
    }

    // Laser source beam line
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(laserColor.copy(alpha = 0.2f), laserColor.copy(alpha = 0.85f)),
            startX = 0f,
            endX = slitBarrierX
        ),
        start = Offset(0f, h * 0.5f),
        end = Offset(slitBarrierX, h * 0.5f),
        strokeWidth = 3f
    )

    // 2. Slit Barrier Wall (Dark metal barrier with two apertures)
    val wallColor = ScienceDarkSurfaceVariant
    val wallBorder = Color(0xFF78909C)
    drawRect(wallColor, Offset(slitBarrierX - 3f, barrierTop), Size(6f, slit1Y - barrierTop - 3f))
    drawRect(wallBorder, Offset(slitBarrierX - 1f, barrierTop), Size(2f, slit1Y - barrierTop - 3f))
    drawRect(wallColor, Offset(slitBarrierX - 3f, slit1Y + 3f), Size(6f, slit2Y - slit1Y - 6f))
    drawRect(wallBorder, Offset(slitBarrierX - 1f, slit1Y + 3f), Size(2f, slit2Y - slit1Y - 6f))
    drawRect(wallColor, Offset(slitBarrierX - 3f, slit2Y + 3f), Size(6f, barrierBottom - slit2Y - 3f))
    drawRect(wallBorder, Offset(slitBarrierX - 1f, slit2Y + 3f), Size(2f, barrierBottom - slit2Y - 3f))

    // Glowing Slit Apertures
    drawCircle(laserColor, 2.5f, Offset(slitBarrierX, slit1Y))
    drawCircle(Color.White, 1.2f, Offset(slitBarrierX, slit1Y))
    drawCircle(laserColor, 2.5f, Offset(slitBarrierX, slit2Y))
    drawCircle(Color.White, 1.2f, Offset(slitBarrierX, slit2Y))

    // 3. Expanding Huygens Circular Wavefronts from Twin Slits
    val maxRadius = screenX - slitBarrierX
    for (ring in 1..4) {
        val r = ring * (maxRadius / 4.5f)
        val alpha = (1f - (r / maxRadius)).coerceIn(0.2f, 0.75f)
        drawArc(
            color = laserColor.copy(alpha = alpha),
            startAngle = -75f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(slitBarrierX - r, slit1Y - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = 1.4f)
        )
        drawArc(
            color = CyanNeon.copy(alpha = alpha),
            startAngle = -75f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(slitBarrierX - r, slit2Y - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = 1.4f)
        )
    }

    // 4. Detection Screen on the Right (Phosphor plate)
    drawRect(
        color = ScienceDarkSurface,
        topLeft = Offset(screenX, barrierTop),
        size = Size(w * 0.08f, barrierBottom - barrierTop)
    )
    drawRect(
        color = CyanGlow,
        topLeft = Offset(screenX, barrierTop),
        size = Size(w * 0.08f, barrierBottom - barrierTop),
        style = Stroke(width = 1f)
    )

    // 5. Interference Fringes on the Screen (Central maximum + side orders)
    val centerY = h * 0.5f
    val fringeOrders = listOf(
        0 to 1.0f,
        -1 to 0.75f, 1 to 0.75f,
        -2 to 0.45f, 2 to 0.45f,
        -3 to 0.20f, 3 to 0.20f
    )
    val fringeSpacing = (slit2Y - slit1Y) * 0.55f

    for ((m, intensity) in fringeOrders) {
        val fY = centerY + m * fringeSpacing
        if (fY in (barrierTop + 2f)..(barrierBottom - 2f)) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        laserColor.copy(alpha = intensity * 0.9f),
                        Color.White.copy(alpha = intensity * 0.95f),
                        laserColor.copy(alpha = intensity * 0.9f),
                        Color.Transparent
                    ),
                    startX = screenX,
                    endX = screenX + w * 0.08f
                ),
                topLeft = Offset(screenX, fY - 3f),
                size = Size(w * 0.08f, 6f)
            )
            if (abs(m) <= 1) {
                drawLine(
                    color = laserColor.copy(alpha = 0.22f * intensity),
                    start = Offset(slitBarrierX, centerY),
                    end = Offset(screenX, fY),
                    strokeWidth = 1f
                )
            }
        }
    }
}

private fun DrawScope.drawPrismDispersionIllustration(w: Float, h: Float) {
    val cx = w * 0.44f
    val cy = h * 0.48f
    val prismSide = min(w * 0.38f, h * 0.65f)
    val prismH = prismSide * 0.866f // cos(30 deg)
    val halfBase = prismSide * 0.5f

    // 1. Equilateral Prism Vertices
    val v0 = Offset(cx, cy - prismH * 0.55f) // Apex
    val v1 = Offset(cx - halfBase, cy + prismH * 0.45f) // Left Base
    val v2 = Offset(cx + halfBase, cy + prismH * 0.45f) // Right Base

    val screenX = w * 0.88f
    val screenTop = h * 0.15f
    val screenBottom = h * 0.85f
    val screenWidth = w * 0.06f

    // 2. Incident White Light Beam (Left Source -> Face 1)
    val entryPt = Offset(v1.x + (v0.x - v1.x) * 0.48f, v1.y + (v0.y - v1.y) * 0.48f)
    val sourcePt = Offset(w * 0.06f, entryPt.y + h * 0.16f)

    // Collimated White Beam
    drawLine(
        color = Color.White.copy(alpha = 0.22f),
        start = sourcePt,
        end = entryPt,
        strokeWidth = 6f
    )
    drawLine(
        color = Color.White.copy(alpha = 0.95f),
        start = sourcePt,
        end = entryPt,
        strokeWidth = 2.4f,
        cap = StrokeCap.Round
    )

    // Emitter housing on left
    drawCircle(ScienceDarkSurfaceVariant, 5f, sourcePt)
    drawCircle(Color.White, 2.5f, sourcePt)

    // 3. Glass Prism Body with Glass Highlights
    val prismPath = Path().apply {
        moveTo(v0.x, v0.y)
        lineTo(v2.x, v2.y)
        lineTo(v1.x, v1.y)
        close()
    }

    drawPath(
        path = prismPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0x2200E5FF),
                Color(0x0C141C2E),
                Color(0x2E00E5FF)
            ),
            start = v0,
            end = Offset(cx, cy + prismH * 0.5f)
        )
    )
    drawPath(
        path = prismPath,
        color = ScienceBorder.copy(alpha = 0.75f),
        style = Stroke(width = 1.8f)
    )
    drawLine(
        color = GlassHighlight.copy(alpha = 0.45f),
        start = v1,
        end = v0,
        strokeWidth = 2.2f
    )
    drawLine(
        color = GlassHighlight.copy(alpha = 0.35f),
        start = v0,
        end = v2,
        strokeWidth = 2.2f
    )

    // 4. Seven Spectral Colors: Red to Violet
    val spectralRays = listOf(
        Color(0xFFFF3333) to 0.46f, // Red (bends least)
        Color(0xFFFF8A00) to 0.50f, // Orange
        Color(0xFFFFD600) to 0.54f, // Yellow
        Color(0xFF00E676) to 0.58f, // Green
        Color(0xFF00E5FF) to 0.62f, // Cyan
        Color(0xFF2979FF) to 0.67f, // Blue
        Color(0xFFB388FF) to 0.72f  // Violet (bends most)
    )

    val f2Vec = v2 - v0
    val firstScreenY = screenTop + (screenBottom - screenTop) * 0.26f
    val lastScreenY = screenTop + (screenBottom - screenTop) * 0.76f

    // Internal rays & Emergent rainbow fan
    spectralRays.forEachIndexed { idx, (color, face2Frac) ->
        val t = idx / (spectralRays.size - 1).toFloat()
        val hitFace2 = Offset(v0.x + f2Vec.x * face2Frac, v0.y + f2Vec.y * face2Frac)
        val hitScreen = Offset(screenX, firstScreenY + (lastScreenY - firstScreenY) * t)

        // Internal ray inside glass
        drawLine(
            color = color.copy(alpha = 0.85f),
            start = entryPt,
            end = hitFace2,
            strokeWidth = 1.6f,
            cap = StrokeCap.Round
        )

        // Emergent ray from Face 2 to screen
        drawLine(
            color = color.copy(alpha = 0.90f),
            start = hitFace2,
            end = hitScreen,
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )
    }

    // 5. Detection Phosphor Screen on Right
    drawRect(
        color = ScienceDarkSurface,
        topLeft = Offset(screenX, screenTop),
        size = Size(screenWidth, screenBottom - screenTop)
    )
    drawRect(
        color = ScienceBorder.copy(alpha = 0.6f),
        topLeft = Offset(screenX, screenTop),
        size = Size(screenWidth, screenBottom - screenTop),
        style = Stroke(width = 1f)
    )

    // Continuous Spectral Gradient on Screen
    val rainbowStops = listOf(
        0.0f to Color(0xFFFF2A2A),
        0.18f to Color(0xFFFF9100),
        0.36f to Color(0xFFFFEA00),
        0.54f to Color(0xFF00E676),
        0.72f to Color(0xFF00E5FF),
        0.88f to Color(0xFF2979FF),
        1.0f to Color(0xFFB388FF)
    )
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = rainbowStops.toTypedArray(),
            startY = firstScreenY - 4f,
            endY = lastScreenY + 4f
        ),
        topLeft = Offset(screenX + 1.5f, firstScreenY - 4f),
        size = Size(screenWidth - 3f, (lastScreenY - firstScreenY) + 8f)
    )
}

