package com.example.healthdiary.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

    @Test
    fun firstLetterIsCapital() {
        assertEquals("Периндоприл", capitalizeSentences("периндоприл"))
    }

    @Test
    fun capitalAfterSentenceEnd() {
        assertEquals(
            "Привет. Как дела? Хорошо! Да",
            capitalizeSentences("привет. как дела? хорошо! да")
        )
    }

    @Test
    fun digitsAndDecimalsAreNotTouched() {
        assertEquals("4.2 мг утром", capitalizeSentences("4.2 мг утром"))
        assertEquals("Принимать 5.5 мг", capitalizeSentences("принимать 5.5 мг"))
    }

    @Test
    fun emptyTextStaysEmpty() {
        assertEquals("", capitalizeSentences("   "))
    }

    @Test
    fun continuationKeepsLowercase() {
        assertEquals("после еды. Вечером", capitalizeSentences("после еды. вечером", false))
        assertEquals(false, endsSentence("Принимать утром"))
        assertEquals(true, endsSentence("Принимать утром. "))
        assertEquals(true, endsSentence(""))
    }
}
