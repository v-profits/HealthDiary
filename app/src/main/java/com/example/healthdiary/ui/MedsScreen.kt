package com.example.healthdiary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.healthdiary.R
import com.example.healthdiary.data.Medication

@Composable
fun MedsScreen(
    meds: List<Medication>,
    onSave: (Medication) -> Unit,
    onDelete: (Medication) -> Unit
) {
    var editing by remember { mutableStateOf<Medication?>(null) }
    var toDelete by remember { mutableStateOf<Medication?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (meds.isEmpty()) {
            EmptyHint(stringResource(R.string.empty_meds))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(meds, key = { it.id }) { m ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(m.name, style = MaterialTheme.typography.titleMedium)
                                val details = listOf(m.dose, m.schedule)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · ")
                                if (details.isNotEmpty()) {
                                    Text(details, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (m.note.isNotBlank()) {
                                    Text(m.note, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(onClick = { editing = m }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_med)
                                )
                            }
                            IconButton(onClick = { toDelete = m }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.btn_delete)
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { editing = Medication() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_med))
        }
    }

    editing?.let { current ->
        MedDialog(
            initial = current,
            onSave = {
                onSave(it)
                editing = null
            },
            onDismiss = { editing = null }
        )
    }

    toDelete?.let { m ->
        ConfirmDialog(
            text = stringResource(R.string.delete_confirm),
            onConfirm = {
                onDelete(m)
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun MedDialog(initial: Medication, onSave: (Medication) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var dose by remember { mutableStateOf(initial.dose) }
    var schedule by remember { mutableStateOf(initial.schedule) }
    var note by remember { mutableStateOf(initial.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (initial.id == 0L) R.string.new_med else R.string.edit_med))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VoiceField(
                    value = name,
                    onChange = { name = it },
                    label = stringResource(R.string.med_name),
                    modifier = Modifier.fillMaxWidth()
                )
                VoiceField(
                    value = dose,
                    onChange = { dose = it },
                    label = stringResource(R.string.med_dose),
                    modifier = Modifier.fillMaxWidth()
                )
                VoiceField(
                    value = schedule,
                    onChange = { schedule = it },
                    label = stringResource(R.string.med_schedule),
                    modifier = Modifier.fillMaxWidth()
                )
                VoiceField(
                    value = note,
                    onChange = { note = it },
                    label = stringResource(R.string.med_note),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        initial.copy(
                            name = name.trim(),
                            dose = dose.trim(),
                            schedule = schedule.trim(),
                            note = note.trim()
                        )
                    )
                }
            ) { Text(stringResource(R.string.btn_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    )
}
