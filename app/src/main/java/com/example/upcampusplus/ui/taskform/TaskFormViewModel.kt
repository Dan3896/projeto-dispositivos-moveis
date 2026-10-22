package com.example.upcampusplus.ui.taskform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upcampusplus.R
import com.example.upcampusplus.domain.AppClock
import com.example.upcampusplus.domain.AreaRepository
import com.example.upcampusplus.domain.TaskRepository
import com.example.upcampusplus.domain.limitTitleInput
import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.model.Priority
import com.example.upcampusplus.domain.model.TaskDraft
import com.example.upcampusplus.domain.normalizeTitle
import com.example.upcampusplus.domain.today
import com.example.upcampusplus.ui.common.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Como o formulário foi aberto. */
data class TaskFormArgs(
    /** Editar ou organizar esta tarefa. */
    val taskId: Long? = null,
    /** Área do filtro ativo em Tarefas (RN-14). */
    val areaId: Int? = null,
)

enum class FormMode {
    CREATE,
    EDIT,

    /** Tarefa do Inbox: a confirmação a organiza (RN-16). */
    ORGANIZE,
}

data class FormFields(
    val title: String,
    val areaId: Int?,
    val priority: Priority,
    val dueDate: LocalDate?,
)

data class TaskFormState(
    val mode: FormMode,
    val loading: Boolean,
    val fields: FormFields,
    val initial: FormFields,
    val today: LocalDate,
    /** RN-22: só tarefa organizada e pendente volta ao Inbox. */
    val canMoveToInbox: Boolean = false,
    val showTitleError: Boolean = false,
    val showAreaError: Boolean = false,
    val saving: Boolean = false,
) {
    /** RF-006: só pergunta antes de descartar se algo mudou. */
    val isDirty: Boolean get() = fields != initial
}

sealed interface TaskFormEvent {
    /** Gravou: fechar o formulário e mostrar a mensagem na tela de origem. */
    data class Done(val message: UiText) : TaskFormEvent

    /** Falhou: o formulário continua aberto e preenchido. */
    data class Error(val message: UiText) : TaskFormEvent

    /** A tarefa não existe mais ou mudou de estado: fechar com aviso. */
    data class Missing(val message: UiText) : TaskFormEvent
}

class TaskFormViewModel(
    private val args: TaskFormArgs,
    private val tasks: TaskRepository,
    private val clock: AppClock,
    areas: AreaRepository,
) : ViewModel() {
    private val _state: MutableStateFlow<TaskFormState>
    val state: StateFlow<TaskFormState>

    val areas: StateFlow<List<Area>> = areas.observeAreas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Area.DEFAULTS)

    private val _events = Channel<TaskFormEvent>(Channel.BUFFERED)
    val events: Flow<TaskFormEvent> = _events.receiveAsFlow()

    init {
        // RN-14: área do filtro ativo, senão Faculdade; prioridade Média; prazo hoje.
        val today = clock.today()
        val defaults = FormFields(
            title = "",
            areaId = args.areaId ?: Area.FACULDADE,
            priority = Priority.MEDIUM,
            dueDate = today,
        )
        val editing = args.taskId != null
        _state = MutableStateFlow(
            TaskFormState(
                mode = if (editing) FormMode.EDIT else FormMode.CREATE,
                loading = editing,
                fields = defaults,
                initial = defaults,
                today = today,
            ),
        )
        state = _state.asStateFlow()
        if (args.taskId != null) load(args.taskId)
    }

    private fun load(taskId: Long) {
        viewModelScope.launch {
            try {
                val task = tasks.getTask(taskId)
                if (task == null) {
                    _events.send(TaskFormEvent.Missing(UiText(R.string.err_task_missing)))
                    return@launch
                }
                // Organizar mostra o que já foi preenchido, sem padrões: área e data são escolhas explícitas.
                val loaded = FormFields(task.title, task.areaId, task.priority, task.dueDate)
                _state.update {
                    it.copy(
                        mode = if (task.organized) FormMode.EDIT else FormMode.ORGANIZE,
                        loading = false,
                        fields = loaded,
                        initial = loaded,
                        canMoveToInbox = task.organized && !task.isCompleted,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(TaskFormEvent.Missing(UiText(R.string.load_error)))
            }
        }
    }

    fun onTitleChange(value: String) =
        updateFields { s -> s.copy(fields = s.fields.copy(title = limitTitleInput(value)), showTitleError = false) }

    fun onAreaChange(areaId: Int) =
        updateFields { s -> s.copy(fields = s.fields.copy(areaId = areaId), showAreaError = false) }

    fun onPriorityChange(priority: Priority) = updateFields { s -> s.copy(fields = s.fields.copy(priority = priority)) }

    /** Nulo = sem data. */
    fun onDueDateChange(date: LocalDate?) = updateFields { s -> s.copy(fields = s.fields.copy(dueDate = date)) }

    private fun updateFields(change: (TaskFormState) -> TaskFormState) {
        _state.update { s -> if (s.saving || s.loading) s else change(s) }
    }

    /** RN-02: o primeiro toque trava o botão até o fim; toques seguintes são ignorados. */
    fun save() {
        val current = _state.value
        val draft = validDraft(current, requireArea = true) ?: return
        submit {
            when (current.mode) {
                FormMode.CREATE -> {
                    tasks.createTask(draft, clock.now())
                    TaskFormEvent.Done(UiText(R.string.msg_task_created))
                }
                FormMode.EDIT -> doneOrMissing(tasks.updateTask(args.taskId!!, draft, clock.now()), R.string.msg_task_saved)
                FormMode.ORGANIZE ->
                    doneOrMissing(tasks.organizeTask(args.taskId!!, draft, clock.now()), R.string.msg_task_organized)
            }
        }
    }

    /** RN-22: grava o que está no formulário e devolve a tarefa ao Inbox, com área e data preservadas. */
    fun moveToInbox() {
        val current = _state.value
        if (!current.canMoveToInbox) return
        val draft = validDraft(current, requireArea = false) ?: return
        submit { doneOrMissing(tasks.moveToInbox(args.taskId!!, draft, clock.now()), R.string.msg_moved_to_inbox) }
    }

    private fun validDraft(current: TaskFormState, requireArea: Boolean): TaskDraft? {
        if (current.saving || current.loading) return null
        val title = normalizeTitle(current.fields.title)
        val missingArea = requireArea && current.fields.areaId == null
        if (title == null || missingArea) {
            _state.update { it.copy(showTitleError = title == null, showAreaError = missingArea) }
            return null
        }
        val f = current.fields
        return TaskDraft(title, f.areaId, f.priority, f.dueDate)
    }

    private fun doneOrMissing(written: Boolean, messageRes: Int): TaskFormEvent =
        if (written) TaskFormEvent.Done(UiText(messageRes)) else TaskFormEvent.Missing(UiText(R.string.err_task_missing))

    private fun submit(write: suspend () -> TaskFormEvent) {
        _state.update { it.copy(saving = true, showTitleError = false, showAreaError = false) }
        viewModelScope.launch {
            try {
                // Continua em "salvando" até a tela fechar, para não gravar de novo.
                _events.send(write())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(saving = false) }
                _events.send(TaskFormEvent.Error(UiText(R.string.err_save)))
            }
        }
    }
}
