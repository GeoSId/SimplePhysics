package com.geosid.simplephysics.ui.expirementsRegistry

import androidx.compose.runtime.Composable

object ExperimentScreenRegistry {
    val screens: Map<String, @Composable () -> Unit> = mapOf()

    fun isReleased(id: String): Boolean = screens.containsKey(id)

    fun getScreen(id: String): (@Composable () -> Unit)? = screens[id]
}
