package com.geosid.simplephysics.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.model.PhysicsExperiment
import com.geosid.simplephysics.ui.components.BackArrowIcon
import com.geosid.simplephysics.ui.components.InfoLightbulbIcon
import com.geosid.simplephysics.ui.components.ScienceExplanationDialog
import com.geosid.simplephysics.ui.theme.*
import androidx.compose.ui.text.style.TextOverflow
import com.geosid.simplephysics.ui.expirementsRegistry.ExperimentScreenRegistry
import org.jetbrains.compose.resources.stringResource
import simplephysics.shared.generated.resources.*

@Composable
fun ExperimentDetailScreen(
    experiment: PhysicsExperiment,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExplanationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScienceDarkBg)
    ) {
        // Top App Bar with Safe Status Bar Insets
        Surface(
            color = ScienceDarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .background(ScienceDarkSurfaceVariant, RoundedCornerShape(10.dp))
                    ) {
                        BackArrowIcon(tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }

                    Column(modifier = Modifier.padding(end = 6.dp)) {
                        Text(
                            text = experiment.title,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = experiment.subtitle,
                            color = AmberVibrant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // "The Science" Info Button
                Button(
                    onClick = { showExplanationDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanGlow,
                        contentColor = CyanNeon
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        InfoLightbulbIcon(tint = CyanNeon, modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(Res.string.action_science),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Full Screen Experiment Canvas Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val screenComposable = ExperimentScreenRegistry.getScreen(experiment.id)
            if (screenComposable != null) {
                screenComposable()
            } else {
                ComingSoonExperimentView(
                    experiment = experiment,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Modal Sheet / Dialog explaining the physics
    if (showExplanationDialog) {
        ScienceExplanationDialog(
            experiment = experiment,
            onDismiss = { showExplanationDialog = false }
        )
    }
}

@Composable
private fun ComingSoonExperimentView(
    experiment: PhysicsExperiment,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(ScienceDarkBg)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = ScienceDarkSurfaceVariant.copy(alpha = 0.95f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Lock Icon & Day Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = AmberVibrant.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberVibrant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = stringResource(Res.string.badge_day_locked, experiment.day),
                            color = AmberVibrant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = CyanGlow,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = experiment.category.shortTitle,
                            color = CyanNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(Res.string.coming_soon_title),
                    color = AmberVibrant,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = experiment.title,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    text = experiment.teaser,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )

                // Science Theory Sneak Peek Card
                Surface(
                    color = ScienceDarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.scientific_principle),
                            color = CyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${experiment.formulaTitle}: ${experiment.formula}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = stringResource(Res.string.coming_soon_in_development),
                    color = TextSecondary.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
