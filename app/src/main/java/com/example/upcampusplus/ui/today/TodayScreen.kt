package com.example.upcampusplus.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.upcampusplus.R
import com.example.upcampusplus.ui.common.LocalMessenger
import com.example.upcampusplus.ui.common.countRes
import com.example.upcampusplus.ui.common.formatLongDate
import com.example.upcampusplus.ui.components.ConfirmDialog
import com.example.upcampusplus.ui.components.CounterCard
import com.example.upcampusplus.ui.components.EmptyState
import com.example.upcampusplus.ui.components.ErrorState
import com.example.upcampusplus.ui.components.LoadingState
import com.example.upcampusplus.ui.components.ScreenHeader
import com.example.upcampusplus.ui.components.SectionHeader
import com.example.upcampusplus.ui.components.TaskCard
import com.example.upcampusplus.ui.components.WrappingRow
import com.example.upcampusplus.ui.theme.ClarityTheme

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNewTask: () -> Unit,
    onAllTasks: () -> Unit,
    onEditTask: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val messenger = LocalMessenger.current
    LaunchedEffect(viewModel) { viewModel.actions.messages.collect { messenger.show(it) } }

    var removalId by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxSize()) {
        when (val s = state) {
            TodayUiState.Loading -> {
                ScreenHeader(stringResource(R.string.today_title), "")
                LoadingState()
            }
            TodayUiState.Error -> {
                ScreenHeader(stringResource(R.string.today_title), "")
                ErrorState(stringResource(R.string.load_error), onRetry = viewModel::retry)
            }
            is TodayUiState.Content -> TodayContent(
                state = s,
                onToggle = viewModel.actions::setCompleted,
                onEditTask = onEditTask,
                onRemove = { removalId = it },
                onNewTask = onNewTask,
                onAllTasks = onAllTasks,
            )
        }
    }

    removalId?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.remove_task_title),
            confirmLabel = stringResource(R.string.action_remove),
            dismissLabel = stringResource(R.string.action_cancel),
            onConfirm = {
                viewModel.actions.remove(id)
                removalId = null
            },
            onDismiss = { removalId = null },
        )
    }
}

@Composable
private fun TodayContent(
    state: TodayUiState.Content,
    onToggle: (Long, Boolean) -> Unit,
    onEditTask: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onNewTask: () -> Unit,
    onAllTasks: () -> Unit,
) {
    val overview = state.overview
    val summary = overview.summary
    val colors = ClarityTheme.colors
    val spacing = ClarityTheme.spacing

    val count = summary.dueTodayCount
    val subtitle = stringResource(
        R.string.today_subtitle,
        formatLongDate(overview.today).replaceFirstChar { it.titlecase() },
        stringResource(countRes(count, R.string.today_count_one, R.string.today_count_other), count),
    )
    val xpDescription = stringResource(R.string.xp_total_description, overview.totalXp)

    ScreenHeader(
        title = stringResource(R.string.today_title),
        subtitle = subtitle,
        trailing = {
            // D-03: XP total discreto no cabeçalho.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clearAndSetSemantics { contentDescription = xpDescription },
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = colors.xp, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(spacing.xs))
                Text(
                    stringResource(R.string.xp_total, overview.totalXp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onHeader,
                )
            }
        },
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item(key = "counters") {
            WrappingRow { itemModifier ->
                CounterCard(count, stringResource(R.string.counter_due), MaterialTheme.colorScheme.primary, itemModifier)
                CounterCard(
                    summary.overdueCount,
                    stringResource(countRes(summary.overdueCount, R.string.counter_overdue_one, R.string.counter_overdue_other)),
                    colors.overdue,
                    itemModifier,
                )
                CounterCard(
                    summary.completedTodayCount,
                    stringResource(
                        countRes(summary.completedTodayCount, R.string.counter_completed_one, R.string.counter_completed_other),
                    ),
                    colors.success,
                    itemModifier,
                )
            }
        }
        item(key = "section") { SectionHeader(stringResource(R.string.section_due)) }
        if (summary.dueToday.isEmpty()) {
            item(key = "empty") {
                if (summary.completedTodayCount > 0) {
                    EmptyState(stringResource(R.string.empty_today_done))
                } else {
                    EmptyState(stringResource(R.string.empty_today), hint = stringResource(R.string.empty_today_hint))
                }
            }
        } else {
            items(summary.dueToday, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    areaName = task.areaId?.let { state.areaNames[it] },
                    today = overview.today,
                    onCheckedChange = { onToggle(task.id, it) },
                    onClick = { onEditTask(task.id) },
                    onRemove = { onRemove(task.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        item(key = "actions") {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm), modifier = Modifier.padding(top = spacing.sm)) {
                Button(onClick = onNewTask, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(spacing.sm))
                    Text(stringResource(R.string.action_new_task))
                }
                OutlinedButton(onClick = onAllTasks, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Outlined.List, contentDescription = null)
                    Spacer(Modifier.width(spacing.sm))
                    Text(stringResource(R.string.action_all_tasks))
                }
            }
        }
        item(key = "hint") {
            Text(
                stringResource(R.string.today_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
