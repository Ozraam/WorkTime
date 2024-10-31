package fr.tristan.workinghours.ui.screen.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.tristan.workinghours.R
import fr.tristan.workinghours.data.WorkDay
import fr.tristan.workinghours.data.WorkDayRepositoryFake
import fr.tristan.workinghours.ui.theme.WorkingHoursTheme
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SimpleDateFormat")
@Composable
fun WorkHourAddScreen(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onTimeChange: (hour: String, minute: String, second: String) -> Unit,
    onTypeOfHourChange: (typeOfHour: HourType) -> Unit,
    viewModel: DayViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy")

    var dropdownExpanded by remember { mutableStateOf(false) }
    var datePickerExpanded by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.date.time
    )

    val items = listOf(
        HourType.WORK_IN,
        HourType.WORK_OUT,
        HourType.LUNCH_IN,
        HourType.LUNCH_OUT
    )

    AlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                if (isValidTime(uiState.hour, uiState.minute, uiState.second, true))
                    onConfirm()
            }) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Column {
                Text(
                    stringResource(R.string.add_hour_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { datePickerExpanded = true },
                        shape = RoundedCornerShape(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null
                        )
                        Text(dateFormat.format(uiState.date).replaceFirstChar { it.uppercase() })
                    }
                }

                if(datePickerExpanded) {
                    DatePickerDialog(
                        onDismissRequest = { datePickerExpanded = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerExpanded = false
                                    viewModel.updateDate(datePickerState.selectedDateMillis!!)
                                }
                            ) {
                                Text(stringResource(R.string.confirm))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { datePickerExpanded = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState,
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.padding(8.dp)
                )

                TimeSelector(
                    uiState,
                    onTimeChange,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                HourTypeSelector(
                    dropdownExpanded,
                    uiState,
                    items,
                    onTypeOfHourChange,
                    onDismissRequest = { dropdownExpanded = false },
                    onExpandRequest = { dropdownExpanded = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
    )
}

@Composable
private fun HourTypeSelector(
    dropdownExpanded: Boolean,
    uiState: DayUiState,
    items: List<HourType>,
    onTypeOfHourChange: (typeOfHour: HourType) -> Unit,
    onDismissRequest: () -> Unit,
    onExpandRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.type_of_hour)
        )

        OutlinedButton(
            onClick = { onExpandRequest() },
            shape = RoundedCornerShape(5.dp)
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(uiState.typeOfHour.title)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        }

        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { onDismissRequest() }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.title) },
                    onClick = {
                        onTypeOfHourChange(item)
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun TimeSelector(
    uiState: DayUiState,
    onTimeChange: (hour: String, minute: String, second: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.time),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                uiState.hour,
                onValueChange = {
                    if (it.length <= 2 && isValidTime(it, uiState.minute, uiState.second))
                        onTimeChange(it, uiState.minute, uiState.second)
                },
                label = { Text(stringResource(R.string.hour)) },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.isFocused) {
                            onTimeChange("", uiState.minute, uiState.second)
                        }
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(
                ":",
                style = MaterialTheme.typography.bodyLarge
            )

            TextField(
                uiState.minute,
                onValueChange = {
                    if (it.length <= 2 && isValidTime(uiState.hour, it, uiState.second))
                        onTimeChange(uiState.hour, it, uiState.second)
                },
                label = { Text(stringResource(R.string.minute)) },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.isFocused) {
                            onTimeChange(uiState.hour, "", uiState.second)
                        }
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(
                ":",
                style = MaterialTheme.typography.bodyLarge
            )

            TextField(
                uiState.second,
                onValueChange = {
                    if (it.length <= 2 && isValidTime(uiState.hour, uiState.minute, it))
                        onTimeChange(uiState.hour, uiState.minute, it)
                },
                label = { Text(stringResource(R.string.second)) },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.isFocused) {
                            onTimeChange(uiState.hour, uiState.minute, "")
                        }
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

// check if hour is under 24 hours, 60 minutes and 60 seconds
fun isValidTime(hours: String, minute: String, second: String, falseOnNull: Boolean = false) : Boolean {
    val hoursInt = hours.toIntOrNull()
    val minuteInt = minute.toIntOrNull()
    val secondInt = second.toIntOrNull()

    if (hoursInt != null && minuteInt != null && secondInt != null) {
        return hoursInt < 24 && minuteInt < 60 && secondInt < 60
    }
    return !falseOnNull
}