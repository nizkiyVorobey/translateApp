package com.example.ttanslateapp.data.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ttanslateapp.util.TRANSLATED_WORDS_TABLE_NAME

fun migrateFrom1To2(database: SupportSQLiteDatabase) {
    database.execSQL("ALTER TABLE $TRANSLATED_WORDS_TABLE_NAME ADD COLUMN created_at INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
    database.execSQL("ALTER TABLE $TRANSLATED_WORDS_TABLE_NAME ADD COLUMN updated_at INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
}