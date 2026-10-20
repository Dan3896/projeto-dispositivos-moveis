package com.example.upcampusplus.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upcampusplus.TasksView
import com.example.upcampusplus.domain.AppClock
import com.example.upcampusplus.domain.AreaRepository
import com.example.upcampusplus.domain.CompleteTask
import com.example.upcampusplus.domain.ReopenTask
import com.example.upcampusplus.domain.TaskRepository
import com.example.upcampusplus.domain.TaskSections
import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.splitSections
import com.example.upcampusplus.domain.todayFlow
import com.example.upcampusplus.ui.common.TaskActions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate

sealed interface TasksUiState {
    data object Loading : TasksUiState
    data object Error : TasksUiState
    data class Content(
        val areas: List<Area>,
        /** Nulo = Todas. */
        val filter: Int?,
        val sections: TaskSections,
        /** Tarefas não organizadas, para o rótulo da aba Inbox. */
        val inboxCount: Int,
        val today: LocalDate,
    ) : TasksUiState {
        val areaNames: Map<Int, String> = areas.associate { it.id to it.name }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel(
    private val filter: MutableStateFlow<Int?>,
    private val view: MutableStateFlow<TasksView>,
    areas: AreaRepository,
    tasks: TaskRepository,
    clock: AppClock,
    completeTask: CompleteTask,
    reopenTask: ReopenTask,
) : ViewModel() {
    val actions = TaskActions(viewModelScope, completeTask, reopenTask, tasks)

    private val retries = MutableStateFlow(0)

    val state: StateFlow<TasksUiState> = retries
        .flatMapLatest {
            combine(areas.observeAreas(), tasks.observeTasks(), filter, clock.todayFlow()) { areaList, all, areaId, today ->
                TasksUiState.Content(areaList, areaId, splitSections(all, areaId), all.count { it.isInInbox }, today)
                    as TasksUiState
            }.catch { emit(TasksUiState.Error) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState.Loading)

    /** Visão aberta (Inbox ou Todas), lembrada na sessão. */
    val selectedView: StateFlow<TasksView> = view

    fun selectView(value: TasksView) {
        view.value = value
    }

    fun selectFilter(areaId: Int?) {
        filter.value = areaId
    }

    fun retry() = retries.update { it + 1 }
}
