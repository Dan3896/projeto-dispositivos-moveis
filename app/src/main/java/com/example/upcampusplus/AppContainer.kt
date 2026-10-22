package com.example.upcampusplus

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.upcampusplus.data.DataStoreOnboardingRepository
import com.example.upcampusplus.data.RoomAreaRepository
import com.example.upcampusplus.data.RoomTaskRepository
import com.example.upcampusplus.data.db.ClarityDatabase
import com.example.upcampusplus.domain.AppClock
import com.example.upcampusplus.domain.AreaRepository
import com.example.upcampusplus.domain.CompleteTask
import com.example.upcampusplus.domain.GetHistory
import com.example.upcampusplus.domain.GetTodaySummary
import com.example.upcampusplus.domain.OnboardingRepository
import com.example.upcampusplus.domain.ReopenTask
import com.example.upcampusplus.domain.SystemAppClock
import com.example.upcampusplus.domain.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow

private val Context.settings by preferencesDataStore(name = "settings")

/** Visões da tela Tarefas (PRD do Inbox, seção 3). */
enum class TasksView { INBOX, ALL }

/** Estado que vale só enquanto o processo vive (RF-015: filtro mantido na sessão). */
class SessionState {
    /** Área do filtro em Tarefas; nulo = Todas. */
    val taskAreaFilter = MutableStateFlow<Int?>(null)

    /** Última visão aberta em Tarefas. */
    val tasksView = MutableStateFlow(TasksView.ALL)
}

/** Injeção de dependências manual (D-05). */
class AppContainer(
    val clock: AppClock,
    val areas: AreaRepository,
    val tasks: TaskRepository,
    val onboarding: OnboardingRepository,
    val session: SessionState = SessionState(),
) {
    val completeTask = CompleteTask(tasks, clock)
    val reopenTask = ReopenTask(tasks, clock)
    val getTodaySummary = GetTodaySummary(tasks, clock)
    val getHistory = GetHistory(tasks, clock)

    companion object {
        fun create(context: Context): AppContainer {
            val db = ClarityDatabase.create(context)
            return AppContainer(
                clock = SystemAppClock,
                areas = RoomAreaRepository(db),
                tasks = RoomTaskRepository(db),
                onboarding = DataStoreOnboardingRepository(context.settings),
            )
        }
    }
}
