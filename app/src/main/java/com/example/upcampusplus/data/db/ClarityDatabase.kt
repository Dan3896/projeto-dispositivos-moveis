package com.example.upcampusplus.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Versão 2 do banco: o Inbox é o estado de organização da tarefa ([MIGRATION_1_2]). Mudanças de
 * esquema entram por migração explícita; não há fallbackToDestructiveMigration (RNF-10). Os
 * esquemas exportados ficam em app/schemas.
 */
@Database(
    entities = [AreaEntity::class, TaskEntity::class, XpEntryEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class ClarityDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao
    abstract fun taskDao(): TaskDao
    abstract fun xpDao(): XpDao

    companion object {
        const val NAME = "clarity.db"

        val MIGRATIONS = arrayOf(MIGRATION_1_2)

        fun create(context: Context): ClarityDatabase =
            Room.databaseBuilder(context, ClarityDatabase::class.java, NAME)
                .addMigrations(*MIGRATIONS)
                .build()
    }
}
