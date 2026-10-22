package com.example.upcampusplus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.upcampusplus.AppContainer
import com.example.upcampusplus.TasksView
import com.example.upcampusplus.ui.common.LocalMessenger
import com.example.upcampusplus.ui.common.UserMessage
import com.example.upcampusplus.ui.history.HistoryScreen
import com.example.upcampusplus.ui.history.HistoryViewModel
import com.example.upcampusplus.ui.inbox.InboxViewModel
import com.example.upcampusplus.ui.taskform.TaskFormArgs
import com.example.upcampusplus.ui.taskform.TaskFormScreen
import com.example.upcampusplus.ui.taskform.TaskFormViewModel
import com.example.upcampusplus.ui.tasks.TasksScreen
import com.example.upcampusplus.ui.tasks.TasksViewModel
import com.example.upcampusplus.ui.today.TodayScreen
import com.example.upcampusplus.ui.today.TodayViewModel
import com.example.upcampusplus.ui.welcome.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val TODAY = "today"
    const val TASKS = "tasks"
    const val HISTORY = "history"

    private const val FORM = "task_form"
    const val ARG_TASK = "taskId"
    const val ARG_AREA = "areaId"
    const val TASK_FORM = "$FORM?$ARG_TASK={$ARG_TASK}&$ARG_AREA={$ARG_AREA}"

    // -1 representa "não informado" nos argumentos opcionais.
    fun taskForm(taskId: Long? = null, areaId: Int? = null) = "$FORM?$ARG_TASK=${taskId ?: -1}&$ARG_AREA=${areaId ?: -1}"
}

/**
 * Destinos da barra inferior. Voltar a partir de Tarefas ou Histórico leva a Hoje (RF-019),
 * e o estado de cada destino (rolagem, filtros) é guardado ao trocar.
 */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.TODAY) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun ClarityNavHost(
    navController: NavHostController,
    container: AppContainer,
    showWelcome: Boolean,
    onWelcomeDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = if (showWelcome) Routes.WELCOME else Routes.TODAY,
        modifier = modifier,
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(onStart = {
                onWelcomeDone()
                // RF-001: voltar a partir de Hoje fecha o app, sem retornar às boas-vindas.
                navController.navigate(Routes.TODAY) { popUpTo(Routes.WELCOME) { inclusive = true } }
            })
        }
        composable(Routes.TODAY) {
            val vm = viewModel {
                TodayViewModel(container.getTodaySummary, container.areas, container.completeTask, container.reopenTask, container.tasks)
            }
            TodayScreen(
                viewModel = vm,
                onNewTask = { navController.navigate(Routes.taskForm()) },
                onAllTasks = {
                    container.session.tasksView.value = TasksView.ALL
                    navController.navigateToTab(Routes.TASKS)
                },
                onEditTask = { navController.navigate(Routes.taskForm(taskId = it)) },
            )
        }
        composable(Routes.TASKS) {
            val vm = viewModel {
                TasksViewModel(
                    container.session.taskAreaFilter,
                    container.session.tasksView,
                    container.areas,
                    container.tasks,
                    container.clock,
                    container.completeTask,
                    container.reopenTask,
                )
            }
            val inboxVm = viewModel { InboxViewModel(container.tasks, container.clock, container.areas) }
            TasksScreen(
                viewModel = vm,
                inboxViewModel = inboxVm,
                onNewTask = { navController.navigate(Routes.taskForm(areaId = it)) },
                onEditTask = { navController.navigate(Routes.taskForm(taskId = it)) },
            )
        }
        composable(Routes.HISTORY) {
            val vm = viewModel { HistoryViewModel(container.getHistory, container.areas, container.clock) }
            HistoryScreen(vm)
        }
        composable(
            Routes.TASK_FORM,
            arguments = listOf(
                navArgument(Routes.ARG_TASK) { type = NavType.LongType; defaultValue = -1L },
                navArgument(Routes.ARG_AREA) { type = NavType.IntType; defaultValue = -1 },
            ),
        ) { entry ->
            val bundle = entry.arguments
            val args = TaskFormArgs(
                taskId = bundle?.getLong(Routes.ARG_TASK)?.takeIf { it >= 0 },
                areaId = bundle?.getInt(Routes.ARG_AREA)?.takeIf { it >= 0 },
            )
            val vm = viewModel {
                TaskFormViewModel(args, container.tasks, container.clock, container.areas)
            }
            val messenger = LocalMessenger.current
            TaskFormScreen(vm, onClose = { message ->
                // Evita fechar duas vezes se o evento chegar durante a animação de saída.
                if (navController.currentBackStackEntry?.id == entry.id) {
                    navController.popBackStack()
                    message?.let { messenger.show(UserMessage(it)) }
                }
            })
        }
    }
}
