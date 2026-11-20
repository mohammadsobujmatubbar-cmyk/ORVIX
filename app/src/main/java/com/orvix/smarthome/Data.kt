package com.orvix.smarthome

import android.content.Context
import androidx.room.*
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
    val gatewayId: Long? = null
)

@Dao
interface OrvixDao {
    @Query("SELECT * FROM rooms ORDER BY id")
    fun observeRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM devices ORDER BY id")
    fun observeDevices(): Flow<List<DeviceEntity>>

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
}

@Database(entities = [RoomEntity::class, DeviceEntity::class], version = 1, exportSchema = false)
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

    suspend fun addRoom(name: String) { dao.insertRoom(RoomEntity(name = name)) }
    suspend fun renameRoom(room: RoomEntity, name: String) { dao.updateRoom(room.copy(name = name)) }
    suspend fun deleteRoom(room: RoomEntity) { dao.deleteRoom(room) }
    suspend fun addDevice(device: DeviceEntity) { dao.insertDevice(device) }
    suspend fun updateDevice(device: DeviceEntity) { dao.updateDevice(device) }
    suspend fun deleteDevice(device: DeviceEntity) { dao.deleteDevice(device) }
}
