package com.example.upcampusplus.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.upcampusplus.R
import com.example.upcampusplus.TasksView
import com.example.upcampusplus.ui.EXTENSION_ENABLED
import com.example.upcampusplus.ui.inbox.InboxPane
import com.example.upcampusplus.ui.inbox.InboxViewModel
import com.example.upcampusplus.ui.common.LocalMessenger
import com.example.upcampusplus.ui.common.countRes
import com.example.upcampusplus.ui.components.ConfirmDialog
import com.example.upcampusplus.ui.components.EmptyLine
import com.example.upcampusplus.ui.components.ErrorState
import com.example.upcampusplus.ui.components.LoadingState
import com.example.upcampusplus.ui.components.ScreenHeader
import com.example.upcampusplus.ui.components.SectionHeader
import com.example.upcampusplus.ui.components.TaskCard
import com.example.upcampusplus.ui.theme.ClarityTheme

/**
 * Tarefas com duas visões do mesmo conjunto de registros (PRD do Inbox, seção 3): Inbox, o que
 * falta organizar, e Todas, organizadas ou não, com filtro por área.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    inboxViewModel: InboxViewModel,
    onNewTask: (areaId: Int?) -> Unit,
    onEditTask: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selected by viewModel.selectedView.collectAsStateWithLifecycle()
    val view = if (EXTENSION_ENABLED) selected else TasksView.ALL
    val messenger = LocalMessenger.current
    LaunchedEffect(viewModel) { viewModel.actions.messages.collect { messenger.show(it) } }
    LaunchedEffect(inboxViewModel) { inboxViewModel.messages.collect { messenger.show(it) } }

    var removalId by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.tasks_title), stringResource(R.string.tasks_subtitle))
        if (EXTENSION_ENABLED) {
            val inboxCount = (state as? TasksUiState.Content)?.inboxCount ?: 0
            PrimaryTabRow(selectedTabIndex = TasksView.entries.indexOf(view)) {
                TasksView.entries.forEach { tab ->
                    Tab(
                        selected = view == tab,
                        onClick = { viewModel.selectView(tab) },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                when (tab) {
                                    TasksView.INBOX ->
                                        if (inboxCount > 0) stringResource(R.string.tab_inbox_count, inboxCount)
                                        else stringResource(R.string.tab_inbox)
                                    TasksView.ALL -> stringResource(R.string.tab_all)
                                },
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
        }
        when (view) {
            TasksView.INBOX -> InboxPane(inboxViewModel, onOrganize = onEditTask)
            TasksView.ALL -> when (val s = state) {
                TasksUiState.Loading -> LoadingState()
                TasksUiState.Error -> ErrorState(stringResource(R.string.load_error), onRetry = viewModel::retry)
                is TasksUiState.Content -> Box(Modifier.fillMaxSize()) {
                    TasksContent(
                        state = s,
                        onFilter = viewModel::selectFilter,
                        onToggle = viewModel.actions::setCompleted,
                        onEditTask = onEditTask,
                        onRemove = { removalId = it },
                    )
                    ExtendedFloatingActionButton(
                        onClick = { onNewTask(s.filter) },
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text(stringResource(R.string.action_new_task)) },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(ClarityTheme.spacing.lg),
                    )
                }
            }
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
private fun TasksContent(
    state: TasksUiState.Content,
    onFilter: (Int?) -> Unit,
    onToggle: (Long, Boolean) -> Unit,
    onEditTask: (Long) -> Unit,
    onRemove: (Long) -> Unit,
) {
    val spacing = ClarityTheme.spacing
    val pending = state.sections.pending
    val completed = state.sections.completed
    val areaIsEmpty = state.filter != null && pending.isEmpty() && completed.isEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Espaço extra no fim para o botão flutuante não cobrir o último cartão.
        contentPadding = PaddingValues(start = spacing.lg, end = spacing.lg, top = spacing.md, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item(key = "filters") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                item {
                    FilterChip(
                        selected = state.filter == null,
                        onClick = { onFilter(null) },
                        label = { Text(stringResource(R.string.filter_all_areas)) },
                    )
                }
                items(state.areas, key = { it.id }) { area ->
                    FilterChip(
                        selected = state.filter == area.id,
                        onClick = { onFilter(area.id) },
                        label = { Text(area.name) },
                    )
                }
            }
        }

        item(key = "pending-header") {
            SectionHeader(
                stringResource(R.string.section_pending),
                count = stringResource(countRes(pending.size, R.string.task_count_one, R.string.task_count_other), pending.size),
            )
        }
        if (pending.isEmpty()) {
            item(key = "pending-empty") {
                EmptyLine(stringResource(if (areaIsEmpty) R.string.empty_area else R.string.empty_pending))
            }
        }
        items(pending, key = { it.id }) { task ->
            TaskCard(
                task = task,
                areaName = task.areaId?.let { state.areaNames[it] },
                today = state.today,
                onCheckedChange = { onToggle(task.id, it) },
                onClick = { onEditTask(task.id) },
                onRemove = { onRemove(task.id) },
                modifier = Modifier.animateItem(),
            )
        }

        item(key = "completed-header") {
            SectionHeader(
                stringResource(R.string.section_completed),
                count = stringResource(countRes(completed.size, R.string.task_count_one, R.string.task_count_other), completed.size),
            )
        }
        if (completed.isEmpty()) {
            item(key = "completed-empty") { EmptyLine(stringResource(R.string.empty_completed)) }
        }
        items(completed, key = { it.id }) { task ->
            TaskCard(
                task = task,
                areaName = task.areaId?.let { state.areaNames[it] },
                today = state.today,
                onCheckedChange = { onToggle(task.id, it) },
                onClick = { onEditTask(task.id) },
                onRemove = { onRemove(task.id) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}
