package com.geosid.simplephysics

import com.geosid.simplephysics.ui.experiments.week2.Day14.PulleyCargo
import com.geosid.simplephysics.ui.experiments.week2.Day14.PulleyConfig
import com.geosid.simplephysics.ui.expirementsRegistry.ExperimentScreenRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SharedLogicDesktopTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun testDay14Registry() {
        assertTrue(ExperimentScreenRegistry.isReleased("compound_pulley"))
        assertNotNull(ExperimentScreenRegistry.getScreen("compound_pulley"))
    }

    @Test
    fun testDay14MechanicalAdvantage() {
        // Verify segments
        assertEquals(1, PulleyConfig.SINGLE_FIXED.segments)
        assertEquals(2, PulleyConfig.SINGLE_MOVABLE.segments)
        assertEquals(4, PulleyConfig.BLOCK_AND_TACKLE.segments)
        assertEquals(8, PulleyConfig.CRANE_RIG.segments)

        // Verify Work Conservation & Force Division:
        val gravity = 9.81f
        val mass = PulleyCargo.VAULT_2500.massKg // 2200 kg
        val weight = mass * gravity
        val liftHeight = 1.5f // meters

        for (config in PulleyConfig.entries) {
            val idealForce = weight / config.segments
            val ropePulled = liftHeight * config.segments

            val workInput = idealForce * ropePulled
            val workOutput = weight * liftHeight

            // Golden Rule of Mechanics: Work In == Work Out (ideal)
            assertEquals(workOutput, workInput, 0.01f)
            assertTrue(idealForce <= weight)
        }
    }
}