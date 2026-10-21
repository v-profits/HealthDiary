package com.example.healthdiary

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthdiary.ui.DiaryApp
import com.example.healthdiary.ui.DiaryViewModel
import com.example.healthdiary.ui.HealthDiaryTheme
import com.example.healthdiary.util.LocaleHelper

class MainActivity : ComponentActivity() {

    // Applies the language chosen in Settings (system / Russian / English).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: DiaryViewModel = viewModel()
            val themeMode by vm.themeMode.collectAsState()
            HealthDiaryTheme(themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DiaryApp(vm)
                }
            }
        }
    }
}
