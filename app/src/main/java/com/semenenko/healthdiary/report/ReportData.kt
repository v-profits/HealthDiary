package com.semenenko.healthdiary.report

/** One row of a report table. [bold] rows are summaries (average, min, max). */
class ReportRow(val cells: List<String>, val bold: Boolean = false)

/**
 * A table of the report: a column per value, so the doctor looks straight at the column he needs.
 * A header may have two lines separated by "\n" (name, then unit). [wide] are the free-text
 * columns (medicines, notes) that get the leftover width and wrap their text.
 */
class ReportTable(
    val title: String,
    val headers: List<String>,
    val rows: List<ReportRow>,
    val wide: Set<Int> = emptySet()
)

class ReportData(
    val title: String,
    val period: String,
    val tables: List<ReportTable>,
    val footer: String
)
