package com.semenenko.healthdiary.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

/** Small settings stored on the phone. */
object AppPrefs {
    private const val FILE = "diary"
    const val KEY_NAME = "patient_name"

    /** 0 = follow the system, 1 = light, 2 = dark. */
    const val KEY_THEME = "theme"

    /** "" = follow the system, "ru", "en". */
    const val KEY_LANG = "lang"

    /** How many times the app was opened, and whether the user has rated it already. */
    /** 0 = cards, 1 = table (view of the readings list). */
    const val KEY_VIEW = "readings_view"

    const val KEY_LAUNCHES = "launches"
    const val KEY_RATED = "rated"

    fun of(ctx: Context): SharedPreferences = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}

/** Applies the language chosen in Settings to an activity context. */
object LocaleHelper {
    fun wrap(base: Context): Context {
        val lang = AppPrefs.of(base).getString(AppPrefs.KEY_LANG, "") ?: ""
        if (lang.isEmpty()) return base
        val config = Configuration(base.resources.configuration)
        config.setLocale(Locale.forLanguageTag(lang))
        return base.createConfigurationContext(config)
    }
}
