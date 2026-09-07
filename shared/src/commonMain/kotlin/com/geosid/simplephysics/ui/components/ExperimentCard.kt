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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
            // Bottle & Cartesian diver
            val bw = w * 0.28f
            val bh = h * 0.75f
            val bx = (w - bw) / 2f
            val by = (h - bh) / 2f

            // Water
            drawRoundRect(
                color = WaterBlue.copy(alpha = 0.5f),
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f)
            )
            drawRoundRect(
                color = Color(0x66FFFFFF),
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f),
                style = Stroke(width = 2.5f)
            )

            // Mini diver inside
            val dx = w * 0.5f
            val dy = by + bh * 0.4f
            drawCircle(Color(0xFFD32F2F), 6f, Offset(dx, dy - 14f))
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(dx - 5f, dy - 8f),
                size = Size(10f, 22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
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
