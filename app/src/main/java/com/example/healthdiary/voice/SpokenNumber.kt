package com.example.healthdiary.voice

/**
 * Turns spoken numbers into digits, so that dictation works the way people really speak:
 *  "сто тридцать пять" -> "135", "пять запятая пять" -> "5.5", "5 и 5" -> "5.5".
 * Russian and English words are understood. Text that already holds digits is left as it is.
 */
object SpokenNumber {

    private val values: Map<String, Int> = HashMap<String, Int>().apply {
        val small = listOf(
            "ноль", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять",
            "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать",
            "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать"
        )
        small.forEachIndexed { i, w -> put(w, i) }
        put("одна", 1)
        put("одно", 1)
        put("две", 2)

        listOf(
            "двадцать", "тридцать", "сорок", "пятьдесят",
            "шестьдесят", "семьдесят", "восемьдесят", "девяносто"
        ).forEachIndexed { i, w -> put(w, 20 + 10 * i) }

        listOf(
            "сто", "двести", "триста", "четыреста", "пятьсот",
            "шестьсот", "семьсот", "восемьсот", "девятьсот"
        ).forEachIndexed { i, w -> put(w, 100 * (i + 1)) }

        listOf(
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
            "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
            "seventeen", "eighteen", "nineteen"
        ).forEachIndexed { i, w -> put(w, i) }

        listOf(
            "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
        ).forEachIndexed { i, w -> put(w, 20 + 10 * i) }
    }

    /** Words that stand for the decimal separator. */
    private val separators = setOf(
        "запятая", "запятую", "точка", "точку", "целых", "целая", "целое",
        "point", "dot", "comma"
    )

    /** Words after a decimal fraction that carry no value ("пять целых пять десятых"). */
    private val ignored = setOf("десятых", "сотых", "десятая", "сотая", "tenths")

    /** Words that may join two numbers into a decimal when [andIsDecimal] is true. */
    private val joiners = setOf("и", "and")

    /** [prev] is a number built so far; can [next] be added to it ("сто" + "тридцать")? */
    private fun canFollow(prev: Int, next: Int): Boolean {
        if (prev >= 100 && prev % 100 == 0) return next in 1..99
        val tail = prev % 100
        return tail in 20..90 && tail % 10 == 0 && next in 1..9
    }

    private fun isNumber(s: String): Boolean = s.isNotEmpty() && s.all { it.isDigit() }

    /**
     * @param andIsDecimal true for fields that hold decimals (sugar, weight, temperature):
     * then "5 и 5" means 5.5. For blood pressure it stays false, so "135 и 80" is not changed.
     */
    fun normalize(raw: String, andIsDecimal: Boolean): String {
        val cleaned = raw.lowercase().replace(Regex("""[,;:!?]+(?=\s|$)"""), " ")
        val words = cleaned.trim().split(Regex("""\s+""")).filter { it.isNotEmpty() }

        // Step 1: number words -> digits.
        val tokens = ArrayList<String>()
        var i = 0
        while (i < words.size) {
            val first = values[words[i]]
            if (first == null) {
                tokens.add(words[i])
                i++
                continue
            }
            var total = first
            var j = i + 1
            while (j < words.size) {
                if (words[j] == "hundred" && total in 1..9) {
                    total *= 100
                    j++
                    continue
                }
                val next = values[words[j]] ?: break
                if (!canFollow(total, next)) break
                total += next
                j++
            }
            tokens.add(total.toString())
            i = j
        }

        // Step 2: "5 запятая 5" and, for decimal fields, "5 и 5" -> "5.5".
        val out = ArrayList<String>()
        var k = 0
        while (k < tokens.size) {
            val t = tokens[k]
            if (t in ignored) {
                k++
                continue
            }
            if (k + 2 < tokens.size && isNumber(t) && isNumber(tokens[k + 2])) {
                val mid = tokens[k + 1]
                if (mid in separators || (andIsDecimal && mid in joiners)) {
                    out.add(t + "." + tokens[k + 2])
                    k += 3
                    continue
                }
            }
            out.add(t)
            k++
        }
        return out.joinToString(" ")
    }
}
