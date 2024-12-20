package fr.tristan.workinghours.ui.screen.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import fr.tristan.workinghours.R
import fr.tristan.workinghours.data.WorkDay
import fr.tristan.workinghours.data.getWorkTime
import fr.tristan.workinghours.ui.screen.home.DayViewModel
import fr.tristan.workinghours.ui.screen.home.isDayFinished
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    dayViewModel: DayViewModel,
    onSettingsTimeConfirm: () -> Unit
) {
    val uiState by dayViewModel.uiSettingsState.collectAsState()
    val dayUi by dayViewModel.dayListUiState.collectAsState()
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = uiState.userInputWorkTime
            )
        )
    }

    if (uiState.dataExported != null) {
        Toast.makeText(LocalContext.current, stringResource(R.string.data_exported,
            uiState.dataExported!!
        ), Toast.LENGTH_SHORT).show()
        dayViewModel.resetDataExported()
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { SettingsTopAppBar(onBackClick) },
        modifier = Modifier.safeDrawingPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            Column {
                Text(
                    stringResource(R.string.provisional_work_time, uiState.provisionalWorkTime),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    modifier = Modifier
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textFieldValueState,
                        onValueChange = {
                            val isDelete = it.text.length < uiState.userInputWorkTime.length
                            if (it.text.length <= uiState.userInputWorkTime.length + 1) {
                                val newText = formatInputTimeText(it.text, isDelete)
                                dayViewModel.updateProvisionalWorkTime(newText)
                                textFieldValueState = textFieldValueState.copy(
                                    text = newText,
                                    selection = TextRange(newText.length)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,

                            ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onSettingsTimeConfirm()
                            }
                        ),
                        modifier = Modifier
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = { onSettingsTimeConfirm() }) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            val context = LocalContext.current
            if (!dayViewModel.isNotificationEnabled(context)) {
                Row(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ask_for_notification_permission),
                        modifier = Modifier.weight(1f)
                    )

                    Button(onClick = { dayViewModel.enableNotification(context) }) {
                        Text(text = stringResource(R.string.enable))

                    }
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row {
                Text(
                    text = stringResource(R.string.export_data),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    modifier = Modifier.padding(8.dp)
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Button(onClick = {
                    dayViewModel.exportData(context)
                }) {
                    Text(text = stringResource(R.string.export))
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = stringResource(R.string.work_time_by_day),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                modifier = Modifier.padding(8.dp)
            )

            val modelProducer = remember { CartesianChartModelProducer() }

            LaunchedEffect(Unit) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(
                            getDayByDayWorkTime(dayUi.listOfDay)
                        )
                    }
                }
            }
            CartesianChartHost(
                rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(
                        title = "Hours",
                        titleComponent = rememberAxisLabelComponent(),
                        itemPlacer = remember { VerticalAxis.ItemPlacer.count() },
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(),

                ),
                modelProducer,

                )
        }

    }
}

fun getDayByDayWorkTime(days: List<WorkDay>): List<Float> {

    val daysCompleted = days.filter { isDayFinished(it) }
    val dayTimeList: MutableList<Float> = mutableListOf()

    for (day in daysCompleted) {
        dayTimeList.add(day.getWorkTime().time.toFloat() / 1000 / 60 / 60)
    }

    return dayTimeList.toList()
}


fun formatInputTimeText(input: String, isDelete: Boolean): String {

    val parts = input.split(":").toMutableList()
    if (isDelete) {
        if (parts.last().length == 2) {
            parts[parts.lastIndex] = parts[parts.lastIndex].dropLast(1)
        }

        return parts.joinToString(":")
    }

    if (parts.size >= 2 && parts.last().isNotEmpty()) {
        if (parts.last().toInt() > 59) {
            parts[parts.lastIndex] = "59"
        }
        if (!canInputBeBiggerIfTrailingNumber(parts.last(), 59)) {
            parts[parts.lastIndex] = parts[parts.lastIndex].padStart(2, '0')
        }
    }

    if (parts.size != 3) {
        if (parts.last().length == 2) {
            parts.add("")
        }
    } else {
        if (parts.last().length >= 3) {
            parts[2] = parts[2].substring(0, 2)
        }
    }

    return parts.joinToString(":")
}

fun canInputBeBiggerIfTrailingNumber(input: String, max: Int): Boolean {
    return input.padEnd(2, '0').toInt() <= max
}

@Composable
fun SettingsTopAppBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.padding(8.dp)
    ) {

        IconButton(
            onClick = { onBackClick() },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back)
            )
        }

        Text(
            stringResource(R.string.settings),
            style = MaterialTheme.typography.displaySmall
        )

    }
}

@Preview
@Composable
fun PreviewSettingsScreen() {

}
