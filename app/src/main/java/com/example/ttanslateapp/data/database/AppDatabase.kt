package com.example.ttanslateapp.data.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ttanslateapp.data.database.migration.migrateFrom1To2
import com.example.ttanslateapp.data.database.migration.migrateFrom2To3
import com.example.ttanslateapp.data.model.HintDb
import com.example.ttanslateapp.data.model.PotentialExamAnswerDb
import com.example.ttanslateapp.data.model.TranslateDb
import com.example.ttanslateapp.data.model.WordInfoDb

@Database(
    version = 3,
    entities = [
        WordInfoDb::class,
        TranslateDb::class,
        HintDb::class,
        PotentialExamAnswerDb::class,
    ],
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
                migrateFrom1To2(database)
            }
        }

        private val migration_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                migrateFrom2To3(database)
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
                    .addMigrations(migration_1_2, migration_2_3)
                    .build()

                INSTANCE = db
                return db
            }
        }
    }
}
