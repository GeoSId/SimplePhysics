package com.geosid.simplephysics.localization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String
) {
    ENGLISH("en", "English", "English", "🇬🇧"),
    GERMAN("de", "German", "Deutsch", "🇩🇪"),
    GREEK("el", "Greek", "Ελληνικά", "🇬🇷"),
    SPANISH("es", "Spanish", "Español", "🇪🇸"),
    FRENCH("fr", "French", "Français", "🇫🇷"),
    ITALIAN("it", "Italian", "Italiano", "🇮🇹"),
    PORTUGUESE("pt", "Portuguese", "Português", "🇵🇹");

    companion object {
        fun fromCode(code: String): AppLanguage {
            val normalized = code.lowercase().take(2)
            return entries.firstOrNull { it.code == normalized } ?: ENGLISH
        }
    }
}

expect fun setAppLocale(languageCode: String)
expect fun getCurrentAppLocale(): String

object LocalizationManager {
    var currentLanguage by mutableStateOf(AppLanguage.fromCode(getCurrentAppLocale()))
        private set

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
        setAppLocale(language.code)
    }
}
