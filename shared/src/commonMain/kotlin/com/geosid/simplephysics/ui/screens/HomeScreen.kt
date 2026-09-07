package com.geosid.simplephysics.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.localization.LocalizationManager
import com.geosid.simplephysics.model.ExperimentCategory
import com.geosid.simplephysics.model.ExperimentRegistry
import com.geosid.simplephysics.model.PhysicsExperiment
import com.geosid.simplephysics.ui.components.ExperimentCard
import com.geosid.simplephysics.ui.components.ExperimentListItem
import com.geosid.simplephysics.ui.components.LanguageSelectionDialog
import com.geosid.simplephysics.ui.components.ScienceFlaskIcon
import com.geosid.simplephysics.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import simplephysics.shared.generated.resources.*

@Composable
fun HomeScreen(
    onSelectExperiment: (PhysicsExperiment) -> Unit,
    onOpenWhiteboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(ExperimentCategory.ALL) }
    var comingSoonExperiment by remember { mutableStateOf<PhysicsExperiment?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // All categories displayed with released count
    val allCategoriesWithCounts = remember {
        ExperimentCategory.values().map { cat ->
            val unlockedCount = if (cat == ExperimentCategory.ALL) {
                ExperimentRegistry.experiments.count { it.isReleased }
            } else {
                ExperimentRegistry.experiments.count { it.category == cat && it.isReleased }
            }
            cat to unlockedCount
        }
    }

    val filteredExperiments = remember(selectedCategory) {
        if (selectedCategory == ExperimentCategory.ALL) {
            ExperimentRegistry.experiments
        } else {
            // Category filters show released experiments; if none released, list is empty to trigger "Coming Soon"
            ExperimentRegistry.experiments.filter { it.category == selectedCategory && it.isReleased }
        }
    }

    val onExperimentClick: (PhysicsExperiment) -> Unit = { exp ->
        if (exp.isReleased) {
            onSelectExperiment(exp)
        } else {
            comingSoonExperiment = exp
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ScienceDarkBg)
    ) {
        val isMobile = maxWidth < 600.dp
        var isListView by remember(isMobile) { mutableStateOf(isMobile) }

        Column(modifier = Modifier.fillMaxSize()) {
            // Responsive App Header with Safe Status Bar Insets
            Surface(
                color = ScienceDarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = if (isMobile) 16.dp else 24.dp, vertical = if (isMobile) 14.dp else 18.dp)
                ) {
                    if (isMobile) {
                        // Mobile Header: Compact Row + Action Button below
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyanGlow),
                                contentAlignment = Alignment.Center
                            ) {
                                ScienceFlaskIcon(
                                    modifier = Modifier.size(22.dp),
                                    tint = CyanNeon
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.app_title),
                                    color = CyanNeon,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = stringResource(Res.string.app_subtitle_mobile),
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Language Switcher Button (Mobile pill)
                            Surface(
                                onClick = { showLanguageDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                color = ScienceDarkSurfaceVariant,
                                border = BorderStroke(1.dp, ScienceBorder),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(LocalizationManager.currentLanguage.flag, fontSize = 16.sp)
                                    Text(
                                        text = LocalizationManager.currentLanguage.code.uppercase(),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onOpenWhiteboard,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberVibrant,
                                contentColor = ScienceDarkBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text(stringResource(Res.string.whiteboard_button_title), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        // Wide Screen Header: Side-by-Side Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CyanGlow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ScienceFlaskIcon(
                                        modifier = Modifier.size(26.dp),
                                        tint = CyanNeon
                                    )
                                }

                                Column {
                                    Text(
                                        text = stringResource(Res.string.app_title),
                                        color = CyanNeon,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                    Text(
                                        text = stringResource(Res.string.app_subtitle_wide),
                                        color = TextPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showLanguageDialog = true },
                                    border = BorderStroke(1.dp, ScienceBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = ScienceDarkSurfaceVariant,
                                        contentColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(LocalizationManager.currentLanguage.flag, fontSize = 16.sp)
                                        Text(
                                            text = LocalizationManager.currentLanguage.nativeName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Button(
                                    onClick = onOpenWhiteboard,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberVibrant,
                                        contentColor = ScienceDarkBg
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(stringResource(Res.string.whiteboard_button_title), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(Res.string.app_description),
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Category Bar with View Mode Toggle (List ☰ vs Grid ⊞)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isMobile) 12.dp else 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Filter Horizontal Row
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allCategoriesWithCounts) { (category, count) ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = if (count > 0) "${category.title} ($count)" else category.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon,
                                selectedLabelColor = ScienceDarkBg,
                                containerColor = ScienceDarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // View Mode Toggle (List vs Grid)
                Surface(
                    color = ScienceDarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        IconButton(
                            onClick = { isListView = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isListView) CyanNeon else androidx.compose.ui.graphics.Color.Transparent)
                        ) {
                            Text(
                                text = "☰",
                                color = if (isListView) ScienceDarkBg else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        IconButton(
                            onClick = { isListView = false },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isListView) CyanNeon else androidx.compose.ui.graphics.Color.Transparent)
                        ) {
                            Text(
                                text = "⊞",
                                color = if (!isListView) ScienceDarkBg else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Experiments Responsive Content Area (List or Grid)
            if (filteredExperiments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = AmberVibrant.copy(alpha = 0.12f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, AmberVibrant.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "🔒",
                                fontSize = 32.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Text(
                            text = stringResource(Res.string.coming_soon_title),
                            color = AmberVibrant,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.coming_soon_empty_filter),
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else if (isListView) {
                // Scrollable List Area (Perfect for Mobile!)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isMobile) 12.dp else 20.dp),
                    contentPadding = PaddingValues(
                        top = 4.dp,
                        bottom = 28.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredExperiments) { exp ->
                        ExperimentListItem(
                            experiment = exp,
                            onClick = { onExperimentClick(exp) }
                        )
                    }
                }
            } else {
                // Responsive Grid Area
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isMobile) 12.dp else 20.dp),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 28.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredExperiments) { exp ->
                        ExperimentCard(
                            experiment = exp,
                            onClick = { onExperimentClick(exp) }
                        )
                    }
                }
            }
        }

        // Modal Dialog for Coming Soon / Locked Experiments
        if (comingSoonExperiment != null) {
            val exp = comingSoonExperiment!!
            AlertDialog(
                onDismissRequest = { comingSoonExperiment = null },
                containerColor = ScienceDarkSurface,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = AmberVibrant.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AmberVibrant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = stringResource(Res.string.badge_day_locked, exp.day),
                                color = AmberVibrant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = stringResource(Res.string.coming_soon_title),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = exp.title,
                            color = CyanNeon,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = exp.subtitle,
                            color = AmberVibrant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Surface(
                            color = ScienceDarkSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = exp.teaser,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Text(
                            text = stringResource(Res.string.coming_soon_dialog_unlocked_soon, exp.day),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Text(
                            text = stringResource(Res.string.coming_soon_theory, exp.formulaTitle),
                            color = PurpleNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { comingSoonExperiment = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = ScienceDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(Res.string.action_got_it), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showLanguageDialog) {
            LanguageSelectionDialog(
                onDismiss = { showLanguageDialog = false }
            )
        }
    }
}

