package com.example.healthdiary.report

import android.content.Context
import com.example.healthdiary.R
import com.example.healthdiary.data.Medication
import com.example.healthdiary.data.Reading
import com.example.healthdiary.data.ReadingType
import com.example.healthdiary.util.fmt
import com.example.healthdiary.util.typeLabelRes
import com.example.healthdiary.util.valueText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Builds the plain-text report that is sent to the doctor through any messenger. */
object ReportBuilder {

    fun build(
        ctx: Context,
        patientName: String,
        days: Int,
        readings: List<Reading>,
        meds: List<Medication>
    ): String {
        val now = System.currentTimeMillis()
        val from = if (days > 0) now - days * 24L * 3_600_000L else 0L
        val list = readings.filter { it.timestamp >= from }.sortedBy { it.timestamp }

        val dayFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dateTimeFmt = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
        val none = "• " + ctx.getString(R.string.report_none)

        val sb = StringBuilder()
        sb.appendLine(
            if (patientName.isBlank()) ctx.getString(R.string.report_title_noname)
            else ctx.getString(R.string.report_title, patientName.trim())
        )
        sb.appendLine(
            if (days > 0) ctx.getString(
                R.string.report_period,
                dayFmt.format(Date(from)),
                dayFmt.format(Date(now))
            ) else ctx.getString(R.string.report_period_all)
        )

        sb.appendLine()
        sb.appendLine(ctx.getString(R.string.report_meds))
        if (meds.isEmpty()) {
            sb.appendLine(none)
        } else {
            meds.forEach { m ->
                val main = listOf(m.name, m.dose, m.schedule)
                    .filter { it.isNotBlank() }
                    .joinToString(" — ")
                val note = if (m.note.isBlank()) "" else " (${m.note.trim()})"
                sb.appendLine("• $main$note")
            }
        }

        sb.appendLine()
        sb.appendLine(ctx.getString(R.string.report_readings))
        if (list.isEmpty()) sb.appendLine(none)

        ReadingType.values().forEach { type ->
            val items = list.filter { it.type == type }
            if (items.isEmpty()) return@forEach
            sb.appendLine()
            sb.appendLine(ctx.getString(typeLabelRes(type)) + ":")
            items.forEach { r ->
                val note = if (r.note.isBlank()) "" else " (${r.note.trim()})"
                sb.appendLine("${dateTimeFmt.format(Date(r.timestamp))} — ${valueText(ctx, r)}$note")
            }
            if (type == ReadingType.PRESSURE && items.size > 1) {
                sb.appendLine(ctx.getString(R.string.report_avg, pressureAverage(items)))
            }
        }

        sb.appendLine()
        sb.append(ctx.getString(R.string.report_footer))
        return sb.toString().trimEnd()
    }

    private fun round(x: Double): Double = Math.round(x).toDouble()

    private fun pressureAverage(items: List<Reading>): String {
        val sys = round(items.map { it.v1 }.average())
        val dia = round(items.mapNotNull { it.v2 }.average())
        val pulses = items.mapNotNull { it.pulse }
        val base = "${fmt(sys)}/${fmt(dia)}"
        return if (pulses.isEmpty()) base else "$base, ${fmt(round(pulses.average()))}"
    }
}
