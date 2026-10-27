package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "admin_logs")
data class AdminLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionType: String, // "DELETE_VIDEO", "RESTORE_VIDEO", "PERMANENT_DELETE", "IMPORT_VIDEO", "CLEAN_STORAGE"
    val videoTitle: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
