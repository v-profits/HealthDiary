package com.example.healthdiary.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.healthdiary.R
import com.example.healthdiary.data.Medication

// Column widths of the medicine table. It is swiped sideways when it is wider than the screen.
private val WName = 160.dp
private val WDose = 110.dp
private val WWhen = 150.dp
private val WNote = 170.dp
private val WAct = 48.dp

@Composable
fun MedsScreen(
    meds: List<Medication>,
    onSave: (Medication) -> Unit,
    onDelete: (Medication) -> Unit,
    onClear: () -> Unit
) {
    var editing by remember { mutableStateOf<Medication?>(null) }
    var toDelete by remember { mutableStateOf<Medication?>(null) }
    var askClear by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (meds.isEmpty()) {
                EmptyHint(stringResource(R.string.empty_meds))
            } else {
                MedsTable(
                    items = meds,
                    onEdit = { editing = it },
                    onDelete = { toDelete = it },
                    modifier = Modifier.weight(1f)
                )
                // Under the table; the height leaves room for the "+" button on the right.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 88.dp)
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Half of the screen wide, so it stays clear of the "+" button on the right.
                    ChipButton(
                        text = stringResource(R.string.btn_clear),
                        onClick = { askClear = true },
                        modifier = Modifier.fillMaxWidth(0.5f),
                        danger = true,
                        stretch = true
                    )
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

    if (askClear) {
        ConfirmDialog(
            text = stringResource(R.string.clear_meds_confirm, meds.size),
            onConfirm = {
                onClear()
                askClear = false
            },
            onDismiss = { askClear = false }
        )
    }
}

@Composable
private fun MedsTable(
    items: List<Medication>,
    onEdit: (Medication) -> Unit,
    onDelete: (Medication) -> Unit,
    modifier: Modifier
) {
    val total = WName + WDose + WWhen + WNote + WAct * 2

    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.width(total)) {
            Row(
                modifier = Modifier
                    .width(total)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderCell(stringResource(R.string.col_med_name), null, WName)
                HeaderCell(stringResource(R.string.col_med_dose), null, WDose)
                HeaderCell(stringResource(R.string.col_med_schedule), null, WWhen)
                HeaderCell(stringResource(R.string.col_note), null, WNote)
                Spacer(modifier = Modifier.width(WAct * 2))
            }
            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(items, key = { it.id }) { m ->
                    Row(
                        modifier = Modifier
                            .width(total)
                            .heightIn(min = 56.dp)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextCell(m.name, WName, MaterialTheme.typography.titleSmall)
                        TextCell(m.dose, WDose, MaterialTheme.typography.bodyMedium)
                        TextCell(
                            m.schedule,
                            WWhen,
                            MaterialTheme.typography.bodyMedium,
                            singleLine = true
                        )
                        TextCell(m.note, WNote, MaterialTheme.typography.bodySmall)
                        IconButton(onClick = { onEdit(m) }, modifier = Modifier.width(WAct)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_med)
                            )
                        }
                        IconButton(onClick = { onDelete(m) }, modifier = Modifier.width(WAct)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.btn_delete)
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun TextCell(
    text: String,
    width: Dp,
    style: androidx.compose.ui.text.TextStyle,
    singleLine: Boolean = false
) {
    Box(modifier = Modifier.width(width).padding(horizontal = 8.dp)) {
        Text(
            text,
            style = style,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis
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
                    capitalizeFirst = true,
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
            ChipButton(
                text = stringResource(R.string.btn_save),
                enabled = name.isNotBlank(),
                selected = true,
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
            )
        },
        dismissButton = {
            ChipButton(text = stringResource(R.string.btn_cancel), onClick = onDismiss)
        }
    )
}
