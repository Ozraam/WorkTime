package fr.tristan.workinghours.ui.screen.home

import android.annotation.SuppressLint
import android.content.Context
import fr.tristan.workinghours.R
import fr.tristan.workinghours.data.WorkDay
import fr.tristan.workinghours.data.getWorkTime
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Date

fun formatWeekToString(week: Pair<Date, Date>): String {
    val dateFormat = SimpleDateFormat.getDateInstance()
    return "${dateFormat.format(week.first)} - ${dateFormat.format(week.second)}"
}

// set hours, minute, second and millisecond to 0
fun normalizeDate(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun normalizeTime(date: Date) : Date {
    val calendarDay = Calendar.getInstance()
    calendarDay.time = date

    val calendar = Calendar.getInstance()
    calendar.time = Date.from(Instant.EPOCH)
    calendar.set(Calendar.HOUR_OF_DAY, calendarDay.get(Calendar.HOUR_OF_DAY))
    calendar.set(Calendar.MINUTE, calendarDay.get(Calendar.MINUTE))
    calendar.set(Calendar.SECOND, calendarDay.get(Calendar.SECOND))
    calendar.set(Calendar.MILLISECOND, calendarDay.get(Calendar.MILLISECOND))

    return calendar.time
}

fun getWeekRange(date: Date): Pair<Date, Date> {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

    val startOfWeek = calendar.time
    calendar.add(Calendar.DATE, 4)
    val endOfWeek = calendar.time
    return normalizeDate(startOfWeek) to normalizeDate(endOfWeek)
}

fun groupDatesByWeek(dates: List<WorkDay>): List<Pair<Pair<Date, Date>, List<WorkDay>>> {

    val groupedDates = dates.groupBy { date ->
        val weekRange = getWeekRange(date.date)
        weekRange
    }

    return groupedDates.map { (week, days) ->
        week to days.sortedBy { it.date }
    }.sortedBy { it.first.first }
}

fun getPrevision(day: WorkDay, provisionalTime: Int, weekOvertime: Int): String {
    val workIn = day.workIn
    val lunchIn = if(day.lunchIn.time == 0L) {
        val calendar = Calendar.getInstance()
        calendar.time = day.workIn
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 30)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.time
    } else day.lunchIn
    val lunchOut = if(day.lunchOut.time == 0L) {
        val calendar = Calendar.getInstance()
        calendar.time = day.workIn
        calendar.set(Calendar.HOUR_OF_DAY, 13)
        calendar.set(Calendar.MINUTE, 30)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.time
    } else day.lunchOut

    val calendar = Calendar.getInstance()
    calendar.time = workIn

    val workInInt =
        (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) * 60 +
                calendar.get(Calendar.SECOND)

    calendar.time = lunchIn
    val lunchInInt =
        (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) * 60 +
                calendar.get(Calendar.SECOND)

    calendar.time = lunchOut
    val lunchOutInt =
        (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) * 60 +
                calendar.get(Calendar.SECOND)

    val workOutInt = -lunchInInt + provisionalTime + lunchOutInt + workInInt - weekOvertime

    calendar.set(Calendar.HOUR_OF_DAY, workOutInt / 3600)
    calendar.set(Calendar.MINUTE,(workOutInt % 3600) / 60)
    calendar.set(Calendar.SECOND, workOutInt % 60)
    val format = SimpleDateFormat.getTimeInstance()
    return format.format(calendar.time)
}

fun formatTimeOrSayNotSetEpoch(date: Date, context: Context) : String {
    if (date.time == 0L) {
        return context.resources.getString(R.string.not_set)
    }

    val dateFormat = SimpleDateFormat.getTimeInstance()
    return dateFormat.format(date)
}

fun isDayFinished(day: WorkDay): Boolean {
    return !(day.workIn.time == 0L || day.workOut.time == 0L || day.lunchIn.time == 0L || day.lunchOut.time == 0L)
}

fun getDaySummary(day: WorkDay): String {
    val dayTime = day.getWorkTime()

    return dayTime.toHoursMinutesSeconds()
}

@SuppressLint("DefaultLocale")
fun Date.toHoursMinutesSeconds(): String {
    val hours = time / 3600000
    val minutes = (time % 3600000) / 60000
    val seconds = (time % 60000) / 1000
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun getWeekSummary(workDays: List<WorkDay>): String {
    var totalTime = Date.from(Instant.EPOCH)
    for (day in workDays) {
        totalTime = Date(totalTime.time + day.getWorkTime().time)
    }
    return totalTime.toHoursMinutesSeconds()
}