package com.example.upcampusplus.ui

import com.example.upcampusplus.R
import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.model.Priority
import com.example.upcampusplus.testing.FakeAreaRepository
import com.example.upcampusplus.testing.FakeClock
import com.example.upcampusplus.testing.FakeTaskRepository
import com.example.upcampusplus.testing.MainDispatcherRule
import com.example.upcampusplus.testing.Today
import com.example.upcampusplus.testing.at
import com.example.upcampusplus.testing.inboxTask
import com.example.upcampusplus.testing.task
import com.example.upcampusplus.ui.taskform.FormMode
import com.example.upcampusplus.ui.taskform.TaskFormArgs
import com.example.upcampusplus.ui.taskform.TaskFormEvent
import com.example.upcampusplus.ui.taskform.TaskFormViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskFormViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val clock = FakeClock()
    private val tasks = FakeTaskRepository()

    private fun viewModel(args: TaskFormArgs = TaskFormArgs()) =
        TaskFormViewModel(args, tasks, clock, FakeAreaRepository())

    @Test
    fun `RF-003 formulario abre com Faculdade, Media e hoje`() {
        val fields = viewModel().state.value.fields
        assertEquals(Area.FACULDADE, fields.areaId)
        assertEquals(Priority.MEDIUM, fields.priority)
        assertEquals(Today, fields.dueDate)
        assertEquals("", fields.title)
    }

    @Test
    fun `T-F4 aberto com filtro Projetos vem com Projetos, Media e hoje`() {
        val fields = viewModel(TaskFormArgs(areaId = Area.PROJETOS)).state.value.fields
        assertEquals(Area.PROJETOS, fields.areaId)
        assertEquals(Priority.MEDIUM, fields.priority)
        assertEquals(Today, fields.dueDate)
    }

    @Test
    fun `T-F1 salvar com titulo so com espacos nao grava e mostra erro`() = runTest(main.dispatcher) {
        val vm = viewModel()
        vm.onTitleChange("   ")
        vm.save()
        advanceUntilIdle()
        assertTrue(vm.state.value.showTitleError)
        assertEquals(0, tasks.createCalls)
        vm.onTitleChange("   a")
        assertFalse(vm.state.value.showTitleError)
    }

    @Test
    fun `T-F3 tocar Salvar 5 vezes cria uma tarefa, sem espacos nas pontas`() = runTest(main.dispatcher) {
        val vm = viewModel()
        vm.onTitleChange("  Estudar ViewModel  ")
        repeat(5) { vm.save() }
        advanceUntilIdle()
        assertEquals(1, tasks.createCalls)
        assertEquals("Estudar ViewModel", tasks.tasks.value.single().title)
        assertEquals(TaskFormEvent.Done(com.example.upcampusplus.ui.common.UiText(R.string.msg_task_created)), vm.events.first())
    }

    @Test
    fun `T-E2 falha ao salvar mantem o formulario aberto e preenchido`() = runTest(main.dispatcher) {
        tasks.failWrites = true
        val vm = viewModel()
        vm.onTitleChange("Estudar ViewModel")
        vm.save()
        advanceUntilIdle()
        val event = vm.events.first()
        assertTrue(event is TaskFormEvent.Error)
        assertEquals(R.string.err_save, (event as TaskFormEvent.Error).message.id)
        assertEquals("Estudar ViewModel", vm.state.value.fields.title)
        assertFalse(vm.state.value.saving)
        assertTrue(tasks.tasks.value.isEmpty())
    }

    @Test
    fun `RF-006 so pergunta antes de descartar se algo mudou`() {
        val vm = viewModel()
        assertFalse(vm.state.value.isDirty)
        vm.onPriorityChange(Priority.HIGH)
        assertTrue(vm.state.value.isDirty)
        vm.onPriorityChange(Priority.MEDIUM)
        assertFalse(vm.state.value.isDirty)
    }

    @Test
    fun `RF-005 e RN-09 editar tarefa concluida nao muda estado nem XP`() = runTest(main.dispatcher) {
        tasks.tasks.value = listOf(task(1, title = "Ler", completedAt = at(Today, 8, 0), completedOn = Today))
        tasks.xpByTask.value = mapOf(1L to 10)
        val vm = viewModel(TaskFormArgs(taskId = 1))
        advanceUntilIdle()
        assertEquals(FormMode.EDIT, vm.state.value.mode)
        assertFalse("concluída não volta ao Inbox", vm.state.value.canMoveToInbox)
        assertEquals("Ler", vm.state.value.fields.title)
        assertFalse(vm.state.value.isDirty)

        vm.onTitleChange("Ler capítulo")
        vm.onDueDateChange(Today.plusDays(3))
        vm.save()
        advanceUntilIdle()

        val saved = tasks.tasks.value.single()
        assertEquals("Ler capítulo", saved.title)
        assertEquals(Today.plusDays(3), saved.dueDate)
        assertNotNull(saved.completedAt)
        assertEquals(10, tasks.xpByTask.value.values.sum())
        assertEquals(R.string.msg_task_saved, (vm.events.first() as TaskFormEvent.Done).message.id)
    }

    @Test
    fun `INB RF-003 organizar abre sem padroes, exige area e aceita ficar sem data`() = runTest(main.dispatcher) {
        tasks.tasks.value = listOf(inboxTask(1, title = "Perguntar ao professor sobre a entrega"))
        val vm = viewModel(TaskFormArgs(taskId = 1))
        advanceUntilIdle()
        val state = vm.state.value
        assertEquals(FormMode.ORGANIZE, state.mode)
        assertEquals("Perguntar ao professor sobre a entrega", state.fields.title)
        assertNull(state.fields.areaId)
        assertNull(state.fields.dueDate)

        vm.save()
        advanceUntilIdle()
        assertTrue(vm.state.value.showAreaError)
        assertTrue("sem confirmação válida continua no Inbox", tasks.getTask(1)!!.isInInbox)

        vm.onAreaChange(Area.TRABALHO)
        assertFalse(vm.state.value.showAreaError)
        vm.save()
        advanceUntilIdle()
        val organized = tasks.getTask(1)!!
        assertTrue(organized.organized)
        assertEquals(Area.TRABALHO, organized.areaId)
        assertNull(organized.dueDate)
        assertEquals(R.string.msg_task_organized, (vm.events.first() as TaskFormEvent.Done).message.id)
    }

    @Test
    fun `INB 4-3 escolher area ou data sem confirmar nao tira do Inbox`() = runTest(main.dispatcher) {
        tasks.tasks.value = listOf(inboxTask(1))
        val vm = viewModel(TaskFormArgs(taskId = 1))
        advanceUntilIdle()
        vm.onAreaChange(Area.PESSOAL)
        vm.onDueDateChange(Today)
        assertTrue("cancelar deve pedir confirmação", vm.state.value.isDirty)
        // Fechar sem confirmar não grava nada.
        val task = tasks.getTask(1)!!
        assertTrue(task.isInInbox)
        assertNull(task.areaId)
        assertNull(task.dueDate)
    }

    @Test
    fun `INB 4-5 mover para o Inbox preserva area e data e nao muda o XP`() = runTest(main.dispatcher) {
        tasks.tasks.value = listOf(task(1, title = "Ler", areaId = Area.PROJETOS, due = Today.plusDays(2)))
        tasks.xpByTask.value = mapOf(1L to 10)
        val vm = viewModel(TaskFormArgs(taskId = 1))
        advanceUntilIdle()
        assertTrue(vm.state.value.canMoveToInbox)
        vm.moveToInbox()
        vm.moveToInbox()
        advanceUntilIdle()
        val moved = tasks.getTask(1)!!
        assertTrue(moved.isInInbox)
        assertEquals(Area.PROJETOS, moved.areaId)
        assertEquals(Today.plusDays(2), moved.dueDate)
        assertFalse(moved.isCompleted)
        assertEquals(10, tasks.xpByTask.value.values.sum())
        assertEquals(R.string.msg_moved_to_inbox, (vm.events.first() as TaskFormEvent.Done).message.id)
    }

    @Test
    fun `RN-21 nova tarefa pode ficar sem prazo`() = runTest(main.dispatcher) {
        val vm = viewModel()
        vm.onTitleChange("Ler sobre Compose")
        vm.onDueDateChange(null)
        vm.save()
        advanceUntilIdle()
        val created = tasks.tasks.value.single()
        assertNull(created.dueDate)
        assertTrue(created.organized)
        assertEquals(Area.FACULDADE, created.areaId)
    }
}
