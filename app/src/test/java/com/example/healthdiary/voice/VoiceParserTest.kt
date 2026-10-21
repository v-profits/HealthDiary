package com.example.healthdiary.voice

import com.example.healthdiary.data.ReadingType
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
    fun noNumbers() {
        assertNull(VoiceParser.parse("давление нормальное"))
    }
}
