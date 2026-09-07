package com.geosid.simplephysics.ui.expirementsRegistry

import androidx.compose.runtime.Composable
import com.geosid.simplephysics.ui.experiments.week1.Day1.StaticStrawExperiment

object ExperimentScreenRegistry {
    val screens: Map<String, @Composable () -> Unit> = mapOf(
        "static_straw" to { StaticStrawExperiment() },
    )

    fun isReleased(id: String): Boolean = screens.containsKey(id)

    fun getScreen(id: String): (@Composable () -> Unit)? = screens[id]
}
