package com.example.upcampusplus.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.upcampusplus.data.db.ClarityDatabase
import com.example.upcampusplus.data.db.MIGRATION_1_2
import com.example.upcampusplus.domain.model.Area
import com.example.upcampusplus.domain.model.Priority
import com.example.upcampusplus.domain.model.TaskDraft
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

/** T-P3: instalar a versão nova sobre a anterior mantém os dados (RNF-10). */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val testDb = "migration-test.db"
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(instrumentation, ClarityDatabase::class.java)

    @Test
    fun migra1Para2ComInboxComoEstadoDaTarefa() = runTest {
        helper.createDatabase(testDb, 1).use { db ->
            (1..4).forEach { db.execSQL("INSERT INTO area (id, name, sort_order) VALUES ($it, 'Área $it', $it)") }
            db.execSQL(
                "INSERT INTO task (id, title, area_id, priority, due_date, completed_at, completed_on, created_at, updated_at) " +
                    "VALUES (1, 'Concluída', 1, 0, '2026-10-14', 500, '2026-10-14', 100, 500)",
            )
            db.execSQL(
                "INSERT INTO task (id, title, area_id, priority, due_date, created_at, updated_at) " +
                    "VALUES (2, 'Pendente', 2, 1, '2026-10-15', 200, 200)",
            )
            // Tarefa 3 concluída e removida: o XP fica, o id não pode voltar.
            db.execSQL(
                "INSERT INTO task (id, title, area_id, priority, due_date, created_at, updated_at) " +
                    "VALUES (3, 'Removida', 1, 1, '2026-10-15', 300, 300)",
            )
            db.execSQL("DELETE FROM task WHERE id = 3")
            db.execSQL("INSERT INTO xp_entry (task_id, amount, earned_at) VALUES (1, 10, 500), (3, 10, 600)")
            db.execSQL("INSERT INTO inbox_item (id, text, created_at) VALUES (1, '  Ideia solta  ', 1000)")
            db.execSQL(
                "INSERT INTO inbox_item (id, text, category, area_id, created_at, classified_at) " +
                    "VALUES (2, 'Room usa SQLite', 'LEARNING', 1, 1100, 1200)",
            )
            db.execSQL(
                "INSERT INTO inbox_item (id, text, category, area_id, task_id, created_at, classified_at) " +
                    "VALUES (3, 'Pendente', 'TASK', 2, 2, 150, 200)",
            )
            db.execSQL("INSERT INTO inbox_item (id, text, created_at) VALUES (4, '${"x".repeat(280)}', 1300)")
        }

        helper.runMigrationsAndValidate(testDb, 2, true, MIGRATION_1_2).use { db ->
            db.query("SELECT id, title, area_id, due_date, organized, completed_at FROM task ORDER BY id").use { c ->
                val rows = buildList {
                    while (c.moveToNext()) {
                        add(
                            listOf(
                                c.getLong(0), c.getString(1), if (c.isNull(2)) null else c.getInt(2),
                                c.getString(3), c.getInt(4), if (c.isNull(5)) null else c.getLong(5),
                            ),
                        )
                    }
                }
                assertEquals(5, rows.size)
                assertEquals(listOf(1L, "Concluída", 1, "2026-10-14", 1, 500L), rows[0])
                assertEquals(listOf(2L, "Pendente", 2, "2026-10-15", 1, null), rows[1])
                // Capturas viram tarefas não organizadas, com ids acima do 3 removido.
                assertEquals(listOf(4L, "Ideia solta", null, null, 0, null), rows[2])
                assertEquals(listOf(5L, "Room usa SQLite", 1, null, 0, null), rows[3])
                assertEquals(6L, rows[4][0])
                assertEquals(100, (rows[4][1] as String).length)
            }
            db.query("SELECT COUNT(*) FROM sqlite_master WHERE name = 'inbox_item'").use { c ->
                c.moveToFirst()
                assertEquals(0, c.getInt(0))
            }
        }

        // O app abre o banco migrado e continua as regras de XP e ids.
        val context = instrumentation.targetContext
        val room = Room.databaseBuilder(context, ClarityDatabase::class.java, testDb)
            .addMigrations(*ClarityDatabase.MIGRATIONS)
            .build()
        try {
            val tasks = RoomTaskRepository(room)
            assertEquals(20, tasks.observeTotalXp().first())
            assertEquals(listOf(6L, 5L, 4L), tasks.observeInbox().first().map { it.id })
            val fresh = tasks.createTask(TaskDraft("Nova", Area.PESSOAL, Priority.MEDIUM, null), Instant.EPOCH)
            assertTrue(fresh > 6)
            assertEquals(10, tasks.completeTask(fresh, Instant.EPOCH, LocalDate.of(2026, 10, 15)).xpGained)
            val organized = tasks.getTask(2)!!
            assertTrue(organized.organized)
            assertFalse(organized.isCompleted)
            assertNull(tasks.getTask(3))
        } finally {
            room.close()
            context.deleteDatabase(testDb)
        }
    }
}
