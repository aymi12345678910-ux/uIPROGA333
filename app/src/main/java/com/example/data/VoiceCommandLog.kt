package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_command_logs")
data class VoiceCommandLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val inputPhrase: String,
    val matchedCommand: String,
    val replyText: String,
    val status: String, // "SUCCESS", "FAILED", "UNRECOGNIZED", "PENDING"
    val timestamp: Long = System.currentTimeMillis()
)
