package com.geosid.simplephysics.localization

import java.util.Locale

actual fun setAppLocale(languageCode: String) {
    val locale = Locale.forLanguageTag(languageCode)
    Locale.setDefault(locale)
}

actual fun getCurrentAppLocale(): String {
    return Locale.getDefault().language
}
