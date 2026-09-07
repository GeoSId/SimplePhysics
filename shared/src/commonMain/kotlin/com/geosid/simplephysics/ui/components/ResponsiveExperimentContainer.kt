package com.geosid.simplephysics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosid.simplephysics.ui.theme.*

/**
 * A responsive layout container for interactive physics experiments.
 *
 * On mobile/portrait displays (< 650dp width or constrained height):
 * - Dedicates the top half to an unobstructed, fully interactive simulation canvas viewport.
 * - Places live telemetry lists and interactive controls in a smoothly scrollable bottom area.
 *
 * On desktop/wide displays:
 * - Shows the full-screen canvas with floating HUD and bottom controls deck overlay.
 */
@Composable
fun ResponsiveExperimentContainer(
    modifier: Modifier = Modifier,
    instructions: String? = null,
    canvasContent: @Composable BoxScope.() -> Unit,
    hudContent: @Composable () -> Unit,
    controlsContent: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ScienceDarkBg)
    ) {
        val isMobile = maxWidth < 650.dp || maxHeight < 550.dp

        if (isMobile) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScienceDarkBg)
            ) {
                // 1. Simulation Canvas Viewport (100% visible & touch interactive!)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.05f)
                        .clipToBounds()
                        .background(ScienceDarkBg)
                ) {
                    canvasContent()
                }

                HorizontalDivider(
                    color = ScienceBorder,
                    thickness = 1.dp
                )

                // 2. Scrollable Area: Telemetry Lists & Interactive Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(ScienceDarkSurface)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (instructions != null) {
                        Surface(
                            color = ScienceDarkSurfaceVariant.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ScienceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = instructions,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Live Telemetry HUD Card
                    hudContent()

                    // Interactive Controls Deck
                    controlsContent()

                    // Extra space for Android navigation bar / gesture insets
                    Spacer(Modifier.navigationBarsPadding())
                    Spacer(Modifier.height(12.dp))
                }
            }
        } else {
            // Desktop / Wide Layout
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScienceDarkBg)
            ) {
                canvasContent()

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    hudContent()
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    controlsContent()
                }
            }
        }
    }
}
