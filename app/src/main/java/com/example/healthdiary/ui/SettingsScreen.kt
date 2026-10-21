package com.example.healthdiary.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.healthdiary.R

private fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** Pairs of (section title, section text) of the user guide. */
private val helpSections = listOf(
    R.string.help_1_title to R.string.help_1_text,
    R.string.help_2_title to R.string.help_2_text,
    R.string.help_3_title to R.string.help_3_text,
    R.string.help_4_title to R.string.help_4_text,
    R.string.help_5_title to R.string.help_5_text,
    R.string.help_6_title to R.string.help_6_text,
    R.string.help_7_title to R.string.help_7_text
)

@Composable
fun SettingsScreen(
    themeMode: Int,
    onThemeChange: (Int) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit
) {
    val ctx = LocalContext.current
    var helpOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium)
        listOf(
            0 to R.string.theme_system,
            1 to R.string.theme_light,
            2 to R.string.theme_dark
        ).forEach { (value, label) ->
            RadioRow(
                selected = themeMode == value,
                label = stringResource(label),
                onClick = { onThemeChange(value) }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium
        )
        listOf(
            "" to R.string.lang_system,
            "ru" to R.string.lang_ru,
            "en" to R.string.lang_en
        ).forEach { (value, label) ->
            RadioRow(
                selected = language == value,
                label = stringResource(label),
                onClick = {
                    if (language != value) {
                        onLanguageChange(value)
                        // The language is applied when the screen is created again.
                        ctx.findActivity()?.recreate()
                    }
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // User guide: tap the card to open or close it.
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { helpOpen = !helpOpen }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.settings_help),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(if (helpOpen) "▲" else "▼")
                }
                if (helpOpen) {
                    helpSections.forEach { (title, text) ->
                        Text(
                            stringResource(title),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            stringResource(text),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.disclaimer),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun RadioRow(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 12.dp))
    }
}
