package com.example.healthdiary.voice

import com.example.healthdiary.data.ReadingType

data class ParsedReading(
    val type: ReadingType,
    val v1: Double?,
    val v2: Double?,
    val pulse: Int?
)

/**
 * Turns a dictated or typed phrase into a reading, for example:
 *  "давление 135 на 80 пульс 72", "сахар 5,4", "вес 84", "pressure 135 over 80".
 * Speech recognizers usually write numbers as digits; spelled-out numbers are not handled.
 */
object VoiceParser {
    private val number = Regex("""\d+(?:[.,]\d+)?""")
    private val pulseRe = Regex("""(?:пульс|pulse|heart\s*rate)\D{0,15}?(\d{2,3})""")

    private fun hasAny(text: String, vararg keys: String) = keys.any { text.contains(it) }

    fun parse(raw: String): ParsedReading? {
        var t = raw.lowercase()

        var pulse: Int? = null
        val m = pulseRe.find(t)
        if (m != null) {
            pulse = m.groupValues[1].toInt()
            t = t.removeRange(m.range)
        }

        val nums = number.findAll(t).map { it.value.replace(',', '.').toDouble() }.toList()
        if (nums.isEmpty()) return null

        val type: ReadingType = when {
            hasAny(t, "давлен", "тонометр", "pressure") -> ReadingType.PRESSURE
            hasAny(t, "сахар", "глюкоз", "sugar", "glucose") -> ReadingType.SUGAR
            hasAny(t, "темпер", "temperature") -> ReadingType.TEMPERATURE
            hasAny(t, "сатурац", "кислород", "spo", "oxygen", "saturation") -> ReadingType.SPO2
            hasAny(t, "вес", "weight") -> ReadingType.WEIGHT
            nums.size >= 2 && nums[0] in 70.0..260.0 && nums[1] in 30.0..160.0 && nums[0] > nums[1] ->
                ReadingType.PRESSURE
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
