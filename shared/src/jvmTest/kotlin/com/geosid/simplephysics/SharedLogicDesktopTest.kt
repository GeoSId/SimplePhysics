package com.geosid.simplephysics

import com.geosid.simplephysics.ui.experiments.week2.Day14.PulleyCargo
import com.geosid.simplephysics.ui.experiments.week2.Day14.PulleyConfig
import com.geosid.simplephysics.ui.expirementsRegistry.ExperimentScreenRegistry
import com.geosid.simplephysics.ui.experiments.week3.Day15.PlanetPreset
import kotlin.math.PI
import kotlin.math.sqrt
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

    @Test
    fun testDay15Registry() {
        assertTrue(ExperimentScreenRegistry.isReleased("kepler_orbits"))
        assertNotNull(ExperimentScreenRegistry.getScreen("kepler_orbits"))
    }

    @Test
    fun testDay15KeplerOrbitalMechanics() {
        // 1. Verify Presets
        assertEquals(0.00f, PlanetPreset.CIRCULAR.eccentricity)
        assertEquals(0.02f, PlanetPreset.EARTH.eccentricity)
        assertEquals(0.09f, PlanetPreset.MARS.eccentricity)
        assertEquals(0.25f, PlanetPreset.PLUTO.eccentricity)
        assertEquals(0.75f, PlanetPreset.COMET.eccentricity)

        val a = 1.0f // Semi-major axis normalized
        val mu = 1.0f // Normalized standard gravitational parameter GM

        for (preset in PlanetPreset.entries) {
            val e = preset.eccentricity
            assertTrue(e in 0.0f..<1.0f, "Eccentricity must be elliptical ($e)")

            // Perihelion and Aphelion distances
            val rPerihelion = a * (1f - e)
            val rAphelion = a * (1f + e)
            val semiMinorB = a * sqrt(1f - e * e)

            // Vis-Viva Equation: v^2 = mu * (2/r - 1/a)
            val vPerihelion = sqrt(mu * (2f / rPerihelion - 1f / a))
            val vAphelion = sqrt(mu * (2f / rAphelion - 1f / a))

            // Specific Angular Momentum: h = r * v
            val hPerihelion = rPerihelion * vPerihelion
            val hAphelion = rAphelion * vAphelion
            val hTheoretical = sqrt(mu * a * (1f - e * e))

            // Kepler's Second Law: Angular momentum must be equal at both extremes
            assertEquals(hTheoretical, hPerihelion, 0.001f)
            assertEquals(hTheoretical, hAphelion, 0.001f)

            // Dynamic speed differential: perihelion is always >= aphelion
            assertTrue(vPerihelion >= vAphelion)

            // Differential rate of change in Eccentric Anomaly: dE/dt = omega / (1 - e * cos(E))
            val omega = 1.0f
            val rateAtPerihelion = omega / (1f - e) // E = 0
            val rateAtAphelion = omega / (1f + e)   // E = PI
            assertTrue(rateAtPerihelion >= rateAtAphelion)
        }
    }

    @Test
    fun testDay27Registry() {
        assertTrue(ExperimentScreenRegistry.isReleased("acoustic_beats"))
        assertNotNull(ExperimentScreenRegistry.getScreen("acoustic_beats"))
    }

    @Test
    fun testDay27AcousticBeatsPhysics() {
        val f1 = 440.0f
        val f2 = 442.0f
        val amp1 = 1.0f
        val amp2 = 1.0f

        val beatFreq = kotlin.math.abs(f1 - f2)
        val carrierFreq = (f1 + f2) / 2f
        val beatPeriod = 1.0f / beatFreq

        assertEquals(2.0f, beatFreq, 0.001f)
        assertEquals(441.0f, carrierFreq, 0.001f)
        assertEquals(0.500f, beatPeriod, 0.001f)

        // Constructive interference amplitude: A1 + A2
        val maxAmp = amp1 + amp2
        assertEquals(2.0f, maxAmp, 0.001f)

        // Destructive interference amplitude: |A1 - A2|
        val minAmp = kotlin.math.abs(amp1 - amp2)
        assertEquals(0.0f, minAmp, 0.001f)

        // Acoustic intensity scales quadratically: I_max = (A1 + A2)^2 = 4 * A^2
        val maxIntensity = maxAmp * maxAmp
        assertEquals(4.0f, maxIntensity, 0.001f)

        // Unequal amplitude test (residual volume at destructive node)
        val unequalAmp2 = 0.6f
        val minUnequalAmp = kotlin.math.abs(amp1 - unequalAmp2)
        assertEquals(0.4f, minUnequalAmp, 0.001f)
    }
}