package com.example.upcampusplus.domain

import com.example.upcampusplus.testing.FakeClock
import com.example.upcampusplus.testing.FakeTaskRepository
import com.example.upcampusplus.testing.Today
import com.example.upcampusplus.testing.at
import com.example.upcampusplus.testing.task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.plus
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DayChangeTest {
    @Test
    fun `T-D1 as 23h59 com pendente de hoje, passar 2 minutos marca atrasada e zera Concluidas`() = runTest {
        val clock = FakeClock()
        clock.set(Today, 23, 59)
        val repo = FakeTaskRepository(
            listOf(
                task(1, due = Today),
                task(2, due = Today, completedAt = at(Today, 20, 0), completedOn = Today),
            ),
        )
        val seen = mutableListOf<TodayOverview>()
        val job = GetTodaySummary(repo, clock)().onEach { seen += it }
            .launchIn(this + UnconfinedTestDispatcher(testScheduler))

        val before = seen.last()
        assertEquals(Today, before.today)
        assertEquals(0, before.summary.overdueCount)
        assertEquals(1, before.summary.completedTodayCount)

        clock.set(Today.plusDays(1), 0, 1)
        advanceTimeBy(2 * 60_000L)

        val after = seen.last()
        assertEquals(Today.plusDays(1), after.today)
        assertEquals(1, after.summary.overdueCount)
        assertEquals(0, after.summary.completedTodayCount)
        assertTrue(after.summary.dueToday.single().isOverdue(after.today))
        job.cancel()
    }

    @Test
    fun `RF-020 a data e conferida em menos de 1 minuto`() {
        assertTrue(DAY_CHECK_INTERVAL_MS < 60_000L)
    }
}
