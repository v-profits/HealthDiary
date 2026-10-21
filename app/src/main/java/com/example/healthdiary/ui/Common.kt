package com.example.healthdiary.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.healthdiary.R
import com.example.healthdiary.util.firstNumber

@Composable
fun ConfirmDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.btn_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
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
 * Text field with a microphone icon on the right. The icon opens the system speech recognizer;
 * the result goes into the field. In numeric fields only the first number of the phrase is kept.
 * [appendVoice]: in text fields the spoken text is added after the existing text (true)
 * or replaces it (false).
 */
@Composable
fun VoiceField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    kind: FieldKind = FieldKind.TEXT,
    singleLine: Boolean = true,
    appendVoice: Boolean = true,
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
                    onChange(
                        if (appendVoice && value.isNotBlank()) value.trimEnd() + " " + heard
                        else heard
                    )
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
            IconButton(onClick = { startVoice() }) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = stringResource(R.string.btn_voice_desc)
                )
            }
        },
        modifier = modifier
    )
}
