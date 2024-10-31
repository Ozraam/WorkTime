package fr.tristan.workinghours.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.tristan.workinghours.R
import fr.tristan.workinghours.data.WorkDay
import fr.tristan.workinghours.ui.screen.AppNavigation
import fr.tristan.workinghours.ui.screen.home.DayCard
import fr.tristan.workinghours.ui.screen.home.DayCardList
import fr.tristan.workinghours.ui.screen.home.DayViewModel
import fr.tristan.workinghours.ui.theme.WorkingHoursTheme
import java.util.Date

@Composable
fun WorkingHoursApp(
    dayViewModel: DayViewModel = viewModel(factory = DayViewModel.Factory),
) {
    AppNavigation(dayViewModel = dayViewModel)
}

