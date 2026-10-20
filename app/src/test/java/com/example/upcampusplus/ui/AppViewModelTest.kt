package com.example.upcampusplus.ui

import com.example.upcampusplus.domain.OnboardingRepository
import com.example.upcampusplus.testing.FakeAreaRepository
import com.example.upcampusplus.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private class FakeOnboarding : OnboardingRepository {
        val done = MutableStateFlow(false)
        override val onboardingDone: Flow<Boolean> = done
        override suspend fun markDone() {
            done.value = true
        }
    }

    @Test
    fun `T-O1 boas-vindas so no primeiro acesso`() = runTest(main.dispatcher) {
        val onboarding = FakeOnboarding()
        val first = AppViewModel(FakeAreaRepository(), onboarding)
        advanceUntilIdle()
        assertEquals(StartupState.Ready(showWelcome = true), first.state.value)

        first.completeOnboarding()
        advanceUntilIdle()

        val reopened = AppViewModel(FakeAreaRepository(), onboarding)
        advanceUntilIdle()
        assertEquals(StartupState.Ready(showWelcome = false), reopened.state.value)
    }

    @Test
    fun `F1 falha ao criar areas mostra erro e Tentar de novo funciona`() = runTest(main.dispatcher) {
        val areas = FakeAreaRepository().apply { failEnsure = true }
        val vm = AppViewModel(areas, FakeOnboarding())
        advanceUntilIdle()
        assertEquals(StartupState.Error, vm.state.value)

        areas.failEnsure = false
        vm.start()
        advanceUntilIdle()
        assertEquals(StartupState.Ready(showWelcome = true), vm.state.value)
    }
}
