package com.semenenko.healthdiary.voice

import com.semenenko.healthdiary.data.ReadingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceParserTest {

    @Test
    fun pressureWithPulse() {
        val p = VoiceParser.parse("Давление 135 на 80 пульс 72")
        assertNotNull(p)
        assertEquals(ReadingType.PRESSURE, p!!.type)
        assertEquals(135.0, p.v1!!, 0.0)
        assertEquals(80.0, p.v2!!, 0.0)
        assertEquals(72, p.pulse)
    }

    @Test
    fun pulseSaidFirst() {
        val p = VoiceParser.parse("пульс 68 давление 128 на 76")
        assertNotNull(p)
        assertEquals(128.0, p!!.v1!!, 0.0)
        assertEquals(76.0, p.v2!!, 0.0)
        assertEquals(68, p.pulse)
    }

    @Test
    fun pressureWithoutKeyword() {
        val p = VoiceParser.parse("135/80")
        assertNotNull(p)
        assertEquals(ReadingType.PRESSURE, p!!.type)
    }

    @Test
    fun sugarWithComma() {
        val p = VoiceParser.parse("сахар 5,4")
        assertNotNull(p)
        assertEquals(ReadingType.SUGAR, p!!.type)
        assertEquals(5.4, p.v1!!, 0.0)
    }

    @Test
    fun englishPressure() {
        val p = VoiceParser.parse("pressure 120 over 80")
        assertNotNull(p)
        assertEquals(ReadingType.PRESSURE, p!!.type)
        assertEquals(120.0, p.v1!!, 0.0)
    }

    @Test
    fun sugarSaidInWords() {
        val a = VoiceParser.parse("сахар пять запятая пять")
        assertNotNull(a)
        assertEquals(ReadingType.SUGAR, a!!.type)
        assertEquals(5.5, a.v1!!, 0.0)
        val b = VoiceParser.parse("сахар 5 и 5")
        assertNotNull(b)
        assertEquals(5.5, b!!.v1!!, 0.0)
    }

    @Test
    fun pressureSaidInWords() {
        val p = VoiceParser.parse("давление сто тридцать пять на восемьдесят пульс семьдесят два")
        assertNotNull(p)
        assertEquals(135.0, p!!.v1!!, 0.0)
        assertEquals(80.0, p.v2!!, 0.0)
        assertEquals(72, p.pulse)
    }

    @Test
    fun chosenTypeIsUsedWhenPhraseHasNoKeyword() {
        val a = VoiceParser.parse("четыре запятая два", ReadingType.SUGAR)
        assertNotNull(a)
        assertEquals(ReadingType.SUGAR, a!!.type)
        assertEquals(4.2, a.v1!!, 0.0)

        val b = VoiceParser.parse("шесть точка семь", ReadingType.SUGAR)
        assertEquals(6.7, b!!.v1!!, 0.0)

        val c = VoiceParser.parse("три и один", ReadingType.WEIGHT)
        assertEquals(ReadingType.WEIGHT, c!!.type)
        assertEquals(3.1, c.v1!!, 0.0)

        assertNull(VoiceParser.parse("четыре запятая два"))
    }

    @Test
    fun noNumbers() {
        assertNull(VoiceParser.parse("давление нормальное"))
    }
}
