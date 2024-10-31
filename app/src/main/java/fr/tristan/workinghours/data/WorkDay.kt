package fr.tristan.workinghours.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import fr.tristan.workinghours.ui.screen.home.normalizeDate
import fr.tristan.workinghours.ui.screen.home.normalizeTime
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@Entity(tableName = "work_day")
data class WorkDay (
    @PrimaryKey
    val date: Date = Date.from(Instant.EPOCH),
    var workIn: Date = Date.from(Instant.EPOCH),
    var workOut: Date = Date.from(Instant.EPOCH),
    var lunchIn: Date = Date.from(Instant.EPOCH),
    var lunchOut: Date = Date.from(Instant.EPOCH),
)

fun WorkDay.getWorkTime(): Date {
    val workTime = this.getWorkTimeInSecond()
    val calendar = Calendar.getInstance()
    calendar.timeZone = TimeZone.getTimeZone("GMT")

    calendar.set(Calendar.HOUR_OF_DAY, workTime / 3600)
    calendar.set(Calendar.MINUTE, (workTime % 3600) / 60)
    calendar.set(Calendar.SECOND, workTime % 60)

    return normalizeTime(calendar.time)
}

fun WorkDay.getWorkTimeInSecond(): Int {
    if (workIn.time == 0L || workOut.time == 0L || lunchIn.time == 0L || lunchOut.time == 0L) {
        return 0
    }

    val calendar = Calendar.getInstance()
    calendar.timeZone = TimeZone.getTimeZone("GMT")

    calendar.time = workIn
    val workIn = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(
        Calendar.SECOND)

    calendar.time = workOut
    val workOut = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(
        Calendar.SECOND)

    calendar.time = lunchIn
    val lunchIn = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(
        Calendar.SECOND)

    calendar.time = lunchOut
    val lunchOut = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(
        Calendar.SECOND)

    val workTime = lunchIn - workIn + workOut - lunchOut
    return workTime
}