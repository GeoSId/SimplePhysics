package com.geosid.simplephysics.localization

import android.os.Build
import android.os.LocaleList
import java.util.Locale

actual fun setAppLocale(languageCode: String) {
    val locale = Locale.forLanguageTag(languageCode)
    Locale.setDefault(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
    }
}

actual fun getCurrentAppLocale(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.getDefault()[0]?.language ?: Locale.getDefault().language
    } else {
        Locale.getDefault().language
    }
}
