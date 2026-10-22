package com.example.upcampusplus.domain

import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.model.Priority
import com.example.upcampusplus.testing.Today
import com.example.upcampusplus.testing.at
import com.example.upcampusplus.testing.inboxTask
import com.example.upcampusplus.testing.task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskRulesTest {
    private val yesterday = Today.minusDays(1)
    private val in3Days = Today.plusDays(3)

    // Exemplo de RF-011 / RF-012: 14/10 Alta, 15/10 Alta, 15/10 Média, 18/10; duas concluídas hoje.
    private val example = listOf(
        task(1, due = in3Days, priority = Priority.HIGH),
        task(2, due = Today, priority = Priority.MEDIUM),
        task(3, due = Today, priority = Priority.HIGH),
        task(4, due = yesterday, priority = Priority.HIGH),
        task(5, due = Today, completedAt = at(Today, 9, 0), completedOn = Today),
        task(6, due = yesterday, completedAt = at(Today, 9, 30), completedOn = Today),
        task(7, due = yesterday, completedAt = at(yesterday, 18, 0), completedOn = yesterday),
    )

    @Test
    fun `T-H1 lista de hoje por prioridade, depois prazo, sem futuras nem concluidas`() {
        val summary = summarizeToday(example, Today)
        assertEquals(listOf(4L, 3L, 2L), summary.dueToday.map { it.id })
        assertTrue(summary.dueToday.first().isOverdue(Today))
        assertFalse(summary.dueToday[1].isOverdue(Today))
    }

    @Test
    fun `T-H2 contadores do exemplo sao 3, 1 e 2`() {
        val summary = summarizeToday(example, Today)
        assertEquals(3, summary.dueTodayCount)
        assertEquals(1, summary.overdueCount)
        assertEquals(2, summary.completedTodayCount)
    }

    @Test
    fun `T-D2 tarefa de ontem concluida hoje sai de Para hoje e Atrasadas e conta em Concluidas`() {
        val before = summarizeToday(listOf(task(1, due = yesterday)), Today)
        val after = summarizeToday(listOf(task(1, due = yesterday, completedAt = at(Today, 8, 0), completedOn = Today)), Today)
        assertEquals(1 to 1, before.dueTodayCount to before.overdueCount)
        assertEquals(Triple(0, 0, 1), Triple(after.dueTodayCount, after.overdueCount, after.completedTodayCount))
    }

    @Test
    fun `T-D4 mudar prazo de hoje para amanha tira a tarefa de Hoje`() {
        val summary = summarizeToday(listOf(task(1, due = Today.plusDays(1))), Today)
        assertTrue(summary.dueToday.isEmpty())
        assertEquals(0, summary.dueTodayCount)
    }

    @Test
    fun `RN-07 concluida nunca e atrasada`() {
        assertFalse(task(1, due = yesterday, completedAt = at(Today, 8, 0), completedOn = Today).isOverdue(Today))
        assertTrue(task(1, due = yesterday).isOverdue(Today))
        assertFalse(task(1, due = Today).isOverdue(Today))
    }

    @Test
    fun `RN-12 empate em prioridade e prazo segue a ordem de criacao`() {
        val tasks = listOf(task(2, createdAt = at(Today, 9, 0)), task(1, createdAt = at(Today, 8, 0)))
        assertEquals(listOf(1L, 2L), summarizeToday(tasks, Today).dueToday.map { it.id })
    }

    @Test
    fun `RN-12 tarefas pendentes por prazo e depois prioridade, concluidas da mais recente`() {
        val sections = splitSections(example, areaId = null)
        assertEquals(listOf(4L, 3L, 2L, 1L), sections.pending.map { it.id })
        assertEquals(listOf(6L, 5L, 7L), sections.completed.map { it.id })
    }

    @Test
    fun `RF-015 filtro por area vale para as duas secoes`() {
        val tasks = listOf(
            task(1, areaId = Area.FACULDADE),
            task(2, areaId = Area.TRABALHO),
            task(3, areaId = Area.FACULDADE, completedAt = at(Today, 8, 0), completedOn = Today),
            task(4, areaId = Area.TRABALHO, completedAt = at(Today, 8, 0), completedOn = Today),
        )
        val sections = splitSections(tasks, Area.FACULDADE)
        assertEquals(listOf(1L), sections.pending.map { it.id })
        assertEquals(listOf(3L), sections.completed.map { it.id })
        assertTrue(splitSections(tasks, Area.PESSOAL).let { it.pending.isEmpty() && it.completed.isEmpty() })
    }

    @Test
    fun `INB RF-006 Hoje ignora tarefa sem data e tarefa no Inbox, mesmo com data de hoje`() {
        val tasks = listOf(
            task(1, due = Today),
            task(2, due = null),
            task(3, due = Today, organized = false),
            task(4, due = yesterday, organized = false),
        )
        val summary = summarizeToday(tasks, Today)
        assertEquals(listOf(1L), summary.dueToday.map { it.id })
        assertEquals(0, summary.overdueCount)
        assertFalse("data preservada no Inbox não conta como atraso", tasks[3].isOverdue(Today))
    }

    @Test
    fun `INB RF-004 Todas mostra organizadas e nao organizadas, cada uma uma vez, sem prazo por ultimo`() {
        val tasks = listOf(
            task(1, due = null),
            inboxTask(2),
            task(3, due = in3Days),
            task(4, due = Today, priority = Priority.LOW),
        )
        val sections = splitSections(tasks, areaId = null)
        assertEquals(listOf(4L, 3L, 1L, 2L), sections.pending.map { it.id })
        assertEquals(tasks.size, sections.pending.distinct().size)
    }

    @Test
    fun `INB RF-005 tarefa sem area nao entra em nenhum filtro de area`() {
        val tasks = listOf(inboxTask(1), task(2, areaId = Area.TRABALHO), task(3, areaId = Area.TRABALHO, organized = false))
        assertEquals(listOf(2L, 3L), splitSections(tasks, Area.TRABALHO).pending.map { it.id })
        Area.DEFAULTS.forEach { area -> assertTrue(splitSections(tasks, area.id).pending.none { it.id == 1L }) }
    }

    @Test
    fun `INB RF-001 Inbox so com nao organizadas, mais recentes primeiro`() {
        val tasks = listOf(
            inboxTask(1, createdAt = at(Today, 8, 0)),
            task(2),
            inboxTask(3, createdAt = at(Today, 9, 0)),
            task(4, completedAt = at(Today, 9, 0), completedOn = Today),
        )
        assertEquals(listOf(3L, 1L), inboxTasks(tasks).map { it.id })
    }

    @Test
    fun `T-D3 prazo e data de calendario e nao muda com o fuso`() {
        val t = task(1, due = Today)
        // Hoje em Tóquio já é dia 16 quando em São Paulo ainda é 15 à noite.
        val clockTokyo = com.example.upcampusplus.testing.FakeClock(at(Today, 22, 0), java.time.ZoneId.of("Asia/Tokyo"))
        assertEquals(Today, t.dueDate)
        assertEquals(Today.plusDays(1), clockTokyo.today())
        assertTrue(t.isOverdue(clockTokyo.today()))
    }
}
