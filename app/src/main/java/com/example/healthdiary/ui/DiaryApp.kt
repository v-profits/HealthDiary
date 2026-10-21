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
import com.example.healthdiary.R
import com.example.healthdiary.data.ReadingType

@Composable
fun DiaryApp(vm: DiaryViewModel) {
    val readings by vm.readings.collectAsState()
    val meds by vm.medications.collectAsState()
    val patientName by vm.patientName.collectAsState()
    val themeMode by vm.themeMode.collectAsState()
    val language by vm.language.collectAsState()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    // null = form closed, 0 = new reading, other = id of the reading being edited
    var formId by rememberSaveable { mutableStateOf<Long?>(null) }
    // measurement type shown in the table; a new reading starts with this type
    var listType by rememberSaveable { mutableStateOf(ReadingType.PRESSURE) }

    if (formId != null) {
        BackHandler { formId = null }
        ReadingForm(
            initial = readings.firstOrNull { it.id == formId },
            defaultType = listType,
            meds = meds,
            onSave = {
                vm.saveReading(it)
                listType = it.type
                formId = null
            },
            onCancel = { formId = null }
        )
    } else {
        val tabs = listOf(
            "📋" to R.string.tab_readings,
            "💊" to R.string.tab_meds,
            "📤" to R.string.tab_send,
            "⚙️" to R.string.tab_settings
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
                        type = listType,
                        onTypeChange = { listType = it },
                        onAdd = { formId = 0L },
                        onEdit = { formId = it.id },
                        onDelete = vm::deleteReading
                    )
                    1 -> MedsScreen(
                        meds = meds,
                        onSave = vm::saveMedication,
                        onDelete = vm::deleteMedication
                    )
                    2 -> SendScreen(
                        readings = readings,
                        meds = meds,
                        patientName = patientName,
                        onNameChange = vm::setPatientName
                    )
                    else -> SettingsScreen(
                        themeMode = themeMode,
                        onThemeChange = vm::setThemeMode,
                        language = language,
                        onLanguageChange = vm::setLanguage
                    )
                }
            }
        }
    }
}
