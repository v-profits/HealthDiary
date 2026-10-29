package com.semenenko.healthdiary.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.semenenko.healthdiary.R
import com.semenenko.healthdiary.data.Medication
import com.semenenko.healthdiary.data.Reading
import com.semenenko.healthdiary.report.ReportBuilder
import com.semenenko.healthdiary.report.ReportData
import com.semenenko.healthdiary.report.ReportPdf
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SendScreen(
    readings: List<Reading>,
    meds: List<Medication>,
    patientName: String,
    onNameChange: (String) -> Unit
) {
    val ctx = LocalContext.current
    var days by rememberSaveable { mutableIntStateOf(7) }
    var name by remember { mutableStateOf(patientName) }

    val data = remember(readings, meds, name, days) {
        ReportBuilder.data(ctx, name, days, readings, meds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VoiceField(
            value = name,
            onChange = {
                name = it
                onNameChange(it)
            },
            label = stringResource(R.string.send_name),
            modifier = Modifier.fillMaxWidth()
        )

        SegmentedRow(
            options = listOf(
                7 to stringResource(R.string.period_7),
                14 to stringResource(R.string.period_14),
                30 to stringResource(R.string.period_30),
                0 to stringResource(R.string.period_all)
            ),
            selected = days,
            onSelect = { days = it },
            showCheck = true
        )

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            ReportPreview(data)
        }

        // The PDF has real columns; the text variant is for chats that cannot show a file.
        ChipButton(
            text = stringResource(R.string.btn_send_pdf),
            onClick = { sharePdf(ctx, data) },
            modifier = Modifier.fillMaxWidth(),
            selected = true,
            stretch = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipButton(
                text = stringResource(R.string.btn_send_text),
                onClick = { shareText(ctx, ReportBuilder.toText(ctx, data)) },
                modifier = Modifier.weight(1f),
                stretch = true
            )
            ChipButton(
                text = stringResource(R.string.btn_copy),
                onClick = { copyReport(ctx, ReportBuilder.toText(ctx, data)) },
                modifier = Modifier.weight(1f),
                stretch = true
            )
        }

        Text(
            text = stringResource(R.string.disclaimer),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun sharePdf(ctx: Context, data: ReportData) {
    try {
        val dir = File(ctx.cacheDir, "reports")
        dir.mkdirs()
        dir.listFiles()?.forEach { it.delete() }
        val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val file = File(dir, "dnevnik_$stamp.pdf")
        ReportPdf.write(ctx, data, file)
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(send, ctx.getString(R.string.share_via)))
    } catch (e: Exception) {
        Toast.makeText(ctx, R.string.pdf_failed, Toast.LENGTH_LONG).show()
    }
}

private fun shareText(ctx: Context, report: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report)
    }
    ctx.startActivity(Intent.createChooser(send, ctx.getString(R.string.share_via)))
}

private fun copyReport(ctx: Context, report: String) {
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("report", report))
    Toast.makeText(ctx, R.string.copied, Toast.LENGTH_SHORT).show()
}
