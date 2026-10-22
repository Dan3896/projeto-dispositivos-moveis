package com.example.upcampusplus.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2: o Inbox deixa de ter tabela própria e passa a ser o estado de organização da tarefa.
 *
 * - `task` ganha `organized` e passa a aceitar área e data nulas (o SQLite exige recriar a tabela).
 * - Tarefas existentes ficam organizadas, com os mesmos ids.
 * - Capturas Sem categoria, Aprendizado e Ideia viram tarefas não organizadas, com o texto
 *   (até 100 caracteres) como título, a área que tinham e a data da captura. Nada se perde.
 * - Capturas classificadas como Tarefa já têm a tarefa criada e não geram outra.
 * - O contador do AUTOINCREMENT é preservado: um id removido nunca volta, e o XP já ganho
 *   com ele (xp_entry.task_id, sem FK) não bloqueia uma tarefa nova (T-X5).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `task_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, `area_id` INTEGER, `priority` INTEGER NOT NULL DEFAULT 1, `due_date` TEXT, " +
                "`organized` INTEGER NOT NULL DEFAULT 1, `completed_at` INTEGER, `completed_on` TEXT, " +
                "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, " +
                "FOREIGN KEY(`area_id`) REFERENCES `area`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )",
        )
        db.execSQL(
            "INSERT INTO task_new (id, title, area_id, priority, due_date, organized, completed_at, completed_on, " +
                "created_at, updated_at) SELECT id, title, area_id, priority, due_date, 1, completed_at, " +
                "completed_on, created_at, updated_at FROM task",
        )
        // Próximo id acima de tudo que já existiu: tarefas removidas e registros de XP.
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = 'task_new'")
        db.execSQL(
            "INSERT INTO sqlite_sequence (name, seq) SELECT 'task_new', MAX(" +
                "COALESCE((SELECT seq FROM sqlite_sequence WHERE name = 'task'), 0), " +
                "COALESCE((SELECT MAX(id) FROM task), 0), " +
                "COALESCE((SELECT MAX(task_id) FROM xp_entry), 0))",
        )
        db.execSQL(
            "INSERT INTO task_new (title, area_id, priority, due_date, organized, created_at, updated_at) " +
                "SELECT substr(trim(text), 1, 100), area_id, 1, NULL, 0, created_at, created_at FROM inbox_item " +
                "WHERE category IS NULL OR category <> 'TASK' ORDER BY created_at, id",
        )
        db.execSQL("DROP TABLE inbox_item")
        db.execSQL("DROP TABLE task")
        db.execSQL("ALTER TABLE task_new RENAME TO task")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_area_id` ON `task` (`area_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_due_date` ON `task` (`due_date`)")
    }
}
