package com.example.upcampusplus.domain

import com.example.upcampusplus.domain.model.CompletionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class CompleteTask(private val tasks: TaskRepository, private val clock: AppClock) {
    suspend operator fun invoke(taskId: Long): CompletionResult =
        tasks.completeTask(taskId, at = clock.now(), on = clock.today())
}

class ReopenTask(private val tasks: TaskRepository, private val clock: AppClock) {
    suspend operator fun invoke(taskId: Long): Boolean = tasks.reopenTask(taskId, clock.now())
}

data class TodayOverview(val today: LocalDate, val summary: TodaySummary, val totalXp: Int)

class GetTodaySummary(private val tasks: TaskRepository, private val clock: AppClock) {
    operator fun invoke(): Flow<TodayOverview> =
        combine(tasks.observeTasks(), clock.todayFlow(), tasks.observeTotalXp()) { all, today, xp ->
            TodayOverview(today, summarizeToday(all, today), xp)
        }
}

class GetHistory(private val tasks: TaskRepository, private val clock: AppClock) {
    operator fun invoke(query: Flow<String>): Flow<HistoryView> =
        combine(tasks.observeTasks(), query, clock.todayFlow()) { all, term, _ ->
            buildHistory(all, clock.zone(), term)
        }
}
