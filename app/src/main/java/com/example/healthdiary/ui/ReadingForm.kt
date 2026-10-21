package com.example.healthdiary.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.healthdiary.R
import com.example.healthdiary.data.Reading
import com.example.healthdiary.data.ReadingType
import com.example.healthdiary.util.Validation
import com.example.healthdiary.util.fmt
import com.example.healthdiary.util.toNum
import com.example.healthdiary.util.typeLabelRes
import com.example.healthdiary.util.unitRes
import com.example.healthdiary.voice.VoiceParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingForm(initial: Reading?, onSave: (Reading) -> Unit, onCancel: () -> Unit) {
    val ctx = LocalContext.current

    var type by rememberSaveable { mutableStateOf(initial?.type ?: ReadingType.PRESSURE) }
    var v1 by rememberSaveable { mutableStateOf(initial?.let { fmt(it.v1) } ?: "") }
    var v2 by rememberSaveable { mutableStateOf(initial?.v2?.let { fmt(it) } ?: "") }
    var pulse by rememberSaveable { mutableStateOf(initial?.pulse?.toString() ?: "") }
    var note by rememberSaveable { mutableStateOf(initial?.note ?: "") }
    var quick by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var showError by rememberSaveable { mutableStateOf(false) }

    fun fill(text: String) {
        val p = VoiceParser.parse(text)
        if (p == null) {
            message = ctx.getString(R.string.voice_not_understood)
            return
        }
        type = p.type
        v1 = p.v1?.let { fmt(it) } ?: ""
        v2 = p.v2?.let { fmt(it) } ?: ""
        pulse = p.pulse?.toString() ?: ""
        message = ctx.getString(R.string.heard, text)
        showError = false
    }

    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val text = res.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (text != null) {
                quick = text
                fill(text)
            }
        }
    }

    fun startVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, ctx.getString(R.string.voice_prompt))
        }
        try {
            speech.launch(intent)
        } catch (e: ActivityNotFoundException) {
            message = ctx.getString(R.string.voice_unavailable)
        }
    }

    fun save() {
        val a = toNum(v1)
        val b = if (type == ReadingType.PRESSURE) toNum(v2) else null
        if (a != null && Validation.ok(type, a, b) && Validation.pulseOk(pulse)) {
            onSave(
                Reading(
                    id = initial?.id ?: 0,
                    timestamp = initial?.timestamp ?: System.currentTimeMillis(),
                    type = type,
                    v1 = a,
                    v2 = b,
                    pulse = if (type == ReadingType.PRESSURE) pulse.trim().toIntOrNull() else null,
                    note = note.trim()
                )
            )
        } else {
            showError = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (initial == null) R.string.add_reading else R.string.edit_reading
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.btn_cancel)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReadingType.values().forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        label = { Text(stringResource(typeLabelRes(t))) }
                    )
                }
            }

            OutlinedTextField(
                value = quick,
                onValueChange = { quick = it },
                label = { Text(stringResource(R.string.quick_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { startVoice() }) {
                    Text("🎤 " + stringResource(R.string.btn_mic))
                }
                OutlinedButton(onClick = { fill(quick) }) {
                    Text(stringResource(R.string.btn_fill))
                }
            }
            message?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            if (type == ReadingType.PRESSURE) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = v1,
                        onChange = { v1 = it },
                        label = stringResource(R.string.field_systolic),
                        modifier = Modifier.weight(1f)
                    )
                    NumberField(
                        value = v2,
                        onChange = { v2 = it },
                        label = stringResource(R.string.field_diastolic),
                        modifier = Modifier.weight(1f)
                    )
                }
                NumberField(
                    value = pulse,
                    onChange = { pulse = it },
                    label = stringResource(R.string.field_pulse),
                    modifier = Modifier.fillMaxWidth(),
                    integerOnly = true
                )
            } else {
                NumberField(
                    value = v1,
                    onChange = { v1 = it },
                    label = stringResource(typeLabelRes(type)) + " (" +
                        stringResource(unitRes(type)) + ")",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.field_note)) },
                modifier = Modifier.fillMaxWidth()
            )

            if (showError) {
                Text(
                    stringResource(R.string.error_value),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.btn_cancel))
                }
                Button(onClick = { save() }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.btn_save))
                }
            }
        }
    }
}
