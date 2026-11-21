package com.orvix.smarthome

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE relays ADD COLUMN topic TEXT NOT NULL DEFAULT ''")
    }
}
