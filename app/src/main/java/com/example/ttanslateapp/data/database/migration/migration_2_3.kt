package com.example.ttanslateapp.data.database.migration

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ttanslateapp.domain.model.migrate_2_3.OldHints
import com.example.ttanslateapp.domain.model.migrate_2_3.OldTranslate
import com.example.ttanslateapp.util.TRANSLATED_WORDS_HINTS
import com.example.ttanslateapp.util.TRANSLATED_WORDS_TABLE_NAME
import com.example.ttanslateapp.util.TRANSLATED_WORDS_TRANSLATIONS
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

fun migrateFrom2To3(database: SupportSQLiteDatabase) {
    database.execSQL(
        "CREATE TABLE IF NOT EXISTS $TRANSLATED_WORDS_TRANSLATIONS (" +
                "'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "'created_at' INTEGER NOT NULL, " +
                "'updated_at' INTEGER  NOT NULL, " +
                "'value' TEXT NOT NULL, " +
                "'is_hidden' INTEGER NOT NULL, " +
                "'word_id' INTEGER NOT NULL, " +
                "FOREIGN KEY('word_id') REFERENCES $TRANSLATED_WORDS_TABLE_NAME('id') ON DELETE CASCADE ON UPDATE NO ACTION )"
    )

    database.execSQL(
        "CREATE TABLE IF NOT EXISTS $TRANSLATED_WORDS_HINTS (" +
                "'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "'created_at' INTEGER NOT NULL, " +
                "'updated_at' INTEGER  NOT NULL, " +
                "'value' TEXT NOT NULL, " +
                "'word_id' INTEGER NOT NULL, " +
                "FOREIGN KEY('word_id') REFERENCES $TRANSLATED_WORDS_TABLE_NAME('id') ON DELETE CASCADE ON UPDATE NO ACTION )"
    )

    // fill new tables (translates and hints)
    val c: Cursor = database.query("SELECT * FROM $TRANSLATED_WORDS_TABLE_NAME", null)
    if (c.moveToFirst()) {
        do {
            val wordId: Long = c.getLong(0)
            val translates: String = c.getString(3)
            val hints: String = c.getString(8)
            val translateListType: Type =
                object : TypeToken<List<OldTranslate>?>() {}.type
            val translateList: List<OldTranslate> =
                Gson().fromJson(translates, translateListType)

            val hintsListType: Type = object : TypeToken<List<OldHints>?>() {}.type
            val hintsList: List<OldHints> = Gson().fromJson(hints, hintsListType)


            translateList.forEach {
                val contentValues = ContentValues()
                contentValues.put("value", it.value)
                contentValues.put("created_at", it.createdAt)
                contentValues.put("updated_at", it.updatedAt)
                contentValues.put("is_hidden", it.isHidden)
                contentValues.put("word_id", wordId)
                database.insert(
                    TRANSLATED_WORDS_TRANSLATIONS,
                    SQLiteDatabase.CONFLICT_REPLACE,
                    contentValues
                )
            }

            hintsList.forEach {
                val contentValues = ContentValues()
                contentValues.put("value", it.value)
                contentValues.put("created_at", it.createdAt)
                contentValues.put("updated_at", it.updatedAt)
                contentValues.put("word_id", wordId)
                database.insert(
                    TRANSLATED_WORDS_HINTS,
                    SQLiteDatabase.CONFLICT_REPLACE,
                    contentValues
                )
            }

        } while (c.moveToNext())
    }
    c.close()


    database.execSQL(
        "CREATE TABLE IF NOT EXISTS 'TRANSLATED_WORDS_TABLE_NAME_temp' (" +
                "'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "'priority' INTEGER NOT NULL, " +
                "'value' TEXT NOT NULL, " +
                "'description' TEXT NOT NULL, " +
                "'sound' TEXT, " +
                "'lang_from' TEXT NOT NULL, " +
                "'lang_to' TEXT NOT NULL, " +
                "'transcription' TEXT NOT NULL, " +
                "'created_at' INTEGER NOT NULL, " +
                "'updated_at' INTEGER  NOT NULL)"
    )

    // Copy the data
    database.execSQL(
        "INSERT INTO 'TRANSLATED_WORDS_TABLE_NAME_temp' (id, priority, value, description, sound, lang_from, lang_to, transcription, created_at, updated_at) " +
                "SELECT id, priority, value, description,sound, langFrom, langTo, transcription, created_at, updated_at " +
                "FROM $TRANSLATED_WORDS_TABLE_NAME"
    )

    // Remove old table
    database.execSQL("DROP TABLE $TRANSLATED_WORDS_TABLE_NAME")
    // Change name of table to correct one
    database.execSQL("ALTER TABLE 'TRANSLATED_WORDS_TABLE_NAME_temp' RENAME TO $TRANSLATED_WORDS_TABLE_NAME")

}