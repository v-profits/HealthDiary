package com.semenenko.healthdiary

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.semenenko.healthdiary.ui.DiaryApp
import com.semenenko.healthdiary.ui.DiaryViewModel
import com.semenenko.healthdiary.ui.HealthDiaryTheme
import com.semenenko.healthdiary.util.LocaleHelper

class MainActivity : ComponentActivity() {

    // Applies the language chosen in Settings (system / Russian / English).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    private val vm: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Counts real openings only: a recreation (language change) has a saved state.
        if (savedInstanceState == null) vm.countLaunch()
        setContent {
            val themeMode by vm.themeMode.collectAsState()
            HealthDiaryTheme(themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DiaryApp(vm)
                }
            }
        }
    }
}
