package com.semenenko.healthdiary.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.semenenko.healthdiary.R
import java.io.File
import java.io.FileOutputStream

/** Draws the report as an A4 PDF with real tables. No outside libraries: the Android PdfDocument. */
object ReportPdf {
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 32f
    private const val PAD = 4f
    private const val LINE = 11f

    private val teal = Color.rgb(11, 122, 117)
    private val zebra = Color.rgb(242, 247, 247)
    private val summary = Color.rgb(205, 237, 234)
    private val grid = Color.rgb(195, 206, 205)

    private fun paint(size: Float, bold: Boolean = false, color: Int = Color.BLACK) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private val body = paint(8.5f)
    private val bodyBold = paint(8.5f, true)
    private val head = paint(8.5f, true, Color.WHITE)
    private val fill = Paint().apply { style = Paint.Style.FILL }
    private val stroke = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 0.5f
        color = grid
    }

    private class Writer(val ctx: Context) {
        val doc = PdfDocument()
        lateinit var page: PdfDocument.Page
        lateinit var canvas: Canvas
        var pageNo = 0
        var y = MARGIN

        fun newPage() {
            if (pageNo > 0) closePage()
            pageNo++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create())
            canvas = page.canvas
            y = MARGIN
        }

        fun closePage() {
            val p = paint(8f, false, Color.GRAY).apply { textAlign = Paint.Align.CENTER }
            canvas.drawText(ctx.getString(R.string.rep_page, pageNo), PAGE_W / 2f, PAGE_H - 16f, p)
            doc.finishPage(page)
        }
    }

    fun write(ctx: Context, data: ReportData, file: File) {
        val w = Writer(ctx)
        w.newPage()

        w.canvas.drawText(data.title, MARGIN, w.y + 14f, paint(16f, true, teal))
        w.y += 22f
        w.canvas.drawText(data.period, MARGIN, w.y + 8f, paint(9.5f, false, Color.DKGRAY))
        w.y += 22f

        data.tables.forEach { table -> drawTable(w, table) }

        // Closing note, wrapped over the full width.
        val note = paint(8f, false, Color.GRAY)
        val lines = wrap(data.footer, note, PAGE_W - 2 * MARGIN)
        if (w.y + lines.size * 10f > PAGE_H - MARGIN - 14f) w.newPage()
        w.y += 6f
        lines.forEach {
            w.canvas.drawText(it, MARGIN, w.y + 8f, note)
            w.y += 10f
        }

        w.closePage()
        FileOutputStream(file).use { w.doc.writeTo(it) }
        w.doc.close()
    }

    private fun drawTable(w: Writer, t: ReportTable) {
        val widths = columnWidths(t)
        val headerLines = t.headers.map { it.split("\n").flatMap { l -> wrap(l, head, 1000f) } }
        val headerH = (headerLines.maxOf { it.size }) * LINE + 2 * PAD

        fun drawHeader() {
            var x = MARGIN
            t.headers.indices.forEach { i ->
                fill.color = teal
                w.canvas.drawRect(x, w.y, x + widths[i], w.y + headerH, fill)
                val center = x + widths[i] / 2f
                headerLines[i].forEachIndexed { k, line ->
                    val p = Paint(head).apply { textAlign = Paint.Align.CENTER }
                    w.canvas.drawText(line, center, w.y + PAD + LINE * (k + 1) - 2f, p)
                }
                x += widths[i]
            }
            w.y += headerH
        }

        // Title of the table together with the header and the first row stay on one page.
        if (w.y + 20f + headerH + 30f > PAGE_H - MARGIN - 14f) w.newPage()
        w.canvas.drawText(t.title, MARGIN, w.y + 12f, paint(12f, true, teal))
        w.y += 18f
        drawHeader()

        t.rows.forEachIndexed { idx, row ->
            val p = if (row.bold) bodyBold else body
            val cellLines = row.cells.mapIndexed { i, text ->
                wrap(text.ifBlank { "" }, p, widths[i] - 2 * PAD)
            }
            val h = (cellLines.maxOf { maxOf(it.size, 1) }) * LINE + 2 * PAD
            if (w.y + h > PAGE_H - MARGIN - 14f) {
                w.newPage()
                drawHeader()
            }
            var x = MARGIN
            row.cells.indices.forEach { i ->
                fill.color = if (row.bold) summary else if (idx % 2 == 1) zebra else Color.WHITE
                w.canvas.drawRect(x, w.y, x + widths[i], w.y + h, fill)
                w.canvas.drawRect(x, w.y, x + widths[i], w.y + h, stroke)
                // All text in the table is centred.
                val cp = Paint(p).apply { textAlign = Paint.Align.CENTER }
                val tx = x + widths[i] / 2f
                cellLines[i].forEachIndexed { k, line ->
                    w.canvas.drawText(line, tx, w.y + PAD + LINE * (k + 1) - 2f, cp)
                }
                x += widths[i]
            }
            w.y += h
        }
        w.y += 16f
    }

    /** Narrow columns take what their text needs; medicines and notes share what is left. */
    private fun columnWidths(t: ReportTable): List<Float> {
        val avail = PAGE_W - 2 * MARGIN
        val n = t.headers.size
        val natural = FloatArray(n) { i ->
            val texts = ArrayList<Pair<String, Paint>>()
            t.headers[i].split("\n").forEach { texts.add(it to head) }
            t.rows.forEach { texts.add(it.cells[i] to if (it.bold) bodyBold else body) }
            texts.maxOf { (s, p) -> p.measureText(s) } + 2 * PAD + 2f
        }
        val w = FloatArray(n) { i -> if (i in t.wide) natural[i].coerceIn(60f, 170f) else natural[i] }
        val sum = w.sum()
        if (sum > avail) {
            // First shrink the wide columns, then everything if that is not enough.
            val wideSum = t.wide.sumOf { w[it].toDouble() }.toFloat()
            val fixed = sum - wideSum
            if (t.wide.isNotEmpty() && avail - fixed > 60f * t.wide.size) {
                val k = (avail - fixed) / wideSum
                t.wide.forEach { w[it] = w[it] * k }
            } else {
                val k = avail / sum
                for (i in 0 until n) w[i] = w[i] * k
            }
        } else if (sum < avail) {
            val extra = avail - sum
            val targets = if (t.wide.isNotEmpty()) t.wide.toList() else (0 until n).toList()
            targets.forEach { w[it] += extra / targets.size }
        }
        return w.toList()
    }

    /** Greedy word wrap; a word longer than the line is cut. */
    private fun wrap(text: String, p: Paint, maxW: Float): List<String> {
        if (text.isEmpty()) return listOf("")
        val out = ArrayList<String>()
        var line = ""
        for (word in text.split(" ")) {
            var wd = word
            while (p.measureText(wd) > maxW && wd.length > 1) {
                val fit = p.breakText(wd, true, maxW, null).coerceAtLeast(1)
                if (line.isNotEmpty()) {
                    out.add(line)
                    line = ""
                }
                out.add(wd.substring(0, fit))
                wd = wd.substring(fit)
            }
            val candidate = if (line.isEmpty()) wd else "$line $wd"
            if (p.measureText(candidate) <= maxW || line.isEmpty()) {
                line = candidate
            } else {
                out.add(line)
                line = wd
            }
        }
        if (line.isNotEmpty()) out.add(line)
        return out.ifEmpty { listOf("") }
    }
}
