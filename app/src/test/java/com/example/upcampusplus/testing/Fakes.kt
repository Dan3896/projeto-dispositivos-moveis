package com.example.upcampusplus.testing

import com.example.upcampusplus.domain.AppClock
import com.example.upcampusplus.domain.AreaRepository
import com.example.upcampusplus.domain.TaskRepository
import com.example.upcampusplus.domain.inboxOrder
import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.model.CompletionResult
import com.example.upcampusplus.domain.model.Priority
import com.example.upcampusplus.domain.model.Task
import com.example.upcampusplus.domain.model.TaskDraft
import com.example.upcampusplus.domain.model.XP_PER_TASK
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

val SaoPaulo: ZoneId = ZoneId.of("America/Sao_Paulo")

/** 15/10/2026, como nos exemplos do PRD. */
val Today: LocalDate = LocalDate.of(2026, 10, 15)

class FakeClock(var instant: Instant = at(Today, 10, 0), var zoneId: ZoneId = SaoPaulo) : AppClock {
    override fun now(): Instant = instant
    override fun zone(): ZoneId = zoneId

    fun set(date: LocalDate, hour: Int, minute: Int) {
        instant = at(date, hour, minute, zoneId)
    }
}

fun at(date: LocalDate, hour: Int, minute: Int, zone: ZoneId = SaoPaulo): Instant =
    LocalDateTime.of(date, java.time.LocalTime.of(hour, minute)).atZone(zone).toInstant()

fun task(
    id: Long,
    due: LocalDate? = Today,
    priority: Priority = Priority.MEDIUM,
    areaId: Int? = Area.FACULDADE,
    title: String = "Tarefa $id",
    organized: Boolean = true,
    completedAt: Instant? = null,
    completedOn: LocalDate? = null,
    createdAt: Instant = Instant.ofEpochMilli(id),
) = Task(id, title, areaId, priority, due, organized, completedAt, completedOn, createdAt, createdAt)

/** Tarefa capturada no Inbox: sem área, sem data. */
fun inboxTask(id: Long, title: String = "Captura $id", createdAt: Instant = Instant.ofEpochMilli(id)) =
    task(id, due = null, areaId = null, title = title, organized = false, createdAt = createdAt)

class FakeAreaRepository : AreaRepository {
    var failEnsure = false
    override fun observeAreas(): Flow<List<Area>> = flowOf(Area.DEFAULTS)
    override suspend fun ensureDefaults() {
        if (failEnsure) throw IOException("falha simulada")
    }
}

/** Mesmas regras do Room em memória, com falhas simuladas para RN-13. */
class FakeTaskRepository(initial: List<Task> = emptyList()) : TaskRepository {
    val tasks = MutableStateFlow(initial)
    val xpByTask = MutableStateFlow<Map<Long, Int>>(emptyMap())
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    var failWrites = false
    var createCalls = 0
    var captureCalls = 0

    override fun observeTasks(): Flow<List<Task>> = tasks
    override fun observeInbox(): Flow<List<Task>> = tasks.map { list -> list.filter { it.isInInbox }.sortedWith(inboxOrder) }
    override fun observeTotalXp(): Flow<Int> = xpByTask.map { it.values.sum() }
    override suspend fun getTask(id: Long): Task? = tasks.value.firstOrNull { it.id == id }

    override suspend fun createTask(draft: TaskDraft, now: Instant): Long {
        createCalls++
        failIfNeeded()
        requireNotNull(draft.areaId)
        val id = nextId++
        tasks.value += Task(id, draft.title, draft.areaId, draft.priority, draft.dueDate, true, null, null, now, now)
        return id
    }

    override suspend fun captureTask(title: String, now: Instant): Long {
        captureCalls++
        failIfNeeded()
        val id = nextId++
        tasks.value += Task(id, title, null, Priority.MEDIUM, null, false, null, null, now, now)
        return id
    }

    override suspend fun organizeTask(id: Long, draft: TaskDraft, now: Instant): Boolean {
        failIfNeeded()
        requireNotNull(draft.areaId)
        val existing = getTask(id)?.takeIf { it.isInInbox } ?: return false
        replace(existing.withDraft(draft, now).copy(organized = true))
        return true
    }

    override suspend fun moveToInbox(id: Long, draft: TaskDraft, now: Instant): Boolean {
        failIfNeeded()
        val existing = getTask(id)?.takeIf { it.organized && !it.isCompleted } ?: return false
        replace(existing.withDraft(draft, now).copy(organized = false))
        return true
    }

    override suspend fun updateTask(id: Long, draft: TaskDraft, now: Instant): Boolean {
        failIfNeeded()
        val existing = getTask(id) ?: return false
        replace(existing.withDraft(draft, now))
        return true
    }

    override suspend fun completeTask(id: Long, at: Instant, on: LocalDate): CompletionResult {
        failIfNeeded()
        val existing = getTask(id)
        if (existing == null || existing.isCompleted || existing.isInInbox) return CompletionResult(false, 0)
        replace(existing.copy(completedAt = at, completedOn = on))
        if (id in xpByTask.value) return CompletionResult(true, 0)
        xpByTask.value += id to XP_PER_TASK
        return CompletionResult(true, XP_PER_TASK)
    }

    override suspend fun reopenTask(id: Long, now: Instant): Boolean {
        failIfNeeded()
        val existing = getTask(id)?.takeIf { it.isCompleted } ?: return false
        replace(existing.copy(completedAt = null, completedOn = null))
        return true
    }

    override suspend fun deleteTask(id: Long): Boolean {
        failIfNeeded()
        val before = tasks.value.size
        tasks.value = tasks.value.filterNot { it.id == id }
        return tasks.value.size < before
    }

    private fun Task.withDraft(draft: TaskDraft, now: Instant) =
        copy(title = draft.title, areaId = draft.areaId, priority = draft.priority, dueDate = draft.dueDate, updatedAt = now)

    private fun replace(task: Task) {
        tasks.value = tasks.value.map { if (it.id == task.id) task else it }
    }

    private fun failIfNeeded() {
        if (failWrites) throw IOException("falha simulada")
    }
}
