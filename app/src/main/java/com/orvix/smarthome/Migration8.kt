package com.orvix.smarthome

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rooms ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
    }
}
