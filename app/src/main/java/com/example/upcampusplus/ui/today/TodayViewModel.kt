package com.example.upcampusplus.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upcampusplus.domain.AreaRepository
import com.example.upcampusplus.domain.CompleteTask
import com.example.upcampusplus.domain.GetTodaySummary
import com.example.upcampusplus.domain.ReopenTask
import com.example.upcampusplus.domain.TaskRepository
import com.example.upcampusplus.domain.TodayOverview
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

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data object Error : TodayUiState
    data class Content(val overview: TodayOverview, val areaNames: Map<Int, String>) : TodayUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    getTodaySummary: GetTodaySummary,
    areas: AreaRepository,
    completeTask: CompleteTask,
    reopenTask: ReopenTask,
    tasks: TaskRepository,
) : ViewModel() {
    val actions = TaskActions(viewModelScope, completeTask, reopenTask, tasks)

    private val retries = MutableStateFlow(0)

    val state: StateFlow<TodayUiState> = retries
        .flatMapLatest {
            combine(getTodaySummary(), areas.observeAreas()) { overview, list ->
                TodayUiState.Content(overview, list.associate { it.id to it.name }) as TodayUiState
            }.catch { emit(TodayUiState.Error) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState.Loading)

    fun retry() = retries.update { it + 1 }
}
