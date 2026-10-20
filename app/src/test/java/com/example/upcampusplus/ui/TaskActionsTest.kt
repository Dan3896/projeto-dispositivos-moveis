package com.example.upcampusplus.ui

import com.example.upcampusplus.R
import com.example.upcampusplus.domain.CompleteTask
import com.example.upcampusplus.domain.ReopenTask
import com.example.upcampusplus.testing.FakeClock
import com.example.upcampusplus.testing.FakeTaskRepository
import com.example.upcampusplus.testing.MainDispatcherRule
import com.example.upcampusplus.testing.Today
import com.example.upcampusplus.testing.task
import com.example.upcampusplus.ui.common.TaskActions
import com.example.upcampusplus.ui.common.UserMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
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
class TaskActionsTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val clock = FakeClock()
    private val repo = FakeTaskRepository(listOf(task(1, due = Today)))

    private fun TestScope.actions() = TaskActions(this, CompleteTask(repo, clock), ReopenTask(repo, clock), repo)

    private suspend fun TaskActions.nextMessage(): UserMessage = messages.first()

    @Test
    fun `RF-008 concluir mostra +10 XP e Desfazer`() = runTest(main.dispatcher) {
        val actions = actions()
        actions.complete(1)
        advanceUntilIdle()
        val message = actions.nextMessage()
        assertEquals(R.string.msg_task_completed_xp, message.text.id)
        assertEquals(listOf<Any>(10), message.text.args)
        assertEquals(R.string.action_undo, message.actionLabel?.id)
        assertEquals(Today, repo.getTask(1)?.completedOn)
    }

    @Test
    fun `T-X3 concluir, desfazer e concluir soma 10 no total e a segunda vez nao mostra XP`() = runTest(main.dispatcher) {
        val actions = actions()
        actions.complete(1)
        advanceUntilIdle()
        actions.nextMessage().onAction!!.invoke()
        advanceUntilIdle()
        assertNull(repo.getTask(1)?.completedAt)

        actions.complete(1)
        advanceUntilIdle()
        assertEquals(R.string.msg_task_completed, actions.nextMessage().text.id)
        assertEquals(10, repo.xpByTask.value.values.sum())
    }

    @Test
    fun `T-X2 dois toques seguidos na caixa gravam uma vez`() = runTest(main.dispatcher) {
        val actions = actions()
        actions.complete(1)
        actions.complete(1)
        advanceUntilIdle()
        assertEquals(10, repo.xpByTask.value.values.sum())
    }

    @Test
    fun `RF-018 falha ao concluir deixa pendente, sem XP, com mensagem`() = runTest(main.dispatcher) {
        repo.failWrites = true
        val actions = actions()
        actions.complete(1)
        advanceUntilIdle()
        assertEquals(R.string.err_complete, actions.nextMessage().text.id)
        assertFalse(repo.getTask(1)!!.isCompleted)
        assertTrue(repo.xpByTask.value.isEmpty())
    }

    @Test
    fun `T-X4 remover tarefa concluida mantem o XP`() = runTest(main.dispatcher) {
        val actions = actions()
        actions.complete(1)
        advanceUntilIdle()
        actions.nextMessage()
        actions.remove(1)
        advanceUntilIdle()
        assertEquals(R.string.msg_task_removed, actions.nextMessage().text.id)
        assertNull(repo.getTask(1))
        assertEquals(10, repo.xpByTask.value.values.sum())
    }

    @Test
    fun `RF-009 reabrir mantem o XP`() = runTest(main.dispatcher) {
        val actions = actions()
        actions.complete(1)
        advanceUntilIdle()
        actions.setCompleted(1, completed = false)
        advanceUntilIdle()
        assertNotNull(repo.getTask(1))
        assertFalse(repo.getTask(1)!!.isCompleted)
        assertEquals(10, repo.xpByTask.value.values.sum())
    }
}
