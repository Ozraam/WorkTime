package fr.tristan.workinghours.ui.screen.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.tristan.workinghours.R
import fr.tristan.workinghours.data.WorkDay
import fr.tristan.workinghours.data.getWorkTimeInSecond
import fr.tristan.workinghours.ui.screen.settings.canInputBeBiggerIfTrailingNumber
import fr.tristan.workinghours.ui.theme.WorkingHoursTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@SuppressLint("SimpleDateFormat")
@Composable
fun DayCard(
    day: WorkDay,
    weekOvertime: Int,
    onEditRequest: (day: Date) -> Unit,
    onDeleteRequest: (day: WorkDay) -> Unit,
    modifier: Modifier = Modifier,
    provisionalTime: Int = 2520000,
    initialMoreInfoExpanded: Boolean = false,
) {
    var moreInfoExpanded by remember { mutableStateOf(initialMoreInfoExpanded) }
    val rotation by animateFloatAsState(
        targetValue = if (moreInfoExpanded) 180f else 0f, label = "",
        animationSpec = spring(
            dampingRatio = DampingRatioLowBouncy,
            stiffness = StiffnessLow
        )
    )

    Card(
        modifier = modifier.clickable {
            moreInfoExpanded = !moreInfoExpanded
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy")
        val context = LocalContext.current
        Column {
            Text(
                dateFormat.format(day.date).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )

            HorizontalDivider()

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row {
                    Text(
                        stringResource(
                            R.string.work_in,
                            formatTimeOrSayNotSetEpoch(day.workIn, context)
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(
                            R.string.work_out,
                            formatTimeOrSayNotSetEpoch(day.workOut, context)
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Row {
                    Text(
                        stringResource(
                            R.string.lunch_in,
                            formatTimeOrSayNotSetEpoch(day.lunchIn, context)
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        stringResource(
                            R.string.lunch_out,
                            formatTimeOrSayNotSetEpoch(day.lunchOut, context)
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            AnimatedVisibility(
                visible = moreInfoExpanded,
            ) {
                HorizontalDivider()
                DaySubRail(
                    day = day,
                    provisionalTime = provisionalTime,
                    weekOvertime = weekOvertime,
                    modifier = Modifier.padding(8.dp),
                    onEditRequest = { onEditRequest(day.date) },
                    onDeleteRequest = onDeleteRequest
                )
            }


            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .rotate(rotation),
            )
        }

    }
}

@SuppressLint("SimpleDateFormat")
@Composable
fun DaySubRail(
    day: WorkDay,
    provisionalTime: Int,
    onDeleteRequest: (day: WorkDay) -> Unit,
    weekOvertime: Int, modifier: Modifier = Modifier,
    onEditRequest: () -> Unit = {},
) {
    var deleteConfirmation by remember { mutableStateOf(false) }
    val dayFormat = SimpleDateFormat("EEEE, dd MMM yyyy")
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (!isDayFinished(day)) {
            Text(
                stringResource(
                    R.string.prevision,
                    getPrevision(day, provisionalTime, 0)
                )
            )
            Text(
                stringResource(
                    R.string.min_prevision,
                    getPrevision(day, provisionalTime, weekOvertime)
                )
            )
        }
        if (isDayFinished(day)) {
            Text(stringResource(R.string.day_summary, getDaySummary(day)))
            Text(stringResource(R.string.overtime, getOvertime(day, provisionalTime)))
        }

        Row(
            modifier = Modifier.align(Alignment.End)
        ) {
            TextButton(
                onClick = { onEditRequest() },
                modifier = Modifier
            ) {
                Text(stringResource(R.string.edit))
            }

            TextButton(
                onClick = { deleteConfirmation = true },
                modifier = Modifier
            ) {
                Text(stringResource(R.string.delete))
            }

            if (deleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { deleteConfirmation = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                deleteConfirmation = false
                                onDeleteRequest(day)
                            }
                        ) {
                            Text(stringResource(R.string.confirmation))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                deleteConfirmation = false
                            }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    title = {
                        Text(
                            stringResource(
                                R.string.delete_day_title,
                                dayFormat.format(day.date))
                        )
                    },
                    text = {
                        Text(
                            stringResource(
                                R.string.delete_day_text,
                                dayFormat.format(day.date)
                            )
                        )
                    }
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun getOvertime(day: WorkDay, provisionalTime: Int): String {
    val overTime = day.getWorkTimeInSecond() - provisionalTime

    val hours = overTime / 3600
    val minutes = (overTime % 3600) / 60
    val seconds = overTime % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun getWeekOvertimeInt(days: List<WorkDay>, provisionalTime: Int): Int {
    var overTime = 0

    for (day in days) {
        if (isDayFinished(day)) {
            overTime += day.getWorkTimeInSecond() - provisionalTime
        }
    }

    return overTime
}

@SuppressLint("DefaultLocale")
fun getWeekOvertime(days: List<WorkDay>, provisionalTime: Int): String {
    val overTime = getWeekOvertimeInt(days, provisionalTime)
    val hours = overTime / 3600
    val minutes = (overTime % 3600) / 60
    val seconds = overTime % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}


@Composable
fun DayCardList(
    days: List<WorkDay>,
    provisionalTime: Int,
    onEditRequest: (day: Date) -> Unit,
    onDeleteRequest: (day: WorkDay) -> Unit,
    onSearchValueChange: (value: String) -> Unit,
    modifier: Modifier = Modifier,
    search: String = "",
) {
    val groupedDates = groupDatesByWeek(days).reversed().map { week ->
        val currentDay = Date()

        val thisWeek = getWeekRange(currentDay)
        val weekReversed = if(week.first == thisWeek) week.second.reversed() else week.second
        week.first to weekReversed.filter {

            if(search.isEmpty()) return@filter true

            val filteringDate = completeDateStringIfNecessary(search).toDate()
            val calendar = Calendar.getInstance()
            if (filteringDate != null) {
                calendar.time = filteringDate
            }

            val calendarDay = Calendar.getInstance()
            calendarDay.time = it.date

            val searchPart = search.split("/").filter { date -> date.isNotEmpty() }
            if (searchPart.size == 1) {
                calendar.set(Calendar.MONTH, calendarDay.get(Calendar.MONTH))
                calendar.set(Calendar.YEAR, calendarDay.get(Calendar.YEAR))
            }
            if (searchPart.size == 2) {
                calendar.set(Calendar.YEAR, calendarDay.get(Calendar.YEAR))
            }

            calendar.get(Calendar.DAY_OF_MONTH) == calendarDay.get(Calendar.DAY_OF_MONTH) &&
                    calendar.get(Calendar.MONTH) == calendarDay.get(Calendar.MONTH) &&
                    calendar.get(Calendar.YEAR) == calendarDay.get(Calendar.YEAR)
        }
    }.filter {
        it.second.isNotEmpty()
    }

    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = search
            )
        )
    }

    Column(
        modifier = modifier,
    ) {
        TextField(
            value = textFieldValueState,
            onValueChange = {
                val isDelete = it.text.length < search.length
                if (it.text.length <= search.length + 1) {
                    val newText = formatInputDateText(it.text, isDelete)
                    onSearchValueChange(newText)

                    textFieldValueState = textFieldValueState.copy(
                        text = newText,
                        selection = TextRange(newText.length)
                    )
                }
            },
            label = { Text(stringResource(R.string.search)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
        )

        CardList(days, groupedDates, provisionalTime, onEditRequest, onDeleteRequest)
    }
}

@Composable
private fun ColumnScope.CardList(
    days: List<WorkDay>,
    groupedDates: List<Pair<Pair<Date, Date>, List<WorkDay>>>,
    provisionalTime: Int,
    onEditRequest: (day: Date) -> Unit,
    onDeleteRequest: (day: WorkDay) -> Unit
) {
    AnimatedVisibility(
        visible = days.isNotEmpty(),
        enter = fadeIn(
            animationSpec = spring(dampingRatio = DampingRatioLowBouncy)
        ),
        exit = fadeOut()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(groupedDates) { week ->
                val weekOvertime = getWeekOvertimeInt(week.second, provisionalTime)
                Text(stringResource(R.string.week, formatWeekToString(week.first)))
                Text(
                    stringResource(R.string.week_summary, getWeekSummary(week.second)),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    stringResource(
                        R.string.week_overtime,
                        getWeekOvertime(week.second, provisionalTime)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Column {
                    for ((index, day) in week.second.withIndex()) {
                        DayCard(
                            day,
                            provisionalTime = provisionalTime,
                            weekOvertime = weekOvertime,
                            onEditRequest = onEditRequest,
                            onDeleteRequest = onDeleteRequest,

                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .animateEnterExit(
                                    enter = slideInVertically(
                                        animationSpec = spring(
                                            stiffness = StiffnessLow,
                                            dampingRatio = DampingRatioLowBouncy
                                        ),
                                        initialOffsetY = { it * (index + 1) }
                                    )
                                )
                        )
                    }
                }

            }
        }
    }
}

fun formatInputDateText(input: String, isDelete: Boolean): String {

    val parts = input.split("/").toMutableList()
    if (isDelete) {
        if (parts.last().length == 2 && parts.size < 3) {
            parts[parts.lastIndex] = parts[parts.lastIndex].dropLast(1)
        }

        return parts.joinToString("/")
    }

    if (parts.size < 3 && parts.last().isNotEmpty()) {
        if (parts.size == 1) {
            if (parts.last().toInt() > 31) {
                parts[parts.lastIndex] = "31"
            }
            if (!canInputBeBiggerIfTrailingNumber(parts.last(), 31)) {
                parts[parts.lastIndex] = parts[parts.lastIndex].padStart(2, '0')
            }
        } else {
            if (parts.last().toInt() > 12) {
                parts[parts.lastIndex] = "12"
            }
            if (!canInputBeBiggerIfTrailingNumber(parts.last(), 12)) {
                parts[parts.lastIndex] = parts[parts.lastIndex].padStart(2, '0')
            }
        }
    }


    if (parts.size < 3) {
        if (parts.last().length == 2) {
            parts.add("")
        }
    } else {
        if (parts.last().length > 4) {
            parts[2] = parts[2].substring(0, 4)
        }
    }

    return parts.joinToString("/")
}

@Preview(showBackground = true)
@Composable
fun WorkDayPreview() {
    WorkingHoursTheme {
        DayCard(
            WorkDay(Date(), Date(), Date(), Date(), Date()),
            weekOvertime = 0,
            initialMoreInfoExpanded = true,
            onEditRequest = {},
            onDeleteRequest = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WorkDayList() {


    WorkingHoursTheme {
        DayCardList(
            listOf(
                WorkDay(
                    Date(),
                    Date(124, 4, 2, 9, 0, 0),
                    Date(124, 4, 2, 17, 0, 0),
                    Date(124, 4, 2, 12, 30, 0),
                    Date(124, 4, 2, 13, 30, 0)
                ),
                WorkDay(
                    Date(),
                    Date(124, 4, 2, 9, 0, 0),
                    Date(124, 4, 2, 17, 0, 0),
                    Date(124, 4, 2, 12, 30, 0),
                    Date(124, 4, 2, 13, 30, 0)
                ),
                WorkDay(
                    Date(),
                    Date(124, 4, 2, 9, 0, 0),
                    Date(124, 4, 2, 17, 0, 0),
                    Date(124, 4, 2, 12, 30, 0),
                    Date(124, 4, 2, 13, 30, 0)
                ),
                WorkDay(
                    Date(),
                    Date(124, 4, 2, 9, 0, 0),
                    Date(124, 4, 2, 17, 0, 0),
                    Date(124, 4, 2, 12, 30, 0),
                    Date(124, 4, 2, 13, 30, 0)
                ),
                WorkDay(
                    Date(),
                    Date(124, 4, 2, 9, 0, 0),
                    Date(124, 4, 2, 17, 0, 0),
                    Date(124, 4, 2, 12, 30, 0),
                    Date(124, 4, 2, 13, 30, 0)
                )
            ),
            provisionalTime = 2520000,
            onEditRequest = {},
            onDeleteRequest = {},
            search = "",
            onSearchValueChange = {}
        )
    }
}