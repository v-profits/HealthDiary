package com.example.healthdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.healthdiary.ui.DiaryApp
import com.example.healthdiary.ui.HealthDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthDiaryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DiaryApp()
                }
            }
        }
    }
}
