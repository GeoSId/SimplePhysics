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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.model.PhysicsExperiment
import com.geosid.simplephysics.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import simplephysics.shared.generated.resources.*

@Composable
fun ExperimentListItem(
    experiment: PhysicsExperiment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked = experiment.isReleased
    val badgeColor = if (isUnlocked) CyanNeon else AmberVibrant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, ScienceBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = ScienceDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Thumbnail Canvas
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ScienceDarkSurfaceVariant)
                    .border(1.dp, ScienceBorder, RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawExperimentIllustration(experiment.id)
                }

                // Small pill badge on thumbnail
                Surface(
                    color = ScienceDarkBg.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
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
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Right Info Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = experiment.category.title.uppercase(),
                        color = PurpleNeon,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    if (isUnlocked) {
                        Text(
                            text = stringResource(Res.string.action_explore),
                            color = CyanNeon,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "🔒 COMING SOON",
                            color = AmberVibrant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))
                Text(
                    text = experiment.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = experiment.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AmberVibrant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    text = experiment.teaser,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
