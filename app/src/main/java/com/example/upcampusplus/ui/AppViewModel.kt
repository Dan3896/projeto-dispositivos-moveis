package com.example.upcampusplus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upcampusplus.domain.AreaRepository
import com.example.upcampusplus.domain.OnboardingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface StartupState {
    data object Loading : StartupState
    data object Error : StartupState
    data class Ready(val showWelcome: Boolean) : StartupState
}

/** F1: cria as áreas (RF-002) e decide se mostra as boas-vindas (RF-001). */
class AppViewModel(private val areas: AreaRepository, private val onboarding: OnboardingRepository) : ViewModel() {
    private val _state = MutableStateFlow<StartupState>(StartupState.Loading)
    val state: StateFlow<StartupState> = _state.asStateFlow()

    init {
        start()
    }

    fun start() {
        _state.value = StartupState.Loading
        viewModelScope.launch {
            _state.value = try {
                areas.ensureDefaults()
                StartupState.Ready(showWelcome = !onboarding.onboardingDone.first())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                StartupState.Error
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            // Se falhar, as boas-vindas aparecem de novo no próximo acesso; nada se perde.
            runCatching { onboarding.markDone() }
        }
    }
}
