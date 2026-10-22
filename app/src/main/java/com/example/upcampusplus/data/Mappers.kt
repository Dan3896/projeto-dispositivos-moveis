package com.example.upcampusplus.data

import com.example.upcampusplus.data.db.AreaEntity
import com.example.upcampusplus.data.db.TaskEntity
import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.model.Priority
import com.example.upcampusplus.domain.model.Task
import java.time.Instant
import java.time.LocalDate

// Datas como texto ISO yyyy-MM-dd: a ordem do texto é a ordem cronológica.
internal fun LocalDate.toDbString(): String = toString()
internal fun String.toLocalDate(): LocalDate = LocalDate.parse(this)

internal fun AreaEntity.toDomain() = Area(id, name, sortOrder)
internal fun Area.toEntity() = AreaEntity(id, name, sortOrder)

internal fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    areaId = areaId,
    priority = Priority.fromCode(priority),
    dueDate = dueDate?.toLocalDate(),
    organized = organized,
    completedAt = completedAt?.let(Instant::ofEpochMilli),
    completedOn = completedOn?.toLocalDate(),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)
