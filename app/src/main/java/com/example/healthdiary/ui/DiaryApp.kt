package com.example.healthdiary.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.healthdiary.data.ReadingType
import com.example.healthdiary.util.typeLabelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryApp(vm: DiaryViewModel) {
    val ctx = LocalContext.current
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
    var menuOpen by remember { mutableStateOf(false) }
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }

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
        return
    }

    // Tab icons are text resources: change them in res/values/tab_icons.xml.
    val tabs = listOf(
        R.string.tab_icon_readings to R.string.tab_readings,
        R.string.tab_icon_meds to R.string.tab_meds,
        R.string.tab_icon_send to R.string.tab_send,
        R.string.tab_icon_settings to R.string.tab_settings
    )

    // The line under the app name tells what the current tab holds.
    val subtitle = when (tab) {
        0 -> stringResource(typeLabelRes(listType)) + " · " +
            stringResource(R.string.count_entries, readings.count { it.type == listType })
        1 -> stringResource(R.string.count_meds, meds.size)
        2 -> stringResource(R.string.subtitle_send)
        else -> stringResource(R.string.tab_settings)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.menu_more)
                            )
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_add_reading)) },
                                onClick = {
                                    menuOpen = false
                                    tab = 0
                                    formId = 0L
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_send)) },
                                onClick = {
                                    menuOpen = false
                                    tab = 2
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_help)) },
                                onClick = {
                                    menuOpen = false
                                    showHelp = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_settings)) },
                                onClick = {
                                    menuOpen = false
                                    tab = 3
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_about)) },
                                onClick = {
                                    menuOpen = false
                                    showAbout = true
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, (icon, label) ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Text(stringResource(icon)) },
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
                    onDelete = vm::deleteReading,
                    onClear = vm::clearReadings
                )
                1 -> MedsScreen(
                    meds = meds,
                    onSave = vm::saveMedication,
                    onDelete = vm::deleteMedication,
                    onClear = vm::clearMedications
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

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(stringResource(R.string.settings_help)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HelpSections()
                }
            },
            confirmButton = {
                ChipButton(
                    text = stringResource(R.string.btn_ok),
                    onClick = { showHelp = false },
                    selected = true
                )
            }
        )
    }

    if (showAbout) {
        val version = remember {
            try {
                ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: ""
            } catch (e: Exception) {
                ""
            }
        }
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(stringResource(R.string.app_name)) },
            text = {
                Column {
                    Text(stringResource(R.string.about_version, version))
                    Text(
                        stringResource(R.string.disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            },
            confirmButton = {
                ChipButton(
                    text = stringResource(R.string.btn_ok),
                    onClick = { showAbout = false },
                    selected = true
                )
            }
        )
    }
}
