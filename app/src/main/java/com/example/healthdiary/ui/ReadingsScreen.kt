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
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.healthdiary.R
import com.example.healthdiary.data.Reading
import com.example.healthdiary.util.typeLabelRes
import com.example.healthdiary.util.valueText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReadingsScreen(
    readings: List<Reading>,
    onAdd: () -> Unit,
    onEdit: (Reading) -> Unit,
    onDelete: (Reading) -> Unit
) {
    val ctx = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    var toDelete by remember { mutableStateOf<Reading?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (readings.isEmpty()) {
            EmptyHint(stringResource(R.string.empty_readings))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(readings, key = { it.id }) { r ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(typeLabelRes(r.type)),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(valueText(ctx, r), style = MaterialTheme.typography.titleLarge)
                                Text(
                                    dateFormat.format(Date(r.timestamp)),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (r.note.isNotBlank()) {
                                    Text(r.note, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            IconButton(onClick = { onEdit(r) }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_reading)
                                )
                            }
                            IconButton(onClick = { toDelete = r }) {
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
}
