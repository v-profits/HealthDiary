package com.example.healthdiary.util

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
