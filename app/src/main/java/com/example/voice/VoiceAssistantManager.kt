package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAssistantManager(private val context: Context) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _speechText = MutableStateFlow("")
    val speechText: StateFlow<String> = _speechText.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private var onResultListener: ((String) -> Unit)? = null

    init {
        try {
            initializeSpeechRecognizer()
        } catch (e: Exception) {
            Log.e("VoiceAssistantManager", "Failed to compile SpeechRecognizer: ${e.localizedMessage}")
            speechRecognizer = null
            _errorState.value = "Голосовое распознавание недоступно на вашем устройстве. Пожалуйста, используйте текстовый ввод."
        }
        try {
            initializeTextToSpeech()
        } catch (e: Exception) {
            Log.e("VoiceAssistantManager", "Failed to construct TextToSpeech: ${e.localizedMessage}")
            textToSpeech = null
            isTtsInitialized = false
        }
    }

    private fun initializeSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceAssistantManager)
                }
            } else {
                _errorState.value = "Голосовое распознавание недоступно на вашем устройстве. Пожалуйста, используйте текстовый ввод."
            }
        } catch (e: Exception) {
            Log.e("VoiceAssistantManager", "Error in createSpeechRecognizer: ${e.localizedMessage}")
            speechRecognizer = null
            _errorState.value = "Голосовое распознавание недоступно на вашем устройстве. Пожалуйста, используйте текстовый ввод."
        }
    }

    private fun initializeTextToSpeech() {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("VoiceAssistantManager", "Error in TextToSpeech instantiation: ${e.localizedMessage}")
            textToSpeech = null
            isTtsInitialized = false
        }
    }

    fun startListening(onResult: (String) -> Unit) {
        onResultListener = onResult
        _errorState.value = null
        _speechText.value = "Запуск микрофона..."
        
        if (speechRecognizer == null) {
            initializeSpeechRecognizer()
        }

        val recognizer = speechRecognizer
        if (recognizer == null) {
            _errorState.value = "Служба распознавания речи недоступна."
            _isListening.value = false
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU") // standard Russian input locale
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            _isListening.value = true
            recognizer.startListening(intent)
        } catch (e: Exception) {
            _errorState.value = "Не удалось запустить микрофон: ${e.localizedMessage}"
            _isListening.value = false
        }
    }

    fun stopListening() {
        _isListening.value = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceAssistantManager", "Stop listening error: ${e.localizedMessage}")
        }
    }

    fun speak(text: String) {
        if (isTtsInitialized && textToSpeech != null) {
            try {
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VoiceCommanderTTS")
            } catch (e: Exception) {
                Log.e("VoiceAssistantManager", "Speak error: ${e.localizedMessage}")
            }
        }
    }

    // TTS initialization callback
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("ru"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // fall back to default locale
                textToSpeech?.setLanguage(Locale.getDefault())
            }
            isTtsInitialized = true
        } else {
            Log.e("VoiceAssistantManager", "TTS initialization failed")
        }
    }

    // Speech Recognition Listener callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _speechText.value = "Говорите..."
    }

    override fun onBeginningOfSpeech() {
        _speechText.value = "Распознаю голос..."
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Can be used for soundwaves animations
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _isListening.value = false
    }

    override fun onError(error: Int) {
        _isListening.value = false
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Ошибка аудиозаписи."
            SpeechRecognizer.ERROR_CLIENT -> "Внутренняя ошибка приложения."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Отсутствуют необходимые разрешения на запись звука."
            SpeechRecognizer.ERROR_NETWORK -> "Ошибка сети. Убедитесь в наличии интернета."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Таймаут подключения к сети."
            SpeechRecognizer.ERROR_NO_MATCH -> "Голос не распознан. Попробуйте сказать четче."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Служба распознавания речи занята."
            SpeechRecognizer.ERROR_SERVER -> "Ошибка сервера распознавания речи."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Голос не обнаружен. Попробуйте еще раз."
            else -> "Неизвестная ошибка распознавания речи (код: $error)."
        }
        _errorState.value = message
        _speechText.value = ""
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val recognizedText = matches?.firstOrNull()
        if (!recognizedText.isNullOrBlank()) {
            _speechText.value = recognizedText
            onResultListener?.invoke(recognizedText)
        } else {
            _errorState.value = "Голос не распознан."
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val recognizedText = matches?.firstOrNull()
        if (!recognizedText.isNullOrBlank()) {
            _speechText.value = "$recognizedText..."
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun onDestroy() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("VoiceAssistantManager", "Destroy SpeechRecognizer error: ${e.localizedMessage}")
        }
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceAssistantManager", "Shutdown TTS error: ${e.localizedMessage}")
        }
    }
}
