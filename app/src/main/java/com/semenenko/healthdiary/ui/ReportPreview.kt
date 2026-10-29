package com.semenenko.healthdiary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.semenenko.healthdiary.report.ReportData
import com.semenenko.healthdiary.report.ReportTable

/** What the doctor will get, as tables: every value has its own column. */
@Composable
fun ReportPreview(data: ReportData, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            data.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(data.period, style = MaterialTheme.typography.bodySmall)
        data.tables.forEach { PreviewTable(it) }
        Text(
            data.footer,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun PreviewTable(t: ReportTable) {
    // Column width follows the longest text, so numbers stay narrow and notes get room.
    val widths = t.headers.indices.map { i ->
        val longest = (t.rows.map { it.cells[i].length } + t.headers[i].split("\n").map { it.length })
            .max().coerceIn(4, 24)
        (longest * 7.5f + 16f).dp
    }
    Text(
        t.title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
    Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        Row(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
            t.headers.forEachIndexed { i, h ->
                Text(
                    h,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(widths[i])
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
        t.rows.forEachIndexed { idx, row ->
            Row(
                modifier = Modifier
                    .background(
                        when {
                            row.bold -> MaterialTheme.colorScheme.primaryContainer
                            idx % 2 == 1 -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.cells.forEachIndexed { i, c ->
                    Text(
                        c,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (row.bold) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(widths[i])
                            .padding(horizontal = 4.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
