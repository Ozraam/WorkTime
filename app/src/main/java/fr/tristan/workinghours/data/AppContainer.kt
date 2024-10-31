package fr.tristan.workinghours.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

interface AppContainer {
    val workDayRepository: WorkDayRepository
    val userPreferencesRepository: UserPreferencesRepository
    val leaveRepository: LeaveRepository
}

class DefaultAppContainer(applicationContext: Context) : AppContainer {
    override val workDayRepository: WorkDayRepository by lazy {
        OfflineWorkDayRepository(WorksDatabase.getDatabase(applicationContext).workDayDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(applicationContext.dataStore)
    }

    override val leaveRepository: LeaveRepository by lazy {
        WorkManagerLeaveRepository(applicationContext)
    }

}

private const val SETTINGS_PREFERENCE_NAME = "settings_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_PREFERENCE_NAME
)