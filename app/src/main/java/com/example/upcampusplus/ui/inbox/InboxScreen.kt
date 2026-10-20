package com.example.upcampusplus.ui.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.upcampusplus.R
import com.example.upcampusplus.domain.TITLE_COUNTER_THRESHOLD
import com.example.upcampusplus.domain.TITLE_MAX_LENGTH
import com.example.upcampusplus.domain.model.Task
import com.example.upcampusplus.ui.common.countRes
import com.example.upcampusplus.ui.common.formatDue
import com.example.upcampusplus.ui.common.formatMoment
import com.example.upcampusplus.ui.components.ConfirmDialog
import com.example.upcampusplus.ui.components.EmptyState
import com.example.upcampusplus.ui.components.ErrorState
import com.example.upcampusplus.ui.components.LoadingState
import com.example.upcampusplus.ui.components.SectionHeader
import com.example.upcampusplus.ui.theme.ClarityTheme

/** Visão Inbox da tela Tarefas (PRD do Inbox, seção 6). As mensagens são coletadas pela tela Tarefas. */
@Composable
fun InboxPane(viewModel: InboxViewModel, onOrganize: (taskId: Long) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val input by viewModel.input.collectAsStateWithLifecycle()
    var removalId by rememberSaveable { mutableStateOf<Long?>(null) }

    when (val s = state) {
        InboxUiState.Loading -> LoadingState()
        InboxUiState.Error -> ErrorState(stringResource(R.string.load_error), onRetry = viewModel::retry)
        is InboxUiState.Content -> InboxContent(
            state = s,
            input = input,
            onInputChange = viewModel::onInputChange,
            onCapture = viewModel::capture,
            onOrganize = onOrganize,
            onRemove = { removalId = it },
        )
    }

    removalId?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.remove_task_title),
            text = stringResource(R.string.remove_inbox_text),
            confirmLabel = stringResource(R.string.action_remove),
            dismissLabel = stringResource(R.string.action_cancel),
            onConfirm = {
                viewModel.remove(id)
                removalId = null
            },
            onDismiss = { removalId = null },
        )
    }
}

@Composable
private fun InboxContent(
    state: InboxUiState.Content,
    input: CaptureInput,
    onInputChange: (String) -> Unit,
    onCapture: () -> Unit,
    onOrganize: (Long) -> Unit,
    onRemove: (Long) -> Unit,
) {
    val spacing = ClarityTheme.spacing
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item(key = "input") {
            Row(verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = input.text,
                    onValueChange = onInputChange,
                    label = { Text(stringResource(R.string.capture_label)) },
                    singleLine = true,
                    // Só leitura, não desabilitado: o foco e o teclado ficam para a próxima captura.
                    readOnly = input.saving,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (input.canSubmit) onCapture() }),
                    supportingText = if (input.text.length >= TITLE_COUNTER_THRESHOLD) {
                        { Text(stringResource(R.string.field_counter, input.text.length, TITLE_MAX_LENGTH)) }
                    } else {
                        null
                    },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(spacing.sm))
                FilledIconButton(onClick = onCapture, enabled = input.canSubmit, modifier = Modifier.padding(top = spacing.sm)) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.capture_add))
                }
            }
        }
        item(key = "header") {
            SectionHeader(
                stringResource(R.string.section_to_organize),
                count = stringResource(
                    countRes(state.tasks.size, R.string.task_count_one, R.string.task_count_other),
                    state.tasks.size,
                ),
            )
        }
        if (state.tasks.isEmpty()) {
            item(key = "empty") {
                EmptyState(stringResource(R.string.empty_inbox), hint = stringResource(R.string.empty_inbox_hint))
            }
        } else {
            items(state.tasks, key = { it.id }) { task ->
                InboxCard(
                    task = task,
                    state = state,
                    onOrganize = { onOrganize(task.id) },
                    onRemove = { onRemove(task.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

/** Tarefa ainda não organizada: título, quando foi capturada e as duas ações explícitas. */
@Composable
private fun InboxCard(
    task: Task,
    state: InboxUiState.Content,
    onOrganize: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ClarityTheme.spacing
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    // Área e data preservadas de quando a tarefa voltou ao Inbox (RN-22).
    val kept = listOfNotNull(
        task.areaId?.let { state.areaNames[it] },
        task.dueDate?.let { formatDue(it, state.today) },
    )
    val captured = stringResource(R.string.captured_at, formatMoment(task.createdAt, state.zone, state.today))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.action_organize_task), onClick = onOrganize),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(start = spacing.lg, top = spacing.md, end = spacing.sm, bottom = spacing.sm)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            Text(
                (listOf(captured) + kept).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = muted,
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = spacing.xs)) {
                FilledTonalButton(onClick = onOrganize) { Text(stringResource(R.string.action_organize)) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.action_remove_task), tint = muted)
                }
            }
        }
    }
}
