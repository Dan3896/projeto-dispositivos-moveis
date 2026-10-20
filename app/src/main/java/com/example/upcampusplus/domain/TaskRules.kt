package com.example.upcampusplus.domain

import com.example.upcampusplus.domain.model.Task
import java.time.LocalDate

/** RN-07: atrasada = organizada, pendente e com prazo anterior a hoje. Concluída nunca é atrasada. */
fun Task.isOverdue(today: LocalDate): Boolean =
    organized && !isCompleted && dueDate != null && dueDate < today

/** RN-12, tela Hoje: prioridade, prazo crescente, criação. */
val todayOrder: Comparator<Task> =
    compareBy<Task>({ it.priority.code }, { it.dueDate }, { it.createdAt }, { it.id })

/** RN-12, Tarefas: pendentes por prazo crescente (sem prazo por último), depois prioridade. */
val pendingOrder: Comparator<Task> =
    compareBy<Task>({ it.dueDate == null }, { it.dueDate }, { it.priority.code }, { it.createdAt }, { it.id })

/** RN-12, Tarefas: concluídas da conclusão mais recente para a mais antiga. */
val completedOrder: Comparator<Task> =
    compareByDescending<Task> { it.completedAt }.thenByDescending { it.id }

/** Inbox: capturas mais recentes primeiro. */
val inboxOrder: Comparator<Task> =
    compareByDescending<Task> { it.createdAt }.thenByDescending { it.id }

data class TodaySummary(
    /** Pendentes com prazo até hoje, na ordem da tela Hoje (inclui atrasadas). */
    val dueToday: List<Task>,
    val overdueCount: Int,
    val completedTodayCount: Int,
) {
    val dueTodayCount: Int get() = dueToday.size
}

/** RN-08: contadores do dia e lista de Hoje. Tarefas no Inbox ou sem prazo ficam de fora. */
fun summarizeToday(tasks: List<Task>, today: LocalDate): TodaySummary {
    val due = tasks
        .filter { it.organized && !it.isCompleted && it.dueDate != null && it.dueDate <= today }
        .sortedWith(todayOrder)
    return TodaySummary(
        dueToday = due,
        overdueCount = due.count { it.isOverdue(today) },
        completedTodayCount = tasks.count { it.completedOn == today },
    )
}

data class TaskSections(val pending: List<Task>, val completed: List<Task>)

/**
 * Tela Tarefas, visão Todas: organizadas e não organizadas, cada uma uma vez. O filtro por área
 * (nulo = Todas) vale para as duas seções; tarefa sem área só aparece em Todas (RF-014, RF-015).
 */
fun splitSections(tasks: List<Task>, areaId: Int?): TaskSections {
    val filtered = if (areaId == null) tasks else tasks.filter { it.areaId == areaId }
    val (completed, pending) = filtered.partition { it.isCompleted }
    return TaskSections(pending.sortedWith(pendingOrder), completed.sortedWith(completedOrder))
}

/** Visão Inbox: tarefas não organizadas, mais recentes primeiro. */
fun inboxTasks(tasks: List<Task>): List<Task> = tasks.filter { it.isInInbox }.sortedWith(inboxOrder)
