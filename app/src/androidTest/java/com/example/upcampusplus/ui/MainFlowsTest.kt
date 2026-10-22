package com.example.upcampusplus.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.upcampusplus.AppContainer
import com.example.upcampusplus.data.RoomAreaRepository
import com.example.upcampusplus.data.RoomTaskRepository
import com.example.upcampusplus.data.db.ClarityDatabase
import com.example.upcampusplus.domain.OnboardingRepository
import com.example.upcampusplus.domain.SystemAppClock
import com.example.upcampusplus.ui.theme.ClarityTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Testes de interface recomendados para F1, F3 e F4, com banco em memória. */
@RunWith(AndroidJUnit4::class)
class MainFlowsTest {
    @get:Rule
    val compose = createComposeRule()

    private class MemoryOnboarding(done: Boolean) : OnboardingRepository {
        val done = MutableStateFlow(done)
        override val onboardingDone: Flow<Boolean> = this.done
        override suspend fun markDone() {
            done.value = true
        }
    }

    private lateinit var db: ClarityDatabase
    private lateinit var onboarding: MemoryOnboarding
    private lateinit var container: AppContainer

    private fun start(onboarded: Boolean) {
        onboarding = MemoryOnboarding(onboarded)
        container = AppContainer(
            clock = SystemAppClock,
            areas = RoomAreaRepository(db),
            tasks = RoomTaskRepository(db),
            onboarding = onboarding,
        )
        compose.setContent { ClarityTheme { ClarityApp(container) } }
    }

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ClarityDatabase::class.java).build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun f1_boasVindasLevamAHojeVazia() {
        start(onboarded = false)
        compose.onNodeWithText("Organize com leveza").assertIsDisplayed()
        compose.onNodeWithText("Começar").performClick()
        compose.waitUntil(5_000) { compose.onAllNodes(hasText("Nada para hoje")).fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(5_000) { onboarding.done.value }
    }

    @Test
    fun f3_criarSoComTituloEF4_concluirGanha10Xp() {
        start(onboarded = true)
        compose.waitUntil(5_000) { compose.onAllNodes(hasText("Nova tarefa")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Total de pontos de experiência: 0").assertIsDisplayed()

        // RNF-01: abrir, digitar, salvar.
        compose.onNodeWithText("Nova tarefa").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Estudar ViewModel")
        compose.onNodeWithText("Salvar").performClick()

        compose.waitUntil(5_000) { compose.onAllNodes(hasText("Estudar ViewModel")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Tarefa criada").assertIsDisplayed()
        compose.onNodeWithText("1 tarefa para hoje", substring = true).assertIsDisplayed()

        // Concluir pela caixa (descrição = título da tarefa).
        compose.onNodeWithContentDescription("Estudar ViewModel").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodes(hasContentDescription("Total de pontos de experiência: 10")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Tarefa concluída · +10 XP").assertIsDisplayed()
        compose.onNodeWithText("Tudo feito por hoje").assertIsDisplayed()

        runBlocking { assertEquals(10, container.tasks.observeTotalXp().first()) }
    }

    @Test
    fun rf004_tituloVazioMostraErro() {
        start(onboarded = true)
        compose.waitUntil(5_000) { compose.onAllNodes(hasText("Nova tarefa")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Nova tarefa").performClick()
        compose.onNodeWithText("Salvar").performClick()
        compose.onNodeWithText("Informe um título").assertIsDisplayed()
        runBlocking { assertEquals(0, container.tasks.observeTasks().first().size) }
    }

    @Test
    fun inbox_capturarSoComTituloEOrganizarComDataDeHojeLevaAHoje() {
        start(onboarded = true)
        compose.waitUntil(5_000) { compose.onAllNodes(hasText("Nova tarefa")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Tarefas").performClick()
        compose.onNodeWithText("Inbox").performClick()

        // INB RF-002: só o título; campo vazio desabilita o botão.
        compose.onNodeWithContentDescription("Adicionar ao Inbox").assertIsNotEnabled()
        compose.onNodeWithText("Inbox vazio").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performTextInput("Perguntar ao professor")
        compose.onNodeWithContentDescription("Adicionar ao Inbox").performClick()
        compose.waitUntil(5_000) { compose.onAllNodes(hasText("Inbox (1)")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Perguntar ao professor").assertIsDisplayed()

        // INB RF-003: área, data opcional e confirmação explícita.
        compose.onNodeWithText("Organizar").performClick()
        compose.waitUntil(5_000) { compose.onAllNodes(hasText("Organizar tarefa")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Sem prazo").assertIsDisplayed()
        compose.onNodeWithText("Organizar").performClick()
        compose.onNodeWithText("Escolha uma área").assertIsDisplayed()
        compose.onNodeWithText("Faculdade").performClick()
        compose.onNodeWithText("Definir").performClick()
        compose.onNodeWithText("OK").performClick()
        compose.onNodeWithText("Organizar").performClick()

        compose.waitUntil(5_000) { compose.onAllNodes(hasText("Inbox vazio")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Tarefa organizada").assertIsDisplayed()

        // INB RF-006: com data de hoje aparece em Hoje; um único registro.
        compose.onNodeWithText("Hoje").performClick()
        compose.waitUntil(5_000) { compose.onAllNodes(hasText("Perguntar ao professor")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("1 tarefa para hoje", substring = true).assertIsDisplayed()
        runBlocking {
            val all = container.tasks.observeTasks().first()
            assertEquals(1, all.size)
            assertTrue(all.single().organized)
        }
    }

    private fun hasText(text: String) = androidx.compose.ui.test.hasText(text, substring = false)
}
