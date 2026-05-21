package com.example.voice

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CommandType(val id: String, val ruName: String, val examples: String) {
    FLASHLIGHT_ON("FLASHLIGHT_ON", "Включить фонарик", "«включить фонарик», «фонарик вкл»"),
    FLASHLIGHT_OFF("FLASHLIGHT_OFF", "Выключить фонарик", "«выключить фонарик», «фонарик выкл»"),
    WIFI_SETTINGS("WIFI_SETTINGS", "Управление Wi-Fi", "«включить вайфай», «вай фай», «wi-fi»"),
    BLUETOOTH_SETTINGS("BLUETOOTH_SETTINGS", "Управление Bluetooth", "«блютуз вкл», «настройки блютуза»"),
    VOLUME_UP("VOLUME_UP", "Увеличить звук", "«сделай громче», «прибавить звук»"),
    VOLUME_DOWN("VOLUME_DOWN", "Уменьшить звук", "«сделай тише», «убавить звук»"),
    VOLUME_MUTE("VOLUME_MUTE", "Выключить звук", "«без звука», «отключить звук»"),
    VOLUME_UNMUTE("VOLUME_UNMUTE", "Включить звук", "«вернуть звук», «обычный режим»"),
    OPEN_TELEGRAM("OPEN_TELEGRAM", "Открыть Telegram", "«открой телеграм», «запусти телеграм»"),
    OPEN_WHATSAPP("OPEN_WHATSAPP", "Открыть WhatsApp", "«открой ватсап», «запусти whatsapp»"),
    OPEN_YOUTUBE("OPEN_YOUTUBE", "Открыть YouTube", "«открой ютуб», «запусти youtube»"),
    OPEN_CAMERA("OPEN_CAMERA", "Открыть Камеру", "«сделай фото», «открой камеру»"),
    OPEN_CALCULATOR("OPEN_CALCULATOR", "Открыть Калькулятор", "«открой калькулятор»"),
    OPEN_CALENDAR("OPEN_CALENDAR", "Открыть Календарь", "«открой календарь»"),
    OPEN_SETTINGS("OPEN_SETTINGS", "Открыть Настройки", "«открыть настройки», «настройки»"),
    OPEN_BATTERY("OPEN_BATTERY", "Настройки батареи", "«настройки батареи», «аккумулятор»"),
    OPEN_DISPLAY("OPEN_DISPLAY", "Настройки экрана", "«настройки экрана», «яркость экрана»"),
    OPEN_SOUND("OPEN_SOUND", "Настройки звука", "«настройки звука», «звуковые настройки»"),
    CHECK_TIME("CHECK_TIME", "Сколько времени", "«сколько времени», «время», «часы»"),
    CHECK_DATE("CHECK_DATE", "Какое сегодня число", "«какое сегодня число», «какая дата», «число»"),
    CHECK_BATTERY("CHECK_BATTERY", "Проверить заряд батареи", "«заряд батареи», «сколько процентов»"),
    WEB_SEARCH("WEB_SEARCH", "Поиск в Интернете", "«найти в гугле [запрос]», «поиск [запрос]»"),
    UNRECOGNIZED("UNRECOGNIZED", "Неизвестная команда", "")
}

data class ExecutionResult(
    val commandType: CommandType,
    val replyText: String,
    val status: String // "SUCCESS", "FAILED"
)

object VoiceCommandProcessor {

