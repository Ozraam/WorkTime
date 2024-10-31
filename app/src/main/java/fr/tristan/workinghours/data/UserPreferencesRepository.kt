package fr.tristan.workinghours.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Date

open class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    open val workTimeFlow: Flow<Int> = dataStore.data
        .catch {
            if(it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[WORK_TIME] ?: 25200 // default to 7 hours in milliseconds
        }

    suspend fun saveWorkTime(workTime: Int) {
        dataStore.edit { preferences ->
            preferences[WORK_TIME] = workTime
        }
    }

    private companion object {
        val WORK_TIME = intPreferencesKey("work_time")
    }
}