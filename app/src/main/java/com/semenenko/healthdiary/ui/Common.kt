package com.semenenko.healthdiary.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.semenenko.healthdiary.R
import com.semenenko.healthdiary.util.capitalizeSentences
import com.semenenko.healthdiary.util.endsSentence
import com.semenenko.healthdiary.util.firstNumber

@Composable
fun ConfirmDialog(
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = {
            ChipButton(
                text = confirmText ?: stringResource(R.string.btn_delete),
                onClick = onConfirm,
                danger = confirmText == null
            )
        },
        dismissButton = {
            ChipButton(text = stringResource(R.string.btn_cancel), onClick = onDismiss)
        }
    )
}

/**
 * One look for every button of the app: the same chip as "Morning / Evening".
 * [selected] fills it (main action or chosen option), [danger] makes the text red.
 */
@Composable
fun ChipButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    danger: Boolean = false,
    enabled: Boolean = true,
    stretch: Boolean = false,
    icon: ImageVector? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = {
            Box(
                modifier = if (stretch) Modifier.fillMaxWidth() else Modifier,
                contentAlignment = Alignment.Center
            ) {
                // The icon stays at the left edge, the text is centred in the whole chip.
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (danger) MaterialTheme.colorScheme.error else LocalContentColor.current,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (-6).dp)
                            .size(20.dp)
                    )
                }
                Text(
                    text,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    // Room for the icon when the chip is not stretched
                    modifier = if (icon != null && !stretch) Modifier.padding(start = 22.dp) else Modifier
                )
            }
        },
        modifier = modifier.heightIn(min = 44.dp),
        shape = CircleShape,
        colors = if (danger) {
            FilterChipDefaults.filterChipColors(labelColor = MaterialTheme.colorScheme.error)
        } else {
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    )
}

/** Pill-shaped choice chip (type, time of day, period, medicines): filled with the accent when chosen. */
@Composable
fun AppChip(selected: Boolean, onClick: () -> Unit, text: String) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = Modifier.heightIn(min = 40.dp),
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

enum class FieldKind { TEXT, DECIMAL, INTEGER }

/**
 * Text field with a microphone icon on the right. The icon opens the system speech recognizer.
 * Text fields: the spoken text is added after the text already in the field; it starts with a
 * capital letter only in an empty field or after ". ! ?", and has capitals after ". ! ?"; a cross appears when the field is not empty
 * and clears it. Numeric fields: only the first number of the phrase is kept and it replaces
 * the value.
 */
@Composable
fun VoiceField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    kind: FieldKind = FieldKind.TEXT,
    singleLine: Boolean = true,
    placeholder: String? = null
) {
    val ctx = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val heard = res.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!heard.isNullOrBlank()) {
                if (kind == FieldKind.TEXT) {
                    // Continues the sentence: no capital unless the text before ends with . ! ?
                    val spoken = capitalizeSentences(heard, endsSentence(value))
                    onChange(if (value.isBlank()) spoken else value.trimEnd() + " " + spoken)
                } else {
                    val number = firstNumber(heard, kind == FieldKind.INTEGER)
                    if (number != null) {
                        onChange(number)
                    } else {
                        Toast.makeText(ctx, R.string.voice_not_number, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun startVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                ctx.resources.configuration.locales[0].toLanguageTag()
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, label)
        }
        try {
            launcher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(ctx, R.string.voice_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = { s ->
            val allowed = when (kind) {
                FieldKind.TEXT -> true
                FieldKind.INTEGER -> s.all { it.isDigit() }
                FieldKind.DECIMAL -> s.all { it.isDigit() || it == '.' || it == ',' }
            }
            if (allowed) onChange(s)
        },
        label = { Text(label) },
        placeholder = if (placeholder != null) {
            { Text(placeholder) }
        } else {
            null
        },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = when (kind) {
                FieldKind.TEXT -> KeyboardType.Text
                FieldKind.INTEGER -> KeyboardType.Number
                FieldKind.DECIMAL -> KeyboardType.Decimal
            }
        ),
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // The cross is only in text fields (numbers are short, backspace is enough)
                if (kind == FieldKind.TEXT && value.isNotEmpty()) {
                    IconButton(onClick = { onChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear_field)
                        )
                    }
                }
                IconButton(onClick = { startVoice() }) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = stringResource(R.string.btn_voice_desc)
                    )
                }
            }
        },
        modifier = modifier
    )
}
