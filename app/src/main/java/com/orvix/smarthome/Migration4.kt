package com.orvix.smarthome

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE relays ADD COLUMN icon TEXT NOT NULL DEFAULT 'light'")
    }
}
