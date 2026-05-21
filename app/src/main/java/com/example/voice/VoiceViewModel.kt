package com.example.voice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.VoiceCommandLog
import com.example.data.VoiceCommandRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository: VoiceCommandRepository

    val assistantManager = VoiceAssistantManager(context)

    // Observable logs from database
    val commandLogs: StateFlow<List<VoiceCommandLog>>

    private val _manualInputText = MutableStateFlow("")
    val manualInputText: StateFlow<String> = _manualInputText.asStateFlow()

    private val _currentCommandResponse = MutableStateFlow<String?>(null)
    val currentCommandResponse: StateFlow<String?> = _currentCommandResponse.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(context)
        repository = VoiceCommandRepository(database.voiceCommandDao())
        commandLogs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Set typed query text
    fun setManualInput(text: String) {
        _manualInputText.value = text
    }

    // Execute speech recognizer
    fun startListening() {
        _currentCommandResponse.value = null
        assistantManager.startListening { recognizedText ->
            executeCommand(recognizedText)
        }
    }

    fun stopListening() {
        assistantManager.stopListening()
    }

    // Main execution gateway
    fun executeCommand(text: String) {
        if (text.isBlank()) return

        _manualInputText.value = ""
        viewModelScope.launch {
            // Process the control action
            val result = VoiceCommandProcessor.processCommand(context, text)
            
            // Speak back the response
            assistantManager.speak(result.replyText)
            
            // Update UI status
            _currentCommandResponse.value = result.replyText

            // Log the action to SQLite database beautifully
            val log = VoiceCommandLog(
                inputPhrase = text,
                matchedCommand = result.commandType.id,
                replyText = result.replyText,
                status = result.status
            )
            repository.insertLog(log)
        }
    }

    // Clear voice history
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
            _currentCommandResponse.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        assistantManager.onDestroy()
    }
}
