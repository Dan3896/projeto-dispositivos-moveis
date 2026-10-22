package com.example.upcampusplus.ui.taskform

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoveToInbox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.upcampusplus.R
import com.example.upcampusplus.domain.TITLE_COUNTER_THRESHOLD
import com.example.upcampusplus.domain.TITLE_MAX_LENGTH
import com.example.upcampusplus.domain.model.Priority
import com.example.upcampusplus.ui.EXTENSION_ENABLED
import com.example.upcampusplus.ui.common.LocalMessenger
import com.example.upcampusplus.ui.common.UiText
import com.example.upcampusplus.ui.common.UserMessage
import com.example.upcampusplus.ui.common.formatFullDate
import com.example.upcampusplus.ui.common.labelRes
import com.example.upcampusplus.ui.components.ConfirmDialog
import com.example.upcampusplus.ui.components.InfoCard
import com.example.upcampusplus.ui.components.LoadingState
import com.example.upcampusplus.ui.components.OptionSelector
import com.example.upcampusplus.ui.components.ScreenHeader
import com.example.upcampusplus.ui.theme.ClarityTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun TaskFormScreen(viewModel: TaskFormViewModel, onClose: (message: UiText?) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val areas by viewModel.areas.collectAsStateWithLifecycle()
    val messenger = LocalMessenger.current
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TaskFormEvent.Done -> onClose(event.message)
                is TaskFormEvent.Missing -> onClose(event.message)
                is TaskFormEvent.Error -> messenger.show(UserMessage(event.message))
            }
        }
    }

    val requestClose = { if (state.isDirty) confirmDiscard = true else onClose(null) }
    BackHandler(enabled = state.isDirty && !state.saving) { confirmDiscard = true }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(
                when (state.mode) {
                    FormMode.CREATE -> R.string.form_new_title
                    FormMode.EDIT -> R.string.form_edit_title
                    FormMode.ORGANIZE -> R.string.form_organize_title
                },
            ),
            subtitle = stringResource(
                if (state.mode == FormMode.ORGANIZE) R.string.form_organize_subtitle else R.string.form_subtitle,
            ),
        )
        if (state.loading) {
            LoadingState()
        } else {
            FormContent(
                state = state,
                areas = areas.map { it.id to it.name },
                onTitleChange = viewModel::onTitleChange,
                onAreaChange = viewModel::onAreaChange,
                onPriorityChange = viewModel::onPriorityChange,
                onDueDateChange = viewModel::onDueDateChange,
                onCancel = requestClose,
                onSave = viewModel::save,
                onMoveToInbox = viewModel::moveToInbox,
                focusTitle = state.mode == FormMode.CREATE,
            )
        }
    }

    if (confirmDiscard) {
        ConfirmDialog(
            title = stringResource(R.string.discard_title),
            text = stringResource(R.string.discard_text),
            confirmLabel = stringResource(R.string.action_discard),
            dismissLabel = stringResource(R.string.action_keep_editing),
            onConfirm = {
                confirmDiscard = false
                onClose(null)
            },
            onDismiss = { confirmDiscard = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormContent(
    state: TaskFormState,
    areas: List<Pair<Int, String>>,
    onTitleChange: (String) -> Unit,
    onAreaChange: (Int) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onDueDateChange: (LocalDate?) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onMoveToInbox: () -> Unit,
    focusTitle: Boolean,
) {
    val spacing = ClarityTheme.spacing
    val fields = state.fields
    val enabled = !state.saving
    val focusRequester = remember { FocusRequester() }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { if (focusTitle) focusRequester.requestFocus() }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                OutlinedTextField(
                    value = fields.title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.field_title)) },
                    leadingIcon = { Icon(Icons.Outlined.EditNote, contentDescription = null) },
                    isError = state.showTitleError,
                    enabled = enabled,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    supportingText = {
                        when {
                            state.showTitleError -> Text(stringResource(R.string.field_title_error))
                            fields.title.length >= TITLE_COUNTER_THRESHOLD ->
                                Text(stringResource(R.string.field_counter, fields.title.length, TITLE_MAX_LENGTH))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )

                FieldLabel(stringResource(R.string.field_area))
                OptionSelector(
                    options = areas,
                    selected = areas.firstOrNull { it.first == fields.areaId },
                    label = { it.second },
                    onSelect = { onAreaChange(it.first) },
                    enabled = enabled,
                )
                if (state.showAreaError) {
                    val areaError = stringResource(R.string.field_area_error)
                    Text(
                        areaError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }

                FieldLabel(stringResource(R.string.field_priority))
                OptionSelector(
                    options = Priority.entries,
                    selected = fields.priority,
                    label = { stringResource(it.labelRes()) },
                    onSelect = onPriorityChange,
                    enabled = enabled,
                )

                DueDateField(
                    date = fields.dueDate,
                    today = state.today,
                    enabled = enabled,
                    onChange = { showDatePicker = true },
                    onClear = { onDueDateChange(null) },
                )

                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm), modifier = Modifier.padding(top = spacing.sm)) {
                    OutlinedButton(onClick = onCancel, enabled = enabled, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Button(onClick = onSave, enabled = enabled, modifier = Modifier.weight(1f)) {
                        if (state.saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(Modifier.width(spacing.sm))
                        }
                        Text(stringResource(if (state.mode == FormMode.ORGANIZE) R.string.action_organize else R.string.action_save))
                    }
                }
                if (state.canMoveToInbox && EXTENSION_ENABLED) {
                    TextButton(onClick = onMoveToInbox, enabled = enabled, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Icon(Icons.Outlined.MoveToInbox, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(spacing.sm))
                        Text(stringResource(R.string.action_move_to_inbox))
                    }
                }
            }
        }

        InfoCard(
            title = stringResource(R.string.tip_title),
            text = stringResource(if (state.mode == FormMode.ORGANIZE) R.string.tip_organize else R.string.tip_text),
            icon = Icons.Outlined.Lightbulb,
        )
    }

    if (showDatePicker) {
        // O DatePicker trabalha em milissegundos UTC à meia-noite; o prazo é só a data (RN-11).
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (fields.dueDate ?: state.today).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        onDueDateChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun DueDateField(date: LocalDate?, today: LocalDate, enabled: Boolean, onChange: () -> Unit, onClear: () -> Unit) {
    val value = when (date) {
        null -> stringResource(R.string.due_none)
        today -> stringResource(R.string.due_today, formatFullDate(date))
        else -> formatFullDate(date)
    }
    val changeDescription = stringResource(R.string.due_change_description)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(ClarityTheme.spacing.md))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.field_due), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
        if (date != null) {
            IconButton(onClick = onClear, enabled = enabled) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.due_clear))
            }
        }
        TextButton(
            onClick = onChange,
            enabled = enabled,
            modifier = Modifier.semantics { contentDescription = changeDescription },
        ) { Text(stringResource(if (date == null) R.string.due_set else R.string.due_change)) }
    }
}
