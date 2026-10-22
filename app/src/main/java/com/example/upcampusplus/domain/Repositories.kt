package com.example.upcampusplus.domain

import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.model.CompletionResult
import com.example.upcampusplus.domain.model.Task
import com.example.upcampusplus.domain.model.TaskDraft
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

/**
 * Contratos da camada de dados. Toda gravação é atômica (RN-13): ou muda tudo, ou lança
 * exceção sem mudar nada.
 */
interface AreaRepository {
    fun observeAreas(): Flow<List<Area>>

    /** Cria as quatro áreas se ainda não existem, sem duplicar (RF-002). */
    suspend fun ensureDefaults()
}

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>

    /** Só as tarefas não organizadas, consultadas pelo estado de organização (PRD do Inbox, seção 7). */
    fun observeInbox(): Flow<List<Task>>

    /** RN-05: soma dos registros de XP. */
    fun observeTotalXp(): Flow<Int>

    suspend fun getTask(id: Long): Task?

    /** Cria uma tarefa já organizada; a área é obrigatória (RN-21). */
    suspend fun createTask(draft: TaskDraft, now: Instant): Long

    /** RN-15: captura no Inbox só com o título; sem área, sem data, prioridade Média. */
    suspend fun captureTask(title: String, now: Instant): Long

    /** RN-09: muda só os campos editáveis; organização e conclusão ficam. Falso se a tarefa não existe mais. */
    suspend fun updateTask(id: Long, draft: TaskDraft, now: Instant): Boolean

    /** RN-16: grava os campos e marca como organizada. Falso se não existe ou já estava organizada. */
    suspend fun organizeTask(id: Long, draft: TaskDraft, now: Instant): Boolean

    /**
     * RN-22: grava os campos e devolve ao Inbox, preservando área e data. Só para tarefa organizada
     * e pendente; conclusão e XP não mudam. Falso caso contrário.
     */
    suspend fun moveToInbox(id: Long, draft: TaskDraft, now: Instant): Boolean

    /** RN-03: grava a conclusão e, se for a primeira, o XP, na mesma transação. Tarefa no Inbox não conclui (RN-20). */
    suspend fun completeTask(id: Long, at: Instant, on: LocalDate): CompletionResult

    /** RN-04: volta para Pendente e limpa a conclusão; o XP fica. Falso se já estava pendente. */
    suspend fun reopenTask(id: Long, now: Instant): Boolean

    /** RN-06: o XP da tarefa permanece. Some de todas as visões, pois é o mesmo registro. */
    suspend fun deleteTask(id: Long): Boolean
}

interface OnboardingRepository {
    val onboardingDone: Flow<Boolean>
    suspend fun markDone()
}
