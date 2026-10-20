package com.example.upcampusplus.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {
    @Query("SELECT * FROM area ORDER BY sort_order")
    fun observeAll(): Flow<List<AreaEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(areas: List<AreaEntity>)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM task")
    fun observeAll(): Flow<List<TaskEntity>>

    /** O Inbox não tem tabela própria: é a consulta pelo estado de organização. */
    @Query("SELECT * FROM task WHERE organized = 0 ORDER BY created_at DESC, id DESC")
    fun observeUnorganized(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Query(
        "UPDATE task SET title = :title, area_id = :areaId, priority = :priority, due_date = :dueDate, " +
            "updated_at = :now WHERE id = :id",
    )
    suspend fun updateFields(id: Long, title: String, areaId: Int?, priority: Int, dueDate: String?, now: Long): Int

    /** Só organiza o que está no Inbox: um segundo toque não muda nada. */
    @Query("UPDATE task SET organized = 1, updated_at = :now WHERE id = :id AND organized = 0")
    suspend fun markOrganized(id: Long, now: Long): Int

    /** Concluída não volta ao Inbox: não existe tarefa não organizada e concluída. */
    @Query(
        "UPDATE task SET organized = 0, updated_at = :now " +
            "WHERE id = :id AND organized = 1 AND completed_at IS NULL",
    )
    suspend fun markUnorganized(id: Long, now: Long): Int

    /** Só conclui se ainda está pendente e organizada: um segundo toque não muda nada. */
    @Query(
        "UPDATE task SET completed_at = :at, completed_on = :on, updated_at = :at " +
            "WHERE id = :id AND completed_at IS NULL AND organized = 1",
    )
    suspend fun markCompleted(id: Long, at: Long, on: String): Int

    @Query(
        "UPDATE task SET completed_at = NULL, completed_on = NULL, updated_at = :now " +
            "WHERE id = :id AND completed_at IS NOT NULL",
    )
    suspend fun markPending(id: Long, now: Long): Int

    @Query("DELETE FROM task WHERE id = :id")
    suspend fun delete(id: Long): Int
}

@Dao
interface XpDao {
    /** Devolve -1 quando a tarefa já tem registro (índice único em task_id). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entry: XpEntryEntity): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_entry")
    fun observeTotal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM xp_entry WHERE task_id = :taskId")
    suspend fun countForTask(taskId: Long): Int
}
