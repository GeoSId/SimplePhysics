package com.geosid.simplephysics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import com.geosid.simplephysics.model.ExperimentRegistry

class SharedCommonTest {

    @Test
    fun testExperimentRegistryHasExperiments() {
        val experiments = ExperimentRegistry.experiments
        assertEquals(84, experiments.size)

        val sampleIds = setOf(
            "projectile_drag",
            "bouncing_ball",
            "plucked_string",
            "fourier_series",
            "ideal_gas_piston",
            "kepler_orbits",
            "pencil_water_bag",
            "disappearing_glass",
            "static_straw",
            "citrus_balloon",
            "cartesian_diver"
        )
        val actualIds = experiments.map { it.id }.toSet()
        sampleIds.forEach { id ->
            kotlin.test.assertTrue(actualIds.contains(id), "Expected experiment $id in registry")
        }
    }

    @Test
    fun testExperimentRetrievalById() {
        val exp = ExperimentRegistry.getById("pencil_water_bag")
        assertNotNull(exp)
        assertEquals("pencil_water_bag", exp.id)
        assertEquals(3, exp.day)

        val projExp = ExperimentRegistry.getById("projectile_drag")
        assertNotNull(projExp)
        assertEquals("projectile_drag", projExp.id)

        val fourierExp = ExperimentRegistry.getById("fourier_series")
        assertNotNull(fourierExp)
        assertEquals("fourier_series", fourierExp.id)

        val bounceExp = ExperimentRegistry.getById("bouncing_ball")
        assertNotNull(bounceExp)

        val stringExp = ExperimentRegistry.getById("plucked_string")
        assertNotNull(stringExp)

        val gasExp = ExperimentRegistry.getById("ideal_gas_piston")
        assertNotNull(gasExp)

        val keplerExp = ExperimentRegistry.getById("kepler_orbits")
        assertNotNull(keplerExp)
    }

    @Test
    fun testAppLanguageFromCode() {
        assertEquals(com.geosid.simplephysics.localization.AppLanguage.ENGLISH, com.geosid.simplephysics.localization.AppLanguage.fromCode("en"))
        assertEquals(com.geosid.simplephysics.localization.AppLanguage.GERMAN, com.geosid.simplephysics.localization.AppLanguage.fromCode("de"))
        assertEquals(com.geosid.simplephysics.localization.AppLanguage.GREEK, com.geosid.simplephysics.localization.AppLanguage.fromCode("el"))
        assertEquals(com.geosid.simplephysics.localization.AppLanguage.SPANISH, com.geosid.simplephysics.localization.AppLanguage.fromCode("es"))
        assertEquals(com.geosid.simplephysics.localization.AppLanguage.FRENCH, com.geosid.simplephysics.localization.AppLanguage.fromCode("fr"))
        assertEquals(com.geosid.simplephysics.localization.AppLanguage.ITALIAN, com.geosid.simplephysics.localization.AppLanguage.fromCode("it"))
        assertEquals(com.geosid.simplephysics.localization.AppLanguage.PORTUGUESE, com.geosid.simplephysics.localization.AppLanguage.fromCode("pt"))
        // Fallback
        assertEquals(com.geosid.simplephysics.localization.AppLanguage.ENGLISH, com.geosid.simplephysics.localization.AppLanguage.fromCode("unknown"))
    }

    @Test
    fun testLocalizationManagerLanguageSwitching() {
        val original = com.geosid.simplephysics.localization.LocalizationManager.currentLanguage
        try {
            com.geosid.simplephysics.localization.LocalizationManager.setLanguage(com.geosid.simplephysics.localization.AppLanguage.GERMAN)
            assertEquals(com.geosid.simplephysics.localization.AppLanguage.GERMAN, com.geosid.simplephysics.localization.LocalizationManager.currentLanguage)

            com.geosid.simplephysics.localization.LocalizationManager.setLanguage(com.geosid.simplephysics.localization.AppLanguage.GREEK)
            assertEquals(com.geosid.simplephysics.localization.AppLanguage.GREEK, com.geosid.simplephysics.localization.LocalizationManager.currentLanguage)
        } finally {
            com.geosid.simplephysics.localization.LocalizationManager.setLanguage(original)
        }
    }
}