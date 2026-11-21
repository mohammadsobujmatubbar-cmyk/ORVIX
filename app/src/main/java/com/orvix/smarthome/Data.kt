package com.orvix.smarthome

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roomId")]
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val name: String,
    val type: String,
    val host: String,
    val port: Int,
    val gatewayId: Long? = null,
    val checkMessage: String = ""
)

@Entity(
    tableName = "relays",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deviceId")]
)
data class RelayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: Long,
    val name: String,
    val onMessage: String,
    val offMessage: String,
    val isOn: Boolean = false,
    val icon: String = "light"
)

@Entity(
    tableName = "sensors",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deviceId")]
)
data class SensorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: Long,
    val name: String,
    val type: String,
    val config: String
)

@Dao
interface OrvixDao {
    @Query("SELECT * FROM rooms ORDER BY id")
    fun observeRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM devices ORDER BY id")
    fun observeDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM relays ORDER BY id")
    fun observeRelays(): Flow<List<RelayEntity>>

    @Query("SELECT * FROM sensors ORDER BY id")
    fun observeSensors(): Flow<List<SensorEntity>>

    @Insert
    suspend fun insertRoom(room: RoomEntity): Long

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Delete
    suspend fun deleteRoom(room: RoomEntity)

    @Insert
    suspend fun insertDevice(device: DeviceEntity): Long

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Delete
    suspend fun deleteDevice(device: DeviceEntity)

    @Insert
    suspend fun insertRelays(relays: List<RelayEntity>)

    @Update
    suspend fun updateRelay(relay: RelayEntity)

    @Delete
    suspend fun deleteRelay(relay: RelayEntity)

    @Query("UPDATE relays SET isOn = :on WHERE deviceId = :deviceId")
    suspend fun setAllRelays(deviceId: Long, on: Boolean)

    @Insert
    suspend fun insertSensor(sensor: SensorEntity)

    @Update
    suspend fun updateSensor(sensor: SensorEntity)

    @Delete
    suspend fun deleteSensor(sensor: SensorEntity)

    @Query("SELECT * FROM mqtt_connections ORDER BY id")
    fun observeMqtt(): Flow<List<MqttConnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMqtt(conn: MqttConnEntity)

    @Query("DELETE FROM mqtt_connections WHERE deviceId = :deviceId")
    suspend fun deleteMqtt(deviceId: Long)
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `relays` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deviceId` INTEGER NOT NULL, `name` TEXT NOT NULL, `onMessage` TEXT NOT NULL, `offMessage` TEXT NOT NULL, `isOn` INTEGER NOT NULL, FOREIGN KEY(`deviceId`) REFERENCES `devices`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_relays_deviceId` ON `relays` (`deviceId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `sensors` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deviceId` INTEGER NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `config` TEXT NOT NULL, FOREIGN KEY(`deviceId`) REFERENCES `devices`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sensors_deviceId` ON `sensors` (`deviceId`)")
    }
}

@Database(
    entities = [RoomEntity::class, DeviceEntity::class, RelayEntity::class, SensorEntity::class, MqttConnEntity::class],
    version = 5,
    exportSchema = false
)
abstract class OrvixDatabase : RoomDatabase() {
    abstract fun dao(): OrvixDao

    companion object {
        @Volatile
        private var instance: OrvixDatabase? = null

        fun get(context: Context): OrvixDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): OrvixDatabase =
            Room.databaseBuilder(context.applicationContext, OrvixDatabase::class.java, "orvix.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        listOf("Bedroom", "Living Room", "Kitchen", "Dining Room").forEach {
                            db.execSQL("INSERT INTO rooms (name) VALUES ('" + it + "')")
                        }
                    }
                })
                .build()
    }
}

class OrvixRepository(private val dao: OrvixDao) {
    val rooms: Flow<List<RoomEntity>> = dao.observeRooms()
    val devices: Flow<List<DeviceEntity>> = dao.observeDevices()
    val relays: Flow<List<RelayEntity>> = dao.observeRelays()
    val sensors: Flow<List<SensorEntity>> = dao.observeSensors()
    val mqtt: Flow<List<MqttConnEntity>> = dao.observeMqtt()

    suspend fun addRoom(name: String) { dao.insertRoom(RoomEntity(name = name)) }
    suspend fun renameRoom(room: RoomEntity, name: String) { dao.updateRoom(room.copy(name = name)) }
    suspend fun deleteRoom(room: RoomEntity) { dao.deleteRoom(room) }
    suspend fun addDevice(device: DeviceEntity) { dao.insertDevice(device) }
    suspend fun updateDevice(device: DeviceEntity) { dao.updateDevice(device) }
    suspend fun deleteDevice(device: DeviceEntity) { dao.deleteDevice(device) }
    suspend fun addRelays(list: List<RelayEntity>) { dao.insertRelays(list) }
    suspend fun updateRelay(relay: RelayEntity) { dao.updateRelay(relay) }
    suspend fun deleteRelay(relay: RelayEntity) { dao.deleteRelay(relay) }
    suspend fun setAllRelays(deviceId: Long, on: Boolean) { dao.setAllRelays(deviceId, on) }
    suspend fun addSensor(sensor: SensorEntity) { dao.insertSensor(sensor) }
    suspend fun updateSensor(sensor: SensorEntity) { dao.updateSensor(sensor) }
    suspend fun deleteSensor(sensor: SensorEntity) { dao.deleteSensor(sensor) }
    suspend fun saveMqtt(c: MqttConnEntity) { dao.upsertMqtt(c) }
    suspend fun deleteMqtt(deviceId: Long) { dao.deleteMqtt(deviceId) }
    suspend fun addMqttDevice(device: DeviceEntity, conn: MqttConnEntity): Long {
        val id = dao.insertDevice(device)
        dao.upsertMqtt(conn.copy(deviceId = id))
        return id
    }
}

@Entity(
    tableName = "mqtt_connections",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deviceId"], unique = true)]
)
data class MqttConnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: Long,
    val name: String,
    val clientId: String,
    val broker: String,
    val port: Int,
    val tls: Boolean,
    val username: String
)
