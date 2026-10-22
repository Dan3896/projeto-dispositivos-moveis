package com.example.upcampusplus.data

import androidx.room.withTransaction
import com.example.upcampusplus.data.db.ClarityDatabase
import com.example.upcampusplus.data.db.TaskEntity
import com.example.upcampusplus.data.db.XpEntryEntity
import com.example.upcampusplus.domain.AreaRepository
import com.example.upcampusplus.domain.TaskRepository
import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.model.CompletionResult
import com.example.upcampusplus.domain.model.Priority
import com.example.upcampusplus.domain.model.Task
import com.example.upcampusplus.domain.model.TaskDraft
import com.example.upcampusplus.domain.model.XP_PER_TASK
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate

class RoomAreaRepository(private val db: ClarityDatabase) : AreaRepository {
    override fun observeAreas(): Flow<List<Area>> =
        db.areaDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun ensureDefaults() {
        db.areaDao().insertIfMissing(Area.DEFAULTS.map { it.toEntity() })
    }
}

class RoomTaskRepository(private val db: ClarityDatabase) : TaskRepository {
    private val tasks = db.taskDao()
    private val xp = db.xpDao()

    override fun observeTasks(): Flow<List<Task>> = tasks.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeInbox(): Flow<List<Task>> =
        tasks.observeUnorganized().map { list -> list.map { it.toDomain() } }

    override fun observeTotalXp(): Flow<Int> = xp.observeTotal()

    override suspend fun getTask(id: Long): Task? = tasks.getById(id)?.toDomain()

    override suspend fun createTask(draft: TaskDraft, now: Instant): Long {
        requireNotNull(draft.areaId) { "Tarefa organizada precisa de área" }
        val millis = now.toEpochMilli()
        return tasks.insert(
            TaskEntity(
                title = draft.title,
                areaId = draft.areaId,
                priority = draft.priority.code,
                dueDate = draft.dueDate?.toDbString(),
                organized = true,
                createdAt = millis,
                updatedAt = millis,
            ),
        )
    }

    override suspend fun captureTask(title: String, now: Instant): Long {
        val millis = now.toEpochMilli()
        return tasks.insert(
            TaskEntity(
                title = title,
                areaId = null,
                priority = Priority.MEDIUM.code,
                dueDate = null,
                organized = false,
                createdAt = millis,
                updatedAt = millis,
            ),
        )
    }

    override suspend fun updateTask(id: Long, draft: TaskDraft, now: Instant): Boolean = writeFields(id, draft, now)

    override suspend fun organizeTask(id: Long, draft: TaskDraft, now: Instant): Boolean {
        requireNotNull(draft.areaId) { "Organizar exige área" }
        return db.withTransaction {
            tasks.markOrganized(id, now.toEpochMilli()) > 0 && writeFields(id, draft, now)
        }
    }

    override suspend fun moveToInbox(id: Long, draft: TaskDraft, now: Instant): Boolean =
        db.withTransaction {
            tasks.markUnorganized(id, now.toEpochMilli()) > 0 && writeFields(id, draft, now)
        }

    override suspend fun completeTask(id: Long, at: Instant, on: LocalDate): CompletionResult =
        db.withTransaction {
            val millis = at.toEpochMilli()
            if (tasks.markCompleted(id, millis, on.toDbString()) == 0) {
                return@withTransaction CompletionResult(completed = false, xpGained = 0)
            }
            val row = xp.insertIfAbsent(XpEntryEntity(taskId = id, amount = XP_PER_TASK, earnedAt = millis))
            CompletionResult(completed = true, xpGained = if (row == -1L) 0 else XP_PER_TASK)
        }

    override suspend fun reopenTask(id: Long, now: Instant): Boolean = tasks.markPending(id, now.toEpochMilli()) > 0

    override suspend fun deleteTask(id: Long): Boolean = tasks.delete(id) > 0

    private suspend fun writeFields(id: Long, draft: TaskDraft, now: Instant): Boolean =
        tasks.updateFields(
            id = id,
            title = draft.title,
            areaId = draft.areaId,
            priority = draft.priority.code,
            dueDate = draft.dueDate?.toDbString(),
            now = now.toEpochMilli(),
        ) > 0
}
