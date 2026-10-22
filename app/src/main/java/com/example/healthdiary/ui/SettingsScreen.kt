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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
    R.string.help_7_title to R.string.help_7_text,
    R.string.help_8_title to R.string.help_8_text
)

/** The user guide text. Shown in Settings and in a dialog opened from the top menu. */
@Composable
fun HelpSections() {
    Column {
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
        ChoiceButtons(
            options = listOf(
                0 to stringResource(R.string.theme_system),
                1 to stringResource(R.string.theme_light),
                2 to stringResource(R.string.theme_dark)
            ),
            selected = themeMode,
            onSelect = onThemeChange
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium
        )
        ChoiceButtons(
            options = listOf(
                "" to stringResource(R.string.lang_system),
                "ru" to stringResource(R.string.lang_ru),
                "en" to stringResource(R.string.lang_en)
            ),
            selected = language,
            onSelect = { value ->
                if (value != language) {
                    onLanguageChange(value)
                    // The language is applied when the screen is created again.
                    ctx.findActivity()?.recreate()
                }
            }
        )

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
                if (helpOpen) HelpSections()
            }
        }

        Text(
            text = stringResource(R.string.disclaimer),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/** A row of equal chip buttons, the same look as "Morning / Evening"; the chosen one is filled. */
@Composable
private fun <T> ChoiceButtons(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            ChipButton(
                text = label,
                onClick = { onSelect(value) },
                modifier = Modifier.weight(1f),
                selected = value == selected
            )
        }
    }
}
