package com.example.upcampusplus.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upcampusplus.domain.AppClock
import com.example.upcampusplus.domain.AreaRepository
import com.example.upcampusplus.domain.GetHistory
import com.example.upcampusplus.domain.HistoryView
import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.today
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Error : HistoryUiState
    data class Content(val history: HistoryView, val areas: List<Area>, val today: LocalDate, val zone: ZoneId) :
        HistoryUiState {
        val areaNames: Map<Int, String> = areas.associate { it.id to it.name }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(getHistory: GetHistory, areas: AreaRepository, private val clock: AppClock) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val retries = MutableStateFlow(0)

    val state: StateFlow<HistoryUiState> = retries
        .flatMapLatest {
            combine(getHistory(_query), areas.observeAreas()) { history, areaList ->
                HistoryUiState.Content(history, areaList, clock.today(), clock.zone()) as HistoryUiState
            }.catch { emit(HistoryUiState.Error) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState.Loading)

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun retry() = retries.update { it + 1 }
}
