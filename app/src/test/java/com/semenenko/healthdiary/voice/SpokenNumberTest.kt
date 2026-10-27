package com.semenenko.healthdiary.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class SpokenNumberTest {

    @Test
    fun wordsBecomeDigits() {
        assertEquals("135", SpokenNumber.normalize("сто тридцать пять", false))
        assertEquals("80", SpokenNumber.normalize("восемьдесят", false))
        assertEquals("72", SpokenNumber.normalize("семьдесят два", false))
        assertEquals("120", SpokenNumber.normalize("one hundred twenty", false))
    }

    @Test
    fun separateNumbersStaySeparate() {
        assertEquals("135 на 80", SpokenNumber.normalize("сто тридцать пять на восемьдесят", false))
        assertEquals("5 5", SpokenNumber.normalize("пять пять", false))
    }

    @Test
    fun commaWordMakesDecimal() {
        assertEquals("5.5", SpokenNumber.normalize("пять запятая пять", false))
        assertEquals("5.5", SpokenNumber.normalize("5 запятая 5", false))
        assertEquals("5.5", SpokenNumber.normalize("пять целых пять десятых", false))
        assertEquals("5.5", SpokenNumber.normalize("five point five", false))
        assertEquals("36.6", SpokenNumber.normalize("тридцать шесть точка шесть", false))
    }

    @Test
    fun andMakesDecimalOnlyWhenAllowed() {
        assertEquals("5.5", SpokenNumber.normalize("5 и 5", true))
        assertEquals("5.5", SpokenNumber.normalize("пять и пять", true))
        assertEquals("135 и 80", SpokenNumber.normalize("135 и 80", false))
    }

    @Test
    fun digitsWithCommaAreLeftAlone() {
        assertEquals("5,4", SpokenNumber.normalize("5,4", true))
    }
}
