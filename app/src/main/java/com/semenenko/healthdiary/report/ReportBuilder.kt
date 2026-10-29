package com.semenenko.healthdiary.report

import android.content.Context
import com.semenenko.healthdiary.R
import com.semenenko.healthdiary.data.DayPart
import com.semenenko.healthdiary.data.Medication
import com.semenenko.healthdiary.data.Reading
import com.semenenko.healthdiary.data.ReadingType
import com.semenenko.healthdiary.util.fmt
import com.semenenko.healthdiary.util.partLabelRes
import com.semenenko.healthdiary.util.splitMeds
import com.semenenko.healthdiary.util.typeLabelRes
import com.semenenko.healthdiary.util.unitRes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Builds the report for the doctor as tables: one table of medicines and one table per
 * measurement type. The same data is shown on screen, sent as PDF, or sent as plain text.
 */
object ReportBuilder {

    fun data(
        ctx: Context,
        patientName: String,
        days: Int,
        readings: List<Reading>,
        meds: List<Medication>
    ): ReportData {
        val now = System.currentTimeMillis()
        val from = if (days > 0) now - days * 24L * 3_600_000L else 0L
        val list = readings.filter { it.timestamp >= from }.sortedBy { it.timestamp }
        val dayFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val whenFmt = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())

        val title = if (patientName.isBlank()) ctx.getString(R.string.report_title_noname)
        else ctx.getString(R.string.report_title, patientName.trim())
        val period = if (days > 0) ctx.getString(
            R.string.report_period, dayFmt.format(Date(from)), dayFmt.format(Date(now))
        ) else ctx.getString(R.string.report_period_all)

        val tables = ArrayList<ReportTable>()

        if (meds.isNotEmpty()) {
            tables.add(
                ReportTable(
                    title = ctx.getString(R.string.report_meds).trimEnd(':'),
                    headers = listOf(
                        ctx.getString(R.string.col_med_name),
                        ctx.getString(R.string.col_med_dose),
                        ctx.getString(R.string.col_med_schedule),
                        ctx.getString(R.string.col_note)
                    ),
                    rows = meds.map { ReportRow(listOf(it.name, it.dose, it.schedule, it.note)) },
                    wide = setOf(0, 2, 3)
                )
            )
        }

        ReadingType.values().forEach { type ->
            val items = list.filter { it.type == type }
            if (items.isEmpty()) return@forEach
            val pressure = type == ReadingType.PRESSURE
            val unit = ctx.getString(unitRes(type))
            val hasMeds = items.any { it.meds.isNotBlank() }
            val hasNote = items.any { it.note.isNotBlank() }

            val headers = ArrayList<String>()
            headers.add(ctx.getString(R.string.rep_col_when))
            headers.add(ctx.getString(R.string.rep_col_part))
            if (pressure) {
                headers.add(ctx.getString(R.string.col_systolic) + "\n" + unit)
                headers.add(ctx.getString(R.string.col_diastolic) + "\n" + unit)
                headers.add(ctx.getString(R.string.col_pulse) + "\n" + ctx.getString(R.string.unit_bpm))
            } else {
                headers.add(ctx.getString(R.string.rep_col_value) + "\n" + unit)
            }
            val wide = HashSet<Int>()
            if (hasMeds) {
                wide.add(headers.size)
                headers.add(ctx.getString(R.string.col_meds))
            }
            if (hasNote) {
                wide.add(headers.size)
                headers.add(ctx.getString(R.string.col_note))
            }

            // Fills the columns after the values with medicines and note when they exist.
            fun tail(meds: String?, note: String?): List<String> {
                val t = ArrayList<String>()
                if (hasMeds) t.add(meds ?: "")
                if (hasNote) t.add(note ?: "")
                return t
            }

            val rows = ArrayList<ReportRow>()
            items.forEach { r ->
                val cells = ArrayList<String>()
                cells.add(whenFmt.format(Date(r.timestamp)))
                cells.add(ctx.getString(partLabelRes(r.dayPart)))
                if (pressure) {
                    cells.add(fmt(r.v1))
                    cells.add(r.v2?.let { fmt(it) } ?: "")
                    cells.add(r.pulse?.toString() ?: "")
                } else {
                    cells.add(fmt(r.v1))
                }
                cells.addAll(tail(splitMeds(r.meds).joinToString(", "), r.note.trim()))
                rows.add(ReportRow(cells))
            }

            if (items.size > 1) {
                if (pressure) {
                    fun avgRow(label: String, part: String, group: List<Reading>): ReportRow {
                        val pulses = group.mapNotNull { it.pulse }
                        val cells = arrayListOf(
                            label,
                            part,
                            fmt(round(group.map { it.v1 }.average())),
                            group.mapNotNull { it.v2 }.takeIf { it.isNotEmpty() }
                                ?.let { fmt(round(it.average())) } ?: "",
                            if (pulses.isEmpty()) "" else fmt(round(pulses.average()))
                        )
                        cells.addAll(tail(null, null))
                        return ReportRow(cells, bold = true)
                    }
                    rows.add(avgRow(ctx.getString(R.string.rep_avg), ctx.getString(R.string.rep_all_day), items))
                    // Morning and evening averages are what a doctor compares first.
                    DayPart.values().forEach { part ->
                        val g = items.filter { it.dayPart == part }
                        if (g.isNotEmpty() && g.size < items.size) {
                            rows.add(avgRow(ctx.getString(R.string.rep_avg), ctx.getString(partLabelRes(part)), g))
                        }
                    }
                } else {
                    val v = items.map { it.v1 }
                    listOf(
                        R.string.rep_avg to round1(v.average()),
                        R.string.rep_min to v.min(),
                        R.string.rep_max to v.max()
                    ).forEach { (label, value) ->
                        val cells = arrayListOf(ctx.getString(label), "", fmt(value))
                        cells.addAll(tail(null, null))
                        rows.add(ReportRow(cells, bold = true))
                    }
                }
            }
            tables.add(
                ReportTable(
                    title = ctx.getString(typeLabelRes(type)),
                    headers = headers,
                    rows = rows,
                    wide = wide
                )
            )
        }

        return ReportData(title, period, tables, ctx.getString(R.string.report_footer))
    }

    /** Plain text with " | " between the columns, for messengers that cannot show a PDF. */
    fun toText(ctx: Context, d: ReportData): String {
        val sb = StringBuilder()
        sb.appendLine(d.title)
        sb.appendLine(d.period)
        if (d.tables.isEmpty()) {
            sb.appendLine()
            sb.appendLine("• " + ctx.getString(R.string.report_none))
        }
        d.tables.forEach { t ->
            sb.appendLine()
            sb.appendLine("■ " + t.title)
            sb.appendLine(t.headers.joinToString(" | ") { it.replace("\n", ", ") })
            t.rows.forEach { r ->
                sb.appendLine(r.cells.joinToString(" | ") { it.ifBlank { "–" } })
            }
        }
        sb.appendLine()
        sb.append(d.footer)
        return sb.toString().trimEnd()
    }

    private fun round(x: Double): Double = Math.round(x).toDouble()

    private fun round1(x: Double): Double = Math.round(x * 10.0) / 10.0
}
