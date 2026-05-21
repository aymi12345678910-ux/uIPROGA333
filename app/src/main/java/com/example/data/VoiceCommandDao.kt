package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceCommandDao {
    @Query("SELECT * FROM voice_command_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<VoiceCommandLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VoiceCommandLog): Long

    @Query("DELETE FROM voice_command_logs")
    suspend fun clearLogs()
}
