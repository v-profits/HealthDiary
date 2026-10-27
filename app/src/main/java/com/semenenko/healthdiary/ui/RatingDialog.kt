package com.semenenko.healthdiary.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.semenenko.healthdiary.R
import com.semenenko.healthdiary.util.RatingPolicy

/**
 * Asks for 1-5 stars. 4-5 open the RuStore page of the app, 1-3 open a letter to the developer.
 * [onRated] is called only when the page or the mail app was really opened.
 */
@Composable
fun RatingDialog(onRated: () -> Unit, onLater: () -> Unit) {
    val ctx = LocalContext.current
    var stars by rememberSaveable { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onLater,
        title = { Text(stringResource(R.string.rate_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.rate_text), textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 1..5) {
                        IconButton(onClick = { stars = i }) {
                            Icon(
                                if (i <= stars) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = stringResource(R.string.rate_star_desc, i),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                if (stars > 0) {
                    Text(
                        stringResource(
                            if (RatingPolicy.goesToStore(stars)) R.string.rate_hint_store
                            else R.string.rate_hint_mail
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            ChipButton(
                text = stringResource(R.string.rate_send),
                enabled = stars > 0,
                selected = true,
                onClick = {
                    val opened = if (RatingPolicy.goesToStore(stars)) openStore(ctx) else writeToDeveloper(ctx, stars)
                    if (opened) onRated()
                    else Toast.makeText(ctx, R.string.rate_no_app, Toast.LENGTH_LONG).show()
                }
            )
        },
        dismissButton = {
            ChipButton(text = stringResource(R.string.rate_later), onClick = onLater)
        }
    )
}

private fun openStore(ctx: Context): Boolean {
    val pkg = ctx.packageName
    val links = listOf(
        "rustore://apps.rustore.ru/app/$pkg", // RuStore app
        "https://www.rustore.ru/catalog/app/$pkg" // web page as a fallback
    )
    for (link in links) {
        try {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
            return true
        } catch (e: ActivityNotFoundException) {
            // try the next link
        }
    }
    return false
}

private fun writeToDeveloper(ctx: Context, stars: Int): Boolean {
    val version = try {
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: ""
    } catch (e: Exception) {
        ""
    }
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(RatingPolicy.DEV_EMAIL))
        putExtra(
            Intent.EXTRA_SUBJECT,
            ctx.getString(R.string.rate_mail_subject, ctx.getString(R.string.app_name))
        )
        putExtra(Intent.EXTRA_TEXT, ctx.getString(R.string.rate_mail_body, stars, version))
    }
    return try {
        ctx.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
