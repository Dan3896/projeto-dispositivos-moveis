package com.example.upcampusplus.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "area")
data class AreaEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

@Entity(
    tableName = "task",
    foreignKeys = [
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["area_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("area_id"), Index("due_date")],
)
data class TaskEntity(
    // autoGenerate gera AUTOINCREMENT: ids de tarefas removidas nunca são reutilizados.
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** Nulo só em tarefa não organizada. */
    @ColumnInfo(name = "area_id") val areaId: Int?,
    @ColumnInfo(defaultValue = "1") val priority: Int,
    /** yyyy-MM-dd; nulo = sem data. */
    @ColumnInfo(name = "due_date") val dueDate: String?,
    /** Estado de organização, independente da conclusão: 0 = no Inbox, 1 = organizada. */
    @ColumnInfo(defaultValue = "1") val organized: Boolean,
    /** Milissegundos UTC; nulo = Pendente. */
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    /** Data local da conclusão (yyyy-MM-dd); nulo junto com completed_at. */
    @ColumnInfo(name = "completed_on") val completedOn: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** Um registro por tarefa, sem FK, para sobreviver à remoção da tarefa (RN-06). */
@Entity(tableName = "xp_entry", indices = [Index(value = ["task_id"], unique = true)])
data class XpEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "task_id") val taskId: Long,
    val amount: Int,
    @ColumnInfo(name = "earned_at") val earnedAt: Long,
)
