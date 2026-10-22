package com.example.upcampusplus.ui.common

import com.example.upcampusplus.R
import com.example.upcampusplus.domain.CompleteTask
import com.example.upcampusplus.domain.ReopenTask
import com.example.upcampusplus.domain.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Concluir, reabrir e remover, compartilhados por Hoje e Tarefas.
 * Em falha nada muda (RN-13) e a pessoa vê uma mensagem (RF-018).
 */
class TaskActions(
    private val scope: CoroutineScope,
    private val completeTask: CompleteTask,
    private val reopenTask: ReopenTask,
    private val tasks: TaskRepository,
) {
    private val _messages = Channel<UserMessage>(Channel.BUFFERED)
    val messages: Flow<UserMessage> = _messages.receiveAsFlow()

    /** Ignora toques repetidos enquanto a gravação da mesma tarefa não termina. */
    private val inFlight = mutableSetOf<Long>()

    fun setCompleted(taskId: Long, completed: Boolean) {
        if (completed) complete(taskId) else reopen(taskId)
    }

    fun complete(taskId: Long) = runOnce(taskId, R.string.err_complete) {
        val result = completeTask(taskId)
        if (result.completed) {
            val text = if (result.xpGained > 0) {
                UiText(R.string.msg_task_completed_xp, listOf(result.xpGained))
            } else {
                UiText(R.string.msg_task_completed)
            }
            _messages.send(UserMessage(text, UiText(R.string.action_undo)) { reopen(taskId) })
        }
    }

    fun reopen(taskId: Long) = runOnce(taskId, R.string.err_reopen) {
        reopenTask(taskId)
    }

    fun remove(taskId: Long) = runOnce(taskId, R.string.err_remove) {
        tasks.deleteTask(taskId)
        _messages.send(UserMessage(UiText(R.string.msg_task_removed)))
    }

    private fun runOnce(taskId: Long, errorRes: Int, block: suspend () -> Unit) {
        if (!inFlight.add(taskId)) return
        scope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(UserMessage(UiText(errorRes)))
            } finally {
                inFlight.remove(taskId)
            }
        }
    }
}
