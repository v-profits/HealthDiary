package com.semenenko.healthdiary.voice

import com.semenenko.healthdiary.data.ReadingType

data class ParsedReading(
    val type: ReadingType,
    val v1: Double?,
    val v2: Double?,
    val pulse: Int?
)

/**
 * Turns a dictated or typed phrase into a reading, for example:
 *  "давление 135 на 80 пульс 72", "сахар пять запятая пять", "вес 84", "pressure 135 over 80".
 * Numbers may be digits or words (see [SpokenNumber]).
 */
object VoiceParser {
    private val number = Regex("""\d+(?:[.,]\d+)?""")
    private val pulseRe = Regex("""(?:пульс|pulse|heart\s*rate)\D{0,15}?(\d{2,3})""")

    private fun hasAny(text: String, vararg keys: String) = keys.any { text.contains(it) }

    /** The measurement named in the phrase, or null when no keyword is said. */
    private fun keywordType(t: String): ReadingType? = when {
        hasAny(t, "давлен", "тонометр", "pressure") -> ReadingType.PRESSURE
        hasAny(t, "сахар", "глюкоз", "sugar", "glucose") -> ReadingType.SUGAR
        hasAny(t, "темпер", "temperature") -> ReadingType.TEMPERATURE
        hasAny(t, "сатурац", "кислород", "spo", "oxygen", "saturation") -> ReadingType.SPO2
        hasAny(t, "вес", "weight") -> ReadingType.WEIGHT
        else -> null
    }

    /**
     * [defaultType] is the measurement already chosen on the screen. When the phrase does not
     * name a measurement, it is used, so "четыре запятая два" is enough for sugar.
     */
    fun parse(raw: String, defaultType: ReadingType? = null): ParsedReading? {
        var t = SpokenNumber.normalize(raw, andIsDecimal = false)
        val keyType = keywordType(t)
        val decimalType = keyType ?: defaultType
        if (decimalType != null && decimalType != ReadingType.PRESSURE) {
            // sugar, weight, temperature: "5 и 5" means 5.5
            t = SpokenNumber.normalize(raw, andIsDecimal = true)
        }

        var pulse: Int? = null
        val m = pulseRe.find(t)
        if (m != null) {
            pulse = m.groupValues[1].toInt()
            t = t.removeRange(m.range)
        }

        val nums = number.findAll(t).map { it.value.replace(',', '.').toDouble() }.toList()
        if (nums.isEmpty()) return null

        val type: ReadingType = keyType ?: when {
            nums.size >= 2 && nums[0] in 70.0..260.0 && nums[1] in 30.0..160.0 && nums[0] > nums[1] ->
                ReadingType.PRESSURE
            defaultType != null && defaultType != ReadingType.PRESSURE -> defaultType
            else -> return null
        }

        return if (type == ReadingType.PRESSURE) {
            if (nums.size < 2) null
            else ParsedReading(type, nums[0], nums[1], pulse ?: nums.getOrNull(2)?.toInt())
        } else {
            ParsedReading(type, nums[0], null, null)
        }
    }
}
