package com.semenenko.healthdiary.ui

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.semenenko.healthdiary.R
import com.semenenko.healthdiary.data.AppDb
import com.semenenko.healthdiary.data.Medication
import com.semenenko.healthdiary.data.Reading
import com.semenenko.healthdiary.data.ReadingType
import com.semenenko.healthdiary.util.AppPrefs
import com.semenenko.healthdiary.util.Backup
import com.semenenko.healthdiary.util.RatingPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiaryViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).dao()
    private val prefs = AppPrefs.of(app)

    val readings: StateFlow<List<Reading>> =
        dao.readings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val medications: StateFlow<List<Medication>> =
        dao.medications().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _patientName = MutableStateFlow(prefs.getString(AppPrefs.KEY_NAME, "") ?: "")
    val patientName: StateFlow<String> = _patientName

    private val _themeMode = MutableStateFlow(prefs.getInt(AppPrefs.KEY_THEME, 0))
    val themeMode: StateFlow<Int> = _themeMode

    private val _language = MutableStateFlow(prefs.getString(AppPrefs.KEY_LANG, "") ?: "")
    val language: StateFlow<String> = _language

    private val _readingsView = MutableStateFlow(prefs.getInt(AppPrefs.KEY_VIEW, 0))
    val readingsView: StateFlow<Int> = _readingsView

    fun setReadingsView(v: Int) {
        _readingsView.value = v
        prefs.edit().putInt(AppPrefs.KEY_VIEW, v).apply()
    }

    private val _ratingDue = MutableStateFlow(false)

    /** True when the "rate the app" dialog must be shown now. */
    val ratingDue: StateFlow<Boolean> = _ratingDue

    /** Called once per real opening of the app (not on rotation or language change). */
    fun countLaunch() {
        if (prefs.getBoolean(AppPrefs.KEY_RATED, false)) return
        val n = prefs.getInt(AppPrefs.KEY_LAUNCHES, 0) + 1
        prefs.edit().putInt(AppPrefs.KEY_LAUNCHES, n).apply()
        _ratingDue.value = RatingPolicy.isDue(n)
    }

    /** "Later": asked again after the next 50 openings. */
    fun ratingLater() {
        _ratingDue.value = false
    }

    /** After a rating the dialog is never shown again. */
    fun ratingDone() {
        prefs.edit().putBoolean(AppPrefs.KEY_RATED, true).apply()
        _ratingDue.value = false
    }

    fun setPatientName(name: String) {
        _patientName.value = name
        prefs.edit().putString(AppPrefs.KEY_NAME, name).apply()
    }

    fun setThemeMode(mode: Int) {
        _themeMode.value = mode
        prefs.edit().putInt(AppPrefs.KEY_THEME, mode).apply()
    }

    /** Saved synchronously: the activity is recreated right after this call. */
    fun setLanguage(lang: String) {
        _language.value = lang
        prefs.edit().putString(AppPrefs.KEY_LANG, lang).commit()
    }

    /** Writes all data into the file the user chose. The result is shown as a short message. */
    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    val json = Backup.toJson(_patientName.value, dao.allReadings(), dao.allMedications())
                    getApplication<Application>().contentResolver.openOutputStream(uri, "wt")!!
                        .use { it.write(json.toByteArray(Charsets.UTF_8)) }
                    true
                } catch (e: Exception) {
                    false
                }
            }
            toast(if (ok) R.string.export_done else R.string.export_failed)
        }
    }

    /** Replaces all current data with the content of the chosen file. A bad file changes nothing. */
    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                try {
                    val text = getApplication<Application>().contentResolver.openInputStream(uri)!!
                        .use { String(it.readBytes(), Charsets.UTF_8) }
                    Backup.parse(text)
                } catch (e: Exception) {
                    null
                }
            }
            if (data == null) {
                toast(R.string.import_bad_file)
                return@launch
            }
            withContext(Dispatchers.IO) {
                dao.deleteAllReadings()
                dao.deleteAllMedications()
                dao.insertReadings(data.readings)
                dao.insertMedications(data.medications)
            }
            if (data.patientName.isNotBlank()) setPatientName(data.patientName)
            Toast.makeText(
                getApplication(),
                getApplication<Application>().getString(
                    R.string.import_done, data.readings.size, data.medications.size
                ),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun toast(res: Int) {
        Toast.makeText(getApplication(), res, Toast.LENGTH_LONG).show()
    }

    fun saveReading(r: Reading) {
        viewModelScope.launch { dao.upsertReading(r) }
    }

    fun deleteReading(r: Reading) {
        viewModelScope.launch { dao.deleteReading(r) }
    }

    fun clearReadings(type: ReadingType) {
        viewModelScope.launch { dao.deleteReadingsOfType(type) }
    }

    fun saveMedication(m: Medication) {
        viewModelScope.launch { dao.upsertMedication(m) }
    }

    fun deleteMedication(m: Medication) {
        viewModelScope.launch { dao.deleteMedication(m) }
    }

    /** Readings keep their own text of the medicines taken, so history is not affected. */
    fun clearMedications() {
        viewModelScope.launch { dao.deleteAllMedications() }
    }
}
