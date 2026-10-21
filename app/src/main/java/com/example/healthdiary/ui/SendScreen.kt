package com.example.healthdiary.ui

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
import androidx.compose.material3.FilterChip
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
import com.example.healthdiary.R
import com.example.healthdiary.data.Medication
import com.example.healthdiary.data.Reading
import com.example.healthdiary.report.ReportBuilder

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

    val report = remember(readings, meds, name, days) {
        ReportBuilder.build(ctx, name, days, readings, meds)
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

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                7 to R.string.period_7,
                14 to R.string.period_14,
                30 to R.string.period_30,
                0 to R.string.period_all
            ).forEach { (value, label) ->
                FilterChip(
                    selected = days == value,
                    onClick = { days = value },
                    label = { Text(stringResource(label)) }
                )
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = report,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { shareReport(ctx, report) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Text(
                    text = stringResource(R.string.btn_share),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            OutlinedButton(onClick = { copyReport(ctx, report) }) {
                Text(stringResource(R.string.btn_copy))
            }
        }

        Text(
            text = stringResource(R.string.disclaimer),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun shareReport(ctx: Context, report: String) {
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
