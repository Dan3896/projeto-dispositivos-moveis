package com.example.upcampusplus.ui

import com.example.upcampusplus.R
import com.example.upcampusplus.testing.FakeAreaRepository
import com.example.upcampusplus.testing.FakeClock
import com.example.upcampusplus.testing.FakeTaskRepository
import com.example.upcampusplus.testing.MainDispatcherRule
import com.example.upcampusplus.testing.inboxTask
import com.example.upcampusplus.testing.task
import com.example.upcampusplus.ui.inbox.InboxUiState
import com.example.upcampusplus.ui.inbox.InboxViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val clock = FakeClock()
    private val tasks = FakeTaskRepository()
    // Lazy: o viewModelScope precisa do Main de teste, instalado pela regra depois da construção.
    private val vm by lazy { InboxViewModel(tasks, clock, FakeAreaRepository()) }

    @Test
    fun `INB RF-002 titulo vazio ou so com espacos desabilita o botao`() {
        assertFalse(vm.input.value.canSubmit)
        vm.onInputChange("   ")
        assertFalse(vm.input.value.canSubmit)
        vm.onInputChange("Lembrar do professor")
        assertTrue(vm.input.value.canSubmit)
    }

    @Test
    fun `INB RF-002 captura so com titulo, sem area nem data, com data de criacao`() = runTest(main.dispatcher) {
        vm.onInputChange("  Perguntar ao professor sobre o formato da entrega  ")
        vm.capture()
        vm.capture()
        advanceUntilIdle()
        assertEquals("toque duplo cria uma só", 1, tasks.captureCalls)
        assertEquals("", vm.input.value.text)
        val created = tasks.tasks.value.single()
        assertEquals("Perguntar ao professor sobre o formato da entrega", created.title)
        assertTrue(created.isInInbox)
        assertNull(created.areaId)
        assertNull(created.dueDate)
        assertEquals(clock.now(), created.createdAt)
        assertEquals(R.string.msg_captured, vm.messages.first().text.id)
    }

    @Test
    fun `INB RF-002 falha de gravacao mantem o texto no campo e avisa`() = runTest(main.dispatcher) {
        tasks.failWrites = true
        vm.onInputChange("Texto")
        vm.capture()
        advanceUntilIdle()
        assertEquals("Texto", vm.input.value.text)
        assertFalse(vm.input.value.saving)
        assertEquals(R.string.err_capture, vm.messages.first().text.id)
    }

    @Test
    fun `T-F2 captura aceita no maximo 100 caracteres`() {
        vm.onInputChange("x".repeat(150))
        assertEquals(100, vm.input.value.text.length)
    }

    @Test
    fun `INB RF-001 lista so as nao organizadas e atualiza apos organizar`() = runTest(main.dispatcher) {
        tasks.tasks.value = listOf(inboxTask(1), task(2), inboxTask(3))
        // A data é conferida em laço infinito; advanceUntilIdle não terminaria enquanto o estado é coletado.
        backgroundScope.launch { vm.state.collect {} }
        runCurrent()
        assertEquals(listOf(3L, 1L), (vm.state.value as InboxUiState.Content).tasks.map { it.id })

        tasks.tasks.value = tasks.tasks.value.map { if (it.id == 3L) it.copy(organized = true, areaId = 1) else it }
        runCurrent()
        assertEquals(listOf(1L), (vm.state.value as InboxUiState.Content).tasks.map { it.id })
    }

    @Test
    fun `INB RF-007 remover tira a tarefa de todas as visoes`() = runTest(main.dispatcher) {
        tasks.tasks.value = listOf(inboxTask(1), task(2))
        vm.remove(1)
        vm.remove(1)
        advanceUntilIdle()
        assertEquals(listOf(2L), tasks.tasks.value.map { it.id })
        assertEquals(R.string.msg_task_removed, vm.messages.first().text.id)
    }
}
