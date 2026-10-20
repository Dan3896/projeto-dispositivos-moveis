package com.example.upcampusplus.data

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.upcampusplus.data.db.ClarityDatabase
import com.example.upcampusplus.data.db.TaskEntity
import com.example.upcampusplus.data.db.XpEntryEntity
import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.model.Priority
import com.example.upcampusplus.domain.model.TaskDraft
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

/** Testes de banco com Room em memória (seção 15): transações, unicidade do XP e atomicidade. */
@RunWith(AndroidJUnit4::class)
class ClarityDatabaseTest {
    private lateinit var db: ClarityDatabase
    private lateinit var areas: RoomAreaRepository
    private lateinit var tasks: RoomTaskRepository

    private val today = LocalDate.of(2026, 10, 15)
    private val now = Instant.parse("2026-10-15T13:00:00Z")
    private fun draft(title: String = "Estudar ViewModel", area: Int = Area.FACULDADE) =
        TaskDraft(title, area, Priority.MEDIUM, today)

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ClarityDatabase::class.java).build()
        areas = RoomAreaRepository(db)
        tasks = RoomTaskRepository(db)
        areas.ensureDefaults()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun rf002_areasIniciaisSemDuplicar() = runTest {
        areas.ensureDefaults()
        areas.ensureDefaults()
        assertEquals(listOf("Faculdade", "Trabalho", "Projetos", "Pessoal"), areas.observeAreas().first().map { it.name })
    }

    @Test
    fun tx1_concluirTarefaNuncaConcluidaDa10Xp() = runTest {
        val id = tasks.createTask(draft(), now)
        val result = tasks.completeTask(id, now, today)
        assertTrue(result.completed)
        assertEquals(10, result.xpGained)
        assertEquals(10, tasks.observeTotalXp().first())
        assertEquals(1, db.xpDao().countForTask(id))
        assertEquals(today, tasks.getTask(id)?.completedOn)
    }

    @Test
    fun tx2_concluirEmParaleloGeraUmRegistro() = runTest {
        val id = tasks.createTask(draft(), now)
        val results = (1..8).map { async { tasks.completeTask(id, now, today) } }.awaitAll()
        assertEquals(1, results.count { it.completed })
        assertEquals(10, results.sumOf { it.xpGained })
        assertEquals(10, tasks.observeTotalXp().first())
    }

    @Test
    fun tx3_concluirReabrirConcluirSoma10() = runTest {
        val id = tasks.createTask(draft(), now)
        tasks.completeTask(id, now, today)
        assertTrue(tasks.reopenTask(id, now))
        val reopened = tasks.getTask(id)!!
        assertNull(reopened.completedAt)
        assertNull(reopened.completedOn)
        assertEquals(10, tasks.observeTotalXp().first())

        val second = tasks.completeTask(id, now, today)
        assertTrue(second.completed)
        assertEquals(0, second.xpGained)
        assertEquals(10, tasks.observeTotalXp().first())
    }

    @Test
    fun tx4_removerTarefaConcluidaMantemXp() = runTest {
        val id = tasks.createTask(draft(), now)
        tasks.completeTask(id, now, today)
        assertTrue(tasks.deleteTask(id))
        assertNull(tasks.getTask(id))
        assertEquals(10, tasks.observeTotalXp().first())
    }

    @Test
    fun tx5_idNuncaReutilizadoENovaTarefaGanhaXp() = runTest {
        tasks.createTask(draft("a"), now)
        val last = tasks.createTask(draft("b"), now)
        tasks.completeTask(last, now, today)
        tasks.deleteTask(last)

        val fresh = tasks.createTask(draft("c"), now)
        assertNotEquals(last, fresh)
        assertTrue(fresh > last)
        assertEquals(10, tasks.completeTask(fresh, now, today).xpGained)
        assertEquals(20, tasks.observeTotalXp().first())
    }

    @Test
    fun rnf09_indiceUnicoImpedeSegundoRegistroDeXp() = runTest {
        db.xpDao().insertIfAbsent(XpEntryEntity(taskId = 99, amount = 10, earnedAt = 0))
        assertEquals(-1L, db.xpDao().insertIfAbsent(XpEntryEntity(taskId = 99, amount = 10, earnedAt = 0)))
        assertEquals(10, tasks.observeTotalXp().first())
    }

    @Test
    fun rn09_editarNaoMudaEstadoNemXp() = runTest {
        val id = tasks.createTask(draft(), now)
        tasks.completeTask(id, now, today)
        val later = now.plusSeconds(60)
        assertTrue(tasks.updateTask(id, TaskDraft("Novo título", Area.TRABALHO, Priority.HIGH, today.plusDays(3)), later))
        val edited = tasks.getTask(id)!!
        assertEquals("Novo título", edited.title)
        assertEquals(Area.TRABALHO, edited.areaId)
        assertEquals(Priority.HIGH, edited.priority)
        assertEquals(today.plusDays(3), edited.dueDate)
        assertEquals(now, edited.completedAt)
        assertEquals(today, edited.completedOn)
        assertEquals(10, tasks.observeTotalXp().first())
    }

    @Test
    fun areaInexistenteEBloqueadaPelaChaveEstrangeira() = runTest {
        try {
            db.taskDao().insert(TaskEntity(title = "x", areaId = 42, priority = 1, dueDate = "2026-10-15", organized = true, createdAt = 0, updatedAt = 0))
            fail("FK deveria bloquear")
        } catch (expected: SQLiteConstraintException) {
            // ok
        }
    }

    @Test
    fun inb002_capturaSoComTituloFicaNoInbox() = runTest {
        val id = tasks.captureTask("Perguntar ao professor", now)
        val task = tasks.getTask(id)!!
        assertFalse(task.organized)
        assertNull(task.areaId)
        assertNull(task.dueDate)
        assertEquals(now, task.createdAt)
        assertEquals(listOf(id), tasks.observeInbox().first().map { it.id })
    }

    @Test
    fun inb003_organizarTiraDoInboxSemCriarOutroRegistro() = runTest {
        val id = tasks.captureTask("Entregar relatório", now)
        assertTrue(tasks.organizeTask(id, TaskDraft("Entregar relatório", Area.TRABALHO, Priority.HIGH, null), now))
        assertTrue(tasks.observeInbox().first().isEmpty())
        val all = tasks.observeTasks().first()
        assertEquals(listOf(id), all.map { it.id })
        assertTrue(all.single().organized)
        assertEquals(Area.TRABALHO, all.single().areaId)
        assertNull(all.single().dueDate)
        assertFalse("segunda organização não muda nada", tasks.organizeTask(id, draft(), now))
    }

    @Test
    fun rn20_tarefaNoInboxNaoConcluiNemGanhaXp() = runTest {
        val id = tasks.captureTask("Ideia", now)
        val result = tasks.completeTask(id, now, today)
        assertFalse(result.completed)
        assertNull(tasks.getTask(id)!!.completedAt)
        assertEquals(0, tasks.observeTotalXp().first())
    }

    @Test
    fun rn22_moverParaOInboxPreservaDadosEXp() = runTest {
        val id = tasks.createTask(TaskDraft("Ler", Area.PROJETOS, Priority.LOW, today.plusDays(2)), now)
        val draft = TaskDraft("Ler", Area.PROJETOS, Priority.LOW, today.plusDays(2))
        assertTrue(tasks.moveToInbox(id, draft, now))
        val moved = tasks.getTask(id)!!
        assertFalse(moved.organized)
        assertEquals(Area.PROJETOS, moved.areaId)
        assertEquals(today.plusDays(2), moved.dueDate)

        // Concluída não volta ao Inbox, e o XP fica como está.
        tasks.organizeTask(id, draft, now)
        tasks.completeTask(id, now, today)
        assertFalse(tasks.moveToInbox(id, draft, now))
        assertTrue(tasks.getTask(id)!!.organized)
        assertEquals(10, tasks.observeTotalXp().first())
    }

    @Test
    fun inb007_removerDoInboxNaoDeixaRegistro() = runTest {
        val id = tasks.captureTask("Apagar", now)
        assertTrue(tasks.deleteTask(id))
        assertNull(tasks.getTask(id))
        assertTrue(tasks.observeTasks().first().isEmpty())
    }

    @Test
    fun inboxComCapturasRecentesPrimeiro() = runTest {
        tasks.captureTask("antiga", now)
        tasks.captureTask("nova", now.plusSeconds(60))
        tasks.createTask(draft("organizada"), now.plusSeconds(120))
        assertEquals(listOf("nova", "antiga"), tasks.observeInbox().first().map { it.title })
    }
}
