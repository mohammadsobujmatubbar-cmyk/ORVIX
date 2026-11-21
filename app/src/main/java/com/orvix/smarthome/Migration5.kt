package com.orvix.smarthome

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `mqtt_connections` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deviceId` INTEGER NOT NULL, `name` TEXT NOT NULL, `clientId` TEXT NOT NULL, `broker` TEXT NOT NULL, `port` INTEGER NOT NULL, `tls` INTEGER NOT NULL, `username` TEXT NOT NULL, FOREIGN KEY(`deviceId`) REFERENCES `devices`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_mqtt_connections_deviceId` ON `mqtt_connections` (`deviceId`)")
    }
}
