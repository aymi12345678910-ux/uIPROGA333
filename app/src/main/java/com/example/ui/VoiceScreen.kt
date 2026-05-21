package com.example.ui

import android.Manifest
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeMute
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.VoiceCommandLog
import com.example.voice.CommandType
import com.example.voice.VoiceViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val isListening by viewModel.assistantManager.isListening.collectAsState()
    val speechText by viewModel.assistantManager.speechText.collectAsState()
    val errorState by viewModel.assistantManager.errorState.collectAsState()
    val manualText by viewModel.manualInputText.collectAsState()
    val commandResponse by viewModel.currentCommandResponse.collectAsState()
    val logs by viewModel.commandLogs.collectAsState()

    // Permissions state management
    val recordAudioPermission = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )

    // Dynamic scale animation for the recording pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Команды", "История & Статистика")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Cyber Voice Module (MICROPHONE & LIVE RESPONSE)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Interactive micro pulsator circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isListening) {
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                } else {
                                    listOf(
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                }
                            )
                        )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                            .border(
                                width = 3.dp,
                                color = if (isListening) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable(
                                onClickLabel = stringResource(R.string.mic_button_desc)
                            ) {
                                if (recordAudioPermission.status.isGranted) {
                                    if (isListening) {
                                        viewModel.stopListening()
                                    } else {
                                        viewModel.startListening()
                                    }
                                } else {
                                    recordAudioPermission.launchPermissionRequest()
                                }
                            }
                            .testTag("microphone_pulse_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Rounded.Mic else Icons.Rounded.MicNone,
                            contentDescription = "Voice Input Mic Trigger",
                            modifier = Modifier.size(36.dp),
                            tint = if (isListening) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transcribed Voice display
                SurfaceResponseBox(
                    isListening = isListening,
                    speechText = speechText,
                    errorState = errorState,
                    commandResponse = commandResponse,
                    hasPermission = recordAudioPermission.status.isGranted
                )

                // Request Permissions Callout
                if (!recordAudioPermission.status.isGranted) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = "Предупреждение",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.permission_required),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = { recordAudioPermission.launchPermissionRequest() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(stringResource(R.string.permission_grant), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Smart Manual Entry Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualText,
                onValueChange = { viewModel.setManualInput(it) },
                placeholder = { Text("Введите команду текстом...", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("manual_command_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    if (manualText.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.executeCommand(manualText) },
                            modifier = Modifier.testTag("send_command_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Send,
                                contentDescription = "Выполнить команду",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab selection (Commands vs History/Stats)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        // 3. Tab Contents
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "tabContent"
        ) { targetTab ->
            when (targetTab) {
                0 -> CommandsGuideList(onCommandClick = { text ->
                    viewModel.executeCommand(text)
                })
                1 -> HistoryAndStatsTab(
                    logs = logs,
                    onClearHistory = { viewModel.clearHistory() }
                )
            }
        }
    }
}

@Composable
fun SurfaceResponseBox(
    isListening: Boolean,
    speechText: String,
    errorState: String?,
    commandResponse: String?,
    hasPermission: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when {
                    isListening -> "Статус: прослушивание эфира"
                    errorState != null -> "Статус: ошибка распознавания"
                    commandResponse != null -> "Команда выполнена успешно"
                    else -> "Статус: готов к приему команд"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isListening -> MaterialTheme.colorScheme.primary
                    errorState != null -> MaterialTheme.colorScheme.error
                    commandResponse != null -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = when {
                    isListening -> speechText
                    errorState != null -> errorState
                    commandResponse != null -> commandResponse
                    !hasPermission -> "Пожалуйста, предоставьте необходимые разрешения микрофону."
                    else -> stringResource(R.string.idle_status)
                },
                label = "responseAnimation"
            ) { text ->
                Text(
                    text = text ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        errorState != null -> MaterialTheme.colorScheme.error
                        isListening -> MaterialTheme.colorScheme.primary
                        commandResponse != null -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CommandsGuideList(onCommandClick: (String) -> Unit) {
    val systemCommands = listOf(
        CommandItemInfo(CommandType.FLASHLIGHT_ON, Icons.Rounded.FlashlightOn, "включить фонарик"),
        CommandItemInfo(CommandType.FLASHLIGHT_OFF, Icons.Rounded.FlashlightOn, "выключить фонарик"),
        CommandItemInfo(CommandType.WIFI_SETTINGS, Icons.Rounded.Wifi, "включить вайфай"),
        CommandItemInfo(CommandType.BLUETOOTH_SETTINGS, Icons.Rounded.Bluetooth, "включить блютуз"),
        CommandItemInfo(CommandType.VOLUME_UP, Icons.Rounded.VolumeUp, "сделай громче"),
        CommandItemInfo(CommandType.VOLUME_DOWN, Icons.Rounded.VolumeUp, "сделай тише"),
        CommandItemInfo(CommandType.VOLUME_MUTE, Icons.Rounded.VolumeMute, "без звука"),
        CommandItemInfo(CommandType.VOLUME_UNMUTE, Icons.Rounded.VolumeMute, "включить звук"),
        CommandItemInfo(CommandType.OPEN_TELEGRAM, Icons.Rounded.Launch, "открой телеграм"),
        CommandItemInfo(CommandType.OPEN_WHATSAPP, Icons.Rounded.Launch, "открой ватсап"),
        CommandItemInfo(CommandType.OPEN_YOUTUBE, Icons.Rounded.Launch, "открой ютуб"),
        CommandItemInfo(CommandType.OPEN_CAMERA, Icons.Rounded.Camera, "открой камеру"),
        CommandItemInfo(CommandType.OPEN_CALCULATOR, Icons.Rounded.Calculate, "открой калькулятор"),
        CommandItemInfo(CommandType.OPEN_CALENDAR, Icons.Rounded.CalendarToday, "открой календарь"),
        CommandItemInfo(CommandType.OPEN_SETTINGS, Icons.Rounded.Settings, "открыть настройки"),
        CommandItemInfo(CommandType.OPEN_BATTERY, Icons.Rounded.BatteryChargingFull, "настройки батареи"),
        CommandItemInfo(CommandType.OPEN_DISPLAY, Icons.Rounded.DisplaySettings, "настройки экрана"),
        CommandItemInfo(CommandType.OPEN_SOUND, Icons.Rounded.Settings, "настройки звука"),
        CommandItemInfo(CommandType.CHECK_TIME, Icons.Rounded.History, "время"),
        CommandItemInfo(CommandType.CHECK_DATE, Icons.Rounded.CalendarToday, "число"),
        CommandItemInfo(CommandType.CHECK_BATTERY, Icons.Rounded.BatteryChargingFull, "заряд батареи"),
        CommandItemInfo(CommandType.WEB_SEARCH, Icons.Rounded.Search, "найти в интернете котики")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Доступные команды управления (20+)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Нажмите на любую карточку ниже, чтобы быстро сымитировать запуск команды и посмотреть ее действие вживую!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(systemCommands) { item ->
            GuideCommandCard(item = item, onClick = { onCommandClick(item.activationPhrase) })
        }
        
        item {
            Spacer(modifier = Modifier.height(160.dp)) // Floating visual padding at the bottom of standard scroll
        }
    }
}

@Composable
fun GuideCommandCard(item: CommandItemInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("guide_command_card_${item.commandType.id.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.commandType.ruName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Что сказать: ${item.commandType.examples}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowRight,
                    contentDescription = "Выполнить",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun HistoryAndStatsTab(
    logs: List<VoiceCommandLog>,
    onClearHistory: () -> Unit
) {
    val totalCount = logs.size
    val successCount = logs.count { it.status == "SUCCESS" }
    val failCount = totalCount - successCount
    val batteryAudits = logs.count { it.matchedCommand == CommandType.CHECK_BATTERY.id }
    val flashlights = logs.count { it.matchedCommand == CommandType.FLASHLIGHT_ON.id || it.matchedCommand == CommandType.FLASHLIGHT_OFF.id }
    val volumeUpdates = logs.count { it.matchedCommand.startsWith("VOLUME") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Metrics Dashboard Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Сводная статистика работы",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBlock("Всего команд", totalCount.toString(), Modifier.weight(1f))
                        MetricBlock("Успешно", "$successCount / $totalCount", Modifier.weight(1f))
                        MetricBlock("Сбои", failCount.toString(), Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Основные категории управления:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Фонарик: $flashlights раз | • Батарея: $batteryAudits раз | • Звук: $volumeUpdates раз",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "История выполнения голосовых инструкций",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("clear_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = "Очистить историю",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Журнал пуст",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Список выполненных голосовых запросов будет отображаться здесь.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 240.dp)
                        )
                    }
                }
            }
        } else {
            items(logs) { log ->
                HistoryRowCard(log = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(160.dp))
        }
    }
}

@Composable
fun MetricBlock(title: String, score: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = score,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HistoryRowCard(log: VoiceCommandLog) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeFormatted = formatter.format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = if (log.status == "SUCCESS") Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                contentDescription = null,
                tint = if (log.status == "SUCCESS") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Запрос: «${log.inputPhrase}»",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ответ: ${log.replyText}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = timeFormatted,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class CommandItemInfo(
    val commandType: CommandType,
    val icon: ImageVector,
    val activationPhrase: String
)
