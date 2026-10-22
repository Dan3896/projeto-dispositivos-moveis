package com.example.upcampusplus.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upcampusplus.R
import com.example.upcampusplus.domain.AppClock
import com.example.upcampusplus.domain.AreaRepository
import com.example.upcampusplus.domain.TaskRepository
import com.example.upcampusplus.domain.limitTitleInput
import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.model.Task
import com.example.upcampusplus.domain.normalizeTitle
import com.example.upcampusplus.domain.todayFlow
import com.example.upcampusplus.ui.common.UiText
import com.example.upcampusplus.ui.common.UserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

sealed interface InboxUiState {
    data object Loading : InboxUiState
    data object Error : InboxUiState
    data class Content(
        /** Tarefas não organizadas, mais recentes primeiro (INB RF-001). */
        val tasks: List<Task>,
        val areas: List<Area>,
        val today: LocalDate,
        val zone: ZoneId,
    ) : InboxUiState {
        val areaNames: Map<Int, String> = areas.associate { it.id to it.name }
    }
}

data class CaptureInput(val text: String = "", val saving: Boolean = false) {
    val canSubmit: Boolean get() = !saving && normalizeTitle(text) != null
}

/** Visão Inbox da tela Tarefas: captura rápida e lista do que falta organizar. */
@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModel(
    private val tasks: TaskRepository,
    private val clock: AppClock,
    areas: AreaRepository,
) : ViewModel() {
    private val retries = MutableStateFlow(0)

    private val _input = MutableStateFlow(CaptureInput())
    val input: StateFlow<CaptureInput> = _input.asStateFlow()

    private val _messages = Channel<UserMessage>(Channel.BUFFERED)
    val messages: Flow<UserMessage> = _messages.receiveAsFlow()

    private val inFlight = mutableSetOf<Long>()

    val state: StateFlow<InboxUiState> = retries
        .flatMapLatest {
            combine(tasks.observeInbox(), areas.observeAreas(), clock.todayFlow()) { inbox, areaList, today ->
                InboxUiState.Content(inbox, areaList, today, clock.zone()) as InboxUiState
            }.catch { emit(InboxUiState.Error) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InboxUiState.Loading)

    fun onInputChange(value: String) = _input.update { if (it.saving) it else it.copy(text = limitTitleInput(value)) }

    /** INB RF-002 e RN-02: um toque, uma tarefa; o texto fica no campo se a gravação falhar. */
    fun capture() {
        val current = _input.value
        if (current.saving) return
        val title = normalizeTitle(current.text) ?: return
        _input.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                tasks.captureTask(title, clock.now())
                _input.value = CaptureInput()
                _messages.send(UserMessage(UiText(R.string.msg_captured)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _input.update { it.copy(saving = false) }
                _messages.send(UserMessage(UiText(R.string.err_capture)))
            }
        }
    }

    /** INB RF-007: o mesmo registro some de todas as visões. */
    fun remove(taskId: Long) {
        if (!inFlight.add(taskId)) return
        viewModelScope.launch {
            try {
                tasks.deleteTask(taskId)
                _messages.send(UserMessage(UiText(R.string.msg_task_removed)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(UserMessage(UiText(R.string.err_remove)))
            } finally {
                inFlight.remove(taskId)
            }
        }
    }

    fun retry() = retries.update { it + 1 }
}
