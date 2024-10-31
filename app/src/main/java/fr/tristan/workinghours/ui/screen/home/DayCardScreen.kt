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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.tristan.workinghours.R
import fr.tristan.workinghours.data.WorkDay
import fr.tristan.workinghours.data.getWorkTimeInSecond
import fr.tristan.workinghours.ui.theme.WorkingHoursTheme
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")
@Composable
fun DayCard(
    day: WorkDay,
    weekOvertime: Int,
    modifier: Modifier = Modifier,
    provisionalTime: Int = 2520000
) {
    var previsionExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (previsionExpanded) 180f else 0f, label = "",
        animationSpec = spring(
            dampingRatio = DampingRatioLowBouncy,
            stiffness = StiffnessLow
        )
    )

    Card(
        modifier = modifier.clickable {
            previsionExpanded = !previsionExpanded
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
                visible = previsionExpanded,
            ) {
                HorizontalDivider()
                DaySubRail(
                    day = day,
                    provisionalTime = provisionalTime,
                    weekOvertime = weekOvertime,
                    modifier = Modifier.padding(8.dp)
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

@Composable
fun DaySubRail(day: WorkDay, provisionalTime: Int, weekOvertime: Int, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
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
fun DayCardList(days: List<WorkDay>, provisionalTime: Int, modifier: Modifier = Modifier) {
    val groupedDates = groupDatesByWeek(days)

    AnimatedVisibility(
        visible = days.isNotEmpty(),
        modifier = modifier,
        enter = fadeIn(
            animationSpec = spring(dampingRatio = DampingRatioLowBouncy)
        ),
        exit = fadeOut()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
        ) {
            items(groupedDates) { week ->
                val weekOvertime = getWeekOvertimeInt(week.second, provisionalTime)
                Text(stringResource(R.string.week, formatWeekToString(week.first)))
                Text(
                    stringResource(R.string.week_summary, getWeekSummary(week.second)),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    stringResource(R.string.week_overtime, getWeekOvertime(week.second, provisionalTime)),
                    style = MaterialTheme.typography.bodySmall
                )
                Column {
                    for ((index, day) in week.second.withIndex()) {
                        DayCard(
                            day,
                            provisionalTime = provisionalTime,
                            weekOvertime = weekOvertime,
                            modifier = Modifier
                                .padding(8.dp)
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


@Preview(showBackground = true)
@Composable
fun WorkDayPreview() {
    WorkingHoursTheme {
        DayCard(WorkDay(Date(), Date(), Date(), Date(), Date()), weekOvertime = 0)
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
            provisionalTime = 2520000
        )
    }
}