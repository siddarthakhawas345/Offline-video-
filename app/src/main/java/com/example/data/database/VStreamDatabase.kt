package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AdminLogDao
import com.example.data.dao.VideoDao
import com.example.data.model.AdminLog
import com.example.data.model.VideoEntity

@Database(
    entities = [VideoEntity::class, AdminLog::class],
    version = 1,
    exportSchema = false
)
abstract class VStreamDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao
    abstract fun adminLogDao(): AdminLogDao

    companion object {
        @Volatile
        private var INSTANCE: VStreamDatabase? = null

        fun getDatabase(context: Context): VStreamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VStreamDatabase::class.java,
                    "vstream_offline_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
