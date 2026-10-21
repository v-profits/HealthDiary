package com.example.healthdiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReadingType { PRESSURE, SUGAR, WEIGHT, TEMPERATURE, SPO2 }

enum class DayPart { MORNING, EVENING }

/**
 * One measurement. For PRESSURE: v1 = systolic, v2 = diastolic, pulse = optional.
 * For all other types only v1 is used.
 * [meds] is a text snapshot of the medicines taken, "Name dose; Name dose",
 * picked from the patient's medicine list, so history stays correct if the list changes later.
 */
@Entity(tableName = "readings")
data class Reading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: ReadingType,
    val v1: Double,
    val v2: Double? = null,
    val pulse: Int? = null,
    val note: String = "",
    val dayPart: DayPart = DayPart.MORNING,
    val meds: String = ""
)

/** A medicine the patient takes. All text is free-form, filled in by the patient. */
@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val dose: String = "",
    val schedule: String = "",
    val note: String = ""
)