    fun processCommand(context: Context, rawText: String): ExecutionResult {
        val text = rawText.lowercase().trim()

        return when {
            // 1. Flashlight ON
            text.contains("фонарик") && (text.contains("включ") || text.contains("вкл")) -> {
                toggleFlashlight(context, true)
            }

            // 2. Flashlight OFF
            text.contains("фонарик") && (text.contains("выключ") || text.contains("выкл") || text.contains("отключ")) -> {
                toggleFlashlight(context, false)
            }

            // 3. Wi-Fi
            (text.contains("вайфай") || text.contains("вай фай") || text.contains("wifi") || text.contains("wi-fi")) && 
            (text.contains("включ") || text.contains("выключ") || text.contains("отключ") || text.contains("открыт") || text.contains("настрой")) -> {
                openWifiSettings(context)
            }

            // 4. Bluetooth
            (text.contains("блютус") || text.contains("блютуз") || text.contains("bluetooth")) &&
            (text.contains("включ") || text.contains("выключ") || text.contains("отключ") || text.contains("открыт") || text.contains("настрой")) -> {
                openBluetoothSettings(context)
            }

            // 5. Volume Up
            text.contains("громче") || text.contains("прибавить звук") || text.contains("увеличить громкость") || text.contains("добавить звук") -> {
                adjustVolume(context, raise = true)
            }

            // 6. Volume Down
            text.contains("тише") || text.contains("убавить звук") || text.contains("уменьшить громкость") || text.contains("тише звук") -> {
                adjustVolume(context, raise = false)
            }

            // 7. Volume Mute
            text.contains("без звука") || text.contains("выключить звук") || text.contains("отключить звук") || text.contains("беззвучный") -> {
                setMuteState(context, mute = true)
            }

            // 8. Volume Unmute
            text.contains("включить звук") || text.contains("вернуть звук") || text.contains("обычный режим") || text.contains("обычный звук") -> {
                setMuteState(context, mute = false)
            }

            // 9. Telegram
            text.contains("телеграм") || text.contains("telegram") -> {
                launchAppOrOfferStore(context, "org.telegram.messenger", "Telegram")
            }

            // 10. WhatsApp
            text.contains("ватсап") || text.contains("whatsapp") || text.contains("вацап") -> {
                launchAppOrOfferStore(context, "com.whatsapp", "WhatsApp")
            }

            // 11. YouTube
            text.contains("ютуб") || text.contains("youtube") -> {
                launchAppOrOfferStore(context, "com.google.android.youtube", "YouTube")
            }

            // 12. Camera
            text.contains("камер") || text.contains("сделать фото") || text.contains("сделай фото") || text.contains("снять видео") -> {
                openSystemIntent(context, MediaStore.ACTION_IMAGE_CAPTURE, CommandType.OPEN_CAMERA, "Открываю камеру")
            }

            // 13. Calculator
            text.contains("калькулятор") || text.contains("calculator") -> {
                launchCalculator(context)
            }

            // 14. Calendar
            text.contains("календарь") || text.contains("calendar") -> {
                openCalendar(context)
            }

            // 15. Settings Battery
            text.contains("батарея") || text.contains("аккумулятор") || text.contains("настройки батареи") -> {
                if (text.contains("заряд") || text.contains("процент") || text.contains("сколько")) {
                    checkBatteryLevel(context)
                } else {
                    openSystemIntent(context, Intent.ACTION_POWER_USAGE_SUMMARY, CommandType.OPEN_BATTERY, "Открываю настройки батареи")
                }
            }

            // 16. Settings Screen
            text.contains("дисплей") || text.contains("настройки экрана") || text.contains("яркость") -> {
                openSystemIntent(context, Settings.ACTION_DISPLAY_SETTINGS, CommandType.OPEN_DISPLAY, "Открываю настройки экрана")
            }

            // 17. Settings Sound
            text.contains("настройки звука") || text.contains("звуков") || text.contains("настройки аудио") -> {
                openSystemIntent(context, Settings.ACTION_SOUND_SETTINGS, CommandType.OPEN_SOUND, "Открываю настройки звука")
            }

            // 18. Settings General
            text.contains("настройки") || text.contains("параметры") -> {
                openSystemIntent(context, Settings.ACTION_SETTINGS, CommandType.OPEN_SETTINGS, "Открываю общие настройки")
            }

            // 19. Check Battery (specific)
            text.contains("заряд") || text.contains("зарядка") || text.contains("сколько батареи") || text.contains("сколько процентов") -> {
                checkBatteryLevel(context)
            }

            // 20. Check Time
            text.contains("время") || text.contains("сколько времени") || text.contains("который час") || text.contains("часы") -> {
                getTimeResponse()
            }

            // 21. Check Date
            text.contains("дата") || text.contains("число") || text.contains("какой сегодня день") || text.contains("какое сегодня число") -> {
                getDateResponse()
            }

            // 22. Web Search
            text.contains("найти") || text.contains("найди") || text.contains("поиск") || text.contains("гугл") || text.contains("google") -> {
                performWebSearch(context, rawText)
            }

            else -> {
                ExecutionResult(
                    CommandType.UNRECOGNIZED,
                    "Команда не распознана. Пожалуйста, попробуйте другую команду из списка доступных.",
                    "FAILED"
                )
            }
        }
    }

