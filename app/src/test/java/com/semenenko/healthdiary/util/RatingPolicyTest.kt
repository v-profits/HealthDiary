package com.semenenko.healthdiary.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RatingPolicyTest {

    @Test
    fun askedAtTenthOpeningAndThenEveryFifty() {
        val due = (1..300).filter { RatingPolicy.isDue(it) }
        assertEquals(listOf(10, 60, 110, 160, 210, 260), due)
    }

    @Test
    fun starsGoToStoreOrMail() {
        assertEquals(listOf(false, false, false, true, true), (1..5).map { RatingPolicy.goesToStore(it) })
    }
}
