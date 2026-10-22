package com.example.upcampusplus.domain

import com.example.upcampusplus.domain.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Registro do Histórico (RN-17): a conclusão de uma tarefa existente. */
data class HistoryEntry(val taskId: Long, val text: String, val areaId: Int?, val at: Instant) {
    val key: String get() = "task-$taskId"
}

data class HistoryDay(val date: LocalDate, val entries: List<HistoryEntry>)

data class HistoryView(
    /** RN-18: registros por área em todo o período; não muda com a busca. */
    val areaCounts: Map<Int, Int>,
    /** Linha do tempo já filtrada pela busca, do dia mais recente ao mais antigo. */
    val days: List<HistoryDay>,
    val totalEntries: Int,
    val query: String,
)

/** RN-17: uma entrada por tarefa concluída, no momento da conclusão. */
fun historyEntries(tasks: List<Task>): List<HistoryEntry> =
    tasks.mapNotNull { task -> task.completedAt?.let { HistoryEntry(task.id, task.title, task.areaId, it) } }
        .sortedWith(compareByDescending<HistoryEntry> { it.at }.thenBy { it.key })

/** RN-19: termo sem espaços nas pontas, contido no texto, sem diferenciar maiúsculas. */
fun HistoryEntry.matches(query: String): Boolean {
    val term = query.trim()
    return term.isEmpty() || text.contains(term, ignoreCase = true)
}

fun buildHistory(tasks: List<Task>, zone: ZoneId, query: String): HistoryView {
    val all = historyEntries(tasks)
    val days = all.filter { it.matches(query) }
        .groupBy { it.at.atZone(zone).toLocalDate() }
        .map { (date, entries) -> HistoryDay(date, entries) }
        .sortedByDescending { it.date }
    return HistoryView(
        areaCounts = all.mapNotNull { it.areaId }.groupingBy { it }.eachCount(),
        days = days,
        totalEntries = all.size,
        query = query.trim(),
    )
}
