package com.geosid.simplephysics.localization

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual fun setAppLocale(languageCode: String) {
    NSUserDefaults.standardUserDefaults.setObject(
        listOf(languageCode),
        "AppleLanguages"
    )
    NSUserDefaults.standardUserDefaults.synchronize()
}

actual fun getCurrentAppLocale(): String {
    val languages = NSLocale.preferredLanguages
    val first = languages.firstOrNull() as? String ?: "en"
    return first.take(2)
}
