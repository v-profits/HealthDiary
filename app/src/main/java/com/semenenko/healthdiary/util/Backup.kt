package com.semenenko.healthdiary.util

import com.semenenko.healthdiary.data.DayPart
import com.semenenko.healthdiary.data.Medication
import com.semenenko.healthdiary.data.Reading
import com.semenenko.healthdiary.data.ReadingType
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Everything the app stores, as one JSON file: export and import of all data. */
object Backup {
    private const val FORMAT = 1

    class Data(
        val patientName: String,
        val readings: List<Reading>,
        val medications: List<Medication>
    )

    fun fileName(): String =
        "healthdiary_" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) + ".json"

    fun toJson(patientName: String, readings: List<Reading>, meds: List<Medication>): String {
        val root = JSONObject()
        root.put("app", "healthdiary")
        root.put("format", FORMAT)
        root.put("patientName", patientName)
        val rs = JSONArray()
        readings.forEach { r ->
            val o = JSONObject()
            o.put("time", r.timestamp)
            o.put("type", r.type.name)
            o.put("v1", r.v1)
            if (r.v2 != null) o.put("v2", r.v2)
            if (r.pulse != null) o.put("pulse", r.pulse)
            o.put("part", r.dayPart.name)
            o.put("note", r.note)
            o.put("meds", r.meds)
            rs.put(o)
        }
        root.put("readings", rs)
        val ms = JSONArray()
        meds.forEach { m ->
            val o = JSONObject()
            o.put("name", m.name)
            o.put("dose", m.dose)
            o.put("schedule", m.schedule)
            o.put("note", m.note)
            ms.put(o)
        }
        root.put("medications", ms)
        return root.toString(2)
    }

    /** Returns null when the text is not a valid backup of this app. Nothing is changed in that case. */
    fun parse(text: String): Data? = try {
        val root = JSONObject(text)
        if (root.optString("app") != "healthdiary") {
            null
        } else {
            val readings = ArrayList<Reading>()
            val rs = root.optJSONArray("readings") ?: JSONArray()
            for (i in 0 until rs.length()) {
                val o = rs.getJSONObject(i)
                readings.add(
                    Reading(
                        timestamp = o.getLong("time"),
                        type = ReadingType.valueOf(o.getString("type")),
                        v1 = o.getDouble("v1"),
                        v2 = if (o.has("v2")) o.getDouble("v2") else null,
                        pulse = if (o.has("pulse")) o.getInt("pulse") else null,
                        dayPart = DayPart.valueOf(o.optString("part", DayPart.MORNING.name)),
                        note = o.optString("note", ""),
                        meds = o.optString("meds", "")
                    )
                )
            }
            val meds = ArrayList<Medication>()
            val ms = root.optJSONArray("medications") ?: JSONArray()
            for (i in 0 until ms.length()) {
                val o = ms.getJSONObject(i)
                meds.add(
                    Medication(
                        name = o.optString("name", ""),
                        dose = o.optString("dose", ""),
                        schedule = o.optString("schedule", ""),
                        note = o.optString("note", "")
                    )
                )
            }
            Data(root.optString("patientName", ""), readings, meds)
        }
    } catch (e: Exception) {
        null
    }
}
