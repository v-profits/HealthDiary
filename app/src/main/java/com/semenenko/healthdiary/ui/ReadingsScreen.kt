package com.semenenko.healthdiary.ui

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.semenenko.healthdiary.R
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

// Column widths of the table. The table is wider than most screens, so it is swiped sideways.
private val WDate = 76.dp
private val WPart = 80.dp
private val WNum = 72.dp
private val WSingle = 104.dp
private val WMeds = 170.dp
private val WNote = 150.dp
private val WAct = 48.dp

@Composable
fun ReadingsScreen(
    readings: List<Reading>,
    type: ReadingType,
    onTypeChange: (ReadingType) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Reading) -> Unit,
    onDelete: (Reading) -> Unit,
    onClear: (ReadingType) -> Unit
) {
    val shown = remember(readings, type) { readings.filter { it.type == type } }
    var toDelete by remember { mutableStateOf<Reading?>(null) }
    var askClear by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReadingType.values().forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { onTypeChange(t) },
                        label = { Text(stringResource(typeLabelRes(t))) }
                    )
                }
            }
            if (shown.isEmpty()) {
                EmptyHint(stringResource(R.string.empty_readings))
            } else {
                ReadingsTable(
                    items = shown,
                    type = type,
                    onEdit = onEdit,
                    onDelete = { toDelete = it },
                    modifier = Modifier.weight(1f)
                )
                // Under the table: removes all entries of the shown type at once.
                // The height leaves room for the "+" button on the right.
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
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_reading))
        }
    }

    toDelete?.let { r ->
        ConfirmDialog(
            text = stringResource(R.string.delete_confirm),
            onConfirm = {
                onDelete(r)
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }

    if (askClear) {
        ConfirmDialog(
            text = stringResource(
                R.string.clear_confirm,
                stringResource(typeLabelRes(type)),
                shown.size
            ),
            onConfirm = {
                onClear(type)
                askClear = false
            },
            onDismiss = { askClear = false }
        )
    }
}

@Composable
private fun ReadingsTable(
    items: List<Reading>,
    type: ReadingType,
    onEdit: (Reading) -> Unit,
    onDelete: (Reading) -> Unit,
    modifier: Modifier
) {
    val pressure = type == ReadingType.PRESSURE
    val total = WDate + WPart +
        (if (pressure) WNum * 3 else WSingle) +
        WMeds + WNote + WAct * 2
    val dateFmt = remember { SimpleDateFormat("dd.MM.yy", Locale.getDefault()) }
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.width(total)) {
            // Header: names and units; the cells below hold only values.
            Row(
                modifier = Modifier
                    .width(total)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderCell(stringResource(R.string.col_date), null, WDate)
                HeaderCell(stringResource(R.string.col_part), null, WPart)
                if (pressure) {
                    val mmHg = stringResource(R.string.unit_pressure)
                    HeaderCell(stringResource(R.string.col_systolic), mmHg, WNum)
                    HeaderCell(stringResource(R.string.col_diastolic), mmHg, WNum)
                    HeaderCell(
                        stringResource(R.string.col_pulse),
                        stringResource(R.string.unit_bpm),
                        WNum
                    )
                } else {
                    HeaderCell(
                        stringResource(typeLabelRes(type)),
                        stringResource(unitRes(type)),
                        WSingle
                    )
                }
                HeaderCell(stringResource(R.string.col_meds), null, WMeds)
                HeaderCell(stringResource(R.string.col_note), null, WNote)
                Spacer(modifier = Modifier.width(WAct * 2))
            }
            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(items, key = { it.id }) { r ->
                    Row(
                        modifier = Modifier
                            .width(total)
                            .heightIn(min = 56.dp)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date with the exact time in small font under it.
                        Column(modifier = Modifier.width(WDate).padding(horizontal = 8.dp)) {
                            Text(
                                dateFmt.format(Date(r.timestamp)),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                timeFmt.format(Date(r.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ValueCell(stringResource(partLabelRes(r.dayPart)), WPart, false)
                        if (pressure) {
                            ValueCell(fmt(r.v1), WNum, true)
                            ValueCell(r.v2?.let { fmt(it) } ?: "", WNum, true)
                            ValueCell(r.pulse?.toString() ?: "", WNum, true)
                        } else {
                            ValueCell(fmt(r.v1), WSingle, true)
                        }
                        // Medicines taken: name and dose, one per line.
                        Column(modifier = Modifier.width(WMeds).padding(horizontal = 8.dp)) {
                            splitMeds(r.meds).forEach { label ->
                                Text(label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Box(modifier = Modifier.width(WNote).padding(horizontal = 8.dp)) {
                            Text(r.note, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onEdit(r) }, modifier = Modifier.width(WAct)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_reading)
                            )
                        }
                        IconButton(onClick = { onDelete(r) }, modifier = Modifier.width(WAct)) {
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
internal fun HeaderCell(title: String, unit: String?, width: Dp) {
    Column(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        if (unit != null) {
            Text(
                unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ValueCell(text: String, width: Dp, big: Boolean) {
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.Center) {
        Text(
            text,
            style = if (big) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium
        )
    }
}
