package com.example.healthdiary.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthdiary.R

@Composable
fun DiaryApp(vm: DiaryViewModel = viewModel()) {
    val readings by vm.readings.collectAsState()
    val meds by vm.medications.collectAsState()
    val patientName by vm.patientName.collectAsState()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    // null = form closed, 0 = new reading, other = id of the reading being edited
    var formId by rememberSaveable { mutableStateOf<Long?>(null) }

    if (formId != null) {
        BackHandler { formId = null }
        ReadingForm(
            initial = readings.firstOrNull { it.id == formId },
            onSave = {
                vm.saveReading(it)
                formId = null
            },
            onCancel = { formId = null }
        )
    } else {
        val tabs = listOf(
            "📋" to R.string.tab_readings,
            "💊" to R.string.tab_meds,
            "📤" to R.string.tab_send
        )
        Scaffold(
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, (icon, label) ->
                        NavigationBarItem(
                            selected = tab == index,
                            onClick = { tab = index },
                            icon = { Text(icon) },
                            label = { Text(stringResource(label)) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                    0 -> ReadingsScreen(
                        readings = readings,
                        onAdd = { formId = 0L },
                        onEdit = { formId = it.id },
                        onDelete = vm::deleteReading
                    )
                    1 -> MedsScreen(
                        meds = meds,
                        onSave = vm::saveMedication,
                        onDelete = vm::deleteMedication
                    )
                    else -> SendScreen(
                        readings = readings,
                        meds = meds,
                        patientName = patientName,
                        onNameChange = vm::setPatientName
                    )
                }
            }
        }
    }
}
