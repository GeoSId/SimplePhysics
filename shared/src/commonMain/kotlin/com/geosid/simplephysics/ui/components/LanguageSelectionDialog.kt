package com.geosid.simplephysics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.geosid.simplephysics.localization.AppLanguage
import com.geosid.simplephysics.localization.LocalizationManager
import com.geosid.simplephysics.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import simplephysics.shared.generated.resources.*

@Composable
fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit = {}
) {
    val currentLang = LocalizationManager.currentLanguage

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ScienceBorder, RoundedCornerShape(20.dp)),
            color = ScienceDarkSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            GlobeIcon(
                                modifier = Modifier.size(20.dp),
                                tint = CyanNeon
                            )
                        }
                        Text(
                            text = stringResource(Res.string.dialog_select_language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        CloseIcon(
                            modifier = Modifier.size(18.dp),
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Language list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        val isSelected = lang == currentLang

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    LocalizationManager.setLanguage(lang)
                                    onLanguageSelected(lang)
                                    onDismiss()
                                },
                            color = if (isSelected) CyanNeon.copy(alpha = 0.12f) else ScienceDarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(1.5.dp, CyanNeon)
                            } else {
                                androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder.copy(alpha = 0.5f))
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = lang.flag,
                                    fontSize = 24.sp
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) CyanNeon else TextPrimary
                                    )
                                    Text(
                                        text = lang.displayName,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(CyanNeon),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CheckmarkIcon(
                                            modifier = Modifier.size(14.dp),
                                            tint = ScienceDarkBg
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
