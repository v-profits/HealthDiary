package com.semenenko.healthdiary.util

import com.semenenko.healthdiary.data.Reading
import com.semenenko.healthdiary.data.ReadingType
import org.junit.Assert.assertEquals
import org.junit.Test

class LevelTest {
    private fun p(s: Double, d: Double) = Reading(timestamp = 0, type = ReadingType.PRESSURE, v1 = s, v2 = d)
    private fun r(t: ReadingType, v: Double) = Reading(timestamp = 0, type = t, v1 = v)

    @Test
    fun pressureLevels() {
        assertEquals(Level.NORMAL, levelOf(p(125.0, 75.0)))
        assertEquals(Level.ELEVATED, levelOf(p(135.0, 80.0)))
        assertEquals(Level.HIGH, levelOf(p(145.0, 80.0)))
        assertEquals(Level.HIGH, levelOf(p(125.0, 92.0)))
    }

    @Test
    fun otherLevels() {
        assertEquals(Level.NORMAL, levelOf(r(ReadingType.SUGAR, 5.0)))
        assertEquals(Level.HIGH, levelOf(r(ReadingType.SUGAR, 7.5)))
        assertEquals(Level.ELEVATED, levelOf(r(ReadingType.SPO2, 94.0)))
        assertEquals(Level.HIGH, levelOf(r(ReadingType.TEMPERATURE, 38.5)))
        assertEquals(Level.NONE, levelOf(r(ReadingType.WEIGHT, 84.0)))
    }
}
