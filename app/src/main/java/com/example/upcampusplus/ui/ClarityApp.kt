package com.example.upcampusplus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.upcampusplus.AppContainer
import com.example.upcampusplus.R
import com.example.upcampusplus.ui.common.LocalMessenger
import com.example.upcampusplus.ui.common.Messenger
import com.example.upcampusplus.ui.components.ErrorState
import com.example.upcampusplus.ui.components.LoadingState
import com.example.upcampusplus.ui.navigation.ClarityNavHost
import com.example.upcampusplus.ui.navigation.Routes
import com.example.upcampusplus.ui.navigation.navigateToTab
import kotlinx.coroutines.launch

/**
 * RF-019: enquanto a extensão não estiver pronta, a barra mostra só Hoje e Tarefas, e Tarefas
 * mostra só a visão Todas (sem a aba Inbox e sem "Mover para o Inbox").
 */
const val EXTENSION_ENABLED = true

private data class Tab(val route: String, val label: Int, val icon: ImageVector, val selectedIcon: ImageVector)

private val CoreTabs = listOf(
    Tab(Routes.TODAY, R.string.nav_today, Icons.Outlined.Today, Icons.Filled.Today),
    Tab(Routes.TASKS, R.string.nav_tasks, Icons.AutoMirrored.Outlined.ListAlt, Icons.AutoMirrored.Filled.ListAlt),
)
private val ExtensionTabs = listOf(
    Tab(Routes.HISTORY, R.string.nav_history, Icons.Outlined.History, Icons.Filled.History),
)
private val Tabs = if (EXTENSION_ENABLED) CoreTabs + ExtensionTabs else CoreTabs

@Composable
fun ClarityApp(container: AppContainer) {
    val appViewModel: AppViewModel = viewModel { AppViewModel(container.areas, container.onboarding) }
    val startup by appViewModel.state.collectAsStateWithLifecycle()

    when (val s = startup) {
        StartupState.Loading -> Box(Modifier.fillMaxSize()) { LoadingState() }
        StartupState.Error -> ErrorState(stringResource(R.string.startup_error), onRetry = appViewModel::start)
        is StartupState.Ready -> MainScaffold(
            container = container,
            showWelcome = s.showWelcome,
            onWelcomeDone = appViewModel::completeOnboarding,
        )
    }
}

@Composable
private fun MainScaffold(container: AppContainer, showWelcome: Boolean, onWelcomeDone: () -> Unit) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalContext.current.resources

    // Snackbar na raiz: sobrevive à troca de tela; a mais nova substitui a anterior.
    val messenger = remember(snackbarHostState) {
        Messenger { message ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = message.text.resolve(resources),
                    actionLabel = message.actionLabel?.resolve(resources),
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) message.onAction?.invoke()
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val showBottomBar = Tabs.any { tab -> destination?.hierarchy?.any { it.route == tab.route } == true }

    CompositionLocalProvider(LocalMessenger provides messenger) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            // O cabeçalho de cada tela cuida da barra de status; aqui só a barra de navegação do sistema.
            contentWindowInsets = WindowInsets.navigationBars,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        Tabs.forEach { tab ->
                            val selected = destination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navController.navigateToTab(tab.route) },
                                icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                                label = { NavLabel(stringResource(tab.label)) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            ClarityNavHost(
                navController = navController,
                container = container,
                showWelcome = showWelcome,
                onWelcomeDone = onWelcomeDone,
                // consumeWindowInsets evita somar a barra do sistema duas vezes com o teclado aberto.
                modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            )
        }
    }
}

/** Rótulo da barra limitado a 1,3x da fonte: em 200% "Histórico" quebraria no meio da palavra. */
@Composable
private fun NavLabel(text: String) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale.coerceAtMost(1.3f))) {
        Text(text, maxLines = 1, softWrap = false)
    }
}
