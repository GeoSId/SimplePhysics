package com.geosid.simplephysics.ui.expirementsRegistry

import androidx.compose.runtime.Composable
import com.geosid.simplephysics.ui.experiments.week1.Day1.StaticStrawExperiment
import com.geosid.simplephysics.ui.experiments.week1.Day2.CitrusBalloonExperiment
import com.geosid.simplephysics.ui.experiments.week1.Day3.PencilWaterBagExperiment
import com.geosid.simplephysics.ui.experiments.week1.Day4.RefractionGlassExperiment
import com.geosid.simplephysics.ui.experiments.week1.Day5.CartesianDiverExperiment
import com.geosid.simplephysics.ui.experiments.week1.Day6.OobleckExperiment
import com.geosid.simplephysics.ui.experiments.week1.Day7.BernoulliBallExperiment
import com.geosid.simplephysics.ui.experiments.week2.Day8.ProjectileMotionExperiment
import com.geosid.simplephysics.ui.experiments.week2.Day9.BouncingBallExperiment

object ExperimentScreenRegistry {
    val screens: Map<String, @Composable () -> Unit> = mapOf(
        //week 1
        "static_straw" to { StaticStrawExperiment() },
        "citrus_balloon" to { CitrusBalloonExperiment() },
        "pencil_water_bag" to { PencilWaterBagExperiment() },
        "disappearing_glass" to { RefractionGlassExperiment() },
        "cartesian_diver" to { CartesianDiverExperiment() },
        "oobleck" to { OobleckExperiment() },
        "bernoulli_ball" to { BernoulliBallExperiment() },
        //week 2
        "projectile_drag" to { ProjectileMotionExperiment() },
        "bouncing_ball" to { BouncingBallExperiment() }
    )

    fun isReleased(id: String): Boolean = screens.containsKey(id)

    fun getScreen(id: String): (@Composable () -> Unit)? = screens[id]
}
