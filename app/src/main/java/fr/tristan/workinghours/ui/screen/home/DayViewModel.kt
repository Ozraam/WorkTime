package fr.tristan.workinghours.ui.screen.home

import android.annotation.SuppressLint
import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.tristan.workinghours.WorkingHoursApplication
import fr.tristan.workinghours.data.LeaveRepository
import fr.tristan.workinghours.data.UserPreferencesRepository
import fr.tristan.workinghours.data.WorkDay
import fr.tristan.workinghours.data.WorkDayRepository
import fr.tristan.workinghours.ui.screen.settings.UiSettingsState
import fr.tristan.workinghours.worker.enabledNotifications
import fr.tristan.workinghours.worker.isNotificationsGranted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.Date

class DayViewModel(
    private val workDayRepository: WorkDayRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManagerLeaveRepository: LeaveRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DayUiState())
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    var userSearchInput by mutableStateOf("")

    val timeSettingsState: StateFlow<Int> =
        userPreferencesRepository.workTimeFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 25200
        )

    private val _uiSettingsState = MutableStateFlow(UiSettingsState())
    val uiSettingsState: StateFlow<UiSettingsState> = _uiSettingsState.asStateFlow()

    val dayListUiState = workDayRepository.getAll().map { DayListUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DayListUiState(listOf())
        )

    private fun setTimeToCurrent() {
        val calendar = java.util.Calendar.getInstance()

        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val second = calendar.get(java.util.Calendar.SECOND)

        _uiState.update {
            it.copy(
                hour = hour.toString().padStart(2, '0'),
                minute = minute.toString().padStart(2, '0'),
                second = second.toString().padStart(2, '0')
            )
        }
    }

    private fun detectTypeOfHour() {
        val date = normalizeDate(uiState.value.date)

        val workDay = dayListUiState.value.listOfDay.find { it.date == date }
        if (workDay == null) {
            _uiState.update {
                it.copy(
                    typeOfHour = HourType.WORK_IN
                )
            }

            return
        }

        if (workDay.lunchIn == Date(0)) {
            _uiState.update {
                it.copy(
                    typeOfHour = HourType.LUNCH_IN
                )
            }
        } else if (workDay.lunchOut == Date(0)) {
            _uiState.update {
                it.copy(
                    typeOfHour = HourType.LUNCH_OUT
                )
            }
        } else if (workDay.workOut == Date(0)) {
            _uiState.update {
                it.copy(
                    typeOfHour = HourType.WORK_OUT
                )
            }
        }
    }

    fun updateTime(hour: String, minute: String, second: String) {
        _uiState.update {
            it.copy(
                hour = hour,
                minute = minute,
                second = second
            )
        }
    }

    fun updateTypeOfHour(typeOfHour: HourType) {
        _uiState.update {
            it.copy(
                typeOfHour = typeOfHour
            )
        }
    }

    fun setUiSettingsTime() {
        viewModelScope.launch {
            userPreferencesRepository.workTimeFlow.collect { value ->
                _uiSettingsState.update {
                    it.copy(
                        userInputWorkTime = convertSecondToTimeString(value),
                        provisionalWorkTime = convertSecondToTimeString(value)
                    )
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun convertSecondToTimeString(time: Int): String {
        val hours = time / 3600
        val minutes = (time % 3600) / 60
        val seconds = (time % 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun updateSettingsTimePrev() {
        viewModelScope.launch {
            runBlocking {
                userPreferencesRepository.saveWorkTime(convertTimeStringToSecond(uiSettingsState.value.userInputWorkTime))
            }
            setUiSettingsTime()
        }
    }

    private fun convertTimeStringToSecond(time: String): Int {
        val timeParts = completeTimeStringIfNecessary(time).split(":")

        val hours = timeParts[0].toInt()
        val minutes = timeParts[1].toInt()
        val seconds = timeParts[2].toInt()

        return hours * 3600 + minutes * 60 + seconds
    }

    // final string should be in format hh:mm:ss
    // if not complete, the missing part will be filled with 0 and the current part is the higher unit example "7:00" -> "07:00:00"
    private fun completeTimeStringIfNecessary(time: String): String {
        val timeParts = time.split(":")

        val hours = timeParts[0]
        val minutes = timeParts.getOrNull(1) ?: "00"
        val seconds = timeParts.getOrNull(2) ?: "00"

        return "${hours.padStart(2, '0')}:${minutes.padStart(2, '0')}:${seconds.padStart(2, '0')}"
    }

    companion object {
        val Factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WorkingHoursApplication)
                DayViewModel(
                    application.container.workDayRepository,
                    application.container.userPreferencesRepository,
                    application.container.leaveRepository
                )
            }
        }
    }

    private fun getCurrentWorkDayIfExistOrNewOne() : Pair<WorkDay, Boolean> {
        val currentDate = getCurrentDay()

        // check if there is already a work day for today
        val day = dayListUiState.value.listOfDay.find { it.date == currentDate }

        val isNewDay = day == null

        val workDay = day ?: WorkDay(currentDate)

        return Pair(workDay, isNewDay)
    }

    private fun getWorkDayIfExistOrNewOne(date: Date) : Pair<WorkDay, Boolean> {
        val day = dayListUiState.value.listOfDay.find { it.date == date }

        val isNewDay = day == null

        val workDay = day ?: WorkDay(date)

        return Pair(workDay, isNewDay)
    }

    private fun getCurrentDay(): Date {
        return normalizeDate(Date())
    }

    private suspend fun saveWorkDay(workDay: WorkDay) {
        workDayRepository.insert(workDay)
    }

    private suspend fun updateWorkDay(workDay: WorkDay) {
        workDayRepository.update(workDay)
    }

    suspend fun saveAnHour() {
        val dateAndType = uiState.value.toDate()
        val (workDay, isNewDay) = getWorkDayIfExistOrNewOne(normalizeDate(uiState.value.date))

        when (dateAndType.typeOfHour) {
            HourType.WORK_IN -> workDay.workIn = dateAndType.date
            HourType.WORK_OUT -> workDay.workOut = dateAndType.date
            HourType.LUNCH_IN -> workDay.lunchIn = dateAndType.date
            HourType.LUNCH_OUT -> {
                if(workDay.date == getCurrentDay()) sendLeaveNotification()
                workDay.lunchOut = dateAndType.date
            }
        }

        if (isNewDay) {
            saveWorkDay(workDay)
        } else {
            updateWorkDay(workDay)
        }
    }

    fun updateProvisionalWorkTime(hour: String) {
        this._uiSettingsState.update {
            it.copy(
                userInputWorkTime = hour
            )
        }
    }

    fun getTodayWorkDay(): WorkDay? {
        // return dayListUiState.value.listOfDay.last()
        return dayListUiState.value.listOfDay.find { it.date == getCurrentDay() }
    }

    private fun sendLeaveNotification() {
        val todayWorkDay = getTodayWorkDay()

        val workOutTime = getPrevisionDate(todayWorkDay!!, timeSettingsState.value, 0)

        val delay = workOutTime.time - System.currentTimeMillis()
        Log.d("Delay", delay.toString())
        workManagerLeaveRepository.prepareLeave(delay)
    }

    fun enableNotification(context: Context) {
        enabledNotifications(context)
    }

    fun isNotificationEnabled(ctx: Context): Boolean {
        return isNotificationsGranted(ctx)
    }

    fun updateDate(selectedDateMillis: Long) {
        this._uiState.update {
            it.copy(
                date = Date(selectedDateMillis)
            )
        }
        detectTypeOfHour()
    }

    private fun resetDate() {
        this._uiState.update {
            it.copy(
                date = Date()
            )
        }
    }

    fun setupUiForAdd() {
        resetDate()
        setTimeToCurrent()
        detectTypeOfHour()
    }

    fun setupUiForAddWithDate(date: Date) {
        updateDate(date.time)
        setTimeToCurrent()
        detectTypeOfHour()
    }

    suspend fun deleteWorkDay(workDay: WorkDay) {
        workDayRepository.delete(workDay)
    }

    fun updateSearch(value: String) {
        userSearchInput = value
    }
}

enum class HourType(val title: String) {
    WORK_IN(title = "Work in"),
    WORK_OUT(title = "Work out"),
    LUNCH_IN(title = "Lunch in"),
    LUNCH_OUT(title = "Lunch out");
}

data class DayListUiState (val listOfDay: List<WorkDay>)
