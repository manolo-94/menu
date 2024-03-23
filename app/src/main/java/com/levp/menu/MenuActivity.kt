package com.levp.menu

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.levp.menu.databinding.ActivityMenuBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private var firsTime: Boolean = true

    companion object{
        const val KEY_MODE_DARK = "mode_dark"
        const val KEY_BLUETOOTH = "bluetooth"
        const val KEY_VOLUME_LEVEL = "volumen_level"
        const val KEY_VIBRATION = "vibration"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CoroutineScope(Dispatchers.IO).launch {
            getSettings().filter { firsTime }.collect{ SettingsModel ->
                if (SettingsModel != null){
                    runOnUiThread{
                        binding.swDarkMode.isChecked = SettingsModel.darkMode
                        binding.swBluetooth.isChecked = SettingsModel.bluetooth
                        binding.rsVolume.setValues(SettingsModel.volume.toFloat())
                        binding.swVibration.isChecked = SettingsModel.vibration

                        firsTime = !firsTime
                    }
                }
            }
        }

        initUI()
    }

    private fun initUI() {

        binding.swDarkMode.setOnCheckedChangeListener { _, value ->

            if(value){
                enableDarkMode()
            }else{
                disableDarkMode()
            }

            CoroutineScope(Dispatchers.IO).launch {
                saveModeDark(value)
            }
        }

        binding.swBluetooth.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveBlueTooth(KEY_BLUETOOTH, value)
            }
        }

        binding.rsVolume.addOnChangeListener { _, value, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                saveVolume(value.toInt())
            }
        }

        binding.swVibration.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveVibration(KEY_VIBRATION, value)
            }
        }
    }

    private suspend fun saveModeDark(value:Boolean){

        dataStore.edit { preference ->
            preference[booleanPreferencesKey(KEY_MODE_DARK)] = value
        }
    }

    private suspend fun saveBlueTooth(key:String, value:Boolean){
        dataStore.edit { preference ->
            preference[booleanPreferencesKey(key)] = value
        }
    }

    private suspend fun saveVolume(value:Int){
        dataStore.edit { preference ->
            preference[intPreferencesKey(KEY_VOLUME_LEVEL)] = value
        }
    }

    private suspend fun saveVibration(key:String, value:Boolean){
        dataStore.edit { preference ->
            preference[booleanPreferencesKey(key)] = value
        }
    }

    private fun getSettings(): Flow<SettingsModel?> {
       return dataStore.data.map { preferences ->
            SettingsModel(
                darkMode = preferences[booleanPreferencesKey(KEY_MODE_DARK)] ?: false,
                bluetooth = preferences[booleanPreferencesKey(KEY_BLUETOOTH)] ?: true,
                volume = preferences[intPreferencesKey(KEY_VOLUME_LEVEL)] ?: 50,
                vibration = preferences[booleanPreferencesKey(KEY_VIBRATION)] ?: false
            )
        }
    }

    private fun enableDarkMode(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        delegate.applyDayNight()
    }

    private fun disableDarkMode(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        delegate.applyDayNight()
    }
}