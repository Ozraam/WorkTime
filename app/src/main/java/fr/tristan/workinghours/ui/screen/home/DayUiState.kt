package fr.tristan.workinghours.ui.screen.home

import java.util.Calendar
import java.util.Date

data class DayUiState(
    val date : Date = Date(),
    val hour : String = "",
    val minute : String = "",
    val second : String = "",
    val typeOfHour : HourType = HourType.WORK_IN,
)

data class DateAndType(
    val date : Date,
    val typeOfHour : HourType
)

fun DayUiState.toDate() : DateAndType {
    val calendar = Calendar.getInstance()

    val hour = this.hour.toInt()
    val minute = this.minute.toInt()
    val second = this.second.toInt()

    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, second)

    return DateAndType(
        date = calendar.time,
        typeOfHour = this.typeOfHour
    )
}