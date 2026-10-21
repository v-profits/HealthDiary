package com.example.healthdiary.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthdiary.data.AppDb
import com.example.healthdiary.data.Medication
import com.example.healthdiary.data.Reading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).dao()
    private val prefs = app.getSharedPreferences("diary", Context.MODE_PRIVATE)

    val readings: StateFlow<List<Reading>> =
        dao.readings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val medications: StateFlow<List<Medication>> =
        dao.medications().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _patientName = MutableStateFlow(prefs.getString(KEY_NAME, "") ?: "")
    val patientName: StateFlow<String> = _patientName

    fun setPatientName(name: String) {
        _patientName.value = name
        prefs.edit().putString(KEY_NAME, name).apply()
    }

    fun saveReading(r: Reading) {
        viewModelScope.launch { dao.upsertReading(r) }
    }

    fun deleteReading(r: Reading) {
        viewModelScope.launch { dao.deleteReading(r) }
    }

    fun saveMedication(m: Medication) {
        viewModelScope.launch { dao.upsertMedication(m) }
    }

    fun deleteMedication(m: Medication) {
        viewModelScope.launch { dao.deleteMedication(m) }
    }

    private companion object {
        const val KEY_NAME = "patient_name"
    }
}
