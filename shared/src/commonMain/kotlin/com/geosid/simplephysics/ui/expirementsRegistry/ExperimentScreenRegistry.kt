package com.geosid.simplephysics.ui.expirementsRegistry

import androidx.compose.runtime.Composable
import com.geosid.simplephysics.ui.experiments.week1.Day1.StaticStrawExperiment
import com.geosid.simplephysics.ui.experiments.week1.Day2.CitrusBalloonExperiment
import com.geosid.simplephysics.ui.experiments.week1.Day3.PencilWaterBagExperiment
import com.geosid.simplephysics.ui.experiments.week1.Day4.RefractionGlassExperiment

object ExperimentScreenRegistry {
    val screens: Map<String, @Composable () -> Unit> = mapOf(
        "static_straw" to { StaticStrawExperiment() },
        "citrus_balloon" to { CitrusBalloonExperiment() },
        "pencil_water_bag" to { PencilWaterBagExperiment() },
        "disappearing_glass" to { RefractionGlassExperiment() },
    )

    fun isReleased(id: String): Boolean = screens.containsKey(id)

    fun getScreen(id: String): (@Composable () -> Unit)? = screens[id]
}
