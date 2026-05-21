package com.example.data

import kotlinx.coroutines.flow.Flow

class VoiceCommandRepository(private val dao: VoiceCommandDao) {
    val allLogs: Flow<List<VoiceCommandLog>> = dao.getAllLogs()

    suspend fun insertLog(log: VoiceCommandLog): Long {
        return dao.insertLog(log)
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }
}
