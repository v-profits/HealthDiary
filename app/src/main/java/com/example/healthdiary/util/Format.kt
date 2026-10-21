package com.example.healthdiary.util

import android.content.Context
import com.example.healthdiary.R
import com.example.healthdiary.data.DayPart
import com.example.healthdiary.data.Medication
import com.example.healthdiary.data.Reading
import com.example.healthdiary.data.ReadingType
import com.example.healthdiary.voice.SpokenNumber
import java.util.Calendar
import java.util.Locale

/** 135.0 -> "135", 36.6 -> "36.6" (decimal separator follows the phone locale). */
fun fmt(v: Double): String =
    if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.getDefault(), "%.1f", v)

/** Accepts both "36,6" and "36.6". */
fun toNum(s: String): Double? = s.trim().replace(',', '.').toDoubleOrNull()

/** First number in a spoken phrase, as text. Used by the microphone in numeric fields. */
fun firstNumber(s: String, integerOnly: Boolean): String? {
    // Words become digits first: "пять запятая пять" -> "5.5", "сто тридцать пять" -> "135".
    val text = SpokenNumber.normalize(s, andIsDecimal = !integerOnly)
    val re = if (integerOnly) Regex("""\d+""") else Regex("""\d+(?:[.,]\d+)?""")
    return re.find(text)?.value
}

fun typeLabelRes(t: ReadingType): Int = when (t) {
    ReadingType.PRESSURE -> R.string.type_pressure
    ReadingType.SUGAR -> R.string.type_sugar
    ReadingType.WEIGHT -> R.string.type_weight
    ReadingType.TEMPERATURE -> R.string.type_temperature
    ReadingType.SPO2 -> R.string.type_spo2
}

fun unitRes(t: ReadingType): Int = when (t) {
    ReadingType.PRESSURE -> R.string.unit_pressure
    ReadingType.SUGAR -> R.string.unit_sugar
    ReadingType.WEIGHT -> R.string.unit_weight
    ReadingType.TEMPERATURE -> R.string.unit_temperature
    ReadingType.SPO2 -> R.string.unit_spo2
}

fun partLabelRes(p: DayPart): Int =
    if (p == DayPart.MORNING) R.string.part_morning else R.string.part_evening

/** Before 14:00 the reading is proposed as "morning", later as "evening". */
fun defaultDayPart(): DayPart =
    if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 14) DayPart.MORNING else DayPart.EVENING

/** "135/80 mmHg, pulse 72" or "5.4 mmol/L". Used in the report for the doctor. */
fun valueText(ctx: Context, r: Reading): String {
    val unit = ctx.getString(unitRes(r.type))
    if (r.type != ReadingType.PRESSURE) return "${fmt(r.v1)} $unit"
    val dia = r.v2?.let { fmt(it) } ?: "?"
    val base = "${fmt(r.v1)}/$dia $unit"
    val pulse = r.pulse ?: return base
    return base + ", " + ctx.getString(R.string.pulse_value, pulse)
}

const val MEDS_SEP = "; "

/** "Perindopril 4 mg": the label of a medicine as it is stored in a reading. */
fun medLabel(m: Medication): String =
    listOf(m.name, m.dose).filter { it.isNotBlank() }.joinToString(" ")

fun splitMeds(s: String): List<String> =
    s.split(";").map { it.trim() }.filter { it.isNotEmpty() }

/** Sanity ranges that catch typos (for example 1350 instead of 135). */
object Validation {
    fun ok(type: ReadingType, a: Double?, b: Double?): Boolean {
        if (a == null) return false
        return when (type) {
            ReadingType.PRESSURE -> b != null && a in 50.0..300.0 && b in 30.0..200.0 && a > b
            ReadingType.SUGAR -> a in 0.5..60.0
            ReadingType.WEIGHT -> a in 20.0..300.0
            ReadingType.TEMPERATURE -> a in 30.0..45.0
            ReadingType.SPO2 -> a in 50.0..100.0
        }
    }

    fun pulseOk(text: String): Boolean {
        if (text.isBlank()) return true
        val p = text.trim().toIntOrNull() ?: return false
        return p in 20..250
    }
}
