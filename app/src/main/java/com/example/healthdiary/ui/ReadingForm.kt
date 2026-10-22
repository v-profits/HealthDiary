package com.example.healthdiary.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.healthdiary.R
import com.example.healthdiary.data.DayPart
import com.example.healthdiary.data.Medication
import com.example.healthdiary.data.Reading
import com.example.healthdiary.data.ReadingType
import com.example.healthdiary.util.MEDS_SEP
import com.example.healthdiary.util.Validation
import com.example.healthdiary.util.defaultDayPart
import com.example.healthdiary.util.fmt
import com.example.healthdiary.util.medLabel
import com.example.healthdiary.util.partLabelRes
import com.example.healthdiary.util.splitMeds
import com.example.healthdiary.util.toNum
import com.example.healthdiary.util.typeLabelRes
import com.example.healthdiary.util.unitRes
import com.example.healthdiary.voice.VoiceParser

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReadingForm(
    initial: Reading?,
    defaultType: ReadingType,
    meds: List<Medication>,
    onSave: (Reading) -> Unit,
    onCancel: () -> Unit
) {
    var type by rememberSaveable { mutableStateOf(initial?.type ?: defaultType) }
    var dayPart by rememberSaveable { mutableStateOf(initial?.dayPart ?: defaultDayPart()) }
    var v1 by rememberSaveable { mutableStateOf(initial?.let { fmt(it.v1) } ?: "") }
    var v2 by rememberSaveable { mutableStateOf(initial?.v2?.let { fmt(it) } ?: "") }
    var pulse by rememberSaveable { mutableStateOf(initial?.pulse?.toString() ?: "") }
    var note by rememberSaveable { mutableStateOf(initial?.note ?: "") }
    var taken by rememberSaveable { mutableStateOf(initial?.meds ?: "") }
    var quick by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }

    // Typing or dictating a whole phrase fills the fields below as soon as it is understood.
    fun onQuick(text: String) {
        quick = text
        val p = VoiceParser.parse(text, type) ?: return
        type = p.type
        v1 = p.v1?.let { fmt(it) } ?: ""
        v2 = p.v2?.let { fmt(it) } ?: ""
        pulse = p.pulse?.toString() ?: ""
        showError = false
    }

    val quickFailed = quick.isNotBlank() && VoiceParser.parse(quick, type) == null

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
                    note = note.trim(),
                    dayPart = dayPart,
                    meds = taken
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

            // Morning / noon / evening / night; wraps to a second line on a narrow screen.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DayPart.values().forEach { part ->
                    FilterChip(
                        selected = dayPart == part,
                        onClick = { dayPart = part },
                        label = { Text(stringResource(partLabelRes(part))) }
                    )
                }
            }

            VoiceField(
                value = quick,
                onChange = { onQuick(it) },
                label = stringResource(R.string.quick_label),
                placeholder = stringResource(R.string.quick_placeholder),
                modifier = Modifier.fillMaxWidth()
            )
            if (quickFailed) {
                Text(
                    stringResource(R.string.quick_not_parsed),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (type == ReadingType.PRESSURE) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VoiceField(
                        value = v1,
                        onChange = { v1 = it },
                        label = stringResource(R.string.field_systolic),
                        kind = FieldKind.INTEGER,
                        modifier = Modifier.weight(1f)
                    )
                    VoiceField(
                        value = v2,
                        onChange = { v2 = it },
                        label = stringResource(R.string.field_diastolic),
                        kind = FieldKind.INTEGER,
                        modifier = Modifier.weight(1f)
                    )
                }
                VoiceField(
                    value = pulse,
                    onChange = { pulse = it },
                    label = stringResource(R.string.field_pulse),
                    kind = FieldKind.INTEGER,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                VoiceField(
                    value = v1,
                    onChange = { v1 = it },
                    label = stringResource(typeLabelRes(type)) + " (" +
                        stringResource(unitRes(type)) + ")",
                    kind = FieldKind.DECIMAL,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Medicines taken: chosen from the list on the "Medicines" tab.
            Text(
                stringResource(R.string.field_meds),
                style = MaterialTheme.typography.labelLarge
            )
            val selected = splitMeds(taken)
            val labels = (meds.map { medLabel(it) } + selected).distinct()
            if (labels.isEmpty()) {
                Text(
                    stringResource(R.string.meds_pick_empty),
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    labels.forEach { label ->
                        FilterChip(
                            selected = label in selected,
                            onClick = {
                                val next = if (label in selected) selected - label
                                else selected + label
                                taken = next.joinToString(MEDS_SEP)
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            VoiceField(
                value = note,
                onChange = { note = it },
                label = stringResource(R.string.field_note),
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
                ChipButton(
                    text = stringResource(R.string.btn_cancel),
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    stretch = true
                )
                ChipButton(
                    text = stringResource(R.string.btn_save),
                    onClick = { save() },
                    modifier = Modifier.weight(1f),
                    selected = true,
                    stretch = true
                )
            }
        }
    }
}
