package com.semenenko.healthdiary.util

import com.semenenko.healthdiary.data.DayPart
import com.semenenko.healthdiary.data.Medication
import com.semenenko.healthdiary.data.Reading
import com.semenenko.healthdiary.data.ReadingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackupTest {

    @Test
    fun exportThenImportKeepsEverything() {
        val readings = listOf(
            Reading(timestamp = 1000, type = ReadingType.PRESSURE, v1 = 135.0, v2 = 80.0, pulse = 72,
                note = "после прогулки", dayPart = DayPart.EVENING, meds = "Лозап 12.5"),
            Reading(timestamp = 2000, type = ReadingType.SUGAR, v1 = 5.4)
        )
        val meds = listOf(Medication(name = "Лозап", dose = "12.5 мг", schedule = "утром", note = "после еды"))
        val back = Backup.parse(Backup.toJson("Иван", readings, meds))!!
        assertEquals("Иван", back.patientName)
        assertEquals(readings, back.readings)
        assertEquals(meds, back.medications)
    }

    @Test
    fun foreignFileIsRejected() {
        assertNull(Backup.parse("{\"hello\": 1}"))
        assertNull(Backup.parse("not json"))
    }
}
