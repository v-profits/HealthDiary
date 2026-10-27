package com.semenenko.healthdiary.util

import android.content.Context
import com.semenenko.healthdiary.R
import com.semenenko.healthdiary.data.DayPart
import com.semenenko.healthdiary.data.Medication
import com.semenenko.healthdiary.data.Reading
import com.semenenko.healthdiary.data.ReadingType
import com.semenenko.healthdiary.voice.SpokenNumber
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

private val sentenceStart = Regex("""([.!?]\s+)(\p{L})""")

/**
 * Spoken text: the first letter after ". ", "! " and "? " is a capital. The very first letter is
 * a capital only when [startsSentence] is true (empty field, or the text before ends with . ! ?);
 * otherwise the spoken text continues the sentence and keeps its first letter as it is.
 * Digits and decimals ("4.2 мг") are not touched.
 */
fun capitalizeSentences(text: String, startsSentence: Boolean = true): String {
    val t = text.trim()
    if (t.isEmpty()) return t
    val first = if (startsSentence) t.replaceFirstChar { it.uppercaseChar() } else t
    return sentenceStart.replace(first) { it.groupValues[1] + it.groupValues[2].uppercase() }
}

/** True when text typed so far is empty or ends a sentence (. ! ?). */
fun endsSentence(text: String): Boolean {
    val t = text.trimEnd()
    return t.isEmpty() || t.last() in ".!?"
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

fun partLabelRes(p: DayPart): Int = when (p) {
    DayPart.MORNING -> R.string.part_morning
    DayPart.NOON -> R.string.part_noon
    DayPart.EVENING -> R.string.part_evening
    DayPart.NIGHT -> R.string.part_night
}

/** Proposed time of day for a new reading: 5-11 morning, 11-16 noon, 16-22 evening, else night. */
fun defaultDayPart(): DayPart = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..10 -> DayPart.MORNING
    in 11..15 -> DayPart.NOON
    in 16..21 -> DayPart.EVENING
    else -> DayPart.NIGHT
}

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