    private fun toggleFlashlight(context: Context, status: Boolean): ExecutionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.getOrNull(0)
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, status)
                val reply = if (status) "Фонарик успешно включен" else "Фонарик успешно выключен"
                ExecutionResult(
                    if (status) CommandType.FLASHLIGHT_ON else CommandType.FLASHLIGHT_OFF,
                    reply,
                    "SUCCESS"
                )
            } else {
                ExecutionResult(
                    if (status) CommandType.FLASHLIGHT_ON else CommandType.FLASHLIGHT_OFF,
                    "На этом устройстве не обнаружена фотовспышка",
                    "FAILED"
                )
            }
        } catch (e: Exception) {
            ExecutionResult(
                if (status) CommandType.FLASHLIGHT_ON else CommandType.FLASHLIGHT_OFF,
                "Ошибка управления фонариком: ${e.localizedMessage}",
                "FAILED"
            )
        }
    }

    private fun openWifiSettings(context: Context): ExecutionResult {
        return try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_WIFI)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ExecutionResult(CommandType.WIFI_SETTINGS, "Открываю панель управления беспроводной сетью Wi-Fi", "SUCCESS")
        } catch (e: Exception) {
            try {
                // Fallback to standard settings
                val intentFallback = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intentFallback)
                ExecutionResult(CommandType.WIFI_SETTINGS, "Открываю системные настройки Wi-Fi", "SUCCESS")
            } catch (ex: Exception) {
                ExecutionResult(CommandType.WIFI_SETTINGS, "Не удалось открыть настройки Wi-Fi: ${ex.localizedMessage}", "FAILED")
            }
        }
    }

    private fun openBluetoothSettings(context: Context): ExecutionResult {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ExecutionResult(CommandType.BLUETOOTH_SETTINGS, "Открываю настройки Bluetooth", "SUCCESS")
        } catch (e: Exception) {
            ExecutionResult(CommandType.BLUETOOTH_SETTINGS, "Не удалось открыть настройки Bluetooth: ${e.localizedMessage}", "FAILED")
        }
    }

    private fun adjustVolume(context: Context, raise: Boolean): ExecutionResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val direction = if (raise) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            val reply = if (raise) "Громкость звука увеличена" else "Громкость звука уменьшена"
            ExecutionResult(
                if (raise) CommandType.VOLUME_UP else CommandType.VOLUME_DOWN,
                reply,
                "SUCCESS"
            )
        } catch (e: Exception) {
            ExecutionResult(
                if (raise) CommandType.VOLUME_UP else CommandType.VOLUME_DOWN,
                "Не удалось отрегулировать звук: ${e.localizedMessage}",
                "FAILED"
            )
        }
    }

    private fun setMuteState(context: Context, mute: Boolean): ExecutionResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (mute) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
                    audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
                    audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
                }
                ExecutionResult(CommandType.VOLUME_MUTE, "Звук временно отключен. Включен беззвучный режим.", "SUCCESS")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI)
                    audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
                    audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
                }
                ExecutionResult(CommandType.VOLUME_UNMUTE, "Звук успешно включен в стандартный режим.", "SUCCESS")
            }
        } catch (e: Exception) {
            ExecutionResult(
                if (mute) CommandType.VOLUME_MUTE else CommandType.VOLUME_UNMUTE,
                "Не удалось настроить беззвучный режим: ${e.localizedMessage}",
                "FAILED"
            )
        }
    }

    private fun launchAppOrOfferStore(context: Context, packageName: String, appName: String): ExecutionResult {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        val command = when(appName) {
            "Telegram" -> CommandType.OPEN_TELEGRAM
            "WhatsApp" -> CommandType.OPEN_WHATSAPP
            "YouTube" -> CommandType.OPEN_YOUTUBE
            else -> CommandType.OPEN_SETTINGS
        }
        return if (launchIntent != null) {
            try {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ExecutionResult(command, "Открываю приложение $appName на вашем устройстве", "SUCCESS")
            } catch (e: Exception) {
                ExecutionResult(command, "Ошибка запуска $appName: ${e.localizedMessage}", "FAILED")
            }
        } else {
            try {
                // Pre-configure play store intent
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(marketIntent)
                ExecutionResult(command, "$appName не установлен, открываю Play Маркет для загрузки", "SUCCESS")
            } catch (e: Exception) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    ExecutionResult(command, "$appName отсутствует, открываю веб-версию Google Play", "SUCCESS")
                } catch (ex: Exception) {
                    ExecutionResult(command, "$appName не установлен на вашем мобильном устройстве", "FAILED")
                }
            }
        }
    }

    private fun launchCalculator(context: Context): ExecutionResult {
        // System calculators can have various package names depending on manufacturer
        val calculatorPackages = listOf(
            "com.android.calculator2",
            "com.sec.android.app.popupcalculator",
            "com.google.android.calculator",
            "com.miui.calculator",
            "com.huawei.android.totemweather" // some huawei packages
        )
        for (pkg in calculatorPackages) {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return ExecutionResult(CommandType.OPEN_CALCULATOR, "Запускаю системный калькулятор", "SUCCESS")
            }
        }
        // Fallback: search general intent or launch with general ACTION
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ExecutionResult(CommandType.OPEN_CALCULATOR, "Открываю калькулятор", "SUCCESS")
        } catch (e: Exception) {
            ExecutionResult(CommandType.OPEN_CALCULATOR, "Не найден калькулятор на устройстве. Пожалуйста, установите его.", "FAILED")
        }
    }

    private fun openCalendar(context: Context): ExecutionResult {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALENDAR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ExecutionResult(CommandType.OPEN_CALENDAR, "Открываю календарь", "SUCCESS")
        } catch (e: Exception) {
            try {
                val secIntent = Intent(Intent.ACTION_VIEW).apply {
                    setData(Uri.parse("content://com.android.calendar/time/"))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(secIntent)
                ExecutionResult(CommandType.OPEN_CALENDAR, "Открываю календарь", "SUCCESS")
            } catch (ex: Exception) {
                ExecutionResult(CommandType.OPEN_CALENDAR, "Ошибка открытия календаря: ${ex.localizedMessage}", "FAILED")
            }
        }
    }

    private fun openSystemIntent(context: Context, action: String, commandType: CommandType, successReply: String): ExecutionResult {
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ExecutionResult(commandType, successReply, "SUCCESS")
        } catch (e: Exception) {
            ExecutionResult(commandType, "Не удалось выполнить команду: ${e.localizedMessage}", "FAILED")
        }
    }

    private fun checkBatteryLevel(context: Context): ExecutionResult {
        return try {
            val batteryStatusIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else -1
            
            if (batteryPct >=0) {
                ExecutionResult(CommandType.CHECK_BATTERY, "Текущий уровень заряда батареи составляет $batteryPct процентов.", "SUCCESS")
            } else {
                ExecutionResult(CommandType.CHECK_BATTERY, "Не удалось получить уровень заряда батареи.", "FAILED")
            }
        } catch (e: Exception) {
            ExecutionResult(CommandType.CHECK_BATTERY, "Ошибка датчика батареи: ${e.localizedMessage}", "FAILED")
        }
    }

    private fun getTimeResponse(): ExecutionResult {
        val sdf = SimpleDateFormat("HH:mm", Locale("ru"))
        val currentTime = sdf.format(Date())
        return ExecutionResult(CommandType.CHECK_TIME, "Текущее время: $currentTime.", "SUCCESS")
    }

    private fun getDateResponse(): ExecutionResult {
        val sdf = SimpleDateFormat("d MMMM yyyy года", Locale("ru"))
        val currentDate = sdf.format(Date())
        return ExecutionResult(CommandType.CHECK_DATE, "Сегодня $currentDate.", "SUCCESS")
    }

    private fun performWebSearch(context: Context, rawText: String): ExecutionResult {
        return try {
            val cleanWords = listOf(
                "найти в интернете", "найти в гугле", "найди в интернете", 
                "найди в гугле", "поиск", "найти", "найди", "гугл", "google"
            )
            var query = rawText.lowercase()
            for (word in cleanWords) {
                if (query.startsWith(word)) {
                    query = query.substring(word.length).trim()
                    break
                } else if (query.contains(word)) {
                    query = query.replace(word, "").trim()
                }
            }
            if (query.isEmpty()) {
                query = rawText
            }

            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ExecutionResult(CommandType.WEB_SEARCH, "Ищу в Интернете по запросу: «$query»", "SUCCESS")
        } catch (e: Exception) {
            try {
                val searchQuery = rawText.replace("поиск", "").trim()
                val url = "https://www.google.com/search?q=" + Uri.encode(searchQuery)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                ExecutionResult(CommandType.WEB_SEARCH, "Ищу в браузере по запросу: «$searchQuery»", "SUCCESS")
            } catch (ex: Exception) {
                ExecutionResult(CommandType.WEB_SEARCH, "Не удалось выполнить веб-поиск: ${ex.localizedMessage}", "FAILED")
            }
        }
    }
}
