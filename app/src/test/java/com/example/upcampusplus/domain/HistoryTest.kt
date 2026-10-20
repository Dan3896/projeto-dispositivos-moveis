package com.example.upcampusplus.domain

import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.testing.SaoPaulo
import com.example.upcampusplus.testing.Today
import com.example.upcampusplus.testing.at
import com.example.upcampusplus.testing.inboxTask
import com.example.upcampusplus.testing.task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryTest {
    private val yesterday = Today.minusDays(1)

    private val tasks = listOf(
        task(1, title = "Ler capítulo de Room", areaId = Area.FACULDADE, completedAt = at(Today, 10, 20), completedOn = Today),
        task(2, title = "Enviar relatório", areaId = Area.TRABALHO, completedAt = at(yesterday, 17, 0), completedOn = yesterday),
        task(3, title = "Pendente não entra", areaId = Area.PESSOAL),
        task(4, title = "Revisar Room", areaId = Area.FACULDADE, completedAt = at(Today, 8, 0), completedOn = Today),
        inboxTask(5, title = "Room no Inbox não entra"),
    )

    @Test
    fun `RF-025 conclusoes agrupadas por dia, mais recente primeiro`() {
        val view = buildHistory(tasks, SaoPaulo, query = "")
        assertEquals(listOf(Today, yesterday), view.days.map { it.date })
        val todayEntries = view.days.first().entries
        assertEquals(listOf("task-1", "task-4"), todayEntries.map { it.key })
        assertEquals("Ler capítulo de Room", todayEntries.first().text)
        assertEquals(Area.FACULDADE, todayEntries.first().areaId)
        assertEquals(3, view.totalEntries)
    }

    @Test
    fun `RF-025 tarefa pendente ou no Inbox nao aparece`() {
        val keys = buildHistory(tasks, SaoPaulo, "").days.flatMap { it.entries }.map { it.key }
        assertTrue("task-3" !in keys && "task-5" !in keys)
    }

    @Test
    fun `T-R1 reabrir a tarefa tira o registro de conclusao e os contadores acompanham`() {
        val reopened = tasks.map { if (it.id == 1L) it.copy(completedAt = null, completedOn = null) else it }
        val before = buildHistory(tasks, SaoPaulo, "")
        val after = buildHistory(reopened, SaoPaulo, "")
        assertEquals(2, before.areaCounts[Area.FACULDADE])
        assertEquals(1, after.areaCounts[Area.FACULDADE])
        assertTrue(after.days.flatMap { it.entries }.none { it.key == "task-1" })
    }

    @Test
    fun `RN-18 contadores por area em todo o periodo`() {
        val counts = buildHistory(tasks, SaoPaulo, "").areaCounts
        assertEquals(mapOf(Area.FACULDADE to 2, Area.TRABALHO to 1), counts)
    }

    @Test
    fun `T-R2 busca sem diferenciar maiusculas e sem mudar contadores`() {
        val all = buildHistory(tasks, SaoPaulo, "")
        val view = buildHistory(tasks, SaoPaulo, "  room ")
        assertEquals(listOf("task-1", "task-4"), view.days.flatMap { it.entries }.map { it.key })
        assertEquals(all.areaCounts, view.areaCounts)
        assertEquals("room", view.query)
    }

    @Test
    fun `RF-027 termo sem resultados e termo vazio`() {
        assertTrue(buildHistory(tasks, SaoPaulo, "xyz").days.isEmpty())
        assertEquals(2, buildHistory(tasks, SaoPaulo, "   ").days.size)
    }
}
