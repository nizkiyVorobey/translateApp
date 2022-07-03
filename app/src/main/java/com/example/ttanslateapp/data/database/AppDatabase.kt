package com.example.ttanslateapp.data.database

import android.app.Application
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ttanslateapp.data.model.ExamAnswerVariantDb
import com.example.ttanslateapp.data.model.TranslatedWordDb
import com.example.ttanslateapp.util.TRANSLATED_WORDS_TABLE_NAME


@Database(
    version = 2,
    entities = [TranslatedWordDb::class, ExamAnswerVariantDb::class],
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun translatedWordDao(): TranslatedWordDao
    abstract fun examWordAnswerDao(): ExamWordAnswerDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        private var LOCK = Any()
        private const val DB_NAME = "dictionary"

        private val migration_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE $TRANSLATED_WORDS_TABLE_NAME ADD COLUMN created_at INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                database.execSQL("ALTER TABLE $TRANSLATED_WORDS_TABLE_NAME ADD COLUMN updated_at INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            }
        }

        fun getInstance(application: Application): AppDatabase {
            INSTANCE?.let {
                return it
            }

            synchronized(LOCK) {
                INSTANCE?.let {
                    return it
                }

                val db = Room.databaseBuilder(
                    application,
                    AppDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration()
                    .addMigrations(migration_1_2)
                    .build()

                INSTANCE = db
                return db
            }
        }
    }
}
