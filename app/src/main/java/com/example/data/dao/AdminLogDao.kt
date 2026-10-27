package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AdminLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminLogDao {

    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<AdminLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AdminLog)

    @Query("DELETE FROM admin_logs")
    suspend fun clearLogs()
}
