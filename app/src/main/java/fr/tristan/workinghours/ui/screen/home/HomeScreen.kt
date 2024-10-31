package fr.tristan.workinghours.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.tristan.workinghours.R
import fr.tristan.workinghours.data.WorkDay
import fr.tristan.workinghours.ui.theme.WorkingHoursTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    dayViewModel: DayViewModel,
    onSettingsClick: () -> Unit,
) {
    var addExpanded by remember { mutableStateOf(false) }
    val uiState by dayViewModel.dayListUiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val provisionalTime by dayViewModel.timeSettingsState.collectAsState()

    Scaffold(
        topBar = { WorkingTopAppBar(onSettingsClick = onSettingsClick) },
        modifier = Modifier.safeDrawingPadding(),
        floatingActionButton = {
            AddFloatingActionButton(
                onClick = {
                    addExpanded = true
                    dayViewModel.setTimeToCurrent()
                    dayViewModel.detectTypeOfHour()
                }
            )
        }
    ) {
        if (uiState.listOfDay.isEmpty()) {
            Box(
                modifier = Modifier.padding(it)
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                FirstTimeHelper()
            }
        } else {
            Column(modifier = Modifier.padding(it)) {
                if (dayViewModel.getTodayWorkDay() != null)
                    TodayWorkDay(
                        day = uiState.listOfDay.last(),
                        modifier = Modifier.padding(8.dp),
                        provisionalTime = provisionalTime,
                        weekOvertime = getWeekOvertimeInt(
                            getWeek(uiState.listOfDay.last(), uiState.listOfDay),
                            provisionalTime
                        )
                    )

                Text(
                    stringResource(R.string.all_work_day),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(8.dp)
                )

                DayCardList(
                    days = uiState.listOfDay,
                    modifier = Modifier,
                    provisionalTime = provisionalTime
                )
            }
        }


        if (addExpanded) {
            WorkHourAddScreen(
                viewModel = dayViewModel,
                onDismiss = {
                    addExpanded = false
                },
                onConfirm = {
                    addExpanded = false
                    coroutineScope.launch {
                        dayViewModel.saveAnHour()
                    }
                },
                onTimeChange = { hour, minute, second ->
                    dayViewModel.updateTime(hour, minute, second)
                },
                onTypeOfHourChange = { typeOfHour ->
                    dayViewModel.updateTypeOfHour(typeOfHour)
                }
            )
        }
    }
}

fun getWeek(day: WorkDay, days: List<WorkDay>): List<WorkDay> {
    val weeks = groupDatesByWeek(days)
    for (week in weeks) {
        if (week.second.contains(day)) {
            return week.second
        }
    }
    return emptyList()
}

@Composable
fun TodayWorkDay(day: WorkDay, weekOvertime: Int, provisionalTime: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            stringResource(R.string.today_work_day),
            style = MaterialTheme.typography.headlineSmall
        )

        DayCard(
            day = day,
            provisionalTime = provisionalTime,
            weekOvertime = weekOvertime
        )
    }
}

@Composable
fun FirstTimeHelper(modifier: Modifier = Modifier) {
    val text = stringResource(id = R.string.first_time_helper)

    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
        modifier = modifier,
        textAlign = TextAlign.Center
    )
}

@Composable
fun WorkingTopAppBar(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit
) {
    Column(modifier = modifier) {
        Row (modifier = Modifier.padding(8.dp)) {
            Column {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall
                )
//                Text(
//                    stringResource(R.string.app_description),
//                    style = MaterialTheme.typography.bodyLarge
//                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = { onSettingsClick() },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings)
                )
            }
        }
    }
}

@Composable
fun AddFloatingActionButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier,
        shape = CutCornerShape(topStart = 20.dp, bottomEnd = 20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_work_day)
        )
    }
}



@Preview
@Composable
fun PreviewTopAppBar() {
    WorkingHoursTheme {
        WorkingTopAppBar(onSettingsClick = {})
    }
}